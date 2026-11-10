# Dokumen Desain Teknis: Migrasi Kozzy ke PHP & MySQL

**Tanggal:** 2026-10-22  
**Status:** Menunggu Persetujuan Pengguna (Pending Approval)  
**Terkait:** [tasks/prd-migrasi-php-mysql.md](file:///d:/coolyeah/SEMESTER%203/1PRAKTIKUM/DPW/.PROJEK/projek%20kozzy%20v1/tasks/prd-migrasi-php-mysql.md)

---

## 1. Latar Belakang & Tujuan (Intent & Why)

Aplikasi penyewaan kos **Kozzy** sebelumnya dibangun menggunakan arsitektur web statis (*client-side only*) di mana seluruh data (pengguna, kamar, booking) disimpan di `localStorage` peramban melalui file `script.js`. Pendekatan ini memiliki keterbatasan mendasar:
- Data tidak tersinkronisasi antarperangkat/pengguna berbeda.
- Tidak ada hak akses nyata antara peran admin dan penyewa kos di tingkat server.
- File bukti pembayaran hanya disimpan dalam format Base64 yang membebani memori peramban.

Tujuan dari proyek ini adalah mentransformasi Kozzy menjadi aplikasi web dinamis penuh menggunakan **PHP Native** dan basis data relasional **MySQL** (lingkungan XAMPP lokal), dengan mengimplementasikan secara presisi skema 8 tabel yang telah dimodelkan dalam ERD (*Entity-Relationship Diagram*).

---

## 2. Sasaran, Batasan, dan Anti-Goals

### Goals
- Mengimplementasikan 8 tabel ternormalisasi (3NF) sesuai rancangan ERD Oracle Data Modeler: `PENGGUNA`, `KAMAR`, `FASILITAS`, `FASILITAS_KAMAR`, `METODE_PEMBAYARAN`, `BOOKING`, `BUKTI_PEMBAYARAN`, `RIWAYAT_VERIFIKASI`.
- Membangun koneksi basis data menggunakan **PDO (PHP Data Objects)** dengan *Prepared Statements*.
- Menyediakan otentikasi sesi server (`$_SESSION`) untuk pemisahan peran `admin` dan `pencari`.
- Mengamankan mutasi multi-tabel (saat booking dan verifikasi admin) dengan *Database Transaction* (`beginTransaction`, `commit`, `rollBack`).
- Mengatur penyimpanan fisik file bukti pembayaran di server dengan pembatasan tipe mime dan nama acak unik.
- Mempertahankan tampilan retro-modern, tata letak CSS, dan interaktivitas JavaScript (modal dan pop-up toast berbasis flash message).
- Mengarsipkan file `.html` statis lama ke folder `backup/`.

### Non-Goals
- Tidak mengintegrasikan *payment gateway* pihak ketiga (Midtrans, Xendit, dll.).
- Tidak mengintegrasikan layanan pihak ketiga untuk email (SMTP) atau WhatsApp gateway riil.
- Tidak menggunakan framework PHP berat (Laravel, CodeIgniter, Symfony).

### Anti-Goals (Hal yang Harus Dihindari)
- **Anti-Goal 1:** Mengubah tampilan atau gaya visual CSS yang sudah ada (desain antarmuka yang sudah disukai tidak boleh berubah).
- **Anti-Goal 2:** Menyederhanakan atau menghapus tabel dari ERD (seperti menggabungkan fasilitas langsung ke tabel kamar atau menghilangkan riwayat verifikasi). Struktur 8 tabel harus dipertahankan secara utuh untuk memenuhi standar akademis.
- **Anti-Goal 3:** Membiarkan celah SQL Injection dengan melakukan konkatenasi variabel string langsung ke kueri SQL.

---

## 3. Pendekatan Arsitektur yang Dipertimbangkan (Approaches)

### Pendekatan 1: Hybrid PHP API + Frontend Fetch JS (Ditolak)
- *Konsep:* Frontend tetap HTML/JS statis, hanya memanggil file endpoint PHP (misal `api/booking.php`) via AJAX/Fetch dan merender DOM lewat JS.
- *Trade-off:* Memisahkan frontend dan backend secara tipis, namun menimbulkan duplikasi validasi data, rendering ganda, serta kurang merefleksikan materi inti praktikum mata kuliah Desain dan Pemrograman Web (DPW).

### Pendekatan 2: Pola MVC PHP Berbasis Router (Ditolak)
- *Konsep:* Membangun sistem Model-View-Controller kustom dengan satu pintu masuk `index.php` dan router URL.
- *Trade-off:* Terlalu rumit untuk cakupan proyek praktikum semester 3 dan menyulitkan navigasi file langsung di web server Apache XAMPP.

### Pendekatan 3: PHP Native Multi-Page Application (MPA) Server-Side Rendering (Dipilih)
- *Konsep:* Mengonversi halaman menjadi file `.php` mandiri (`index.php`, `kamar.php`, `booking.php`, dll.) dengan komponen modular (`includes/header.php`, `includes/nav.php`, `includes/footer.php`), session bawaan PHP, dan form POST standar.
- *Alasan Pemilihan:* Pendekatan ini merupakan standar terbaik untuk praktikum DPW, sangat mudah didebug di XAMPP, memisahkan logika presentasi dan pemrosesan secara lugas, serta memudahkan integrasi session dan otentikasi.

---

## 4. Rincian Desain Arsitektur & Teknis

### A. Pemetaan Skema Basis Data (Oracle ERD ke MySQL DDL)

| Tabel ERD | Kolom Utama MySQL | Tipe Data & Keterangan |
|---|---|---|
| **pengguna** | `id_pengguna` (PK, INT AUTO_INC)<br>`nama` (VARCHAR 100)<br>`email` (VARCHAR 100 UNIQUE)<br>`password` (VARCHAR 255)<br>`no_telepon` (VARCHAR 20)<br>`peran` (ENUM 'admin', 'pencari') | Menyimpan data akun. Password disimpan plaintext untuk saat ini sesuai instruksi pengembang. |
| **kamar** | `kode_kamar` (PK, VARCHAR 10)<br>`tipe` (ENUM 'putra', 'putri')<br>`harga` (INT/BIGINT)<br>`status` (ENUM 'tersedia', 'terisi', 'pemeliharaan')<br>`terisi_hingga` (DATE NULL)<br>`deskripsi` (TEXT) | Data master kamar kos (A-01 s/d B-04). |
| **fasilitas** | `id_fasilitas` (PK, INT AUTO_INC)<br>`nama_fasilitas` (VARCHAR 50) | Master daftar fasilitas (Wi-Fi, Kasur, Lemari, AC, Meja, K. Mandi Dalam). |
| **fasilitas_kamar** | `kamar_kode_kamar` (FK -> kamar)<br>`fasilitas_id_fasilitas` (FK -> fasilitas)<br>PK: Composite (`kamar_kode_kamar`, `fasilitas_id_fasilitas`) | *Junction table* relasi many-to-many antara kamar dan fasilitas. |
| **metode_pembayaran** | `id_metode` (PK, INT AUTO_INC)<br>`nama_metode` (VARCHAR 50)<br>`nomor_tujuan` (VARCHAR 100) | Master pilihan pembayaran (Transfer BCA, QRIS, E-Wallet). |
| **booking** | `id_booking` (PK, VARCHAR 20 - format KZ-XXXX)<br>`tanggal_mulai` (DATE)<br>`durasi` (INT - 1, 3, atau 6)<br>`status` (ENUM 'pending', 'approved', 'rejected')<br>`alasan` (TEXT NULL)<br>`id_pengguna` (FK -> pengguna)<br>`kode_kamar` (FK -> kamar)<br>`id_metode` (FK -> metode_pembayaran)<br>`created_at` (DATETIME DEFAULT NOW) | Data transaksi penyewaan kamar kos. |
| **bukti_pembayaran** | `id_bukti` (PK, INT AUTO_INC)<br>`file_bukti` (VARCHAR 255)<br>`tanggal_unggah` (DATETIME DEFAULT NOW)<br>`id_booking` (FK -> booking) | Menyimpan path file bukti transfer di server (`assets/uploads/bukti/...`). |
| **riwayat_verifikasi** | `id_riwayat` (PK, INT AUTO_INC)<br>`keputusan` (ENUM 'approved', 'rejected')<br>`tanggal_keputusan` (DATETIME DEFAULT NOW)<br>`catatan` (TEXT NULL)<br>`id_pengguna` (FK -> pengguna, peran admin)<br>`id_booking` (FK -> booking) | Tabel audit log keputusan verifikasi admin atas setiap pengajuan booking. |

### B. Struktur Direktori Proyek

```
projek kozzy v1/
├── assets/
│   ├── css/
│   │   └── style.css            (dipindahkan dari style.css root)
│   ├── js/
│   │   └── script.js            (disederhanakan untuk modal, file preview, dan toast)
│   ├── img/
│   │   ├── logo kos.png
│   │   ├── foto depan.jpeg
│   │   ├── balkon.jpeg
│   │   ├── samping.jpeg
│   │   ├── teras.jpeg
│   │   ├── utamaa.jpeg
│   │   ├── putra.jpg
│   │   └── putri.jpg
│   ├── kamar/                   (foto kamar A-01 s/d B-04)
│   └── uploads/
│       └── bukti/               (folder penyimpanan bukti pembayaran)
├── backup/                      (arsip seluruh file .html statis lama)
├── config/
│   └── database.php             (koneksi PDO dan konstanta sistem)
├── docs/                        (spesifikasi & dokumen desain)
├── erd/                         (ERD.png & RELATIONAL.png)
├── includes/
│   ├── header.php               (HTML head, link font, link style.css)
│   ├── nav.php                  (navbar dinamis menyesuaikan peran sesi)
│   ├── footer.php               (footer & script pemicu flash toast)
│   └── helper.php               (format rupiah, format tgl, auto-expire kamar, session auth check)
├── tasks/                       (PRD & file tugas)
├── database.sql                 (skema DDL dan data awal lengkap)
├── index.php                    (beranda utama dinamis)
├── kamar.php                    (daftar kamar, filter, dan modal detail)
├── booking.php                  (form pemesanan kamar & upload bukti)
├── sewaku.php                   (status & riwayat sewa pengguna aktif)
├── profil.php                   (profil akun, statistik, riwayat transaksi)
├── admin.php                    (panel persetujuan booking & status kamar)
├── login.php                    (halaman masuk & daftar akun)
└── logout.php                   (penghancur sesi login)
```

### C. Alur Bisnis Kunci

1. **Autentikasi & Otorisasi:**
   - Sesi diatur terpusat di `config/database.php` dengan `session_start()`.
   - Helper `cek_login()` dan `cek_admin()` memastikan proteksi halaman: jika penyewa membuka `admin.php`, diarahkan ke `index.php`; jika admin membuka `sewaku.php` atau `booking.php`, diarahkan ke `admin.php`.
2. **Transaksi Pengajuan Booking (`booking.php`):**
   - Validasi ketersediaan kamar dan aturan "satu akun maksimal satu sewa aktif".
   - Upload file bukti pembayaran ke `assets/uploads/bukti/bukti_KZ-XXXX_<timestamp>.<ext>`.
   - `PDO::beginTransaction()`:
     - `INSERT INTO booking (id_booking, tanggal_mulai, durasi, status, id_pengguna, kode_kamar, id_metode) VALUES (...)`
     - `INSERT INTO bukti_pembayaran (file_bukti, id_booking) VALUES (...)`
   - `PDO::commit()` dan set flash message sukses.
3. **Persetujuan / Penolakan Admin (`admin.php`):**
   - **Setujui (`approve`):**
     - `PDO::beginTransaction()`:
       - `UPDATE booking SET status = 'approved' WHERE id_booking = ?`
       - Hitung tanggal selesai: `DATE_ADD(tanggal_mulai, INTERVAL durasi MONTH)`
       - `UPDATE kamar SET status = 'terisi', terisi_hingga = ? WHERE kode_kamar = ?`
       - `INSERT INTO riwayat_verifikasi (keputusan, catatan, id_pengguna, id_booking) VALUES ('approved', 'Pengajuan disetujui.', ?, ?)`
     - `PDO::commit()`.
   - **Tolak (`reject`):**
     - `PDO::beginTransaction()`:
       - `UPDATE booking SET status = 'rejected', alasan = ? WHERE id_booking = ?`
       - `INSERT INTO riwayat_verifikasi (keputusan, catatan, id_pengguna, id_booking) VALUES ('rejected', ?, ?, ?)`
     - `PDO::commit()`.
4. **Pembersihan Kamar Otomatis:**
   - Helper `cekMasaSewa($pdo)` dipanggil di setiap permintaan halaman:
     `UPDATE kamar SET status = 'tersedia', terisi_hingga = NULL WHERE status = 'terisi' AND terisi_hingga < CURDATE()`

---

## 5. Yang Diserahkan kepada Pelaksana (Left to the Builder)

Rincian eksekusi implementasi berikut akan dituangkan dalam rencana implementasi step-by-step (*Implementation Plan*) setelah persetujuan dokumen desain ini:
1. Pembuatan file skema `database.sql` dan eksekusi import ke database lokal `kozzy_db` di MySQL XAMPP.
2. Pemindahan file statis lama ke folder `backup/`.
3. Pembuatan koneksi PDO terpusat `config/database.php` dan fungsi helper di `includes/helper.php`.
4. Pembuatan komponen template modular `includes/header.php`, `includes/nav.php`, `includes/footer.php`.
5. Implementasi sistem autentikasi `login.php` dan `logout.php`.
6. Implementasi halaman publik: `index.php` dan `kamar.php`.
7. Implementasi alur transaksi: `booking.php` dan `sewaku.php`.
8. Implementasi panel administrator: `admin.php`.
9. Implementasi profil pengguna: `profil.php`.
10. Verifikasi menyeluruh fungsionalitas dan pengujian alur bisnis.
