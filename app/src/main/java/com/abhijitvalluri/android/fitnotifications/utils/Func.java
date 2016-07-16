package com.abhijitvalluri.android.fitnotifications.utils;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful functions for the app
 */
public class Func {

    public static List<String> getInstalledPackageNames(PackageManager pm) {
        List<String> packageNames = new ArrayList<>();
        for (ResolveInfo info : getInstalledPackages(pm)) {
            packageNames.add(info.activityInfo.packageName);
        }

        return packageNames;
    }

    public static List<ResolveInfo> getInstalledPackages(PackageManager pm) {
        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        return pm.queryIntentActivities(startupIntent, 0);



    }
}
