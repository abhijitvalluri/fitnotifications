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

package com.abhijitvalluri.android.fitnotifications.appchoices.store;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.database.AppSelectionCursorWrapper;
import com.abhijitvalluri.android.fitnotifications.database.AppSelectionDbHelper;
import com.abhijitvalluri.android.fitnotifications.database.AppSelectionDbSchema.AppChoiceTable;
import com.abhijitvalluri.android.fitnotifications.appchoices.models.AppSelection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Singleton to store the details of a list of all app choices.
 */
public class AppSelectionsStore {
    private static AppSelectionsStore sAppSelectionsStore;

    private SQLiteDatabase mDatabase;
    private HashMap<String, String> mPackageToAppName = new HashMap<>();
    private List<String> mSelectedAppsPackageNames = new ArrayList<>();

    public static AppSelectionsStore get(Context context) {
        if (sAppSelectionsStore == null) {
            sAppSelectionsStore = new AppSelectionsStore(context);
        }
        return sAppSelectionsStore;
    }

    private AppSelectionsStore(Context context) {
        Context c = context.getApplicationContext();
        try {
            mDatabase = new AppSelectionDbHelper(c).getWritableDatabase();
        } catch (SQLException e) {
            Log.wtf("DB_OPEN_ERROR", "Unable to open the database for read/write: " + e.getMessage());
            Toast.makeText(c, "ERROR! Unable to store app settings because there is no free space! Please create more free space!", Toast.LENGTH_LONG).show();
        }
    }

    public ArrayList<AppSelection> getAppSelections() {
        ArrayList<AppSelection> appSelections = new ArrayList<>();
        mPackageToAppName = new HashMap<>();
        mSelectedAppsPackageNames = new ArrayList<>();

        try (AppSelectionCursorWrapper cursor = queryAppSelections(null, null)) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                AppSelection appSelection = cursor.getAppSelection();
                appSelections.add(appSelection);
                mPackageToAppName.put(appSelection.getAppPackageName(), appSelection.getAppName());
                if (appSelection.isSelected()) {
                    mSelectedAppsPackageNames.add(appSelection.getAppPackageName());
                }
                cursor.moveToNext();
            }
        }

        Collections.sort(appSelections, (lhs, rhs) -> String.CASE_INSENSITIVE_ORDER.compare(lhs.getAppName(), rhs.getAppName()));

        return appSelections;
    }

    public void deleteAppSelection(AppSelection appSelection) {
        String appPackageName = appSelection.getAppPackageName();

        mDatabase.delete(AppChoiceTable.NAME, AppChoiceTable.Cols.APP_PACKAGE_NAME + " = ?", new String[]{appPackageName});
    }

    // Usage: MUST call getAppSelectionsSubList() once before calling this method.
    public boolean contains(String appPackageName) {
        return mPackageToAppName.containsKey(appPackageName);
    }

    public List<String> getSelectedAppsPackageNames() {
        getAppSelections();
        return mSelectedAppsPackageNames;
    }

    // Usage: MUST call getAppSelectionsSubList() once before calling this method.
    public String getAppName(String appPackageName) {
        return mPackageToAppName.get(appPackageName);
    }

    public Integer size() {
        String countQuery = "SELECT * FROM " + AppChoiceTable.NAME;
        Cursor cursor = mDatabase.rawQuery(countQuery, null);
        int count = cursor.getCount();
        cursor.close();

        return count;
    }

    public AppSelection getAppSelection(String appPackageName) {

        try (AppSelectionCursorWrapper cursor = queryAppSelections(
                AppChoiceTable.Cols.APP_PACKAGE_NAME + " = ?",
                new String[]{appPackageName})) {
            if (cursor.getCount() == 0) {
                return null;
            }

            cursor.moveToFirst();
            return cursor.getAppSelection();
        }
    }

    public void addAppSelection(AppSelection appSelection) throws SQLException {
        ContentValues values = getContentValues(appSelection);

        if (mDatabase.insertOrThrow(AppChoiceTable.NAME, null, values) == -1) {
            throw new SQLException("An error occurred while inserting " + values.toString());
        }
    }

    public void updateAppSelection(AppSelection appSelection) {
        String appPackageName = appSelection.getAppPackageName();
        ContentValues values = getContentValues(appSelection);

        mDatabase.update(AppChoiceTable.NAME, values, AppChoiceTable.Cols.APP_PACKAGE_NAME + " = ?", new String[]{appPackageName});
    }

    private AppSelectionCursorWrapper queryAppSelections(String whereClause, String[] whereArgs) {
        Cursor cursor = mDatabase.query(
                AppChoiceTable.NAME,
                null, // Columns - null selects all columns
                whereClause,
                whereArgs,
                null, // groupBy
                null, // having
                null  // orderBy
        );

        return new AppSelectionCursorWrapper(cursor);
    }

    private static ContentValues getContentValues(AppSelection appSelection) {
        ContentValues values = new ContentValues();
        values.put(AppChoiceTable.Cols.APP_PACKAGE_NAME, appSelection.getAppPackageName());
        values.put(AppChoiceTable.Cols.APP_NAME, appSelection.getAppName());
        values.put(AppChoiceTable.Cols.SELECTION, appSelection.isSelected() ? 1 : 0);
        values.put(AppChoiceTable.Cols.FILTER_TEXT, appSelection.getFilterText());
        values.put(AppChoiceTable.Cols.START_TIME_HOUR, appSelection.getStartTimeHour());
        values.put(AppChoiceTable.Cols.START_TIME_MINUTE, appSelection.getStartTimeMinute());
        values.put(AppChoiceTable.Cols.STOP_TIME_HOUR, appSelection.getStopTimeHour());
        values.put(AppChoiceTable.Cols.STOP_TIME_MINUTE, appSelection.getStopTimeMinute());
        values.put(AppChoiceTable.Cols.DISCARD_EMPTY_NOTIFICATIONS, appSelection.isDiscardEmptyNotifications() ? 1 : 0);
        values.put(AppChoiceTable.Cols.DISCARD_ONGOING_NOTIFICATIONS, appSelection.isDiscardOngoingNotifications() ? 1 : 0);
        values.put(AppChoiceTable.Cols.ALL_DAY_SCHEDULE, appSelection.isAllDaySchedule() ? 1 : 0);
        values.put(AppChoiceTable.Cols.DAYS_OF_WEEK, appSelection.getDaysOfWeek());

        return values;
    }
}
