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

// 2. Ambil seluruh riwayat booking milik pengguna
$stmt = $pdo->prepare("
    SELECT b.*, 
           k.tipe AS tipe_kamar, k.harga, k.terisi_hingga,
           m.nama_metode,
           bp.file_bukti
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
    'rejected' => ['merah',  'Ditolak']
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
            <b>Menunggu verifikasi admin.</b> Pembayaranmu sudah masuk antrean — biasanya diverifikasi dalam waktu kurang dari 1×24 jam.
          </div>
        <?php elseif ($b['status'] === 'approved'): ?>
          <div class="pesan sukses">
            <b>Selamat, kamarmu aktif!</b> Terisi atas nama kamu sampai <?= formatTanggal($b['terisi_hingga']) ?>.
          </div>
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
