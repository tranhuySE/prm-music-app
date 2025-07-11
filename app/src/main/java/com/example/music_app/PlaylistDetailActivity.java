package com.example.music_app;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import androidx.appcompat.widget.Toolbar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.PlaylistSongAdapter;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.PlayList;
import com.example.music_app.entity.Song;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AppCompatActivity {

    private int playlistId;
    private String playlistName;
    private AppDatabase db;
    private PlaylistSongAdapter adapter;
    private List<Song> songList = new ArrayList<>();
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_playlist_detail);
        db = AppDatabase.getInstance(this);

        playlistId = getIntent().getIntExtra("playlistId", -1);
        playlistName = getIntent().getStringExtra("playlistName");

        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(playlistName);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> finish());

        RecyclerView recyclerView = findViewById(R.id.recyclerSongs);
        adapter = new PlaylistSongAdapter(this,songList, this::removeSongFromPlaylist);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadSongs();
    }
    private void loadSongs() {
        new Thread(() -> {
            List<Song> songs = db.playlistDao().getSongsInPlaylist(playlistId);
            runOnUiThread(() -> {
                songList.clear();
                songList.addAll(songs);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }

    private void removeSongFromPlaylist(Song song) {
        new Thread(() -> {
            db.playlistDao().removeSongFromPlaylist(playlistId, song.getPath());
            loadSongs();
        }).start();
    }

    // Menu xoá playlist
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete_playlist) {
            confirmDeletePlaylist();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void confirmDeletePlaylist() {
        new AlertDialog.Builder(this)
                .setTitle("Xoá Playlist")
                .setMessage("Bạn có chắc muốn xoá playlist \"" + playlistName + "\" không?")
                .setPositiveButton("Xoá", (dialog, which) -> {
                    new Thread(() -> {
                        SessionManager sessionManager = new SessionManager(this);
                        String currentUser = sessionManager.getUsername();
                        PlayList playlist = db.playlistDao().getPlaylistById(playlistId);
                        if (playlist != null && playlist.getUser().equals(currentUser)) {
                            db.playlistDao().deletePlaylist(playlist);
                            runOnUiThread(() -> {
                                Intent resultIntent = new Intent();
                                resultIntent.putExtra("deletedPlaylistId", playlistId);
                                setResult(RESULT_OK, resultIntent);
                                finish();
                            });
                        } else {
                            runOnUiThread(() -> {
                                Snackbar.make(this.getCurrentFocus(),"Lỗi xóa playlist",Snackbar.LENGTH_SHORT).show();
                            });
                        }
                    }).start();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}