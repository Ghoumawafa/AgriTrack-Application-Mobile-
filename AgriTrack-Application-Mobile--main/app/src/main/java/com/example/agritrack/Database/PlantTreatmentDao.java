package com.example.agritrack.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.agritrack.Models.PlantTreatment;

import java.util.List;

/**
 * Data Access Object for PlantTreatment entities
 * Provides both synchronous and LiveData queries for flexibility
 * Includes relationship queries with Plant entity
 */
@Dao
public interface PlantTreatmentDao {

    // Return row id for potential future use
    @Insert
    long insert(PlantTreatment treatment);

    @Update
    void update(PlantTreatment treatment);

    @Delete
    void delete(PlantTreatment treatment);

    // Synchronous queries (use on background threads)
    @Query("SELECT * FROM plant_treatments ORDER BY treatmentDate DESC")
    List<PlantTreatment> getAllTreatments();

    @Query("SELECT * FROM plant_treatments WHERE id = :id LIMIT 1")
    PlantTreatment getById(int id);

    @Query("SELECT * FROM plant_treatments WHERE plantId = :plantId ORDER BY treatmentDate DESC")
    List<PlantTreatment> getByPlantId(int plantId);

    @Query("SELECT * FROM plant_treatments WHERE detectedDisease = :disease ORDER BY treatmentDate DESC")
    List<PlantTreatment> getByDisease(String disease);

    @Query("SELECT * FROM plant_treatments WHERE status = :status ORDER BY treatmentDate DESC")
    List<PlantTreatment> getByStatus(String status);

    @Query("SELECT * FROM plant_treatments WHERE severity = :severity ORDER BY treatmentDate DESC")
    List<PlantTreatment> getBySeverity(String severity);

    // LiveData queries for reactive UI updates
    @Query("SELECT * FROM plant_treatments ORDER BY treatmentDate DESC")
    LiveData<List<PlantTreatment>> getAllTreatmentsLive();

    @Query("SELECT * FROM plant_treatments WHERE plantId = :plantId ORDER BY treatmentDate DESC")
    LiveData<List<PlantTreatment>> getByPlantIdLive(int plantId);

    @Query("SELECT * FROM plant_treatments WHERE status = :status ORDER BY treatmentDate DESC")
    LiveData<List<PlantTreatment>> getByStatusLive(String status);

    @Query("SELECT * FROM plant_treatments WHERE severity = :severity ORDER BY treatmentDate DESC")
    LiveData<List<PlantTreatment>> getBySeverityLive(String severity);

    @Query("SELECT * FROM plant_treatments WHERE confidenceScore >= :minConfidence ORDER BY treatmentDate DESC")
    LiveData<List<PlantTreatment>> getByMinConfidenceLive(float minConfidence);

    // Utility queries
    @Query("SELECT COUNT(*) FROM plant_treatments")
    int getCount();

    @Query("SELECT COUNT(*) FROM plant_treatments WHERE plantId = :plantId")
    int getCountByPlantId(int plantId);

    @Query("SELECT COUNT(*) FROM plant_treatments WHERE detectedDisease = :disease")
    int getCountByDisease(String disease);

    @Query("SELECT COUNT(*) FROM plant_treatments WHERE status = 'Détecté'")
    int getPendingTreatmentsCount();

    @Query("SELECT SUM(cost) FROM plant_treatments WHERE plantId = :plantId")
    double getTotalCostByPlantId(int plantId);

    @Query("SELECT SUM(cost) FROM plant_treatments")
    double getTotalCost();

    @Query("DELETE FROM plant_treatments")
    void deleteAll();

    @Query("DELETE FROM plant_treatments WHERE plantId = :plantId")
    void deleteByPlantId(int plantId);

    // Get distinct diseases for filtering
    @Query("SELECT DISTINCT detectedDisease FROM plant_treatments WHERE detectedDisease != '' ORDER BY detectedDisease")
    List<String> getDistinctDiseases();

    // Get recent treatments (last 30 days)
    @Query("SELECT * FROM plant_treatments WHERE treatmentDate >= datetime('now', '-30 days') ORDER BY treatmentDate DESC")
    LiveData<List<PlantTreatment>> getRecentTreatmentsLive();

    // Get high severity treatments
    @Query("SELECT * FROM plant_treatments WHERE severity = 'Sévère' AND status != 'Traité' ORDER BY treatmentDate DESC")
    LiveData<List<PlantTreatment>> getHighSeverityUntreatedLive();
}

