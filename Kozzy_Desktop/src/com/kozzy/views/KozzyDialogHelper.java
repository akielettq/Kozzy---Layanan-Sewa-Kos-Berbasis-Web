package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import java.awt.*;

/**
 * KozzyDialogHelper: Penyedia jendela dialog / pop-up kustom bertema Kozzy Workstation.
 * Menggantikan dialog bawaan JOptionPane yang kaku agar tampilan pop-up estetis, rapi, dan konsisten.
 */
public class KozzyDialogHelper {

    public static void showSuccess(Component parent, String title, String message) {
        showCustomAlert(parent, title, message, UITheme.PRIMARY_GREEN);
    }

    public static void showWarning(Component parent, String title, String message) {
        showCustomAlert(parent, title, message, UITheme.WARNING_AMBER);
    }

    public static void showError(Component parent, String title, String message) {
        showCustomAlert(parent, title, message, UITheme.DANGER_RED);
    }

    public static boolean showConfirm(Component parent, String title, String message, String confirmBtnText, PillButton.Style confirmStyle) {
        Window owner = (parent instanceof Window) ? (Window) parent : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        JDialog dlg = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setResizable(true);
        dlg.getContentPane().setBackground(UITheme.BG_CANVAS);
        dlg.setLayout(new BorderLayout());

        final boolean[] result = {false};

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(18, 22, 18, 22)
                )
        ));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));

        // Teks menggunakan tabel HTML 100% agar responsif tanpa terpotong di tepi kanan
        JLabel lblMsg = new JLabel("<html><table width='390' border='0' cellspacing='0' cellpadding='0'><tr><td align='center' style='color:#5A5750; font-family:Segoe UI, sans-serif; font-size:12px; line-height:1.45;'>"
                + message.replace("\n", "<br>") + "</td></tr></table></html>", SwingConstants.CENTER);
        lblMsg.setFont(UITheme.FONT_BODY);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(18));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        PillButton btnCancel = new PillButton("Batalkan", PillButton.Style.OUTLINE);
        btnCancel.setPreferredSize(new Dimension(110, 36));
        btnCancel.addActionListener(e -> {
            result[0] = false;
            dlg.dispose();
        });
        btnRow.add(btnCancel);

        PillButton btnConfirm = new PillButton(confirmBtnText, confirmStyle != null ? confirmStyle : PillButton.Style.PRIMARY);
        btnConfirm.setPreferredSize(new Dimension(140, 36));
        btnConfirm.addActionListener(e -> {
            result[0] = true;
            dlg.dispose();
        });
        btnRow.add(btnConfirm);

        card.add(btnRow);
        dlg.add(card, BorderLayout.CENTER);

        dlg.pack();
        int targetW = Math.max(480, dlg.getWidth() + 24);
        int targetH = Math.max(230, dlg.getHeight() + 16);
        dlg.setSize(targetW, targetH);
        dlg.setMinimumSize(new Dimension(460, 220));
        dlg.setLocationRelativeTo(owner);

        dlg.setVisible(true);

        return result[0];
    }

    private static void showCustomAlert(Component parent, String title, String message, Color themeColor) {
        Window owner = (parent instanceof Window) ? (Window) parent : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        JDialog dlg = new JDialog(owner, title, Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setResizable(true);
        dlg.getContentPane().setBackground(UITheme.BG_CANVAS);
        dlg.setLayout(new BorderLayout());

        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(18, 22, 18, 22)
                )
        ));

        // Judul Pesan Dialog
        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(10));

        // Teks Informasi Pesan dengan pembungkus tabel responsif bebas terpotong
        JLabel lblMsg = new JLabel("<html><table width='390' border='0' cellspacing='0' cellpadding='0'><tr><td align='center' style='color:#5A5750; font-family:Segoe UI, sans-serif; font-size:12px; line-height:1.45;'>"
                + message.replace("\n", "<br>") + "</td></tr></table></html>", SwingConstants.CENTER);
        lblMsg.setFont(UITheme.FONT_BODY);
        lblMsg.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblMsg);
        card.add(Box.createVerticalStrut(18));

        PillButton btnOk = new PillButton("Mengerti", PillButton.Style.PRIMARY);
        btnOk.setPreferredSize(new Dimension(130, 36));
        btnOk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnOk.addActionListener(e -> dlg.dispose());
        card.add(btnOk);

        dlg.add(card, BorderLayout.CENTER);

        dlg.pack();
        int targetW = Math.max(480, dlg.getWidth() + 24);
        int targetH = Math.max(220, dlg.getHeight() + 16);
        dlg.setSize(targetW, targetH);
        dlg.setMinimumSize(new Dimension(460, 210));
        dlg.setLocationRelativeTo(owner);

        dlg.setVisible(true);
    }

    /**
     * Membuka modal preview gambar bukti pembayaran yang interaktif, proporsional,
     * dilengkapi kemampuan Zoom In/Out, Fit to Window, dan Ukuran Asli 100%.
     */
    public static void showImagePreview(Component parent, String title, String imagePath, String subtitle) {
        JDialog dlg = createImagePreviewDialog(parent, title, imagePath, subtitle);
        dlg.setVisible(true);
    }

    public static JDialog createImagePreviewDialog(Component parent, String title, String imagePath, String subtitle) {
        Window owner = (parent instanceof Window) ? (Window) parent : (parent != null ? SwingUtilities.getWindowAncestor(parent) : null);
        JDialog dlg = new JDialog(owner, title != null ? title : "Pratinjau Bukti Pembayaran", Dialog.ModalityType.APPLICATION_MODAL);
        dlg.setSize(620, 740);
        dlg.setMinimumSize(new Dimension(500, 560));
        dlg.setResizable(true);
        dlg.setLocationRelativeTo(owner);
        dlg.getContentPane().setBackground(UITheme.BG_CANVAS);
        dlg.setLayout(new BorderLayout());

        JPanel content = new JPanel(new BorderLayout(0, 10));
        content.setBackground(UITheme.BG_CARD);
        content.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(12, 12, 12, 12),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(UITheme.BORDER_DARK, 1, true),
                        BorderFactory.createEmptyBorder(14, 16, 14, 16)
                )
        ));

        // 1. Header Panel
        JPanel pnlHeader = new JPanel();
        pnlHeader.setLayout(new BoxLayout(pnlHeader, BoxLayout.Y_AXIS));
        pnlHeader.setOpaque(false);

        JLabel lblTag = new JLabel("▪ DOKUMEN BUKTI TRANSAKSI", SwingConstants.CENTER);
        lblTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblTag.setForeground(UITheme.ACCENT_TERA);
        lblTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTag);
        pnlHeader.add(Box.createVerticalStrut(4));

        JLabel lblTitle = new JLabel(title != null ? title : "Bukti Pembayaran Transfer", SwingConstants.CENTER);
        lblTitle.setFont(UITheme.FONT_TITLE);
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        pnlHeader.add(lblTitle);

        if (subtitle != null && !subtitle.isEmpty()) {
            pnlHeader.add(Box.createVerticalStrut(3));
            JLabel lblSub = new JLabel(subtitle, SwingConstants.CENTER);
            lblSub.setFont(UITheme.FONT_SMALL);
            lblSub.setForeground(UITheme.TEXT_MUTED);
            lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
            pnlHeader.add(lblSub);
        }

        pnlHeader.add(Box.createVerticalStrut(8));

        // 2. Toolbar Navigasi Zoom & Fit
        JPanel pnlToolbar = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        pnlToolbar.setOpaque(false);

        final java.awt.image.BufferedImage baseImage = com.kozzy.views.components.ImageHelper.loadOriginal(imagePath);

        final double[] zoomFactor = {1.0};
        final boolean[] isFitMode = {true};

        JLabel lblZoomInfo = new JLabel("Pas Layar (Fit)");
        lblZoomInfo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblZoomInfo.setForeground(UITheme.TEXT_DARK);
        lblZoomInfo.setPreferredSize(new Dimension(95, 26));
        lblZoomInfo.setHorizontalAlignment(SwingConstants.CENTER);

        JButton btnZoomOut = new JButton("-");
        btnZoomOut.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnZoomOut.setToolTipText("Perkecil Tampilan (Zoom Out)");
        btnZoomOut.setFocusPainted(false);
        btnZoomOut.setPreferredSize(new Dimension(36, 26));

        JButton btnFit = new JButton("Pas Layar");
        btnFit.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnFit.setToolTipText("Sesuaikan Gambar dengan Ukuran Jendela");
        btnFit.setFocusPainted(false);
        btnFit.setPreferredSize(new Dimension(88, 26));

        JButton btnActual = new JButton("100% Asli");
        btnActual.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnActual.setToolTipText("Tampilkan Resolusi Piksel 1:1 Asli");
        btnActual.setFocusPainted(false);
        btnActual.setPreferredSize(new Dimension(86, 26));

        JButton btnZoomIn = new JButton("+");
        btnZoomIn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnZoomIn.setToolTipText("Perbesar Tampilan (Zoom In)");
        btnZoomIn.setFocusPainted(false);
        btnZoomIn.setPreferredSize(new Dimension(36, 26));

        pnlToolbar.add(btnZoomOut);
        pnlToolbar.add(btnFit);
        pnlToolbar.add(lblZoomInfo);
        pnlToolbar.add(btnActual);
        pnlToolbar.add(btnZoomIn);

        pnlHeader.add(pnlToolbar);
        content.add(pnlHeader, BorderLayout.NORTH);

        // 3. Center: Interactive Scaled Image Canvas
        final JScrollPane scrollImg = new JScrollPane();
        scrollImg.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true));
        scrollImg.getViewport().setBackground(new Color(0xF4, 0xF1, 0xEA));
        scrollImg.getVerticalScrollBar().setUnitIncrement(14);
        scrollImg.getHorizontalScrollBar().setUnitIncrement(14);

        final JPanel imageCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (baseImage == null) return;

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int panelW = getWidth();
                int panelH = getHeight();

                int drawW;
                int drawH;

                if (isFitMode[0]) {
                    int availW = scrollImg.getViewport().getWidth() - 16;
                    int availH = scrollImg.getViewport().getHeight() - 16;
                    if (availW <= 10) availW = panelW - 16;
                    if (availH <= 10) availH = panelH - 16;

                    double scale = Math.min((double) availW / baseImage.getWidth(), (double) availH / baseImage.getHeight());
                    drawW = Math.max(1, (int) (baseImage.getWidth() * scale));
                    drawH = Math.max(1, (int) (baseImage.getHeight() * scale));
                } else {
                    drawW = Math.max(1, (int) (baseImage.getWidth() * zoomFactor[0]));
                    drawH = Math.max(1, (int) (baseImage.getHeight() * zoomFactor[0]));
                }

                int dx = Math.max(0, (panelW - drawW) / 2);
                int dy = Math.max(0, (panelH - drawH) / 2);

                // Bayangan struk tipis
                g2.setColor(new Color(0, 0, 0, 25));
                g2.fillRoundRect(dx + 3, dy + 3, drawW, drawH, 8, 8);

                g2.drawImage(baseImage, dx, dy, drawW, drawH, null);
                g2.dispose();
            }
        };

        imageCanvas.setBackground(new Color(0xF4, 0xF1, 0xEA));
        imageCanvas.setOpaque(true);

        Runnable updateCanvasSize = () -> {
            if (baseImage == null) return;
            if (isFitMode[0]) {
                int vpW = scrollImg.getViewport().getWidth();
                int vpH = scrollImg.getViewport().getHeight();
                imageCanvas.setPreferredSize(new Dimension(Math.max(100, vpW), Math.max(100, vpH)));
                lblZoomInfo.setText("Pas Layar");
            } else {
                int dw = (int) (baseImage.getWidth() * zoomFactor[0]);
                int dh = (int) (baseImage.getHeight() * zoomFactor[0]);
                imageCanvas.setPreferredSize(new Dimension(dw + 20, dh + 20));
                lblZoomInfo.setText(String.format("%d%%", (int) Math.round(zoomFactor[0] * 100)));
            }
            imageCanvas.revalidate();
            imageCanvas.repaint();
        };

        btnFit.addActionListener(e -> {
            isFitMode[0] = true;
            updateCanvasSize.run();
        });

        btnActual.addActionListener(e -> {
            isFitMode[0] = false;
            zoomFactor[0] = 1.0;
            updateCanvasSize.run();
        });

        btnZoomIn.addActionListener(e -> {
            if (isFitMode[0]) {
                int vpW = Math.max(1, scrollImg.getViewport().getWidth() - 16);
                double fitScale = (double) vpW / baseImage.getWidth();
                zoomFactor[0] = Math.max(fitScale, 0.5);
                isFitMode[0] = false;
            }
            zoomFactor[0] = Math.min(3.0, zoomFactor[0] + 0.2);
            updateCanvasSize.run();
        });

        btnZoomOut.addActionListener(e -> {
            if (isFitMode[0]) {
                int vpW = Math.max(1, scrollImg.getViewport().getWidth() - 16);
                double fitScale = (double) vpW / baseImage.getWidth();
                zoomFactor[0] = Math.max(fitScale, 0.5);
                isFitMode[0] = false;
            }
            zoomFactor[0] = Math.max(0.2, zoomFactor[0] - 0.2);
            updateCanvasSize.run();
        });

        scrollImg.getViewport().addComponentListener(new java.awt.event.ComponentAdapter() {
            @Override
            public void componentResized(java.awt.event.ComponentEvent e) {
                if (isFitMode[0]) {
                    updateCanvasSize.run();
                }
            }
        });

        scrollImg.setViewportView(imageCanvas);
        content.add(scrollImg, BorderLayout.CENTER);

        // 4. Footer: Tombol Tutup
        JPanel pnlBottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        pnlBottom.setOpaque(false);

        PillButton btnClose = new PillButton("Tutup Pratinjau", PillButton.Style.OUTLINE);
        btnClose.setPreferredSize(new Dimension(150, 36));
        btnClose.addActionListener(e -> dlg.dispose());
        pnlBottom.add(btnClose);

        content.add(pnlBottom, BorderLayout.SOUTH);

        dlg.add(content, BorderLayout.CENTER);
        dlg.pack();
        dlg.setSize(Math.max(620, dlg.getWidth()), Math.max(720, dlg.getHeight()));
        dlg.setLocationRelativeTo(owner);

        // Inisialisasi awal ukuran canvas
        SwingUtilities.invokeLater(updateCanvasSize);

        return dlg;
    }
}
