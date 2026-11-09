package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.BookingController;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
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
 * - Sisi Kiri: Kartu Status Sewa Berjalan Lengkap (Foto Kamar, Periode Sewa, Ketentuan Hunian).
 * - Sisi Kanan: Tabel Riwayat Seluruh Booking + Panel Rincian Keputusan Verifikasi.
 * - Seluruh teks informasi ditata rata tengah elegan (Poin 4).
 * - Tata letak proporsional tanpa void/space kosong menganga (Poin 2).
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
        // 1. HEADER (Rata Tengah - Poin 4)
        // ==========================================
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        // Section Tag Khas Kozzy Web (▪ STATUS SEWA & RIWAYAT)
        JLabel lblSectionTag = new JLabel("▪ STATUS SEWA & RIWAYAT", SwingConstants.CENTER);
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(UITheme.ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblSectionTag);
        titleBox.add(Box.createVerticalStrut(2));

        JLabel lblTitle = new JLabel("Status Sewaku & Riwayat Booking", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));

        JLabel lblSub = new JLabel("Pantau status hunian aktif serta seluruh riwayat pengajuan pemesanan kamar kosmu.", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.CENTER);
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

        JLabel lblRTitle = new JLabel("Riwayat Pengajuan Booking", SwingConstants.CENTER);
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

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < cols.length; i++) {
            if (i != 4) {
                bookingTable.getColumnModel().getColumn(i).setCellRenderer(centerRender);
            }
        }

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

        bookingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int row = bookingTable.getSelectedRow();
                    if (row >= 0 && row < userBookings.size()) {
                        Booking sel = userBookings.get(row);
                        KozzyDialogHelper.showImagePreview(
                            SwingUtilities.getWindowAncestor(DesktopSewakuView.this),
                            "Bukti Pembayaran — " + sel.getIdBooking(),
                            sel.getFileBukti(),
                            "Kamar " + sel.getKodeKamar() + " · " + sel.getDurasi() + " Bulan"
                        );
                    }
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(bookingTable);
        scrollTable.setBorder(null);
        scrollTable.getViewport().setBackground(UITheme.BG_CARD);
        pnlRiwayat.add(scrollTable, BorderLayout.CENTER);

        // Panel Catatan / Keputusan di Bawah Tabel (Rata Tengah)
        JPanel pnlNote = new JPanel(new BorderLayout(8, 0));
        pnlNote.setOpaque(false);
        pnlNote.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));

        lblDetailNote = new JLabel("Pilih salah satu baris booking di atas untuk melihat catatan verifikasi admin.", SwingConstants.CENTER);
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
                KozzyDialogHelper.showImagePreview(
                    SwingUtilities.getWindowAncestor(this),
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

            // Cek booking aktif berjalan
            if (activeBooking == null && "approved".equalsIgnoreCase(b.getStatus())) {
                if (b.hitungTanggalSelesai() != null && !b.hitungTanggalSelesai().toLocalDate().isBefore(now)) {
                    activeBooking = b;
                }
            }
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
            // Empty State (Rata Tengah - Poin 4)
            JPanel empty = new JPanel();
            empty.setLayout(new BoxLayout(empty, BoxLayout.Y_AXIS));
            empty.setOpaque(false);
            empty.setBorder(BorderFactory.createEmptyBorder(40, 20, 20, 20));

            JLabel lblEmptyIcon = new JLabel("🛏️", SwingConstants.CENTER);
            lblEmptyIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 48));
            lblEmptyIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(lblEmptyIcon);
            empty.add(Box.createVerticalStrut(14));

            JLabel lblEmpty = new JLabel("Tidak Ada Sewa Kos Aktif Saat Ini", SwingConstants.CENTER);
            lblEmpty.setFont(UITheme.FONT_TITLE);
            lblEmpty.setForeground(UITheme.TEXT_DARK);
            lblEmpty.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(lblEmpty);
            empty.add(Box.createVerticalStrut(6));

            JLabel lblSub = new JLabel("<html><center style='color:#6E6A62;'>Pilih kamar kos impianmu sekarang dan ajukan pemesanan secara mudah.</center></html>", SwingConstants.CENTER);
            lblSub.setFont(UITheme.FONT_BODY);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
            empty.add(lblSub);
            empty.add(Box.createVerticalStrut(20));

            PillButton btnCari = new PillButton("Cari & Booking Kamar →", PillButton.Style.PRIMARY);
            btnCari.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnCari.addActionListener(e -> {
                if (callback != null) callback.onExploreRooms();
            });
            empty.add(btnCari);

            pnlActiveRental.add(empty, BorderLayout.CENTER);
        } else {
            // Tampilan Sewa Aktif Berjalan (Rata Tengah Lengkap - Poin 4)
            JPanel centerContent = new JPanel();
            centerContent.setLayout(new BoxLayout(centerContent, BoxLayout.Y_AXIS));
            centerContent.setOpaque(false);

            // 1. Header Sewa Aktif (Rata Tengah)
            JPanel topRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
            topRow.setOpaque(false);
            topRow.setAlignmentX(Component.CENTER_ALIGNMENT);

            JLabel lblH = new JLabel("SEWA AKTIF SAAT INI", SwingConstants.CENTER);
            lblH.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblH.setForeground(UITheme.PRIMARY_GREEN);
            topRow.add(lblH);

            topRow.add(PillBadge.createStatusBadge("APPROVED"));
            centerContent.add(topRow);
            centerContent.add(Box.createVerticalStrut(10));

            // 2. Foto Kamar (Rata Tengah)
            String photoPath = "assets/kamar/" + b.getKodeKamar() + ".jpg";
            BufferedImage img = ImageHelper.loadAndScale(photoPath, 380, 160, 8);
            JLabel lblPhoto = new JLabel(new ImageIcon(img), SwingConstants.CENTER);
            lblPhoto.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerContent.add(lblPhoto);
            centerContent.add(Box.createVerticalStrut(10));

            // 3. Info Kamar & Periode (Rata Tengah)
            JLabel lblKamar = new JLabel("Kamar " + b.getKodeKamar(), SwingConstants.CENTER);
            lblKamar.setFont(new Font("Segoe UI", Font.BOLD, 20));
            lblKamar.setForeground(UITheme.TEXT_DARK);
            lblKamar.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerContent.add(lblKamar);
            centerContent.add(Box.createVerticalStrut(4));

            JLabel lblPeriode = new JLabel("Masa Sewa: " + b.getTanggalMulai() + " s.d. " + b.hitungTanggalSelesai(), SwingConstants.CENTER);
            lblPeriode.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblPeriode.setForeground(UITheme.ACCENT_TERA);
            lblPeriode.setAlignmentX(Component.CENTER_ALIGNMENT);
            centerContent.add(lblPeriode);
            centerContent.add(Box.createVerticalStrut(10));

            // 4. Detail Pembayaran Grid (Rata Tengah)
            JPanel pnlMeta = new JPanel(new GridLayout(4, 2, 8, 6));
            pnlMeta.setOpaque(false);
            pnlMeta.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 12, 8, 12)
            ));
            pnlMeta.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlMeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

            pnlMeta.add(createMetaLabel("Nomor Booking:"));
            pnlMeta.add(createMetaValue(b.getIdBooking()));

            pnlMeta.add(createMetaLabel("Metode Bayar:"));
            pnlMeta.add(createMetaValue(b.getNamaMetode() != null ? b.getNamaMetode() : "Transfer Bank"));

            pnlMeta.add(createMetaLabel("Total Dibayar:"));
            pnlMeta.add(createMetaValue(String.format("Rp %,d", b.hitungTotalBiaya()).replace(',', '.')));

            pnlMeta.add(createMetaLabel("Bukti Transfer:"));
            PillButton btnBuktiAktif = new PillButton("Lihat Berkas", PillButton.Style.OUTLINE);
            btnBuktiAktif.setPreferredSize(new Dimension(100, 26));
            btnBuktiAktif.addActionListener(e -> {
                KozzyDialogHelper.showImagePreview(
                    SwingUtilities.getWindowAncestor(this),
                    "Bukti Pembayaran — " + b.getIdBooking(),
                    b.getFileBukti(),
                    "Kamar " + b.getKodeKamar() + " · " + b.getDurasi() + " Bulan"
                );
            });
            JPanel pnlBtnWrap = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
            pnlBtnWrap.setOpaque(false);
            pnlBtnWrap.add(btnBuktiAktif);
            pnlMeta.add(pnlBtnWrap);

            centerContent.add(pnlMeta);
            centerContent.add(Box.createVerticalStrut(10));

            // 5. Kartu Panduan Hunian & Bantuan (Mengisi Ruang tanpa Void - Poin 2)
            JPanel pnlInfoHunian = new JPanel(new GridLayout(2, 1, 0, 2));
            pnlInfoHunian.setBackground(UITheme.BG_CARD_ALT);
            pnlInfoHunian.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
            ));
            pnlInfoHunian.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlInfoHunian.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

            JLabel lblInfo1 = new JLabel("• Kunci Fisik dapat diambil di pos pengelola atau hubungi pengurus kos.", SwingConstants.CENTER);
            lblInfo1.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblInfo1.setForeground(UITheme.TEXT_DARK);
            pnlInfoHunian.add(lblInfo1);

            JLabel lblInfo2 = new JLabel("• WiFi Kost: 'Kozzy_Resident_5G' · Sandi tersedia di kamar.", SwingConstants.CENTER);
            lblInfo2.setFont(new Font("Segoe UI", Font.BOLD, 10));
            lblInfo2.setForeground(UITheme.PRIMARY_GREEN);
            pnlInfoHunian.add(lblInfo2);

            centerContent.add(pnlInfoHunian);

            pnlActiveRental.add(centerContent, BorderLayout.CENTER);
        }

        pnlActiveRental.revalidate();
        pnlActiveRental.repaint();
    }

    private JLabel createMetaLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_MUTED);
        return l;
    }

    private JLabel createMetaValue(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(new Font("Segoe UI", Font.BOLD, 12));
        l.setForeground(UITheme.TEXT_DARK);
        return l;
    }

    private void showBookingDetailNote(Booking b) {
        if (b == null) return;
        if (btnLihatBuktiRiwayat != null) btnLihatBuktiRiwayat.setEnabled(true);
        StringBuilder sb = new StringBuilder("<html><body style='font-size:10.5px; width:440px; text-align:center;'><center>");
        sb.append("<b>").append(b.getIdBooking()).append("</b> · Kamar: <b>").append(b.getKodeKamar()).append("</b>");
        sb.append(" · Status: <b>").append(b.getStatus().toUpperCase()).append("</b>");

        if ("rejected".equalsIgnoreCase(b.getStatus())) {
            String alasan = b.getAlasan() != null ? b.getAlasan() : "Bukti pembayaran tidak sesuai.";
            sb.append("<br><span style='color:#C63624;'>Alasan Penolakan: ").append(alasan).append("</span>");
        } else if ("approved".equalsIgnoreCase(b.getStatus())) {
            sb.append("<br><span style='color:#1F5B41;'>Pembayaran sah terverifikasi. Kunci fisik dapat diambil di pengelola kos.</span>");
        } else {
            sb.append("<br><span style='color:#B47514;'>Menunggu admin memverifikasi bukti pembayaranmu.</span>");
        }
        sb.append("</center></body></html>");
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

        JLabel lIcon = new JLabel("🔒", SwingConstants.CENTER);
        lIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 44));
        lIcon.setAlignmentX(Component.CENTER_ALIGNMENT);
        p.add(lIcon);
        p.add(Box.createVerticalStrut(10));

        JLabel lMsg = new JLabel("Silakan Masuk Terlebih Dahulu", SwingConstants.CENTER);
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
