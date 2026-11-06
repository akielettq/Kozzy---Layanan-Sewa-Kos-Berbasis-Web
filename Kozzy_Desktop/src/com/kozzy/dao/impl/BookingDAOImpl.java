package com.kozzy.dao.impl;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.interfaces.IBookingDAO;
import com.kozzy.models.Booking;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingDAOImpl: Mengelola transaksi booking, bukti pembayaran, dan verifikasi admin di MySQL (kozzy_db).
 * Menggunakan Database Transaction JDBC (commit/rollback) demi menjaga konsistensi data relasional.
 */
public class BookingDAOImpl implements IBookingDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final OfflineFallback fallback = OfflineFallback.getInstance();

    @Override
    public boolean createBooking(Booking booking, String proofFilePath) {
        if (db.isConnected()) {
            String sqlBooking = "INSERT INTO booking (id_booking, tanggal_mulai, durasi, status, id_pengguna, kode_kamar, id_metode, created_at) " +
                                "VALUES (?, ?, ?, 'pending', ?, ?, ?, NOW())";
            String sqlBukti = "INSERT INTO bukti_pembayaran (file_bukti, tanggal_unggah, id_booking) VALUES (?, NOW(), ?)";

            Connection conn = null;
            try {
                conn = db.getConnection();
                conn.setAutoCommit(false); // Mulai Transaksi

                // 1. Simpan salinan file bukti pembayaran jika ada
                String storedFileName = "bukti_" + System.currentTimeMillis() + ".jpg";
                if (proofFilePath != null && !proofFilePath.isEmpty()) {
                    File src = new File(proofFilePath);
                    if (src.exists()) {
                        File destDir = new File("assets/uploads/bukti");
                        if (!destDir.exists()) destDir.mkdirs();
                        File dest = new File(destDir, storedFileName);
                        Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // 2. Insert record booking
                try (PreparedStatement psB = conn.prepareStatement(sqlBooking)) {
                    psB.setString(1, booking.getIdBooking());
                    psB.setDate(2, booking.getTanggalMulai());
                    psB.setInt(3, booking.getDurasi());
                    psB.setInt(4, booking.getIdPengguna());
                    psB.setString(5, booking.getKodeKamar());
                    psB.setInt(6, booking.getIdMetode());
                    psB.executeUpdate();
                }

                // 3. Insert record bukti_pembayaran
                try (PreparedStatement psP = conn.prepareStatement(sqlBukti)) {
                    psP.setString(1, storedFileName);
                    psP.setString(2, booking.getIdBooking());
                    psP.executeUpdate();
                }

                conn.commit(); // Transaksi berhasil disimpan ke MySQL
                fallback.addBooking(booking, proofFilePath);
                return true;
            } catch (Exception e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                }
                System.err.println("[BookingDAO] Galat SQL createBooking: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
        }
        return fallback.addBooking(booking, proofFilePath);
    }

    @Override
    public List<Booking> getByPengguna(int idPengguna) {
        if (db.isConnected()) {
            List<Booking> list = new ArrayList<>();
            String sql = "SELECT b.*, k.tipe AS tipe_kamar, k.harga AS harga_kamar, m.nama_metode, bp.file_bukti " +
                         "FROM booking b " +
                         "JOIN kamar k ON b.kode_kamar = k.kode_kamar " +
                         "JOIN metode_pembayaran m ON b.id_metode = m.id_metode " +
                         "LEFT JOIN bukti_pembayaran bp ON b.id_booking = bp.id_booking " +
                         "WHERE b.id_pengguna = ? " +
                         "ORDER BY b.created_at DESC";

            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idPengguna);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        list.add(mapResultSetToBooking(rs));
                    }
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[BookingDAO] Galat SQL getByPengguna: " + e.getMessage());
            }
        }
        return fallback.getBookingsByPengguna(idPengguna);
    }

    @Override
    public List<Booking> getAllBookings(String statusFilter) {
        if (db.isConnected()) {
            List<Booking> list = new ArrayList<>();
            StringBuilder sql = new StringBuilder(
                "SELECT b.*, p.nama AS nama_penyewa, k.tipe AS tipe_kamar, k.harga AS harga_kamar, m.nama_metode, bp.file_bukti " +
                "FROM booking b " +
                "JOIN pengguna p ON b.id_pengguna = p.id_pengguna " +
                "JOIN kamar k ON b.kode_kamar = k.kode_kamar " +
                "JOIN metode_pembayaran m ON b.id_metode = m.id_metode " +
                "LEFT JOIN bukti_pembayaran bp ON b.id_booking = bp.id_booking "
            );

            boolean hasFilter = (statusFilter != null && !statusFilter.isEmpty() && !statusFilter.equalsIgnoreCase("semua"));
            if (hasFilter) {
                sql.append("WHERE LOWER(b.status) = LOWER(?) ");
            }
            sql.append("ORDER BY b.created_at DESC");

            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql.toString())) {
                if (hasFilter) {
                    ps.setString(1, statusFilter);
                }
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Booking b = mapResultSetToBooking(rs);
                        b.setNamaPengguna(rs.getString("nama_penyewa"));
                        list.add(b);
                    }
                }
                if (!list.isEmpty()) {
                    return list;
                }
            } catch (SQLException e) {
                System.err.println("[BookingDAO] Galat SQL getAllBookings: " + e.getMessage());
            }
        }
        return fallback.getAllBookings(statusFilter);
    }

    @Override
    public boolean approveBooking(String idBooking, int idAdmin) {
        return approveBooking(idBooking, "Pembayaran sah terverifikasi. Kunci fisik dapat diambil di pengelola.", idAdmin);
    }

    @Override
    public boolean approveBooking(String idBooking, String catatan, int adminId) {
        if (db.isConnected()) {
            Booking b = getById(idBooking);
            if (b == null) return false;

            String sqlBooking = "UPDATE booking SET status = 'approved', alasan = NULL WHERE id_booking = ?";
            String sqlKamar = "UPDATE kamar SET status = 'terisi', terisi_hingga = DATE_ADD(?, INTERVAL ? MONTH) WHERE kode_kamar = ?";
            String sqlVerif = "INSERT INTO riwayat_verifikasi (keputusan, tanggal_keputusan, catatan, id_pengguna, id_booking) " +
                              "VALUES ('approved', NOW(), ?, ?, ?)";

            Connection conn = null;
            try {
                conn = db.getConnection();
                conn.setAutoCommit(false); // Transaksi JDBC

                try (PreparedStatement ps1 = conn.prepareStatement(sqlBooking)) {
                    ps1.setString(1, idBooking);
                    ps1.executeUpdate();
                }

                try (PreparedStatement ps2 = conn.prepareStatement(sqlKamar)) {
                    ps2.setDate(1, b.getTanggalMulai());
                    ps2.setInt(2, b.getDurasi());
                    ps2.setString(3, b.getKodeKamar());
                    ps2.executeUpdate();
                }

                try (PreparedStatement ps3 = conn.prepareStatement(sqlVerif)) {
                    ps3.setString(1, (catatan != null && !catatan.isEmpty()) ? catatan : "Pembayaran sah terverifikasi. Kunci dapat diambil di pengelola.");
                    ps3.setInt(2, adminId);
                    ps3.setString(3, idBooking);
                    ps3.executeUpdate();
                }

                conn.commit();
                fallback.approveBooking(idBooking, catatan, adminId);
                return true;
            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                }
                System.err.println("[BookingDAO] Galat SQL approveBooking: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
        }
        return fallback.approveBooking(idBooking, catatan, adminId);
    }

    @Override
    public boolean rejectBooking(String idBooking, String alasan, int adminId) {
        if (db.isConnected()) {
            String sqlBooking = "UPDATE booking SET status = 'rejected', alasan = ? WHERE id_booking = ?";
            String sqlVerif = "INSERT INTO riwayat_verifikasi (keputusan, tanggal_keputusan, catatan, id_pengguna, id_booking) " +
                              "VALUES ('rejected', NOW(), ?, ?, ?)";

            Connection conn = null;
            try {
                conn = db.getConnection();
                conn.setAutoCommit(false); // Transaksi JDBC

                try (PreparedStatement ps1 = conn.prepareStatement(sqlBooking)) {
                    ps1.setString(1, alasan);
                    ps1.setString(2, idBooking);
                    ps1.executeUpdate();
                }

                try (PreparedStatement ps2 = conn.prepareStatement(sqlVerif)) {
                    ps2.setString(1, alasan);
                    ps2.setInt(2, adminId);
                    ps2.setString(3, idBooking);
                    ps2.executeUpdate();
                }

                conn.commit();
                fallback.rejectBooking(idBooking, alasan, adminId);
                return true;
            } catch (SQLException e) {
                if (conn != null) {
                    try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
                }
                System.err.println("[BookingDAO] Galat SQL rejectBooking: " + e.getMessage());
            } finally {
                if (conn != null) {
                    try { conn.setAutoCommit(true); } catch (SQLException ex) { ex.printStackTrace(); }
                }
            }
        }
        return fallback.rejectBooking(idBooking, alasan, adminId);
    }

    @Override
    public boolean hasActiveBooking(int idPengguna) {
        if (db.isConnected()) {
            String sql = "SELECT COUNT(*) FROM booking WHERE id_pengguna = ? AND (status = 'approved' OR status = 'pending')";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idPengguna);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                System.err.println("[BookingDAO] Galat SQL hasActiveBooking: " + e.getMessage());
            }
        }
        return fallback.hasActiveBooking(idPengguna);
    }

    @Override
    public boolean hasPendingBookingForRoom(String kodeKamar) {
        if (db.isConnected()) {
            String sql = "SELECT COUNT(*) FROM booking WHERE kode_kamar = ? AND status = 'pending'";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, kodeKamar);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            } catch (SQLException e) {
                System.err.println("[BookingDAO] Galat SQL hasPendingBookingForRoom: " + e.getMessage());
            }
        }
        return fallback.hasPendingBookingForRoom(kodeKamar);
    }

    @Override
    public Booking getById(String idBooking) {
        if (db.isConnected()) {
            String sql = "SELECT b.*, k.tipe AS tipe_kamar, k.harga AS harga_kamar, m.nama_metode, bp.file_bukti " +
                         "FROM booking b " +
                         "JOIN kamar k ON b.kode_kamar = k.kode_kamar " +
                         "JOIN metode_pembayaran m ON b.id_metode = m.id_metode " +
                         "LEFT JOIN bukti_pembayaran bp ON b.id_booking = bp.id_booking " +
                         "WHERE b.id_booking = ?";

            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, idBooking);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToBooking(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[BookingDAO] Galat SQL getById: " + e.getMessage());
            }
        }
        return fallback.getById(idBooking);
    }

    private Booking mapResultSetToBooking(ResultSet rs) throws SQLException {
        Booking b = new Booking(
                rs.getString("id_booking"),
                rs.getDate("tanggal_mulai"),
                rs.getInt("durasi"),
                rs.getString("status"),
                rs.getString("alasan"),
                rs.getInt("id_pengguna"),
                rs.getString("kode_kamar"),
                rs.getInt("id_metode"),
                rs.getTimestamp("created_at")
        );
        b.setTipeKamar(rs.getString("tipe_kamar"));
        b.setHargaKamar(rs.getInt("harga_kamar"));
        b.setNamaMetode(rs.getString("nama_metode"));
        b.setFileBukti(rs.getString("file_bukti"));
        return b;
    }
}
