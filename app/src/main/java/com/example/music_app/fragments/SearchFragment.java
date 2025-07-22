package com.example.music_app.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class SearchFragment extends Fragment {

    private static final int REQUEST_CODE = 123;
    private static final int MIN_SEARCH_LENGTH = 1;

    private EditText edtSearch;
    private RecyclerView rvSongs;
    private SongAdapter adapter;
    private final List<Song> allSongs = new ArrayList<>();
    private final List<Song> filteredSongs = new ArrayList<>();
    private Context context;
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        context = getContext();

        initializeViews(view);
        setupRecyclerView();
        checkAndRequestPermission();
        setupSearchListener();

        return view;
    }

    private void initializeViews(View view) {
        edtSearch = view.findViewById(R.id.edtSearch);
        rvSongs = view.findViewById(R.id.rvSongs);
    }

    private void setupRecyclerView() {
        rvSongs.setLayoutManager(new LinearLayoutManager(context));
        adapter = new SongAdapter(filteredSongs, context);
        rvSongs.setAdapter(adapter);
    }

    private void checkAndRequestPermission() {
        if (hasPermission()) {
            loadSongs();
        } else {
            requestPermission();
        }
    }

    private void setupSearchListener() {
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterSongs(s.toString());
            }
        });
    }

    private void filterSongs(String query) {
        String keyword = removeAccent(query.toLowerCase().trim());
        filteredSongs.clear();

        if (keyword.length() >= MIN_SEARCH_LENGTH) {
            for (Song song : allSongs) {
                String title = removeAccent(song.getTitle().toLowerCase());
                String artist = removeAccent(song.getArtist().toLowerCase());
                if (title.contains(keyword) || artist.contains(keyword)) {
                    filteredSongs.add(song);
                }
            }
        } else {
            filteredSongs.addAll(allSongs);
        }

        adapter.notifyDataSetChanged();
    }

    private boolean hasPermission() {
        if (context == null) return false;

        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (context == null) return;

        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                ? Manifest.permission.READ_MEDIA_AUDIO
                : Manifest.permission.READ_EXTERNAL_STORAGE;

        requestPermissions(new String[]{permission}, REQUEST_CODE);
    }

    private void loadSongs() {
        if (context == null) return;

        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            SongDao songDao = db.songDao();
            List<Song> allFromDB = songDao.getAllSongs();

            requireActivity().runOnUiThread(() -> {
                allSongs.clear();
                filteredSongs.clear();

                allSongs.addAll(allFromDB);
                filteredSongs.addAll(allSongs);
                adapter.notifyDataSetChanged();
            });
        });
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
        } else if (context != null) {
            Toast.makeText(context, "Từ chối quyền truy cập nhạc!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Clear references to avoid memory leaks
        context = null;
    }
}