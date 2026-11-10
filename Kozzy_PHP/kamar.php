<?php
// kamar.php — Katalog Kamar Kos & Filter Dinamis
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

$judulHalaman = "Daftar Kamar — Kozzy";
$tipe = $_GET['tipe'] ?? 'semua';
if ($tipe !== 'putra' && $tipe !== 'putri') {
    $tipe = 'semua';
}

$whereClause = "";
$params = [];
if ($tipe !== 'semua') {
    $whereClause = "WHERE k.tipe = ?";
    $params[] = $tipe;
}

$sql = "
    SELECT k.*, 
           GROUP_CONCAT(f.nama_fasilitas ORDER BY f.id_fasilitas SEPARATOR ', ') AS daftar_fasilitas
    FROM kamar k
    LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar
    LEFT JOIN fasilitas f ON fk.fasilitas_id_fasilitas = f.id_fasilitas
    $whereClause
    GROUP BY k.kode_kamar
    ORDER BY (k.status = 'terisi') ASC, k.kode_kamar ASC
";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$kamarList = $stmt->fetchAll();

$u = userLogin();
$sewaAktif = $u ? punyaSewaAktif($pdo, $u['id_pengguna']) : null;

$infoStatus = [
    'tersedia'     => ['hijau',  'Tersedia'],
    'terisi'       => ['merah',  'Terisi'],
    'pemeliharaan' => ['kuning', 'Perbaikan']
];

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi">
  <span class="label-bagian">DAFTAR KAMAR</span>
  <h1>Pilih kamarmu.</h1>
  <p class="sub">Klik tombol Detail untuk melihat fasilitas lengkap dan foto kamar.</p>
  
  <div class="baris-filter">
    <a class="chip <?= $tipe === 'semua' ? 'aktif' : '' ?>" id="chipSemua" href="kamar.php">Semua</a>
    <a class="chip <?= $tipe === 'putra' ? 'aktif' : '' ?>" id="chipPutra" href="kamar.php?tipe=putra">Putra</a>
    <a class="chip <?= $tipe === 'putri' ? 'aktif' : '' ?>" id="chipPutri" href="kamar.php?tipe=putri">Putri</a>
  </div>

  <div id="daftarKamar">
    <?php if (empty($kamarList)): ?>
      <p>Waduh, belum ada kamar di kategori ini.</p>
    <?php else: ?>
      <?php foreach ($kamarList as $k): 
        $st = $infoStatus[$k['status']] ?? ['abu', $k['status']];
        $isTerisi = ($k['status'] === 'terisi');
        $adaPending = adaPendingDiKamar($pdo, $k['kode_kamar']);

        $tombolAksi = '';
        if ($u && $u['peran'] === 'admin') {
            $tombolAksi = "<p class='ket' style='text-align:center'>Akun admin hanya dapat meninjau detail kamar.</p>";
        } elseif ($isTerisi) {
            $tombolAksi = "<button class='tombol penuh' disabled>Kamar Terisi</button>" .
              ($k['terisi_hingga'] ? "<p class='ket'>Kosong mulai " . formatTanggal($k['terisi_hingga']) . ".</p>" : "");
        } elseif ($k['status'] === 'pemeliharaan') {
            $tombolAksi = "<button class='tombol penuh' disabled>Sedang Perbaikan</button>";
        } elseif (!$u) {
            $urlLogin = "login.php?balik=" . urlencode("booking.php?kode=" . $k['kode_kamar']);
            $tombolAksi = "<a class='tombol hijau penuh' style='display:block;text-align:center;text-decoration:none;' href='$urlLogin'>Masuk untuk Booking →</a>";
        } elseif ($sewaAktif) {
            $tombolAksi = "<button class='tombol penuh' disabled>Punya Sewa Aktif</button><p class='ket'>Satu akun hanya bisa menyewa satu kamar.</p>";
        } elseif ($adaPending) {
            $tombolAksi = "<button class='tombol penuh' disabled>Sedang Diproses</button><p class='ket'>Ada pengajuan lain yang sedang diverifikasi admin.</p>";
        } else {
            $urlBooking = "booking.php?kode=" . urlencode($k['kode_kamar']);
            $tombolAksi = "<a class='tombol hijau penuh' style='display:block;text-align:center;text-decoration:none;' href='$urlBooking'>Booking Kamar Ini →</a>";
        }

        $kamarDataJson = htmlspecialchars(json_encode([
            'kode_kamar'       => $k['kode_kamar'],
            'tipe'             => $k['tipe'],
            'harga'            => $k['harga'],
            'status'           => $k['status'],
            'terisi_hingga'    => $k['terisi_hingga'],
            'deskripsi'        => $k['deskripsi'],
            'daftar_fasilitas' => $k['daftar_fasilitas'],
            'tombol_aksi'      => $tombolAksi
        ]), ENT_QUOTES, 'UTF-8');
      ?>
        <div class="kartu-kamar <?= $isTerisi ? 'abu' : '' ?>">
          <?php 
            $fotoPath = "kamar/{$k['kode_kamar']}.jpg";
            $fotoVersi = file_exists(__DIR__ . "/$fotoPath") ? filemtime(__DIR__ . "/$fotoPath") : time();
            $fallbackFoto = ($k['tipe'] === 'putra') ? 'putra.jpg' : 'putri.jpg';
          ?>
          <img src="<?= $fotoPath ?>?v=<?= $fotoVersi ?>" 
               style="width: 100%; height: 125px; object-fit: cover; border-radius: 10px; border: 1.5px solid var(--tinta);" 
               onerror="this.onerror=null; this.src='<?= $fallbackFoto ?>';" 
               alt="Kamar <?= htmlspecialchars($k['kode_kamar']) ?>">
          <div>
            <div class="kartu-atas">
              <h3><?= htmlspecialchars($k['kode_kamar']) ?></h3>
              <span class="tipe <?= $k['tipe'] === 'putra' ? 'tipe-putra' : 'tipe-putri' ?>">
                <?= htmlspecialchars($k['tipe']) ?>
              </span>
              <span class="lencana <?= $st[0] ?>"><?= $st[1] ?></span>
            </div>
            <p><?= htmlspecialchars($k['deskripsi']) ?></p>
            <p class="fasilitas"><?= htmlspecialchars($k['daftar_fasilitas'] ?: '—') ?></p>
            <?php if ($isTerisi && $k['terisi_hingga']): ?>
              <p class="ket">terisi s.d. <?= formatTanggal($k['terisi_hingga']) ?></p>
            <?php endif; ?>
          </div>
          <div class="kartu-kanan">
            <div class="harga"><?= rupiah($k['harga']) ?> <small>/bulan</small></div>
            <button type="button" class="tombol kecil" onclick="bukaDetailKamar(<?= $kamarDataJson ?>)">Detail</button>
          </div>
        </div>
      <?php endforeach; ?>
    <?php endif; ?>
  </div>
</section>

<div class="modal" id="modalDetail" onclick="if (event.target == this) tutupModal('modalDetail')">
  <div class="modal-kotak">
    <button type="button" class="tutup" onclick="tutupModal('modalDetail')">×</button>
    <img id="dFoto" class="detail-foto" src="" alt="Foto kamar">
    <h3 id="dKode"></h3>
    <span id="dStatus" class="lencana"></span>
    <p id="dHarga" class="harga-besar"></p>
    <p id="dDeskripsi" class="deskripsi"></p>
    <p><b>Fasilitas:</b> <span id="dFasilitas"></span></p>
    <div id="dAksi"></div>
  </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
