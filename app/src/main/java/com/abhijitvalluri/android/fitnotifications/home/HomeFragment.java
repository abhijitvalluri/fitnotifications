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

package com.abhijitvalluri.android.fitnotifications.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.appchoices.AppChoicesActivity;
import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.settings.SettingsActivity;
import com.abhijitvalluri.android.fitnotifications.setup.AppIntroActivity;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.utils.DebugLog;
import com.abhijitvalluri.android.fitnotifications.widget.ServiceToggle;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

/**
 * Contains the main home fragment
 */
public class HomeFragment extends Fragment {

    private final Handler mHandler = new Handler(Looper.getMainLooper());

    public static final String STATE_IS_DONATE_BANNER = "donateBanner";

    private static boolean mDismissPlaceholderNotif;
    private static int mPlaceholderNotifDismissDelayMillis;

    private TextView mInstructionTV;
    private TextView mAppSelectionTV;
    private Button mServiceButton;
    private TextView mDemoTV;
    private TextView mNotificationAccessTV;
    private TextView mServiceStateTV;
    private TextView mBannerTV;
    private SwitchCompat mEnableLogs;
    private Button mSendLogs;
    private TextView mLogStatus;

    private SharedPreferences mPreferences;
    private Boolean mIsDonateBanner = false;

    private Bundle LAUNCH_ACTIVITY_ANIM_BUNDLE;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        if (savedInstanceState != null) {
            mIsDonateBanner = savedInstanceState.getBoolean(STATE_IS_DONATE_BANNER);
        }

        mInstructionTV = (TextView) v.findViewById(R.id.instructionsTV);
        mAppSelectionTV = (TextView) v.findViewById(R.id.appSelectionTV);
        mServiceButton = (Button) v.findViewById(R.id.serviceButton);
        mDemoTV = (TextView) v.findViewById(R.id.demoNotifTV);
        mNotificationAccessTV = (TextView) v.findViewById(R.id.notificationAccessTV);
        mServiceStateTV = (TextView) v.findViewById(R.id.serviceStateText);
        mBannerTV = (TextView) v.findViewById(R.id.rate_app);
        mEnableLogs = (SwitchCompat) v.findViewById(R.id.enableLogSwitch);
        mSendLogs = (Button) v.findViewById(R.id.sendLogsButton);
        mLogStatus = (TextView) v.findViewById(R.id.logStatus);

        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        if (!mIsDonateBanner && Math.random() < 0.5) {
            mIsDonateBanner = true;
            if (mPreferences.getBoolean(getString(R.string.probably_donated), false)) {
                mBannerTV.setVisibility(View.GONE);
            } else {
                mBannerTV.setText(R.string.support_dev);
            }
        } else {
            mIsDonateBanner = false;
            if (mPreferences.getBoolean(getString(R.string.probably_rated_app), false)) {
                mBannerTV.setVisibility(View.GONE);
            } else {
                mBannerTV.setText(R.string.rate_app);
            }
        }

        boolean enableLogs = mPreferences.getBoolean(getString(R.string.enable_debug_logs), false);
        mEnableLogs.setChecked(enableLogs);
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mSendLogs.setVisibility((enableLogs ? View.VISIBLE : View.GONE));
            mLogStatus.setVisibility((enableLogs ? View.VISIBLE : View.GONE));
        } else {
            mSendLogs.setVisibility(View.GONE);
            mLogStatus.setVisibility(View.GONE);
        }

        mEnableLogs.setOnCheckedChangeListener((buttonView, isChecked) -> {
            mPreferences.edit().putBoolean(getString(R.string.enable_debug_logs), isChecked).apply();
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mSendLogs.setVisibility((isChecked ? View.VISIBLE : View.GONE));
                mLogStatus.setVisibility((isChecked ? View.VISIBLE : View.GONE));
            } else {
                mSendLogs.setVisibility(View.GONE);
                mLogStatus.setVisibility(View.GONE);
            }

            DebugLog log = DebugLog.get(getActivity());
            int status = isChecked ? log.enable() : log.disable();
            updateLogStatus(status);
            if (isChecked) {
                new AlertDialog.Builder(mContext)
                        .setTitle("Warning: Storage Usage!")
                        .setMessage("Please note that enabling logging will use storage space on your phone. We will limit log file size to 10 MB.\n\n" +
                                "If you enable logging for too long, then old log contents will be over-written to stay within the 10 MB file size limit. " +
                                "To avoid this, and preserve debugging information, please enable logs for only a brief period during troubleshooting.")
                        .setPositiveButton(android.R.string.ok, null)
                        .create().show();
            }
        });

        mSendLogs.setOnClickListener(v1 -> new AlertDialog.Builder(mContext)
                .setTitle("Send logs to developer?")
                .setMessage("If you send logs to the developer now, the app will stop collecting logs immediately and send whatever logs are present. It will then delete the logs from your phone. Do you want to proceed?")
                .setPositiveButton("SEND", (dialog, which) -> {
                    StringBuilder logcat=new StringBuilder();
                    try {
                        Process process = Runtime.getRuntime().exec("logcat -d");
                        BufferedReader bufferedReader = new BufferedReader(
                                new InputStreamReader(process.getInputStream()));

                        String line;
                        while ((line = bufferedReader.readLine()) != null) {
                            logcat.append(line).append('\n');
                        }
                    } catch (Exception e) {
                        logcat.append("Exception when accessing logcat. Exception: ")
                              .append(e.getMessage());
                    }

                    sendDebugLogEmail(logcat);
                })
                .setNegativeButton("CANCEL", null)
                .create().show());

        DebugLog log = DebugLog.get(getActivity());

        if (log.getFileStatus() == DebugLog.STATUS_LOG_OPENED) {
            updateLogStatus(log.getWriteStatus());
        } else {
            updateLogStatus(log.getFileStatus());
        }

        initializeSettings();
        initializeButtons();

        LAUNCH_ACTIVITY_ANIM_BUNDLE = ActivityOptions.
                makeCustomAnimation(mContext,
                        R.transition.left_in,
                        R.transition.left_out).toBundle();
        activateTextViewLinks();

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        boolean isInitialSetupDone = mPreferences.getBoolean(getString(R.string.done_first_launch_key), false);
        if (isInitialSetupDone
                && ContextCompat.checkSelfPermission(
                        mContext,
                        Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(mContext, getString(R.string.notification_permission_toast), Toast.LENGTH_LONG).show();
            startActivity(AppIntroActivity.newIntentForRequestingNotificationPermission(mContext), LAUNCH_ACTIVITY_ANIM_BUNDLE);
        }
    }

    @SuppressLint("SetTextI18n")
    private void sendDebugLogEmail(final StringBuilder body) {
        final LinearLayout layout = new LinearLayout(mContext);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32,16,32,16);


        final TextView title = new TextView(mContext);
        title.setText("Explain the problem below:");
        title.setTextSize(18);
        final EditText input = new EditText(mContext);
        layout.addView(title);
        layout.addView(input);

        final AlertDialog dialog = new AlertDialog.Builder(mContext)
                .setTitle("Send Logs: Step 1")
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();

        dialog.setOnShowListener((DialogInterface.OnShowListener) dialogInterface -> {

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String issue = input.getText().toString();
                issue = issue.trim();
                if (issue.isEmpty()) {
                    Toast.makeText(mContext, "You must describe the problem you are facing to proceed!", Toast.LENGTH_SHORT).show();
                } else {
                    body.insert(0, "\n\n------\n\n");
                    body.insert(0, issue);

                    DebugLog log = DebugLog.get(getActivity());
                    startActivity(log.emailLogIntent(mContext, body.toString()));

                    mEnableLogs.setChecked(false);
                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void updateLogStatus(int status) {
        switch (status) {
            case DebugLog.STATUS_LOG_OPENED:
                mLogStatus.setText("STATUS: Log opened successfully");
                break;
            case DebugLog.STATUS_IO_EXCEPTION:
                mLogStatus.setText("STATUS: Error opening log");
                break;
            case DebugLog.STATUS_WRITE_OK:
                mLogStatus.setText("STATUS: Writing logs...");
                break;
            case DebugLog.STATUS_UNINITIALIZED:
                mLogStatus.setText("STATUS: Logs cleared and uninitialized");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_IS_DONATE_BANNER, mIsDonateBanner);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeServiceButtons();
        updateNotificationAccessText();
    }

    public static void onPlaceholderNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissPlaceholderNotif = dismissNotif;
        mPlaceholderNotifDismissDelayMillis = delaySeconds*1000;
    }

    private void activateTextViewLinks() {
        mInstructionTV.setText(R.string.instructions);
        mInstructionTV.setOnClickListener(v -> startActivity(AppIntroActivity.newIntent(mContext), LAUNCH_ACTIVITY_ANIM_BUNDLE));

        mAppSelectionTV.setOnClickListener(v -> startActivity(AppChoicesActivity.newIntent(mContext), LAUNCH_ACTIVITY_ANIM_BUNDLE));
    }

    private void initializeSettings() {
        PreferenceManager.setDefaultValues(mContext, R.xml.main_settings, false);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mDismissPlaceholderNotif = preferences.getBoolean(
                getString(R.string.dismiss_placeholder_notif_key), false);
        mPlaceholderNotifDismissDelayMillis = preferences.getInt(
                getString(R.string.placeholder_dismiss_delay_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;
    }

    private void initializeButtons() {
        initializeServiceButtons();
        initializeDemoButton();
        initializeEnableNotificationButton();

        mBannerTV.setOnClickListener(v -> {
            if (mIsDonateBanner) {
                mPreferences.edit().putBoolean(getString(R.string.probably_donated), true).apply();
                startActivity(HomeActivity.userDonationIntent());
            } else {
                Uri uri = Uri.parse("market://details?id=" + Constants.PACKAGE_NAME);
                Intent gotoPlayStore = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                gotoPlayStore.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    mPreferences.edit().putBoolean(getString(R.string.probably_rated_app), true).apply();
                    startActivity(gotoPlayStore);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + Constants.PACKAGE_NAME)));
                }
            }
        });
    }

    private void initializeEnableNotificationButton() {
        updateNotificationAccessText();
        mNotificationAccessTV.setOnClickListener(v -> startActivity(new
                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS")));
    }

    private void initializeDemoButton() {
        mDemoTV.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.POST_NOTIFICATIONS) !=
                    PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(mContext, getString(R.string.notification_permission_toast), Toast.LENGTH_LONG).show();
                startActivity(AppIntroActivity.newIntentForRequestingNotificationPermission(mContext), LAUNCH_ACTIVITY_ANIM_BUNDLE);
                return;
            }

            Bundle newExtra = new Bundle();

            NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, Constants.NOTIFICATION_CHANNEL_ID_CURRENT);
            String notificationText = "Sample notification subject";
            String notificationBigText = "Sample notification body. This is where the details of the notification will be shown.";

            RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification);
            contentView.setTextViewText(R.id.customNotificationText, getString(R.string.notification_text));
            String content = "[" + "example" + "] " + notificationText + " -- " + notificationBigText;
            builder.setSmallIcon(R.drawable.ic_sms_white_24dp)
                    .setContentText(content)
                    .setExtras(newExtra)
                    .setContentTitle("Sample Notification Title")
                    .setContent(contentView);

            // Creates an explicit intent for the SettingsActivity in the app
            Intent settingsIntent = new Intent(mContext, SettingsActivity.class);

            // The stack builder object will contain an artificial back stack for the
            // started Activity.
            // This ensures that navigating backward from the Activity leads out of
            // the application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(mContext);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(SettingsActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(settingsIntent);
            PendingIntent settingsPendingIntent =
                    stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
                    );
            builder.setContentIntent(settingsPendingIntent).setAutoCancel(true);

            ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(Constants.NOTIFICATION_ID, builder.build());

            Toast.makeText(mContext, getString(R.string.test_notification_sent), Toast.LENGTH_LONG)
                    .show();

            if (mDismissPlaceholderNotif) {
                mHandler.postDelayed(() -> ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                        .cancel(Constants.NOTIFICATION_ID), mPlaceholderNotifDismissDelayMillis);
            }
        });
    }

    private void updateNotificationAccessText() {
        Set<String> EnabledListenerPackagesSet = NotificationManagerCompat.
                getEnabledListenerPackages(mContext);
        if (EnabledListenerPackagesSet.contains(Constants.PACKAGE_NAME)
                && EnabledListenerPackagesSet.contains(Constants.FITBIT_PACKAGE_NAME)) {
            mNotificationAccessTV.setText(getString(R.string.disable_notification_access));
        } else {
            mNotificationAccessTV.setText(getString(R.string.enable_notification_access));
        }
    }

    private void updateWidget() {
        Intent i = new Intent(mContext, ServiceToggle.class);
        i.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int[] ids = AppWidgetManager.getInstance(mContext).getAppWidgetIds(new ComponentName(mContext, ServiceToggle.class));
        i.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids);
        mContext.sendBroadcast(i);
    }

    private void initializeServiceButtons() {
        boolean serviceEnabled = NLService.isEnabled();

        if(serviceEnabled) {
            mServiceButton.setText(R.string.turn_off_service);
            mServiceStateTV.setText(R.string.service_on);
            mServiceStateTV.setTextColor(ContextCompat.getColor(mContext, R.color.brightGreen));
            mServiceButton.setOnClickListener(v -> {
                mContext.stopService(new Intent(mContext, NLService.class));
                mPreferences.edit().putBoolean(getString(R.string.notification_listener_service_state_key), false).apply();
                NLService.setEnabled(false);
                updateWidget();
                initializeServiceButtons();
            });
        } else {
            mServiceButton.setText(R.string.turn_on_service);
            mServiceStateTV.setText(R.string.service_off);
            mServiceStateTV.setTextColor(ContextCompat.getColor(mContext, R.color.red));
            mServiceButton.setOnClickListener(v -> {
                mContext.startService(new Intent(mContext, NLService.class));
                mPreferences.edit().putBoolean(getString(R.string.notification_listener_service_state_key), true).apply();
                NLService.setEnabled(true);
                updateWidget();
                initializeServiceButtons();
            });
        }
    }
}
