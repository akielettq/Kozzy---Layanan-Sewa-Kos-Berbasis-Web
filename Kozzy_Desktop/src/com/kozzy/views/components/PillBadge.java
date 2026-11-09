package com.kozzy.views.components;

import com.kozzy.config.UITheme;

import javax.swing.*;
import java.awt.*;

/**
 * PillBadge: Komponen lencana status (chip/pill) bergaya desktop modern dan jelas.
 */
public class PillBadge extends JLabel {
    private Color bgColor;
    private Color fgColor;

    public PillBadge(String text, Color bg, Color fg) {
        super(text != null ? text.toUpperCase() : "");
        this.bgColor = bg;
        this.fgColor = fg;

        setFont(UITheme.FONT_BADGE);
        setForeground(fg);
        setOpaque(false);
        setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
    }

    public static PillBadge createStatusBadge(String status) {
        if (status == null) status = "-";
        String s = status.trim().toLowerCase();

        switch (s) {
            case "tersedia":
            case "approved":
            case "aktif":
                return new PillBadge(status.toUpperCase(), new Color(0xDC, 0xED, 0xE1), UITheme.PRIMARY_GREEN);
            case "terisi":
            case "rejected":
            case "ditolak":
                return new PillBadge(status.toUpperCase(), new Color(0xFB, 0xE5, 0xDF), new Color(0xBE, 0x3B, 0x12));
            case "pemeliharaan":
            case "perbaikan":
            case "pending":
            case "menunggu":
                String display = "pemeliharaan".equalsIgnoreCase(s) ? "PERBAIKAN" : status.toUpperCase();
                return new PillBadge(display, new Color(0xFE, 0xF3, 0xD6), new Color(0x8A, 0x5A, 0x0B));
            case "putri":
                return new PillBadge("PUTRI", UITheme.ACCENT_TERA, Color.WHITE);
            case "putra":
                return new PillBadge("PUTRA", UITheme.PRIMARY_GREEN, new Color(0xF4, 0xEF, 0xE4));
            default:
                return new PillBadge(status.toUpperCase(), UITheme.BADGE_MUTED_BG, UITheme.BADGE_MUTED_FG);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int w = getWidth();
        int h = getHeight();

        // Background kapsul bulat
        g2.setColor(bgColor != null ? bgColor : UITheme.BADGE_MUTED_BG);
        g2.fillRoundRect(0, 0, w, h, h, h);

        // Border rapi khas Kozzy Web
        Color borderClr = fgColor.equals(Color.WHITE) || fgColor.equals(new Color(0xF4, 0xEF, 0xE4))
                ? UITheme.BORDER_DARK
                : fgColor;
        g2.setColor(borderClr);
        g2.setStroke(new BasicStroke(1.2f));
        g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);

        g2.dispose();
        super.paintComponent(g);
    }
}
