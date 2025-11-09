package com.example.lkms.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.Experiment;

// 1. NÂNG CẤP LÊN DÙNG ListAdapter VÀ DiffUtil
public class ExperimentAdapter extends ListAdapter<Experiment, ExperimentAdapter.ViewHolder> {

    // (Tùy chọn) Thêm listener để xử lý click
    private OnItemClickListener listener;

    public ExperimentAdapter() {
        super(DIFF_CALLBACK);
    }

    // 2. BỔ SUNG DiffUtil để RecyclerView tự cập nhật hiệu quả
    private static final DiffUtil.ItemCallback<Experiment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Experiment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Experiment oldItem, @NonNull Experiment newItem) {
            // Model của bạn đã có 'id', hãy dùng nó
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Experiment oldItem, @NonNull Experiment newItem) {
            // So sánh nội dung
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getDueDate().equals(newItem.getDueDate());
        }
    };


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 3. SỬA LẠI: Load đúng layout "item_experiment.xml"
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_experiment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Experiment item = getItem(position);
        // 4. SỬA LẠI: Gọi hàm bind để gán dữ liệu
        holder.bind(item);
    }

    // 5. KHÔNG CẦN getItemCount() (ListAdapter đã tự xử lý)
    // 6. KHÔNG CẦN updateData() (Chỉ cần gọi adapter.submitList(newList) từ Fragment)

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textStatus, textDue;
        // MaterialCardView card; // Không cần 'card' nếu không dùng

        ViewHolder(View view) {
            super(view);
            // 7. SỬA LẠI: Ánh xạ đúng ID từ "item_experiment.xml"
            textName = view.findViewById(R.id.textExperimentName);
            textStatus = view.findViewById(R.id.textExperimentStatus);
            textDue = view.findViewById(R.id.textExperimentDueDate);

            // (Tùy chọn) Xử lý click
            view.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        // 8. BỔ SUNG: Hàm bind() để gán dữ liệu
        public void bind(Experiment item) {
            // 9. SỬA LẠI: Gọi đúng hàm item.getName()
            textName.setText(item.getName());
            textStatus.setText(item.getStatus());
            textDue.setText(item.getDueDate());
        }
    }

    // (Tùy chọn) Interface cho OnClick
    public interface OnItemClickListener {
        void onItemClick(Experiment experiment);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
}