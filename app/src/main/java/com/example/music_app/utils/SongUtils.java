package com.example.music_app.utils;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;

import com.example.music_app.AppDatabase;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.DAO.SongFavoriteDao;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Song;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SongUtils {
    public static List<Song> getTopPlayedSongs(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        SongDao songDao = db.songDao();
        return songDao.getTopSongs(3);
    }

    public static List<Song> getLatestSongs(List<Song> all) {
        int size = all.size();
        return all.subList(Math.max(0, size - 5), size);
    }


    public static List<Song> getFavoriteList(Context context){
        AppDatabase db = AppDatabase.getInstance(context);
        SessionManager sessionManager = new SessionManager(context);
        String currentUser = sessionManager.getUsername();
        SongFavoriteDao songFavoriteDao = db.songFavoriteDao();
        return songFavoriteDao.getFavoritesForUser(currentUser);
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

    public static List<Song> getAllSongsFromDevice(Context context) {
        List<Song> songList = new ArrayList<>();

        MediaScannerConnection.scanFile(
                context,
                new String[]{"/sdcard/Music", "/sdcard/Download"},
                null,
                null
        );

        Uri collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

        String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
        String[] projection = {
                MediaStore.Audio.Media.DATA, // path
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.DURATION
        };

        try (Cursor cursor = context.getContentResolver().query(collection, projection, selection, null, null)) {
            if (cursor != null) {
                int pathColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                int titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);

                while (cursor.moveToNext()) {
                    String path = cursor.getString(pathColumn);
                    String title = cursor.getString(titleColumn);
                    String artist = cursor.getString(artistColumn);
                    long duration = cursor.getLong(durationColumn);

                    Song song = new Song(path, title, artist, duration, false, 0);
                    songList.add(song);
                }
                cursor.close();
            }
        }

        return songList;
    }


}

