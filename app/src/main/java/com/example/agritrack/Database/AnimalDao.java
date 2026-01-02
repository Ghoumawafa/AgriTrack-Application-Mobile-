package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AnimalDao {

    @Query("SELECT * FROM animals ORDER BY name")
    List<AnimalEntity> getAll();

    @Query("SELECT * FROM animals WHERE species = :species ORDER BY name")
    List<AnimalEntity> getBySpecies(String species);

    @Query("SELECT * FROM animals WHERE id = :id LIMIT 1")
    AnimalEntity getById(long id);


    @Query("SELECT COUNT(*) FROM animals WHERE species = :species")
    int getCountBySpecies(String species);

    @Query("SELECT DISTINCT species FROM animals ORDER BY species")
    List<String> getAllSpecies();

    @Query("SELECT id FROM animals WHERE species = :species")
    List<Long> getIdsBySpecies(String species);
    @Insert
    long insert(AnimalEntity animal);

    @Update
    int update(AnimalEntity animal);

    @Delete
    int delete(AnimalEntity animal);

    @Query("DELETE FROM animals WHERE id = :id")
    int deleteById(long id);
}
