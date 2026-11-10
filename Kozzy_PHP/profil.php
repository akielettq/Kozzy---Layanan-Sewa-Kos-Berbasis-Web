<?php
// profil.php — Profil Akun, Statistik, dan Riwayat Lengkap Pengguna
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

// 1. Guard Autentikasi
$user = userLogin();
if (!$user) {
    setFlash("Masuk dulu ya untuk melihat profil akunmu.", "kuning");
    header("Location: login.php?balik=profil.php");
    exit;
}

$judulHalaman = "Profil Saya — Kozzy";
$isAdmin = ($user['peran'] === 'admin');

// 2. Hitung Statistik Akun
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

// 3. Ambil Riwayat Booking Lengkap
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
    'rejected' => ['merah',  'Ditolak']
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
  <h2 class="judul-admin" style="margin-top:32px;">Keluar Akun</h2>
  <button type="button" class="tombol merah" onclick="tanyaLogout()">Keluar dari Akun</button>
</section>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
