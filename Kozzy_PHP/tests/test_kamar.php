<?php
require_once __DIR__ . '/../config/database.php';

$stmt = $pdo->query("
    SELECT k.*, 
           GROUP_CONCAT(f.nama_fasilitas ORDER BY f.id_fasilitas SEPARATOR ', ') AS daftar_fasilitas
    FROM kamar k
    LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar
    LEFT JOIN fasilitas f ON fk.fasilitas_id_fasilitas = f.id_fasilitas
    GROUP BY k.kode_kamar
    ORDER BY (k.status = 'terisi') ASC, k.kode_kamar ASC
");
$rooms = $stmt->fetchAll();
assert(count($rooms) === 8, "Must return 8 rooms");
assert(!empty($rooms[0]['daftar_fasilitas']), "First room must have joined facilities");
echo "Kamar Query Test Passed! (Rooms: " . count($rooms) . ")\n";
