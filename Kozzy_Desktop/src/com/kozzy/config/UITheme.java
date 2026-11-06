package com.kozzy.config;

import javax.swing.*;
import java.awt.*;

/**
 * UITheme: Menyediakan palet warna, font, dan token desain desktop workstation Kozzy.
 * Menggabungkan estetika retro-modern Kozzy dengan tata letak desktop workstation profesional.
 */
public class UITheme {
    // Palet Warna Utama
    public static final Color BG_CANVAS      = new Color(0xF4, 0xEF, 0xE4); // --kertas: #F4EFE4 (Warm Cream Canvas)
    public static final Color BG_CARD        = new Color(0xFF, 0xFF, 0xFF); // White Card / Table Surface
    public static final Color BG_CARD_ALT    = new Color(0xFB, 0xF8, 0xF0); // Soft Antique White
    public static final Color TEXT_DARK      = new Color(0x22, 0x1F, 0x19); // Primary Ink
    public static final Color TEXT_MUTED     = new Color(0x6E, 0x6A, 0x62); // Secondary / Muted Text
    public static final Color BORDER_COLOR   = new Color(0xD8, 0xD2, 0xC5); // Crisp divider line
    public static final Color BORDER_DARK    = new Color(0x22, 0x1F, 0x19); // Retro border outline

    // Warna Aksen & Tombol
    public static final Color PRIMARY_GREEN  = new Color(0x1F, 0x5B, 0x41); // Forest Green
    public static final Color PRIMARY_HOVER  = new Color(0x17, 0x47, 0x33); // Darker Forest Green
    public static final Color ACCENT_TERA    = new Color(0xE2, 0x59, 0x2C); // Terracotta Accent
    public static final Color WARNING_AMBER  = new Color(0xB4, 0x75, 0x14); // Amber / Pending
    public static final Color DANGER_RED     = new Color(0xC6, 0x36, 0x24); // Red Tolak

    // Sidebar Desktop Spesifik
    public static final Color SIDEBAR_BG     = new Color(0x1C, 0x26, 0x21); // Slate Deep Forest
    public static final Color SIDEBAR_HOVER  = new Color(0x27, 0x36, 0x2F); // Hover state
    public static final Color SIDEBAR_ACTIVE = new Color(0x23, 0x47, 0x37); // Active tab state
    public static final Color SIDEBAR_TEXT   = new Color(0xF5, 0xF3, 0xEE); // High contrast text
    public static final Color SIDEBAR_MUTED  = new Color(0x9E, 0xA8, 0xA1); // Sidebar secondary text

    // Lencana Status (Pills)
    public static final Color BADGE_GREEN_BG = new Color(0xDC, 0xED, 0xE1);
    public static final Color BADGE_GREEN_FG = new Color(0x13, 0x4E, 0x33);
    public static final Color BADGE_TERA_BG  = new Color(0xFB, 0xE5, 0xDF);
    public static final Color BADGE_TERA_FG  = new Color(0xBE, 0x3B, 0x12);
    public static final Color BADGE_AMBER_BG = new Color(0xFE, 0xF3, 0xD6);
    public static final Color BADGE_AMBER_FG = new Color(0x7D, 0x52, 0x0A);
    public static final Color BADGE_MUTED_BG = new Color(0xEA, 0xE5, 0xD9);
    public static final Color BADGE_MUTED_FG = new Color(0x5A, 0x57, 0x50);

    // Tipografi Desktop Modern
    public static final Font FONT_LOGO        = new Font("Segoe UI", Font.BOLD, 20);
    public static final Font FONT_HEADER      = new Font("Segoe UI", Font.BOLD, 22);
    public static final Font FONT_TITLE       = new Font("Segoe UI", Font.BOLD, 16);
    public static final Font FONT_SUBTITLE    = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FONT_BODY_BOLD   = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_BODY        = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL       = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BADGE       = new Font("Segoe UI", Font.BOLD, 11);

    /**
     * Konfigurasi FlatLaf Look and Feel untuk lingkungan Desktop Workstation.
     */
    public static void setupFlatLaf() {
        try {
            com.formdev.flatlaf.FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 8);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.trackArc", 999);
            UIManager.put("Panel.background", BG_CANVAS);
            UIManager.put("Table.background", BG_CARD);
            UIManager.put("Table.selectionBackground", new Color(0xDC, 0xED, 0xE1));
            UIManager.put("Table.selectionForeground", TEXT_DARK);
            UIManager.put("Table.rowHeight", 36);
            UIManager.put("TableHeader.background", BG_CARD_ALT);
            UIManager.put("TableHeader.font", FONT_BODY_BOLD);
            UIManager.put("TableHeader.separatorColor", BORDER_COLOR);
        } catch (Throwable t) {
            System.err.println("FlatLaf tidak tersedia, menggunakan Look & Feel sistem: " + t.getMessage());
        }
    }
}
