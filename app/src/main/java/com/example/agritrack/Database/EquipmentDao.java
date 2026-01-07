package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface EquipmentDao {

    @Query("SELECT * FROM equipments ORDER BY name")
    List<EquipmentEntity> getAll();

    @Query("SELECT * FROM equipments WHERE id = :id LIMIT 1")
    EquipmentEntity getById(long id);

    @Insert
    long insert(EquipmentEntity equipment);

    @Update
    int update(EquipmentEntity equipment);

    @Delete
    int delete(EquipmentEntity equipment);
}
