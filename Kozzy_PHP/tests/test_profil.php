<?php
require_once __DIR__ . '/../config/database.php';

// Test hitung statistik untuk user id 3 (Fildza: 1 booking approved seharga 1150000)
$stmt = $pdo->prepare("
    SELECT 
        COUNT(*) AS total_booking,
        COALESCE(SUM(CASE WHEN b.status = 'approved' THEN k.harga * b.durasi ELSE 0 END), 0) AS total_dibayar
    FROM booking b
    JOIN kamar k ON b.kode_kamar = k.kode_kamar
    WHERE b.id_pengguna = ?
");
$stmt->execute([3]);
$stat = $stmt->fetch();
assert((int)$stat['total_booking'] >= 1, "Fildza should have >= 1 booking");
assert((int)$stat['total_dibayar'] === 1150000, "Fildza total paid should be 1,150,000");
echo "Profile Stats Test Passed!\n";
