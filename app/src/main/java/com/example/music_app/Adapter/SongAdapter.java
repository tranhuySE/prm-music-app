package com.example.music_app.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.music_app.AppDatabase;
import com.example.music_app.DAO.SongDao;
import com.example.music_app.DAO.SongFavoriteDao;
import com.example.music_app.PlayerActivity;
import com.example.music_app.R;
import com.example.music_app.auth.SessionManager;
import com.example.music_app.entity.Song;
import com.example.music_app.entity.SongFavorite;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.util.List;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.SongViewHolder> {

    private final List<Song> songList;
    private final Context context;
    private  final SongDao songDao;

    private final SongFavoriteDao favoriteDao;
    private final String currentUser;

    public SongAdapter(List<Song> songList, Context context) {
        this.songList = songList;
        this.context = context;
        AppDatabase db = AppDatabase.getInstance(context);
        this.songDao = db.songDao();
        SessionManager sessionManager = new SessionManager(context);
        this.currentUser = sessionManager.getUsername();
        this.favoriteDao = db.songFavoriteDao();
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
        holder.tvDuration.setText(formatTime((int) song.getDuration()));

        // Load ảnh cover nếu có
        if (song.getImgUrl() != null && !song.getImgUrl().isEmpty()) {
            // Nếu có ảnh từ internet => dùng Glide hoặc Picasso để load ảnh
            Glide.with(context).load(song.getImgUrl()).into(holder.imgCover);
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
                                .into(holder.imgCover);
                    });
                }
            }).start();
        }
        new Thread(() -> {
            boolean isFav = favoriteDao.isFavorite(currentUser, song.getPath());
            song.setFavorite(isFav);
            ((Activity) context).runOnUiThread(() -> {
                holder.imgFavorite.setImageResource(isFav ? R.drawable.ic_favorite_outline : R.drawable.ic_star);
            });
        }).start();

        holder.imgFavorite.setOnClickListener(v -> {
            boolean newFavoriteState = !song.isFavorite();
            song.setFavorite(newFavoriteState);

            new Thread(() -> {
                if (newFavoriteState) {
                    favoriteDao.insert(new SongFavorite(currentUser, song.getPath()));
                } else {
                    favoriteDao.deleteByUsernameAndPath(currentUser, song.getPath());
                }
                ((Activity) context).runOnUiThread(() -> {
                    String message = newFavoriteState ? "Đã thêm vào yêu thích 💛" : "Đã bỏ khỏi yêu thích 🤍";
                    Snackbar.make(holder.itemView, message, Snackbar.LENGTH_SHORT).show();
                    notifyItemChanged(position);
                });
            }).start();
        });

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

    private String formatTime(int durationSeconds) {
        int minutes = durationSeconds / 60;
        int seconds = durationSeconds % 60;
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
