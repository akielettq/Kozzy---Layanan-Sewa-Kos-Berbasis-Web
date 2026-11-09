package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.AdminController;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
import com.kozzy.views.components.ImageHelper;
import com.kozzy.views.components.PillBadge;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DesktopAdminView: Konsol Manajemen Operasional Admin Berorientasi Desktop Workstation.
 * - Sisi Kiri: Verifikasi Pengajuan Booking (Tabel Pending, Preview Bukti Transfer, Tombol Setujui/Tolak).
 * - Sisi Kanan: Pengendalian Status Ketersediaan Kamar (Dropdown Tersedia / Perbaikan / Terisi).
 * - Seluruh teks informasi dan header ditata terpusat (Poin 4).
 * - Seluruh notifikasi & pop-up menggunakan KozzyDialogHelper (Poin 7).
 */
public class DesktopAdminView extends JPanel {

    private final AdminController controller = new AdminController();

    // Sisi Kiri: Verifikasi
    private JTable pendingTable;
    private DefaultTableModel pendingModel;
    private List<Booking> pendingList = new ArrayList<>();
    private Booking selectedBooking;

    private JLabel lblTenantName;
    private JLabel lblTenantDetail;
    private JLabel lblBuktiThumb;
    private PillButton btnLihatBukti;
    private PillButton btnSetujui;
    private PillButton btnTolak;

    // Sisi Kanan: Kamar Controller
    private JPanel pnlRoomList;

    public DesktopAdminView() {
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

        // Section Tag Khas Kozzy Web (▪ KONSOL ADMINISTRATOR)
        JLabel lblSectionTag = new JLabel("▪ KONSOL ADMINISTRATOR", SwingConstants.CENTER);
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(UITheme.ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblSectionTag);
        titleBox.add(Box.createVerticalStrut(2));

        JLabel lblTitle = new JLabel("Panel Manajemen & Verifikasi Admin", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblTitle);
        titleBox.add(Box.createVerticalStrut(2));

        JLabel lblSub = new JLabel("Verifikasi booking sewa masuk dan kendalikan status ketersediaan kamar kos.", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.CENTER);
        add(header, BorderLayout.NORTH);

        // ==========================================
        // 2. SPLIT WORKSTATION (50% / 50%)
        // ==========================================
        JPanel splitContainer = new JPanel(new GridLayout(1, 2, 14, 0));
        splitContainer.setOpaque(false);

        // PANEL KIRI: VERIFIKASI BOOKING
        JPanel pnlVerif = new JPanel(new BorderLayout(0, 10));
        pnlVerif.setBackground(UITheme.BG_CARD);
        pnlVerif.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblVTitle = new JLabel("Pengajuan Booking Menunggu Persetujuan", SwingConstants.CENTER);
        lblVTitle.setFont(UITheme.FONT_TITLE);
        lblVTitle.setForeground(UITheme.TEXT_DARK);
        pnlVerif.add(lblVTitle, BorderLayout.NORTH);

        // Tabel Pending
        String[] cols = {"ID", "PENYEWA", "GENDER", "KAMAR", "DURASI", "METODE"};
        pendingModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        pendingTable = new JTable(pendingModel);
        pendingTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        pendingTable.setShowGrid(false);
        pendingTable.setShowHorizontalLines(true);
        pendingTable.setGridColor(new Color(0xEE, 0xEA, 0xE0));
        pendingTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pendingTable.setFillsViewportHeight(true);

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < pendingTable.getColumnCount(); i++) {
            pendingTable.getColumnModel().getColumn(i).setCellRenderer(centerRender);
        }

        pendingTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        pendingTable.getColumnModel().getColumn(1).setPreferredWidth(95);
        pendingTable.getColumnModel().getColumn(2).setPreferredWidth(65);
        pendingTable.getColumnModel().getColumn(3).setPreferredWidth(55);
        pendingTable.getColumnModel().getColumn(4).setPreferredWidth(60);
        pendingTable.getColumnModel().getColumn(5).setPreferredWidth(80);

        pendingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = pendingTable.getSelectedRow();
                if (row >= 0 && row < pendingList.size()) {
                    selectPendingBooking(pendingList.get(row));
                }
            }
        });

        pendingTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && selectedBooking != null) {
                    showBuktiPreview(selectedBooking);
                }
            }
        });

        JScrollPane scrollPending = new JScrollPane(pendingTable);
        scrollPending.setPreferredSize(new Dimension(0, 150));
        scrollPending.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        scrollPending.getViewport().setBackground(UITheme.BG_CARD);
        pnlVerif.add(scrollPending, BorderLayout.CENTER);

        // Preview Box Pengajuan Terpilih di Bawah Tabel
        JPanel previewBox = new JPanel(new BorderLayout(12, 0));
        previewBox.setOpaque(false);
        previewBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Kiri Preview: Info Teks & Tombol Aksi (Rata Tengah)
        JPanel pnlPreviewText = new JPanel();
        pnlPreviewText.setLayout(new BoxLayout(pnlPreviewText, BoxLayout.Y_AXIS));
        pnlPreviewText.setOpaque(false);

        lblTenantName = new JLabel("Pilih pengajuan booking di atas", SwingConstants.CENTER);
        lblTenantName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTenantName.setForeground(UITheme.TEXT_DARK);
        lblTenantName.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlPreviewText.add(lblTenantName);
        pnlPreviewText.add(Box.createVerticalStrut(4));

        lblTenantDetail = new JLabel("-", SwingConstants.CENTER);
        lblTenantDetail.setFont(UITheme.FONT_SMALL);
        lblTenantDetail.setForeground(UITheme.TEXT_MUTED);
        lblTenantDetail.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlPreviewText.add(lblTenantDetail);
        pnlPreviewText.add(Box.createVerticalStrut(10));

        JPanel pnlBtnAction = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlBtnAction.setOpaque(false);
        pnlBtnAction.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLihatBukti = new PillButton("Lihat Bukti", PillButton.Style.OUTLINE);
        btnLihatBukti.setEnabled(false);
        btnLihatBukti.setPreferredSize(new Dimension(100, 32));
        btnLihatBukti.addActionListener(e -> {
            if (selectedBooking != null) {
                showBuktiPreview(selectedBooking);
            }
        });
        pnlBtnAction.add(btnLihatBukti);

        btnSetujui = new PillButton("Setujui", PillButton.Style.PRIMARY);
        btnSetujui.setEnabled(false);
        btnSetujui.setPreferredSize(new Dimension(95, 32));
        btnSetujui.addActionListener(e -> prosesSetujui());
        pnlBtnAction.add(btnSetujui);

        btnTolak = new PillButton("Tolak", PillButton.Style.DANGER);
        btnTolak.setEnabled(false);
        btnTolak.setPreferredSize(new Dimension(90, 32));
        btnTolak.addActionListener(e -> prosesTolak());
        pnlBtnAction.add(btnTolak);

        pnlPreviewText.add(pnlBtnAction);
        previewBox.add(pnlPreviewText, BorderLayout.CENTER);

        // Kanan Preview: Thumbnail Bukti Bayar
        JPanel pnlThumb = new JPanel(new BorderLayout());
        pnlThumb.setOpaque(false);
        pnlThumb.setPreferredSize(new Dimension(100, 100));

        lblBuktiThumb = new JLabel("Tidak Ada Foto", SwingConstants.CENTER);
        lblBuktiThumb.setFont(UITheme.FONT_SMALL);
        lblBuktiThumb.setForeground(UITheme.TEXT_MUTED);
        lblBuktiThumb.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true));
        lblBuktiThumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBuktiThumb.setToolTipText("Klik untuk memperbesar bukti pembayaran");
        lblBuktiThumb.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectedBooking != null) {
                    showBuktiPreview(selectedBooking);
                }
            }
        });
        pnlThumb.add(lblBuktiThumb, BorderLayout.CENTER);
        previewBox.add(pnlThumb, BorderLayout.EAST);

        pnlVerif.add(previewBox, BorderLayout.SOUTH);
        splitContainer.add(pnlVerif);

        // PANEL KANAN: KONTROL STATUS KAMAR
        JPanel pnlKamarCtrl = new JPanel(new BorderLayout(0, 10));
        pnlKamarCtrl.setBackground(UITheme.BG_CARD);
        pnlKamarCtrl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblKTitle = new JLabel("Pengendalian Status Ketersediaan Kamar", SwingConstants.CENTER);
        lblKTitle.setFont(UITheme.FONT_TITLE);
        lblKTitle.setForeground(UITheme.TEXT_DARK);
        pnlKamarCtrl.add(lblKTitle, BorderLayout.NORTH);

        pnlRoomList = new JPanel();
        pnlRoomList.setLayout(new BoxLayout(pnlRoomList, BoxLayout.Y_AXIS));
        pnlRoomList.setOpaque(false);

        JScrollPane scrollRooms = new JScrollPane(pnlRoomList);
        scrollRooms.setBorder(null);
        scrollRooms.getViewport().setBackground(UITheme.BG_CARD);
        pnlKamarCtrl.add(scrollRooms, BorderLayout.CENTER);

        splitContainer.add(pnlKamarCtrl);
        add(splitContainer, BorderLayout.CENTER);

        refreshData();
    }

    public void refreshData() {
        // 1. Muat Booking Pending melalui Controller
        pendingList = controller.getPendingBookings();
        pendingModel.setRowCount(0);

        for (Booking b : pendingList) {
            String genderStr = (b.getJenisKelaminPengguna() != null && !b.getJenisKelaminPengguna().isEmpty())
                    ? b.getJenisKelaminPengguna() : "Laki-laki";
            pendingModel.addRow(new Object[]{
                    b.getIdBooking(),
                    b.getNamaPengguna(),
                    genderStr,
                    b.getKodeKamar(),
                    b.getDurasi() + " Bulan",
                    b.getNamaMetode()
            });
        }

        if (!pendingList.isEmpty()) {
            pendingTable.setRowSelectionInterval(0, 0);
            selectPendingBooking(pendingList.get(0));
        } else {
            selectedBooking = null;
            lblTenantName.setText("Tidak ada pengajuan booking yang pending.");
            lblTenantDetail.setText("Semua permohonan telah selesai diverifikasi.");
            lblBuktiThumb.setIcon(null);
            if (btnLihatBukti != null) btnLihatBukti.setEnabled(false);
            btnSetujui.setEnabled(false);
            btnTolak.setEnabled(false);
        }

        // 2. Muat Status Kamar melalui Controller
        List<Kamar> allKamar = controller.getAllKamar();
        pnlRoomList.removeAll();

        for (Kamar k : allKamar) {
            pnlRoomList.add(createRoomControlRow(k));
            pnlRoomList.add(Box.createVerticalStrut(6));
        }

        revalidate();
        repaint();
    }

    private JPanel createRoomControlRow(Kamar k) {
        boolean isTerisi = "terisi".equalsIgnoreCase(k.getStatus());

        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setBackground(isTerisi ? new Color(0xF5, 0xF2, 0xEB) : UITheme.BG_CARD_ALT);
        row.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));

        // Kiri: Kode + Gender + Harga
        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        left.setOpaque(false);

        JLabel lblKode = new JLabel(k.getKodeKamar());
        lblKode.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblKode.setForeground(UITheme.TEXT_DARK);
        left.add(lblKode);

        left.add(PillBadge.createStatusBadge(k.getTipe()));

        JLabel lblHrg = new JLabel(k.formatRupiah());
        lblHrg.setFont(UITheme.FONT_SMALL);
        lblHrg.setForeground(UITheme.PRIMARY_GREEN);
        left.add(lblHrg);

        row.add(left, BorderLayout.WEST);

        // Kanan: Dropdown Status
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false);

        if (isTerisi) {
            JComboBox<String> cmb = new JComboBox<>(new String[]{"Terisi"});
            cmb.setEnabled(false);
            cmb.setFont(UITheme.FONT_SMALL);
            right.add(cmb);
        } else {
            String[] options = {"Tersedia", "Perbaikan"};
            JComboBox<String> cmb = new JComboBox<>(options);
            cmb.setFont(UITheme.FONT_SMALL);

            boolean isPerbaikan = "pemeliharaan".equalsIgnoreCase(k.getStatus());
            cmb.setSelectedItem(isPerbaikan ? "Perbaikan" : "Tersedia");

            cmb.addActionListener(e -> {
                String sel = (String) cmb.getSelectedItem();
                String targetStatus = "Tersedia".equalsIgnoreCase(sel) ? "tersedia" : "pemeliharaan";
                boolean ok = controller.updateStatusKamar(k.getKodeKamar(), targetStatus);
                if (ok) {
                    if (!controller.isDatabaseConnected()) {
                        KozzyDialogHelper.showWarning(this,
                                "Pemberitahuan Database",
                                "Status kamar diubah ke " + sel.toUpperCase() + ".\n\n" +
                                "Database MySQL belum aktif di XAMPP.\n" +
                                "Perubahan disimpan sementara di memori cadangan.");
                    }
                    refreshData();
                }
            });
            right.add(cmb);
        }

        row.add(right, BorderLayout.EAST);
        return row;
    }

    private void selectPendingBooking(Booking b) {
        this.selectedBooking = b;
        if (b == null) return;
        String jk = (b.getJenisKelaminPengguna() != null && !b.getJenisKelaminPengguna().isEmpty()) ? " (" + b.getJenisKelaminPengguna() + ")" : "";
        lblTenantName.setText(b.getNamaPengguna() + jk + " · Kamar " + b.getKodeKamar());
        String info = "<html><center>ID: <b>" + b.getIdBooking() + "</b> · Durasi: <b>" + b.getDurasi() + " Bulan</b><br>" +
                "Total Tagihan: <b>" + String.format("Rp %,d", b.hitungTotalBiaya()).replace(',', '.') + "</b> (" + b.getNamaMetode() + ")</center></html>";
        lblTenantDetail.setText(info);

        if (btnLihatBukti != null) btnLihatBukti.setEnabled(true);
        btnSetujui.setEnabled(true);
        btnTolak.setEnabled(true);

        if (b.getFileBukti() != null) {
            BufferedImage thumb = ImageHelper.loadAndScale(b.getFileBukti(), 100, 100, 6);
            lblBuktiThumb.setIcon(new ImageIcon(thumb));
            lblBuktiThumb.setText("");
        } else {
            lblBuktiThumb.setIcon(null);
            lblBuktiThumb.setText("Tidak Ada Foto");
        }
    }

    private void prosesSetujui() {
        if (selectedBooking == null) return;

        boolean confirm = KozzyDialogHelper.showConfirm(
                this,
                "Konfirmasi Persetujuan",
                "Setujui pengajuan sewa " + selectedBooking.getIdBooking() + " untuk Kamar " + selectedBooking.getKodeKamar() + "?\nStatus kamar akan otomatis menjadi TERISI.",
                "Setujui Sekarang",
                PillButton.Style.PRIMARY
        );

        if (confirm) {
            boolean ok = controller.approveBooking(selectedBooking.getIdBooking());
            if (ok) {
                String note = controller.isDatabaseConnected() ? "Data perubahan telah tersimpan ke database MySQL." : "Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
                KozzyDialogHelper.showSuccess(this, "Pengajuan Disetujui", "Pengajuan berhasil disetujui!\n\n" + note);
                refreshData();
            }
        }
    }

    private void prosesTolak() {
        if (selectedBooking == null) return;

        Window win = SwingUtilities.getWindowAncestor(this);
        String alasan = RejectBookingDialog.showDialog(win, selectedBooking);

        if (alasan != null && !alasan.trim().isEmpty()) {
            boolean ok = controller.rejectBooking(selectedBooking.getIdBooking(), alasan.trim());
            if (ok) {
                String note = controller.isDatabaseConnected() ? "Data perubahan telah tersimpan ke database MySQL." : "Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
                KozzyDialogHelper.showSuccess(this, "Pengajuan Ditolak", "Pengajuan berhasil ditolak dengan alasan:\n\"" + alasan.trim() + "\"\n\n" + note);
                refreshData();
            }
        }
    }

    private void showBuktiPreview(Booking b) {
        if (b == null) return;
        Window owner = SwingUtilities.getWindowAncestor(this);
        String sub = "Penyewa: " + b.getNamaPengguna() + " · Kamar " + b.getKodeKamar() + " · " + b.getDurasi() + " Bulan";
        KozzyDialogHelper.showImagePreview(owner, "Bukti Pembayaran — " + b.getIdBooking(), b.getFileBukti(), sub);
    }
}
