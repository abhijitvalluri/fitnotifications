package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.os.Bundle;

import com.abhijitvalluri.android.fitnotifications.utils.DebugLog;

/**
 * MessageExtractor that omits the group summary notifications.
 */
class IgnoreSummaryMessageExtractor extends GenericMessageExtractor {

    @Override
    public CharSequence[] getTitleAndText(String appPackageName, Bundle extras, int notificationFlags) {
        DebugLog debugLog = getDebugLog();
        if (isLoggingEnabled()) {
            debugLog.writeLog("Entered 'IgnoreSummaryMessageExtractor' getTitleAndText method");
            debugLog.writeLog("NotificationFlags = " + notificationFlags);
        }

        if ((notificationFlags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            if (isLoggingEnabled()) {
                debugLog.writeLog("Notification is a group summary. Ignoring.");
            }
            return null;
        }

        // TODO: GMail repeats non-summary notifications occasionally
        // some of them will be filtered out by the GenericMessageExtractor implementation
        // but sometimes they are not immediately one after another: M1 - M2 - M3 - M1
        // TODO: keep track of several previous messages hashes to ignore the duplicates?

        if (isLoggingEnabled()) {
            debugLog.writeLog("Calling method from super class");
        }
        return super.getTitleAndText(appPackageName, extras, notificationFlags);
    }
}
