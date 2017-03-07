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

import android.database.Cursor;
import android.database.CursorWrapper;

import com.abhijitvalluri.android.fitnotifications.database.AppSelectionDbSchema.AppChoiceTable;
import com.abhijitvalluri.android.fitnotifications.models.AppSelection;

/**
 * A wrapper for the cursor class to make accessing data from the database more convenient.
 */
public class AppSelectionCursorWrapper extends CursorWrapper {

    public AppSelectionCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public AppSelection getAppSelection() {
        String appPackageName = getString(getColumnIndex(AppChoiceTable.Cols.APP_PACKAGE_NAME));
        String appName        = getString(getColumnIndex(AppChoiceTable.Cols.APP_NAME));
        int isSelected        = getInt(getColumnIndex(AppChoiceTable.Cols.SELECTION));
        String filterText     = getString(getColumnIndex(AppChoiceTable.Cols.FILTER_TEXT));
        int startTimeHour     = getInt(getColumnIndex(AppChoiceTable.Cols.START_TIME_HOUR));
        int startTimeMinute   = getInt(getColumnIndex(AppChoiceTable.Cols.START_TIME_MINUTE));
        int stopTimeHour      = getInt(getColumnIndex(AppChoiceTable.Cols.STOP_TIME_HOUR));
        int stopTimeMinute    = getInt(getColumnIndex(AppChoiceTable.Cols.STOP_TIME_MINUTE));
        int discardEmptyNotif = getInt(getColumnIndex(AppChoiceTable.Cols.DISCARD_EMPTY_NOTIFICATIONS));
        int allDaySchedule    = getInt(getColumnIndex(AppChoiceTable.Cols.ALL_DAY_SCHEDULE));

        return new AppSelection(appPackageName,
                                appName,
                                isSelected != 0,
                                filterText,
                                startTimeHour,
                                startTimeMinute,
                                stopTimeHour,
                                stopTimeMinute,
                                discardEmptyNotif != 0,
                                allDaySchedule != 0);
    }
}
