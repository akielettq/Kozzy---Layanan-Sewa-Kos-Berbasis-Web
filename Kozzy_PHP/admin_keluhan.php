<?php
// admin_keluhan.php — Admin menangani keluhan / laporan kerusakan dari penghuni
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);
wajibLogin('admin');

$judulHalaman = "Keluhan Penghuni — Kozzy";

// Fitur ini butuh tabel keluhan (migrasi_v4_keluhan.sql). Beri petunjuk ramah jika belum ada.
if (!tabelKeluhanAda($pdo)) {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Fitur keluhan belum aktif</h2>
        <p>Tabel <code>keluhan</code> belum ada di database. Jalankan <code>migrasi_v4_keluhan.sql</code> di phpMyAdmin, lalu muat ulang halaman ini.</p>
        <a class="tombol hijau" href="admin.php">Kembali ke Panel Admin</a>
      </div>
    </section>
    <?php
    require_once __DIR__ . '/includes/footer.php';
    exit;
}

$statusValid  = array_keys(opsiStatusKeluhan());
$filterStatus = in_array($_GET['status'] ?? '', $statusValid, true) ? $_GET['status'] : 'semua';

// ====== UBAH STATUS & CATATAN (POST) ======
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['aksi'] ?? '') === 'ubah_status') {
    $kembali = in_array($_POST['kembali_status'] ?? '', $statusValid, true) ? '?status=' . $_POST['kembali_status'] : '';
    $idKeluhan = (int)($_POST['id_keluhan'] ?? 0);
    $statusBaru = trim($_POST['status'] ?? '');
    $catatan = trim(preg_replace('/[ \t]+/', ' ', str_replace(["\r\n", "\r"], "\n", (string)($_POST['catatan'] ?? ''))));

    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        setFlash("Validasi keamanan sesi kedaluwarsa. Silakan muat ulang halaman.", "merah");
    } elseif (!in_array($statusBaru, $statusValid, true)) {
        setFlash("Status keluhan tidak valid.", "kuning");
    } elseif (mb_strlen($catatan) > KELUHAN_MAKS_KARAKTER) {
        setFlash("Catatan admin maksimal " . KELUHAN_MAKS_KARAKTER . " karakter.", "kuning");
    } else {
        $cek = $pdo->prepare("SELECT COUNT(*) FROM keluhan WHERE id_keluhan = ?");
        $cek->execute([$idKeluhan]);
        if ((int)$cek->fetchColumn() === 0) {
            setFlash("Keluhan tidak ditemukan.", "kuning");
        } else {
            $pdo->prepare("UPDATE keluhan SET status = ?, catatan_admin = ?, updated_at = NOW() WHERE id_keluhan = ?")
                ->execute([$statusBaru, $catatan !== '' ? $catatan : null, $idKeluhan]);
            setFlash("Keluhan #$idKeluhan diperbarui menjadi " . opsiStatusKeluhan()[$statusBaru][1] . ".", "hijau");
        }
    }
    header("Location: admin_keluhan.php" . $kembali);
    exit;
}

// Jumlah per status untuk chip filter
$hitung = array_fill_keys($statusValid, 0);
foreach ($pdo->query("SELECT status, COUNT(*) AS jml FROM keluhan GROUP BY status") as $r) {
    $hitung[$r['status']] = (int)$r['jml'];
}
$totalSemua = array_sum($hitung);

// Daftar keluhan: yang perlu ditangani di atas, yang selesai di bawah
$where = $filterStatus !== 'semua' ? "WHERE kl.status = ?" : "";
$stmt = $pdo->prepare("
    SELECT kl.*, u.nama AS nama_penghuni, u.no_telepon, k.tipe AS tipe_kamar
    FROM keluhan kl
    JOIN pengguna u ON kl.id_pengguna = u.id_pengguna
    JOIN kamar k ON kl.kode_kamar = k.kode_kamar
    $where
    ORDER BY FIELD(kl.status, 'baru', 'diproses', 'selesai'), kl.created_at DESC, kl.id_keluhan DESC
    LIMIT 200
");
$stmt->execute($filterStatus !== 'semua' ? [$filterStatus] : []);
$keluhanList = $stmt->fetchAll();

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi" id="isiAdminKeluhan">
  <span class="label-bagian">ADMIN</span>
  <h1>Keluhan Penghuni</h1>
  <p class="sub">Tangani laporan kerusakan dari penghuni. Ubah status dan beri catatan agar penghuni bisa memantau.
    <a href="admin.php" style="text-decoration:underline">← Panel Admin</a></p>

  <div class="stat-baris">
    <div><b><?= $hitung['baru'] ?></b><span>baru</span></div>
    <div><b><?= $hitung['diproses'] ?></b><span>diproses</span></div>
    <div><b><?= $hitung['selesai'] ?></b><span>selesai</span></div>
    <div><b><?= $totalSemua ?></b><span>total</span></div>
  </div>

  <div class="baris-filter" style="margin-top:14px;">
    <a class="chip <?= $filterStatus === 'semua' ? 'aktif' : '' ?>" id="chipKelSemua" href="admin_keluhan.php">Semua (<?= $totalSemua ?>)</a>
    <?php foreach (opsiStatusKeluhan() as $kodeSt => $infoSt): ?>
      <a class="chip <?= $filterStatus === $kodeSt ? 'aktif' : '' ?>" id="chipKel<?= ucfirst($kodeSt) ?>" href="admin_keluhan.php?status=<?= $kodeSt ?>"><?= $infoSt[1] ?> (<?= $hitung[$kodeSt] ?>)</a>
    <?php endforeach; ?>
  </div>

  <?php if (empty($keluhanList)): ?>
    <p class="sub">Tidak ada keluhan<?= $filterStatus !== 'semua' ? ' dengan status ini' : '' ?>. Semua beres!</p>
  <?php else: ?>
    <?php foreach ($keluhanList as $kl):
      $st = opsiStatusKeluhan()[$kl['status']] ?? ['abu', $kl['status']];
      $labelKategori = opsiKategoriKeluhan()[$kl['kategori']] ?? $kl['kategori'];
      $pesanWa = "Halo {$kl['nama_penghuni']}, ini admin Kozzy. Terkait laporan \"$labelKategori\" di Kamar {$kl['kode_kamar']}: ";
    ?>
      <div class="kartu-keluhan" id="kel-<?= (int)$kl['id_keluhan'] ?>">
        <div class="kartu-atas">
          <h3><?= htmlspecialchars($labelKategori) ?></h3>
          <span class="tipe <?= $kl['tipe_kamar'] === 'putra' ? 'tipe-putra' : 'tipe-putri' ?>"><?= htmlspecialchars($kl['tipe_kamar']) ?></span>
          <span class="lencana <?= $st[0] ?>"><?= $st[1] ?></span>
        </div>
        <p class="ket">
          <b>Kamar <?= htmlspecialchars($kl['kode_kamar']) ?></b> · <?= htmlspecialchars($kl['nama_penghuni']) ?> ·
          <a href="<?= htmlspecialchars(linkWhatsApp($kl['no_telepon'], $pesanWa)) ?>" target="_blank" rel="noopener" style="text-decoration:underline"><?= htmlspecialchars($kl['no_telepon']) ?> (WhatsApp)</a>
          · dilaporkan <?= formatTanggal($kl['created_at']) ?> · #<?= (int)$kl['id_keluhan'] ?>
        </p>
        <p class="isi-keluhan"><?= nl2br(htmlspecialchars($kl['deskripsi'])) ?></p>
        <?php if (!empty($kl['foto'])): ?>
          <a class="foto-keluhan" href="<?= htmlspecialchars($kl['foto']) ?>" target="_blank" rel="noopener">
            <img src="<?= htmlspecialchars($kl['foto']) ?>" alt="Foto keluhan" loading="lazy">
          </a>
        <?php endif; ?>

        <form method="POST" action="admin_keluhan.php" class="form-keluhan-admin">
          <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
          <input type="hidden" name="aksi" value="ubah_status">
          <input type="hidden" name="id_keluhan" value="<?= (int)$kl['id_keluhan'] ?>">
          <input type="hidden" name="kembali_status" value="<?= $filterStatus !== 'semua' ? $filterStatus : '' ?>">
          <select name="status" class="masukan pilih-penuh" aria-label="Status keluhan">
            <?php foreach (opsiStatusKeluhan() as $kodeSt => $infoSt): ?>
              <option value="<?= $kodeSt ?>" <?= $kl['status'] === $kodeSt ? 'selected' : '' ?>><?= $infoSt[1] ?></option>
            <?php endforeach; ?>
          </select>
          <textarea name="catatan" class="masukan" rows="2" maxlength="<?= KELUHAN_MAKS_KARAKTER ?>"
                    placeholder="Catatan untuk penghuni (opsional), mis. teknisi datang Senin"><?= htmlspecialchars($kl['catatan_admin'] ?? '') ?></textarea>
          <button type="submit" class="tombol kecil hijau">Simpan</button>
        </form>
        <?php if (!empty($kl['updated_at'])): ?>
          <p class="ket" style="margin-top:6px;">Terakhir diperbarui <?= formatTanggal($kl['updated_at']) ?></p>
        <?php endif; ?>
      </div>
    <?php endforeach; ?>
  <?php endif; ?>
</section>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
