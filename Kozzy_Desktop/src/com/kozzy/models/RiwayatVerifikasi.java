package com.kozzy.models;

import java.sql.Timestamp;

/**
 * Model Enkapsulasi untuk Audit Log Riwayat Verifikasi oleh Admin.
 */
public class RiwayatVerifikasi {
    private int idRiwayat;
    private String keputusan; // "approved", "rejected"
    private Timestamp tanggalKeputusan;
    private String catatan;
    private int idPengguna; // ID Admin
    private String idBooking;
    private String namaAdmin;

    public RiwayatVerifikasi() {}

    public RiwayatVerifikasi(int idRiwayat, String keputusan, Timestamp tanggalKeputusan,
                             String catatan, int idPengguna, String idBooking, String namaAdmin) {
        this.idRiwayat = idRiwayat;
        this.keputusan = keputusan;
        this.tanggalKeputusan = tanggalKeputusan;
        this.catatan = catatan;
        this.idPengguna = idPengguna;
        this.idBooking = idBooking;
        this.namaAdmin = namaAdmin;
    }

    public int getIdRiwayat() { return idRiwayat; }
    public void setIdRiwayat(int idRiwayat) { this.idRiwayat = idRiwayat; }

    public String getKeputusan() { return keputusan; }
    public void setKeputusan(String keputusan) { this.keputusan = keputusan; }

    public Timestamp getTanggalKeputusan() { return tanggalKeputusan; }
    public void setTanggalKeputusan(Timestamp tanggalKeputusan) { this.tanggalKeputusan = tanggalKeputusan; }

    public String getCatatan() { return catatan; }
    public void setCatatan(String catatan) { this.catatan = catatan; }

    public int getIdPengguna() { return idPengguna; }
    public void setIdPengguna(int idPengguna) { this.idPengguna = idPengguna; }

    public String getIdBooking() { return idBooking; }
    public void setIdBooking(String idBooking) { this.idBooking = idBooking; }

    public String getNamaAdmin() { return namaAdmin; }
    public void setNamaAdmin(String namaAdmin) { this.namaAdmin = namaAdmin; }
}
