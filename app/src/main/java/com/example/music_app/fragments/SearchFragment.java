package com.example.music_app.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.Adapter.SongAdapter;
import com.example.music_app.AppDatabase;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.R;
import com.example.music_app.entity.Song;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText edtSearch;
    private RecyclerView rvSongs;
    private SongAdapter adapter;
    private final List<Song> allSongs = new ArrayList<>();
    private final List<Song> filteredSongs = new ArrayList<>();
    private static final int REQUEST_CODE = 123;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        edtSearch = view.findViewById(R.id.edtSearch);
        rvSongs = view.findViewById(R.id.rvSongs);
        rvSongs.setLayoutManager(new LinearLayoutManager(context));

        if (hasPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }

        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

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
        });

        return view;
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_AUDIO) == PackageManager.PERMISSION_GRANTED;
        } else {
            return ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(new String[]{Manifest.permission.READ_MEDIA_AUDIO}, REQUEST_CODE);
        } else {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
        }
    }

    private void loadSongs() {
        allSongs.clear();
        filteredSongs.clear();

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            SongDao songDao = db.songDao();
            List<Song> allFromDB = songDao.getAllSongs();

            requireActivity().runOnUiThread(() -> {
                allSongs.addAll(allFromDB);
                filteredSongs.addAll(allSongs);
                adapter = new SongAdapter(filteredSongs, context);
                rvSongs.setAdapter(adapter);
            });
        }).start();
    }

    public static String removeAccent(String input) {
        if (input == null) return "";
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            loadSongs();
        } else {
            Toast.makeText(context, "Từ chối quyền truy cập nhạc!", Toast.LENGTH_SHORT).show();
        }
    }
}
