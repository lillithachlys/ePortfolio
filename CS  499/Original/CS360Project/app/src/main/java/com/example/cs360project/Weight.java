package com.example.cs360project;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity(tableName = "weights",
        foreignKeys = @ForeignKey(
                entity = User.class,
                parentColumns = "uid",
                childColumns =  "uid",
                onDelete = ForeignKey.CASCADE
        ), indices = {@Index("uid")}
)
public class Weight {
    @PrimaryKey(autoGenerate = true)
    public int wid;
    public int uid;
    public String day;
    public String weightValue;
}
