<?php
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

// Guard Autentikasi
$user = userLogin();
if (!$user) {
    setFlash("Masuk dulu ya untuk melihat profil akunmu.", "kuning");
    header("Location: login.php?balik=profil.php");
    exit;
}

$judulHalaman = "Profil Saya — Kozzy";
$isAdmin = ($user['peran'] === 'admin');

$balikRaw    = trim($_GET['balik'] ?? $_POST['balik'] ?? '');
$balikProfil = $balikRaw !== '' ? sanitasiUrlBalik($balikRaw) : '';

$pesanError = '';
$bukaForm   = !$isAdmin && !empty($_GET['lengkapi']);

// ====== PROSES UBAH DATA DIRI ======
if ($_SERVER['REQUEST_METHOD'] === 'POST' && !$isAdmin && ($_POST['aksi'] ?? '') === 'ubah_data') {
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        $pesanError = "Validasi keamanan kedaluwarsa. Silakan muat ulang halaman.";
    } else {
        $lama  = muatPengguna($pdo, (int)$user['id_pengguna']);
        $input = $_POST;

        if (!empty($lama['jenis_kelamin'])) {
            $input['jenis_kelamin'] = $lama['jenis_kelamin'];
        }

        [$d, $err] = validasiDataDiri($input);
        if ($err !== '') {
            $pesanError = $err;
        } else {
            $pdo->prepare("
                UPDATE pengguna
                SET no_telepon = ?, jenis_kelamin = ?, asal_daerah = ?, status_penghuni = ?,
                    instansi = ?, nama_wali = ?, no_hp_wali = ?
                WHERE id_pengguna = ?
            ")->execute([
                $d['no_telepon'], $d['jenis_kelamin'], $d['asal_daerah'], $d['status_penghuni'],
                $d['instansi'], $d['nama_wali'], $d['no_hp_wali'], $user['id_pengguna']
            ]);

            $_SESSION['user']['no_telepon']    = $d['no_telepon'];
            $_SESSION['user']['jenis_kelamin'] = $d['jenis_kelamin'];

            setFlash("Data diri berhasil disimpan.", "hijau");
            header("Location: " . ($balikProfil !== '' ? $balikProfil : 'profil.php'));
            exit;
        }
    }
    $bukaForm = true; 
}

// ====== PROSES UBAH PASSWORD ======
$errSandi  = '';
$bukaSandi = false;
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['aksi'] ?? '') === 'ubah_password') {
    $bukaSandi = true;
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        $errSandi = "Validasi keamanan kedaluwarsa. Silakan muat ulang halaman.";
    } else {
        $sandiLama  = trim($_POST['sandi_lama'] ?? '');
        $sandiBaru  = trim($_POST['sandi_baru'] ?? '');
        $sandiUlang = trim($_POST['sandi_ulang'] ?? '');
        $dataSekarang = muatPengguna($pdo, (int)$user['id_pengguna']);

        if (!$dataSekarang || !hash_equals((string)$dataSekarang['password'], $sandiLama)) {
            $errSandi = "Password lama tidak sesuai.";
        } elseif (strlen($sandiBaru) < 6) {
            $errSandi = "Password baru minimal 6 karakter.";
        } elseif ($sandiBaru === $sandiLama) {
            $errSandi = "Password baru harus berbeda dari password lama.";
        } elseif ($sandiBaru !== $sandiUlang) {
            $errSandi = "Konfirmasi password baru belum sama.";
        } else {
            $pdo->prepare("UPDATE pengguna SET password = ? WHERE id_pengguna = ?")->execute([$sandiBaru, $user['id_pengguna']]);
            session_regenerate_id(true);
            setFlash("Password berhasil diubah.", "hijau");
            header("Location: profil.php");
            exit;
        }
    }
}

$pengguna = muatPengguna($pdo, (int)$user['id_pengguna']) ?? [];
$user['no_telepon'] = $pengguna['no_telepon'] ?? $user['no_telepon'];

$adaError = ($pesanError !== '');
$isi = function (string $kunciPost, string $kolom) use ($adaError, $pengguna) {
    return $adaError ? (string)($_POST[$kunciPost] ?? '') : (string)($pengguna[$kolom] ?? '');
};
$jkTerkunci = !empty($pengguna['jenis_kelamin']);

$stmtStat = $pdo->prepare("
    SELECT 
        COUNT(*) AS total_booking,
        COALESCE(SUM(CASE WHEN b.status = 'approved' THEN k.harga * b.durasi ELSE 0 END), 0) AS total_dibayar
    FROM booking b
    JOIN kamar k ON b.kode_kamar = k.kode_kamar
    WHERE b.id_pengguna = ?
");
$stmtStat->execute([$user['id_pengguna']]);
$stat = $stmtStat->fetch();

$totalBooking = (int)($stat['total_booking'] ?? 0);
$totalDibayar = (int)($stat['total_dibayar'] ?? 0);
$sewaAktif    = punyaSewaAktif($pdo, $user['id_pengguna']);

$stmtRiwayat = $pdo->prepare("
    SELECT b.*, k.harga, k.tipe AS tipe_kamar
    FROM booking b
    JOIN kamar k ON b.kode_kamar = k.kode_kamar
    WHERE b.id_pengguna = ?
    ORDER BY b.created_at DESC
");
$stmtRiwayat->execute([$user['id_pengguna']]);
$riwayatList = $stmtRiwayat->fetchAll();

$infoStatus = [
    'pending'  => ['kuning', 'Menunggu'],
    'approved' => ['hijau',  'Disetujui'],
    'rejected' => ['merah',  'Ditolak'],
    'cancelled' => ['abu',   'Dibatalkan'],
    'expired'  => ['abu',    'Kedaluwarsa']
];

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi" id="isiProfil">
  <span class="label-bagian">AKUN SAYA</span>
  <h1>Profilku</h1>
  <p class="sub">Data akun, statistik pemesanan, dan riwayat sewamu.</p>

  <!-- Kartu Identitas Akun -->
  <div class="panel profil-kartu">
    <span class="avatar-besar"><?= htmlspecialchars(strtoupper(substr($user['nama'], 0, 1))) ?></span>
    <div>
      <h2 style="font-size:24px;margin-bottom:4px;"><?= htmlspecialchars($user['nama']) ?></h2>
      <p class="ket" style="margin:2px 0;"><?= htmlspecialchars($user['email']) ?></p>
      <p class="ket" style="margin:2px 0 8px;">No. WhatsApp: <?= htmlspecialchars($user['no_telepon'] ?: '—') ?></p>
      <span class="lencana <?= $isAdmin ? 'merah' : 'hijau' ?>">
        <?= $isAdmin ? 'Admin Kozzy' : 'Pencari Kos' ?>
      </span>
    </div>
  </div>

  <!-- Statistik Singkat -->
  <div class="stat-baris">
    <div><b><?= $totalBooking ?></b><span>total booking</span></div>
    <div><b><?= rupiah($totalDibayar) ?></b><span>sudah dibayar</span></div>
    <div><b><?= $sewaAktif ? 'Ada' : 'Tidak' ?></b><span>sewa aktif</span></div>
  </div>

  <?php if (!$isAdmin): ?>
    <!-- Data Diri -->
    <h2 class="judul-admin" id="data-diri" style="margin-top:28px;">Data Diri</h2>
    <?php if (!dataDiriLengkap($pengguna)): ?>
      <div class="pesan tunggu" style="margin-bottom:12px;">
        <b>Data dirimu belum lengkap.</b> Lengkapi jenis kelamin, asal daerah, dan kontak wali agar bisa booking kamar.
      </div>
    <?php endif; ?>
    <?php
      $labelInstansi = ['mahasiswa' => 'Kampus', 'pekerja' => 'Tempat kerja'][$pengguna['status_penghuni'] ?? ''] ?? 'Instansi';
    ?>
    <div class="panel">
      <div class="info-grid">
        <div><small>Jenis kelamin</small><b><?= htmlspecialchars(opsiJenisKelamin()[$pengguna['jenis_kelamin'] ?? ''] ?? '—') ?></b></div>
        <div><small>Asal daerah</small><b><?= htmlspecialchars($pengguna['asal_daerah'] ?: '—') ?></b></div>
        <div><small>Status</small><b><?= htmlspecialchars(opsiStatusPenghuni()[$pengguna['status_penghuni'] ?? ''] ?? '—') ?></b></div>
        <div><small><?= $labelInstansi ?></small><b><?= htmlspecialchars($pengguna['instansi'] ?: '—') ?></b></div>
        <div><small>Nama wali</small><b><?= htmlspecialchars($pengguna['nama_wali'] ?: '—') ?></b></div>
        <div><small>No. HP wali</small><b><?= htmlspecialchars($pengguna['no_hp_wali'] ?: '—') ?></b></div>
      </div>
      <button type="button" class="tombol kecil" onclick="bukaFormDataDiri()">
        <?= dataDiriLengkap($pengguna) ? 'Ubah Data Diri' : 'Lengkapi Data Diri' ?>
      </button>
    </div>

    <!-- Bagian Sewa Aktif -->
    <h2 class="judul-admin" style="margin-top:28px;">Sewa Aktif</h2>
    <?php if ($sewaAktif): 
      $stSewa = $infoStatus[$sewaAktif['status']] ?? ['abu', $sewaAktif['status']];
    ?>
      <div class="panel kartu-sewa">
        <div class="kartu-atas">
          <span class="lencana <?= $stSewa[0] ?>"><?= $stSewa[1] ?></span>
          <span class="ket kode"><?= htmlspecialchars($sewaAktif['id_booking']) ?></span>
        </div>
        <h2 style="font-size:20px;margin:8px 0;">Kamar <?= htmlspecialchars($sewaAktif['kode_kamar']) ?></h2>
        <?php if ($sewaAktif['status'] === 'approved'): ?>
          <p class="ket">Terisi atas nama kamu sampai <?= formatTanggal($sewaAktif['terisi_hingga']) ?>.</p>
        <?php else: ?>
          <p class="ket">Pengajuanmu masih menunggu verifikasi admin.</p>
        <?php endif; ?>
        <a class="tombol kecil" href="sewaku.php" style="margin-top:10px;display:inline-block;">Buka Sewaku →</a>
      </div>
    <?php else: ?>
      <div class="panel-pesan">
        <h2>Belum ada sewa aktif</h2>
        <p>Mau cari kamar kos yang nyaman? Masih ada yang kosong, nih.</p>
        <a class="tombol hijau" href="kamar.php">Cari Kamar →</a>
      </div>
    <?php endif; ?>

    <!-- Riwayat Seluruh Booking -->
    <h2 class="judul-admin" style="margin-top:28px;">Riwayat Booking</h2>
    <?php if (empty($riwayatList)): ?>
      <p class="sub">Belum pernah melakukan booking kamar kos.</p>
    <?php else: ?>
      <?php foreach ($riwayatList as $r): 
        $stR = $infoStatus[$r['status']] ?? ['abu', $r['status']];
      ?>
        <div class="riwayat-baris">
          <div>
            <b><?= htmlspecialchars($r['id_booking']) ?></b> · Kamar <?= htmlspecialchars($r['kode_kamar']) ?>
            <span class="ket">· <?= formatTanggal($r['tanggal_mulai']) ?> · <?= (int)$r['durasi'] ?> bln · <?= rupiah($r['harga'] * $r['durasi']) ?></span>
          </div>
          <span class="lencana <?= $stR[0] ?>"><?= $stR[1] ?></span>
        </div>
      <?php endforeach; ?>
    <?php endif; ?>
  <?php endif; ?>

  <!-- Tombol Keluar Akun -->
  <h2 class="judul-admin" id="keamanan" style="margin-top:32px;">Keamanan Akun</h2>
  <div class="panel">
    <p class="ket" style="margin:0 0 10px;">Ganti password secara berkala agar akunmu tetap aman.</p>
    <button type="button" class="tombol kecil" onclick="bukaFormSandi()">Ubah Password</button>
  </div>

  <h2 class="judul-admin" style="margin-top:32px;">Keluar Akun</h2>
  <button type="button" class="tombol merah" onclick="tanyaLogout()">Keluar dari Akun</button>
</section>

<!-- Modal Ubah Password -->
<div class="modal modal-atas" id="modalSandi" onclick="if (event.target == this) tutupModal('modalSandi')">
  <div class="modal-kotak">
    <button type="button" class="tutup" onclick="tutupModal('modalSandi')">×</button>
    <h3>Ubah Password</h3>
    <form method="POST" action="profil.php">
      <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
      <input type="hidden" name="aksi" value="ubah_password">

      <?php if ($errSandi !== ''): ?>
        <div class="pesan gagal" style="margin-bottom:6px;"><?= htmlspecialchars($errSandi) ?></div>
      <?php endif; ?>

      <label for="sandiLama">Password lama</label>
      <input id="sandiLama" name="sandi_lama" type="password" class="masukan" autocomplete="current-password" required>

      <label for="sandiBaru">Password baru</label>
      <input id="sandiBaru" name="sandi_baru" type="password" class="masukan" autocomplete="new-password" placeholder="min. 6 karakter" required>

      <label for="sandiUlang">Ulangi password baru</label>
      <input id="sandiUlang" name="sandi_ulang" type="password" class="masukan" autocomplete="new-password" required>

      <div style="margin-top:16px;">
        <button type="submit" class="tombol hijau">Simpan Password</button>
        <button type="button" class="tombol" onclick="tutupModal('modalSandi')">Batal</button>
      </div>
    </form>
  </div>
</div>
<script>
function bukaFormSandi() {
  document.getElementById('modalSandi').classList.add('tampil');
}
<?php if ($bukaSandi): ?>
document.addEventListener('DOMContentLoaded', bukaFormSandi);
<?php endif; ?>
</script>

<?php if (!$isAdmin): ?>
<!-- Modal Ubah Data Diri -->
<div class="modal modal-atas" id="modalDataDiri" onclick="if (event.target == this) tutupModal('modalDataDiri')">
  <div class="modal-kotak lebar">
    <button type="button" class="tutup" onclick="tutupModal('modalDataDiri')">×</button>
    <h3>Data Diri</h3>
    <p class="sub-tengah" style="text-align:left;">Data ini membantu pengelola kos mengenal penghuni dan menghubungi wali jika diperlukan.</p>

    <form method="POST" action="profil.php">
      <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
      <input type="hidden" name="aksi" value="ubah_data">
      <input type="hidden" name="balik" value="<?= htmlspecialchars($balikProfil) ?>">

      <?php if ($pesanError !== ''): ?>
        <div class="pesan gagal" style="margin-bottom:6px;"><?= htmlspecialchars($pesanError) ?></div>
      <?php endif; ?>

      <div class="baris-2">
        <div>
          <label for="profJk">Jenis kelamin</label>
          <select id="profJk" name="jenis_kelamin" class="masukan pilih-penuh" <?= $jkTerkunci ? 'disabled' : 'required' ?>>
            <option value="">— Pilih —</option>
            <?php foreach (opsiJenisKelamin() as $kode => $label): ?>
              <option value="<?= $kode ?>" <?= $isi('jenis_kelamin', 'jenis_kelamin') === $kode || ($jkTerkunci && $pengguna['jenis_kelamin'] === $kode) ? 'selected' : '' ?>><?= $label ?></option>
            <?php endforeach; ?>
          </select>
        </div>
        <div>
          <label for="profTelp">No. WhatsApp</label>
          <input id="profTelp" name="telp" class="masukan" inputmode="numeric" value="<?= htmlspecialchars($isi('telp', 'no_telepon')) ?>" required>
        </div>
      </div>
      <p class="petunjuk">
        <?= $jkTerkunci ? 'Jenis kelamin menentukan tipe kos yang bisa dipesan, sehingga tidak bisa diubah sendiri. Hubungi admin jika ada kesalahan.' : 'Jenis kelamin menentukan tipe kos yang bisa kamu pesan (putra / putri).' ?>
      </p>

      <label for="profAsal">Asal daerah</label>
      <input id="profAsal" name="asal_daerah" class="masukan" maxlength="100" placeholder="cth. Bangkinang, Riau" value="<?= htmlspecialchars($isi('asal_daerah', 'asal_daerah')) ?>" required>

      <div class="baris-2">
        <div>
          <label for="profStatus">Status</label>
          <select id="profStatus" name="status_penghuni" class="masukan pilih-penuh" onchange="gantiLabelInstansi()" required>
            <option value="">— Pilih —</option>
            <?php foreach (opsiStatusPenghuni() as $kode => $label): ?>
              <option value="<?= $kode ?>" <?= $isi('status_penghuni', 'status_penghuni') === $kode ? 'selected' : '' ?>><?= $label ?></option>
            <?php endforeach; ?>
          </select>
        </div>
        <div>
          <label for="profInstansi" id="labelInstansi">Kampus / tempat kerja</label>
          <input id="profInstansi" name="instansi" class="masukan" maxlength="100" value="<?= htmlspecialchars($isi('instansi', 'instansi')) ?>">
        </div>
      </div>

      <div class="judul-seksi">Data wali</div>
      <div class="baris-2">
        <div>
          <label for="profWali">Nama wali</label>
          <input id="profWali" name="nama_wali" class="masukan" maxlength="100" value="<?= htmlspecialchars($isi('nama_wali', 'nama_wali')) ?>" required>
        </div>
        <div>
          <label for="profHpWali">No. HP wali</label>
          <input id="profHpWali" name="no_hp_wali" class="masukan" inputmode="numeric" value="<?= htmlspecialchars($isi('no_hp_wali', 'no_hp_wali')) ?>" required>
        </div>
      </div>

      <div style="margin-top:16px;">
        <button type="submit" class="tombol hijau">Simpan</button>
        <button type="button" class="tombol" onclick="tutupModal('modalDataDiri')">Batal</button>
      </div>
    </form>
  </div>
</div>

<script>
function bukaFormDataDiri() {
  document.getElementById('modalDataDiri').classList.add('tampil');
  gantiLabelInstansi();
}
<?php if ($bukaForm): ?>
document.addEventListener('DOMContentLoaded', bukaFormDataDiri);
<?php endif; ?>
</script>
<?php endif; ?>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
