package com.abhijitvalluri.android.fitnotifications;

import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Date;

public class FitNotifications extends AppCompatActivity {

    private static boolean mDismissPlaceholderNotif;
    private static int mPlaceholderNotifDismissDelayMillis;

    private final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    private final Handler mHandler = new Handler();

    private TextView mInstructionTB;
    private TextView mAppSelectionTB;
    private TextView mAboutAppTB;
    private TextView mFAQsTB;
    private TextView mChangelogTB;
    private TextView mDisclaimerTB;
    private TextView mCreditsTB;

    private Button mStartServiceButton;
    private Button mStopServiceButton;
    private Button mDemoButton;
    private Button mEnableNotificationsButton;

    private Bundle mBundleAnim;
    private SharedPreferences mPreferences;

    public static void onPlaceholderNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissPlaceholderNotif = dismissNotif;
        mPlaceholderNotifDismissDelayMillis = delaySeconds*1000;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fit_notifications);

        // Initialize settings to defaults
        PreferenceManager.setDefaultValues(this, R.xml.main_settings, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDismissPlaceholderNotif = mPreferences.getBoolean(
                getString(R.string.dismiss_placeholder_notif_key), false);
        mPlaceholderNotifDismissDelayMillis = mPreferences.getInt(
                getString(R.string.placeholder_dismiss_delay_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;

        mInstructionTB = (TextView) findViewById(R.id.instructionsTB);
        mAppSelectionTB = (TextView) findViewById(R.id.appSelectionTB);
        mAboutAppTB = (TextView) findViewById(R.id.aboutAppTB);
        mFAQsTB = (TextView) findViewById(R.id.faqsTB);
        mChangelogTB = (TextView) findViewById(R.id.changelogTB);
        mDisclaimerTB = (TextView) findViewById(R.id.disclaimerTB);
        mCreditsTB = (TextView) findViewById(R.id.creditsTB);

        mStartServiceButton = (Button) findViewById(R.id.leftButton);
        mStopServiceButton = (Button) findViewById(R.id.rightButton);
        mDemoButton = (Button) findViewById(R.id.demoNotificationButton);
        mEnableNotificationsButton = (Button) findViewById(R.id.enableNotificationAccessButton);
        mBundleAnim = ActivityOptions.
                makeCustomAnimation(FitNotifications.this,
                                    R.transition.left_in,
                                    R.transition.left_out).toBundle();

        activateTextViewLinks();
        initializeButtons();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_main_settings:
                startActivity(SettingsActivity.newIntent(this), mBundleAnim);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void activateTextViewLinks() {
        mInstructionTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(FitNotifications.this, getString(R.string.instructions), getString(R.string.instructions_text));
                startActivity(intent, mBundleAnim);
            }
        });

        mAppSelectionTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AppChoicesActivity.newIntent(FitNotifications.this), mBundleAnim);
            }
        });

        mAboutAppTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(FitNotifications.this, getString(R.string.about_app), getString(R.string.about_app_text));
                startActivity(intent, mBundleAnim);
            }
        });

        mFAQsTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(FitNotifications.this, getString(R.string.faqs), getString(R.string.faqs_text));
                startActivity(intent, mBundleAnim);
            }
        });

        mChangelogTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(FitNotifications.this, getString(R.string.changelog), getString(R.string.changelog_text));
                startActivity(intent, mBundleAnim);
            }
        });

        mDisclaimerTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(FitNotifications.this, getString(R.string.disclaimer), getString(R.string.disclaimer_text));
                startActivity(intent, mBundleAnim);
            }
        });

        mCreditsTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(FitNotifications.this, getString(R.string.credits), getString(R.string.credits_text));
                startActivity(intent, mBundleAnim);
            }
        });
    }

    private void initializeButtons() {
        initializeServiceButtons();
        initializeDemoButton();
        initializeEnableNotificationButton();
    }

    private void initializeEnableNotificationButton() {
        mEnableNotificationsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                enableNotificationAccess();
            }
        });
    }
    private void initializeDemoButton() {
        mDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle newExtra = new Bundle();
                newExtra.putChar(Constants.DEMO_NOTIFICATION, '1');

                NotificationCompat.Builder builder = new NotificationCompat.Builder(FitNotifications.this);
                String notificationText = "Sample notification subject";
                String notificationBigText = "Sample notification body. This is where the details of the notification will be shown.";


                StringBuilder sb = new StringBuilder();
                sb.append("[").append("example").append("] ");
                sb.append(notificationText);
                if (notificationBigText.length() > 0) {
                    sb.append(" -- ").append(notificationBigText);
                }

                RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
                contentView.setTextViewText(R.id.customNotificationText, getString(R.string.demo_notification_text));
                builder.setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(sb.toString())
                        .setExtras(newExtra)
                        .setContentTitle("Sample Notification Title")
                        .setContent(contentView);

                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .notify(NOTIFICATION_ID, builder.build());

                Toast.makeText(FitNotifications.this, "Demo notification sent", Toast.LENGTH_LONG)
                        .show();

                if (mDismissPlaceholderNotif) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                                    .cancel(NOTIFICATION_ID);
                        }
                    }, mPlaceholderNotifDismissDelayMillis);
                }
            }
        });
    }

    private void enableNotificationAccess() {
        String message = "You must enable notification access in order for this app to work.\n\n" +
                "To enable notification access, allow access for " + getString(R.string.app_name) +
                " on the next screen.";

        new AlertDialog.Builder(FitNotifications.this)
                .setMessage(message)
                .setTitle(getString(R.string.enable_notification_access))
                .setPositiveButton("ENABLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new
                                Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
                    }
                })
                .setNegativeButton("CANCEL", null)
                .create()
                .show();
    }

    private void initializeServiceButtons() {
        mStartServiceButton.setText(R.string.start_service);
        mStopServiceButton.setText(R.string.stop_service);
        mStartServiceButton.setEnabled(!isNLSEnabled());
        mStopServiceButton.setEnabled(isNLSEnabled());
        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(FitNotifications.this, NotificationListener.class));
                setNLSEnabled(true);
                mStartServiceButton.setEnabled(false);
                mStopServiceButton.setEnabled(true);
            }
        });
        mStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(FitNotifications.this, NotificationListener.class));
                setNLSEnabled(false);
                mStartServiceButton.setEnabled(true);
                mStopServiceButton.setEnabled(false);
            }
        });
    }

    private void setNLSEnabled(boolean enabled) {
        mPreferences.edit().putBoolean(Constants.SERVICE_STATE, enabled).apply();
        NotificationListener.setEnabled(enabled);
    }

    private boolean isNLSEnabled() {
        return mPreferences.getBoolean(Constants.SERVICE_STATE, false);
    }
}

