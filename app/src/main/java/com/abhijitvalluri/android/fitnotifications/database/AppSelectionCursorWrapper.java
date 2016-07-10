package com.abhijitvalluri.android.fitnotifications.database;

import android.database.Cursor;
import android.database.CursorWrapper;

import com.abhijitvalluri.android.fitnotifications.models.AppSelection;
import com.abhijitvalluri.android.fitnotifications.database.AppSelectionDbSchema.AppChoiceTable;

/**
 * A wrapper for the cursor class to make accessing data from the database more convenient.
 */
public class AppSelectionCursorWrapper extends CursorWrapper {

    public AppSelectionCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public AppSelection getAppSelection() {
        String appPackageName = getString(getColumnIndex(AppChoiceTable.Cols.APP_PACKAGE_NAME));
        String appName = getString(getColumnIndex(AppChoiceTable.Cols.APP_NAME));
        Integer isSelected = getInt(getColumnIndex(AppChoiceTable.Cols.SELECTION));

        return new AppSelection(appPackageName, appName, isSelected != 0);
    }
}
