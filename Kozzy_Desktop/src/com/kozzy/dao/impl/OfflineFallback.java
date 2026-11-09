package com.kozzy.dao.impl;

import com.kozzy.models.*;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * OfflineFallback: Penyedia data sampel cadangan jika database MySQL (XAMPP) sedang tidak aktif.
 * Menggunakan memori sederhana (in-memory) tanpa dependensi eksternal, sehingga kode sangat simpel,
 * bersih, dan mudah dijelaskan kepada dosen pengampu.
 */
public class OfflineFallback {
    private static OfflineFallback instance;

    private final List<Pengguna> users = new ArrayList<>();
    private final List<Kamar> rooms = new ArrayList<>();
    private final List<MetodePembayaran> methods = new ArrayList<>();
    private final List<Booking> bookings = new ArrayList<>();
    private final List<RiwayatVerifikasi> history = new ArrayList<>();

    private int nextBookingNum = 120;
    private int nextVerifId = 3;

    public static synchronized OfflineFallback getInstance() {
        if (instance == null) {
            instance = new OfflineFallback();
        }
        return instance;
    }

    private OfflineFallback() {
        initDefaultData();
    }

    private void initDefaultData() {
        // 1. Data Pengguna (Sesuai kozzy_db.sql resmi)
        users.add(new Admin(1, "Admin Kozzy (Akiel)", "admin@kozzy.id", "admin123", "089661327050", "Laki-laki"));
        users.add(new Pencari(2, "Farah Aprelia", "farah@kozzy.id", "123456", "081234567891", "Perempuan"));
        users.add(new Pencari(3, "Fildza Khusaini", "fildza@kozzy.id", "123456", "081234567892", "Perempuan"));
        users.add(new Pencari(4, "Muhammad Abiyyu", "abiyyu@kozzy.id", "abiyyu123", "081234567801", "Laki-laki"));
        users.add(new Pencari(5, "Rania Syifa", "rania@kozzy.id", "rania123", "081234567802", "Perempuan"));
        users.add(new Pencari(6, "Ilyas Rahman", "ilyas@kozzy.id", "ilyas123", "081234567803", "Laki-laki"));
        users.add(new Pencari(7, "Najwa Khairunnisa", "najwa@kozzy.id", "najwa123", "081234567804", "Perempuan"));
        users.add(new Pencari(8, "Islam Rahmatan", "alam@kozzy.id", "alam123", "081234567805", "Laki-laki"));
        users.add(new Pencari(9, "Naurah Rafeylah", "naurah@kozzy.id", "naurah123", "081234567806", "Perempuan"));
        users.add(new Pencari(10, "Muhammad Hujjatul", "dihar@kozzy.id", "dihar123", "081234567807", "Laki-laki"));
        users.add(new Admin(11, "Backup Admin", "pengelola@kozzy.id", "pengelola123", "089661327099", "Laki-laki"));
        users.add(new Pencari(12, "Fakhry Ramdhani", "fakhry@kozzy.id", "fakhry123", "081234567808", "Laki-laki"));
        users.add(new Pencari(13, "Fais Munawwar", "wars@kozzy.id", "fais123", "081234567808", "Laki-laki"));

        // 2. Data Master Kamar (10 Kamar sesuai skema kozzy_db.sql: A = Putra, B = Putri)
        Kamar a1 = new Kamar("A-01", "putra", 850000, "tersedia", null, "Kamar lantai 1, dekat area parkiran motor, sirkulasi udara baik.");
        a1.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "Lemari Pakaian", "AC Dingin 1/2 PK", "Area Parkir Motor"));
        rooms.add(a1);

        Kamar a2 = new Kamar("A-02", "putra", 950000, "tersedia", null, "Kamar lantai 1 menghadap halaman taman dengan jendela kaca lebar.");
        a2.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "Lemari Pakaian", "AC Dingin 1/2 PK", "Meja Belajar"));
        rooms.add(a2);

        Kamar a3 = new Kamar("A-03", "putra", 1100000, "terisi", Date.valueOf("2026-12-01"), "Kamar lantai 2 dengan lemari pakaian besar dan meja belajar ergonomis.");
        a3.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "AC Dingin 1/2 PK", "Meja Belajar", "Kamar Mandi Dalam"));
        rooms.add(a3);

        Kamar a4 = new Kamar("A-04", "putra", 900000, "terisi", Date.valueOf("2026-11-20"), "Kamar lantai 2 dengan meja kerja dan rak buku minimalis.");
        a4.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "Lemari Pakaian", "Meja Belajar", "Keamanan CCTV 24 Jam"));
        rooms.add(a4);

        Kamar a5 = new Kamar("A-05", "putra", 1200000, "tersedia", null, "Kamar VIP putra lantai 2 dilengkapi pendingin AC dan kamar mandi dalam.");
        a5.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "AC Dingin 1/2 PK", "Kamar Mandi Dalam", "Water Heater"));
        rooms.add(a5);

        Kamar b1 = new Kamar("B-01", "putri", 950000, "tersedia", null, "Kamar lantai 1 dekat lobi utama, aman, nyaman, dan tenang.");
        b1.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "Lemari Pakaian", "AC Dingin 1/2 PK", "Dapur Bersama"));
        rooms.add(b1);

        Kamar b2 = new Kamar("B-02", "putri", 1150000, "terisi", Date.valueOf("2026-11-15"), "Kamar lantai 1 dekat taman dalam dengan suasana asri dan sejuk.");
        b2.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "AC Dingin 1/2 PK", "Kamar Mandi Dalam", "Dapur Bersama"));
        rooms.add(b2);

        Kamar b3 = new Kamar("B-03", "putri", 1050000, "tersedia", null, "Kamar lantai 2 dengan meja rias, lemari besar, dan pencahayaan optimal.");
        b3.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "Lemari Pakaian", "Meja Belajar", "Keamanan CCTV 24 Jam"));
        rooms.add(b3);

        Kamar b4 = new Kamar("B-04", "putri", 1350000, "pemeliharaan", null, "Kamar sedang dalam pemeliharaan rutin, pengecatan dinding, dan servis AC.");
        b4.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "Lemari Pakaian", "AC Dingin 1/2 PK", "Dapur Bersama"));
        rooms.add(b4);

        Kamar b5 = new Kamar("B-05", "putri", 1250000, "tersedia", null, "Kamar VIP putri lantai 2 dilengkapi kamar mandi dalam dan balkon pribadi.");
        b5.setFasilitas(Arrays.asList("Wi-Fi Cepat 100 Mbps", "Kasur Springbed", "AC Dingin 1/2 PK", "Kamar Mandi Dalam", "Water Heater"));
        rooms.add(b5);

        // 3. Data Metode Pembayaran
        methods.add(new MetodePembayaran(1, "Transfer Bank BCA", "0123-456-789 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(2, "Transfer Bank Mandiri", "9876-543-210 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(3, "Transfer Bank BNI", "1122-334-455 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(4, "Transfer Bank BRI", "5566-778-899 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(5, "Bank Syariah Indonesia (BSI)", "7788-990-011 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(6, "GoPay", "0896-6132-7050 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(7, "OVO", "0896-6132-7050 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(8, "DANA", "0896-6132-7050 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(9, "ShopeePay", "0896-6132-7050 a.n Kozzy Kos"));
        methods.add(new MetodePembayaran(10, "QRIS All Payment", "NMID: ID102030405060 a.n Kozzy Kos"));

        // 4. Data Booking Sampel
        Booking bFarah = new Booking("KZ-0117", Date.valueOf("2026-10-07"), 1, "approved", null, 2, "A-03", 1, new Timestamp(System.currentTimeMillis() - 86400000L * 7));
        bFarah.setNamaPengguna("Farah Aprelia");
        bFarah.setTipeKamar("putra");
        bFarah.setHargaKamar(1100000);
        bFarah.setNamaMetode("Transfer Bank BCA");
        bFarah.setFileBukti("bukti_1794242133182.jpg");
        bookings.add(bFarah);

        Booking bFildza = new Booking("KZ-0119", Date.valueOf("2026-11-05"), 3, "pending", null, 3, "B-02", 8, new Timestamp(System.currentTimeMillis() - 3600000L * 3));
        bFildza.setNamaPengguna("Fildza Khusaini");
        bFildza.setTipeKamar("putri");
        bFildza.setHargaKamar(1150000);
        bFildza.setNamaMetode("DANA");
        bFildza.setFileBukti("bukti_1794199489985.jpg");
        bookings.add(bFildza);

        // 5. Data Riwayat Verifikasi
        history.add(new RiwayatVerifikasi(1, "approved", new Timestamp(System.currentTimeMillis() - 86400000L * 6), "Pembayaran sah terverifikasi. Kunci fisik dapat diambil di kantor pengelola.", 1, "KZ-0117", "Admin Kozzy (Akiel)"));
    }

    // --- Pengguna ---
    public Pengguna findUserByEmail(String email) {
        if (email == null) return null;
        for (Pengguna u : users) {
            if (u.getEmail().equalsIgnoreCase(email.trim())) return u;
        }
        return null;
    }

    public Pengguna findUserById(int idPengguna) {
        for (Pengguna u : users) {
            if (u.getIdPengguna() == idPengguna) return u;
        }
        return null;
    }

    public boolean addUser(Pengguna user) {
        if (user == null || findUserByEmail(user.getEmail()) != null) return false;
        user.setIdPengguna(users.size() + 1);
        users.add(user);
        return true;
    }

    // --- Kamar ---
    public List<Kamar> getAllRooms(String filterTipe) {
        List<Kamar> res = new ArrayList<>();
        boolean checkTipe = (filterTipe != null && !filterTipe.isEmpty() && !filterTipe.equalsIgnoreCase("semua"));
        for (Kamar k : rooms) {
            if (!checkTipe || k.getTipe().equalsIgnoreCase(filterTipe)) {
                res.add(k);
            }
        }
        return res;
    }

    public Kamar getRoomByKode(String kodeKamar) {
        for (Kamar k : rooms) {
            if (k.getKodeKamar().equalsIgnoreCase(kodeKamar)) return k;
        }
        return null;
    }

    public boolean updateRoomStatus(String kodeKamar, String newStatus) {
        Kamar k = getRoomByKode(kodeKamar);
        if (k != null) {
            k.setStatus(newStatus.toLowerCase());
            return true;
        }
        return false;
    }

    // --- Metode Pembayaran ---
    public List<MetodePembayaran> getAllMethods() {
        return new ArrayList<>(methods);
    }

    // --- Booking ---
    public List<Booking> getBookingsByPengguna(int idPengguna) {
        List<Booking> res = new ArrayList<>();
        for (Booking b : bookings) {
            if (b.getIdPengguna() == idPengguna) res.add(b);
        }
        return res;
    }

    public List<Booking> getAllBookings(String statusFilter) {
        List<Booking> res = new ArrayList<>();
        boolean check = (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("semua"));
        for (Booking b : bookings) {
            if (!check || (b.getStatus() != null && b.getStatus().equalsIgnoreCase(statusFilter))) {
                if (b.getJenisKelaminPengguna() == null || b.getNamaPengguna() == null) {
                    Pengguna u = findUserById(b.getIdPengguna());
                    if (u != null) {
                        b.setJenisKelaminPengguna(u.getJenisKelamin());
                        if (b.getNamaPengguna() == null || b.getNamaPengguna().isEmpty()) {
                            b.setNamaPengguna(u.getNama());
                        }
                    }
                }
                res.add(b);
            }
        }
        return res;
    }

    public Booking getById(String idBooking) {
        for (Booking b : bookings) {
            if (b.getIdBooking().equalsIgnoreCase(idBooking)) return b;
        }
        return null;
    }

    public boolean hasActiveBooking(int idPengguna) {
        for (Booking b : bookings) {
            if (b.getIdPengguna() == idPengguna && ("approved".equalsIgnoreCase(b.getStatus()) || "pending".equalsIgnoreCase(b.getStatus()))) {
                return true;
            }
        }
        return false;
    }

    public boolean hasPendingBookingForRoom(String kodeKamar) {
        for (Booking b : bookings) {
            if (b.getKodeKamar().equalsIgnoreCase(kodeKamar) && "pending".equalsIgnoreCase(b.getStatus())) {
                return true;
            }
        }
        return false;
    }

    public boolean addBooking(Booking b, String proofFilePath) {
        if (b == null) return false;
        if (b.getStatus() == null || b.getStatus().isEmpty()) b.setStatus("pending");
        if (b.getIdBooking() == null || b.getIdBooking().isEmpty()) {
            b.setIdBooking("KZ-0" + (nextBookingNum++));
        }
        if (proofFilePath != null && !proofFilePath.isEmpty()) {
            b.setFileBukti(proofFilePath);
        }
        bookings.add(0, b);
        return true;
    }

    public boolean approveBooking(String idBooking, int adminId) {
        return approveBooking(idBooking, "Pembayaran sah terverifikasi. Kunci fisik dapat diambil di pengelola.", adminId);
    }

    public boolean approveBooking(String idBooking, String catatan, int adminId) {
        for (Booking b : bookings) {
            if (b.getIdBooking().equalsIgnoreCase(idBooking)) {
                b.setStatus("approved");
                b.setAlasan(null);

                // Update status kamar jadi terisi
                Kamar k = getRoomByKode(b.getKodeKamar());
                if (k != null) {
                    k.setStatus("terisi");
                    LocalDate end = LocalDate.now().plusMonths(b.getDurasi());
                    k.setTerisiHingga(Date.valueOf(end));
                }

                // Tambah riwayat verifikasi
                history.add(0, new RiwayatVerifikasi(
                        nextVerifId++, "approved", new Timestamp(System.currentTimeMillis()),
                        catatan, adminId, idBooking, "Admin Kozzy"
                ));
                return true;
            }
        }
        return false;
    }

    public boolean rejectBooking(String idBooking, String alasan, int adminId) {
        for (Booking b : bookings) {
            if (b.getIdBooking().equalsIgnoreCase(idBooking)) {
                b.setStatus("rejected");
                b.setAlasan(alasan);

                // Tambah riwayat verifikasi
                history.add(0, new RiwayatVerifikasi(
                        nextVerifId++, "rejected", new Timestamp(System.currentTimeMillis()),
                        alasan, adminId, idBooking, "Admin Kozzy"
                ));
                return true;
            }
        }
        return false;
    }

    // --- Riwayat ---
    public List<RiwayatVerifikasi> getAllHistory() {
        return new ArrayList<>(history);
    }
}
