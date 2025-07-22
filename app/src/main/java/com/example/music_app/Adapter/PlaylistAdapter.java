package com.example.music_app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.R;
import com.example.music_app.entity.PlayList;
import com.example.music_app.entity.Song;
import android.content.Context;
import android.content.Intent;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<PlayList> playlists;
    private final OnPlaylistClickListener listener;
    private OnPlaylistShareListener shareListener;

    public interface OnPlaylistClickListener {
        void onClick(PlayList playlist);
    }

    public interface OnPlaylistShareListener {
        void onShare(PlayList playlist);
    }

    public PlaylistAdapter(List<PlayList> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    public void setPlaylists(List<PlayList> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
    }

    public void setOnPlaylistShareListener(OnPlaylistShareListener listener) {
        this.shareListener = listener;
    }

    @NonNull
    @Override
    public PlaylistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_play_list, parent, false);
        return new PlaylistViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistViewHolder holder, int position) {
        PlayList playlist = playlists.get(position);
        holder.playlistName.setText(playlist.getName());
        holder.itemView.setOnClickListener(v -> listener.onClick(playlist));
        // Thêm xử lý chia sẻ khi long click
        holder.itemView.setOnLongClickListener(v -> {
            if (shareListener != null) shareListener.onShare(playlist);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return playlists != null ? playlists.size() : 0;
    }

    // Hàm chia sẻ playlist (có thể gọi từ Activity/Fragment)
    public static void sharePlaylist(Context context, PlayList playlist) {
        StringBuilder builder = new StringBuilder();
        builder.append("Playlist: ").append(playlist.getName()).append("\n");
        if (playlist.getSongs() != null) {
            for (Song song : playlist.getSongs()) {
                builder.append("- ").append(song.getTitle()).append(" - ").append(song.getArtist()).append("\n");
            }
        }
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, builder.toString());
        shareIntent.setType("text/plain");
        context.startActivity(Intent.createChooser(shareIntent, "Chia sẻ playlist qua"));
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView playlistName;
        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.tvPlaylistName);
        }
    }
}

