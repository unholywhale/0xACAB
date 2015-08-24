package com.example.alex.xacab;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.media.MediaPlayer;
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

    public final static String[] LIST_SAMPLE = { "First item", "Second item", "Third item" };
    private MediaPlayer mediaPlayer = new MediaPlayer();
    private Intent musicServiceIntent;
    public final static String INTENT_EXTRA = "SONG_SOURCE";
    private LibraryFragment mLibraryFragment;
    private ArtistFragment mArtistFragment;
    public final String ARTIST_TAG = "artist_tag";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
        //mLibraryFragment = (LibraryFragment) getFragmentManager().findFragmentById(R.id.fragment_library);
        //mArtistFragment = (ArtistFragment) getFragmentManager().findFragmentById(R.id.fragment_artist_list_item);
        if (savedInstanceState == null) {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.main_activity_container, new LibraryFragment());
            transaction.addToBackStack(null);
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
    public void onArtistItemSelected(View item) {

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
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
