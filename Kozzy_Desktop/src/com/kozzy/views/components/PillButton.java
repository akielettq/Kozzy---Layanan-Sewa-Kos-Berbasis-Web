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
    private int cornerRadius = 999;

    public PillButton(String text, Style style) {
        super(text);
        this.style = style;

        setFont(UITheme.FONT_BODY_BOLD);
        setFocusPainted(false);
        setBorderPainted(false);
        setContentAreaFilled(false);
        setOpaque(false);
        setCursor(new Cursor(Cursor.HAND_CURSOR));
        setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));

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
        int r = (cornerRadius >= 100) ? h : cornerRadius;

        Color bg;
        Color border = UITheme.BORDER_DARK;
        Color fg;

        if (!isEnabled()) {
            bg = new Color(0xEB, 0xE7, 0xDF);
            border = new Color(0xD8, 0xD2, 0xC5);
            fg = new Color(0x9E, 0x9A, 0x92);
        } else {
            switch (style) {
                case PRIMARY:
                    bg = isPressed ? new Color(0x15, 0x40, 0x2F) : (isHovered ? UITheme.PRIMARY_HOVER : UITheme.PRIMARY_GREEN);
                    border = UITheme.BORDER_DARK;
                    fg = new Color(0xF4, 0xEF, 0xE4); // var(--kertas)
                    break;
                case TERA:
                    bg = isPressed ? new Color(0xB8, 0x43, 0x1E) : (isHovered ? new Color(0xD0, 0x4E, 0x24) : UITheme.ACCENT_TERA);
                    border = UITheme.BORDER_DARK;
                    fg = Color.WHITE;
                    break;
                case DANGER:
                    bg = isPressed ? new Color(0x9E, 0x27, 0x18) : (isHovered ? new Color(0xB5, 0x2E, 0x1E) : UITheme.DANGER_RED);
                    border = UITheme.BORDER_DARK;
                    fg = Color.WHITE;
                    break;
                case SECONDARY:
                    bg = isPressed ? new Color(0xDC, 0xD7, 0xCC) : (isHovered ? new Color(0xEA, 0xE5, 0xDA) : new Color(0xF4, 0xEF, 0xE4));
                    border = UITheme.BORDER_DARK;
                    fg = UITheme.TEXT_DARK;
                    break;
                case OUTLINE:
                default:
                    bg = isPressed ? new Color(0xEA, 0xE5, 0xD8) : (isHovered ? new Color(0xF8, 0xF5, 0xEE) : Color.WHITE);
                    border = UITheme.BORDER_DARK;
                    fg = UITheme.TEXT_DARK;
                    break;
            }
        }

        int shiftX = 0;
        int shiftY = 0;
        if (isEnabled()) {
            if (isPressed) {
                shiftX = 1;
                shiftY = 1;
            } else if (isHovered) {
                shiftX = -2;
                shiftY = -2;
            }
        }

        // Tactile drop shadow khas Kozzy Web (box-shadow: 3px 3px 0 var(--tinta))
        if (isHovered && !isPressed && isEnabled()) {
            g2.setColor(new Color(0x22, 0x1F, 0x19, 160));
            g2.fillRoundRect(2, 2, w - 4, h - 4, r, r);
        }

        // Fill background button surface
        g2.setColor(bg);
        g2.fillRoundRect(2 + shiftX, 2 + shiftY, w - 4, h - 4, r, r);

        // Draw crisp retro ink border
        if (border != null && isEnabled()) {
            g2.setColor(border);
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawRoundRect(2 + shiftX, 2 + shiftY, w - 5, h - 5, r, r);
        }

        g2.dispose();

        // Render button label text and icon with synchronized physical shift
        Graphics2D gText = (Graphics2D) g.create();
        gText.translate(shiftX, shiftY);
        setForeground(fg);
        super.paintComponent(gText);
        gText.dispose();
    }
}
