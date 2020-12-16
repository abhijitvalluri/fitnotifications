/*
   Copyright 2020 Abhijit Kiran Valluri

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
package com.abhijitvalluri.android.fitnotifications.settings;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;

import com.abhijitvalluri.android.fitnotifications.R;

import androidx.preference.DialogPreference;

/**
 * A {@link androidx.preference.Preference} that displays a number picker as a dialog.
 */
public class NumberPickerPreference extends DialogPreference {

    public static final int DEFAULT_MAX_VALUE = 100;
    public static final int DEFAULT_MIN_VALUE = 0;

    private final int minValue;
    private final int maxValue;
    private final String beforeText;
    private final String afterText;

    public NumberPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.NumberPickerPreference);
        minValue = a.getInteger(R.styleable.NumberPickerPreference_minValue, DEFAULT_MIN_VALUE);
        maxValue = a.getInteger(R.styleable.NumberPickerPreference_maxValue, DEFAULT_MAX_VALUE);
        beforeText = a.getString(R.styleable.NumberPickerPreference_beforeText);
        afterText = a.getString(R.styleable.NumberPickerPreference_afterText);
        a.recycle();
    }

    public NumberPickerPreferenceDialog createDialog(String key) {
        return NumberPickerPreferenceDialog.newInstance(key, minValue, maxValue, beforeText, afterText);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getInt(index, minValue);
    }

    public void setValue(int value) {
        super.persistInt(value);
        notifyChanged();
    }

    public int getValue() {
        return super.getPersistedInt(minValue);
    }
}
