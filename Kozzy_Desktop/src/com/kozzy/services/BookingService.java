package com.kozzy.services;

import com.kozzy.dao.impl.BookingDAOImpl;
import com.kozzy.dao.impl.KamarDAOImpl;
import com.kozzy.dao.interfaces.IBookingDAO;
import com.kozzy.dao.interfaces.IKamarDAO;
import com.kozzy.models.Booking;

public class BookingService {
    private final IBookingDAO bookingDAO;
    private final IKamarDAO kamarDAO;

    public BookingService() {
        this.bookingDAO = new BookingDAOImpl();
        this.kamarDAO = new KamarDAOImpl();
    }

    public void cekMasaSewa() {
        kamarDAO.updateExpiredRooms();
    }

    public String ajukanBooking(Booking booking, String proofFilePath) {
        // 1. Cek apakah pengguna sudah memiliki sewa aktif atau pending
        if (bookingDAO.hasActiveBooking(booking.getIdPengguna())) {
            return "Kamu masih memiliki pengajuan atau sewa aktif. Selesaikan sewa tersebut sebelum memesan kamar baru.";
        }

        // 2. Cek apakah kamar sedang ada pengajuan pending
        if (bookingDAO.hasPendingBookingForRoom(booking.getKodeKamar())) {
            return "Kamar ini sedang dalam proses verifikasi booking oleh pengguna lain. Silakan pilih kamar lain.";
        }

        // 3. Generate ID Booking unik: BK-timestamp-kode
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
