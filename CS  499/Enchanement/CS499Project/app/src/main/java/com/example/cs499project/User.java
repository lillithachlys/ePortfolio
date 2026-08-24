package com.example.cs499project;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.ColumnInfo;

// USER CONSTRUCTOR
@Entity(tableName = "users")
public class User {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "username")
    public String userName;

    @ColumnInfo(name = "password")
    public String userPassword;
}

