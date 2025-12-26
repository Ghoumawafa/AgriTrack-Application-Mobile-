package com.example.agritrack.DataStorage;


public class Crop {
    private String name;
    private String type;
    private String plantingDate;
    private double area;
    private double expectedYield;

    public Crop(String name, String type, String plantingDate, double area) {
        this.name = name;
        this.type = type;
        this.plantingDate = plantingDate;
        this.area = area;
        this.expectedYield = 0.0;
    }

    // Getters et Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPlantingDate() { return plantingDate; }
    public void setPlantingDate(String plantingDate) { this.plantingDate = plantingDate; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getExpectedYield() { return expectedYield; }
    public void setExpectedYield(double expectedYield) { this.expectedYield = expectedYield; }
}