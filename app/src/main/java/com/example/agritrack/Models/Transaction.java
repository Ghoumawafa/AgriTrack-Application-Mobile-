package com.example.agritrack.Models;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;
import androidx.room.Ignore;
import java.io.Serializable;

@Entity(tableName = "transactions")
public class Transaction implements Serializable {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @NonNull
    public String userEmail;  // ✅ NOUVEAU : lien avec l'utilisateur

    public double amount;
    public String date;
    public String description;
    public String category;
    public String type;

    public Transaction() {
        // Constructeur vide requis par Room
    }

    // ✅ NOUVEAU CONSTRUCTEUR avec userEmail
    @Ignore
    public Transaction(String userEmail, String description, double amount,
                       String date, String category, String type) {
        this.userEmail = userEmail;
        this.description = description;
        this.amount = amount;
        this.date = date;
        this.category = category;
        this.type = type;
    }

    // ✅ TOUS LES GETTERS/SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    // ✅ NOUVEAU userEmail
    @NonNull public String getUserEmail() { return userEmail; }
    public void setUserEmail(@NonNull String userEmail) { this.userEmail = userEmail; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
