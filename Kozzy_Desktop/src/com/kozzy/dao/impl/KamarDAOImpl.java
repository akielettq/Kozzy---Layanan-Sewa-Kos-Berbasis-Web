package com.kozzy.dao.impl;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.interfaces.IKamarDAO;
import com.kozzy.models.Kamar;

import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * KamarDAOImpl: Mengelola query data Kamar Kos dan Fasilitas dari database MySQL (kozzy_db).
 */
public class KamarDAOImpl implements IKamarDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final OfflineFallback fallback = OfflineFallback.getInstance();

    @Override
    public List<Kamar> getAllKamar(String filterTipe) {
        if (db.isConnected()) {
            List<Kamar> list = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
                "SELECT k.*, GROUP_CONCAT(f.nama_fasilitas SEPARATOR ', ') AS daftar_fasilitas " +
                "FROM kamar k " +
                "LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar " +
                "LEFT JOIN fasilitas f ON fk.fasilitas_id_fasilitas = f.id_fasilitas "
            );

            boolean hasFilter = (filterTipe != null && !filterTipe.isEmpty() && !filterTipe.equalsIgnoreCase("semua"));
            if (hasFilter) {
                sql.append("WHERE LOWER(k.tipe) = LOWER(?) ");
            }
            sql.append("GROUP BY k.kode_kamar ORDER BY " +
                       "CASE WHEN LOWER(k.status) = 'tersedia' THEN 1 ELSE 2 END ASC, k.kode_kamar ASC");

            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                if (hasFilter) {
                    ps.setString(1, filterTipe);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToKamar(rs));
                    }
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[KamarDAO] Galat SQL getAllKamar: " + e.getMessage());
            }
        }
        return fallback.getAllRooms(filterTipe);
    }

    @Override
    public Kamar getByKode(String kodeKamar) {
        if (db.isConnected()) {
            String sql = "SELECT k.*, GROUP_CONCAT(f.nama_fasilitas SEPARATOR ', ') AS daftar_fasilitas " +
                         "FROM kamar k " +
                         "LEFT JOIN fasilitas_kamar fk ON k.kode_kamar = fk.kamar_kode_kamar " +
                         "LEFT JOIN fasilitas f ON fk.fasilitas_id_fasilitas = f.id_fasilitas " +
                         "WHERE k.kode_kamar = ? " +
                         "GROUP BY k.kode_kamar";

            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kodeKamar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToKamar(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[KamarDAO] Galat SQL getByKode: " + e.getMessage());
            }
        }
        return fallback.getRoomByKode(kodeKamar);
    }

    @Override
    public boolean updateStatus(String kodeKamar, String newStatus) {
        return updateStatus(kodeKamar, newStatus, null);
    }

    @Override
    public boolean updateStatus(String kodeKamar, String newStatus, Date terisiHingga) {
        String dbStatus = newStatus.toLowerCase();
        if ("perbaikan".equals(dbStatus)) {
            dbStatus = "pemeliharaan";
        }

        if (db.isConnected()) {
            String sql = "UPDATE kamar SET status = ?, terisi_hingga = ? WHERE kode_kamar = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, dbStatus);
                ps.setDate(2, terisiHingga);
                ps.setString(3, kodeKamar);
                int rows = ps.executeUpdate();
                fallback.updateRoomStatus(kodeKamar, dbStatus);
                return rows > 0;
            } catch (SQLException e) {
                System.err.println("[KamarDAO] Galat SQL updateStatus: " + e.getMessage());
            }
        }
        return fallback.updateRoomStatus(kodeKamar, dbStatus);
    }

    @Override
    public void updateExpiredRooms() {
        if (db.isConnected()) {
            String sql = "UPDATE kamar SET status = 'tersedia', terisi_hingga = NULL " +
                         "WHERE status = 'terisi' AND terisi_hingga < CURDATE()";
            try (Connection conn = db.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException e) {
                System.err.println("[KamarDAO] Galat SQL updateExpiredRooms: " + e.getMessage());
            }
        }
    }

    private Kamar mapResultSetToKamar(ResultSet rs) throws SQLException {
        String kode = rs.getString("kode_kamar");
        String tipe = rs.getString("tipe");
        int harga = rs.getInt("harga");
        String status = rs.getString("status");
        Date terisiHingga = rs.getDate("terisi_hingga");
        String deskripsi = rs.getString("deskripsi");

        Kamar k = new Kamar(kode, tipe, harga, status, terisiHingga, deskripsi);

        String fasText = rs.getString("daftar_fasilitas");
        if (fasText != null && !fasText.trim().isEmpty()) {
            String[] arr = fasText.split(",\\s*");
            k.setFasilitas(Arrays.asList(arr));
        }
        return k;
    }
}
