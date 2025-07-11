package com.example.music_app.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;

@Entity(
        tableName = "playlist_songs",
        primaryKeys = {"playlistId", "songPath"},
        foreignKeys = {
                @ForeignKey(entity = PlayList.class,
                        parentColumns = "id",
                        childColumns = "playlistId",
                        onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Song.class,
                        parentColumns = "path",
                        childColumns = "songPath",
                        onDelete = ForeignKey.CASCADE)
        }
)
public class PlayListSong {
    @NonNull
    private int playlistId;

    @NonNull
    private String songPath;

    // Constructor, Getter, Setter

    public PlayListSong(int playlistId, @NonNull String songPath) {
        this.playlistId = playlistId;
        this.songPath = songPath;
    }

    public int getPlaylistId() { return playlistId; }

    public void setPlaylistId(int playlistId) { this.playlistId = playlistId; }

    public String getSongPath() { return songPath; }

    public void setSongPath(String songPath) { this.songPath = songPath; }
}
