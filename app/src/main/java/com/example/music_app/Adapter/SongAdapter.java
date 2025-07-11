package com.example.music_app.Adapter;

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
import com.example.music_app.PlayerActivity;
import com.example.music_app.R;
import com.example.music_app.entity.Song;
import com.google.gson.Gson;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songList;
    private final Context context;

    public SongAdapter(List<Song> songList, Context context) {
        this.songList = songList;
        this.context = context;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder, int position) {
        Song song = songList.get(position);
        holder.tvTitle.setText(song.getTitle());
        holder.tvArtist.setText(song.getArtist());
        holder.tvDuration.setText(formatTime(song.getDuration()));

        // Load ảnh cover nếu có
        byte[] art = getAlbumArt(song.getPath());
        if (art != null) {
            holder.imgCover.setImageBitmap(android.graphics.BitmapFactory.decodeByteArray(art, 0, art.length));
        } else {
            holder.imgCover.setImageResource(R.drawable.default_cover); // ảnh mặc định
        }

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, PlayerActivity.class);
            intent.putExtra("song_title", song.getTitle());
            intent.putExtra("song_artist", song.getArtist());
            intent.putExtra("song_path", song.getPath());
            intent.putExtra("song_duration", song.getDuration());
            intent.putExtra("song_list", new Gson().toJson(songList)); // truyền JSON
            intent.putExtra("current_index", position);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return songList.size();
    }

    public static class SongViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvArtist, tvDuration;
        ImageView imgCover, imgFavorite;

        public SongViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tv_Title);
            tvArtist = itemView.findViewById(R.id.tv_Artist);
            tvDuration = itemView.findViewById(R.id.tv_Duration);
            imgCover = itemView.findViewById(R.id.imgCover);
            imgFavorite = itemView.findViewById(R.id.imgFavorite);
        }
    }

    private String formatTime(long durationMillis) {
        int minutes = (int) (durationMillis / 1000 / 60);
        int seconds = (int) ((durationMillis / 1000) % 60);
        return String.format("%02d:%02d", minutes, seconds);
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
}
