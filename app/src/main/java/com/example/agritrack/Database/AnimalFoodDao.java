package com.example.agritrack.Database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AnimalFoodDao {

    @Query("SELECT * FROM animal_foods ORDER BY name")
    List<AnimalFoodEntity> getAll();

    @Query("SELECT * FROM animal_foods WHERE type = :type ORDER BY name")
    List<AnimalFoodEntity> getByType(String type);

    @Query("SELECT * FROM animal_foods WHERE id = :id LIMIT 1")
    AnimalFoodEntity getById(long id);

    @Query("SELECT * FROM animal_foods WHERE quantity <= alert_quantity")
    List<AnimalFoodEntity> getLowStock();

    @Insert
    long insert(AnimalFoodEntity food);

    @Update
    int update(AnimalFoodEntity food);

    @Delete
    int delete(AnimalFoodEntity food);

    @Query("UPDATE animal_foods SET quantity = quantity - :amount WHERE id = :id AND quantity >= :amount")
    int consumeFood(long id, double amount);

    @Query("UPDATE animal_foods SET quantity = quantity + :amount WHERE id = :id")
    int addStock(long id, double amount);
}