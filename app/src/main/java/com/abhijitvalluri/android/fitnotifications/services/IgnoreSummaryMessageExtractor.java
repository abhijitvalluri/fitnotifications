package com.abhijitvalluri.android.fitnotifications.services;

import android.app.Notification;
import android.os.Bundle;

/**
 * MessageExtractor that omits the group summary notifications.
 */
class IgnoreSummaryMessageExtractor extends GenericMessageExtractor {

    @Override
    public CharSequence[] getTitleAndText(String appPackageName, Bundle extras, int notificationFlags) {
        if ((notificationFlags & Notification.FLAG_GROUP_SUMMARY) != 0) {
            return null;
        }

        // TODO: GMail repeats non-summary notifications occasionally
        // some of them will be filtered out by the GenericMessageExtractor implementation
        // but sometimes they are not immediately one after another: M1 - M2 - M3 - M1
        // TODO: keep track of several previous messages hashes to ignore the duplicates?

        return super.getTitleAndText(appPackageName, extras, notificationFlags);
    }
}
