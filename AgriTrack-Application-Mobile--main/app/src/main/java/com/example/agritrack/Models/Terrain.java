package com.example.agritrack.Models;

import java.io.Serializable;

public class Terrain implements Serializable {
    private String id;
    private String name;
    private String location; // Coordonnées GPS ou description
    private double area;     // Surface
    private String soilType; // Argileux, Sableux, etc.

    public Terrain() {}

    public Terrain(String id, String name, String location, double area, String soilType) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.area = area;
        this.soilType = soilType;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }
}
