<?php
// tests/test_security_fixes.php — TDD for Important Security Fixes
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../includes/helper.php';

echo "=== TDD: SECURITY FIXES ===\n";

// 1. URL Balik Sanitization (Open Redirect Prevention)
assert(function_exists('sanitasiUrlBalik'), "Fungsi sanitasiUrlBalik harus didefinisikan");
assert(sanitasiUrlBalik('https://evil.com') === 'index.php', "External HTTPS URL harus ditolak");
assert(sanitasiUrlBalik('http://evil.com') === 'index.php', "External HTTP URL harus ditolak");
assert(sanitasiUrlBalik('//evil.com') === 'index.php', "Protocol-relative URL harus ditolak");
assert(sanitasiUrlBalik('javascript:alert(1)') === 'index.php', "Javascript scheme harus ditolak");
assert(sanitasiUrlBalik('booking.php?kode=A-01') === 'booking.php?kode=A-01', "Relative path sah harus diizinkan");
assert(sanitasiUrlBalik('admin.php') === 'admin.php', "Relative path admin.php harus diizinkan");
assert(sanitasiUrlBalik('') === 'index.php', "Empty URL default ke index.php");
echo "[PASS] 1. sanitasiUrlBalik works as expected.\n";

// 2. MIME Type Validation
assert(function_exists('validasiMimeGambar'), "Fungsi validasiMimeGambar harus didefinisikan");
$realImg = __DIR__ . '/../putra.jpg';
assert(validasiMimeGambar($realImg) === true, "putra.jpg adalah gambar JPEG valid");

$fakeImg = __DIR__ . '/fake_image.jpg';
file_put_contents($fakeImg, "<?php echo 'malicious php'; ?>");
$isFakeValid = validasiMimeGambar($fakeImg);
unlink($fakeImg);
assert($isFakeValid === false, "File teks palsu berekstensi .jpg harus ditolak");
echo "[PASS] 2. validasiMimeGambar works as expected.\n";

// 3. CSRF Protection
assert(function_exists('csrfToken'), "Fungsi csrfToken harus didefinisikan");
assert(function_exists('validasiCsrf'), "Fungsi validasiCsrf harus didefinisikan");
$token = csrfToken();
assert(!empty($token) && strlen($token) >= 32, "CSRF token harus string acak min 32 kar");
assert(validasiCsrf($token) === true, "Token asli harus valid");
assert(validasiCsrf('invalid_token_123') === false, "Token palsu harus invalid");
assert(validasiCsrf('') === false, "Token kosong harus invalid");
echo "[PASS] 3. CSRF token generation and validation works as expected.\n";

echo "=== ALL SECURITY TDD TESTS PASSED ===\n";
