package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v7.app.NotificationCompat;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.SettingsActivity;
import com.abhijitvalluri.android.fitnotifications.utils.AppSelectionsStore;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.R;
import com.ibm.icu.text.Transliterator;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Blaze Notification Service
 */
public class NLService extends NotificationListenerService {

    private static final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
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

    public static void onSplitNotificationSettingUpdated(boolean enabled,
                                                         int charLimit,
                                                         int numSplitNotifs) {
        mSplitNotification = enabled;
        mFitbitNotifCharLimit = charLimit;
        mNumSplitNotifications = numSplitNotifs;
    }

    public static void onLimitNotificationSettingUpdated(boolean limitNotif, int durationSeconds) {
        mLimitNotifications = limitNotif;
        mNotifLimitDurationMillis = durationSeconds*1000;
    }

    public static void onRelayedNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissRelayedNotif = dismissNotif;
        mRelayedNotifDismissDelayMillis = delaySeconds*1000;
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
        // TODO(abhijitvalluri): Think about monetization options for advanced features,
        // like customizations for notification layout/delays/collation?

        if (!mIsServiceEnabled) {
            return;
        }

        if (mDisableWhenScreenOn) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            boolean isScreenOn;
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {
                // API >= 20
                isScreenOn = pm.isInteractive();
            } else {
                // API <= 19, use deprecated
                isScreenOn = pm.isScreenOn();
            }

            if (isScreenOn) {
                return;
            }
        }

        Notification notification = sbn.getNotification();
        final String appPackageName = sbn.getPackageName();

        Bundle extras = notification.extras;

        // DISREGARD SPAMMY NOTIFICATIONS
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            if ((notification.flags & Notification.FLAG_LOCAL_ONLY) > 0 ||
                (notification.flags & Notification.FLAG_GROUP_SUMMARY) > 0 ||
                (notification.flags & Notification.FLAG_ONGOING_EVENT) > 0) {
                // Do not process notification if it is spammy
                return;
            }
        } else {
            if ((notification.flags & Notification.FLAG_ONGOING_EVENT) > 0) {
                // For API 19, this fixes Facebook messenger but NOT Gmail. But that is good,
                // as Gmail does not send a notification with non-empty text anyway when it is
                // notifying about multiple email. Dig deeper and get extra info somehow? TODO
                return;
            }
        }

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
            if (mTransliterateNotif) {
                notificationText = Transliterator.getInstance("Any-Latin")
                        .transform(extras.getCharSequence(Notification.EXTRA_TEXT).toString());
            } else {
                notificationText = extras.getCharSequence(Notification.EXTRA_TEXT).toString();
            }
        } catch (NullPointerException e) {
            notificationText = "";
        }

        try {
            if (mTransliterateNotif) {
                notificationBigText = Transliterator.getInstance("Any-Latin")
                        .transform(extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString()); // TODO: Apparently need minimum API 21 to use EXTRA_BIG_TEXT
            } else {
                notificationBigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
            }
        } catch (NullPointerException e) {
            notificationBigText = "";
        }

        if (notificationBigText.length() > 0 && notificationBigText.startsWith(notificationText)) {
            notificationBigText = notificationBigText.substring(notificationText.length());
        }

        StringBuilder sb = new StringBuilder();
        if (mDisplayAppName) {
            sb.append("[")
              .append(mAppSelectionsStore.getAppName(appPackageName))
              .append("] ");
        }

        sb.append(notificationText);

        if (notificationBigText.length() > 0) {
            sb.append(" -- ").append(notificationBigText);
        }

        RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
        contentView.setTextViewText(
                R.id.customNotificationText, getString(R.string.notification_text));

        builder.setSmallIcon(R.drawable.ic_sms_white_24dp)
                .setContent(contentView);

        if (mTransliterateNotif) {
            try {
                builder.setContentTitle(Transliterator.getInstance("Any-Latin")
                        .transform(extras.getCharSequence(Notification.EXTRA_TITLE).toString()));
            } catch (NullPointerException e) {
                builder.setContentTitle(extras.getCharSequence(Notification.EXTRA_TITLE));
            }
        } else {
            builder.setContentTitle(extras.getCharSequence(Notification.EXTRA_TITLE));
        }

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
        PendingIntent settingsPendingIntent =
                stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        builder.setContentIntent(settingsPendingIntent).setAutoCancel(true);

        StringBuilder notifStrB = new StringBuilder(sb.toString().trim().replaceAll("\\s+", " "));

        if (mSplitNotification && notifStrB.length() > mFitbitNotifCharLimit) {
            int notifCount = 1; // start from 1 to send one less within the while loop

            int charLimit = mFitbitNotifCharLimit - 7; // 7 chars for "... [1]" with changing number
            while (notifCount < mNumSplitNotifications && notifStrB.length() > mFitbitNotifCharLimit) {
                String partialText;
                int whiteSpaceIndex = notifStrB.lastIndexOf(" ", charLimit);

                if (whiteSpaceIndex > 0) {
                    partialText = notifStrB.substring(0, whiteSpaceIndex);
                    notifStrB.delete(0, whiteSpaceIndex+1);
                } else {
                    partialText = notifStrB.substring(0, charLimit);
                    notifStrB.delete(0, charLimit);
                }

                partialText = partialText.concat("... [" + notifCount + "]");

                builder.setContentText(partialText);
                final Notification notif = builder.build();

                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNotificationManager.notify(NOTIFICATION_ID, notif);
                    }
                }, 500*notifCount);

                notifCount++;
            }

            if (notifStrB.length() > 0) {
                builder.setContentText(notifStrB.toString());
                final Notification notif = builder.build();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mNotificationManager.notify(NOTIFICATION_ID, notif);
                    }
                }, 500*notifCount);
            }
        } else { // Do not split the notification
            builder.setContentText(sb.toString());
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
        return NLService.mSelectedAppsPackageNames.contains(appPackageName);
    }

    public static void setEnabled(boolean enabled) {
        mIsServiceEnabled = enabled;
    }

    public static boolean isEnabled() {
        return mIsServiceEnabled;
    }

}
