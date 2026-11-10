<?php
// includes/helper.php — Fungsi Bantuan (Helper) Sistem Kozzy

/**
 * Format angka ke bentuk Rupiah Indonesia (cth: Rp 850.000)
 */
function rupiah($nominal): string {
    return "Rp " . number_format((float)$nominal, 0, ',', '.');
}

/**
 * Format tanggal ISO (YYYY-MM-DD) ke format Indonesia (cth: 12 Feb 2026)
 */
function formatTanggal(?string $iso): string {
    if (!$iso) return "—";
    $bulanNama = [1 => "Jan", "Feb", "Mar", "Apr", "Mei", "Jun", "Jul", "Agu", "Sep", "Okt", "Nov", "Des"];
    $waktu = strtotime($iso);
    if (!$waktu) return $iso;
    $tgl = (int)date('j', $waktu);
    $bln = (int)date('n', $waktu);
    $thn = date('Y', $waktu);
    return "$tgl " . ($bulanNama[$bln] ?? "") . " $thn";
}

/**
 * Otomatis mengosongkan kamar yang masa sewanya telah kedaluwarsa (expired)
 */
function cekMasaSewa(PDO $pdo): void {
    try {
        $stmt = $pdo->prepare("UPDATE kamar SET status = 'tersedia', terisi_hingga = NULL WHERE status = 'terisi' AND terisi_hingga IS NOT NULL AND terisi_hingga < CURDATE()");
        $stmt->execute();
    } catch (Exception $e) {
        // Log atau biarkan lewat jika error minor
    }
}

/**
 * Menyimpan pesan flash sementara ke sesi
 */
function setFlash(string $pesan, string $tipe = 'info'): void {
    $_SESSION['flash'] = [
        'pesan' => $pesan,
        'tipe'  => $tipe
    ];
}

/**
 * Mengambil pesan flash dan langsung menghapusnya dari sesi
 */
function getFlash(): ?array {
    if (isset($_SESSION['flash'])) {
        $flash = $_SESSION['flash'];
        unset($_SESSION['flash']);
        return $flash;
    }
    return null;
}

/**
 * Mendapatkan data pengguna yang sedang login
 */
function userLogin(): ?array {
    return $_SESSION['user'] ?? null;
}

/**
 * Memastikan pengguna telah login, dan opsional membatasi peran (admin / pencari)
 */
function wajibLogin(?string $peran = null): void {
    $user = userLogin();
    if (!$user) {
        $tujuan = $_SERVER['REQUEST_URI'] ?? 'index.php';
        setFlash("Silakan masuk terlebih dahulu untuk mengakses halaman ini.", "kuning");
        header("Location: login.php?balik=" . urlencode($tujuan));
        exit;
    }
    if ($peran !== null && $user['peran'] !== $peran) {
        setFlash("Akses ditolak: Anda tidak memiliki izin untuk halaman ini.", "merah");
        header("Location: index.php");
        exit;
    }
}

/**
 * Memeriksa apakah pengguna memiliki sewa aktif
 * (pending = sedang diproses, approved + kamar masih terisi = sedang menghuni)
 */
function punyaSewaAktif(PDO $pdo, int $id_pengguna): ?array {
    $stmt = $pdo->prepare("
        SELECT b.*, k.status AS status_kamar, k.terisi_hingga
        FROM booking b
        JOIN kamar k ON b.kode_kamar = k.kode_kamar
        WHERE b.id_pengguna = ? AND (
            b.status = 'pending' OR 
            (b.status = 'approved' AND k.status = 'terisi' AND DATE_ADD(b.tanggal_mulai, INTERVAL b.durasi MONTH) >= CURDATE())
        )
        ORDER BY b.created_at DESC
        LIMIT 1
    ");
    $stmt->execute([$id_pengguna]);
    $res = $stmt->fetch();
    return $res ?: null;
}

/**
 * Memeriksa apakah kamar sedang memiliki pengajuan booking lain yang pending
 */
function adaPendingDiKamar(PDO $pdo, string $kode_kamar): bool {
    $stmt = $pdo->prepare("SELECT COUNT(*) FROM booking WHERE kode_kamar = ? AND status = 'pending'");
    $stmt->execute([$kode_kamar]);
    return ((int)$stmt->fetchColumn()) > 0;
}

/**
 * Mencegah Open Redirect dengan membatasi URL balik hanya path internal aplikasi
 */
function sanitasiUrlBalik(?string $url): string {
    if (empty($url)) return 'index.php';
    $url = trim($url);
    // Tolak skema protokol (http:, https:, javascript:) atau protocol-relative (//)
    if (preg_match('#^(https?:)?//#i', $url) || preg_match('#^[a-zA-Z0-9_-]+:#i', $url)) {
        return 'index.php';
    }
    return $url;
}

/**
 * Validasi MIME-type biner asli file gambar
 */
function validasiMimeGambar(string $filePath): bool {
    if (!file_exists($filePath) || !is_readable($filePath)) return false;
    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mime = finfo_file($finfo, $filePath);
    finfo_close($finfo);
    $allowedMimes = ['image/jpeg', 'image/png', 'image/webp'];
    return in_array($mime, $allowedMimes, true);
}

/**
 * Mendapatkan atau membuat CSRF token untuk sesi saat ini
 */
function csrfToken(): string {
    if (empty($_SESSION['csrf_token'])) {
        $_SESSION['csrf_token'] = bin2hex(random_bytes(32));
    }
    return $_SESSION['csrf_token'];
}

/**
 * Memvalidasi token CSRF yang dikirimkan melalui form POST
 */
function validasiCsrf(?string $token): bool {
    if (empty($_SESSION['csrf_token']) || empty($token)) return false;
    return hash_equals($_SESSION['csrf_token'], $token);
}
