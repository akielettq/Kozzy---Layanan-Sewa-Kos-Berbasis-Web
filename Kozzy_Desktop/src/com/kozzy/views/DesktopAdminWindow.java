package com.kozzy.views;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.config.UITheme;
import com.kozzy.controllers.AdminController;
import com.kozzy.views.components.PillBadge;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * DesktopAdminWindow: Jendela Mandiri / Terpisah Khusus Konsol Manajemen Admin Kozzy.
 * - Memisahkan modul administrasi dari jendela utama pengguna (Poin 1: panel admin pisah).
 * - Berjalan sebagai workstation mandiri dengan kontrol verifikasi transaksi dan ketersediaan kamar.
 */
public class DesktopAdminWindow extends JFrame {

    private static DesktopAdminWindow instance = null;

    private final DesktopAdminView adminView;
    private final AdminController controller = new AdminController();
    private Runnable onDataChangedCallback;

    public DesktopAdminWindow() {
        super("Kozzy Desktop — Konsol Manajemen & Verifikasi Admin (Terpisah)");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(1140, 720);
        setMinimumSize(new Dimension(980, 620));
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

        // Header Khusus Admin Bar
        JPanel topBar = createAdminTopBar();
        add(topBar, BorderLayout.NORTH);

        // Konten Konsol Admin
        adminView = new DesktopAdminView();
        add(adminView, BorderLayout.CENTER);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                instance = null;
                if (onDataChangedCallback != null) {
                    onDataChangedCallback.run();
                }
            }
        });
    }

    private JPanel createAdminTopBar() {
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setBackground(UITheme.SIDEBAR_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

        // Kiri: Judul Konsol Admin
        JPanel leftPnl = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        leftPnl.setOpaque(false);

        JLabel lblShield = new JLabel("🛡️");
        lblShield.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 22));
        leftPnl.add(lblShield);

        JPanel textPnl = new JPanel();
        textPnl.setLayout(new BoxLayout(textPnl, BoxLayout.Y_AXIS));
        textPnl.setOpaque(false);

        JLabel lblTitle = new JLabel("PANEL OPERASIONAL ADMINISTRATOR");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(UITheme.SIDEBAR_TEXT);
        textPnl.add(lblTitle);

        JLabel lblSub = new JLabel("Jendela Terpisah: Verifikasi Berkas Sewa & Manajemen Kamar Kos");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(UITheme.SIDEBAR_MUTED);
        textPnl.add(lblSub);

        leftPnl.add(textPnl);
        bar.add(leftPnl, BorderLayout.WEST);

        // Kanan: Status & Tombol Aksi
        JPanel rightPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPnl.setOpaque(false);

        boolean dbOk = controller.isDatabaseConnected();
        JLabel lblDb = new JLabel(dbOk ? "● MySQL Aktif" : "○ Mode Cadangan");
        lblDb.setFont(UITheme.FONT_SMALL);
        lblDb.setForeground(dbOk ? new Color(0x74, 0xD4, 0xA0) : UITheme.WARNING_AMBER);
        lblDb.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3B, 0x4D, 0x43), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        rightPnl.add(lblDb);

        PillButton btnRefresh = new PillButton("Refresh Data", PillButton.Style.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(125, 32));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefresh.addActionListener(e -> {
            adminView.refreshData();
            if (onDataChangedCallback != null) onDataChangedCallback.run();
        });
        rightPnl.add(btnRefresh);

        PillButton btnClose = new PillButton("Tutup Konsol", PillButton.Style.OUTLINE);
        btnClose.setPreferredSize(new Dimension(105, 32));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnClose.addActionListener(e -> dispose());
        rightPnl.add(btnClose);

        bar.add(rightPnl, BorderLayout.EAST);
        return bar;
    }

    public void setOnDataChangedCallback(Runnable callback) {
        this.onDataChangedCallback = callback;
    }

    public void refreshData() {
        adminView.refreshData();
    }

    /**
     * Membuka atau memfokuskan jendela admin mandiri.
     */
    public static DesktopAdminWindow open(Runnable onDataChanged) {
        if (instance == null) {
            instance = new DesktopAdminWindow();
            instance.setOnDataChangedCallback(onDataChanged);
            instance.setVisible(true);
        } else {
            instance.setOnDataChangedCallback(onDataChanged);
            instance.refreshData();
            instance.toFront();
            instance.requestFocus();
        }
        return instance;
    }
}
