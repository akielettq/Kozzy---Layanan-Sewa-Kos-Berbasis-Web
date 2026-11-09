package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.AuthController;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import java.awt.*;

/**
 * DesktopLoginView: Kartu Autentikasi Pengguna & Pendaftaran Berorientasi Desktop.
 * - Desain terpusat di layar, bersih, teks rata tengah (Poin 4).
 * - Setiap pendaftaran baru dan login langsung otomatis diarahkan ke Beranda (Poin 8).
 * - Fitur siap produksi: pemilihan jenis kelamin akun dan tombol mata untuk cek salah ketik kata sandi (typo).
 */
public class DesktopLoginView extends JPanel {

    public interface LoginSuccessCallback {
        void onLoginSuccess();
    }

    private final LoginSuccessCallback callback;
    private final AuthController controller = new AuthController();

    private boolean isLoginTab = true;

    // Komponen Form Masuk
    private JTextField txtLoginEmail;
    private JPasswordField txtLoginPassword;
    private JButton btnLoginEye;

    // Komponen Form Daftar
    private JTextField txtRegNama;
    private JRadioButton rbRegLaki;
    private JRadioButton rbRegPerempuan;
    private JTextField txtRegEmail;
    private JTextField txtRegPhone;
    private JPasswordField txtRegPassword;
    private JButton btnRegEye;

    private JPanel formContainer;
    private CardLayout formCards;
    private PillButton btnTabMasuk;
    private PillButton btnTabDaftar;

    public DesktopLoginView(LoginSuccessCallback callback) {
        this.callback = callback;

        setLayout(new GridBagLayout());
        setOpaque(false);

        initUI();
    }

    private void initUI() {
        // Kartu Form Utama (Lebar 460px) dengan bayangan taktil khas Kozzy Web
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(new Color(0x22, 0x1F, 0x19, 45));
                g2.fillRoundRect(5, 5, w - 7, h - 7, 18, 18);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, w - 6, h - 6, 18, 18);
                g2.setColor(UITheme.BORDER_DARK);
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, w - 6, h - 6, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 570));
        card.setBackground(UITheme.BG_CARD);
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(18, 32, 20, 32));

        // Brand Logo
        java.awt.image.BufferedImage logoImg = com.kozzy.views.components.ImageHelper.loadAndScale("assets/logo kos.png", 36, 36, 0);
        if (logoImg != null) {
            JLabel lblLogo = new JLabel(new ImageIcon(logoImg), SwingConstants.CENTER);
            lblLogo.setAlignmentX(Component.CENTER_ALIGNMENT);
            card.add(lblLogo);
            card.add(Box.createVerticalStrut(6));
        }

        // Section Tag Khas Kozzy Web (▪ AKSES SISTEM KOZZY)
        JLabel lblSectionTag = new JLabel("▪ AKSES SISTEM KOZZY", SwingConstants.CENTER);
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(UITheme.ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblSectionTag);
        card.add(Box.createVerticalStrut(2));

        // Header Rata Tengah
        JLabel lblTitle = new JLabel("Akses Akun Kozzy", SwingConstants.CENTER);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));

        JLabel lblSub = new JLabel("Masuk atau buat akun baru untuk mulai memesan kos.", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setMaximumSize(new Dimension(400, 24));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(12));

        // Tab Toggle: Masuk vs Daftar (Tengah)
        JPanel tabRow = new JPanel(new GridLayout(1, 2, 8, 0));
        tabRow.setOpaque(false);
        tabRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnTabMasuk = new PillButton("Masuk ke Akun", PillButton.Style.PRIMARY);
        btnTabDaftar = new PillButton("Daftar Akun Baru", PillButton.Style.OUTLINE);

        btnTabMasuk.addActionListener(e -> switchTab(true));
        btnTabDaftar.addActionListener(e -> switchTab(false));

        tabRow.add(btnTabMasuk);
        tabRow.add(btnTabDaftar);
        card.add(tabRow);
        card.add(Box.createVerticalStrut(16));

        // Card Container untuk Form Masuk & Form Daftar
        formCards = new CardLayout();
        formContainer = new JPanel(formCards);
        formContainer.setOpaque(false);

        // ==========================
        // FORM MASUK (Rata Tengah)
        // ==========================
        JPanel pnlFormMasuk = new JPanel();
        pnlFormMasuk.setLayout(new BoxLayout(pnlFormMasuk, BoxLayout.Y_AXIS));
        pnlFormMasuk.setOpaque(false);

        pnlFormMasuk.add(createCenteredFieldLabel("Alamat Email"));
        txtLoginEmail = new JTextField();
        txtLoginEmail.setHorizontalAlignment(JTextField.CENTER);
        txtLoginEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtLoginEmail.putClientProperty("JTextField.placeholderText", "nama@email.com");
        pnlFormMasuk.add(txtLoginEmail);
        pnlFormMasuk.add(Box.createVerticalStrut(12));

        pnlFormMasuk.add(createCenteredFieldLabel("Kata Sandi Akun"));
        txtLoginPassword = new JPasswordField();
        txtLoginPassword.setHorizontalAlignment(JTextField.CENTER);
        txtLoginPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtLoginPassword.putClientProperty("JTextField.placeholderText", "Masukkan kata sandi");
        pnlFormMasuk.add(createPasswordInputWithEye(txtLoginPassword, true, 34));
        pnlFormMasuk.add(Box.createVerticalStrut(20));

        PillButton btnSubmitMasuk = new PillButton("Masuk Sekarang →", PillButton.Style.PRIMARY);
        btnSubmitMasuk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSubmitMasuk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmitMasuk.addActionListener(e -> doLogin());
        pnlFormMasuk.add(btnSubmitMasuk);

        // ==========================
        // FORM DAFTAR (Rata Tengah)
        // ==========================
        JPanel pnlFormDaftar = new JPanel();
        pnlFormDaftar.setLayout(new BoxLayout(pnlFormDaftar, BoxLayout.Y_AXIS));
        pnlFormDaftar.setOpaque(false);

        pnlFormDaftar.add(createCenteredFieldLabel("Nama Lengkap"));
        txtRegNama = new JTextField();
        txtRegNama.setHorizontalAlignment(JTextField.CENTER);
        txtRegNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtRegNama.putClientProperty("JTextField.placeholderText", "Nama lengkap Anda");
        pnlFormDaftar.add(txtRegNama);
        pnlFormDaftar.add(Box.createVerticalStrut(6));

        pnlFormDaftar.add(createCenteredFieldLabel("Jenis Kelamin"));
        JPanel pnlGender = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        pnlGender.setOpaque(false);
        rbRegLaki = new JRadioButton("Laki-laki", true);
        rbRegPerempuan = new JRadioButton("Perempuan");
        rbRegLaki.setFont(UITheme.FONT_BODY);
        rbRegPerempuan.setFont(UITheme.FONT_BODY);
        rbRegLaki.setOpaque(false);
        rbRegPerempuan.setOpaque(false);
        rbRegLaki.setCursor(new Cursor(Cursor.HAND_CURSOR));
        rbRegPerempuan.setCursor(new Cursor(Cursor.HAND_CURSOR));
        ButtonGroup bgGender = new ButtonGroup();
        bgGender.add(rbRegLaki);
        bgGender.add(rbRegPerempuan);
        pnlGender.add(rbRegLaki);
        pnlGender.add(rbRegPerempuan);
        pnlFormDaftar.add(pnlGender);
        pnlFormDaftar.add(Box.createVerticalStrut(6));

        pnlFormDaftar.add(createCenteredFieldLabel("Alamat Email Aktif"));
        txtRegEmail = new JTextField();
        txtRegEmail.setHorizontalAlignment(JTextField.CENTER);
        txtRegEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtRegEmail.putClientProperty("JTextField.placeholderText", "nama@email.com");
        pnlFormDaftar.add(txtRegEmail);
        pnlFormDaftar.add(Box.createVerticalStrut(6));

        pnlFormDaftar.add(createCenteredFieldLabel("Nomor WhatsApp (Aktif)"));
        txtRegPhone = new JTextField();
        txtRegPhone.setHorizontalAlignment(JTextField.CENTER);
        txtRegPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtRegPhone.putClientProperty("JTextField.placeholderText", "08xxxxxxxxxx");
        pnlFormDaftar.add(txtRegPhone);
        pnlFormDaftar.add(Box.createVerticalStrut(6));

        pnlFormDaftar.add(createCenteredFieldLabel("Buat Kata Sandi"));
        txtRegPassword = new JPasswordField();
        txtRegPassword.setHorizontalAlignment(JTextField.CENTER);
        txtRegPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        txtRegPassword.putClientProperty("JTextField.placeholderText", "Minimal 5 karakter");
        pnlFormDaftar.add(createPasswordInputWithEye(txtRegPassword, false, 32));
        pnlFormDaftar.add(Box.createVerticalStrut(12));

        PillButton btnSubmitDaftar = new PillButton("Daftar & Langsung Masuk →", PillButton.Style.PRIMARY);
        btnSubmitDaftar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSubmitDaftar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmitDaftar.addActionListener(e -> doRegister());
        pnlFormDaftar.add(btnSubmitDaftar);

        formContainer.add(pnlFormMasuk, "MASUK");
        formContainer.add(pnlFormDaftar, "DAFTAR");
        card.add(formContainer);

        add(card);
    }

    private JPanel createPasswordInputWithEye(JPasswordField pwdField, boolean isLogin, int height) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, height));

        JButton btnEye = new JButton("👁") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Latar belakang tombol
                if (getModel().isPressed()) {
                    g2.setColor(new Color(0xD8, 0xD4, 0xCA));
                } else if (getModel().isRollover()) {
                    g2.setColor(new Color(0xE8, 0xE4, 0xD8));
                } else {
                    g2.setColor(UITheme.BG_CARD_ALT);
                }
                g2.fillRoundRect(0, 0, w, h, 6, 6);

                // Garis batas tepi
                g2.setColor(UITheme.BORDER_COLOR);
                g2.drawRoundRect(0, 0, w - 1, h - 1, 6, 6);

                boolean isRevealed = (pwdField.getEchoChar() == (char) 0);

                // Gambar Ikon Mata Vektor Anti-Aliased (Bebas Ketergantungan Font Emoji OS)
                int cx = w / 2;
                int cy = h / 2;
                int ew = 18; // Lebar kontur mata
                int eh = 10; // Tinggi kontur mata

                g2.setColor(isRevealed ? UITheme.PRIMARY_GREEN : UITheme.TEXT_DARK);
                g2.setStroke(new BasicStroke(1.6f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                // Kontur kelopak mata
                java.awt.geom.Path2D eye = new java.awt.geom.Path2D.Float();
                eye.moveTo(cx - ew / 2.0f, cy);
                eye.quadTo(cx, cy - eh, cx + ew / 2.0f, cy);
                eye.quadTo(cx, cy + eh, cx - ew / 2.0f, cy);
                eye.closePath();
                g2.draw(eye);

                // Pupil tengah
                int pr = 6;
                g2.fillOval(cx - pr / 2, cy - pr / 2, pr, pr);

                if (isRevealed) {
                    // Garis diagonal (slash) halus menandakan sedang diintip / klik untuk sembunyikan
                    g2.setColor(UITheme.ACCENT_TERA);
                    g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                    g2.drawLine(cx - 9, cy + 7, cx + 9, cy - 7);
                }

                g2.dispose();
            }
        };

        btnEye.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 15));
        btnEye.setFocusPainted(false);
        btnEye.setFocusable(false);
        btnEye.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEye.setToolTipText("Lihat Kata Sandi (Cek Typo)");
        btnEye.setPreferredSize(new Dimension(42, height));
        btnEye.setBackground(UITheme.BG_CARD_ALT);
        btnEye.setForeground(UITheme.TEXT_DARK);
        btnEye.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(2, 6, 2, 6)
        ));

        char defaultEcho = pwdField.getEchoChar() != 0 ? pwdField.getEchoChar() : '•';

        btnEye.addActionListener(e -> {
            if (pwdField.getEchoChar() != (char) 0) {
                pwdField.setEchoChar((char) 0);
                btnEye.setText("🙈");
                btnEye.setToolTipText("Sembunyikan Kata Sandi");
            } else {
                pwdField.setEchoChar(defaultEcho);
                btnEye.setText("👁");
                btnEye.setToolTipText("Lihat Kata Sandi (Cek Typo)");
            }
            btnEye.repaint();
            pwdField.requestFocusInWindow();
        });

        if (isLogin) {
            this.btnLoginEye = btnEye;
        } else {
            this.btnRegEye = btnEye;
        }

        row.add(pwdField, BorderLayout.CENTER);
        row.add(btnEye, BorderLayout.EAST);
        return row;
    }

    private JLabel createCenteredFieldLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.CENTER);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_DARK);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    public void switchTab(boolean isMasuk) {
        this.isLoginTab = isMasuk;
        btnTabMasuk.setStyle(isMasuk ? PillButton.Style.PRIMARY : PillButton.Style.OUTLINE);
        btnTabDaftar.setStyle(!isMasuk ? PillButton.Style.PRIMARY : PillButton.Style.OUTLINE);

        // Reset intip password ke kondisi sensor saat ganti tab demi keamanan
        if (txtLoginPassword != null && txtLoginPassword.getEchoChar() == (char) 0) {
            txtLoginPassword.setEchoChar('•');
            if (btnLoginEye != null) {
                btnLoginEye.setText("👁");
                btnLoginEye.setToolTipText("Lihat Kata Sandi (Cek Typo)");
                btnLoginEye.repaint();
            }
        }
        if (txtRegPassword != null && txtRegPassword.getEchoChar() == (char) 0) {
            txtRegPassword.setEchoChar('•');
            if (btnRegEye != null) {
                btnRegEye.setText("👁");
                btnRegEye.setToolTipText("Lihat Kata Sandi (Cek Typo)");
                btnRegEye.repaint();
            }
        }

        formCards.show(formContainer, isMasuk ? "MASUK" : "DAFTAR");
    }

    private void doLogin() {
        String email = txtLoginEmail.getText().trim();
        String password = new String(txtLoginPassword.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            KozzyDialogHelper.showWarning(this, "Data Belum Lengkap", "Silakan isi alamat email dan kata sandi.");
            return;
        }

        boolean success = controller.login(email, password);
        if (success) {
            // Poin 8: Login langsung bawa ke beranda
            if (callback != null) callback.onLoginSuccess();
        } else {
            KozzyDialogHelper.showError(this, "Gagal Masuk", "Alamat email atau kata sandi tidak cocok.");
        }
    }

    private void doRegister() {
        String nama = txtRegNama.getText().trim();
        String email = txtRegEmail.getText().trim();
        String phone = txtRegPhone.getText().trim();
        String pass = new String(txtRegPassword.getPassword()).trim();
        String gender = getSelectedGender();

        if (nama.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            KozzyDialogHelper.showWarning(this, "Data Belum Lengkap", "Semua kolom pendaftaran wajib diisi.");
            return;
        }

        String err = controller.register(nama, email, pass, phone, gender);
        if (err == null) {
            // Poin 8: Pendaftaran baru langsung bawa ke beranda dengan sesi aktif
            KozzyDialogHelper.showSuccess(this, "Pendaftaran Berhasil!",
                    "Selamat datang di Kozzy, " + nama + "!\nAkun barumu (" + gender + ") telah aktif dan kamu langsung diarahkan ke Beranda.");
            if (callback != null) callback.onLoginSuccess();
        } else {
            KozzyDialogHelper.showError(this, "Gagal Mendaftar", err);
        }
    }

    public boolean isLoginTab() {
        return isLoginTab;
    }

    public JTextField getTxtLoginEmail() {
        return txtLoginEmail;
    }

    public JPasswordField getTxtLoginPassword() {
        return txtLoginPassword;
    }

    public JButton getBtnLoginEye() {
        return btnLoginEye;
    }

    public JTextField getTxtRegNama() {
        return txtRegNama;
    }

    public JTextField getTxtRegEmail() {
        return txtRegEmail;
    }

    public JTextField getTxtRegPhone() {
        return txtRegPhone;
    }

    public JPasswordField getTxtRegPassword() {
        return txtRegPassword;
    }

    public JButton getBtnRegEye() {
        return btnRegEye;
    }

    public JRadioButton getRbRegLaki() {
        return rbRegLaki;
    }

    public JRadioButton getRbRegPerempuan() {
        return rbRegPerempuan;
    }

    public String getSelectedGender() {
        return (rbRegPerempuan != null && rbRegPerempuan.isSelected()) ? "Perempuan" : "Laki-laki";
    }
}
