package com.kozzy.controllers;

import com.kozzy.models.Pengguna;
import com.kozzy.services.AuthService;
import com.kozzy.services.SessionManager;

/**
 * AuthController: Menangani proses logika otentikasi (login, register, logout)
 * serta mengelola status sesi aktif pengguna.
 * Memisahkan logika proses akun dari class tampilan (View).
 */
public class AuthController {

    private final AuthService authService;
    private final SessionManager sessionManager;

    public AuthController() {
        this.authService = new AuthService();
        this.sessionManager = SessionManager.getInstance();
    }

    public AuthController(AuthService authService, SessionManager sessionManager) {
        this.authService = authService;
        this.sessionManager = sessionManager;
    }

    /**
     * Memproses login menggunakan kredensial email dan password.
     */
    public boolean login(String email, String password) {
        if (email == null || password == null) return false;
        return authService.login(email.trim(), password.trim());
    }

    /**
     * Memproses pendaftaran akun pencari kost baru.
     * Mengembalikan pesan kesalahan jika gagal, atau null jika sukses.
     */
    public String register(String nama, String email, String password, String noTelepon) {
        if (nama == null || nama.trim().isEmpty() ||
            email == null || email.trim().isEmpty() ||
            password == null || password.trim().isEmpty() ||
            noTelepon == null || noTelepon.trim().isEmpty()) {
            return "Semua field pendaftaran wajib diisi.";
        }
        return authService.register(nama.trim(), email.trim(), password.trim(), noTelepon.trim());
    }

    /**
     * Mengakhiri sesi pengguna yang aktif (logout).
     */
    public void logout() {
        authService.logout();
    }

    /**
     * Mendapatkan entitas pengguna yang saat ini sedang login.
     */
    public Pengguna getCurrentUser() {
        return sessionManager.getCurrentUser();
    }

    /**
     * Memeriksa apakah ada pengguna yang aktif login.
     */
    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    /**
     * Mendaftarkan pendengar (listener) perubahan sesi login/logout.
     */
    public void addSessionListener(SessionManager.SessionListener listener) {
        sessionManager.addSessionListener(listener);
    }
}
