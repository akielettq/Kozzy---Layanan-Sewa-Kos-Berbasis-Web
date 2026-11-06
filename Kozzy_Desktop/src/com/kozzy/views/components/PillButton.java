package com.kozzy.views.components;

import com.kozzy.config.UITheme;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * PillButton: Tombol kustom desktop dengan visual profesional, feedback hover/pressed halus.
 */
public class PillButton extends JButton {

    public enum Style {
        PRIMARY,    // Forest Green solid
        OUTLINE,    // Border outline dengan background transparan/putih
        TERA,       // Terracotta solid
        DANGER,     // Merah solid
        SECONDARY   // Abu-abu terang
    }

    private Style style;
    private boolean isHovered = false;
    private boolean isPressed = false;
    private int cornerRadius = 10;

    public PillButton(String text, Style style) {
        super(text);
        this.style = style;

        setFont(UITheme.FONT_BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (isEnabled()) {
                    isHovered = true;
                    repaint();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                isHovered = false;
                isPressed = false;
                repaint();
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (isEnabled()) {
                    isPressed = true;
                    repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                isPressed = false;
                repaint();
            }
        });
    }

    public void setStyle(Style style) {
        this.style = style;
        repaint();
    }

    public void setCornerRadius(int r) {
        this.cornerRadius = r;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        Color bg;
        Color border = null;
        Color fg;

        if (!isEnabled()) {
            bg = new Color(0xEB, 0xE7, 0xDF);
            fg = new Color(0x9E, 0x9A, 0x92);
        } else {
            switch (style) {
                case PRIMARY:
                    bg = isPressed ? new Color(0x12, 0x37, 0x26) : (isHovered ? UITheme.PRIMARY_HOVER : UITheme.PRIMARY_GREEN);
                    fg = Color.WHITE;
                    break;
                case TERA:
                    bg = isPressed ? new Color(0xB8, 0x43, 0x1E) : (isHovered ? new Color(0xD0, 0x4E, 0x24) : UITheme.ACCENT_TERA);
                    fg = Color.WHITE;
                    break;
                case DANGER:
                    bg = isPressed ? new Color(0x9E, 0x27, 0x18) : (isHovered ? new Color(0xB5, 0x2E, 0x1E) : UITheme.DANGER_RED);
                    fg = Color.WHITE;
                    break;
                case SECONDARY:
                    bg = isPressed ? new Color(0xDC, 0xD7, 0xCC) : (isHovered ? new Color(0xE8, 0xE4, 0xDA) : new Color(0xF0, 0xED, 0xE5));
                    border = UITheme.BORDER_COLOR;
                    fg = UITheme.TEXT_DARK;
                    break;
                case OUTLINE:
                default:
                    bg = isPressed ? new Color(0xEA, 0xE5, 0xD8) : (isHovered ? new Color(0xF5, 0xF2, 0xEA) : Color.WHITE);
                    border = UITheme.BORDER_DARK;
                    fg = UITheme.TEXT_DARK;
                    break;
            }
        }

        // Fill background
        g2.setColor(bg);
        g2.fillRoundRect(1, 1, w - 2, h - 2, cornerRadius, cornerRadius);

        // Draw border jika ada
        if (border != null && isEnabled()) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(1, 1, w - 3, h - 3, cornerRadius, cornerRadius);
        }

        g2.dispose();

        setForeground(fg);
        super.paintComponent(g);
    }
}
