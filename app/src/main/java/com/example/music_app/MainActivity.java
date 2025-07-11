package com.example.music_app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.music_app.entity.Song;
import com.example.music_app.utils.SongUtils;
import com.google.gson.Gson;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    GridLayout gridHot, gridNew;
    List<Song> allSongs, hotSongs, newSongs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        allSongs = SongUtils.getAllAudioFromDevice(this); // Tự viết hàm lấy bài hát
        // Giả lập dữ liệu
        hotSongs = SongUtils.getTopPlayedSongs(allSongs); // Lấy 6 bài hot
        newSongs = SongUtils.getLatestSongs(allSongs);    // Lấy 6 bài mới

        LinearLayout layoutHotSongs = findViewById(R.id.layoutHotSongs);
        LinearLayout layoutLatestSongs = findViewById(R.id.layoutLatestSongs);

        showHorizontalSongs(hotSongs, layoutHotSongs);
        showHorizontalSongs(newSongs, layoutLatestSongs);

        // Gắn sự kiện cho bottom nav
        findViewById(R.id.btnSearch).setOnClickListener(v -> {
            startActivity(new Intent(this, SearchActivity.class));
        });
    }

    private void showHorizontalSongs(List<Song> songs, LinearLayout parentLayout) {
        parentLayout.removeAllViews(); // Xóa dữ liệu cũ nếu có

        for (Song song : songs) {
            View itemView = LayoutInflater.from(this).inflate(R.layout.item_horizontal_song, parentLayout, false);

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
                Intent intent = new Intent(MainActivity.this, PlayerActivity.class);
                intent.putExtra("song_title", song.getTitle());
                intent.putExtra("song_artist", song.getArtist());
                intent.putExtra("song_path", song.getPath());
                intent.putExtra("song_duration", song.getDuration());
                intent.putExtra("song_list", new Gson().toJson(songs)); // chỉ truyền danh sách hiện tại
                intent.putExtra("current_index", songs.indexOf(song));
                startActivity(intent);
            });

            parentLayout.addView(itemView);
        }
    }
}