package com.abhijitvalluri.android.fitnotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Blaze Notification Service
 */
public class NotificationListener extends NotificationListenerService {

    private static final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    private final Handler mHandler = new Handler();

    private static List<String> mSelectedAppsPackageNames;

    private static boolean mIsServiceEnabled;
    private static boolean mDismissPlaceholderNotif;
    private static boolean mDismissRelayedNotif;
    private static boolean mLimitNotifications;
    private static int mPlaceholderNotifDismissDelayMillis;
    private static int mRelayedNotifDismissDelayMillis;
    private static int mNotifLimitDurationMillis;

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
        preferences.edit().putBoolean(Constants.SERVICE_STATE, mIsServiceEnabled).apply();

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

        Toast.makeText(this, getString(R.string.notification_service_started), Toast.LENGTH_LONG).show();
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
        mPlaceholderNotifDismissDelayMillis = delaySeconds*1000;
    }

    public static void onLimitNotificationSettingUpdated(boolean limitNotif, int durationSeconds) {
        mLimitNotifications = limitNotif;
        mNotifLimitDurationMillis = durationSeconds*1000;
    }

    public static void onRelayedNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissRelayedNotif = dismissNotif;
        mRelayedNotifDismissDelayMillis = delaySeconds*1000;
    }

    @Override
    public void onNotificationPosted(final StatusBarNotification sbn) {
        // TODO(abhijitvalluri): Think about monetization options for advanced features,
        // like customizations for notification layout/delays/collation?

        if (!mIsServiceEnabled) {
            return;
        }

        Notification notification = sbn.getNotification();
        final String appPackageName = sbn.getPackageName();

        Bundle extras = notification.extras;

        if (!notificationFromSelectedApp(appPackageName)) {
            return;
        }

        if (mLimitNotifications) {
            Long currentTimeMillis = System.currentTimeMillis();
            Long lastNotificationTime = mLastNotificationTimeMap.get(appPackageName);
            if (lastNotificationTime != null
                    && currentTimeMillis < lastNotificationTime + mNotifLimitDurationMillis) {
                return;
            } else {
                mLastNotificationTimeMap.put(appPackageName, currentTimeMillis);
            }
        }


        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        String notificationText, notificationBigText;

        try {
            notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
        } catch (NullPointerException e) {
            notificationText = "";
        }

        try {
            notificationBigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
        } catch (NullPointerException e) {
            notificationBigText = "";
        }

        if (notificationBigText.length() > 0 && notificationBigText.startsWith(notificationText)) {
            notificationBigText = notificationBigText.substring(notificationText.length());
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[")
          .append(mAppSelectionsStore.getAppName(appPackageName))
          .append("] ")
          .append(notificationText);

        if (notificationBigText.length() > 0) {
            sb.append(" -- ").append(notificationBigText);
        }

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        contentView.setTextViewText(
                R.id.customNotificationText, getString(R.string.notification_text));

        builder.setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(sb.toString())
                .setContentTitle(extras.getCharSequence(Notification.EXTRA_TITLE))
                .setContent(contentView);

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());

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
                        cancelNotification(appPackageName, sbn.getTag(), sbn.getId());
                    }
                }
            }, mRelayedNotifDismissDelayMillis);
        }
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

    // Checks if the notification comes from a selected application
    private boolean notificationFromSelectedApp(String appPackageName) {
        return NotificationListener.mSelectedAppsPackageNames.contains(appPackageName);
    }

    public static void setEnabled(boolean enabled) {
        mIsServiceEnabled = enabled;
    }

}
