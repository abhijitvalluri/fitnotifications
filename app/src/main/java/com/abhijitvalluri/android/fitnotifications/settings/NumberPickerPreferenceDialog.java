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
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NumberPickerPreferenceDialog extends PreferenceDialogFragmentCompat {

    private static final String ARG_MIN_VALUE = "minValue";
    private static final String ARG_MAX_VALUE = "maxValue";
    private static final String ARG_BEFORE_TEXT = "beforeText";
    private static final String ARG_AFTER_TEXT = "afterText";

    private int minValue;
    private int maxValue;
    private String beforeText;
    private String afterText;

    private NumberPicker numberPicker;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        assert getArguments() != null; // Should never be null if created via newInstance
        minValue = getArguments().getInt(ARG_MIN_VALUE);
        maxValue = getArguments().getInt(ARG_MAX_VALUE);
        beforeText = getArguments().getString(ARG_BEFORE_TEXT);
        afterText = getArguments().getString(ARG_AFTER_TEXT);
    }

    @Override
    protected View onCreateDialogView(@NonNull Context context) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.weight = 1;

        numberPicker = new NumberPicker(getContext());
        numberPicker.setLayoutParams(layoutParams);
        numberPicker.setVerticalScrollBarEnabled(false);

        TextView beforeTV = new TextView(getContext());
        beforeTV.setText(beforeText);
        beforeTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
        beforeTV.setPadding(0,20,0,0);
        beforeTV.setGravity(Gravity.END);
        beforeTV.setTypeface(Typeface.DEFAULT_BOLD);

        TextView afterTV = new TextView(getContext());
        afterTV.setText(afterText);
        afterTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18.0f);
        afterTV.setPadding(0,0,0,20);
        afterTV.setGravity(Gravity.START);
        afterTV.setTypeface(Typeface.DEFAULT_BOLD);

        layoutParams.weight = 5;
        beforeTV.setLayoutParams(layoutParams);
        afterTV.setLayoutParams(layoutParams);

        LinearLayout dialogView = new LinearLayout(getContext());
        dialogView.setOrientation(LinearLayout.VERTICAL);
        dialogView.addView(beforeTV);
        dialogView.addView(numberPicker);
        dialogView.addView(afterTV);

        return dialogView;
    }

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        numberPicker.setMinValue(minValue);
        numberPicker.setMaxValue(maxValue);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue(((NumberPickerPreference) getPreference()).getValue());
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            numberPicker.clearFocus();
            int newValue = numberPicker.getValue();
            if (getPreference().callChangeListener(newValue)) {
                ((NumberPickerPreference) getPreference()).setValue(newValue);
            }
        }
    }

    public static NumberPickerPreferenceDialog newInstance(String key,
                                                           int minValue,
                                                           int maxValue,
                                                           String beforeText,
                                                           String afterText) {
        NumberPickerPreferenceDialog dialog = new NumberPickerPreferenceDialog();
        Bundle b = new Bundle();
        b.putString(ARG_KEY, key);
        b.putInt(ARG_MIN_VALUE, minValue);
        b.putInt(ARG_MAX_VALUE, maxValue);
        b.putString(ARG_BEFORE_TEXT, beforeText);
        b.putString(ARG_AFTER_TEXT, afterText);
        dialog.setArguments(b);

        return dialog;
    }
}
