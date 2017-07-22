package com.abhijitvalluri.android.fitnotifications.services;

import android.os.Bundle;

import com.abhijitvalluri.android.fitnotifications.utils.DebugLog;

import java.util.HashMap;
import java.util.Map;


/**
 * An extension of the <code>BasicMessageExtractor</code> that also filters out
 * every other instance of duplicate notifications
 */
class GenericMessageExtractor extends BasicMessageExtractor {

    private Map<String, String> mNotificationStringMap = new HashMap<>();


    @Override
    public CharSequence[] getTitleAndText(String appPackageName, Bundle extras, int notificationFlags) {
        DebugLog debugLog = getDebugLog();
        if (isLoggingEnabled()) {
            debugLog.writeLog("Entered GenericMessageExtractor getTitleAndText method. Calling super method.");
            debugLog.writeLog("NotificationFlags = " + notificationFlags);
        }

        CharSequence[] titleAndText = super.getTitleAndText(appPackageName, extras, notificationFlags);

        String text = titleAndText[1].toString();
        String prevNotificationText = mNotificationStringMap.put(appPackageName, text);
        // TODO: add more specific checks to avoid blocking legitimate identical notifications
        if (text.equals(prevNotificationText)) {
            // do not send the duplicate notification, but only for every 2nd occurrence
            // (i.e. when the same text arrives for the 3rd time - send it)
            if (isLoggingEnabled()) {
                debugLog.writeLog("Same notification arrived twice. Not sending it.");
            }
            mNotificationStringMap.remove(appPackageName);
            titleAndText[1] = null;
        }

        return titleAndText;
    }


}
