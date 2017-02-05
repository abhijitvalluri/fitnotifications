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

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.abhijitvalluri.android.fitnotifications.models.AppSelection;
import com.abhijitvalluri.android.fitnotifications.utils.Func;

import java.util.Date;

/**
 * AppSettingsActivity is an activity that allows user to set additional preferences regarding each app
 */
public class AppSettingsActivity extends AppCompatActivity implements TimePickerFragment.TimePickerListener {

    public static final int START_TIME_REQUEST = 0;
    public static final int STOP_TIME_REQUEST = 1;
    public static final String APP_SELECTION_EXTRA = "appSelectionExtra";

    public static final String STATE_APP_SELECTION = "appSelection";
    public static final String STATE_START_TIME_HOUR = "startTimeHour";
    public static final String STATE_START_TIME_MINUTE = "startTimeMinute";
    public static final String STATE_STOP_TIME_HOUR = "stopTimeHour";
    public static final String STATE_STOP_TIME_MINUTE = "stopTimeMinute";
    public static final String STATE_DISCARD_EMPTY_NOTIFICATIONS = "discardEmptyNotifications";

    private static final String DIALOG_TIME = "dialogTime";

    private AppSelection mAppSelection;
    private EditText mFilterText;
    private Button mStartTimeButton;
    private Button mStopTimeButton;
    private Switch mSwitch;
    private int mStartTimeHour;
    private int mStartTimeMinute;
    private int mStopTimeHour;
    private int mStopTimeMinute;
    private boolean mDiscardEmptyNotifications;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        mFilterText = (EditText) findViewById(R.id.filterText);
        mStartTimeButton = (Button) findViewById(R.id.start_time);
        mStopTimeButton = (Button) findViewById(R.id.stop_time);
        mSwitch = (Switch) findViewById(R.id.discard_empty);

        if (savedInstanceState == null) {
            mAppSelection = getIntent().getParcelableExtra(APP_SELECTION_EXTRA);
            mStartTimeHour = mAppSelection.getStartTimeHour();
            mStartTimeMinute = mAppSelection.getStartTimeMinute();
            mStopTimeHour = mAppSelection.getStopTimeHour();
            mStopTimeMinute = mAppSelection.getStopTimeMinute();
            mDiscardEmptyNotifications = mAppSelection.isDiscardEmptyNotifications();
        } else {
            mAppSelection = savedInstanceState.getParcelable(STATE_APP_SELECTION);
            mStartTimeHour = savedInstanceState.getInt(STATE_START_TIME_HOUR);
            mStartTimeMinute = savedInstanceState.getInt(STATE_START_TIME_MINUTE);
            mStopTimeHour = savedInstanceState.getInt(STATE_STOP_TIME_HOUR);
            mStopTimeMinute = savedInstanceState.getInt(STATE_STOP_TIME_MINUTE);
            mDiscardEmptyNotifications = savedInstanceState.getBoolean(STATE_DISCARD_EMPTY_NOTIFICATIONS);
        }

        mSwitch.setChecked(mDiscardEmptyNotifications);
        setTitle(mAppSelection.getAppName());

        mStartTimeButton.setText(
                android.text.format.DateFormat.format(
                        "h:mm a",
                        Func.convertHourMinute2Date(mStartTimeHour, mStartTimeMinute)));

        mStopTimeButton.setText(
                android.text.format.DateFormat.format(
                        "h:mm a",
                        Func.convertHourMinute2Date(mStopTimeHour, mStopTimeMinute)));

        mFilterText.setText(mAppSelection.getFilterText());

        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDiscardEmptyNotifications = mSwitch.isChecked();
            }
        });

        mStartTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mStartTimeHour,
                                                                           mStartTimeMinute,
                                                                           mStopTimeHour,
                                                                           mStopTimeMinute,
                                                                           R.string.start_time_heading,
                                                                           START_TIME_REQUEST);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mStopTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getSupportFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mStopTimeHour,
                                                                           mStopTimeMinute,
                                                                           mStartTimeHour,
                                                                           mStartTimeMinute,
                                                                           R.string.stop_time_heading,
                                                                           STOP_TIME_REQUEST);
                dialog.show(manager, DIALOG_TIME);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_APP_SELECTION, mAppSelection);
        outState.putInt(STATE_START_TIME_HOUR, mStartTimeHour);
        outState.putInt(STATE_START_TIME_MINUTE, mStartTimeMinute);
        outState.putInt(STATE_STOP_TIME_HOUR, mStopTimeHour);
        outState.putInt(STATE_STOP_TIME_MINUTE, mStopTimeMinute);
        outState.putBoolean(STATE_DISCARD_EMPTY_NOTIFICATIONS, mDiscardEmptyNotifications);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        mAppSelection.setFilterText(mFilterText.getText().toString());
        mAppSelection.setStartTimeHour(mStartTimeHour);
        mAppSelection.setStartTimeMinute(mStartTimeMinute);
        mAppSelection.setStopTimeHour(mStopTimeHour);
        mAppSelection.setStopTimeMinute(mStopTimeMinute);
        mAppSelection.setDiscardEmptyNotifications(mDiscardEmptyNotifications);

        Intent intent = new Intent();
        intent.putExtra(APP_SELECTION_EXTRA, mAppSelection);
        if (getParent() == null) {
            setResult(RESULT_OK, intent);
        } else {
            getParent().setResult(RESULT_OK, intent);
        }

        finish();
        overridePendingTransition(R.transition.right_in, R.transition.right_out);
        super.onBackPressed();
    }

    @Override
    public void onActivityResult2(int requestCode, Intent data) {
        int hour = data.getIntExtra(TimePickerFragment.EXTRA_HOUR, 0);
        int minute = data.getIntExtra(TimePickerFragment.EXTRA_MINUTE, 0);
        if (requestCode == START_TIME_REQUEST) {
            mStartTimeHour = hour;
            mStartTimeMinute = minute;
            Date date = Func.convertHourMinute2Date(hour, minute);
            mStartTimeButton.setText(android.text.format.DateFormat.format("h:mm a", date));
        } else if (requestCode == STOP_TIME_REQUEST) {
            mStopTimeHour = hour;
            mStopTimeMinute = minute;
            Date date = Func.convertHourMinute2Date(hour, minute);
            mStopTimeButton.setText(android.text.format.DateFormat.format("h:mm a", date));
        }
    }
}
