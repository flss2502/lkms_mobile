package com.example.lkms.data.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.data.models.InventoryItem;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class InventoryRepository {

    private final DatabaseHelper dbHelper;
    private final ExecutorService executor;

    // Dùng MutableLiveData để có thể cập nhật danh sách
    private final MutableLiveData<List<InventoryItem>> allItems = new MutableLiveData<>();

    public InventoryRepository(Application application) {
        dbHelper = new DatabaseHelper(application);
        executor = Executors.newSingleThreadExecutor();
        loadAllItems(); // Tải dữ liệu ngay khi khởi tạo
    }

    // Hàm public trả về LiveData (không thể bị sửa đổi từ bên ngoài)
    public LiveData<List<InventoryItem>> getAllItems() {
        return allItems;
    }

    // Hàm tải dữ liệu trên luồng nền
    public void loadAllItems() {
        executor.execute(() -> {
            List<InventoryItem> items = dbHelper.getAllInventoryItems();
            allItems.postValue(items); // postValue an toàn khi gọi từ luồng nền
        });
    }

    // Bạn có thể thêm các hàm add, update, delete ở đây
    // Ví dụ:
    public void insert(InventoryItem item) {
        executor.execute(() -> {
            dbHelper.addInventoryItem(item);
            loadAllItems(); // Tải lại danh sách sau khi thêm
        });
    }

    public void updateQuantity(int itemId, double newQuantity) {
        executor.execute(() -> {
            dbHelper.updateInventoryItemQuantity(itemId, newQuantity);
            loadAllItems(); // Tải lại danh sách sau khi cập nhật
        });
    }

    public void update(InventoryItem item) {
        executor.execute(() -> {
            dbHelper.updateInventoryItem(item);
            loadAllItems(); // Tải lại danh sách sau khi cập nhật
        });
    }

    public void delete(InventoryItem item) {
        executor.execute(() -> {
            dbHelper.deleteInventoryItem(item.getInv_id());
            loadAllItems();
        });
    }
}