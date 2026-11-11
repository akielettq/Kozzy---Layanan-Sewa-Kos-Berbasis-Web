package com.kozzy.views;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.config.UITheme;
import com.kozzy.controllers.AuthController;
import com.kozzy.models.Pengguna;
import com.kozzy.views.components.DesktopSidebar;

import javax.swing.*;
import java.awt.*;

/**
 * DesktopMainWindow: Jendela Utama Aplikasi Desktop Workstation Kozzy.
 * - Sisi Kiri: DesktopSidebar (Navigasi Rail Instan).
 * - Sisi Atas: Banner Pemberitahuan jika MySQL belum aktif beserta tombol coba sambung ulang.
 * - Sisi Tengah: CardLayout Konten Desktop (100% Full Layar, Tanpa Scroll Vertikal Halaman).
 */
public class DesktopMainWindow extends JFrame {

    private final DesktopSidebar sidebar;
    private final CardLayout cardLayout;
    private final JPanel contentCards;
    private final JPanel bannerOffline;
    private String currentPage = "BERANDA";

    private final AuthController authController = new AuthController();
    private final DesktopBerandaView berandaView;
    private final DesktopKamarView kamarView;
    private final DesktopSewakuView sewakuView;
    private final DesktopAdminView adminView;
    private final DesktopProfilView profilView;
    private final DesktopLoginView loginView;

    public DesktopMainWindow() {
        super("Kozzy Desktop — Sistem Manajemen Kos Bulanan");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1500, 900);
        setMinimumSize(new Dimension(1250, 800));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_CANVAS);
        setLayout(new BorderLayout());

        // 1. Sidebar Kiri
        sidebar = new DesktopSidebar(cardKey -> showPage(cardKey));
        add(sidebar, BorderLayout.WEST);

        // 2. CardLayout Tengah
        cardLayout = new CardLayout();
        contentCards = new JPanel(cardLayout);
        contentCards.setOpaque(false);

        // Inisialisasi Seluruh Halaman Desktop
        berandaView = new DesktopBerandaView(new DesktopBerandaView.BerandaActionCallback() {
            @Override
            public void onSelectRoom(String kodeKamar) {
                showPage("KAMAR");
                kamarView.selectRoomByKode(kodeKamar);
            }

            @Override
            public void onNavigateToKamar() {
                showPage("KAMAR");
            }

            @Override
            public void onNavigateToLogin() {
                showPage("LOGIN");
            }
        });

        kamarView = new DesktopKamarView(new DesktopKamarView.KamarActionCallback() {
            @Override
            public void onBookingRequested(com.kozzy.models.Kamar kamar) {
                DesktopBookingDialog dlg = new DesktopBookingDialog(DesktopMainWindow.this, kamar, () -> {
                    sewakuView.refreshData();
                    berandaView.refreshData();
                    showPage("SEWAKU");
                });
                dlg.setVisible(true);
            }

            @Override
            public void onLoginRequested() {
                showPage("LOGIN");
            }
        });

        sewakuView = new DesktopSewakuView(new DesktopSewakuView.SewakuActionCallback() {
            @Override
            public void onExploreRooms() {
                showPage("KAMAR");
            }

            @Override
            public void onLoginRequested() {
                showPage("LOGIN");
            }
        });

        adminView = new DesktopAdminView();

        profilView = new DesktopProfilView(new DesktopProfilView.ProfilActionCallback() {
            @Override
            public void onLogout() {
                showPage("BERANDA");
            }

            @Override
            public void onNavigateToSewaku() {
                showPage("SEWAKU");
            }
        });

        loginView = new DesktopLoginView(() -> {
            sidebar.updateUserState();
            Pengguna user = authController.getCurrentUser();
            if (user != null && user.isAdmin()) {
                adminView.refreshData();
                showPage("ADMIN");
            } else {
                sewakuView.refreshData();
                showPage("SEWAKU");
            }
        });

        contentCards.add(berandaView, "BERANDA");
        contentCards.add(kamarView, "KAMAR");
        contentCards.add(sewakuView, "SEWAKU");
        contentCards.add(adminView, "ADMIN");
        contentCards.add(profilView, "PROFIL");
        contentCards.add(loginView, "LOGIN");

        // Wrapper Konten Kanan: Banner Notifikasi + CardLayout
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        bannerOffline = createDatabaseNotificationBanner();
        rightContainer.add(bannerOffline, BorderLayout.NORTH);
        rightContainer.add(contentCards, BorderLayout.CENTER);

        add(rightContainer, BorderLayout.CENTER);

        // Listen for session changes
        authController.addSessionListener(user -> {
            sidebar.updateUserState();
            berandaView.refreshData();
        });

        showPage("BERANDA");
    }

    private JPanel createDatabaseNotificationBanner() {
        JPanel banner = new JPanel(new BorderLayout(12, 0));
        banner.setBackground(new Color(0xFF, 0xFB, 0xEB));
        banner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xF5, 0xE0, 0x96)),
                BorderFactory.createEmptyBorder(8, 20, 8, 20)
        ));

        JLabel lblMsg = new JLabel("Pemberitahuan: Database MySQL belum aktif (localhost:3306/kozzy_db). Buka XAMPP lalu klik Start pada MySQL.");
        lblMsg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMsg.setForeground(new Color(0x92, 0x5D, 0x05));
        banner.add(lblMsg, BorderLayout.WEST);

        JPanel actionPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        actionPnl.setOpaque(false);

        JButton btnReconnect = new JButton("Coba Hubungkan Ulang");
        btnReconnect.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnReconnect.setForeground(UITheme.PRIMARY_GREEN);
        btnReconnect.setBackground(Color.WHITE);
        btnReconnect.setFocusPainted(false);
        btnReconnect.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnReconnect.addActionListener(e -> {
            boolean connected = DatabaseConnection.checkConnection(true);
            if (connected) {
                banner.setBackground(new Color(0xE8, 0xF5, 0xE9));
                banner.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(0xA5, 0xD6, 0xA7)),
                        BorderFactory.createEmptyBorder(8, 20, 8, 20)
                ));
                lblMsg.setText("✓ Berhasil terhubung ke database MySQL (kozzy_db)!");
                lblMsg.setForeground(new Color(0x1F, 0x5B, 0x41));
                btnReconnect.setVisible(false);
                refreshCurrentPage();
                berandaView.refreshData();
                JOptionPane.showMessageDialog(this, "Berhasil terhubung ke database MySQL (kozzy_db)!", "Koneksi Berhasil", JOptionPane.INFORMATION_MESSAGE);
                Timer t = new Timer(3500, ev -> banner.setVisible(false));
                t.setRepeats(false);
                t.start();
            } else {
                JOptionPane.showMessageDialog(
                    this,
                    "MySQL belum aktif!\n\n" +
                    "Pastikan modul MySQL di XAMPP Control Panel sudah dalam status 'Running' (tombol Start sudah diklik).\n" +
                    (DatabaseConnection.getInstance().getLastErrorMessage().isEmpty() ? "" : "\nInfo: " + DatabaseConnection.getInstance().getLastErrorMessage()),
                    "Pemberitahuan Database",
                    JOptionPane.WARNING_MESSAGE
                );
            }
        });
        actionPnl.add(btnReconnect);

        JButton btnClose = new JButton("x");
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnClose.setForeground(new Color(0x92, 0x5D, 0x05));
        btnClose.setContentAreaFilled(false);
        btnClose.setBorderPainted(false);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.addActionListener(e -> banner.setVisible(false));
        actionPnl.add(btnClose);

        banner.add(actionPnl, BorderLayout.EAST);

        // Tampilkan hanya jika MySQL belum aktif
        banner.setVisible(!DatabaseConnection.checkConnection());

        return banner;
    }

    public void showPage(String cardKey) {
        this.currentPage = cardKey;
        cardLayout.show(contentCards, cardKey);
        sidebar.setActiveKey(cardKey);
        refreshCurrentPage();
    }

    private void refreshCurrentPage() {
        switch (currentPage) {
            case "BERANDA":
                berandaView.refreshData();
                break;
            case "KAMAR":
                kamarView.loadKamarList();
                break;
            case "SEWAKU":
                sewakuView.refreshData();
                break;
            case "ADMIN":
                adminView.refreshData();
                break;
            case "PROFIL":
                profilView.refreshData();
                break;
        }
    }
}
