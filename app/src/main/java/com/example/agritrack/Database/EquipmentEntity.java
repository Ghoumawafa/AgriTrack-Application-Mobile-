package com.example.agritrack.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "equipments")
public class EquipmentEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "type")
    private String type;

    @NonNull
    @ColumnInfo(name = "purchase_date")
    private String purchaseDate;

    @ColumnInfo(name = "cost")
    private double cost;

    @NonNull
    @ColumnInfo(name = "status")
    private String status;

    @ColumnInfo(name = "usage_hours")
    private int usageHours;

    public EquipmentEntity(@NonNull String name,
                           @NonNull String type,
                           @NonNull String purchaseDate,
                           double cost,
                           @NonNull String status,
                           int usageHours) {
        this.name = name;
        this.type = type;
        this.purchaseDate = purchaseDate;
        this.cost = cost;
        this.status = status;
        this.usageHours = usageHours;
    }

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

    @NonNull
    public String getPurchaseDate() {
        return purchaseDate;
    }

    public void setPurchaseDate(@NonNull String purchaseDate) {
        this.purchaseDate = purchaseDate;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public int getUsageHours() {
        return usageHours;
    }

    public void setUsageHours(int usageHours) {
        this.usageHours = usageHours;
    }
}
