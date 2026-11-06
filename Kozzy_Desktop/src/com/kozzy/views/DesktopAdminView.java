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
 * - Seluruh informasi muat dalam 1 layar desktop terintegrasi tanpa scroll halaman vertikal.
 * - Murni tampilan UI; seluruh logika verifikasi & update status didelegasikan ke AdminController.
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
        // 1. HEADER
        // ==========================================
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Panel Manajemen & Verifikasi Admin");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        titleBox.add(lblTitle);

        JLabel lblSub = new JLabel("Verifikasi booking masuk dan atur status ketersediaan kamar.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titleBox.add(lblSub);

        header.add(titleBox, BorderLayout.WEST);
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
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblVTitle = new JLabel("Pengajuan Booking Menunggu Persetujuan");
        lblVTitle.setFont(UITheme.FONT_TITLE);
        lblVTitle.setForeground(UITheme.TEXT_DARK);
        pnlVerif.add(lblVTitle, BorderLayout.NORTH);

        // Tabel Pending
        String[] cols = {"ID", "PENYEWA", "KAMAR", "DURASI", "METODE"};
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

        pendingTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        pendingTable.getColumnModel().getColumn(1).setPreferredWidth(100);
        pendingTable.getColumnModel().getColumn(2).setPreferredWidth(60);
        pendingTable.getColumnModel().getColumn(3).setPreferredWidth(65);
        pendingTable.getColumnModel().getColumn(4).setPreferredWidth(85);

        pendingTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = pendingTable.getSelectedRow();
                if (row >= 0 && row < pendingList.size()) {
                    selectPendingBooking(pendingList.get(row));
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
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Kiri Preview: Info Teks & Tombol Aksi
        JPanel pnlPreviewText = new JPanel();
        pnlPreviewText.setLayout(new BoxLayout(pnlPreviewText, BoxLayout.Y_AXIS));
        pnlPreviewText.setOpaque(false);

        lblTenantName = new JLabel("Pilih pengajuan booking di atas");
        lblTenantName.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTenantName.setForeground(UITheme.TEXT_DARK);
        pnlPreviewText.add(lblTenantName);
        pnlPreviewText.add(Box.createVerticalStrut(4));

        lblTenantDetail = new JLabel("-");
        lblTenantDetail.setFont(UITheme.FONT_SMALL);
        lblTenantDetail.setForeground(UITheme.TEXT_MUTED);
        pnlPreviewText.add(lblTenantDetail);
        pnlPreviewText.add(Box.createVerticalStrut(10));

        // Tombol Setujui & Tolak
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        btnRow.setOpaque(false);

        btnSetujui = new PillButton("Setujui Pengajuan", PillButton.Style.PRIMARY);
        btnSetujui.setEnabled(false);
        btnSetujui.addActionListener(e -> prosesSetujui());
        btnRow.add(btnSetujui);

        btnTolak = new PillButton("Tolak", PillButton.Style.DANGER);
        btnTolak.setEnabled(false);
        btnTolak.addActionListener(e -> prosesTolak());
        btnRow.add(btnTolak);

        pnlPreviewText.add(btnRow);
        previewBox.add(pnlPreviewText, BorderLayout.CENTER);

        // Kanan Preview: Thumbnail Bukti Transfer
        lblBuktiThumb = new JLabel();
        lblBuktiThumb.setPreferredSize(new Dimension(80, 80));
        lblBuktiThumb.setHorizontalAlignment(SwingConstants.CENTER);
        lblBuktiThumb.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        lblBuktiThumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBuktiThumb.setToolTipText("Klik untuk memperbesar bukti pembayaran");
        lblBuktiThumb.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectedBooking != null && selectedBooking.getFileBukti() != null) {
                    showBuktiDialog(selectedBooking.getFileBukti());
                }
            }
        });
        previewBox.add(lblBuktiThumb, BorderLayout.EAST);

        pnlVerif.add(previewBox, BorderLayout.SOUTH);
        splitContainer.add(pnlVerif);

        // PANEL KANAN: KELOLA STATUS KAMAR
        JPanel pnlKamarCtrl = new JPanel(new BorderLayout(0, 10));
        pnlKamarCtrl.setBackground(UITheme.BG_CARD);
        pnlKamarCtrl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblKTitle = new JLabel("Kelola Status Ketersediaan Kamar");
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
            pendingModel.addRow(new Object[]{
                    b.getIdBooking(),
                    b.getNamaPengguna(),
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
        // Aturan: Jika terisi -> disabled, hanya ada "Terisi".
        //         Jika belum terisi -> pilihan HANYA "Tersedia" dan "Perbaikan".
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
                        JOptionPane.showMessageDialog(this,
                                "Status kamar diubah ke " + sel.toUpperCase() + ".\n\n" +
                                "⚠️ Catatan: Database MySQL belum aktif di XAMPP.\n" +
                                "Perubahan disimpan sementara di memori cadangan.",
                                "Pemberitahuan Database", JOptionPane.WARNING_MESSAGE);
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

        lblTenantName.setText(b.getNamaPengguna() + " · " + b.getKodeKamar());
        String info = "<html>ID: <b>" + b.getIdBooking() + "</b> · Durasi: <b>" + b.getDurasi() + " Bulan</b><br>" +
                "Total: <b>" + String.format("Rp %,d", b.hitungTotalBiaya()).replace(',', '.') + "</b> (" + b.getNamaMetode() + ")</html>";
        lblTenantDetail.setText(info);

        btnSetujui.setEnabled(true);
        btnTolak.setEnabled(true);

        if (b.getFileBukti() != null) {
            BufferedImage thumb = ImageHelper.loadAndScale(b.getFileBukti(), 100, 100, 6);
            lblBuktiThumb.setIcon(new ImageIcon(thumb));
        } else {
            lblBuktiThumb.setIcon(null);
            lblBuktiThumb.setText("Tidak Ada Foto");
        }
    }

    private void prosesSetujui() {
        if (selectedBooking == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Setujui pengajuan sewa " + selectedBooking.getIdBooking() + " untuk Kamar " + selectedBooking.getKodeKamar() + "?\n" +
                        "Status kamar akan otomatis berubah menjadi TERISI.",
                "Konfirmasi Persetujuan",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
        );

        if (confirm == JOptionPane.YES_OPTION) {
            boolean ok = controller.approveBooking(selectedBooking.getIdBooking());
            if (ok) {
                String note = controller.isDatabaseConnected() ? "Data perubahan telah tersimpan ke database MySQL." : "⚠️ Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
                JOptionPane.showMessageDialog(this, "Pengajuan berhasil disetujui!\n\n" + note, "Sukses", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }
        }
    }

    private void prosesTolak() {
        if (selectedBooking == null) return;

        String alasan = JOptionPane.showInputDialog(
                this,
                "Masukkan alasan penolakan pengajuan " + selectedBooking.getIdBooking() + ":",
                "Alasan Penolakan",
                JOptionPane.WARNING_MESSAGE
        );

        if (alasan != null && !alasan.trim().isEmpty()) {
            boolean ok = controller.rejectBooking(selectedBooking.getIdBooking(), alasan.trim());
            if (ok) {
                String note = controller.isDatabaseConnected() ? "Data perubahan telah tersimpan ke database MySQL." : "⚠️ Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
                JOptionPane.showMessageDialog(this, "Pengajuan berhasil ditolak.\n\n" + note, "Informasi", JOptionPane.INFORMATION_MESSAGE);
                refreshData();
            }
        }
    }

    private void showBuktiDialog(String path) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Bukti Pembayaran Transfer", true);
        dlg.setSize(520, 620);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(UITheme.BG_CANVAS);
        dlg.setLayout(new BorderLayout(10, 10));

        BufferedImage fullImg = ImageHelper.loadAndScale(path, 480, 500, 10);
        JLabel lbl = new JLabel(new ImageIcon(fullImg), SwingConstants.CENTER);
        dlg.add(lbl, BorderLayout.CENTER);

        PillButton btnClose = new PillButton("Tutup Preview", PillButton.Style.OUTLINE);
        btnClose.addActionListener(e -> dlg.dispose());
        JPanel btm = new JPanel(new FlowLayout(FlowLayout.CENTER));
        btm.setOpaque(false);
        btm.add(btnClose);
        dlg.add(btm, BorderLayout.SOUTH);

        dlg.setVisible(true);
    }
}
