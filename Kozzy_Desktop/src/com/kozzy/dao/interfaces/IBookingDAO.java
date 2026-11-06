package com.kozzy.dao.interfaces;

import com.kozzy.models.Booking;
import java.util.List;

public interface IBookingDAO {
    boolean createBooking(Booking booking, String proofFilePath);
    List<Booking> getByPengguna(int idPengguna);
    List<Booking> getAllBookings(String filterStatus);
    boolean approveBooking(String idBooking, int idAdmin);
    boolean approveBooking(String idBooking, String catatan, int idAdmin);
    boolean rejectBooking(String idBooking, String alasan, int idAdmin);
    boolean hasActiveBooking(int idPengguna);
    boolean hasPendingBookingForRoom(String kodeKamar);
    Booking getById(String idBooking);
}
