package com.kozzy.views.components;

import com.kozzy.config.UITheme;
import com.kozzy.models.Pengguna;
import com.kozzy.services.SessionManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * DesktopSidebar: Komponen navigasi kiri (Navigation Rail) khas aplikasi desktop profesional.
 * Menyediakan akses 1-klik instan ke seluruh modul tanpa navigasi bertingkat atau scroll panjang.
 */
public class DesktopSidebar extends JPanel {

    public interface NavListener {
        void onNavSelected(String cardKey);
    }

    private final NavListener listener;
    private final List<NavItem> navItems = new ArrayList<>();
    private NavItem activeItem;

    private NavItem itemBeranda;
    private NavItem itemKamar;
    private NavItem itemSewaku;
    private NavItem itemAdmin;
    private NavItem itemProfil;

    private JPanel userCardPanel;

    public DesktopSidebar(NavListener listener) {
        this.listener = listener;

        setPreferredSize(new Dimension(240, 0));
        setMinimumSize(new Dimension(240, 0));
        setBackground(UITheme.SIDEBAR_BG);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // ==========================================
        // 1. HEADER BRAND
        // ==========================================
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(22, 18, 18, 18));

        JPanel brandRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandRow.setOpaque(false);
        brandRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Logo dengan kontainer kapsul terang agar kontras tajam
        JPanel logoCard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(UITheme.BG_CANVAS);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        logoCard.setOpaque(false);
        logoCard.setPreferredSize(new Dimension(34, 34));
        BufferedImage logo = ImageHelper.loadAndScale("assets/logo kos.png", 26, 26, 0);
        JLabel lblLogo = new JLabel(new ImageIcon(logo), SwingConstants.CENTER);
        logoCard.add(lblLogo, BorderLayout.CENTER);
        brandRow.add(logoCard);

        JLabel lblName = new JLabel("KOZZY");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(UITheme.SIDEBAR_TEXT);
        brandRow.add(lblName);

        JLabel lblDot = new JLabel(".");
        lblDot.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblDot.setForeground(UITheme.ACCENT_TERA);
        brandRow.add(lblDot);

        headerPanel.add(brandRow);
        headerPanel.add(Box.createVerticalStrut(6));

        JLabel lblTag = new JLabel("Sewa Kos Putra & Putri");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTag.setForeground(UITheme.SIDEBAR_MUTED);
        lblTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(lblTag);

        headerPanel.add(Box.createVerticalStrut(14));

        // Separator tipis
        JSeparator sep = new JSeparator();
        sep.setForeground(new Color(0x2C, 0x3E, 0x33));
        sep.setBackground(new Color(0x2C, 0x3E, 0x33));
        sep.setMaximumSize(new Dimension(Integer.MAX_VALUE, 1));
        headerPanel.add(sep);

        add(headerPanel, BorderLayout.NORTH);

        // ==========================================
        // 2. MENU ITEM NAVIGASI
        // ==========================================
        JPanel menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        menuPanel.setOpaque(false);
        menuPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

        itemBeranda = new NavItem("BERANDA", "🏠", "Beranda");
        itemKamar   = new NavItem("KAMAR",   "🛏️", "Katalog Kamar");
        itemSewaku  = new NavItem("SEWAKU",  "📋", "Sewa Saya");
        itemAdmin   = new NavItem("ADMIN",   "🛡️", "Panel Admin (Pisah)");
        itemProfil  = new NavItem("PROFIL",  "👤", "Profil Akun");

        navItems.add(itemBeranda);
        navItems.add(itemKamar);
        navItems.add(itemSewaku);
        navItems.add(itemAdmin);
        navItems.add(itemProfil);

        menuPanel.add(itemBeranda);
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(itemKamar);
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(itemSewaku);
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(itemAdmin);
        menuPanel.add(Box.createVerticalStrut(6));
        menuPanel.add(itemProfil);

        menuPanel.add(Box.createVerticalGlue());
        add(menuPanel, BorderLayout.CENTER);

        // ==========================================
        // 3. USER FOOTER CARD
        // ==========================================
        userCardPanel = new JPanel(new BorderLayout(8, 0));
        userCardPanel.setOpaque(false);
        userCardPanel.setBorder(BorderFactory.createEmptyBorder(12, 14, 16, 14));
        add(userCardPanel, BorderLayout.SOUTH);

        // Set default active
        setActiveKey("BERANDA");
        updateUserState();

        // Listen for session changes
        SessionManager.getInstance().addSessionListener(user -> updateUserState());
    }

    public void updateUserState() {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        boolean isLoggedIn = (user != null);
        boolean isAdmin = (user != null && user.isAdmin());

        itemAdmin.setVisible(isAdmin);
        itemProfil.setVisible(isLoggedIn);
        itemSewaku.setVisible(!isAdmin); // Sewaku hanya untuk pencari

        userCardPanel.removeAll();

        if (!isLoggedIn) {
            PillButton btnLogin = new PillButton("Masuk ke Akun", PillButton.Style.PRIMARY);
            btnLogin.setCornerRadius(8);
            btnLogin.setPreferredSize(new Dimension(210, 38));
            btnLogin.addActionListener(e -> {
                if (listener != null) listener.onNavSelected("LOGIN");
            });
            userCardPanel.add(btnLogin, BorderLayout.CENTER);
        } else {
            JPanel card = new JPanel(new BorderLayout(10, 0)) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(0x26, 0x37, 0x2E));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            card.setOpaque(false);
            card.setCursor(new Cursor(Cursor.HAND_CURSOR));
            card.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));

            // Inisial avatar bundar
            String initial = (user.getNama() != null && !user.getNama().isEmpty())
                    ? user.getNama().substring(0, 1).toUpperCase() : "U";
            JLabel lblAvatar = new JLabel(initial, SwingConstants.CENTER) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(user.isAdmin() ? UITheme.ACCENT_TERA : UITheme.PRIMARY_GREEN);
                    g2.fillOval(0, 0, getWidth(), getHeight());
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            lblAvatar.setPreferredSize(new Dimension(34, 34));
            lblAvatar.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblAvatar.setForeground(Color.WHITE);
            card.add(lblAvatar, BorderLayout.WEST);

            // Nama & Role
            JPanel textPnl = new JPanel();
            textPnl.setLayout(new BoxLayout(textPnl, BoxLayout.Y_AXIS));
            textPnl.setOpaque(false);

            String displayName = user.getNama();
            if (displayName.length() > 14) displayName = displayName.substring(0, 12) + "...";
            JLabel lblUserName = new JLabel(displayName);
            lblUserName.setFont(new Font("Segoe UI", Font.BOLD, 12));
            lblUserName.setForeground(UITheme.SIDEBAR_TEXT);
            lblUserName.setAlignmentX(Component.LEFT_ALIGNMENT);
            textPnl.add(lblUserName);

            JLabel lblRole = new JLabel(user.isAdmin() ? "Administrator" : "Pencari Kos");
            lblRole.setFont(new Font("Segoe UI", Font.PLAIN, 10));
            lblRole.setForeground(UITheme.SIDEBAR_MUTED);
            lblRole.setAlignmentX(Component.LEFT_ALIGNMENT);
            textPnl.add(lblRole);

            card.add(textPnl, BorderLayout.CENTER);

            // Tombol Logout vektor presisi
            JButton btnLogout = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(UITheme.SIDEBAR_MUTED);
                    int w = getWidth(), h = getHeight();
                    int cx = w / 2, cy = h / 2;
                    g2.drawRect(cx - 5, cy - 6, 6, 12);
                    g2.drawLine(cx - 1, cy, cx + 5, cy);
                    g2.drawLine(cx + 2, cy - 3, cx + 5, cy);
                    g2.drawLine(cx + 2, cy + 3, cx + 5, cy);
                    g2.dispose();
                }
            };
            btnLogout.setPreferredSize(new Dimension(24, 24));
            btnLogout.setToolTipText("Keluar dari Akun");
            btnLogout.setFocusPainted(false);
            btnLogout.setBorderPainted(false);
            btnLogout.setContentAreaFilled(false);
            btnLogout.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnLogout.addActionListener(e -> {
                boolean confirm = UITheme.showConfirm(
                        this,
                        "Konfirmasi Keluar",
                        "Apakah kamu yakin ingin keluar dari akun saat ini?",
                        "Keluar Akun",
                        PillButton.Style.DANGER
                );
                if (confirm) {
                    SessionManager.getInstance().logout();
                    if (listener != null) listener.onNavSelected("BERANDA");
                }
            });
            card.add(btnLogout, BorderLayout.EAST);

            // Klik card membuka profil
            card.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (listener != null) listener.onNavSelected("PROFIL");
                }
            });

            userCardPanel.add(card, BorderLayout.CENTER);
        }

        revalidate();
        repaint();
    }

    public void setActiveKey(String cardKey) {
        for (NavItem item : navItems) {
            boolean isCurrent = item.cardKey.equals(cardKey);
            item.setActive(isCurrent);
            if (isCurrent) {
                this.activeItem = item;
            }
        }
        repaint();
    }

    // ==========================================
    // INNER CLASS: NAV ITEM
    // ==========================================
    private class NavItem extends JPanel {
        private final String cardKey;
        private final String iconText;
        private final String titleText;
        private boolean isActive = false;
        private boolean isHover = false;

        private final JLabel lblIcon;
        private final JLabel lblTitle;

        public NavItem(String cardKey, String iconText, String titleText) {
            this.cardKey = cardKey;
            this.iconText = iconText;
            this.titleText = titleText;

            setLayout(new BorderLayout(12, 0));
            setOpaque(false);
            setPreferredSize(new Dimension(212, 40));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            lblIcon = new JLabel(iconText);
            lblIcon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
            lblIcon.setForeground(UITheme.SIDEBAR_MUTED);
            add(lblIcon, BorderLayout.WEST);

            lblTitle = new JLabel(titleText);
            lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
            lblTitle.setForeground(UITheme.SIDEBAR_MUTED);
            add(lblTitle, BorderLayout.CENTER);

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
                    if (listener != null) {
                        listener.onNavSelected(cardKey);
                    }
                }
            });
        }

        public void setActive(boolean active) {
            this.isActive = active;
            lblTitle.setForeground(active ? UITheme.SIDEBAR_TEXT : UITheme.SIDEBAR_MUTED);
            lblTitle.setFont(new Font("Segoe UI", active ? Font.BOLD : Font.PLAIN, 13));
            lblIcon.setForeground(active ? Color.WHITE : UITheme.SIDEBAR_MUTED);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();

            if (isActive) {
                // Background item aktif
                g2.setColor(UITheme.SIDEBAR_ACTIVE);
                g2.fillRoundRect(0, 0, w, h, 8, 8);

                // Indikator garis aksen di sisi kiri
                g2.setColor(new Color(0x48, 0xC7, 0x8E));
                g2.fillRoundRect(0, 4, 4, h - 8, 2, 2);
            } else if (isHover) {
                // Background item hover
                g2.setColor(UITheme.SIDEBAR_HOVER);
                g2.fillRoundRect(0, 0, w, h, 8, 8);
            }

            g2.dispose();
            super.paintComponent(g);
        }
    }
}
