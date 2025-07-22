package com.example.music_app;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongFavoriteAdapter;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Song;

import java.util.List;

public class ListSongFavoriteActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SongFavoriteAdapter adapter;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song_favorite);

        initViews();
        initSession();
        setupRecyclerView();
        loadAndDisplayFavorites();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewFavorites);
    }

    private void initSession() {
        sessionManager = new SessionManager(this);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SongFavoriteAdapter(null, this);
        recyclerView.setAdapter(adapter);
    }

    private void loadAndDisplayFavorites() {
        String currentUser = sessionManager.getUsername();
        List<Song> favoriteList = AppDatabase.getInstance(this)
                .songFavoriteDao()
                .getFavoritesForUser(currentUser);
        adapter.updateList(favoriteList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAndDisplayFavorites(); // Tự động cập nhật khi quay lại activity
    }
}
