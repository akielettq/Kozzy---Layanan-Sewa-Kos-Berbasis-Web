<?php
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

$judulHalaman = "Masuk & Daftar — Kozzy";

// Jika sudah login, alihkan ke beranda
if (userLogin()) {
    header("Location: index.php");
    exit;
}

$balik = $_GET['balik'] ?? $_POST['balik'] ?? 'index.php';
$loginSalah = '';
$daftarSalah = '';
$tabAktif = 'masuk';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $aksi = $_POST['aksi'] ?? '';

    if ($aksi === 'login') {
        $tabAktif = 'masuk';
        $email = trim(strtolower($_POST['email'] ?? ''));
        $password = trim($_POST['password'] ?? '');

        if ($email === '' || $password === '') {
            $loginSalah = 'Email dan password wajib diisi.';
        } else {
            $stmt = $pdo->prepare("SELECT * FROM pengguna WHERE email = ?");
            $stmt->execute([$email]);
            $user = $stmt->fetch();

            if ($user && $user['password'] === $password) {
                session_regenerate_id(true);

                $_SESSION['user'] = [
                    'id_pengguna' => $user['id_pengguna'],
                    'nama'        => $user['nama'],
                    'email'       => $user['email'],
                    'no_telepon'  => $user['no_telepon'],
                    'peran'       => $user['peran'],
                    'jenis_kelamin' => $user['jenis_kelamin'] ?? null
                ];
                setFlash("Selamat datang kembali, " . htmlspecialchars($user['nama']) . "!", "hijau");
                
                $tujuan = sanitasiUrlBalik($balik);
                header("Location: " . $tujuan);
                exit;
            } else {
                $loginSalah = 'Email atau password salah.';
            }
        }
    }

    if ($aksi === 'daftar') {
        $tabAktif = 'daftar';
        $nama  = trim($_POST['nama'] ?? '');
        $email = trim(strtolower($_POST['email'] ?? ''));
        $sandi = trim($_POST['sandi'] ?? '');
        $sandi2 = trim($_POST['sandi2'] ?? '');

        [$diri, $errDiri] = validasiDataDiri($_POST);

        if (mb_strlen($nama) < 3) {
            $daftarSalah = 'Nama lengkap minimal 3 karakter.';
        } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $daftarSalah = 'Format email belum benar.';
        } elseif (strlen($sandi) < 6) {
            $daftarSalah = 'Password minimal 6 karakter.';
        } elseif ($sandi !== $sandi2) {
            $daftarSalah = 'Konfirmasi password belum sama dengan password.';
        } elseif ($errDiri !== '') {
            $daftarSalah = $errDiri;
        } else {
            // Cek apakah email sudah terdaftar
            $stmtCek = $pdo->prepare("SELECT COUNT(*) FROM pengguna WHERE email = ?");
            $stmtCek->execute([$email]);
            if ($stmtCek->fetchColumn() > 0) {
                $daftarSalah = 'Email sudah terdaftar. Silakan masuk.';
            } else {
                // Simpan pengguna baru
                $stmtInsert = $pdo->prepare("
                    INSERT INTO pengguna (nama, email, password, no_telepon, peran,
                                          jenis_kelamin, asal_daerah, status_penghuni, instansi, nama_wali, no_hp_wali)
                    VALUES (?, ?, ?, ?, 'pencari', ?, ?, ?, ?, ?, ?)
                ");
                $stmtInsert->execute([
                    $nama, $email, $sandi, $diri['no_telepon'],
                    $diri['jenis_kelamin'], $diri['asal_daerah'], $diri['status_penghuni'],
                    $diri['instansi'], $diri['nama_wali'], $diri['no_hp_wali']
                ]);
                $idBaru = (int)$pdo->lastInsertId();

                session_regenerate_id(true);

                $_SESSION['user'] = [
                    'id_pengguna' => $idBaru,
                    'nama'        => $nama,
                    'email'       => $email,
                    'no_telepon'  => $diri['no_telepon'],
                    'peran'       => 'pencari',
                    'jenis_kelamin' => $diri['jenis_kelamin']
                ];
                setFlash("Pendaftaran berhasil! Selamat datang di Kozzy.", "hijau");

                $tujuan = sanitasiUrlBalik($balik);
                header("Location: " . $tujuan);
                exit;
            }
        }
    }
}

require_once __DIR__ . '/includes/header.php';
require_once __DIR__ . '/includes/nav.php';
?>

<section class="wadah hal-isi" id="kartuAuth" style="max-width:<?= $tabAktif === 'daftar' ? 600 : 480 ?>px">
 <div class="kartu-auth">
  <h3 id="judulAuth"><?= $tabAktif === 'masuk' ? 'Masuk ke Kozzy' : 'Buat akun baru' ?></h3>
  <p class="sub-tengah" id="subAuth" style="text-align:left"><?= $tabAktif === 'masuk' ? 'Masuk dulu untuk bisa booking kamar.' : 'Daftar sekali, lalu booking kamar kapan saja.' ?></p>

  <div class="auth-tab">
    <button type="button" id="tabMasuk" class="<?= $tabAktif === 'masuk' ? 'aktif' : '' ?>" onclick="gantiTab('masuk')">Masuk</button>
    <button type="button" id="tabDaftar" class="<?= $tabAktif === 'daftar' ? 'aktif' : '' ?>" onclick="gantiTab('daftar')">Daftar</button>
  </div>

  <!-- Form Masuk -->
  <form id="formMasuk" method="POST" action="login.php" style="<?= $tabAktif === 'masuk' ? '' : 'display:none' ?>">
    <input type="hidden" name="aksi" value="login">
    <input type="hidden" name="balik" value="<?= htmlspecialchars($balik) ?>">

    <label>Email</label>
    <input id="loginEmail" name="email" class="masukan" autocomplete="username" placeholder="nama@email.com" value="<?= htmlspecialchars($_POST['email'] ?? '') ?>" required>

    <label>Password</label>
    <input id="loginPass" name="password" class="masukan" type="password" autocomplete="current-password" placeholder="••••••" required>

    <p class="salah" id="loginSalah"><?= htmlspecialchars($loginSalah) ?></p>
    <button type="submit" class="tombol hijau penuh">Masuk →</button>
    <p class="ganti-tab">Lupa password? <a href="https://wa.me/6289661327050?text=Halo%20admin%20Kozzy%2C%20saya%20lupa%20password%20akun%20saya." target="_blank" rel="noopener">Hubungi admin lewat WhatsApp</a></p>
  </form>

  <!-- Form Daftar -->
  <form id="formDaftar" method="POST" action="login.php" style="<?= $tabAktif === 'daftar' ? '' : 'display:none' ?>">
    <input type="hidden" name="aksi" value="daftar">
    <input type="hidden" name="balik" value="<?= htmlspecialchars($balik) ?>">

    <div class="seksi-judul" style="margin-top:18px">
      <span class="no">1</span>
      <div><b>Data akun</b><small>Untuk masuk ke Kozzy</small></div>
    </div>
    <label for="daftarNama">Nama lengkap</label>
    <input id="daftarNama" name="nama" class="masukan" autocomplete="name" placeholder="cth. Rani Putri" value="<?= htmlspecialchars($_POST['nama'] ?? '') ?>" required>

    <label for="daftarEmail">Email</label>
    <input id="daftarEmail" name="email" type="email" class="masukan" autocomplete="email" placeholder="nama@email.com" value="<?= htmlspecialchars($_POST['email'] ?? '') ?>" required>

    <div class="baris-2">
      <div>
        <label for="daftarSandi">Password</label>
        <input id="daftarSandi" name="sandi" class="masukan" type="password" autocomplete="new-password" placeholder="min. 6 karakter" required>
      </div>
      <div>
        <label for="daftarSandi2">Ulangi password</label>
        <input id="daftarSandi2" name="sandi2" class="masukan" type="password" autocomplete="new-password" placeholder="ketik ulang" required>
      </div>
    </div>
    <label class="pilihan" style="margin:8px 0 0">
      <input type="checkbox" onchange="tampilSandi(this.checked)"> Tampilkan password
    </label>


    <div class="seksi-judul">
      <span class="no">2</span>
      <div><b>Data diri</b><small>Menentukan tipe kos yang bisa kamu pesan</small></div>
    </div>

    <div class="label-grup" id="lblJk">Jenis kelamin</div>
    <div class="segmen" style="--n:2" role="radiogroup" aria-labelledby="lblJk">
      <?php foreach (opsiJenisKelamin() as $kode => $label): ?>
        <label class="segmen-opsi">
          <input type="radio" name="jenis_kelamin" value="<?= $kode ?>" <?= (($_POST['jenis_kelamin'] ?? '') === $kode) ? 'checked' : '' ?> required>
          <span><?= $label ?></span>
        </label>
      <?php endforeach; ?>
    </div>

    <div class="baris-2">
      <div>
        <label for="daftarTelp">No. WhatsApp</label>
        <input id="daftarTelp" name="telp" class="masukan" type="tel" inputmode="numeric" autocomplete="tel" placeholder="08xxxxxxxxxx" value="<?= htmlspecialchars($_POST['telp'] ?? '') ?>" required>
      </div>
      <div>
        <label for="daftarAsal">Asal daerah</label>
        <input id="daftarAsal" name="asal_daerah" class="masukan" autocomplete="off" placeholder="cth. Bangkinang, Riau" maxlength="100" value="<?= htmlspecialchars($_POST['asal_daerah'] ?? '') ?>" required>
      </div>
    </div>

    <div class="label-grup" id="lblStatus">Status</div>
    <div class="segmen" style="--n:3" role="radiogroup" aria-labelledby="lblStatus">
      <?php foreach (opsiStatusPenghuni() as $kode => $label): ?>
        <label class="segmen-opsi">
          <input type="radio" name="status_penghuni" value="<?= $kode ?>" onchange="gantiLabelInstansi()" <?= (($_POST['status_penghuni'] ?? '') === $kode) ? 'checked' : '' ?> required>
          <span><?= $label ?></span>
        </label>
      <?php endforeach; ?>
    </div>

    <label for="daftarInstansi" id="labelInstansi">Kampus / tempat kerja</label>
    <input id="daftarInstansi" name="instansi" class="masukan" autocomplete="off" maxlength="100" placeholder="cth. Universitas Riau" value="<?= htmlspecialchars($_POST['instansi'] ?? '') ?>">

    <div class="seksi-judul">
      <span class="no">3</span>
      <div><b>Data wali</b><small>Kontak keluarga untuk keadaan darurat</small></div>
    </div>
    <div class="baris-2">
      <div>
        <label for="daftarWali">Nama wali</label>
        <input id="daftarWali" name="nama_wali" class="masukan" autocomplete="off" maxlength="100" placeholder="cth. Bapak Ahmad" value="<?= htmlspecialchars($_POST['nama_wali'] ?? '') ?>" required>
      </div>
      <div>
        <label for="daftarHpWali">No. HP wali</label>
        <input id="daftarHpWali" name="no_hp_wali" class="masukan" type="tel" inputmode="numeric" autocomplete="off" placeholder="08xxxxxxxxxx" value="<?= htmlspecialchars($_POST['no_hp_wali'] ?? '') ?>" required>
      </div>
    </div>
    <div class="catatan-kotak">Nomor wali hanya dilihat pengelola kos dan dipakai bila ada keadaan darurat.</div>

    <p class="salah" id="daftarSalah"><?= htmlspecialchars($daftarSalah) ?></p>
    <button type="submit" class="tombol hijau penuh">Buat Akun →</button>
    <p class="ganti-tab">Sudah punya akun? <a href="#" onclick="gantiTab('masuk'); return false;">Masuk di sini</a></p>
  </form>
 </div>
</section>

<script>
document.addEventListener('DOMContentLoaded', function () {
  gantiLabelInstansi();

  var err = document.querySelector('#daftarSalah:not(:empty), #loginSalah:not(:empty)');
  if (err && err.scrollIntoView) err.scrollIntoView({ block: 'center' });
});
</script>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
