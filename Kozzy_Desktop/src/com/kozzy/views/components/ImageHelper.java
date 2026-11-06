package com.kozzy.views.components;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * ImageHelper: Utilitas pemuatan gambar, caching, penskalaan halus, dan pemotongan rounded corner.
 */
public class ImageHelper {

    public static File resolveFile(String path) {
        if (path == null || path.trim().isEmpty()) return null;

        File f = new File(path);
        if (f.exists()) return f;

        // 1. Coba direktori Kozzy_Desktop
        File fDeskA = new File("d:/Antigrav/Kozzy_Desktop", path);
        if (fDeskA.exists()) return fDeskA;
        File fDeskB = new File("D:/Antigrav/Kozzy_Desktop", path);
        if (fDeskB.exists()) return fDeskB;

        // 2. Cek relatif dari direktori user saat ini
        File fUser = new File(System.getProperty("user.dir"), path);
        if (fUser.exists()) return fUser;

        // 3. Cek subdirektori assets
        String relPath = path.replace("assets/", "").replace("assets\\", "");
        File fSubDesk = new File("d:/Antigrav/Kozzy_Desktop/assets", relPath);
        if (fSubDesk.exists()) return fSubDesk;

        File fSubUser = new File(new File(System.getProperty("user.dir"), "assets"), relPath);
        if (fSubUser.exists()) return fSubUser;

        // 4. Fallback ke Kozzy_GUI_NoDB atau Kozzy_GUI jika aset berada di sana
        File fFallback1 = new File("d:/Antigrav/Kozzy_GUI_NoDB/assets", relPath);
        if (fFallback1.exists()) return fFallback1;

        File fFallback2 = new File("d:/Antigrav/Kozzy_GUI/assets", relPath);
        if (fFallback2.exists()) return fFallback2;

        return f;
    }

    public static BufferedImage loadAndScale(String path, int targetWidth, int targetHeight, int cornerRadius) {
        try {
            File file = resolveFile(path);
            if (file != null && file.exists()) {
                BufferedImage original = ImageIO.read(file);
                if (original != null) {
                    return scaleAndClip(original, targetWidth, targetHeight, cornerRadius);
                }
            }
        } catch (Exception e) {
            System.err.println("[ImageHelper] Galat memuat gambar (" + path + "): " + e.getMessage());
        }

        // Placeholder cadangan jika file gambar tidak ditemukan
        BufferedImage placeholder = new BufferedImage(Math.max(1, targetWidth), Math.max(1, targetHeight), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = placeholder.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(0xE8, 0xEE, 0xE9));
        if (cornerRadius > 0) {
            g2.fillRoundRect(0, 0, targetWidth, targetHeight, cornerRadius, cornerRadius);
        } else {
            g2.fillRect(0, 0, targetWidth, targetHeight);
        }
        g2.setColor(new Color(0x6A, 0x82, 0x75));
        g2.setFont(new Font("Segoe UI", Font.BOLD, 13));
        String text = (path != null && path.contains("/")) ? path.substring(path.lastIndexOf('/') + 1) : "Foto Kamar";
        FontMetrics fm = g2.getFontMetrics();
        int tx = (targetWidth - fm.stringWidth(text)) / 2;
        int ty = (targetHeight + fm.getAscent() - fm.getDescent()) / 2;
        g2.drawString(text, tx, ty);
        g2.dispose();
        return placeholder;
    }

    private static BufferedImage scaleAndClip(BufferedImage src, int width, int height, int cornerRadius) {
        BufferedImage scaled = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (cornerRadius > 0) {
            g2.setClip(new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        }

        // Pertahankan rasio aspek gambar (cover crop)
        double scaleX = (double) width / src.getWidth();
        double scaleY = (double) height / src.getHeight();
        double scale = Math.max(scaleX, scaleY);

        int drawW = (int) (src.getWidth() * scale);
        int drawH = (int) (src.getHeight() * scale);
        int drawX = (width - drawW) / 2;
        int drawY = (height - drawH) / 2;

        g2.drawImage(src, drawX, drawY, drawW, drawH, null);
        g2.dispose();
        return scaled;
    }
}
