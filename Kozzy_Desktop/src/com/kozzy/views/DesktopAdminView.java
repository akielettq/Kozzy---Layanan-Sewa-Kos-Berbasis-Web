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
import java.awt.event.HierarchyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DesktopAdminView: Konsol Manajemen Operasional Admin Berorientasi Desktop Workstation.
 * - Dapat dibuka sebagai Jendela Terpisah mandiri (openAdminWindow) agar terpisah dari jendela pengguna.
 * - Sisi Kiri: Verifikasi Pengajuan Booking (Tabel Pending, Preview Bukti Transfer, Tombol Lihat Bukti/Setujui/Tolak).
 * - Sisi Kanan: Pengendalian Status Ketersediaan Kamar (Dropdown Tersedia / Perbaikan / Terisi).
 * - Seluruh pop-up menggunakan dialog kustom UITheme (termasuk multiline JTextArea untuk alasan penolakan).
 */
public class DesktopAdminView extends JPanel {

    private static JFrame adminWindowInstance = null;
    private static DesktopAdminView adminViewInstance = null;
    private static JLabel lblAdminTopDbStatus = null;

    private final AdminController controller = new AdminController();
    private Runnable onDataChangedCallback;

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

    public void setOnDataChangedCallback(Runnable callback) {
        this.onDataChangedCallback = callback;
    }

    /**
     * Membuka atau memfokuskan Panel Admin dalam jendela workstation terpisah (mandiri).
     */
    public static JFrame openAdminWindow(Runnable onDataChanged) {
        if (adminWindowInstance == null || !adminWindowInstance.isDisplayable()) {
            JFrame frame = new JFrame("Kozzy Desktop — Konsol Manajemen & Verifikasi Admin (Terpisah)");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(1140, 720);
            frame.setMinimumSize(new Dimension(980, 620));
            frame.setLocationRelativeTo(null);
            frame.getContentPane().setBackground(UITheme.BG_CANVAS);
            frame.setLayout(new BorderLayout());

            try {
                java.io.File iconFile = ImageHelper.resolveFile("assets/logo kos.png");
                if (iconFile != null && iconFile.exists()) {
                    frame.setIconImage(javax.imageio.ImageIO.read(iconFile));
                }
            } catch (Exception ignored) {}

            adminViewInstance = new DesktopAdminView();
            adminViewInstance.setOnDataChangedCallback(onDataChanged);

            frame.add(createAdminTopBar(frame, adminViewInstance), BorderLayout.NORTH);
            frame.add(adminViewInstance, BorderLayout.CENTER);

            frame.addHierarchyListener(e -> {
                if ((e.getChangeFlags() & HierarchyEvent.DISPLAYABILITY_CHANGED) != 0 && !frame.isDisplayable()) {
                    adminWindowInstance = null;
                    if (adminViewInstance != null && adminViewInstance.onDataChangedCallback != null) {
                        adminViewInstance.onDataChangedCallback.run();
                    }
                }
            });

            adminWindowInstance = frame;
            frame.setVisible(true);
        } else {
            if (adminViewInstance != null) {
                adminViewInstance.setOnDataChangedCallback(onDataChanged);
                adminViewInstance.refreshData();
            }
            adminWindowInstance.setVisible(true);
            adminWindowInstance.toFront();
            adminWindowInstance.requestFocus();
        }
        return adminWindowInstance;
    }

    /**
     * Menutup jendela admin terpisah secara otomatis saat logout atau berganti ke akun non-admin.
     */
    public static void closeAdminWindow() {
        if (adminWindowInstance != null) {
            JFrame frameToClose = adminWindowInstance;
            adminWindowInstance = null;
            adminViewInstance = null;
            lblAdminTopDbStatus = null;
            if (frameToClose.isDisplayable()) {
                frameToClose.dispose();
            }
        }
    }

    private static JPanel createAdminTopBar(JFrame frame, DesktopAdminView view) {
        JPanel bar = new JPanel(new BorderLayout(14, 0));
        bar.setBackground(UITheme.SIDEBAR_BG);
        bar.setBorder(BorderFactory.createEmptyBorder(12, 20, 12, 20));

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

        JPanel rightPnl = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPnl.setOpaque(false);

        boolean dbOk = view.controller.isDatabaseConnected();
        lblAdminTopDbStatus = new JLabel(dbOk ? "● MySQL Aktif" : "○ Mode Cadangan");
        lblAdminTopDbStatus.setFont(UITheme.FONT_SMALL);
        lblAdminTopDbStatus.setForeground(dbOk ? new Color(0x74, 0xD4, 0xA0) : UITheme.WARNING_AMBER);
        lblAdminTopDbStatus.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x3B, 0x4D, 0x43), 1, true),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        rightPnl.add(lblAdminTopDbStatus);

        PillButton btnRefresh = new PillButton("Refresh Data", PillButton.Style.SECONDARY);
        btnRefresh.setPreferredSize(new Dimension(125, 32));
        btnRefresh.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnRefresh.addActionListener(e -> view.refreshData());
        rightPnl.add(btnRefresh);

        PillButton btnClose = new PillButton("Tutup Konsol", PillButton.Style.OUTLINE);
        btnClose.setPreferredSize(new Dimension(105, 32));
        btnClose.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnClose.addActionListener(e -> frame.dispose());
        rightPnl.add(btnClose);

        bar.add(rightPnl, BorderLayout.EAST);
        return bar;
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

        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 0; i < pendingTable.getColumnCount(); i++) {
            pendingTable.getColumnModel().getColumn(i).setCellRenderer(centerRender);
        }

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
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Kiri Preview: Info Teks & Tombol Aksi
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

        // Tombol Lihat Bukti, Setujui & Tolak
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnLihatBukti = new PillButton("Lihat Bukti", PillButton.Style.OUTLINE);
        btnLihatBukti.setEnabled(false);
        btnLihatBukti.setPreferredSize(new Dimension(100, 32));
        btnLihatBukti.addActionListener(e -> {
            if (selectedBooking != null) {
                showBuktiDialog(selectedBooking.getFileBukti());
            }
        });
        btnRow.add(btnLihatBukti);

        btnSetujui = new PillButton("Setujui", PillButton.Style.PRIMARY);
        btnSetujui.setEnabled(false);
        btnSetujui.setPreferredSize(new Dimension(95, 32));
        btnSetujui.addActionListener(e -> prosesSetujui());
        btnRow.add(btnSetujui);

        btnTolak = new PillButton("Tolak", PillButton.Style.DANGER);
        btnTolak.setEnabled(false);
        btnTolak.setPreferredSize(new Dimension(90, 32));
        btnTolak.addActionListener(e -> prosesTolak());
        btnRow.add(btnTolak);

        pnlPreviewText.add(btnRow);
        previewBox.add(pnlPreviewText, BorderLayout.CENTER);

        // Kanan Preview: Thumbnail Bukti Transfer
        JPanel pnlThumb = new JPanel(new BorderLayout());
        pnlThumb.setOpaque(false);
        pnlThumb.setPreferredSize(new Dimension(96, 96));

        lblBuktiThumb = new JLabel("Tidak Ada Foto", SwingConstants.CENTER);
        lblBuktiThumb.setFont(UITheme.FONT_SMALL);
        lblBuktiThumb.setForeground(UITheme.TEXT_MUTED);
        lblBuktiThumb.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true));
        lblBuktiThumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblBuktiThumb.setToolTipText("Klik untuk memperbesar bukti pembayaran");

        java.awt.event.MouseAdapter previewClickAdapter = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectedBooking != null && (e.getComponent() == lblBuktiThumb || e.getClickCount() == 2)) {
                    showBuktiDialog(selectedBooking.getFileBukti());
                }
            }
        };
        lblBuktiThumb.addMouseListener(previewClickAdapter);
        pendingTable.addMouseListener(previewClickAdapter);

        pnlThumb.add(lblBuktiThumb, BorderLayout.CENTER);
        previewBox.add(pnlThumb, BorderLayout.EAST);

        pnlVerif.add(previewBox, BorderLayout.SOUTH);
        splitContainer.add(pnlVerif);

        // PANEL KANAN: KELOLA STATUS KAMAR
        JPanel pnlKamarCtrl = new JPanel(new BorderLayout(0, 10));
        pnlKamarCtrl.setBackground(UITheme.BG_CARD);
        pnlKamarCtrl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        JLabel lblKTitle = new JLabel("Kelola Status Ketersediaan Kamar", SwingConstants.CENTER);
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
        if (lblAdminTopDbStatus != null) {
            boolean dbOk = controller.isDatabaseConnected();
            lblAdminTopDbStatus.setText(dbOk ? "● MySQL Aktif" : "○ Mode Cadangan");
            lblAdminTopDbStatus.setForeground(dbOk ? new Color(0x74, 0xD4, 0xA0) : UITheme.WARNING_AMBER);
        }

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
            lblBuktiThumb.setText("Tidak Ada Foto");
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

        if (onDataChangedCallback != null) {
            onDataChangedCallback.run();
        }
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
                if (targetStatus.equalsIgnoreCase(k.getStatus())) {
                    return;
                }
                boolean ok = controller.updateStatusKamar(k.getKodeKamar(), targetStatus);
                if (ok) {
                    if (!controller.isDatabaseConnected()) {
                        UITheme.showWarning(this,
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

        lblTenantName.setText(b.getNamaPengguna() + " · Kamar " + b.getKodeKamar());
        String info = "<html><center>ID: <b>" + b.getIdBooking() + "</b> · Durasi: <b>" + b.getDurasi() + " Bulan</b><br>" +
                "Total Tagihan: <b>" + String.format("Rp %,d", b.hitungTotalBiaya()).replace(',', '.') + "</b> (" + b.getNamaMetode() + ")</center></html>";
        lblTenantDetail.setText(info);

        if (btnLihatBukti != null) btnLihatBukti.setEnabled(true);
        btnSetujui.setEnabled(true);
        btnTolak.setEnabled(true);

        if (b.getFileBukti() != null) {
            BufferedImage thumb = ImageHelper.loadAndScale(b.getFileBukti(), 92, 92, 6);
            lblBuktiThumb.setIcon(new ImageIcon(thumb));
            lblBuktiThumb.setText("");
        } else {
            lblBuktiThumb.setIcon(null);
            lblBuktiThumb.setText("Tidak Ada Foto");
        }
    }

    private void prosesSetujui() {
        if (selectedBooking == null) return;

        boolean confirm = UITheme.showConfirm(
                this,
                "Konfirmasi Persetujuan",
                "Setujui pengajuan sewa " + selectedBooking.getIdBooking() + " untuk Kamar " + selectedBooking.getKodeKamar() + "?\n" +
                        "Status kamar akan otomatis berubah menjadi TERISI.",
                "Setujui Sekarang",
                PillButton.Style.PRIMARY
        );

        if (confirm) {
            boolean ok = controller.approveBooking(selectedBooking.getIdBooking());
            if (ok) {
                String note = controller.isDatabaseConnected()
                        ? "Data perubahan telah tersimpan ke database MySQL."
                        : "Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
                UITheme.showSuccess(this, "Pengajuan Disetujui", "Pengajuan berhasil disetujui!\n\n" + note);
                refreshData();
            }
        }
    }

    private void prosesTolak() {
        if (selectedBooking == null) return;

        String alasan = UITheme.showRejectBookingDialog(this, selectedBooking);

        if (alasan != null && !alasan.trim().isEmpty()) {
            boolean ok = controller.rejectBooking(selectedBooking.getIdBooking(), alasan.trim());
            if (ok) {
                String note = controller.isDatabaseConnected()
                        ? "Data perubahan telah tersimpan ke database MySQL."
                        : "Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
                UITheme.showSuccess(this, "Pengajuan Ditolak", "Pengajuan berhasil ditolak dengan alasan:\n\"" + alasan.trim() + "\"\n\n" + note);
                refreshData();
            }
        }
    }

    private void showBuktiDialog(String path) {
        String sub = (selectedBooking != null)
                ? "Penyewa: " + selectedBooking.getNamaPengguna() + " · Kamar " + selectedBooking.getKodeKamar() + " · " + selectedBooking.getDurasi() + " Bulan"
                : "";
        String title = (selectedBooking != null)
                ? "Bukti Pembayaran — " + selectedBooking.getIdBooking()
                : "Bukti Pembayaran Transfer";
        UITheme.showImagePreview(this, title, path, sub);
    }
}
