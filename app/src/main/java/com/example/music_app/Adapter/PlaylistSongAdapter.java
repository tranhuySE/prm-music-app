package com.example.music_app.Adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.PlayerActivity;
import com.example.music_app.R;
import com.example.music_app.entity.Song;
import com.google.gson.Gson;

import java.util.List;

public class PlaylistSongAdapter extends RecyclerView.Adapter<PlaylistSongAdapter.SongViewHolder> {

    private final List<Song> songs;
    private OnSongRemoveListener removeListener;

    private Context context;

    public interface OnSongRemoveListener {
        void onRemove(Song song);
    }

    public PlaylistSongAdapter(Context context,List<Song> songs, OnSongRemoveListener removeListener) {
        this.context = context;
        this.songs = songs;
        this.removeListener = removeListener;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.item_song_play_list, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songs.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(formatTime((int) song.getDuration()));

        if (song.getImgUrl() != null && !song.getImgUrl().isEmpty()) {
            // Nếu có ảnh từ internet => dùng Glide hoặc Picasso để load ảnh
            Glide.with(context).load(song.getImgUrl()).into(holder.Img);
        } else {
            // Nếu không, dùng MediaMetadataRetriever như cũ
            new Thread(() -> {
                byte[] art = getAlbumArt(song.getPath());
                if (art != null) {
                    ((Activity) context).runOnUiThread(() -> {
                        Glide.with(context)
                                .asBitmap()
                                .load(art)
                                .placeholder(R.drawable.default_cover)
                                .into(holder.Img);
                    });
                }
            }).start();
        }

        // Format duration từ milliseconds sang mm:ss

        holder.btnDelete.setOnClickListener(v -> {
            if (removeListener != null) {
                removeListener.onRemove(song);
            }
        });

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("song_title", song.getTitle());
            intent.putExtra("song_artist", song.getArtist());
            intent.putExtra("song_path", song.getPath());
            intent.putExtra("song_duration", song.getDuration());
            intent.putExtra("song_list", new Gson().toJson(songs));
            intent.putExtra("current_index", position);
            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return songs.size();
    }

    static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageView btnDelete , Img;

        SongViewHolder(View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_Title);
            tvArtist = itemView.findViewById(R.id.tv_Artist);
            tvDuration = itemView.findViewById(R.id.tv_Duration);
            btnDelete = itemView.findViewById(R.id.btn_delete);
            Img = itemView.findViewById(R.id.imgCover);
        }
    }

    private byte[] getAlbumArt(String path) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(path);
            byte[] art = retriever.getEmbeddedPicture();
            retriever.release();
            return art;
        } catch (Exception e) {
            return null;
        }
    }

    private String formatTime(int durationSeconds) {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
}

