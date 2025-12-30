package com.example.agritrack.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.agritrack.Models.Plant;

import java.util.List;

/**
 * Data Access Object for Plant entities
 * Provides both synchronous and LiveData queries for flexibility
 */
@Dao
public interface PlantDao {

    // Return row id for potential future use
    @Insert
    long insert(Plant plant);

    @Update
    void update(Plant plant);

    @Delete
    void delete(Plant plant);

    // Synchronous queries (use on background threads)
    @Query("SELECT * FROM plants ORDER BY plantingDate DESC")
    List<Plant> getAllPlants();

    @Query("SELECT * FROM plants WHERE id = :id LIMIT 1")
    Plant getById(int id);

    @Query("SELECT * FROM plants WHERE type = :type ORDER BY plantingDate DESC")
    List<Plant> getByType(String type);

    @Query("SELECT * FROM plants WHERE growthStage = :stage ORDER BY plantingDate DESC")
    List<Plant> getByGrowthStage(String stage);

    @Query("SELECT * FROM plants WHERE name LIKE '%' || :searchQuery || '%' OR type LIKE '%' || :searchQuery || '%' ORDER BY plantingDate DESC")
    List<Plant> searchPlants(String searchQuery);

    // LiveData queries for reactive UI updates
    @Query("SELECT * FROM plants ORDER BY plantingDate DESC")
    LiveData<List<Plant>> getAllPlantsLive();

    @Query("SELECT * FROM plants WHERE type = :type ORDER BY plantingDate DESC")
    LiveData<List<Plant>> getByTypeLive(String type);

    @Query("SELECT * FROM plants WHERE growthStage = :stage ORDER BY plantingDate DESC")
    LiveData<List<Plant>> getByGrowthStageLive(String stage);

    @Query("SELECT * FROM plants WHERE location = :location ORDER BY plantingDate DESC")
    LiveData<List<Plant>> getByLocationLive(String location);

    // Utility queries
    @Query("SELECT COUNT(*) FROM plants")
    int getCount();

    @Query("SELECT COUNT(*) FROM plants WHERE type = :type")
    int getCountByType(String type);

    @Query("SELECT SUM(quantity) FROM plants WHERE type = :type")
    int getTotalQuantityByType(String type);

    @Query("SELECT SUM(area) FROM plants")
    double getTotalArea();

    @Query("DELETE FROM plants")
    void deleteAll();

    // Get distinct types for filtering
    @Query("SELECT DISTINCT type FROM plants ORDER BY type")
    List<String> getDistinctTypes();

    // Get distinct growth stages
    @Query("SELECT DISTINCT growthStage FROM plants ORDER BY growthStage")
    List<String> getDistinctGrowthStages();
}

