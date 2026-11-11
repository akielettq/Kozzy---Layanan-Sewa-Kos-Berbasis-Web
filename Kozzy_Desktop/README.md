# Kozzy Desktop

Aplikasi Desktop Sistem Manajemen & Pemesanan Kos Bulanan **Kozzy** berbasis **Java Swing (PBO)**.
---

## 📁 Struktur Direktori Proyek

```
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
│       ├── DesktopProfilView.java      # Profil Pengguna & Ringkasan Transaksi
│       ├── DesktopLoginView.java       # Form Masuk/Daftar & Tombol Cepat Demo
│       └── DesktopBookingDialog.java   # Modal Formulir Pemesanan Kamar
├── compile.bat             # Skrip kompilasi 1-klik (Windows)
├── run.bat                 # Skrip eksekusi 1-klik (Windows)
└── README.md
```
---

## 🔑 Akun Demo Pengujian

| Peran Akun | Nama | Email | Kata Sandi | Keterangan Uji Coba |
| :--- | :--- | :--- | :--- | :--- |
| **Pencari Kos** | Farah | `farah@kozzy.id` | `123456` | Memiliki hunian aktif Kamar A-03 |
| **Pencari Kos** | Fildza | `fildza@kozzy.id` | `123456` | Pengajuan Kamar B-02 status pending |
| **Admin Kos** | Admin Kozzy (Akiel) | `admin@kozzy.id` | `admin123` | Akses penuh Panel Admin & verifikasi |
