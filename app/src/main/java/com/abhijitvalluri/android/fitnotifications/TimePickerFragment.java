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

package com.abhijitvalluri.android.fitnotifications;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.utils.Func;

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
    private String mOtherTimeFormatted;
    private int mRequestCode;

    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour = getArguments().getInt(ARG_HOUR);
        int minute = getArguments().getInt(ARG_MINUTE);
        mOtherHour = getArguments().getInt(ARG_OTHER_HOUR);
        mOtherMinute = getArguments().getInt(ARG_OTHER_MINUTE);
        mOtherTimeFormatted = DateFormat.getTimeFormat(getActivity()).format(
                Func.convertHourMinute2Date(mOtherHour, mOtherMinute));
        mRequestCode = getArguments().getInt(ARG_REQUEST_CODE);

        View v = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_time, null);

        mTimePicker = (TimePicker) v.findViewById(R.id.dialog_time_time_picker);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mTimePicker.setHour(hour);
            mTimePicker.setMinute(minute);
        } else {
            //noinspection deprecation
            mTimePicker.setCurrentHour(hour);
            //noinspection deprecation
            mTimePicker.setCurrentMinute(minute);
        }
        mTimePicker.setIs24HourView(DateFormat.is24HourFormat(getActivity()));

        @StringRes int titleStringId = mRequestCode == AppSettingsActivity.START_TIME_REQUEST ?
                R.string.start_time_heading : R.string.stop_time_heading;

        final AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle(titleStringId)
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int hour, minute;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            hour = mTimePicker.getHour();
                            minute = mTimePicker.getMinute();
                        } else {
                            //noinspection deprecation
                            hour = mTimePicker.getCurrentHour();
                            //noinspection deprecation
                            minute = mTimePicker.getCurrentMinute();
                        }
                        sendResult(mRequestCode, hour, minute);
                    }
                })
                .create();

        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                int hour, minute;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    hour = mTimePicker.getHour();
                    minute = mTimePicker.getMinute();
                } else {
                    //noinspection deprecation
                    hour = mTimePicker.getCurrentHour();
                    //noinspection deprecation
                    minute = mTimePicker.getCurrentMinute();
                }
                Button positiveButton = ((AlertDialog)dialog).getButton(DialogInterface.BUTTON_POSITIVE);
                sanityCheckTimeChoice(positiveButton, hour, minute);
            }
        });

        mTimePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                Button positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);

                sanityCheckTimeChoice(positiveButton, hourOfDay, minute);
            }
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
            Toast.makeText(getContext(), errToast, Toast.LENGTH_SHORT).show();
        } else {
            positiveButton.setEnabled(true);
        }
    }

    private void sendResult(int requestCode, int hour, int minute) {
        Intent intent = new Intent();
        intent.putExtra(EXTRA_HOUR, hour);
        intent.putExtra(EXTRA_MINUTE, minute);

        TimePickerListener listener = (TimePickerListener) getContext();
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
