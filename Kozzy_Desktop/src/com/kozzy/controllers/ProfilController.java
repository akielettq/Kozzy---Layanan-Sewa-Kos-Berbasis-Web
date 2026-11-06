package com.kozzy.controllers;

import com.kozzy.dao.impl.BookingDAOImpl;
import com.kozzy.dao.interfaces.IBookingDAO;
import com.kozzy.models.Booking;
import com.kozzy.models.Pengguna;
import com.kozzy.services.SessionManager;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * ProfilController: Menangani logika proses data profil pengguna dan kalkulasi statistik akun.
 * Memisahkan logika perhitungan pengeluaran dan riwayat dari class tampilan (View).
 */
public class ProfilController {

    private final IBookingDAO bookingDAO;

    /**
     * DTO (Data Transfer Object) untuk data profil dan ringkasan aktivitas akun
     */
    public static class ProfilSummary {
        private final Pengguna user;
        private final int totalBooking;
        private final int totalAktif;
        private final int totalPengeluaran;

        public ProfilSummary(Pengguna user, int totalBooking, int totalAktif, int totalPengeluaran) {
            this.user = user;
            this.totalBooking = totalBooking;
            this.totalAktif = totalAktif;
            this.totalPengeluaran = totalPengeluaran;
        }

        public Pengguna getUser() { return user; }
        public int getTotalBooking() { return totalBooking; }
        public int getTotalAktif() { return totalAktif; }
        public int getTotalPengeluaran() { return totalPengeluaran; }

        public String getFormattedPengeluaran() {
            NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
            nf.setMaximumFractionDigits(0);
            return nf.format(totalPengeluaran).replace("Rp", "Rp ");
        }
    }

    public ProfilController() {
        this.bookingDAO = new BookingDAOImpl();
    }

    public ProfilController(IBookingDAO bookingDAO) {
        this.bookingDAO = bookingDAO;
    }

    /**
     * Menghitung statistik akun pengguna yang sedang login.
     */
    public ProfilSummary getProfilSummary() {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            return new ProfilSummary(null, 0, 0, 0);
        }

        List<Booking> list = bookingDAO.getByPengguna(user.getIdPengguna());
        int totalBooking = list.size();
        int totalAktif = 0;
        int totalPengeluaran = 0;

        for (Booking b : list) {
            if ("disetujui".equalsIgnoreCase(b.getStatus()) || "approved".equalsIgnoreCase(b.getStatus())) {
                totalAktif++;
                totalPengeluaran += b.hitungTotalBiaya();
            }
        }

        return new ProfilSummary(user, totalBooking, totalAktif, totalPengeluaran);
    }
}
