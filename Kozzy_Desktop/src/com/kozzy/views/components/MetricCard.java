package com.kozzy.views.components;

import com.kozzy.config.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * MetricCard: Komponen kartu ringkasan KPI untuk Dashboard Desktop.
 * Menggunakan perataan tengah (Centered Alignment) sesuai standar visual modern (Poin 4).
 */
public class MetricCard extends JPanel {
    private final JLabel lblValue;
    private final JLabel lblSub;
    private final JLabel lblTitle;

    public MetricCard(String title, String initialValue, String subtitle, Color accentColor) {
        setLayout(new BorderLayout(0, 6));
        setBackground(UITheme.BG_CARD);
        setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // Top: Label Title + Accent Dot (Tengah)
        JPanel topPnl = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        topPnl.setOpaque(false);

        if (accentColor != null) {
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(accentColor);
                    g2.fillRoundRect(0, 3, 7, 7, 2, 2);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(8, 12);
                }
            };
            dot.setOpaque(false);
            topPnl.add(dot);
        }

        lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.TEXT_MUTED);
        lblTitle.setHorizontalAlignment(SwingConstants.CENTER);
        topPnl.add(lblTitle);
        add(topPnl, BorderLayout.NORTH);

        // Center: Big Value (Tengah)
        lblValue = new JLabel(initialValue, SwingConstants.CENTER);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblValue.setForeground(accentColor != null ? accentColor : UITheme.TEXT_DARK);
        add(lblValue, BorderLayout.CENTER);

        // Bottom: Subtitle / Context Note (Tengah)
        lblSub = new JLabel(subtitle, SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_SMALL);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        add(lblSub, BorderLayout.SOUTH);
    }

    public void setValue(String value) {
        lblValue.setText(value);
    }

    public void setSubtitle(String sub) {
        lblSub.setText(sub);
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Tactile shadow khas Kozzy Web
        g2.setColor(new Color(0x22, 0x1F, 0x19, 40));
        g2.fillRoundRect(3, 3, w - 5, h - 5, 14, 14);

        // Kartu Putih/Krem dengan sudut rounded
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w - 4, h - 4, 14, 14);

        // Border tinta retro-modern 1.4f
        g2.setColor(UITheme.BORDER_DARK);
        g2.setStroke(new BasicStroke(1.4f));
        g2.drawRoundRect(0, 0, w - 4, h - 4, 14, 14);

        g2.dispose();
        super.paintComponent(g);
    }
}
