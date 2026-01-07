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
}
