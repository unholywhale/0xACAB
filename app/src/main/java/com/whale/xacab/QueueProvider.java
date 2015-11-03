package com.whale.xacab;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

public class QueueProvider extends ContentProvider {
    private static final String AUTHORITY = "com.whale.xacab.provider";
    private static final String DB_BASE_NAME = "queue";
    public static final int QUEUE = 100;
    public static final int QUEUE_ID = 110;
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + DB_BASE_NAME);
    private QueueDB mDB;

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sUriMatcher.addURI(AUTHORITY, DB_BASE_NAME, QUEUE);
        sUriMatcher.addURI(AUTHORITY, DB_BASE_NAME + "/#", QUEUE_ID);
    }

    public QueueProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase db = mDB.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        int rowsDeleted = 0;
        switch (uriType) {
            case QUEUE_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = db.delete(QueueDB.TABLE_NAME, QueueDB.KEY_ID + "=" + id + " and " + selection, selectionArgs);
                }
                break;
            case QUEUE:
                rowsDeleted = db.delete(QueueDB.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public String getType(Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = mDB.getWritableDatabase();
        int uriType = sUriMatcher.match(uri);
        long id = 0;
        switch(uriType) {
            case QUEUE:
                db.insert(QueueDB.TABLE_NAME, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return Uri.parse(DB_BASE_NAME + "/" + id);
    }

    @Override
    public boolean onCreate() {
        mDB = new QueueDB(getContext());
        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(QueueDB.TABLE_NAME);
        int uriType = sUriMatcher.match(uri);
        switch (uriType) {
            case QUEUE_ID:
                queryBuilder.appendWhere(QueueDB.KEY_ID + "=" + uri.getLastPathSegment());
                break;
            case QUEUE:
                break;
            default:
                throw new IllegalArgumentException("Unknown URI");
        }
        Cursor cursor = queryBuilder.query(mDB.getReadableDatabase(),
                projection, selection, selectionArgs, null, null, sortOrder);
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        return 0;
    }
}
