<?php
// booking.php — Pengajuan Booking Kamar & Unggah Bukti Pembayaran
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

$judulHalaman = "Booking Kamar — Kozzy";
$user = userLogin();

// 1. Validasi Autentikasi & Hak Akses
if (!$user) {
    $kodeReq = $_GET['kode'] ?? '';
    $urlBalik = "booking.php" . ($kodeReq ? "?kode=" . urlencode($kodeReq) : "");
    setFlash("Masuk dulu ya untuk melanjutkan proses booking.", "kuning");
    header("Location: login.php?balik=" . urlencode($urlBalik));
    exit;
}

if ($user['peran'] === 'admin') {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Booking tidak tersedia untuk akun admin</h2>
        <p>Akun admin hanya dapat meninjau detail kamar dan mengelola persetujuan sewa kos.</p>
        <a class="tombol hijau" href="kamar.php">Kembali ke Kamar →</a>
      </div>
    </section>
    <?php
    require_once __DIR__ . '/includes/footer.php';
    exit;
}

// 2. Validasi Kamar yang Dipilih
$kode = trim($_GET['kode'] ?? $_POST['kode'] ?? '');
if ($kode === '') {
    setFlash("Pilih kamar yang ingin kamu sewa terlebih dahulu.", "kuning");
    header("Location: kamar.php");
    exit;
}

// Ambil detail kamar dan fasilitas
$stmtKamar = $pdo->prepare("
    SELECT k.*, 
           GROUP_CONCAT(f.nama_fasilitas ORDER BY f.id_fasilitas SEPARATOR ', ') AS daftar_fasilitas
    FROM kamar k
    LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar
    LEFT JOIN fasilitas f ON fk.fasilitas_id_fasilitas = f.id_fasilitas
    WHERE k.kode_kamar = ?
    GROUP BY k.kode_kamar
");
$stmtKamar->execute([$kode]);
$kamar = $stmtKamar->fetch();

if (!$kamar) {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Kamar tidak ditemukan</h2>
        <p>Kamar dengan kode tersebut tidak terdaftar di sistem kami.</p>
        <a class="tombol hijau" href="kamar.php">Lihat Kamar Lain →</a>
      </div>
    </section>
    <?php
    require_once __DIR__ . '/includes/footer.php';
    exit;
}

// 3. Validasi Kondisi Bisnis
$sewaAktif = punyaSewaAktif($pdo, $user['id_pengguna']);
$adaPending = adaPendingDiKamar($pdo, $kode);

if ($sewaAktif) {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Kamu sudah punya sewa aktif</h2>
        <p>Booking <?= htmlspecialchars($sewaAktif['id_booking']) ?> masih berjalan. Satu akun hanya bisa menyewa satu kamar.</p>
        <a class="tombol hijau" href="sewaku.php">Lihat Sewaku →</a>
      </div>
    </section>
    <?php
    require_once __DIR__ . '/includes/footer.php';
    exit;
}

if ($kamar['status'] !== 'tersedia' || $adaPending) {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Kamar tidak dapat dibooking</h2>
        <p><?= $adaPending ? "Ada pengajuan lain yang sedang menunggu verifikasi admin untuk kamar ini." : "Kamar ini sedang " . ($kamar['status'] === 'terisi' ? "terisi" : "dalam perbaikan") . "." ?></p>
        <a class="tombol hijau" href="kamar.php">Cari Kamar Lain →</a>
      </div>
    </section>
    <?php
    require_once __DIR__ . '/includes/footer.php';
    exit;
}

// Ambil daftar metode pembayaran
$metodeList = $pdo->query("SELECT * FROM metode_pembayaran ORDER BY id_metode ASC")->fetchAll();

$pesanError = '';

// ====== 4. PROSES SUBMIT BOOKING (POST) ======
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        $pesanError = "Validasi keamanan kedaluwarsa. Silakan muat ulang halaman.";
    } else {
        $tanggalMulai = trim($_POST['tanggal_mulai'] ?? '');
        $durasi       = (int)($_POST['durasi'] ?? 1);
        $idMetode     = (int)($_POST['id_metode'] ?? 0);
        $setuju       = isset($_POST['setuju']);

        $hariIni = date('Y-m-d');

        if ($tanggalMulai === '' || $tanggalMulai < $hariIni) {
            $pesanError = "Pilih tanggal mulai sewa yang valid (minimal hari ini).";
        } elseif (!in_array($durasi, [1, 3, 6])) {
            $pesanError = "Durasi sewa harus 1, 3, atau 6 bulan.";
        } elseif ($idMetode <= 0) {
            $pesanError = "Pilih metode pembayaran terlebih dahulu.";
        } elseif (!$setuju) {
            $pesanError = "Anda harus menyetujui syarat & ketentuan sewa.";
        } else {
            // Cek kembali ketersediaan kamar secara real-time
            if (adaPendingDiKamar($pdo, $kode)) {
                $pesanError = "Kamar ini baru saja diajukan oleh pengguna lain.";
            } else {
                // Tangani upload file bukti pembayaran jika ada
                $fileBuktiSimpan = null;
                if (isset($_FILES['bukti_bayar']) && $_FILES['bukti_bayar']['error'] !== UPLOAD_ERR_NO_FILE) {
                    if ($_FILES['bukti_bayar']['error'] !== UPLOAD_ERR_OK) {
                        $pesanError = "Gagal mengunggah file bukti pembayaran (kode error: " . (int)$_FILES['bukti_bayar']['error'] . ").";
                    } else {
                        $fileTmp  = $_FILES['bukti_bayar']['tmp_name'];
                        $fileName = $_FILES['bukti_bayar']['name'];
                        $fileSize = $_FILES['bukti_bayar']['size'];
                        $fileExt  = strtolower(pathinfo($fileName, PATHINFO_EXTENSION));

                        $ekstensiBoleh = ['jpg', 'jpeg', 'png', 'webp'];
                        $maxSize = 2 * 1024 * 1024; // 2MB

                        if (!in_array($fileExt, $ekstensiBoleh)) {
                            $pesanError = "Format bukti pembayaran harus JPG, PNG, atau WEBP.";
                        } elseif ($fileSize > $maxSize) {
                            $pesanError = "Ukuran file bukti pembayaran maksimal 2MB.";
                        } elseif (!validasiMimeGambar($fileTmp)) {
                            $pesanError = "Format berkas gambar bukti pembayaran tidak valid.";
                        } else {
                            $namaFileBaru = "bukti_" . uniqid() . "_" . time() . "." . $fileExt;
                            $targetDir = __DIR__ . "/assets/uploads/bukti/";
                            if (!is_dir($targetDir)) {
                                mkdir($targetDir, 0777, true);
                            }
                            $targetPath = $targetDir . $namaFileBaru;
                            if (move_uploaded_file($fileTmp, $targetPath)) {
                                $fileBuktiSimpan = "assets/uploads/bukti/" . $namaFileBaru;
                            } else {
                                $pesanError = "Gagal mengunggah file bukti pembayaran ke server.";
                            }
                        }
                    }
                }

            if (empty($pesanError)) {
                // Generate ID Booking acak yang unik (KZ-XXXX)
                do {
                    $idBooking = "KZ-" . strtoupper(substr(bin2hex(random_bytes(2)), 0, 4));
                    $stmtCek = $pdo->prepare("SELECT COUNT(*) FROM booking WHERE id_booking = ?");
                    $stmtCek->execute([$idBooking]);
                } while ($stmtCek->fetchColumn() > 0);

                // Eksekusi Transaksi Database
                try {
                    $pdo->beginTransaction();

                    $stmtInsertBooking = $pdo->prepare("
                        INSERT INTO booking (id_booking, tanggal_mulai, durasi, status, id_pengguna, kode_kamar, id_metode, created_at)
                        VALUES (?, ?, ?, 'pending', ?, ?, ?, NOW())
                    ");
                    $stmtInsertBooking->execute([
                        $idBooking,
                        $tanggalMulai,
                        $durasi,
                        $user['id_pengguna'],
                        $kode,
                        $idMetode
                    ]);

                    if ($fileBuktiSimpan !== null) {
                        $stmtInsertBukti = $pdo->prepare("
                            INSERT INTO bukti_pembayaran (file_bukti, tanggal_unggah, id_booking)
                            VALUES (?, NOW(), ?)
                        ");
                        $stmtInsertBukti->execute([$fileBuktiSimpan, $idBooking]);
                    }

                    $pdo->commit();

                    setFlash("Pengajuan booking $idBooking berhasil dikirim! Silakan pantau status verifikasi admin di halaman Sewaku.", "hijau");
                    header("Location: sewaku.php");
                    exit;

                } catch (Exception $e) {
                    $pdo->rollBack();
                    // Hapus file jika sempat terupload tapi transaksi DB gagal
                    if ($fileBuktiSimpan && file_exists(__DIR__ . '/' . $fileBuktiSimpan)) {
                        unlink(__DIR__ . '/' . $fileBuktiSimpan);
                    }
                    $pesanError = "Terjadi kesalahan saat memproses booking: " . $e->getMessage();
                }
            }
        }
    }
}
}

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi" id="blokBooking">
  <span class="label-bagian">BOOKING</span>
  <h1>Kunci kamarmu.</h1>

  <!-- Ringkasan Kamar -->
  <div id="infoKamarBooking" class="panel ringkasan">
    <img src="kamar/<?= htmlspecialchars($kamar['kode_kamar']) ?>.jpg" onerror="this.onerror=null; this.src='<?= $kamar['tipe'] === 'putra' ? 'putra.jpg' : 'putri.jpg' ?>';" alt="Kamar <?= htmlspecialchars($kamar['kode_kamar']) ?>">
    <div>
      <b style="font-size:18px">Kamar <?= htmlspecialchars($kamar['kode_kamar']) ?></b>
      <div class="ket">Kos <?= ucfirst($kamar['tipe']) ?> · <?= htmlspecialchars($kamar['daftar_fasilitas']) ?></div>
      <div class="harga"><?= rupiah($kamar['harga']) ?> <small>/bulan</small></div>
    </div>
  </div>

  <?php if (!empty($pesanError)): ?>
    <div class="pesan gagal" style="margin-bottom:16px;">
      <b>Terjadi kendala:</b> <?= htmlspecialchars($pesanError) ?>
    </div>
  <?php endif; ?>

  <form method="POST" action="booking.php" enctype="multipart/form-data" class="panel">
    <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
    <input type="hidden" name="kode" value="<?= htmlspecialchars($kamar['kode_kamar']) ?>">
    <input type="hidden" id="hargaKamarPerBulan" value="<?= (int)$kamar['harga'] ?>">

    <h3>Detail sewa</h3>
    <label for="fTanggal">Tanggal mulai sewa</label>
    <input type="date" id="fTanggal" name="tanggal_mulai" class="masukan" min="<?= date('Y-m-d') ?>" value="<?= htmlspecialchars($_POST['tanggal_mulai'] ?? date('Y-m-d')) ?>" required>

    <label for="fDurasi">Durasi sewa</label>
    <select id="fDurasi" name="durasi" class="masukan" onchange="hitungTotal()" required>
      <option value="1" <?= (($_POST['durasi'] ?? 1) == 1) ? 'selected' : '' ?>>1 bulan</option>
      <option value="3" <?= (($_POST['durasi'] ?? 1) == 3) ? 'selected' : '' ?>>3 bulan</option>
      <option value="6" <?= (($_POST['durasi'] ?? 1) == 6) ? 'selected' : '' ?>>6 bulan</option>
    </select>

    <div class="total-bar">
      <span>TOTAL BAYAR</span>
      <b id="totalHarga"><?= rupiah($kamar['harga'] * (int)($_POST['durasi'] ?? 1)) ?></b>
    </div>

    <h3>Pembayaran</h3>
    <?php foreach ($metodeList as $m): ?>
      <label class="pilihan">
        <input type="radio" name="id_metode" value="<?= $m['id_metode'] ?>" onchange="pilihMetode('<?= htmlspecialchars($m['nama_metode']) ?>')" <?= (($_POST['id_metode'] ?? 1) == $m['id_metode']) ? 'checked' : '' ?> required>
        <?= htmlspecialchars($m['nama_metode']) ?> — <?= htmlspecialchars($m['nomor_tujuan']) ?>
      </label>
    <?php endforeach; ?>
    
    <p id="infoBayar" class="info-bayar" style="display:none"></p>

    <label for="fBukti">Unggah bukti pembayaran (struk / screenshot transfer)</label>
    <input type="file" id="fBukti" name="bukti_bayar" class="masukan" accept="image/*" onchange="pilihBukti(this)">
    <p id="namaBukti" class="ket"></p>

    <label class="setuju">
      <input type="checkbox" id="fSetuju" name="setuju" required <?= isset($_POST['setuju']) ? 'checked' : '' ?>>
      Satu akun hanya bisa menyewa satu kamar, dan pengajuan saya akan diverifikasi oleh admin terlebih dahulu.
    </label>

    <div style="margin-top:16px;">
      <button type="submit" class="tombol hijau">Kirim &amp; Minta Verifikasi →</button>
      <a class="tombol" href="kamar.php">Batal</a>
    </div>
  </form>
</section>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
