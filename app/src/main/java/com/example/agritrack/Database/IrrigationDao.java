package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Data Access Object for Irrigation entities
 * Provides both synchronous and LiveData queries for flexibility
 */
@Dao
public interface IrrigationDao {

    // return row id for mapping to Firebase node
    @Insert
    long insert(IrrigationEntity irrigation);

    @Update
    void update(IrrigationEntity irrigation);

    @Delete
    void delete(IrrigationEntity irrigation);

    // Synchronous queries (use on background threads)
    @Query("SELECT * FROM irrigations ORDER BY irrigationDate DESC")
    List<IrrigationEntity> getAllIrrigations();

    @Query("SELECT * FROM irrigations WHERE terrainName = :terrain ORDER BY irrigationDate DESC")
    List<IrrigationEntity> getByTerrain(String terrain);

    @Query("SELECT * FROM irrigations WHERE id = :id LIMIT 1")
    IrrigationEntity getById(int id);

    // Utility queries
    @Query("SELECT COUNT(*) FROM irrigations")
    int getCount();

    @Query("DELETE FROM irrigations")
    void deleteAll();

    // New queries for statistics and calendar features

    // Get total water used for a specific date
    @Query("SELECT SUM(waterUsed) FROM irrigations WHERE DATE(irrigationDate/1000, 'unixepoch') = DATE(:dateInMillis/1000, 'unixepoch')")
    Double getTotalWaterUsedOnDate(long dateInMillis);

    // Get total water used today
    @Query("SELECT SUM(waterUsed) FROM irrigations WHERE DATE(irrigationDate/1000, 'unixepoch') = DATE('now')")
    Double getTotalWaterUsedToday();

    // Get all irrigations for a specific date
    @Query("SELECT * FROM irrigations WHERE DATE(irrigationDate/1000, 'unixepoch') = DATE(:dateInMillis/1000, 'unixepoch') ORDER BY irrigationDate DESC")
    List<IrrigationEntity> getIrrigationsByDate(long dateInMillis);

    // Get irrigation count for a specific date (for calendar marking)
    @Query("SELECT COUNT(*) FROM irrigations WHERE DATE(irrigationDate/1000, 'unixepoch') = DATE(:dateInMillis/1000, 'unixepoch')")
    int getIrrigationCountOnDate(long dateInMillis);

    // Get total water used in the last 7 days
    @Query("SELECT SUM(waterUsed) FROM irrigations WHERE irrigationDate >= :startDate")
    Double getTotalWaterUsedSince(long startDate);

    // Get average water used per irrigation
    @Query("SELECT AVG(waterUsed) FROM irrigations WHERE waterUsed > 0")
    Double getAverageWaterUsed();

    // --- Fallback statistics based on waterQuantity (for older rows where waterUsed wasn't saved) ---
    @Query("SELECT SUM(waterQuantity) FROM irrigations WHERE DATE(irrigationDate/1000, 'unixepoch') = DATE('now')")
    Double getTotalWaterQuantityToday();

    @Query("SELECT AVG(waterQuantity) FROM irrigations WHERE waterQuantity > 0")
    Double getAverageWaterQuantity();

    @Query("SELECT SUM(waterQuantity) FROM irrigations WHERE DATE(irrigationDate/1000, 'unixepoch') = DATE(:dateInMillis/1000, 'unixepoch')")
    Double getTotalWaterQuantityOnDate(long dateInMillis);

    // Get dates with irrigation events (for calendar)
    @Query("SELECT DISTINCT DATE(irrigationDate/1000, 'unixepoch') as date FROM irrigations ORDER BY date DESC")
    List<String> getDatesWithIrrigations();
}
