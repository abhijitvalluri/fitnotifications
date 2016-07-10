package com.abhijitvalluri.android.fitnotifications.models;

/**
 * App Selection model for the Fit notification app.
 */
public class AppSelection {


    private String mAppPackageName;
    private String mAppName;
    private boolean mIsSelected;

    public AppSelection(String appPackageName, String appName) {
        mAppPackageName = appPackageName;
        mAppName = appName;
    }

    public AppSelection(String appPackageName, String appName, boolean isSelected) {
        mAppPackageName = appPackageName;
        mAppName = appName;
        mIsSelected = isSelected;
    }

    public boolean isSelected() {
        return mIsSelected;
    }

    public void setSelected(boolean selected) {
        mIsSelected = selected;
    }

    public String getAppPackageName() {
        return mAppPackageName;
    }

    public String getAppName() {
        return mAppName;
    }
}
