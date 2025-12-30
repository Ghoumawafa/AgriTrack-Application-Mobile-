package com.example.agritrack.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "animal_foods")
public class AnimalFoodEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "type")
    private String type; // Grain, Fourrage, Complément

    @ColumnInfo(name = "quantity")
    private double quantity; // Quantité en stock

    @NonNull
    @ColumnInfo(name = "unit")
    private String unit; // kg, sac, tonne

    @ColumnInfo(name = "cost")
    private double cost; // Coût unitaire

    @ColumnInfo(name = "alert_quantity")
    private double alertQuantity; // Quantité d'alerte (stock bas)

    public AnimalFoodEntity(@NonNull String name,
                            @NonNull String type,
                            double quantity,
                            @NonNull String unit,
                            double cost,
                            double alertQuantity) {
        this.name = name;
        this.type = type;
        this.quantity = quantity;
        this.unit = unit;
        this.cost = cost;
        this.alertQuantity = alertQuantity;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    @NonNull
    public String getUnit() {
        return unit;
    }

    public void setUnit(@NonNull String unit) {
        this.unit = unit;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public double getAlertQuantity() {
        return alertQuantity;
    }

    public void setAlertQuantity(double alertQuantity) {
        this.alertQuantity = alertQuantity;
    }
}