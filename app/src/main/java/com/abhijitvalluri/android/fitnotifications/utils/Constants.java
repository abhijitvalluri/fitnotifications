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

package com.abhijitvalluri.android.fitnotifications.utils;

import com.abhijitvalluri.android.fitnotifications.BuildConfig;

import java.util.Date;

/**
 * List of all constants used in the app
 */
public class Constants {
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final int VERSION_CODE = BuildConfig.VERSION_CODE;
    public static final String FITBIT_PACKAGE_NAME = "com.fitbit.FitbitMobile";
    public static final int DEFAULT_NOTIF_CHAR_LIMIT = 100;
    public static final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    public static final int DEFAULT_NUM_NOTIF = 100;
    public static final int DEFAULT_DELAY_SECONDS = 7;
    public static final String NOTIFICATION_CHANNEL_ID_003 = "fitNotifications_channel_003";
    public static final String NOTIFICATION_CHANNEL_ID_005 = "fitNotifications_channel_005";
    public static final String NOTIFICATION_CHANNEL_ID_007 = "fitNotifications_channel_007";
    public static final String NOTIFICATION_CHANNEL_ID_CURRENT = "fitNotifications_channel_009";
}
