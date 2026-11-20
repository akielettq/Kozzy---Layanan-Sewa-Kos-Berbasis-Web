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

// 2b. Syarat data diri lengkap & tipe kos harus sesuai jenis kelamin penyewa
$pengguna = muatPengguna($pdo, (int)$user['id_pengguna']);
if (!dataDiriLengkap($pengguna)) {
    setFlash("Lengkapi data diri dulu (jenis kelamin, asal daerah, dan kontak wali) sebelum booking kamar.", "kuning");
    header("Location: profil.php?lengkapi=1&balik=" . urlencode("booking.php?kode=" . $kode));
    exit;
}

$tipeSesuai = tipeKosUntuk($pengguna['jenis_kelamin']);
if ($kamar['tipe'] !== $tipeSesuai) {
    require_once __DIR__ . '/includes/header.php';
    require_once __DIR__ . '/includes/nav.php';
    ?>
    <section class="wadah hal-isi">
      <div class="panel-pesan">
        <h2>Kamar ini khusus Kos <?= ucfirst(htmlspecialchars($kamar['tipe'])) ?></h2>
        <p>Akunmu terdaftar sebagai <?= htmlspecialchars(opsiJenisKelamin()[$pengguna['jenis_kelamin']]) ?>, jadi kamu hanya bisa memesan kamar Kos <?= ucfirst($tipeSesuai) ?>.</p>
        <a class="tombol hijau" href="kamar.php?tipe=<?= $tipeSesuai ?>">Lihat Kamar Kos <?= ucfirst($tipeSesuai) ?> →</a>
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

// Ambil daftar metode pembayaran, dikelompokkan per kategori (bank / ewallet / qris)
$metodeList = $pdo->query("SELECT * FROM metode_pembayaran ORDER BY id_metode ASC")->fetchAll();
$metodeById = [];
$metodePerKategori = ['bank' => [], 'ewallet' => [], 'qris' => []];
foreach ($metodeList as $m) {
    $metodeById[(int)$m['id_metode']] = $m;
    if (isset($metodePerKategori[$m['kategori']])) {
        $metodePerKategori[$m['kategori']][] = $m;
    }
}

// Kategori yang tampil terpilih: sesuai isian sebelumnya (jika validasi gagal), selain itu Transfer Bank
$idMetodeLama = (int)($_POST['id_metode'] ?? 0);
$kategoriAwal = $metodeById[$idMetodeLama]['kategori'] ?? 'bank';
if (empty($metodePerKategori[$kategoriAwal])) {
    foreach ($metodePerKategori as $kat => $isi) {
        if (!empty($isi)) { $kategoriAwal = $kat; break; }
    }
}
$durasiAwal = parseDurasi($_POST['durasi'] ?? '') ?? BOOKING_MIN_BULAN;

$pesanError = '';

// ====== 4. PROSES SUBMIT BOOKING (POST) ======
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        $pesanError = "Validasi keamanan kedaluwarsa. Silakan muat ulang halaman.";
    } else {
        $tanggalMulai = trim($_POST['tanggal_mulai'] ?? '');
        $durasi       = parseDurasi($_POST['durasi'] ?? '');
        $idMetode     = (int)($_POST['id_metode'] ?? 0);
        $setuju       = isset($_POST['setuju']);

        $hariIni = date('Y-m-d');

        if ($tanggalMulai === '' || $tanggalMulai < $hariIni) {
            $pesanError = "Pilih tanggal mulai sewa yang valid (minimal hari ini).";
        } elseif ($durasi === null) {
            $pesanError = "Durasi sewa harus berupa bilangan bulat antara " . BOOKING_MIN_BULAN . " dan " . BOOKING_MAKS_BULAN . " bulan.";
        } elseif (!isset($metodeById[$idMetode])) {
            $pesanError = "Pilih cara pembayaran beserta bank / e-wallet tujuannya terlebih dahulu.";
        } elseif (!$setuju) {
            $pesanError = "Anda harus menyetujui syarat & ketentuan sewa.";
        } else {
            // Cek kembali ketersediaan kamar secara real-time
            if (adaPendingDiKamar($pdo, $kode)) {
                $pesanError = "Kamar ini baru saja diajukan oleh pengguna lain.";
            } else {
                // Tangani upload file bukti pembayaran jika ada (divalidasi isi filenya, bukan sekadar nama)
                $fileBuktiSimpan = null;
                if (isset($_FILES['bukti_bayar']) && $_FILES['bukti_bayar']['error'] !== UPLOAD_ERR_NO_FILE) {
                    [$fileBuktiSimpan, $errUpload] = simpanBuktiUpload($_FILES['bukti_bayar']);
                    if ($errUpload !== '') {
                        $pesanError = $errUpload;
                        $fileBuktiSimpan = null;
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
  <h1>Kunci <em>kamarmu</em>.</h1>
  <?= renderStepper([
      ['status' => 'selesai',  'judul' => 'Pilih kamar',  'ket' => 'Kamar ' . $kamar['kode_kamar']],
      ['status' => 'sekarang', 'judul' => 'Isi data & bayar', 'ket' => 'Kamu di sini'],
      ['status' => 'menunggu', 'judul' => 'Verifikasi admin', 'ket' => 'Setelah dikirim'],
  ], 'stepper-booking') ?>

  <div class="booking-grid">
  <div class="booking-utama">

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
    <div class="durasi-input">
      <button type="button" class="durasi-btn" onclick="ubahDurasi(-1)" aria-label="Kurangi satu bulan">−</button>
      <input type="number" id="fDurasi" name="durasi" class="masukan" inputmode="numeric" step="1"
             min="<?= BOOKING_MIN_BULAN ?>" max="<?= BOOKING_MAKS_BULAN ?>" value="<?= $durasiAwal ?>" required>
      <button type="button" class="durasi-btn" onclick="ubahDurasi(1)" aria-label="Tambah satu bulan">+</button>
      <span class="durasi-satuan">bulan</span>
    </div>
    <div class="durasi-cepat" role="group" aria-label="Pilihan cepat durasi">
      <span class="petunjuk" style="margin:0">Pilih cepat:</span>
      <?php foreach ([1, 3, 6, 12] as $cepat): ?>
        <button type="button" class="chip <?= $durasiAwal === $cepat ? 'aktif' : '' ?>" data-bulan="<?= $cepat ?>" onclick="setDurasi(<?= $cepat ?>)"><?= $cepat ?> bln</button>
      <?php endforeach; ?>
    </div>
    <p class="petunjuk">Bebas tentukan lamanya sewa, minimal <?= BOOKING_MIN_BULAN ?> dan maksimal <?= BOOKING_MAKS_BULAN ?> bulan.</p>

    <div class="total-bar">
      <span>TOTAL BAYAR</span>
      <b id="totalHarga"><?= rupiah($kamar['harga'] * $durasiAwal) ?></b>
    </div>

    <h3>Pembayaran</h3>
    <label>Cara pembayaran</label>
    <div class="kategori-bayar" role="radiogroup" aria-label="Cara pembayaran">
      <?php foreach (infoKategoriBayar() as $kat => [$judulKat, $ketKat]):
        if (empty($metodePerKategori[$kat])) continue; ?>
        <label class="kat-kartu <?= $kat === $kategoriAwal ? 'aktif' : '' ?>">
          <input type="radio" name="kategori_bayar" value="<?= $kat ?>" onchange="pilihKategoriBayar('<?= $kat ?>')" <?= $kat === $kategoriAwal ? 'checked' : '' ?>>
          <b><?= htmlspecialchars($judulKat) ?></b>
          <small><?= htmlspecialchars($ketKat) ?></small>
        </label>
      <?php endforeach; ?>
    </div>

    <?php
    // Transfer Bank & E-Wallet: dropdown pilihan, rincian tujuan muncul setelah dipilih
    $panelPilihan = [
        'bank'    => ['panelBank',    'selBank',    'detailBank',    'Pilih bank tujuan',    '— Pilih bank —'],
        'ewallet' => ['panelEwallet', 'selEwallet', 'detailEwallet', 'Pilih e-wallet tujuan', '— Pilih e-wallet —'],
    ];
    foreach ($panelPilihan as $kat => [$idPanel, $idSel, $idDetail, $labelSel, $placeholder]):
        if (empty($metodePerKategori[$kat])) continue;
        $aktif = ($kat === $kategoriAwal);
    ?>
      <div class="panel-bayar" id="<?= $idPanel ?>" style="<?= $aktif ? '' : 'display:none' ?>">
        <label for="<?= $idSel ?>"><?= htmlspecialchars($labelSel) ?></label>
        <select id="<?= $idSel ?>" name="id_metode" class="masukan pilih-penuh" onchange="pilihRekening(this, '<?= $idDetail ?>')" required <?= $aktif ? '' : 'disabled' ?>>
          <option value=""><?= htmlspecialchars($placeholder) ?></option>
          <?php foreach ($metodePerKategori[$kat] as $m): ?>
            <option value="<?= (int)$m['id_metode'] ?>"
                    data-nama="<?= htmlspecialchars($m['nama_metode']) ?>"
                    data-tujuan="<?= htmlspecialchars($m['nomor_tujuan']) ?>"
                    <?= $idMetodeLama === (int)$m['id_metode'] ? 'selected' : '' ?>><?= htmlspecialchars(namaPendekMetode($m['nama_metode'])) ?></option>
          <?php endforeach; ?>
        </select>
        <div class="detail-bayar" id="<?= $idDetail ?>" style="display:none"></div>
      </div>
    <?php endforeach; ?>

    <?php if (!empty($metodePerKategori['qris'])):
        $metodeQris = $metodePerKategori['qris'][0];
        $aktifQris = ($kategoriAwal === 'qris');
    ?>
      <!-- QRIS -->
      <div class="panel-bayar" id="panelQris" style="<?= $aktifQris ? '' : 'display:none' ?>">
        <div class="qris-kotak">
          <img src="assets/qris.jpeg" alt="Kode QRIS Kos Kozzy (contoh)">
          <div class="qris-teks">
            <span class="badge-dummy">QRIS</span>
            <p><b>Scan kode QR ini untuk membayar</b></p>
            <p class="ket"><?= htmlspecialchars($metodeQris['nomor_tujuan']) ?></p>
            <p>Nominal: <b class="nominal-bayar"><?= rupiah($kamar['harga'] * $durasiAwal) ?></b></p>
          </div>
        </div>
        <input type="hidden" id="inQris" name="id_metode" value="<?= (int)$metodeQris['id_metode'] ?>" <?= $aktifQris ? '' : 'disabled' ?>>
      </div>
    <?php endif; ?>

    <label for="fBukti">Unggah bukti pembayaran (struk / screenshot transfer)</label>
    <input type="file" id="fBukti" name="bukti_bayar" class="masukan" accept="image/*" onchange="pilihBukti(this)">
    <p id="namaBukti" class="ket"></p>

    <label class="setuju">
      <input type="checkbox" id="fSetuju" name="setuju" required <?= isset($_POST['setuju']) ? 'checked' : '' ?>>
      Satu akun hanya bisa menyewa satu kamar, dan pengajuan saya akan diverifikasi oleh admin terlebih dahulu.
    </label>

    <div style="margin-top:16px;">
      <button type="submit" class="tombol tera">Kirim &amp; Minta Verifikasi →</button>
      <a class="tombol" href="kamar.php">Batal</a>
    </div>
  </form>
  </div><!-- /booking-utama -->

  <?php
    $fotoBooking = fotoKamar($kamar['kode_kamar'], $kamar['tipe']);
    $tingkatBooking = tingkatKamar($kamar['daftar_fasilitas']);
    $tglAwal = (isset($_POST['tanggal_mulai']) && preg_match('/^\d{4}-\d{2}-\d{2}$/', $_POST['tanggal_mulai'])) ? $_POST['tanggal_mulai'] : date('Y-m-d');
  ?>
  <aside id="infoKamarBooking" class="panel ringkasan-sticky" aria-label="Ringkasan booking">
    <img class="rb-foto" src="<?= htmlspecialchars($fotoBooking[0]) ?>" onerror="this.onerror=null; this.src='<?= $kamar['tipe'] === 'putra' ? 'putra.jpg' : 'putri.jpg' ?>';" alt="Kamar <?= htmlspecialchars($kamar['kode_kamar']) ?>">
    <div class="rb-isi">
      <div class="rb-judul"><b>Kamar <?= htmlspecialchars($kamar['kode_kamar']) ?></b> <span class="tingkat t-<?= $tingkatBooking[0] ?>"><?= $tingkatBooking[1] ?></span></div>
      <div class="ket">Kos <?= ucfirst($kamar['tipe']) ?></div>
      <div class="tag-fas">
        <?php foreach (tagFasilitas($kamar['daftar_fasilitas']) as $tg): ?><span><?= ikonFasilitas($tg) ?><?= htmlspecialchars($tg) ?></span><?php endforeach; ?>
      </div>
      <dl class="rb-baris">
        <div><dt>Harga</dt><dd><?= rupiah($kamar['harga']) ?> /bulan</dd></div>
        <div><dt>Mulai</dt><dd id="rbMulai"><?= formatTanggal($tglAwal) ?></dd></div>
        <div><dt>Durasi</dt><dd id="rbDurasi"><?= labelDurasiBulan($durasiAwal) ?></dd></div>
        <div><dt>Sampai</dt><dd id="rbSampai"><?= formatTanggal(tambahBulan($tglAwal, $durasiAwal)) ?></dd></div>
        <div><dt>Pembayaran</dt><dd id="rbMetode">Belum dipilih</dd></div>
      </dl>
      <div class="rb-total"><span>TOTAL BAYAR</span><b class="nominal-bayar"><?= rupiah($kamar['harga'] * $durasiAwal) ?></b></div>
    </div>
  </aside>
  </div><!-- /booking-grid -->
</section>

<script>
// Sinkronkan tampilan pembayaran dengan pilihan awal (penting saat form dimuat ulang setelah error)
document.addEventListener('DOMContentLoaded', function () {
  // Peringatan jika script.js tertinggal versi dari halaman ini (mis. hanya sebagian file yang ditimpa)
  if (typeof ubahDurasi !== 'function' || typeof setDurasi !== 'function') {
    var formBooking = document.querySelector('#blokBooking form');
    if (formBooking) {
      var info = document.createElement('div');
      info.className = 'pesan gagal';
      info.setAttribute('role', 'alert');
      info.innerHTML = '<b>Berkas script.js belum versi terbaru.</b> Tombol durasi dan ringkasan belum akan berfungsi. ' +
                       'Timpa <code>script.js</code> dengan versi terbaru, lalu muat ulang dengan <b>Ctrl+F5</b>.';
      formBooking.parentNode.insertBefore(info, formBooking);
    }
  }
  var radio = document.querySelector('input[name="kategori_bayar"]:checked');
  pilihKategoriBayar(radio ? radio.value : 'bank');
  var pasangan = [['selBank', 'detailBank'], ['selEwallet', 'detailEwallet']];
  pasangan.forEach(function (p) {
    var sel = document.getElementById(p[0]);
    if (sel && sel.value !== '') pilihRekening(sel, p[1]);
  });
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
