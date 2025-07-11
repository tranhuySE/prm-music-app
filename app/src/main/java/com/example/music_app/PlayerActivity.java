package com.example.music_app;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.music_app.NotificationReceiver;

import com.bumptech.glide.Glide;
import com.example.music_app.entity.Song;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PlayerActivity extends AppCompatActivity {

    private TextView tvTitle, tvArtist, tvCurrentTime, tvDuration;
    private SeekBar seekBar;
    private ImageView imgCover, btnBack, btnPrev, btnStop, btnNext, btnLoop;
    private MediaPlayer mediaPlayer;
    private Handler handler = new Handler();
    private Runnable updateSeekBar;
    private boolean isLooping = false;

    private String songPath;
    private String songArtirt;
    List<Song> songList;
    int currentIndex;
    private static final String CHANNEL_ID = "music_channel";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1000);
            }
        }
        initView();
        String jsonList = getIntent().getStringExtra("song_list");
        currentIndex = getIntent().getIntExtra("current_index", 0);
        songList = new Gson().fromJson(jsonList, new TypeToken<List<Song>>() {}.getType());

        songPath = getIntent().getStringExtra("song_path");
        songArtirt = getIntent().getStringExtra("song_artist");
        File file = new File(songPath);
        tvTitle.setText(file.getName());
        tvArtist.setText(songArtirt);

        setupMediaPlayer();
        try {
            displaySongImage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        createNotificationChannel();
        showNotification();

        LocalBroadcastManager.getInstance(this).registerReceiver(playPauseReceiver,
                new IntentFilter("UPDATE_PLAY_PAUSE_ICON"));

        btnBack.setOnClickListener(v -> finish());

        btnStop.setOnClickListener(v -> {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.pause();
                btnStop.setImageResource(R.drawable.ic_play);
            } else {
                mediaPlayer.start();
                btnStop.setImageResource(R.drawable.ic_stop);
            }
            showNotification();
        });

        btnLoop.setOnClickListener(v -> {
            isLooping = !isLooping;
            mediaPlayer.setLooping(isLooping);
            Toast.makeText(this, isLooping ? "Bật lặp lại" : "Tắt lặp lại", Toast.LENGTH_SHORT).show();
        });

        btnNext.setOnClickListener(v -> {
            int nextIndex = (currentIndex + 1) % songList.size();
            loadSong(nextIndex);
        });

        btnPrev.setOnClickListener(v -> {
            int prevIndex = (currentIndex - 1 + songList.size()) % songList.size();
            loadSong(prevIndex);
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && mediaPlayer != null) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private final BroadcastReceiver playPauseReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (mediaPlayer != null) {
                // Đồng bộ icon trên màn hình chính
                btnStop.setImageResource(mediaPlayer.isPlaying() ? R.drawable.ic_stop : R.drawable.ic_play);
            }
        }
    };

    void loadSong(int index) {
        currentIndex = index;
        Song song = songList.get(index);
        song.incrementPlayCount();
        songPath = song.getPath();
        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
        } else {
            mediaPlayer = new MediaPlayer();
        }

        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            mediaPlayer.setOnCompletionListener(mp -> {
                if (!isLooping) {
                    int nextIndex = (currentIndex + 1) % songList.size();
                    loadSong(nextIndex);
                } else {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            });

            NotificationReceiver.mediaPlayer = mediaPlayer;
            NotificationReceiver.playerActivity = this;

            seekBar.setMax(mediaPlayer.getDuration());
            tvDuration.setText(formatTime(mediaPlayer.getDuration()));

            handler.removeCallbacks(updateSeekBar);
            updateSeekBar = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                        handler.postDelayed(this, 500);
                    }
                }
            };
            handler.post(updateSeekBar);

            displaySongImage();
            showNotification();

        } catch (IOException e) {
            Toast.makeText(this, "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
        }
    }

    private void initView() {
        tvTitle = findViewById(R.id.tv_Title);
        tvArtist = findViewById(R.id.tv_Artist);
        tvCurrentTime = findViewById(R.id.tv_CurrentTime);
        tvDuration = findViewById(R.id.tv_Duration);
        seekBar = findViewById(R.id.seek_Bar);
        imgCover = findViewById(R.id.imgCover);
        btnBack = findViewById(R.id.btn_Back);
        btnPrev = findViewById(R.id.btn_Prev);
        btnStop = findViewById(R.id.btn_Stop);
        btnNext = findViewById(R.id.btn_Next);
        btnLoop = findViewById(R.id.btn_Loop);
    }

    private void setupMediaPlayer() {
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            NotificationReceiver.mediaPlayer = mediaPlayer;
            NotificationReceiver.playerActivity = this;
            songList.get(currentIndex).incrementPlayCount();
            mediaPlayer.setOnCompletionListener(mp -> {
                if (!isLooping) {
                    int nextIndex = (currentIndex + 1) % songList.size();
                    loadSong(nextIndex);
                } else {
                    mediaPlayer.seekTo(0);
                    mediaPlayer.start();
                }
            });

            seekBar.setMax(mediaPlayer.getDuration());
            tvDuration.setText(formatTime(mediaPlayer.getDuration()));

            updateSeekBar = new Runnable() {
                @Override
                public void run() {
                    if (mediaPlayer != null) {
                        seekBar.setProgress(mediaPlayer.getCurrentPosition());
                        tvCurrentTime.setText(formatTime(mediaPlayer.getCurrentPosition()));
                        handler.postDelayed(this, 500);
                    }
                }
            };
            handler.post(updateSeekBar);

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Lỗi phát nhạc", Toast.LENGTH_SHORT).show();
        }
    }

    private void displaySongImage() throws IOException {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(songPath);
            byte[] art = retriever.getEmbeddedPicture();
            if (art != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(art, 0, art.length);
                Glide.with(this)
                        .load(bitmap)
                        .circleCrop()
                        .into(imgCover);
            } else {
                Glide.with(this)
                        .load(R.drawable.default_cover)
                        .circleCrop()
                        .into(imgCover);
            }

            RotateAnimation rotate = new RotateAnimation(0, 360,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF, 0.5f);
            rotate.setDuration(10000);
            rotate.setRepeatCount(Animation.INFINITE);
            rotate.setInterpolator(new LinearInterpolator());
            imgCover.startAnimation(rotate);
        } catch (Exception e) {
            imgCover.setImageResource(R.drawable.default_cover);
        } finally {
            retriever.release();
        }
    }

    private String formatTime(int millis) {
        int minutes = millis / 1000 / 60;
        int seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    void showNotification() {
        Intent prevIntent = new Intent(this, NotificationReceiver.class).setAction("ACTION_PREV");
        PendingIntent prevPending = PendingIntent.getBroadcast(this, 3, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        boolean isPlaying = mediaPlayer != null && mediaPlayer.isPlaying();

        Intent pauseIntent = new Intent(this, NotificationReceiver.class)
                .setAction("ACTION_PAUSE");
        Intent playIntent = new Intent(this, NotificationReceiver.class)
                .setAction("ACTION_PLAY");
        Intent nextIntent = new Intent(this, NotificationReceiver.class)
                .setAction("ACTION_NEXT");

        PendingIntent playPending = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent pausePending = PendingIntent.getBroadcast(this, 1, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        PendingIntent nextPending = PendingIntent.getBroadcast(this, 2, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_star)
                .setContentTitle("Đang phát: " + tvTitle.getText())
                .setContentText("Soundy")
                .addAction(R.drawable.ic_prev, "Prev", prevPending)
                .addAction(isPlaying ? R.drawable.ic_stop : R.drawable.ic_play,
                        isPlaying ? "Pause" : "Play",
                        isPlaying ? pausePending : playPending)
                .addAction(R.drawable.ic_next, "Next", nextPending)
                .setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(1)  // Hiển thị icon Play/Pause trong compact view
                )
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(isPlaying);

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(1, builder.build());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            handler.removeCallbacks(updateSeekBar);
            mediaPlayer.release();
            mediaPlayer = null;
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.cancel(1);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(playPauseReceiver);
    }
}
