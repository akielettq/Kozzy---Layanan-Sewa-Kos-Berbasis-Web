package com.kozzy.config;

import com.kozzy.models.Booking;
import com.kozzy.views.components.ImageHelper;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * UITheme: Menyediakan palet warna, font, token desain, serta dialog pop-up kustom
 * untuk desktop workstation Kozzy.
 */
public class UITheme {
    // Palet Warna Utama
    public static final Color BG_CANVAS      = new Color(0xF4, 0xEF, 0xE4); // --kertas: #F4EFE4 (Warm Cream Canvas)
    public static final Color BG_CARD        = new Color(0xFF, 0xFF, 0xFF); // White Card / Table Surface
    public static final Color BG_CARD_ALT    = new Color(0xFB, 0xF8, 0xF0); // Soft Antique White
    public static final Color TEXT_DARK      = new Color(0x22, 0x1F, 0x19); // Primary Ink
    public static final Color TEXT_MUTED     = new Color(0x6E, 0x6A, 0x62); // Secondary / Muted Text
    public static final Color BORDER_COLOR   = new Color(0xD8, 0xD2, 0xC5); // Crisp divider line
    public static final Color BORDER_DARK    = new Color(0x22, 0x1F, 0x19); // Retro border outline

    // Warna Aksen & Tombol
    public static final Color PRIMARY_GREEN  = new Color(0x1F, 0x5B, 0x41); // Forest Green
    public static final Color PRIMARY_HOVER  = new Color(0x17, 0x47, 0x33); // Darker Forest Green
    public static final Color ACCENT_TERA    = new Color(0xE2, 0x59, 0x2C); // Terracotta Accent
    public static final Color WARNING_AMBER  = new Color(0xB4, 0x75, 0x14); // Amber / Pending
    public static final Color DANGER_RED     = new Color(0xC6, 0x36, 0x24); // Red Tolak

    // Sidebar Desktop Spesifik
    public static final Color SIDEBAR_BG     = new Color(0x1C, 0x26, 0x21); // Slate Deep Forest
    public static final Color SIDEBAR_HOVER  = new Color(0x27, 0x36, 0x2F); // Hover state
    public static final Color SIDEBAR_ACTIVE = new Color(0x23, 0x47, 0x37); // Active tab state
    public static final Color SIDEBAR_TEXT   = new Color(0xF5, 0xF3, 0xEE); // High contrast text
    public static final Color SIDEBAR_MUTED  = new Color(0x9E, 0xA8, 0xA1); // Sidebar secondary text

    // Lencana Status (Pills)
    public static final Color BADGE_GREEN_BG = new Color(0xDC, 0xED, 0xE1);
    public static final Color BADGE_GREEN_FG = new Color(0x13, 0x4E, 0x33);
    public static final Color BADGE_TERA_BG  = new Color(0xFB, 0xE5, 0xDF);
    public static final Color BADGE_TERA_FG  = new Color(0xBE, 0x3B, 0x12);
    public static final Color BADGE_AMBER_BG = new Color(0xFE, 0xF3, 0xD6);
    public static final Color BADGE_AMBER_FG = new Color(0x7D, 0x52, 0x0A);
    public static final Color BADGE_MUTED_BG = new Color(0xEA, 0xE5, 0xD9);
    public static final Color BADGE_MUTED_FG = new Color(0x5A, 0x57, 0x50);

    // Tipografi Desktop Modern
    public static final Font FONT_LOGO        = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER      = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE       = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Konfigurasi FlatLaf Look and Feel untuk lingkungan Desktop Workstation.
     */
    public static void setupFlatLaf() {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("Panel.background", BG_CANVAS);
            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.selectionBackground", new Color(0xDC, 0xED, 0xE1));
            UIManager.put("Table.selectionForeground", TEXT_DARK);
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("TableHeader.background", BG_CARD_ALT);
            UIManager.put("TableHeader.font", FONT_BODY_BOLD);
            UIManager.put("TableHeader.separatorColor", BORDER_COLOR);
        } catch (Throwable t) {
            System.err.println("FlatLaf tidak tersedia, menggunakan Look & Feel sistem: " + t.getMessage());
        }
    }

    // =========================================================================
    // KUSTOM POP-UP DIALOGS (Menggantikan JOptionPane Bawaan & Textfield Biasa)
    // =========================================================================

    public static void showSuccess(Component parent, String title, String message) {
        showCustomAlert(parent, title, message, PRIMARY_GREEN);
    }

    public static void showWarning(Component parent, String title, String message) {
        showCustomAlert(parent, title, message, WARNING_AMBER);
    }

    public static void showError(Component parent, String title, String message) {
        showCustomAlert(parent, title, message, DANGER_RED);
    }

    private static String formatHtmlMessage(String message) {
        if (message == null || message.isEmpty()) return "";
        return message
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("\n", "<br>");
    }

    private static void showCustomAlert(Component parent, String title, String message, Color accentColor) {
        Window owner = (parent instanceof Window) ? (Window) parent : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        JDialog dlg = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setResizable(true);
        dlg.getContentPane().setBackground(BG_CANVAS);
        dlg.setLayout(new BorderLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(18, 22, 18, 22)
                )
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(accentColor != null ? accentColor : TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));

        JLabel lblMsg = new JLabel("<html><table width='390' border='0' cellspacing='0' cellpadding='0'><tr><td align='center' style='color:#5A5750; font-family:Segoe UI, sans-serif; font-size:12px; line-height:1.45;'>"
                + formatHtmlMessage(message) + "</td></tr></table></html>", SwingConstants.CENTER);
        lblMsg.setFont(FONT_BODY);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(18));

        PillButton btnOk = new PillButton("Mengerti", PillButton.Style.PRIMARY);
        btnOk.setPreferredSize(new Dimension(130, 36));
        btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOk.addActionListener(e -> dlg.dispose());
        card.add(btnOk);

        dlg.add(card, BorderLayout.CENTER);
        dlg.pack();
        int targetW = Math.max(480, dlg.getWidth() + 24);
        int targetH = Math.max(220, dlg.getHeight() + 16);
        dlg.setSize(targetW, targetH);
        dlg.setMinimumSize(new Dimension(460, 210));
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);
    }

    public static boolean showConfirm(Component parent, String title, String message, String confirmBtnText, PillButton.Style confirmStyle) {
        Window owner = (parent instanceof Window) ? (Window) parent : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        JDialog dlg = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setResizable(true);
        dlg.getContentPane().setBackground(BG_CANVAS);
        dlg.setLayout(new BorderLayout());

        final boolean[] result = {false};

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(18, 22, 18, 22)
                )
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));

        JLabel lblMsg = new JLabel("<html><table width='390' border='0' cellspacing='0' cellpadding='0'><tr><td align='center' style='color:#5A5750; font-family:Segoe UI, sans-serif; font-size:12px; line-height:1.45;'>"
                + formatHtmlMessage(message) + "</td></tr></table></html>", SwingConstants.CENTER);
        lblMsg.setFont(FONT_BODY);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        PillButton btnCancel = new PillButton("Batalkan", PillButton.Style.OUTLINE);
        btnCancel.setPreferredSize(new Dimension(110, 36));
        btnCancel.addActionListener(e -> {
            result[0] = false;
            dlg.dispose();
        });
        btnRow.add(btnCancel);

        PillButton btnConfirm = new PillButton(confirmBtnText, confirmStyle != null ? confirmStyle : PillButton.Style.PRIMARY);
        btnConfirm.setPreferredSize(new Dimension(145, 36));
        btnConfirm.addActionListener(e -> {
            result[0] = true;
            dlg.dispose();
        });
        btnRow.add(btnConfirm);

        card.add(btnRow);
        dlg.add(card, BorderLayout.CENTER);

        dlg.pack();
        int targetW = Math.max(480, dlg.getWidth() + 24);
        int targetH = Math.max(230, dlg.getHeight() + 16);
        dlg.setSize(targetW, targetH);
        dlg.setMinimumSize(new Dimension(460, 220));
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);

        return result[0];
    }

    /**
     * Dialog Kustom Penolakan Booking Kamar:
     * Menggunakan area teks multiline (JTextArea) + tombol chip pilihan alasan cepat,
     * bukan text field 1 baris biasa.
     */
    public static String showRejectBookingDialog(Component parent, Booking booking) {
        Window owner = (parent instanceof Window) ? (Window) parent : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        String dlgTitle = "Konfirmasi Penolakan Booking — " + (booking != null ? booking.getIdBooking() : "");
        JDialog dlg = new JDialog(owner, dlgTitle, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.getContentPane().setBackground(BG_CANVAS);
        dlg.setLayout(new BorderLayout());

        final String[] resultReason = {null};

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(18, 24, 18, 24)
                )
        ));

        JLabel lblSectionTag = new JLabel("▪ VERIFIKASI PENOLAKAN BOOKING", SwingConstants.CENTER);
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSectionTag);
        card.add(Box.createVerticalStrut(6));

        JLabel lblTitle = new JLabel("Penolakan Pengajuan Booking", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));

        String detailText = (booking != null)
                ? "ID: " + booking.getIdBooking() + " · Penyewa: " + booking.getNamaPengguna() + " · Kamar " + booking.getKodeKamar()
                : "Silakan masukkan alasan penolakan sewa kamar.";
        JLabel lblSub = new JLabel(detailText, SwingConstants.CENTER);
        lblSub.setFont(FONT_SMALL);
        lblSub.setForeground(TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(14));

        JLabel lblQuick = new JLabel("Pilih Alasan Cepat (Klik untuk mengisi otomatis):", SwingConstants.CENTER);
        lblQuick.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblQuick.setForeground(TEXT_DARK);
        lblQuick.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblQuick);
        card.add(Box.createVerticalStrut(8));

        JTextArea txtAlasan = new JTextArea(4, 30);
        txtAlasan.setFont(FONT_BODY);
        txtAlasan.setLineWrap(true);
        txtAlasan.setWrapStyleWord(true);
        txtAlasan.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtAlasan.putClientProperty("JTextArea.placeholderText", "Ketik alasan penolakan secara lengkap di sini atau klik salah satu opsi cepat di atas...");
        txtAlasan.setText("");

        JPanel chipsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        chipsPanel.setOpaque(false);
        chipsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        chipsPanel.setPreferredSize(new Dimension(480, 84));
        chipsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 90));

        String[] quickOptions = {
                "Bukti transfer tidak valid/palsu",
                "Nominal transfer tidak sesuai",
                "Kamar telah terisi penyewa lain",
                "Data penyewa tidak dapat diverifikasi"
        };

        for (String opt : quickOptions) {
            JButton btnChip = new JButton(opt);
            btnChip.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btnChip.setForeground(new Color(0x6E, 0x6A, 0x62));
            btnChip.setBackground(new Color(0xF4, 0xEF, 0xE4));
            btnChip.setFocusPainted(false);
            btnChip.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnChip.setPreferredSize(new Dimension(230, 36));
            btnChip.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xD8, 0xD2, 0xC5), 1, true),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
            ));
            btnChip.addActionListener(e -> {
                txtAlasan.setText(opt);
                txtAlasan.requestFocusInWindow();
                txtAlasan.setCaretPosition(opt.length());
            });
            btnChip.addChangeListener(e -> {
                if (btnChip.getModel().isRollover()) {
                    btnChip.setBackground(new Color(0xFB, 0xE5, 0xDF));
                    btnChip.setForeground(DANGER_RED);
                } else {
                    btnChip.setBackground(new Color(0xF4, 0xEF, 0xE4));
                    btnChip.setForeground(new Color(0x6E, 0x6A, 0x62));
                }
            });
            chipsPanel.add(btnChip);
        }
        card.add(chipsPanel);
        card.add(Box.createVerticalStrut(14));

        JLabel lblTextTitle = new JLabel("Rincian Alasan Penolakan (Akan dibaca penyewa):", SwingConstants.CENTER);
        lblTextTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTextTitle.setForeground(TEXT_DARK);
        lblTextTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTextTitle);
        card.add(Box.createVerticalStrut(6));

        JScrollPane scroll = new JScrollPane(txtAlasan);
        scroll.setBorder(BorderFactory.createLineBorder(BORDER_DARK, 1, true));
        scroll.setPreferredSize(new Dimension(480, 100));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(scroll);
        card.add(Box.createVerticalStrut(16));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        PillButton btnCancel = new PillButton("Batalkan", PillButton.Style.OUTLINE);
        btnCancel.setPreferredSize(new Dimension(130, 38));
        btnCancel.addActionListener(e -> {
            resultReason[0] = null;
            dlg.dispose();
        });
        btnRow.add(btnCancel);

        PillButton btnConfirm = new PillButton("Tolak Booking", PillButton.Style.DANGER);
        btnConfirm.setPreferredSize(new Dimension(160, 38));
        btnConfirm.addActionListener(e -> {
            String txt = txtAlasan.getText().trim();
            if (txt.isEmpty()) {
                showWarning(dlg, "Alasan Belum Diisi", "Alasan penolakan tidak boleh kosong.\nSilakan ketik alasan atau pilih salah satu opsi cepat.");
                return;
            }
            resultReason[0] = txt;
            dlg.dispose();
        });
        btnRow.add(btnConfirm);

        card.add(btnRow);

        JScrollPane scrollCard = new JScrollPane(card);
        scrollCard.setBorder(null);
        scrollCard.getViewport().setBackground(BG_CANVAS);
        scrollCard.getVerticalScrollBar().setUnitIncrement(14);
        dlg.add(scrollCard, BorderLayout.CENTER);

        dlg.pack();
        int w = Math.max(560, dlg.getWidth() + 24);
        int h = Math.max(560, dlg.getHeight() + 24);
        dlg.setSize(w, h);
        dlg.setMinimumSize(new Dimension(540, 520));
        dlg.setResizable(true);
        dlg.setLocationRelativeTo(owner);
        dlg.setVisible(true);

        return resultReason[0];
    }

    /**
     * Membuka jendela pop-up pratinjau bukti pembayaran interaktif (Zoom In/Out, Pas Layar, 100% Asli).
     */
    public static void showImagePreview(Component parent, String title, String imagePath, String subtitle) {
        Window owner = (parent instanceof Window) ? (Window) parent : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        JDialog dlg = new JDialog(owner, title != null ? title : "Pratinjau Bukti Pembayaran", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(620, 740);
        dlg.setMinimumSize(new Dimension(500, 560));
        dlg.setResizable(true);
        dlg.setLocationRelativeTo(owner);
        dlg.getContentPane().setBackground(BG_CANVAS);
        dlg.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(BG_CARD);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16)
                )
        ));

        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        JLabel lblTag = new JLabel("▪ DOKUMEN BUKTI TRANSAKSI", SwingConstants.CENTER);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(ACCENT_TERA);
        lblTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTag);
        pnlHeader.add(Box.createVerticalStrut(4));

        JLabel lblTitle = new JLabel(title != null ? title : "Bukti Pembayaran Transfer", SwingConstants.CENTER);
        lblTitle.setFont(FONT_TITLE);
        lblTitle.setForeground(TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            pnlHeader.add(Box.createVerticalStrut(3));
            JLabel lblSub = new JLabel(subtitle, SwingConstants.CENTER);
            lblSub.setFont(FONT_SMALL);
            lblSub.setForeground(TEXT_MUTED);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlHeader.add(lblSub);
        }

        pnlHeader.add(Box.createVerticalStrut(8));

        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlToolbar.setOpaque(false);

        final BufferedImage baseImage = ImageHelper.loadOriginal(imagePath);
        final double[] zoomFactor = {1.0};
        final boolean[] isFitMode = {true};

        JLabel lblZoomInfo = new JLabel("Pas Layar");
        lblZoomInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblZoomInfo.setForeground(TEXT_DARK);
        lblZoomInfo.setPreferredSize(new Dimension(95, 26));
        lblZoomInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnZoomOut = new JButton("-");
        btnZoomOut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnZoomOut.setFocusPainted(false);
        btnZoomOut.setPreferredSize(new Dimension(36, 26));

        JButton btnFit = new JButton("Pas Layar");
        btnFit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnFit.setFocusPainted(false);
        btnFit.setPreferredSize(new Dimension(88, 26));

        JButton btnActual = new JButton("100% Asli");
        btnActual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnActual.setFocusPainted(false);
        btnActual.setPreferredSize(new Dimension(86, 26));

        JButton btnZoomIn = new JButton("+");
        btnZoomIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnZoomIn.setFocusPainted(false);
        btnZoomIn.setPreferredSize(new Dimension(36, 26));

        pnlToolbar.add(btnZoomOut);
        pnlToolbar.add(btnFit);
        pnlToolbar.add(lblZoomInfo);
        pnlToolbar.add(btnActual);
        pnlToolbar.add(btnZoomIn);

        pnlHeader.add(pnlToolbar);
        content.add(pnlHeader, BorderLayout.NORTH);

        final JScrollPane scrollImg = new JScrollPane();
        scrollImg.setBorder(BorderFactory.createLineBorder(BORDER_COLOR, 1, true));
        scrollImg.getViewport().setBackground(new Color(0xF4, 0xF1, 0xEA));
        scrollImg.getVerticalScrollBar().setUnitIncrement(14);
        scrollImg.getHorizontalScrollBar().setUnitIncrement(14);

        final JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        imageLabel.setOpaque(true);
        imageLabel.setBackground(new Color(0xF4, 0xF1, 0xEA));
        scrollImg.setViewportView(imageLabel);

        final int[] lastRenderedW = {-1};
        final int[] lastRenderedH = {-1};

        Runnable renderImage = () -> {
            if (baseImage == null) return;
            int drawW;
            int drawH;
            if (isFitMode[0]) {
                int availW = Math.max(240, scrollImg.getViewport().getWidth() - 24);
                int availH = Math.max(240, scrollImg.getViewport().getHeight() - 24);
                double scale = Math.min((double) availW / baseImage.getWidth(), (double) availH / baseImage.getHeight());
                drawW = Math.max(1, (int) (baseImage.getWidth() * scale));
                drawH = Math.max(1, (int) (baseImage.getHeight() * scale));
                lblZoomInfo.setText("Pas Layar");
            } else {
                drawW = Math.max(1, (int) (baseImage.getWidth() * zoomFactor[0]));
                drawH = Math.max(1, (int) (baseImage.getHeight() * zoomFactor[0]));
                lblZoomInfo.setText(String.format("%d%%", (int) Math.round(zoomFactor[0] * 100)));
            }
            if (drawW == lastRenderedW[0] && drawH == lastRenderedH[0]) return;
            lastRenderedW[0] = drawW;
            lastRenderedH[0] = drawH;

            BufferedImage scaled = ImageHelper.scaleProportional(baseImage, drawW, drawH, 8);
            if (scaled != null) {
                imageLabel.setIcon(new ImageIcon(scaled));
            }
            imageLabel.revalidate();
            imageLabel.repaint();
        };

        btnFit.addActionListener(e -> {
            isFitMode[0] = true;
            lastRenderedW[0] = -1;
            renderImage.run();
        });

        btnActual.addActionListener(e -> {
            isFitMode[0] = false;
            zoomFactor[0] = 1.0;
            lastRenderedW[0] = -1;
            renderImage.run();
        });

        btnZoomIn.addActionListener(e -> {
            if (baseImage == null) return;
            if (isFitMode[0]) {
                int availW = Math.max(240, scrollImg.getViewport().getWidth() - 24);
                int availH = Math.max(240, scrollImg.getViewport().getHeight() - 24);
                zoomFactor[0] = Math.min((double) availW / baseImage.getWidth(), (double) availH / baseImage.getHeight());
                isFitMode[0] = false;
            }
            zoomFactor[0] = Math.min(3.0, zoomFactor[0] + 0.2);
            lastRenderedW[0] = -1;
            renderImage.run();
        });

        btnZoomOut.addActionListener(e -> {
            if (baseImage == null) return;
            if (isFitMode[0]) {
                int availW = Math.max(240, scrollImg.getViewport().getWidth() - 24);
                int availH = Math.max(240, scrollImg.getViewport().getHeight() - 24);
                zoomFactor[0] = Math.min((double) availW / baseImage.getWidth(), (double) availH / baseImage.getHeight());
                isFitMode[0] = false;
            }
            zoomFactor[0] = Math.max(0.2, zoomFactor[0] - 0.2);
            lastRenderedW[0] = -1;
            renderImage.run();
        });

        scrollImg.getViewport().addChangeListener(e -> {
            if (isFitMode[0]) {
                renderImage.run();
            }
        });

        content.add(scrollImg, BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlBottom.setOpaque(false);

        PillButton btnClose = new PillButton("Tutup Pratinjau", PillButton.Style.OUTLINE);
        btnClose.setPreferredSize(new Dimension(150, 36));
        btnClose.addActionListener(e -> dlg.dispose());
        pnlBottom.add(btnClose);

        content.add(pnlBottom, BorderLayout.SOUTH);

        dlg.add(content, BorderLayout.CENTER);
        dlg.pack();
        dlg.setSize(Math.max(620, dlg.getWidth()), Math.max(720, dlg.getHeight()));
        dlg.setLocationRelativeTo(owner);

        SwingUtilities.invokeLater(renderImage);
        dlg.setVisible(true);
    }
}
