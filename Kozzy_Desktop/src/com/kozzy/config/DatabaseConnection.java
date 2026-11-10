package com.kozzy.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * DatabaseConnection: Kelas Singleton JDBC untuk koneksi ke database MySQL (kozzy_db).
 * Didesain simpel, jelas, dan mudah dipahami untuk mahasiswa dalam tugas/ujian PBO.
 */
public class DatabaseConnection {
    private static DatabaseConnection instance;

    // Parameter koneksi MySQL (Port standar XAMPP 3306, user: root, password: "")
    public static final String DB_NAME = "kozzy_db";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Jakarta&connectTimeout=1200&socketTimeout=2000";
    private static final String USER = "root";
    private static final String PASS = "";

    private Boolean connectedCache = null;
    private long lastCheckTime = 0;
    private String lastErrorMessage = "";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseConnection] Driver MySQL JDBC tidak ditemukan: " + e.getMessage());
        }
    }

    private DatabaseConnection() {}

    /**
     * Singleton Pattern: Mengambil objek tunggal DatabaseConnection
     */
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    /**
     * Membuka koneksi baru ke database MySQL
     */
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    /**
     * static helper agar kode pemanggil lebih ringkas jika diinginkan
     */
    public static Connection getStaticConnection() throws SQLException {
        return getInstance().getConnection();
    }

    /**
     * Memeriksa status koneksi ke MySQL.
     * Menggunakan cache 5 detik agar respons UI tetap instan meski MySQL belum dinyalakan di XAMPP.
     */
    public boolean isConnected() {
        return isConnected(false);
    }

    /**
     * Memeriksa status koneksi ke MySQL dengan opsi paksa periksa ulang tanpa cache.
     */
    public boolean isConnected(boolean forceCheck) {
        long now = System.currentTimeMillis();
        if (!forceCheck && connectedCache != null && (now - lastCheckTime) < 5000) {
            return connectedCache;
        }

        try (Connection conn = getConnection()) {
            connectedCache = (conn != null && !conn.isClosed());
            if (connectedCache) {
                lastErrorMessage = "";
                System.out.println("[Database] Status: Terhubung ke MySQL (" + DB_NAME + ")");
            }
        } catch (Exception e) {
            connectedCache = false;
            lastErrorMessage = e.getMessage();
        }

        lastCheckTime = now;
        return connectedCache;
    }

    public static boolean checkConnection() {
        return getInstance().isConnected(false);
    }

    public static boolean checkConnection(boolean force) {
        return getInstance().isConnected(force);
    }

    public String getLastErrorMessage() {
        return lastErrorMessage != null ? lastErrorMessage : "";
    }
}
