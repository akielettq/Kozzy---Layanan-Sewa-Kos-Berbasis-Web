<?php
// sewaku.php — Status & Riwayat Sewa Penyewa Kos
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

$user = userLogin();

// 1. Guard Autentikasi
if (!$user) {
    setFlash("Masuk dulu ya untuk melihat status sewamu.", "kuning");
    header("Location: login.php?balik=sewaku.php");
    exit;
}

if ($user['peran'] === 'admin') {
    header("Location: admin.php");
    exit;
}

$judulHalaman = "Sewaku — Kozzy";

// ====== PEMBATALAN PENGAJUAN OLEH PENYEWA (POST) ======
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['aksi'] ?? '') === 'batal') {
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        setFlash("Validasi keamanan sesi kedaluwarsa. Silakan muat ulang halaman.", "merah");
    } else {
        $idBatal = trim($_POST['id_booking'] ?? '');
        // Hanya pengajuan milik sendiri yang masih menunggu verifikasi yang bisa dibatalkan
        $stmtBatal = $pdo->prepare("
            UPDATE booking SET status = 'cancelled', alasan = 'Dibatalkan oleh penyewa.'
            WHERE id_booking = ? AND id_pengguna = ? AND status = 'pending'
        ");
        $stmtBatal->execute([$idBatal, $user['id_pengguna']]);
        if ($stmtBatal->rowCount() === 1) {
            setFlash("Pengajuan $idBatal dibatalkan. Kamar kembali tersedia untuk penyewa lain.", "hijau");
        } else {
            setFlash("Pengajuan tidak bisa dibatalkan, kemungkinan sudah diproses admin.", "kuning");
        }
    }
    header("Location: sewaku.php");
    exit;
}

// 2. Ambil seluruh riwayat booking milik pengguna
$stmt = $pdo->prepare("
    SELECT b.*, 
           k.tipe AS tipe_kamar, k.harga, k.terisi_hingga,
           (b.status = 'approved' AND k.status = 'terisi' AND b.tanggal_mulai <= CURDATE()
            AND DATE_ADD(b.tanggal_mulai, INTERVAL b.durasi MONTH) >= CURDATE()) AS berjalan,
           DATE_ADD(b.tanggal_mulai, INTERVAL b.durasi MONTH) AS akhir_sewa,
           (b.tanggal_mulai > CURDATE()) AS belum_mulai,
           (DATE_ADD(b.tanggal_mulai, INTERVAL b.durasi MONTH) < CURDATE()) AS sudah_berakhir,
           m.nama_metode,
           bp.file_bukti,
           TIMESTAMPDIFF(MINUTE, NOW(), DATE_ADD(b.created_at, INTERVAL " . (int)BATAS_JAM_PENGAJUAN . " HOUR)) AS sisa_menit
    FROM booking b
    JOIN kamar k ON b.kode_kamar = k.kode_kamar
    JOIN metode_pembayaran m ON b.id_metode = m.id_metode
    LEFT JOIN bukti_pembayaran bp ON b.id_booking = bp.id_booking
    WHERE b.id_pengguna = ?
    ORDER BY b.created_at DESC
");
$stmt->execute([$user['id_pengguna']]);
$bookingList = $stmt->fetchAll();

$infoStatus = [
    'pending'  => ['kuning', 'Menunggu Verifikasi'],
    'approved' => ['hijau',  'Disetujui'],
    'rejected' => ['merah',  'Ditolak'],
    'cancelled' => ['abu',   'Dibatalkan'],
    'expired'  => ['abu',    'Kedaluwarsa']
];

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi" id="isiSewaku">
  <span class="label-bagian">SEWAKU</span>
  <h1>Hai, <?= htmlspecialchars(explode(' ', $user['nama'])[0]) ?></h1>
  <p class="sub">Pantau status sewa dan riwayat pemesanan kamarmu.</p>

  <?php if (empty($bookingList)): ?>
    <div class="panel-pesan">
      <h2>Belum ada sewa</h2>
      <p>Kamarnya masih banyak yang kosong dan siap kamu huni, nih.</p>
      <a class="tombol hijau" href="kamar.php">Cari Kamar →</a>
    </div>
  <?php else: ?>
    <?php foreach ($bookingList as $b): 
      $st = $infoStatus[$b['status']] ?? ['abu', $b['status']];
    ?>
      <div class="panel kartu-sewa">
        <div class="kartu-atas">
          <span class="lencana <?= $st[0] ?>"><?= $st[1] ?></span>
          <span class="ket kode"><?= htmlspecialchars($b['id_booking']) ?></span>
        </div>

        <h2>Kamar <?= htmlspecialchars($b['kode_kamar']) ?> — Kos <?= ucfirst($b['tipe_kamar']) ?></h2>

        <?= renderStepper(tahapanSewa($b)) ?>
        
        <div class="info-grid">
          <div>
            <small>Mulai sewa</small>
            <b><?= formatTanggal($b['tanggal_mulai']) ?></b>
          </div>
          <div>
            <small>Durasi</small>
            <b><?= (int)$b['durasi'] ?> bulan</b>
          </div>
          <div>
            <small>Total tagihan</small>
            <b><?= rupiah($b['harga'] * $b['durasi']) ?></b>
          </div>
          <div>
            <small>Metode</small>
            <b><?= htmlspecialchars($b['nama_metode']) ?></b>
          </div>
          <div>
            <small>Bukti bayar</small>
            <?php if (!empty($b['file_bukti'])): ?>
              <button type="button" class="tombol kecil" onclick="lihatBukti('<?= htmlspecialchars($b['file_bukti']) ?>')">Lihat Bukti</button>
            <?php else: ?>
              <b>—</b>
            <?php endif; ?>
          </div>
        </div>

        <?php if ($b['status'] === 'pending'): ?>
          <div class="pesan tunggu">
            <b>Menunggu verifikasi admin.</b> Pengajuan otomatis kedaluwarsa jika belum diverifikasi dalam <?= (int)BATAS_JAM_PENGAJUAN ?> jam (sisa waktu: <?= labelSisaWaktu((int)$b['sisa_menit']) ?>).
          </div>
          <form method="POST" action="sewaku.php" style="margin-top:10px;" onsubmit="return confirm('Batalkan pengajuan <?= htmlspecialchars($b['id_booking']) ?> untuk Kamar <?= htmlspecialchars($b['kode_kamar']) ?>?')">
            <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
            <input type="hidden" name="aksi" value="batal">
            <input type="hidden" name="id_booking" value="<?= htmlspecialchars($b['id_booking']) ?>">
            <button type="submit" class="tombol kecil merah">Batalkan Pengajuan</button>
          </form>
        <?php elseif ($b['status'] === 'approved'): ?>
          <div class="pesan sukses">
            <b>Selamat, kamarmu aktif!</b> Terisi atas nama kamu sampai <?= formatTanggal($b['terisi_hingga']) ?>.
          </div>
          <?php if (!empty($b['berjalan'])): ?>
            <a class="tombol kecil" href="keluhan.php" style="margin-top:10px;display:inline-block;">Ada masalah? Lapor Keluhan →</a>
          <?php endif; ?>
        <?php elseif ($b['status'] === 'cancelled'): ?>
          <div class="pesan netral">
            <b>Pengajuan dibatalkan.</b> Kamu membatalkan pengajuan ini, jadi kamar sudah kembali tersedia.
          </div>
          <a class="tombol kecil" href="kamar.php" style="margin-top:10px;display:inline-block;">Cari Kamar →</a>
        <?php elseif ($b['status'] === 'expired'): ?>
          <div class="pesan netral">
            <b>Pengajuan kedaluwarsa.</b> Pengajuan tidak diverifikasi dalam <?= (int)BATAS_JAM_PENGAJUAN ?> jam sehingga ditutup otomatis. Kamu bisa mengajukan lagi kapan saja.
          </div>
          <a class="tombol kecil" href="kamar.php" style="margin-top:10px;display:inline-block;">Ajukan Lagi →</a>
        <?php elseif ($b['status'] === 'rejected'): ?>
          <div class="pesan gagal">
            <b>Ditolak admin.</b> Alasan: <?= htmlspecialchars($b['alasan'] ?: 'Data pembayaran tidak sesuai.') ?>
          </div>
          <a class="tombol kecil" href="kamar.php" style="margin-top:10px;display:inline-block;">Cari Kamar Lain →</a>
        <?php endif; ?>
      </div>
    <?php endforeach; ?>
  <?php endif; ?>
</section>

<!-- Modal Lihat Bukti Pembayaran -->
<div class="modal" id="modalBukti" onclick="if (event.target == this) tutupModal('modalBukti')">
  <div class="modal-kotak tengah">
    <button type="button" class="tutup" onclick="tutupModal('modalBukti')">×</button>
    <h3>Bukti Pembayaran</h3>
    <img id="buktiImg" src="" style="width:100%;max-height:450px;object-fit:contain;border:1.5px solid #221F19;border-radius:12px;margin:12px 0;" alt="Bukti bayar">
    <button type="button" class="tombol penuh" onclick="tutupModal('modalBukti')">Tutup</button>
  </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
