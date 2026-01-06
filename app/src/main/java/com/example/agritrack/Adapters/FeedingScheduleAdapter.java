package com.example.agritrack.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.AnimalDao;
import com.example.agritrack.Database.AnimalEntity;
import com.example.agritrack.Database.AnimalFeedingScheduleDao;
import com.example.agritrack.Database.AnimalFeedingScheduleEntity;
import com.example.agritrack.Database.AnimalFeedingRecordDao;
import com.example.agritrack.Database.AnimalFeedingRecordEntity;
import com.example.agritrack.Database.AgriTrackRoomDatabase;
import com.example.agritrack.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FeedingScheduleAdapter extends RecyclerView.Adapter<FeedingScheduleAdapter.ViewHolder> {

    private Context context;
    private List<AnimalFeedingScheduleEntity> schedules;
    private AnimalFeedingScheduleDao scheduleDao;
    private AnimalDao animalDao;
    private AnimalFeedingRecordDao recordDao;

    public FeedingScheduleAdapter(Context context, AnimalFeedingScheduleDao scheduleDao) {
        this.context = context;
        this.schedules = new ArrayList<>();
        this.scheduleDao = scheduleDao;

        AgriTrackRoomDatabase database = AgriTrackRoomDatabase.getInstance(context);
        this.animalDao = database.animalDao();
        this.recordDao = database.animalFeedingRecordDao();
    }

    public void setSchedules(List<AnimalFeedingScheduleEntity> schedules) {
        this.schedules = schedules;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_feeding_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimalFeedingScheduleEntity schedule = schedules.get(position);

        // Récupérer l'animal
        new Thread(() -> {
            AnimalEntity animal = animalDao.getById(schedule.getAnimalId());

            ((android.app.Activity) context).runOnUiThread(() -> {
                if (animal != null) {
                    holder.tvAnimalName.setText(animal.getName());
                    holder.tvAnimalSpecies.setText(getEmojiForSpecies(animal.getSpecies()) + " " + animal.getSpecies());
                }
            });
        }).start();

        // Horaire
        holder.tvScheduledTime.setText(schedule.getScheduledTime());
        holder.tvMealNumber.setText("Repas " + schedule.getMealNumber() + "/" + schedule.getTotalMeals());

        // Quantités
        String quantities = String.format(Locale.getDefault(),
                "🌾 Foin: %.1f kg\n🌽 Céréales: %.1f kg\n💊 Compléments: %.1f kg\n💧 Eau: %.1f L",
                schedule.getHayQuantity(),
                schedule.getGrainsQuantity(),
                schedule.getSupplementsQuantity(),
                schedule.getWaterQuantity()
        );
        holder.tvQuantities.setText(quantities);

        // État
        if (schedule.isFed()) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
            holder.tvStatus.setText("✅ Donné à " + schedule.getActualTime());
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.btnMarkAsFed.setVisibility(View.GONE);
            holder.btnSkip.setVisibility(View.GONE);
        } else if (schedule.isSkipped()) {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.holo_red_light));
            holder.tvStatus.setText("⚠️ Sauté: " + schedule.getSkipReason());
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.btnMarkAsFed.setVisibility(View.GONE);
            holder.btnSkip.setVisibility(View.GONE);
        } else {
            holder.cardView.setCardBackgroundColor(context.getResources().getColor(android.R.color.white));
            holder.tvStatus.setText("⏳ En attente");
            holder.tvStatus.setTextColor(context.getResources().getColor(android.R.color.holo_orange_dark));
            holder.btnMarkAsFed.setVisibility(View.VISIBLE);
            holder.btnSkip.setVisibility(View.VISIBLE);
        }

        // Boutons
        holder.btnMarkAsFed.setOnClickListener(v -> markAsFed(schedule, position));
        holder.btnSkip.setOnClickListener(v -> showSkipDialog(schedule, position));
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    private void markAsFed(AnimalFeedingScheduleEntity schedule, int position) {
        schedule.setFed(true);
        schedule.setActualTime(new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date()));
        schedule.setFedBy("Utilisateur"); // TODO: Récupérer le vrai nom

        new Thread(() -> {
            scheduleDao.update(schedule);
            AnimalFeedingRecordEntity record = new AnimalFeedingRecordEntity(
                    schedule.getAnimalId(),
                    schedule.getId(),
                    schedule.getFeedingDate(),
                    schedule.getActualTime(),
                    "FED"
            );
            record.setQuantityGiven(schedule.getHayQuantity() + schedule.getGrainsQuantity() + schedule.getSupplementsQuantity());
            record.setFedBy(schedule.getFedBy());
            record.setNotes(schedule.getNotes());
            recordDao.insert(record);

            ((android.app.Activity) context).runOnUiThread(() -> {
                notifyItemChanged(position);
                Toast.makeText(context, "✅ Repas marqué comme donné", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }

    private void showSkipDialog(AnimalFeedingScheduleEntity schedule, int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_skip_feeding, null);

        EditText etReason = dialogView.findViewById(R.id.etSkipReason);

        builder.setView(dialogView)
                .setTitle("Sauter ce repas")
                .setPositiveButton("Confirmer", (dialog, which) -> {
                    String reason = etReason.getText().toString().trim();
                    if (reason.isEmpty()) {
                        reason = "Non spécifié";
                    }

                    schedule.setSkipped(true);
                    schedule.setSkipReason(reason);

                    new Thread(() -> {
                        scheduleDao.update(schedule);
                        String nowTime = new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date());
                        AnimalFeedingRecordEntity record = new AnimalFeedingRecordEntity(
                                schedule.getAnimalId(),
                                schedule.getId(),
                                schedule.getFeedingDate(),
                                nowTime,
                                "SKIPPED"
                        );
                        record.setFedBy(schedule.getFedBy());
                        record.setNotes("Raison: " + schedule.getSkipReason());
                        recordDao.insert(record);

                        ((android.app.Activity) context).runOnUiThread(() -> {
                            notifyItemChanged(position);
                            Toast.makeText(context, "⚠️ Repas sauté", Toast.LENGTH_SHORT).show();
                        });
                    }).start();
                })
                .setNegativeButton("Annuler", null)
                .show();
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
        TextView tvAnimalName, tvAnimalSpecies, tvScheduledTime, tvMealNumber, tvQuantities, tvStatus;
        Button btnMarkAsFed, btnSkip;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = (CardView) itemView;
            tvAnimalName = itemView.findViewById(R.id.tvAnimalName);
            tvAnimalSpecies = itemView.findViewById(R.id.tvAnimalSpecies);
            tvScheduledTime = itemView.findViewById(R.id.tvScheduledTime);
            tvMealNumber = itemView.findViewById(R.id.tvMealNumber);
            tvQuantities = itemView.findViewById(R.id.tvQuantities);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            btnMarkAsFed = itemView.findViewById(R.id.btnMarkAsFed);
            btnSkip = itemView.findViewById(R.id.btnSkip);
        }
    }
}
