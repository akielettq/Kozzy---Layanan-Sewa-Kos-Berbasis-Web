<?php
// login.php — Halaman Masuk & Pendaftaran Akun Kozzy
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

    // ====== 1. PROSES MASUK ======
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
                // Regenerasi session ID untuk cegah session fixation
                session_regenerate_id(true);

                // Sesi berhasil dibuat
                $_SESSION['user'] = [
                    'id_pengguna' => $user['id_pengguna'],
                    'nama'        => $user['nama'],
                    'email'       => $user['email'],
                    'no_telepon'  => $user['no_telepon'],
                    'peran'       => $user['peran']
                ];
                setFlash("Selamat datang kembali, " . htmlspecialchars($user['nama']) . "!", "hijau");
                
                // Alihkan ke URL tujuan yang telah disanitasi
                $tujuan = sanitasiUrlBalik($balik);
                header("Location: " . $tujuan);
                exit;
            } else {
                $loginSalah = 'Email atau password salah.';
            }
        }
    }

    // ====== 2. PROSES DAFTAR ======
    if ($aksi === 'daftar') {
        $tabAktif = 'daftar';
        $nama  = trim($_POST['nama'] ?? '');
        $email = trim(strtolower($_POST['email'] ?? ''));
        $telp  = trim($_POST['telp'] ?? '');
        $sandi = trim($_POST['sandi'] ?? '');

        if (strlen($nama) < 3) {
            $daftarSalah = 'Nama lengkap minimal 3 karakter.';
        } elseif (!filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $daftarSalah = 'Format email belum benar.';
        } elseif (strpos($telp, '08') !== 0 || strlen($telp) < 10) {
            $daftarSalah = 'No. WhatsApp harus diawali 08 dan minimal 10 digit.';
        } elseif (strlen($sandi) < 6) {
            $daftarSalah = 'Password minimal 6 karakter.';
        } else {
            // Cek apakah email sudah terdaftar
            $stmtCek = $pdo->prepare("SELECT COUNT(*) FROM pengguna WHERE email = ?");
            $stmtCek->execute([$email]);
            if ($stmtCek->fetchColumn() > 0) {
                $daftarSalah = 'Email sudah terdaftar. Silakan masuk.';
            } else {
                // Simpan pengguna baru
                $stmtInsert = $pdo->prepare("
                    INSERT INTO pengguna (nama, email, password, no_telepon, peran) 
                    VALUES (?, ?, ?, ?, 'pencari')
                ");
                $stmtInsert->execute([$nama, $email, $sandi, $telp]);
                $idBaru = (int)$pdo->lastInsertId();

                // Regenerasi session ID untuk cegah session fixation
                session_regenerate_id(true);

                // Langsung login otomatis
                $_SESSION['user'] = [
                    'id_pengguna' => $idBaru,
                    'nama'        => $nama,
                    'email'       => $email,
                    'no_telepon'  => $telp,
                    'peran'       => 'pencari'
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

<section class="wadah hal-isi" style="max-width:480px">
  <h3 id="judulAuth"><?= $tabAktif === 'masuk' ? 'Masuk ke Kozzy' : 'Buat akun baru' ?></h3>
  <p class="sub-tengah" style="text-align:left">Masuk dulu untuk bisa booking kamar.</p>

  <div class="auth-tab">
    <button type="button" id="tabMasuk" class="<?= $tabAktif === 'masuk' ? 'aktif' : '' ?>" onclick="gantiTab('masuk')">Masuk</button>
    <button type="button" id="tabDaftar" class="<?= $tabAktif === 'daftar' ? 'aktif' : '' ?>" onclick="gantiTab('daftar')">Daftar</button>
  </div>

  <!-- Form Masuk -->
  <form id="formMasuk" method="POST" action="login.php" style="<?= $tabAktif === 'masuk' ? '' : 'display:none' ?>">
    <input type="hidden" name="aksi" value="login">
    <input type="hidden" name="balik" value="<?= htmlspecialchars($balik) ?>">
    
    <label>Email</label>
    <input id="loginEmail" name="email" class="masukan" placeholder="nama@email.com" value="<?= htmlspecialchars($_POST['email'] ?? '') ?>" required>
    
    <label>Password</label>
    <input id="loginPass" name="password" class="masukan" type="password" placeholder="••••••" required>
    
    <p class="salah" id="loginSalah"><?= htmlspecialchars($loginSalah) ?></p>
    <button type="submit" class="tombol hijau penuh">Masuk →</button>
  </form>

  <!-- Form Daftar -->
  <form id="formDaftar" method="POST" action="login.php" style="<?= $tabAktif === 'daftar' ? '' : 'display:none' ?>">
    <input type="hidden" name="aksi" value="daftar">
    <input type="hidden" name="balik" value="<?= htmlspecialchars($balik) ?>">
    
    <label>Nama lengkap</label>
    <input id="daftarNama" name="nama" class="masukan" placeholder="cth. Rani Putri" value="<?= htmlspecialchars($_POST['nama'] ?? '') ?>" required>
    
    <label>Email</label>
    <input id="daftarEmail" name="email" type="email" class="masukan" placeholder="nama@email.com" value="<?= htmlspecialchars($_POST['email'] ?? '') ?>" required>
    
    <label>No. WhatsApp</label>
    <input id="daftarTelp" name="telp" class="masukan" placeholder="08xxxxxxxxxx" value="<?= htmlspecialchars($_POST['telp'] ?? '') ?>" required>
    
    <label>Password (minimal 6 karakter)</label>
    <input id="daftarSandi" name="sandi" class="masukan" type="password" placeholder="••••••" required>
    
    <p class="salah" id="daftarSalah"><?= htmlspecialchars($daftarSalah) ?></p>
    <button type="submit" class="tombol hijau penuh">Buat Akun →</button>
  </form>
</section>

<?php require_once __DIR__ . '/includes/footer.php'; ?>
