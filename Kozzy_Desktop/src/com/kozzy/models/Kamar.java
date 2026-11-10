package com.kozzy.models;

import java.sql.Date;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Model Enkapsulasi untuk Entitas Kamar.
 */
public class Kamar {
    private String kodeKamar;
    private String tipe; // "putra" atau "putri"
    private int harga;
    private String status; // "tersedia", "terisi", "pemeliharaan"
    private Date terisiHingga;
    private String deskripsi;
    private List<String> fasilitas;

    public Kamar() {
        this.fasilitas = new ArrayList<>();
    }

    public Kamar(String kodeKamar, String tipe, int harga, String status, Date terisiHingga, String deskripsi) {
        this.kodeKamar = kodeKamar;
        this.tipe = tipe;
        this.harga = harga;
        this.status = status;
        this.terisiHingga = terisiHingga;
        this.deskripsi = deskripsi;
        this.fasilitas = new ArrayList<>();
    }

    public boolean isTersedia() {
        return "tersedia".equalsIgnoreCase(this.status);
    }

    public String formatRupiah() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        String formatted = nf.format(harga);
        return formatted.replace(",00", "") + " / bulan";
    }

    public String getFasilitasText() {
        if (fasilitas == null || fasilitas.isEmpty()) {
            return "Fasilitas standar";
        }
        return String.join(" • ", fasilitas);
    }

    // Getters and Setters
    public String getKodeKamar() { return kodeKamar; }
    public void setKodeKamar(String kodeKamar) { this.kodeKamar = kodeKamar; }

    public String getTipe() { return tipe; }
    public void setTipe(String tipe) { this.tipe = tipe; }

    public int getHarga() { return harga; }
    public void setHarga(int harga) { this.harga = harga; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getTerisiHingga() { return terisiHingga; }
    public void setTerisiHingga(Date terisiHingga) { this.terisiHingga = terisiHingga; }

    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }

    public List<String> getFasilitas() { return fasilitas; }
    public void setFasilitas(List<String> fasilitas) { this.fasilitas = fasilitas; }

    @Override
    public String toString() {
        return "Kamar " + kodeKamar + " (" + tipe.toUpperCase() + ") - " + formatRupiah();
    }
}
