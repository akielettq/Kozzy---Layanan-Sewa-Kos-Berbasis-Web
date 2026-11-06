package com.kozzy.dao.interfaces;

import com.kozzy.models.Pengguna;

public interface IUserDAO {
    Pengguna findByEmail(String email);
    boolean register(Pengguna user);
    boolean emailExists(String email);
}
