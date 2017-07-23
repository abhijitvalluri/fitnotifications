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

package com.abhijitvalluri.android.fitnotifications.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Useful functions for the app
 */
public class Func {

    public static List<String> getInstalledPackageNames(PackageManager pm, Context context) {
        List<String> packageNames = new ArrayList<>();
        for (ResolveInfo info : getInstalledPackages(pm, context)) {
            packageNames.add(info.activityInfo.packageName);
        }

        DebugLog log = DebugLog.get(context);
        if (log.isEnabled()) {
            log.writeLog("In getInstalledPackageNames: Got " + packageNames.size() + " apps via getInstalledPackages.");
        }

        return packageNames;
    }

    public static List<ResolveInfo> getInstalledPackages(PackageManager pm, Context context) {
        DebugLog log = DebugLog.get(context);
        if (log.isEnabled()) {
            log.writeLog("Getting installed packages. Will try a few different methods to see if I receive a suitable app list.");
        }

        Intent startupIntent = new Intent(Intent.ACTION_MAIN);
        startupIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(startupIntent, 0);

        if (log.isEnabled()) {
            log.writeLog("Got " + resolveInfos.size() + " apps via queryIntentActivities.");
        }

        List<ApplicationInfo> appInfos = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        if (log.isEnabled()) {
            log.writeLog("Got " + appInfos.size() + " apps via getInstalledApplications with GET_META_DATA.");
        }

        appInfos = pm.getInstalledApplications(0);
        if (log.isEnabled()) {
            log.writeLog("Got " + appInfos.size() + " apps via getInstalledApplications with no flags");
        }

        return resolveInfos;
    }

    public static Date convertHourMinute2Date(int hour, int minute) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, 0);

        return cal.getTime();
    }
}
