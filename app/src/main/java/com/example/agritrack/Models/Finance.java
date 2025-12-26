package com.example.agritrack.Models;

import java.io.Serializable;

public class Finance implements Serializable {
    private String id;
    private String type;        // REVENU, DEPENSE
    private double amount;
    private String category;    // Vente, Achat Semences, Salaires, etc.
    private String date;
    private String description;

    public Finance() {}

    public Finance(String id, String type, double amount, String category, String date, String description) {
        this.id = id;
        this.type = type;
        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
