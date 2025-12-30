package com.example.agritrack.Adapters;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Models.PlantTreatment;
import com.example.agritrack.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlantTreatmentAdapter extends RecyclerView.Adapter<PlantTreatmentAdapter.TreatmentViewHolder> {

    private Context context;
    private List<PlantTreatment> treatmentList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEdit(PlantTreatment treatment);
        void onDelete(PlantTreatment treatment);
    }

    public PlantTreatmentAdapter(Context context, List<PlantTreatment> treatmentList, OnItemActionListener listener) {
        this.context = context;
        this.treatmentList = treatmentList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public TreatmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_treatment, parent, false);
        return new TreatmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreatmentViewHolder holder, int position) {
        PlantTreatment treatment = treatmentList.get(position);
        if (treatment == null) return;

        String disease = treatment.getDetectedDisease() != null && !treatment.getDetectedDisease().isEmpty()
            ? treatment.getDetectedDisease() : "Aucune maladie détectée";
        holder.txtDisease.setText(disease);

        String confidence = String.format(Locale.getDefault(), "Confiance: %.0f%%", 
            treatment.getConfidenceScore() * 100);
        holder.txtConfidence.setText(confidence);

        String severity = treatment.getSeverity() != null ? treatment.getSeverity() : "N/A";
        holder.txtSeverity.setText("Sévérité: " + severity);

        String status = treatment.getStatus() != null ? treatment.getStatus() : "-";
        holder.txtStatus.setText(status);

        if (treatment.getTreatmentDate() != null) {
            holder.txtDate.setText(dateFormat.format(treatment.getTreatmentDate()));
        } else {
            holder.txtDate.setText("-");
        }

        // Load image if available
        if (treatment.getImagePath() != null && !treatment.getImagePath().isEmpty()) {
            File imgFile = new File(treatment.getImagePath());
            if (imgFile.exists()) {
                holder.imgTreatment.setImageBitmap(BitmapFactory.decodeFile(imgFile.getAbsolutePath()));
            } else {
                holder.imgTreatment.setImageResource(R.drawable.agritrack_logo);
            }
        } else {
            holder.imgTreatment.setImageResource(R.drawable.agritrack_logo);
        }

        // Wire actions
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(treatment);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(treatment);
        });
    }

    @Override
    public int getItemCount() {
        return treatmentList != null ? treatmentList.size() : 0;
    }

    public void updateData(List<PlantTreatment> newList) {
        this.treatmentList = newList;
        notifyDataSetChanged();
    }

    static class TreatmentViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTreatment;
        TextView txtDisease, txtConfidence, txtSeverity, txtStatus, txtDate;
        ImageButton btnEdit, btnDelete;

        public TreatmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTreatment = itemView.findViewById(R.id.imgTreatment);
            txtDisease = itemView.findViewById(R.id.txtDisease);
            txtConfidence = itemView.findViewById(R.id.txtConfidence);
            txtSeverity = itemView.findViewById(R.id.txtSeverity);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

