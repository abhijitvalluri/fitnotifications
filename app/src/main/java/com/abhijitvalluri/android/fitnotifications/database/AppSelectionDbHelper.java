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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.abhijitvalluri.android.fitnotifications.database.AppSelectionDbSchema.AppChoiceTable;

/**
 * Helper class for the Database
 */
public class AppSelectionDbHelper extends SQLiteOpenHelper {
    private static final int VERSION = 5;
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
                AppChoiceTable.Cols.SELECTION + ", " +
                AppChoiceTable.Cols.FILTER_TEXT + ", " +
                AppChoiceTable.Cols.START_TIME_HOUR + ", " +
                AppChoiceTable.Cols.START_TIME_MINUTE + ", " +
                AppChoiceTable.Cols.STOP_TIME_HOUR + ", " +
                AppChoiceTable.Cols.STOP_TIME_MINUTE + ", " +
                AppChoiceTable.Cols.DISCARD_EMPTY_NOTIFICATIONS + ", " +
                AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS + ", " +
                AppChoiceTable.Cols.ALL_DAY_SCHEDULE +
                AppChoiceTable.Cols.DAYS_OF_WEEK +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion == 1 && newVersion == VERSION) {
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.FILTER_TEXT + " TEXT NOT NULL DEFAULT '';");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.START_TIME_HOUR + " INTEGER NOT NULL DEFAULT 0;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.START_TIME_MINUTE + " INTEGER NOT NULL DEFAULT 0;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.STOP_TIME_HOUR + " INTEGER NOT NULL DEFAULT 23;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.STOP_TIME_MINUTE + " INTEGER NOT NULL DEFAULT 59;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DISCARD_EMPTY_NOTIFICATIONS + " INTEGER NOT NULL DEFAULT 0;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS + " INTEGER NOT NULL DEFAULT 1;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.ALL_DAY_SCHEDULE + " INTEGER NOT NULL DEFAULT 1;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DAYS_OF_WEEK + " INTEGER NOT NULL DEFAULT 127;");
        } else if (oldVersion == 2 && newVersion == VERSION) {
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.ALL_DAY_SCHEDULE + " INTEGER NOT NULL DEFAULT 1;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS + " INTEGER NOT NULL DEFAULT 1;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DAYS_OF_WEEK + " INTEGER NOT NULL DEFAULT 127;");
        } else if (oldVersion == 3 && newVersion == VERSION) {
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS + " INTEGER NOT NULL DEFAULT 1;");
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DAYS_OF_WEEK + " INTEGER NOT NULL DEFAULT 127;");
        } else if (oldVersion == 4 && newVersion == VERSION) {
            db.execSQL("alter table " + AppChoiceTable.NAME + " add column " +
                    AppChoiceTable.Cols.DAYS_OF_WEEK + " INTEGER NOT NULL DEFAULT 127;");
        }
    }
}
