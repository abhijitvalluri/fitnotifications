package com.abhijitvalluri.android.fitnotifications.services;

import android.os.Bundle;

import com.abhijitvalluri.android.fitnotifications.utils.DebugLog;


abstract class MessageExtractor {

    private boolean mLoggingEnabled = false;
    private DebugLog mDebugLog = null;

    /**
     * [0] - title
     * [1] - text
     */
    abstract CharSequence[] getTitleAndText(String appPackageName, Bundle extras, int notificationFlags);

    boolean isLoggingEnabled() {
        return mLoggingEnabled;
    }

    void setLoggingEnabled(boolean loggingEnabled) {
        mLoggingEnabled = loggingEnabled;
    }

    DebugLog getDebugLog() {
        return mDebugLog;
    }

    void setDebugLog(DebugLog debugLog) {
        mDebugLog = debugLog;
    }
}
