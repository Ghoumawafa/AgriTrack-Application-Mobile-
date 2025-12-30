package com.example.agritrack.Models;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;
import com.example.agritrack.Database.DateConverter;
import java.io.Serializable;
import java.util.Date;

/**
 * PlantTreatment entity with foreign key to Plant
 * Stores disease detection results from AI analysis and treatment information
 * Indexes on: plantId, treatmentDate, detectedDisease
 */
@Entity(
    tableName = "plant_treatments",
    foreignKeys = @ForeignKey(
        entity = Plant.class,
        parentColumns = "id",
        childColumns = "plantId",
        onDelete = ForeignKey.CASCADE
    ),
    indices = {
        @Index(value = "plantId"),
        @Index(value = "treatmentDate"),
        @Index(value = "detectedDisease")
    }
)
@TypeConverters(DateConverter.class)
public class PlantTreatment implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int plantId;              // Foreign key to Plant
    private Date treatmentDate;       // Date of detection/treatment

    // AI Disease Detection fields
    private String detectedDisease;   // Disease name detected by AI
    private float confidenceScore;    // AI confidence (0.0 to 1.0)
    private String imagePath;         // Path to captured image
    private String severity;          // "Faible", "Modéré", "Sévère"
    private String recommendedAction; // AI-generated recommendation

    // Treatment fields
    private String treatmentName;     // Nom du produit (Fertilisant, Pesticide, etc.)
    private String treatmentType;     // "Fertilisant", "Pesticide", "Herbicide", "Fongicide"
    private double quantity;          // Quantité appliquée
    private String unit;              // "L", "kg", "ml"
    private double cost;              // Coût du traitement
    private String status;            // "Détecté", "En traitement", "Traité"
    private String treatmentNotes;    // Notes additionnelles

    // Room uses this constructor
    public PlantTreatment() {
        this.plantId = 0;
        this.treatmentDate = new Date();
        this.detectedDisease = "";
        this.confidenceScore = 0.0f;
        this.imagePath = "";
        this.severity = "Modéré";
        this.recommendedAction = "";
        this.treatmentName = "";
        this.treatmentType = "";
        this.quantity = 0.0;
        this.unit = "L";
        this.cost = 0.0;
        this.status = "Détecté";
        this.treatmentNotes = "";
    }

    // Convenience constructor for AI detection
    @Ignore
    public PlantTreatment(int plantId, String detectedDisease, float confidenceScore, String imagePath) {
        this.plantId = plantId;
        this.treatmentDate = new Date();
        this.detectedDisease = detectedDisease != null ? detectedDisease : "";
        this.confidenceScore = confidenceScore;
        this.imagePath = imagePath != null ? imagePath : "";
        this.severity = determineSeverity(confidenceScore);
        this.recommendedAction = "";
        this.treatmentName = "";
        this.treatmentType = "";
        this.quantity = 0.0;
        this.unit = "L";
        this.cost = 0.0;
        this.status = "Détecté";
        this.treatmentNotes = "";
    }

    // Helper method to determine severity based on confidence
    private String determineSeverity(float confidence) {
        if (confidence >= 0.8f) return "Sévère";
        if (confidence >= 0.5f) return "Modéré";
        return "Faible";
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) { this.plantId = plantId; }

    public Date getTreatmentDate() { return treatmentDate; }
    public void setTreatmentDate(Date treatmentDate) { this.treatmentDate = treatmentDate; }

    public String getDetectedDisease() { return detectedDisease; }
    public void setDetectedDisease(String detectedDisease) { this.detectedDisease = detectedDisease; }

    public float getConfidenceScore() { return confidenceScore; }
    public void setConfidenceScore(float confidenceScore) { this.confidenceScore = confidenceScore; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public String getSeverity() { return severity; }
    public void setSeverity(String severity) { this.severity = severity; }

    public String getRecommendedAction() { return recommendedAction; }
    public void setRecommendedAction(String recommendedAction) { this.recommendedAction = recommendedAction; }

    public String getTreatmentName() { return treatmentName; }
    public void setTreatmentName(String treatmentName) { this.treatmentName = treatmentName; }

    public String getTreatmentType() { return treatmentType; }
    public void setTreatmentType(String treatmentType) { this.treatmentType = treatmentType; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getTreatmentNotes() { return treatmentNotes; }
    public void setTreatmentNotes(String treatmentNotes) { this.treatmentNotes = treatmentNotes; }

    @Override
    public String toString() {
        return "PlantTreatment{" +
                "id=" + id +
                ", plantId=" + plantId +
                ", detectedDisease='" + detectedDisease + '\'' +
                ", confidenceScore=" + confidenceScore +
                ", status='" + status + '\'' +
                '}';
    }
}
