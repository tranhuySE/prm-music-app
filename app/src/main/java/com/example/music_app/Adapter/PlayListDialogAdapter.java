package com.example.music_app.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.example.music_app.R;
import com.example.music_app.entity.PlayList;

import java.util.List;

public class PlayListDialogAdapter extends ArrayAdapter<PlayList> {
    public PlayListDialogAdapter(@NonNull Context context, List<PlayList> playlists) {
        super(context, 0, playlists);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_playlist_dialog, parent, false);
        }

        PlayList playlist = getItem(position);
        TextView name = convertView.findViewById(R.id.tvPlaylistName);
        name.setText(playlist.getName());
        return convertView;
    }
}
