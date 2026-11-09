package com.kozzy.models;

/**
 * Subclass Pencari: Mewarisi Pengguna dengan hak akses sewa kos.
 */
public class Pencari extends Pengguna {

    public Pencari() {
        super();
        this.peran = "pencari";
    }

    public Pencari(int idPengguna, String nama, String email, String password, String noTelepon) {
        super(idPengguna, nama, email, password, noTelepon, "pencari");
    }

    public Pencari(int idPengguna, String nama, String email, String password, String noTelepon, String jenisKelamin) {
        super(idPengguna, nama, email, password, noTelepon, "pencari", jenisKelamin);
    }

    @Override
    public boolean bisaBooking() {
        return true;
    }

    @Override
    public String getRoleDisplayName() {
        return "Pencari Kos";
    }
}
