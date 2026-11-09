package com.kozzy.models;

/**
 * Subclass Admin: Mewarisi Pengguna dengan hak akses kelola dan verifikasi.
 */
public class Admin extends Pengguna {

    public Admin() {
        super();
        this.peran = "admin";
    }

    public Admin(int idPengguna, String nama, String email, String password, String noTelepon) {
        super(idPengguna, nama, email, password, noTelepon, "admin");
    }

    public Admin(int idPengguna, String nama, String email, String password, String noTelepon, String jenisKelamin) {
        super(idPengguna, nama, email, password, noTelepon, "admin", jenisKelamin);
    }

    @Override
    public boolean bisaBooking() {
        return false;
    }

    @Override
    public String getRoleDisplayName() {
        return "Administrator";
    }
}
