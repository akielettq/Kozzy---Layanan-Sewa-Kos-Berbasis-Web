<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../includes/helper.php';

// Test 1: Cari akun admin
$stmt = $pdo->prepare("SELECT * FROM pengguna WHERE email = ?");
$stmt->execute(['admin@kozzy.id']);
$admin = $stmt->fetch();
assert($admin !== false, "Admin user should exist in DB");
assert($admin['peran'] === 'admin', "Role should be admin");

// Test 2: Cari akun pencari
$stmt->execute(['farah@kozzy.id']);
$farah = $stmt->fetch();
assert($farah !== false, "Farah user should exist in DB");
assert($farah['peran'] === 'pencari', "Role should be pencari");

echo "Auth Unit Tests Passed!\n";
