package com.example.music_app.entity;

public class Song {
    private String title;
    private String artist;
    private String path;
    private long duration;
    private int playCount = 0;

    public Song(String title, String artist, String path, long duration) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public int getPlayCount() {
        return playCount;
    }

    public void incrementPlayCount() {
        playCount++;
    }
}

