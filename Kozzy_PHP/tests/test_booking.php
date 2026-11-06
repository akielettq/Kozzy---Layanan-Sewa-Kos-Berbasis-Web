<?php
require_once __DIR__ . '/../config/database.php';

// Test Transaction: Simulasi insert booking & bukti lalu rollback
$pdo->beginTransaction();
$testId = "KZ-TEST";
$stmt = $pdo->prepare("
    INSERT INTO booking (id_booking, tanggal_mulai, durasi, status, id_pengguna, kode_kamar, id_metode, created_at)
    VALUES (?, CURDATE(), 1, 'pending', 2, 'A-01', 1, NOW())
");
$stmt->execute([$testId]);

$stmtBukti = $pdo->prepare("INSERT INTO bukti_pembayaran (file_bukti, id_booking) VALUES (?, ?)");
$stmtBukti->execute(['assets/uploads/bukti/test.jpg', $testId]);

$count = $pdo->query("SELECT COUNT(*) FROM booking WHERE id_booking = '$testId'")->fetchColumn();
assert((int)$count === 1, "Booking should be visible within transaction");

$pdo->rollBack();

$countAfter = $pdo->query("SELECT COUNT(*) FROM booking WHERE id_booking = '$testId'")->fetchColumn();
assert((int)$countAfter === 0, "Booking should be gone after rollback");

echo "Booking Transaction Test Passed!\n";
