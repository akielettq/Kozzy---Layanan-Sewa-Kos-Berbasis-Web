package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.AuthController;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import java.awt.*;

/**
 * DesktopLoginView: Kartu Autentikasi Pengguna & Pendaftaran Berorientasi Desktop.
 * - Desain terpusat di layar, bersih, dan cepat digunakan.
 * - Murni antarmuka tampilan (View); validasi dan proses login didelegasikan ke AuthController.
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

    // Komponen Form Daftar
    private JTextField txtRegNama;
    private JTextField txtRegEmail;
    private JTextField txtRegPhone;
    private JPasswordField txtRegPassword;

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
        // Kartu Form Utama (Lebar 440px)
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setPreferredSize(new Dimension(460, 520));
        card.setBackground(UITheme.BG_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(28, 32, 28, 32)
        ));

        // Header
        JLabel lblTitle = new JLabel("Akun Pengguna");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(lblTitle);
        card.add(Box.createVerticalStrut(4));

        JLabel lblSub = new JLabel("Masuk untuk mulai memesan kamar kos.", SwingConstants.CENTER);
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.TEXT_MUTED);
        lblSub.setAlignmentX(Component.CENTER_ALIGNMENT);
        lblSub.setMaximumSize(new Dimension(400, 24));
        card.add(lblSub);
        card.add(Box.createVerticalStrut(18));

        // Tab Toggle: Masuk vs Daftar
        JPanel tabRow = new JPanel(new GridLayout(1, 2, 8, 0));
        tabRow.setOpaque(false);
        tabRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));

        btnTabMasuk = new PillButton("Masuk ke Akun", PillButton.Style.PRIMARY);
        btnTabDaftar = new PillButton("Daftar Baru", PillButton.Style.OUTLINE);

        btnTabMasuk.addActionListener(e -> switchTab(true));
        btnTabDaftar.addActionListener(e -> switchTab(false));

        tabRow.add(btnTabMasuk);
        tabRow.add(btnTabDaftar);
        card.add(tabRow);
        card.add(Box.createVerticalStrut(18));

        // Card Container untuk Form Masuk & Form Daftar
        formCards = new CardLayout();
        formContainer = new JPanel(formCards);
        formContainer.setOpaque(false);

        // FORM MASUK
        JPanel pnlFormMasuk = new JPanel();
        pnlFormMasuk.setLayout(new BoxLayout(pnlFormMasuk, BoxLayout.Y_AXIS));
        pnlFormMasuk.setOpaque(false);

        pnlFormMasuk.add(createFieldLabel("Email"));
        txtLoginEmail = new JTextField("farah@kozzy.id");
        txtLoginEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        pnlFormMasuk.add(txtLoginEmail);
        pnlFormMasuk.add(Box.createVerticalStrut(10));

        pnlFormMasuk.add(createFieldLabel("Kata Sandi"));
        txtLoginPassword = new JPasswordField("123456");
        txtLoginPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        pnlFormMasuk.add(txtLoginPassword);
        pnlFormMasuk.add(Box.createVerticalStrut(18));

        PillButton btnSubmitMasuk = new PillButton("Masuk Sekarang", PillButton.Style.PRIMARY);
        btnSubmitMasuk.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSubmitMasuk.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmitMasuk.addActionListener(e -> doLogin());
        pnlFormMasuk.add(btnSubmitMasuk);

        // FORM DAFTAR
        JPanel pnlFormDaftar = new JPanel();
        pnlFormDaftar.setLayout(new BoxLayout(pnlFormDaftar, BoxLayout.Y_AXIS));
        pnlFormDaftar.setOpaque(false);

        pnlFormDaftar.add(createFieldLabel("Nama Lengkap"));
        txtRegNama = new JTextField();
        txtRegNama.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pnlFormDaftar.add(txtRegNama);
        pnlFormDaftar.add(Box.createVerticalStrut(8));

        pnlFormDaftar.add(createFieldLabel("Email"));
        txtRegEmail = new JTextField();
        txtRegEmail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pnlFormDaftar.add(txtRegEmail);
        pnlFormDaftar.add(Box.createVerticalStrut(8));

        pnlFormDaftar.add(createFieldLabel("No. WhatsApp"));
        txtRegPhone = new JTextField();
        txtRegPhone.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pnlFormDaftar.add(txtRegPhone);
        pnlFormDaftar.add(Box.createVerticalStrut(8));

        pnlFormDaftar.add(createFieldLabel("Kata Sandi"));
        txtRegPassword = new JPasswordField();
        txtRegPassword.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        pnlFormDaftar.add(txtRegPassword);
        pnlFormDaftar.add(Box.createVerticalStrut(14));

        PillButton btnSubmitDaftar = new PillButton("Daftar Akun Baru", PillButton.Style.PRIMARY);
        btnSubmitDaftar.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnSubmitDaftar.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnSubmitDaftar.addActionListener(e -> doRegister());
        pnlFormDaftar.add(btnSubmitDaftar);

        formContainer.add(pnlFormMasuk, "MASUK");
        formContainer.add(pnlFormDaftar, "DAFTAR");
        card.add(formContainer);

        add(card);
    }

    private JLabel createFieldLabel(String text) {
        JLabel l = new JLabel(text, SwingConstants.LEFT);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_DARK);
        l.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private void switchTab(boolean isMasuk) {
        this.isLoginTab = isMasuk;
        btnTabMasuk.setStyle(isMasuk ? PillButton.Style.PRIMARY : PillButton.Style.OUTLINE);
        btnTabDaftar.setStyle(!isMasuk ? PillButton.Style.PRIMARY : PillButton.Style.OUTLINE);
        formCards.show(formContainer, isMasuk ? "MASUK" : "DAFTAR");
    }

    private void doLogin() {
        String email = txtLoginEmail.getText().trim();
        String password = new String(txtLoginPassword.getPassword()).trim();

        if (email.isEmpty() || password.isEmpty()) {
            UITheme.showWarning(this, "Peringatan", "Silakan isi email dan kata sandi.");
            return;
        }

        boolean success = controller.login(email, password);
        if (success) {
            if (callback != null) callback.onLoginSuccess();
        } else {
            UITheme.showError(this, "Gagal Masuk", "Email atau kata sandi tidak cocok.");
        }
    }

    private void doRegister() {
        String nama = txtRegNama.getText().trim();
        String email = txtRegEmail.getText().trim();
        String phone = txtRegPhone.getText().trim();
        String pass = new String(txtRegPassword.getPassword()).trim();

        if (nama.isEmpty() || email.isEmpty() || phone.isEmpty() || pass.isEmpty()) {
            UITheme.showWarning(this, "Peringatan", "Semua field pendaftaran wajib diisi.");
            return;
        }

        String err = controller.register(nama, email, pass, phone);
        if (err == null) {
            UITheme.showSuccess(this, "Sukses", "Pendaftaran berhasil! Silakan masuk dengan akun barumu.");
            switchTab(true);
            txtLoginEmail.setText(email);
            txtLoginPassword.setText(pass);
        } else {
            UITheme.showError(this, "Gagal Mendaftar", err);
        }
    }
}
