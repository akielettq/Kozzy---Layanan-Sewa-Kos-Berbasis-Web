package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.BerandaController;
import com.kozzy.controllers.BerandaController.BerandaSummary;
import com.kozzy.models.Kamar;
import com.kozzy.models.Pengguna;
import com.kozzy.views.components.MetricCard;
import com.kozzy.views.components.PillBadge;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DesktopBerandaView: Tampilan Dashboard Utama Komprehensif Berorientasi Desktop.
 * - Seluruh informasi muat dalam 1 layar tanpa scroll vertikal.
 * - Teks judul, metrik, ubin kamar, dan informasi ditata rata tengah (Poin 4).
 * - Akses langsung ke Panel Admin Terpisah untuk akun pengelola/administrator (Poin 1).
 */
public class DesktopBerandaView extends JPanel {

    public interface BerandaActionCallback {
        void onSelectRoom(String kodeKamar);
        void onNavigateToKamar();
        void onNavigateToLogin();
        void onOpenAdminWindow();
    }

    private final BerandaActionCallback callback;
    private final BerandaController controller = new BerandaController();

    private MetricCard mcTotal;
    private MetricCard mcTersedia;
    private MetricCard mcTerisi;
    private MetricCard mcPending;

    private JPanel gridKamarPanel;
    private JLabel lblWelcome;
    private JPanel cardAksiButtonPanel;

    public DesktopBerandaView(BerandaActionCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout(0, 16));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // ==========================================
        // 1. TOP HEADER BAR (Rata Tengah - Poin 4)
        // ==========================================
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTag = new JLabel("▪ SISTEM OPERASIONAL KOZZY", SwingConstants.CENTER);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(UITheme.ACCENT_TERA);
        lblTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblTag);
        titleBox.add(Box.createVerticalStrut(2));

        JLabel lblTitle = new JLabel("Dashboard Operasional Kos", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));

        lblWelcome = new JLabel("Selamat Datang di Sistem Operasional Kozzy", SwingConstants.CENTER);
        lblWelcome.setFont(UITheme.FONT_BODY);
        lblWelcome.setForeground(UITheme.TEXT_MUTED);
        lblWelcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblWelcome);

        headerBar.add(titleBox, BorderLayout.CENTER);

        // Date Badge di pojok kanan atas (Penyewa tidak menampilkan status teknis MySQL)
        JPanel topMeta = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        topMeta.setOpaque(false);

        String tglStr = new SimpleDateFormat("EEEE, dd MMMM yyyy", new Locale("id", "ID")).format(new Date());
        JLabel lblDate = new JLabel(tglStr);
        lblDate.setFont(UITheme.FONT_SMALL);
        lblDate.setForeground(UITheme.TEXT_MUTED);
        lblDate.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        topMeta.add(lblDate);
        headerBar.add(topMeta, BorderLayout.EAST);

        add(headerBar, BorderLayout.NORTH);

        // ==========================================
        // 2. CENTER CONTENT (METRICS + SPLIT BODY)
        // ==========================================
        JPanel centerContainer = new JPanel(new BorderLayout(0, 14));
        centerContainer.setOpaque(false);

        // A. Baris Kartu Metrik KPI (4 Kolom Rata Tengah - Poin 4)
        JPanel metricRow = new JPanel(new GridLayout(1, 4, 12, 0));
        metricRow.setOpaque(false);
        metricRow.setPreferredSize(new Dimension(0, 95));

        mcTotal = new MetricCard("Total Kamar", "8", "Gedung A & Gedung B", UITheme.TEXT_MUTED);
        mcTersedia = new MetricCard("Kamar Tersedia", "0", "Siap Disewa", UITheme.PRIMARY_GREEN);
        mcTerisi = new MetricCard("Kamar Terisi", "0", "Sedang Dihuni", UITheme.ACCENT_TERA);
        mcPending = new MetricCard("Pengajuan Pending", "0", "Menunggu Verifikasi", UITheme.WARNING_AMBER);

        mcTotal.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mcTotal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (callback != null) callback.onNavigateToKamar();
            }
        });

        mcPending.setCursor(new Cursor(Cursor.HAND_CURSOR));
        mcPending.setToolTipText("Klik untuk membuka verifikasi di Panel Admin");
        mcPending.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (callback != null) callback.onOpenAdminWindow();
            }
        });

        metricRow.add(mcTotal);
        metricRow.add(mcTersedia);
        metricRow.add(mcTerisi);
        metricRow.add(mcPending);

        centerContainer.add(metricRow, BorderLayout.NORTH);

        // B. Body Dua Kolom (Grid Kamar 66% + Info & Aksi 34%)
        JPanel splitBody = new JPanel(new GridBagLayout());
        splitBody.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.insets = new Insets(0, 0, 0, 0);

        // KOLOM KIRI (66%): Grid Status 10 Kamar
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.66;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 0, 0, 8);

        JPanel pnlKiri = new JPanel(new BorderLayout(0, 10));
        pnlKiri.setBackground(UITheme.BG_CARD);
        pnlKiri.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // Header Kiri Rata Tengah (Poin 4)
        JPanel headerKiri = new JPanel(new GridLayout(2, 1, 0, 2));
        headerKiri.setOpaque(false);
        JLabel lblKiriJudul = new JLabel("Peta Ketersediaan Kamar Kos", SwingConstants.CENTER);
        lblKiriJudul.setFont(UITheme.FONT_TITLE);
        lblKiriJudul.setForeground(UITheme.TEXT_DARK);
        headerKiri.add(lblKiriJudul);

        JLabel lblKiriHint = new JLabel("Klik ubin kamar di bawah untuk melihat rincian fasilitas & foto lengkap", SwingConstants.CENTER);
        lblKiriHint.setFont(UITheme.FONT_SMALL);
        lblKiriHint.setForeground(UITheme.TEXT_MUTED);
        headerKiri.add(lblKiriHint);

        pnlKiri.add(headerKiri, BorderLayout.NORTH);

        // 10 Kamar dalam Grid 5 Kolom x 2 Baris
        gridKamarPanel = new JPanel(new GridLayout(2, 5, 8, 8));
        gridKamarPanel.setOpaque(false);
        pnlKiri.add(gridKamarPanel, BorderLayout.CENTER);

        splitBody.add(pnlKiri, gbc);

        // KOLOM KANAN (34%): Info Gedung & Aksi Cepat
        gbc.gridx = 1;
        gbc.gridy = 0;
        gbc.weightx = 0.34;
        gbc.weighty = 1.0;
        gbc.insets = new Insets(0, 8, 0, 0);

        JPanel pnlKanan = new JPanel(new GridLayout(2, 1, 0, 10));
        pnlKanan.setOpaque(false);

        // Kartu 1: Info Gedung (Rata Tengah - Poin 4)
        JPanel cardInfoGedung = new JPanel();
        cardInfoGedung.setLayout(new BoxLayout(cardInfoGedung, BoxLayout.Y_AXIS));
        cardInfoGedung.setBackground(UITheme.BG_CARD);
        cardInfoGedung.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblGedungTitle = new JLabel("Ketentuan Lingkungan Gedung", SwingConstants.CENTER);
        lblGedungTitle.setFont(UITheme.FONT_TITLE);
        lblGedungTitle.setForeground(UITheme.TEXT_DARK);
        lblGedungTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardInfoGedung.add(lblGedungTitle);
        cardInfoGedung.add(Box.createVerticalStrut(10));

        cardInfoGedung.add(createBuildingRow("GEDUNG A (PUTRA)", "Khusus Pria. Parkiran luas, dekat area kampus, sirkulasi udara baik.", UITheme.PRIMARY_GREEN));
        cardInfoGedung.add(Box.createVerticalStrut(8));
        cardInfoGedung.add(createBuildingRow("GEDUNG B (PUTRI)", "Khusus Mahasiswi & Karyawati. Tenang, aman, CCTV 24 jam, dapur bersama.", UITheme.ACCENT_TERA));

        pnlKanan.add(cardInfoGedung);

        // Kartu 2: Aksi Cepat & Bantuan
        JPanel cardAksi = new JPanel(new BorderLayout(0, 10));
        cardAksi.setBackground(UITheme.BG_CARD);
        cardAksi.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblAksiTitle = new JLabel("Pusat Layanan & Navigasi Cepat", SwingConstants.CENTER);
        lblAksiTitle.setFont(UITheme.FONT_TITLE);
        lblAksiTitle.setForeground(UITheme.TEXT_DARK);
        cardAksi.add(lblAksiTitle, BorderLayout.NORTH);

        JPanel pnlBantuan = new JPanel();
        pnlBantuan.setLayout(new BoxLayout(pnlBantuan, BoxLayout.Y_AXIS));
        pnlBantuan.setOpaque(false);

        JLabel lblWa = new JLabel("WhatsApp: 0896-6132-7050", SwingConstants.CENTER);
        lblWa.setFont(UITheme.FONT_BODY);
        lblWa.setForeground(UITheme.TEXT_DARK);
        lblWa.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlBantuan.add(lblWa);
        pnlBantuan.add(Box.createVerticalStrut(4));

        JLabel lblAlamat = new JLabel("Lokasi: Jl. Bangau Perkasa, Pekanbaru", SwingConstants.CENTER);
        lblAlamat.setFont(UITheme.FONT_SMALL);
        lblAlamat.setForeground(UITheme.TEXT_MUTED);
        lblAlamat.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlBantuan.add(lblAlamat);

        cardAksi.add(pnlBantuan, BorderLayout.CENTER);

        cardAksiButtonPanel = new JPanel(new GridLayout(1, 1, 6, 0));
        cardAksiButtonPanel.setOpaque(false);
        cardAksi.add(cardAksiButtonPanel, BorderLayout.SOUTH);

        pnlKanan.add(cardAksi);

        splitBody.add(pnlKanan, gbc);

        centerContainer.add(splitBody, BorderLayout.CENTER);
        add(centerContainer, BorderLayout.CENTER);

        // Muat data awal
        refreshData();
    }

    private JPanel createBuildingRow(String header, String desc, Color dotColor) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);

        JPanel dot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(dotColor);
                g2.fillOval(0, 4, 8, 8);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(10, 16);
            }
        };
        dot.setOpaque(false);
        row.add(dot, BorderLayout.WEST);

        JPanel text = new JPanel();
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.setOpaque(false);

        JLabel lblH = new JLabel(header);
        lblH.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblH.setForeground(UITheme.TEXT_DARK);
        text.add(lblH);

        JLabel lblD = new JLabel("<html><body style='width: 250px; font-size: 11px; color: #6E6A62;'>" + desc + "</body></html>");
        text.add(lblD);

        row.add(text, BorderLayout.CENTER);
        return row;
    }

    public void refreshData() {
        BerandaSummary summary = controller.getSummary();

        Pengguna user = summary.getCurrentUser();
        boolean isAdmin = (user != null && user.isAdmin());

        if (user != null) {
            lblWelcome.setText("Selamat Datang, " + user.getNama() + " (" + user.getRoleDisplayName() + ")");
        } else {
            lblWelcome.setText("Selamat Datang di Sistem Operasional Kozzy");
        }

        mcTotal.setValue(String.valueOf(summary.getTotalKamar()));
        mcTersedia.setValue(String.valueOf(summary.getKamarTersedia()));
        mcTerisi.setValue(String.valueOf(summary.getKamarTerisi()));
        mcPending.setValue(String.valueOf(summary.getTotalPending()));

        // Tombol Aksi Kolom Kanan Disesuaikan dengan Hak Akses (Poin 1)
        if (cardAksiButtonPanel != null) {
            cardAksiButtonPanel.removeAll();
            if (isAdmin) {
                cardAksiButtonPanel.setLayout(new GridLayout(2, 1, 0, 6));

                PillButton btnAdminWin = new PillButton("Buka Panel Admin (Jendela Terpisah) →", PillButton.Style.PRIMARY);
                btnAdminWin.addActionListener(e -> {
                    if (callback != null) callback.onOpenAdminWindow();
                });
                cardAksiButtonPanel.add(btnAdminWin);

                PillButton btnKamar = new PillButton("Buka Katalog Kamar", PillButton.Style.OUTLINE);
                btnKamar.addActionListener(e -> {
                    if (callback != null) callback.onNavigateToKamar();
                });
                cardAksiButtonPanel.add(btnKamar);
            } else {
                cardAksiButtonPanel.setLayout(new GridLayout(1, 1, 0, 0));
                PillButton btnJelajah = new PillButton("Buka Katalog Kamar Lengkap →", PillButton.Style.PRIMARY);
                btnJelajah.addActionListener(e -> {
                    if (callback != null) callback.onNavigateToKamar();
                });
                cardAksiButtonPanel.add(btnJelajah);
            }
            cardAksiButtonPanel.revalidate();
            cardAksiButtonPanel.repaint();
        }

        // Render Ubin Status Kamar dari data yang disiapkan Controller
        gridKamarPanel.removeAll();
        for (Kamar k : summary.getRoomList()) {
            gridKamarPanel.add(createRoomTile(k));
        }

        revalidate();
        repaint();
    }

    private JPanel createRoomTile(Kamar k) {
        boolean isAvail = "tersedia".equalsIgnoreCase(k.getStatus());
        boolean isTerisi = "terisi".equalsIgnoreCase(k.getStatus());
        final boolean[] isHover = {false};

        JPanel tile = new JPanel(new BorderLayout(4, 4)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (isHover[0]) {
                    g2.setColor(new Color(0x22, 0x1F, 0x19, 25));
                    g2.fillRoundRect(2, 2, w - 3, h - 3, 10, 10);
                }

                Color bg = isAvail ? (isHover[0] ? new Color(0xF4, 0xFA, 0xF6) : new Color(0xFB, 0xFC, 0xFA))
                                   : (isTerisi ? new Color(0xF7, 0xF5, 0xF2) : new Color(0xFD, 0xF9, 0xEE));
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w - 2, h - 2, 10, 10);

                g2.setColor(isHover[0] ? UITheme.PRIMARY_GREEN : new Color(0x22, 0x1F, 0x19, 130));
                g2.setStroke(new BasicStroke(isHover[0] ? 1.6f : 1.1f));
                g2.drawRoundRect(0, 0, w - 2, h - 2, 10, 10);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        tile.setOpaque(false);
        tile.setBorder(BorderFactory.createEmptyBorder(6, 8, 6, 8));
        tile.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Top: Kode Kamar + Gender
        JPanel top = new JPanel(new BorderLayout(2, 0));
        top.setOpaque(false);

        JLabel lblKode = new JLabel(k.getKodeKamar());
        lblKode.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblKode.setForeground(UITheme.TEXT_DARK);
        top.add(lblKode, BorderLayout.WEST);

        PillBadge badgeGender = PillBadge.createStatusBadge(k.getTipe());
        badgeGender.setFont(new Font("Segoe UI", Font.BOLD, 9));
        badgeGender.setBorder(BorderFactory.createEmptyBorder(1, 4, 1, 4));
        top.add(badgeGender, BorderLayout.EAST);
        tile.add(top, BorderLayout.NORTH);

        // Center: Harga singkat (Tengah)
        String hrgStr = (k.getHarga() / 1000) + " rb / bln";
        JLabel lblHrg = new JLabel(hrgStr, SwingConstants.CENTER);
        lblHrg.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHrg.setForeground(isAvail ? UITheme.PRIMARY_GREEN : UITheme.TEXT_MUTED);
        tile.add(lblHrg, BorderLayout.CENTER);

        // Bottom: Status badge
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        btm.setOpaque(false);
        PillBadge bStatus = PillBadge.createStatusBadge(k.getStatus());
        bStatus.setFont(new Font("Segoe UI", Font.BOLD, 9));
        bStatus.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
        btm.add(bStatus);
        tile.add(btm, BorderLayout.SOUTH);

        tile.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                isHover[0] = true;
                tile.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHover[0] = false;
                tile.repaint();
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (callback != null) {
                    callback.onSelectRoom(k.getKodeKamar());
                }
            }
        });

        return tile;
    }
}
