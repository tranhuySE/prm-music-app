package com.example.music_app;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongFavoriteAdapter;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Song;

import java.util.List;

public class ListSongFavoriteActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_song_favorite);
        RecyclerView recyclerView = findViewById(R.id.recyclerViewFavorites);
        SessionManager sessionManager = new SessionManager(this);
        String currentUser = sessionManager.getUsername();

        List<Song> favoriteList = AppDatabase.getInstance(this)
                .songFavoriteDao()
                .getFavoritesForUser(currentUser);

        SongFavoriteAdapter adapter = new SongFavoriteAdapter(favoriteList, this);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }
}