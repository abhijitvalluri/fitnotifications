package com.abhijitvalluri.android.fitnotifications.database;

/**
 * Database Schema to store the Fit Notification app's app selection choices.
 */
public class AppSelectionDbSchema {
    public static final class AppChoiceTable {
        public static final String NAME = "appChoices";

        public static final class Cols {
            public static final String APP_PACKAGE_NAME = "appPackageName";
            public static final String APP_NAME = "appName";
            public static final String SELECTION = "selection";
        }
    }
}
