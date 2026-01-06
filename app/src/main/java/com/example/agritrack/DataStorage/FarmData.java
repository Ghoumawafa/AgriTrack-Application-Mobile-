package com.example.agritrack.DataStorage;

import java.util.ArrayList;
import java.util.List;

public class FarmData {
    private String farmName;
    private String location;
    private double area; // en hectares
    private String soilType;
    private List<Crop> crops;
    private List<Equipment> equipment;

    public FarmData() {
        this.farmName = "Ma Ferme";
        this.location = "Non spécifié";
        this.area = 0.0;
        this.soilType = "Non spécifié";
        this.crops = new ArrayList<>();
        this.equipment = new ArrayList<>();
    }

    // Getters et Setters
    public String getFarmName() { return farmName; }
    public void setFarmName(String farmName) { this.farmName = farmName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getArea() { return area; }
    public void setArea(double area) { this.area = area; }

    public String getSoilType() { return soilType; }
    public void setSoilType(String soilType) { this.soilType = soilType; }

    public List<Crop> getCrops() { return crops; }
    public void setCrops(List<Crop> crops) { this.crops = crops; }

    public List<Equipment> getEquipment() { return equipment; }
    public void setEquipment(List<Equipment> equipment) { this.equipment = equipment; }

    // Méthodes utilitaires
    public void addCrop(Crop crop) {
        this.crops.add(crop);
    }

    public void addEquipment(Equipment equipment) {
        this.equipment.add(equipment);
    }

    public void removeCrop(int position) {
        if (position >= 0 && position < crops.size()) {
            crops.remove(position);
        }
    }

    public void removeEquipment(int position) {
        if (position >= 0 && position < equipment.size()) {
            equipment.remove(position);
        }
    }
}