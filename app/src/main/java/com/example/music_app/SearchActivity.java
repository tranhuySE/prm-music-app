package com.example.music_app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.entity.Song;
import com.example.music_app.utils.SongUtils;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText edtSearch;
    private RecyclerView rvSongs;
    private SongAdapter adapter;
    private final List<Song> allSongs = new ArrayList<>();
    private final List<Song> filteredSongs = new ArrayList<>();

    private static final int REQUEST_CODE = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        edtSearch = findViewById(R.id.edtSearch);
        rvSongs = findViewById(R.id.rvSongs);
        rvSongs.setLayoutManager(new LinearLayoutManager(this));

        try {
            loadSongs();
        } catch (Exception e) {
            Log.e("ERROR_LOAD", "Lỗi load bài hát", e);
        }

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String keyword = removeAccent(s.toString().toLowerCase());
                filteredSongs.clear();

                for (Song song : allSongs) {
                    String title = removeAccent(song.getTitle().toLowerCase());
                    String artist = removeAccent(song.getArtist().toLowerCase());

                    if (title.contains(keyword) || artist.contains(keyword)) {
                        filteredSongs.add(song);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        findViewById(R.id.btnHomeS).setOnClickListener(v -> {
            startActivity(new Intent(this,MainActivity.class));
        });

        findViewById(R.id.btnSearchS).setOnClickListener(v -> {
            Toast.makeText(this , "Bạn đang ở trang Search", Toast.LENGTH_SHORT).show();
        });
    }

    public static String removeAccent(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return checkSelfPermission(Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void loadSongs() {
        allSongs.clear();
        filteredSongs.clear();

        allSongs.addAll(SongUtils.getAllAudioFromDevice(this));
        filteredSongs.addAll(allSongs);

        adapter = new SongAdapter(filteredSongs, this);
        rvSongs.setAdapter(adapter);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            Log.e("Permission", "Quyền bị từ chối.");
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
