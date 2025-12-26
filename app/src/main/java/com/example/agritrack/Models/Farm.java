package com.example.agritrack.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Farm implements Serializable {
    private String name;
    private String location;
    private double totalArea;
    
    // Lists of Entities
    private List<Animal> animals;
    private List<AnimalFood> animalFoods;
    private List<Plant> plants;
    private List<PlantTreatment> treatments;
    private List<Equipment> equipments;
    private List<Terrain> terrains;
    private List<User> users;
    private List<Finance> finances;
    private List<Irrigation> irrigations;

    public Farm() {
        this.name = "Ma Ferme";
        this.animals = new ArrayList<>();
        this.animalFoods = new ArrayList<>();
        this.plants = new ArrayList<>();
        this.treatments = new ArrayList<>();
        this.equipments = new ArrayList<>();
        this.terrains = new ArrayList<>();
        this.users = new ArrayList<>();
        this.finances = new ArrayList<>();
        this.irrigations = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public double getTotalArea() { return totalArea; }
    public void setTotalArea(double totalArea) { this.totalArea = totalArea; }

    public List<Animal> getAnimals() { return animals; }
    public void setAnimals(List<Animal> animals) { this.animals = animals; }

    public List<AnimalFood> getAnimalFoods() { return animalFoods; }
    public void setAnimalFoods(List<AnimalFood> animalFoods) { this.animalFoods = animalFoods; }

    public List<Plant> getPlants() { return plants; }
    public void setPlants(List<Plant> plants) { this.plants = plants; }

    public List<PlantTreatment> getTreatments() { return treatments; }
    public void setTreatments(List<PlantTreatment> treatments) { this.treatments = treatments; }

    public List<Equipment> getEquipments() { return equipments; }
    public void setEquipments(List<Equipment> equipments) { this.equipments = equipments; }

    public List<Terrain> getTerrains() { return terrains; }
    public void setTerrains(List<Terrain> terrains) { this.terrains = terrains; }

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }

    public List<Finance> getFinances() { return finances; }
    public void setFinances(List<Finance> finances) { this.finances = finances; }

    public List<Irrigation> getIrrigations() { return irrigations; }
    public void setIrrigations(List<Irrigation> irrigations) { this.irrigations = irrigations; }
}
