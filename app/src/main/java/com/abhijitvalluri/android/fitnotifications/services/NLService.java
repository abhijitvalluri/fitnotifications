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

package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.SettingsActivity;
import com.abhijitvalluri.android.fitnotifications.models.AppSelection;
import com.abhijitvalluri.android.fitnotifications.utils.AppSelectionsStore;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.utils.TranslitUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fit Notification Service
 */
public class NLService extends NotificationListenerService {

    private static final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

    private static final MessageExtractor DEFAULT_EXTRACTOR = new GenericMessageExtractor();
    private final Map<String, MessageExtractor> messageExtractors = new TreeMap<>();

    private final Handler mHandler = new Handler();

    private static List<String> mSelectedAppsPackageNames;
    private static boolean mIsServiceEnabled;
    private static boolean mDismissPlaceholderNotif;
    private static boolean mDismissRelayedNotif;
    private static boolean mLimitNotifications;
    private static boolean mDisableWhenScreenOn;
    private static boolean mTransliterateNotif;
    private static boolean mSplitNotification;
    private static boolean mDisplayAppName;
    private static int mFitbitNotifCharLimit;
    private static int mNumSplitNotifications;
    private static int mPlaceholderNotifDismissDelayMillis;
    private static int mRelayedNotifDismissDelayMillis;
    private static int mNotifLimitDurationMillis;

    private TranslitUtil translitUtil;

    private NotificationManager mNotificationManager;
    private AppSelectionsStore mAppSelectionsStore;
    private HashMap<String, Long> mLastNotificationTimeMap;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAppSelectionsStore = AppSelectionsStore.get(this);
        mLastNotificationTimeMap = new HashMap<>();

        mSelectedAppsPackageNames = mAppSelectionsStore.getSelectedAppsPackageNames();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsServiceEnabled = true;

        // FIXME: preferences keys should not be "translatable" ?
        mDismissPlaceholderNotif = preferences.getBoolean(
                                        getString(R.string.dismiss_placeholder_notif_key), false);
        mDismissRelayedNotif = preferences.getBoolean(
                                        getString(R.string.dismiss_relayed_notif_key), false);
        mPlaceholderNotifDismissDelayMillis = preferences.getInt(
                getString(R.string.placeholder_dismiss_delay_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;
        mRelayedNotifDismissDelayMillis = preferences.getInt(
                getString(R.string.relayed_dismiss_delay_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;

        mLimitNotifications = preferences.getBoolean(
                getString(R.string.limit_notif_key), false);
        mNotifLimitDurationMillis = preferences.getInt(
                getString(R.string.notif_limit_duration_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;
        mDisableWhenScreenOn = preferences.getBoolean(
                getString(R.string.disable_forward_screen_on_key), false);
        mTransliterateNotif = preferences.getBoolean(
                getString(R.string.transliterate_notification_key), true);
        mSplitNotification = preferences.getBoolean(
                getString(R.string.split_notification_key), false);
        mFitbitNotifCharLimit = preferences.getInt(
                getString(R.string.notification_text_limit_key), Constants.DEFAULT_NOTIF_CHAR_LIMIT);
        mNumSplitNotifications = preferences.getInt(
                getString(R.string.num_split_notifications_key), Constants.DEFAULT_NUM_NOTIF);
        mDisplayAppName = preferences.getBoolean(getString(R.string.display_app_name_key), true);

        Toast.makeText(this, getString(R.string.notification_service_started), Toast.LENGTH_LONG).show();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // base context is needed to access Resources
        Resources res = getResources();

        translitUtil = new TranslitUtil(res);

        // Telegram
        messageExtractors.put("org.telegram.messenger", new GroupSummaryMessageExtractor(res, true));
        // WhatsApp
        messageExtractors.put("com.whatsapp", new GroupSummaryMessageExtractor(res, false));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }

    public static void onAppSelectionsUpdated(Context context) {
        mSelectedAppsPackageNames = AppSelectionsStore.get(context).getSelectedAppsPackageNames();
    }

    public static void onPlaceholderNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissPlaceholderNotif = dismissNotif;
        mPlaceholderNotifDismissDelayMillis = delaySeconds * 1000;
    }

    public static void onSplitNotificationSettingUpdated(boolean enabled,
                                                         int charLimit,
                                                         int numSplitNotifs) {
        mSplitNotification = enabled;
        mFitbitNotifCharLimit = charLimit;
        mNumSplitNotifications = numSplitNotifs;
    }

    public static void onLimitNotificationSettingUpdated(boolean limitNotif, int durationSeconds) {
        mLimitNotifications = limitNotif;
        mNotifLimitDurationMillis = durationSeconds * 1000;
    }

    public static void onRelayedNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissRelayedNotif = dismissNotif;
        mRelayedNotifDismissDelayMillis = delaySeconds * 1000;
    }

    public static void onDisableWhenScreenOnUpdated(boolean disableWhenScreenOn) {
        mDisableWhenScreenOn = disableWhenScreenOn;
    }

    public static void onTransliterateNotificationUpdated(boolean enableTransliteration) {
        mTransliterateNotif = enableTransliteration;
    }

    public static void onDisplayAppNameUpdated(boolean displayAppName) {
        mDisplayAppName = displayAppName;
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {

        if (!mIsServiceEnabled) {
            return;
        }

        if (mDisableWhenScreenOn && isScreenOn()) {
            return;
        }

        Notification notification = sbn.getNotification();
        final String appPackageName = sbn.getPackageName();
        Bundle extras = notification.extras;

        // DISREGARD SPAMMY NOTIFICATIONS
        if ((notification.flags & Notification.FLAG_ONGOING_EVENT) > 0) {
            // Discard ongoing notifications
            // TODO: Nothing else apart from this is consistent.
            // I tried to see InboxStyle notifications vs. not and that did not help
            // Not all use the EXTRA_SUMMARY_GROUP correctly either
            // Perhaps best option is to allow users to custom discard useless
            // messages via a string match
            return;
        }

        if (!appNotificationsActive(appPackageName)) {
            return;
        }

        if (mLimitNotifications) {
            Long currentTimeMillis = System.currentTimeMillis();
            Long lastNotificationTime = mLastNotificationTimeMap.get(appPackageName);
            if (lastNotificationTime != null
                    && currentTimeMillis < lastNotificationTime + mNotifLimitDurationMillis) {
                return;
            }
            mLastNotificationTimeMap.put(appPackageName, currentTimeMillis);
        }

        String filterText = null;
        boolean discardEmptyNotifications = false;

        {
            AppSelection appSelection = AppSelectionsStore.get(this).getAppSelection(appPackageName);
            if (appSelection != null) {
                filterText = appSelection.getFilterText().trim();
                discardEmptyNotifications = appSelection.isDiscardEmptyNotifications();
            }
        }

        CharSequence[] titleAndText = getMessageExtractor(appPackageName).getTitleAndText(appPackageName, extras, notification.flags);

        if (titleAndText[1] == null && discardEmptyNotifications) {
            return;
        }

        if (anyMatchesFilter(filterText, titleAndText)) {
            return;
        }

        CharSequence notificationTitle = titleAndText[0];
        String notificationText = titleAndText[1] == null ? "" : titleAndText[1].toString();

        if (mDisplayAppName) {
            notificationText = "[" + mAppSelectionsStore.getAppName(appPackageName) + "] " + notificationText;
        }

        if (mTransliterateNotif) {
            notificationTitle = translitUtil.transliterate(notificationTitle);
            notificationText = translitUtil.transliterate(notificationText);
        }

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        contentView.setTextViewText(
                R.id.customNotificationText, getString(R.string.notification_text));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(R.drawable.ic_sms_white_24dp)
                .setContent(contentView)
                .setContentTitle(notificationTitle)
                .setLocalOnly(true)         // avoid bridging this notification to other devices
                .setContentIntent(createSettingsIntent())
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // prevent notification from appearing on the lock screen
            builder.setVisibility(Notification.VISIBILITY_SECRET);
        }

        if (mSplitNotification && notificationText.length() > mFitbitNotifCharLimit) {
            List<String> slices = sliceNotificationText(notificationText);
            for (int i = 0; i < slices.size(); i++) {
                builder.setContentText(slices.get(i));
                final Notification notif = builder.build();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNotificationManager.notify(NOTIFICATION_ID, notif);
                    }
                }, 500 * (i + 1));
            }
        } else { // Do not split the notification
            builder.setContentText(notificationText);
            mNotificationManager.notify(NOTIFICATION_ID, builder.build());
        }

        if (mDismissPlaceholderNotif) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mNotificationManager.cancel(NOTIFICATION_ID);
                }
            }, mPlaceholderNotifDismissDelayMillis);
        }

        if (mDismissRelayedNotif) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        cancelNotification(sbn.getKey());
                    } else {
                        //noinspection deprecation
                        cancelNotification(appPackageName, sbn.getTag(), sbn.getId());
                    }
                }
            }, mRelayedNotifDismissDelayMillis);
        }
    }

    @NonNull
    private MessageExtractor getMessageExtractor(String appPackageName) {
        MessageExtractor extractor = messageExtractors.get(appPackageName);
        return extractor == null ? DEFAULT_EXTRACTOR : extractor;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn,
                                     NotificationListenerService.RankingMap rankingMap) {
        onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

    /**
     * Creates an intent to open Fit Notifications settings when notification is clicked.
     */
    private PendingIntent createSettingsIntent() {
        // Creates an explicit intent for the SettingsActivity in the app
        Intent settingsIntent = new Intent(this, SettingsActivity.class);

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of the application to
        // the Home screen.
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(SettingsActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(settingsIntent);
        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    /**
     * Checks if the notification comes from a selected application and if the application
     * schedule is currently active.
     */
    private boolean appNotificationsActive(String appPackageName) {
        if (!mSelectedAppsPackageNames.contains(appPackageName)) {
            return false;
        }

        AppSelection appSelection = AppSelectionsStore.get(this).getAppSelection(appPackageName);
        if (appSelection == null) { // Should never happen. So if it does, just return false
            return false;
        }

        if (appSelection.isAllDaySchedule()) {
            return true;
        }

        int startTime = appSelection.getStartTime();
        int stopTime = appSelection.getStopTime();

        if (startTime == 0 && stopTime == 0) {
            // All day enabled
            return true;
        }

        // Get current time
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        int currTime = hour * 60 + minute;

        if (startTime < stopTime) {
            // Schedule is within one day
            return ((currTime >= startTime) && (currTime < stopTime));
        } else {
            // Schedule spans across a day
            return ((currTime >= startTime) || (currTime < stopTime));
        }
    }

    private boolean isScreenOn() {
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
            // API >= 20
            return pm.isInteractive();
        }

        // API <= 19, use deprecated
        //noinspection deprecation
        return pm.isScreenOn();
    }

    /**
     * Checks if any of the <code>CharSequence</code> items contains the provided <code>filter</code>
     * text.
     */
    private static boolean anyMatchesFilter(String filter, CharSequence ... items) {
        if (filter != null && !filter.isEmpty()) {
            String[] parts = filter.split("\\s*;\\s*");

            for (CharSequence item : items) {
                if (item != null) {
                    String tmp = item.toString();
                    for (String filterText : parts) {
                        if (filterText.length() > 0 && tmp.contains(filterText)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Slices the text into up to <code>mNumSplitNotifications + 1</code> pieces each not longer than
     * <code>mFitbitNotifCharLimit</code> (except for the last piece which contains the all remaining text)
     */
    private static List<String> sliceNotificationText(String notificationString) {
        List<String> slices = new ArrayList<>(mNumSplitNotifications + 1);

        int notifCount = 1;

        int charLimit = mFitbitNotifCharLimit - 6;      // 6 chars for " (2/3)" with changing numbers
        while (notifCount < mNumSplitNotifications && notificationString.length() > mFitbitNotifCharLimit) {
            String partialText;
            int whitespacePos = notificationString.lastIndexOf(" ", charLimit);

            // TODO: check that partialText is not very short (whitespacePos > charLimit / 2 ?)
            if (whitespacePos > 0) {
                partialText = notificationString.substring(0, whitespacePos);
                notificationString = notificationString.substring(whitespacePos + 1);
            } else {
                partialText = notificationString.substring(0, charLimit);
                notificationString = notificationString.substring(charLimit);
            }

            slices.add(partialText);
            notifCount++;
        }

        if (notificationString.length() > 0) {
            slices.add(notificationString);
        }

        // add " (2/3)" suffixes to all but the last piece
        for (int i = 0; i < slices.size() - 1; i++) {
            slices.set(i,
                    String.format(Locale.ENGLISH, "%s (%d/%d)", slices.get(i), i + 1, slices.size()));
        }

        return slices;
    }

    public static void setEnabled(boolean enabled) {
        mIsServiceEnabled = enabled;
    }

    public static boolean isEnabled() {
        return mIsServiceEnabled;
    }
}
