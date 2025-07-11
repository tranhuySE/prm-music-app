package com.example.music_app;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.music_app.DAO.PlaylistDao;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.DAO.SongFavoriteDao;
import com.example.music_app.DAO.UserDao;
import com.example.music_app.entity.PlayList;
import com.example.music_app.entity.PlayListSong;
import com.example.music_app.entity.Song;
import com.example.music_app.entity.SongFavorite;
import com.example.music_app.entity.User;

@Database(entities = {User.class, Song.class , PlayList.class, PlayListSong.class , SongFavorite.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase INSTANCE;

    public abstract UserDao userDao();
    public abstract SongDao songDao();
    public abstract PlaylistDao playlistDao();
    public abstract SongFavoriteDao songFavoriteDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "PRM302")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return INSTANCE;
    }
}

