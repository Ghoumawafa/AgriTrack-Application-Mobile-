package com.example.agritrack.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.PlantDao;
import com.example.agritrack.Database.PlantEntity;
import com.example.agritrack.R;

import java.util.ArrayList;
import java.util.List;

public class PlantAdapter extends RecyclerView.Adapter<PlantAdapter.ViewHolder> {

    public interface OnTreatmentsClickListener {
        void onTreatmentsClick(PlantEntity plant);
    }

    private final Context context;
    private final PlantDao plantDao;
    private final OnTreatmentsClickListener treatmentsClickListener;

    private final List<PlantEntity> items = new ArrayList<>();

    public PlantAdapter(Context context, PlantDao plantDao, OnTreatmentsClickListener treatmentsClickListener) {
        this.context = context;
        this.plantDao = plantDao;
        this.treatmentsClickListener = treatmentsClickListener;
    }

    public void submitList(List<PlantEntity> plants) {
        items.clear();
        if (plants != null) {
            items.addAll(plants);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_plant, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PlantEntity plant = items.get(position);

        holder.txtPlantName.setText(nonEmpty(plant.getName(), "Plante"));

        String typeAndStage = nonEmpty(plant.getType(), "-") + " • " + nonEmpty(plant.getGrowthStage(), "-");
        holder.txtPlantType.setText(typeAndStage);

        String details = "Quantité: " + plant.getQuantity() + " • Zone: " + nonEmpty(plant.getLocation(), "-");
        holder.txtPlantDetails.setText(details);

        holder.txtGrowthStage.setText(nonEmpty(plant.getGrowthStage(), "-"));
        holder.txtPlantingDate.setText(nonEmpty(plant.getPlantingDate(), "-"));

        holder.btnDelete.setOnClickListener(v -> {
            new AlertDialog.Builder(context)
                    .setTitle("Supprimer")
                    .setMessage("Supprimer cette plante ?")
                    .setPositiveButton("Supprimer", (d, which) -> {
                        plantDao.delete(plant);
                        int idx = holder.getAdapterPosition();
                        if (idx != RecyclerView.NO_POSITION && idx < items.size()) {
                            items.remove(idx);
                            notifyItemRemoved(idx);
                        } else {
                            notifyDataSetChanged();
                        }
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
        });

        holder.btnEdit.setOnClickListener(v -> showEditDialog(plant));

        holder.btnViewTreatments.setOnClickListener(v -> {
            if (treatmentsClickListener != null) {
                treatmentsClickListener.onTreatmentsClick(plant);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtPlantName;
        TextView txtPlantType;
        TextView txtPlantDetails;
        TextView txtGrowthStage;
        TextView txtPlantingDate;
        ImageButton btnViewTreatments;
        ImageButton btnEdit;
        ImageButton btnDelete;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtPlantName = itemView.findViewById(R.id.txtPlantName);
            txtPlantType = itemView.findViewById(R.id.txtPlantType);
            txtPlantDetails = itemView.findViewById(R.id.txtPlantDetails);
            txtGrowthStage = itemView.findViewById(R.id.txtGrowthStage);
            txtPlantingDate = itemView.findViewById(R.id.txtPlantingDate);
            btnViewTreatments = itemView.findViewById(R.id.btnViewTreatments);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }

    private void showEditDialog(PlantEntity plant) {
        LinearLayout container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density);
        container.setPadding(padding, padding, padding, padding);

        EditText editName = new EditText(context);
        editName.setHint("Nom de la plante");
        editName.setText(nonEmpty(plant.getName(), ""));

        EditText editType = new EditText(context);
        editType.setHint("Type (ex: Céréale)");
        editType.setText(nonEmpty(plant.getType(), ""));

        EditText editStage = new EditText(context);
        editStage.setHint("Stade (ex: Croissance)");
        editStage.setText(nonEmpty(plant.getGrowthStage(), ""));

        EditText editQuantity = new EditText(context);
        editQuantity.setHint("Quantité");
        editQuantity.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        editQuantity.setText(String.valueOf(plant.getQuantity()));

        EditText editLocation = new EditText(context);
        editLocation.setHint("Zone/Emplacement (ex: A1)");
        editLocation.setText(nonEmpty(plant.getLocation(), ""));

        EditText editDate = new EditText(context);
        editDate.setHint("Date plantation (ex: 2025-12-29)");
        editDate.setText(nonEmpty(plant.getPlantingDate(), ""));

        container.addView(editName);
        container.addView(editType);
        container.addView(editStage);
        container.addView(editQuantity);
        container.addView(editLocation);
        container.addView(editDate);

        new AlertDialog.Builder(context)
                .setTitle("Modifier Plante")
                .setView(container)
                .setPositiveButton("Enregistrer", (d, which) -> {
                    plant.setName(safeTrim(editName.getText().toString()));
                    plant.setType(safeTrim(editType.getText().toString()));
                    plant.setGrowthStage(safeTrim(editStage.getText().toString()));
                    plant.setLocation(safeTrim(editLocation.getText().toString()));
                    plant.setPlantingDate(safeTrim(editDate.getText().toString()));
                    plant.setQuantity(parseInt(editQuantity.getText().toString(), plant.getQuantity()));
                    plantDao.update(plant);
                    notifyDataSetChanged();
                })
                .setNegativeButton("Annuler", null)
                .show();
    }

    private static String nonEmpty(String value, String fallback) {
        if (TextUtils.isEmpty(value)) return fallback;
        return value;
    }

    private static String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }

    private static int parseInt(String value, int fallback) {
        try {
            if (TextUtils.isEmpty(value)) return fallback;
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }
}
