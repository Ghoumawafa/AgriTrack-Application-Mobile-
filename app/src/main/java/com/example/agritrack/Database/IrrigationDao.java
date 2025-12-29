package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.agritrack.Models.Irrigation;

import java.util.List;

@Dao
public interface IrrigationDao {

    @Insert
    void insert(Irrigation irrigation);

    @Update
    void update(Irrigation irrigation);

    @Delete
    void delete(Irrigation irrigation);

    @Query("SELECT * FROM irrigations ORDER BY irrigationDate DESC")
    List<Irrigation> getAllIrrigations();

    @Query("SELECT * FROM irrigations WHERE terrainName = :terrain")
    List<Irrigation> getByTerrain(String terrain);
}
