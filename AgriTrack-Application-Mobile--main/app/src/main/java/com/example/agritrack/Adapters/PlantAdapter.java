package com.example.agritrack.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Models.Plant;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.PlantViewHolder> {

    private Context context;
    private List<Plant> plantList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEdit(Plant plant);
        void onDelete(Plant plant);
        void onViewTreatments(Plant plant);
    }

    public PlantAdapter(Context context, List<Plant> plantList, OnItemActionListener listener) {
        this.context = context;
        this.plantList = plantList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PlantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_plant, parent, false);
        return new PlantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlantViewHolder holder, int position) {
        Plant plant = plantList.get(position);
        if (plant == null) return;

        String name = plant.getName() != null ? plant.getName() : "-";
        holder.txtPlantName.setText(name);

        String type = plant.getType() != null ? plant.getType() : "N/A";
        String stage = plant.getGrowthStage() != null ? plant.getGrowthStage() : "N/A";
        String typeStage = String.format(Locale.getDefault(), "%s • %s", type, stage);
        holder.txtPlantType.setText(typeStage);

        String location = plant.getLocation() != null && !plant.getLocation().isEmpty() 
            ? plant.getLocation() : "N/A";
        String details = String.format(Locale.getDefault(), "Quantité: %d • Zone: %s", 
            plant.getQuantity(), location);
        holder.txtPlantDetails.setText(details);

        holder.txtGrowthStage.setText(stage);

        if (plant.getPlantingDate() != null) {
            holder.txtPlantingDate.setText(dateFormat.format(plant.getPlantingDate()));
        } else {
            holder.txtPlantingDate.setText("-");
        }

        // Wire actions
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(plant);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(plant);
        });

        holder.btnViewTreatments.setOnClickListener(v -> {
            if (listener != null) listener.onViewTreatments(plant);
        });

        // Click to view details/treatments
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onViewTreatments(plant);
        });
    }

    @Override
    public int getItemCount() {
        return plantList != null ? plantList.size() : 0;
    }

    public void updateData(List<Plant> newList) {
        this.plantList = newList;
        notifyDataSetChanged();
    }

    static class PlantViewHolder extends RecyclerView.ViewHolder {
        TextView txtPlantName, txtPlantType, txtPlantDetails, txtGrowthStage, txtPlantingDate;
        ImageButton btnEdit, btnDelete, btnViewTreatments;

        public PlantViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlantName = itemView.findViewById(R.id.txtPlantName);
            txtPlantType = itemView.findViewById(R.id.txtPlantType);
            txtPlantDetails = itemView.findViewById(R.id.txtPlantDetails);
            txtGrowthStage = itemView.findViewById(R.id.txtGrowthStage);
            txtPlantingDate = itemView.findViewById(R.id.txtPlantingDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnViewTreatments = itemView.findViewById(R.id.btnViewTreatments);
        }
    }
}

