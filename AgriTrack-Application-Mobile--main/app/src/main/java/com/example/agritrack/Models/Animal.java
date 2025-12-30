package com.example.agritrack.Models;

import java.io.Serializable;

public class Animal implements Serializable {
    private String id;
    private String name;
    private String species; // Vache, Mouton, Chèvre, etc.
    private String breed;   // Race
    private String birthDate;
    private  double weight;
    private String gender;
    private String healthStatus; // Sain, Malade, En traitement

    public Animal() {}

    public Animal(String id, String name, String species, String breed, String birthDate, double weight, String gender, String healthStatus) {
        this.id = id;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.weight = weight;
        this.gender = gender;
        this.healthStatus = healthStatus;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecies() { return species; }
    public void setSpecies(String species) { this.species = species; }

    public String getBreed() { return breed; }
    public void setBreed(String breed) { this.breed = breed; }

    public String getBirthDate() { return birthDate; }
    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public double getWeight() { return weight; }
    public void setWeight(double weight) { this.weight = weight; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getHealthStatus() { return healthStatus; }
    public void setHealthStatus(String healthStatus) { this.healthStatus = healthStatus; }
}
