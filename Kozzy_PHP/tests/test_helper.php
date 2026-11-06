<?php
require_once __DIR__ . '/../config/database.php';
require_once __DIR__ . '/../includes/helper.php';

cekMasaSewa($pdo);

// 1. rupiah()
assert(rupiah(850000) === "Rp 850.000", "Format rupiah 850.000");
assert(rupiah(0) === "Rp 0", "Format rupiah 0");

// 2. formatTanggal()
assert(formatTanggal("2026-02-12") === "12 Feb 2026", "Format tanggal 2026-02-12");
assert(formatTanggal(null) === "—", "Format tanggal null");

// 3. punyaSewaAktif()
// Farah (id 2) has pending KZ-0118
$farahSewa = punyaSewaAktif($pdo, 2);
assert($farahSewa !== null, "Farah harus punya sewa aktif (pending)");
assert($farahSewa['id_booking'] === 'KZ-0118', "ID booking pending harus KZ-0118");

// Akiel (admin, id 1) has no bookings
$adminSewa = punyaSewaAktif($pdo, 1);
assert($adminSewa === null, "Admin tidak punya sewa aktif");

// 4. adaPendingDiKamar()
assert(adaPendingDiKamar($pdo, 'A-03') === true, "A-03 has pending booking KZ-0118");
assert(adaPendingDiKamar($pdo, 'A-01') === false, "A-01 has no pending booking");

echo rupiah(850000) . " | " . formatTanggal("2026-02-12") . " | Helper All Tests Passed OK\n";
