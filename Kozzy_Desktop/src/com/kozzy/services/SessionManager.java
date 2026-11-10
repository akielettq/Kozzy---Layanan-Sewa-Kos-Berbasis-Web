package com.kozzy.services;

import com.kozzy.models.Pengguna;

import java.util.ArrayList;
import java.util.List;

/**
 * SessionManager: Pengelola sesi global pengguna yang sedang aktif (Singleton).
 */
public class SessionManager {
    private static SessionManager instance;
    private Pengguna currentUser;

    public interface SessionListener {
        void onSessionChanged(Pengguna user);
    }

    private final List<SessionListener> listeners = new ArrayList<>();

    private SessionManager() {}

    public static synchronized SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    public Pengguna getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Pengguna user) {
        this.currentUser = user;
        notifyListeners();
    }

    public boolean isLoggedIn() {
        return currentUser != null;
    }

    public boolean isAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }

    public void logout() {
        this.currentUser = null;
        notifyListeners();
    }

    public void addSessionListener(SessionListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    private void notifyListeners() {
        for (SessionListener l : listeners) {
            l.onSessionChanged(currentUser);
        }
    }
}
