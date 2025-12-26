package com.example.agritrack.DataStorage;


public class Equipment {
    private String name;
    private String type;
    private String purchaseDate;
    private double cost;
    private String status;
    private int usageHours;

    public Equipment() {
        this.name = "Nouvel équipement";
        this.type = "Non spécifié";
        this.purchaseDate = "2024-01-01";
        this.cost = 0.0;
        this.status = "actif";
        this.usageHours = 0;
    }

    public Equipment(String name, String type, String purchaseDate, double cost) {
        this.name = name;
        this.type = type;
        this.purchaseDate = purchaseDate;
        this.cost = cost;
        this.status = "actif";
        this.usageHours = 0;
    }

    // Getters et Setters
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