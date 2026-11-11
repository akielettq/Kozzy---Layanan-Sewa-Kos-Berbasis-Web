package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.AuthController;
import com.kozzy.controllers.ProfilController;
import com.kozzy.controllers.ProfilController.ProfilSummary;
import com.kozzy.models.Pengguna;
import com.kozzy.views.components.PillBadge;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import java.awt.*;

/**
 * DesktopProfilView: Tampilan Profil Akun & Statistik Ringkas Pengguna.
 * - Desain ringkas, elegan, dan informatif.
 * - Pas dalam 1 layar tanpa scroll vertikal.
 * - Murni tampilan UI; seluruh kalkulasi riwayat didelegasikan ke ProfilController.
 */
public class DesktopProfilView extends JPanel {

    public interface ProfilActionCallback {
        void onLogout();
        void onNavigateToSewaku();
    }

    private final ProfilActionCallback callback;
    private final ProfilController controller = new ProfilController();
    private final AuthController authController = new AuthController();

    private JLabel lblAvatar;
    private JLabel lblName;
    private JLabel lblEmail;
    private JLabel lblPhone;
    private JPanel pnlRoleBadge;

    private JLabel lblStatTotal;
    private JLabel lblStatAktif;
    private JLabel lblStatPengeluaran;
    private PillButton btnLihatSewa;

    public DesktopProfilView(ProfilActionCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Profil Akun Pengguna");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        titleBox.add(lblTitle);

        JLabel lblSub = new JLabel("Informasi data diri dan ringkasan aktivitas akun Kozzy kamu.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // Center Container (50% / 50%)
        JPanel centerPnl = new JPanel(new GridLayout(1, 2, 14, 0));
        centerPnl.setOpaque(false);

        // Kartu Kiri: Identitas Pengguna
        JPanel pnlUserCard = new JPanel();
        pnlUserCard.setLayout(new BoxLayout(pnlUserCard, BoxLayout.Y_AXIS));
        pnlUserCard.setBackground(UITheme.BG_CARD);
        pnlUserCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        lblAvatar = new JLabel("U", SwingConstants.CENTER) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.PRIMARY_GREEN);
                g2.fillOval(0, 0, getWidth(), getHeight());
                g2.dispose();
                super.paintComponent(g);
            }
        };
        lblAvatar.setPreferredSize(new Dimension(72, 72));
        lblAvatar.setMaximumSize(new Dimension(72, 72));
        lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 32));
        lblAvatar.setForeground(Color.WHITE);
        lblAvatar.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlUserCard.add(lblAvatar);
        pnlUserCard.add(Box.createVerticalStrut(14));

        lblName = new JLabel("Nama Pengguna");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(UITheme.TEXT_DARK);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlUserCard.add(lblName);
        pnlUserCard.add(Box.createVerticalStrut(4));

        pnlRoleBadge = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        pnlRoleBadge.setOpaque(false);
        pnlRoleBadge.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        pnlRoleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlUserCard.add(pnlRoleBadge);
        pnlUserCard.add(Box.createVerticalStrut(16));

        // Info List
        JPanel infoList = new JPanel(new GridLayout(2, 2, 8, 10));
        infoList.setOpaque(false);
        infoList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        infoList.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        infoList.setAlignmentX(Component.CENTER_ALIGNMENT);

        infoList.add(new JLabel("Alamat Email:"));
        lblEmail = new JLabel("email@kozzy.id");
        lblEmail.setFont(UITheme.FONT_BODY_BOLD);
        infoList.add(lblEmail);

        infoList.add(new JLabel("Nomor WhatsApp:"));
        lblPhone = new JLabel("08123456789");
        lblPhone.setFont(UITheme.FONT_BODY_BOLD);
        infoList.add(lblPhone);

        pnlUserCard.add(infoList);
        pnlUserCard.add(Box.createVerticalGlue());

        PillButton btnKeluar = new PillButton("Keluar dari Akun (Logout)", PillButton.Style.OUTLINE);
        btnKeluar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnKeluar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnKeluar.addActionListener(e -> {
            boolean confirm = UITheme.showConfirm(
                    this,
                    "Konfirmasi Keluar",
                    "Apakah kamu yakin ingin keluar dari akun saat ini?",
                    "Keluar Akun",
                    PillButton.Style.DANGER
            );
            if (confirm) {
                authController.logout();
                if (callback != null) callback.onLogout();
            }
        });
        pnlUserCard.add(btnKeluar);
        centerPnl.add(pnlUserCard);

        // Kartu Kanan: Statistik Akun & Aktivitas
        JPanel pnlStats = new JPanel();
        pnlStats.setLayout(new BoxLayout(pnlStats, BoxLayout.Y_AXIS));
        pnlStats.setBackground(UITheme.BG_CARD);
        pnlStats.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblStatTitle = new JLabel("Statistik & Ringkasan Sewa");
        lblStatTitle.setFont(UITheme.FONT_TITLE);
        lblStatTitle.setForeground(UITheme.TEXT_DARK);
        lblStatTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlStats.add(lblStatTitle);
        pnlStats.add(Box.createVerticalStrut(14));

        JPanel statGrid = new JPanel(new GridLayout(3, 1, 0, 10));
        statGrid.setOpaque(false);
        statGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 165));
        statGrid.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblStatTotal = new JLabel("0 Kali");
        lblStatAktif = new JLabel("Tidak Ada");
        lblStatPengeluaran = new JLabel("Rp 0");

        statGrid.add(createStatRow("Total Pengajuan Booking", lblStatTotal, UITheme.TEXT_DARK));
        statGrid.add(createStatRow("Status Sewa Saat Ini", lblStatAktif, UITheme.PRIMARY_GREEN));
        statGrid.add(createStatRow("Akumulasi Nilai Sewa", lblStatPengeluaran, UITheme.ACCENT_TERA));

        pnlStats.add(statGrid);
        pnlStats.add(Box.createVerticalStrut(20));

        btnLihatSewa = new PillButton("Buka Halaman Sewa Saya", PillButton.Style.PRIMARY);
        btnLihatSewa.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnLihatSewa.addActionListener(e -> {
            if (callback != null) callback.onNavigateToSewaku();
        });
        pnlStats.add(btnLihatSewa);
        pnlStats.add(Box.createVerticalGlue());

        centerPnl.add(pnlStats);

        add(centerPnl, BorderLayout.CENTER);
        refreshData();
    }

    private JPanel createStatRow(String title, JLabel valLabel, Color valColor) {
        JPanel r = new JPanel(new BorderLayout(8, 0));
        r.setBackground(UITheme.BG_CARD_ALT);
        r.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        JLabel lblT = new JLabel(title);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblT.setForeground(UITheme.TEXT_MUTED);
        r.add(lblT, BorderLayout.WEST);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        valLabel.setForeground(valColor);
        r.add(valLabel, BorderLayout.EAST);

        return r;
    }

    public void refreshData() {
        ProfilSummary summary = controller.getProfilSummary();
        Pengguna user = summary.getUser();
        if (user == null) return;

        String name = user.getNama();
        lblName.setText(name);
        lblAvatar.setText(name != null && !name.isEmpty() ? name.substring(0, 1).toUpperCase() : "U");
        lblEmail.setText(user.getEmail());
        lblPhone.setText(user.getNoTelepon());

        pnlRoleBadge.removeAll();
        pnlRoleBadge.add(PillBadge.createStatusBadge(user.isAdmin() ? "ADMINISTRATOR" : "PENCARI KOS"));

        if (btnLihatSewa != null) {
            btnLihatSewa.setText(user.isAdmin()
                    ? "Buka Panel Admin (Jendela Terpisah)"
                    : "Buka Halaman Sewa Saya");
        }

        lblStatTotal.setText(summary.getTotalBooking() + " Transaksi");
        lblStatAktif.setText(summary.getTotalAktif() > 0 ? "Aktif Menghuni" : "Tidak Ada");
        lblStatPengeluaran.setText(summary.getFormattedPengeluaran());

        revalidate();
        repaint();
    }
}
