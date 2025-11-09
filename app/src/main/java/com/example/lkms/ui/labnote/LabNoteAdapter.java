package com.example.lkms.ui.labnote;

import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.LabNote;

public class LabNoteAdapter extends ListAdapter<LabNote, LabNoteAdapter.LabNoteViewHolder> {

    public LabNoteAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<LabNote> DIFF_CALLBACK = new DiffUtil.ItemCallback<LabNote>() {
        @Override
        public boolean areItemsTheSame(@NonNull LabNote oldItem, @NonNull LabNote newItem) {
            return oldItem.getNote_id() == newItem.getNote_id();
        }
        @Override
        public boolean areContentsTheSame(@NonNull LabNote oldItem, @NonNull LabNote newItem) {
            return oldItem.getHtml_content().equals(newItem.getHtml_content());
        }
    };

    @NonNull
    @Override
    public LabNoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_lab_note, parent, false);
        return new LabNoteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LabNoteViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    static class LabNoteViewHolder extends RecyclerView.ViewHolder {
        private final TextView textTimestamp;
        private final TextView textContent;

        LabNoteViewHolder(@NonNull View itemView) {
            super(itemView);
            textTimestamp = itemView.findViewById(R.id.text_note_timestamp);
            textContent = itemView.findViewById(R.id.text_note_content);
        }

        void bind(LabNote labNote) {
            textTimestamp.setText(labNote.getTimestamp());

            // Chuyển đổi HTML sang Text để TextView có thể hiển thị
            Spanned formattedHtml = Html.fromHtml(labNote.getHtml_content(), Html.FROM_HTML_MODE_LEGACY);
            textContent.setText(formattedHtml);
        }
    }
}
