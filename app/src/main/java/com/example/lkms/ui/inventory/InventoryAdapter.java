package com.example.lkms.ui.inventory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.InventoryItem;

import java.util.Locale;

// 1. Dùng ListAdapter để tối ưu hiệu suất
public class InventoryAdapter extends ListAdapter<InventoryItem, InventoryAdapter.InventoryViewHolder> {

    private OnItemClickListener listener;
    private OnItemLongClickListener longClickListener;

    // 2. Cung cấp callback cho DiffUtil
    public InventoryAdapter() {
        super(DIFF_CALLBACK);
    }

    // 3. DiffUtil Callback giúp RecyclerView biết item nào thay đổi
    private static final DiffUtil.ItemCallback<InventoryItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<InventoryItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull InventoryItem oldItem, @NonNull InventoryItem newItem) {
            return oldItem.getInv_id() == newItem.getInv_id();
        }

        @Override
        public boolean areContentsTheSame(@NonNull InventoryItem oldItem, @NonNull InventoryItem newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getQuantity() == newItem.getQuantity() &&
                    oldItem.getLocation().equals(newItem.getLocation()) &&
                    oldItem.getCategory().equals(newItem.getCategory());
        }
    };

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 4. Liên kết với layout list_item_inventory.xml
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        InventoryItem currentItem = getItem(position);
        holder.bind(currentItem);
    }

    // 5. Lớp ViewHolder
    class InventoryViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName;
        private final TextView textQuantityUnit;
        private final TextView textCategory;
        private final TextView textLocation;

        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            // 6. Ánh xạ View từ layout list_item_inventory.xml
            textName = itemView.findViewById(R.id.text_inventory_name);
            textQuantityUnit = itemView.findViewById(R.id.text_inventory_quantity_unit);
            textCategory = itemView.findViewById(R.id.text_inventory_category);
            textLocation = itemView.findViewById(R.id.text_inventory_location);

            // Xử lý Click
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });

            // Xử lý Long Click (XÓA)
            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(getItem(position));
                    return true; // Đánh dấu là đã xử lý long click
                }
                return false;
            });
        }



        public void bind(InventoryItem item) {
            textName.setText(item.getName());
            textCategory.setText(item.getCategory());
            textLocation.setText(item.getLocation());

            // Format số lượng + đơn vị (ví dụ: "1.5 L")
            String quantityStr = String.format(Locale.getDefault(), "%.1f %s",
                    item.getQuantity(), item.getUnit());
            textQuantityUnit.setText(quantityStr);
        }
    }

    // 9. Interface để xử lý click (cho Bước 4)
    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public interface OnItemLongClickListener {
        void onItemLongClick(InventoryItem item);
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}