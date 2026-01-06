package com.example.agritrack.Models;

import java.io.Serializable;

public class PlantTreatment implements Serializable {
    private String id;
    private String name;          // Nom du produit
    private String type;          // Fertilisant, Pesticide, Herbicide
    private String targetPlantId; // ID de la plante traitée
    private String date;
    private double quantity;      // Quantité appliquée
    private String unit;          // L, kg
    private double cost;

    public PlantTreatment() {}

    public PlantTreatment(String id, String name, String type, String targetPlantId, String date, double quantity, String unit, double cost) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.targetPlantId = targetPlantId;
        this.date = date;
        this.quantity = quantity;
        this.unit = unit;
        this.cost = cost;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getTargetPlantId() { return targetPlantId; }
    public void setTargetPlantId(String targetPlantId) { this.targetPlantId = targetPlantId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}
