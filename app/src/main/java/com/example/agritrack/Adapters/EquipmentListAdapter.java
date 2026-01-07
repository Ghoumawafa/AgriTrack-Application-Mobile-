package com.example.agritrack.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.EquipmentEntity;
import com.example.agritrack.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EquipmentListAdapter extends RecyclerView.Adapter<EquipmentListAdapter.ViewHolder> {

    public interface OnEquipmentClickListener {
        void onEquipmentClick(EquipmentEntity equipment);
    }

    private final List<EquipmentEntity> items = new ArrayList<>();
    private final OnEquipmentClickListener listener;

    public EquipmentListAdapter(OnEquipmentClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitList(List<EquipmentEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= items.size()) return RecyclerView.NO_ID;
        EquipmentEntity e = items.get(position);
        return e != null ? e.getId() : RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EquipmentEntity e = items.get(position);
        if (e == null) return;

        String name = safe(e.getName());
        String type = safe(e.getType());
        String purchaseDate = safe(e.getPurchaseDate());
        String status = safe(e.getStatus());

        holder.txtName.setText(name);
        holder.txtMeta.setText(String.format(Locale.getDefault(), "%s — %s", type, purchaseDate));
        holder.txtStatus.setText(String.format(Locale.getDefault(), "Statut: %s", status));

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEquipmentClick(e);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialTextView txtName;
        final MaterialTextView txtMeta;
        final MaterialTextView txtStatus;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtName = itemView.findViewById(R.id.txtEquipmentName);
            txtMeta = itemView.findViewById(R.id.txtEquipmentMeta);
            txtStatus = itemView.findViewById(R.id.txtEquipmentStatus);
        }
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
