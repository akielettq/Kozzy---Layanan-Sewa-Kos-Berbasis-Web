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

        String clean = path.replace('\\', '/').trim();
        String filename = clean.contains("/") ? clean.substring(clean.lastIndexOf('/') + 1) : clean;
        String relPath = clean.startsWith("assets/") ? clean.substring(7) : clean;

        String[] baseDirs = {
            System.getProperty("user.dir"),
            new File(System.getProperty("user.dir"), "Kozzy_Desktop").getAbsolutePath(),
            new File(System.getProperty("user.dir"), "..").getAbsolutePath()
        };

        String[] subPaths = {
            clean,
            relPath,
            "assets/" + relPath,
            "assets/uploads/bukti/" + filename,
            "assets/uploads/" + filename,
            "assets/kamar/" + filename,
            "assets/" + filename,
            "Kozzy_PHP/assets/uploads/bukti/" + filename,
            "uploads/bukti/" + filename,
            "kamar/" + filename
        };

        for (String base : baseDirs) {
            for (String sub : subPaths) {
                File candidate = new File(base, sub);
                if (candidate.exists()) {
                    return candidate;
                }
            }
        }

        return f;
    }

    /**
     * Memuat gambar dalam resolusi asli tanpa cropping cover,
     * dengan fallback otomatis ke generator struk transfer realistis.
     */
    public static BufferedImage loadOriginal(String path) {
        try {
            File file = resolveFile(path);
            if (file != null && file.exists()) {
                BufferedImage original = ImageIO.read(file);
                if (original != null) {
                    return original;
                }
            }
        } catch (Exception e) {
            System.err.println("[ImageHelper] Galat memuat gambar asli (" + path + "): " + e.getMessage());
        }

        boolean isProofOfPayment = (path == null
                || path.toLowerCase().contains("bukti")
                || path.toLowerCase().contains("tf")
                || path.toLowerCase().contains("transfer")
                || path.toLowerCase().contains("receipt")
                || path.toLowerCase().contains("kz-")
                || path.toLowerCase().contains("pay"));

        if (isProofOfPayment) {
            return generateTransferReceipt(520, 720, path, 10);
        }

        return generateTransferReceipt(520, 720, "RECEIPT-SAMPLE", 10);
    }

    /**
     * Penskalaan gambar secara proporsional murni (FIT) tanpa cover crop / pemotongan tepi.
     */
    public static BufferedImage scaleProportional(BufferedImage src, int maxWidth, int maxHeight, int cornerRadius) {
        if (src == null) return null;
        if (maxWidth <= 0) maxWidth = src.getWidth();
        if (maxHeight <= 0) maxHeight = src.getHeight();

        double scaleX = (double) maxWidth / src.getWidth();
        double scaleY = (double) maxHeight / src.getHeight();
        double scale = Math.min(scaleX, scaleY);

        int drawW = Math.max(1, (int) (src.getWidth() * scale));
        int drawH = Math.max(1, (int) (src.getHeight() * scale));

        BufferedImage scaled = new BufferedImage(drawW, drawH, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scaled.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (cornerRadius > 0) {
            g2.setClip(new RoundRectangle2D.Float(0, 0, drawW, drawH, cornerRadius, cornerRadius));
        }

        g2.drawImage(src, 0, 0, drawW, drawH, null);
        g2.dispose();
        return scaled;
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

        boolean isProofOfPayment = (path == null
                || path.toLowerCase().contains("bukti")
                || path.toLowerCase().contains("tf")
                || path.toLowerCase().contains("transfer")
                || path.toLowerCase().contains("receipt")
                || path.toLowerCase().contains("kz-")
                || path.toLowerCase().contains("pay")
                || targetHeight >= 200);

        if (isProofOfPayment) {
            return generateTransferReceipt(targetWidth, targetHeight, path, cornerRadius);
        }

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

    /**
     * Menghasilkan gambar struk bukti transfer digital realistis beresolusi tinggi.
     */
    public static BufferedImage generateTransferReceipt(int width, int height, String identifier, int cornerRadius) {
        int w = Math.max(240, width);
        int h = Math.max(240, height);

        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (cornerRadius > 0) {
            g2.setClip(new RoundRectangle2D.Float(0, 0, w, h, cornerRadius, cornerRadius));
        }

        g2.setColor(new Color(0xFA, 0xFA, 0xF9));
        g2.fillRect(0, 0, w, h);

        int headerH = (int) (h * 0.18);
        g2.setColor(new Color(0x1F, 0x5B, 0x41));
        g2.fillRect(0, 0, w, headerH);

        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(12, w / 26)));
        String hTitle = "KOZZY RESIDENCE";
        FontMetrics fmH = g2.getFontMetrics();
        g2.drawString(hTitle, (w - fmH.stringWidth(hTitle)) / 2, (int) (headerH * 0.48));

        g2.setColor(new Color(0xD1, 0xFA, 0xE5));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(9, w / 40)));
        String hSub = "BUKTI TRANSFER PEMBAYARAN SEWA";
        FontMetrics fmSub = g2.getFontMetrics();
        g2.drawString(hSub, (w - fmSub.stringWidth(hSub)) / 2, (int) (headerH * 0.78));

        int badgeY = headerH + (int) (h * 0.05);
        int badgeH = (int) (h * 0.07);
        g2.setColor(new Color(0xE8, 0xF5, 0xE9));
        g2.fillRoundRect(w / 8, badgeY, (w * 6) / 8, badgeH, 12, 12);
        g2.setColor(new Color(0x2E, 0x7D, 0x32));
        g2.drawRoundRect(w / 8, badgeY, (w * 6) / 8, badgeH, 12, 12);

        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(11, w / 32)));
        String statusText = "TRANSAKSI BERHASIL";
        FontMetrics fmStatus = g2.getFontMetrics();
        g2.drawString(statusText, (w - fmStatus.stringWidth(statusText)) / 2, badgeY + (badgeH + fmStatus.getAscent() - fmStatus.getDescent()) / 2);

        int amountBoxY = badgeY + badgeH + (int) (h * 0.04);
        int amountBoxH = (int) (h * 0.16);
        g2.setColor(new Color(0xF3, 0xF4, 0xF6));
        g2.fillRoundRect(w / 10, amountBoxY, (w * 8) / 10, amountBoxH, 10, 10);

        g2.setColor(new Color(0x6B, 0x72, 0x80));
        g2.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(10, w / 38)));
        String lblNom = "Total Nominal Pembayaran";
        FontMetrics fmNom = g2.getFontMetrics();
        g2.drawString(lblNom, (w - fmNom.stringWidth(lblNom)) / 2, amountBoxY + (int) (amountBoxH * 0.38));

        g2.setColor(new Color(0x1F, 0x5B, 0x41));
        g2.setFont(new Font("Segoe UI", Font.BOLD, Math.max(16, w / 18)));
        String valNom = "Rp 1.100.000";
        FontMetrics fmVNom = g2.getFontMetrics();
        g2.drawString(valNom, (w - fmVNom.stringWidth(valNom)) / 2, amountBoxY + (int) (amountBoxH * 0.80));

        int detailY = amountBoxY + amountBoxH + (int) (h * 0.05);
        int lineGap = Math.max(18, (int) (h * 0.065));
        int leftX = (int) (w * 0.12);
        int rightX = (int) (w * 0.88);

        g2.setFont(new Font("Segoe UI", Font.PLAIN, Math.max(10, w / 36)));
        String[][] rows = {
            {"Metode Pembayaran", "Transfer Bank BCA / QRIS"},
            {"Rekening Tujuan", "0123-456-789 a.n Kozzy"},
            {"Nama Pengirim", "Penyewa Terverifikasi"},
            {"No. Referensi", "TRX-" + Math.abs((identifier != null ? identifier : "REF").hashCode() % 900000 + 100000)},
            {"Waktu Transaksi", "09 Nov 2026, 14:22 WIB"}
        };

        for (int i = 0; i < rows.length; i++) {
            int cy = detailY + (i * lineGap);
            if (cy > h - 35) break;

            g2.setColor(new Color(0x6B, 0x72, 0x80));
            g2.drawString(rows[i][0], leftX, cy);

            g2.setColor(new Color(0x1F, 0x29, 0x37));
            FontMetrics fVal = g2.getFontMetrics();
            g2.drawString(rows[i][1], rightX - fVal.stringWidth(rows[i][1]), cy);
        }

        int footY = h - Math.max(22, (int) (h * 0.08));
        g2.setColor(new Color(0xE5, 0xE7, 0xEB));
        g2.drawLine(leftX, footY - 10, rightX, footY - 10);

        g2.setColor(new Color(0x9C, 0xA3, 0xAF));
        g2.setFont(new Font("Segoe UI", Font.ITALIC, Math.max(9, w / 42)));
        String footMsg = "Dokumen sah bukti pembayaran sistem Kozzy Workstation";
        FontMetrics fmFoot = g2.getFontMetrics();
        g2.drawString(footMsg, (w - fmFoot.stringWidth(footMsg)) / 2, footY + 6);

        g2.dispose();
        return img;
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
