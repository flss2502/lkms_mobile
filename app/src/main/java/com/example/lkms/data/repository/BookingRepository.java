package com.example.lkms.data.repository; // (Hoặc package 'data' của bạn)

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.data.models.Booking; // <-- SỬA LỖI: Import model "thật"
import com.example.lkms.data.models.Equipment;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BookingRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;
    private final String currentUid;

    private final MutableLiveData<List<Equipment>> allEquipment = new MutableLiveData<>();
    // SỬA LỖI: Dùng model "thật"
    private final MutableLiveData<List<Booking>> bookingsForDate = new MutableLiveData<>();

    public BookingRepository(Application application) {
        dbHelper = new DatabaseHelper(application.getApplicationContext());
        executor = Executors.newSingleThreadExecutor();

        // Đảm bảo lấy UID một cách an toàn
        currentUid = (FirebaseAuth.getInstance().getCurrentUser() != null) ?
                FirebaseAuth.getInstance().getCurrentUser().getUid() : "";

        loadAllEquipment(); // Tải danh sách thiết bị 1 lần
    }

    // Lấy danh sách Thiết bị (cho Spinner)
    public LiveData<List<Equipment>> getAllEquipment() {
        return allEquipment;
    }

    public void loadAllEquipment() {
        executor.execute(() -> {
            List<Equipment> equipment = dbHelper.getAllEquipment();
            allEquipment.postValue(equipment);
        });
    }

    // Lấy danh sách Lịch đặt (cho RecyclerView)
    // SỬA LỖI: Dùng model "thật"
    public LiveData<List<Booking>> getBookingsForDate() {
        return bookingsForDate;
    }

    // ===== SỬA LỖI LOGIC Ở ĐÂY =====
    /**
     * Lọc lịch đặt theo ngày (do CalendarView gọi)
     */
    public void loadBookingsForDate(Date date) {
        executor.execute(() -> {
            // SỬA: Gọi đúng hàm lấy lịch theo ngày
            List<Booking> bookings = dbHelper.getBookingsByDate(date);
            bookingsForDate.postValue(bookings);
        });
    }
    // =================================

    // Thêm lịch đặt mới
    // SỬA LỖI: Tham số là model "thật"
    public void insertBooking(Booking booking) {
        executor.execute(() -> {
            dbHelper.addBooking(booking); // <-- Lỗi 2 đã được sửa

            // Tải lại danh sách cho ngày hôm đó
            try {
                // SỬA LỖI: Model "Booking" có hàm getStartTime()
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(booking.getStartTime());
                if(date != null) {
                    loadBookingsForDate(date); // Tải lại ngày vừa thêm
                }
            } catch (Exception e) {
                loadBookingsForDate(new Date()); // Tải lại ngày hôm nay nếu có lỗi
            }
        });
    }

    public void delete(Booking booking) {
        executor.execute(() -> {
            // 1. Xóa khỏi CSDL
            dbHelper.deleteBooking(booking.getBook_id());

            // 2. Tải lại danh sách cho ngày hôm đó
            try {
                Date date = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault()).parse(booking.getStartTime());
                if (date != null) {
                    loadBookingsForDate(date); // Tải lại ngày của lịch vừa xóa
                }
            } catch (Exception e) {
                loadBookingsForDate(new Date()); // Tải lại ngày hôm nay nếu có lỗi
            }
        });
    }
}