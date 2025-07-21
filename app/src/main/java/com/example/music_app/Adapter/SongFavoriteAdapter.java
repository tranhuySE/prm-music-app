package com.example.music_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.AppDatabase;
import com.example.music_app.R;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Song;
import com.example.music_app.entity.SongFavorite;

import java.util.List;

public class SongFavoriteAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songFavorite;
    private final Context context;

    public SongFavoriteAdapter(List<Song> songFavorite, Context context) {
        this.songFavorite = songFavorite;
        this.context = context;
    }

    @NonNull
    @Override
    public SongAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song_play_list, parent, false);
        return new SongAdapter.SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongViewHolder holder, int position) {
        Song song = songFavorite.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(formatTime((int) song.getDuration()));

        Glide.with(context)
                .load(song.getImgUrl())
                .placeholder(R.drawable.default_cover)
                .into(holder.imgCover);

        // Luôn gắn icon yêu thích
        holder.imgFavorite.setImageResource(R.drawable.ic_favorite_outline);

        // Bấm để BỎ yêu thích
        holder.imgFavorite.setOnClickListener(v -> {
            SessionManager sessionManager = new SessionManager(context);
            String currentUser = sessionManager.getUsername();

            AppDatabase.getInstance(context).songFavoriteDao()
                    .deleteByUsernameAndPath(currentUser, song.getPath());

            // Xóa khỏi danh sách và cập nhật UI
            int removedIndex = holder.getAdapterPosition();
            songFavorite.remove(removedIndex);
            notifyItemRemoved(removedIndex);

            Toast.makeText(context, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return songFavorite != null ? songFavorite.size() : 0;
    }

    private String formatTime(int durationSeconds) {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
