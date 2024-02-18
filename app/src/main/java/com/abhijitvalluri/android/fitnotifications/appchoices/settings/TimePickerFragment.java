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

package com.abhijitvalluri.android.fitnotifications.appchoices.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.R;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

/**
 * TimePickerFragment hosts the time picker dialog.
 */
public class TimePickerFragment extends DialogFragment {
    public static final String EXTRA_HOUR =
            "com.abhijitvalluri.android.fitnotifications.hour";
    public static final String EXTRA_MINUTE =
            "com.abhijitvalluri.android.fitnotifications.minute";

    private static final String ARG_HOUR = "hour";
    private static final String ARG_MINUTE = "minute";
    private static final String ARG_OTHER_HOUR = "otherHour";
    private static final String ARG_OTHER_MINUTE = "otherMinute";
    private static final String ARG_REQUEST_CODE = "resultCode";

    private TimePicker mTimePicker;
    private int mOtherHour;
    private int mOtherMinute;
    private int mRequestCode;

    private Context mContext;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
    }

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        assert getArguments() != null; // Should never be null if created via newInstance
        int hour = getArguments().getInt(ARG_HOUR);
        int minute = getArguments().getInt(ARG_MINUTE);
        mOtherHour = getArguments().getInt(ARG_OTHER_HOUR);
        mOtherMinute = getArguments().getInt(ARG_OTHER_MINUTE);
        mRequestCode = getArguments().getInt(ARG_REQUEST_CODE);

        View v = LayoutInflater.from(mContext).inflate(R.layout.dialog_time, null);

        mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_time_picker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minute);
        } else {
            mTimePicker.setCurrentHour(hour);
            mTimePicker.setCurrentMinute(minute);
        }
        mTimePicker.setIs24HourView(DateFormat.is24HourFormat(mContext));

        @StringRes int titleStringId = mRequestCode == AppSettingsActivity.START_TIME_REQUEST ?
                R.string.start_time_heading : R.string.stop_time_heading;

        final AlertDialog dialog = new AlertDialog.Builder(mContext, R.style.TimePickerAlertDialogTheme)
                .setView(v)
                .setTitle(titleStringId)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, (dialog1, which) -> {
                    int hour1, minute1;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        hour1 = mTimePicker.getHour();
                        minute1 = mTimePicker.getMinute();
                    } else {
                        hour1 = mTimePicker.getCurrentHour();
                        minute1 = mTimePicker.getCurrentMinute();
                    }
                    sendResult(mRequestCode, hour1, minute1);
                })
                .create();

        dialog.setOnShowListener(dialog12 -> {
            int hour12, minute12;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                hour12 = mTimePicker.getHour();
                minute12 = mTimePicker.getMinute();
            } else {
                hour12 = mTimePicker.getCurrentHour();
                minute12 = mTimePicker.getCurrentMinute();
            }
            Button positiveButton = ((AlertDialog) dialog12).getButton(DialogInterface.BUTTON_POSITIVE);
            sanityCheckTimeChoice(positiveButton, hour12, minute12);
        });

        mTimePicker.setOnTimeChangedListener((view, hourOfDay, minute13) -> {
            Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

            sanityCheckTimeChoice(positiveButton, hourOfDay, minute13);
        });

        return dialog;
    }

    private void sanityCheckTimeChoice(Button positiveButton, int hour, int minute) {
        int time = hour * 60 + minute;
        int otherTime = mOtherHour * 60 + mOtherMinute;

        if (time == otherTime) {
            String errToast;
            positiveButton.setEnabled(false);
            if (mRequestCode == AppSettingsActivity.START_TIME_REQUEST) {
                errToast = getString(R.string.start_time_error);
            } else {
                errToast = getString(R.string.stop_time_error);
            }
            Toast.makeText(mContext, errToast, Toast.LENGTH_SHORT).show();
        } else {
            positiveButton.setEnabled(true);
        }
    }

    private void sendResult(int requestCode, int hour, int minute) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_HOUR, hour);
        intent.putExtra(EXTRA_MINUTE, minute);

        TimePickerListener listener = (TimePickerListener) mContext;
        listener.onActivityResult2(requestCode, intent);
    }

    public static TimePickerFragment newInstance(int hour,
                                                 int minute,
                                                 int otherHour,
                                                 int otherMinute,
                                                 int requestCode) {
        Bundle args = new Bundle();
        args.putInt(ARG_HOUR, hour);
        args.putInt(ARG_MINUTE, minute);
        args.putInt(ARG_OTHER_HOUR, otherHour);
        args.putInt(ARG_OTHER_MINUTE, otherMinute);
        args.putInt(ARG_REQUEST_CODE, requestCode);

        TimePickerFragment fragment = new TimePickerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public interface TimePickerListener {
        void  onActivityResult2(int requestCode, Intent data);
    }
}
