package com.kozzy.dao.impl;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.interfaces.IMetodeDAO;
import com.kozzy.models.MetodePembayaran;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * MetodeDAOImpl: Mengelola data opsi Metode Pembayaran dari database MySQL (kozzy_db).
 */
public class MetodeDAOImpl implements IMetodeDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final OfflineFallback fallback = OfflineFallback.getInstance();

    @Override
    public List<MetodePembayaran> getAllMetode() {
        if (db.isConnected()) {
            List<MetodePembayaran> list = new ArrayList<>();
            String sql = "SELECT * FROM metode_pembayaran ORDER BY id_metode ASC";
            try (Connection conn = db.getConnection();
                 Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    list.add(new MetodePembayaran(
                            rs.getInt("id_metode"),
                            rs.getString("nama_metode"),
                            rs.getString("nomor_tujuan")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[MetodeDAO] Galat SQL getAllMetode: " + e.getMessage());
            }
        }
        return fallback.getAllMethods();
    }
}
