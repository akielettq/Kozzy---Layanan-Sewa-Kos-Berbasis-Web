<?php
// logout.php — Mengakhiri Sesi Pengguna
require_once __DIR__ . '/config/database.php';
require_once __DIR__ . '/includes/helper.php';

session_unset();
session_destroy();

// Mulai sesi baru untuk menampung pesan flash
session_start();
setFlash("Kamu telah keluar dari akun.", "info");

header("Location: index.php");
exit;
