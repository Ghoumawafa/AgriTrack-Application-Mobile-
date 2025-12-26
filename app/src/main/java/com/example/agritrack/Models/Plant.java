package com.example.agritrack.Models;

import java.io.Serializable;

public class Plant implements Serializable {
    private String id;
    private String name;        // e.g., Blé, Maïs
    private String type;        // Céréale, Fruit, Légume
    private String plantingDate;
    private String harvestDate; // Date prévue ou réelle de récolte
    private double area;        // Surface occupée (en hectares ou m2)
    private double expectedYield; // Rendement prévu

    public Plant() {}

    public Plant(String id, String name, String type, String plantingDate, double area) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.plantingDate = plantingDate;
        this.area = area;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPlantingDate() { return plantingDate; }
    public void setPlantingDate(String plantingDate) { this.plantingDate = plantingDate; }

    public String getHarvestDate() { return harvestDate; }
    public void setHarvestDate(String harvestDate) { this.harvestDate = harvestDate; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getExpectedYield() { return expectedYield; }
    public void setExpectedYield(double expectedYield) { this.expectedYield = expectedYield; }
}
