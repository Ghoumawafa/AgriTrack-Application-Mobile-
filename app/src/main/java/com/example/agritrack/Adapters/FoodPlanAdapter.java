package com.example.agritrack.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agritrack.Database.AnimalFoodPlanEntity;
import com.example.agritrack.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FoodPlanAdapter extends RecyclerView.Adapter<FoodPlanAdapter.ViewHolder> {

    private Context context;
    private List<AnimalFoodPlanEntity> plans;

    public FoodPlanAdapter(Context context) {
        this.context = context;
        this.plans = new ArrayList<>();
    }

    public void setPlans(List<AnimalFoodPlanEntity> plans) {
        this.plans = plans;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_food_plan, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        AnimalFoodPlanEntity plan = plans.get(position);

        // En-tête
        String emoji = getEmojiForSpecies(plan.getSpecies());
        holder.tvTitle.setText(emoji + " " + plan.getSpecies() + " - " + plan.getCategory());
        holder.tvSubtitle.setText(plan.getAgeCategory() + " • " + plan.getMinWeight() + "-" + plan.getMaxWeight() + " kg");

        // Informations nutritionnelles
        String nutrition = String.format(Locale.getDefault(),
                "📊 Ration quotidienne: %.1f kg\n\n" +
                        "🌾 Foin: %.0f%%\n" +
                        "🌽 Céréales: %.0f%%\n" +
                        "💊 Compléments: %.0f%%\n" +
                        "💧 Eau: %.1f L",
                plan.getTotalDailyFood(),
                plan.getHayPercentage(),
                plan.getGrainsPercentage(),
                plan.getSupplementsPercentage(),
                plan.getWaterLiters()
        );
        holder.tvNutrition.setText(nutrition);

        // Horaires
        try {
            JSONArray times = new JSONArray(plan.getFeedingTimes());
            StringBuilder timesStr = new StringBuilder("🕐 Horaires: ");
            for (int i = 0; i < times.length(); i++) {
                if (i > 0) timesStr.append(", ");
                timesStr.append(times.getString(i));
            }
            timesStr.append(" (").append(plan.getMealsPerDay()).append(" repas/jour)");
            holder.tvSchedule.setText(timesStr.toString());
        } catch (JSONException e) {
            holder.tvSchedule.setText("🕐 Horaires: " + plan.getMealsPerDay() + " repas/jour");
        }

        // Coût et recommandations
        holder.tvCost.setText(String.format(Locale.getDefault(),
                "💰 Coût estimé: %.2f DT/jour",
                plan.getEstimatedCostPerDay()));

        if (plan.getRecommendations() != null && !plan.getRecommendations().isEmpty()) {
            holder.tvRecommendations.setVisibility(View.VISIBLE);
            holder.tvRecommendations.setText("💡 " + plan.getRecommendations());
        } else {
            holder.tvRecommendations.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return plans.size();
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
        TextView tvTitle, tvSubtitle, tvNutrition, tvSchedule, tvCost, tvRecommendations;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvPlanTitle);
            tvSubtitle = itemView.findViewById(R.id.tvPlanSubtitle);
            tvNutrition = itemView.findViewById(R.id.tvNutrition);
            tvSchedule = itemView.findViewById(R.id.tvSchedule);
            tvCost = itemView.findViewById(R.id.tvCost);
            tvRecommendations = itemView.findViewById(R.id.tvRecommendations);
        }
    }
}