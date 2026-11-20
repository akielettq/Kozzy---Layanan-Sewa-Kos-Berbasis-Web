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

    // Terpisah agar kegagalan di sini (mis. migrasi status belum dijalankan) tidak mengganggu pembaruan kamar
    try {
        kedaluwarsakanPengajuan($pdo);
    } catch (Exception $e) {
        // Dibiarkan lewat
    }
}

// Batas waktu verifikasi: pengajuan 'pending' yang lebih lama dari ini otomatis menjadi 'expired'
const BATAS_JAM_PENGAJUAN = 48;

/**
 * Mengubah pengajuan yang tidak diverifikasi dalam BATAS_JAM_PENGAJUAN jam menjadi 'expired'
 * sehingga kamar tidak tertahan selamanya. Mengembalikan jumlah pengajuan yang kedaluwarsa.
 */
function kedaluwarsakanPengajuan(PDO $pdo): int {
    $jam = (int)BATAS_JAM_PENGAJUAN;
    $stmt = $pdo->prepare("
        UPDATE booking
        SET status = 'expired', alasan = ?
        WHERE status = 'pending' AND created_at < (NOW() - INTERVAL $jam HOUR)
    ");
    $stmt->execute(["Pengajuan kedaluwarsa karena belum diverifikasi dalam $jam jam."]);
    return $stmt->rowCount();
}

/**
 * Teks sisa waktu verifikasi dari jumlah menit (dihitung di SQL agar zona waktu PHP & MySQL tidak bentrok)
 */
function labelSisaWaktu(int $menit): string {
    if ($menit <= 0)  return "segera kedaluwarsa";
    if ($menit < 60)  return "kurang dari 1 jam";
    $jamTotal = intdiv($menit, 60);
    if ($jamTotal < 72) return "$jamTotal jam"; // mis. batas 48 jam tampil "47 jam", bukan "1 hari 23 jam"
    $hari = intdiv($jamTotal, 24);
    $jam  = $jamTotal % 24;
    return trim("$hari hari " . ($jam > 0 ? "$jam jam" : ""));
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

// ---------------------------------------------------------------------
// UPLOAD GAMBAR (bukti pembayaran, foto keluhan)
// ---------------------------------------------------------------------
const BUKTI_MAKS_BYTE = 2 * 1024 * 1024; // 2 MB
const BUKTI_MAKS_PIXEL = 8000;

/**
 * Memeriksa file gambar: ukuran, MIME asli (bukan dari nama/ekstensi), dan keterbacaan sebagai gambar.
 * Mengembalikan [ekstensi_aman, pesan_error]. Ekstensi ditentukan dari isi file, bukan dari nama file pengguna.
 * $label dipakai di pesan error, mis. "bukti pembayaran" atau "foto keluhan".
 */
function validasiBuktiUpload(string $tmpPath, string $label = 'bukti pembayaran'): array {
    if (!is_file($tmpPath) || !is_readable($tmpPath)) {
        return ['', "Berkas $label tidak dapat dibaca."];
    }
    $ukuran = filesize($tmpPath);
    if ($ukuran === false || $ukuran <= 0) {
        return ['', "Berkas $label kosong."];
    }
    if ($ukuran > BUKTI_MAKS_BYTE) {
        return ['', "Ukuran file $label maksimal 2MB."];
    }

    $finfo = finfo_open(FILEINFO_MIME_TYPE);
    $mime = finfo_file($finfo, $tmpPath);
    finfo_close($finfo);
    $peta = ['image/jpeg' => 'jpg', 'image/png' => 'png', 'image/webp' => 'webp'];
    if (!isset($peta[$mime])) {
        return ['', "Format $label harus JPG, PNG, atau WEBP."];
    }

    $info = @getimagesize($tmpPath);
    if ($info === false || ($info[0] ?? 0) < 1 || ($info[1] ?? 0) < 1) {
        return ['', 'Berkas bukan gambar yang valid.'];
    }
    if ($info[0] > BUKTI_MAKS_PIXEL || $info[1] > BUKTI_MAKS_PIXEL) {
        return ['', 'Dimensi gambar terlalu besar (maksimal ' . BUKTI_MAKS_PIXEL . ' piksel).'];
    }
    return [$peta[$mime], ''];
}

/**
 * Pesan ramah untuk kode error upload bawaan PHP
 */
function pesanErrorUpload(int $kode, string $label = 'bukti pembayaran'): string {
    switch ($kode) {
        case UPLOAD_ERR_INI_SIZE:
        case UPLOAD_ERR_FORM_SIZE: return "Ukuran file $label maksimal 2MB.";
        case UPLOAD_ERR_PARTIAL:   return 'Unggahan terputus. Silakan coba lagi.';
        default:                   return "Gagal mengunggah file $label (kode error: $kode).";
    }
}

/**
 * Membuat folder upload dan memasang .htaccess agar tidak ada script yang bisa dijalankan dari folder itu
 */
function siapkanFolderUpload(string $dir): void {
    if (!is_dir($dir)) {
        mkdir($dir, 0755, true);
    }
    $ht = rtrim($dir, '/\\') . '/.htaccess';
    if (!file_exists($ht)) {
        file_put_contents($ht, <<<'HT'
# Folder upload: tidak boleh menjalankan script apa pun, hanya menyajikan gambar
<IfModule mod_php.c>
  php_flag engine off
</IfModule>
<IfModule mod_php7.c>
  php_flag engine off
</IfModule>
<IfModule mod_php8.c>
  php_flag engine off
</IfModule>
RemoveHandler .php .phtml .php3 .php4 .php5 .php7 .phar
<FilesMatch "\.(php[0-9]?|phtml|phar|pl|py|cgi|sh)$">
  <IfModule mod_authz_core.c>
    Require all denied
  </IfModule>
  <IfModule !mod_authz_core.c>
    Order allow,deny
    Deny from all
  </IfModule>
</FilesMatch>
HT
        );
    }
}

/**
 * Memvalidasi lalu menyimpan gambar dari $_FILES ke assets/uploads/{bukti|keluhan}/.
 * Nama file dibuat acak dan ekstensinya ditentukan dari isi file.
 * Mengembalikan [path_relatif_atau_null, pesan_error].
 */
function simpanUploadGambar(array $file, string $subfolder): array {
    $label = ['bukti' => 'bukti pembayaran', 'keluhan' => 'foto keluhan'][$subfolder] ?? null;
    if ($label === null) {
        return [null, 'Tujuan penyimpanan tidak dikenal.'];
    }
    if (($file['error'] ?? UPLOAD_ERR_NO_FILE) !== UPLOAD_ERR_OK) {
        return [null, pesanErrorUpload((int)($file['error'] ?? UPLOAD_ERR_NO_FILE), $label)];
    }
    if (!is_uploaded_file($file['tmp_name'])) {
        return [null, 'Berkas tidak valid.'];
    }
    [$ext, $err] = validasiBuktiUpload($file['tmp_name'], $label);
    if ($err !== '') {
        return [null, $err];
    }

    $dir = dirname(__DIR__) . '/assets/uploads/' . $subfolder;
    siapkanFolderUpload($dir);
    $nama = $subfolder . '_' . bin2hex(random_bytes(8)) . '.' . $ext;
    if (!move_uploaded_file($file['tmp_name'], $dir . '/' . $nama)) {
        return [null, "Gagal mengunggah file $label ke server."];
    }
    @chmod($dir . '/' . $nama, 0644);
    return ["assets/uploads/$subfolder/$nama", ''];
}

/**
 * Bukti pembayaran booking (pembungkus simpanUploadGambar agar kode lama tetap berjalan)
 */
function simpanBuktiUpload(array $file): array {
    return simpanUploadGambar($file, 'bukti');
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


// =====================================================================
// DATA DIRI PENGGUNA (jenis kelamin, asal daerah, status, wali)
// =====================================================================

/**
 * Pilihan jenis kelamin (nilai di basis data => label tampilan)
 */
function opsiJenisKelamin(): array {
    return ['L' => 'Laki-laki', 'P' => 'Perempuan'];
}

/**
 * Pilihan status penghuni (nilai di basis data => label tampilan)
 */
function opsiStatusPenghuni(): array {
    return ['mahasiswa' => 'Mahasiswa', 'pekerja' => 'Pekerja', 'lainnya' => 'Lainnya'];
}

/**
 * Jenis kelamin -> tipe kos yang boleh dipesan (L = putra, P = putri). NULL jika belum diisi.
 */
function tipeKosUntuk(?string $jenisKelamin): ?string {
    if ($jenisKelamin === 'L') return 'putra';
    if ($jenisKelamin === 'P') return 'putri';
    return null;
}

/**
 * Merapikan nomor HP: buang spasi/strip/titik, ubah awalan +62 atau 62 menjadi 0
 */
function bersihkanNomorHp(string $nomor): string {
    $n = preg_replace('/[^0-9+]/', '', trim($nomor));
    if (strpos($n, '+62') === 0) {
        $n = '0' . substr($n, 3);
    } elseif (strpos($n, '62') === 0) {
        $n = '0' . substr($n, 2);
    }
    return preg_replace('/\D/', '', $n);
}

/**
 * Nomor HP Indonesia yang sah: diawali 08, total 10-14 digit
 */
function nomorHpValid(string $nomor): bool {
    return (bool)preg_match('/^08[0-9]{8,12}$/', $nomor);
}

/**
 * Mengambil data pengguna terbaru dari basis data (bukan dari sesi)
 */
function muatPengguna(PDO $pdo, int $idPengguna): ?array {
    $stmt = $pdo->prepare("SELECT * FROM pengguna WHERE id_pengguna = ?");
    $stmt->execute([$idPengguna]);
    $res = $stmt->fetch();
    return $res ?: null;
}

/**
 * Data diri dianggap lengkap jika jenis kelamin, asal daerah, dan HP wali sudah terisi.
 * Dipakai sebagai syarat sebelum booking kamar.
 */
function dataDiriLengkap(?array $pengguna): bool {
    if (!$pengguna) return false;
    return !empty($pengguna['jenis_kelamin'])
        && !empty($pengguna['asal_daerah'])
        && !empty($pengguna['no_hp_wali']);
}

/**
 * Validasi isian data diri (dipakai form Daftar dan form Edit Data Diri di profil).
 * Mengembalikan [data_bersih, pesan_error]; pesan_error kosong jika semua valid.
 */
function validasiDataDiri(array $in, bool $wajibJenisKelamin = true): array {
    $rapikan = function ($v) {
        return trim(preg_replace('/\s+/', ' ', (string)$v));
    };

    $d = [
        'no_telepon'      => bersihkanNomorHp((string)($in['telp'] ?? '')),
        'jenis_kelamin'   => trim((string)($in['jenis_kelamin'] ?? '')),
        'asal_daerah'     => $rapikan($in['asal_daerah'] ?? ''),
        'status_penghuni' => trim((string)($in['status_penghuni'] ?? '')),
        'instansi'        => $rapikan($in['instansi'] ?? ''),
        'nama_wali'       => $rapikan($in['nama_wali'] ?? ''),
        'no_hp_wali'      => bersihkanNomorHp((string)($in['no_hp_wali'] ?? '')),
    ];

    $error = '';
    if (!nomorHpValid($d['no_telepon'])) {
        $error = 'No. WhatsApp harus diawali 08 dan terdiri dari 10–14 digit.';
    } elseif ($wajibJenisKelamin && !isset(opsiJenisKelamin()[$d['jenis_kelamin']])) {
        $error = 'Pilih jenis kelamin terlebih dahulu.';
    } elseif (mb_strlen($d['asal_daerah']) < 3 || mb_strlen($d['asal_daerah']) > 100) {
        $error = 'Asal daerah wajib diisi (3–100 karakter), contoh: Pekanbaru, Riau.';
    } elseif (!isset(opsiStatusPenghuni()[$d['status_penghuni']])) {
        $error = 'Pilih status kamu: mahasiswa, pekerja, atau lainnya.';
    } elseif ($d['status_penghuni'] !== 'lainnya' && mb_strlen($d['instansi']) < 2) {
        $error = $d['status_penghuni'] === 'mahasiswa' ? 'Nama kampus wajib diisi.' : 'Nama tempat kerja wajib diisi.';
    } elseif (mb_strlen($d['instansi']) > 100) {
        $error = 'Nama kampus / tempat kerja maksimal 100 karakter.';
    } elseif (mb_strlen($d['nama_wali']) < 3 || mb_strlen($d['nama_wali']) > 100) {
        $error = 'Nama wali wajib diisi (minimal 3 karakter).';
    } elseif (!nomorHpValid($d['no_hp_wali'])) {
        $error = 'No. HP wali harus diawali 08 dan terdiri dari 10–14 digit.';
    } elseif ($d['no_hp_wali'] === $d['no_telepon']) {
        $error = 'No. HP wali harus berbeda dengan No. WhatsApp kamu.';
    }

    // Instansi bersifat opsional untuk status "lainnya"
    if ($d['instansi'] === '') {
        $d['instansi'] = null;
    }
    return [$d, $error];
}

// =====================================================================
// METODE PEMBAYARAN
// =====================================================================

/**
 * Label kategori pembayaran (nilai di basis data => judul & keterangan singkat)
 */
function infoKategoriBayar(): array {
    return [
        'bank'    => ['Transfer Bank', 'Pilih salah satu bank'],
        'ewallet' => ['E-Wallet',      'GoPay, DANA, atau OVO'],
        'qris'    => ['QRIS',          'Scan dari aplikasi apa pun'],
    ];
}

/**
 * Nama pendek untuk dropdown: "Transfer Bank Mandiri" -> "Mandiri", "E-Wallet DANA" -> "DANA"
 */
function namaPendekMetode(string $namaMetode): string {
    $pendek = preg_replace('/^(Transfer( Bank)?|E-Wallet)\s+/i', '', $namaMetode);
    return $pendek !== '' ? $pendek : $namaMetode;
}


// =====================================================================
// KELUHAN / LAPORAN KERUSAKAN
// =====================================================================

const KELUHAN_MAKS_AKTIF = 3;       // laporan yang belum selesai per penghuni
const KELUHAN_MIN_KARAKTER = 10;
const KELUHAN_MAKS_KARAKTER = 500;

/** Kategori keluhan (nilai di basis data => label) */
function opsiKategoriKeluhan(): array {
    return [
        'ac'         => 'AC / pendingin',
        'listrik'    => 'Listrik & lampu',
        'air'        => 'Air & kamar mandi',
        'wifi'       => 'Wi-Fi',
        'furnitur'   => 'Furnitur (kasur, lemari, meja)',
        'kebersihan' => 'Kebersihan',
        'lainnya'    => 'Lainnya',
    ];
}

/** Status keluhan (nilai di basis data => [warna lencana, label]) */
function opsiStatusKeluhan(): array {
    return [
        'baru'     => ['merah',  'Baru'],
        'diproses' => ['kuning', 'Diproses'],
        'selesai'  => ['hijau',  'Selesai'],
    ];
}

/**
 * Sewa yang sedang berjalan milik penghuni: sudah disetujui, kamar terisi, dan hari ini berada di dalam masa sewa.
 * Hanya penghuni seperti ini yang boleh melaporkan keluhan. Mengembalikan baris booking atau null.
 */
function sewaBerjalan(PDO $pdo, int $idPengguna): ?array {
    $stmt = $pdo->prepare("
        SELECT b.id_booking, b.kode_kamar, b.tanggal_mulai,
               DATE_ADD(b.tanggal_mulai, INTERVAL b.durasi MONTH) AS akhir_sewa
        FROM booking b
        JOIN kamar k ON b.kode_kamar = k.kode_kamar
        WHERE b.id_pengguna = ? AND b.status = 'approved' AND k.status = 'terisi'
          AND b.tanggal_mulai <= CURDATE()
          AND DATE_ADD(b.tanggal_mulai, INTERVAL b.durasi MONTH) >= CURDATE()
        ORDER BY b.tanggal_mulai DESC
        LIMIT 1
    ");
    $stmt->execute([$idPengguna]);
    return $stmt->fetch() ?: null;
}

/** Apakah tabel keluhan sudah ada (migrasi v4 sudah dijalankan)? Mencegah halaman error jika belum. */
function tabelKeluhanAda(PDO $pdo): bool {
    try {
        $pdo->query("SELECT 1 FROM keluhan LIMIT 1");
        return true;
    } catch (PDOException $e) {
        return false;
    }
}

/** Jumlah keluhan berstatus 'baru' (untuk lencana di navigasi admin). 0 jika tabel belum ada. */
function jumlahKeluhanBaru(PDO $pdo): int {
    try {
        return (int)$pdo->query("SELECT COUNT(*) FROM keluhan WHERE status = 'baru'")->fetchColumn();
    } catch (PDOException $e) {
        return 0;
    }
}

/**
 * Validasi isian keluhan. Mengembalikan [data_bersih, pesan_error]; pesan_error kosong jika valid.
 */
function validasiKeluhan(array $in): array {
    $kategori = trim((string)($in['kategori'] ?? ''));
    $deskripsi = str_replace(["\r\n", "\r"], "\n", (string)($in['deskripsi'] ?? ''));
    $deskripsi = preg_replace('/[ \t]+/', ' ', $deskripsi);       // rapikan spasi berlebih
    $deskripsi = preg_replace('/\n{3,}/', "\n\n", trim($deskripsi)); // maksimal satu baris kosong

    $error = '';
    if (!isset(opsiKategoriKeluhan()[$kategori])) {
        $error = 'Pilih kategori keluhan terlebih dahulu.';
    } elseif (mb_strlen($deskripsi) < KELUHAN_MIN_KARAKTER) {
        $error = 'Jelaskan keluhanmu minimal ' . KELUHAN_MIN_KARAKTER . ' karakter agar mudah ditangani.';
    } elseif (mb_strlen($deskripsi) > KELUHAN_MAKS_KARAKTER) {
        $error = 'Deskripsi keluhan maksimal ' . KELUHAN_MAKS_KARAKTER . ' karakter.';
    }
    return [['kategori' => $kategori, 'deskripsi' => $deskripsi], $error];
}

/**
 * Tautan WhatsApp (wa.me) dari nomor lokal 08xx, dengan pesan awal opsional
 */
function linkWhatsApp(string $nomor, string $pesan = ''): string {
    $n = bersihkanNomorHp($nomor);
    if (strpos($n, '0') === 0) {
        $n = '62' . substr($n, 1);
    }
    return 'https://wa.me/' . $n . ($pesan !== '' ? '?text=' . rawurlencode($pesan) : '');
}


// =====================================================================
// TAMPILAN: tingkat kamar, ikon fasilitas, galeri foto, stepper
// =====================================================================

/**
 * Tingkat kamar, dihitung dari fasilitasnya (tanpa kolom tambahan di database).
 * VIP = kamar mandi dalam + (water heater atau balkon); Deluxe = kamar mandi dalam;
 * Standar = ber-AC; selain itu Ekonomi. Mengembalikan [kode_css, label].
 */
function tingkatKamar(?string $daftarFasilitas): array {
    $f = (string)$daftarFasilitas;
    $kmDalam = (bool)preg_match('/Mandi Dalam/i', $f);
    $heater  = (bool)preg_match('/Water Heater/i', $f);
    $balkon  = (bool)preg_match('/Balkon/i', $f);
    if ($kmDalam && ($heater || $balkon)) return ['vip', 'VIP'];
    if ($kmDalam)                         return ['deluxe', 'Deluxe'];
    if (preg_match('/\bAC\b/', $f))       return ['standar', 'Standar'];
    return ['ekonomi', 'Ekonomi'];
}

/**
 * Ikon SVG (garis, mengikuti warna teks) untuk label fasilitas singkat; string kosong jika tidak ada ikonnya.
 */
function ikonFasilitas(string $label): string {
    $isi = [
        'AC'           => '<path d="M12 3v18M4.6 7.5l14.8 9M19.4 7.5L4.6 16.5"/><path d="M9.5 4.6 12 6.6l2.5-2M9.5 19.4l2.5-2 2.5 2"/>',
        'Kipas'        => '<circle cx="12" cy="12" r="1.3"/><path d="M12 10.6C10.9 7.9 11.1 4.9 12.9 4.1c1.7-.7 2.9.8 2.5 2.5-.4 1.9-1.7 3.5-3.4 4z"/><g transform="rotate(120 12 12)"><path d="M12 10.6C10.9 7.9 11.1 4.9 12.9 4.1c1.7-.7 2.9.8 2.5 2.5-.4 1.9-1.7 3.5-3.4 4z"/></g><g transform="rotate(240 12 12)"><path d="M12 10.6C10.9 7.9 11.1 4.9 12.9 4.1c1.7-.7 2.9.8 2.5 2.5-.4 1.9-1.7 3.5-3.4 4z"/></g>',
        'KM Dalam'     => '<path d="M5 11a6.5 6.5 0 0 1 13 0z"/><path d="M11.5 4.5V3"/><path d="M7.5 14.5v2M11.5 14v3M15.5 14.5v2M9.5 19v1.5M13.5 19v1.5"/>',
        'Water Heater' => '<path d="M12 3c.6 3.3 5 4.7 5 9.6A5 5 0 0 1 7 12.6c0-2 1-3.3 2.2-4.3.1 1.6.9 2.5 1.9 2.7.4-2.6-.9-4.3.9-7.9z"/>',
        'Balkon'       => '<rect x="5" y="3.5" width="14" height="8.5" rx="1"/><path d="M3.5 12h17M6 12v7.5M10 12v7.5M14 12v7.5M18 12v7.5M3.5 19.5h17"/>',
        'Meja Belajar' => '<path d="M4 5.5h5.2A2.8 2.8 0 0 1 12 8.3V20a2.4 2.4 0 0 0-2.4-2.4H4z"/><path d="M20 5.5h-5.2A2.8 2.8 0 0 0 12 8.3V20a2.4 2.4 0 0 1 2.4-2.4H20z"/>',
        'Dapur'        => '<path d="M5 10h14v5.5A4.5 4.5 0 0 1 14.5 20h-5A4.5 4.5 0 0 1 5 15.5z"/><path d="M3 10h18M9 7c0-1.2 1-1.2 1-2.5M14 7c0-1.2 1-1.2 1-2.5"/>',
    ];
    if (!isset($isi[$label])) return '';
    return '<svg class="ikon" viewBox="0 0 24 24" width="14" height="14" fill="none" stroke="currentColor" '
         . 'stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true">' . $isi[$label] . '</svg>';
}

/**
 * Daftar foto sebuah kamar untuk galeri. Berkas yang dikenali di folder kamar/:
 *   A-06.jpg (foto utama), A-06_2.jpg, A-06_3.jpg, ... (foto tambahan); ekstensi jpg/jpeg/png/webp.
 * Jika tidak ada satu pun, dipakai foto bawaan putra.jpg / putri.jpg. Mengembalikan path relatif (dengan ?v=waktu ubah).
 */
function fotoKamar(string $kode, string $tipe, int $maks = 8): array {
    $kodeAman = preg_replace('/[^A-Za-z0-9\-]/', '', $kode);
    $dir = dirname(__DIR__) . '/kamar/';
    $ketemu = [];
    foreach (['jpg', 'jpeg', 'png', 'webp'] as $ext) {
        foreach (glob($dir . $kodeAman . '.' . $ext) ?: [] as $f)              $ketemu[$f] = 0;
        foreach (glob($dir . $kodeAman . '_[0-9]*.' . $ext) ?: [] as $f)       $ketemu[$f] = preg_match('/_(\d+)\.[a-z]+$/i', $f, $m) ? (int)$m[1] : 99;
    }
    asort($ketemu);
    $hasil = [];
    foreach (array_slice(array_keys($ketemu), 0, $maks) as $f) {
        $hasil[] = 'kamar/' . basename($f) . '?v=' . filemtime($f);
    }
    return $hasil ?: [$tipe === 'putra' ? 'putra.jpg' : 'putri.jpg'];
}

/**
 * Tiga tahap perjalanan sebuah booking untuk stepper di halaman Sewaku:
 * Diajukan -> Diverifikasi -> Aktif. Tiap tahap: ['status' => selesai|sekarang|menunggu|gagal|lewat, 'judul', 'ket'].
 * Membutuhkan kolom hasil query sewaku (created_at, status, tanggal_mulai, belum_mulai, sudah_berakhir, akhir_sewa).
 */
function tahapanSewa(array $b): array {
    $diajukan = ['status' => 'selesai', 'judul' => 'Diajukan', 'ket' => !empty($b['created_at']) ? formatTanggal($b['created_at']) : ''];
    $lewat    = ['status' => 'lewat', 'judul' => 'Aktif', 'ket' => 'Tidak dilanjutkan'];
    switch ($b['status'] ?? '') {
        case 'pending':
            return [$diajukan,
                ['status' => 'sekarang', 'judul' => 'Diverifikasi', 'ket' => 'Menunggu admin'],
                ['status' => 'menunggu', 'judul' => 'Aktif', 'ket' => 'Setelah disetujui']];
        case 'approved':
            $diverif = ['status' => 'selesai', 'judul' => 'Diverifikasi', 'ket' => 'Disetujui'];
            if (!empty($b['sudah_berakhir'])) {
                $aktif = ['status' => 'selesai', 'judul' => 'Selesai', 'ket' => 'Berakhir ' . formatTanggal($b['akhir_sewa'])];
            } elseif (!empty($b['belum_mulai'])) {
                $aktif = ['status' => 'menunggu', 'judul' => 'Aktif', 'ket' => 'Mulai ' . formatTanggal($b['tanggal_mulai'])];
            } else {
                $aktif = ['status' => 'sekarang', 'judul' => 'Aktif', 'ket' => 'Sampai ' . formatTanggal($b['akhir_sewa'])];
            }
            return [$diajukan, $diverif, $aktif];
        case 'rejected':
            return [$diajukan, ['status' => 'gagal', 'judul' => 'Ditolak', 'ket' => 'Oleh admin'], $lewat];
        case 'cancelled':
            return [$diajukan, ['status' => 'gagal', 'judul' => 'Dibatalkan', 'ket' => 'Oleh kamu'], $lewat];
        case 'expired':
            return [$diajukan, ['status' => 'gagal', 'judul' => 'Kedaluwarsa', 'ket' => 'Tidak diverifikasi'], $lewat];
    }
    return [$diajukan];
}

/**
 * Menghasilkan HTML stepper dari daftar tahap (lihat tahapanSewa). Semua teks di-escape.
 */
function renderStepper(array $tahap, string $kelasTambahan = ''): string {
    $html = '<ol class="stepper ' . htmlspecialchars($kelasTambahan) . '">';
    foreach ($tahap as $i => $t) {
        $tanda = ['selesai' => '✓', 'gagal' => '✕'][$t['status']] ?? (string)($i + 1);
        $html .= '<li class="' . htmlspecialchars($t['status']) . '"><span class="bulat">' . $tanda . '</span>'
               . '<b>' . htmlspecialchars($t['judul']) . '</b>'
               . ($t['ket'] !== '' ? '<small>' . htmlspecialchars($t['ket']) . '</small>' : '')
               . '</li>';
    }
    return $html . '</ol>';
}

/**
 * Menambah n bulan ke sebuah tanggal (Y-m-d) dengan aturan sama seperti MySQL DATE_ADD(... INTERVAL n MONTH):
 * jika hari tidak ada di bulan tujuan, dipakai hari terakhir bulan itu (31 Jan + 1 bulan = 28/29 Feb).
 */
function tambahBulan(string $tanggal, int $n): string {
    $d = new DateTime($tanggal);
    $hari = (int)$d->format('j');
    $d->modify('first day of this month');
    $d->modify(($n >= 0 ? '+' : '') . $n . ' months');
    $d->setDate((int)$d->format('Y'), (int)$d->format('n'), min($hari, (int)$d->format('t')));
    return $d->format('Y-m-d');
}

/**
 * Label singkat fasilitas unggulan untuk ditampilkan sebagai tag di kartu kamar & ringkasan booking
 */
function tagFasilitas(?string $daftar): array {
    $aturan = [
        '/\bAC\b/' => 'AC', '/Kipas/i' => 'Kipas', '/Mandi Dalam/i' => 'KM Dalam',
        '/Water Heater/i' => 'Water Heater', '/Balkon/i' => 'Balkon',
        '/Meja Belajar/i' => 'Meja Belajar', '/Dapur/i' => 'Dapur',
    ];
    $tag = [];
    foreach (explode(', ', (string)$daftar) as $nama) {
        foreach ($aturan as $pola => $label) {
            if (preg_match($pola, $nama)) { $tag[$label] = true; break; }
        }
    }
    return array_keys($tag);
}


// =====================================================================
// DURASI SEWA (bebas, dalam bulan)
// =====================================================================

const BOOKING_MIN_BULAN = 1;
const BOOKING_MAKS_BULAN = 24;   // ubah di sini jika ingin batas sewa yang lebih panjang

/**
 * Membaca durasi sewa dari input pengguna. Hanya bilangan bulat tanpa tanda/pecahan
 * dalam rentang BOOKING_MIN_BULAN..BOOKING_MAKS_BULAN yang diterima; selain itu null.
 */
function parseDurasi($mentah): ?int {
    $s = trim((string)$mentah);
    if (!preg_match('/^\d{1,3}$/', $s)) {
        return null;
    }
    $n = (int)$s;
    return ($n >= BOOKING_MIN_BULAN && $n <= BOOKING_MAKS_BULAN) ? $n : null;
}

/**
 * Label durasi yang enak dibaca: "3 bulan", "12 bulan (1 tahun)", "18 bulan (1 tahun 6 bln)"
 */
function labelDurasiBulan(int $n): string {
    if ($n < 12) return "$n bulan";
    $tahun = intdiv($n, 12);
    $sisa  = $n % 12;
    return "$n bulan ($tahun tahun" . ($sisa > 0 ? " $sisa bln" : "") . ")";
}
