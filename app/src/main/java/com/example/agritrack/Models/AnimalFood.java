package com.example.agritrack.Models;

import java.io.Serializable;

public class AnimalFood implements Serializable {
    private String id;
    private String name;
    private String type;     // Grain, Fourrage, Complément
    private double quantity; // Quantité en stock
    private String unit;     // kg, sac, tonne
    private double cost;     // Coût unitaire

    public AnimalFood() {}

    public AnimalFood(String id, String name, String type, double quantity, String unit, double cost) {
        this.id = id;
        this.name = name;
        this.type = type;
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

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }
}
