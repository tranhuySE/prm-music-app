package com.example.music_app.DAO;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Delete;

import com.example.music_app.entity.Song;
import com.example.music_app.entity.SongFavorite;

import java.util.List;

@Dao
public interface SongFavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(SongFavorite songFavorite);

    @Query("DELETE FROM song_favorites WHERE username = :username AND songPath = :path")
    void deleteByUsernameAndPath(String username, String path);

    @Query("SELECT EXISTS(SELECT 1 FROM song_favorites WHERE username = :username AND songPath = :path)")
    boolean isFavorite(String username, String path);

    @Query("SELECT * FROM songs WHERE path IN (SELECT songPath FROM song_favorites WHERE username = :username)")
    List<Song> getFavoritesForUser(String username);
}

