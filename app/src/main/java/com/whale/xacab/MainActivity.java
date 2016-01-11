package com.whale.xacab;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.BitmapFactory;
import android.media.session.MediaSession;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;
import java.util.Random;


public class MainActivity extends Activity implements SelectionListener {

    public final static String APP_TITLE = "0xACAB";
    public final static String INTENT_SONG_STATUS = "com.whale.xacab.SONG_STOPPED";
    public final static String INTENT_SONG_NEXT = "com.whale.xacab.SONG_NEXT";
    public final static String INTENT_SONG_PREV = "com.whale.xacab.SONG_PREV";
    public final static String INTENT_SONG_PLAY = "com.whale.xacab.SONG_PLAY";
    public final static String INTENT_EXTRA = "SONG_SOURCE";
    public final static String INTENT_DURATION = "SONG_DURATION";
    public final static String CURRENT_QUEUE_POSITION = "CURRENT_QUEUE_POSITION";
    public final static String TAG_QUEUE = "QUEUE";
    public static final String TAG_LIBRARY = "LIBRARY";
    public static final String TAG_FILES = "FILES";
    public final static String TAG_SEEK_BAR = "SEEK_BAR";
    private static final String IS_SHUFFLING = "IS_SHUFFLED";
    private static final String IS_REPEATING = "IS_REPEATING";
    public static final int ADD_NEXT = 0;
    public static final int ADD_LAST = 1;
    public static String songStatus = MusicService.SONG_STOPPED;
    public QueueDB db;
    public boolean isPlaying = false;
    public boolean isShuffling = false;
    public boolean isRepeating = false;
    public boolean isLibrary = true;
    private Integer mCurrentQueuePosition = -1;
    private AudioListModel currentSong;
    private boolean mReorderMode = false;
    private Intent musicServiceIntent;
    private SeekBarFragment mSeekBarFragment;
    private LibraryFragment mLibraryFragment;
    private FilesFragment mFilesFragment;
    private ArtistFragment mArtistFragment;
    private QueueFragment mQueueFragment;
    private IntentFilter mIntentFilter;
    private RelativeLayout mButtonsContainer;
    private ArrayList<AudioListModel> mQueueData = new ArrayList<>();
    private Menu mMenu;
    private View mButtons;
    private MediaSession mSession;
    private boolean mSelectMode = false;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(INTENT_SONG_STATUS)) {
                if (currentSong == null) {
                    return;
                }
                String receiveValue = intent.getStringExtra(MusicService.SONG_STATUS);
                if (receiveValue.equals(MusicService.SONG_STARTED)) {
                    isPlaying = true;
                    changePlayStatus(MusicService.SONG_STARTED);
                    int position = intent.getIntExtra(MusicService.SONG_POSITION, -1);
                    int step = currentSong.getDuration() / 200;
                    if (position != -1) {
//                        mProgress = position;
//                        mSeekBar.setProgress(position / step);
//                        mPlayerProgress.setText(MusicUtils.makeTimeString(getApplicationContext(), mProgress / 1000));
//                        mPlayerDuration.setText(MusicUtils.makeTimeString(getApplicationContext(),
//                                currentSong.getDuration() / 1000));
                        mSeekBarFragment.startTasks(currentSong.getDuration(), position, step, currentSong.getArtist(), currentSong.getTitle());
                    }

                } else if (receiveValue.equals(MusicService.SONG_STOPPED)) {
                    isPlaying = false;
                    changePlayStatus(MusicService.SONG_STOPPED);
                }
                makeNotification();
            } else if (intent.getAction().equals(INTENT_SONG_NEXT)) {
                if (mCurrentQueuePosition != -1) {
                    nextSong();
                }
            } else if (intent.getAction().equals(INTENT_SONG_PREV)) {
                if (mCurrentQueuePosition != -1) {
                    prevSong();
                }
            } else if (intent.getAction().equals(INTENT_SONG_PLAY)) {
                musicServiceIntent.removeExtra(INTENT_EXTRA);
                startService(musicServiceIntent);
            }
        }
    };

    @Override
    public int getCurrentQueuePosition() {
        return mCurrentQueuePosition;
    }

    @Override
    public void setCurrentQueuePosition(int currentQueuePosition) {
        this.mCurrentQueuePosition = currentQueuePosition;
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

    private void nextSong() {
        int position;
        if (mCurrentQueuePosition != -1) {
            if (isShuffling) {
                Random rand = new Random();
                position = rand.nextInt(mQueueData.size());
            } else {
                if (!isRepeating && mCurrentQueuePosition == mQueueData.size() - 1) {
                    return;
                } else if (mCurrentQueuePosition == mQueueData.size() - 1) {
                    position = 0;
                } else {
                    position = mCurrentQueuePosition + 1;
                }
            }
            onQueueItemSelected(position);
        }
    }

    private void prevSong() {
        if (mCurrentQueuePosition == 0) {
            onQueueItemSelected(mQueueData.size() - 1);
        } else {
            onQueueItemSelected(mCurrentQueuePosition - 1);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSession = new MediaSession(this, "SESSION");
        mSession.setActive(true);
        setFragments();
        mButtonsContainer = (RelativeLayout) findViewById(R.id.main_buttons_container);
        changeButtons(R.layout.container_queue, R.id.container_queue_buttons);
        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(INTENT_SONG_STATUS);
        mIntentFilter.addAction(INTENT_SONG_PREV);
        mIntentFilter.addAction(INTENT_SONG_NEXT);
        mIntentFilter.addAction(INTENT_SONG_PLAY);
        db = new QueueDB(this);
        musicServiceIntent = new Intent(getApplicationContext(), MusicService.class);
        populateQueueData();
        if (mCurrentQueuePosition != -1) {
            currentSong = mQueueData.get(mCurrentQueuePosition);
        }
        if (savedInstanceState == null) {
            openSeekBarFragment();
            openQueueFragment();
        }
    }



    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(CURRENT_QUEUE_POSITION, mCurrentQueuePosition);
        outState.putBoolean(IS_SHUFFLING, isShuffling);
        outState.putBoolean(IS_REPEATING, isRepeating);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        mCurrentQueuePosition = savedInstanceState.getInt(CURRENT_QUEUE_POSITION);
        if (mCurrentQueuePosition != -1) {
            currentSong = mQueueData.get(mCurrentQueuePosition);
        }
        isShuffling = savedInstanceState.getBoolean(IS_SHUFFLING);
        isRepeating = savedInstanceState.getBoolean(IS_REPEATING);
        invalidateQueue();
        invalidateButtons();
    }

    private void setFragments() {

        FragmentManager fm = getFragmentManager();
        mSeekBarFragment = (SeekBarFragment) fm.findFragmentByTag(TAG_SEEK_BAR);
        if (mSeekBarFragment == null) {
            mSeekBarFragment = new SeekBarFragment();
        }
        mQueueFragment = (QueueFragment) fm.findFragmentByTag(TAG_QUEUE);
        if (mQueueFragment == null) {
            mQueueFragment = new QueueFragment();
        }
        mArtistFragment = new ArtistFragment();
        mLibraryFragment = new LibraryFragment();
        mFilesFragment = new FilesFragment();

    }

    private void invalidateButtons() {
        ImageView shuffleButton = (ImageView) mButtons.findViewById(R.id.player_shuffle);
        if (shuffleButton != null) {
            if (isShuffling) {
                shuffleButton.setImageResource(R.drawable.ic_action_shuffle);
            } else {
                shuffleButton.setImageResource(R.drawable.ic_action_shuffle_disabled);
            }
        }
        ImageView repeatButton = (ImageView) mButtons.findViewById(R.id.player_repeat);
        if (repeatButton != null) {
            if (isRepeating) {
                repeatButton.setImageResource(R.drawable.ic_action_repeat);
            } else {
                repeatButton.setImageResource(R.drawable.ic_action_repeat_disabled);
            }
        }
    }


    @Override
    public void onSeekBarChanged(int duration) {
        Intent intent = new Intent(getApplicationContext(), MusicService.class);
        intent.putExtra(INTENT_DURATION, duration);
        startService(intent);
    }

    @Override
    public void onQueueAdd() {
        openLibrary(false);
    }

    private void populateQueueData() {
        String[] columns = AudioListModel.getColumns();
        Cursor cursor = getContentResolver().query(QueueProvider.CONTENT_URI, columns, null, null, QueueDB.KEY_SORT);
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

    @Override
    public void onArtistItemSelected(AudioListModel item, int mode) {
        if (mode == ADD_LAST) {
            onArtistItemSelected(item);
            Toast.makeText(this, "Added last", Toast.LENGTH_SHORT);
        } else if (mode == ADD_NEXT) {
            if (mCurrentQueuePosition == -1) {
                onArtistItemSelected(item);
                Toast.makeText(this, "Added last", Toast.LENGTH_SHORT);
                return;
            }
            ArrayList<ContentValues> contentValuesList = new ArrayList<>();
            ContentValues cv;
            String[] columns = AudioListModel.getColumns();
            String where = QueueDB.KEY_SORT + " > " + mCurrentQueuePosition.toString();
            //String[] whereArgs = {mCurrentQueuePosition.toString()};
            Cursor cursor = getContentResolver().query(QueueProvider.CONTENT_URI, columns, where, null, QueueDB.KEY_SORT);
            while (cursor.moveToNext()) {
//                String title = cursor.getString(cursor.getColumnIndexOrThrow(QueueDB.KEY_TITLE));
//                int sort = cursor.getInt(cursor.getColumnIndexOrThrow(QueueDB.KEY_SORT));
                cv = new ContentValues();
                DatabaseUtils.cursorRowToContentValues(cursor, cv);
                contentValuesList.add(cv);
            }
            if (!item.isAlbum) {
                for (ContentValues el : contentValuesList) {
                    Integer i = el.getAsInteger(QueueDB.KEY_SORT);
                    i++;
                    el.remove(QueueDB.KEY_SORT);
                    el.put(QueueDB.KEY_SORT, i);
                    Long id = el.getAsLong(QueueDB.KEY_ID);
                    String selection = QueueDB.KEY_ID + "=?";
                    String[] selectionArgs = {id.toString()};
                    getContentResolver().update(QueueProvider.CONTENT_URI, el, selection, selectionArgs);
                }
                item.setSort(mCurrentQueuePosition + 2);
                onArtistItemSelected(item);
            }

        }
    }

    @Override
    public void onArtistItemSelected(AudioListModel item) {
        new AddToQueueTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, item);
    }

    private void clearQueue() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Clear queue?");
        builder.setPositiveButton("CLEAR", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new ClearQueueTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //blank
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void onLibraryItemSelected(View item) {
        TextView vArtist = (TextView) item.findViewById(R.id.library_artist);
        String artist = (String) vArtist.getText();
        openArtistFragment(artist);
    }

    @Override
    public void setFragmentTitle(String title) {
        setTitle(title);
    }

    public void makeNotification() {
        Intent contentIntent = new Intent(getApplicationContext(), MainActivity.class);
        Intent nextIntent = new Intent();
        nextIntent.setAction(INTENT_SONG_NEXT);
        Intent prevIntent = new Intent();
        prevIntent.setAction(INTENT_SONG_PREV);
        Intent playIntent = new Intent();
        playIntent.setAction(INTENT_SONG_PLAY);

        PendingIntent pendingContentIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingNextIntent = PendingIntent.getBroadcast(this, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingPrevIntent = PendingIntent.getBroadcast(this, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingPlayIntent = PendingIntent.getBroadcast(this, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setVisibility(Notification.VISIBILITY_PUBLIC)
                .setSmallIcon(R.drawable.ic_stat_player)
                .setContentIntent(pendingContentIntent)
                .addAction(R.drawable.ic_action_previous, "Previous", pendingPrevIntent)
                .addAction(isPlaying ? R.drawable.ic_action_pause : R.drawable.ic_action_play, "Play", pendingPlayIntent)
                .addAction(R.drawable.ic_action_next, "Next", pendingNextIntent)
                .setStyle(new Notification.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setMediaSession(mSession.getSessionToken()))
                .setContentTitle(currentSong.getTitle())
                .setContentText(currentSong.getArtist())
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .build();
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        manager.notify(1, notification);
    }

    @Override
    public void onQueueItemSelected(int position) {
        mCurrentQueuePosition = position;
        currentSong = mQueueData.get(position);
        musicServiceIntent.putExtra(INTENT_EXTRA, currentSong.getData());
        startService(musicServiceIntent);
        getContentResolver().notifyChange(QueueProvider.CONTENT_URI, null);
    }


    private void changeButtons(int layoutId, int id) {
        LayoutInflater inflater = getLayoutInflater();
        View queueLayout = inflater.inflate(layoutId, null);
        View buttons = queueLayout.findViewById(id);
        mButtons = buttons;

        ImageView playButton = (ImageView) buttons.findViewById(R.id.player_play);
        ImageView nextButton = (ImageView) buttons.findViewById(R.id.player_next);
        ImageView previousButton = (ImageView) buttons.findViewById(R.id.player_previous);
        final ImageView repeatButton = (ImageView) buttons.findViewById(R.id.player_repeat);
        final ImageView shuffleButton = (ImageView) buttons.findViewById(R.id.player_shuffle);
        if (nextButton != null) {
            nextButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    nextSong();
                }
            });
        }
        if (previousButton != null) {
            previousButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    prevSong();
                }
            });
        }
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicServiceIntent.removeExtra(INTENT_EXTRA);
                startService(musicServiceIntent);
            }
        });
        if (repeatButton != null) {
            repeatButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isRepeating) {
                        isRepeating = false;
                        repeatButton.setImageResource(R.drawable.ic_action_repeat_disabled);
                    } else {
                        isRepeating = true;
                        repeatButton.setImageResource(R.drawable.ic_action_repeat);
                    }
                }
            });
        }
        if (shuffleButton != null) {
            shuffleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isShuffling) {
                        isShuffling = false;
                        shuffleButton.setImageResource(R.drawable.ic_action_shuffle_disabled);
                    } else {
                        isShuffling = true;
                        shuffleButton.setImageResource(R.drawable.ic_action_shuffle);
                    }
                }
            });
        }
        changePlayStatus(songStatus);
        ((ViewGroup) buttons.getParent()).removeView(buttons);
        mButtonsContainer.removeAllViews();
        mButtonsContainer.addView(buttons);
    }

//    public void openQueueFragment() {
//        if (getFragmentManager().findFragmentByTag("QUEUE") == null) {
//            FragmentTransaction transaction = getFragmentManager().beginTransaction();
//            transaction.replace(R.id.main_activity_container, mQueueFragment, "QUEUE");
//            transaction.addToBackStack(null);
//            transaction.commit();
//        } else {
//            onBackPressed();
//        }
//
//    }

    @Override
    public void onBackPressed() {
        if (mReorderMode) {
            reorderMode();
        } else {
            super.onBackPressed();
        }
    }


    @Override
    public void setLibraryMenu() {
        setMenuItemVisibility(R.id.action_reorder, false);
        setMenuItemVisibility(R.id.action_clear_queue, false);
        setMenuItemVisibility(R.id.action_select, false);
        setMenuItemVisibility(R.id.action_close, true);
//        setMenuItemVisibility(R.id.action_library_switch, true);
    }

    @Override
    public void setFilesMenu() {
        setMenuItemVisibility(R.id.action_reorder, false);
        setMenuItemVisibility(R.id.action_clear_queue, false);
        setMenuItemVisibility(R.id.action_select, true);
        setMenuItemVisibility(R.id.action_close, true);
//        setMenuItemVisibility(R.id.action_library_switch, true);
    }

    @Override
    public void setQueueMenu() {
        setMenuItemVisibility(R.id.action_reorder, true);
        setMenuItemVisibility(R.id.action_clear_queue, true);
        setMenuItemVisibility(R.id.action_select, true);
        setMenuItemVisibility(R.id.action_close, false);
//        setMenuItemVisibility(R.id.action_library_switch, false);
    }

    @Override
    public void setMenuItemVisibility(int resourceId, boolean visible) {
        if (mMenu != null) {
            MenuItem item = mMenu.findItem(resourceId);
            if (item != null) {
                item.setVisible(visible);
            }
        }
    }

    public void closeLibrary() {
        if (mArtistFragment != null) {
            if (mArtistFragment.getActivity() == this) {
                getFragmentManager().popBackStack();
                onBackPressed();
            } else {
                onBackPressed();
            }
        } else {
            onBackPressed();
        }
    }

    private void selectMode() {
        if (mQueueFragment.isAdded()) {
            mSelectMode = !mSelectMode;
            mQueueFragment.getAdapter().setCheckboxVisibility(mSelectMode);
            mQueueFragment.getAdapter().notifyDataSetChanged();
        } else if (mFilesFragment.isAdded()) {
            mSelectMode = !mSelectMode;
            mFilesFragment.selectMode(mSelectMode);
            mFilesFragment.getAdapter().setCheckBoxVisibility(mSelectMode);
            mFilesFragment.getAdapter().notifyDataSetChanged();
        }
    }

    @Override
    public void setSelectMode(boolean enabled) {
        mSelectMode = enabled;
    }

    @Override
    public void invalidateQueue() {
        mQueueData.clear();
        populateQueueData();
    }

    @Override
    public void deleteSelected() {

        HashMap<Integer, QueueFragment.QueueAdapter.QueueHashHolder> hashMap = mQueueFragment.getAdapter().getHashMapChecked();
        new DeleteSelectedTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, hashMap);

    }

    private void reorderMode() {
        mReorderMode = !mReorderMode;
        MenuItem reorder = mMenu.findItem(R.id.action_reorder);
        MenuItem clear = mMenu.findItem(R.id.action_clear_queue);
        MenuItem select = mMenu.findItem(R.id.action_select);
        if (mReorderMode) {
            reorder.setIcon(R.drawable.ic_action_close);
            clear.setVisible(false);
            select.setVisible(false);
        } else {
            reorder.setIcon(R.drawable.ic_action_reorder);
            clear.setVisible(true);
            select.setVisible(true);
        }
        if (mQueueFragment != null) {
            mQueueFragment.reorderMode(mReorderMode);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // Open fragments
    ///////////////////////////////////////////////////////////////////////////

    private void openQueueFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_activity_container, mQueueFragment, TAG_QUEUE);
        transaction.commit();  // Do not add to backstack
        setTitle(R.string.queue_header);
    }

    private void openSeekBarFragment() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.seek_bar_container, mSeekBarFragment, TAG_SEEK_BAR);
        transaction.commit();
    }

    private void openLibraryFragment() {
        getFragmentManager().popBackStack(TAG_FILES, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_activity_container, mLibraryFragment, TAG_LIBRARY);
        transaction.addToBackStack(TAG_LIBRARY);
        transaction.commit();
        setTitle(R.string.library_header);
    }

    private void openFilesFragment() {
        getFragmentManager().popBackStack(TAG_LIBRARY, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.main_activity_container, mFilesFragment, TAG_FILES);
        transaction.addToBackStack(TAG_FILES);
        transaction.commit();
        setTitle(R.string.files_header);
    }

    private void openArtistFragment(String artist) {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        mArtistFragment = new ArtistFragment();
        mArtistFragment.setArtist(artist);
        transaction.replace(R.id.main_activity_container, mArtistFragment);
        transaction.addToBackStack(null);
        transaction.commit();
        setTitle(artist);
    }

    @Override
    public void openLibrary(boolean librarySwitch) {
        if (librarySwitch) {
            isLibrary = !isLibrary;
        }
//        MenuItem librarySwitchButton = mMenu.findItem(R.id.action_library_switch);
        if (isLibrary) {
//            librarySwitchButton.setIcon(R.drawable.ic_action_files_disabled);
            openLibraryFragment();
        } else {
//            librarySwitchButton.setIcon(R.drawable.ic_action_files);
            openFilesFragment();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        switch (id) {
            case R.id.action_clear_queue:
                if (!mSelectMode) {
                    clearQueue();
                } else {
                    deleteSelected();
                }
                break;
            case R.id.action_close:
                closeLibrary();
                break;
            case R.id.action_select:
                selectMode();
                break;
            case R.id.action_reorder:
                reorderMode();
                break;
//            case R.id.action_library_switch:
//                openLibrary(true);
//                break;
            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private class AddToQueueTask extends AsyncTask<AudioListModel, Void, Void> {

        @Override
        protected Void doInBackground(AudioListModel... params) {

            AudioListModel item = params[0];
            ContentValues values = new ContentValues();
            if (item.isAlbum) {
                //String[] from = AudioListModel.getColumns();
                String[] from = new String[]{
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
                Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, from, selection, where, MediaStore.Audio.Media.TRACK);
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
                        values.put(QueueDB.KEY_SORT, mQueueData.size() + 1);
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
                if (item.getSort() != -1) {
                    values.put(QueueDB.KEY_SORT, item.getSort());
                } else {
                    values.put(QueueDB.KEY_SORT, mQueueData.size() + 1);
                }
                getContentResolver().insert(QueueProvider.CONTENT_URI, values);
                invalidateQueue();
            }

            return null;
        }

    }

    private class ClearQueueTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            getContentResolver().delete(QueueProvider.CONTENT_URI, null, null);
            mQueueData.clear();
            mCurrentQueuePosition = -1;
            return null;
        }
    }

    private class DeleteSelectedTask extends AsyncTask<HashMap<Integer, QueueFragment.QueueAdapter.QueueHashHolder>, Void, Void> {

        @Override
        protected Void doInBackground(HashMap<Integer, QueueFragment.QueueAdapter.QueueHashHolder>... params) {
            HashMap<Integer, QueueFragment.QueueAdapter.QueueHashHolder> hashMap = params[0];
            for (QueueFragment.QueueAdapter.QueueHashHolder holder : hashMap.values()) {
                String where = QueueDB.KEY_ID + "=?;";
                String[] args = {holder.id.toString()};
                getContentResolver().delete(QueueProvider.CONTENT_URI, where, args);
            }
            mSelectMode = false;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mQueueFragment.getAdapter().setCheckboxVisibility(mSelectMode);
                    mQueueFragment.getAdapter().notifyDataSetChanged();
                    invalidateQueue();
                }
            });
            return null;
        }
    }

}
