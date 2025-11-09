package com.example.lkms.ui.protocol_sops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.Protocol;

import java.util.Locale;

public class ProtocolAdapter extends ListAdapter<Protocol, ProtocolAdapter.ProtocolViewHolder> {

    private OnItemClickListener clickListener;
    private OnItemLongClickListener longClickListener;

    public ProtocolAdapter() {
        super(DIFF_CALLBACK);
    }

    private static final DiffUtil.ItemCallback<Protocol> DIFF_CALLBACK = new DiffUtil.ItemCallback<Protocol>() {
        @Override
        public boolean areItemsTheSame(@NonNull Protocol oldItem, @NonNull Protocol newItem) {
            return oldItem.getProto_id() == newItem.getProto_id();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Protocol oldItem, @NonNull Protocol newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getVersion() == newItem.getVersion() &&
                    oldItem.getContentData().equals(newItem.getContentData());
        }
    };

    @NonNull
    @Override
    public ProtocolViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_protocol, parent, false);
        return new ProtocolViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProtocolViewHolder holder, int position) {
        Protocol currentItem = getItem(position);
        holder.bind(currentItem);
    }

    class ProtocolViewHolder extends RecyclerView.ViewHolder {
        private final ImageView iconType;
        private final TextView textTitle, textVersion, textMimeType;

        public ProtocolViewHolder(@NonNull View itemView) {
            super(itemView);
            iconType = itemView.findViewById(R.id.icon_protocol_type);
            textTitle = itemView.findViewById(R.id.text_protocol_title);
            textVersion = itemView.findViewById(R.id.text_protocol_version);
            textMimeType = itemView.findViewById(R.id.text_protocol_mimetype);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (clickListener != null && position != RecyclerView.NO_POSITION) {
                    clickListener.onItemClick(getItem(position));
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getAdapterPosition();
                if (longClickListener != null && position != RecyclerView.NO_POSITION) {
                    longClickListener.onItemLongClick(getItem(position));
                    return true;
                }
                return false;
            });
        }

        void bind(Protocol protocol) {
            textTitle.setText(protocol.getTitle());
            textVersion.setText(String.format(Locale.getDefault(), "Phiên bản: %d", protocol.getVersion()));

            if ("HTML".equals(protocol.getContentType())) {
                iconType.setImageResource(R.drawable.code_24px); // (Cần tạo icon này)
                textMimeType.setText("Nội dung HTML");
            } else {
                textMimeType.setText(protocol.getContentMimeType());
                if (protocol.getContentMimeType() != null) {
                    if (protocol.getContentMimeType().contains("pdf")) {
                        iconType.setImageResource(R.drawable.picture_as_pdf_24px); // (Cần tạo icon này)
                    } else if (protocol.getContentMimeType().contains("word")) {
                        iconType.setImageResource(R.drawable.doc); // (Cần tạo icon này)
                    } else {
                        iconType.setImageResource(R.drawable.file_present_24px); // (Icon file chung)
                    }
                }
            }
        }
    }

    // Interfaces for click
    public interface OnItemClickListener {
        void onItemClick(Protocol protocol);
    }
    public interface OnItemLongClickListener {
        void onItemLongClick(Protocol protocol);
    }
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.clickListener = listener;
    }
    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        this.longClickListener = listener;
    }
}