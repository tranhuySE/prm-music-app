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

import java.util.List;

public class SongFavoriteAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> favoriteSongs;
    private final Context context;

    public SongFavoriteAdapter(List<Song> favoriteSongs, Context context) {
        this.favoriteSongs = favoriteSongs;
        this.context = context;
    }

    @NonNull
    @Override
    public SongAdapter.SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(context).inflate(R.layout.item_song_play_list, parent, false);
        return new SongAdapter.SongViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.SongViewHolder holder, int position) {
        Song song = favoriteSongs.get(position);

        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(formatTime((int) song.getDuration()));

        Glide.with(context)
                .load(song.getImgUrl())
                .placeholder(R.drawable.default_cover)
                .into(holder.imgCover);

        holder.imgFavorite.setImageResource(R.drawable.ic_favorite_outline);

        holder.imgFavorite.setOnClickListener(v -> {
            String username = new SessionManager(context).getUsername();

            AppDatabase.getInstance(context).songFavoriteDao()
                    .deleteByUsernameAndPath(username, song.getPath());

            int removedPosition = holder.getAdapterPosition();
            if (removedPosition != RecyclerView.NO_POSITION) {
                favoriteSongs.remove(removedPosition);
                notifyItemRemoved(removedPosition);
                Toast.makeText(context, "Đã bỏ yêu thích", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return favoriteSongs != null ? favoriteSongs.size() : 0;
    }

    private String formatTime(int durationInSeconds) {
        int minutes = durationInSeconds / 60;
        int seconds = durationInSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}
