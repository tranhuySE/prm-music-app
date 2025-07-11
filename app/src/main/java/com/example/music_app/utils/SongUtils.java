package com.example.music_app.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.music_app.entity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongUtils {
    public static List<Song> getTopPlayedSongs(List<Song> all) {
        Collections.sort(all, (s1, s2) -> Integer.compare(s2.getPlayCount(), s1.getPlayCount()));
        return all.subList(0, Math.min(3, all.size()));
    }

    public static List<Song> getLatestSongs(List<Song> all) {
        int size = all.size();
        return all.subList(Math.max(0, size - 5), size);
    }

    public static Bitmap getSongCover(String path) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(path);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                return BitmapFactory.decodeByteArray(art, 0, art.length);
            }
        } catch (Exception e) {}
        return null;
    }

    public static List<Song> getAllAudioFromDevice(Context context) {
        List<Song> songs = new ArrayList<>();

        MediaScannerConnection.scanFile(
                context,
                new String[]{"/sdcard/Music", "/sdcard/Download"},
                null,
                null
        );

        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DATA,
                MediaStore.Audio.Media.DURATION
        };

        String selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0";

        Cursor cursor = context.getContentResolver().query(
                uri,
                projection,
                selection,
                null,
                MediaStore.Audio.Media.TITLE + " ASC"
        );

        if (cursor != null) {
            while (cursor.moveToNext()) {
                String title = cursor.getString(0);
                String artist = cursor.getString(1);
                String path = cursor.getString(2);
                long duration = cursor.getLong(3);

                songs.add(new Song(title, artist, path, duration));
            }
            cursor.close();
        }

        return songs;
    }
}

