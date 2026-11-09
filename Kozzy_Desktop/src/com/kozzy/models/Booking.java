package com.kozzy.models;

import java.sql.Date;
import java.sql.Timestamp;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;

/**
 * Model Enkapsulasi untuk Entitas Booking dengan relasi bisnis.
 */
public class Booking {
    private String idBooking;
    private Date tanggalMulai;
    private int durasi; // dalam bulan
    private String status; // "pending", "approved", "rejected"
    private String alasan;
    private int idPengguna;
    private String kodeKamar;
    private int idMetode;
    private Timestamp createdAt;

    // Relasi tambahan untuk kenyamanan View (Join DB)
    private String namaPengguna;
    private String emailPengguna;
    private String noTeleponPengguna;
    private String jenisKelaminPengguna;
    private String tipeKamar;
    private int hargaKamar;
    private String namaMetode;
    private String nomorTujuan;
    private String fileBukti;

    public Booking() {}

    public Booking(String idBooking, Date tanggalMulai, int durasi, String status, String alasan,
                   int idPengguna, String kodeKamar, int idMetode, Timestamp createdAt) {
        this.idBooking = idBooking;
        this.tanggalMulai = tanggalMulai;
        this.durasi = durasi;
        this.status = status;
        this.alasan = alasan;
        this.idPengguna = idPengguna;
        this.kodeKamar = kodeKamar;
        this.idMetode = idMetode;
        this.createdAt = createdAt;
    }

    public int hitungTotalBiaya() {
        return hargaKamar * durasi;
    }

    public String formatTotalBiaya() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
        return nf.format(hitungTotalBiaya()).replace(",00", "");
    }

    public Date hitungTanggalSelesai() {
        if (tanggalMulai == null) return null;
        LocalDate local = tanggalMulai.toLocalDate().plusMonths(durasi);
        return Date.valueOf(local);
    }

    // Getters dan Setters
    public String getIdBooking() { return idBooking; }
    public void setIdBooking(String idBooking) { this.idBooking = idBooking; }

    public Date getTanggalMulai() { return tanggalMulai; }
    public void setTanggalMulai(Date tanggalMulai) { this.tanggalMulai = tanggalMulai; }

    public int getDurasi() { return durasi; }
    public void setDurasi(int durasi) { this.durasi = durasi; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getAlasan() { return alasan; }
    public void setAlasan(String alasan) { this.alasan = alasan; }

    public int getIdPengguna() { return idPengguna; }
    public void setIdPengguna(int idPengguna) { this.idPengguna = idPengguna; }

    public String getKodeKamar() { return kodeKamar; }
    public void setKodeKamar(String kodeKamar) { this.kodeKamar = kodeKamar; }

    public int getIdMetode() { return idMetode; }
    public void setIdMetode(int idMetode) { this.idMetode = idMetode; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getNamaPengguna() { return namaPengguna; }
    public void setNamaPengguna(String namaPengguna) { this.namaPengguna = namaPengguna; }

    public String getEmailPengguna() { return emailPengguna; }
    public void setEmailPengguna(String emailPengguna) { this.emailPengguna = emailPengguna; }

    public String getNoTeleponPengguna() { return noTeleponPengguna; }
    public void setNoTeleponPengguna(String noTeleponPengguna) { this.noTeleponPengguna = noTeleponPengguna; }

    public String getJenisKelaminPengguna() { return jenisKelaminPengguna; }
    public void setJenisKelaminPengguna(String jenisKelaminPengguna) { this.jenisKelaminPengguna = jenisKelaminPengguna; }

    public String getTipeKamar() { return tipeKamar; }
    public void setTipeKamar(String tipeKamar) { this.tipeKamar = tipeKamar; }

    public int getHargaKamar() { return hargaKamar; }
    public void setHargaKamar(int hargaKamar) { this.hargaKamar = hargaKamar; }

    public String getNamaMetode() { return namaMetode; }
    public void setNamaMetode(String namaMetode) { this.namaMetode = namaMetode; }

    public String getNomorTujuan() { return nomorTujuan; }
    public void setNomorTujuan(String nomorTujuan) { this.nomorTujuan = nomorTujuan; }

    public String getFileBukti() { return fileBukti; }
    public void setFileBukti(String fileBukti) { this.fileBukti = fileBukti; }
}
