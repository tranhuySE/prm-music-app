package com.example.music_app.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "playlists")
public class PlayList {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String user;
    private String name;

    // Constructor đầy đủ
    public PlayList(int id,String name, String user) {
        this.id = id;
        this.name = name;
        this.user = user;
    }
    @Ignore
    public PlayList(String name, String user) {
        this.name = name;
        this.user = user;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public String getUser() { return user; }

    public void setUser(String user) { this.user = user; }
}
