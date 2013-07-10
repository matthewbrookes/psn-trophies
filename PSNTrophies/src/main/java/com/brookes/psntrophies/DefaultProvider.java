package com.brookes.psntrophies;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

/**
 * Default Provider is only used to allow the app to sync and doesn't store data
 */
public class DefaultProvider extends ContentProvider {

    static final String AUTHORITY = "content://com.brookes.psntrophies.provider";
    public static final Uri CONTENT_URI = Uri.parse(AUTHORITY);

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] strings, String s, String[] strings2, String s2) {
        return null;
    }

    @Override
    public String getType(Uri uri) {
        String ret = getContext().getContentResolver().getType(CONTENT_URI);
        return ret;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override
    public int delete(Uri uri, String s, String[] strings) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {
        return 0;
    }
}
