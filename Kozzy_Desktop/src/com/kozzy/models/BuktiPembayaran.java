package com.kozzy.models;

import java.sql.Timestamp;

/**
 * Model Enkapsulasi untuk Entitas Bukti Pembayaran.
 */
public class BuktiPembayaran {
    private int idBukti;
    private String fileBukti;
    private Timestamp tanggalUnggah;
    private String idBooking;

    public BuktiPembayaran() {}

    public BuktiPembayaran(int idBukti, String fileBukti, Timestamp tanggalUnggah, String idBooking) {
        this.idBukti = idBukti;
        this.fileBukti = fileBukti;
        this.tanggalUnggah = tanggalUnggah;
        this.idBooking = idBooking;
    }

    public int getIdBukti() { return idBukti; }
    public void setIdBukti(int idBukti) { this.idBukti = idBukti; }

    public String getFileBukti() { return fileBukti; }
    public void setFileBukti(String fileBukti) { this.fileBukti = fileBukti; }

    public Timestamp getTanggalUnggah() { return tanggalUnggah; }
    public void setTanggalUnggah(Timestamp tanggalUnggah) { this.tanggalUnggah = tanggalUnggah; }

    public String getIdBooking() { return idBooking; }
    public void setIdBooking(String idBooking) { this.idBooking = idBooking; }
}
