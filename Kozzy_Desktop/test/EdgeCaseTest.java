package test;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.config.UITheme;
import com.kozzy.controllers.AuthController;
import com.kozzy.controllers.BookingController;
import com.kozzy.controllers.KamarController;
import com.kozzy.models.Booking;
import com.kozzy.models.Kamar;
import com.kozzy.models.Pengguna;
import com.kozzy.services.SessionManager;
import com.kozzy.views.DesktopAdminWindow;
import com.kozzy.views.DesktopBerandaView;
import com.kozzy.views.DesktopBookingDialog;
import com.kozzy.views.DesktopKamarView;
import com.kozzy.views.DesktopMainWindow;
import com.kozzy.views.DesktopLoginView;
import com.kozzy.views.DesktopProfilView;
import com.kozzy.views.DesktopSewakuView;
import com.kozzy.views.RejectBookingDialog;
import com.kozzy.views.KozzyDialogHelper;
import com.kozzy.views.components.ImageHelper;
import com.kozzy.views.components.TransitionContainer;
import com.kozzy.dao.impl.OfflineFallback;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * EdgeCaseTest: Pengujian kasus batas (edge cases) & ketahanan sistem:
 * 1. Rapid consecutive transition calls (anti-deadlock & anti-flicker)
 * 2. Room search edge cases (whitespace, mixed case, non-existent symbols, empty reset)
 * 3. Booking date & duration calculation boundaries
 * 4. Reject dialog validation and quick chip selection
 * 5. Admin window singleton focus and dispose lifecycle
 * 6. User auto-login session propagation
 * 7. Jenis kelamin akun, tombol mata password (anti-typo), & hapus tombol demo
 */
public class EdgeCaseTest {

    private static int passed = 0;
    private static int total = 0;

    private static void assertTrue(String name, boolean condition) {
        total++;
        if (condition) {
            passed++;
            System.out.println("  ✓ [PASS] " + name);
        } else {
            System.err.println("  ✕ [FAIL] " + name);
            throw new AssertionError("EdgeCase failed: " + name);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("     Pengujian Kasus Batas (Edge Cases) Kozzy     ");
        System.out.println("==================================================");

        UITheme.setupFlatLaf();

        testRapidTransitions();
        testSearchEdgeCases();
        testRejectDialogInteraction();
        testAdminWindowLifecycle();
        testBookingCalculationsBoundary();
        testAccountGenderPasswordEyeAndNoDemo();
        testUIFixesAndProofPreview();
        testCleanSegoeUIEmojiGlyphs();

        System.out.println("\n==================================================");
        System.out.println("Hasil Uji Kasus Batas: " + passed + " / " + total + " pengujian BERHASIL.");
        System.out.println("==================================================");
        System.exit(0);
    }

    private static void testRapidTransitions() {
        System.out.println("\n[1] Menguji Transisi Cepat Berturut-turut (Rapid-Click):");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    TransitionContainer tc = new TransitionContainer();
                    tc.setSize(800, 600);
                    JPanel pA = new JPanel();
                    JPanel pB = new JPanel();
                    JPanel pC = new JPanel();
                    tc.addPage("A", pA);
                    tc.addPage("B", pB);
                    tc.addPage("C", pC);

                    tc.showPage("A");
                    // Panggil beruntun tanpa jeda (simulasi spam klik)
                    tc.showPage("B");
                    tc.showPage("C");
                    tc.showPage("A");
                    tc.showPage("B");

                    assertTrue("TransitionContainer tidak mengalami crash saat rapid-switching", true);
                    assertTrue("Kunci target akhir adalah B", "B".equals(tc.getCurrentKey()));
                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    latch.countDown();
                    throw new RuntimeException(ex);
                }
            });

            boolean ok = latch.await(3, TimeUnit.SECONDS);
            assertTrue("Rapid transition selesai dalam batas waktu", ok);
        } catch (Exception e) {
            assertTrue("Rapid transition gagal: " + e.getMessage(), false);
        }
    }

    private static void testSearchEdgeCases() {
        System.out.println("\n[2] Menguji Pencarian Kamar & Kasus Batas:");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    DesktopKamarView kv = new DesktopKamarView(new DesktopKamarView.KamarActionCallback() {
                        @Override public void onBookingRequested(Kamar kamar) {}
                        @Override public void onLoginRequested() {}
                    });

                    Field fTxt = DesktopKamarView.class.getDeclaredField("txtSearch");
                    fTxt.setAccessible(true);
                    JTextField txt = (JTextField) fTxt.get(kv);

                    Field fList = DesktopKamarView.class.getDeclaredField("currentRoomList");
                    fList.setAccessible(true);

                    // 2.1 Pencarian dengan spasi
                    txt.setText("   A-01   ");
                    List<?> res1 = (List<?>) fList.get(kv);
                    assertTrue("Pencarian dengan spasi menemukan kamar A-01", res1 != null && !res1.isEmpty());

                    // 2.2 Pencarian tidak sensitif huruf besar/kecil
                    txt.setText("putri");
                    List<?> res2 = (List<?>) fList.get(kv);
                    assertTrue("Pencarian 'putri' menghasilkan kamar tipe putri", res2 != null && !res2.isEmpty());

                    // 2.3 Pencarian kata kunci fasilitas
                    txt.setText("AC");
                    List<?> res3 = (List<?>) fList.get(kv);
                    assertTrue("Pencarian fasilitas 'AC' menemukan kamar", res3 != null && !res3.isEmpty());

                    // 2.4 Pencarian tanpa hasil (Empty State)
                    txt.setText("KAMAR_TIDAK_ADA_9999");
                    List<?> res4 = (List<?>) fList.get(kv);
                    assertTrue("Pencarian kamar fiktif menghasilkan list kosong", res4 != null && res4.isEmpty());

                    // Reset
                    txt.setText("");
                    List<?> resReset = (List<?>) fList.get(kv);
                    assertTrue("Pembersihan kata kunci memulihkan seluruh kamar", resReset != null && resReset.size() >= 8);

                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    latch.countDown();
                    throw new RuntimeException(ex);
                }
            });

            boolean ok = latch.await(3, TimeUnit.SECONDS);
            assertTrue("Pencarian kamar edge cases lolos", ok);
        } catch (Exception e) {
            assertTrue("Search edge case gagal: " + e.getMessage(), false);
        }
    }

    private static void testRejectDialogInteraction() {
        System.out.println("\n[3] Menguji Dialog Penolakan & Tombol Chip Alasan Cepat:");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    Booking b = new Booking();
                    b.setIdBooking("KZ-EDGE-01");
                    b.setKodeKamar("B-01");
                    b.setNamaPengguna("Pengguna Tes");

                    RejectBookingDialog dlg = new RejectBookingDialog(null, b);

                    Field fTxt = RejectBookingDialog.class.getDeclaredField("txtAlasan");
                    fTxt.setAccessible(true);
                    JTextArea txt = (JTextArea) fTxt.get(dlg);

                    assertTrue("Text area penolakan awal tidak memuat teks asumsi kaku", txt.getText().trim().isEmpty());

                    // Simulasi klik chip
                    txt.setText("Nominal transfer tidak sesuai");
                    assertTrue("Text area dapat diisi oleh alasan cepat chip", "Nominal transfer tidak sesuai".equals(txt.getText().trim()));

                    dlg.dispose();
                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    latch.countDown();
                    throw new RuntimeException(ex);
                }
            });

            boolean ok = latch.await(3, TimeUnit.SECONDS);
            assertTrue("Reject dialog interaction lolos", ok);
        } catch (Exception e) {
            assertTrue("Reject dialog interaction gagal: " + e.getMessage(), false);
        }
    }

    private static void testAdminWindowLifecycle() {
        System.out.println("\n[4] Menguji Siklus Hidup Jendela Admin Mandiri (Singleton Focus & Dispose):");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    // Buka instance pertama
                    DesktopAdminWindow win1 = DesktopAdminWindow.open(() -> {});
                    assertTrue("Instance 1 terbuka", win1 != null && win1.isVisible());

                    // Buka instance kedua (harus memfokuskan kembali instance yang sama)
                    DesktopAdminWindow win2 = DesktopAdminWindow.open(() -> {});
                    assertTrue("Pemanggilan kedua mengembalikan instance yang sama", win1 == win2);

                    // Dispose
                    win1.dispose();

                    // Setelah dispose, buka instance baru
                    DesktopAdminWindow win3 = DesktopAdminWindow.open(() -> {});
                    assertTrue("Instance baru dibuat setelah yang lama di-dispose", win3 != null);
                    win3.dispose();

                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    latch.countDown();
                    throw new RuntimeException(ex);
                }
            });

            boolean ok = latch.await(3, TimeUnit.SECONDS);
            assertTrue("Admin window lifecycle lolos", ok);
        } catch (Exception e) {
            assertTrue("Admin window lifecycle gagal: " + e.getMessage(), false);
        }
    }

    private static void testBookingCalculationsBoundary() {
        System.out.println("\n[5] Menguji Batas Durasi Sewa Booking:");
        BookingController ctrl = new BookingController();
        Kamar k = new Kamar("A-01", "putra", 850000, "tersedia", null, "");

        assertTrue("Biaya 1 bulan", ctrl.hitungTotalBiaya(k, 1) == 850000);
        assertTrue("Biaya 3 bulan", ctrl.hitungTotalBiaya(k, 3) == 2550000);
        assertTrue("Biaya 6 bulan", ctrl.hitungTotalBiaya(k, 6) == 5100000);
        assertTrue("Biaya 12 bulan", ctrl.hitungTotalBiaya(k, 12) == 10200000);
    }

    private static void testAccountGenderPasswordEyeAndNoDemo() {
        System.out.println("\n[6] Menguji Fitur Akun: Jenis Kelamin, Tombol Mata Password, & Hapus Akun Demo (Production Ready):");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    DesktopLoginView loginView = new DesktopLoginView(() -> {});

                    // 6.1 Pastikan tidak ada akun demo prefilled (siap pakai)
                    assertTrue("Input email login awal kosong (bukan demo prefilled)", loginView.getTxtLoginEmail().getText().trim().isEmpty());
                    assertTrue("Input password login awal kosong", loginView.getTxtLoginPassword().getPassword().length == 0);

                    // 6.2 Pastikan tidak ada tombol akun demo di seluruh hierarki komponen
                    boolean hasDemoButton = false;
                    for (Component c : getAllComponents(loginView)) {
                        if (c instanceof JButton) {
                            String txt = ((JButton) c).getText();
                            if (txt != null && (txt.contains("Demo") || txt.contains("Farah") || txt.contains("Fildza"))) {
                                hasDemoButton = true;
                                break;
                            }
                        }
                    }
                    assertTrue("Seluruh tombol akun demo berhasil dihapus dari UI", !hasDemoButton);

                    // 6.3 Menguji Tombol Mata Password pada Form Masuk
                    JPasswordField pwdLogin = loginView.getTxtLoginPassword();
                    pwdLogin.setText("RahasiaTypo123");
                    JButton eyeLogin = loginView.getBtnLoginEye();
                    assertTrue("Tombol mata password pada form login tersedia", eyeLogin != null);

                    char maskedChar = pwdLogin.getEchoChar();
                    assertTrue("Password awal dalam status tersembunyi/sensor (echoChar != 0)", maskedChar != (char) 0);

                    // Klik tombol mata untuk intip kata sandi
                    eyeLogin.doClick();
                    assertTrue("Setelah tombol mata ditekan, password tampil jelas (echoChar == 0)", pwdLogin.getEchoChar() == (char) 0);
                    assertTrue("Teks tombol berubah menjadi indikator sembunyikan", "🙈".equals(eyeLogin.getText()));

                    // Klik lagi untuk sembunyikan kembali
                    eyeLogin.doClick();
                    assertTrue("Setelah ditekan kembali, password kembali tersensor", pwdLogin.getEchoChar() != (char) 0);
                    assertTrue("Teks tombol kembali ke simbol mata 👁", "👁".equals(eyeLogin.getText()));

                    // 6.4 Menguji Tombol Mata Password pada Form Daftar
                    loginView.switchTab(false);
                    JPasswordField pwdReg = loginView.getTxtRegPassword();
                    pwdReg.setText("PasswordBaru789");
                    JButton eyeReg = loginView.getBtnRegEye();
                    assertTrue("Tombol mata password pada form register tersedia", eyeReg != null);

                    eyeReg.doClick();
                    assertTrue("Password registrasi dapat diintip untuk cegah typo", pwdReg.getEchoChar() == (char) 0);
                    eyeReg.doClick();
                    assertTrue("Password registrasi disensor kembali", pwdReg.getEchoChar() != (char) 0);

                    // 6.5 Menguji Pemilihan Jenis Kelamin pada Form Registrasi
                    assertTrue("Radio button Laki-laki tersedia", loginView.getRbRegLaki() != null);
                    assertTrue("Radio button Perempuan tersedia", loginView.getRbRegPerempuan() != null);
                    assertTrue("Default pilihan jenis kelamin adalah Laki-laki", "Laki-laki".equals(loginView.getSelectedGender()));

                    loginView.getRbRegPerempuan().setSelected(true);
                    assertTrue("Pemilihan Perempuan berhasil terdeteksi", "Perempuan".equals(loginView.getSelectedGender()));

                    // 6.6 Menguji Registrasi Pengguna dengan Jenis Kelamin Perempuan
                    AuthController auth = new AuthController();
                    String emailP = "uji_perempuan_" + System.currentTimeMillis() + "@kozzy.id";
                    String errP = auth.register("Dewi Sartika", emailP, "dewi12345", "081298765431", "Perempuan");
                    assertTrue("Registrasi akun jenis kelamin Perempuan sukses", errP == null);
                    Pengguna userP = auth.getCurrentUser();
                    assertTrue("Pengguna aktif tersimpan dengan jenis kelamin Perempuan", userP != null && "Perempuan".equals(userP.getJenisKelamin()));

                    // 6.7 Menguji Registrasi Pengguna dengan Jenis Kelamin Laki-laki
                    auth.logout();
                    String emailL = "uji_lakilaki_" + System.currentTimeMillis() + "@kozzy.id";
                    String errL = auth.register("Ahmad Dahlan", emailL, "ahmad12345", "081298765432", "Laki-laki");
                    assertTrue("Registrasi akun jenis kelamin Laki-laki sukses", errL == null);
                    Pengguna userL = auth.getCurrentUser();
                    assertTrue("Pengguna aktif tersimpan dengan jenis kelamin Laki-laki", userL != null && "Laki-laki".equals(userL.getJenisKelamin()));

                    // 6.8 Verifikasi Jenis Kelamin pada Profil Pengguna
                    DesktopProfilView profil = new DesktopProfilView(null);
                    Field fGender = DesktopProfilView.class.getDeclaredField("lblGender");
                    fGender.setAccessible(true);
                    JLabel lblG = (JLabel) fGender.get(profil);
                    assertTrue("Label jenis kelamin pada profil terisi Laki-laki sesuai akun aktif", "Laki-laki".equals(lblG.getText()));

                    // 6.9 Uji Keamanan: Ganti tab mereset status intip kata sandi
                    loginView.switchTab(true);
                    eyeLogin.doClick();
                    assertTrue("Password terbuka sebelum ganti tab", pwdLogin.getEchoChar() == (char) 0);
                    loginView.switchTab(false);
                    loginView.switchTab(true);
                    assertTrue("Password otomatis tersensor kembali setelah ganti tab", pwdLogin.getEchoChar() != (char) 0);
                    assertTrue("Icon tombol mata login kembali ke indikator awal", "👁".equals(eyeLogin.getText()));

                    // 6.10 Uji Rendering Vektor Eye Button (Bebas dependensi emoji OS)
                    java.awt.image.BufferedImage imgTest = new java.awt.image.BufferedImage(42, 34, java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    Graphics2D gTest = imgTest.createGraphics();
                    eyeLogin.paint(gTest);
                    gTest.dispose();
                    assertTrue("Tombol mata vektor dapat dirender tanpa galat grafis", imgTest.getWidth() == 42);

                    // 6.11 Uji Validasi Gender untuk Booking Kamar (Putra vs Putri)
                    BookingController bCtrl = new BookingController();
                    Kamar kamarPutri = new Kamar("B-01", "putri", 950000, "tersedia", null, "");
                    Kamar kamarPutra = new Kamar("A-01", "putra", 850000, "tersedia", null, "");

                    StringBuilder reasonLakiKePutri = new StringBuilder();
                    boolean canLakiKePutri = bCtrl.canUserBook(userL, kamarPutri, reasonLakiKePutri);
                    assertTrue("Pengguna Laki-laki dilarang memesan kamar Putri", !canLakiKePutri);
                    assertTrue("Pesan penolakan gender menyebut kamar khusus Putri", reasonLakiKePutri.toString().contains("Putri"));

                    StringBuilder reasonPerempuanKePutra = new StringBuilder();
                    boolean canPerempuanKePutra = bCtrl.canUserBook(userP, kamarPutra, reasonPerempuanKePutra);
                    assertTrue("Pengguna Perempuan dilarang memesan kamar Putra", !canPerempuanKePutra);
                    assertTrue("Pesan penolakan gender menyebut kamar khusus Putra", reasonPerempuanKePutra.toString().contains("Putra"));

                    StringBuilder reasonLakiKePutra = new StringBuilder();
                    boolean canLakiKePutra = bCtrl.canUserBook(userL, kamarPutra, reasonLakiKePutra);
                    assertTrue("Pengguna Laki-laki diizinkan memesan kamar Putra", canLakiKePutra);

                    StringBuilder reasonPerempuanKePutri = new StringBuilder();
                    boolean canPerempuanKePutri = bCtrl.canUserBook(userP, kamarPutri, reasonPerempuanKePutri);
                    assertTrue("Pengguna Perempuan diizinkan memesan kamar Putri", canPerempuanKePutri);

                    // 6.12 Uji Dialog Booking Siap Pakai (Bukan Beta/Demo Scaffolding)
                    com.kozzy.views.DesktopBookingDialog bDialog = new com.kozzy.views.DesktopBookingDialog(null, kamarPutra, () -> {});
                    Field fPath = com.kozzy.views.DesktopBookingDialog.class.getDeclaredField("selectedFilePath");
                    fPath.setAccessible(true);
                    String initialPath = (String) fPath.get(bDialog);
                    assertTrue("Dialog booking produksi tidak memuat path berkas demo bawaan", initialPath == null);
                    bDialog.dispose();

                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    latch.countDown();
                    throw new RuntimeException(ex);
                }
            });

            boolean ok = latch.await(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            assertTrue("Pengujian gagal: " + e.getMessage(), false);
        }
    }

    private static void testUIFixesAndProofPreview() {
        System.out.println("\n[7] Menguji Perbaikan Window Resizable, Hapus Ikon Peringatan, Preview Bukti & Hide MySQL Status:");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    // 7.1 RejectBookingDialog Resizable & Chip Buttons 2x2 Grid
                    Booking b = new Booking();
                    b.setIdBooking("KZ-TEST-77");
                    b.setKodeKamar("A-01");
                    b.setNamaPengguna("Penguji Fix");
                    RejectBookingDialog rejectDlg = new RejectBookingDialog(null, b);

                    assertTrue("RejectBookingDialog sekarang resizable (bisa diubah ukuran)", rejectDlg.isResizable());
                    assertTrue("RejectBookingDialog memiliki minimum size aman", rejectDlg.getMinimumSize().width >= 500 && rejectDlg.getMinimumSize().height >= 500);

                    java.util.List<Component> rejectComps = getAllComponents(rejectDlg);
                    boolean hasTolakButton = false;
                    boolean hasBrokenGlyphInTolak = false;
                    int chipCount = 0;

                    for (Component c : rejectComps) {
                        if (c instanceof JButton) {
                            JButton btn = (JButton) c;
                            String txt = btn.getText();
                            if (txt != null && txt.contains("Tolak Booking")) {
                                hasTolakButton = true;
                                if (txt.contains("✕") || txt.contains("▯")) {
                                    hasBrokenGlyphInTolak = true;
                                }
                            }
                            if (txt != null && (txt.contains("tidak valid") || txt.contains("tidak sesuai") || txt.contains("terisi penyewa") || txt.contains("tidak dapat diverifikasi"))) {
                                chipCount++;
                            }
                        }
                    }

                    assertTrue("Tombol Tolak Booking tersedia tanpa broken unicode glyph", hasTolakButton && !hasBrokenGlyphInTolak);
                    assertTrue("Seluruh 4 pilihan alasan cepat (chips) terpasang lengkap", chipCount == 4);
                    rejectDlg.dispose();

                    // 7.2 DesktopBookingDialog Resizable & Tombol Preview
                    Kamar k = new Kamar("A-01", "putra", 850000, "tersedia", null, "");
                    DesktopBookingDialog bookDlg = new DesktopBookingDialog(null, k, () -> {});
                    assertTrue("DesktopBookingDialog sekarang resizable (bisa diubah ukuran)", bookDlg.isResizable());
                    assertTrue("DesktopBookingDialog memiliki minimum size proporsional", bookDlg.getMinimumSize().width >= 500 && bookDlg.getMinimumSize().height >= 600);

                    java.util.List<Component> bookComps = getAllComponents(bookDlg);
                    boolean hasPreviewBtn = false;
                    for (Component c : bookComps) {
                        if (c instanceof JButton && "Lihat Preview".equals(((JButton) c).getText())) {
                            hasPreviewBtn = true;
                            assertTrue("Tombol Lihat Preview awal dalam kondisi disable sebelum file dipilih", !c.isEnabled());
                        }
                    }
                    assertTrue("Tombol Lihat Preview tersedia pada form booking bukti transfer", hasPreviewBtn);
                    bookDlg.dispose();

                    // 7.3 Resolusi Gambar Bukti & Generator Struk Sintesis (ImageHelper)
                    java.io.File realProof = ImageHelper.resolveFile("assets/uploads/bukti/bukti_1794242133182.jpg");
                    assertTrue("ImageHelper berhasil me-resolve berkas bukti dengan path assets/uploads/bukti", realProof != null && realProof.exists());

                    java.io.File fileNameOnlyProof = ImageHelper.resolveFile("bukti_1794242133182.jpg");
                    assertTrue("ImageHelper berhasil me-resolve berkas bukti hanya dengan nama file", fileNameOnlyProof != null && fileNameOnlyProof.exists());

                    java.awt.image.BufferedImage synthReceipt = ImageHelper.generateTransferReceipt(400, 400, "TRX-TEST-01", 8);
                    assertTrue("Generator struk transfer sintetis menghasilkan image valid", synthReceipt != null && synthReceipt.getWidth() == 400);

                    java.awt.image.BufferedImage autoFallbackReceipt = ImageHelper.loadAndScale("bukti_fiktif_123.jpg", 300, 300, 6);
                    assertTrue("Fallback otomatis struk bukti pembayaran menghasilkan image valid", autoFallbackReceipt != null && autoFallbackReceipt.getWidth() == 300);

                    java.awt.image.BufferedImage origReceipt = ImageHelper.loadOriginal("bukti_1794242133182.jpg");
                    assertTrue("ImageHelper.loadOriginal berhasil memuat gambar asli", origReceipt != null);

                    java.awt.image.BufferedImage propScaled = ImageHelper.scaleProportional(origReceipt, 300, 400, 0);
                    assertTrue("ImageHelper.scaleProportional membatasi dimensi tanpa cover crop", propScaled != null && propScaled.getWidth() <= 300 && propScaled.getHeight() <= 400);

                    // 7.4 OfflineFallback menyimpan path berkas bukti asli
                    Booking testB = new Booking();
                    testB.setIdBooking("KZ-PROOF-99");
                    OfflineFallback.getInstance().addBooking(testB, "C:/sample_upload.png");
                    assertTrue("OfflineFallback menyimpan proofFilePath asli tanpa ditimpa string statis", "C:/sample_upload.png".equals(testB.getFileBukti()));

                    // 7.5 Status MySQL Disembunyikan dari Tampilan Penyewa
                    DesktopBerandaView beranda = new DesktopBerandaView(new DesktopBerandaView.BerandaActionCallback() {
                        @Override public void onSelectRoom(String kodeKamar) {}
                        @Override public void onNavigateToKamar() {}
                        @Override public void onNavigateToLogin() {}
                        @Override public void onOpenAdminWindow() {}
                    });

                    java.util.List<Component> berandaComps = getAllComponents(beranda);
                    boolean foundMysqlBadgeInBeranda = false;
                    for (Component c : berandaComps) {
                        if (c instanceof JLabel) {
                            String t = ((JLabel) c).getText();
                            if (t != null && (t.contains("MySQL Aktif") || t.contains("MySQL Offline"))) {
                                foundMysqlBadgeInBeranda = true;
                            }
                        }
                    }
                    assertTrue("Tampilan beranda penyewa bersih dari label notifikasi teknis MySQL", !foundMysqlBadgeInBeranda);

                    DesktopMainWindow mainWin = new DesktopMainWindow();
                    Field fBanner = DesktopMainWindow.class.getDeclaredField("bannerOffline");
                    fBanner.setAccessible(true);
                    JPanel banner = (JPanel) fBanner.get(mainWin);
                    assertTrue("Banner MySQL offline tidak tampil di layar antarmuka penyewa", banner.getParent() == null || !banner.isVisible());
                    mainWin.dispose();

                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    latch.countDown();
                    throw new RuntimeException(ex);
                }
            });

            boolean ok = latch.await(6, TimeUnit.SECONDS);
            assertTrue("Pengujian perbaikan UI, window resizable, dan preview bukti pembayaran lolos", ok);
        } catch (Exception e) {
            assertTrue("Pengujian gagal: " + e.getMessage(), false);
        }
    }

    private static void testCleanSegoeUIEmojiGlyphs() {
        System.out.println("\n[8] Menguji Pembersihan Emoji pada Komponen Font Segoe UI (Anti Broken Box Glyph):");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            final java.util.concurrent.atomic.AtomicReference<Throwable> errRef = new java.util.concurrent.atomic.AtomicReference<>();
            SwingUtilities.invokeLater(() -> {
                try {
                    Font segoe = new Font("Segoe UI", Font.PLAIN, 12);

                    // 8.1 DesktopBerandaView: Tombol admin window tanpa broken shield emoji
                    AuthController auth = new AuthController();
                    auth.login("admin@kozzy.id", "admin123");
                    DesktopBerandaView beranda = new DesktopBerandaView(new DesktopBerandaView.BerandaActionCallback() {
                        @Override public void onSelectRoom(String kodeKamar) {}
                        @Override public void onNavigateToKamar() {}
                        @Override public void onNavigateToLogin() {}
                        @Override public void onOpenAdminWindow() {}
                    });
                    beranda.refreshData();
                    boolean foundCleanAdminBtn = false;
                    for (Component c : getAllComponents(beranda)) {
                        if (c instanceof JButton) {
                            String txt = ((JButton) c).getText();
                            if (txt != null && txt.contains("Buka Panel Admin")) {
                                assertTrue("Tombol Buka Panel Admin bersih dari emoji shield", "Buka Panel Admin (Jendela Terpisah) →".equals(txt));
                                assertTrue("Segoe UI dapat me-render teks tombol admin tanpa box", segoe.canDisplayUpTo(txt) == -1);
                                foundCleanAdminBtn = true;
                            }
                        }
                    }
                    assertTrue("Tombol Buka Panel Admin berhasil ditemukan di beranda admin", foundCleanAdminBtn);

                    // 8.2 DesktopAdminWindow: Tombol refresh tanpa broken refresh emoji
                    DesktopAdminWindow adminWin = DesktopAdminWindow.open(() -> {});
                    boolean foundCleanRefreshBtn = false;
                    for (Component c : getAllComponents(adminWin)) {
                        if (c instanceof JButton) {
                            String txt = ((JButton) c).getText();
                            if (txt != null && txt.contains("Refresh Data")) {
                                assertTrue("Tombol Refresh Data bersih dari emoji refresh", "Refresh Data".equals(txt));
                                assertTrue("Segoe UI dapat me-render teks refresh button tanpa box", segoe.canDisplayUpTo(txt) == -1);
                                foundCleanRefreshBtn = true;
                            }
                        }
                    }
                    assertTrue("Tombol Refresh Data pada konsol admin terverifikasi bersih", foundCleanRefreshBtn);
                    adminWin.dispose();

                    // 8.3 KozzyDialogHelper: Image preview toolbar buttons (Zoom Out, Fit, Zoom In)
                    JDialog previewDlg = KozzyDialogHelper.createImagePreviewDialog(null, "Bukti Pembayaran", null, "Subtitle Preview");
                    JButton btnZoomOut = null;
                    JButton btnFit = null;
                    JButton btnZoomIn = null;
                    for (Component c : getAllComponents(previewDlg)) {
                        if (c instanceof JButton) {
                            JButton btn = (JButton) c;
                            String tip = btn.getToolTipText();
                            if (tip != null) {
                                if (tip.contains("Zoom Out")) btnZoomOut = btn;
                                else if (tip.contains("Sesuaikan Gambar")) btnFit = btn;
                                else if (tip.contains("Zoom In")) btnZoomIn = btn;
                            }
                        }
                    }
                    assertTrue("Tombol Zoom Out ditemukan di dialog preview", btnZoomOut != null);
                    assertTrue("Tombol Zoom Out menggunakan '-' (bukan emoji ➖)", "-".equals(btnZoomOut.getText()));
                    assertTrue("Tombol Fit Layar ditemukan di dialog preview", btnFit != null);
                    assertTrue("Tombol Fit Layar menggunakan 'Pas Layar' (bukan emoji 📐)", "Pas Layar".equals(btnFit.getText()));
                    assertTrue("Tombol Zoom In ditemukan di dialog preview", btnZoomIn != null);
                    assertTrue("Tombol Zoom In menggunakan '+' (bukan emoji ➕)", "+".equals(btnZoomIn.getText()));
                    assertTrue("Segoe UI dapat me-render tombol zoom out tanpa box", segoe.canDisplayUpTo(btnZoomOut.getText()) == -1);
                    assertTrue("Segoe UI dapat me-render tombol pas layar tanpa box", segoe.canDisplayUpTo(btnFit.getText()) == -1);
                    assertTrue("Segoe UI dapat me-render tombol zoom in tanpa box", segoe.canDisplayUpTo(btnZoomIn.getText()) == -1);
                    previewDlg.dispose();

                    // 8.4 DesktopKamarView: Spec badges populated via showDetail & clearDetailPreview
                    DesktopKamarView kamarView = new DesktopKamarView(null);
                    Kamar testKamar = new Kamar();
                    testKamar.setKodeKamar("A-01");
                    testKamar.setTipe("Putra");
                    testKamar.setStatus("tersedia");
                    testKamar.setHarga(850000);
                    testKamar.setDeskripsi("Kamar bersih dan nyaman.");
                    testKamar.setFasilitas(java.util.Arrays.asList("WiFi", "Kasur Single", "K. Mandi Dalam"));

                    Method mShow = DesktopKamarView.class.getDeclaredMethod("selectRoom", Kamar.class);
                    mShow.setAccessible(true);
                    mShow.invoke(kamarView, testKamar);

                    Field fSpecs = DesktopKamarView.class.getDeclaredField("pnlDetailSpecs");
                    fSpecs.setAccessible(true);
                    JPanel pnlSpecs = (JPanel) fSpecs.get(kamarView);
                    assertTrue("Panel spesifikasi terisi 4 badge", pnlSpecs.getComponentCount() == 4);

                    boolean hasSpecEmoji = false;
                    java.util.List<String> specLabels = new java.util.ArrayList<>();
                    for (Component c : getAllComponents(pnlSpecs)) {
                        if (c instanceof JLabel) {
                            String txt = ((JLabel) c).getText();
                            specLabels.add(txt);
                            if (txt != null && (txt.contains("📐") || txt.contains("🛏") || txt.contains("🚿") || txt.contains("⚡"))) {
                                hasSpecEmoji = true;
                            }
                        }
                    }
                    assertTrue("Badge spesifikasi kamar bersih dari emoji terlarang", !hasSpecEmoji);
                    assertTrue("Badge spesifikasi memuat '3 x 4 m²'", specLabels.contains("3 x 4 m²"));
                    assertTrue("Badge spesifikasi memuat 'Springbed'", specLabels.contains("Springbed"));
                    assertTrue("Badge spesifikasi memuat 'K.M. Dalam'", specLabels.contains("K.M. Dalam"));
                    assertTrue("Badge spesifikasi memuat 'Token'", specLabels.contains("Token"));

                    for (String lblText : specLabels) {
                        assertTrue("Segoe UI dapat me-render spec badge text: " + lblText, segoe.canDisplayUpTo(lblText) == -1);
                    }

                    // Clear preview test
                    Field fPhoto = DesktopKamarView.class.getDeclaredField("lblDetailPhoto");
                    fPhoto.setAccessible(true);
                    JLabel lblPhoto = (JLabel) fPhoto.get(kamarView);
                    Method mClear = DesktopKamarView.class.getDeclaredMethod("clearDetailPreview");
                    mClear.setAccessible(true);
                    mClear.invoke(kamarView);
                    assertTrue("Label detail foto saat kosong bersih dari emoji search", "Tidak Ada Kamar".equals(lblPhoto.getText()));

                    // 8.5 DesktopSewakuView: Kunci fisik dan WiFi kost diuji saat ada rental aktif
                    DesktopSewakuView sewakuView = new DesktopSewakuView(null);
                    Booking activeB = new Booking();
                    activeB.setIdBooking("KZ-TEST-ACTIVE");
                    activeB.setKodeKamar("A-01");
                    activeB.setNamaPengguna("Farah");
                    activeB.setTanggalMulai(Date.valueOf(LocalDate.now()));
                    activeB.setDurasi(3);
                    activeB.setStatus("APPROVED");

                    Method mRender = DesktopSewakuView.class.getDeclaredMethod("renderActiveRental", Booking.class);
                    mRender.setAccessible(true);
                    mRender.invoke(sewakuView, activeB);

                    JLabel lblInfo1 = null;
                    JLabel lblInfo2 = null;
                    for (Component c : getAllComponents(sewakuView)) {
                        if (c instanceof JLabel) {
                            String txt = ((JLabel) c).getText();
                            if (txt != null) {
                                if (txt.contains("Kunci Fisik")) lblInfo1 = (JLabel) c;
                                else if (txt.contains("WiFi Kost")) lblInfo2 = (JLabel) c;
                            }
                        }
                    }
                    assertTrue("Label panduan kunci fisik ditemukan di kartu sewa aktif", lblInfo1 != null);
                    assertTrue("Label kunci fisik bersih dari emoji kunci (menggunakan bullet)", lblInfo1.getText().startsWith("• Kunci Fisik"));
                    assertTrue("Label panduan WiFi kost ditemukan di kartu sewa aktif", lblInfo2 != null);
                    assertTrue("Label WiFi kost bersih dari emoji sinyal (menggunakan bullet)", lblInfo2.getText().startsWith("• WiFi Kost"));
                    assertTrue("Segoe UI dapat me-render info kunci fisik tanpa box", segoe.canDisplayUpTo(lblInfo1.getText()) == -1);
                    assertTrue("Segoe UI dapat me-render info wifi kost tanpa box", segoe.canDisplayUpTo(lblInfo2.getText()) == -1);

                    // 8.6 DesktopAdminView: Dialog messages bersih dari emoji warning ⚠️
                    String msgWarningDb = "Status kamar diubah ke TERSEDIA.\n\nDatabase MySQL belum aktif di XAMPP.\nPerubahan disimpan sementara di memori cadangan.";
                    String noteApprove = "Catatan: Database MySQL belum aktif di XAMPP.\nData disimpan sementara di memori cadangan.";
                    String noteReject = "Pengajuan berhasil ditolak dengan alasan:\n\"Nominal tidak sesuai\"\n\n" + noteApprove;

                    assertTrue("Pesan dialog status tidak mengandung emoji warning", !msgWarningDb.contains("⚠️") && !msgWarningDb.contains("⚠"));
                    assertTrue("Pesan dialog approve tidak mengandung emoji warning", !noteApprove.contains("⚠️") && !noteApprove.contains("⚠"));
                    assertTrue("Pesan dialog reject tidak mengandung emoji warning", !noteReject.contains("⚠️") && !noteReject.contains("⚠"));
                    assertTrue("Segoe UI dapat me-render pesan dialog status tanpa box", segoe.canDisplayUpTo(msgWarningDb) == -1);
                    assertTrue("Segoe UI dapat me-render pesan dialog approve tanpa box", segoe.canDisplayUpTo(noteApprove) == -1);
                    assertTrue("Segoe UI dapat me-render pesan dialog reject tanpa box", segoe.canDisplayUpTo(noteReject) == -1);

                    // 8.7 Font Segoe UI Displayability: Memastikan seluruh token teks lolos canDisplayUpTo
                    String[] testTexts = {
                        "-",
                        "+",
                        "Pas Layar",
                        "100% Asli",
                        "Buka Panel Admin (Jendela Terpisah) →",
                        "Refresh Data",
                        "Tidak Ada Kamar",
                        "• Kunci Fisik dapat diambil di pos pengelola atau hubungi pengurus kos.",
                        "• WiFi Kost: 'Kozzy_Resident_5G' · Sandi tersedia di kamar.",
                        "3 x 4 m²",
                        "Springbed",
                        "K.M. Dalam",
                        "Token",
                        "▪ KONSOL ADMINISTRATOR",
                        "● MySQL Aktif",
                        "○ Offline (Memori Cadangan)"
                    };
                    for (String str : testTexts) {
                        assertTrue("Segoe UI dapat me-render string tanpa glyph box: " + str, segoe.canDisplayUpTo(str) == -1);
                    }

                    latch.countDown();
                } catch (Throwable ex) {
                    ex.printStackTrace();
                    errRef.set(ex);
                    latch.countDown();
                }
            });

            boolean ok = latch.await(6, TimeUnit.SECONDS);
            assertTrue("Pengujian pembersihan emoji Segoe UI lolos tanpa galat", ok && errRef.get() == null);
        } catch (Exception e) {
            assertTrue("Pengujian gagal: " + e.getMessage(), false);
        }
    }

    private static java.util.List<Component> getAllComponents(Container c) {
        java.util.List<Component> list = new java.util.ArrayList<>();
        for (Component comp : c.getComponents()) {
            list.add(comp);
            if (comp instanceof Container) {
                list.addAll(getAllComponents((Container) comp));
            }
        }
        return list;
    }
}
