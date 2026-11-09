package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.models.Booking;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * RejectBookingDialog: Modal Khusus Penolakan Booking dengan Tampilan Modern.
 * - Menggantikan input dialog biasa (JOptionPane) yang hanya berupa 1-baris textfield mentah (Poin 7).
 * - Dilengkapi pilihan alasan cepat (Quick Chips) dan area teks multiline yang nyaman dan berpusat.
 * - Terintegrasi dengan KozzyDialogHelper untuk validasi tanpa JOptionPane bawaan OS.
 */
public class RejectBookingDialog extends JDialog {

    private String resultReason = null;
    private JTextArea txtAlasan;

    public RejectBookingDialog(Window owner, Booking booking) {
        super(owner, "Konfirmasi Penolakan Booking — " + (booking != null ? booking.getIdBooking() : ""), ModalityType.APPLICATION_MODAL);

        getContentPane().setBackground(UITheme.BG_CANVAS);
        setLayout(new BorderLayout());

        initUI(booking);

        pack();
        int w = Math.max(560, getWidth() + 24);
        int h = Math.max(560, getHeight() + 24);
        setSize(w, h);
        setMinimumSize(new Dimension(540, 520));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private void initUI(Booking booking) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(18, 24, 18, 24)
                )
        ));

        // Section Tag Khas Kozzy Web (▪ VERIFIKASI PENOLAKAN)
        JLabel lblSectionTag = new JLabel("▪ VERIFIKASI PENOLAKAN BOOKING", SwingConstants.CENTER);
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(UITheme.ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSectionTag);
        card.add(Box.createVerticalStrut(8));

        // 1. Header Penjelasan Terpusat Rapi
        JLabel lblTitle = new JLabel("Penolakan Pengajuan Booking", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));

        String detailText = (booking != null)
                ? "ID: " + booking.getIdBooking() + " · Penyewa: " + booking.getNamaPengguna() + " · Kamar " + booking.getKodeKamar()
                : "Silakan masukkan alasan penolakan sewa kamar.";
        JLabel lblSub = new JLabel(detailText, SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSub);
        card.add(Box.createVerticalStrut(14));

        // 2. Chip Pilihan Alasan Cepat (2 Kolom x 2 Baris Sempurna Tanpa Terpotong)
        JLabel lblQuick = new JLabel("Pilih Alasan Cepat (Klik untuk memilih):", SwingConstants.CENTER);
        lblQuick.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblQuick.setForeground(UITheme.TEXT_DARK);
        lblQuick.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblQuick);
        card.add(Box.createVerticalStrut(8));

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
            JButton btnChip = createChipButton(opt);
            chipsPanel.add(btnChip);
        }
        card.add(chipsPanel);
        card.add(Box.createVerticalStrut(14));

        // 3. JTextArea Modern Multiline (Bukan textfield biasa)
        JLabel lblTextTitle = new JLabel("Rincian Alasan Penolakan (Akan dibaca penyewa):", SwingConstants.CENTER);
        lblTextTitle.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTextTitle.setForeground(UITheme.TEXT_DARK);
        lblTextTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTextTitle);
        card.add(Box.createVerticalStrut(6));

        txtAlasan = new JTextArea(4, 30);
        txtAlasan.setFont(UITheme.FONT_BODY);
        txtAlasan.setLineWrap(true);
        txtAlasan.setWrapStyleWord(true);
        txtAlasan.setBorder(new EmptyBorder(8, 10, 8, 10));
        txtAlasan.putClientProperty("JTextArea.placeholderText", "Ketik alasan penolakan di sini atau klik salah satu pilihan cepat di atas...");
        txtAlasan.setText(""); // Bersih awal tanpa asumsi prasangka

        JScrollPane scroll = new JScrollPane(txtAlasan);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true));
        scroll.setPreferredSize(new Dimension(480, 95));
        scroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 140));
        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(scroll);
        card.add(Box.createVerticalStrut(16));

        // 4. Tombol Aksi Bawah
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        PillButton btnCancel = new PillButton("Batalkan", PillButton.Style.OUTLINE);
        btnCancel.setPreferredSize(new Dimension(130, 38));
        btnCancel.addActionListener(e -> {
            resultReason = null;
            dispose();
        });
        btnRow.add(btnCancel);

        PillButton btnConfirm = new PillButton("Tolak Booking", PillButton.Style.DANGER);
        btnConfirm.setPreferredSize(new Dimension(160, 38));
        btnConfirm.addActionListener(e -> {
            String txt = txtAlasan.getText().trim();
            if (txt.isEmpty()) {
                KozzyDialogHelper.showWarning(this, "Alasan Belum Diisi", "Alasan penolakan tidak boleh kosong.\nSilakan ketik alasan atau pilih salah satu opsi cepat.");
                return;
            }
            resultReason = txt;
            dispose();
        });
        btnRow.add(btnConfirm);

        card.add(btnRow);

        JScrollPane scrollCard = new JScrollPane(card);
        scrollCard.setBorder(null);
        scrollCard.getViewport().setBackground(UITheme.BG_CANVAS);
        scrollCard.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollCard, BorderLayout.CENTER);
    }

    private JButton createChipButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        btn.setForeground(new Color(0x6E, 0x6A, 0x62));
        btn.setBackground(new Color(0xF4, 0xEF, 0xE4));
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(new Dimension(230, 36));
        btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD8, 0xD2, 0xC5), 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        btn.addActionListener(e -> txtAlasan.setText(text));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(new Color(0xFB, 0xE5, 0xDF));
                btn.setForeground(UITheme.DANGER_RED);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(new Color(0xF4, 0xEF, 0xE4));
                btn.setForeground(new Color(0x6E, 0x6A, 0x62));
            }
        });
        return btn;
    }

    public static String showDialog(Window owner, Booking booking) {
        RejectBookingDialog dlg = new RejectBookingDialog(owner, booking);
        dlg.setVisible(true);
        return dlg.resultReason;
    }
}
