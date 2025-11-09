package com.example.lkms.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lkms.R;
import com.example.lkms.data.models.InventoryAlert;

import java.util.List;

public class InventoryAlertAdapter extends RecyclerView.Adapter<InventoryAlertAdapter.ViewHolder> {

    private final List<InventoryAlert> alertList;

    public InventoryAlertAdapter(List<InventoryAlert> alertList) {
        this.alertList = alertList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_alert, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        InventoryAlert alert = alertList.get(position);
        holder.textAlertMessage.setText(alert.getItemName() + ": " + alert.getReason());
    }

    @Override
    public int getItemCount() {
        return alertList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textAlertMessage;
        ViewHolder(View view) {
            super(view);
            textAlertMessage = view.findViewById(R.id.textAlertMessage);
        }
    }
}