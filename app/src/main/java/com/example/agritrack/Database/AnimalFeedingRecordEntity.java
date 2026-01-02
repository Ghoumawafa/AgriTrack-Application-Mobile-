package com.example.agritrack.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;
import androidx.room.Index;

@Entity(
        tableName = "animal_feeding_records",
        foreignKeys = {
                @ForeignKey(entity = AnimalEntity.class, parentColumns = "id", childColumns = "animal_id", onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = AnimalFeedingScheduleEntity.class, parentColumns = "id", childColumns = "schedule_id", onDelete = ForeignKey.CASCADE)
        },
        indices = {
                @Index(value = "animal_id"),
                @Index(value = "schedule_id"),
                @Index(value = {"record_date", "record_time"})
        }
)
public class AnimalFeedingRecordEntity {
    @PrimaryKey(autoGenerate = true)
    private long id;

    @ColumnInfo(name = "animal_id")
    private long animalId;

    @ColumnInfo(name = "schedule_id")
    private Long scheduleId;

    @ColumnInfo(name = "record_date")
    @NonNull
    private String recordDate; // yyyy-MM-dd

    @ColumnInfo(name = "record_time")
    @NonNull
    private String recordTime; // HH:mm

    @ColumnInfo(name = "status")
    @NonNull
    private String status; // FED or SKIPPED

    @ColumnInfo(name = "quantity_given", defaultValue = "0")
    private double quantityGiven;

    @ColumnInfo(name = "leftovers", defaultValue = "0")
    private double leftovers;

    @ColumnInfo(name = "fed_by")
    private String fedBy;

    @ColumnInfo(name = "notes")
    private String notes;

    public AnimalFeedingRecordEntity(long animalId, Long scheduleId, @NonNull String recordDate, @NonNull String recordTime, @NonNull String status) {
        this.animalId = animalId;
        this.scheduleId = scheduleId;
        this.recordDate = recordDate;
        this.recordTime = recordTime;
        this.status = status;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getAnimalId() { return animalId; }
    public void setAnimalId(long animalId) { this.animalId = animalId; }
    public Long getScheduleId() { return scheduleId; }
    public void setScheduleId(Long scheduleId) { this.scheduleId = scheduleId; }
    public String getRecordDate() { return recordDate; }
    public void setRecordDate(String recordDate) { this.recordDate = recordDate; }
    public String getRecordTime() { return recordTime; }
    public void setRecordTime(String recordTime) { this.recordTime = recordTime; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public double getQuantityGiven() { return quantityGiven; }
    public void setQuantityGiven(double quantityGiven) { this.quantityGiven = quantityGiven; }
    public double getLeftovers() { return leftovers; }
    public void setLeftovers(double leftovers) { this.leftovers = leftovers; }
    public String getFedBy() { return fedBy; }
    public void setFedBy(String fedBy) { this.fedBy = fedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
