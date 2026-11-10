# Kozzy Migrasi PHP & MySQL Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Memigrasikan aplikasi web sewa kos Kozzy dari web statis berbasis `localStorage` menjadi web dinamis penuh berbasis PHP Native (MPA) dan basis data relasional MySQL (8 tabel 3NF) pada lingkungan XAMPP lokal.

**Architecture:** Arsitektur PHP Multi-Page Application (MPA) Server-Side Rendering dengan modular templating (`includes/header.php`, `includes/nav.php`, `includes/footer.php`), koneksi aman PDO dengan Prepared Statements, session native `$_SESSION`, transaksi database multi-tabel untuk booking dan verifikasi admin, serta penyimpanan fisik bukti pembayaran di server.

**Tech Stack:** PHP 8.2 (XAMPP), MySQL / MariaDB (XAMPP), PDO, HTML5, CSS3, JavaScript ES6 (untuk modal & toast).

**Spec:** [docs/superpowers/specs/2026-10-22-migrasi-php-mysql-design.md](file:///d:/coolyeah/SEMESTER%203/1PRAKTIKUM/DPW/.PROJEK/projek%20kozzy%20v1/docs/superpowers/specs/2026-10-22-migrasi-php-mysql-design.md)  
**PRD:** [tasks/prd-migrasi-php-mysql.md](file:///d:/coolyeah/SEMESTER%203/1PRAKTIKUM/DPW/.PROJEK/projek%20kozzy%20v1/tasks/prd-migrasi-php-mysql.md)

## Global Constraints

- **PHP Version:** PHP 8.2+ CLI & Apache module (`C:\xampp\php\php.exe`).
- **Database Driver:** `PDO_MYSQL` dengan `ERRMODE_EXCEPTION` dan `FETCH_ASSOC`.
- **Database Schema:** Tepat 8 tabel sesuai ERD Oracle: `pengguna`, `kamar`, `fasilitas`, `fasilitas_kamar`, `metode_pembayaran`, `booking`, `bukti_pembayaran`, `riwayat_verifikasi`.
- **Authentication:** Sesi server `$_SESSION['user']` membedakan hak akses `admin` vs `pencari`. Password disimpan plaintext sementara sesuai instruksi.
- **Upload Directory:** `assets/uploads/bukti/` dengan validasi ekstensi (`jpg, jpeg, png, webp`), MIME type, dan maks. 2MB.
- **Visual Integrity:** Desain, layout, CSS, modal, dan toast dari antarmuka lama dipertahankan 100%.

## Review Focus

1. **Aturan Sewa Tunggal:** Pengguna yang sudah memiliki booking pending atau approved tidak boleh dapat mengajukan booking baru.
2. **Kamar Eksklusif:** Kamar yang sedang pending verifikasi atau sedang terisi tidak dapat dibooking oleh pengguna lain.
3. **Integritas Transaksi Booking:** Jika unggah berkas bukti gagal atau kueri booking gagal, transaksi database harus di-rollback secara utuh tanpa meninggalkan catatan yatim.
4. **Integritas Transaksi Verifikasi Admin:** Saat admin menyetujui booking, perubahan status booking, status kamar (`terisi_hingga`), dan penambahan audit log ke `riwayat_verifikasi` harus atomik.
5. **Auto-Expire Kamar:** Kamar yang telah melewati tanggal `terisi_hingga` harus otomatis kembali berstatus `tersedia` ketika halaman diakses.

---

### Task 1: Environment Setup, Static Backup, and Database Schema

**Files:**
- Create: `database.sql`
- Create: `backup/` (berisi salinan `*.html`)
- Create: `assets/uploads/bukti/.gitkeep`

**Interfaces:**
- Produces: Basis data `kozzy_db` dengan 8 tabel dan data awal (*seeding*).

- [x] **Step 1: Buat direktori backup dan salin file HTML statis lama**
  Salin `admin.html`, `booking.html`, `index.html`, `kamar.html`, `login.html`, `profil.html`, `sewaku.html` ke dalam folder `backup/`.
- [x] **Step 2: Buat skema DDL dan data awal di `database.sql`**
  Tulis DDL MySQL 8 tabel (`pengguna`, `kamar`, `fasilitas`, `fasilitas_kamar`, `metode_pembayaran`, `booking`, `bukti_pembayaran`, `riwayat_verifikasi`) dengan *Foreign Keys* (`ON UPDATE CASCADE ON DELETE RESTRICT`).
  Sertakan data seeding awal: 1 admin (`admin@kozzy.id` / `admin123`), 2 pencari (`farah@kozzy.id`, `fildza@kozzy.id`), 8 kamar (A-01 s/d B-04), master fasilitas, relasi fasilitas-kamar, dan master metode pembayaran.
- [x] **Step 3: Eksekusi import basis data ke MySQL**
  Jalankan kueri pembuatan database `kozzy_db` dan import `database.sql` menggunakan binary MySQL XAMPP:
  `C:\xampp\mysql\bin\mysql.exe -u root -e "CREATE DATABASE IF NOT EXISTS kozzy_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"`
  `C:\xampp\mysql\bin\mysql.exe -u root kozzy_db < database.sql`
- [x] **Step 4: Verifikasi tabel dan data awal**
  Jalankan perintah SQL pengecekan:
  `C:\xampp\mysql\bin\mysql.exe -u root kozzy_db -e "SHOW TABLES; SELECT COUNT(*) FROM kamar; SELECT COUNT(*) FROM pengguna;"`
  Verifikasi output: 8 tabel terbuat, 8 baris kamar, 3 baris pengguna.

---

### Task 2: Central Configuration, PDO Database Connection, and Core Helpers

**Files:**
- Create: `config/database.php`
- Create: `includes/helper.php`

**Interfaces:**
- Produces:
  - Variabel global `$pdo` (instance `PDO`)
  - Fungsi `rupiah(int $nominal): string`
  - Fungsi `formatTanggal(?string $iso): string`
  - Fungsi `cekMasaSewa(PDO $pdo): void`
  - Fungsi `setFlash(string $pesan, string $tipe = 'info'): void`
  - Fungsi `getFlash(): ?array`
  - Fungsi `userLogin(): ?array`
  - Fungsi `wajibLogin(?string $peran = null): void`

- [x] **Step 1: Buat file koneksi PDO di `config/database.php`**
  Inisialisasi `session_start()` jika belum aktif. Buka koneksi PDO ke `mysql:host=localhost;dbname=kozzy_db;charset=utf8mb4`, user `root`, pass kosong. Atur atribut `ATTR_ERRMODE => ERRMODE_EXCEPTION` dan `ATTR_DEFAULT_FETCH_MODE => FETCH_ASSOC`.
- [x] **Step 2: Buat fungsi helper di `includes/helper.php`**
  Implementasikan format rupiah (`"Rp " . number_format(...)`), format tanggal Indonesia ("12 Feb 2026"), flash message setter & getter, `userLogin()`, serta `cekMasaSewa($pdo)` yang menjalankan pembaruan otomatis kamar expired.
- [x] **Step 3: Uji fungsi koneksi dan helper melalui skrip CLI**
  Jalankan: `C:\xampp\php\php.exe -r "require 'config/database.php'; require 'includes/helper.php'; cekMasaSewa($pdo); echo 'Helper OK';" `
  Expected: Output `Helper OK`.

---

### Task 3: Shared Layout Components & UI Assets Restructuring

**Files:**
- Create: `includes/header.php`
- Create: `includes/nav.php`
- Create: `includes/footer.php`
- Modify: `script.js` (sesuaikan untuk modal & toast interaktif tanpa localStorage)

**Interfaces:**
- Consumes: `userLogin()`, `getFlash()`.
- Produces: Komponen tata letak bersama untuk semua halaman.

- [x] **Step 1: Buat template `includes/header.php`**
  Memuat tag `<!DOCTYPE html>`, `<meta>`, link Google Font Space Grotesk, dan `<link rel="stylesheet" href="style.css">`.
- [x] **Step 2: Buat template `includes/nav.php`**
  Memuat navbar responsif, logo kos, menu navigasi (`Beranda`, `Kamar`, `Sewaku`, `Admin`), dan penanda menu aktif berdasarkan `basename($_SERVER['PHP_SELF'])`. Menampilkan chip profil atau tombol masuk/keluar berdasarkan sesi `userLogin()`.
- [x] **Step 3: Buat template `includes/footer.php`**
  Memuat informasi kontak footer, modal konfirmasi logout, container `#toastWrap`, tag `<script src="script.js"></script>`, dan render inline JS pemicu flash message toast jika ada pesan di `getFlash()`.
- [x] **Step 4: Sederhanakan `script.js`**
  Pertahankan fungsi UI modal (`tutupModal`, `tanyaLogout`, `bukaDetail`), toast helper, dan kalkulator durasi sewa di form booking, serta bersihkan logika penyimpanan `localStorage` lama.
- [x] **Step 5: Verifikasi syntax PHP komponen layout**
  Jalankan: `C:\xampp\php\php.exe -l includes/header.php; C:\xampp\php\php.exe -l includes/nav.php; C:\xampp\php\php.exe -l includes/footer.php`
  Expected: `No syntax errors detected in ...`

---

### Task 4: Authentication & Session System (Login, Register, Logout)

**Files:**
- Create: `login.php`
- Create: `logout.php`

**Interfaces:**
- Consumes: `$pdo`, `setFlash()`, `userLogin()`.
- Produces: Sesi otentikasi server `$_SESSION['user']`.

- [x] **Step 1: Implementasi logika POST login dan form di `login.php`**
  Jika sudah login, redirect ke `index.php`. Jika menerima `POST['aksi'] == 'login'`, cocokkan email dan password plaintext ke tabel `pengguna`. Jika valid, set `$_SESSION['user'] = $row`, set flash "Selamat datang kembali!", redirect ke halaman tujuan atau `index.php`. Jika gagal, tampilkan pesan error.
- [x] **Step 2: Implementasi logika POST pendaftaran di `login.php`**
  Jika menerima `POST['aksi'] == 'daftar'`, validasi nama (min 3 kar), format email, no WA (awalan 08, min 10 digit), password (min 6 kar), dan pastikan email belum terdaftar. Jika valid, `INSERT INTO pengguna`, langsung login ke sesi, dan redirect.
- [x] **Step 3: Implementasi `logout.php`**
  Hancurkan session (`session_unset()`, `session_destroy()`), redirect ke `index.php` dengan flash toast "Kamu telah keluar.".
- [x] **Step 4: Verifikasi syntax dan tes login via kueri**
  Jalankan `C:\xampp\php\php.exe -l login.php; C:\xampp\php\php.exe -l logout.php`.
  Expected: `No syntax errors detected`.

---

### Task 5: Room Browsing & Dynamic Details

**Files:**
- Create: `index.php`
- Create: `kamar.php`

**Interfaces:**
- Consumes: `$pdo`, `includes/header.php`, `includes/nav.php`, `includes/footer.php`.
- Produces: Tampilan katalog kamar dinamis dari basis data.

- [x] **Step 1: Implementasi beranda di `index.php`**
  Tampilkan hero banner, statistik singkat, preview 3 kamar tersedia dari basis data (`SELECT * FROM kamar WHERE status = 'tersedia' LIMIT 3`), fasilitas umum, dan testimoni/keunggulan.
- [x] **Step 2: Implementasi katalog kamar dan filter di `kamar.php`**
  Ambil parameter `GET['tipe']` (semua / putra / putri). Query kamar digabungkan dengan fasilitas melalui relasi:
  `SELECT k.*, GROUP_CONCAT(f.nama_fasilitas SEPARATOR ', ') AS daftar_fasilitas FROM kamar k LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar LEFT JOIN fasilitas f ON fk.fasilitas_id_fasilitas = f.id_fasilitas GROUP BY k.kode_kamar ORDER BY (k.status = 'terisi') ASC, k.kode_kamar ASC`
- [x] **Step 3: Implementasi modal detail kamar di `kamar.php`**
  Tampilkan detail kamar, foto, status badge, harga, deskripsi, fasilitas lengkap, serta tombol aksi cerdas:
  - Jika belum login: tombol "Booking Kamar Ini" mengarahkan ke `login.php?balik=booking.php?kode=...`
  - Jika login admin: sembunyikan tombol booking
  - Jika user memiliki sewa aktif: tombol disabled "Punya Sewa Aktif"
  - Jika kamar memiliki pengajuan pending: tombol disabled "Sedang Diproses"
  - Jika kamar terisi: tombol disabled "Kamar Terisi"
  - Jika tersedia: tombol "Booking Kamar Ini →"
- [x] **Step 4: Verifikasi syntax PHP**
  Jalankan `C:\xampp\php\php.exe -l index.php; C:\xampp\php\php.exe -l kamar.php`.
  Expected: `No syntax errors detected`.

---

### Task 6: Room Booking & File Upload Transaction

**Files:**
- Create: `booking.php`
- Modify: `assets/uploads/bukti/`

**Interfaces:**
- Consumes: `$pdo`, `userLogin()`, `wajibLogin('pencari')`.
- Produces: Transaksi booking baru di tabel `booking` dan `bukti_pembayaran`.

- [x] **Step 1: Implementasi pengecekan guard di `booking.php`**
  Pastikan pengguna sudah login dan bukan admin. Ambil `GET['kode']`, validasi kamar ada dan berstatus `tersedia`. Pastikan pengguna tidak memiliki sewa aktif dan kamar tidak memiliki pending booking lain.
- [x] **Step 2: Implementasi form booking dan pilihan metode pembayaran dinamis**
  Tampilkan ringkasan kamar, input tanggal mulai sewa (min hari ini), select durasi (1, 3, 6 bulan), kalkulator total bayar dinamis, radio button metode pembayaran dari tabel `metode_pembayaran`, input file bukti pembayaran, dan persetujuan syarat.
- [x] **Step 3: Implementasi pemrosesan POST booking dengan transaksi PDO**
  Validasi input server-side. Jika berkas bukti diunggah: validasi MIME tipe gambar (`image/jpeg`, `image/png`, `image/webp`), ukuran maks 2MB, simpan ke `assets/uploads/bukti/bukti_{idBaru}_{timestamp}.{ext}`.
  Jalankan `PDO::beginTransaction()`:
  - `INSERT INTO booking (id_booking, tanggal_mulai, durasi, status, id_pengguna, kode_kamar, id_metode) VALUES (...)`
  - Jika ada bukti: `INSERT INTO bukti_pembayaran (file_bukti, id_booking) VALUES (...)`
  - `PDO::commit()`
  Redirect ke `sewaku.php` dengan flash message sukses dan ID booking baru.
- [x] **Step 4: Verifikasi syntax PHP**
  Jalankan `C:\xampp\php\php.exe -l booking.php`.
  Expected: `No syntax errors detected`.

---

### Task 7: Tenant Rental Status & Verification History

**Files:**
- Create: `sewaku.php`

**Interfaces:**
- Consumes: `$pdo`, `userLogin()`, `wajibLogin('pencari')`.
- Produces: Tampilan riwayat sewa lengkap milik penyewa aktif.

- [x] **Step 1: Implementasi kueri status sewa penyewa di `sewaku.php`**
  Ambil seluruh daftar booking milik penyewa yang sedang login:
  `SELECT b.*, k.tipe AS tipe_kamar, k.harga, k.terisi_hingga, m.nama_metode, bp.file_bukti FROM booking b JOIN kamar k ON b.kode_kamar = k.kode_kamar JOIN metode_pembayaran m ON b.id_metode = m.id_metode LEFT JOIN bukti_pembayaran bp ON b.id_booking = bp.id_booking WHERE b.id_pengguna = ? ORDER BY b.created_at DESC`
- [x] **Step 2: Render kartu status sewa**
  Tampilkan kartu sewa dengan badge status (kuning: pending, hijau: approved, merah: rejected).
  - Jika pending: tampilkan banner "Menunggu verifikasi admin".
  - Jika approved: tampilkan banner "Selamat, kamarmu aktif! Terisi sampai ...".
  - Jika rejected: tampilkan banner "Ditolak admin. Alasan: ..." dan tombol cari kamar lain.
  - Tampilkan tombol "Lihat Bukti Bayar" yang membuka modal gambar pratinjau bukti transfer jika tersedia.
- [x] **Step 3: Verifikasi syntax PHP**
  Jalankan `C:\xampp\php\php.exe -l sewaku.php`.
  Expected: `No syntax errors detected`.

---

### Task 8: Admin Panel & Verification Workflow

**Files:**
- Create: `admin.php`

**Interfaces:**
- Consumes: `$pdo`, `wajibLogin('admin')`.
- Produces: Mutasi persetujuan/penolakan booking dan audit log di `riwayat_verifikasi`.

- [x] **Step 1: Implementasi guard hak akses admin di `admin.php`**
  Pastikan user yang membuka halaman memiliki peran `admin`. Jika tidak, alihkan ke `index.php`.
- [x] **Step 2: Implementasi kueri statistik dan daftar antrean pending**
  Hitung total kamar, kamar tersedia, kamar terisi, dan jumlah pengajuan pending. Ambil antrean pending:
  `SELECT b.*, u.nama, u.email, u.no_telepon, k.harga, m.nama_metode, bp.file_bukti FROM booking b JOIN pengguna u ON b.id_pengguna = u.id_pengguna JOIN kamar k ON b.kode_kamar = k.kode_kamar JOIN metode_pembayaran m ON b.id_metode = m.id_metode LEFT JOIN bukti_pembayaran bp ON b.id_booking = bp.id_booking WHERE b.status = 'pending' ORDER BY b.created_at ASC`
- [x] **Step 3: Implementasi aksi Setujui (`POST['aksi'] == 'approve'`)**
  Bungkus dalam `PDO::beginTransaction()`:
  - Cek kamar masih berstatus `tersedia`.
  - Hitung `terisi_hingga = DATE_ADD(tanggal_mulai, INTERVAL durasi MONTH)`.
  - `UPDATE booking SET status = 'approved' WHERE id_booking = ?`
  - `UPDATE kamar SET status = 'terisi', terisi_hingga = ? WHERE kode_kamar = ?`
  - `INSERT INTO riwayat_verifikasi (keputusan, tanggal_keputusan, catatan, id_pengguna, id_booking) VALUES ('approved', NOW(), 'Pengajuan booking disetujui.', ?, ?)`
  - `PDO::commit()`
  Redirect dengan flash toast sukses.
- [x] **Step 4: Implementasi aksi Tolak (`POST['aksi'] == 'reject'`)**
  Validasi input alasan (min. 5 karakter).
  Bungkus dalam `PDO::beginTransaction()`:
  - `UPDATE booking SET status = 'rejected', alasan = ? WHERE id_booking = ?`
  - `INSERT INTO riwayat_verifikasi (keputusan, tanggal_keputusan, catatan, id_pengguna, id_booking) VALUES ('rejected', NOW(), ?, ?, ?)`
  - `PDO::commit()`
  Redirect dengan flash toast penolakan.
- [x] **Step 5: Implementasi kelola status kamar manual & riwayat keputusan**
  Daftar kamar: kamar terisi terkunci (disabled); kamar belum terisi dapat diubah antara `tersedia` dan `pemeliharaan`.
  Tampilkan riwayat verifikasi keputusan yang telah diambil.
- [x] **Step 6: Verifikasi syntax PHP**
  Jalankan `C:\xampp\php\php.exe -l admin.php`.
  Expected: `No syntax errors detected`.

---

### Task 9: User Profile, Booking Statistics & History

**Files:**
- Create: `profil.php`

**Interfaces:**
- Consumes: `$pdo`, `wajibLogin()`.
- Produces: Tampilan profil lengkap, metrik statistik, dan riwayat pemesanan.

- [x] **Step 1: Implementasi guard login dan hitung statistik profil di `profil.php`**
  Hitung `total_booking` dan `total_dibayar` (penjumlahan `kamar.harga * booking.durasi` untuk booking yang `approved`). Cek apakah pengguna memiliki sewa aktif saat ini.
- [x] **Step 2: Render kartu identitas pengguna dan ringkasan statistik**
  Tampilkan inisial avatar, nama lengkap, email, nomor WhatsApp, dan lencana peran (`Admin Kozzy` atau `Pencari Kos`). Tampilkan 3 kotak statistik: Total Booking, Sudah Dibayar, dan Status Sewa Aktif.
- [x] **Step 3: Render kartu sewa aktif dan riwayat booking**
  Jika ada sewa aktif, tampilkan kartu sewa aktif dengan tombol pintas ke `sewaku.php`. Tampilkan daftar riwayat semua pemesanan masa lalu secara berurutan dari yang terbaru.
- [x] **Step 4: Verifikasi syntax PHP**
  Jalankan `C:\xampp\php\php.exe -l profil.php`.
  Expected: `No syntax errors detected`.

---

### Task 10: End-to-End Verification & Edge-Case Testing

**Files:**
- Test via Apache HTTP Request / PHP CLI Built-in Server
- Verify: Seluruh 8 skenario pengujian fungsional

- [x] **Step 1: Jalankan PHP Development Server untuk verifikasi lokal**
  `C:\xampp\php\php.exe -S localhost:8000`
- [x] **Step 2: Verifikasi alur publik & kamar**
  Buka `http://localhost:8000/index.php` dan `kamar.php`. Pastikan kartu kamar, harga rupiah, fasilitas kamar (hasil relasi many-to-many), dan filter tipe putra/putri tampil sempurna.
- [x] **Step 3: Verifikasi autentikasi**
  Uji registrasi pengguna baru (`budi@kozzy.id`), login dengan `farah@kozzy.id`, dan login admin `admin@kozzy.id`. Pastikan hak akses terisolasi.
- [x] **Step 4: Verifikasi pengajuan sewa & unggah bukti**
  Lakukan booking kamar A-01 dengan durasi 3 bulan dan unggah sampel gambar bukti transfer. Pastikan file tersimpan di `assets/uploads/bukti/` dan record tersimpan di `booking` serta `bukti_pembayaran`.
- [x] **Step 5: Verifikasi persetujuan admin & audit log**
  Masuk sebagai admin, setujui pengajuan kamar A-01. Pastikan kamar A-01 berubah menjadi `terisi` dengan tanggal `terisi_hingga`, dan tabel `riwayat_verifikasi` terisi record baru.
- [x] **Step 6: Verifikasi penolakan admin**
  Ajukan booking kamar lain, lalu masuk sebagai admin dan lakukan tolak dengan alasan. Pastikan alasan tampil di `sewaku.php` penyewa.
- [x] **Step 7: Verifikasi pengosongan kamar otomatis (Auto-Expire)**
  Ubah sementara `terisi_hingga` kamar menjadi tanggal kemarin di database. Refresh halaman, pastikan status kamar otomatis kembali menjadi `tersedia`.
