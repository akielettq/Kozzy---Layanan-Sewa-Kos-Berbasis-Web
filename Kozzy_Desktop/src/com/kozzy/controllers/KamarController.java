package com.kozzy.controllers;

import com.kozzy.dao.impl.KamarDAOImpl;
import com.kozzy.dao.interfaces.IKamarDAO;
import com.kozzy.models.Kamar;

import java.util.ArrayList;
import java.util.List;

/**
 * KamarController: Menangani logika proses dan manipulasi data kamar kos.
 * Memisahkan pemrosesan filter, pencarian, dan pengambilan data kamar dari class tampilan (View).
 */
public class KamarController {

    private final IKamarDAO kamarDAO;

    public KamarController() {
        this.kamarDAO = new KamarDAOImpl();
    }

    public KamarController(IKamarDAO kamarDAO) {
        this.kamarDAO = kamarDAO;
    }

    /**
     * Mengambil daftar kamar berdasarkan filter kategori.
     * Mendukung filter: "semua", "putra", "putri", dan "tersedia".
     */
    public List<Kamar> getKamarList(String filter) {
        if (filter == null || filter.trim().isEmpty() || "semua".equalsIgnoreCase(filter)) {
            return kamarDAO.getAllKamar("semua");
        }

        if ("tersedia".equalsIgnoreCase(filter)) {
            List<Kamar> all = kamarDAO.getAllKamar("semua");
            List<Kamar> filtered = new ArrayList<>();
            for (Kamar k : all) {
                if ("tersedia".equalsIgnoreCase(k.getStatus())) {
                    filtered.add(k);
                }
            }
            return filtered;
        }

        // Filter berdasarkan tipe: putra / putri
        return kamarDAO.getAllKamar(filter.toLowerCase().trim());
    }

    /**
     * Mencari kamar spesifik berdasarkan kode kamar (misal: "A-01", "B-03").
     */
    public Kamar getKamarByKode(String kodeKamar) {
        if (kodeKamar == null || kodeKamar.trim().isEmpty()) {
            return null;
        }
        return kamarDAO.getByKode(kodeKamar.trim());
    }
}
