package com.kozzy.views.components;

import com.kozzy.config.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * MetricCard: Komponen kartu ringkasan KPI untuk Dashboard Desktop.
 */
public class MetricCard extends JPanel {
    private final JLabel lblValue;
    private final JLabel lblSub;

    public MetricCard(String title, String initialValue, String subtitle, Color accentColor) {
        setLayout(new BorderLayout(10, 8));
        setBackground(UITheme.BG_CARD);
        setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));

        // Top: Label Title + Accent Bar
        JPanel topPnl = new JPanel(new BorderLayout());
        topPnl.setOpaque(false);

        JLabel lblTitle = new JLabel(title.toUpperCase());
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblTitle.setForeground(UITheme.TEXT_MUTED);
        topPnl.add(lblTitle, BorderLayout.WEST);

        if (accentColor != null) {
            JPanel dot = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(accentColor);
                    g2.fillOval(0, 2, 8, 8);
                    g2.dispose();
                }

                @Override
                public Dimension getPreferredSize() {
                    return new Dimension(10, 12);
                }
            };
            dot.setOpaque(false);
            topPnl.add(dot, BorderLayout.EAST);
        }
        add(topPnl, BorderLayout.NORTH);

        // Center: Big Value
        lblValue = new JLabel(initialValue);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblValue.setForeground(UITheme.TEXT_DARK);
        add(lblValue, BorderLayout.CENTER);

        // Bottom: Subtitle / Context Note
        lblSub = new JLabel(subtitle);
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

        // Kartu Putih dengan sudut rounded
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, w - 1, h - 1, 12, 12);

        // Border halus
        g2.setColor(UITheme.BORDER_COLOR);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, 12, 12);

        g2.dispose();
        super.paintComponent(g);
    }
}
