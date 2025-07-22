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
    private List<Song> favoriteList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_song_favorite);

        initializeViews();
        setupSession();
        loadFavoriteSongs();
        setupRecyclerView();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewFavorites);
    }

    private void setupSession() {
        sessionManager = new SessionManager(this);
    }

    private void loadFavoriteSongs() {
        String currentUser = sessionManager.getUsername();
        favoriteList = AppDatabase.getInstance(this)
                .songFavoriteDao()
                .getFavoritesForUser(currentUser);
    }

    private void setupRecyclerView() {
        adapter = new SongFavoriteAdapter(favoriteList, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshFavoriteList();
    }

    private void refreshFavoriteList() {
        loadFavoriteSongs();
        adapter.updateList(favoriteList);
    }
}