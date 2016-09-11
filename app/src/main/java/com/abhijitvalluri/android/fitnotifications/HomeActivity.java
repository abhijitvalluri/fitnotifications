package com.abhijitvalluri.android.fitnotifications;

import android.app.Activity;
import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;

import java.util.Date;

public class HomeActivity extends AppCompatActivity
        implements SettingsActivity.SettingsFragment.SetupCallback {

    private static final int APP_INTRO_FIRST_LAUNCH_INTENT = 1;

    private static boolean mDismissPlaceholderNotif;
    private static int mPlaceholderNotifDismissDelayMillis;
    private static boolean mInteractiveSetupEnabled;

    private final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    private final Handler mHandler = new Handler();


    private Bundle LAUNCH_ACTIVITY_ANIM_BUNDLE;

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

    private SharedPreferences mPreferences;

    // TODO: implement proper callbacks where possible
    //TODO: use Google Analytics! HIGH PRIORITY!
    public static void onPlaceholderNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissPlaceholderNotif = dismissNotif;
        mPlaceholderNotifDismissDelayMillis = delaySeconds*1000;
    }

    public void onOverrideInteractiveSetup(boolean enabled) {
        mInteractiveSetupEnabled = enabled;
        updateSetup();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        LAUNCH_ACTIVITY_ANIM_BUNDLE = ActivityOptions.
                makeCustomAnimation(HomeActivity.this,
                        R.transition.left_in,
                        R.transition.left_out).toBundle();

        // Initialize settings to defaults
        initializeSettings();

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

        activateTextViewLinks();
        initializeButtons();

        if (mPreferences.getInt(getString(R.string.version_key), 0) < Constants.VERSION_CODE
            && mPreferences.getInt(getString(R.string.version_key), 0) > 0) {
            // Updated from old version
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle(R.string.whats_new)
                    .setMessage(Html.fromHtml(getString(R.string.whats_new_text)))
                    .setPositiveButton(android.R.string.ok, null)
                    .create()
                    .show();

            mPreferences.edit().putInt(getString(R.string.version_key), Constants.VERSION_CODE).apply();
        }

        if (!mPreferences.getBoolean(getString(R.string.done_first_launch_key), false)) { // This is the first launch
            startActivityForResult(new Intent(HomeActivity.this, AppIntroActivity.class),
                    APP_INTRO_FIRST_LAUNCH_INTENT,
                    LAUNCH_ACTIVITY_ANIM_BUNDLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeServiceButtons();
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
                startActivity(SettingsActivity.newIntent(this), LAUNCH_ACTIVITY_ANIM_BUNDLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    private void initializeSettings() {
        PreferenceManager.setDefaultValues(this, R.xml.main_settings, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mDismissPlaceholderNotif = mPreferences.getBoolean(
                getString(R.string.dismiss_placeholder_notif_key), false);
        mPlaceholderNotifDismissDelayMillis = mPreferences.getInt(
                getString(R.string.placeholder_dismiss_delay_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;
        mInteractiveSetupEnabled = !mPreferences.getBoolean(getString(R.string.override_interactive_setup_key), false);

    }

    private void updateSetup() {
        if (mInteractiveSetupEnabled) {
            mInstructionTB.setText(R.string.instructions);
            mInstructionTB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(AppIntroActivity.newIntent(HomeActivity.this), LAUNCH_ACTIVITY_ANIM_BUNDLE);
                }
            });
        } else {
            mInstructionTB.setText(R.string.instructions_manual);
            mInstructionTB.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = InfoActivity.newIntent(HomeActivity.this, getString(R.string.instructions_manual_heading), getString(R.string.instructions_text));
                    startActivity(intent, LAUNCH_ACTIVITY_ANIM_BUNDLE);
                }
            });
        }
    }

    private void activateTextViewLinks() {
        updateSetup();

        mAppSelectionTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AppChoicesActivity.newIntent(HomeActivity.this), LAUNCH_ACTIVITY_ANIM_BUNDLE);
            }
        });

        mAboutAppTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(HomeActivity.this, getString(R.string.about_app), getString(R.string.about_app_text));
                startActivity(intent, LAUNCH_ACTIVITY_ANIM_BUNDLE);
            }
        });

        mFAQsTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(HomeActivity.this, getString(R.string.faqs), getString(R.string.faqs_text));
                startActivity(intent, LAUNCH_ACTIVITY_ANIM_BUNDLE);
            }
        });

        mChangelogTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(HomeActivity.this, getString(R.string.whats_new), getString(R.string.whats_new_text));
                startActivity(intent, LAUNCH_ACTIVITY_ANIM_BUNDLE);
            }
        });

        mDisclaimerTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(HomeActivity.this, getString(R.string.disclaimer), getString(R.string.disclaimer_text));
                startActivity(intent, LAUNCH_ACTIVITY_ANIM_BUNDLE);
            }
        });

        mCreditsTB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = InfoActivity.newIntent(HomeActivity.this, getString(R.string.credits), getString(R.string.credits_text));
                startActivity(intent, LAUNCH_ACTIVITY_ANIM_BUNDLE);
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

                NotificationCompat.Builder builder = new NotificationCompat.Builder(HomeActivity.this);
                String notificationText = "Sample notification subject";
                String notificationBigText = "Sample notification body. This is where the details of the notification will be shown.";


                StringBuilder sb = new StringBuilder();
                sb.append("[").append("example").append("] ");
                sb.append(notificationText);
                if (notificationBigText.length() > 0) {
                    sb.append(" -- ").append(notificationBigText);
                }

                RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.custom_notification);
                contentView.setTextViewText(R.id.customNotificationText, getString(R.string.placeholder_notification_text));
                builder.setSmallIcon(R.mipmap.ic_launcher)
                        .setContentText(sb.toString())
                        .setExtras(newExtra)
                        .setContentTitle("Sample Notification Title")
                        .setContent(contentView);

                // Creates an explicit intent for the SettingsActivity in the app
                Intent settingsIntent = new Intent(HomeActivity.this, SettingsActivity.class);

                // The stack builder object will contain an artificial back stack for the
                // started Activity.
                // This ensures that navigating backward from the Activity leads out of
                // the application to the Home screen.
                TaskStackBuilder stackBuilder = TaskStackBuilder.create(HomeActivity.this);
                // Adds the back stack for the Intent (but not the Intent itself)
                stackBuilder.addParentStack(SettingsActivity.class);
                // Adds the Intent that starts the Activity to the top of the stack
                stackBuilder.addNextIntent(settingsIntent);
                PendingIntent settingsPendingIntent =
                        stackBuilder.getPendingIntent(
                                0,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                builder.setContentIntent(settingsPendingIntent).setAutoCancel(true);

                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                        .notify(NOTIFICATION_ID, builder.build());

                Toast.makeText(HomeActivity.this, getString(R.string.test_notification_sent), Toast.LENGTH_LONG)
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

        new AlertDialog.Builder(HomeActivity.this)
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
        mStartServiceButton.setEnabled(!NLService.isEnabled());
        mStopServiceButton.setEnabled(NLService.isEnabled());
        mStartServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent(HomeActivity.this, NLService.class));
                NLService.setEnabled(true);
                mStartServiceButton.setEnabled(false);
                mStopServiceButton.setEnabled(true);
            }
        });
        mStopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent(HomeActivity.this, NLService.class));
                NLService.setEnabled(false);
                mStartServiceButton.setEnabled(true);
                mStopServiceButton.setEnabled(false);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case APP_INTRO_FIRST_LAUNCH_INTENT:
                if (resultCode == Activity.RESULT_OK) {
                    mPreferences.edit().putBoolean(getString(R.string.done_first_launch_key), true).apply();
                } else {
                    new AlertDialog.Builder(HomeActivity.this)
                            .setMessage(getString(R.string.setup_incomplete))
                            .setTitle(getString(R.string.setup_incomplete_title))
                            .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    startActivityForResult(new Intent(HomeActivity.this, AppIntroActivity.class), APP_INTRO_FIRST_LAUNCH_INTENT, LAUNCH_ACTIVITY_ANIM_BUNDLE);
                                }
                            })
                            .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    mPreferences.edit().putBoolean(getString(R.string.done_first_launch_key), true).apply();
                                    Toast.makeText(HomeActivity.this, "Okay, you can check out the new setup process at any time!", Toast.LENGTH_LONG).show();
                                }
                            })
                            .create()
                            .show();
                }
                return;
            default:
        }
    }
}

