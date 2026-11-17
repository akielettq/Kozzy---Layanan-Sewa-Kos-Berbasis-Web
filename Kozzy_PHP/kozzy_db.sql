
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";

CREATE TABLE `booking` (
  `id_booking` varchar(20) NOT NULL,
  `tanggal_mulai` date NOT NULL,
  `durasi` int(11) NOT NULL,
  `status` enum('pending','approved','rejected','cancelled','expired') NOT NULL DEFAULT 'pending',
  `alasan` text DEFAULT NULL,
  `id_pengguna` int(11) NOT NULL,
  `kode_kamar` varchar(10) NOT NULL,
  `id_metode` int(11) NOT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO `booking` (`id_booking`, `tanggal_mulai`, `durasi`, `status`, `alasan`, `id_pengguna`, `kode_kamar`, `id_metode`, `created_at`) VALUES
('KZ-0101', '2026-08-01', 3, 'approved', NULL, 4, 'A-01', 1, '2026-07-28 10:00:00'),
('KZ-0102', '2026-08-10', 1, 'approved', NULL, 5, 'B-01', 2, '2026-08-05 11:30:00'),
('KZ-0103', '2026-08-15', 6, 'approved', NULL, 6, 'A-02', 6, '2026-08-12 14:15:00'),
('KZ-0104', '2026-08-28', 1, 'rejected', 'Bukti transfer buram/tidak terbaca nomor referensinya.', 7, 'B-03', 3, '2026-08-28 09:20:00'),
('KZ-0105', '2026-09-01', 3, 'approved', NULL, 8, 'A-03', 4, '2026-09-01 16:45:00'),
('KZ-0106', '2026-09-08', 1, 'rejected', 'Nominal transfer tidak sesuai dengan tarif sewa kamar yang dipilih.', 9, 'B-01', 7, '2026-09-08 13:10:00'),
('KZ-0107', '2026-09-27', 1, 'approved', NULL, 10, 'A-04', 8, '2026-09-27 10:05:00'),
('KZ-0108', '2026-10-10', 3, 'approved', NULL, 12, 'B-03', 9, '2026-10-10 15:40:00'),
('KZ-0109', '2026-10-26', 1, 'pending', NULL, 4, 'A-05', 2, '2026-10-26 19:30:00'),
('KZ-0110', '2026-10-27', 3, 'pending', NULL, 5, 'B-05', 10, '2026-10-27 08:20:00'),
('KZ-0117', '2026-10-07', 1, 'approved', NULL, 3, 'B-02', 1, '2026-10-07 09:00:00'),
('KZ-0118', '2026-10-31', 1, 'rejected', 'Kamu cewe', 2, 'A-03', 2, '2026-10-27 21:00:00'),
('KZ-2E63', '2026-10-30', 1, 'rejected', 'gatau', 2, 'B-01', 1, '2026-10-30 09:21:55');

-- --------------------------------------------------------

--
-- Table structure for table `bukti_pembayaran`
--

CREATE TABLE `bukti_pembayaran` (
  `id_bukti` int(11) NOT NULL,
  `file_bukti` varchar(255) NOT NULL,
  `tanggal_unggah` datetime NOT NULL DEFAULT current_timestamp(),
  `id_booking` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `bukti_pembayaran`
--

INSERT INTO `bukti_pembayaran` (`id_bukti`, `file_bukti`, `tanggal_unggah`, `id_booking`) VALUES
(1, 'assets/uploads/bukti/bukti_KZ-0101_1722135600.jpg', '2026-07-28 10:05:00', 'KZ-0101'),
(2, 'assets/uploads/bukti/bukti_KZ-0102_1722832200.jpg', '2026-08-05 11:35:00', 'KZ-0102'),
(3, 'assets/uploads/bukti/bukti_KZ-0103_1723446900.png', '2026-08-12 14:20:00', 'KZ-0103'),
(4, 'assets/uploads/bukti/bukti_KZ-0104_1724811600.jpg', '2026-08-28 09:25:00', 'KZ-0104'),
(5, 'assets/uploads/bukti/bukti_KZ-0105_1725183900.png', '2026-09-01 16:50:00', 'KZ-0105'),
(6, 'assets/uploads/bukti/bukti_KZ-0106_1725772200.jpeg', '2026-09-08 13:15:00', 'KZ-0106'),
(7, 'assets/uploads/bukti/bukti_KZ-0107_1727409900.jpg', '2026-09-27 10:10:00', 'KZ-0107'),
(8, 'assets/uploads/bukti/bukti_KZ-0108_1728636000.png', '2026-10-10 15:45:00', 'KZ-0108'),
(9, 'assets/uploads/bukti/bukti_KZ-0109_1729945800.jpg', '2026-10-26 19:35:00', 'KZ-0109'),
(10, 'assets/uploads/bukti/bukti_KZ-0110_1729992000.png', '2026-10-27 08:25:00', 'KZ-0110'),
(11, 'assets/uploads/bukti/bukti_KZ-0117_1728291600.jpg', '2026-10-07 09:05:00', 'KZ-0117'),
(12, 'assets/uploads/bukti/bukti_KZ-0118_1729995600.png', '2026-10-27 21:05:00', 'KZ-0118');

-- --------------------------------------------------------

--
-- Table structure for table `fasilitas`
--

CREATE TABLE `fasilitas` (
  `id_fasilitas` int(11) NOT NULL,
  `nama_fasilitas` varchar(50) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fasilitas`
--

INSERT INTO `fasilitas` (`id_fasilitas`, `nama_fasilitas`) VALUES
(1, 'Wi-Fi Cepat 100 Mbps'),
(2, 'Kasur Springbed & Bantal'),
(3, 'Lemari Pakaian 2 Pintu'),
(4, 'AC Dingin 1/2 PK'),
(5, 'Meja Belajar & Kursi Kerja'),
(6, 'Kamar Mandi Dalam'),
(7, 'Water Heater (Pemanas Air)'),
(8, 'Dapur Bersama & Kulkas'),
(9, 'Area Parkir Motor Beratap'),
(10, 'Keamanan CCTV 24 Jam'),
(11, 'Kipas Angin'),
(12, 'Balkon Pribadi'),
(13, 'Mesin Cuci Bersama'),
(14, 'Ruang Santai Bersama'),
(15, 'Dispenser Air Minum'),
(16, 'Meja Rias & Cermin');

-- --------------------------------------------------------

--
-- Table structure for table `fasilitas_kamar`
--

CREATE TABLE `fasilitas_kamar` (
  `kamar_kode_kamar` varchar(10) NOT NULL,
  `fasilitas_id_fasilitas` int(11) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `fasilitas_kamar`
--

INSERT INTO `fasilitas_kamar` (`kamar_kode_kamar`, `fasilitas_id_fasilitas`) VALUES
('A-01', 1),
('A-01', 2),
('A-01', 3),
('A-01', 4),
('A-01', 9),
('A-01', 10),
('A-02', 1),
('A-02', 2),
('A-02', 3),
('A-02', 4),
('A-02', 5),
('A-02', 9),
('A-02', 10),
('A-03', 1),
('A-03', 2),
('A-03', 4),
('A-03', 5),
('A-03', 9),
('A-03', 10),
('A-04', 1),
('A-04', 2),
('A-04', 3),
('A-04', 5),
('A-04', 9),
('A-04', 10),
('A-05', 1),
('A-05', 2),
('A-05', 3),
('A-05', 4),
('A-05', 5),
('A-05', 6),
('A-05', 7),
('A-05', 9),
('A-05', 10),
('B-01', 1),
('B-01', 2),
('B-01', 3),
('B-01', 4),
('B-01', 8),
('B-01', 10),
('B-02', 1),
('B-02', 2),
('B-02', 4),
('B-02', 6),
('B-02', 8),
('B-02', 10),
('B-03', 1),
('B-03', 2),
('B-03', 3),
('B-03', 5),
('B-03', 8),
('B-03', 10),
('B-04', 1),
('B-04', 2),
('B-04', 3),
('B-04', 4),
('B-04', 8),
('B-04', 10),
('B-05', 1),
('B-05', 2),
('B-05', 3),
('B-05', 4),
('B-05', 5),
('B-05', 6),
('B-05', 7),
('B-05', 8),
('B-05', 10),
('A-06', 1),
('A-06', 2),
('A-06', 3),
('A-06', 9),
('A-06', 10),
('A-06', 11),
('A-06', 15),
('A-07', 1),
('A-07', 2),
('A-07', 3),
('A-07', 9),
('A-07', 10),
('A-07', 11),
('A-07', 15),
('A-08', 1),
('A-08', 2),
('A-08', 3),
('A-08', 9),
('A-08', 10),
('A-08', 11),
('A-08', 15),
('A-09', 1),
('A-09', 2),
('A-09', 3),
('A-09', 9),
('A-09', 10),
('A-09', 11),
('A-09', 15),
('A-10', 1),
('A-10', 2),
('A-10', 3),
('A-10', 5),
('A-10', 9),
('A-10', 10),
('A-10', 11),
('A-10', 15),
('A-11', 1),
('A-11', 2),
('A-11', 3),
('A-11', 4),
('A-11', 5),
('A-11', 9),
('A-11', 10),
('A-11', 13),
('A-12', 1),
('A-12', 2),
('A-12', 3),
('A-12', 4),
('A-12', 5),
('A-12', 9),
('A-12', 10),
('A-12', 13),
('A-13', 1),
('A-13', 2),
('A-13', 3),
('A-13', 4),
('A-13', 5),
('A-13', 9),
('A-13', 10),
('A-13', 13),
('A-13', 14),
('A-14', 1),
('A-14', 2),
('A-14', 3),
('A-14', 4),
('A-14', 5),
('A-14', 9),
('A-14', 10),
('A-14', 13),
('A-14', 14),
('A-15', 1),
('A-15', 2),
('A-15', 3),
('A-15', 4),
('A-15', 5),
('A-15', 9),
('A-15', 10),
('A-15', 13),
('A-15', 14),
('A-16', 1),
('A-16', 2),
('A-16', 3),
('A-16', 4),
('A-16', 5),
('A-16', 6),
('A-16', 9),
('A-16', 10),
('A-16', 14),
('A-17', 1),
('A-17', 2),
('A-17', 3),
('A-17', 4),
('A-17', 5),
('A-17', 6),
('A-17', 9),
('A-17', 10),
('A-17', 14),
('A-18', 1),
('A-18', 2),
('A-18', 3),
('A-18', 4),
('A-18', 5),
('A-18', 6),
('A-18', 7),
('A-18', 9),
('A-18', 10),
('A-18', 12),
('A-18', 13),
('A-19', 1),
('A-19', 2),
('A-19', 3),
('A-19', 4),
('A-19', 5),
('A-19', 6),
('A-19', 7),
('A-19', 9),
('A-19', 10),
('A-19', 12),
('A-19', 13),
('A-20', 1),
('A-20', 2),
('A-20', 3),
('A-20', 4),
('A-20', 5),
('A-20', 6),
('A-20', 7),
('A-20', 9),
('A-20', 10),
('A-20', 12),
('A-20', 13),
('B-06', 1),
('B-06', 2),
('B-06', 3),
('B-06', 9),
('B-06', 10),
('B-06', 11),
('B-06', 15),
('B-07', 1),
('B-07', 2),
('B-07', 3),
('B-07', 9),
('B-07', 10),
('B-07', 11),
('B-07', 15),
('B-08', 1),
('B-08', 2),
('B-08', 3),
('B-08', 9),
('B-08', 10),
('B-08', 11),
('B-08', 15),
('B-09', 1),
('B-09', 2),
('B-09', 3),
('B-09', 9),
('B-09', 10),
('B-09', 11),
('B-09', 15),
('B-10', 1),
('B-10', 2),
('B-10', 3),
('B-10', 5),
('B-10', 9),
('B-10', 10),
('B-10', 11),
('B-10', 15),
('B-11', 1),
('B-11', 2),
('B-11', 3),
('B-11', 4),
('B-11', 5),
('B-11', 9),
('B-11', 10),
('B-11', 13),
('B-11', 16),
('B-12', 1),
('B-12', 2),
('B-12', 3),
('B-12', 4),
('B-12', 5),
('B-12', 9),
('B-12', 10),
('B-12', 13),
('B-12', 16),
('B-13', 1),
('B-13', 2),
('B-13', 3),
('B-13', 4),
('B-13', 5),
('B-13', 9),
('B-13', 10),
('B-13', 13),
('B-13', 14),
('B-13', 16),
('B-14', 1),
('B-14', 2),
('B-14', 3),
('B-14', 4),
('B-14', 5),
('B-14', 9),
('B-14', 10),
('B-14', 13),
('B-14', 14),
('B-14', 16),
('B-15', 1),
('B-15', 2),
('B-15', 3),
('B-15', 4),
('B-15', 5),
('B-15', 9),
('B-15', 10),
('B-15', 13),
('B-15', 14),
('B-15', 16),
('B-16', 1),
('B-16', 2),
('B-16', 3),
('B-16', 4),
('B-16', 5),
('B-16', 6),
('B-16', 9),
('B-16', 10),
('B-16', 14),
('B-16', 16),
('B-17', 1),
('B-17', 2),
('B-17', 3),
('B-17', 4),
('B-17', 5),
('B-17', 6),
('B-17', 9),
('B-17', 10),
('B-17', 14),
('B-17', 16),
('B-18', 1),
('B-18', 2),
('B-18', 3),
('B-18', 4),
('B-18', 5),
('B-18', 6),
('B-18', 7),
('B-18', 9),
('B-18', 10),
('B-18', 12),
('B-18', 13),
('B-18', 16),
('B-19', 1),
('B-19', 2),
('B-19', 3),
('B-19', 4),
('B-19', 5),
('B-19', 6),
('B-19', 7),
('B-19', 9),
('B-19', 10),
('B-19', 12),
('B-19', 13),
('B-19', 16),
('B-20', 1),
('B-20', 2),
('B-20', 3),
('B-20', 4),
('B-20', 5),
('B-20', 6),
('B-20', 7),
('B-20', 9),
('B-20', 10),
('B-20', 12),
('B-20', 13),
('B-20', 16);

-- --------------------------------------------------------

--
-- Table structure for table `kamar`
--

CREATE TABLE `kamar` (
  `kode_kamar` varchar(10) NOT NULL,
  `tipe` enum('putra','putri') NOT NULL,
  `harga` int(11) NOT NULL,
  `status` enum('tersedia','terisi','pemeliharaan') NOT NULL DEFAULT 'tersedia',
  `terisi_hingga` date DEFAULT NULL,
  `deskripsi` text DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `kamar`
--

INSERT INTO `kamar` (`kode_kamar`, `tipe`, `harga`, `status`, `terisi_hingga`, `deskripsi`) VALUES
('A-01', 'putra', 850000, 'terisi', '2026-11-01', 'Kamar lantai 1, dekat area parkiran motor, sirkulasi udara baik.'),
('A-02', 'putra', 950000, 'terisi', '2027-02-15', 'Kamar lantai 1 menghadap halaman taman dengan jendela kaca lebar.'),
('A-03', 'putra', 1100000, 'terisi', '2026-12-01', 'Kamar lantai 2 dengan lemari pakaian besar dan meja belajar ergonomis.'),
('A-04', 'putra', 900000, 'terisi', '2026-11-20', 'Kamar lantai 2 dengan meja kerja dan rak buku minimalis.'),
('A-05', 'putra', 1200000, 'tersedia', NULL, 'Kamar VIP putra lantai 2 dilengkapi pendingin AC dan kamar mandi dalam.'),
('B-01', 'putri', 950000, 'tersedia', NULL, 'Kamar lantai 1 dekat lobi utama, aman, nyaman, dan tenang.'),
('B-02', 'putri', 1150000, 'terisi', '2026-11-15', 'Kamar lantai 1 dekat taman dalam dengan suasana asri dan sejuk.'),
('B-03', 'putri', 1050000, 'terisi', '2027-01-10', 'Kamar lantai 2 dengan meja rias, lemari besar, dan pencahayaan optimal.'),
('B-04', 'putri', 1350000, 'pemeliharaan', NULL, 'Kamar sedang dalam pemeliharaan rutin, pengecatan dinding, dan servis AC.'),
('B-05', 'putri', 1250000, 'tersedia', NULL, 'Kamar VIP putri lantai 2 dilengkapi kamar mandi dalam dan balkon pribadi.'),
('A-06', 'putra', 750000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat lobi utama dengan kipas angin, cocok untuk mahasiswa.'),
('A-07', 'putra', 750000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat dapur bersama dengan kipas angin, cocok untuk mahasiswa.'),
('A-08', 'putra', 800000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat area parkir motor dengan kipas angin, cocok untuk mahasiswa.'),
('A-09', 'putra', 800000, 'tersedia', NULL, 'Kamar hemat lantai 1 menghadap taman dalam dengan kipas angin, cocok untuk mahasiswa.'),
('A-10', 'putra', 850000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat tangga dan area jemuran dengan kipas angin dan meja belajar.'),
('A-11', 'putra', 950000, 'tersedia', NULL, 'Kamar lantai 2 dekat tangga dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('A-12', 'putra', 950000, 'tersedia', NULL, 'Kamar lantai 2 menghadap halaman depan dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('A-13', 'putra', 1000000, 'pemeliharaan', NULL, 'Kamar sedang dalam perbaikan rutin (pengecatan dan servis AC).'),
('A-14', 'putra', 1050000, 'tersedia', NULL, 'Kamar lantai 2 di sisi tenang bangunan dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('A-15', 'putra', 1100000, 'tersedia', NULL, 'Kamar lantai 2 dekat area jemuran dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('A-16', 'putra', 1200000, 'tersedia', NULL, 'Kamar deluxe lantai 3 di sisi tenang bangunan dengan AC dan kamar mandi dalam.'),
('A-17', 'putra', 1250000, 'tersedia', NULL, 'Kamar deluxe lantai 3 menghadap jalan dengan AC dan kamar mandi dalam.'),
('A-18', 'putra', 1350000, 'tersedia', NULL, 'Kamar VIP lantai 3 di sudut atas dengan pemandangan taman dengan kamar mandi dalam, water heater, dan balkon pribadi.'),
('A-19', 'putra', 1400000, 'tersedia', NULL, 'Kamar VIP lantai 3 dengan pemandangan halaman depan dengan kamar mandi dalam, water heater, dan balkon pribadi.'),
('A-20', 'putra', 1450000, 'tersedia', NULL, 'Kamar VIP lantai 3 di pojok atas yang paling tenang dengan kamar mandi dalam, water heater, dan balkon pribadi.'),
('B-06', 'putri', 750000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat lobi utama dengan kipas angin, cocok untuk mahasiswa.'),
('B-07', 'putri', 750000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat dapur bersama dengan kipas angin, cocok untuk mahasiswa.'),
('B-08', 'putri', 800000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat area parkir motor dengan kipas angin, cocok untuk mahasiswa.'),
('B-09', 'putri', 800000, 'tersedia', NULL, 'Kamar hemat lantai 1 menghadap taman dalam dengan kipas angin, cocok untuk mahasiswa.'),
('B-10', 'putri', 850000, 'tersedia', NULL, 'Kamar hemat lantai 1 dekat tangga dan area jemuran dengan kipas angin dan meja belajar.'),
('B-11', 'putri', 950000, 'tersedia', NULL, 'Kamar lantai 2 dekat tangga dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('B-12', 'putri', 950000, 'tersedia', NULL, 'Kamar lantai 2 menghadap halaman depan dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('B-13', 'putri', 1000000, 'tersedia', NULL, 'Kamar lantai 2 dekat ruang santai dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('B-14', 'putri', 1050000, 'tersedia', NULL, 'Kamar lantai 2 di sisi tenang bangunan dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('B-15', 'putri', 1100000, 'tersedia', NULL, 'Kamar lantai 2 dekat area jemuran dengan AC dan meja belajar, nyaman untuk belajar maupun bekerja.'),
('B-16', 'putri', 1200000, 'tersedia', NULL, 'Kamar deluxe lantai 3 di sisi tenang bangunan dengan AC dan kamar mandi dalam.'),
('B-17', 'putri', 1250000, 'tersedia', NULL, 'Kamar deluxe lantai 3 menghadap jalan dengan AC dan kamar mandi dalam.'),
('B-18', 'putri', 1350000, 'tersedia', NULL, 'Kamar VIP lantai 3 di sudut atas dengan pemandangan taman dengan kamar mandi dalam, water heater, dan balkon pribadi.'),
('B-19', 'putri', 1400000, 'tersedia', NULL, 'Kamar VIP lantai 3 dengan pemandangan halaman depan dengan kamar mandi dalam, water heater, dan balkon pribadi.'),
('B-20', 'putri', 1450000, 'tersedia', NULL, 'Kamar VIP lantai 3 di pojok atas yang paling tenang dengan kamar mandi dalam, water heater, dan balkon pribadi.');

-- --------------------------------------------------------

--
-- Table structure for table `metode_pembayaran`
--

CREATE TABLE `metode_pembayaran` (
  `id_metode` int(11) NOT NULL,
  `nama_metode` varchar(50) NOT NULL,
  `nomor_tujuan` varchar(100) NOT NULL,
  `kategori` enum('bank','ewallet','qris') NOT NULL DEFAULT 'bank'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `metode_pembayaran`
--

INSERT INTO `metode_pembayaran` (`id_metode`, `nama_metode`, `nomor_tujuan`, `kategori`) VALUES
(1, 'Transfer BCA', 'VA 8899 0812 1111 2223 (a.n. Kozzy)', 'bank'),
(2, 'QRIS All Payment', 'Scan QRIS melalui seluruh aplikasi m-Banking / e-Wallet', 'qris'),
(3, 'E-Wallet GoPay', '0812-3456-7890 (a.n. Kos Kozzy)', 'ewallet'),
(4, 'E-Wallet DANA', '0812-3456-7890 (a.n. Kos Kozzy)', 'ewallet'),
(5, 'E-Wallet OVO', '0812-3456-7890 (a.n. Kos Kozzy)', 'ewallet'),
(6, 'Transfer Bank Mandiri', 'Rekening 108-00-1234567-8 (a.n. Kos Kozzy)', 'bank'),
(7, 'Transfer Bank BNI', 'Rekening 023-8899-771 (a.n. Kos Kozzy)', 'bank'),
(8, 'Transfer Bank BRI', 'Rekening 0012-01-098765-50-1 (a.n. Kos Kozzy)', 'bank'),
(9, 'Transfer Bank BSI', 'Rekening 714-5566-778 (a.n. Kos Kozzy)', 'bank'),
(10, 'Transfer Bank Riau Kepri Syariah', 'Rekening 101-22-33445 (a.n. Kos Kozzy)', 'bank');

-- --------------------------------------------------------

--
-- Table structure for table `pengguna`
--

CREATE TABLE `pengguna` (
  `id_pengguna` int(11) NOT NULL,
  `nama` varchar(100) NOT NULL,
  `email` varchar(100) NOT NULL,
  `password` varchar(255) NOT NULL,
  `no_telepon` varchar(20) NOT NULL,
  `peran` enum('admin','pencari') NOT NULL DEFAULT 'pencari',
  `jenis_kelamin` enum('L','P') DEFAULT NULL,
  `asal_daerah` varchar(100) DEFAULT NULL,
  `status_penghuni` enum('mahasiswa','pekerja','lainnya') DEFAULT NULL,
  `instansi` varchar(100) DEFAULT NULL,
  `nama_wali` varchar(100) DEFAULT NULL,
  `no_hp_wali` varchar(20) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `pengguna`
--

INSERT INTO `pengguna` (`id_pengguna`, `nama`, `email`, `password`, `no_telepon`, `peran`, `jenis_kelamin`, `asal_daerah`, `status_penghuni`, `instansi`, `nama_wali`, `no_hp_wali`) VALUES
(1, 'Admin Kozzy (Akiel)', 'admin@kozzy.id', 'admin123', '089661327050', 'admin', NULL, NULL, NULL, NULL, NULL, NULL),
(2, 'Farah Aprelia', 'farah@kozzy.id', '123456', '081234567891', 'pencari', 'P', 'Pekanbaru, Riau', 'mahasiswa', 'Universitas Riau', 'Wali Farah', '082112340002'),
(3, 'Fildza Khusaini', 'fildza@kozzy.id', '123456', '081234567892', 'pencari', 'P', 'Dumai, Riau', 'mahasiswa', 'Universitas Riau', 'Wali Fildza', '082112340003'),
(4, 'Muhammad Abiyyu', 'abiyyu@kozzy.id', 'abiyyu123', '081234567801', 'pencari', 'L', 'Bangkinang, Riau', 'mahasiswa', 'UIN Suska Riau', 'Wali Abiyyu', '082112340004'),
(5, 'Rania Syifa', 'rania@kozzy.id', 'rania123', '081234567802', 'pencari', 'P', 'Siak Sri Indrapura, Riau', 'mahasiswa', 'UIN Suska Riau', 'Wali Rania', '082112340005'),
(6, 'Ilyas Rahman', 'ilyas@kozzy.id', 'ilyas123', '081234567803', 'pencari', 'L', 'Duri, Riau', 'mahasiswa', 'Politeknik Caltex Riau', 'Wali Ilyas', '082112340006'),
(7, 'Najwa Khairunnisa', 'najwa@kozzy.id', 'najwa123', '081234567804', 'pencari', 'P', 'Rengat, Riau', 'mahasiswa', 'Universitas Riau', 'Wali Najwa', '082112340007'),
(8, 'Islam Rahmatan', 'alam@kozzy.id', 'alam123', '081234567805', 'pencari', 'L', 'Bengkalis, Riau', 'mahasiswa', 'Universitas Riau', 'Wali Islam', '082112340008'),
(9, 'Naurah Rafeylah', 'naurah@kozzy.id', 'naurah123', '081234567806', 'pencari', 'P', 'Padang, Sumatera Barat', 'mahasiswa', 'UIN Suska Riau', 'Wali Naurah', '082112340009'),
(10, 'Muhammad Hujjatul', 'dihar@kozzy.id', 'dihar123', '081234567807', 'pencari', 'L', 'Medan, Sumatera Utara', 'mahasiswa', 'Politeknik Caltex Riau', 'Wali Hujjatul', '082112340010'),
(11, 'Backup Admin', 'pengelola@kozzy.id', 'pengelola123', '089661327099', 'admin', NULL, NULL, NULL, NULL, NULL, NULL),
(12, 'Fakhry Ramdhani', 'fakhry@kozzy.id', 'fakhry123', '081234567808', 'pencari', 'L', 'Jambi, Jambi', 'mahasiswa', 'Universitas Riau', 'Wali Fakhry', '082112340012'),
(13, 'Fais Munawwar', 'wars@kozzy.id', 'fais123', '081234567808', 'pencari', 'L', 'Pasir Pengaraian, Riau', 'mahasiswa', 'UIN Suska Riau', 'Wali Fais', '082112340013');

-- --------------------------------------------------------

--
-- Table structure for table `riwayat_verifikasi`
--

CREATE TABLE `riwayat_verifikasi` (
  `id_riwayat` int(11) NOT NULL,
  `keputusan` enum('approved','rejected') NOT NULL,
  `tanggal_keputusan` datetime NOT NULL DEFAULT current_timestamp(),
  `catatan` text DEFAULT NULL,
  `id_pengguna` int(11) NOT NULL,
  `id_booking` varchar(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `riwayat_verifikasi`
--

INSERT INTO `riwayat_verifikasi` (`id_riwayat`, `keputusan`, `tanggal_keputusan`, `catatan`, `id_pengguna`, `id_booking`) VALUES
(1, 'approved', '2026-07-28 11:00:00', 'Pengajuan disetujui. Pembayaran via Transfer BCA telah dikonfirmasi valid.', 1, 'KZ-0101'),
(2, 'approved', '2026-08-05 12:00:00', 'Pengajuan disetujui. Transaksi QRIS berhasil diverifikasi sistem.', 1, 'KZ-0102'),
(3, 'approved', '2026-08-12 15:00:00', 'Pengajuan sewa 6 bulan disetujui. Bukti transfer Bank Mandiri sesuai tarif.', 11, 'KZ-0103'),
(4, 'rejected', '2026-08-28 10:30:00', 'Pengajuan ditolak. Struk transfer buram dan tidak dapat diverifikasi ke bank.', 1, 'KZ-0104'),
(5, 'approved', '2026-09-01 17:30:00', 'Pengajuan disetujui. Pembayaran via dompet digital DANA telah masuk ke kas.', 1, 'KZ-0105'),
(6, 'rejected', '2026-09-08 14:00:00', 'Pengajuan ditolak. Nominal transfer kurang dari harga sewa yang tertera.', 11, 'KZ-0106'),
(7, 'approved', '2026-09-27 11:00:00', 'Pengajuan disetujui. Mutasi Bank BRI telah dicek dan dinyatakan sah.', 1, 'KZ-0107'),
(8, 'approved', '2026-10-10 16:30:00', 'Pengajuan disetujui. Pembayaran melalui Bank BSI telah lunas.', 11, 'KZ-0108'),
(9, 'approved', '2026-10-15 09:00:00', 'Verifikasi berkala penyewa aktif semester ganjil berjalan tertib.', 1, 'KZ-0101'),
(10, 'approved', '2026-10-20 14:00:00', 'Konfirmasi fasilitas kamar telah diterima oleh penghuni dalam kondisi baik.', 11, 'KZ-0102'),
(11, 'approved', '2026-10-07 09:30:00', 'Pengajuan disetujui. Pembayaran via Transfer BCA telah dikonfirmasi.', 1, 'KZ-0117'),
(12, 'approved', '2026-10-25 10:00:00', 'Pemeriksaan lanjutan kelengkapan dokumen penghuni baru telah diverifikasi.', 11, 'KZ-0107'),
(13, 'rejected', '2026-10-30 09:19:44', 'Kamu cewe', 1, 'KZ-0118'),
(14, 'rejected', '2026-10-30 09:23:35', 'gatau', 1, 'KZ-2E63');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `booking`
--
ALTER TABLE `booking`
  ADD PRIMARY KEY (`id_booking`),
  ADD KEY `fk_booking_pengguna` (`id_pengguna`),
  ADD KEY `fk_booking_kamar` (`kode_kamar`),
  ADD KEY `fk_booking_metode` (`id_metode`);

--
-- Indexes for table `bukti_pembayaran`
--
ALTER TABLE `bukti_pembayaran`
  ADD PRIMARY KEY (`id_bukti`),
  ADD KEY `fk_bukti_booking` (`id_booking`);

--
-- Indexes for table `fasilitas`
--
ALTER TABLE `fasilitas`
  ADD PRIMARY KEY (`id_fasilitas`);

--
-- Indexes for table `fasilitas_kamar`
--
ALTER TABLE `fasilitas_kamar`
  ADD PRIMARY KEY (`kamar_kode_kamar`,`fasilitas_id_fasilitas`),
  ADD KEY `fk_fk_fasilitas` (`fasilitas_id_fasilitas`);

--
-- Indexes for table `kamar`
--
ALTER TABLE `kamar`
  ADD PRIMARY KEY (`kode_kamar`);

--
-- Indexes for table `metode_pembayaran`
--
ALTER TABLE `metode_pembayaran`
  ADD PRIMARY KEY (`id_metode`);

--
-- Indexes for table `pengguna`
--
ALTER TABLE `pengguna`
  ADD PRIMARY KEY (`id_pengguna`),
  ADD UNIQUE KEY `uq_pengguna_email` (`email`);

--
-- Indexes for table `riwayat_verifikasi`
--
ALTER TABLE `riwayat_verifikasi`
  ADD PRIMARY KEY (`id_riwayat`),
  ADD KEY `fk_verif_pengguna` (`id_pengguna`),
  ADD KEY `fk_verif_booking` (`id_booking`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `bukti_pembayaran`
--
ALTER TABLE `bukti_pembayaran`
  MODIFY `id_bukti` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=13;

--
-- AUTO_INCREMENT for table `fasilitas`
--
ALTER TABLE `fasilitas`
  MODIFY `id_fasilitas` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=17;

--
-- AUTO_INCREMENT for table `metode_pembayaran`
--
ALTER TABLE `metode_pembayaran`
  MODIFY `id_metode` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `pengguna`
--
ALTER TABLE `pengguna`
  MODIFY `id_pengguna` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `riwayat_verifikasi`
--
ALTER TABLE `riwayat_verifikasi`
  MODIFY `id_riwayat` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `booking`
--
ALTER TABLE `booking`
  ADD CONSTRAINT `fk_booking_kamar` FOREIGN KEY (`kode_kamar`) REFERENCES `kamar` (`kode_kamar`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_booking_metode` FOREIGN KEY (`id_metode`) REFERENCES `metode_pembayaran` (`id_metode`) ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_booking_pengguna` FOREIGN KEY (`id_pengguna`) REFERENCES `pengguna` (`id_pengguna`) ON UPDATE CASCADE;

--
-- Constraints for table `bukti_pembayaran`
--
ALTER TABLE `bukti_pembayaran`
  ADD CONSTRAINT `fk_bukti_booking` FOREIGN KEY (`id_booking`) REFERENCES `booking` (`id_booking`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `fasilitas_kamar`
--
ALTER TABLE `fasilitas_kamar`
  ADD CONSTRAINT `fk_fk_fasilitas` FOREIGN KEY (`fasilitas_id_fasilitas`) REFERENCES `fasilitas` (`id_fasilitas`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_fk_kamar` FOREIGN KEY (`kamar_kode_kamar`) REFERENCES `kamar` (`kode_kamar`) ON DELETE CASCADE ON UPDATE CASCADE;

--
-- Constraints for table `riwayat_verifikasi`
--
ALTER TABLE `riwayat_verifikasi`
  ADD CONSTRAINT `fk_verif_booking` FOREIGN KEY (`id_booking`) REFERENCES `booking` (`id_booking`) ON DELETE CASCADE ON UPDATE CASCADE,
  ADD CONSTRAINT `fk_verif_pengguna` FOREIGN KEY (`id_pengguna`) REFERENCES `pengguna` (`id_pengguna`) ON UPDATE CASCADE;
-- --------------------------------------------------------

--
-- Table structure for table `keluhan`
--

CREATE TABLE `keluhan` (
  `id_keluhan` int(11) NOT NULL AUTO_INCREMENT,
  `id_pengguna` int(11) NOT NULL,
  `kode_kamar` varchar(10) NOT NULL,
  `kategori` enum('ac','listrik','air','wifi','furnitur','kebersihan','lainnya') NOT NULL DEFAULT 'lainnya',
  `deskripsi` varchar(500) NOT NULL,
  `foto` varchar(255) DEFAULT NULL,
  `status` enum('baru','diproses','selesai') NOT NULL DEFAULT 'baru',
  `catatan_admin` varchar(500) DEFAULT NULL,
  `created_at` datetime NOT NULL DEFAULT current_timestamp(),
  `updated_at` datetime DEFAULT NULL,
  PRIMARY KEY (`id_keluhan`),
  KEY `idx_keluhan_status` (`status`,`created_at`),
  KEY `idx_keluhan_pengguna` (`id_pengguna`),
  KEY `idx_keluhan_kamar` (`kode_kamar`),
  CONSTRAINT `fk_keluhan_pengguna` FOREIGN KEY (`id_pengguna`) REFERENCES `pengguna` (`id_pengguna`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_keluhan_kamar` FOREIGN KEY (`kode_kamar`) REFERENCES `kamar` (`kode_kamar`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `keluhan`
--

INSERT INTO `keluhan` (`id_keluhan`, `id_pengguna`, `kode_kamar`, `kategori`, `deskripsi`, `foto`, `status`, `catatan_admin`, `created_at`, `updated_at`) VALUES
(1, 6, 'A-02', 'ac', 'AC kamar tidak dingin sejak dua hari lalu, sudah dicoba mode cool 16 derajat.', NULL, 'diproses', 'Teknisi dijadwalkan datang hari Senin, mohon kamar dalam keadaan tidak terkunci.', '2026-11-10 09:15:00', '2026-11-11 10:00:00'),
(2, 8, 'A-03', 'wifi', 'Wi-Fi sering putus pada malam hari, terutama di atas jam 9.', NULL, 'baru', NULL, '2026-11-13 20:40:00', NULL),
(3, 12, 'B-03', 'air', 'Keran di kamar mandi bocor dan menetes terus.', NULL, 'selesai', 'Keran sudah diganti dan sudah dicek ulang.', '2026-11-03 08:20:00', '2026-11-04 14:30:00');

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
