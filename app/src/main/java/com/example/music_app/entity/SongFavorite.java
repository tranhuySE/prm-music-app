package com.example.music_app.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "song_favorites")
public class SongFavorite {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String username;
    private String songPath;
    public SongFavorite(String username, String songPath) {
        this.username = username;
        this.songPath = songPath;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getSongPath() { return songPath; }
    public void setSongPath(String songPath) { this.songPath = songPath; }
}
