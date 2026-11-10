package com.kozzy.models;

/**
 * Model Enkapsulasi untuk Entitas Metode Pembayaran.
 */
public class MetodePembayaran {
    private int idMetode;
    private String namaMetode;
    private String nomorTujuan;

    public MetodePembayaran() {}

    public MetodePembayaran(int idMetode, String namaMetode, String nomorTujuan) {
        this.idMetode = idMetode;
        this.namaMetode = namaMetode;
        this.nomorTujuan = nomorTujuan;
    }

    public int getIdMetode() { return idMetode; }
    public void setIdMetode(int idMetode) { this.idMetode = idMetode; }

    public String getNamaMetode() { return namaMetode; }
    public void setNamaMetode(String namaMetode) { this.namaMetode = namaMetode; }

    public String getNomorTujuan() { return nomorTujuan; }
    public void setNomorTujuan(String nomorTujuan) { this.nomorTujuan = nomorTujuan; }

    @Override
    public String toString() {
        return namaMetode + " (" + nomorTujuan + ")";
    }
}
