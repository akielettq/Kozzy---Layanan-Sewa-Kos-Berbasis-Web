<?php
require_once __DIR__ . '/../config/database.php';

// Simulasi aksi admin: Setujui KZ-0118 (pending di A-03)
$pdo->beginTransaction();

$b = $pdo->query("SELECT * FROM booking WHERE id_booking = 'KZ-0118'")->fetch();
assert($b !== false, "KZ-0118 should exist");
assert($b['status'] === 'pending', "KZ-0118 should be pending");

// Approve
$pdo->prepare("UPDATE booking SET status = 'approved' WHERE id_booking = 'KZ-0118'")->execute();
$pdo->prepare("UPDATE kamar SET status = 'terisi', terisi_hingga = DATE_ADD(CURDATE(), INTERVAL 1 MONTH) WHERE kode_kamar = 'A-03'")->execute();
$pdo->prepare("INSERT INTO riwayat_verifikasi (keputusan, tanggal_keputusan, catatan, id_pengguna, id_booking) VALUES ('approved', NOW(), 'Test approve', 1, 'KZ-0118')")->execute();

$k = $pdo->query("SELECT * FROM kamar WHERE kode_kamar = 'A-03'")->fetch();
assert($k['status'] === 'terisi', "Kamar A-03 should become terisi");

$rv = $pdo->query("SELECT * FROM riwayat_verifikasi WHERE id_booking = 'KZ-0118'")->fetch();
assert($rv !== false, "Riwayat verifikasi record should be created");

$pdo->rollBack(); // Kembalikan ke state awal

echo "Admin Verification Workflow Test Passed!\n";
