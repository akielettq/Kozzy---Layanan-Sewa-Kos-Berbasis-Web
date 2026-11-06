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

        setSize(480, 560);
        setLocationRelativeTo(owner);
        getContentPane().setBackground(UITheme.BG_CARD);
        setLayout(new BorderLayout());

        initUI();
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
        content.add(lblTitle);
        content.add(Box.createVerticalStrut(4));

        JLabel lblSub = new JLabel("Kamar " + kamar.getKodeKamar() + " (" + kamar.getTipe().toUpperCase() + ") · " + kamar.formatRupiah() + " / bln");
        lblSub.setFont(UITheme.FONT_BODY);
        lblSub.setForeground(UITheme.PRIMARY_GREEN);
        content.add(lblSub);
        content.add(Box.createVerticalStrut(16));

        // Form Fields
        content.add(createLabel("Tanggal Mulai Sewa (YYYY-MM-DD)"));
        txtTanggal = new JTextField(LocalDate.now().plusDays(1).toString());
        txtTanggal.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        content.add(txtTanggal);
        content.add(Box.createVerticalStrut(10));

        content.add(createLabel("Durasi Sewa"));
        Integer[] durasiOptions = {1, 3, 6, 12};
        cmbDurasi = new JComboBox<>(durasiOptions);
        cmbDurasi.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cmbDurasi.addActionListener(e -> updateTotalBiaya());
        content.add(cmbDurasi);
        content.add(Box.createVerticalStrut(10));

        content.add(createLabel("Metode Pembayaran"));
        List<MetodePembayaran> methods = controller.getAllPaymentMethods();
        cmbMetode = new JComboBox<>(methods.toArray(new MetodePembayaran[0]));
        cmbMetode.setMaximumSize(new Dimension(Integer.MAX_VALUE, 32));
        cmbMetode.addActionListener(e -> updateMetodeInfo());
        content.add(cmbMetode);
        content.add(Box.createVerticalStrut(6));

        // Info Rekening Tujuan
        lblNomorTujuan = new JLabel("Tujuan: -");
        lblNomorTujuan.setFont(UITheme.FONT_SMALL);
        lblNomorTujuan.setForeground(UITheme.TEXT_MUTED);
        content.add(lblNomorTujuan);
        content.add(Box.createVerticalStrut(10));

        // Bukti Transfer
        content.add(createLabel("Bukti Transfer"));
        JPanel fileRow = new JPanel(new BorderLayout(8, 0));
        fileRow.setOpaque(false);
        fileRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 34));

        lblFileBukti = new JLabel("bukti_transfer.jpg (Default Demo)");
        lblFileBukti.setFont(UITheme.FONT_SMALL);
        lblFileBukti.setForeground(UITheme.TEXT_MUTED);
        fileRow.add(lblFileBukti, BorderLayout.CENTER);

        JButton btnChoose = new JButton("Pilih File...");
        btnChoose.setFont(UITheme.FONT_SMALL);
        btnChoose.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnChoose.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                selectedFilePath = f.getAbsolutePath();
                lblFileBukti.setText(f.getName());
            }
        });
        fileRow.add(btnChoose, BorderLayout.EAST);
        content.add(fileRow);
        content.add(Box.createVerticalStrut(16));

        // Total Biaya Box
        JPanel totalBox = new JPanel(new BorderLayout());
        totalBox.setBackground(UITheme.BG_CARD_ALT);
        totalBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(UITheme.BORDER_COLOR, 1, true),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
        ));
        totalBox.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

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

        PillButton btnBatal = new PillButton("Batal", PillButton.Style.OUTLINE);
        btnBatal.addActionListener(e -> dispose());
        btnRow.add(btnBatal);

        PillButton btnSubmit = new PillButton("Kirim Pengajuan Booking →", PillButton.Style.PRIMARY);
        btnSubmit.addActionListener(e -> submitBooking());
        btnRow.add(btnSubmit);

        content.add(btnRow);

        add(content, BorderLayout.CENTER);

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
            JOptionPane.showMessageDialog(this, "Silakan login terlebih dahulu.", "Peringatan", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String tglStr = txtTanggal.getText().trim();
        Date tglMulai;
        try {
            tglMulai = Date.valueOf(tglStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Format tanggal salah. Gunakan YYYY-MM-DD (Contoh: " + LocalDate.now() + ")", "Galat", JOptionPane.ERROR_MESSAGE);
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
            String dbNote = controller.isDatabaseConnected() ? "" : "\n\n⚠️ Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
            JOptionPane.showMessageDialog(this, "Pengajuan booking berhasil dikirim! Menunggu verifikasi admin." + dbNote, "Sukses", JOptionPane.INFORMATION_MESSAGE);
            dispose();
            if (onSuccess != null) onSuccess.run();
        } else {
            JOptionPane.showMessageDialog(this, err, "Gagal Booking", JOptionPane.ERROR_MESSAGE);
        }
    }
}
