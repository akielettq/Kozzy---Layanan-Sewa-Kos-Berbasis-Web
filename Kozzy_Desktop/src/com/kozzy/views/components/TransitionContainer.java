package com.kozzy.views.components;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * TransitionContainer: Wadah Navigasi Layar Desktop dengan Animasi Transisi Halus.
 * - Memanfaatkan JavaFX (JFXPanel, FadeTransition, TranslateTransition, ParallelTransition)
 *   untuk akselerasi grafis 60 FPS maksimal saat perpindahan halaman (Poin 9).
 * - Dilengkapi fallback otomatis Swing timer jika lingkungan JavaFX berjalan tanpa modul.
 * - Anti-glitch dan aman terhadap rapid-click (transisi beruntun cepat).
 */
public class TransitionContainer extends JPanel {

    private final Map<String, JComponent> cards = new HashMap<>();
    private String currentKey = null;
    private JComponent currentComponent = null;

    // JavaFX Engine Components
    private static boolean javaFXInitialized = false;
    private static boolean javaFXAvailable = false;
    private JFXPanel fxPanel = null;
    private ParallelTransition currentFXTransition = null;

    // Swing Fallback Components
    private Timer swingFallbackTimer;
    private float swingProgress = 1.0f;
    private BufferedImage snapshotFrom = null;
    private BufferedImage snapshotTo = null;
    private boolean isAnimating = false;

    private static final int DURATION_MS = 220;
    private static final int SWING_FRAME_DELAY = 16; // ~60 FPS

    public TransitionContainer() {
        setLayout(new BorderLayout());
        setOpaque(false);
        initJavaFX();
    }

    private synchronized void initJavaFX() {
        if (javaFXInitialized) return;
        try {
            // Inisialisasi JavaFX Toolkit melalui instansiasi dummy JFXPanel
            JFXPanel dummy = new JFXPanel();
            Platform.setImplicitExit(false);
            javaFXAvailable = true;
            javaFXInitialized = true;
        } catch (Throwable t) {
            javaFXAvailable = false;
            javaFXInitialized = true;
            System.err.println("[TransitionContainer] JavaFX tidak aktif, menggunakan fallback Swing: " + t.getMessage());
        }
    }

    public boolean isJavaFXEnabled() {
        return javaFXAvailable;
    }

    public boolean isAnimating() {
        return isAnimating;
    }

    public void addPage(String key, JComponent component) {
        cards.put(key, component);
    }

    public void showPage(String key) {
        if (key == null || !cards.containsKey(key)) return;
        if (key.equals(currentKey) && !isAnimating) return;

        JComponent targetComponent = cards.get(key);
        JComponent previousComponent = currentComponent;

        // Startup awal atau container belum berukuran: langsung pasang tanpa animasi
        if (previousComponent == null || getWidth() <= 0 || getHeight() <= 0) {
            cancelAnyRunningAnimation();
            removeAll();
            currentKey = key;
            currentComponent = targetComponent;
            add(targetComponent, BorderLayout.CENTER);
            revalidate();
            repaint();
            return;
        }

        // Hentikan transisi yang sedang berjalan jika ada
        cancelAnyRunningAnimation();

        int w = Math.max(1, getWidth());
        int h = Math.max(1, getHeight());

        // Ambil snapshot visual halaman lama dan target
        snapshotFrom = captureComponent(previousComponent, w, h);
        targetComponent.setSize(w, h);
        targetComponent.doLayout();
        snapshotTo = captureComponent(targetComponent, w, h);

        currentKey = key;
        currentComponent = targetComponent;
        isAnimating = true;

        if (javaFXAvailable) {
            performJavaFXTransition(key, targetComponent, snapshotFrom, snapshotTo, w, h);
        } else {
            performSwingFallbackTransition(key, targetComponent);
        }
    }

    /**
     * Menjalankan transisi antarmuka menggunakan engine JavaFX berkemampuan 60 FPS hardware-accelerated.
     */
    private void performJavaFXTransition(String key, JComponent targetComponent,
                                        BufferedImage imgFrom, BufferedImage imgTo, int w, int h) {
        if (fxPanel == null) {
            fxPanel = new JFXPanel();
            fxPanel.setOpaque(false);
        }

        removeAll();
        add(fxPanel, BorderLayout.CENTER);
        revalidate();
        repaint();

        Platform.runLater(() -> {
            try {
                javafx.scene.image.Image fxImgFrom = SwingFXUtils.toFXImage(imgFrom, null);
                javafx.scene.image.Image fxImgTo   = SwingFXUtils.toFXImage(imgTo, null);

                ImageView ivFrom = new ImageView(fxImgFrom);
                ImageView ivTo   = new ImageView(fxImgTo);

                ivFrom.setFitWidth(w);
                ivFrom.setFitHeight(h);
                ivTo.setFitWidth(w);
                ivTo.setFitHeight(h);

                ivFrom.setOpacity(1.0);
                ivTo.setOpacity(0.0);
                ivTo.setTranslateY(18.0); // Efek subtle glide slide-up

                StackPane root = new StackPane(ivFrom, ivTo);
                root.setStyle("-fx-background-color: transparent;");
                Scene scene = new Scene(root, w, h, javafx.scene.paint.Color.TRANSPARENT);
                fxPanel.setScene(scene);

                FadeTransition fadeOut = new FadeTransition(Duration.millis(DURATION_MS), ivFrom);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setInterpolator(Interpolator.EASE_BOTH);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(DURATION_MS), ivTo);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.setInterpolator(Interpolator.EASE_OUT);

                TranslateTransition slideIn = new TranslateTransition(Duration.millis(DURATION_MS), ivTo);
                slideIn.setFromY(18.0);
                slideIn.setToY(0.0);
                slideIn.setInterpolator(Interpolator.SPLINE(0.25, 0.1, 0.25, 1.0));

                ParallelTransition pt = new ParallelTransition(fadeOut, fadeIn, slideIn);
                currentFXTransition = pt;

                pt.setOnFinished(e -> {
                    currentFXTransition = null;
                    SwingUtilities.invokeLater(() -> finishTransition(key, targetComponent));
                });

                pt.play();
            } catch (Throwable t) {
                // Jika terjadi galat runtime JavaFX, beralih ke penyelesaian instan
                SwingUtilities.invokeLater(() -> finishTransition(key, targetComponent));
            }
        });
    }

    /**
     * Fallback transisi Swing Timer jika JavaFX tidak tersedia.
     */
    private void performSwingFallbackTransition(String key, JComponent targetComponent) {
        long animStartTime = System.currentTimeMillis();
        swingProgress = 0.0f;

        swingFallbackTimer = new Timer(SWING_FRAME_DELAY, e -> {
            long elapsed = System.currentTimeMillis() - animStartTime;
            float rawProgress = Math.min(1.0f, (float) elapsed / DURATION_MS);
            swingProgress = 1.0f - (float) Math.pow(1.0f - rawProgress, 3);

            if (rawProgress >= 1.0f) {
                swingFallbackTimer.stop();
                finishTransition(key, targetComponent);
            } else {
                repaint();
            }
        });
        swingFallbackTimer.start();
    }

    private void finishTransition(String key, JComponent targetComponent) {
        isAnimating = false;
        snapshotFrom = null;
        snapshotTo = null;

        removeAll();
        add(targetComponent, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void cancelAnyRunningAnimation() {
        if (currentFXTransition != null) {
            try {
                Platform.runLater(() -> {
                    if (currentFXTransition != null) {
                        currentFXTransition.stop();
                        currentFXTransition = null;
                    }
                });
            } catch (Throwable ignored) {}
        }
        if (swingFallbackTimer != null && swingFallbackTimer.isRunning()) {
            swingFallbackTimer.stop();
        }
        isAnimating = false;
        snapshotFrom = null;
        snapshotTo = null;
    }

    private BufferedImage captureComponent(JComponent comp, int w, int h) {
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = img.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        comp.paint(g2);
        g2.dispose();
        return img;
    }

    @Override
    protected void paintChildren(Graphics g) {
        if (!javaFXAvailable && isAnimating && snapshotTo != null) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

            if (snapshotFrom != null) {
                float alphaFrom = Math.max(0.0f, 1.0f - swingProgress);
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaFrom));
                g2.drawImage(snapshotFrom, 0, 0, null);
            }

            float alphaTo = Math.min(1.0f, swingProgress);
            int slideOffset = (int) ((1.0f - swingProgress) * 16);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alphaTo));
            g2.drawImage(snapshotTo, 0, slideOffset, null);

            g2.dispose();
        } else {
            super.paintChildren(g);
        }
    }

    public String getCurrentKey() {
        return currentKey;
    }

    public JComponent getPage(String key) {
        return cards.get(key);
    }
}
