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
import android.os.Looper;
import android.os.PowerManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;

import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.appchoices.models.AppSelection;
import com.abhijitvalluri.android.fitnotifications.appchoices.store.AppSelectionsStore;
import com.abhijitvalluri.android.fitnotifications.settings.SettingsActivity;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.utils.DebugLog;
import com.abhijitvalluri.android.fitnotifications.utils.TranslitUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.preference.PreferenceManager;

/**
 * Fit Notification Service
 */
public class NLService extends NotificationListenerService {

    private static final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);

    private static final MessageExtractor sDefaultExtractor = new GenericMessageExtractor();
    private final Map<String, MessageExtractor> mMessageExtractors = new TreeMap<>();

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    private static List<String> mSelectedAppsPackageNames;
    private static boolean mIsServiceEnabled;
    private static boolean mDismissPlaceholderNotif;
    private static boolean mDismissRelayedNotif;
    private static boolean mForwardOnlyPriorityNotifs;
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

    private DebugLog mDebugLog;
    private TranslitUtil translitUtil;
    private NotificationManager mNotificationManager;
    private AppSelectionsStore mAppSelectionsStore;
    private Map<String, Long> mLastNotificationTimeMap;
    private int mInterruptionFilter;

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mAppSelectionsStore = AppSelectionsStore.get(this);
        mDebugLog = DebugLog.get(this);
        mLastNotificationTimeMap = new HashMap<>();
        mInterruptionFilter = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                              ? getCurrentInterruptionFilter() : 0;

        mSelectedAppsPackageNames = mAppSelectionsStore.getSelectedAppsPackageNames();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsServiceEnabled = preferences.getBoolean(getString(R.string.notification_listener_service_state_key), true);

        // FIXME: preferences keys should not be "translatable" ?
        mDismissPlaceholderNotif = preferences.getBoolean(
                                        getString(R.string.dismiss_placeholder_notif_key), false);
        mDismissRelayedNotif = preferences.getBoolean(
                                        getString(R.string.dismiss_relayed_notif_key), false);
        mForwardOnlyPriorityNotifs = preferences.getBoolean(
                                        getString(R.string.forward_priority_only_notifications_key), false);
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
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        // base context is needed to access Resources
        Resources res = getResources();

        translitUtil = new TranslitUtil(res);

        // Telegram
        mMessageExtractors.put("org.telegram.messenger", new GroupSummaryMessageExtractor(res, true));
        // WhatsApp
        mMessageExtractors.put("com.whatsapp", new GroupSummaryMessageExtractor(res, false));
        // WhatsApp+ unofficial app
        mMessageExtractors.put("com.WhatsApp4Plus", new GroupSummaryMessageExtractor(res, false));
        // Google Calendar
        mMessageExtractors.put("com.google.android.calendar", new BasicMessageExtractor());
        // GMail
        mMessageExtractors.put("com.google.android.gm", new IgnoreSummaryMessageExtractor());
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

    public static void onForwardOnlyPriorityNotifSettingUpdated(boolean forwardOnlyPriorityNotifs) {
        mForwardOnlyPriorityNotifs = forwardOnlyPriorityNotifs;
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
        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("++++++++++++");
            mDebugLog.writeLog("Entered onNotificationPosted. Notification from: " + sbn.getPackageName());
        }

        if (!mIsServiceEnabled) {
            return;
        }

        if (mDisableWhenScreenOn && isScreenOn()) {
            return;
        }

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("Service is enabled");
        }

        Notification notification = sbn.getNotification();
        final String appPackageName = sbn.getPackageName();
        Bundle extras = notification.extras;

        if (!appNotificationsActive(appPackageName)) {
            return;
        }

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog(appPackageName + " is selected");
        }

        String filterText = null;
        boolean discardEmptyNotifications = false;
        boolean discardOngoingNotifications = true;

        {
            AppSelection appSelection = AppSelectionsStore.get(this).getAppSelection(appPackageName);
            if (appSelection != null) {
                filterText = appSelection.getFilterText().trim();
                discardEmptyNotifications = appSelection.isDiscardEmptyNotifications();
                discardOngoingNotifications = appSelection.isDiscardOngoingNotifications();
            }
        }

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("Discard Empty: " + discardEmptyNotifications + ", Discard Ongoing: " + discardOngoingNotifications);
        }

        // DISREGARD SPAMMY NOTIFICATIONS
        if (discardOngoingNotifications && (notification.flags & Notification.FLAG_ONGOING_EVENT) > 0) {
            // Discard ongoing notifications
            // TODO: Nothing else apart from this is consistent.
            // I tried to see InboxStyle notifications vs. not and that did not help
            // Not all use the EXTRA_SUMMARY_GROUP correctly either
            // Perhaps best option is to allow users to custom discard useless
            // messages via a string match
            return;
        }

        CharSequence[] titleAndText = getMessageExtractor(appPackageName).getTitleAndText(appPackageName, extras, notification.flags);

        // "generic" extractor will never return null as the notificationText
        // and app-specific extractors will return null for notifications that should be skipped
        if (titleAndText == null || titleAndText[1] == null || (titleAndText[1].length() == 0 && discardEmptyNotifications)) {
            if (mDebugLog.isEnabled()) {
                mDebugLog.writeLog("Extractor gave null title or null text or is empty");
                mDebugLog.writeLog("titleAndText: " + (titleAndText == null ? "null" : "not null"));
                if (titleAndText != null) {
                    mDebugLog.writeLog("titleAndText[1]: " + (titleAndText[1] == null ? "null" : "not null"));
                    if (titleAndText[1] != null) {
                        mDebugLog.writeLog("titleAndText[1].length(): " + titleAndText[1].length());
                    }
                }
            }
            return;
        }

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("Extractor gave non null titleAndText and non-empty too");
        }

        if (isFiltered(filterText, titleAndText)) {
            return;
        }

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("Unfiltered and retained (not discarded)");
        }

        if (mLimitNotifications) {
            long currentTimeMillis = System.currentTimeMillis();
            Long lastNotificationTime = mLastNotificationTimeMap.get(appPackageName);
            if (lastNotificationTime != null && currentTimeMillis < lastNotificationTime + mNotifLimitDurationMillis) {
                return;
            }
            mLastNotificationTimeMap.put(appPackageName, currentTimeMillis);
        }

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("Unlimited");
        }

        CharSequence notificationTitle = titleAndText[0];
        String notificationText = titleAndText[1].toString();

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

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("Constructing notification");
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID_CURRENT);
        builder.setSmallIcon(R.drawable.ic_sms_white_24dp)
                .setContent(contentView)
                .setContentTitle(notificationTitle)
                .setLocalOnly(true)         // avoid bridging this notification to other devices
                .setContentIntent(createSettingsIntent())
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // prevent notification from appearing on the lock screen
            builder.setVisibility(NotificationCompat.VISIBILITY_SECRET);
        }

        if (mSplitNotification && notificationText.length() > mFitbitNotifCharLimit) {
            List<String> slices = sliceNotificationText(notificationText);
            for (int i = 0; i < slices.size(); i++) {
                builder.setContentText(slices.get(i));
                final Notification notif = builder.build();
                mHandler.postDelayed(() -> mNotificationManager.notify(NOTIFICATION_ID, notif), 500 * (i + 1));
            }
        } else { // Do not split the notification
            builder.setContentText(notificationText);
            final Notification notif = builder.build();
            mHandler.postDelayed(() -> mNotificationManager.notify(NOTIFICATION_ID, notif), 500);
        }

        if (mDismissPlaceholderNotif) {
            mHandler.postDelayed(() -> mNotificationManager.cancel(NOTIFICATION_ID), mPlaceholderNotifDismissDelayMillis);
        }

        if (mDismissRelayedNotif) {
            mHandler.postDelayed(() -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    cancelNotification(sbn.getKey());
                } else {
                    cancelNotification(appPackageName, sbn.getTag(), sbn.getId());
                }
            }, mRelayedNotifDismissDelayMillis);
        }

        if (mDebugLog.isEnabled()) {
            mDebugLog.writeLog("Notification sent");
            mDebugLog.writeLog("------------");
        }
    }

    @NonNull
    private MessageExtractor getMessageExtractor(String appPackageName) {
        MessageExtractor extractor = mMessageExtractors.get(appPackageName);

        if (extractor == null) {
            sDefaultExtractor.setDebugLog(mDebugLog);
            sDefaultExtractor.setLoggingEnabled(mDebugLog.isEnabled());
            return sDefaultExtractor;
        } else {
            extractor.setDebugLog(mDebugLog);
            extractor.setLoggingEnabled(mDebugLog.isEnabled());
            return extractor;
        }
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn,
                                     NotificationListenerService.RankingMap rankingMap) {

        if (mForwardOnlyPriorityNotifs) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                    mInterruptionFilter == INTERRUPTION_FILTER_PRIORITY) {
                String packageName = sbn.getPackageName();
                String rankingKey = null;
                for (String s : rankingMap.getOrderedKeys()) {
                    if (s.contains(packageName)) {
                        rankingKey = s;
                        break;
                    }
                }

                Ranking ranking = new Ranking();
                if (rankingKey != null && rankingMap.getRanking(rankingKey, ranking)) {
                    if (!ranking.matchesInterruptionFilter()) {
                        return;
                    }
                }
            }
        }
        onNotificationPosted(sbn);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // Do nothing
    }

    @Override
    public void onInterruptionFilterChanged(int interruptionFilter) {
        mInterruptionFilter = interruptionFilter;
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

        // Day of week check: Check if today is selected in the schedule
        // Get current time
        Calendar cal = Calendar.getInstance();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1; // 0 is Sunday, 6 is Saturday
        if ((appSelection.getDaysOfWeek() & (1 << dayOfWeek)) == 0) { // Today is not in schedule
            return false;
        }
        // If we reach here, then today is okay by the schedule

        if (appSelection.isAllDaySchedule()) {
            return true;
        }

        int startTime = appSelection.getStartTime();
        int stopTime = appSelection.getStopTime();

        if (startTime == 0 && stopTime == 0) {
            // All day enabled
            return true;
        }

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
    private static boolean isFiltered(String filter, CharSequence ... items) {
        if (filter != null && !filter.isEmpty()) {
            String[] parts = filter.split("\\s*;\\s*");

            ArrayList<String> positiveFilters = new ArrayList<>();
            ArrayList<String> negativeFilters = new ArrayList<>();

            for (String filterText : parts) {
                String tmp = filterText.trim();
                if (tmp.length() > 0) {
                    if (tmp.charAt(0) == '+') {
                        if (tmp.length() > 1) {
                            positiveFilters.add(tmp.substring(1));
                        }
                    } else if (tmp.charAt(0) == '-') {
                        if (tmp.length() > 1) {
                            negativeFilters.add(tmp.substring(1));
                        }
                    } else {
                        negativeFilters.add(tmp);
                    }
                }
            }

            StringBuilder notificationText = new StringBuilder();
            for (CharSequence item : items) {
                if (item != null) {
                    notificationText.append(item).append(' ');
                }
            }

            if (notificationText.length() != 0) {
                String tmp = notificationText.toString();

                // First check for negative filters
                for (String filterText : negativeFilters) {
                    if (tmp.contains(filterText)) {
                        return true;
                    }
                }

                // Now check for positive filters
                for (String filterText : positiveFilters) {
                    if (!tmp.contains(filterText)) {
                        return true;
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
