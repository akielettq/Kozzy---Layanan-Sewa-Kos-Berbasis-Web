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
 * - Seluruh teks informasi dan label ditata rata tengah (Poin 4).
 * - Pas dalam 1 layar tanpa scroll vertikal.
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
    private JLabel lblGender;
    private JPanel pnlRoleBadge;

    private JLabel lblStatTotal;
    private JLabel lblStatAktif;
    private JLabel lblStatPengeluaran;

    public DesktopProfilView(ProfilActionCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // Header (Rata Tengah - Poin 4)
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        // Section Tag Khas Kozzy Web (▪ PROFIL AKUN PENGGUNA)
        JLabel lblSectionTag = new JLabel("▪ PROFIL AKUN PENGGUNA", SwingConstants.CENTER);
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(UITheme.ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblSectionTag);
        titleBox.add(Box.createVerticalStrut(2));

        JLabel lblTitle = new JLabel("Profil Akun Pengguna", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));

        JLabel lblSub = new JLabel("Informasi data diri dan ringkasan aktivitas akun Kozzy kamu.", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // Center Container (50% / 50%)
        JPanel centerPnl = new JPanel(new GridLayout(1, 2, 14, 0));
        centerPnl.setOpaque(false);

        // Kartu Kiri: Identitas Pengguna (Rata Tengah)
        JPanel pnlUserCard = new JPanel();
        pnlUserCard.setLayout(new BoxLayout(pnlUserCard, BoxLayout.Y_AXIS));
        pnlUserCard.setBackground(UITheme.BG_CARD);
        pnlUserCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
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

        lblName = new JLabel("Nama Pengguna", SwingConstants.CENTER);
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(UITheme.TEXT_DARK);
        lblName.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlUserCard.add(lblName);
        pnlUserCard.add(Box.createVerticalStrut(4));

        pnlRoleBadge = new JPanel(new FlowLayout(FlowLayout.CENTER));
        pnlRoleBadge.setOpaque(false);
        pnlRoleBadge.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlUserCard.add(pnlRoleBadge);
        pnlUserCard.add(Box.createVerticalStrut(16));

        // Info List (Rata Tengah)
        JPanel infoList = new JPanel(new GridLayout(3, 1, 0, 8));
        infoList.setOpaque(false);
        infoList.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD8, 0xD2, 0xC5), 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));
        infoList.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));
        infoList.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel rowEmail = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        rowEmail.setOpaque(false);
        JLabel lblEmailH = new JLabel("Alamat Email:");
        lblEmailH.setFont(UITheme.FONT_SMALL);
        lblEmailH.setForeground(UITheme.TEXT_MUTED);
        rowEmail.add(lblEmailH);
        lblEmail = new JLabel("email@kozzy.id");
        lblEmail.setFont(UITheme.FONT_BODY_BOLD);
        lblEmail.setForeground(UITheme.TEXT_DARK);
        rowEmail.add(lblEmail);
        infoList.add(rowEmail);

        JPanel rowPhone = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        rowPhone.setOpaque(false);
        JLabel lblPhoneH = new JLabel("WhatsApp:");
        lblPhoneH.setFont(UITheme.FONT_SMALL);
        lblPhoneH.setForeground(UITheme.TEXT_MUTED);
        rowPhone.add(lblPhoneH);
        lblPhone = new JLabel("08123456789");
        lblPhone.setFont(UITheme.FONT_BODY_BOLD);
        lblPhone.setForeground(UITheme.TEXT_DARK);
        rowPhone.add(lblPhone);
        infoList.add(rowPhone);

        JPanel rowGender = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        rowGender.setOpaque(false);
        JLabel lblGenderH = new JLabel("Jenis Kelamin:");
        lblGenderH.setFont(UITheme.FONT_SMALL);
        lblGenderH.setForeground(UITheme.TEXT_MUTED);
        rowGender.add(lblGenderH);
        lblGender = new JLabel("Laki-laki");
        lblGender.setFont(UITheme.FONT_BODY_BOLD);
        lblGender.setForeground(UITheme.TEXT_DARK);
        rowGender.add(lblGender);
        infoList.add(rowGender);

        pnlUserCard.add(infoList);
        pnlUserCard.add(Box.createVerticalGlue());

        PillButton btnKeluar = new PillButton("Keluar dari Akun (Logout)", PillButton.Style.OUTLINE);
        btnKeluar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnKeluar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        btnKeluar.addActionListener(e -> {
            authController.logout();
            if (callback != null) callback.onLogout();
        });
        pnlUserCard.add(btnKeluar);
        centerPnl.add(pnlUserCard);

        // Kartu Kanan: Statistik Akun & Aktivitas (Rata Tengah)
        JPanel pnlStats = new JPanel();
        pnlStats.setLayout(new BoxLayout(pnlStats, BoxLayout.Y_AXIS));
        pnlStats.setBackground(UITheme.BG_CARD);
        pnlStats.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel lblStatTitle = new JLabel("Statistik & Ringkasan Sewa", SwingConstants.CENTER);
        lblStatTitle.setFont(UITheme.FONT_TITLE);
        lblStatTitle.setForeground(UITheme.TEXT_DARK);
        lblStatTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlStats.add(lblStatTitle);
        pnlStats.add(Box.createVerticalStrut(14));

        JPanel statGrid = new JPanel(new GridLayout(3, 1, 0, 10));
        statGrid.setOpaque(false);

        lblStatTotal = new JLabel("0 Kali", SwingConstants.CENTER);
        lblStatAktif = new JLabel("Tidak Ada", SwingConstants.CENTER);
        lblStatPengeluaran = new JLabel("Rp 0", SwingConstants.CENTER);

        statGrid.add(createStatRow("Total Pengajuan Booking", lblStatTotal, UITheme.TEXT_DARK));
        statGrid.add(createStatRow("Status Sewa Saat Ini", lblStatAktif, UITheme.PRIMARY_GREEN));
        statGrid.add(createStatRow("Akumulasi Nilai Sewa", lblStatPengeluaran, UITheme.ACCENT_TERA));

        pnlStats.add(statGrid);
        pnlStats.add(Box.createVerticalStrut(20));

        PillButton btnLihatSewa = new PillButton("Buka Halaman Sewa Saya →", PillButton.Style.PRIMARY);
        btnLihatSewa.setAlignmentX(Component.CENTER_ALIGNMENT);
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
        JPanel r = new JPanel(new GridLayout(2, 1, 0, 3));
        r.setBackground(UITheme.BG_CARD_ALT);
        r.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        JLabel lblT = new JLabel(title, SwingConstants.CENTER);
        lblT.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblT.setForeground(UITheme.TEXT_MUTED);
        r.add(lblT);

        valLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valLabel.setForeground(valColor);
        valLabel.setHorizontalAlignment(SwingConstants.CENTER);
        r.add(valLabel);

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
        lblGender.setText(user.getJenisKelamin());

        pnlRoleBadge.removeAll();
        pnlRoleBadge.add(PillBadge.createStatusBadge(user.isAdmin() ? "ADMINISTRATOR" : "PENCARI KOS"));

        lblStatTotal.setText(summary.getTotalBooking() + " Transaksi");
        lblStatAktif.setText(summary.getTotalAktif() > 0 ? "Aktif Menghuni" : "Tidak Ada");
        lblStatPengeluaran.setText(summary.getFormattedPengeluaran());

        revalidate();
        repaint();
    }
}
