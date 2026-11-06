package com.kozzy.controllers;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.impl.BookingDAOImpl;
import com.kozzy.dao.impl.KamarDAOImpl;
import com.kozzy.dao.impl.VerifikasiDAOImpl;
import com.kozzy.dao.interfaces.IBookingDAO;
import com.kozzy.dao.interfaces.IKamarDAO;
import com.kozzy.dao.interfaces.IVerifikasiDAO;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
import com.kozzy.models.Pengguna;
import com.kozzy.models.RiwayatVerifikasi;
import com.kozzy.services.SessionManager;

import java.util.List;

/**
 * AdminController: Menangani seluruh logika proses pengelolaan administratif kos.
 * Mencakup verifikasi sewa (approve/reject), perubahan status ketersediaan kamar,
 * dan peninjauan riwayat log verifikasi.
 * Memisahkan logika proses admin dari class tampilan (View).
 */
public class AdminController {

    private final IBookingDAO bookingDAO;
    private final IKamarDAO kamarDAO;
    private final IVerifikasiDAO verifikasiDAO;

    public AdminController() {
        this.bookingDAO = new BookingDAOImpl();
        this.kamarDAO = new KamarDAOImpl();
        this.verifikasiDAO = new VerifikasiDAOImpl();
    }

    public AdminController(IBookingDAO bookingDAO, IKamarDAO kamarDAO, IVerifikasiDAO verifikasiDAO) {
        this.bookingDAO = bookingDAO;
        this.kamarDAO = kamarDAO;
        this.verifikasiDAO = verifikasiDAO;
    }

    /**
     * Mengambil daftar seluruh permohonan booking yang berstatus 'pending'.
     */
    public List<Booking> getPendingBookings() {
        return bookingDAO.getAllBookings("pending");
    }

    /**
     * Mengambil daftar seluruh kamar untuk kontrol operasional ketersediaan.
     */
    public List<Kamar> getAllKamar() {
        return kamarDAO.getAllKamar("semua");
    }

    /**
     * Mengambil seluruh log audit riwayat verifikasi yang telah dilakukan admin.
     */
    public List<RiwayatVerifikasi> getRiwayatVerifikasi() {
        return verifikasiDAO.getAllRiwayat();
    }

    /**
     * Memperbarui status ketersediaan kamar kos (misal: "tersedia", "pemeliharaan").
     */
    public boolean updateStatusKamar(String kodeKamar, String statusBaru) {
        if (kodeKamar == null || statusBaru == null) return false;
        return kamarDAO.updateStatus(kodeKamar.trim(), statusBaru.trim(), null);
    }

    /**
     * Menyetujui pengajuan booking sewa kamar oleh penyewa.
     */
    public boolean approveBooking(String idBooking) {
        if (idBooking == null || idBooking.trim().isEmpty()) return false;
        Pengguna admin = SessionManager.getInstance().getCurrentUser();
        int idAdmin = (admin != null) ? admin.getIdPengguna() : 1;
        return bookingDAO.approveBooking(idBooking.trim(), idAdmin);
    }

    /**
     * Menolak pengajuan booking sewa kamar beserta alasan penolakan.
     */
    public boolean rejectBooking(String idBooking, String alasan) {
        if (idBooking == null || idBooking.trim().isEmpty()) return false;
        Pengguna admin = SessionManager.getInstance().getCurrentUser();
        int idAdmin = (admin != null) ? admin.getIdPengguna() : 1;
        String reason = (alasan != null && !alasan.trim().isEmpty()) ? alasan.trim() : "Ditolak oleh admin kos.";
        return bookingDAO.rejectBooking(idBooking.trim(), reason, idAdmin);
    }

    /**
     * Memeriksa status konektivitas basis data MySQL.
     */
    public boolean isDatabaseConnected() {
        return DatabaseConnection.checkConnection();
    }
}
