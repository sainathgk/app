package com.education.database.schoolapp;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

/**
 * Created by Sainath on 23-10-2015.
 */
public class SchoolDataContentProvider extends ContentProvider {
    private SQLiteDatabase mSchoolDB = null;

    @Override
    public boolean onCreate() {
        mSchoolDB = new SchoolDatabaseHelper(getContext()).getWriteableDB();
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (mSchoolDB != null) {
            return mSchoolDB.query(getDBTableName(uri), projection, selection, selectionArgs, null, null, sortOrder);
        }
        return null;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        if (mSchoolDB != null) {
            mSchoolDB.insert(getDBTableName(uri), null, values);
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if (mSchoolDB != null) {
            return mSchoolDB.delete(getDBTableName(uri), selection, selectionArgs);
        }
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if (mSchoolDB != null) {
            return mSchoolDB.update(getDBTableName(uri), values, selection, selectionArgs);
        }
        return 0;
    }

    private String getDBTableName(Uri uri) {
        String dbTableName = uri.toString();
        dbTableName = dbTableName.substring(dbTableName.lastIndexOf('/') + 1, dbTableName.length());
        return dbTableName;
    }
}
