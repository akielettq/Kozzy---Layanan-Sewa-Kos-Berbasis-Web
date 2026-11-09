package com.kozzy.models;

/**
 * Superclass abstrak Pengguna merepresentasikan konsep Enkapsulasi,
 * Pewarisan (Inheritance), dan Polimorfisme pada PBO.
 */
public abstract class Pengguna {
    protected int idPengguna;
    protected String nama;
    protected String email;
    protected String password;
    protected String noTelepon;
    protected String peran; // "admin" atau "pencari"
    protected String jenisKelamin; // "Laki-laki" atau "Perempuan"

    public Pengguna() {}

    public Pengguna(int idPengguna, String nama, String email, String password, String noTelepon, String peran) {
        this(idPengguna, nama, email, password, noTelepon, peran, "Laki-laki");
    }

    public Pengguna(int idPengguna, String nama, String email, String password, String noTelepon, String peran, String jenisKelamin) {
        this.idPengguna = idPengguna;
        this.nama = nama;
        this.email = email;
        this.password = password;
        this.noTelepon = noTelepon;
        this.peran = peran;
        this.jenisKelamin = (jenisKelamin == null || jenisKelamin.trim().isEmpty()) ? "Laki-laki" : jenisKelamin;
    }

    // Metode abstrak untuk polimorfisme perilaku antar peran
    public abstract boolean bisaBooking();
    public abstract String getRoleDisplayName();

    // Getters dan Setters (Enkapsulasi)
    public int getIdPengguna() { return idPengguna; }
    public void setIdPengguna(int idPengguna) { this.idPengguna = idPengguna; }

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getNoTelepon() { return noTelepon; }
    public void setNoTelepon(String noTelepon) { this.noTelepon = noTelepon; }

    public String getPeran() { return peran; }
    public void setPeran(String peran) { this.peran = peran; }

    public String getJenisKelamin() {
        return (jenisKelamin != null && !jenisKelamin.trim().isEmpty()) ? jenisKelamin : "Laki-laki";
    }

    public void setJenisKelamin(String jenisKelamin) {
        this.jenisKelamin = jenisKelamin;
    }

    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(this.peran);
    }
}
