package com.kozzy.dao.impl;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.interfaces.IVerifikasiDAO;
import com.kozzy.models.RiwayatVerifikasi;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * VerifikasiDAOImpl: Mengambil data log audit verifikasi persetujuan/penolakan sewa dari MySQL (kozzy_db).
 */
public class VerifikasiDAOImpl implements IVerifikasiDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final OfflineFallback fallback = OfflineFallback.getInstance();

    @Override
    public List<RiwayatVerifikasi> getAllRiwayat() {
        if (db.isConnected()) {
            List<RiwayatVerifikasi> list = new ArrayList<>();
            String sql = "SELECT rv.*, p.nama AS nama_admin " +
                         "FROM riwayat_verifikasi rv " +
                         "JOIN pengguna p ON rv.id_pengguna = p.id_pengguna " +
                         "ORDER BY rv.tanggal_keputusan DESC";

            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new RiwayatVerifikasi(
                        rs.getInt("id_riwayat"),
                        rs.getString("keputusan"),
                        rs.getTimestamp("tanggal_keputusan"),
                        rs.getString("catatan"),
                        rs.getInt("id_pengguna"),
                        rs.getString("id_booking"),
                        rs.getString("nama_admin")
                    ));
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[VerifikasiDAO] Galat SQL getAllRiwayat: " + e.getMessage());
            }
        }
        return fallback.getAllHistory();
    }
}
