package com.kozzy.models;

/**
 * Model Enkapsulasi untuk Entitas Fasilitas.
 */
public class Fasilitas {
    private int idFasilitas;
    private String namaFasilitas;

    public Fasilitas() {}

    public Fasilitas(int idFasilitas, String namaFasilitas) {
        this.idFasilitas = idFasilitas;
        this.namaFasilitas = namaFasilitas;
    }

    public int getIdFasilitas() { return idFasilitas; }
    public void setIdFasilitas(int idFasilitas) { this.idFasilitas = idFasilitas; }

    public String getNamaFasilitas() { return namaFasilitas; }
    public void setNamaFasilitas(String namaFasilitas) { this.namaFasilitas = namaFasilitas; }

    @Override
    public String toString() {
        return namaFasilitas;
    }
}
