<?php
// tests/test_xampp_apache.php
$baseUrl = 'http://127.0.0.1/kozzy%20v1';
$cookie = __DIR__ . '/cookie_xampp.txt';
if (file_exists($cookie)) unlink($cookie);

function req($url, $post = null, $cookie = null) {
    $ch = curl_init();
    curl_setopt($ch, CURLOPT_URL, $url);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch, CURLOPT_FOLLOWLOCATION, false);
    curl_setopt($ch, CURLOPT_HEADER, true);
    if ($cookie) {
        curl_setopt($ch, CURLOPT_COOKIEJAR, $cookie);
        curl_setopt($ch, CURLOPT_COOKIEFILE, $cookie);
    }
    if ($post) {
        curl_setopt($ch, CURLOPT_POST, true);
        curl_setopt($ch, CURLOPT_POSTFIELDS, $post);
    }
    $res = curl_exec($ch);
    $code = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $headerSize = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
    $header = substr($res, 0, $headerSize);
    $body = substr($res, $headerSize);
    curl_close($ch);
    return ['code' => $code, 'header' => $header, 'body' => $body];
}

echo "Testing Apache Port 80 at $baseUrl\n";

// 1. Index
$r1 = req("$baseUrl/index.php");
echo "1. Index: HTTP {$r1['code']} (expected 200)\n";
assert($r1['code'] === 200, "Index should be 200");

// 2. Kamar
$r2 = req("$baseUrl/kamar.php");
echo "2. Kamar: HTTP {$r2['code']} (expected 200)\n";
assert($r2['code'] === 200, "Kamar should be 200");

// 3. Login as Admin
$r3 = req("$baseUrl/login.php", [
    'aksi' => 'login',
    'email' => 'admin@kozzy.id',
    'password' => 'admin123',
    'balik' => 'admin.php'
], $cookie);
echo "3. Login Admin: HTTP {$r3['code']} (expected 302)\n";
assert($r3['code'] === 302, "Login should redirect");

// 4. Access Admin Panel
$r4 = req("$baseUrl/admin.php", null, $cookie);
echo "4. Admin Panel: HTTP {$r4['code']} (expected 200)\n";
assert($r4['code'] === 200, "Admin panel should be 200");
assert(strpos($r4['body'], "Panel Admin") !== false, "Must contain Panel Admin");

// 5. Logout
$r5 = req("$baseUrl/logout.php", null, $cookie);
echo "5. Logout: HTTP {$r5['code']} (expected 302)\n";
assert($r5['code'] === 302, "Logout should redirect");

// 6. Access Admin Panel again (should redirect to login or show denied)
$r6 = req("$baseUrl/admin.php", null, $cookie);
echo "6. Access Admin after logout: HTTP {$r6['code']} (expected 302)\n";
assert($r6['code'] === 302, "Admin after logout should redirect to login.php");

// 7. Login as Farah (tenant)
$r7 = req("$baseUrl/login.php", [
    'aksi' => 'login',
    'email' => 'farah@kozzy.id',
    'password' => '123456',
    'balik' => 'sewaku.php'
], $cookie);
echo "7. Login Tenant (Farah): HTTP {$r7['code']} (expected 302)\n";
assert($r7['code'] === 302, "Login farah should redirect");

// 8. Access Sewaku
$r8 = req("$baseUrl/sewaku.php", null, $cookie);
echo "8. Sewaku: HTTP {$r8['code']} (expected 200)\n";
assert($r8['code'] === 200, "Sewaku should be 200");
assert(strpos($r8['body'], "KZ-0118") !== false, "Sewaku must show pending booking KZ-0118");

// 9. Access Profile
$r9 = req("$baseUrl/profil.php", null, $cookie);
echo "9. Profil: HTTP {$r9['code']} (expected 200)\n";
assert($r9['code'] === 200, "Profil should be 200");
assert(strpos($r9['body'], "Farah") !== false, "Profil must show Farah");

if (file_exists($cookie)) unlink($cookie);
echo "\nALL XAMPP APACHE TESTS PASSED SUCCESSFULLY!\n";
