package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.BookingController;
import com.kozzy.controllers.KamarController;
import com.kozzy.models.Kamar;
import com.kozzy.models.Pengguna;
import com.kozzy.services.SessionManager;
import com.kozzy.views.components.ImageHelper;
import com.kozzy.views.components.PillBadge;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DesktopKamarView: Tampilan Katalog Kamar berbasis Master-Detail Desktop Workstation.
 * - Mengeliminasi ruang kosong berlebih dengan tata letak padat, proporsional, dan informatif (Poin 2 & 5).
 * - Seluruh teks informasi, status, dan preview ditata rata tengah elegan (Poin 4).
 * - Dilengkapi pencarian kata kunci instan, filter kategori, dan penanganan empty-state rapi.
 */
public class DesktopKamarView extends JPanel {

    public interface KamarActionCallback {
        void onBookingRequested(Kamar kamar);
        void onLoginRequested();
    }

    private final KamarActionCallback callback;
    private final KamarController controller = new KamarController();
    private final BookingController bookingController = new BookingController();

    private JTable roomTable;
    private DefaultTableModel tableModel;
    private List<Kamar> allFilteredRooms = new ArrayList<>();
    private List<Kamar> currentRoomList = new ArrayList<>();
    private Kamar selectedRoom;
    private String currentFilter = "semua";
    private String searchKeyword = "";

    // Input Pencarian
    private JTextField txtSearch;
    private JLabel lblRoomCount;
    private JLabel lblMasterHint;

    // Komponen Detail Kanan
    private JLabel lblDetailPhoto;
    private JLabel lblDetailKode;
    private JPanel pnlDetailBadges;
    private JLabel lblDetailHarga;
    private JLabel lblDetailDeskripsi;
    private JPanel pnlDetailSpecs;
    private JPanel pnlDetailFasilitas;
    private JPanel pnlDetailHighlight;
    private JPanel pnlDetailAction;

    // Filter Buttons
    private PillButton btnFilterSemua;
    private PillButton btnFilterPutri;
    private PillButton btnFilterPutra;
    private PillButton btnFilterTersedia;

    public DesktopKamarView(KamarActionCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(16, 22, 16, 22));

        initUI();
    }

    private void initUI() {
        // ==========================================
        // 1. TOP HEADER & FILTER / SEARCH BAR (Tengah)
        // ==========================================
        JPanel topContainer = new JPanel();
        topContainer.setLayout(new BoxLayout(topContainer, BoxLayout.Y_AXIS));
        topContainer.setOpaque(false);

        // Section Tag Khas Kozzy Web (▪ KATALOG KAMAR & FASILITAS)
        JLabel lblSectionTag = new JLabel("▪ KATALOG KAMAR & FASILITAS", SwingConstants.CENTER);
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(UITheme.ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        topContainer.add(lblSectionTag);
        topContainer.add(Box.createVerticalStrut(2));

        // Header Title Rata Tengah (Poin 4)
        JLabel lblTitle = new JLabel("Katalog & Ketersediaan Kamar Kos", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_HEADER);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        topContainer.add(lblTitle);
        topContainer.add(Box.createVerticalStrut(2));

        JLabel lblSub = new JLabel("Pilih kamar untuk melihat foto resolusi tinggi, fasilitas lengkap, dan status ketersediaan.", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        topContainer.add(lblSub);
        topContainer.add(Box.createVerticalStrut(10));

        // Baris Bar Pencarian & Tombol Filter Rata Tengah (Poin 2 & 5)
        JPanel searchFilterRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        searchFilterRow.setOpaque(false);
        searchFilterRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Input Cari Kamar Ringkas dengan border tinta retro
        JPanel searchBox = new JPanel(new BorderLayout(6, 0));
        searchBox.setBackground(UITheme.BG_CARD);
        searchBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(4, 10, 4, 10)
        ));
        searchBox.setPreferredSize(new Dimension(280, 34));

        JLabel lblSearchIcon = new JLabel("🔍");
        lblSearchIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 12));
        searchBox.add(lblSearchIcon, BorderLayout.WEST);

        txtSearch = new JTextField();
        txtSearch.setBorder(null);
        txtSearch.setFont(UITheme.FONT_BODY);
        txtSearch.setOpaque(false);
        txtSearch.putClientProperty("JTextField.placeholderText", "Cari kode, tipe, fasilitas...");
        txtSearch.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override
            public void removeUpdate(DocumentEvent e) { onSearchChanged(); }
            @Override
            public void changedUpdate(DocumentEvent e) { onSearchChanged(); }
        });
        searchBox.add(txtSearch, BorderLayout.CENTER);
        searchFilterRow.add(searchBox);

        // Filter Pills
        btnFilterSemua = new PillButton("Semua Kamar", PillButton.Style.PRIMARY);
        btnFilterPutri = new PillButton("Kos Putri", PillButton.Style.OUTLINE);
        btnFilterPutra = new PillButton("Kos Putra", PillButton.Style.OUTLINE);
        btnFilterTersedia = new PillButton("Hanya Tersedia", PillButton.Style.OUTLINE);

        btnFilterSemua.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnFilterPutri.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnFilterPutra.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnFilterTersedia.setFont(new Font("Segoe UI", Font.BOLD, 11));

        btnFilterSemua.addActionListener(e -> setFilter("semua"));
        btnFilterPutri.addActionListener(e -> setFilter("putri"));
        btnFilterPutra.addActionListener(e -> setFilter("putra"));
        btnFilterTersedia.addActionListener(e -> setFilter("tersedia"));

        searchFilterRow.add(btnFilterSemua);
        searchFilterRow.add(btnFilterPutri);
        searchFilterRow.add(btnFilterPutra);
        searchFilterRow.add(btnFilterTersedia);

        topContainer.add(searchFilterRow);
        add(topContainer, BorderLayout.NORTH);

        // ==========================================
        // 2. MASTER-DETAIL WORKSTATION (50% - 50%)
        // ==========================================
        JPanel splitContainer = new JPanel(new GridLayout(1, 2, 14, 0));
        splitContainer.setOpaque(false);

        // A. PANEL KIRI: DAFTAR KAMAR TERORGANISIR
        JPanel pnlMaster = new JPanel(new BorderLayout(0, 8));
        pnlMaster.setBackground(UITheme.BG_CARD);
        pnlMaster.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(12, 14, 12, 14)
        ));

        // Subheader Master: Rata Tengah (Poin 4)
        JPanel pnlMasterTop = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlMasterTop.setOpaque(false);

        lblRoomCount = new JLabel("Daftar Kamar Kos", SwingConstants.CENTER);
        lblRoomCount.setFont(UITheme.FONT_BODY_BOLD);
        lblRoomCount.setForeground(UITheme.TEXT_DARK);
        pnlMasterTop.add(lblRoomCount);

        lblMasterHint = new JLabel("Pilih salah satu baris kamar untuk melihat detail lengkap", SwingConstants.CENTER);
        lblMasterHint.setFont(UITheme.FONT_SMALL);
        lblMasterHint.setForeground(UITheme.TEXT_MUTED);
        pnlMasterTop.add(lblMasterHint);
        pnlMaster.add(pnlMasterTop, BorderLayout.NORTH);

        String[] cols = {"KODE", "TIPE", "STATUS", "BIAYA SEWA"};
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
        roomTable.setRowHeight(42);
        roomTable.setFillsViewportHeight(true);

        // Seluruh kolom rata tengah
        DefaultTableCellRenderer centerRender = new DefaultTableCellRenderer();
        centerRender.setHorizontalAlignment(SwingConstants.CENTER);
        roomTable.getColumnModel().getColumn(0).setPreferredWidth(60);
        roomTable.getColumnModel().getColumn(0).setCellRenderer(centerRender);

        roomTable.getColumnModel().getColumn(1).setPreferredWidth(85);
        roomTable.getColumnModel().getColumn(1).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                String str = (val != null) ? val.toString() : "";
                PillBadge b = PillBadge.createStatusBadge(str);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                p.setOpaque(true);
                p.setBackground(isSel ? table.getSelectionBackground() : table.getBackground());
                p.add(b);
                return p;
            }
        });

        roomTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        roomTable.getColumnModel().getColumn(2).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                String str = (val != null) ? val.toString() : "";
                PillBadge b = PillBadge.createStatusBadge(str);
                JPanel p = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 6));
                p.setOpaque(true);
                p.setBackground(isSel ? table.getSelectionBackground() : table.getBackground());
                p.add(b);
                return p;
            }
        });

        DefaultTableCellRenderer centerHrgRender = new DefaultTableCellRenderer();
        centerHrgRender.setHorizontalAlignment(SwingConstants.CENTER);
        centerHrgRender.setForeground(UITheme.PRIMARY_GREEN);
        centerHrgRender.setFont(new Font("Segoe UI", Font.BOLD, 12));
        roomTable.getColumnModel().getColumn(3).setPreferredWidth(115);
        roomTable.getColumnModel().getColumn(3).setCellRenderer(centerHrgRender);

        roomTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int row = roomTable.getSelectedRow();
                if (row >= 0 && row < currentRoomList.size()) {
                    selectRoom(currentRoomList.get(row));
                }
            }
        });

        JScrollPane scrollTable = new JScrollPane(roomTable);
        scrollTable.setBorder(BorderFactory.createLineBorder(new Color(0xD8, 0xD2, 0xC5), 1, true));
        scrollTable.getViewport().setBackground(UITheme.BG_CARD);
        pnlMaster.add(scrollTable, BorderLayout.CENTER);

        splitContainer.add(pnlMaster);

        // B. PANEL KANAN: DETAIL PREVIEW KAMAR INSTAN (Teks Rata Tengah & Terisi Padat)
        JPanel pnlDetail = new JPanel(new BorderLayout(0, 8));
        pnlDetail.setBackground(UITheme.BG_CARD);
        pnlDetail.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));

        // 1. Foto Preview Kamar
        lblDetailPhoto = new JLabel();
        lblDetailPhoto.setPreferredSize(new Dimension(0, 175));
        lblDetailPhoto.setHorizontalAlignment(SwingConstants.CENTER);
        lblDetailPhoto.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true));
        pnlDetail.add(lblDetailPhoto, BorderLayout.NORTH);

        // 2. Info Detail Tengah (Semua Rata Tengah - Poin 4)
        JPanel infoCenter = new JPanel();
        infoCenter.setLayout(new BoxLayout(infoCenter, BoxLayout.Y_AXIS));
        infoCenter.setOpaque(false);

        infoCenter.add(Box.createVerticalStrut(4));

        // Kode Kamar Rata Tengah
        lblDetailKode = new JLabel("Kamar A-01", SwingConstants.CENTER);
        lblDetailKode.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblDetailKode.setForeground(UITheme.TEXT_DARK);
        lblDetailKode.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCenter.add(lblDetailKode);
        infoCenter.add(Box.createVerticalStrut(3));

        // Badges Rata Tengah
        pnlDetailBadges = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pnlDetailBadges.setOpaque(false);
        pnlDetailBadges.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCenter.add(pnlDetailBadges);
        infoCenter.add(Box.createVerticalStrut(4));

        // Harga Rata Tengah
        lblDetailHarga = new JLabel("Rp 850.000 / bulan", SwingConstants.CENTER);
        lblDetailHarga.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblDetailHarga.setForeground(UITheme.PRIMARY_GREEN);
        lblDetailHarga.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCenter.add(lblDetailHarga);
        infoCenter.add(Box.createVerticalStrut(6));

        // Kartu Spesifikasi Cepat (4 Badges)
        pnlDetailSpecs = new JPanel(new GridLayout(1, 4, 6, 0));
        pnlDetailSpecs.setOpaque(false);
        pnlDetailSpecs.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        pnlDetailSpecs.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCenter.add(pnlDetailSpecs);
        infoCenter.add(Box.createVerticalStrut(6));

        // Deskripsi Rata Tengah
        lblDetailDeskripsi = new JLabel("Kamar tidur berfasilitas lengkap dengan sirkulasi udara alami yang segar.", SwingConstants.CENTER);
        lblDetailDeskripsi.setFont(UITheme.FONT_BODY);
        lblDetailDeskripsi.setForeground(UITheme.TEXT_MUTED);
        lblDetailDeskripsi.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCenter.add(lblDetailDeskripsi);
        infoCenter.add(Box.createVerticalStrut(6));

        // Fasilitas Chips (Rata Tengah)
        JLabel lblFasTitle = new JLabel("Fasilitas Termasuk:", SwingConstants.CENTER);
        lblFasTitle.setFont(UITheme.FONT_BODY_BOLD);
        lblFasTitle.setForeground(UITheme.TEXT_DARK);
        lblFasTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCenter.add(lblFasTitle);
        infoCenter.add(Box.createVerticalStrut(3));

        pnlDetailFasilitas = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 3));
        pnlDetailFasilitas.setOpaque(false);
        pnlDetailFasilitas.setAlignmentX(Component.CENTER_ALIGNMENT);
        infoCenter.add(pnlDetailFasilitas);
        infoCenter.add(Box.createVerticalStrut(6));

        // Kartu Ketentuan & Jaminan Tambahan (Mengisi Ruang Kerja dengan Padat & Rapi - Poin 2)
        pnlDetailHighlight = new JPanel(new GridLayout(2, 1, 0, 2));
        pnlDetailHighlight.setBackground(UITheme.BG_CARD_ALT);
        pnlDetailHighlight.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        pnlDetailHighlight.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        pnlDetailHighlight.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblHigh1 = new JLabel("• Bebas Jam Malam Teratur  ·  • CCTV Lingkungan 24 Jam  ·  • Dapur Bersama", SwingConstants.CENTER);
        lblHigh1.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblHigh1.setForeground(UITheme.TEXT_DARK);
        pnlDetailHighlight.add(lblHigh1);

        JLabel lblHigh2 = new JLabel("Termasuk Air Bersih & WiFi Kecepatan Tinggi · Deposit Dikembalikan", SwingConstants.CENTER);
        lblHigh2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblHigh2.setForeground(UITheme.TEXT_MUTED);
        pnlDetailHighlight.add(lblHigh2);
        infoCenter.add(pnlDetailHighlight);

        pnlDetail.add(infoCenter, BorderLayout.CENTER);

        // 3. Tombol Aksi Bawah
        pnlDetailAction = new JPanel(new BorderLayout());
        pnlDetailAction.setOpaque(false);
        pnlDetailAction.setBorder(BorderFactory.createEmptyBorder(6, 0, 0, 0));
        pnlDetail.add(pnlDetailAction, BorderLayout.SOUTH);

        splitContainer.add(pnlDetail);
        add(splitContainer, BorderLayout.CENTER);

        // Muat Data Kamar
        loadKamarList();
    }

    private void onSearchChanged() {
        this.searchKeyword = txtSearch.getText().trim().toLowerCase();
        applyFilterAndSearch();
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
        allFilteredRooms.clear();
        allFilteredRooms.addAll(controller.getKamarList(currentFilter));
        applyFilterAndSearch();
    }

    private void applyFilterAndSearch() {
        currentRoomList.clear();

        for (Kamar k : allFilteredRooms) {
            if (searchKeyword.isEmpty()) {
                currentRoomList.add(k);
            } else {
                boolean matchKode = k.getKodeKamar().toLowerCase().contains(searchKeyword);
                boolean matchTipe = k.getTipe().toLowerCase().contains(searchKeyword);
                boolean matchStatus = k.getStatus().toLowerCase().contains(searchKeyword);
                boolean matchFas = false;
                for (String fas : k.getFasilitas()) {
                    if (fas.toLowerCase().contains(searchKeyword)) {
                        matchFas = true;
                        break;
                    }
                }
                if (matchKode || matchTipe || matchStatus || matchFas) {
                    currentRoomList.add(k);
                }
            }
        }

        tableModel.setRowCount(0);
        for (Kamar k : currentRoomList) {
            tableModel.addRow(new Object[]{
                    k.getKodeKamar(),
                    k.getTipe(),
                    k.getStatus(),
                    k.formatRupiah() + " / bln"
            });
        }

        lblRoomCount.setText("Menampilkan " + currentRoomList.size() + " Kamar Kos");

        if (!currentRoomList.isEmpty()) {
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
        if (txtSearch != null) txtSearch.setText("");
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
        BufferedImage img = ImageHelper.loadAndScale(photoPath, 420, 175, 8);
        lblDetailPhoto.setIcon(new ImageIcon(img));
        lblDetailPhoto.setText("");

        // 2. Info Teks Rata Tengah (Poin 4)
        lblDetailKode.setText("Kamar " + k.getKodeKamar());

        pnlDetailBadges.removeAll();
        pnlDetailBadges.add(PillBadge.createStatusBadge(k.getTipe()));
        pnlDetailBadges.add(PillBadge.createStatusBadge(k.getStatus()));

        lblDetailHarga.setText(k.formatRupiah() + " / bulan");
        lblDetailDeskripsi.setText("<html><center style='width: 360px; font-size: 11px; color: #5A5750;'>" +
                k.getDeskripsi() + "</center></html>");

        // 3. Spesifikasi Ruangan Rapi (Mengisi Ruang Kerja - Poin 2)
        pnlDetailSpecs.removeAll();
        pnlDetailSpecs.add(createSpecBadge("3 x 4 m²", "Luas Kamar"));
        pnlDetailSpecs.add(createSpecBadge("Springbed", "Kasur Single"));
        pnlDetailSpecs.add(createSpecBadge("K.M. Dalam", "K. Mandi"));
        pnlDetailSpecs.add(createSpecBadge("Token", "Listrik"));

        // 4. Fasilitas (Rata Tengah)
        pnlDetailFasilitas.removeAll();
        for (String fas : k.getFasilitas()) {
            JLabel lblFas = new JLabel("• " + fas);
            lblFas.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            lblFas.setForeground(new Color(0x1F, 0x5B, 0x41));
            lblFas.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(0xDC, 0xED, 0xE1), 1, true),
                    BorderFactory.createEmptyBorder(2, 6, 2, 6)
            ));
            pnlDetailFasilitas.add(lblFas);
        }

        // 5. Tombol Aksi
        pnlDetailAction.removeAll();
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        boolean isAdmin = (user != null && user.isAdmin());

        if (isAdmin) {
            JLabel lblAdminNote = new JLabel("Akun Admin hanya dapat meninjau detail kamar.", SwingConstants.CENTER);
            lblAdminNote.setFont(UITheme.FONT_SMALL);
            lblAdminNote.setForeground(UITheme.TEXT_MUTED);
            pnlDetailAction.add(lblAdminNote, BorderLayout.CENTER);
        } else if (k.isTersedia()) {
            PillButton btnBook = new PillButton("Ajukan Booking Kamar Ini →", PillButton.Style.PRIMARY);
            btnBook.setPreferredSize(new Dimension(0, 38));
            btnBook.addActionListener(e -> {
                if (user == null) {
                    if (callback != null) callback.onLoginRequested();
                } else {
                    StringBuilder reason = new StringBuilder();
                    if (!bookingController.canUserBook(user, k, reason)) {
                        KozzyDialogHelper.showWarning(this, "Tidak Memenuhi Ketentuan Kamar", reason.toString());
                        return;
                    }
                    if (callback != null) callback.onBookingRequested(k);
                }
            });
            pnlDetailAction.add(btnBook, BorderLayout.CENTER);
        } else if ("terisi".equalsIgnoreCase(k.getStatus())) {
            String hingga = (k.getTerisiHingga() != null) ? " s.d. " + k.getTerisiHingga() : "";
            JLabel lblNote = new JLabel("Kamar sedang dihuni penyewa" + hingga + ".", SwingConstants.CENTER);
            lblNote.setFont(UITheme.FONT_BODY_BOLD);
            lblNote.setForeground(UITheme.ACCENT_TERA);
            pnlDetailAction.add(lblNote, BorderLayout.CENTER);
        } else {
            JLabel lblNote = new JLabel("Kamar sedang dalam proses perbaikan/pemeliharaan.", SwingConstants.CENTER);
            lblNote.setFont(UITheme.FONT_BODY_BOLD);
            lblNote.setForeground(UITheme.WARNING_AMBER);
            pnlDetailAction.add(lblNote, BorderLayout.CENTER);
        }

        pnlDetailBadges.revalidate();
        pnlDetailBadges.repaint();
        pnlDetailSpecs.revalidate();
        pnlDetailSpecs.repaint();
        pnlDetailFasilitas.revalidate();
        pnlDetailFasilitas.repaint();
        pnlDetailAction.revalidate();
        pnlDetailAction.repaint();
    }

    private JPanel createSpecBadge(String title, String sub) {
        JPanel p = new JPanel(new GridLayout(2, 1, 0, 1));
        p.setBackground(UITheme.BG_CARD_ALT);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(3, 4, 3, 4)
        ));

        JLabel l1 = new JLabel(title, SwingConstants.CENTER);
        l1.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l1.setForeground(UITheme.TEXT_DARK);
        p.add(l1);

        JLabel l2 = new JLabel(sub, SwingConstants.CENTER);
        l2.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        l2.setForeground(UITheme.TEXT_MUTED);
        p.add(l2);

        return p;
    }

    private JPanel createSpecBadge(String icon, String title, String sub) {
        return createSpecBadge(title, sub);
    }

    private void clearDetailPreview() {
        lblDetailPhoto.setIcon(null);
        lblDetailPhoto.setText("Tidak Ada Kamar");
        lblDetailKode.setText("Tidak Ditemukan");
        pnlDetailBadges.removeAll();
        lblDetailHarga.setText("-");

        String keywordDisplay = (searchKeyword != null && !searchKeyword.isEmpty()) ? searchKeyword : currentFilter;
        lblDetailDeskripsi.setText("<html><center style='color:#C63624;'>Tidak ada kamar yang sesuai dengan kriteria '<b>" +
                keywordDisplay + "</b>'.<br><br><span style='color:#6E6A62;'>Coba ubah kata kunci atau reset filter pencarian.</span></center></html>");

        pnlDetailSpecs.removeAll();
        pnlDetailFasilitas.removeAll();
        pnlDetailAction.removeAll();

        PillButton btnReset = new PillButton("Reset & Tampilkan Semua Kamar", PillButton.Style.PRIMARY);
        btnReset.setPreferredSize(new Dimension(0, 38));
        btnReset.addActionListener(e -> {
            if (txtSearch != null) txtSearch.setText("");
            setFilter("semua");
        });
        pnlDetailAction.add(btnReset, BorderLayout.CENTER);

        revalidate();
        repaint();
    }
}
