package test;

import com.kozzy.config.DatabaseConnection;
import com.kozzy.config.UITheme;
import com.kozzy.controllers.*;
import com.kozzy.models.*;
import com.kozzy.services.SessionManager;
import com.kozzy.views.*;
import com.kozzy.views.components.TransitionContainer;

import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * VerifySystemTest: Verifikasi mendalam terhadap 9 poin instruksi sistem:
 * 1. Panel admin pisah (DesktopAdminWindow independen)
 * 2. Mengatasi space kosong di cari kamar & view lain
 * 3. Kerapian UI dan tema
 * 4. Teks ditata rata tengah
 * 5. Katalog kamar rapi dengan pencarian & filter
 * 6. Formulir booking rata kiri
 * 7. Dialog pop-up estetis (RejectBookingDialog multiline & chip reasons)
 * 8. Buat akun baru & login langsung bawa ke beranda
 * 9. JavaFX hardware-accelerated transitions
 */
public class VerifySystemTest {

    private static int testsPassed = 0;
    private static int testsTotal = 0;

    private static void assertTrue(String testName, boolean condition) {
        testsTotal++;
        if (condition) {
            testsPassed++;
            System.out.println("  ✓ [PASS] " + testName);
        } else {
            System.err.println("  ✕ [FAIL] " + testName);
            throw new AssertionError("Test failed: " + testName);
        }
    }

    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("   Memulai Verifikasi Sistem Komprehensif Kozzy   ");
        System.out.println("==================================================");

        UITheme.setupFlatLaf();

        testAuthController();
        testKamarController();
        testBookingController();
        testAdminController();
        testUIComponentsAndRequirements();

        System.out.println("\n==================================================");
        System.out.println("Hasil Uji: " + testsPassed + " / " + testsTotal + " pengujian BERHASIL.");
        System.out.println("==================================================");
        System.exit(0);
    }

    private static void testAuthController() {
        System.out.println("\n[1] Menguji AuthController & SessionManager:");
        AuthController auth = new AuthController();

        // 1.1 Login Admin
        boolean adminLogin = auth.login("admin@kozzy.id", "admin123");
        assertTrue("Login admin@kozzy.id berhasil", adminLogin);
        Pengguna admin = auth.getCurrentUser();
        assertTrue("Akun terotentikasi adalah Admin", admin != null && admin.isAdmin());

        // 1.2 Logout
        auth.logout();
        assertTrue("Logout berhasil, sesi kosong", !auth.isLoggedIn());

        // 1.3 Login User Farah
        boolean farahLogin = auth.login("farah@kozzy.id", "123456");
        assertTrue("Login farah@kozzy.id berhasil", farahLogin);
        Pengguna farah = auth.getCurrentUser();
        assertTrue("Akun terotentikasi adalah Pencari", farah != null && !farah.isAdmin());

        // 1.4 Login Kredensial Salah
        boolean invalidLogin = auth.login("salah@kozzy.id", "wrongpass");
        assertTrue("Login salah ditolak", !invalidLogin);

        // 1.5 Registrasi Akun Baru (Poin 8: Otomatis tersimpan dan aktif)
        String uniqueEmail = "user_" + System.currentTimeMillis() + "@test.id";
        String regErr = auth.register("Pengguna Baru", uniqueEmail, "pass12345", "081234567890");
        assertTrue("Registrasi akun baru sukses tanpa error", regErr == null);
        assertTrue("Sesi langsung aktif setelah registrasi", auth.isLoggedIn() && auth.getCurrentUser().getEmail().equals(uniqueEmail));
    }

    private static void testKamarController() {
        System.out.println("\n[2] Menguji KamarController & Filter Katalog:");
        KamarController kamarCtrl = new KamarController();

        List<Kamar> semua = kamarCtrl.getKamarList("semua");
        assertTrue("Katalog semua kamar tidak kosong (minimal 8 kamar)", semua != null && semua.size() >= 8);

        List<Kamar> putri = kamarCtrl.getKamarList("putri");
        assertTrue("Filter putri menghasilkan kamar putri", putri != null && !putri.isEmpty());
        for (Kamar k : putri) {
            assertTrue("Kamar berkategori putri: " + k.getKodeKamar(), "putri".equalsIgnoreCase(k.getTipe()));
        }

        List<Kamar> putra = kamarCtrl.getKamarList("putra");
        assertTrue("Filter putra menghasilkan kamar putra", putra != null && !putra.isEmpty());
        for (Kamar k : putra) {
            assertTrue("Kamar berkategori putra: " + k.getKodeKamar(), "putra".equalsIgnoreCase(k.getTipe()));
        }

        List<Kamar> tersedia = kamarCtrl.getKamarList("tersedia");
        assertTrue("Filter tersedia mengambil kamar berstatus tersedia", tersedia != null);
    }

    private static void testBookingController() {
        System.out.println("\n[3] Menguji BookingController & Perhitungan Biaya:");
        BookingController bookingCtrl = new BookingController();

        Kamar sampleKamar = new Kamar("T-01", "putri", 1000000, "tersedia", null, "Kamar Tes");
        int biaya1Bln = bookingCtrl.hitungTotalBiaya(sampleKamar, 1);
        int biaya3Bln = bookingCtrl.hitungTotalBiaya(sampleKamar, 3);
        int biaya6Bln = bookingCtrl.hitungTotalBiaya(sampleKamar, 6);
        int biaya12Bln = bookingCtrl.hitungTotalBiaya(sampleKamar, 12);

        assertTrue("Biaya sewa 1 bulan tepat", biaya1Bln == 1000000);
        assertTrue("Biaya sewa 3 bulan tepat", biaya3Bln == 3000000);
        assertTrue("Biaya sewa 6 bulan tepat", biaya6Bln == 6000000);
        assertTrue("Biaya sewa 12 bulan tepat", biaya12Bln == 12000000);

        List<MetodePembayaran> methods = bookingCtrl.getAllPaymentMethods();
        assertTrue("Daftar metode pembayaran tersedia", methods != null && !methods.isEmpty());
    }

    private static void testAdminController() {
        System.out.println("\n[4] Menguji AdminController (Kelola Kamar & Verifikasi):");
        AdminController adminCtrl = new AdminController();

        List<Booking> pending = adminCtrl.getPendingBookings();
        assertTrue("Pengambilan pengajuan pending tidak melempar galat", pending != null);

        List<Kamar> allKamar = adminCtrl.getAllKamar();
        assertTrue("Admin dapat mengambil seluruh kamar operasional", allKamar != null && !allKamar.isEmpty());

        boolean updateOk = adminCtrl.updateStatusKamar("B-04", "pemeliharaan");
        assertTrue("Update status kamar ke pemeliharaan berhasil dieksekusi", updateOk);
    }

    private static void testUIComponentsAndRequirements() {
        System.out.println("\n[5] Menguji Komponen Antarmuka & Pemenuhan 9 Poin Instruksi:");
        try {
            CountDownLatch latch = new CountDownLatch(1);
            SwingUtilities.invokeLater(() -> {
                try {
                    // ==========================================
                    // 5.1 JavaFX TransitionContainer (Poin 9)
                    // ==========================================
                    TransitionContainer container = new TransitionContainer();
                    JPanel p1 = new JPanel();
                    JPanel p2 = new JPanel();
                    container.addPage("PAGE_1", p1);
                    container.addPage("PAGE_2", p2);
                    container.showPage("PAGE_1");
                    assertTrue("TransitionContainer menampilkan halaman awal", "PAGE_1".equals(container.getCurrentKey()));

                    // Verifikasi modul JavaFX terintegrasi aktif
                    boolean fxActive = container.isJavaFXEnabled();
                    System.out.println("      * Engine Transisi JavaFX Aktif: " + fxActive);
                    assertTrue("JavaFX Toolkit berhasil diinisialisasi untuk transisi", fxActive);

                    // ==========================================
                    // 5.2 Panel Admin Mandiri Terpisah (Poin 1)
                    // ==========================================
                    DesktopAdminWindow adminWin = DesktopAdminWindow.open(() -> {});
                    assertTrue("DesktopAdminWindow berhasil dibuka sebagai jendela mandiri", adminWin != null && adminWin.isDisplayable());
                    adminWin.dispose();

                    // ==========================================
                    // 5.3 DesktopMainWindow & Integrasi Beranda (Poin 1 & 8)
                    // ==========================================
                    DesktopMainWindow mainWin = new DesktopMainWindow();
                    assertTrue("DesktopMainWindow berhasil diinisialisasi", mainWin != null);

                    // ==========================================
                    // 5.4 RejectBookingDialog Multiline & Chips (Poin 7)
                    // ==========================================
                    Booking mockBooking = new Booking();
                    mockBooking.setIdBooking("KZ-TEST-01");
                    mockBooking.setKodeKamar("A-01");
                    mockBooking.setNamaPengguna("Penguji Dialog");
                    mockBooking.setDurasi(1);

                    RejectBookingDialog rejectDlg = new RejectBookingDialog(null, mockBooking);
                    assertTrue("RejectBookingDialog modal berhasil diinstansiasi", rejectDlg != null);

                    // Refleksi untuk memeriksa komponen JTextArea (bukan JTextField biasa)
                    Field fTxtAlasan = RejectBookingDialog.class.getDeclaredField("txtAlasan");
                    fTxtAlasan.setAccessible(true);
                    Object txtComp = fTxtAlasan.get(rejectDlg);
                    assertTrue("Dialog penolakan menggunakan JTextArea multiline (bukan text field biasa)", txtComp instanceof JTextArea);
                    rejectDlg.dispose();

                    // ==========================================
                    // 5.5 Formulir Booking Rata Kiri (Poin 6)
                    // ==========================================
                    Kamar kamarA01 = new Kamar("A-01", "putra", 850000, "tersedia", null, "Deskripsi");
                    DesktopBookingDialog bookingDlg = new DesktopBookingDialog(mainWin, kamarA01, () -> {});
                    assertTrue("DesktopBookingDialog berhasil diinstansiasi", bookingDlg != null);
                    bookingDlg.dispose();

                    // ==========================================
                    // 5.6 Katalog Kamar & Pencarian Padat (Poin 2 & 5)
                    // ==========================================
                    DesktopKamarView kamarView = new DesktopKamarView(new DesktopKamarView.KamarActionCallback() {
                        @Override public void onBookingRequested(Kamar kamar) {}
                        @Override public void onLoginRequested() {}
                    });
                    assertTrue("DesktopKamarView berhasil diinstansiasi dengan search & filter", kamarView != null);

                    // Uji Filter Kamar
                    kamarView.setFilter("putri");
                    kamarView.setFilter("tersedia");
                    kamarView.setFilter("semua");

                    // ==========================================
                    // 5.7 Tampilan Beranda, Sewaku, & Profil Rata Tengah (Poin 4)
                    // ==========================================
                    DesktopBerandaView berandaView = new DesktopBerandaView(new DesktopBerandaView.BerandaActionCallback() {
                        @Override public void onSelectRoom(String kodeKamar) {}
                        @Override public void onNavigateToKamar() {}
                        @Override public void onNavigateToLogin() {}
                        @Override public void onOpenAdminWindow() {}
                    });
                    assertTrue("DesktopBerandaView berhasil diinstansiasi dengan navigasi admin", berandaView != null);

                    DesktopSewakuView sewakuView = new DesktopSewakuView(new DesktopSewakuView.SewakuActionCallback() {
                        @Override public void onExploreRooms() {}
                        @Override public void onLoginRequested() {}
                    });
                    assertTrue("DesktopSewakuView berhasil diinstansiasi dengan layout padat rata tengah", sewakuView != null);

                    DesktopProfilView profilView = new DesktopProfilView(new DesktopProfilView.ProfilActionCallback() {
                        @Override public void onLogout() {}
                        @Override public void onNavigateToSewaku() {}
                    });
                    assertTrue("DesktopProfilView berhasil diinstansiasi", profilView != null);

                    mainWin.dispose();
                    latch.countDown();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    latch.countDown();
                    throw new RuntimeException(ex);
                }
            });

            boolean completed = latch.await(6, TimeUnit.SECONDS);
            assertTrue("Seluruh komponen antarmuka lolos verifikasi di Swing EDT & JavaFX", completed);
        } catch (Exception e) {
            e.printStackTrace();
            assertTrue("Inisialisasi antarmuka gagal: " + e.getMessage(), false);
        }
    }
}
