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

import androidx.appcompat.widget.SwitchCompat;
import androidx.fragment.app.FragmentManager;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.abhijitvalluri.android.fitnotifications.models.AppSelection;
import com.abhijitvalluri.android.fitnotifications.utils.Func;

/**
 * AppSettingsActivity is an activity that allows user to set additional preferences regarding each app
 */
public class AppSettingsActivity extends AppCompatActivity implements TimePickerFragment.TimePickerListener {

    public static final int START_TIME_REQUEST = 0;
    public static final String APP_SELECTION_EXTRA = "appSelectionExtra";

    private static final int STOP_TIME_REQUEST = 1;
    private static final String STATE_APP_SELECTION = "appSelection";
    private static final String STATE_START_TIME_HOUR = "startTimeHour";
    private static final String STATE_START_TIME_MINUTE = "startTimeMinute";
    private static final String STATE_STOP_TIME_HOUR = "stopTimeHour";
    private static final String STATE_STOP_TIME_MINUTE = "stopTimeMinute";
    private static final String STATE_DISCARD_EMPTY_NOTIFICATIONS = "discardEmptyNotifications";
    private static final String STATE_DISCARD_ONGOING_NOTIFICATIONS = "discardOngoingNotifications";
    private static final String STATE_ALL_DAY_SCHEDULE = "allDaySchedule";
    private static final String STATE_DAYS_OF_WEEK = "daysOfWeek";

    private static final String DIALOG_TIME = "dialogTime";

    private static final int SUNDAY     = 0b0000001;
    private static final int MONDAY     = 0b0000010;
    private static final int TUESDAY    = 0b0000100;
    private static final int WEDNESDAY  = 0b0001000;
    private static final int THURSDAY   = 0b0010000;
    private static final int FRIDAY     = 0b0100000;
    private static final int SATURDAY   = 0b1000000;

    private AppSelection mAppSelection;
    private EditText mFilterText;
    private TextView mNextDay;
    private Button mStartTimeButton;
    private Button mStopTimeButton;

    private Button mSundayBtn;
    private Button mMondayBtn;
    private Button mTuesdayBtn;
    private Button mWednesdayBtn;
    private Button mThursdayBtn;
    private Button mFridayBtn;
    private Button mSaturdayBtn;
    private int mDaysOfWeek;

    private int mStartTimeHour;
    private int mStartTimeMinute;
    private int mStopTimeHour;
    private int mStopTimeMinute;
    private boolean mDiscardEmptyNotifications;
    private boolean mDiscardOngoingNotifications;
    private boolean mAllDaySchedule;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        SwitchCompat discardEmptySwitch;
        SwitchCompat discardOngoingSwitch;
        SwitchCompat allDaySwitch;
        ImageButton filterTextInfo;
        TextView filterTextDescription;

        mFilterText = (EditText) findViewById(R.id.filter_text);
        mStartTimeButton = (Button) findViewById(R.id.start_time);
        mStopTimeButton = (Button) findViewById(R.id.stop_time);
        mNextDay = (TextView) findViewById(R.id.next_day);
        discardEmptySwitch = (SwitchCompat) findViewById(R.id.discard_empty);
        discardOngoingSwitch = (SwitchCompat) findViewById(R.id.discard_ongoing);
        allDaySwitch = (SwitchCompat) findViewById(R.id.all_day);
        filterTextInfo = findViewById(R.id.filter_text_info);
        filterTextDescription = findViewById(R.id.filter_text_desc);

        filterTextInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterTextInstructions();
            }
        });

        filterTextDescription.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterTextInstructions();
            }
        });

        mFilterText.setHorizontallyScrolling(false);
        mFilterText.setMaxLines(5);

        mSundayBtn = (Button) findViewById(R.id.button_sunday);
        mMondayBtn = (Button) findViewById(R.id.button_monday);
        mTuesdayBtn = (Button) findViewById(R.id.button_tuesday);
        mWednesdayBtn = (Button) findViewById(R.id.button_wednesday);
        mThursdayBtn = (Button) findViewById(R.id.button_thursday);
        mFridayBtn = (Button) findViewById(R.id.button_friday);
        mSaturdayBtn = (Button) findViewById(R.id.button_saturday);

        if (savedInstanceState == null) {
            mAppSelection = getIntent().getParcelableExtra(APP_SELECTION_EXTRA);
            assert mAppSelection != null; // Should always be true.
            mStartTimeHour = mAppSelection.getStartTimeHour();
            mStartTimeMinute = mAppSelection.getStartTimeMinute();
            mStopTimeHour = mAppSelection.getStopTimeHour();
            mStopTimeMinute = mAppSelection.getStopTimeMinute();
            mDiscardEmptyNotifications = mAppSelection.isDiscardEmptyNotifications();
            mDiscardOngoingNotifications = mAppSelection.isDiscardOngoingNotifications();
            mAllDaySchedule = mAppSelection.isAllDaySchedule();
            mDaysOfWeek = mAppSelection.getDaysOfWeek();
        } else {
            mAppSelection = savedInstanceState.getParcelable(STATE_APP_SELECTION);
            mStartTimeHour = savedInstanceState.getInt(STATE_START_TIME_HOUR);
            mStartTimeMinute = savedInstanceState.getInt(STATE_START_TIME_MINUTE);
            mStopTimeHour = savedInstanceState.getInt(STATE_STOP_TIME_HOUR);
            mStopTimeMinute = savedInstanceState.getInt(STATE_STOP_TIME_MINUTE);
            mDiscardEmptyNotifications = savedInstanceState.getBoolean(STATE_DISCARD_EMPTY_NOTIFICATIONS);
            mDiscardOngoingNotifications = savedInstanceState.getBoolean(STATE_DISCARD_ONGOING_NOTIFICATIONS);
            mAllDaySchedule = savedInstanceState.getBoolean(STATE_ALL_DAY_SCHEDULE);
            mDaysOfWeek = savedInstanceState.getInt(STATE_DAYS_OF_WEEK);
        }

        discardEmptySwitch.setChecked(mDiscardEmptyNotifications);
        discardOngoingSwitch.setChecked(mDiscardOngoingNotifications);
        setTitle(mAppSelection.getAppName());
        mFilterText.setText(mAppSelection.getFilterText());

        allDaySwitch.setChecked(mAllDaySchedule);
        setupScheduleSettings();
        setupWeekdayButtons();
        setupWeekdayButtonsOnClickListeners();

        discardEmptySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDiscardEmptyNotifications = isChecked;
            }
        });

        discardOngoingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mDiscardOngoingNotifications = isChecked;
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
                                                                           STOP_TIME_REQUEST);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        allDaySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mAllDaySchedule = isChecked;
                setupScheduleSettings();
            }
        });
    }

    private void showFilterTextInstructions() {
        new AlertDialog.Builder(AppSettingsActivity.this)
                .setTitle(getString(R.string.filter_text_instructions_title))
                .setMessage(getString(R.string.filter_text_instructions_message))
                .setPositiveButton(android.R.string.ok, null)
                .create()
                .show();
    }

    private void setupWeekdayButtonsOnClickListeners() {
        mSundayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mDaysOfWeek & SUNDAY) > 0) { // This means Sunday was already selected. Now, turn it off.
                    mDaysOfWeek &= ~SUNDAY;
                    mSundayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_inactive));
                } else { // AppSettingsActivity.this means Sunday was already off. Now, turn it on.
                    mDaysOfWeek |= SUNDAY;
                    mSundayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_active));
                }
            }
        });

        mMondayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mDaysOfWeek & MONDAY) > 0) {
                    mDaysOfWeek &= ~MONDAY;
                    mMondayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_inactive));
                } else {
                    mDaysOfWeek |= MONDAY;
                    mMondayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_active));
                }
            }
        });

        mTuesdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mDaysOfWeek & TUESDAY) > 0) {
                    mDaysOfWeek &= ~TUESDAY;
                    mTuesdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_inactive));
                } else {
                    mDaysOfWeek |= TUESDAY;
                    mTuesdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_active));
                }
            }
        });

        mWednesdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mDaysOfWeek & WEDNESDAY) > 0) {
                    mDaysOfWeek &= ~WEDNESDAY;
                    mWednesdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_inactive));
                } else {
                    mDaysOfWeek |= WEDNESDAY;
                    mWednesdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_active));
                }
            }
        });

        mThursdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mDaysOfWeek & THURSDAY) > 0) {
                    mDaysOfWeek &= ~THURSDAY;
                    mThursdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_inactive));
                } else {
                    mDaysOfWeek |= THURSDAY;
                    mThursdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_active));
                }
            }
        });

        mFridayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mDaysOfWeek & FRIDAY) > 0) {
                    mDaysOfWeek &= ~FRIDAY;
                    mFridayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_inactive));
                } else {
                    mDaysOfWeek |= FRIDAY;
                    mFridayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_active));
                }
            }
        });

        mSaturdayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((mDaysOfWeek & SATURDAY) > 0) {
                    mDaysOfWeek &= ~SATURDAY;
                    mSaturdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_inactive));
                } else {
                    mDaysOfWeek |= SATURDAY;
                    mSaturdayBtn.setBackground(ContextCompat.getDrawable(AppSettingsActivity.this, R.drawable.button_bg_round_active));
                }
            }
        });

    }

    private void setupWeekdayButtons() {
        if ((mDaysOfWeek & SUNDAY) > 0) {
            mSundayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_active));
        } else {
            mSundayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_inactive));
        }

        if ((mDaysOfWeek & MONDAY) > 0) {
            mMondayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_active));
        } else {
            mMondayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_inactive));
        }

        if ((mDaysOfWeek & TUESDAY) > 0) {
            mTuesdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_active));
        } else {
            mTuesdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_inactive));
        }

        if ((mDaysOfWeek & WEDNESDAY) > 0) {
            mWednesdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_active));
        } else {
            mWednesdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_inactive));
        }

        if ((mDaysOfWeek & THURSDAY) > 0) {
            mThursdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_active));
        } else {
            mThursdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_inactive));
        }

        if ((mDaysOfWeek & FRIDAY) > 0) {
            mFridayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_active));
        } else {
            mFridayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_inactive));
        }

        if ((mDaysOfWeek & SATURDAY) > 0) {
            mSaturdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_active));
        } else {
            mSaturdayBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.button_bg_round_inactive));
        }

    }

    private void setupScheduleSettings() {
        if (mAllDaySchedule) {
            mStartTimeButton.setEnabled(false);
            mStartTimeButton.setBackgroundColor(0x649e9e9e);
            mStartTimeButton.setText(R.string.schedule_disabled);
            mStopTimeButton.setEnabled(false);
            mStopTimeButton.setBackgroundColor(0x649e9e9e);
            mStopTimeButton.setText(R.string.schedule_disabled);
            mNextDay.setVisibility(View.INVISIBLE);
        } else {
            mStartTimeButton.setEnabled(true);
            mStopTimeButton.setEnabled(true);

            mStartTimeButton.setBackgroundColor(0xffff4081);
            mStopTimeButton.setBackgroundColor(0xffff4081);

            final java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);

            mStartTimeButton.setText(timeFormat.format(
                    Func.convertHourMinute2Date(mStartTimeHour, mStartTimeMinute)));

            mStopTimeButton.setText(timeFormat.format(
                    Func.convertHourMinute2Date(mStopTimeHour, mStopTimeMinute)));

            int startTime = mStartTimeHour * 60 + mStartTimeMinute;
            int stopTime = mStopTimeHour * 60 + mStopTimeMinute;

            if (startTime > stopTime) {
                mNextDay.setVisibility(View.VISIBLE);
            } else {
                mNextDay.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(STATE_APP_SELECTION, mAppSelection);
        outState.putInt(STATE_START_TIME_HOUR, mStartTimeHour);
        outState.putInt(STATE_START_TIME_MINUTE, mStartTimeMinute);
        outState.putInt(STATE_STOP_TIME_HOUR, mStopTimeHour);
        outState.putInt(STATE_STOP_TIME_MINUTE, mStopTimeMinute);
        outState.putBoolean(STATE_DISCARD_EMPTY_NOTIFICATIONS, mDiscardEmptyNotifications);
        outState.putBoolean(STATE_ALL_DAY_SCHEDULE, mAllDaySchedule);
        outState.putBoolean(STATE_DISCARD_ONGOING_NOTIFICATIONS, mDiscardOngoingNotifications);
        outState.putInt(STATE_DAYS_OF_WEEK, mDaysOfWeek);

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        mAppSelection.setFilterText(mFilterText.getText().toString());
        mAppSelection.setStartTimeHour(mStartTimeHour);
        mAppSelection.setStartTimeMinute(mStartTimeMinute);
        mAppSelection.setStopTimeHour(mStopTimeHour);
        mAppSelection.setStopTimeMinute(mStopTimeMinute);
        mAppSelection.setDiscardEmptyNotifications(mDiscardEmptyNotifications);
        mAppSelection.setDiscardOngoingNotifications(mDiscardOngoingNotifications);
        mAppSelection.setAllDaySchedule(mAllDaySchedule);
        mAppSelection.setDaysOfWeek(mDaysOfWeek);

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

        java.text.DateFormat timeFormat = DateFormat.getTimeFormat(this);
        String formattedTime = timeFormat.format(Func.convertHourMinute2Date(hour, minute));
        int time = hour * 60 + minute;

        if (requestCode == START_TIME_REQUEST) {
            mStartTimeHour = hour;
            mStartTimeMinute = minute;
            mStartTimeButton.setText(formattedTime);
            int stopTime = mStopTimeHour * 60 + mStopTimeMinute;
            if (time > stopTime) {
                mNextDay.setVisibility(View.VISIBLE);
            } else {
                mNextDay.setVisibility(View.INVISIBLE);
            }

        } else if (requestCode == STOP_TIME_REQUEST) {
            mStopTimeHour = hour;
            mStopTimeMinute = minute;
            mStopTimeButton.setText(formattedTime);
            int startTime = mStartTimeHour * 60 + mStartTimeMinute;
            if (time < startTime) {
                mNextDay.setVisibility(View.VISIBLE);
            } else {
                mNextDay.setVisibility(View.INVISIBLE);
            }
        }
    }
}
