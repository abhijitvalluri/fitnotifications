package com.abhijitvalluri.android.fitnotifications.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abhijitvalluri.android.fitnotifications.database.AppSelectionDbSchema.AppChoiceTable;

/**
 * Helper class for the Database
 */
public class AppSelectionDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 1;
    private static final String DATABASE_NAME = "fitNotificationAppSelection.db";

    public AppSelectionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + AppChoiceTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                AppChoiceTable.Cols.APP_PACKAGE_NAME + ", " +
                AppChoiceTable.Cols.APP_NAME + ", " +
                AppChoiceTable.Cols.SELECTION +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
