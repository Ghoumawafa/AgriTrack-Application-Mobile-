package com.example.agritrack.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(
        tableName = "animal_feeding_schedules",
        foreignKeys = {
                @ForeignKey(
                        entity = AnimalEntity.class,
                        parentColumns = "id",
                        childColumns = "animal_id",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = AnimalFoodPlanEntity.class,
                        parentColumns = "id",
                        childColumns = "plan_id",
                        onDelete = ForeignKey.SET_NULL
                )
        },
        indices = {
                @Index("animal_id"),
                @Index("plan_id"),
                @Index(value = {"feeding_date", "scheduled_time"})
        }
)
public class AnimalFeedingScheduleEntity {

    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "animal_id")
    private long animalId;

    @ColumnInfo(name = "plan_id")
    private Long planId; // Nullable

    @ColumnInfo(name = "meal_number")
    private int mealNumber; // 1, 2, 3 (pour le jour)

    @ColumnInfo(name = "total_meals")
    private int totalMeals; // Total de repas dans la journée

    @NonNull
    @ColumnInfo(name = "feeding_date")
    private String feedingDate; // Format: yyyy-MM-dd

    @NonNull
    @ColumnInfo(name = "scheduled_time")
    private String scheduledTime; // Format: HH:mm

    @ColumnInfo(name = "hay_quantity")
    private double hayQuantity; // kg

    @ColumnInfo(name = "grains_quantity")
    private double grainsQuantity; // kg

    @ColumnInfo(name = "supplements_quantity")
    private double supplementsQuantity; // kg

    @ColumnInfo(name = "water_quantity")
    private double waterQuantity; // litres

    @ColumnInfo(name = "is_fed")
    private boolean isFed; // Marqué comme donné

    @ColumnInfo(name = "actual_time")
    private String actualTime; // Heure réelle de distribution

    @ColumnInfo(name = "fed_by")
    private String fedBy; // Nom de la personne

    @ColumnInfo(name = "notes")
    private String notes;

    @ColumnInfo(name = "is_skipped")
    private boolean isSkipped; // Repas sauté

    @ColumnInfo(name = "skip_reason")
    private String skipReason;

    // Constructeur
    public AnimalFeedingScheduleEntity(long animalId, Long planId, int mealNumber, int totalMeals,
                                       @NonNull String feedingDate, @NonNull String scheduledTime,
                                       double hayQuantity, double grainsQuantity,
                                       double supplementsQuantity, double waterQuantity) {
        this.animalId = animalId;
        this.planId = planId;
        this.mealNumber = mealNumber;
        this.totalMeals = totalMeals;
        this.feedingDate = feedingDate;
        this.scheduledTime = scheduledTime;
        this.hayQuantity = hayQuantity;
        this.grainsQuantity = grainsQuantity;
        this.supplementsQuantity = supplementsQuantity;
        this.waterQuantity = waterQuantity;
        this.isFed = false;
        this.isSkipped = false;
    }

    // Getters et Setters complets
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getAnimalId() { return animalId; }
    public void setAnimalId(long animalId) { this.animalId = animalId; }

    public Long getPlanId() { return planId; }
    public void setPlanId(Long planId) { this.planId = planId; }

    public int getMealNumber() { return mealNumber; }
    public void setMealNumber(int mealNumber) { this.mealNumber = mealNumber; }

    public int getTotalMeals() { return totalMeals; }
    public void setTotalMeals(int totalMeals) { this.totalMeals = totalMeals; }

    @NonNull
    public String getFeedingDate() { return feedingDate; }
    public void setFeedingDate(@NonNull String feedingDate) { this.feedingDate = feedingDate; }

    @NonNull
    public String getScheduledTime() { return scheduledTime; }
    public void setScheduledTime(@NonNull String scheduledTime) { this.scheduledTime = scheduledTime; }

    public double getHayQuantity() { return hayQuantity; }
    public void setHayQuantity(double hayQuantity) { this.hayQuantity = hayQuantity; }

    public double getGrainsQuantity() { return grainsQuantity; }
    public void setGrainsQuantity(double grainsQuantity) { this.grainsQuantity = grainsQuantity; }

    public double getSupplementsQuantity() { return supplementsQuantity; }
    public void setSupplementsQuantity(double supplementsQuantity) { this.supplementsQuantity = supplementsQuantity; }

    public double getWaterQuantity() { return waterQuantity; }
    public void setWaterQuantity(double waterQuantity) { this.waterQuantity = waterQuantity; }

    public boolean isFed() { return isFed; }
    public void setFed(boolean fed) { isFed = fed; }

    public String getActualTime() { return actualTime; }
    public void setActualTime(String actualTime) { this.actualTime = actualTime; }

    public String getFedBy() { return fedBy; }
    public void setFedBy(String fedBy) { this.fedBy = fedBy; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public boolean isSkipped() { return isSkipped; }
    public void setSkipped(boolean skipped) { isSkipped = skipped; }

    public String getSkipReason() { return skipReason; }
    public void setSkipReason(String skipReason) { this.skipReason = skipReason; }

}