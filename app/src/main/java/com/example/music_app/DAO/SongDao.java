package com.example.music_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.example.music_app.entity.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<Song> songs);

    @Query("SELECT * FROM songs")
    List<Song> getAllSongs();

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE path = :path")
    void increasePlayCount(String path);

    @Query("SELECT * FROM songs ORDER BY playCount DESC LIMIT :limit")
    List<Song> getTopSongs(int limit);
}

