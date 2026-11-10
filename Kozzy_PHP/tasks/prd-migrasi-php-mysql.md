# PRD: Migrasi Kozzy dari Web Statis ke PHP & MySQL

## 1. Pendahuluan & Gambaran Umum (Overview)

**Kozzy** adalah aplikasi web penyewaan kos bulanan (kos putra dan putri) di Pekanbaru yang sebelumnya berjalan sebagai website statis dengan penyimpanan data sementara di `localStorage` peramban.

Proyek ini bertujuan memigrasikan Kozzy menjadi aplikasi web dinamis penuh berbasis **PHP Native** dan basis data relasional **MySQL**. Migrasi ini mengimplementasikan skema relasional 8 tabel sesuai rancangan ERD (Oracle Data Modeler), memungkinkan persistensi data multi-pengguna yang andal, otentikasi sesi berbasis server, pengunggahan bukti pembayaran riil ke server, serta alur persetujuan/penolakan booking oleh administrator dengan pencatatan riwayat audit.

---

## 2. Sasaran & Tujuan (Goals)

- Menggantikan seluruh ketergantungan `localStorage` dengan persistensi data terpusat di basis data MySQL.
- Menerapkan struktur basis data 8 tabel ternormalisasi (3NF) sesuai diagram ERD (`PENGGUNA`, `KAMAR`, `FASILITAS`, `FASILITAS_KAMAR`, `METODE_PEMBAYARAN`, `BOOKING`, `BUKTI_PEMBAYARAN`, `RIWAYAT_VERIFIKASI`).
- Menyediakan otentikasi dan manajemen sesi aman menggunakan `$_SESSION` PHP (peran admin dan pencari kos).
- Mengamankan interaksi basis data menggunakan **PDO (PHP Data Objects)** dengan *Prepared Statements* untuk mencegah serangan SQL Injection.
- Mengimplementasikan fitur unggah file bukti pembayaran fisik ke direktori server (`assets/uploads/bukti/`).
- Mempertahankan antarmuka pengguna (UI), desain retro-modern yang estetik, animasi modal, dan notifikasi toast menggunakan integrasi session flash.
- Mengotomatiskan pengosongan status kamar ketika masa sewa penghuni telah habis (*expired*).

---

## 3. Cerita Pengguna (User Stories)

### US-001: Inisialisasi Skema Basis Data & Data Seeding
**Deskripsi:** Sebagai pengembang, saya ingin membuat skema tabel MySQL 8 tabel dan memasukkan data awal agar sistem memiliki data master kamar, fasilitas, metode pembayaran, dan akun uji coba.

**Kriteria Penerimaan:**
- [ ] File `database.sql` berisi skema DDL lengkap dengan kunci primer (*Primary Key*) dan relasi kunci asing (*Foreign Key*).
- [ ] Tersedia data *seeding* awal untuk tabel `PENGGUNA` (admin dan 2 pencari), `KAMAR` (8 kamar A-01 s/d B-04), `FASILITAS`, `FASILITAS_KAMAR`, dan `METODE_PEMBAYARAN`.
- [ ] Eksekusi import SQL di MySQL/phpMyAdmin berjalan sukses tanpa galat relasi.

### US-002: Konfigurasi Koneksi Database PDO & Helper Sistem
**Deskripsi:** Sebagai pengembang, saya ingin modul koneksi terpusat menggunakan PDO dan fungsi pembantu (*helper*) agar kueri basis data aman dan terstruktur.

**Kriteria Penerimaan:**
- [ ] File `config/database.php` menginisialisasi koneksi PDO dengan mode *error exception* (`ERRMODE_EXCEPTION`) dan *fetch assoc*.
- [ ] Fungsi pembantu format rupiah, tanggal Indonesia, dan flash message tersedia di `includes/helper.php`.
- [ ] Helper `cekMasaSewa($pdo)` otomatis memperbarui status kamar yang telah lewat tanggal sewa menjadi `tersedia`.

### US-003: Autentikasi Pengguna & Manajemen Sesi (Login, Daftar, Logout)
**Deskripsi:** Sebagai pengguna (pencari kos atau admin), saya ingin mendaftar dan masuk ke akun saya agar data sewa dan hak akses saya dikenali oleh server.

**Kriteria Penerimaan:**
- [ ] Form pendaftaran memvalidasi format email, nomor telepon (awalan 08, min. 10 digit), dan ketiadaan duplikasi email.
- [ ] Pengguna baru tersimpan di tabel `PENGGUNA` dengan peran `pencari`.
- [ ] Form login mencocokkan email dan password, kemudian menyimpan data identitas dan peran ke dalam `$_SESSION['user']`.
- [ ] Pengguna dapat keluar (*logout*) yang menghancurkan sesi server dan mengarahkan kembali ke beranda.
- [ ] Halaman dilindungi hak akses: `admin.php` hanya dapat dibuka oleh peran admin, sedangkan `booking.php` dan `sewaku.php` hanya untuk pencari.
- [ ] Verify in browser using dev-browser skill.

### US-004: Penjelajahan Kamar & Detail Fasilitas
**Deskripsi:** Sebagai pencari kos, saya ingin melihat daftar seluruh kamar, memfilter berdasarkan tipe (putra/putri/semua), dan melihat detail fasilitas kamar yang diambil secara dinamis dari database.

**Kriteria Penerimaan:**
- [ ] Halaman `kamar.php` menampilkan kartu kamar dari kueri `KAMAR` dengan fasilitas gabungan dari `FASILITAS_KAMAR` dan `FASILITAS`.
- [ ] Filter kategori (Semua, Putra, Putri) berfungsi dinamis menyaring data.
- [ ] Kamar yang berstatus `terisi` secara visual tampil di bagian bawah dengan penanda abu-abu.
- [ ] Modal detail menampilkan foto, harga, status, deskripsi, fasilitas lengkap, dan tombol aksi yang tepat.
- [ ] Verify in browser using dev-browser skill.

### US-005: Alur Pengajuan Booking & Upload Bukti Pembayaran
**Deskripsi:** Sebagai pencari kos yang sudah masuk akun, saya ingin mengajukan sewa kamar untuk durasi 1, 3, atau 6 bulan, memilih metode pembayaran, serta mengunggah foto bukti pembayaran.

**Kriteria Penerimaan:**
- [ ] Validasi server menolak pengajuan jika pengguna sudah memiliki sewa berstatus `pending` atau `approved`.
- [ ] Validasi server menolak pengajuan jika kamar sedang tidak `tersedia` atau ada pengajuan lain yang `pending` di kamar tersebut.
- [ ] File gambar bukti pembayaran tervalidasi (tipe mime JPG/PNG/WEBP, ukuran maks. 2MB), disimpan ke `assets/uploads/bukti/` dengan nama unik.
- [ ] Penyimpanan data booking dan bukti pembayaran dilakukan dalam satu transaksi basis data (`PDO::beginTransaction()`).
- [ ] Nomor booking acak terbuat dengan format `KZ-XXXX`.
- [ ] Menampilkan modal konfirmasi sukses setelah pengajuan berhasil dikirim.
- [ ] Verify in browser using dev-browser skill.

### US-006: Halaman Sewaku & Pemantauan Status
**Deskripsi:** Sebagai pencari kos, saya ingin melihat riwayat dan status sewa saya (`pending`, `approved`, atau `rejected`) secara berkala.

**Kriteria Penerimaan:**
- [ ] Halaman `sewaku.php` menampilkan seluruh daftar booking milik pengguna yang sedang login.
- [ ] Menampilkan lencana warna status (kuning untuk pending, hijau untuk approved, merah untuk rejected).
- [ ] Jika status ditolak (`rejected`), menampilkan alasan penolakan yang ditulis admin.
- [ ] Modal dapat menampilkan pratinjau foto bukti pembayaran yang pernah diunggah.
- [ ] Verify in browser using dev-browser skill.

### US-007: Panel Admin (Kelola Pengajuan & Status Kamar)
**Deskripsi:** Sebagai administrator kos, saya ingin meninjau pengajuan yang pending, melihat bukti pembayaran, menyetujui atau menolak dengan alasan, serta mengatur status kamar manual.

**Kriteria Penerimaan:**
- [ ] Ringkasan statistik (total kamar, kamar tersedia, kamar terisi, pengajuan menunggu) dihitung dari kueri dinamis.
- [ ] Tombol Setujui (`approve`) memicu transaksi: status booking menjadi `approved`, status kamar menjadi `terisi` beserta tanggal `terisi_hingga`, dan data tersimpan ke tabel `RIWAYAT_VERIFIKASI`.
- [ ] Tombol Tolak (`reject`) membuka modal alasan; saat dikonfirmasi, status booking menjadi `rejected`, alasan tercatat, status kamar tetap tersedia, dan catatan tersimpan ke `RIWAYAT_VERIFIKASI`.
- [ ] Admin dapat mengubah status kamar yang belum terisi antara `tersedia` dan `pemeliharaan`. Kamar yang sedang `terisi` terkunci dan tidak dapat diubah statusnya secara manual.
- [ ] Verify in browser using dev-browser skill.

### US-008: Halaman Profil & Riwayat Lengkap
**Deskripsi:** Sebagai pengguna, saya ingin melihat data profil saya, ringkasan statistik pemesanan, informasi sewa aktif, dan seluruh riwayat pemesanan masa lalu.

**Kriteria Penerimaan:**
- [ ] Halaman `profil.php` menampilkan inisial avatar, nama, email, nomor telepon, dan lencana peran.
- [ ] Statistik total booking, total nominal yang pernah dibayar, dan status sewa aktif tampil akurat.
- [ ] Daftar riwayat booking terurut dari yang terbaru (*descending*).
- [ ] Verify in browser using dev-browser skill.

---

## 4. Kebutuhan Fungsional (Functional Requirements)

- **FR-1:** Sistem harus terhubung ke basis data MySQL menggunakan PDO dengan penanganan kesalahan berbasis *Exception*.
- **FR-2:** Skema database harus mengimplementasikan 8 entitas: `PENGGUNA`, `KAMAR`, `FASILITAS`, `FASILITAS_KAMAR`, `METODE_PEMBAYARAN`, `BOOKING`, `BUKTI_PEMBAYARAN`, dan `RIWAYAT_VERIFIKASI`.
- **FR-3:** Sistem harus mengisolasi hak akses berdasarkan peran akun (`admin` dan `pencari`) melalui `$_SESSION['user']['peran']`.
- **FR-4:** Aturan bisnis sewa tunggal: Satu akun pencari hanya boleh memiliki satu sewa aktif (status `pending` atau `approved` dengan kamar yang masih terisi).
- **FR-5:** Aturan bisnis kamar eksklusif: Satu kamar tidak dapat diajukan sewa jika sedang dalam perbaikan, sudah terisi, atau sedang memiliki pengajuan lain yang berstatus `pending`.
- **FR-6:** Penyimpanan bukti pembayaran harus memvalidasi ekstensi file (`.jpg`, `.jpeg`, `.png`, `.webp`), membatasi ukuran maksimal 2MB, menghasilkan nama file acak yang aman, dan menyimpan file di `assets/uploads/bukti/`.
- **FR-7:** Setiap kali admin menyetujui atau menolak booking, sistem wajib mencatat entri log ke tabel `RIWAYAT_VERIFIKASI` yang merekam ID admin, ID booking, keputusan, tanggal keputusan, dan catatan/alasan.
- **FR-8:** Setiap kali aplikasi menerima permintaan (*request*), helper `cekMasaSewa()` harus memeriksa dan mengosongkan kamar yang masa sewanya (`terisi_hingga`) telah melewati tanggal hari ini.
- **FR-9:** Seluruh form mutasi data (login, daftar, booking, verifikasi admin, ubah status kamar) harus diproses melalui metode HTTP POST dengan pengalihan balik (*redirect*) dan pengiriman pesan notifikasi via *flash session*.

---

## 5. Batasan & Hal di Luar Cakupan (Non-Goals)

- Tidak menggunakan payment gateway otomatis pihak ketiga (seperti Midtrans/Xendit); verifikasi pembayaran tetap manual oleh admin berdasarkan bukti transfer yang diunggah.
- Tidak menyertakan integrasi pengiriman email otomatis (SMTP) atau notifikasi WhatsApp gateway riil.
- Tidak menggunakan framework PHP berat (seperti Laravel/CodeIgniter); implementasi murni menggunakan PHP Native terstruktur sesuai lingkup praktikum DPW.
- Enkripsi password menggunakan plaintext sementara waktu sesuai instruksi pengembang untuk kemudahan observasi basis data selama praktikum.

---

## 6. Pertimbangan Desain & Antarmuka (Design Considerations)

- Seluruh styling visual (font *Space Grotesk*, palet warna hangat/retro, tombol border tebal, chip filter, modal interaktif, dan toast pop-up) di [style.css](file:///d:/coolyeah/SEMESTER%203/1PRAKTIKUM/DPW/.PROJEK/projek%20kozzy%20v1/style.css) dipertahankan 100%.
- Kode HTML yang berulang (navigasi atas, header, footer) dipisahkan ke dalam komponen modular di `includes/header.php`, `includes/nav.php`, dan `includes/footer.php` agar mudah dirawat (*DRY principle*).
- JavaScript pendukung dipertahankan untuk mengelola fungsionalitas UI modal, pemilihan file, kalkulasi total harga dinamis di form booking, dan pemicu toast notifikasi.

---

## 7. Pertimbangan Teknis & Lingkungan (Technical Considerations)

- **Lingkungan Eksekusi:** PHP 8.2+ (XAMPP di `C:\xampp\php\php.exe`) dan MySQL 8.0/MariaDB (XAMPP di `C:\xampp\mysql\bin\mysql.exe`).
- **Database Driver:** `PDO_MYSQL` dengan `PDO::ATTR_ERRMODE => PDO::ERRMODE_EXCEPTION`, `PDO::ATTR_DEFAULT_FETCH_MODE => PDO::FETCH_ASSOC`.
- **Karakter Set Database:** `utf8mb4` dengan *collation* `utf8mb4_unicode_ci`.
- **Manajemen File:** Seluruh file statis lama (`.html`) dipindahkan ke direktori `backup/` sebagai salinan arsip sebelum file `.php` aktif di root.
- **Folder Upload:** Folder `assets/uploads/bukti/` harus dipastikan dibuat dengan hak akses tulis (*write permission*).

---

## 8. Metrik Keberhasilan (Success Metrics)

- 100% fungsionalitas aplikasi yang sebelumnya berjalan di `localStorage` berhasil beroperasi menggunakan basis data MySQL tanpa galat.
- Semua kueri mutasi multi-tabel (booking dan persetujuan admin) berhasil dieksekusi dengan integritas data utuh (tidak ada data yatim / *orphan records*).
- Transisi antara halaman web berjalan cepat dan mulus dengan responsivitas feedback toast yang tepat.
- Seluruh 8 tabel di basis data memiliki relasi *Foreign Key* yang valid dan dapat diinspeksi secara rapi di phpMyAdmin / MySQL CLI.

---

## 9. Pertanyaan Terbuka & Keputusan (Resolved Decisions)

- *Arsitektur:* PHP Native Server-Side Rendering (MPA) dengan modular includes.
- *Koneksi:* PDO dengan Prepared Statements.
- *Skema:* 8 tabel penuh sesuai ERD Oracle Data Modeler.
- *Upload:* File fisik disimpan di server, path dicatat di `BUKTI_PEMBAYARAN`.
- *Password:* Plaintext sementara untuk kepraktisan praktikum DPW.
- *Pembersihan Otomatis:* Helper `cekMasaSewa()` dijalankan otomatis saat request.
