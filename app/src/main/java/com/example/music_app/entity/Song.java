package com.example.music_app.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "songs")
public class Song {

    @PrimaryKey
    @NonNull
    private String path;
    private String title;
    private String artist;
    private long duration;
    private int playCount;

    @Ignore
    private boolean isFavorite;
    private String imgUrl;

    public boolean isFavorite() {
        return isFavorite;
    }
    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public Song(@NonNull String path, String title, String artist, long duration, int playCount) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.playCount = playCount;
    }

    @Ignore
    public Song(String pathOrUrl, String title, String artist, long duration, boolean isFavorite, int playCount, String imgUrl) {
        this.path = pathOrUrl;
        this.title = title;
        this.artist = artist;
        this.duration = duration;
        this.isFavorite = isFavorite;
        this.playCount = playCount;
        this.imgUrl = imgUrl;
    }

    @Ignore
    public Song(@NonNull String path, String title, String artist, long duration, boolean isFavorite, int playCount) {
        this(path, title, artist, duration, playCount);
        this.isFavorite = isFavorite;
    }
    // Getter + Setter
    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @NonNull
    public String getPath() {
        return path;
    }

    public void setPath(@NonNull String path) {
        this.path = path;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void setPlayCount(int playCount) {
        this.playCount = playCount;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}


