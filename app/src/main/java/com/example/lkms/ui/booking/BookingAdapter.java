package com.example.lkms.ui.booking;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.Booking; // <-- SỬA LỖI: Import model "thật"
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// SỬA LỖI: Dùng model "thật" (Booking)
public class BookingAdapter extends ListAdapter<Booking, BookingAdapter.BookingViewHolder> {

    // (Sửa lại: Dùng interface tùy chỉnh, không dùng AdapterView)
    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public BookingAdapter() {
        super(DIFF_CALLBACK);
    }

    // SỬA LỖI: Dùng model "thật" (Booking)
    private static final DiffUtil.ItemCallback<Booking> DIFF_CALLBACK = new DiffUtil.ItemCallback<Booking>() {
        @Override
        public boolean areItemsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getBook_id() == newItem.getBook_id();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Booking oldItem, @NonNull Booking newItem) {
            return oldItem.getStartTime().equals(newItem.getStartTime()) &&
                    oldItem.getEquipmentId() == newItem.getEquipmentId();
        }
    };

    @NonNull
    @Override
    public BookingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_booking_full, parent, false);
        return new BookingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BookingViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    // Sửa 'static class' thành 'class' để truy cập listener
    class BookingViewHolder extends RecyclerView.ViewHolder {
        private final TextView textStartTime, textEndTime, textEquipName, textUserName, textNotes;
        private final String currentUid;

        BookingViewHolder(@NonNull View itemView) {
            super(itemView);
            textStartTime = itemView.findViewById(R.id.text_start_time);
            textEndTime = itemView.findViewById(R.id.text_end_time);
            textEquipName = itemView.findViewById(R.id.text_equip_name);
            textUserName = itemView.findViewById(R.id.text_user_name);
            textNotes = itemView.findViewById(R.id.text_notes);
            currentUid = FirebaseAuth.getInstance().getUid();

            // (Thêm click listener)
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (clickListener != null && position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(getItem(position));
                }
            });

            // (Code long click của bạn đã đúng)
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }

        // SỬA LỖI: Dùng model "thật" (Booking)
        void bind(Booking booking) {
            textEquipName.setText(booking.getEquipmentName());

            textStartTime.setText(formatTime(booking.getStartTime()));
            textEndTime.setText(formatTime(booking.getEndTime()));

            String userName = booking.getUserName();
            if (currentUid != null && currentUid.equals(booking.getUserId())) {
                textUserName.setText("Đặt bởi: Bạn");
            } else {
                textUserName.setText("Đặt bởi: " + (userName.equals("N/A") ? booking.getUserId().substring(0, 6) + "..." : userName));
            }

            if (!TextUtils.isEmpty(booking.getNotes())) {
                textNotes.setVisibility(View.VISIBLE);
                textNotes.setText("Ghi chú: " + booking.getNotes());
            } else {
                textNotes.setVisibility(View.GONE);
            }
        }

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

    // Các interface tùy chỉnh (Đã đúng)
    public interface OnItemClickListener {
        void onItemClick(Booking booking);
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(Booking booking);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}