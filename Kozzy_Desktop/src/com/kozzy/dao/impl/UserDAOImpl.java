package com.kozzy.dao.impl;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.dao.interfaces.IUserDAO;
import com.kozzy.models.Admin;
import com.kozzy.models.Pencari;
import com.kozzy.models.Pengguna;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * UserDAOImpl: Mengelola operasi data akun Pengguna (Admin & Pencari) ke MySQL kozzy_db.
 * Dilengkapi prinsip Polimorfisme OOP dan fallback offline jika MySQL XAMPP belum aktif.
 */
public class UserDAOImpl implements IUserDAO {
    private final DatabaseConnection db = DatabaseConnection.getInstance();
    private final OfflineFallback fallback = OfflineFallback.getInstance();

    public UserDAOImpl() {
        initSchemaIfNeeded();
    }

    private void initSchemaIfNeeded() {
        if (db.isConnected()) {
            try (Connection conn = db.getConnection();
                 Statement st = conn.createStatement()) {
                try {
                    st.executeUpdate("ALTER TABLE pengguna ADD COLUMN jenis_kelamin VARCHAR(20) NOT NULL DEFAULT 'Laki-laki'");
                } catch (SQLException ignored) {}
                st.executeUpdate("UPDATE pengguna SET jenis_kelamin = 'Perempuan' WHERE LOWER(email) IN ('farah@kozzy.id', 'fildza@kozzy.id', 'rania@kozzy.id', 'najwa@kozzy.id', 'naurah@kozzy.id') AND jenis_kelamin = 'Laki-laki'");
            } catch (SQLException ignored) {}
        }
    }

    private Pengguna mapResultSetToUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id_pengguna");
        String nama = rs.getString("nama");
        String mail = rs.getString("email");
        String pass = rs.getString("password");
        String telp = rs.getString("no_telepon");
        String peran = rs.getString("peran");
        String jenisKelamin = "Laki-laki";
        try {
            String jk = rs.getString("jenis_kelamin");
            if (jk != null && !jk.trim().isEmpty()) {
                jenisKelamin = jk;
            }
        } catch (SQLException ignored) {}

        // Polimorfisme OOP: Instansiasi objek sesuai peran
        if ("admin".equalsIgnoreCase(peran)) {
            return new Admin(id, nama, mail, pass, telp, jenisKelamin);
        } else {
            return new Pencari(id, nama, mail, pass, telp, jenisKelamin);
        }
    }

    @Override
    public Pengguna findByEmail(String email) {
        if (db.isConnected()) {
            String sql = "SELECT * FROM pengguna WHERE LOWER(email) = LOWER(?)";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] Galat SQL findByEmail: " + e.getMessage());
            }
        }
        return fallback.findUserByEmail(email);
    }

    @Override
    public Pengguna findById(int idPengguna) {
        if (db.isConnected()) {
            String sql = "SELECT * FROM pengguna WHERE id_pengguna = ?";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, idPengguna);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return mapResultSetToUser(rs);
                    }
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] Galat SQL findById: " + e.getMessage());
            }
        }
        return fallback.findUserById(idPengguna);
    }

    @Override
    public boolean emailExists(String email) {
        return findByEmail(email) != null;
    }

    @Override
    public boolean register(Pengguna user) {
        if (db.isConnected()) {
            String sql = "INSERT INTO pengguna (nama, email, password, no_telepon, peran, jenis_kelamin) VALUES (?, ?, ?, ?, ?, ?)";
            try (Connection conn = db.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, user.getNama());
                ps.setString(2, user.getEmail().toLowerCase());
                ps.setString(3, user.getPassword());
                ps.setString(4, user.getNoTelepon());
                ps.setString(5, user.getPeran());
                ps.setString(6, user.getJenisKelamin());

                int affected = ps.executeUpdate();
                if (affected > 0) {
                    try (ResultSet rs = ps.getGeneratedKeys()) {
                        if (rs.next()) {
                            user.setIdPengguna(rs.getInt(1));
                        }
                    }
                    fallback.addUser(user);
                    return true;
                }
            } catch (SQLException e) {
                System.err.println("[UserDAO] Mencoba simpan dengan skema standar (fallback): " + e.getMessage());
                String fallbackSql = "INSERT INTO pengguna (nama, email, password, no_telepon, peran) VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = db.getConnection();
                     PreparedStatement ps = conn.prepareStatement(fallbackSql, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setString(1, user.getNama());
                    ps.setString(2, user.getEmail().toLowerCase());
                    ps.setString(3, user.getPassword());
                    ps.setString(4, user.getNoTelepon());
                    ps.setString(5, user.getPeran());
                    int affected = ps.executeUpdate();
                    if (affected > 0) {
                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (rs.next()) {
                                user.setIdPengguna(rs.getInt(1));
                            }
                        }
                        fallback.addUser(user);
                        return true;
                    }
                } catch (SQLException ex) {
                    System.err.println("[UserDAO] Galat SQL register: " + ex.getMessage());
                }
            }
        }
        return fallback.addUser(user);
    }
}
