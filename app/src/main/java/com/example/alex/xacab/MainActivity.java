package com.example.alex.xacab;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Queue;

/**
 * @TODO: Handle orientation change
 * @TODO: Async queries
 *
 */

public class MainActivity extends Activity implements SelectionListener {

    public final static String INTENT_SONG_STATUS = "com.example.alex.xacab.SONG_STOPPED";
    protected Context context;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Intent musicServiceIntent;
    public QueueDB db;
    public final static String INTENT_EXTRA = "SONG_SOURCE";
    private LibraryFragment mLibraryFragment;
    private ArtistFragment mArtistFragment;
    private ActionBar mActionBar;
    public final String ARTIST_TAG = "artist_tag";
    private IntentFilter mIntentFilter;
    private RelativeLayout mButtonsContainer;
    private ArrayList<String> queueData = new ArrayList<>();
    public static int currentQueuePosition = -1;
    private Menu mMenu;

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == INTENT_SONG_STATUS) {
                String receiveValue = intent.getStringExtra(MusicService.SONG_STATUS);
                if (receiveValue.equals(MusicService.SONG_STARTED)) {
                    Toast.makeText(context, "Song started", Toast.LENGTH_SHORT).show();
                } else if (receiveValue.equals(MusicService.SONG_STOPPED)) {
                    Toast.makeText(context, "Song stopped", Toast.LENGTH_SHORT).show();
                }

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mButtonsContainer = (RelativeLayout) findViewById(R.id.main_buttons_container);
        changeButtons(R.layout.container_main, R.id.container_main_buttons);
        mActionBar = getActionBar();
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(INTENT_SONG_STATUS);
        db = new QueueDB(this);
        populateQueueData();
        musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
        //mLibraryFragment = (LibraryFragment) getFragmentManager().findFragmentById(R.id.fragment_library);
        //mArtistFragment = (ArtistFragment) getFragmentManager().findFragmentById(R.id.fragment_artist_list_item);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_activity_container, new LibraryFragment());
            // No need to add to the backstack since it's the first fragment to load
            transaction.commit();
        } else {

        }


    }

    private void populateQueueData() {
        Cursor cursor = getContentResolver().query(QueueProvider.CONTENT_URI, new String[] { QueueDB.KEY_DATA }, null, null, null);
        while (cursor.moveToNext()) {
            queueData.add(cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_DATA)));
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

    private class AddToQueueTask extends AsyncTask<AudioListModel, Void, Void> {

        @Override
        protected Void doInBackground(AudioListModel... params) {

            AudioListModel item = params[0];

            ContentValues values = new ContentValues();
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

            queueData.add(item.getData());

            return null;
        }

    }

    private void clearQueue() {
        new ClearQueueTask().execute();
    }

    private class ClearQueueTask extends AsyncTask<Void, Void, Void> {


        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(QueueProvider.CONTENT_URI, null, null);
            queueData.clear();
            return null;
        }
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
        String data = queueData.get(position);
        currentQueuePosition = position;
        getContentResolver().notifyChange(QueueProvider.CONTENT_URI, null);
        musicServiceIntent.putExtra(INTENT_EXTRA, data);
        startService(musicServiceIntent);
    }

    @Override
    public void onQueueFragmentShow() {
        changeButtons(R.layout.container_queue, R.id.container_queue_buttons);
        mMenu.findItem(R.id.action_clear_queue).setVisible(true);
    }

    @Override
    public void onQueueFragmentHide() {
        changeButtons(R.layout.container_main, R.id.container_main_buttons);
        mMenu.findItem(R.id.action_clear_queue).setVisible(false);
    }

    private void changeButtons(int layoutId, int id) {
        LayoutInflater inflater = getLayoutInflater();
        View queueLayout = inflater.inflate(layoutId, null);
        View buttons = queueLayout.findViewById(id);
        ((ViewGroup) buttons.getParent()).removeView(buttons);
        mButtonsContainer.removeAllViews();
        mButtonsContainer.addView(buttons);
    }

    public void openQueueFragment() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        QueueFragment queueFragment = (QueueFragment) getFragmentManager().findFragmentByTag("QUEUE");
        if (queueFragment == null) {
            queueFragment = new QueueFragment();
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_activity_container, queueFragment, "QUEUE");
            transaction.addToBackStack(null);
            transaction.commit();
        } else {
            onBackPressed();
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
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
