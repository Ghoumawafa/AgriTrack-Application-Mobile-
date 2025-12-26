package com.example.agritrack.Adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Models.DashboardModule;
import com.example.agritrack.R;


import java.util.List;

public class DashboardAdapter extends RecyclerView.Adapter<DashboardAdapter.ViewHolder> {

    private List<DashboardModule> modules;

    public DashboardAdapter(List<DashboardModule> modules) {
        this.modules = modules;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dashboard_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        DashboardModule module = modules.get(position);

        holder.moduleIcon.setText(module.getIcon());
        holder.moduleTitle.setText(module.getTitle());
        holder.moduleDescription.setText(module.getDescription());

        try {
            holder.cardView.setCardBackgroundColor(
                    android.graphics.Color.parseColor(module.getColor()));
        } catch (Exception e) {
            holder.cardView.setCardBackgroundColor(0xFF4CAF50);
        }

        holder.cardView.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), module.getActivityClass());
            v.getContext().startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return modules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView moduleIcon, moduleTitle, moduleDescription;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            moduleIcon = itemView.findViewById(R.id.module_icon);
            moduleTitle = itemView.findViewById(R.id.module_title);
            moduleDescription = itemView.findViewById(R.id.module_description);
        }
    }
}