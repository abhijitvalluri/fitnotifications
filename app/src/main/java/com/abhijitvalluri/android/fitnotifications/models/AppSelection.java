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

package com.abhijitvalluri.android.fitnotifications.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * App Selection model for the Fit notification app.
 */
public class AppSelection implements Parcelable {

    private String mAppPackageName;
    private String mAppName;
    private boolean mIsSelected;
    private String mFilterText;
    private int mStartTimeHour;
    private int mStartTimeMinute;
    private int mStopTimeHour;
    private int mStopTimeMinute;
    private boolean mDiscardEmptyNotifications;
    private boolean mAllDaySchedule;

    public AppSelection(String appPackageName, String appName) {
        mAppPackageName = appPackageName;
        mAppName = appName;
        mFilterText = "";
        mStartTimeHour = 0;
        mStartTimeMinute = 0;
        mStopTimeHour = 23;
        mStopTimeMinute = 59;
        mAllDaySchedule = true;
    }

    public AppSelection(String appPackageName,
                        String appName,
                        boolean isSelected,
                        String filterText,
                        int startTimeHour,
                        int startTimeMinute,
                        int stopTimeHour,
                        int stopTimeMinute,
                        boolean discardEmptyNotifications,
                        boolean allDaySchedule) {
        mAppPackageName = appPackageName;
        mAppName = appName;
        mIsSelected = isSelected;
        mFilterText = filterText;
        mStartTimeHour = startTimeHour;
        mStartTimeMinute = startTimeMinute;
        mStopTimeHour = stopTimeHour;
        mStopTimeMinute = stopTimeMinute;
        mDiscardEmptyNotifications = discardEmptyNotifications;
        mAllDaySchedule = allDaySchedule;
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

    public String getFilterText() {
        return mFilterText;
    }

    public void setFilterText(String filterText) {
        mFilterText = filterText;
    }

    public int getStartTimeHour() {
        return mStartTimeHour;
    }

    public void setStartTimeHour(int startTimeHour) {
        mStartTimeHour = startTimeHour;
    }

    public int getStartTimeMinute() {
        return mStartTimeMinute;
    }

    public void setStartTimeMinute(int startTimeMinute) {
        mStartTimeMinute = startTimeMinute;
    }

    public int getStopTimeHour() {
        return mStopTimeHour;
    }

    public void setStopTimeHour(int stopTimeHour) {
        mStopTimeHour = stopTimeHour;
    }

    public int getStopTimeMinute() {
        return mStopTimeMinute;
    }

    public void setStopTimeMinute(int stopTimeMinute) {
        mStopTimeMinute = stopTimeMinute;
    }

    public boolean isDiscardEmptyNotifications() {
        return mDiscardEmptyNotifications;
    }

    public void setDiscardEmptyNotifications(boolean discardEmptyNotifications) {
        mDiscardEmptyNotifications = discardEmptyNotifications;
    }

    public boolean isAllDaySchedule() {
        return mAllDaySchedule;
    }

    public void setAllDaySchedule(boolean allDaySchedule) {
        mAllDaySchedule = allDaySchedule;
    }

    public int getStartTime() {
        return mStartTimeHour * 60 + mStartTimeMinute;
    }

    public int getStopTime() {
        return mStopTimeHour * 60 + mStopTimeMinute;
    }

    private AppSelection(Parcel in) {
        mAppPackageName = in.readString();
        mAppName = in.readString();
        mIsSelected = in.readByte() != 0x00;
        mFilterText = in.readString();
        mStartTimeHour = in.readInt();
        mStartTimeMinute = in.readInt();
        mStopTimeHour = in.readInt();
        mStopTimeMinute = in.readInt();
        mDiscardEmptyNotifications = in.readByte() != 0x00;
        mAllDaySchedule = in.readByte() != 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAppPackageName);
        dest.writeString(mAppName);
        dest.writeByte((byte) (mIsSelected ? 0x01 : 0x00));
        dest.writeString(mFilterText);
        dest.writeInt(mStartTimeHour);
        dest.writeInt(mStartTimeMinute);
        dest.writeInt(mStopTimeHour);
        dest.writeInt(mStopTimeMinute);
        dest.writeByte((byte) (mDiscardEmptyNotifications ? 0x01 : 0x00));
        dest.writeByte((byte) (mAllDaySchedule ? 0x01 : 0x00));
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<AppSelection> CREATOR = new Parcelable.Creator<AppSelection>() {
        @Override
        public AppSelection createFromParcel(Parcel in) {
            return new AppSelection(in);
        }

        @Override
        public AppSelection[] newArray(int size) {
            return new AppSelection[size];
        }
    };
}
