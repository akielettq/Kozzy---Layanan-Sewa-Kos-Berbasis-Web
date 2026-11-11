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
 * - Murni tampilan UI form; seluruh validasi dan proses pengajuan sewa didelegasikan ke BookingController.
 * - Menggunakan jendela pop-up kustom UITheme dan pratinjau bukti pembayaran interaktif.
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
    private String selectedFilePath = "assets/uploads/bukti/bukti_kz0118.jpg";

    public DesktopBookingDialog(Frame owner, Kamar kamar, Runnable onSuccess) {
        super(owner, "Pengajuan Sewa Kamar " + kamar.getKodeKamar(), true);
        this.kamar = kamar;
        this.onSuccess = onSuccess;

        getContentPane().setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout());

        initUI();

        pack();
        int w = Math.max(520, getWidth() + 20);
        int h = Math.max(580, getHeight() + 20);
        setSize(w, h);
        setMinimumSize(new Dimension(500, 560));
        setResizable(true);
        setLocationRelativeTo(owner);
    }

    private void initUI() {
        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);
        content.setBorder(BorderFactory.createEmptyBorder(20, 24, 20, 24));

        // Header
        JLabel lblTitle = new JLabel("Formulir Pemesanan Kamar");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(UITheme.TEXT_DARK);
        lblTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));

        JLabel lblSub = new JLabel("Kamar " + kamar.getKodeKamar() + " (" + kamar.getTipe().toUpperCase() + ") · " + kamar.formatRupiah());
        lblSub.setFont(UITheme.FONT_BODY_BOLD);
        lblSub.setForeground(UITheme.PRIMARY_GREEN);
        lblSub.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblSub);
        content.add(Box.createVerticalStrut(16));

        // Form Fields
        content.add(createLabel("Tanggal Mulai Sewa (YYYY-MM-DD)"));
        txtTanggal = new JTextField(LocalDate.now().plusDays(1).toString());
        txtTanggal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        txtTanggal.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(txtTanggal);
        content.add(Box.createVerticalStrut(10));

        content.add(createLabel("Durasi Sewa (Bulan)"));
        Integer[] durasiOptions = {1, 3, 6, 12};
        cmbDurasi = new JComboBox<>(durasiOptions);
        cmbDurasi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbDurasi.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbDurasi.addActionListener(e -> updateTotalBiaya());
        content.add(cmbDurasi);
        content.add(Box.createVerticalStrut(10));

        content.add(createLabel("Metode Pembayaran"));
        List<MetodePembayaran> methods = controller.getAllPaymentMethods();
        cmbMetode = new JComboBox<>(methods.toArray(new MetodePembayaran[0]));
        cmbMetode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        cmbMetode.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbMetode.addActionListener(e -> updateMetodeInfo());
        content.add(cmbMetode);
        content.add(Box.createVerticalStrut(6));

        // Info Rekening Tujuan
        lblNomorTujuan = new JLabel("Tujuan: -");
        lblNomorTujuan.setFont(UITheme.FONT_SMALL);
        lblNomorTujuan.setForeground(UITheme.TEXT_MUTED);
        lblNomorTujuan.setAlignmentX(Component.LEFT_ALIGNMENT);
        content.add(lblNomorTujuan);
        content.add(Box.createVerticalStrut(12));

        // Bukti Transfer
        content.add(createLabel("Bukti Transfer"));
        JPanel fileRow = new JPanel(new BorderLayout(8, 0));
        fileRow.setOpaque(false);
        fileRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));
        fileRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        lblFileBukti = new JLabel("bukti_transfer.jpg (Default Demo)");
        lblFileBukti.setFont(UITheme.FONT_SMALL);
        lblFileBukti.setForeground(UITheme.TEXT_MUTED);
        fileRow.add(lblFileBukti, BorderLayout.CENTER);

        JPanel pnlFileBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlFileBtns.setOpaque(false);

        JButton btnPreview = new JButton("Lihat Preview");
        btnPreview.setFont(UITheme.FONT_SMALL);
        btnPreview.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPreview.addActionListener(e -> {
            UITheme.showImagePreview(
                this,
                "Pratinjau Bukti Pembayaran",
                selectedFilePath,
                "Kamar " + kamar.getKodeKamar()
            );
        });

        JButton btnChoose = new JButton("Pilih File...");
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
            }
        });

        pnlFileBtns.add(btnChoose);
        pnlFileBtns.add(btnPreview);
        fileRow.add(pnlFileBtns, BorderLayout.EAST);
        content.add(fileRow);
        content.add(Box.createVerticalStrut(16));

        // Total Biaya Box
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(UITheme.BG_CARD_ALT);
        totalBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        totalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        totalBox.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblTotalT = new JLabel("Total Biaya Sewa:");
        lblTotalT.setFont(UITheme.FONT_BODY_BOLD);
        lblTotalT.setForeground(UITheme.TEXT_DARK);
        totalBox.add(lblTotalT, BorderLayout.WEST);

        lblTotalBiaya = new JLabel("Rp 850.000");
        lblTotalBiaya.setFont(new Font("Segoe UI", Font.BOLD, 17));
        lblTotalBiaya.setForeground(UITheme.PRIMARY_GREEN);
        totalBox.add(lblTotalBiaya, BorderLayout.EAST);
        content.add(totalBox);

        content.add(Box.createVerticalGlue());

        // Tombol Aksi Bawah
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        btnRow.setOpaque(false);
        btnRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btnRow.setAlignmentX(Component.LEFT_ALIGNMENT);

        PillButton btnBatal = new PillButton("Batal", PillButton.Style.OUTLINE);
        btnBatal.addActionListener(e -> dispose());
        btnRow.add(btnBatal);

        PillButton btnSubmit = new PillButton("Kirim Pengajuan Booking", PillButton.Style.PRIMARY);
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

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY_BOLD);
        l.setForeground(UITheme.TEXT_DARK);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        return l;
    }

    private void updateMetodeInfo() {
        MetodePembayaran m = (MetodePembayaran) cmbMetode.getSelectedItem();
        if (m != null) {
            lblNomorTujuan.setText("Tujuan: " + m.getNamaMetode() + " (" + m.getNomorTujuan() + ")");
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
            UITheme.showWarning(this, "Peringatan", "Silakan login terlebih dahulu.");
            return;
        }

        String tglStr = txtTanggal.getText().trim();
        Date tglMulai;
        try {
            tglMulai = Date.valueOf(tglStr);
        } catch (Exception ex) {
            UITheme.showError(this, "Format Tanggal Salah", "Format tanggal salah. Gunakan YYYY-MM-DD (Contoh: " + LocalDate.now() + ")");
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
            String dbNote = controller.isDatabaseConnected()
                    ? ""
                    : "\n\nCatatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
            UITheme.showSuccess(this, "Pengajuan Terkirim", "Pengajuan booking berhasil dikirim! Menunggu verifikasi admin." + dbNote);
            dispose();
            if (onSuccess != null) onSuccess.run();
        } else {
            UITheme.showError(this, "Gagal Booking", err);
        }
    }
}
