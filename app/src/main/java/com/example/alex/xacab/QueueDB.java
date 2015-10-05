package com.example.alex.xacab;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;

import java.util.ArrayList;

/**
 * Created by Guesto on 25.08.2015.
 */
public class QueueDB extends SQLiteOpenHelper {

    public static final String TABLE_NAME = "queue";
    public static final int DB_VERSION = 2;
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_ALBUM = "album";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DATA = "data";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_YEAR = "year";
    public static final String KEY_ALBUM_ID = "album_id";
    public static final String KEY_TRACK_ID = "track_id";
    public static final String KEY_ID = "_id";

    public QueueDB(Context context) {
        super(context, TABLE_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_QUEUE_TABLE = "CREATE TABLE " + TABLE_NAME + " ( " +
                KEY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                KEY_ARTIST + " TEXT," +
                KEY_ALBUM + " TEXT," +
                KEY_TITLE + " TEXT," +
                KEY_DATA + " TEXT," +
                KEY_DURATION + " INTEGER," +
                KEY_NUMBER + " INTEGER," +
                KEY_YEAR + " INTEGER," +
                KEY_ALBUM_ID + " INTEGER," +
                KEY_TRACK_ID + " INTEGER )";
        db.execSQL(CREATE_QUEUE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS queue");
        this.onCreate(db);
    }

    public boolean addToQueue(AudioListModel item) {
        if (item.isAlbum == true) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            ContentValues values = new ContentValues();
            values.put(KEY_ARTIST, item.getArtist());
            values.put(KEY_ALBUM, item.getAlbum());
            values.put(KEY_TITLE, item.getTitle());
            values.put(KEY_DATA, item.getData());
            values.put(KEY_DURATION, item.getDuration());
            values.put(KEY_NUMBER, item.getNumber());
            values.put(KEY_YEAR, item.getYear());
            values.put(KEY_ALBUM_ID, item.getAlbumId());
            values.put(KEY_TRACK_ID, item.getTrackId());

            db.insert(TABLE_NAME, null, values);
            db.close();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            db.close();
            return false;
        }
    }

    public ArrayList<AudioListModel> getQueue() {
        ArrayList<AudioListModel> queueList = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        String[] columns = new String[] {
                KEY_ARTIST,
                KEY_ALBUM,
                KEY_TITLE,
                KEY_DATA,
                KEY_DURATION,
                KEY_NUMBER,
                KEY_YEAR,
                KEY_ALBUM_ID,
                KEY_TRACK_ID
        };
        Cursor cursor = db.query(TABLE_NAME, columns, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ALBUM));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATA));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_YEAR));
            long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ALBUM_ID));
            long trackId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TRACK_ID));
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_ID));
            queueList.add(new AudioListModel(artist, album, title, data, duration, _id, year, albumId, trackId));
        }

        return queueList;
    }

}
