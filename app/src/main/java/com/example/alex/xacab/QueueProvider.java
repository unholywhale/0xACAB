package com.example.alex.xacab;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class QueueProvider extends ContentProvider {
    private static final String AUTHORITY = "com.example.alex.xacab.provider";
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
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
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
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
