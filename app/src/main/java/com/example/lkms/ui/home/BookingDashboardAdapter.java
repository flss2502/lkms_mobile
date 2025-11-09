package com.example.lkms.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.Booking; // <-- SỬA LỖI: Dùng model "thật"
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// SỬA LỖI: Dùng model "thật"
public class BookingDashboardAdapter extends RecyclerView.Adapter<BookingDashboardAdapter.ViewHolder> {

    private final List<Booking> bookingList;
    private final String currentUid;

    public BookingDashboardAdapter(List<Booking> bookingList) {
        this.bookingList = bookingList;
        this.currentUid = FirebaseAuth.getInstance().getUid(); // Lấy UID một lần
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Dùng layout "list_item_booking.xml" (layout đơn giản của bạn)
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_booking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // SỬA LỖI: Dùng model "thật"
        Booking booking = bookingList.get(position);

        holder.textBookingTitle.setText(booking.getEquipmentName());

        // Hiển thị "Bạn" nếu là người đặt
        String userName = booking.getUserName();
        if (currentUid != null && currentUid.equals(booking.getUserId())) {
            userName = "Bạn";
        } else if (userName.equals("N/A")) {
            userName = "Người dùng khác"; // Ẩn tên nếu JOIN bị null
        }

        String timeRange = formatTime(booking.getStartTime()) + " - " + formatTime(booking.getEndTime());
        holder.textBookingTime.setText(timeRange + " (bởi " + userName + ")");
    }

    @Override
    public int getItemCount() {
        return bookingList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textBookingTitle, textBookingTime;

        ViewHolder(View view) {
            super(view);
            textBookingTitle = view.findViewById(R.id.textBookingTitle);
            textBookingTime = view.findViewById(R.id.textBookingTime);
        }
    }

    // Hàm trợ giúp đổi "2025-11-10T10:00:00" -> "10:00"
    private String formatTime(String isoTime) {
        try {
            SimpleDateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = isoFormat.parse(isoTime);
            return timeFormat.format(date);
        } catch (Exception e) {
            return "N/A";
        }
    }
}