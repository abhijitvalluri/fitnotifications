/*
   Copyright 2017 Abhijit Kiran Valluri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.abhijitvalluri.android.fitnotifications.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.database.AppSelectionDbSchema.AppChoiceTable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Helper class for the Database
 */
public class AppSelectionDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 8;
    private static final String DATABASE_NAME = "fitNotificationAppSelection.db";
    private HashMap<String, String> mDbAlterCommands;
    private Context mContext;

    public AppSelectionDbHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);

        mContext = context;
        mDbAlterCommands = new HashMap<>();
        mDbAlterCommands.put(
                AppChoiceTable.Cols.APP_PACKAGE_NAME,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.APP_PACKAGE_NAME + " TEXT NOT NULL DEFAULT '';");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.APP_NAME,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.APP_NAME + " TEXT NOT NULL DEFAULT '';");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.SELECTION,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.SELECTION + " INTEGER NOT NULL DEFAULT 0;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.FILTER_TEXT,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.FILTER_TEXT + " TEXT NOT NULL DEFAULT '';");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.START_TIME_HOUR,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.START_TIME_HOUR + " INTEGER NOT NULL DEFAULT 0;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.START_TIME_MINUTE,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.START_TIME_MINUTE + " INTEGER NOT NULL DEFAULT 0;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.STOP_TIME_HOUR,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.STOP_TIME_HOUR + " INTEGER NOT NULL DEFAULT 23;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.STOP_TIME_MINUTE,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.STOP_TIME_MINUTE + " INTEGER NOT NULL DEFAULT 59;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.DISCARD_EMPTY_NOTIFICATIONS,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.DISCARD_EMPTY_NOTIFICATIONS + " INTEGER NOT NULL DEFAULT 0;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.ALL_DAY_SCHEDULE,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.ALL_DAY_SCHEDULE + " INTEGER NOT NULL DEFAULT 1;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS + " INTEGER NOT NULL DEFAULT 1;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.DAYS_OF_WEEK,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.DAYS_OF_WEEK + " INTEGER NOT NULL DEFAULT 127;");
        mDbAlterCommands.put(
                AppChoiceTable.Cols.CUSTOM_PREFIX,
                "alter table " + AppChoiceTable.NAME + " add column " +
                AppChoiceTable.Cols.CUSTOM_PREFIX + " TEXT DEFAULT '';");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB_CREATE", "Creating table " + AppChoiceTable.NAME);
        db.execSQL("create table " + AppChoiceTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                AppChoiceTable.Cols.APP_PACKAGE_NAME + ", " +
                AppChoiceTable.Cols.APP_NAME + ", " +
                AppChoiceTable.Cols.SELECTION + ", " +
                AppChoiceTable.Cols.FILTER_TEXT + ", " +
                AppChoiceTable.Cols.START_TIME_HOUR + ", " +
                AppChoiceTable.Cols.START_TIME_MINUTE + ", " +
                AppChoiceTable.Cols.STOP_TIME_HOUR + ", " +
                AppChoiceTable.Cols.STOP_TIME_MINUTE + ", " +
                AppChoiceTable.Cols.DISCARD_EMPTY_NOTIFICATIONS + ", " +
                AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS + ", " +
                AppChoiceTable.Cols.ALL_DAY_SCHEDULE + ", " +
                AppChoiceTable.Cols.DAYS_OF_WEEK + ", " +
                AppChoiceTable.Cols.CUSTOM_PREFIX +
                ")"
                // ALERT!!! Make sure you have a comma to separate all names! Had a bug because I forgot it of ALL_DAY_SCHEDULE
        );
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            super.onDowngrade(db, oldVersion, newVersion);
        } catch (Exception e) {
            Log.e("DB_DOWNGRADE", "Failed to downgrade DB: " + e.getMessage());
            Toast.makeText(mContext, "App update has an issue! Please send logs to developer!", Toast.LENGTH_LONG).show();
        }
    }

        @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DB_UPGRADE", "Updating " + AppChoiceTable.NAME + " table to version " +
                            newVersion + " from version " + oldVersion);
        Cursor cursor = db.query(AppChoiceTable.NAME, null, null, null, null, null, null);
        ArrayList<String> existentColumns = new ArrayList<>(Arrays.asList(cursor.getColumnNames()));
        cursor.close();
        ArrayList<String> missingColumns = AppChoiceTable.Cols.NAME_LIST;
        missingColumns.removeAll(existentColumns);

        try {
            for (String columnName : missingColumns) {
                Log.d("DB_UPGRADE", "Adding column " + columnName + " to table using: " +
                        mDbAlterCommands.get(columnName));
                db.execSQL(mDbAlterCommands.get(columnName));
            }
        } catch (Exception e) {
            Log.e("DB_UPGRADE", "Failed to upgrade DB: " + e.getMessage());
            Toast.makeText(mContext, "App update has an issue! Please send logs to developer!", Toast.LENGTH_LONG).show();
        }
    }
}
