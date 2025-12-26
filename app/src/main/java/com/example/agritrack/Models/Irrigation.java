package com.example.agritrack.Models;

import java.io.Serializable;
import java.util.Date;

public class Irrigation implements Serializable {
    private int id;
    private String terrainName;
    private Date irrigationDate;
    private double waterQuantity; // en litres
    private String method; // goutte-à-goutte, aspersion, etc.
    private String status; // planifié, en cours, terminé
    private String notes;

    public Irrigation() {
    }

    public Irrigation(String terrainName, Date irrigationDate, double waterQuantity, String method) {
        this.terrainName = terrainName;
        this.irrigationDate = irrigationDate;
        this.waterQuantity = waterQuantity;
        this.method = method;
        this.status = "planifié";
    }

    // Getters et Setters
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