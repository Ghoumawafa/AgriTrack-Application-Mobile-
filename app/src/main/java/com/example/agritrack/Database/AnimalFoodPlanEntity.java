package com.example.agritrack.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "animal_food_plans")
public class AnimalFoodPlanEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @NonNull
    @ColumnInfo(name = "species")
    private String species; // Vache, Mouton, Chèvre, Poule

    @NonNull
    @ColumnInfo(name = "category")
    private String category; // Laitière, Viande, Pondeuse

    @NonNull
    @ColumnInfo(name = "age_category")
    private String ageCategory; // Jeune, Adulte, Senior

    @ColumnInfo(name = "min_weight")
    private double minWeight;

    @ColumnInfo(name = "max_weight")
    private double maxWeight;

    @ColumnInfo(name = "total_daily_food")
    private double totalDailyFood; // kg total par jour

    @ColumnInfo(name = "hay_percentage")
    private double hayPercentage; // % de foin

    @ColumnInfo(name = "grains_percentage")
    private double grainsPercentage; // % de céréales

    @ColumnInfo(name = "supplements_percentage")
    private double supplementsPercentage; // % de compléments

    @ColumnInfo(name = "water_liters")
    private double waterLiters; // Litres d'eau par jour

    @NonNull
    @ColumnInfo(name = "feeding_times")
    private String feedingTimes; // JSON: ["06:00", "12:00", "18:00"]

    @ColumnInfo(name = "meals_per_day")
    private int mealsPerDay;

    @ColumnInfo(name = "recommendations")
    private String recommendations; // Notes spéciales

    @ColumnInfo(name = "estimated_cost_per_day")
    private double estimatedCostPerDay;

    // Constructeurs
    public AnimalFoodPlanEntity() {}

    @Ignore
    public AnimalFoodPlanEntity(@NonNull String species, @NonNull String category,
                                @NonNull String ageCategory, double minWeight, double maxWeight,
                                double totalDailyFood, double hayPercentage, double grainsPercentage,
                                double supplementsPercentage, double waterLiters,
                                @NonNull String feedingTimes, int mealsPerDay,
                                String recommendations, double estimatedCostPerDay) {
        this.species = species;
        this.category = category;
        this.ageCategory = ageCategory;
        this.minWeight = minWeight;
        this.maxWeight = maxWeight;
        this.totalDailyFood = totalDailyFood;
        this.hayPercentage = hayPercentage;
        this.grainsPercentage = grainsPercentage;
        this.supplementsPercentage = supplementsPercentage;
        this.waterLiters = waterLiters;
        this.feedingTimes = feedingTimes;
        this.mealsPerDay = mealsPerDay;
        this.recommendations = recommendations;
        this.estimatedCostPerDay = estimatedCostPerDay;
    }

    // Getters et Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    @NonNull
    public String getSpecies() { return species; }
    public void setSpecies(@NonNull String species) { this.species = species; }

    @NonNull
    public String getCategory() { return category; }
    public void setCategory(@NonNull String category) { this.category = category; }

    @NonNull
    public String getAgeCategory() { return ageCategory; }
    public void setAgeCategory(@NonNull String ageCategory) { this.ageCategory = ageCategory; }

    public double getMinWeight() { return minWeight; }
    public void setMinWeight(double minWeight) { this.minWeight = minWeight; }

    public double getMaxWeight() { return maxWeight; }
    public void setMaxWeight(double maxWeight) { this.maxWeight = maxWeight; }

    public double getTotalDailyFood() { return totalDailyFood; }
    public void setTotalDailyFood(double totalDailyFood) { this.totalDailyFood = totalDailyFood; }

    public double getHayPercentage() { return hayPercentage; }
    public void setHayPercentage(double hayPercentage) { this.hayPercentage = hayPercentage; }

    public double getGrainsPercentage() { return grainsPercentage; }
    public void setGrainsPercentage(double grainsPercentage) { this.grainsPercentage = grainsPercentage; }

    public double getSupplementsPercentage() { return supplementsPercentage; }
    public void setSupplementsPercentage(double supplementsPercentage) { this.supplementsPercentage = supplementsPercentage; }

    public double getWaterLiters() { return waterLiters; }
    public void setWaterLiters(double waterLiters) { this.waterLiters = waterLiters; }

    @NonNull
    public String getFeedingTimes() { return feedingTimes; }
    public void setFeedingTimes(@NonNull String feedingTimes) { this.feedingTimes = feedingTimes; }

    public int getMealsPerDay() { return mealsPerDay; }
    public void setMealsPerDay(int mealsPerDay) { this.mealsPerDay = mealsPerDay; }

    public String getRecommendations() { return recommendations; }
    public void setRecommendations(String recommendations) { this.recommendations = recommendations; }

    public double getEstimatedCostPerDay() { return estimatedCostPerDay; }
    public void setEstimatedCostPerDay(double estimatedCostPerDay) { this.estimatedCostPerDay = estimatedCostPerDay; }
}