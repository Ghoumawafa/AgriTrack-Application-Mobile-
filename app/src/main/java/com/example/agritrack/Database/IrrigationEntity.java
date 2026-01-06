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

    // New fields for IoT integration
    // Selected via UI pin spinners (layout_add_irrigation.xml)
    private int sensorPin;        // Button pin for simulated sensor
    private int actuatorPin;      // LED pin for irrigation indicator
    private int sensorValue;      // Current simulated sensor value
    private double waterUsed;     // Actual water used in this irrigation event (liters)
    private long durationMinutes; // Duration of irrigation in minutes

    public IrrigationEntity() {
        // Pins are controlled by ESP32 firmware / Firebase hardware config; keep neutral defaults here.
        this.sensorPin = -1;
        this.actuatorPin = -1;
        this.sensorValue = 2000;
        this.waterUsed = 0.0;
        this.durationMinutes = 0;
    }

    @Ignore
    public IrrigationEntity(String terrainName, Date irrigationDate, double waterQuantity, String method, String status, String notes) {
        this();
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

    public int getSensorPin() {
        return sensorPin;
    }

    public void setSensorPin(int sensorPin) {
        this.sensorPin = sensorPin;
    }

    public int getActuatorPin() {
        return actuatorPin;
    }

    public void setActuatorPin(int actuatorPin) {
        this.actuatorPin = actuatorPin;
    }

    public int getSensorValue() {
        return sensorValue;
    }

    public void setSensorValue(int sensorValue) {
        this.sensorValue = sensorValue;
    }

    public double getWaterUsed() {
        return waterUsed;
    }

    public void setWaterUsed(double waterUsed) {
        this.waterUsed = waterUsed;
    }

    public long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
}
