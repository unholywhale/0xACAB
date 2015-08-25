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

    private static final String TABLE_QUEUE = "queue";
    private static final int DB_VERSION = 1;
    private static final String KEY_ARTIST = "artist";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATA = "data";
    private static final String KEY_DURATION = "duration";
    private static final String KEY_NUMBER = "number";
    private static final String KEY_YEAR = "year";
    private static final String KEY_ALBUM_ID = "album_id";
    private static final String KEY_TRACK_ID = "track_id";

    public QueueDB(Context context) {
        super(context, TABLE_QUEUE, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_QUEUE_TABLE = "CREATE TABLE queue ( " +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "artist TEXT," +
                "album TEXT," +
                "title TEXT," +
                "data TEXT," +
                "duration INTEGER," +
                "number INTEGER," +
                "year INTEGER," +
                "album_id INTEGER," +
                "track_id INTEGER )";
        db.execSQL(CREATE_QUEUE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS queue");
        this.onCreate(db);
    }

    public void addToQueue(AudioListModel item) {
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

            db.insert(TABLE_QUEUE, null, values);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public ArrayList<AudioListModel> getQueue() {
        ArrayList<AudioListModel> queueList = new ArrayList<>();
        SQLiteDatabase db = getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_QUEUE;
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
        Cursor cursor = db.query(TABLE_QUEUE, columns, null, null, null, null, null);
        while (cursor.moveToNext()) {
            String artist = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ARTIST));
            String album = cursor.getString(cursor.getColumnIndexOrThrow(KEY_ALBUM));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(KEY_TITLE));
            String data = cursor.getString(cursor.getColumnIndexOrThrow(KEY_DATA));
            int duration = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_DURATION));
            int year = cursor.getInt(cursor.getColumnIndexOrThrow(KEY_YEAR));
            long albumId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_ALBUM_ID));
            long trackId = cursor.getLong(cursor.getColumnIndexOrThrow(KEY_TRACK_ID));
            int _id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            queueList.add(new AudioListModel(artist, album, title, data, duration, _id, year, albumId, trackId));
        }

        return queueList;
    }

    public void removeFromQueue(long trackId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_QUEUE,
                KEY_TRACK_ID + " = ?",
                new String[] {String.valueOf(trackId)});
        db.close();
    }
}
