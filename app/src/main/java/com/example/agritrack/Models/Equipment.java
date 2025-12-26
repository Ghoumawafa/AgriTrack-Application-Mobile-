package com.example.agritrack.Models;

import java.io.Serializable;

public class Equipment implements Serializable {
    private String id;
    private String name;
    private String type;         // Tracteur, Charrue, etc.
    private String purchaseDate;
    private double cost;
    private String status;       // Actif, En panne, En maintenance
    private int usageHours;

    public Equipment() {}

    public Equipment(String id, String name, String type, String purchaseDate, double cost, String status) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.purchaseDate = purchaseDate;
        this.cost = cost;
        this.status = status;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPurchaseDate() { return purchaseDate; }
    public void setPurchaseDate(String purchaseDate) { this.purchaseDate = purchaseDate; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public int getUsageHours() { return usageHours; }
    public void setUsageHours(int usageHours) { this.usageHours = usageHours; }
}
