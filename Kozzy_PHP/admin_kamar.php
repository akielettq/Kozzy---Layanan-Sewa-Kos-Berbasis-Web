<?php
// admin_kamar.php — CRUD Kamar (Create, Read, Update, Delete) khusus Admin Kozzy
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

cekMasaSewa($pdo);

// 1. Guard: halaman ini hanya untuk admin (belum login -> ke login.php, bukan admin -> ke index.php)
wajibLogin('admin');

$judulHalaman = "Kelola Kamar — Kozzy";

// Daftar fasilitas master, dipakai untuk checkbox pada form
$fasilitasList    = $pdo->query("SELECT * FROM fasilitas ORDER BY id_fasilitas ASC")->fetchAll();
$idFasilitasValid = array_map('intval', array_column($fasilitasList, 'id_fasilitas'));

$pesanError = '';
$formLama   = null; // isian form disimpan di sini agar tidak hilang jika validasi gagal

// ====== FUNGSI BANTU ======

/**
 * Membaca dan memvalidasi isian form kamar.
 * Mengembalikan ['data' => [...], 'error' => 'pesan atau string kosong'].
 */
function validasiInputKamar(array $post, array $idFasilitasValid, bool $modeTambah): array {
    $kode      = strtoupper(trim($post['kode_kamar'] ?? ''));
    $tipe      = trim($post['tipe'] ?? '');
    $harga     = filter_var($post['harga'] ?? '', FILTER_VALIDATE_INT);
    $status    = trim($post['status'] ?? 'tersedia'); // select yang disabled tidak terkirim -> default tersedia
    $deskripsi = trim($post['deskripsi'] ?? '');

    // Hanya terima id fasilitas yang benar-benar ada di tabel fasilitas
    $fasilitas = array_map('intval', (array)($post['fasilitas'] ?? []));
    $fasilitas = array_values(array_unique(array_intersect($fasilitas, $idFasilitasValid)));

    $error = '';
    if ($modeTambah && !preg_match('/^[A-Z0-9-]{1,10}$/', $kode)) {
        $error = 'Kode kamar wajib diisi (maks. 10 karakter: huruf, angka, atau tanda minus), contoh A-05.';
    } elseif (!in_array($tipe, ['putra', 'putri'], true)) {
        $error = 'Tipe kamar harus putra atau putri.';
    } elseif ($harga === false || $harga < 1 || $harga > 100000000) {
        $error = 'Harga harus berupa angka bulat antara 1 sampai 100.000.000.';
    } elseif (!in_array($status, ['tersedia', 'pemeliharaan'], true)) {
        $error = 'Status kamar tidak valid.';
    } elseif (strlen($deskripsi) > 500) {
        $error = 'Deskripsi maksimal 500 karakter.';
    }

    return [
        'data' => [
            'kode_kamar' => $kode,
            'tipe'       => $tipe,
            'harga'      => (int)$harga,
            'status'     => $status,
            'deskripsi'  => $deskripsi,
            'fasilitas'  => $fasilitas,
        ],
        'error' => $error,
    ];
}

/**
 * Menyimpan relasi kamar-fasilitas (tabel fasilitas_kamar):
 * hapus relasi lama lalu isi ulang sesuai checkbox yang dipilih.
 */
function simpanFasilitasKamar(PDO $pdo, string $kode, array $idFasilitas): void {
    $pdo->prepare("DELETE FROM fasilitas_kamar WHERE kamar_kode_kamar = ?")->execute([$kode]);
    $stmt = $pdo->prepare("INSERT INTO fasilitas_kamar (kamar_kode_kamar, fasilitas_id_fasilitas) VALUES (?, ?)");
    foreach ($idFasilitas as $idF) {
        $stmt->execute([$kode, $idF]);
    }
}

// ====== 2. PROSES AKSI (POST): TAMBAH, UBAH, HAPUS ======
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    if (!validasiCsrf($_POST['csrf_token'] ?? '')) {
        setFlash("Sesi tidak valid. Silakan coba lagi.", "merah");
        header("Location: admin_kamar.php");
        exit;
    }

    $aksi = $_POST['aksi'] ?? '';

    // ---------- CREATE: tambah kamar baru ----------
    if ($aksi === 'tambah') {
        $hasil      = validasiInputKamar($_POST, $idFasilitasValid, true);
        $d          = $hasil['data'];
        $pesanError = $hasil['error'];

        // Kode kamar adalah primary key, jadi tidak boleh kembar
        if ($pesanError === '') {
            $stmtCek = $pdo->prepare("SELECT COUNT(*) FROM kamar WHERE kode_kamar = ?");
            $stmtCek->execute([$d['kode_kamar']]);
            if ((int)$stmtCek->fetchColumn() > 0) {
                $pesanError = "Kode kamar {$d['kode_kamar']} sudah dipakai. Gunakan kode lain.";
            }
        }

        if ($pesanError === '') {
            try {
                $pdo->beginTransaction();
                $pdo->prepare("INSERT INTO kamar (kode_kamar, tipe, harga, status, deskripsi) VALUES (?, ?, ?, ?, ?)")
                    ->execute([$d['kode_kamar'], $d['tipe'], $d['harga'], $d['status'], $d['deskripsi']]);
                simpanFasilitasKamar($pdo, $d['kode_kamar'], $d['fasilitas']);
                $pdo->commit();

                setFlash("Kamar {$d['kode_kamar']} berhasil ditambahkan.", "hijau");
                header("Location: admin_kamar.php");
                exit;
            } catch (PDOException $e) {
                if ($pdo->inTransaction()) {
                    $pdo->rollBack();
                }
                $pesanError = "Kamar gagal disimpan. Silakan coba lagi.";
            }
        }

        // Validasi/simpan gagal: simpan isian agar form dibuka lagi dengan data yang sama
        $formLama = array_merge($d, ['aksi' => 'tambah', 'terisi' => false]);
    }

    // ---------- UPDATE: ubah data kamar ----------
    elseif ($aksi === 'ubah') {
        $hasil      = validasiInputKamar($_POST, $idFasilitasValid, false);
        $d          = $hasil['data'];
        $pesanError = $hasil['error'];

        $stmtK = $pdo->prepare("SELECT * FROM kamar WHERE kode_kamar = ?");
        $stmtK->execute([$d['kode_kamar']]);
        $kamarLama = $stmtK->fetch();

        if (!$kamarLama) {
            setFlash("Kamar tidak ditemukan.", "merah");
            header("Location: admin_kamar.php");
            exit;
        }

        // Kamar yang sedang terisi dikunci: tidak boleh diedit (statusnya diatur otomatis lewat booking)
        if ($kamarLama['status'] === 'terisi') {
            setFlash("Kamar {$d['kode_kamar']} sedang terisi, jadi tidak bisa diedit.", "kuning");
            header("Location: admin_kamar.php");
            exit;
        }

        if ($pesanError === '') {
            try {
                $pdo->beginTransaction();
                $pdo->prepare("UPDATE kamar SET tipe = ?, harga = ?, status = ?, deskripsi = ? WHERE kode_kamar = ?")
                    ->execute([$d['tipe'], $d['harga'], $d['status'], $d['deskripsi'], $d['kode_kamar']]);
                simpanFasilitasKamar($pdo, $d['kode_kamar'], $d['fasilitas']);
                $pdo->commit();

                setFlash("Data kamar {$d['kode_kamar']} berhasil diperbarui.", "hijau");
                header("Location: admin_kamar.php");
                exit;
            } catch (PDOException $e) {
                if ($pdo->inTransaction()) {
                    $pdo->rollBack();
                }
                $pesanError = "Perubahan gagal disimpan. Silakan coba lagi.";
            }
        }

        $formLama = array_merge($d, ['aksi' => 'ubah', 'terisi' => false]);
    }

    // ---------- DELETE: hapus kamar ----------
    elseif ($aksi === 'hapus') {
        $kode = strtoupper(trim($_POST['kode_kamar'] ?? ''));

        $stmtK = $pdo->prepare("SELECT * FROM kamar WHERE kode_kamar = ?");
        $stmtK->execute([$kode]);
        $k = $stmtK->fetch();

        if (!$k) {
            setFlash("Kamar tidak ditemukan.", "merah");
        } elseif ($k['status'] === 'terisi') {
            setFlash("Kamar $kode sedang terisi penghuni, tidak bisa dihapus.", "kuning");
        } else {
            // Relasi booking -> kamar memakai ON DELETE RESTRICT, jadi cek dulu agar pesannya jelas
            $stmtB = $pdo->prepare("SELECT COUNT(*) FROM booking WHERE kode_kamar = ?");
            $stmtB->execute([$kode]);
            $jmlBooking = (int)$stmtB->fetchColumn();

            if ($jmlBooking > 0) {
                setFlash("Kamar $kode punya $jmlBooking riwayat booking sehingga tidak bisa dihapus. Ubah statusnya menjadi Perbaikan jika tidak ingin dipesan.", "kuning");
            } else {
                try {
                    // Baris di fasilitas_kamar ikut terhapus otomatis (ON DELETE CASCADE)
                    $pdo->prepare("DELETE FROM kamar WHERE kode_kamar = ?")->execute([$kode]);
                    setFlash("Kamar $kode berhasil dihapus.", "hijau");
                } catch (PDOException $e) {
                    setFlash("Kamar $kode tidak dapat dihapus karena masih terhubung dengan data lain.", "merah");
                }
            }
        }
        header("Location: admin_kamar.php");
        exit;
    }
}

// ====== 3. READ: AMBIL DATA UNTUK TAMPILAN ======
$kamarAll = $pdo->query("
    SELECT k.*,
           GROUP_CONCAT(f.nama_fasilitas ORDER BY f.id_fasilitas SEPARATOR ', ') AS daftar_fasilitas,
           GROUP_CONCAT(f.id_fasilitas ORDER BY f.id_fasilitas SEPARATOR ',')     AS id_fasilitas_list,
           (SELECT COUNT(*) FROM booking b WHERE b.kode_kamar = k.kode_kamar)     AS jml_booking
    FROM kamar k
    LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar
    LEFT JOIN fasilitas f        ON fk.fasilitas_id_fasilitas = f.id_fasilitas
    GROUP BY k.kode_kamar
    ORDER BY (k.status = 'terisi') ASC, k.kode_kamar ASC
")->fetchAll();

$jmlStatus     = array_count_values(array_column($kamarAll, 'status'));
$totalKamar    = count($kamarAll);
$kamarTersedia = $jmlStatus['tersedia'] ?? 0;
$kamarTerisi   = $jmlStatus['terisi'] ?? 0;
$kamarPerbaikan = $jmlStatus['pemeliharaan'] ?? 0;

$infoStatus = [
    'tersedia'     => ['hijau',  'Tersedia'],
    'terisi'       => ['merah',  'Terisi'],
    'pemeliharaan' => ['kuning', 'Perbaikan'],
];

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<style>
  .bar-kelola { display: flex; justify-content: space-between; gap: 10px; flex-wrap: wrap; margin: 18px 0 12px; }
  .tabel-bungkus { overflow-x: auto; border: 1.5px solid var(--tinta); border-radius: 14px; background: var(--kartu); }
  .tabel-kamar { width: 100%; border-collapse: collapse; font-size: 14px; }
  .tabel-kamar th, .tabel-kamar td { padding: 10px 12px; text-align: left; vertical-align: middle; border-bottom: 1px solid rgba(34, 31, 25, .15); }
  .tabel-kamar th { font-size: 13px; font-weight: 500; color: var(--redup); }
  .tabel-kamar tr:last-child td { border-bottom: none; }
  .tabel-kamar .kosong { text-align: center; color: var(--redup); padding: 24px; }
  .aksi-sel { white-space: nowrap; }
  .aksi-sel form { display: inline; margin: 0; }
  .grid-fasilitas { display: grid; grid-template-columns: 1fr 1fr; gap: 0 14px; }
  .modal-lebar { max-width: 520px; }
  select.pilih-penuh { width: 100%; margin-left: 0; }
  /* Modal tinggi: rata atas + margin auto, jadi tetap di tengah jika muat dan bisa di-scroll jika tidak */
  #modalKamar.tampil { align-items: flex-start; }
  #modalKamar .modal-kotak { margin: auto; }
  /* Baris kamar terisi: sedikit redup, tombol tidak bisa ditekan */
  .tabel-kamar .baris-terisi td { background: rgba(34, 31, 25, .05); }
  .tabel-kamar .tombol:disabled { opacity: .35; cursor: not-allowed; transform: none; }
  /* Kode dan harga jangan terpotong ke baris baru */
  .tabel-kamar td:nth-child(1), .tabel-kamar td:nth-child(3) { white-space: nowrap; }
</style>

<section class="wadah hal-isi">
  <span class="label-bagian">ADMIN</span>
  <h1>Kelola Kamar</h1>
  <p class="sub">Tambah, lihat, ubah, dan hapus data kamar kos.</p>

  <div class="stat-baris">
    <div><b><?= $totalKamar ?></b><span>total kamar</span></div>
    <div><b><?= $kamarTersedia ?></b><span>tersedia</span></div>
    <div><b><?= $kamarTerisi ?></b><span>terisi</span></div>
    <div><b><?= $kamarPerbaikan ?></b><span>perbaikan</span></div>
  </div>

  <?php if ($pesanError !== ''): ?>
    <div class="pesan gagal" style="margin-top:16px;">
      <b>Terjadi kendala:</b> <?= htmlspecialchars($pesanError) ?>
    </div>
  <?php endif; ?>

  <div class="bar-kelola">
    <a class="tombol kecil" href="admin.php">← Panel Admin</a>
    <button type="button" class="tombol kecil hijau" onclick="bukaFormKamar()">+ Tambah Kamar</button>
  </div>

  <!-- READ: tabel daftar kamar -->
  <div class="tabel-bungkus">
    <table class="tabel-kamar">
      <thead>
        <tr>
          <th>Kode</th>
          <th>Tipe</th>
          <th>Harga/bulan</th>
          <th>Status</th>
          <th>Fasilitas</th>
          <th>Aksi</th>
        </tr>
      </thead>
      <tbody>
        <?php if (empty($kamarAll)): ?>
          <tr><td colspan="6" class="kosong">Belum ada data kamar. Klik "+ Tambah Kamar" untuk menambahkan.</td></tr>
        <?php endif; ?>

        <?php foreach ($kamarAll as $k):
          $st = $infoStatus[$k['status']] ?? ['abu', $k['status']];

          // Data yang dipakai JavaScript untuk mengisi form saat tombol Edit ditekan
          $dataForm = [
              'aksi'       => 'ubah',
              'kode_kamar' => $k['kode_kamar'],
              'tipe'       => $k['tipe'],
              'harga'      => (int)$k['harga'],
              'status'     => $k['status'],
              'deskripsi'  => (string)$k['deskripsi'],
              'fasilitas'  => $k['id_fasilitas_list'] ? array_map('intval', explode(',', $k['id_fasilitas_list'])) : [],
              'terisi'     => ($k['status'] === 'terisi'),
          ];
          $pesanKonfirmasi = "Hapus kamar {$k['kode_kamar']}? Data yang dihapus tidak bisa dikembalikan.";

          // Kamar terisi: tombol Edit & Hapus dinonaktifkan
          $terkunci  = ($k['status'] === 'terisi');
          $infoKunci = $terkunci ? ' disabled title="Kamar sedang terisi, tidak bisa diubah"' : '';
        ?>
          <tr class="<?= $terkunci ? 'baris-terisi' : '' ?>">
            <td><b><?= htmlspecialchars($k['kode_kamar']) ?></b></td>
            <td>
              <span class="tipe <?= $k['tipe'] === 'putra' ? 'tipe-putra' : 'tipe-putri' ?>">
                <?= htmlspecialchars($k['tipe']) ?>
              </span>
            </td>
            <td><?= rupiah($k['harga']) ?></td>
            <td>
              <span class="lencana <?= $st[0] ?>"><?= htmlspecialchars($st[1]) ?></span>
              <?php if ($k['terisi_hingga']): ?>
                <div class="muted" style="font-size:12px;">s.d. <?= formatTanggal($k['terisi_hingga']) ?></div>
              <?php endif; ?>
            </td>
            <td><?= $k['daftar_fasilitas'] ? htmlspecialchars($k['daftar_fasilitas']) : '—' ?></td>
            <td class="aksi-sel">
              <button type="button" class="tombol kecil"
                      data-kamar="<?= htmlspecialchars(json_encode($dataForm), ENT_QUOTES) ?>"
                      onclick="bukaFormKamar(this)"<?= $infoKunci ?>>Edit</button>

              <form method="POST" action="admin_kamar.php"
                    onsubmit="return confirm(<?= htmlspecialchars(json_encode($pesanKonfirmasi), ENT_QUOTES) ?>)">
                <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
                <input type="hidden" name="aksi" value="hapus">
                <input type="hidden" name="kode_kamar" value="<?= htmlspecialchars($k['kode_kamar']) ?>">
                <button type="submit" class="tombol kecil merah"<?= $infoKunci ?>>Hapus</button>
              </form>
            </td>
          </tr>
        <?php endforeach; ?>
      </tbody>
    </table>
  </div>
</section>

<!-- CREATE & UPDATE: satu form dipakai untuk tambah dan edit -->
<div class="modal" id="modalKamar" onclick="if (event.target == this) tutupModal('modalKamar')">
  <div class="modal-kotak modal-lebar">
    <button type="button" class="tutup" onclick="tutupModal('modalKamar')">×</button>
    <h3 id="judulFormKamar">Tambah Kamar</h3>

    <form method="POST" action="admin_kamar.php" id="formKamar">
      <input type="hidden" name="csrf_token" value="<?= csrfToken() ?>">
      <input type="hidden" name="aksi" id="fAksi" value="tambah">

      <label for="fKode">Kode kamar</label>
      <input type="text" id="fKode" name="kode_kamar" class="masukan" maxlength="10" placeholder="cth. A-05" required>

      <label for="fTipe">Tipe kos</label>
      <select id="fTipe" name="tipe" class="masukan pilih-penuh" required>
        <option value="putra">Putra</option>
        <option value="putri">Putri</option>
      </select>

      <label for="fHarga">Harga per bulan (Rp)</label>
      <input type="number" id="fHarga" name="harga" class="masukan" min="1" max="100000000" placeholder="cth. 950000" required>

      <label for="fStatus">Status</label>
      <select id="fStatus" name="status" class="masukan pilih-penuh">
        <option value="tersedia">Tersedia</option>
        <option value="pemeliharaan">Perbaikan</option>
        <option value="terisi" disabled>Terisi (diatur otomatis lewat booking)</option>
      </select>
      <p class="ket" id="catatanStatus" style="display:none;">Kamar sedang terisi, jadi statusnya tidak bisa diubah manual.</p>

      <label for="fDeskripsi">Deskripsi</label>
      <textarea id="fDeskripsi" name="deskripsi" class="masukan" rows="3" maxlength="500" placeholder="cth. Kamar lantai 2 dengan lemari besar."></textarea>

      <label>Fasilitas</label>
      <div class="grid-fasilitas">
        <?php foreach ($fasilitasList as $f): ?>
          <label class="pilihan">
            <input type="checkbox" class="cbFasilitas" name="fasilitas[]" value="<?= (int)$f['id_fasilitas'] ?>">
            <?= htmlspecialchars($f['nama_fasilitas']) ?>
          </label>
        <?php endforeach; ?>
      </div>

      <div style="margin-top:16px;">
        <button type="submit" class="tombol hijau">Simpan</button>
        <button type="button" class="tombol" onclick="tutupModal('modalKamar')">Batal</button>
      </div>
    </form>
  </div>
</div>

<script>
// Tombol "+ Tambah Kamar" memanggil tanpa argumen, tombol "Edit" mengirim dirinya sendiri (this)
function bukaFormKamar(tombol) {
  var data = null;
  if (tombol && tombol.dataset.kamar) {
    data = JSON.parse(tombol.dataset.kamar);
  }
  tampilkanFormKamar(data);
}

function tampilkanFormKamar(d) {
  d = d || {
    aksi: 'tambah', kode_kamar: '', tipe: 'putra', harga: '',
    status: 'tersedia', deskripsi: '', fasilitas: [], terisi: false
  };
  var edit = (d.aksi === 'ubah');

  document.getElementById('judulFormKamar').textContent = edit ? 'Edit Kamar ' + d.kode_kamar : 'Tambah Kamar';
  document.getElementById('fAksi').value = d.aksi;

  var kode = document.getElementById('fKode');
  kode.value = d.kode_kamar;
  kode.readOnly = edit; // kode kamar adalah primary key, tidak diubah saat edit

  document.getElementById('fTipe').value = d.tipe;
  document.getElementById('fHarga').value = d.harga;

  var status = document.getElementById('fStatus');
  status.value = d.status;
  status.disabled = !!d.terisi;
  document.getElementById('catatanStatus').style.display = d.terisi ? 'block' : 'none';

  document.getElementById('fDeskripsi').value = d.deskripsi;

  var dipilih = (d.fasilitas || []).map(Number);
  document.querySelectorAll('.cbFasilitas').forEach(function (cb) {
    cb.checked = dipilih.indexOf(parseInt(cb.value, 10)) !== -1;
  });

  document.getElementById('modalKamar').classList.add('tampil');
}
</script>

<?php if ($formLama !== null): ?>
<script>
  // Validasi server gagal: buka kembali form dengan isian sebelumnya
  tampilkanFormKamar(<?= json_encode($formLama, JSON_HEX_TAG | JSON_HEX_AMP | JSON_HEX_APOS | JSON_HEX_QUOT) ?>);
</script>
<?php endif; ?>

<?php require_once __DIR__ . '/includes/footer.php'; ?>