package com.whale.xacab;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

import java.io.IOException;

public class MusicService extends Service implements MediaPlayer.OnPreparedListener {

    public static final String SONG_STATUS = "SONG_STATUS";
    public static final String SONG_POSITION = "SONG_POSITION";
    public static final String SONG_STOPPED = "SONG_STOPPED";
    public static final String SONG_STARTED = "SONG_STARTED";
    private final String TAG = "MusicService";
    private MediaPlayer mediaPlayer;
    private int mStartID;
    private String currentSong;
    private int mDuration;
    private Intent mBroadcastIntent;


    @Override
    public void onCreate() {
        super.onCreate();

        mBroadcastIntent = new Intent();
        mBroadcastIntent.setAction(MainActivity.INTENT_SONG_STATUS);
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnPreparedListener(this);

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                songStopped();
                nextSong();
            }
        });
    }

    private void songStopped() {
        mBroadcastIntent.putExtra(SONG_STATUS, SONG_STOPPED);
        sendBroadcast(mBroadcastIntent);
    }

    private void songStarted() {
        mBroadcastIntent.putExtra(SONG_STATUS, SONG_STARTED);
        mBroadcastIntent.putExtra(SONG_POSITION, mediaPlayer.getCurrentPosition());
        sendBroadcast(mBroadcastIntent);
    }

    private void getPosition() {
        mBroadcastIntent.putExtra(SONG_POSITION, mediaPlayer.getCurrentPosition());
        sendBroadcast(mBroadcastIntent);
    }

    private void nextSong() {
        Intent intent = new Intent();
        intent.setAction(MainActivity.INTENT_SONG_NEXT);
        sendBroadcast(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (mediaPlayer != null) {
            mStartID = startId;
            String songSource = intent.getStringExtra(MainActivity.INTENT_EXTRA);
            boolean getPosition = intent.getBooleanExtra(MainActivity.INTENT_GET_POSITION, false);
            int duration = intent.getIntExtra(MainActivity.INTENT_DURATION, -1);
            if (getPosition) {
                getPosition();
            } else {
                try {
                    if (mediaPlayer.isPlaying()) {
                        if (duration != -1) {
                            mediaPlayer.seekTo(duration);
                            mediaPlayer.start();
                            songStarted();
                        } else if (songSource == null) {
                            mediaPlayer.pause();
                            songStopped();
                        } else if (!songSource.equals(currentSong)) {
                            mediaPlayer.stop();
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(songSource);
                            mediaPlayer.prepareAsync();
                            currentSong = songSource;
                        } else {
                            mediaPlayer.pause();
                            songStopped();
                        }
                    } else {
                        if (duration != -1) {
                            mediaPlayer.seekTo(duration);
                        } else if (songSource == null) {
                            mediaPlayer.start();
                            songStarted();
                        } else if (songSource.equals(currentSong)) {
                            mediaPlayer.start();
                            songStarted();
                        } else {
                            mediaPlayer.reset();
                            mediaPlayer.setDataSource(songSource);
                            mediaPlayer.prepareAsync();
                            currentSong = songSource;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {

        if (mediaPlayer != null) {

            mediaPlayer.stop();
            mediaPlayer.release();
            songStopped();

        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        songStarted();
    }

}
