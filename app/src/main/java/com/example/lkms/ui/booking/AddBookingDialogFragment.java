package com.example.lkms.ui.booking;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.lkms.R;
import com.example.lkms.data.models.Booking;
import com.example.lkms.data.models.BookingDashboard;
import com.example.lkms.data.models.Equipment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddBookingDialogFragment extends DialogFragment {

    private static final String ARG_DATE = "selected_date";

    private BookingViewModel viewModel;
    private List<Equipment> equipmentList;
    private Equipment selectedEquipment;
    private Calendar selectedDate;
    private int startHour = -1, startMinute = -1, endHour = -1, endMinute = -1;

    // Views
    private AutoCompleteTextView spinnerEquipment;
    private TextView textSelectedDate;
    private TextInputEditText editStartTime, editEndTime, editNotes;
    private TextInputLayout layoutStartTime, layoutEndTime, layoutEquipment;
    private MaterialButton buttonCancel, buttonSave;
    private ArrayAdapter<Equipment> equipmentAdapter;

    /**
     * @param date Ngày được chọn từ CalendarView
     */
    public static AddBookingDialogFragment newInstance(Date date) {
        AddBookingDialogFragment fragment = new AddBookingDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Lấy ViewModel từ Activity
        viewModel = new ViewModelProvider(requireActivity()).get(BookingViewModel.class);

        selectedDate = Calendar.getInstance();
        if (getArguments() != null) {
            Date date = (Date) getArguments().getSerializable(ARG_DATE);
            if (date != null) {
                selectedDate.setTime(date);
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_add_booking, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);
        setupClickListeners();

        // Hiển thị ngày đã chọn
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        textSelectedDate.setText("Ngày: " + dateFormat.format(selectedDate.getTime()));

        // Tải danh sách thiết bị
        equipmentAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line);
        spinnerEquipment.setAdapter(equipmentAdapter);

        viewModel.getAllEquipment().observe(getViewLifecycleOwner(), equipment -> {
            equipmentList = equipment;
            equipmentAdapter.clear();
            equipmentAdapter.addAll(equipmentList);
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.95);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void bindViews(View view) {
        spinnerEquipment = view.findViewById(R.id.spinner_equipment);
        textSelectedDate = view.findViewById(R.id.text_selected_date);
        editStartTime = view.findViewById(R.id.edit_start_time);
        editEndTime = view.findViewById(R.id.edit_end_time);
        editNotes = view.findViewById(R.id.edit_notes);
        layoutStartTime = view.findViewById(R.id.layout_start_time);
        layoutEndTime = view.findViewById(R.id.layout_end_time);
        layoutEquipment = view.findViewById(R.id.layout_equipment_spinner);
        buttonCancel = view.findViewById(R.id.button_cancel);
        buttonSave = view.findViewById(R.id.button_save);
    }

    private void setupClickListeners() {
        buttonCancel.setOnClickListener(v -> dismiss());
        buttonSave.setOnClickListener(v -> saveBooking());

        // Lấy thiết bị đã chọn
        spinnerEquipment.setOnItemClickListener((parent, view, position, id) -> {
            selectedEquipment = equipmentAdapter.getItem(position);
        });

        // Mở TimePicker khi nhấn
        editStartTime.setOnClickListener(v -> showTimePicker(true));
        editEndTime.setOnClickListener(v -> showTimePicker(false));
    }

    private void showTimePicker(boolean isStartTime) {
        Calendar now = Calendar.getInstance();
        int hour = now.get(Calendar.HOUR_OF_DAY);
        int minute = now.get(Calendar.MINUTE);

        TimePickerDialog timePicker = new TimePickerDialog(getContext(), (view, hourOfDay, minuteOfHour) -> {
            String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minuteOfHour);
            if (isStartTime) {
                startHour = hourOfDay;
                startMinute = minuteOfHour;
                editStartTime.setText(timeStr);
                layoutStartTime.setError(null);
            } else {
                endHour = hourOfDay;
                endMinute = minuteOfHour;
                editEndTime.setText(timeStr);
                layoutEndTime.setError(null);
            }
        }, hour, minute, true); // true = 24h format
        timePicker.show();
    }

    private void saveBooking() {
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) {
            Toast.makeText(getContext(), "Lỗi: Chưa xác thực người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        // --- Validate ---
        if (selectedEquipment == null) {
            layoutEquipment.setError("Vui lòng chọn thiết bị");
            return;
        }
        if (startHour == -1) {
            layoutStartTime.setError("Vui lòng chọn giờ bắt đầu");
            return;
        }
        if (endHour == -1) {
            layoutEndTime.setError("Vui lòng chọn giờ kết thúc");
            return;
        }
        if (endHour < startHour || (endHour == startHour && endMinute <= startMinute)) {
            layoutEndTime.setError("Giờ kết thúc phải sau giờ bắt đầu");
            return;
        }
        // --- Hết Validate ---

        // Tạo chuỗi ISO 8601
        Calendar startCal = (Calendar) selectedDate.clone();
        startCal.set(Calendar.HOUR_OF_DAY, startHour);
        startCal.set(Calendar.MINUTE, startMinute);

        Calendar endCal = (Calendar) selectedDate.clone();
        endCal.set(Calendar.HOUR_OF_DAY, endHour);
        endCal.set(Calendar.MINUTE, endMinute);

        SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
        String startTimeStr = isoFormat.format(startCal.getTime());
        String endTimeStr = isoFormat.format(endCal.getTime());
        String notes = editNotes.getText().toString().trim();

        // ===== SỬA LỖI 2 & 3 Ở ĐÂY =====
        // Dùng model "Booking" (thật), không dùng "BookingDashboard"
        Booking newBooking = new Booking(
                selectedEquipment.getEquip_id(),
                uid,
                startTimeStr,
                endTimeStr,
                notes
        );

        // viewModel.insertBooking() (đã sửa) giờ chấp nhận model "Booking"
        viewModel.insertBooking(newBooking);
        // =============================

        Toast.makeText(getContext(), "Đã đặt lịch cho " + selectedEquipment.getName(), Toast.LENGTH_SHORT).show();
        dismiss();
    }
}