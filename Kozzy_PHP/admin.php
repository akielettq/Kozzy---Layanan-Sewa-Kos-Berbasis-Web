<?php
// admin.php — Panel Administrator Kozzy
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

$user = userLogin();

// 1. Guard Autentikasi Khusus Admin
if (!$user) {
    setFlash("Halaman ini khusus akun admin. Silakan masuk terlebih dahulu.", "kuning");
    header("Location: login.php?balik=admin.php");
    exit;
}

if ($user['peran'] !== 'admin') {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Khusus Admin</h2>
        <p>Halaman ini hanya dapat diakses oleh akun admin. Coba masuk dengan admin@kozzy.id / admin123.</p>
        <a class="tombol hijau" href="login.php">Ganti Akun</a>
      </div>
    </section>
    <?php
    require_once __DIR__ . '/includes/footer.php';
    exit;
}

$judulHalaman = "Panel Admin — Kozzy";

// ====== 2. PROSES AKSI ADMIN (POST) ======
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        setFlash("Validasi keamanan sesi kedaluwarsa. Silakan muat ulang halaman.", "merah");
        header("Location: admin.php");
        exit;
    }
    $aksi = $_POST['aksi'] ?? '';

    // A. SETUJUI BOOKING
    if ($aksi === 'approve') {
        $idBooking = trim($_POST['id_booking'] ?? '');

        // Ambil data booking & kamar
        $stmtB = $pdo->prepare("SELECT b.*, k.status AS status_kamar FROM booking b JOIN kamar k ON b.kode_kamar = k.kode_kamar WHERE b.id_booking = ?");
        $stmtB->execute([$idBooking]);
        $b = $stmtB->fetch();

        if (!$b) {
            setFlash("Booking tidak ditemukan.", "merah");
        } elseif ($b['status'] !== 'pending') {
            setFlash("Booking ini sudah diproses sebelumnya.", "kuning");
        } elseif ($b['status_kamar'] !== 'tersedia') {
            setFlash("Kamar sudah tidak tersedia — tolak pengajuannya.", "merah");
        } else {
            try {
                $pdo->beginTransaction();

                // Hitung tanggal akhir sewa
                $stmtTgl = $pdo->prepare("SELECT DATE_ADD(?, INTERVAL ? MONTH) AS tgl_selesai");
                $stmtTgl->execute([$b['tanggal_mulai'], $b['durasi']]);
                $terisiHingga = $stmtTgl->fetchColumn();

                // Update status booking
                $pdo->prepare("UPDATE booking SET status = 'approved' WHERE id_booking = ?")->execute([$idBooking]);

                // Update status kamar menjadi terisi
                $pdo->prepare("UPDATE kamar SET status = 'terisi', terisi_hingga = ? WHERE kode_kamar = ?")->execute([$terisiHingga, $b['kode_kamar']]);

                // Catat ke tabel riwayat_verifikasi (Audit Log)
                $pdo->prepare("
                    INSERT INTO riwayat_verifikasi (keputusan, tanggal_keputusan, catatan, id_pengguna, id_booking)
                    VALUES ('approved', NOW(), 'Pengajuan booking disetujui oleh admin.', ?, ?)
                ")->execute([$user['id_pengguna'], $idBooking]);

                $pdo->commit();
                setFlash("Disetujui! Kamar " . htmlspecialchars($b['kode_kamar']) . " terisi sampai " . formatTanggal($terisiHingga) . ".", "hijau");

            } catch (Exception $e) {
                $pdo->rollBack();
                setFlash("Gagal menyetujui booking: " . $e->getMessage(), "merah");
            }
        }
        header("Location: admin.php");
        exit;
    }

    // B. TOLAK BOOKING
    if ($aksi === 'reject') {
        $idBooking = trim($_POST['id_booking'] ?? '');
        $alasan    = trim($_POST['alasan'] ?? '');

        if (strlen($alasan) < 5) {
            setFlash("Tulis alasan penolakan secara jelas (minimal 5 karakter).", "kuning");
        } else {
            try {
                $pdo->beginTransaction();

                // Update status booking
                $pdo->prepare("UPDATE booking SET status = 'rejected', alasan = ? WHERE id_booking = ?")->execute([$alasan, $idBooking]);

                // Catat ke tabel riwayat_verifikasi (Audit Log)
                $pdo->prepare("
                    INSERT INTO riwayat_verifikasi (keputusan, tanggal_keputusan, catatan, id_pengguna, id_booking)
                    VALUES ('rejected', NOW(), ?, ?, ?)
                ")->execute([$alasan, $user['id_pengguna'], $idBooking]);

                $pdo->commit();
                setFlash("Booking $idBooking telah ditolak. Kamar tetap tersedia.", "merah");

            } catch (Exception $e) {
                $pdo->rollBack();
                setFlash("Gagal menolak booking: " . $e->getMessage(), "merah");
            }
        }
        header("Location: admin.php");
        exit;
    }

    // C. UBAH STATUS KAMAR MANUAL
    if ($aksi === 'ubah_status_kamar') {
        $kodeKamar  = trim($_POST['kode_kamar'] ?? '');
        $statusBaru = trim($_POST['status_baru'] ?? '');

        // Cek kamar
        $stmtK = $pdo->prepare("SELECT * FROM kamar WHERE kode_kamar = ?");
        $stmtK->execute([$kodeKamar]);
        $k = $stmtK->fetch();

        if (!$k) {
            setFlash("Kamar tidak ditemukan.", "merah");
        } elseif ($k['status'] === 'terisi') {
            setFlash("Kamar $kodeKamar sedang terisi penghuni, statusnya tidak bisa diubah manual.", "kuning");
        } elseif (!in_array($statusBaru, ['tersedia', 'pemeliharaan'])) {
            setFlash("Pilihan status tidak valid.", "merah");
        } else {
            $pdo->prepare("UPDATE kamar SET status = ?, terisi_hingga = NULL WHERE kode_kamar = ?")->execute([$statusBaru, $kodeKamar]);
            $labelStatus = $statusBaru === 'tersedia' ? 'Tersedia' : 'Perbaikan';
            setFlash("Status kamar $kodeKamar berhasil diubah menjadi: $labelStatus.", "hijau");
        }
        header("Location: admin.php");
        exit;
    }
}

// ====== 3. AMBIL DATA TAMPILAN ADMIN ======
// Statistik
$totalKamar   = (int)$pdo->query("SELECT COUNT(*) FROM kamar")->fetchColumn();
$kamarTersedia = (int)$pdo->query("SELECT COUNT(*) FROM kamar WHERE status = 'tersedia'")->fetchColumn();
$kamarTerisi   = (int)$pdo->query("SELECT COUNT(*) FROM kamar WHERE status = 'terisi'")->fetchColumn();
$totalPending  = (int)$pdo->query("SELECT COUNT(*) FROM booking WHERE status = 'pending'")->fetchColumn();

// Antrean Pending
$stmtPending = $pdo->query("
    SELECT b.*, 
           u.nama AS nama_penyewa, u.email AS email_penyewa, u.no_telepon,
           k.harga,
           m.nama_metode,
           bp.file_bukti
    FROM booking b
    JOIN pengguna u ON b.id_pengguna = u.id_pengguna
    JOIN kamar k ON b.kode_kamar = k.kode_kamar
    JOIN metode_pembayaran m ON b.id_metode = m.id_metode
    LEFT JOIN bukti_pembayaran bp ON b.id_booking = bp.id_booking
    WHERE b.status = 'pending'
    ORDER BY b.created_at ASC
");
$pendingList = $stmtPending->fetchAll();

// Riwayat Keputusan
$stmtKeputusan = $pdo->query("
    SELECT b.*, 
           u.nama AS nama_penyewa,
           k.tipe AS tipe_kamar,
           rv.keputusan AS verif_keputusan, rv.tanggal_keputusan, rv.catatan AS verif_catatan
    FROM booking b
    JOIN pengguna u ON b.id_pengguna = u.id_pengguna
    JOIN kamar k ON b.kode_kamar = k.kode_kamar
    LEFT JOIN riwayat_verifikasi rv ON b.id_booking = rv.id_booking
    WHERE b.status != 'pending'
    ORDER BY b.created_at DESC
");
$keputusanList = $stmtKeputusan->fetchAll();

// Daftar Seluruh Kamar (Urutkan yang belum terisi di atas, terisi di bawah)
$kamarAll = $pdo->query("SELECT * FROM kamar ORDER BY (status = 'terisi') ASC, kode_kamar ASC")->fetchAll();

$infoStatus = [
    'pending'  => ['kuning', 'Menunggu'],
    'approved' => ['hijau',  'Disetujui'],
    'rejected' => ['merah',  'Ditolak']
];

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi" id="isiAdmin">
  <span class="label-bagian">ADMIN</span>
  <h1>Panel Admin</h1>
  <p class="sub">Setujui atau tolak pengajuan booking, dan atur status ketersediaan kamar.</p>

  <!-- Statistik Ringkasan -->
  <div class="stat-baris">
    <div><b><?= $totalKamar ?></b><span>total kamar</span></div>
    <div><b><?= $kamarTersedia ?></b><span>tersedia</span></div>
    <div><b><?= $kamarTerisi ?></b><span>terisi</span></div>
    <div><b><?= $totalPending ?></b><span>menunggu</span></div>
  </div>

  <!-- 1. Antrean Pengajuan Pending -->
  <h2 class="judul-admin">Pengajuan Menunggu Verifikasi</h2>
  <?php if (empty($pendingList)): ?>
    <p class="sub">Tidak ada pengajuan yang menunggu. Semua beres!</p>
  <?php else: ?>
    <?php foreach ($pendingList as $p): ?>
      <div class="admin-kartu">
        <div>
          <b><?= htmlspecialchars($p['nama_penyewa']) ?></b>
          <div class="muted">
            <?= htmlspecialchars($p['email_penyewa']) ?> · No. WA: <?= htmlspecialchars($p['no_telepon']) ?> · Kamar <?= htmlspecialchars($p['kode_kamar']) ?> (<?= rupiah($p['harga']) ?>/bln)
          </div>
          <div class="muted">
            <b><?= htmlspecialchars($p['id_booking']) ?></b> · Mulai <?= formatTanggal($p['tanggal_mulai']) ?> · <?= (int)$p['durasi'] ?> bulan · via <?= htmlspecialchars($p['nama_metode']) ?>
            <?php if (!empty($p['file_bukti'])): ?>
              · <button type="button" class="tombol kecil" onclick="lihatBukti('<?= htmlspecialchars($p['file_bukti']) ?>')">lihat bukti</button>
            <?php else: ?>
              · <em>tanpa bukti</em>
            <?php endif; ?>
          </div>
        </div>

        <div class="admin-aksi">
          <form method="POST" action="admin.php" style="display:inline;">
            <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
            <input type="hidden" name="aksi" value="approve">
            <input type="hidden" name="id_booking" value="<?= htmlspecialchars($p['id_booking']) ?>">
            <button type="submit" class="tombol kecil hijau" onclick="return confirm('Setujui pengajuan <?= htmlspecialchars($p['id_booking']) ?> untuk Kamar <?= htmlspecialchars($p['kode_kamar']) ?>?')">✓ Setujui</button>
          </form>
          <button type="button" class="tombol kecil merah" onclick="bukaModalTolak('<?= htmlspecialchars($p['id_booking']) ?>')">× Tolak</button>
        </div>
      </div>
    <?php endforeach; ?>
  <?php endif; ?>

  <!-- 2. Riwayat Keputusan -->
  <h2 class="judul-admin" style="margin-top:32px;">Riwayat Keputusan</h2>
  <?php if (empty($keputusanList)): ?>
    <p class="sub">Belum ada keputusan yang diambil.</p>
  <?php else: ?>
    <?php foreach ($keputusanList as $k2): 
      $st = $infoStatus[$k2['status']] ?? ['abu', $k2['status']];
    ?>
      <div class="admin-kartu">
        <div>
          <b><?= htmlspecialchars($k2['id_booking']) ?> · Kamar <?= htmlspecialchars($k2['kode_kamar']) ?></b>
          <div class="muted">
            <?= htmlspecialchars($k2['nama_penyewa']) ?> · Mulai <?= formatTanggal($k2['tanggal_mulai']) ?> · <?= (int)$k2['durasi'] ?> bln
            <?php if (!empty($k2['alasan'])): ?>
              · <span style="color:#d9534f">alasan tolak: <?= htmlspecialchars($k2['alasan']) ?></span>
            <?php endif; ?>
          </div>
        </div>
        <span class="lencana <?= $st[0] ?>"><?= $st[1] ?></span>
      </div>
    <?php endforeach; ?>
  <?php endif; ?>

  <!-- 3. Status Kamar -->
  <h2 class="judul-admin" style="margin-top:32px;">Status Kamar</h2>
  <p class="sub"><a class="tombol kecil hijau" href="admin_kamar.php">Kelola Kamar (Tambah / Edit / Hapus)</a></p>
  <?php foreach ($kamarAll as $k): 
    $sedangTerisi = ($k['status'] === 'terisi');
  ?>
    <div class="baris-kamar <?= $sedangTerisi ? 'abu' : '' ?>">
      <b><?= htmlspecialchars($k['kode_kamar']) ?></b>
      <span class="tipe <?= $k['tipe'] === 'putra' ? 'tipe-putra' : 'tipe-putri' ?>">
        <?= htmlspecialchars($k['tipe']) ?>
      </span>
      <span class="ket"><?= rupiah($k['harga']) ?>/bln</span>
      <?php if ($k['terisi_hingga']): ?>
        <span class="ket">terisi s.d. <?= formatTanggal($k['terisi_hingga']) ?></span>
      <?php endif; ?>

      <?php if ($sedangTerisi): ?>
        <select class="masukan" disabled title="Kamar sedang terisi, status diatur otomatis lewat booking">
          <option selected>Terisi</option>
        </select>
      <?php else: ?>
        <form method="POST" action="admin.php" style="margin:0;">
          <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
          <input type="hidden" name="aksi" value="ubah_status_kamar">
          <input type="hidden" name="kode_kamar" value="<?= htmlspecialchars($k['kode_kamar']) ?>">
          <select name="status_baru" class="masukan" onchange="this.form.submit()">
            <option value="tersedia" <?= $k['status'] === 'tersedia' ? 'selected' : '' ?>>Tersedia</option>
            <option value="pemeliharaan" <?= $k['status'] === 'pemeliharaan' ? 'selected' : '' ?>>Perbaikan</option>
          </select>
        </form>
      <?php endif; ?>
    </div>
  <?php endforeach; ?>
</section>

<!-- Modal Alasan Penolakan -->
<div class="modal" id="modalTolak" onclick="if (event.target == this) tutupModal('modalTolak')">
  <div class="modal-kotak">
    <button type="button" class="tutup" onclick="tutupModal('modalTolak')">×</button>
    <h3>Tolak pengajuan ini?</h3>
    <p class="sub-tengah">Alasan penolakan akan disampaikan secara transparan kepada pencari kos.</p>
    <form method="POST" action="admin.php">
      <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
      <input type="hidden" name="aksi" value="reject">
      <input type="hidden" id="tolakIdBooking" name="id_booking" value="">
      <textarea id="tolakAlasan" name="alasan" class="masukan" rows="3" placeholder="cth. Bukti pembayaran tidak terbaca atau nominal tidak sesuai..." required></textarea>
      <br>
      <div style="margin-top:12px;">
        <button type="submit" class="tombol merah">Ya, Tolak</button>
        <button type="button" class="tombol" onclick="tutupModal('modalTolak')">Batal</button>
      </div>
    </form>
  </div>
</div>

<!-- Modal Lihat Bukti Bayar -->
<div class="modal" id="modalBukti" onclick="if (event.target == this) tutupModal('modalBukti')">
  <div class="modal-kotak tengah">
    <button type="button" class="tutup" onclick="tutupModal('modalBukti')">×</button>
    <h3>Bukti Pembayaran</h3>
    <img id="buktiImg" src="" style="width:100%;max-height:450px;object-fit:contain;border:1.5px solid #221F19;border-radius:12px;margin:12px 0;" alt="Bukti bayar">
    <button type="button" class="tombol penuh" onclick="tutupModal('modalBukti')">Tutup</button>
  </div>
</div>

<script>
function bukaModalTolak(idBooking) {
  document.getElementById('tolakIdBooking').value = idBooking;
  document.getElementById('tolakAlasan').value = '';
  document.getElementById('modalTolak').classList.add('tampil');
}
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>