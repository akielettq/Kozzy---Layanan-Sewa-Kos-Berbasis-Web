package com.kozzy;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.config.UITheme;
import com.kozzy.controllers.BookingController;
import com.kozzy.views.DesktopMainWindow;
import com.kozzy.views.KozzyDialogHelper;

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


            } catch (Exception ex) {
                ex.printStackTrace();
                KozzyDialogHelper.showError(null, "Galat Memulai Aplikasi", ex.getMessage());
            }
        });
    }
}
