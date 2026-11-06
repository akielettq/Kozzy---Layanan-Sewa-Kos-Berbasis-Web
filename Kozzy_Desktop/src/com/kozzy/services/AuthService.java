package com.kozzy.services;

import com.kozzy.dao.impl.UserDAOImpl;
import com.kozzy.dao.interfaces.IUserDAO;
import com.kozzy.models.Pencari;
import com.kozzy.models.Pengguna;

public class AuthService {
    private final IUserDAO userDAO;

    public AuthService() {
        this.userDAO = new UserDAOImpl();
    }

    public boolean login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }

        Pengguna user = userDAO.findByEmail(email.trim());
        if (user != null && user.getPassword().equals(password.trim())) {
            SessionManager.getInstance().setCurrentUser(user);
            return true;
        }
        return false;
    }

    public String register(String nama, String email, String password, String noTelepon) {
        if (nama == null || nama.trim().length() < 3) {
            return "Nama lengkap minimal 3 karakter.";
        }
        if (email == null || !email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$")) {
            return "Format email tidak valid.";
        }
        if (password == null || password.trim().length() < 5) {
            return "Password minimal 5 karakter.";
        }
        if (noTelepon == null || !noTelepon.matches("^08[0-9]{8,12}$")) {
            return "Nomor WhatsApp harus berawalan 08 dan minimal 10 digit.";
        }
        if (userDAO.emailExists(email.trim())) {
            return "Email tersebut sudah terdaftar. Silakan gunakan email lain.";
        }

        Pencari pencari = new Pencari(0, nama.trim(), email.trim(), password.trim(), noTelepon.trim());
        boolean success = userDAO.register(pencari);
        if (success) {
            SessionManager.getInstance().setCurrentUser(pencari);
            return null; // Null menandakan sukses tanpa pesan error
        } else {
            return "Terjadi kesalahan sistem saat menyimpan akun.";
        }
    }

    public void logout() {
        SessionManager.getInstance().logout();
    }
}
