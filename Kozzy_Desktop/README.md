# Kozzy Desktop Workstation GUI (Desktop-First / Full-Screen / Tanpa Scroll Halaman)

Aplikasi Desktop Sistem Manajemen & Pemesanan Kos Bulanan **Kozzy** berbasis **Java Swing (Object-Oriented Programming - PBO)**.

Versi ini dirancang khusus mengikuti standar **Aplikasi Desktop Workstation Modern (Single Viewport / Full Screen Layout)** sesuai arahan dosen:
- **Bukan replika website**: Menghilangkan tata letak landing page web panjang yang boros tempat dan mengharuskan scroll ke bawah.
- **Navigasi Rail Instan (Desktop Sidebar)**: Seluruh modul dapat diakses dalam 1-klik langsung dari menu navigasi kiri.
- **100% Fit di Satu Layar (Zero Page Scroll)**: Setiap halaman dirancang proporsional sehingga seluruh metrik, tabel master-detail, kartu hunian, dan kontrol admin tampil utuh dalam satu layar.
- **Penyimpanan Lokal JSON Persisten (`data/kozzy_data.json`)**: Berjalan mandiri tanpa perlu server MySQL/XAMPP, namun data transaksi tetap tersimpan permanen.

---

## 📁 Struktur Direktori Proyek

```text
Kozzy_Desktop/
├── assets/                 # Gambar kamar (A-01 s.d. B-04), foto gedung, logo kos, dan bukti
├── bin/                    # Bytecode hasil kompilasi (.class)
├── data/
│   └── kozzy_data.json     # Penyimpanan berkas lokal JSON persisten
├── lib/
│   └── flatlaf-3.5.4.jar   # Modern Look & Feel FlatLaf Desktop
├── src/com/kozzy/
│   ├── Main.java           # Entry point utama aplikasi desktop
│   ├── config/
│   │   └── UITheme.java    # Token desain workstation, font, & palet warna Kozzy
│   ├── models/             # Domain Model PBO (Pengguna, Admin, Pencari, Kamar, Booking, dll.)
│   ├── dao/
│   │   ├── interfaces/     # Kontrak Abstraksi DAO (IUserDAO, IKamarDAO, IBookingDAO, dll.)
│   │   └── impl/           # Implementasi MockDataStore & SimpleJson (In-Memory + JSON)
│   ├── services/           # Logika Bisnis (AuthService, BookingService, SessionManager)
│   └── views/
│       ├── components/     # DesktopSidebar, MetricCard, PillBadge, PillButton, ImageHelper
│       ├── DesktopMainWindow.java      # Frame Utama (Sidebar + CardLayout)
│       ├── DesktopBerandaView.java     # Dashboard KPI & Peta Ketersediaan Kamar
│       ├── DesktopKamarView.java       # Master-Detail Katalog & Preview Kamar
│       ├── DesktopSewakuView.java      # Sewa Aktif & Riwayat Pemesanan Penghuni
│       ├── DesktopAdminView.java       # Konsol Verifikasi Bukti & Pengatur Status Kamar
│       ├── DesktopProfilView.java      # Profil Pengguna, Jenis Kelamin, & Ringkasan Transaksi
│       ├── DesktopLoginView.java       # Form Masuk/Daftar, Intip Password (Typo), & Pilihan Gender
│       └── DesktopBookingDialog.java   # Modal Formulir Pemesanan Kamar
├── compile.bat             # Skrip kompilasi 1-klik (Windows)
├── run.bat                 # Skrip eksekusi 1-klik (Windows)
└── README.md
```

---

## 🖥️ Fitur & Tampilan Antarmuka Desktop

1. **🏠 Beranda (Dashboard Operasional)**:
   - 4 Kartu Metrik KPI di bagian atas: *Total Kamar*, *Kamar Tersedia*, *Kamar Terisi*, dan *Pengajuan Pending*.
   - Peta Ketersediaan 8 Kamar (Grid interaktif): Klik ubin kamar mana saja untuk langsung membuka detailnya di katalog.
   - Panel informasi ketentuan gedung & kontak bantuan pengelola.
2. **🛏️ Katalog Kamar (Master-Detail Layout)**:
   - **Kolom Kiri (Master)**: Tabel daftar kamar dengan filter instan (*Semua*, *Putri*, *Putra*, *Hanya Tersedia*).
   - **Kolom Kanan (Live Preview)**: Foto kamar terpilih beresolusi tinggi, harga sewa, fasilitas lengkap, dan tombol *Ajukan Booking*.
   - Tidak perlu membuka pop-up berulang kali hanya untuk membandingkan antar kamar!
3. **📋 Sewa Saya (Sewaku)**:
   - **Sisi Kiri**: Rincian hunian aktif berjalan (foto kamar, periode sewa aktif, metode pembayaran, total biaya).
   - **Sisi Kanan**: Tabel riwayat pengajuan booking lengkap dengan status keputusan (*Approved*, *Pending*, *Rejected*) beserta catatan verifikasi admin.
4. **🛡️ Panel Admin (Konsol Verifikasi)**:
   - Hanya muncul saat login sebagai Administrator.
   - **Sisi Kiri**: Tabel pengajuan masuk, thumbnail bukti transfer (bisa diklik untuk memperbesar), serta tombol *✓ Setujui* dan *✕ Tolak*.
   - **Sisi Kanan**: Kontrol status 8 kamar (Kamar terisi terkunci otomatis; kamar belum terisi dapat diubah antara *Tersedia* dan *Perbaikan*).
5. **👤 Profil Pengguna**:
   - Menampilkan avatar inisial, nama, jenis kelamin (*Laki-laki* / *Perempuan*), email, no. WhatsApp, peran akun, dan statistik akumulasi transaksi.
6. **🔑 Autentikasi Siap Pakai (Bukan Versi Beta/Demo)**:
   - Form Masuk & Form Daftar bersih tanpa tombol uji coba akun demo.
   - Dilengkapi tombol mata (eye toggle) untuk memeriksa ketikan kata sandi agar terhindar dari salah ketik (typo).
   - Dilengkapi pemilihan jenis kelamin pengguna saat pendaftaran akun baru.

---

## 🚀 Cara Menjalankan

### Melalui Skrip Batch (Windows):
1. Dobel-klik `compile.bat` untuk mengompilasi.
2. Dobel-klik `run.bat` untuk langsung menjalankan aplikasi desktop.

### Melalui Terminal / Command Prompt:
```bash
# Kompilasi
javac -encoding UTF-8 -cp "lib/*;bin" -d bin src\com\kozzy\*.java

# Jalankan
java -cp "bin;lib/*" com.kozzy.Main
```

---

## 🔑 Akun Demo Pengujian

| Peran Akun | Nama | Email | Kata Sandi | Keterangan Uji Coba |
| :--- | :--- | :--- | :--- | :--- |
| **Pencari Kos** | Farah | `farah@kozzy.id` | `123456` | Memiliki hunian aktif Kamar A-03 |
| **Pencari Kos** | Fildza | `fildza@kozzy.id` | `123456` | Pengajuan Kamar B-02 status pending |
| **Admin Kos** | Admin Kozzy (Akiel) | `admin@kozzy.id` | `admin123` | Akses penuh Panel Admin & verifikasi |
