package com.example.cs499project;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

// WEIGHT CONSTRUCTOR
@Entity(tableName = "weights",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "uid",
                childColumns =  "uid",
                onDelete = ForeignKey.CASCADE
        ), indices = {@Index(value = {"uid", "day"}, unique = true)} // user and days are unique
)

public class Weight {
    @PrimaryKey(autoGenerate = true)
    public int wid;
    public int uid;
    public String day;
    public String weightValue;
}
