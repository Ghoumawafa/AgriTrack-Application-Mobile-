package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TerrainDao {

    @Query("SELECT * FROM terrains ORDER BY name")
    List<TerrainEntity> getAll();

    @Query("SELECT * FROM terrains WHERE id = :id LIMIT 1")
    TerrainEntity getById(long id);

    @Insert
    long insert(TerrainEntity terrain);

    @Update
    int update(TerrainEntity terrain);

    @Delete
    int delete(TerrainEntity terrain);
}
