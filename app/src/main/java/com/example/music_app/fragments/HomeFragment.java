package com.example.music_app.fragments;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.music_app.AppDatabase;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.PlayerActivity;
import com.example.music_app.R;
import com.example.music_app.entity.Song;
import com.example.music_app.utils.SongUtils;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private LinearLayout layoutHotSongs, layoutLatestSongs,layoutFavorite;
    private List<Song> allSongs, hotSongs, newSongs,favoriteSong;
    private ProgressBar progressBar;
    private Context context;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        context = getContext();

        layoutHotSongs = view.findViewById(R.id.layoutHotSongs);
        layoutLatestSongs = view.findViewById(R.id.layoutLatestSongs);
        layoutFavorite = view.findViewById(R.id.layoutFavoriteSong);
        progressBar = view.findViewById(R.id.progressBar);
        allSongs = new ArrayList<>();

        loadData();

        return view;
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE); // Đặt ngay khi bắt đầu

        new Thread(() -> {
            AppDatabase db = AppDatabase.getInstance(context);
            SongDao songDao = db.songDao();

            List<Song> songsFromDb = songDao.getAllSongs();

            // Nếu chưa có thì mới lấy từ mạng hoặc thiết bị
            if (songsFromDb.isEmpty()) {
//                List<Song> songsFromDeezer = SongUtils.getAllSongsFromJamendo(); // Có thể chậm ở đây
                List<Song> songsFromDeezer = SongUtils.getAllSongsFromDevice(context); // Có thể chậm ở đây
                songDao.insertAll(songsFromDeezer);
                songsFromDb = songsFromDeezer;
            }

            List<Song> finalSongs = songsFromDb;
            requireActivity().runOnUiThread(() -> {
                allSongs.clear();
                allSongs.addAll(finalSongs);

                hotSongs = SongUtils.getTopPlayedSongs(context);
                newSongs = SongUtils.getLatestSongs(allSongs);
                favoriteSong = SongUtils.getFavoriteList(context);

                showHorizontalSongs(hotSongs, layoutHotSongs);
                showHorizontalSongs(newSongs, layoutLatestSongs);
                showHorizontalSongs(favoriteSong, layoutFavorite);

                progressBar.setVisibility(View.GONE); // Ẩn sau khi hiển thị xong
            });
        }).start();
    }

    private void showHorizontalSongs(List<Song> songs, LinearLayout parentLayout) {
        parentLayout.removeAllViews();

        for (Song song : songs) {
            View itemView = LayoutInflater.from(context).inflate(R.layout.item_horizontal_song, parentLayout, false);

            ImageView imgCover = itemView.findViewById(R.id.imgCover);
            TextView tvTitle = itemView.findViewById(R.id.tvTitle);
            tvTitle.setText(song.getTitle());

            Bitmap cover = SongUtils.getSongCover(song.getPath());
            if (cover != null) {
                imgCover.setImageBitmap(cover);
            } else {
                imgCover.setImageResource(R.drawable.default_cover);
            }

            itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, PlayerActivity.class);
                intent.putExtra("song_title", song.getTitle());
                intent.putExtra("song_artist", song.getArtist());
                intent.putExtra("song_path", song.getPath());
                intent.putExtra("song_duration", song.getDuration());
                intent.putExtra("song_list", new Gson().toJson(songs));
                intent.putExtra("current_index", songs.indexOf(song));
                startActivity(intent);
            });

            parentLayout.addView(itemView);
        }
    }
}
