<?php
// index.php — Beranda Utama Kozzy
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

// Otomatis bersihkan kamar expired saat diakses
cekMasaSewa($pdo);

$judulHalaman = "Kozzy — Sewa Kos Bulanan Putra & Putri Pekanbaru";

// Statistik dinamis
$jmlKamar = (int)$pdo->query("SELECT COUNT(*) FROM kamar")->fetchColumn();
$hargaTermurah = (int)$pdo->query("SELECT MIN(harga) FROM kamar")->fetchColumn();
$jmlTersedia = (int)$pdo->query("SELECT COUNT(*) FROM kamar WHERE status = 'tersedia'")->fetchColumn();

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="hero">
  <div class="wadah hero-grid">
    <div>
      <span class="label-hero">SEWA KOS BULANAN</span>
      <h1>Cari kos nggak perlu <em>keliling kota</em> lagi.</h1>
      <p class="lede">Lihat kamar beserta fasilitasnya, booking, bayar, lalu pantau verifikasi admin secara online dari mana saja.</p>
      <div class="hero-aksi">
        <a class="tombol hijau" href="kamar.php">Lihat Kamar (<?= $jmlTersedia ?> Tersedia) →</a>
      </div>
      <div class="hero-stat">
        <div><b><?= $jmlKamar ?></b><span>kamar</span></div>
        <div><b><?= rupiah($hargaTermurah) ?></b><span>mulai / bulan</span></div>
        <div><b>2</b><span>gedung</span></div>
      </div>
    </div>
    <div class="hero-foto">
      <img class="foto-utama" src="utamaa.jpeg" alt="Kamar kos">
      <img class="foto-samping" src="samping.jpeg" alt="Detail kamar">
    </div>
  </div>
</section>

<div class="wadah bagian">
  <span class="label-bagian">01 · PILIH KOS</span>
  <h2>Gedung terpisah, privasi aman.</h2>
  <p class="sub">Dua gedung terpisah untuk pria dan wanita di Pekanbaru.</p>
  <div class="pilih-grid">
    <a class="pilih-kartu" href="kamar.php?tipe=putra">
      <img src="putra.jpg" alt="Kos Putra">
      <div class="pilih-isi">
        <span>GEDUNG A</span>
        <h3>Kos Putra</h3>
        <p>Koridor tenang, parkiran luas, dekat kampus.</p>
        <b class="tombol kecil">Lihat Kamar →</b>
      </div>
    </a>
    <a class="pilih-kartu" href="kamar.php?tipe=putri">
      <img src="putri.jpg" alt="Kos Putri">
      <div class="pilih-isi">
        <span>GEDUNG B</span>
        <h3>Kos Putri</h3>
        <p>Taman belakang, CCTV di tiap lorong, suasana nyaman.</p>
        <b class="tombol kecil">Lihat Kamar →</b>
      </div>
    </a>
  </div>
</div>

<div class="wadah bagian">
  <span class="label-bagian">02 · CARA SEWA</span>
  <h2>Dari lihat-lihat sampai kunci di tangan.</h2>
  <div class="langkah-wrap">
    <div class="langkah"><b>01</b><div><h3>Pilih &amp; booking kamar</h3><p>Cek foto dan fasilitas, lalu tentukan jadwal sewamu.</p></div></div>
    <div class="langkah"><b>02</b><div><h3>Bayar sesuai tagihan</h3><p>Transfer BCA, QRIS, atau E-Wallet, lalu unggah bukti transfer.</p></div></div>
    <div class="langkah"><b>03</b><div><h3>Verifikasi admin</h3><p>Admin memverifikasi pembayaranmu dalam waktu singkat.</p></div></div>
    <div class="langkah"><b>04</b><div><h3>Serah terima kunci</h3><p>Ambil kunci fisik di kos dan kamarmu siap dihuni.</p></div></div>
  </div>
</div>

<div class="cta">
  <div class="wadah cta-dalam">
    <div>
      <h2>Tunggu apa lagi?</h2>
      <p>Pilih tanggal mulai, bayar tagihan, dan biarkan admin kami urus verifikasinya.</p>
    </div>
    <a class="tombol terang" href="kamar.php">Mulai Cari Kamar →</a>
  </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
