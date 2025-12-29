package com.example.agritrack.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Models.Irrigation;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class IrrigationAdapter extends RecyclerView.Adapter<IrrigationAdapter.IrrigationViewHolder> {

    private Context context;
    private List<Irrigation> irrigationList;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
    private OnItemActionListener listener;

    public interface OnItemActionListener {
        void onEdit(Irrigation irrigation);
        void onDelete(Irrigation irrigation);
    }

    public IrrigationAdapter(Context context, List<Irrigation> irrigationList, OnItemActionListener listener) {
        this.context = context;
        this.irrigationList = irrigationList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public IrrigationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_irrigation, parent, false);
        return new IrrigationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IrrigationViewHolder holder, int position) {
        Irrigation irrigation = irrigationList.get(position);
        if (irrigation == null) return;

        String terrain = irrigation.getTerrainName() != null ? irrigation.getTerrainName() : "-";
        holder.txtTerrainName.setText(terrain);

        String method = irrigation.getMethod() != null ? irrigation.getMethod() : "N/A";
        double qty = irrigation.getWaterQuantity();
        String details = String.format(Locale.getDefault(), "%s • %.1f L", method, qty);
        holder.txtDetails.setText(details);

        String status = irrigation.getStatus() != null ? irrigation.getStatus() : "-";
        holder.txtStatus.setText(status);

        if (irrigation.getIrrigationDate() != null) {
            holder.txtDate.setText(dateFormat.format(irrigation.getIrrigationDate()));
        } else {
            holder.txtDate.setText("-");
        }

        // wire actions
        holder.btnEdit.setOnClickListener(v -> {
            if (listener != null) listener.onEdit(irrigation);
        });

        holder.btnDelete.setOnClickListener(v -> {
            if (listener != null) listener.onDelete(irrigation);
        });
    }

    @Override
    public int getItemCount() {
        return irrigationList == null ? 0 : irrigationList.size();
    }

    public static class IrrigationViewHolder extends RecyclerView.ViewHolder {
        TextView txtTerrainName, txtDetails, txtStatus, txtDate;
        ImageButton btnEdit, btnDelete;

        public IrrigationViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTerrainName = itemView.findViewById(R.id.txtTerrainName);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}