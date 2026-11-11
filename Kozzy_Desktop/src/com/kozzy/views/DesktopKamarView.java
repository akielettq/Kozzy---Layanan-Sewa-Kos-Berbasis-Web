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
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/** Katalog kamar berbentuk card; daftar dan detail tidak lagi ditampilkan berdampingan. */
public class DesktopKamarView extends JPanel {
    public interface KamarActionCallback {
        void onBookingRequested(Kamar kamar);
        void onLoginRequested();
    }

    private final KamarActionCallback callback;
    private final KamarController controller = new KamarController();
    private final List<Kamar> currentRoomList = new ArrayList<>();
    private JPanel cardsPanel;
    private JPanel filterRow;
    private JLabel resultLabel;
    private String currentFilter = "semua";
    private PillButton btnSemua, btnPutri, btnPutra, btnTersedia;

    public DesktopKamarView(KamarActionCallback callback) {
        this.callback = callback;
        setLayout(new BorderLayout(0, 12));
        setOpaque(false);
        setBorder(new EmptyBorder(18, 22, 18, 22));
        initUI();
    }

    private void initUI() {
        JPanel top = new JPanel(new BorderLayout(10, 8));
        top.setOpaque(false);
        JPanel titleBox = new JPanel();
        titleBox.setOpaque(false);
        titleBox.setLayout(new BoxLayout(titleBox, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Katalog Kamar");
        title.setFont(UITheme.FONT_HEADER);
        title.setForeground(UITheme.TEXT_DARK);
        JLabel subtitle = new JLabel("Pilih kamar yang sesuai, lalu lihat detail atau ajukan booking.");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        titleBox.add(title);
        titleBox.add(Box.createVerticalStrut(3));
        titleBox.add(subtitle);
        top.add(titleBox, BorderLayout.WEST);

        filterRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 7, 0));
        filterRow.setOpaque(false);
        btnSemua = new PillButton("Semua Kamar", PillButton.Style.PRIMARY);
        btnPutri = new PillButton("Kos Putri", PillButton.Style.OUTLINE);
        btnPutra = new PillButton("Kos Putra", PillButton.Style.OUTLINE);
        btnTersedia = new PillButton("Hanya Tersedia", PillButton.Style.OUTLINE);
        btnSemua.addActionListener(e -> setFilter("semua"));
        btnPutri.addActionListener(e -> setFilter("putri"));
        btnPutra.addActionListener(e -> setFilter("putra"));
        btnTersedia.addActionListener(e -> setFilter("tersedia"));
        filterRow.add(btnSemua); filterRow.add(btnPutri); filterRow.add(btnPutra); filterRow.add(btnTersedia);
        top.add(filterRow, BorderLayout.EAST);
        top.add(resultLabel = new JLabel(""), BorderLayout.SOUTH);
        resultLabel.setFont(UITheme.FONT_SMALL);
        resultLabel.setForeground(UITheme.TEXT_MUTED);
        add(top, BorderLayout.NORTH);

        cardsPanel = new JPanel(new GridLayout(0, 5, 12, 12));
        cardsPanel.setOpaque(false);
        cardsPanel.setBorder(new EmptyBorder(2, 1, 2, 1));
        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        add(scroll, BorderLayout.CENTER);

        cardsPanel.addComponentListener(new ComponentAdapter() {
            @Override public void componentResized(ComponentEvent e) { updateGridColumns(); }
        });
        loadKamarList();
    }

    private void updateGridColumns() {
        int width = cardsPanel.getWidth();
        int columns = width >= 1100 ? 5 : width >= 850 ? 4 : 3;
        GridLayout layout = (GridLayout) cardsPanel.getLayout();
        if (layout.getColumns() != columns) {
            layout.setColumns(columns);
            cardsPanel.revalidate();
        }
    }

    public void setFilter(String filter) {
        currentFilter = filter;
        btnSemua.setStyle(PillButton.Style.OUTLINE);
        btnPutri.setStyle(PillButton.Style.OUTLINE);
        btnPutra.setStyle(PillButton.Style.OUTLINE);
        btnTersedia.setStyle(PillButton.Style.OUTLINE);
        if ("putri".equals(filter)) btnPutri.setStyle(PillButton.Style.PRIMARY);
        else if ("putra".equals(filter)) btnPutra.setStyle(PillButton.Style.PRIMARY);
        else if ("tersedia".equals(filter)) btnTersedia.setStyle(PillButton.Style.PRIMARY);
        else btnSemua.setStyle(PillButton.Style.PRIMARY);
        loadKamarList();
    }

    public void loadKamarList() {
        currentRoomList.clear();
        currentRoomList.addAll(controller.getKamarList(currentFilter));
        cardsPanel.removeAll();
        resultLabel.setText(currentRoomList.size() + " kamar ditampilkan");
        for (Kamar kamar : currentRoomList) cardsPanel.add(createRoomCard(kamar));
        // Filter (mis. Putri/Putra) hanya menghasilkan 1 baris sehingga GridLayout meregangkannya
        // setinggi viewport. Tambahkan sel kosong sampai jumlahnya sama dengan filter 'semua'
        // agar ukuran kartu selalu sama di semua filter.
        int totalKamar = "semua".equals(currentFilter)
                ? currentRoomList.size() : controller.getKamarList("semua").size();
        for (int i = currentRoomList.size(); i < totalKamar; i++) {
            JPanel filler = new JPanel();
            filler.setOpaque(false);
            cardsPanel.add(filler);
        }
        cardsPanel.revalidate();
        cardsPanel.repaint();
        updateGridColumns();
    }

    public void selectRoomByKode(String kodeKamar) {
        setFilter("semua");
        for (Kamar kamar : currentRoomList) {
            if (kamar.getKodeKamar().equalsIgnoreCase(kodeKamar)) {
                showDetail(kamar);
                break;
            }
        }
    }

    private JPanel createRoomCard(Kamar kamar) {
        JPanel card = new JPanel(new BorderLayout(0, 7));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(8, 8, 8, 8)));

        JLabel photo = new JLabel();
        photo.setHorizontalAlignment(SwingConstants.CENTER);
        photo.setOpaque(true);
        photo.setBackground(UITheme.BG_CARD_ALT);
        photo.setPreferredSize(new Dimension(190, 112));
        BufferedImage image = ImageHelper.loadAndScale("assets/kamar/" + kamar.getKodeKamar() + ".jpg", 220, 125, 8);
        photo.setIcon(new ImageIcon(image));
        card.add(photo, BorderLayout.NORTH);

        JPanel info = new JPanel();
        info.setOpaque(false);
        info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
        JLabel code = new JLabel(kamar.getKodeKamar());
        code.setFont(new Font("Segoe UI", Font.BOLD, 17));
        code.setForeground(UITheme.TEXT_DARK);
        code.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(code);
        info.add(Box.createVerticalStrut(5));

        JPanel badges = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        badges.setOpaque(false);
        badges.setAlignmentX(Component.LEFT_ALIGNMENT);
        badges.add(PillBadge.createStatusBadge(kamar.getStatus()));
        info.add(badges);
        info.add(Box.createVerticalStrut(7));

        JLabel price = new JLabel(kamar.formatRupiah());
        price.setFont(new Font("Segoe UI", Font.BOLD, 14));
        price.setForeground(UITheme.PRIMARY_GREEN);
        price.setAlignmentX(Component.LEFT_ALIGNMENT);
        info.add(price);
        card.add(info, BorderLayout.CENTER);

        JPanel actions = new JPanel(new GridLayout(1, 2, 7, 0));
        actions.setOpaque(false);
        PillButton detail = new PillButton("Detail", PillButton.Style.OUTLINE);
        PillButton booking = new PillButton("Booking", PillButton.Style.PRIMARY);
        detail.setBorder(new EmptyBorder(7, 5, 7, 5));
        booking.setBorder(new EmptyBorder(7, 5, 7, 5));
        detail.addActionListener(e -> showDetail(kamar));
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        boolean admin = user != null && user.isAdmin();
        booking.setEnabled(kamar.isTersedia() && !admin);
        if (admin) booking.setToolTipText("Akun admin hanya dapat melihat detail kamar");
        else if (!kamar.isTersedia()) booking.setToolTipText("Kamar belum tersedia untuk booking");
        booking.addActionListener(e -> requestBooking(kamar));
        actions.add(detail);
        actions.add(booking);
        card.add(actions, BorderLayout.SOUTH);
        return card;
    }

    private void requestBooking(Kamar kamar) {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            if (callback != null) callback.onLoginRequested();
        } else if (kamar.isTersedia() && !user.isAdmin()) {
            if (callback != null) callback.onBookingRequested(kamar);
        }
    }

    private void showDetail(Kamar kamar) {
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog(owner, "Detail Kamar " + kamar.getKodeKamar(), Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setBackground(UITheme.BG_CARD_ALT);

        JPanel shell = new JPanel(new BorderLayout(0, 0));
        shell.setBackground(UITheme.BG_CARD_ALT);
        shell.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                new EmptyBorder(18, 20, 18, 20)));

        // Header dialog: judul kecil dan tombol close yang konsisten dengan tema Kozzy.
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel eyebrow = new JLabel("DETAIL KAMAR");
        eyebrow.setFont(new Font("Segoe UI", Font.BOLD, 11));
        eyebrow.setForeground(UITheme.PRIMARY_GREEN);
        header.add(eyebrow, BorderLayout.WEST);

        JButton closeButton = new JButton("×");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 23));
        closeButton.setForeground(UITheme.TEXT_DARK);
        closeButton.setBackground(UITheme.BG_CARD);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        closeButton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(0, 9, 2, 9)));
        closeButton.setPreferredSize(new Dimension(36, 34));
        closeButton.addActionListener(e -> dialog.dispose());
        header.add(closeButton, BorderLayout.EAST);
        shell.add(header, BorderLayout.NORTH);

        // Isi detail dapat di-scroll pada layar kecil; area tombol tetap terlihat.
        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(new EmptyBorder(12, 0, 8, 0));
        GridBagConstraints bodyGbc = new GridBagConstraints();
        bodyGbc.gridx = 0;
        bodyGbc.weightx = 1;
        bodyGbc.anchor = GridBagConstraints.WEST;

        JLabel photo = new JLabel();
        photo.setHorizontalAlignment(SwingConstants.CENTER);
        photo.setOpaque(true);
        photo.setBackground(UITheme.BG_CARD);
        photo.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        photo.setIcon(new ImageIcon(ImageHelper.loadAndScale(
                "assets/kamar/" + kamar.getKodeKamar() + ".jpg", 490, 225, 14)));
        photo.setPreferredSize(new Dimension(490, 227));
        photo.setMinimumSize(new Dimension(490, 227));
        bodyGbc.gridy = 0;
        bodyGbc.fill = GridBagConstraints.NONE;
        bodyGbc.anchor = GridBagConstraints.CENTER;
        bodyGbc.insets = new Insets(0, 0, 12, 0);
        body.add(photo, bodyGbc);

        // Nama kamar dan status ditempatkan dalam satu baris agar mudah dipindai.
        JPanel titleRow = new JPanel(new BorderLayout(12, 0));
        titleRow.setOpaque(false);
        titleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));

        JPanel titleGroup = new JPanel();
        titleGroup.setOpaque(false);
        titleGroup.setLayout(new BoxLayout(titleGroup, BoxLayout.Y_AXIS));
        titleGroup.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel roomName = new JLabel(kamar.getKodeKamar());
        roomName.setFont(new Font("Segoe UI", Font.BOLD, 25));
        roomName.setForeground(UITheme.TEXT_DARK);
        JLabel roomType = new JLabel("Kos " + capitalize(kamar.getTipe()));
        roomType.setFont(UITheme.FONT_BODY);
        roomType.setForeground(UITheme.TEXT_MUTED);
        titleGroup.add(roomName);
        titleGroup.add(Box.createVerticalStrut(1));
        titleGroup.add(roomType);
        titleGroup.add(Box.createVerticalStrut(6));
        titleGroup.add(PillBadge.createStatusBadge(kamar.getStatus()));
        titleRow.add(titleGroup, BorderLayout.WEST);
        bodyGbc.gridy = 1;
        bodyGbc.fill = GridBagConstraints.HORIZONTAL;
        bodyGbc.anchor = GridBagConstraints.WEST;
        bodyGbc.insets = new Insets(0, 0, 10, 0);
        body.add(titleRow, bodyGbc);

        // Harga dibuat sebagai panel aksen, bukan sekadar teks kecil di antara informasi.
        JPanel pricePanel = new JPanel(new BorderLayout(8, 0));
        pricePanel.setBackground(new Color(0xE8, 0xF1, 0xE9));
        pricePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xD2, 0xE2, 0xD5), 1, true),
                new EmptyBorder(11, 14, 11, 14)));
        pricePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 67));
        JLabel priceCaption = new JLabel("HARGA SEWA");
        priceCaption.setFont(new Font("Segoe UI", Font.BOLD, 10));
        priceCaption.setForeground(UITheme.TEXT_MUTED);
        JLabel price = new JLabel(kamar.formatRupiah());
        price.setFont(new Font("Segoe UI", Font.BOLD, 22));
        price.setForeground(UITheme.PRIMARY_GREEN);
        JPanel priceText = new JPanel();
        priceText.setOpaque(false);
        priceText.setLayout(new BoxLayout(priceText, BoxLayout.Y_AXIS));
        priceText.add(priceCaption);
        priceText.add(Box.createVerticalStrut(2));
        priceText.add(price);
        pricePanel.add(priceText, BorderLayout.WEST);
        bodyGbc.gridy = 2;
        bodyGbc.insets = new Insets(0, 0, 11, 0);
        body.add(pricePanel, bodyGbc);

        JLabel descriptionTitle = detailSectionTitle("TENTANG KAMAR");
        bodyGbc.gridy = 3;
        bodyGbc.insets = new Insets(0, 0, 5, 0);
        body.add(descriptionTitle, bodyGbc);
        JTextArea description = new JTextArea(
                kamar.getDeskripsi() == null || kamar.getDeskripsi().trim().isEmpty()
                        ? "Belum ada deskripsi untuk kamar ini."
                        : kamar.getDeskripsi());
        description.setFont(UITheme.FONT_BODY);
        description.setForeground(UITheme.TEXT_MUTED);
        description.setLineWrap(true);
        description.setWrapStyleWord(true);
        description.setEditable(false);
        description.setFocusable(false);
        description.setOpaque(false);
        description.setBorder(null);
        description.setRows(2);
        description.setColumns(45);
        bodyGbc.gridy = 4;
        bodyGbc.fill = GridBagConstraints.HORIZONTAL;
        bodyGbc.insets = new Insets(0, 0, 8, 0);
        body.add(description, bodyGbc);

        JLabel facilitiesTitle = detailSectionTitle("FASILITAS KAMAR");
        bodyGbc.gridy = 5;
        bodyGbc.insets = new Insets(0, 0, 6, 0);
        body.add(facilitiesTitle, bodyGbc);

        JPanel facilities = new JPanel(new GridLayout(0, 2, 8, 8));
        facilities.setOpaque(false);
        facilities.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        List<String> facilityItems = kamar.getFasilitas();
        if (facilityItems == null || facilityItems.isEmpty()) {
            facilities.add(createFacilityChip("Fasilitas standar"));
        } else {
            for (String item : facilityItems) {
                if (item != null && !item.trim().isEmpty()) {
                    facilities.add(createFacilityChip(item.trim()));
                }
            }
        }
        bodyGbc.gridy = 6;
        bodyGbc.insets = new Insets(0, 0, 0, 0);
        bodyGbc.fill = GridBagConstraints.HORIZONTAL;
        bodyGbc.anchor = GridBagConstraints.WEST;
        body.add(facilities, bodyGbc);

        JScrollPane bodyScroll = new JScrollPane(body);
        bodyScroll.setBorder(null);
        bodyScroll.setOpaque(false);
        bodyScroll.getViewport().setOpaque(false);
        bodyScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        bodyScroll.getVerticalScrollBar().setUnitIncrement(14);
        bodyScroll.getVerticalScrollBar().setPreferredSize(new Dimension(7, 0));
        shell.add(bodyScroll, BorderLayout.CENTER);

        // Footer terpisah agar aksi utama selalu berada di bagian bawah dialog.
        JPanel footer = new JPanel(new BorderLayout(10, 0));
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, UITheme.BORDER_COLOR),
                new EmptyBorder(14, 0, 0, 0)));

        JLabel hint = new JLabel(kamar.isTersedia() ? "Tertarik dengan kamar ini?" : "Kamar belum dapat dipesan.");
        hint.setFont(UITheme.FONT_SMALL);
        hint.setForeground(UITheme.TEXT_MUTED);
        footer.add(hint, BorderLayout.WEST);

        JPanel footerActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        footerActions.setOpaque(false);
        PillButton closeFooter = new PillButton("Tutup", PillButton.Style.OUTLINE);
        closeFooter.addActionListener(e -> dialog.dispose());
        PillButton booking = new PillButton("Booking Kamar Ini", PillButton.Style.PRIMARY);
        booking.setEnabled(kamar.isTersedia());
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user != null && user.isAdmin()) {
            booking.setEnabled(false);
            booking.setToolTipText("Akun admin hanya dapat melihat detail kamar");
        } else if (!kamar.isTersedia()) {
            booking.setToolTipText("Kamar belum tersedia untuk booking");
        }
        booking.addActionListener(e -> {
            dialog.dispose();
            requestBooking(kamar);
        });
        closeFooter.setPreferredSize(new Dimension(92, 40));
        booking.setPreferredSize(new Dimension(205, 40));
        footerActions.add(closeFooter);
        footerActions.add(booking);
        footer.add(footerActions, BorderLayout.EAST);
        shell.add(footer, BorderLayout.SOUTH);

        dialog.setContentPane(shell);
        dialog.getRootPane().setDefaultButton(booking);
        dialog.getRootPane().registerKeyboardAction(
                e -> dialog.dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int dialogWidth = Math.min(570, screen.width - 80);
        int dialogHeight = Math.min(735, screen.height - 90);
        dialog.setSize(dialogWidth, dialogHeight);
        dialog.setMinimumSize(new Dimension(Math.min(500, dialogWidth), Math.min(560, dialogHeight)));
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    private JLabel detailSectionTitle(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 11));
        label.setForeground(UITheme.TEXT_DARK);
        return label;
    }

    private JPanel createFacilityChip(String text) {
        JPanel chip = new JPanel(new BorderLayout(7, 0));
        chip.setBackground(UITheme.BG_CARD);
        chip.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                new EmptyBorder(7, 9, 7, 9)));
        JLabel check = new JLabel("✓");
        check.setFont(new Font("Segoe UI", Font.BOLD, 12));
        check.setForeground(UITheme.PRIMARY_GREEN);
        JLabel value = new JLabel(text);
        value.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        value.setForeground(UITheme.TEXT_DARK);
        chip.add(check, BorderLayout.WEST);
        chip.add(value, BorderLayout.CENTER);
        chip.setPreferredSize(new Dimension(220, 34));
        return chip;
    }

    private String capitalize(String value) {
        if (value == null || value.trim().isEmpty()) return "";
        String normalized = value.trim().toLowerCase();
        return normalized.substring(0, 1).toUpperCase() + normalized.substring(1);
    }

}
