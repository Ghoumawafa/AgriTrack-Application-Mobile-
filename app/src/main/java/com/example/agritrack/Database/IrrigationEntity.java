package com.example.agritrack.Database;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = "irrigations")
public class IrrigationEntity {
    @PrimaryKey(autoGenerate = true)
    private int id;

    private String terrainName;
    private Date irrigationDate;
    private double waterQuantity;
    private String method;
    private String status;
    private String notes;

    public IrrigationEntity() {
    }

    @Ignore
    public IrrigationEntity(String terrainName, Date irrigationDate, double waterQuantity, String method, String status, String notes) {
        this.terrainName = terrainName;
        this.irrigationDate = irrigationDate;
        this.waterQuantity = waterQuantity;
        this.method = method;
        this.status = status;
        this.notes = notes;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTerrainName() {
        return terrainName;
    }

    public void setTerrainName(String terrainName) {
        this.terrainName = terrainName;
    }

    public Date getIrrigationDate() {
        return irrigationDate;
    }

    public void setIrrigationDate(Date irrigationDate) {
        this.irrigationDate = irrigationDate;
    }

    public double getWaterQuantity() {
        return waterQuantity;
    }

    public void setWaterQuantity(double waterQuantity) {
        this.waterQuantity = waterQuantity;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
