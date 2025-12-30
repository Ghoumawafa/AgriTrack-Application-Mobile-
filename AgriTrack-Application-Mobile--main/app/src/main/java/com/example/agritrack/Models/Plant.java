package com.example.agritrack.Models;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.agritrack.Database.DateConverter;
import java.io.Serializable;
import java.util.Date;

/**
 * Plant entity with indexes for optimized queries
 * Indexes on: name, type, plantingDate, growthStage
 */
@Entity(
    tableName = "plants",
    indices = {
        @Index(value = "name"),
        @Index(value = "type"),
        @Index(value = "plantingDate"),
        @Index(value = "growthStage")
    }
)
@TypeConverters(DateConverter.class)
public class Plant implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;           // e.g., Blé, Maïs, Tomate
    private String type;           // Céréale, Fruit, Légume
    private Date plantingDate;     // Date de plantation
    private Date harvestDate;      // Date prévue ou réelle de récolte
    private String growthStage;    // "Semis", "Croissance", "Floraison", "Récolte"
    private double area;           // Surface occupée (en hectares ou m2)
    private double expectedYield;  // Rendement prévu
    private String location;       // Emplacement/zone
    private int quantity;          // Nombre de plants
    private String notes;          // Notes additionnelles
    private String imageUrl;       // Chemin de l'image (optionnel)

    // Room uses this constructor
    public Plant() {
        this.name = "";
        this.type = "";
        this.plantingDate = new Date();
        this.harvestDate = null;
        this.growthStage = "Semis";
        this.area = 0.0;
        this.expectedYield = 0.0;
        this.location = "";
        this.quantity = 1;
        this.notes = "";
        this.imageUrl = null;
    }

    // Convenience constructor
    @Ignore
    public Plant(String name, String type, Date plantingDate, String growthStage) {
        this.name = name != null ? name : "";
        this.type = type != null ? type : "";
        this.plantingDate = plantingDate != null ? plantingDate : new Date();
        this.growthStage = growthStage != null ? growthStage : "Semis";
        this.harvestDate = null;
        this.area = 0.0;
        this.expectedYield = 0.0;
        this.location = "";
        this.quantity = 1;
        this.notes = "";
        this.imageUrl = null;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Date getPlantingDate() { return plantingDate; }
    public void setPlantingDate(Date plantingDate) { this.plantingDate = plantingDate; }

    public Date getHarvestDate() { return harvestDate; }
    public void setHarvestDate(Date harvestDate) { this.harvestDate = harvestDate; }

    public String getGrowthStage() { return growthStage; }
    public void setGrowthStage(String growthStage) { this.growthStage = growthStage; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public double getExpectedYield() { return expectedYield; }
    public void setExpectedYield(double expectedYield) { this.expectedYield = expectedYield; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    @Override
    public String toString() {
        return "Plant{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", type='" + type + '\'' +
                ", growthStage='" + growthStage + '\'' +
                '}';
    }
}
