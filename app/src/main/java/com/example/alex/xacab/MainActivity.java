package com.example.alex.xacab;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


public class MainActivity extends Activity implements SelectionListener {

    public final static String INTENT_SONG_STATUS = "com.example.alex.xacab.SONG_STOPPED";
    public final static int MAX_PROGRESS = 200;
    public final static String INTENT_EXTRA = "SONG_SOURCE";
    public final static String INTENT_DURATION = "SONG_DURATION";
    public final static String CURRENT_SONG_PREFERENCE = "currentSong";
    public static final int NUM_PAGES = 3;
    public static String songStatus = MusicService.SONG_STOPPED;
    public QueueDB db;
    private int currentQueuePosition = -1;
    private boolean isPlaying = false;
    private Intent musicServiceIntent;
    private LibraryFragment mLibraryFragment;
    private ArtistFragment mArtistFragment;
    private QueueFragment mQueueFragment;
    private IntentFilter mIntentFilter;
    private RelativeLayout mButtonsContainer;
    private ArrayList<AudioListModel> mQueueData = new ArrayList<>();
    private Menu mMenu;
    private View mButtons;
    private SeekBar mSeekBar;
    private PagerAdapter mPager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(INTENT_SONG_STATUS)) {
                String receiveValue = intent.getStringExtra(MusicService.SONG_STATUS);
                if (receiveValue.equals(MusicService.SONG_STARTED)) {
                    isPlaying = true;
                    changePlayStatus(MusicService.SONG_STARTED);
                    int position = intent.getIntExtra(MusicService.SONG_POSITION, -1);
                    int step = mQueueData.get(currentQueuePosition).getDuration() / MAX_PROGRESS;
                    if (position != -1) {
                        mSeekBar.setProgress(position/step);
                    }
                    new SeekBarRefresh().execute(step);
                } else if (receiveValue.equals(MusicService.SONG_STOPPED)) {
                    isPlaying = false;
                    changePlayStatus(MusicService.SONG_STOPPED);
                }

            }
        }
    };

    public int getCurrentQueuePosition() {
        return currentQueuePosition;
    }

    public void setCurrentQueuePosition(int currentQueuePosition) {
        this.currentQueuePosition = currentQueuePosition;

    }

    private void changePlayStatus(String status) {
        ImageView playButton = (ImageView) mButtons.findViewById(R.id.player_play);
        if (!songStatus.equals(status)) {
            songStatus = status;
        }
        if (status.equals(MusicService.SONG_STOPPED)) {
            playButton.setImageResource(R.drawable.ic_action_play);
        } else if (status.equals(MusicService.SONG_STARTED)) {
            playButton.setImageResource(R.drawable.ic_action_pause);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences preferences = getPreferences(0);
        setFragments();
        mButtonsContainer = (RelativeLayout) findViewById(R.id.main_buttons_container);
        changeButtons(R.layout.container_queue, R.id.container_queue_buttons);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(INTENT_SONG_STATUS);
        db = new QueueDB(this);
        musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
        populateQueueData();
        mSeekBar = (SeekBar) findViewById(R.id.player_slider);
        mSeekBar.setMax(MAX_PROGRESS);
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if (currentQueuePosition != -1) {
                    int duration = mQueueData.get(currentQueuePosition).getDuration();
                    int newDuration = duration * seekBar.getProgress() / MAX_PROGRESS;
                    Intent intent = new Intent(getApplicationContext(), MusicService.class);
                    intent.putExtra(INTENT_DURATION, newDuration);
                    startService(intent);
                }

            }
        });

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_activity_container, mQueueFragment);
           // transaction.replace(R.id.drawer_container, mQueueFragment);
            //transaction.addToBackStack(null);
            // No need to add to the backstack since it's the first fragment to load
            transaction.commit();
        }
    }

    private void setFragments() {

        mArtistFragment = new ArtistFragment();
//        mArtistFragment.setEnterTransition(mFade);
//        mArtistFragment.setExitTransition(mFade);
//        mArtistFragment.setReenterTransition(mFade);
//        mArtistFragment.setReturnTransition(mFade);
        mQueueFragment = new QueueFragment();
//        mQueueFragment.setEnterTransition(mFade);
//        mQueueFragment.setExitTransition(mFade);
//        mQueueFragment.setReenterTransition(mFade);
//        mQueueFragment.setReturnTransition(mFade);
        mLibraryFragment = new LibraryFragment();
//        mLibraryFragment.setEnterTransition(mFade);
//        mLibraryFragment.setExitTransition(mFade);
//        mLibraryFragment.setReenterTransition(mFade);
//        mLibraryFragment.setReturnTransition(mFade);
    }

    private void populateQueueData() {
        String[] columns = AudioListModel.getColumns();
        Cursor cursor = getContentResolver().query(QueueProvider.CONTENT_URI, columns, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String artist = cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_ARTIST));
                String album = cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_ALBUM));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_TITLE));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_DATA));
                int duration = cursor.getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_DURATION));
                int number = cursor.getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_NUMBER));
                int year = cursor.getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_YEAR));
                long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(QueueDB.KEY_ALBUM_ID));
                long trackId = cursor.getLong(cursor.getColumnIndexOrThrow(QueueDB.KEY_TRACK_ID));
                AudioListModel song = new AudioListModel(artist, album, title, data, duration, number, year, albumId, trackId);
                mQueueData.add(song);
            }
            cursor.close();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mBroadcastReceiver, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver);
    }

    public void onItemSelected(View item) {
/*
        String filePath = ((TextView) item).getText().toString();
        musicServiceIntent.putExtra(INTENT_EXTRA, filePath);
        startService(musicServiceIntent);
*/
    }

    @Override
    public void onArtistItemSelected(AudioListModel item) {
        new AddToQueueTask().execute(item);
    }

    private void clearQueue() {
        new ClearQueueTask().execute();

    }

    public void onLibraryItemSelected(View item) {
        TextView vArtist = (TextView) item.findViewById(R.id.library_artist);
        String artist = (String) vArtist.getText();
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mArtistFragment = new ArtistFragment();
        mArtistFragment.setArtist(artist);
        transaction.replace(R.id.main_activity_container, mArtistFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onQueueItemSelected(int position) {
        String data = mQueueData.get(position).getData();
        currentQueuePosition = position;
        musicServiceIntent.putExtra(INTENT_EXTRA, data);
        startService(musicServiceIntent);
        getContentResolver().notifyChange(QueueProvider.CONTENT_URI, null);
    }

    @Override
    public void onQueueFragmentShow() {
        changeButtons(R.layout.container_queue, R.id.container_queue_buttons);
        mMenu.findItem(R.id.action_clear_queue).setVisible(true);
        mMenu.findItem(R.id.action_add).setVisible(true);
    }

    @Override
    public void onQueueFragmentHide() {
        changeButtons(R.layout.container_main, R.id.container_main_buttons);
        mMenu.findItem(R.id.action_clear_queue).setVisible(false);
        mMenu.findItem(R.id.action_add).setVisible(false);
    }

    private void changeButtons(int layoutId, int id) {
        LayoutInflater inflater = getLayoutInflater();
        View queueLayout = inflater.inflate(layoutId, null);
        View buttons = queueLayout.findViewById(id);
        mButtons = buttons;
        ImageView playButton = (ImageView) buttons.findViewById(R.id.player_play);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicServiceIntent.removeExtra(INTENT_EXTRA);
                startService(musicServiceIntent);
            }
        });
        changePlayStatus(songStatus);
        ((ViewGroup) buttons.getParent()).removeView(buttons);
        mButtonsContainer.removeAllViews();
        mButtonsContainer.addView(buttons);
    }

    public void openQueueFragment() {
        if (getFragmentManager().findFragmentByTag("QUEUE") == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_activity_container, mQueueFragment, "QUEUE");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            onBackPressed();
        }

    }

    public void openLibraryFragment() {
        if (getFragmentManager().findFragmentByTag("LIBRARY") == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_activity_container, mLibraryFragment, "LIBRARY");
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_queue:
                openQueueFragment();
                break;
            case R.id.action_clear_queue:
                clearQueue();
                break;
            case R.id.action_add:
                openLibraryFragment();
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private class AddToQueueTask extends AsyncTask<AudioListModel, Void, Void> {

        @Override
        protected Void doInBackground(AudioListModel... params) {

            AudioListModel item = params[0];
            ArrayList<AudioListModel> tracksToAdd;
            ContentValues values = new ContentValues();
            if (item.isAlbum) {
                //String[] from = AudioListModel.getColumns();
                String[] from = new String[] {
                        MediaStore.Audio.Media.TITLE,
                        MediaStore.Audio.Media.ALBUM,
                        MediaStore.Audio.Media.ALBUM_ID,
                        MediaStore.Audio.Media.YEAR,
                        MediaStore.Audio.Media.ARTIST,
                        MediaStore.Audio.Media.DATA,
                        MediaStore.Audio.Media.DURATION,
                        MediaStore.Audio.Media.TRACK,
                        MediaStore.Audio.Media._ID
                };
                String selection = MediaStore.Audio.Media.ALBUM_ID + "=?";
                Long aId = item.getAlbumId();
                String[] where = {aId.toString()};
                Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, from, selection, where, null);
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        values.clear();
                        String artist = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST));
                        String album = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM));
                        String title = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE));
                        String data = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA));
                        int duration = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION));
                        int number = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK));
                        int year = cursor.getInt(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR));
                        long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID));
                        long trackId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                        AudioListModel newItem = new AudioListModel(artist, album, title, data, duration, number, year, albumId, trackId);
                        values.put(QueueDB.KEY_ARTIST, newItem.getArtist());
                        values.put(QueueDB.KEY_ALBUM, newItem.getAlbum());
                        values.put(QueueDB.KEY_TITLE, newItem.getTitle());
                        values.put(QueueDB.KEY_DATA, newItem.getData());
                        values.put(QueueDB.KEY_DURATION, newItem.getDuration());
                        values.put(QueueDB.KEY_NUMBER, newItem.getNumber());
                        values.put(QueueDB.KEY_YEAR, newItem.getYear());
                        values.put(QueueDB.KEY_ALBUM_ID, newItem.getAlbumId());
                        values.put(QueueDB.KEY_TRACK_ID, newItem.getTrackId());

                        getContentResolver().insert(QueueProvider.CONTENT_URI, values);
                        mQueueData.add(newItem);
                    }
                }
            } else {
                values.put(QueueDB.KEY_ARTIST, item.getArtist());
                values.put(QueueDB.KEY_ALBUM, item.getAlbum());
                values.put(QueueDB.KEY_TITLE, item.getTitle());
                values.put(QueueDB.KEY_DATA, item.getData());
                values.put(QueueDB.KEY_DURATION, item.getDuration());
                values.put(QueueDB.KEY_NUMBER, item.getNumber());
                values.put(QueueDB.KEY_YEAR, item.getYear());
                values.put(QueueDB.KEY_ALBUM_ID, item.getAlbumId());
                values.put(QueueDB.KEY_TRACK_ID, item.getTrackId());

                getContentResolver().insert(QueueProvider.CONTENT_URI, values);
                mQueueData.add(item);
            }

            return null;
        }

    }

    private class ClearQueueTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(QueueProvider.CONTENT_URI, null, null);
            mQueueData.clear();
            currentQueuePosition = -1;
            return null;
        }
    }

    private class SeekBarRefresh extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... params) {
            Integer step = params[0];
            while (isPlaying) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mSeekBar.setProgress(mSeekBar.getProgress() + 1);
                    }
                });
                try {
                    Thread.sleep(step);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }
    }


}
