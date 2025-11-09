package com.example.lkms.ui.booking;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.Booking;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Calendar;
import java.util.Date;

public class BookingFragment extends Fragment {

    private BookingViewModel viewModel;
    private BookingAdapter adapter;
    private RecyclerView recyclerView;
    private TextView textEmpty;
    private CalendarView calendarView;
    private FloatingActionButton fab;

    private Date selectedDate; // Lưu ngày đã chọn

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Lấy ViewModel (liên kết với Activity)
        viewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);
        selectedDate = new Date(); // Mặc định là hôm nay
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_booking, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_bookings);
        textEmpty = root.findViewById(R.id.text_empty_bookings);
        calendarView = root.findViewById(R.id.calendar_view);
        fab = root.findViewById(R.id.fab_add_booking);

        setupRecyclerView();
        setupCalendarView();
        setupFab();

        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Quan sát (Observe) danh sách lịch đặt
        viewModel.getBookingsForDate().observe(getViewLifecycleOwner(), bookings -> {
            if (bookings == null || bookings.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                textEmpty.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                textEmpty.setVisibility(View.GONE);
                adapter.submitList(bookings);
            }
        });

        // Tải dữ liệu cho ngày hôm nay
        viewModel.loadBookingsForDate(selectedDate);
    }

    private void setupCalendarView() {
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            // Khi người dùng chọn ngày mới
            Calendar cal = Calendar.getInstance();
            cal.set(year, month, dayOfMonth);
            selectedDate = cal.getTime();

            // Tải lại danh sách
            viewModel.loadBookingsForDate(selectedDate);
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            // Mở Dialog Thêm mới với ngày đã chọn
            AddBookingDialogFragment dialog = AddBookingDialogFragment.newInstance(selectedDate);
            dialog.show(getParentFragmentManager(), "AddBookingDialog");
        });
    }

    private void setupRecyclerView() {
        adapter = new BookingAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // (Tùy chọn) Xử lý khi nhấn vào (để Sửa)
        // adapter.setOnItemClickListener(booking -> {
        //    // Mở Dialog Sửa
        // });

        // ===== BỔ SUNG: Xử lý khi nhấn giữ (để Xóa) =====
        adapter.setOnItemLongClickListener(booking -> {
            // Chỉ cho phép xóa nếu là người đặt
            String currentUid = FirebaseAuth.getInstance().getUid();
            if (currentUid != null && currentUid.equals(booking.getUserId())) {
                showDeleteConfirmationDialog(booking);
            } else {
                Toast.makeText(getContext(), "Bạn không thể hủy lịch đặt của người khác", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Booking booking) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xác nhận hủy lịch")
                .setMessage("Bạn có chắc chắn muốn hủy lịch đặt này?\n\n" +
                        booking.getEquipmentName() + "\n" +
                        booking.getStartTime() + " - " + booking.getEndTime())
                .setIcon(R.drawable.ic_dialog_alert) // (Icon bạn đã có)
                .setPositiveButton("Hủy lịch", (dialog, which) -> {
                    // Gọi ViewModel để thực hiện xóa
                    viewModel.delete(booking);
                    Toast.makeText(getContext(), "Đã hủy lịch", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Không", null)
                .show();
    }
}