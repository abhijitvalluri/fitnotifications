package com.abhijitvalluri.android.fitnotifications.utils;

import android.os.Build;

public class DefaultBuildVersionProvider implements BuildVersionProvider {
    @Override
    public int currentVersion() {
        return Build.VERSION.SDK_INT;
    }
}
