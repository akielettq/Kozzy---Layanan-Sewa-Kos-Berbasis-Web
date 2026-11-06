package com.kozzy.controllers;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.impl.BookingDAOImpl;
import com.kozzy.dao.impl.KamarDAOImpl;
import com.kozzy.dao.interfaces.IBookingDAO;
import com.kozzy.dao.interfaces.IKamarDAO;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
import com.kozzy.models.Pengguna;
import com.kozzy.services.SessionManager;

import java.util.List;

/**
 * BerandaController: Menangani kalkulasi metrik ringkasan eksekutif (KPI)
 * dan status operasional sistem untuk tampilan Beranda.
 * Memisahkan logika hitung dan agregasi data dari class tampilan (View).
 */
public class BerandaController {

    private final IKamarDAO kamarDAO;
    private final IBookingDAO bookingDAO;

    /**
     * DTO (Data Transfer Object) untuk menampung ringkasan statistik beranda
     */
    public static class BerandaSummary {
        private final int totalKamar;
        private final int kamarTersedia;
        private final int kamarTerisi;
        private final int totalPending;
        private final List<Kamar> roomList;
        private final boolean databaseConnected;
        private final Pengguna currentUser;

        public BerandaSummary(int totalKamar, int kamarTersedia, int kamarTerisi,
                              int totalPending, List<Kamar> roomList,
                              boolean databaseConnected, Pengguna currentUser) {
            this.totalKamar = totalKamar;
            this.kamarTersedia = kamarTersedia;
            this.kamarTerisi = kamarTerisi;
            this.totalPending = totalPending;
            this.roomList = roomList;
            this.databaseConnected = databaseConnected;
            this.currentUser = currentUser;
        }

        public int getTotalKamar() { return totalKamar; }
        public int getKamarTersedia() { return kamarTersedia; }
        public int getKamarTerisi() { return kamarTerisi; }
        public int getTotalPending() { return totalPending; }
        public List<Kamar> getRoomList() { return roomList; }
        public boolean isDatabaseConnected() { return databaseConnected; }
        public Pengguna getCurrentUser() { return currentUser; }
    }

    public BerandaController() {
        this.kamarDAO = new KamarDAOImpl();
        this.bookingDAO = new BookingDAOImpl();
    }

    public BerandaController(IKamarDAO kamarDAO, IBookingDAO bookingDAO) {
        this.kamarDAO = kamarDAO;
        this.bookingDAO = bookingDAO;
    }

    /**
     * Mengumpulkan dan mengkalkulasi seluruh statistik operasional untuk Beranda.
     */
    public BerandaSummary getSummary() {
        boolean dbOk = DatabaseConnection.checkConnection();
        Pengguna user = SessionManager.getInstance().getCurrentUser();

        List<Kamar> allKamar = kamarDAO.getAllKamar("semua");
        int total = allKamar.size();
        int tersedia = 0;
        int terisi = 0;

        for (Kamar k : allKamar) {
            if ("tersedia".equalsIgnoreCase(k.getStatus())) {
                tersedia++;
            } else if ("terisi".equalsIgnoreCase(k.getStatus())) {
                terisi++;
            }
        }

        List<Booking> pending = bookingDAO.getAllBookings("pending");
        int totalPending = pending.size();

        return new BerandaSummary(total, tersedia, terisi, totalPending, allKamar, dbOk, user);
    }

    public boolean isDatabaseConnected() {
        return DatabaseConnection.checkConnection();
    }
}
