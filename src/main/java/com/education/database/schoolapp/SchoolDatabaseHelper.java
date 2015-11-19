package com.education.database.schoolapp;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Sainath on 23-10-2015.
 */
public class SchoolDatabaseHelper extends SQLiteOpenHelper {

    public SchoolDatabaseHelper(Context context) {
        super(context, "SchoolData", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SchoolDataConstants.CREATE_IDENTITY_TABLE);
        db.execSQL(SchoolDataConstants.CREATE_USER_PROFILE);
/*
        db.execSQL(SchoolDataConstants.CREATE_ATTENDANCE_TABLE);
        db.execSQL(SchoolDataConstants.CREATE_RECEIVED_MESSAGES);
        db.execSQL(SchoolDataConstants.CREATE_SENT_MESSAGES);
        db.execSQL(SchoolDataConstants.CREATE_RECEIVED_NOTIFICATIONS);
        db.execSQL(SchoolDataConstants.CREATE_SENT_NOTIFICATIONS);
        db.execSQL(SchoolDataConstants.CREATE_ALBUM_DETAILS);
        db.execSQL(SchoolDataConstants.CREATE_ALBUM_DATA);
*/
        db.execSQL(SchoolDataConstants.CREATE_SERVER_MESSAGE_IDS);
        db.execSQL(SchoolDataConstants.CREATE_CLASS_STUDENTS);
        db.execSQL(SchoolDataConstants.CREATE_SENT_MESSAGES_ALL);
        db.execSQL(SchoolDataConstants.CREATE_RECEIVED_MESSAGES_ALL);
        db.execSQL(SchoolDataConstants.CREATE_ALBUM_IMAGES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public SQLiteDatabase getWriteableDB() {
        return this.getWritableDatabase();
    }
}
