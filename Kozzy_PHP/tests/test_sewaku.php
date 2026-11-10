<?php
require_once __DIR__ . '/../config/database.php';

$stmt = $pdo->prepare("
    SELECT b.*, k.tipe AS tipe_kamar, k.harga, m.nama_metode
    FROM booking b
    JOIN kamar k ON b.kode_kamar = k.kode_kamar
    JOIN metode_pembayaran m ON b.id_metode = m.id_metode
    WHERE b.id_pengguna = ?
");
$stmt->execute([3]); // Fildza
$bookings = $stmt->fetchAll();
assert(count($bookings) >= 1, "Fildza should have at least 1 booking");
assert($bookings[0]['id_booking'] === 'KZ-0117', "Booking id should match KZ-0117");
echo "Sewaku Query Test Passed!\n";
