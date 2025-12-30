package com.example.agritrack.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.agritrack.Models.Irrigation;

import java.util.List;

/**
 * Data Access Object for Irrigation entities
 * Provides both synchronous and LiveData queries for flexibility
 */
@Dao
public interface IrrigationDao {

    // return row id for mapping to Firebase node
    @Insert
    long insert(Irrigation irrigation);

    @Update
    void update(Irrigation irrigation);

    @Delete
    void delete(Irrigation irrigation);

    // Synchronous queries (use on background threads)
    @Query("SELECT * FROM irrigations ORDER BY irrigationDate DESC")
    List<Irrigation> getAllIrrigations();

    @Query("SELECT * FROM irrigations WHERE terrainName = :terrain ORDER BY irrigationDate DESC")
    List<Irrigation> getByTerrain(String terrain);

    @Query("SELECT * FROM irrigations WHERE id = :id LIMIT 1")
    Irrigation getById(int id);

    @Query("SELECT * FROM irrigations WHERE remoteKey = :remoteKey LIMIT 1")
    Irrigation getByRemoteKey(String remoteKey);

    // LiveData queries for reactive UI updates
    @Query("SELECT * FROM irrigations ORDER BY irrigationDate DESC")
    LiveData<List<Irrigation>> getAllIrrigationsLive();

    @Query("SELECT * FROM irrigations WHERE terrainName = :terrain ORDER BY irrigationDate DESC")
    LiveData<List<Irrigation>> getByTerrainLive(String terrain);

    @Query("SELECT * FROM irrigations WHERE hardwareEnabled = 1 ORDER BY irrigationDate DESC")
    LiveData<List<Irrigation>> getHardwareEnabledLive();

    // Utility queries
    @Query("SELECT COUNT(*) FROM irrigations")
    int getCount();

    @Query("DELETE FROM irrigations")
    void deleteAll();
}
