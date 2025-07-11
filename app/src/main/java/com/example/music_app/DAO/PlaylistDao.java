package com.example.music_app.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.music_app.entity.PlayList;
import com.example.music_app.entity.PlayListSong;
import com.example.music_app.entity.Song;

import java.util.List;

@Dao
public interface PlaylistDao {

    // Playlist
    @Insert
    long insertPlaylist(PlayList playlist);

    @Query("SELECT * FROM playlists WHERE user = :username")
    List<PlayList> getPlaylistsByUser(String username);

    @Delete
    void deletePlaylist(PlayList playlist);
    // Playlist-Song
    @Insert
    void insertSongToPlaylist(PlayListSong playlistSong);

    @Query("SELECT * FROM songs WHERE path IN (SELECT songPath FROM playlist_songs WHERE playlistId = :playlistId)")
    List<Song> getSongsInPlaylist(int playlistId);

    @Query("SELECT COUNT(*) FROM playlist_songs WHERE playlistId = :playlistId AND songPath = :songPath")
    int countSongInPlaylist(int playlistId, String songPath);
    @Query("SELECT * FROM playlists WHERE id = :id")
    PlayList getPlaylistById(int id);
    @Query("DELETE FROM playlist_songs WHERE playlistId = :playlistId AND songPath = :songPath")
    void removeSongFromPlaylist(int playlistId, String songPath);
}
