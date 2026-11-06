package com.kozzy.controllers;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.impl.BookingDAOImpl;
import com.kozzy.dao.impl.KamarDAOImpl;
import com.kozzy.dao.impl.MetodeDAOImpl;
import com.kozzy.dao.interfaces.IBookingDAO;
import com.kozzy.dao.interfaces.IKamarDAO;
import com.kozzy.dao.interfaces.IMetodeDAO;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
import com.kozzy.models.MetodePembayaran;
import com.kozzy.models.Pengguna;
import com.kozzy.services.BookingService;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingController: Menangani seluruh logika proses pemesanan/sewa kamar kos.
 * Mencakup validasi kelayakan sewa, kalkulasi total tarif, pengajuan sewa baru,
 * serta penelusuran sewa aktif dan riwayat pemesanan penyewa.
 * Memisahkan logika proses booking dari class tampilan (View).
 */
public class BookingController {

    private final IBookingDAO bookingDAO;
    private final IKamarDAO kamarDAO;
    private final IMetodeDAO metodeDAO;
    private final BookingService bookingService;

    public BookingController() {
        this.bookingDAO = new BookingDAOImpl();
        this.kamarDAO = new KamarDAOImpl();
        this.metodeDAO = new MetodeDAOImpl();
        this.bookingService = new BookingService();
    }

    public BookingController(IBookingDAO bookingDAO, IKamarDAO kamarDAO, IMetodeDAO metodeDAO, BookingService bookingService) {
        this.bookingDAO = bookingDAO;
        this.kamarDAO = kamarDAO;
        this.metodeDAO = metodeDAO;
        this.bookingService = bookingService;
    }

    /**
     * Memeriksa dan memperbarui masa sewa yang telah kedaluwarsa.
     */
    public void cekMasaSewa() {
        bookingService.cekMasaSewa();
    }

    /**
     * Mengambil seluruh daftar metode pembayaran aktif.
     */
    public List<MetodePembayaran> getAllPaymentMethods() {
        return metodeDAO.getAllMetode();
    }

    /**
     * Memvalidasi apakah seorang pengguna berhak melakukan booking kamar.
     */
    public boolean canUserBook(Pengguna user, Kamar kamar, StringBuilder outReason) {
        if (user == null) {
            if (outReason != null) {
                outReason.append("Silakan masuk ke akun pencari terlebih dahulu untuk memesan kamar.");
            }
            return false;
        }

        if (user.isAdmin()) {
            if (outReason != null) {
                outReason.append("Akun pengelola/admin tidak dapat memesan kamar kos.");
            }
            return false;
        }

        if (kamar == null || !"tersedia".equalsIgnoreCase(kamar.getStatus())) {
            if (outReason != null) {
                outReason.append("Kamar ini sedang tidak tersedia untuk dipesan.");
            }
            return false;
        }

        return true;
    }

    /**
     * Menghitung total biaya sewa berdasarkan tarif kamar dan durasi bulan.
     */
    public int hitungTotalBiaya(Kamar kamar, int durasiBulan) {
        if (kamar == null || durasiBulan <= 0) return 0;
        return kamar.getHarga() * durasiBulan;
    }

    /**
     * Memproses pengajuan sewa kamar baru.
     */
    public String submitBooking(Pengguna user, Kamar kamar, Date tanggalMulai, int durasi, int idMetode, String pathBukti) {
        if (user == null || kamar == null || tanggalMulai == null || durasi <= 0) return "Data pemesanan tidak lengkap.";
        Booking b = new Booking();
        b.setIdPengguna(user.getIdPengguna());
        b.setKodeKamar(kamar.getKodeKamar());
        b.setTanggalMulai(tanggalMulai);
        b.setDurasi(durasi);
        b.setIdMetode(idMetode);
        return bookingService.ajukanBooking(b, pathBukti);
    }

    /**
     * Mengambil daftar seluruh riwayat booking milik pengguna tertentu.
     */
    public List<Booking> getUserBookings(int idPengguna) {
        return bookingDAO.getByPengguna(idPengguna);
    }

    /**
     * Mencari pemesanan yang sedang berstatus aktif berjalan untuk pengguna tertentu.
     */
    public Booking getActiveRental(int idPengguna) {
        List<Booking> list = bookingDAO.getByPengguna(idPengguna);
        for (Booking b : list) {
            if ("approved".equalsIgnoreCase(b.getStatus()) || "disetujui".equalsIgnoreCase(b.getStatus())) {
                return b;
            }
        }
        return null;
    }

    /**
     * Mencari data kamar untuk booking tertentu.
     */
    public Kamar getKamarByKode(String kodeKamar) {
        return kamarDAO.getByKode(kodeKamar);
    }

    /**
     * Memproses pengajuan booking lengkap dengan pembuatan ID booking dan validasi.
     */
    public String ajukanBooking(Booking booking, String proofFilePath) {
        return bookingService.ajukanBooking(booking, proofFilePath);
    }

    /**
     * Memeriksa status konektivitas basis data MySQL.
     */
    public boolean isDatabaseConnected() {
        return DatabaseConnection.checkConnection();
    }
}
