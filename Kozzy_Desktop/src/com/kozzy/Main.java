package com.kozzy;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.config.UITheme;
import com.kozzy.controllers.BookingController;
import com.kozzy.views.DesktopMainWindow;

import javax.swing.*;

/**
 * Main: Titik awal eksekusi aplikasi Kozzy Desktop GUI.
 */
public class Main {
    public static void main(String[] args) {
        // Inisialisasi Look and Feel FlatLaf
        UITheme.setupFlatLaf();

        SwingUtilities.invokeLater(() -> {
            try {
                // Periksa masa sewa kamar yang telah berakhir melalui Controller
                new BookingController().cekMasaSewa();

                DesktopMainWindow window = new DesktopMainWindow();
                window.setVisible(true);

                // Pemberitahuan jika MySQL belum aktif saat startup
                if (!DatabaseConnection.checkConnection() && System.getProperty("kozzy.silent") == null) {
                    UITheme.showWarning(
                        window,
                        "Koneksi Database MySQL",
                        "Pemberitahuan: Database MySQL Belum Aktif\n\n" +
                        "Aplikasi tidak dapat terhubung ke database MySQL (localhost:3306/kozzy_db).\n" +
                        "Silakan buka XAMPP Control Panel dan klik 'Start' pada modul MySQL.\n\n" +
                        "Catatan: Aplikasi sementara berjalan dengan data cadangan offline."
                    );
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                UITheme.showError(null, "Error", "Galat memulai aplikasi: " + ex.getMessage());
            }
        });
    }
}
