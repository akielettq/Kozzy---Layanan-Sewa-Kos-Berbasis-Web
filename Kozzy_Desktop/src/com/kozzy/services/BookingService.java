package com.kozzy.services;

import com.kozzy.dao.impl.BookingDAOImpl;
import com.kozzy.dao.impl.KamarDAOImpl;
import com.kozzy.dao.impl.UserDAOImpl;
import com.kozzy.dao.interfaces.IBookingDAO;
import com.kozzy.dao.interfaces.IKamarDAO;
import com.kozzy.dao.interfaces.IUserDAO;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
import com.kozzy.models.Pengguna;

public class BookingService {
    private final IBookingDAO bookingDAO;
    private final IKamarDAO kamarDAO;
    private final IUserDAO userDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAOImpl();
        this.kamarDAO = new KamarDAOImpl();
        this.userDAO = new UserDAOImpl();
    }

    public void cekMasaSewa() {
        kamarDAO.updateExpiredRooms();
    }

    public String ajukanBooking(Booking booking, String proofFilePath) {
        // 1. Cek apakah pengguna sudah memiliki sewa aktif atau pending
        if (bookingDAO.hasActiveBooking(booking.getIdPengguna())) {
            return "Kamu masih memiliki pengajuan atau sewa aktif. Selesaikan sewa tersebut sebelum memesan kamar baru.";
        }

        // 2. Cek ketersediaan kamar
        Kamar kamar = kamarDAO.getByKode(booking.getKodeKamar());
        if (kamar != null && !"tersedia".equalsIgnoreCase(kamar.getStatus())) {
            return "Kamar " + kamar.getKodeKamar() + " saat ini sedang berstatus " + kamar.getStatus() + ".";
        }

        // 3. Cek kesesuaian jenis kelamin penyewa dengan tipe kamar (Putra vs Putri)
        Pengguna user = userDAO.findById(booking.getIdPengguna());
        if (user != null && kamar != null) {
            String jk = user.getJenisKelamin().trim().toLowerCase();
            String tipe = kamar.getTipe().trim().toLowerCase();
            if (jk.contains("laki") && tipe.contains("putri")) {
                return "Kamar " + kamar.getKodeKamar() + " adalah kamar khusus Putri. Akun Anda berjenis kelamin Laki-laki.";
            }
            if (jk.contains("perempuan") && tipe.contains("putra")) {
                return "Kamar " + kamar.getKodeKamar() + " adalah kamar khusus Putra. Akun Anda berjenis kelamin Perempuan.";
            }
        }

        // 4. Cek apakah kamar sedang ada pengajuan pending
        if (bookingDAO.hasPendingBookingForRoom(booking.getKodeKamar())) {
            return "Kamar ini sedang dalam proses verifikasi booking oleh pengguna lain. Silakan pilih kamar lain.";
        }

        // 5. Generate ID Booking unik: BK-timestamp-kode
        String idBooking = "BK-" + (System.currentTimeMillis() / 1000) + "-" + booking.getKodeKamar();
        booking.setIdBooking(idBooking);

        boolean success = bookingDAO.createBooking(booking, proofFilePath);
        if (success) {
            return null; // Sukses
        } else {
            return "Gagal memproses pemesanan ke basis data. Pastikan file bukti valid.";
        }
    }
}
