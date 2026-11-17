<?php
// kamar.php — Katalog Kamar Kos & Filter Dinamis
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

$judulHalaman = "Daftar Kamar — Kozzy";

// ---------- Filter & urutan dari URL ----------
$tipe = $_GET['tipe'] ?? 'semua';
if ($tipe !== 'putra' && $tipe !== 'putri') {
    $tipe = 'semua';
}

// 'tersedia' = kamar yang benar-benar bisa dipesan sekarang (tidak terisi, tidak diperbaiki,
// dan tidak sedang menunggu verifikasi pengajuan lain)
$statusFilter = ($_GET['status'] ?? 'semua') === 'tersedia' ? 'tersedia' : 'semua';

$urut = in_array($_GET['urut'] ?? '', ['termurah', 'termahal'], true) ? $_GET['urut'] : 'kode';

// Tautan filter: mempertahankan pilihan lain, hanya mengganti bagian yang diubah
function urlKamar(array $ubah = []): string {
    global $tipe, $statusFilter, $urut;
    $s = array_merge(['tipe' => $tipe, 'status' => $statusFilter, 'urut' => $urut], $ubah);
    $q = [];
    if ($s['tipe'] !== 'semua')   $q['tipe'] = $s['tipe'];
    if ($s['status'] !== 'semua') $q['status'] = $s['status'];
    if ($s['urut'] !== 'kode')    $q['urut'] = $s['urut'];
    return 'kamar.php' . ($q ? '?' . http_build_query($q) : '');
}
$sqlPending = "EXISTS (SELECT 1 FROM booking bp WHERE bp.kode_kamar = k.kode_kamar AND bp.status = 'pending')";

$kondisi = [];
$params = [];
if ($tipe !== 'semua') {
    $kondisi[] = "k.tipe = ?";
    $params[] = $tipe;
}
if ($statusFilter === 'tersedia') {
    $kondisi[] = "k.status = 'tersedia'";
    $kondisi[] = "NOT $sqlPending";
}
$whereClause = $kondisi ? "WHERE " . implode(" AND ", $kondisi) : "";

// Kamar yang bisa dipesan selalu di atas; yang menunggu verifikasi, diperbaiki, dan terisi di bawah
$urutHarga = ['kode' => '', 'termurah' => 'k.harga ASC, ', 'termahal' => 'k.harga DESC, '][$urut];

$sql = "
    SELECT k.*,
           GROUP_CONCAT(f.nama_fasilitas ORDER BY f.id_fasilitas SEPARATOR ', ') AS daftar_fasilitas,
           CASE WHEN k.status = 'terisi' THEN 3
                WHEN k.status = 'pemeliharaan' THEN 2
                WHEN $sqlPending THEN 1
                ELSE 0 END AS urutan_status
    FROM kamar k
    LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar
    LEFT JOIN fasilitas f ON fk.fasilitas_id_fasilitas = f.id_fasilitas
    $whereClause
    GROUP BY k.kode_kamar
    ORDER BY urutan_status ASC, {$urutHarga}k.kode_kamar ASC
";
$stmt = $pdo->prepare($sql);
$stmt->execute($params);
$kamarList = $stmt->fetchAll();

$u = userLogin();
$sewaAktif = $u ? punyaSewaAktif($pdo, $u['id_pengguna']) : null;

// Tipe kos yang boleh dipesan akun ini (berdasarkan jenis kelamin yang tercatat)
$jkUser = null;
if ($u && $u['peran'] !== 'admin') {
    $dataUser = muatPengguna($pdo, (int)$u['id_pengguna']);
    $jkUser = $dataUser['jenis_kelamin'] ?? null;
}
$tipeSesuai = tipeKosUntuk($jkUser);

// Kode kamar yang sedang punya pengajuan pending (satu query untuk seluruh daftar)
// Pengajuan yang menunggu verifikasi per kamar: kapan diajukan & oleh siapa (untuk mengenali pengajuan sendiri)
$pendingInfo = $pdo->query("SELECT kode_kamar, MIN(created_at) AS sejak, MIN(id_pengguna) AS id_pengguna FROM booking WHERE status = 'pending' GROUP BY kode_kamar")
    ->fetchAll(PDO::FETCH_UNIQUE | PDO::FETCH_ASSOC);

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
  <h1>Pilih <em>kamarmu</em>.</h1>
  <p class="sub">Klik tombol Detail untuk melihat fasilitas lengkap dan foto kamar.</p>

  <div class="filter-panel">
    <div class="filter-baris">
      <span class="filter-label">Tipe</span>
      <a class="chip <?= $tipe === 'semua' ? 'aktif' : '' ?>" id="chipSemua" href="<?= urlKamar(['tipe' => 'semua']) ?>">Semua</a>
      <a class="chip <?= $tipe === 'putra' ? 'aktif' : '' ?>" id="chipPutra" href="<?= urlKamar(['tipe' => 'putra']) ?>">Putra</a>
      <a class="chip <?= $tipe === 'putri' ? 'aktif' : '' ?>" id="chipPutri" href="<?= urlKamar(['tipe' => 'putri']) ?>">Putri</a>
      <span class="filter-label" style="margin-left:14px">Status</span>
      <a class="chip <?= $statusFilter === 'semua' ? 'aktif' : '' ?>" id="chipSemuaStatus" href="<?= urlKamar(['status' => 'semua']) ?>">Semua</a>
      <a class="chip <?= $statusFilter === 'tersedia' ? 'aktif' : '' ?>" id="chipTersedia" href="<?= urlKamar(['status' => 'tersedia']) ?>">Hanya yang tersedia</a>
    </div>

    <div class="filter-baris">
      <span class="filter-label">Urutkan</span>
      <form method="get" action="kamar.php" style="display:inline-flex;gap:8px;align-items:center;margin:0">
        <?php if ($tipe !== 'semua'): ?><input type="hidden" name="tipe" value="<?= $tipe ?>"><?php endif; ?>
        <?php if ($statusFilter !== 'semua'): ?><input type="hidden" name="status" value="<?= $statusFilter ?>"><?php endif; ?>
        <select name="urut" id="selUrut" class="pilih-urut" onchange="this.form.submit()" aria-label="Urutkan kamar">
          <option value="kode"     <?= $urut === 'kode' ? 'selected' : '' ?>>Nomor kamar</option>
          <option value="termurah" <?= $urut === 'termurah' ? 'selected' : '' ?>>Harga: termurah ke termahal</option>
          <option value="termahal" <?= $urut === 'termahal' ? 'selected' : '' ?>>Harga: termahal ke termurah</option>
        </select>
        <noscript><button type="submit" class="tombol kecil">Terapkan</button></noscript>
      </form>
      <?php if ($tipe !== 'semua' || $statusFilter !== 'semua' || $urut !== 'kode'): ?>
        <a class="reset-filter" id="resetFilter" href="kamar.php">Reset filter</a>
      <?php endif; ?>
    </div>
  </div>

  <?php
    $jmlBisaDipesan = 0; $jmlMenunggu = 0;
    foreach ($kamarList as $kk) {
        if ($kk['status'] !== 'tersedia') continue;
        if (isset($pendingInfo[$kk['kode_kamar']])) $jmlMenunggu++; else $jmlBisaDipesan++;
    }
  ?>
  <p class="ket" id="jumlahKamar" style="margin:0 0 14px;">
    Menampilkan <b><?= count($kamarList) ?></b> kamar · <?= $jmlBisaDipesan ?> tersedia<?= $jmlMenunggu ? " · $jmlMenunggu menunggu verifikasi" : '' ?>
  </p>

  <div id="daftarKamar" class="grid-kamar">
    <?php if (empty($kamarList)): ?>
      <p>Waduh, belum ada kamar yang cocok dengan filter ini. <a href="kamar.php" style="text-decoration:underline">Reset filter</a></p>
    <?php else: ?>
      <?php foreach ($kamarList as $k):
        $st = $infoStatus[$k['status']] ?? ['abu', $k['status']];
        $isTerisi = ($k['status'] === 'terisi');

        // Pengajuan yang sedang menunggu verifikasi admin (hanya relevan untuk kamar yang masih berstatus tersedia)
        $dataPending = ($k['status'] === 'tersedia') ? ($pendingInfo[$k['kode_kamar']] ?? null) : null;
        $adaPending  = $dataPending !== null;
        $milikSaya   = $adaPending && $u && (int)$dataPending['id_pengguna'] === (int)$u['id_pengguna'];
        $infoPending = null;
        if ($adaPending) {
            $infoPending = $milikSaya
                ? "Pengajuanmu untuk kamar ini sedang diverifikasi admin (diajukan " . formatTanggal($dataPending['sejak']) . ")."
                : "Ada pengajuan yang sedang menunggu verifikasi admin (diajukan " . formatTanggal($dataPending['sejak']) . "). Jika ditolak, kamar ini tersedia lagi.";
        }
        if ($adaPending) $st = ['kuning', 'Menunggu verifikasi'];

        $tombolAksi = '';
        if ($u && $u['peran'] === 'admin') {
            $tombolAksi = "<p class='ket' style='text-align:center'>Akun admin hanya dapat meninjau detail kamar.</p>";
            if ($adaPending) {
                $tombolAksi .= "<a class='tombol penuh' style='display:block;text-align:center;text-decoration:none;' href='admin.php'>Buka Verifikasi di Panel Admin →</a>";
            }
        } elseif ($isTerisi) {
            $tombolAksi = "<button class='tombol penuh' disabled>Kamar Terisi</button>" .
              ($k['terisi_hingga'] ? "<p class='ket'>Kosong mulai " . formatTanggal($k['terisi_hingga']) . ".</p>" : "");
        } elseif ($k['status'] === 'pemeliharaan') {
            $tombolAksi = "<button class='tombol penuh' disabled>Sedang Perbaikan</button>";
        } elseif ($milikSaya) {
            $tombolAksi = "<button class='tombol penuh' disabled>Pengajuanmu Diproses</button><p class='ket'>Pantau statusnya di halaman <a href='sewaku.php' style='text-decoration:underline'>Sewaku</a>.</p>";
        } elseif (!$u) {
            if ($adaPending) {
                $tombolAksi = "<button class='tombol penuh' disabled>Menunggu Verifikasi</button><p class='ket'>Kamar ini sedang diajukan penyewa lain dan menunggu verifikasi admin.</p>";
            } else {
                $urlLogin = "login.php?balik=" . urlencode("booking.php?kode=" . $k['kode_kamar']);
                $tombolAksi = "<a class='tombol tera penuh' style='display:block;text-align:center;text-decoration:none;' href='$urlLogin'>Masuk untuk Booking →</a>";
            }
        } elseif ($tipeSesuai !== null && $k['tipe'] !== $tipeSesuai) {
            $tombolAksi = "<button class='tombol penuh' disabled>Khusus Kos " . ucfirst($k['tipe']) . "</button><p class='ket'>Akunmu terdaftar sebagai " . opsiJenisKelamin()[$jkUser] . ", jadi hanya bisa memesan Kos " . ucfirst($tipeSesuai) . ".</p>";
        } elseif ($sewaAktif) {
            $tombolAksi = "<button class='tombol penuh' disabled>Punya Sewa Aktif</button><p class='ket'>Satu akun hanya bisa menyewa satu kamar.</p>";
        } elseif ($adaPending) {
            $tombolAksi = "<button class='tombol penuh' disabled>Menunggu Verifikasi</button><p class='ket'>Kamar ini sedang diajukan penyewa lain dan menunggu verifikasi admin.</p>";
        } else {
            $urlBooking = "booking.php?kode=" . urlencode($k['kode_kamar']);
            $tombolAksi = "<a class='tombol tera penuh' style='display:block;text-align:center;text-decoration:none;' href='$urlBooking'>Booking Kamar Ini →</a>";
        }

        // Foto (utama + tambahan) & tingkat kamar; dihitung di server sehingga tidak ada permintaan gambar yang 404
        $fotoList = fotoKamar($k['kode_kamar'], $k['tipe']);
        $tingkat  = tingkatKamar($k['daftar_fasilitas']);

        $kamarDataJson = htmlspecialchars(json_encode([
            'kode_kamar'       => $k['kode_kamar'],
            'tipe'             => $k['tipe'],
            'harga'            => $k['harga'],
            'status'           => $k['status'],
            'terisi_hingga'    => $k['terisi_hingga'],
            'deskripsi'        => $k['deskripsi'],
            'daftar_fasilitas' => $k['daftar_fasilitas'],
            'pending'          => $adaPending,
            'info_pending'     => $infoPending,
            'tombol_aksi'      => $tombolAksi,
            'foto'             => $fotoList,
            'tingkat'          => $tingkat[1],
            'tingkat_kode'     => $tingkat[0]
        ]), ENT_QUOTES, 'UTF-8');

        $tagList = tagFasilitas($k['daftar_fasilitas']);
        $jmlFas = $k['daftar_fasilitas'] ? count(explode(', ', $k['daftar_fasilitas'])) : 0;
        $sisaFas = max(0, $jmlFas - count($tagList));
      ?>
        <div class="kartu-kamar <?= $isTerisi ? 'abu' : '' ?> <?= $adaPending ? 'proses' : '' ?>">
          <div class="kk-foto">
            <img src="<?= htmlspecialchars($fotoList[0]) ?>"
                 onerror="this.onerror=null; this.src='<?= $k['tipe'] === 'putra' ? 'putra.jpg' : 'putri.jpg' ?>';"
                 alt="Kamar <?= htmlspecialchars($k['kode_kamar']) ?>" loading="lazy">
            <span class="tingkat t-<?= $tingkat[0] ?>"><?= $tingkat[1] ?></span>
            <?php if (count($fotoList) > 1): ?>
              <span class="jml-foto"><?= count($fotoList) ?> foto</span>
            <?php endif; ?>
            <?php if ($adaPending): ?>
              <span class="pita">Menunggu verifikasi</span>
            <?php endif; ?>
          </div>
          <div class="kk-isi">
            <div class="kartu-atas">
              <h3><?= htmlspecialchars($k['kode_kamar']) ?></h3>
              <span class="tipe <?= $k['tipe'] === 'putra' ? 'tipe-putra' : 'tipe-putri' ?>"><?= htmlspecialchars($k['tipe']) ?></span>
              <span class="lencana <?= $st[0] ?>"><?= $st[1] ?></span>
            </div>
            <div class="harga kk-harga"><?= rupiah($k['harga']) ?> <small>/bulan</small></div>
            <p class="kk-desk"><?= htmlspecialchars($k['deskripsi']) ?></p>
            <?php if ($tagList): ?>
              <div class="tag-fas">
                <?php foreach ($tagList as $tg): ?><span><?= ikonFasilitas($tg) ?><?= htmlspecialchars($tg) ?></span><?php endforeach; ?>
                <?php if ($sisaFas > 0): ?><span class="lain">+<?= $sisaFas ?> lainnya</span><?php endif; ?>
              </div>
            <?php endif; ?>
            <?php if ($infoPending): ?>
              <p class="info-proses"><?= htmlspecialchars($infoPending) ?></p>
            <?php endif; ?>
            <?php if ($isTerisi && $k['terisi_hingga']): ?>
              <p class="ket" style="margin-top:8px">Terisi s.d. <?= formatTanggal($k['terisi_hingga']) ?></p>
            <?php endif; ?>
          </div>
          <div class="kk-aksi">
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
    <div class="galeri" id="dGaleri">
      <img id="dFoto" class="detail-foto" src="" alt="Foto kamar">
      <button type="button" class="galeri-nav prev" id="dPrev" onclick="geserFoto(-1)" aria-label="Foto sebelumnya">‹</button>
      <button type="button" class="galeri-nav next" id="dNext" onclick="geserFoto(1)" aria-label="Foto berikutnya">›</button>
      <span class="galeri-hitung" id="dHitung"></span>
      <div class="galeri-titik" id="dTitik"></div>
    </div>
    <h3 id="dKode"></h3>
    <span id="dStatus" class="lencana"></span>
    <span id="dTingkat" class="tingkat"></span>
    <p id="dHarga" class="harga-besar"></p>
    <p id="dDeskripsi" class="deskripsi"></p>
    <p id="dInfo" class="info-proses" style="display:none"></p>
    <p><b>Fasilitas:</b> <span id="dFasilitas"></span></p>
    <div id="dAksi"></div>
  </div>
</div>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
