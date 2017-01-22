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
