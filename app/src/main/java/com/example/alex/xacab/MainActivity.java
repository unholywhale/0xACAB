package com.example.alex.xacab;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

/**
 * @TODO: Handle orientation change
 * @TODO: Async queries
 *
 */

public class MainActivity extends Activity implements SelectionListener {

    public final static String INTENT_SONG_COMPLETED = "com.example.alex.xacab.SONG_COMPLETED";
    protected Context context;
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Intent musicServiceIntent;
    public QueueDB db;
    public final static String INTENT_EXTRA = "SONG_SOURCE";
    private LibraryFragment mLibraryFragment;
    private ArtistFragment mArtistFragment;
    private ActionBar mActionBar;
    public final String ARTIST_TAG = "artist_tag";

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == INTENT_SONG_COMPLETED) {
                String receiveValue = MusicService.SONG_COMPLETED;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mActionBar = getActionBar();
        db = new QueueDB(this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    private class AddToQueueTask extends AsyncTask<AudioListModel, Void, Boolean> {

        @Override
        protected Boolean doInBackground(AudioListModel... params) {
            AudioListModel item = params[0];
            Boolean result = db.addToQueue(item);

            return result;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result == true) {

            }
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
    public void onQueueItemSelected(TextView viewData) {
        String data = viewData.getText().toString();
        musicServiceIntent.putExtra(INTENT_EXTRA, data);
        startService(musicServiceIntent);
    }

    public void openQueueFragment() {
        mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        QueueFragment queueFragment = new QueueFragment();
        transaction.replace(R.id.main_activity_container, queueFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_queue) {
            openQueueFragment();
        }
        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }
}
