package com.example.music_app.fragments;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.example.music_app.Adapter.PlaylistAdapter;
import com.example.music_app.AppDatabase;
import com.example.music_app.PlaylistDetailActivity;
import com.example.music_app.R;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.PlayList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class PlayListFragment extends Fragment {

    private RecyclerView recyclerView;
    private PlaylistAdapter adapter;
    private AppDatabase db;
    private FloatingActionButton btnAdd;
    private SessionManager sessionManager;

    public PlayListFragment() {
        // Required empty public constructor
    }

    public static PlayListFragment newInstance() {
        return new PlayListFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Không khởi tạo sessionManager ở đây vì getContext() có thể null
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_play_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerPlaylists);
        btnAdd = view.findViewById(R.id.btnAdd);

        db = AppDatabase.getInstance(requireContext());
        sessionManager = new SessionManager(requireContext());

        adapter = new PlaylistAdapter(new ArrayList<>(), playlist -> {
            // Mở PlaylistDetailActivity
            Intent intent = new Intent(getContext(), PlaylistDetailActivity.class);
            intent.putExtra("playlistId", playlist.getId());
            intent.putExtra("playlistName", playlist.getName());
            launcher.launch(intent);
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        loadPlaylists();

        btnAdd.setOnClickListener(v -> showAddDialog());

        return view;
    }

    private void loadPlaylists() {
        new Thread(() -> {
            String currentUser = sessionManager.getUsername();
            if (currentUser == null) return; // user chưa đăng nhập
            List<PlayList> playlists = db.playlistDao().getPlaylistsByUser(currentUser);
            requireActivity().runOnUiThread(() -> adapter.setPlaylists(playlists));
        }).start();
    }

    private void showAddDialog() {
        EditText input = new EditText(getContext());
        input.setHint("Tên playlist");

        new AlertDialog.Builder(getContext())
                .setTitle("Tạo Playlist mới")
                .setView(input)
                .setPositiveButton("Tạo", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        String username = sessionManager.getUsername();
                        if (username == null) return;
                        PlayList newPlaylist = new PlayList(name, username);
                        new Thread(() -> {
                            db.playlistDao().insertPlaylist(newPlaylist);
                            loadPlaylists();
                        }).start();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
    private final ActivityResultLauncher<Intent> launcher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == getActivity().RESULT_OK && result.getData() != null) {
                    int deletedId = result.getData().getIntExtra("deletedPlaylistId", -1);
                    if (deletedId != -1) {
                        loadPlaylists(); // Reload lại danh sách
                    }
                }
            });
}
