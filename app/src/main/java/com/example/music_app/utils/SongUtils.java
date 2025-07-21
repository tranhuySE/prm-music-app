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

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

    public static List<Song> getAllSongsFromDeezer() {
        List<Song> songList = new ArrayList<>();

        try {
            URL url = new URL("https://api.deezer.com/search?q=nhac%20tre&output=json");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            InputStream inputStream = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject jsonResponse = new JSONObject(jsonBuilder.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("data");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);

                String title = item.getString("title");
                String artist = item.getJSONObject("artist").getString("name");
                String previewUrl = item.getString("preview"); // MP3 link
                long duration = 30_000; // preview chỉ 30s

                Song song = new Song(previewUrl, title, artist, duration, false, 0);
                songList.add(song);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return songList;
    }


    public static List<Song> getAllSongsFromJamendo() {
        List<Song> songList = new ArrayList<>();
        String clientId = "99d59977";

        try {
            String apiUrl = "https://api.jamendo.com/v3.0/tracks/?" +
                    "client_id=" + clientId +
                    "&format=json&limit=20&fuzzytags=pop&audioformat=mp32";

            URL url = new URL(apiUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder jsonBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            JSONObject jsonResponse = new JSONObject(jsonBuilder.toString());
            JSONArray dataArray = jsonResponse.getJSONArray("results");

            for (int i = 0; i < dataArray.length(); i++) {
                JSONObject item = dataArray.getJSONObject(i);

                String title = item.getString("name");
                String artist = item.getString("artist_name");
                String audio = item.getString("audio");
                String image = item.getString("album_image"); // ảnh đại diện
                int duration = item.getInt("duration");

                Song song = new Song(audio, title, artist, duration, false, 0);
                song.setImgUrl(image); // Nếu bạn có trường ảnh
                songList.add(song);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return songList;
    }


}

