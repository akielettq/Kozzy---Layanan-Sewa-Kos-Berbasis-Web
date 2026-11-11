package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.BookingController;
import com.kozzy.models.Booking;
import com.kozzy.models.Pengguna;
import com.kozzy.services.SessionManager;
import com.kozzy.views.components.ImageHelper;
import com.kozzy.views.components.PillBadge;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * DesktopSewakuView: Tampilan Status Sewa Aktif & Riwayat Pemesanan Penghuni.
 * - Sisi Kiri: Kartu Status Sewa Berjalan Lengkap (Foto Kamar, Periode Sewa, Panduan Hunian).
 * - Sisi Kanan: Tabel Riwayat Seluruh Booking + Panel Rincian Keputusan Verifikasi & Pratinjau Bukti.
 * - Tata letak proporsional tanpa ruang kosong berlebih.
 */
public class DesktopSewakuView extends JPanel {

    public interface SewakuActionCallback {
        void onExploreRooms();
        void onLoginRequested();
    }

    private final SewakuActionCallback callback;
    private final BookingController controller = new BookingController();

    private JPanel pnlActiveRental;
    private JTable bookingTable;
    private DefaultTableModel tableModel;
    private List<Booking> userBookings = new ArrayList<>();
    private JLabel lblDetailNote;
    private PillButton btnLihatBuktiRiwayat;

    public DesktopSewakuView(SewakuActionCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // ==========================================
        // 1. HEADER
        // ==========================================
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Status Sewaku & Riwayat Booking");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        titleBox.add(lblTitle);

        JLabel lblSub = new JLabel("Pantau status hunian aktif serta seluruh riwayat pengajuan pemesanan kamar kosmu.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        // ==========================================
        // 2. SPLIT CONTENT (SEWA AKTIF vs RIWAYAT - 50% / 50%)
        // ==========================================
        JPanel splitContainer = new JPanel(new GridLayout(1, 2, 14, 0));
        splitContainer.setOpaque(false);

        // PANEL KIRI: KARTU SEWA AKTIF
        pnlActiveRental = new JPanel(new BorderLayout());
        pnlActiveRental.setBackground(UITheme.BG_CARD);
        pnlActiveRental.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(16, 18, 16, 18)
        ));
        splitContainer.add(pnlActiveRental);

        // PANEL KANAN: TABEL RIWAYAT BOOKING
        JPanel pnlRiwayat = new JPanel(new BorderLayout(0, 10));
        pnlRiwayat.setBackground(UITheme.BG_CARD);
        pnlRiwayat.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblRTitle = new JLabel("Riwayat Pengajuan Booking");
        lblRTitle.setFont(UITheme.FONT_TITLE);
        lblRTitle.setForeground(UITheme.TEXT_DARK);
        pnlRiwayat.add(lblRTitle, BorderLayout.NORTH);

        String[] cols = {"ID", "KAMAR", "MULAI", "DURASI", "STATUS", "BIAYA"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };

        bookingTable = new JTable(tableModel);
        bookingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        bookingTable.setShowGrid(false);
        bookingTable.setShowHorizontalLines(true);
        bookingTable.setGridColor(new Color(0xEE, 0xEA, 0xE0));
        bookingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        bookingTable.setFillsViewportHeight(true);
        bookingTable.setRowHeight(38);

        bookingTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        bookingTable.getColumnModel().getColumn(1).setPreferredWidth(65);
        bookingTable.getColumnModel().getColumn(2).setPreferredWidth(80);
        bookingTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        bookingTable.getColumnModel().getColumn(4).setPreferredWidth(85);
        bookingTable.getColumnModel().getColumn(5).setPreferredWidth(95);
        bookingTable.getColumnModel().getColumn(4).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                String str = (val != null) ? val.toString() : "";
                PillBadge b = PillBadge.createStatusBadge(str);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 4));
                p.setOpaque(true);
                p.setBackground(isSel ? table.getSelectionBackground() : table.getBackground());
                p.add(b);
                return p;
            }
        });

        bookingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = bookingTable.getSelectedRow();
                if (row >= 0 && row < userBookings.size()) {
                    showBookingDetailNote(userBookings.get(row));
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(bookingTable);
        scrollTable.setBorder(null);
        scrollTable.getViewport().setBackground(UITheme.BG_CARD);
        pnlRiwayat.add(scrollTable, BorderLayout.CENTER);

        // Panel Catatan / Keputusan di Bawah Tabel
        JPanel pnlNote = new JPanel(new BorderLayout(8, 0));
        pnlNote.setOpaque(false);
        pnlNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lblDetailNote = new JLabel("Pilih salah satu baris booking di atas untuk melihat catatan verifikasi admin.");
        lblDetailNote.setFont(UITheme.FONT_SMALL);
        lblDetailNote.setForeground(UITheme.TEXT_MUTED);
        pnlNote.add(lblDetailNote, BorderLayout.CENTER);

        btnLihatBuktiRiwayat = new PillButton("Lihat Bukti", PillButton.Style.OUTLINE);
        btnLihatBuktiRiwayat.setPreferredSize(new Dimension(105, 32));
        btnLihatBuktiRiwayat.setEnabled(false);
        btnLihatBuktiRiwayat.addActionListener(e -> {
            int row = bookingTable.getSelectedRow();
            if (row >= 0 && row < userBookings.size()) {
                Booking sel = userBookings.get(row);
                UITheme.showImagePreview(
                        this,
                        "Bukti Pembayaran — " + sel.getIdBooking(),
                        sel.getFileBukti(),
                        "Kamar " + sel.getKodeKamar() + " · " + sel.getDurasi() + " Bulan"
                );
            }
        });
        pnlNote.add(btnLihatBuktiRiwayat, BorderLayout.EAST);

        pnlRiwayat.add(pnlNote, BorderLayout.SOUTH);

        splitContainer.add(pnlRiwayat);
        add(splitContainer, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            renderUnauthenticatedState();
            return;
        }

        userBookings = controller.getUserBookings(user.getIdPengguna());

        // Update Tabel Riwayat
        tableModel.setRowCount(0);
        Booking activeBooking = null;
        Booking fallbackApproved = null;
        LocalDate now = LocalDate.now();

        for (Booking b : userBookings) {
            tableModel.addRow(new Object[]{
                    b.getIdBooking(),
                    b.getKodeKamar(),
                    (b.getTanggalMulai() != null ? b.getTanggalMulai().toString() : "-"),
                    b.getDurasi() + " Bulan",
                    b.getStatus(),
                    String.format("Rp %,d", b.hitungTotalBiaya()).replace(',', '.')
            });

            // Cek booking aktif berjalan (konsisten dengan ProfilController & BookingService)
            if ("approved".equalsIgnoreCase(b.getStatus()) || "disetujui".equalsIgnoreCase(b.getStatus())) {
                if (fallbackApproved == null) {
                    fallbackApproved = b;
                }
                if (activeBooking == null && b.hitungTanggalSelesai() != null && !b.hitungTanggalSelesai().toLocalDate().isBefore(now)) {
                    activeBooking = b;
                }
            }
        }
        if (activeBooking == null) {
            activeBooking = fallbackApproved;
        }

        // Render Sisi Kiri (Sewa Aktif)
        renderActiveRental(activeBooking);

        if (!userBookings.isEmpty()) {
            bookingTable.setRowSelectionInterval(0, 0);
            showBookingDetailNote(userBookings.get(0));
        } else {
            lblDetailNote.setText("Belum ada transaksi pemesanan kamar yang diajukan.");
            if (btnLihatBuktiRiwayat != null) btnLihatBuktiRiwayat.setEnabled(false);
        }

        revalidate();
        repaint();
    }

    private void renderActiveRental(Booking b) {
        pnlActiveRental.removeAll();

        if (b == null) {
            // Empty State
            JPanel empty = new JPanel();
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setOpaque(false);
            empty.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

            JLabel lblEmptyIcon = new JLabel("🛏️");
            lblEmptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            lblEmptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(lblEmptyIcon);
            empty.add(Box.createVerticalStrut(14));

            JLabel lblEmpty = new JLabel("Tidak Ada Sewa Kos Aktif Saat Ini");
            lblEmpty.setFont(UITheme.FONT_TITLE);
            lblEmpty.setForeground(UITheme.TEXT_DARK);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(lblEmpty);
            empty.add(Box.createVerticalStrut(6));

            JLabel lblSub = new JLabel("<html><center style='color:#6E6A62;'>Pilih kamar kos impianmu sekarang dan ajukan pemesanan secara mudah.</center></html>");
            lblSub.setFont(UITheme.FONT_BODY);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(lblSub);
            empty.add(Box.createVerticalStrut(20));

            PillButton btnCari = new PillButton("Cari & Booking Kamar", PillButton.Style.PRIMARY);
            btnCari.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnCari.addActionListener(e -> {
                if (callback != null) callback.onExploreRooms();
            });
            empty.add(btnCari);

            pnlActiveRental.add(empty, BorderLayout.CENTER);
        } else {
            // 1. Header Sewa Aktif (North)
            JPanel topRow = new JPanel(new BorderLayout());
            topRow.setOpaque(false);
            topRow.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));

            JLabel lblH = new JLabel("SEWA AKTIF SAAT INI");
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblH.setForeground(UITheme.PRIMARY_GREEN);
            topRow.add(lblH, BorderLayout.WEST);

            JPanel badgeWrapper = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            badgeWrapper.setOpaque(false);
            badgeWrapper.add(PillBadge.createStatusBadge("APPROVED"));
            topRow.add(badgeWrapper, BorderLayout.EAST);
            pnlActiveRental.add(topRow, BorderLayout.NORTH);

            // 2. Konten Detail Sewa (Center)
            JPanel centerContent = new JPanel();
            centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
            centerContent.setOpaque(false);

            // Foto Kamar
            String photoPath = "assets/kamar/" + b.getKodeKamar() + ".jpg";
            BufferedImage img = ImageHelper.loadAndScale(photoPath, 380, 165, 8);
            JLabel lblPhoto = new JLabel(new ImageIcon(img));
            lblPhoto.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerContent.add(lblPhoto);
            centerContent.add(Box.createVerticalStrut(10));

            // Info Kamar & Periode
            JLabel lblKamar = new JLabel("Kamar " + b.getKodeKamar());
            lblKamar.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblKamar.setForeground(UITheme.TEXT_DARK);
            lblKamar.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerContent.add(lblKamar);
            centerContent.add(Box.createVerticalStrut(4));

            JLabel lblPeriode = new JLabel("Masa Sewa: " + b.getTanggalMulai() + " s.d. " + b.hitungTanggalSelesai());
            lblPeriode.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPeriode.setForeground(UITheme.ACCENT_TERA);
            lblPeriode.setAlignmentX(Component.LEFT_ALIGNMENT);
            centerContent.add(lblPeriode);
            centerContent.add(Box.createVerticalStrut(10));

            // Detail Pembayaran
            JPanel pnlMeta = new JPanel(new GridLayout(4, 2, 8, 6));
            pnlMeta.setOpaque(false);
            pnlMeta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            pnlMeta.setAlignmentX(Component.LEFT_ALIGNMENT);

            pnlMeta.add(createMetaLabel("Nomor Booking:"));
            pnlMeta.add(createMetaValue(b.getIdBooking()));

            pnlMeta.add(createMetaLabel("Metode Bayar:"));
            pnlMeta.add(createMetaValue(b.getNamaMetode() != null ? b.getNamaMetode() : "Transfer Bank"));

            pnlMeta.add(createMetaLabel("Total Dibayar:"));
            pnlMeta.add(createMetaValue(String.format("Rp %,d", b.hitungTotalBiaya()).replace(',', '.')));

            pnlMeta.add(createMetaLabel("Bukti Transfer:"));
            PillButton btnBuktiAktif = new PillButton("Lihat Berkas", PillButton.Style.OUTLINE);
            // Padding default PillButton (8/16) membuat teks terpotong pada ukuran 100x26,
            // jadi gunakan padding lebih kecil dan biarkan lebar mengikuti teks.
            btnBuktiAktif.setBorder(BorderFactory.createEmptyBorder(4, 14, 4, 14));
            btnBuktiAktif.addActionListener(e -> {
                UITheme.showImagePreview(
                        this,
                        "Bukti Pembayaran — " + b.getIdBooking(),
                        b.getFileBukti(),
                        "Kamar " + b.getKodeKamar() + " · " + b.getDurasi() + " Bulan"
                );
            });
            JPanel pnlBtnWrap = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
            pnlBtnWrap.setOpaque(false);
            pnlBtnWrap.add(btnBuktiAktif);
            pnlMeta.add(pnlBtnWrap);
            // Batas tinggi mengikuti isi (sebelumnya dikunci 110px sehingga baris terakhir terpotong).
            pnlMeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlMeta.getPreferredSize().height));

            centerContent.add(pnlMeta);
            centerContent.add(Box.createVerticalStrut(10));

            // Kartu Panduan Hunian & Bantuan (Mengisi Ruang Tanpa Space Kosong)
            JPanel pnlInfoHunian = new JPanel(new GridLayout(2, 1, 0, 2));
            pnlInfoHunian.setBackground(UITheme.BG_CARD_ALT);
            pnlInfoHunian.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            pnlInfoHunian.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel lblInfo1 = new JLabel("• Kunci fisik dapat diambil di pos pengelola atau hubungi pengurus kos.");
            lblInfo1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblInfo1.setForeground(UITheme.TEXT_DARK);
            pnlInfoHunian.add(lblInfo1);

            JLabel lblInfo2 = new JLabel("• WiFi Kost: 'Kozzy_Resident_5G' · Sandi tersedia di kamar.");
            lblInfo2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblInfo2.setForeground(UITheme.PRIMARY_GREEN);
            pnlInfoHunian.add(lblInfo2);
            // Sama seperti pnlMeta: tinggi mengikuti isi agar teks tidak terpotong (sebelumnya dikunci 50px).
            pnlInfoHunian.setMaximumSize(new Dimension(Integer.MAX_VALUE, pnlInfoHunian.getPreferredSize().height));

            centerContent.add(pnlInfoHunian);

            pnlActiveRental.add(centerContent, BorderLayout.CENTER);
        }

        pnlActiveRental.revalidate();
        pnlActiveRental.repaint();
    }

    private JLabel createMetaLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private JLabel createMetaValue(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

    private void showBookingDetailNote(Booking b) {
        if (b == null) return;
        if (btnLihatBuktiRiwayat != null) btnLihatBuktiRiwayat.setEnabled(true);
        StringBuilder sb = new StringBuilder("<html><body style='font-size:10.5px; width:360px;'>");
        sb.append("<b>").append(b.getIdBooking()).append("</b> · Kamar: <b>").append(b.getKodeKamar()).append("</b>");
        sb.append(" · Status: <b>").append(b.getStatus().toUpperCase()).append("</b>");

        if ("rejected".equalsIgnoreCase(b.getStatus())) {
            String alasan = b.getAlasan() != null ? b.getAlasan() : "Bukti pembayaran tidak sesuai.";
            String safeAlasan = alasan
                    .replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\n", "<br>");
            sb.append("<br><span style='color:#C63624;'>Alasan Penolakan: ").append(safeAlasan).append("</span>");
        } else if ("approved".equalsIgnoreCase(b.getStatus())) {
            sb.append("<br><span style='color:#1F5B41;'>Pembayaran sah terverifikasi. Kunci fisik dapat diambil di pengelola kos.</span>");
        } else {
            sb.append("<br><span style='color:#B47514;'>Menunggu admin memverifikasi bukti pembayaranmu.</span>");
        }
        sb.append("</body></html>");
        lblDetailNote.setText(sb.toString());
    }

    private void renderUnauthenticatedState() {
        pnlActiveRental.removeAll();
        tableModel.setRowCount(0);
        if (btnLihatBuktiRiwayat != null) btnLihatBuktiRiwayat.setEnabled(false);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(60, 20, 20, 20));

        JLabel lIcon = new JLabel("🔒");
        lIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        lIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lIcon);
        p.add(Box.createVerticalStrut(10));

        JLabel lMsg = new JLabel("Silakan Masuk Terlebih Dahulu");
        lMsg.setFont(UITheme.FONT_TITLE);
        lMsg.setForeground(UITheme.TEXT_DARK);
        lMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lMsg);
        p.add(Box.createVerticalStrut(16));

        PillButton btnLogin = new PillButton("Masuk ke Akun", PillButton.Style.PRIMARY);
        btnLogin.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnLogin.addActionListener(e -> {
            if (callback != null) callback.onLoginRequested();
        });
        p.add(btnLogin);

        pnlActiveRental.add(p, BorderLayout.CENTER);
        lblDetailNote.setText("Masuk ke akunmu untuk melihat riwayat pengajuan sewa.");

        revalidate();
        repaint();
    }
}
