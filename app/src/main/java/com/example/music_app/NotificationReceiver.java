package com.example.music_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class NotificationReceiver extends BroadcastReceiver {

    public static MediaPlayer mediaPlayer;
    public static PlayerActivity playerActivity;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null || mediaPlayer == null) return;

        switch (action) {
            case "ACTION_PAUSE":
                if (mediaPlayer.isPlaying()) mediaPlayer.pause();
                break;
            case "ACTION_PLAY":
                if (!mediaPlayer.isPlaying()) mediaPlayer.start();
                break;
            case "ACTION_NEXT":
                if (playerActivity != null) {
                    int nextIndex = (playerActivity.currentIndex + 1) % playerActivity.songList.size();
                    playerActivity.loadSong(nextIndex);
                }
                break;

            case "ACTION_PREV":
                if (playerActivity != null) {
                    int prevIndex = (playerActivity.currentIndex - 1 + playerActivity.songList.size()) % playerActivity.songList.size();
                    playerActivity.loadSong(prevIndex);
                }
                break;
        }

        // Gửi broadcast cập nhật icon
        Intent updateIntent = new Intent("UPDATE_PLAY_PAUSE_ICON");
        LocalBroadcastManager.getInstance(context).sendBroadcast(updateIntent);

        if (playerActivity != null) {
            playerActivity.showNotification(); // Cập nhật lại notification
        }
    }
}
