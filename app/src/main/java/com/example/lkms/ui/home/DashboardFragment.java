package com.example.lkms.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.DatabaseHelper;
import com.example.lkms.databinding.FragmentDashboardBinding;
import com.example.lkms.data.models.Experiment;
import com.example.lkms.data.models.InventoryItem;
import com.example.lkms.data.models.InventoryAlert;

// ===== SỬA LỖI 1: Import Model và Adapter "Thật" =====
import com.example.lkms.data.models.Booking;
// (Không cần BookingAdapter ở đây)
// ===================================================

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseHelper dbHelper;

    // Card 1: Experiments
    private ExperimentAdapter experimentAdapter;
    private List<Experiment> experimentList = new ArrayList<>();

    // Card 2: Inventory Alerts
    private InventoryAlertAdapter inventoryAlertAdapter;
    private List<InventoryAlert> inventoryAlertList = new ArrayList<>();

    // ===== SỬA LỖI 2: Dùng Adapter "Dashboard" mới =====
    private BookingDashboardAdapter bookingAdapter; // (Đây là RecyclerView.Adapter)
    private List<Booking> bookingList = new ArrayList<>(); // (Dùng model thật)
    // ===============================================

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        mAuth = FirebaseAuth.getInstance();
        dbHelper = new DatabaseHelper(requireContext());
        currentUser = mAuth.getCurrentUser();

        binding.fabAdd.setOnClickListener(v -> addDummyExperiment());

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadDashboardData();
    }

    private void loadDashboardData() {
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            if (name == null || name.isEmpty()) name = currentUser.getEmail();
            binding.textTitle.setText("Chào mừng, " + (name != null ? name.split("@")[0] : "bạn") + "!");
        } else {
            binding.textTitle.setText("Chào mừng, Khách!");
        }
        binding.textSubtitle.setText("Tổng quan hoạt động lab của bạn");

        new Thread(() -> {
            loadExperimentData();
            loadInventoryAlerts();
            loadBookings(); // Tải data thật

            if (getActivity() != null) {
                getActivity().runOnUiThread(this::setupRecyclerViews);
            }
        }).start();
    }

    private void loadExperimentData() {
        if (currentUser == null) return;
        experimentList.clear();
        experimentList.addAll(dbHelper.getExperimentsByUser(currentUser.getUid()));

        if (experimentList.isEmpty()) {
            dbHelper.addExperiment(new Experiment("Phân tích Protein", "In Progress", "10 Nov"), currentUser.getUid());
            dbHelper.addExperiment(new Experiment("Nuôi cấy Tế bào", "Paused", "12 Nov"), currentUser.getUid());
            experimentList.addAll(dbHelper.getExperimentsByUser(currentUser.getUid()));
        }
    }

    private void loadInventoryAlerts() {
        inventoryAlertList.clear();
        List<InventoryItem> allItems = dbHelper.getAllInventoryItems();

        for (InventoryItem item : allItems) {
            if (item.getQuantity() <= 10) {
                inventoryAlertList.add(new InventoryAlert(
                        item.getName(),
                        "Chỉ còn " + item.getQuantity() + " " + item.getUnit()
                ));
            }
        }
        if (inventoryAlertList.isEmpty()) {
            inventoryAlertList.add(new InventoryAlert("Không có cảnh báo", "Tồn kho của bạn đã đủ"));
        }
    }

    // ===== SỬA LỖI 3: Tải dữ liệu "Thật" từ CSDL (của mọi người) =====
    private void loadBookings() {
        bookingList.clear();
        // Gọi hàm CSDL thật (hàm này đã JOIN và lấy cả tên thiết bị/người dùng)
        bookingList.addAll(dbHelper.getAllUpcomingBookings());
    }
    // ============================================

    private void setupRecyclerViews() {
        // 1. Cài đặt Thí nghiệm (Card 1)
        binding.recyclerOngoingExperiments.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        experimentAdapter = new ExperimentAdapter();
        binding.recyclerOngoingExperiments.setAdapter(experimentAdapter);
        experimentAdapter.submitList(new ArrayList<>(experimentList));

        RecyclerView.OnFlingListener flingListener = binding.recyclerOngoingExperiments.getOnFlingListener();
        if (flingListener instanceof PagerSnapHelper) {
            binding.recyclerOngoingExperiments.setOnFlingListener(null);
        }
        new PagerSnapHelper().attachToRecyclerView(binding.recyclerOngoingExperiments);

        // 2. Cài đặt Cảnh báo Tồn kho (Card 2)
        binding.recyclerInventoryAlerts.setLayoutManager(new LinearLayoutManager(getContext()));
        inventoryAlertAdapter = new InventoryAlertAdapter(inventoryAlertList);
        binding.recyclerInventoryAlerts.setAdapter(inventoryAlertAdapter);
        binding.recyclerInventoryAlerts.setNestedScrollingEnabled(false);

        // ===== SỬA LỖI 4: Cài đặt Booking Adapter (dùng Adapter đơn giản) =====
        binding.recyclerUpcomingBookings.setLayoutManager(new LinearLayoutManager(getContext()));
        bookingAdapter = new BookingDashboardAdapter(bookingList); // <-- Dùng Adapter mới
        binding.recyclerUpcomingBookings.setAdapter(bookingAdapter);
        binding.recyclerUpcomingBookings.setNestedScrollingEnabled(false);
        // =============================================================
    }

    private void addDummyExperiment() {
        if (currentUser == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập trước!", Toast.LENGTH_SHORT).show();
            return;
        }

        Experiment newExp = new Experiment("Thí nghiệm Mới", "Pending", "Due 20 Nov");
        dbHelper.addExperiment(newExp, currentUser.getUid());

        List<Experiment> updatedList = dbHelper.getExperimentsByUser(currentUser.getUid());
        experimentList.clear();
        experimentList.addAll(updatedList);
        experimentAdapter.submitList(new ArrayList<>(experimentList));

        Toast.makeText(getContext(), "Experiment added!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}