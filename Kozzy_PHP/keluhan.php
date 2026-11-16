<?php
// keluhan.php — Laporan keluhan / kerusakan oleh penghuni dan pemantauan statusnya
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);
wajibLogin();
$user = userLogin();

// Admin mengelola keluhan di halamannya sendiri
if ($user['peran'] === 'admin') {
    header("Location: admin_keluhan.php");
    exit;
}

$judulHalaman = "Keluhan — Kozzy";

// Fitur ini butuh tabel keluhan (migrasi_v4_keluhan.sql). Beri petunjuk ramah jika belum ada.
if (!tabelKeluhanAda($pdo)) {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Fitur keluhan belum aktif</h2>
        <p>Tabel <code>keluhan</code> belum ada di database. Jalankan <code>migrasi_v4_keluhan.sql</code> di phpMyAdmin, lalu muat ulang halaman ini.</p>
        <a class="tombol hijau" href="sewaku.php">Kembali ke Sewaku</a>
      </div>
    </section>
    <?php
    require_once __DIR__ . '/includes/footer.php';
    exit;
}

$idPengguna = (int)$user['id_pengguna'];
$sewa = sewaBerjalan($pdo, $idPengguna);
$pesanError = '';

// ====== PROSES BUAT KELUHAN (POST) ======
if ($_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['aksi'] ?? '') === 'buat') {
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        $pesanError = "Validasi keamanan kedaluwarsa. Silakan muat ulang halaman.";
    } elseif (!$sewa) {
        $pesanError = "Laporan hanya bisa dibuat oleh penghuni yang sewanya sedang berjalan.";
    } else {
        $stmtAktif = $pdo->prepare("SELECT COUNT(*) FROM keluhan WHERE id_pengguna = ? AND status <> 'selesai'");
        $stmtAktif->execute([$idPengguna]);
        $jmlAktif = (int)$stmtAktif->fetchColumn();

        [$data, $err] = validasiKeluhan($_POST);
        if ($jmlAktif >= KELUHAN_MAKS_AKTIF) {
            $err = "Kamu masih punya $jmlAktif laporan yang belum selesai. Tunggu hingga ditangani sebelum membuat laporan baru.";
        }

        // Foto bersifat opsional; hanya disimpan jika isian teks sudah valid
        $pathFoto = null;
        if ($err === '' && isset($_FILES['foto']) && $_FILES['foto']['error'] !== UPLOAD_ERR_NO_FILE) {
            [$pathFoto, $errFoto] = simpanUploadGambar($_FILES['foto'], 'keluhan');
            if ($errFoto !== '') {
                $err = $errFoto;
                $pathFoto = null;
            }
        }

        if ($err !== '') {
            $pesanError = $err;
        } else {
            try {
                $pdo->prepare("
                    INSERT INTO keluhan (id_pengguna, kode_kamar, kategori, deskripsi, foto)
                    VALUES (?, ?, ?, ?, ?)
                ")->execute([$idPengguna, $sewa['kode_kamar'], $data['kategori'], $data['deskripsi'], $pathFoto]);
            } catch (PDOException $e) {
                if ($pathFoto) @unlink(__DIR__ . '/' . $pathFoto); // jangan tinggalkan file yatim
                throw $e;
            }
            setFlash("Laporan keluhan terkirim. Admin akan segera menanganinya.", "hijau");
            header("Location: keluhan.php");
            exit;
        }
    }
}

// Riwayat keluhan milik penghuni (terbaru di atas)
$stmt = $pdo->prepare("SELECT * FROM keluhan WHERE id_pengguna = ? ORDER BY created_at DESC, id_keluhan DESC");
$stmt->execute([$idPengguna]);
$keluhanList = $stmt->fetchAll();

$jmlAktifSekarang = 0;
foreach ($keluhanList as $kl) {
    if ($kl['status'] !== 'selesai') $jmlAktifSekarang++;
}

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi" id="isiKeluhan">
  <span class="label-bagian">KELUHAN</span>
  <h1>Ada masalah di <em>kamarmu</em>?</h1>
  <p class="sub">Laporkan kerusakan atau gangguan, lalu pantau penanganannya di sini.</p>

  <?php if ($sewa): ?>
    <div class="panel">
      <p class="ket" style="margin:0 0 6px;">
        Laporan untuk <b>Kamar <?= htmlspecialchars($sewa['kode_kamar']) ?></b> · sewa berjalan sampai <?= formatTanggal($sewa['akhir_sewa']) ?>
      </p>

      <?php if ($pesanError !== ''): ?>
        <div class="pesan gagal" id="keluhanSalah" role="alert"><?= htmlspecialchars($pesanError) ?></div>
      <?php endif; ?>

      <?php if ($jmlAktifSekarang >= KELUHAN_MAKS_AKTIF): ?>
        <div class="pesan tunggu">
          Kamu sudah memiliki <?= $jmlAktifSekarang ?> laporan yang belum selesai. Laporan baru bisa dibuat setelah salah satunya selesai ditangani.
        </div>
      <?php else: ?>
        <form method="POST" action="keluhan.php" enctype="multipart/form-data">
          <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
          <input type="hidden" name="aksi" value="buat">

          <label for="kelKategori">Kategori</label>
          <select id="kelKategori" name="kategori" class="masukan pilih-penuh" required>
            <option value="">— Pilih kategori —</option>
            <?php foreach (opsiKategoriKeluhan() as $kode => $label): ?>
              <option value="<?= $kode ?>" <?= (($_POST['kategori'] ?? '') === $kode) ? 'selected' : '' ?>><?= htmlspecialchars($label) ?></option>
            <?php endforeach; ?>
          </select>

          <label for="kelDeskripsi">Ceritakan masalahnya</label>
          <textarea id="kelDeskripsi" name="deskripsi" class="masukan" rows="4"
                    maxlength="<?= KELUHAN_MAKS_KARAKTER ?>" required
                    placeholder="cth. AC tidak dingin sejak kemarin, sudah dicoba mode cool 16 derajat."
                    oninput="hitungKarakterKeluhan(this)"><?= htmlspecialchars($_POST['deskripsi'] ?? '') ?></textarea>
          <p class="petunjuk"><span id="hitungKel">0</span>/<?= KELUHAN_MAKS_KARAKTER ?> karakter (minimal <?= KELUHAN_MIN_KARAKTER ?>)</p>

          <label for="kelFoto">Foto (opsional)</label>
          <input id="kelFoto" type="file" name="foto" accept="image/jpeg,image/png,image/webp" class="masukan">
          <p class="petunjuk">JPG, PNG, atau WEBP, maksimal 2 MB. Foto membantu admin memahami masalahnya.</p>

          <button type="submit" class="tombol hijau" style="margin-top:14px;">Kirim Laporan →</button>
        </form>
      <?php endif; ?>
    </div>
  <?php else: ?>
    <div class="pesan tunggu">
      <b>Laporan hanya untuk penghuni dengan sewa yang sedang berjalan.</b>
      Setelah booking-mu disetujui dan masa sewa dimulai, kamu bisa melapor dari halaman ini.
    </div>
    <p style="margin-top:12px;"><a class="tombol kecil" href="kamar.php">Cari Kamar →</a>
      <a class="tombol kecil" href="sewaku.php">Lihat Sewaku</a></p>
  <?php endif; ?>

  <h2 class="judul-admin" style="margin-top:32px;">Laporanku</h2>
  <?php if (empty($keluhanList)): ?>
    <p class="sub">Belum ada laporan. Semoga kamarmu selalu nyaman.</p>
  <?php else: ?>
    <?php foreach ($keluhanList as $kl):
      $st = opsiStatusKeluhan()[$kl['status']] ?? ['abu', $kl['status']];
    ?>
      <div class="kartu-keluhan">
        <div class="kartu-atas">
          <h3><?= htmlspecialchars(opsiKategoriKeluhan()[$kl['kategori']] ?? $kl['kategori']) ?></h3>
          <span class="lencana <?= $st[0] ?>"><?= $st[1] ?></span>
        </div>
        <p class="ket">Kamar <?= htmlspecialchars($kl['kode_kamar']) ?> · dilaporkan <?= formatTanggal($kl['created_at']) ?></p>
        <p class="isi-keluhan"><?= nl2br(htmlspecialchars($kl['deskripsi'])) ?></p>
        <?php if (!empty($kl['foto'])): ?>
          <a class="foto-keluhan" href="<?= htmlspecialchars($kl['foto']) ?>" target="_blank" rel="noopener">
            <img src="<?= htmlspecialchars($kl['foto']) ?>" alt="Foto keluhan" loading="lazy">
          </a>
        <?php endif; ?>
        <?php if (!empty($kl['catatan_admin'])): ?>
          <div class="pesan <?= $kl['status'] === 'selesai' ? 'sukses' : 'tunggu' ?>">
            <b>Catatan admin:</b> <?= htmlspecialchars($kl['catatan_admin']) ?>
          </div>
        <?php elseif ($kl['status'] === 'baru'): ?>
          <p class="ket">Laporanmu sudah diterima dan menunggu ditangani admin.</p>
        <?php endif; ?>
      </div>
    <?php endforeach; ?>
  <?php endif; ?>
</section>

<script>
function hitungKarakterKeluhan(el) {
  var h = document.getElementById('hitungKel');
  if (h) h.textContent = el.value.length;
}
document.addEventListener('DOMContentLoaded', function () {
  var d = document.getElementById('kelDeskripsi');
  if (d) hitungKarakterKeluhan(d);
  var err = document.getElementById('keluhanSalah');
  if (err && err.scrollIntoView) err.scrollIntoView({ block: 'center' });
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
