package com.example.agritrack.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "animals")
public class AnimalEntity {

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    private long id;

    @NonNull
    @ColumnInfo(name = "name")
    private String name;

    @NonNull
    @ColumnInfo(name = "species")
    private String species; // Vache, Mouton, Chèvre, etc.

    @NonNull
    @ColumnInfo(name = "breed")
    private String breed;   // Race

    @NonNull
    @ColumnInfo(name = "birth_date")
    private String birthDate;

    @ColumnInfo(name = "weight")
    private double weight;

    @NonNull
    @ColumnInfo(name = "gender")
    private String gender;

    @NonNull
    @ColumnInfo(name = "health_status")
    private String healthStatus; // Sain, Malade, Convalescent

    public AnimalEntity(@NonNull String name,
                        @NonNull String species,
                        @NonNull String breed,
                        @NonNull String birthDate,
                        double weight,
                        @NonNull String gender,
                        @NonNull String healthStatus) {
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.birthDate = birthDate;
        this.weight = weight;
        this.gender = gender;
        this.healthStatus = healthStatus;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getSpecies() {
        return species;
    }

    public void setSpecies(@NonNull String species) {
        this.species = species;
    }

    @NonNull
    public String getBreed() {
        return breed;
    }

    public void setBreed(@NonNull String breed) {
        this.breed = breed;
    }

    @NonNull
    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(@NonNull String birthDate) {
        this.birthDate = birthDate;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @NonNull
    public String getGender() {
        return gender;
    }

    public void setGender(@NonNull String gender) {
        this.gender = gender;
    }

    @NonNull
    public String getHealthStatus() {
        return healthStatus;
    }

    public void setHealthStatus(@NonNull String healthStatus) {
        this.healthStatus = healthStatus;
    }
}