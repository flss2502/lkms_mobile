package com.example.lkms.ui.myexperiment;

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

public class NotebookAdapter extends ListAdapter<Experiment, NotebookAdapter.ExperimentViewHolder> {

    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public NotebookAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Experiment> DIFF_CALLBACK = new DiffUtil.ItemCallback<Experiment>() {
        @Override
        public boolean areItemsTheSame(@NonNull Experiment oldItem, @NonNull Experiment newItem) {
            return oldItem.getId() == newItem.getId();
        }
        @Override
        public boolean areContentsTheSame(@NonNull Experiment oldItem, @NonNull Experiment newItem) {
            return oldItem.getName().equals(newItem.getName()) &&
                    oldItem.getStatus().equals(newItem.getStatus()) &&
                    oldItem.getDueDate().equals(newItem.getDueDate());
        }
    };

    @NonNull
    @Override
    public ExperimentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_notebook, parent, false);
        return new ExperimentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ExperimentViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class ExperimentViewHolder extends RecyclerView.ViewHolder {
        private final TextView textName, textStatus, textDueDate;

        public ExperimentViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.text_notebook_name);
            textStatus = itemView.findViewById(R.id.text_notebook_status);
            textDueDate = itemView.findViewById(R.id.text_notebook_due_date);

            itemView.setOnClickListener(v -> {
                int pos = getAdapterPosition();
                if (clickListener != null && pos != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(getItem(pos));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int pos = getAdapterPosition();
                if (longClickListener != null && pos != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(getItem(pos));
                    return true;
                }
                return false;
            });
        }

        void bind(Experiment experiment) {
            textName.setText(experiment.getName());
            textStatus.setText(experiment.getStatus());
            textDueDate.setText("Due: " + experiment.getDueDate());
        }
    }

    // Interfaces for click
    public interface OnItemClickListener { void onItemClick(Experiment experiment); }
    public interface OnItemLongClickListener { void onItemLongClick(Experiment experiment); }
    public void setOnItemClickListener(OnItemClickListener listener) { this.clickListener = listener; }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) { this.longClickListener = listener; }
}
