package com.kozzy.dao.interfaces;

import com.kozzy.models.Kamar;
import java.sql.Date;
import java.util.List;

public interface IKamarDAO {
    List<Kamar> getAllKamar(String filterTipe);
    Kamar getByKode(String kodeKamar);
    boolean updateStatus(String kodeKamar, String status, Date terisiHingga);
    boolean updateStatus(String kodeKamar, String status);
    void updateExpiredRooms();
}
