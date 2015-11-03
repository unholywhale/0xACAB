package com.whale.xacab;

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
    public static final int DB_VERSION = 3;
    public static final String KEY_ARTIST = "artist";
    public static final String KEY_ALBUM = "album";
    public static final String KEY_TITLE = "title";
    public static final String KEY_DATA = "data";
    public static final String KEY_DURATION = "duration";
    public static final String KEY_NUMBER = "number";
    public static final String KEY_SORT = "sort";
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
                KEY_SORT + " INTEGER," +
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


}
