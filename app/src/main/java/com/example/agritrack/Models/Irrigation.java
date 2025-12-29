package com.example.agritrack.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.agritrack.Database.DateConverter;
import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "irrigations")
@TypeConverters(DateConverter.class)
public class Irrigation implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String terrainName;
    private Date irrigationDate;
    private double waterQuantity;
    private String method;
    private String status;
    private String notes;

    // Room uses this constructor
    public Irrigation() {
        // ensure non-null defaults to avoid NPEs in UI
        this.terrainName = "";
        this.method = "N/A";
        this.status = "planifié";
        this.notes = "";
    }

    // We tell Room to ignore this one to avoid the warning
    @Ignore
    public Irrigation(String terrainName, Date irrigationDate, double waterQuantity, String method) {
        this.terrainName = terrainName != null ? terrainName : "";
        this.irrigationDate = irrigationDate != null ? irrigationDate : new Date();
        this.waterQuantity = waterQuantity;
        this.method = (method == null || method.isEmpty()) ? "N/A" : method;
        this.status = "planifié";
        this.notes = "";
    }

    // --- Getters & Setters ---
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTerrainName() { return terrainName; }
    public void setTerrainName(String terrainName) { this.terrainName = terrainName; }
    public Date getIrrigationDate() { return irrigationDate; }
    public void setIrrigationDate(Date irrigationDate) { this.irrigationDate = irrigationDate; }
    public double getWaterQuantity() { return waterQuantity; }
    public void setWaterQuantity(double waterQuantity) { this.waterQuantity = waterQuantity; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}