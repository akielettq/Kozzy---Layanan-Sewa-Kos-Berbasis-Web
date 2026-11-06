package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.KamarController;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DesktopKamarView: Tampilan Katalog Kamar berbasis Master-Detail Desktop Workstation.
 * - Kolom Kiri: Tabel daftar kamar ringkas dengan filter cepat (Semua, Putri, Putra, Tersedia).
 * - Kolom Kanan: Panel preview detail instan (foto kamar, harga, status, fasilitas, tombol booking).
 * - Seluruh informasi muat dalam 1 layar tanpa scroll halaman vertikal.
 * - Murni tampilan UI; logika pemrosesan filter didelegasikan ke KamarController.
 */
public class DesktopKamarView extends JPanel {

    public interface KamarActionCallback {
        void onBookingRequested(Kamar kamar);
        void onLoginRequested();
    }

    private final KamarActionCallback callback;
    private final KamarController controller = new KamarController();

    private JTable roomTable;
    private DefaultTableModel tableModel;
    private List<Kamar> currentRoomList = new ArrayList<>();
    private Kamar selectedRoom;
    private String currentFilter = "semua";

    // Komponen Detail Kanan
    private JLabel lblDetailPhoto;
    private JLabel lblDetailKode;
    private JPanel pnlDetailBadges;
    private JLabel lblDetailHarga;
    private JLabel lblDetailDeskripsi;
    private JPanel pnlDetailFasilitas;
    private JPanel pnlDetailAction;

    // Filter Buttons
    private PillButton btnFilterSemua;
    private PillButton btnFilterPutri;
    private PillButton btnFilterPutra;
    private PillButton btnFilterTersedia;

    public DesktopKamarView(KamarActionCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout(0, 14));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        initUI();
    }

    private void initUI() {
        // ==========================================
        // 1. TOP HEADER & FILTER BAR
        // ==========================================
        JPanel topBar = new JPanel(new BorderLayout(12, 0));
        topBar.setOpaque(false);

        JPanel titleBox = new JPanel();
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        titleBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Katalog & Ketersediaan Kamar");
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        titleBox.add(lblTitle);

        JLabel lblSub = new JLabel("Pilih kamar dari tabel untuk melihat preview foto dan rincian fasilitas.");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        titleBox.add(lblSub);

        topBar.add(titleBox, BorderLayout.WEST);

        // Filter Pills
        JPanel filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        filterRow.setOpaque(false);

        btnFilterSemua = new PillButton("Semua Kamar", PillButton.Style.PRIMARY);
        btnFilterPutri = new PillButton("Kos Putri", PillButton.Style.OUTLINE);
        btnFilterPutra = new PillButton("Kos Putra", PillButton.Style.OUTLINE);
        btnFilterTersedia = new PillButton("Hanya Tersedia", PillButton.Style.OUTLINE);

        btnFilterSemua.addActionListener(e -> setFilter("semua"));
        btnFilterPutri.addActionListener(e -> setFilter("putri"));
        btnFilterPutra.addActionListener(e -> setFilter("putra"));
        btnFilterTersedia.addActionListener(e -> setFilter("tersedia"));

        filterRow.add(btnFilterSemua);
        filterRow.add(btnFilterPutri);
        filterRow.add(btnFilterPutra);
        filterRow.add(btnFilterTersedia);

        topBar.add(filterRow, BorderLayout.EAST);
        add(topBar, BorderLayout.NORTH);

        // ==========================================
        // 2. MASTER-DETAIL SPLIT CONTAINER (50% - 50%)
        // ==========================================
        JPanel splitContainer = new JPanel(new GridLayout(1, 2, 14, 0));
        splitContainer.setOpaque(false);

        // A. PANEL KIRI: TABEL MASTER DAFTAR KAMAR
        JPanel pnlMaster = new JPanel(new BorderLayout());
        pnlMaster.setBackground(UITheme.BG_CARD);
        pnlMaster.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));

        String[] cols = {"KODE", "TIPE", "STATUS", "HARGA"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        roomTable = new JTable(tableModel);
        roomTable.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.setShowGrid(false);
        roomTable.setShowHorizontalLines(true);
        roomTable.setGridColor(new Color(0xEE, 0xEA, 0xE0));
        roomTable.setFillsViewportHeight(true);

        // Renderer rata tengah dan badge visual
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(65);
        roomTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);

        roomTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        roomTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
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

        roomTable.getColumnModel().getColumn(2).setPreferredWidth(105);
        roomTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
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

        DefaultTableCellRenderer rightRender = new DefaultTableCellRenderer();
        rightRender.setHorizontalAlignment(SwingConstants.RIGHT);
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(125);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(rightRender);

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = roomTable.getSelectedRow();
                if (row >= 0 && row < currentRoomList.size()) {
                    selectRoom(currentRoomList.get(row));
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(roomTable);
        scrollTable.setBorder(null);
        scrollTable.getViewport().setBackground(UITheme.BG_CARD);
        pnlMaster.add(scrollTable, BorderLayout.CENTER);

        splitContainer.add(pnlMaster);

        // B. PANEL KANAN: DETAIL PREVIEW KAMAR INSTAN
        JPanel pnlDetail = new JPanel(new BorderLayout(0, 10));
        pnlDetail.setBackground(UITheme.BG_CARD);
        pnlDetail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(14, 16, 14, 16)
        ));

        // 1. Foto Preview Kamar
        lblDetailPhoto = new JLabel();
        lblDetailPhoto.setPreferredSize(new Dimension(0, 200));
        lblDetailPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailPhoto.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        pnlDetail.add(lblDetailPhoto, BorderLayout.NORTH);

        // 2. Info Detail Tengah
        JPanel infoCenter = new JPanel();
        infoCenter.setLayout(new BoxLayout(infoCenter, BoxLayout.Y_AXIS));
        infoCenter.setOpaque(false);

        // Baris Header Detail: Kode + Badges
        JPanel headerRow = new JPanel(new BorderLayout());
        headerRow.setOpaque(false);
        headerRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblDetailKode = new JLabel("A-01");
        lblDetailKode.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDetailKode.setForeground(UITheme.TEXT_DARK);
        headerRow.add(lblDetailKode, BorderLayout.WEST);

        pnlDetailBadges = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlDetailBadges.setOpaque(false);
        headerRow.add(pnlDetailBadges, BorderLayout.EAST);
        infoCenter.add(headerRow);
        infoCenter.add(Box.createVerticalStrut(6));

        // Harga
        lblDetailHarga = new JLabel("Rp 850.000 / bln");
        lblDetailHarga.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDetailHarga.setForeground(UITheme.PRIMARY_GREEN);
        lblDetailHarga.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCenter.add(lblDetailHarga);
        infoCenter.add(Box.createVerticalStrut(8));

        // Deskripsi
        lblDetailDeskripsi = new JLabel("Kamar tidur dengan ventilasi alami.");
        lblDetailDeskripsi.setFont(UITheme.FONT_BODY);
        lblDetailDeskripsi.setForeground(UITheme.TEXT_MUTED);
        lblDetailDeskripsi.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCenter.add(lblDetailDeskripsi);
        infoCenter.add(Box.createVerticalStrut(12));

        // Fasilitas Chips
        JLabel lblFasTitle = new JLabel("Fasilitas Tersedia:");
        lblFasTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblFasTitle.setForeground(UITheme.TEXT_DARK);
        lblFasTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCenter.add(lblFasTitle);
        infoCenter.add(Box.createVerticalStrut(6));

        pnlDetailFasilitas = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 6));
        pnlDetailFasilitas.setOpaque(false);
        pnlDetailFasilitas.setAlignmentX(Component.LEFT_ALIGNMENT);
        infoCenter.add(pnlDetailFasilitas);

        infoCenter.add(Box.createVerticalGlue());
        pnlDetail.add(infoCenter, BorderLayout.CENTER);

        // 3. Tombol Aksi Bawah
        pnlDetailAction = new JPanel(new BorderLayout());
        pnlDetailAction.setOpaque(false);
        pnlDetailAction.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        pnlDetail.add(pnlDetailAction, BorderLayout.SOUTH);

        splitContainer.add(pnlDetail);

        add(splitContainer, BorderLayout.CENTER);

        // Muat Data Kamar
        loadKamarList();
    }

    public void setFilter(String filter) {
        this.currentFilter = filter;

        btnFilterSemua.setStyle(PillButton.Style.OUTLINE);
        btnFilterPutri.setStyle(PillButton.Style.OUTLINE);
        btnFilterPutra.setStyle(PillButton.Style.OUTLINE);
        btnFilterTersedia.setStyle(PillButton.Style.OUTLINE);

        if ("putri".equalsIgnoreCase(filter)) btnFilterPutri.setStyle(PillButton.Style.PRIMARY);
        else if ("putra".equalsIgnoreCase(filter)) btnFilterPutra.setStyle(PillButton.Style.PRIMARY);
        else if ("tersedia".equalsIgnoreCase(filter)) btnFilterTersedia.setStyle(PillButton.Style.PRIMARY);
        else btnFilterSemua.setStyle(PillButton.Style.PRIMARY);

        loadKamarList();
    }

    public void loadKamarList() {
        currentRoomList.clear();
        currentRoomList.addAll(controller.getKamarList(currentFilter));

        tableModel.setRowCount(0);
        for (Kamar k : currentRoomList) {
            tableModel.addRow(new Object[]{
                    k.getKodeKamar(),
                    k.getTipe(),
                    k.getStatus(),
                    k.formatRupiah()
            });
        }

        if (!currentRoomList.isEmpty()) {
            // Pilih kamar pertama atau kamar yang sedang aktif
            int targetIdx = 0;
            if (selectedRoom != null) {
                for (int i = 0; i < currentRoomList.size(); i++) {
                    if (currentRoomList.get(i).getKodeKamar().equalsIgnoreCase(selectedRoom.getKodeKamar())) {
                        targetIdx = i;
                        break;
                    }
                }
            }
            roomTable.setRowSelectionInterval(targetIdx, targetIdx);
            selectRoom(currentRoomList.get(targetIdx));
        } else {
            clearDetailPreview();
        }
    }

    public void selectRoomByKode(String kodeKamar) {
        setFilter("semua");
        for (int i = 0; i < currentRoomList.size(); i++) {
            if (currentRoomList.get(i).getKodeKamar().equalsIgnoreCase(kodeKamar)) {
                roomTable.setRowSelectionInterval(i, i);
                selectRoom(currentRoomList.get(i));
                break;
            }
        }
    }

    private void selectRoom(Kamar k) {
        this.selectedRoom = k;
        if (k == null) return;

        // 1. Muat Foto
        String photoPath = "assets/kamar/" + k.getKodeKamar() + ".jpg";
        BufferedImage img = ImageHelper.loadAndScale(photoPath, 380, 190, 8);
        lblDetailPhoto.setIcon(new ImageIcon(img));

        // 2. Info Teks
        lblDetailKode.setText(k.getKodeKamar());

        pnlDetailBadges.removeAll();
        pnlDetailBadges.add(PillBadge.createStatusBadge(k.getTipe()));
        pnlDetailBadges.add(PillBadge.createStatusBadge(k.getStatus()));

        lblDetailHarga.setText(k.formatRupiah());
        lblDetailDeskripsi.setText("<html><body style='width: 380px; font-size: 11px; color: #5A5750;'>" +
                k.getDeskripsi() + "</body></html>");

        // 3. Fasilitas
        pnlDetailFasilitas.removeAll();
        for (String fas : k.getFasilitas()) {
            JLabel lblFas = new JLabel("• " + fas);
            lblFas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblFas.setForeground(new Color(0x1F, 0x5B, 0x41));
            lblFas.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xDC, 0xED, 0xE1), 1, true),
                    BorderFactory.createEmptyBorder(3, 8, 3, 8)
            ));
            pnlDetailFasilitas.add(lblFas);
        }

        // 4. Tombol Aksi
        pnlDetailAction.removeAll();
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = (user != null && user.isAdmin());

        if (isAdmin) {
            JLabel lblAdminNote = new JLabel("ℹ️ Akun Admin hanya dapat meninjau detail kamar.");
            lblAdminNote.setFont(UITheme.FONT_SMALL);
            lblAdminNote.setForeground(UITheme.TEXT_MUTED);
            pnlDetailAction.add(lblAdminNote, BorderLayout.CENTER);
        } else if (k.isTersedia()) {
            PillButton btnBook = new PillButton("Ajukan Booking Kamar Ini →", PillButton.Style.PRIMARY);
            btnBook.setPreferredSize(new Dimension(0, 40));
            btnBook.addActionListener(e -> {
                if (user == null) {
                    if (callback != null) callback.onLoginRequested();
                } else {
                    if (callback != null) callback.onBookingRequested(k);
                }
            });
            pnlDetailAction.add(btnBook, BorderLayout.CENTER);
        } else if ("terisi".equalsIgnoreCase(k.getStatus())) {
            String hingga = (k.getTerisiHingga() != null) ? " s.d. " + k.getTerisiHingga() : "";
            JLabel lblNote = new JLabel("Kamar sedang dihuni penyewa" + hingga + ".");
            lblNote.setFont(UITheme.FONT_BODY_BOLD);
            lblNote.setForeground(UITheme.ACCENT_TERA);
            pnlDetailAction.add(lblNote, BorderLayout.CENTER);
        } else {
            JLabel lblNote = new JLabel("Kamar sedang dalam proses perbaikan/pemeliharaan.");
            lblNote.setFont(UITheme.FONT_BODY_BOLD);
            lblNote.setForeground(UITheme.WARNING_AMBER);
            pnlDetailAction.add(lblNote, BorderLayout.CENTER);
        }

        pnlDetailBadges.revalidate();
        pnlDetailBadges.repaint();
        pnlDetailFasilitas.revalidate();
        pnlDetailFasilitas.repaint();
        pnlDetailAction.revalidate();
        pnlDetailAction.repaint();
    }

    private void clearDetailPreview() {
        lblDetailPhoto.setIcon(null);
        lblDetailKode.setText("-");
        pnlDetailBadges.removeAll();
        lblDetailHarga.setText("-");
        lblDetailDeskripsi.setText("Pilih kamar dari tabel untuk melihat preview.");
        pnlDetailFasilitas.removeAll();
        pnlDetailAction.removeAll();
        revalidate();
        repaint();
    }
}
