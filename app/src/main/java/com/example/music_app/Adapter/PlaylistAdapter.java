package com.example.music_app.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.music_app.R;
import com.example.music_app.entity.PlayList;

import java.util.List;

public class PlaylistAdapter extends RecyclerView.Adapter<PlaylistAdapter.PlaylistViewHolder> {

    private List<PlayList> playlists;
    private final OnPlaylistClickListener listener;

    public interface OnPlaylistClickListener {
        void onClick(PlayList playlist);
    }

    public PlaylistAdapter(List<PlayList> playlists, OnPlaylistClickListener listener) {
        this.playlists = playlists;
        this.listener = listener;
    }

    public void setPlaylists(List<PlayList> playlists) {
        this.playlists = playlists;
        notifyDataSetChanged();
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
    }

    @Override
    public int getItemCount() {
        return playlists != null ? playlists.size() : 0;
    }

    static class PlaylistViewHolder extends RecyclerView.ViewHolder {
        TextView playlistName;
        public PlaylistViewHolder(@NonNull View itemView) {
            super(itemView);
            playlistName = itemView.findViewById(R.id.tvPlaylistName);
        }
    }
}

