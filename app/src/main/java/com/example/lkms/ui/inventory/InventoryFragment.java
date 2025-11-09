package com.example.lkms.ui.inventory;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.InventoryItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class InventoryFragment extends Fragment {

    private InventoryViewModel inventoryViewModel;
    private InventoryAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmpty;
    private SearchView searchView;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Liên kết với file fragment_inventory.xml
        View root = inflater.inflate(R.layout.fragment_inventory, container, false);

        // Ánh xạ các Views
        recyclerView = root.findViewById(R.id.recycler_view_inventory);
        textEmpty = root.findViewById(R.id.text_empty_inventory);
        searchView = root.findViewById(R.id.search_view_inventory);
        fab = root.findViewById(R.id.fab_add_inventory);

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Lấy ViewModel
        inventoryViewModel = new ViewModelProvider(requireActivity()).get(InventoryViewModel.class);

        // 2. Thiết lập RecyclerView và Adapter
        setupRecyclerView();

        // 3. Quan sát (Observe) dữ liệu từ ViewModel
        inventoryViewModel.getFilteredItems().observe(getViewLifecycleOwner(), items -> {
            // Khi dữ liệu thay đổi (từ DB hoặc do tìm kiếm), cập nhật Adapter
            adapter.submitList(items);

            // Hiển thị/ẩn thông báo "danh sách trống"
            if (items == null || items.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
            }
        });

        // 4. Xử lý logic cho Bước 4 (Search)
        setupSearch();

        // 5. Xử lý logic cho Bước 4 (FAB Add)
        setupFab();
    }

    private void setupRecyclerView() {
        adapter = new InventoryAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // ===== CẬP NHẬT LOGIC "SỬA" (Click) =====
        adapter.setOnItemClickListener(item -> {
            // Mở Dialog ở chế độ SỬA
            // Dùng getParentFragmentManager() để Dialog có thể tìm thấy ViewModel của Fragment này
            AddEditInventoryDialogFragment dialog = AddEditInventoryDialogFragment.newInstance(item);
            dialog.show(getParentFragmentManager(), "EditInventoryDialog");
        });

        // ===== THÊM LOGIC "XÓA" (Long Click) =====
        adapter.setOnItemLongClickListener(item -> {
            // Hiển thị dialog xác nhận xóa
            showDeleteConfirmationDialog(item);
        });
    }

    private void setupSearch() {
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Thường không cần làm gì khi submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Khi người dùng gõ, gọi ViewModel để lọc
                inventoryViewModel.setSearchQuery(newText);
                return true;
            }
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            // ===== CẬP NHẬT LOGIC "THÊM MỚI" =====
            // Mở Dialog ở chế độ THÊM MỚI
            AddEditInventoryDialogFragment dialog = AddEditInventoryDialogFragment.newInstance();
            dialog.show(getParentFragmentManager(), "AddInventoryDialog");
        });
    }

    private void showDeleteConfirmationDialog(InventoryItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận xóa")
                .setMessage("Bạn có chắc chắn muốn xóa vật tư này?\n(" + item.getName() + ")")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    // Gọi ViewModel để thực hiện xóa
                    inventoryViewModel.delete(item);
                })
                .setNegativeButton("Hủy", null)
                .setIcon(R.drawable.ic_dialog_alert)
                .show();
    }
}