package com.example.agritrack.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.agritrack.Database.DateConverter;
import java.io.Serializable;
import java.util.Date;

/**
 * Irrigation entity with indexes for optimized queries
 * Indexes on: terrainName, remoteKey, irrigationDate, hardwareEnabled
 */
@Entity(
    tableName = "irrigations",
    indices = {
        @Index(value = "terrainName"),
        @Index(value = "remoteKey", unique = true),
        @Index(value = "irrigationDate"),
        @Index(value = "hardwareEnabled")
    }
)
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

    // NEW fields for IoT integration
    private String remoteKey;        // firebase node key for this zone
    private String mode;             // "auto" or "manual"
    private boolean manualState;     // in manual mode, true=ON
    private double sensorValue;      // latest sensor value sent from ESP32
    private int pumpPin;             // configured pump pin
    private int sensorPin;           // configured sensor pin
    private boolean hardwareEnabled; // whether hardware is enabled for this zone

    // Room uses this constructor
    public Irrigation() {
        // ensure non-null defaults to avoid NPEs in UI
        this.terrainName = "";
        this.method = "N/A";
        this.status = "planifié";
        this.notes = "";
        // defaults for IoT
        this.remoteKey = null;
        this.mode = "auto";
        this.manualState = false;
        this.sensorValue = 0.0;
        this.pumpPin = -1;
        this.sensorPin = -1;
        this.hardwareEnabled = false;
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
        this.remoteKey = null;
        this.mode = "auto";
        this.manualState = false;
        this.sensorValue = 0.0;
        this.pumpPin = -1;
        this.sensorPin = -1;
        this.hardwareEnabled = false;
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

    // IoT getters/setters
    public String getRemoteKey() { return remoteKey; }
    public void setRemoteKey(String remoteKey) { this.remoteKey = remoteKey; }
    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }
    public boolean isManualState() { return manualState; }
    public void setManualState(boolean manualState) { this.manualState = manualState; }
    public double getSensorValue() { return sensorValue; }
    public void setSensorValue(double sensorValue) { this.sensorValue = sensorValue; }
    public int getPumpPin() { return pumpPin; }
    public void setPumpPin(int pumpPin) { this.pumpPin = pumpPin; }
    public int getSensorPin() { return sensorPin; }
    public void setSensorPin(int sensorPin) { this.sensorPin = sensorPin; }
    public boolean isHardwareEnabled() { return hardwareEnabled; }
    public void setHardwareEnabled(boolean hardwareEnabled) { this.hardwareEnabled = hardwareEnabled; }
}