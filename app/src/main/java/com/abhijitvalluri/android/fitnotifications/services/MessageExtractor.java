package com.abhijitvalluri.android.fitnotifications.services;

import android.os.Bundle;


public interface MessageExtractor {

    /**
     * [0] - title
     * [1] - text
     */
    CharSequence[] getTitleAndText(String appPackageName, Bundle extras, int notificationFlags);

}
