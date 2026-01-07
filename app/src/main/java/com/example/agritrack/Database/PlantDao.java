package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for Plant entities
 * Provides both synchronous and LiveData queries for flexibility
 */
@Dao
public interface PlantDao {

    // Return row id for potential future use
    @Insert
    long insert(PlantEntity plant);

    @Update
    void update(PlantEntity plant);

    @Delete
    void delete(PlantEntity plant);

    // Synchronous queries (use on background threads)
    @Query("SELECT * FROM plants ORDER BY plantingDate DESC")
    List<PlantEntity> getAllPlants();

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    PlantEntity getById(int id);

    @Query("SELECT * FROM plants WHERE type = :type ORDER BY plantingDate DESC")
    List<PlantEntity> getByType(String type);

    // Utility queries
    @Query("SELECT COUNT(*) FROM plants")
    int getCount();

    @Query("DELETE FROM plants")
    void deleteAll();

    // Get distinct types for filtering
    @Query("SELECT DISTINCT type FROM plants ORDER BY type")
    List<String> getDistinctTypes();
}

