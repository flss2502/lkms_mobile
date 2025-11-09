package com.example.lkms.ui.inventory;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData; // <-- THAY ĐỔI 1: Dùng MediatorLiveData
import androidx.lifecycle.MutableLiveData;
// import androidx.lifecycle.Transformations; // <-- KHÔNG CẦN NỮA

import com.example.lkms.data.repository.InventoryRepository;
import com.example.lkms.data.models.InventoryItem;

import java.util.ArrayList;
import java.util.List;

public class InventoryViewModel extends AndroidViewModel {

    private final InventoryRepository repository;
    private final LiveData<List<InventoryItem>> allItems;
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");

    // ===== THAY ĐỔI 2: Dùng MediatorLiveData =====
    // LiveData này sẽ lắng nghe cả 'allItems' và 'searchQuery'
    private final MediatorLiveData<List<InventoryItem>> filteredItems = new MediatorLiveData<>();

    public InventoryViewModel(@NonNull Application application) {
        super(application);
        repository = new InventoryRepository(application);
        allItems = repository.getAllItems();

        // ===== THAY ĐỔI 3: Thiết lập Mediator =====
        // Thêm 'allItems' làm nguồn
        filteredItems.addSource(allItems, items -> {
            // Khi 'allItems' thay đổi (ví dụ: thêm, xóa), gọi hàm lọc
            filterList(items, searchQuery.getValue());
        });

        // Thêm 'searchQuery' làm nguồn
        filteredItems.addSource(searchQuery, query -> {
            // Khi 'searchQuery' thay đổi (người dùng gõ), gọi hàm lọc
            filterList(allItems.getValue(), query);
        });
    }

    /**
     * Hàm trợ giúp (helper) để lọc danh sách
     */
    private void filterList(List<InventoryItem> items, String query) {
        if (items == null) {
            filteredItems.setValue(new ArrayList<>()); // Trả về danh sách trống nếu data gốc là null
            return;
        }

        if (query == null || query.isEmpty()) {
            filteredItems.setValue(items); // Trả về tất cả nếu không tìm kiếm
        } else {
            List<InventoryItem> filteredList = new ArrayList<>();
            String lowerCaseQuery = query.toLowerCase();
            for (InventoryItem item : items) {
                if (item.getName().toLowerCase().contains(lowerCaseQuery) ||
                        (item.getCategory() != null && item.getCategory().toLowerCase().contains(lowerCaseQuery))) {
                    filteredList.add(item);
                }
            }
            filteredItems.setValue(filteredList); // Gửi đi danh sách đã lọc
        }
    }


    // Fragment sẽ gọi hàm này để lấy danh sách hiển thị
    public LiveData<List<InventoryItem>> getFilteredItems() {
        return filteredItems;
    }

    // Fragment gọi hàm này khi người dùng gõ vào thanh tìm kiếm
    public void setSearchQuery(String query) {
        searchQuery.setValue(query);
    }

    // Các hàm này giữ nguyên
    public void insert(InventoryItem item) {
        repository.insert(item);
    }

    public void update(InventoryItem item) {
        repository.update(item);
    }

    public void delete(InventoryItem item) {
        repository.delete(item);
    }

    public void updateQuantity(int id, double quantity) {
        repository.updateQuantity(id, quantity);
    }
}