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

import java.util.ArrayList;
import java.util.Arrays;

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
            public static final String FILTER_TEXT = "filterText";
            public static final String START_TIME_HOUR = "startTimeHour";
            public static final String START_TIME_MINUTE = "startTimeMinute";
            public static final String STOP_TIME_HOUR = "stopTimeHour";
            public static final String STOP_TIME_MINUTE = "stopTimeMinute";
            public static final String DISCARD_EMPTY_NOTIFICATIONS = "discardEmptyNotifications";
            public static final String DISCARD_ONGOING_NOTIFICATIONS = "discardOngoingNotifications";
            public static final String ALL_DAY_SCHEDULE = "allDaySchedule";
            public static final String DAYS_OF_WEEK = "daysOfWeek";
            public static final String CUSTOM_PREFIX = "customPrefix";

            static final ArrayList<String> NAME_LIST = new ArrayList<>(Arrays.asList(
                    "_id",
                    APP_PACKAGE_NAME,
                    APP_NAME,
                    SELECTION,
                    FILTER_TEXT,
                    START_TIME_HOUR,
                    START_TIME_MINUTE,
                    STOP_TIME_HOUR,
                    STOP_TIME_MINUTE,
                    DISCARD_EMPTY_NOTIFICATIONS,
                    DISCARD_ONGOING_NOTIFICATIONS,
                    ALL_DAY_SCHEDULE,
                    DAYS_OF_WEEK,
                    CUSTOM_PREFIX
            ));
        }
    }
}
