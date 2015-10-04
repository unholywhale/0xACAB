package com.example.alex.xacab;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {

    public static final String SONG_COMPLETED = "SONG_COMPLETED";
    private final String TAG = "MusicService";
    private MediaPlayer mediaPlayer;
    private int mStartID;
    private String currentSong;


    @Override
    public void onCreate() {
        super.onCreate();


        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

            }
        });
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaPlayer != null) {
            mStartID = startId;
            String songSource = intent.getStringExtra(MainActivity.INTENT_EXTRA);

            try {
                if (mediaPlayer.isPlaying()) {
                    if (!songSource.equals(currentSong)) {
                        mediaPlayer.stop();
                        mediaPlayer.reset();
                        mediaPlayer.setDataSource(songSource);
                        mediaPlayer.prepareAsync();
                        currentSong = songSource;
                    } else {
                        mediaPlayer.pause();
                    }
                } else {
                    if (songSource.equals(currentSong)) {
                        mediaPlayer.start();
                    } else {
                        mediaPlayer.setDataSource(songSource);
                        mediaPlayer.prepare();
                        mediaPlayer.start();
                        currentSong = songSource;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.release();

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
    }

}
