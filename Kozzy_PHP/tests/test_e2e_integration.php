<?php
// tests/test_e2e_integration.php — Pengujian Integrasi End-to-End Kozzy

$baseUrl = "http://127.0.0.1:8088";

function httpReq($url, $method = 'GET', $data = [], $cookieFile = null) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, false);
    curl_setopt($ch, CURLOPT_HEADER, true);

    if ($cookieFile) {
        curl_setopt($ch, CURLOPT_COOKIEJAR, $cookieFile);
        curl_setopt($ch, CURLOPT_COOKIEFILE, $cookieFile);
    }

    if ($method === 'POST') {
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
    }

    $response = curl_exec($ch);
    $httpCode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
    $header = substr($response, 0, $headerSize);
    $body = substr($response, $headerSize);
    curl_close($ch);

    return [
        'code'   => $httpCode,
        'header' => $header,
        'body'   => $body
    ];
}

echo "=== MEMULAI TEST INTEGRASI END-TO-END KOZZY ===\n";

// 1. Test Halaman Beranda (index.php)
$resIndex = httpReq("$baseUrl/index.php");
assert($resIndex['code'] === 200, "Beranda harus mengembalikan status 200");
assert(strpos($resIndex['body'], "Cari kos nggak perlu") !== false, "Beranda harus memuat hero header");
assert(strpos($resIndex['body'], "kamar") !== false, "Beranda harus memuat statistik kamar");
echo "[PASS] 1. Beranda (index.php) berhasil diakses (HTTP 200).\n";

// 2. Test Halaman Kamar & Filter (kamar.php)
$resKamar = httpReq("$baseUrl/kamar.php");
assert($resKamar['code'] === 200, "Katalog kamar harus 200");
assert(strpos($resKamar['body'], "A-01") !== false, "Kamar A-01 harus tampil");
assert(strpos($resKamar['body'], "Wi-Fi, Kasur, Lemari, AC") !== false, "Fasilitas kamar hasil relasi harus tampil");

$resPutri = httpReq("$baseUrl/kamar.php?tipe=putri");
assert($resPutri['code'] === 200, "Filter putri harus 200");
assert(strpos($resPutri['body'], "B-01") !== false, "Kamar B-01 harus tampil");
assert(strpos($resPutri['body'], "A-01") === false, "Kamar putra A-01 tidak boleh muncul di filter putri");
echo "[PASS] 2. Katalog Kamar (kamar.php) & Filter berhasil diuji.\n";

// 3. Test Autentikasi: Login Akun Admin & Pencari
$cookieAdmin = __DIR__ . "/cookie_admin.txt";
$cookiePencari = __DIR__ . "/cookie_pencari.txt";
if (file_exists($cookieAdmin)) unlink($cookieAdmin);
if (file_exists($cookiePencari)) unlink($cookiePencari);

// Login Admin
$resLoginAdmin = httpReq("$baseUrl/login.php", 'POST', [
    'aksi'     => 'login',
    'email'    => 'admin@kozzy.id',
    'password' => 'admin123',
    'balik'    => 'admin.php'
], $cookieAdmin);
assert($resLoginAdmin['code'] === 302, "Login admin harus me-redirect (302)");

// Akses Panel Admin dengan cookie admin
$resAdminPanel = httpReq("$baseUrl/admin.php", 'GET', [], $cookieAdmin);
assert($resAdminPanel['code'] === 200, "Panel admin harus dapat dibuka oleh admin (200)");
assert(strpos($resAdminPanel['body'], "Panel Admin") !== false, "Harus memuat header Panel Admin");
preg_match('/name="csrf_token" value="([^"]+)"/', $resAdminPanel['body'], $tokenMatch);
$csrfToken = $tokenMatch[1] ?? '';
assert(!empty($csrfToken), "Panel admin harus memuat CSRF token");
echo "[PASS] 3. Login Admin & Hak Akses Panel Admin berhasil diuji.\n";

// Login Pencari (Farah)
$resLoginFarah = httpReq("$baseUrl/login.php", 'POST', [
    'aksi'     => 'login',
    'email'    => 'farah@kozzy.id',
    'password' => '123456',
    'balik'    => 'sewaku.php'
], $cookiePencari);
assert($resLoginFarah['code'] === 302, "Login farah harus me-redirect (302)");

// Akses Sewaku dengan cookie pencari
$resSewaku = httpReq("$baseUrl/sewaku.php", 'GET', [], $cookiePencari);
assert($resSewaku['code'] === 200, "Halaman Sewaku harus 200");
assert(strpos($resSewaku['body'], "KZ-0118") !== false, "Booking pending Farah KZ-0118 harus tampil di sewaku");
echo "[PASS] 4. Login Pencari & Halaman Sewaku berhasil diuji.\n";

// 5. Test Proteksi Keamanan: Pencari mencoba buka admin.php
$resPencariBukaAdmin = httpReq("$baseUrl/admin.php", 'GET', [], $cookiePencari);
assert(strpos($resPencariBukaAdmin['body'], "Khusus Admin") !== false, "Pencari tidak boleh dapat melihat isi panel admin");
echo "[PASS] 5. Proteksi Role-Based Access Control (RBAC) berhasil mencegah akses tidak sah.\n";

// 6. Test Transaksi Verifikasi Admin: Setujui KZ-0118
$resApprove = httpReq("$baseUrl/admin.php", 'POST', [
    'csrf_token' => $csrfToken,
    'aksi'       => 'approve',
    'id_booking' => 'KZ-0118'
], $cookieAdmin);
assert($resApprove['code'] === 302, "Approve harus me-redirect");

// Cek di database apakah status berubah jadi approved dan kamar A-03 jadi terisi
require_once __DIR__ . '/../config/database.php';
$cekB = $pdo->query("SELECT * FROM booking WHERE id_booking = 'KZ-0118'")->fetch();
assert($cekB['status'] === 'approved', "Status booking KZ-0118 harus approved");

$cekK = $pdo->query("SELECT * FROM kamar WHERE kode_kamar = 'A-03'")->fetch();
assert($cekK['status'] === 'terisi', "Kamar A-03 sekarang harus berstatus terisi");
assert(!empty($cekK['terisi_hingga']), "Tanggal terisi_hingga kamar A-03 harus terisi");

$cekLog = $pdo->query("SELECT * FROM riwayat_verifikasi WHERE id_booking = 'KZ-0118'")->fetch();
assert($cekLog !== false, "Audit log verifikasi harus tercatat");
assert($cekLog['keputusan'] === 'approved', "Keputusan log harus approved");
echo "[PASS] 6. Verifikasi Persetujuan Admin & Transaksi Relasional 3 Tabel berhasil dieksekusi.\n";

// Kembalikan status KZ-0118 untuk konsistensi data awal
$pdo->prepare("UPDATE booking SET status = 'pending' WHERE id_booking = 'KZ-0118'")->execute();
$pdo->prepare("UPDATE kamar SET status = 'tersedia', terisi_hingga = NULL WHERE kode_kamar = 'A-03'")->execute();
$pdo->prepare("DELETE FROM riwayat_verifikasi WHERE id_booking = 'KZ-0118'")->execute();

// Bersihkan cookie file
if (file_exists($cookieAdmin)) unlink($cookieAdmin);
if (file_exists($cookiePencari)) unlink($cookiePencari);

echo "\n=== SELURUH PENGUJIAN END-TO-END BERHASIL 100% ===\n";
