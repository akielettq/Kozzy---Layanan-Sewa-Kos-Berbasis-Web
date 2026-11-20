package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.BerandaController;
import com.kozzy.controllers.BerandaController.BerandaSummary;
import com.kozzy.views.components.ImageHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

public class DesktopBerandaView extends JPanel {

    public interface BerandaActionCallback {
        void onSelectRoom(String kodeKamar);
        void onNavigateToKamar();
        void onNavigateToLogin();
    }

    private final BerandaActionCallback callback;
    private final BerandaController controller = new BerandaController();

    private static final String[] SLIDE_PATHS = {
            "assets/utamaa.jpg",
            "assets/kamar/A-01.jpg",
            "assets/putra.png",
            "assets/putri.png"
    };
    private final BufferedImage[] slideImages = new BufferedImage[SLIDE_PATHS.length];
    private int currentSlide = 0;
    private Timer carouselTimer;

    private HeroPanel heroPanel;
    private final IndicatorDash[] indicatorDashes = new IndicatorDash[SLIDE_PATHS.length];
    private JLabel lblRoomCount;
    private JLabel lblRoomSubtitle;
    private JPanel bubbleCard;

    public DesktopBerandaView(BerandaActionCallback callback) {
        this.callback = callback;

        setLayout(new BorderLayout(0, 10));
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(10, 20, 16, 20));

        loadSlideImages();
        initUI();
        initCarouselTimer();
        refreshData();
    }

    private void loadSlideImages() {
        for (int i = 0; i < SLIDE_PATHS.length; i++) {
            try {
                File file = ImageHelper.resolveFile(SLIDE_PATHS[i]);
                if (file != null && file.exists()) {
                    slideImages[i] = ImageIO.read(file);
                }
            } catch (Exception e) {
                System.err.println("[DesktopBerandaView] Galat memuat slide " + SLIDE_PATHS[i] + ": " + e.getMessage());
            }
        }
    }

    private void initCarouselTimer() {
        carouselTimer = new Timer(5500, e -> {
            currentSlide = (currentSlide + 1) % SLIDE_PATHS.length;
            updateSlideView();
        });
        carouselTimer.start();
    }

    private void updateSlideView() {
        for (int i = 0; i < indicatorDashes.length; i++) {
            if (indicatorDashes[i] != null) {
                indicatorDashes[i].setActive(i == currentSlide);
            }
        }
        if (heroPanel != null) {
            heroPanel.repaint();
        }
    }

    private void setSlide(int index) {
        if (index >= 0 && index < SLIDE_PATHS.length) {
            currentSlide = index;
            updateSlideView();
            if (carouselTimer != null) {
                carouselTimer.restart();
            }
        }
    }

    private void prevSlide() {
        currentSlide = (currentSlide - 1 + SLIDE_PATHS.length) % SLIDE_PATHS.length;
        updateSlideView();
        if (carouselTimer != null) {
            carouselTimer.restart();
        }
    }

    private void nextSlide() {
        currentSlide = (currentSlide + 1) % SLIDE_PATHS.length;
        updateSlideView();
        if (carouselTimer != null) {
            carouselTimer.restart();
        }
    }

    private void initUI() {
        // TOP HEADER BAR
        JPanel headerBar = new JPanel(new BorderLayout());
        headerBar.setOpaque(false);
        headerBar.setBorder(BorderFactory.createEmptyBorder(2, 6, 8, 6));

        // Brand Logo di Kiri
        JPanel brandBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
        brandBox.setOpaque(false);

        JLabel lblLogoText = new JLabel("kozzy");
        lblLogoText.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblLogoText.setForeground(new Color(0x3B, 0x2A, 0x68));
        brandBox.add(lblLogoText);

        JLabel lblLogoDot = new JLabel(".");
        lblLogoDot.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblLogoDot.setForeground(UITheme.ACCENT_TERA);
        brandBox.add(lblLogoDot);

        headerBar.add(brandBox, BorderLayout.WEST);

        // Tombol Pill CTA di Kanan ("Lihat Kamar")
        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightActions.setOpaque(false);

        JButton btnViewHomes = new JButton("Lihat Kamar") {
            private boolean isHover = false;
            {
                setFont(new Font("Segoe UI", Font.BOLD, 13));
                setForeground(new Color(0x4A, 0x36, 0x7B));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                if (isHover) {
                    g2.setColor(new Color(0xEE, 0xEA, 0xF8));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);

                g2.setColor(new Color(0x5E, 0x48, 0x93));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        btnViewHomes.addActionListener(e -> {
            if (callback != null) callback.onNavigateToKamar();
        });
        rightActions.add(btnViewHomes);

        headerBar.add(rightActions, BorderLayout.EAST);
        add(headerBar, BorderLayout.NORTH);

        // HERO SHOWCASE PANEL
        heroPanel = new HeroPanel();
        heroPanel.setLayout(new BorderLayout(0, 0));
        heroPanel.setBorder(BorderFactory.createEmptyBorder(18, 16, 26, 16));

        // A. Top-Center: Slider Indicators
        JPanel sliderIndicatorPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        sliderIndicatorPanel.setOpaque(false);
        for (int i = 0; i < SLIDE_PATHS.length; i++) {
            final int slideIdx = i;
            indicatorDashes[i] = new IndicatorDash(i == currentSlide);
            indicatorDashes[i].addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    setSlide(slideIdx);
                }
            });
            sliderIndicatorPanel.add(indicatorDashes[i]);
        }
        heroPanel.add(sliderIndicatorPanel, BorderLayout.NORTH);

        // B. Center: Container with Left/Right Nav Arrows and Headline Typography
        JPanel centerNavContainer = new JPanel(new BorderLayout(14, 0));
        centerNavContainer.setOpaque(false);

        // Tombol Panah Kiri (Prev)
        JPanel prevBox = new JPanel(new GridBagLayout());
        prevBox.setOpaque(false);
        CarouselArrowButton btnPrev = new CarouselArrowButton(false);
        btnPrev.addActionListener(e -> prevSlide());
        prevBox.add(btnPrev);
        centerNavContainer.add(prevBox, BorderLayout.WEST);

        // Center: Headline Typography
        JPanel headlinePanel = new JPanel();
        headlinePanel.setLayout(new BoxLayout(headlinePanel, BoxLayout.Y_AXIS));
        headlinePanel.setOpaque(false);
        headlinePanel.setBorder(BorderFactory.createEmptyBorder(55, 12, 0, 0));

        JLabel lblExclusive = new JLabel("Exclusive");
        lblExclusive.setFont(new Font("Segoe UI", Font.PLAIN, 32));
        lblExclusive.setForeground(new Color(255, 255, 255, 245));
        lblExclusive.setAlignmentX(Component.LEFT_ALIGNMENT);
        headlinePanel.add(lblExclusive);

        headlinePanel.add(Box.createVerticalStrut(2));

        JLabel lblKost = new JLabel("Kost Putra & Putri");
        lblKost.setFont(new Font("Segoe UI", Font.BOLD, 52));
        lblKost.setForeground(Color.WHITE);
        lblKost.setAlignmentX(Component.LEFT_ALIGNMENT);
        headlinePanel.add(lblKost);

        headlinePanel.add(Box.createVerticalStrut(14));

        JLabel lblDesc = new JLabel("<html><body style='width: 500px; line-height: 1.5; color: #F0F0F0; font-size: 13px;'>"
                + "Nikmati hunian nyaman, aman, dan bebas repot dengan fasilitas lengkap di lokasi strategis Pekanbaru."
                + "</body></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblDesc.setForeground(new Color(240, 240, 240));
        lblDesc.setAlignmentX(Component.LEFT_ALIGNMENT);
        headlinePanel.add(lblDesc);

        centerNavContainer.add(headlinePanel, BorderLayout.CENTER);

        // Tombol Panah Kanan (Next)
        JPanel nextBox = new JPanel(new GridBagLayout());
        nextBox.setOpaque(false);
        CarouselArrowButton btnNext = new CarouselArrowButton(true);
        btnNext.addActionListener(e -> nextSlide());
        nextBox.add(btnNext);
        centerNavContainer.add(nextBox, BorderLayout.EAST);

        heroPanel.add(centerNavContainer, BorderLayout.CENTER);

        // C. Bottom: Split (West = Lencana Kamar Tersedia, East = Widget WhatsApp)
        JPanel bottomContainer = new JPanel(new BorderLayout(20, 0));
        bottomContainer.setOpaque(false);
        bottomContainer.setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));

        // WEST: Lencana Kamar Tersedia Real-Time
        JPanel pnlBadgeKamar = createAvailableRoomBadge();
        bottomContainer.add(pnlBadgeKamar, BorderLayout.WEST);

        // EAST: Widget WhatsApp (Bubble Greeting + Floating Button)
        JPanel pnlWhatsAppWidget = createWhatsAppWidget();
        bottomContainer.add(pnlWhatsAppWidget, BorderLayout.EAST);

        heroPanel.add(bottomContainer, BorderLayout.SOUTH);

        add(heroPanel, BorderLayout.CENTER);
    }

    private JPanel createAvailableRoomBadge() {
        JPanel badge = new JPanel(new BorderLayout(14, 0)) {
            private boolean isHover = false;
            {
                setOpaque(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 22));
                setPreferredSize(new Dimension(410, 62));

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHover = false;
                        repaint();
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (callback != null) callback.onNavigateToKamar();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Frosted Glass Effect
                Color bg = isHover ? new Color(0, 0, 0, 160) : new Color(0, 0, 0, 120);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, w - 1, h - 1, 14, 14);

                g2.setColor(isHover ? new Color(255, 255, 255, 120) : new Color(255, 255, 255, 75));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawRoundRect(0, 0, w - 1, h - 1, 14, 14);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        // Indikator Titik Hijau + Angka Kamar
        JPanel numBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        numBox.setOpaque(false);

        JPanel greenDot = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0x25, 0xD3, 0x66));
                g2.fillOval(0, 4, 10, 10);
                g2.dispose();
            }

            @Override
            public Dimension getPreferredSize() {
                return new Dimension(10, 18);
            }
        };
        greenDot.setOpaque(false);
        numBox.add(greenDot);

        lblRoomCount = new JLabel("0");
        lblRoomCount.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblRoomCount.setForeground(Color.WHITE);
        numBox.add(lblRoomCount);

        badge.add(numBox, BorderLayout.WEST);

        // Deskripsi Status
        JPanel textBox = new JPanel(new GridLayout(2, 1, 0, 2));
        textBox.setOpaque(false);

        JLabel lblTitle = new JLabel("Kamar Tersedia Siap Huni");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(Color.WHITE);
        textBox.add(lblTitle);

        lblRoomSubtitle = new JLabel("Kamar siap huni di Gedung A & B • Klik untuk lihat");
        lblRoomSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblRoomSubtitle.setForeground(new Color(0xDD, 0xDD, 0xDD));
        textBox.add(lblRoomSubtitle);

        badge.add(textBox, BorderLayout.CENTER);

        return badge;
    }

    private JPanel createWhatsAppWidget() {
        JPanel widget = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 0));
        widget.setOpaque(false);

        // Speech Bubble Greeting Card
        bubbleCard = new JPanel(new BorderLayout(10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Drop shadow
                g2.setColor(new Color(0, 0, 0, 45));
                g2.fillRoundRect(2, 3, w - 4, h - 4, 12, 12);

                // White card surface
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(0, 0, w - 2, h - 2, 12, 12);

                // Border
                g2.setColor(new Color(0xDC, 0xD6, 0xCB));
                g2.setStroke(new BasicStroke(1.0f));
                g2.drawRoundRect(0, 0, w - 2, h - 2, 12, 12);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        bubbleCard.setOpaque(false);
        bubbleCard.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 12));

        JPanel bubbleText = new JPanel(new GridLayout(3, 1, 0, 2));
        bubbleText.setOpaque(false);
        bubbleText.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 8));

        JLabel lblHi = new JLabel("Hai! Ada pertanyaan seputar kamar Kozzy?");
        lblHi.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblHi.setForeground(UITheme.TEXT_DARK);
        bubbleText.add(lblHi);

        JLabel lblSub = new JLabel("Hubungi tim pengelola kami via WhatsApp sekarang!");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblSub.setForeground(UITheme.TEXT_MUTED);
        bubbleText.add(lblSub);

        JLabel lblHours = new JLabel("Jam operasional: 08.00 - 22.00 WIB");
        lblHours.setFont(new Font("Segoe UI", Font.BOLD, 9));
        lblHours.setForeground(new Color(0x1F, 0x5B, 0x41));
        bubbleText.add(lblHours);

        bubbleCard.add(bubbleText, BorderLayout.CENTER);

        JButton btnCloseBubble = new JButton("×");
        btnCloseBubble.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCloseBubble.setForeground(UITheme.TEXT_MUTED);
        btnCloseBubble.setFocusPainted(false);
        btnCloseBubble.setBorderPainted(false);
        btnCloseBubble.setContentAreaFilled(false);
        btnCloseBubble.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCloseBubble.addActionListener(e -> bubbleCard.setVisible(false));
        bubbleCard.add(btnCloseBubble, BorderLayout.EAST);

        widget.add(bubbleCard);

        // Tombol Bulat WhatsApp
        JButton btnWhatsApp = new JButton() {
            private boolean isHover = false;
            {
                setPreferredSize(new Dimension(54, 54));
                setFocusPainted(false);
                setContentAreaFilled(false);
                setBorderPainted(false);
                setCursor(new Cursor(Cursor.HAND_CURSOR));
                setToolTipText("Hubungi Pengelola Kozzy via WhatsApp");

                addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        isHover = true;
                        repaint();
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        isHover = false;
                        repaint();
                    }
                });
            }

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();
                int d = Math.min(w, h) - 6;
                int x = (w - d) / 2;
                int y = (h - d) / 2;

                // Shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillOval(x + 1, y + 2, d, d);

                // WhatsApp Green Circle
                Color waColor = isHover ? new Color(0x1E, 0xBE, 0x5D) : new Color(0x25, 0xD3, 0x66);
                g2.setColor(waColor);
                g2.fillOval(x, y, d, d);

                // White ring border
                g2.setColor(new Color(255, 255, 255, 120));
                g2.setStroke(new BasicStroke(1.2f));
                g2.drawOval(x, y, d, d);

                // WhatsApp Speech Bubble Icon
                int cx = w / 2;
                int cy = h / 2;
                int r = d / 2 - 10;

                g2.setColor(Color.WHITE);
                Area bubble = new Area(new Ellipse2D.Float(cx - r, cy - r, r * 2, r * 2));
                Path2D tail = new Path2D.Float();
                tail.moveTo(cx - r * 0.7f, cy + r * 0.5f);
                tail.lineTo(cx - r * 0.95f, cy + r * 1.0f);
                tail.lineTo(cx - r * 0.25f, cy + r * 0.8f);
                tail.closePath();
                bubble.add(new Area(tail));
                g2.fill(bubble);

                // Inner phone silhouette in WA green
                g2.setColor(waColor);
                Path2D phone = new Path2D.Float();
                phone.moveTo(cx - 5, cy - 4);
                phone.curveTo(cx - 3, cy - 7, cx + 3, cy - 7, cx + 5, cy - 4);
                phone.lineTo(cx + 4, cy - 1);
                phone.curveTo(cx + 2, cy - 2, cx - 2, cy + 2, cx - 1, cy + 4);
                phone.lineTo(cx - 4, cy + 5);
                phone.curveTo(cx - 7, cy + 3, cx - 7, cy - 3, cx - 5, cy - 4);
                phone.closePath();
                g2.fill(phone);

                g2.dispose();
            }
        };

        btnWhatsApp.addActionListener(e -> showWhatsAppHelpDialog());
        widget.add(btnWhatsApp);

        return widget;
    }

    private void showWhatsAppHelpDialog() {
        Window parentWin = SwingUtilities.getWindowAncestor(this);
        JDialog dlg = new JDialog(parentWin instanceof Frame ? (Frame) parentWin : null, "Pusat Bantuan & Pengelola Kozzy", true);
        dlg.setLayout(new BorderLayout());
        dlg.setSize(520, 380);
        dlg.setLocationRelativeTo(parentWin);
        dlg.setResizable(false);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(UITheme.BG_CARD);
        content.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel lblTitle = new JLabel("Hubungi Pengelola Kozzy");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblTitle);

        JLabel lblSub = new JLabel("Layanan informasi, survei lokasi, & reservasi");
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(lblSub);

        content.add(Box.createVerticalStrut(20));

        // WhatsApp Number Card
        JPanel cardPhone = new JPanel();
        cardPhone.setLayout(new BoxLayout(cardPhone, BoxLayout.Y_AXIS));
        cardPhone.setBackground(new Color(0xFB, 0xFC, 0xFA));
        cardPhone.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0xDC, 0xED, 0xE1), 1, true),
                BorderFactory.createEmptyBorder(14, 24, 14, 24)
        ));
        cardPhone.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel lblWaTag = new JLabel("NOMOR WHATSAPP RESMI");
        lblWaTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblWaTag.setForeground(new Color(0x1F, 0x5B, 0x41));
        lblWaTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPhone.add(lblWaTag);

        cardPhone.add(Box.createVerticalStrut(4));

        final String phoneNum = "0896-6132-7050";
        JLabel lblNum = new JLabel(phoneNum);
        lblNum.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblNum.setForeground(UITheme.TEXT_DARK);
        lblNum.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPhone.add(lblNum);

        cardPhone.add(Box.createVerticalStrut(12));

        JButton btnCopy = new JButton("Salin Nomor Telepon");
        btnCopy.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCopy.setForeground(Color.WHITE);
        btnCopy.setBackground(new Color(0x1F, 0x5B, 0x41));
        btnCopy.setFocusPainted(false);
        btnCopy.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCopy.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCopy.setBorder(BorderFactory.createEmptyBorder(8, 22, 8, 22));

        btnCopy.addActionListener(ev -> {
            StringSelection sel = new StringSelection(phoneNum.replace("-", ""));
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            btnCopy.setText("✓ Nomor Berhasil Disalin!");
            btnCopy.setBackground(new Color(0x25, 0xD3, 0x66));
            Timer t = new Timer(2200, te -> {
                btnCopy.setText("Salin Nomor Telepon");
                btnCopy.setBackground(new Color(0x1F, 0x5B, 0x41));
            });
            t.setRepeats(false);
            t.start();
        });
        cardPhone.add(btnCopy);

        content.add(cardPhone);
        content.add(Box.createVerticalStrut(18));

        // Info Detail
        JPanel infoGrid = new JPanel(new GridLayout(2, 1, 0, 4));
        infoGrid.setOpaque(false);

        JLabel lblJam = new JLabel("Jam Operasional: Setiap Hari (08.00 - 22.00 WIB)", SwingConstants.CENTER);
        lblJam.setFont(UITheme.FONT_SMALL);
        lblJam.setForeground(UITheme.TEXT_MUTED);
        infoGrid.add(lblJam);

        JLabel lblLokasi = new JLabel("Lokasi: Jl. Bangau Perkasa, Pekanbaru", SwingConstants.CENTER);
        lblLokasi.setFont(UITheme.FONT_SMALL);
        lblLokasi.setForeground(UITheme.TEXT_MUTED);
        infoGrid.add(lblLokasi);

        content.add(infoGrid);
        content.add(Box.createVerticalStrut(16));

        // Tombol Tutup
        JButton btnClose = new JButton("Tutup");
        btnClose.setFont(UITheme.FONT_BODY_BOLD);
        btnClose.setForeground(UITheme.TEXT_MUTED);
        btnClose.setBackground(UITheme.BG_CARD_ALT);
        btnClose.setFocusPainted(false);
        btnClose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnClose.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnClose.addActionListener(ev -> dlg.dispose());
        content.add(btnClose);

        dlg.getContentPane().add(content, BorderLayout.CENTER);
        dlg.setVisible(true);
    }

    /**
     * Memperbarui data ringkasan ketersediaan kamar dari Controller
     */
    public void refreshData() {
        BerandaSummary summary = controller.getSummary();
        int avail = summary.getKamarTersedia();
        int total = summary.getTotalKamar();

        if (lblRoomCount != null) {
            lblRoomCount.setText(String.valueOf(avail));
        }
        if (lblRoomSubtitle != null) {
            lblRoomSubtitle.setText(avail + " dari " + total + " kamar siap huni • Klik untuk lihat");
        }

        revalidate();
        repaint();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        if (carouselTimer != null && carouselTimer.isRunning()) {
            carouselTimer.stop();
        }
    }

    @Override
    public void addNotify() {
        super.addNotify();
        if (carouselTimer != null && !carouselTimer.isRunning()) {
            carouselTimer.start();
        }
    }

    // =========================================================================
    // CUSTOM HERO CANVAS PANEL
    // =========================================================================
    private class HeroPanel extends JPanel {
        public HeroPanel() {
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

            int w = getWidth();
            int h = getHeight();
            int cornerRadius = 22;

            // Clip to rounded rectangle
            Shape roundedClip = new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius);
            g2.setClip(roundedClip);

            // Draw current slide image (Aspect-Fill)
            BufferedImage img = (currentSlide >= 0 && currentSlide < slideImages.length) ? slideImages[currentSlide] : null;
            if (img != null) {
                double scale = Math.max((double) w / img.getWidth(), (double) h / img.getHeight());
                int drawW = (int) Math.ceil(img.getWidth() * scale);
                int drawH = (int) Math.ceil(img.getHeight() * scale);
                int drawX = (w - drawW) / 2;
                int drawY = (h - drawH) / 2;

                g2.drawImage(img, drawX, drawY, drawW, drawH, null);
            } else {
                // Fallback elegant gradient
                Paint p = new GradientPaint(0, 0, new Color(0x3B, 0x48, 0x40), w, h, new Color(0x1C, 0x26, 0x21));
                g2.setPaint(p);
                g2.fillRect(0, 0, w, h);
            }

            // Dark vignette/gradient overlay dari kiri ke kanan (agar teks putih terbaca tajam)
            Paint leftGradient = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 160),
                    (int) (w * 0.70), 0, new Color(0, 0, 0, 25)
            );
            g2.setPaint(leftGradient);
            g2.fillRect(0, 0, w, h);

            // Dark vignette/gradient overlay dari bawah ke atas
            Paint bottomGradient = new GradientPaint(
                    0, (int) (h * 0.45), new Color(0, 0, 0, 0),
                    0, h, new Color(0, 0, 0, 175)
            );
            g2.setPaint(bottomGradient);
            g2.fillRect(0, 0, w, h);

            // Soft top gradient untuk slider dash
            Paint topGradient = new GradientPaint(
                    0, 0, new Color(0, 0, 0, 90),
                    0, 70, new Color(0, 0, 0, 0)
            );
            g2.setPaint(topGradient);
            g2.fillRect(0, 0, w, 70);

            // Frame border outline
            g2.setColor(new Color(255, 255, 255, 50));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(0, 0, w - 1, h - 1, cornerRadius, cornerRadius);

            g2.dispose();
        }
    }

    // SLIDER INDICATOR DASH COMPONENT ([- - - -])
    private static class IndicatorDash extends JComponent {
        private boolean active;
        private boolean hover = false;

        public IndicatorDash(boolean active) {
            this.active = active;
            setPreferredSize(new Dimension(36, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }
            });
        }

        public void setActive(boolean active) {
            this.active = active;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            int barH = active ? 4 : 3;
            int y = (h - barH) / 2;

            if (active) {
                g2.setColor(Color.WHITE);
            } else if (hover) {
                g2.setColor(new Color(255, 255, 255, 210));
            } else {
                g2.setColor(new Color(255, 255, 255, 110));
            }

            g2.fillRoundRect(2, y, w - 4, barH, 2, 2);
            g2.dispose();
        }
    }

    // CAROUSEL MANUAL NAVIGATION ARROW BUTTON (‹ / ›)
    private static class CarouselArrowButton extends JButton {
        private final boolean isNext;
        private boolean hover = false;
        private boolean pressed = false;

        public CarouselArrowButton(boolean isNext) {
            this.isNext = isNext;
            setPreferredSize(new Dimension(38, 38));
            setMinimumSize(new Dimension(38, 38));
            setMaximumSize(new Dimension(38, 38));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            setToolTipText(isNext ? "Foto Berikutnya" : "Foto Sebelumnya");

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    pressed = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    pressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    pressed = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            int w = getWidth();
            int h = getHeight();

            // Background lingkaran bergaya frosted glass semi-transparan
            Color bg;
            if (pressed) {
                bg = new Color(0, 0, 0, 180);
            } else if (hover) {
                bg = new Color(0, 0, 0, 140);
            } else {
                bg = new Color(0, 0, 0, 75);
            }
            g2.setColor(bg);
            g2.fillOval(1, 1, w - 2, h - 2);

            // Garis tepi tipis elegan
            Color border = hover ? new Color(255, 255, 255, 160) : new Color(255, 255, 255, 75);
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawOval(1, 1, w - 2, h - 2);

            // Chevron panah vektor tajam
            float cx = w / 2.0f;
            float cy = h / 2.0f;

            Color arrowColor = hover ? Color.WHITE : new Color(255, 255, 255, 225);
            g2.setColor(arrowColor);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D chevron = new Path2D.Float();
            if (isNext) {
                // Panah Kanan: >
                chevron.moveTo(cx - 2.5f, cy - 6.5f);
                chevron.lineTo(cx + 3.0f, cy);
                chevron.lineTo(cx - 2.5f, cy + 6.5f);
            } else {
                // Panah Kiri: <
                chevron.moveTo(cx + 2.5f, cy - 6.5f);
                chevron.lineTo(cx - 3.0f, cy);
                chevron.lineTo(cx + 2.5f, cy + 6.5f);
            }
            g2.draw(chevron);

            g2.dispose();
        }
    }
}