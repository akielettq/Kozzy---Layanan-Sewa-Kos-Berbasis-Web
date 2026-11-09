package com.kozzy.views;

import com.kozzy.config.UITheme;
import com.kozzy.controllers.BookingController;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
import com.kozzy.models.MetodePembayaran;
import com.kozzy.models.Pengguna;
import com.kozzy.services.SessionManager;
import com.kozzy.views.components.PillButton;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

/**
 * DesktopBookingDialog: Modal Dialog Pengajuan Booking Kamar Berorientasi Desktop.
 * - Seluruh elemen formulir dan teks informasi ditata RATA KIRI secara konsisten (Poin 6).
 * - Notifikasi & validasi menggunakan jendela pop-up kustom estetis (Poin 7).
 */
public class DesktopBookingDialog extends JDialog {

    private final Kamar kamar;
    private final Runnable onSuccess;
    private final BookingController controller = new BookingController();

    private JTextField txtTanggal;
    private JComboBox<Integer> cmbDurasi;
    private JComboBox<MetodePembayaran> cmbMetode;
    private JLabel lblNomorTujuan;
    private JLabel lblTotalBiaya;
    private JLabel lblFileBukti;
    private String selectedFilePath = null;

    public DesktopBookingDialog(Frame owner, Kamar kamar, Runnable onSuccess) {
        super(owner, "Formulir Booking Kamar " + kamar.getKodeKamar(), true);
        this.kamar = kamar;
        this.onSuccess = onSuccess;

        initUI();

        pack();
        int w = Math.max(560, getWidth() + 24);
        int h = Math.max(660, getHeight() + 24);
        setSize(w, h);
        setMinimumSize(new Dimension(520, 620));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        getContentPane().setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(22, 26, 22, 26));

        // 1. Header (Rata Kiri)
        JLabel lblSectionTag = new JLabel("▪ FORMULIR PEMESANAN KAMAR");
        lblSectionTag.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblSectionTag.setForeground(UITheme.ACCENT_TERA);
        lblSectionTag.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblSectionTag);
        content.add(Box.createVerticalStrut(2));

        JLabel lblTitle = new JLabel("Formulir Pemesanan Kamar");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));

        JLabel lblSub = new JLabel("Kamar " + kamar.getKodeKamar() + " (" + kamar.getTipe().toUpperCase() + ") · " + kamar.formatRupiah() + " / bln");
        lblSub.setFont(UITheme.FONT_BODY_BOLD);
        lblSub.setForeground(UITheme.PRIMARY_GREEN);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblSub);
        content.add(Box.createVerticalStrut(18));

        // 2. Field Tanggal Mulai (Rata Kiri)
        content.add(createLeftLabel("Tanggal Mulai Sewa (Format: YYYY-MM-DD)"));
        txtTanggal = new JTextField(LocalDate.now().plusDays(1).toString());
        txtTanggal.setFont(UITheme.FONT_BODY);
        txtTanggal.setHorizontalAlignment(JTextField.LEFT);
        txtTanggal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(txtTanggal);
        content.add(Box.createVerticalStrut(10));

        // 3. Field Durasi Sewa (Rata Kiri)
        content.add(createLeftLabel("Durasi Masa Sewa"));
        Integer[] durasiOptions = {1, 3, 6, 12};
        cmbDurasi = new JComboBox<>(durasiOptions);
        cmbDurasi.setFont(UITheme.FONT_BODY);
        cmbDurasi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbDurasi.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbDurasi.addActionListener(e -> updateTotalBiaya());
        content.add(cmbDurasi);
        content.add(Box.createVerticalStrut(10));

        // 4. Field Metode Pembayaran (Rata Kiri)
        content.add(createLeftLabel("Pilihan Rekening Pembayaran"));
        List<MetodePembayaran> methods = controller.getAllPaymentMethods();
        cmbMetode = new JComboBox<>(methods.toArray(new MetodePembayaran[0]));
        cmbMetode.setFont(UITheme.FONT_BODY);
        cmbMetode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbMetode.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbMetode.addActionListener(e -> updateMetodeInfo());
        content.add(cmbMetode);
        content.add(Box.createVerticalStrut(4));

        // Info Rekening Tujuan (Rata Kiri)
        lblNomorTujuan = new JLabel("Rekening Tujuan: -");
        lblNomorTujuan.setFont(UITheme.FONT_SMALL);
        lblNomorTujuan.setForeground(UITheme.TEXT_MUTED);
        lblNomorTujuan.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblNomorTujuan);
        content.add(Box.createVerticalStrut(12));

        // 5. Bukti Transfer File Row (Rata Kiri) dengan Tombol Preview Aktif
        content.add(createLeftLabel("Berkas Bukti Transfer Bank / E-Wallet"));
        JPanel fileRow = new JPanel(new BorderLayout(8, 0));
        fileRow.setOpaque(false);
        fileRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        fileRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblFileBukti = new JLabel("Belum ada berkas dipilih (Silakan pilih bukti transfer)");
        lblFileBukti.setFont(UITheme.FONT_SMALL);
        lblFileBukti.setForeground(UITheme.TEXT_MUTED);
        lblFileBukti.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (selectedFilePath != null && !selectedFilePath.trim().isEmpty()) {
                    KozzyDialogHelper.showImagePreview(
                        DesktopBookingDialog.this,
                        "Pratinjau Bukti Pembayaran",
                        selectedFilePath,
                        "Kamar " + kamar.getKodeKamar() + " · " + new File(selectedFilePath).getName()
                    );
                }
            }
        });
        fileRow.add(lblFileBukti, BorderLayout.CENTER);

        JPanel pnlFileBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlFileBtns.setOpaque(false);

        JButton btnPreviewFile = new JButton("Lihat Preview");
        btnPreviewFile.setFont(UITheme.FONT_SMALL);
        btnPreviewFile.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPreviewFile.setEnabled(false);
        btnPreviewFile.addActionListener(e -> {
            if (selectedFilePath != null && !selectedFilePath.trim().isEmpty()) {
                KozzyDialogHelper.showImagePreview(
                    this,
                    "Pratinjau Bukti Pembayaran",
                    selectedFilePath,
                    "Kamar " + kamar.getKodeKamar() + " · " + new File(selectedFilePath).getName()
                );
            }
        });

        JButton btnChoose = new JButton("Pilih Berkas...");
        btnChoose.setFont(UITheme.FONT_SMALL);
        btnChoose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChoose.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Pilih Berkas Bukti Transfer Pembayaran");
            chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "Berkas Gambar (JPG, JPEG, PNG, GIF, BMP, WEBP)",
                "jpg", "jpeg", "png", "gif", "bmp", "webp"
            ));
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedFilePath = f.getAbsolutePath();
                lblFileBukti.setText(f.getName());
                lblFileBukti.setForeground(UITheme.TEXT_DARK);
                lblFileBukti.setCursor(new Cursor(Cursor.HAND_CURSOR));
                lblFileBukti.setToolTipText("Klik untuk melihat pratinjau gambar bukti transfer");
                btnPreviewFile.setEnabled(true);
            }
        });

        pnlFileBtns.add(btnChoose);
        pnlFileBtns.add(btnPreviewFile);
        fileRow.add(pnlFileBtns, BorderLayout.EAST);
        content.add(fileRow);
        content.add(Box.createVerticalStrut(16));

        // 6. Total Biaya Sewa Box khas Kozzy Web (.total-bar)
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(new Color(0x22, 0x1F, 0x19));
        totalBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0x22, 0x1F, 0x19), 1, true),
                BorderFactory.createEmptyBorder(12, 16, 12, 16)
        ));
        totalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        totalBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTotalT = new JLabel("TOTAL TAGIHAN SEWA");
        lblTotalT.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTotalT.setForeground(new Color(0xF4, 0xEF, 0xE4));
        totalBox.add(lblTotalT, BorderLayout.WEST);

        lblTotalBiaya = new JLabel("Rp 850.000");
        lblTotalBiaya.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTotalBiaya.setForeground(new Color(0xF4, 0xEF, 0xE4));
        totalBox.add(lblTotalBiaya, BorderLayout.EAST);
        content.add(totalBox);

        content.add(Box.createVerticalGlue());

        // 7. Tombol Aksi Bawah
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        PillButton btnBatal = new PillButton("Batalkan", PillButton.Style.OUTLINE);
        btnBatal.setPreferredSize(new Dimension(100, 36));
        btnBatal.addActionListener(e -> dispose());
        btnRow.add(btnBatal);

        PillButton btnSubmit = new PillButton("Kirim Pengajuan Booking →", PillButton.Style.PRIMARY);
        btnSubmit.setPreferredSize(new Dimension(210, 36));
        btnSubmit.addActionListener(e -> submitBooking());
        btnRow.add(btnSubmit);

        content.add(btnRow);

        JScrollPane scrollPane = new JScrollPane(content);
        scrollPane.setBorder(null);
        scrollPane.getViewport().setBackground(UITheme.BG_CARD);
        scrollPane.getVerticalScrollBar().setUnitIncrement(14);
        add(scrollPane, BorderLayout.CENTER);

        updateMetodeInfo();
        updateTotalBiaya();
    }

    private JLabel createLeftLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void updateMetodeInfo() {
        MetodePembayaran m = (MetodePembayaran) cmbMetode.getSelectedItem();
        if (m != null) {
            lblNomorTujuan.setText("Rekening Tujuan: " + m.getNamaMetode() + " — " + m.getNomorTujuan());
        }
    }

    private void updateTotalBiaya() {
        int durasi = (Integer) cmbDurasi.getSelectedItem();
        int total = controller.hitungTotalBiaya(kamar, durasi);
        lblTotalBiaya.setText(String.format("Rp %,d", total).replace(',', '.'));
    }

    private void submitBooking() {
        Pengguna user = SessionManager.getInstance().getCurrentUser();
        if (user == null) {
            KozzyDialogHelper.showWarning(this, "Akses Terbatas", "Silakan login ke akun terlebih dahulu sebelum mengajukan booking.");
            return;
        }

        StringBuilder canBookReason = new StringBuilder();
        if (!controller.canUserBook(user, kamar, canBookReason)) {
            KozzyDialogHelper.showWarning(this, "Tidak Memenuhi Ketentuan Kamar", canBookReason.toString());
            return;
        }

        if (selectedFilePath == null || selectedFilePath.trim().isEmpty()) {
            KozzyDialogHelper.showWarning(this, "Bukti Pembayaran Belum Dipilih",
                    "Silakan klik 'Pilih Berkas...' untuk melampirkan foto/struk bukti transfer sebelum mengirim pengajuan booking.");
            return;
        }

        File proofFile = new File(selectedFilePath);
        if (!proofFile.exists() || !proofFile.isFile()) {
            KozzyDialogHelper.showError(this, "Berkas Tidak Ditemukan",
                    "Berkas bukti transfer yang dipilih tidak ditemukan pada sistem komputer Anda.");
            return;
        }

        String tglStr = txtTanggal.getText().trim();
        Date tglMulai;
        try {
            tglMulai = Date.valueOf(tglStr);
        } catch (Exception ex) {
            KozzyDialogHelper.showError(this, "Format Tanggal Salah", "Gunakan format tanggal YYYY-MM-DD.\nContoh yang benar: " + LocalDate.now());
            return;
        }

        int durasi = (Integer) cmbDurasi.getSelectedItem();
        MetodePembayaran m = (MetodePembayaran) cmbMetode.getSelectedItem();
        int idMetode = (m != null) ? m.getIdMetode() : 1;

        Booking booking = new Booking();
        booking.setIdPengguna(user.getIdPengguna());
        booking.setKodeKamar(kamar.getKodeKamar());
        booking.setTanggalMulai(tglMulai);
        booking.setDurasi(durasi);
        booking.setIdMetode(idMetode);

        String err = controller.ajukanBooking(booking, selectedFilePath);
        if (err == null) {
            KozzyDialogHelper.showSuccess(this, "Pengajuan Terkirim", "Pengajuan booking kamar " + kamar.getKodeKamar() + " berhasil dikirim!\nMenunggu verifikasi admin.");
            dispose();
            if (onSuccess != null) onSuccess.run();
        } else {
            KozzyDialogHelper.showError(this, "Pengajuan Gagal", err);
        }
    }
}
