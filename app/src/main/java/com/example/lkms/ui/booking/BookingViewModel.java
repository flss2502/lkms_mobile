package com.example.lkms.ui.booking; // Tạo package 'booking' mới

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.lkms.data.models.Booking;
import com.example.lkms.data.repository.BookingRepository;
import com.example.lkms.data.models.BookingDashboard;
import com.example.lkms.data.models.Equipment;

import java.util.Date;
import java.util.List;

public class BookingViewModel extends AndroidViewModel {

    private final BookingRepository repository;
    private final LiveData<List<Equipment>> allEquipment;
    private final LiveData<List<Booking>> bookingsForDate;

    public BookingViewModel(@NonNull Application application) {
        super(application);
        repository = new BookingRepository(application);
        allEquipment = repository.getAllEquipment();
        bookingsForDate = repository.getBookingsForDate();
    }

    // ===== Lấy dữ liệu =====

    public LiveData<List<Equipment>> getAllEquipment() {
        return allEquipment;
    }

    public LiveData<List<Booking>> getBookingsForDate() {
        return bookingsForDate;
    }

    // ===== Hành động =====

    /**
     * Tải lịch đặt cho một ngày cụ thể (do CalendarView gọi)
     */
    public void loadBookingsForDate(Date date) {
        repository.loadBookingsForDate(date);
    }

    /**
     * Thêm một lịch đặt mới (do Dialog gọi)
     */
    public void insertBooking(Booking booking) {
        repository.insertBooking(booking);
    }

    public void delete(Booking booking) {
        repository.delete(booking);
    }
}