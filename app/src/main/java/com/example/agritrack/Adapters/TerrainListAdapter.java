package com.example.agritrack.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.TerrainEntity;
import com.example.agritrack.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TerrainListAdapter extends RecyclerView.Adapter<TerrainListAdapter.ViewHolder> {

    public interface OnTerrainClickListener {
        void onTerrainClick(TerrainEntity terrain);

        void onTerrainLongClick(TerrainEntity terrain);
    }

    private final List<TerrainEntity> items = new ArrayList<>();
    private final OnTerrainClickListener listener;

    public TerrainListAdapter(OnTerrainClickListener listener) {
        this.listener = listener;
        setHasStableIds(true);
    }

    public void submitList(List<TerrainEntity> newItems) {
        items.clear();
        if (newItems != null) {
            items.addAll(newItems);
        }
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        if (position < 0 || position >= items.size()) return RecyclerView.NO_ID;
        TerrainEntity t = items.get(position);
        return t != null ? t.getId() : RecyclerView.NO_ID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_terrain_row, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TerrainEntity t = items.get(position);
        if (t == null) return;

        String row = String.format(Locale.getDefault(), "%s — %.2f ha — %s",
                safe(t.getName()),
                t.getArea(),
                safe(t.getLocation()));
        holder.text1.setText(row);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTerrainClick(t);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTerrainLongClick(t);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final MaterialTextView text1;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            text1 = itemView.findViewById(android.R.id.text1);
        }
    }

    private static String safe(String v) {
        return v == null ? "" : v.trim();
    }
}
