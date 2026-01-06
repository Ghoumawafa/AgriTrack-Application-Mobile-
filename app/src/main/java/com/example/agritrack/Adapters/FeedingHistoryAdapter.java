package com.example.agritrack.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.Database.AnimalFeedingRecordEntity;
import com.example.agritrack.R;

import java.util.ArrayList;
import java.util.List;

public class FeedingHistoryAdapter extends RecyclerView.Adapter<FeedingHistoryAdapter.ViewHolder> {

    private Context context;
    private List<AnimalFeedingRecordEntity> records;
    private AnimalDao animalDao;

    public FeedingHistoryAdapter(Context context) {
        this.context = context;
        this.records = new ArrayList<>();
        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(context);
        this.animalDao = database.animalDao();
    }

    public void setRecords(List<AnimalFeedingRecordEntity> records) {
        this.records = records;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feeding_record, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimalFeedingRecordEntity record = records.get(position);

        new Thread(() -> {
            AnimalEntity animal = animalDao.getById(record.getAnimalId());
            ((android.app.Activity) context).runOnUiThread(() -> {
                if (animal != null) {
                    holder.tvAnimalName.setText(animal.getName());
                    holder.tvSpecies.setText(getEmojiForSpecies(animal.getSpecies()) + " " + animal.getSpecies());
                } else {
                    holder.tvAnimalName.setText("Animal #" + record.getAnimalId());
                    holder.tvSpecies.setText("");
                }
            });
        }).start();

        holder.tvDateTime.setText(record.getRecordDate() + " " + record.getRecordTime());
        holder.tvStatus.setText(record.getStatus().equals("FED") ? "Donné" : "Sauté");
        holder.tvQuantities.setText(String.format("Quantité: %.2f kg", record.getQuantityGiven()));

        if (record.getFedBy() != null && !record.getFedBy().isEmpty()) {
            holder.tvBy.setText("Par: " + record.getFedBy());
            holder.tvBy.setVisibility(View.VISIBLE);
        } else {
            holder.tvBy.setVisibility(View.GONE);
        }

        if (record.getNotes() != null && !record.getNotes().isEmpty()) {
            holder.tvNotes.setText(record.getNotes());
            holder.tvNotes.setVisibility(View.VISIBLE);
        } else {
            holder.tvNotes.setVisibility(View.GONE);
        }

        if ("FED".equals(record.getStatus())) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
        }
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    private String getEmojiForSpecies(String species) {
        switch (species) {
            case "Vache": return "🐄";
            case "Mouton": return "🐑";
            case "Chèvre": return "🐐";
            case "Poule": return "🐓";
            default: return "🐾";
        }
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView tvAnimalName, tvSpecies, tvDateTime, tvStatus, tvQuantities, tvBy, tvNotes;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvSpecies = itemView.findViewById(R.id.tvSpecies);
            tvDateTime = itemView.findViewById(R.id.tvDateTime);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvQuantities = itemView.findViewById(R.id.tvQuantities);
            tvBy = itemView.findViewById(R.id.tvBy);
            tvNotes = itemView.findViewById(R.id.tvNotes);
        }
    }
}
