package com.kozzy.views;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.config.UITheme;
import com.kozzy.controllers.AuthController;
import com.kozzy.models.Pengguna;
import com.kozzy.views.components.DesktopSidebar;
import com.kozzy.views.components.PillButton;
import com.kozzy.views.components.TransitionContainer;

import javax.swing.*;
import java.awt.*;

/**
 * DesktopMainWindow: Jendela Utama Aplikasi Desktop Workstation Kozzy.
 * - Sisi Kiri: DesktopSidebar (Navigasi Rail Instan).
 * - Sisi Atas: Banner Pemberitahuan jika MySQL belum aktif beserta tombol coba sambung ulang.
 * - Sisi Tengah: TransitionContainer Konten Desktop dengan Animasi Transisi Halus 60 FPS (Poin 9).
 * - Panel Admin dibuka terpisah sebagai jendela workstation mandiri (Poin 1).
 * - Setiap pendaftaran akun baru & login otomatis langsung diarahkan ke Beranda (Poin 8).
 */
public class DesktopMainWindow extends JFrame {

    private final DesktopSidebar sidebar;
    private final TransitionContainer contentCards;
    private final JPanel bannerOffline;
    private String currentPage = "BERANDA";

    private final AuthController authController = new AuthController();
    private final DesktopBerandaView berandaView;
    private final DesktopKamarView kamarView;
    private final DesktopSewakuView sewakuView;
    private final DesktopProfilView profilView;
    private final DesktopLoginView loginView;

    public DesktopMainWindow() {
        super("Kozzy Desktop — Sistem Manajemen & Pemesanan Kos Bulanan");

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1180, 740);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_CANVAS);
        setLayout(new BorderLayout());

        // Set application icon
        try {
            java.io.File iconFile = com.kozzy.views.components.ImageHelper.resolveFile("assets/logo kos.png");
            if (iconFile != null && iconFile.exists()) {
                setIconImage(javax.imageio.ImageIO.read(iconFile));
            }
        } catch (Exception ignored) {}

        // 1. Sidebar Kiri
        sidebar = new DesktopSidebar(cardKey -> showPage(cardKey));
        add(sidebar, BorderLayout.WEST);

        // 2. TransitionContainer Tengah (Transisi Animasi Halus Berkecepatan Tinggi)
        contentCards = new TransitionContainer();

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

            @Override
            public void onOpenAdminWindow() {
                showPage("ADMIN");
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

        // Poin 8: Setiap buat akun baru dan login langsung bawa ke Beranda
        loginView = new DesktopLoginView(() -> {
            sidebar.updateUserState();
            berandaView.refreshData();
            showPage("BERANDA");
        });

        contentCards.addPage("BERANDA", berandaView);
        contentCards.addPage("KAMAR", kamarView);
        contentCards.addPage("SEWAKU", sewakuView);
        contentCards.addPage("PROFIL", profilView);
        contentCards.addPage("LOGIN", loginView);

        // Wrapper Konten Kanan: Transition Container (Penyewa tidak menampilkan banner teknis basis data)
        JPanel rightContainer = new JPanel(new BorderLayout());
        rightContainer.setOpaque(false);

        bannerOffline = new JPanel();
        bannerOffline.setVisible(false);
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
                lblMsg.setText("Berhasil terhubung ke database MySQL (kozzy_db)!");
                lblMsg.setForeground(new Color(0x1F, 0x5B, 0x41));
                btnReconnect.setVisible(false);
                refreshCurrentPage();
                berandaView.refreshData();
                KozzyDialogHelper.showSuccess(this, "Koneksi Berhasil", "Berhasil terhubung ke database MySQL (kozzy_db)!");
                Timer t = new Timer(3500, ev -> banner.setVisible(false));
                t.setRepeats(false);
                t.start();
            } else {
                KozzyDialogHelper.showWarning(
                    this,
                    "MySQL Belum Aktif",
                    "Pastikan modul MySQL di XAMPP Control Panel sudah dalam status 'Running' (tombol Start sudah diklik).\n" +
                    (DatabaseConnection.getInstance().getLastErrorMessage().isEmpty() ? "" : "\nInfo: " + DatabaseConnection.getInstance().getLastErrorMessage())
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
        // Poin 1: Panel Admin dipisah sebagai jendela tersendiri
        if ("ADMIN".equalsIgnoreCase(cardKey)) {
            DesktopAdminWindow.open(() -> {
                berandaView.refreshData();
                kamarView.loadKamarList();
                sewakuView.refreshData();
            });
            return;
        }

        this.currentPage = cardKey;
        contentCards.showPage(cardKey);
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
            case "PROFIL":
                profilView.refreshData();
                break;
        }
    }
}
