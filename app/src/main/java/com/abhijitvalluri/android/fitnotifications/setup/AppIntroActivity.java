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

package com.abhijitvalluri.android.fitnotifications.setup;

import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.AppChoicesActivity;
import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.SettingsActivity;
import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.utils.Func;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Intro activity to display a helpful setup tutorial
 */
public class AppIntroActivity extends IntroActivity {

    private final Integer NOTIFICATION_ID = (int)((new Date().getTime() / 1000L) % Integer.MAX_VALUE);
    private static final int ENABLE_NOTIFICATION_ACCESS_INTENT = 1;
    private static final int INSTALL_FITBIT_INTENT = 2;
    private static final int LAUNCH_FITBIT_INTENT = 3;
    private static final int APP_CHOICES_INTENT = 4;

    private Bundle LAUNCH_ACTIVITY_ANIM_BUNDLE;

    private FragmentSlide mEnableNotificationSlide;
    private FragmentSlide mFitbitInstallSlide;
    private FragmentSlide mLaunchFitbitSlide;
    private FragmentSlide mAppChoicesSlide;
    private PackageManager mPackageManager;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, AppIntroActivity.class);
    }

    private void addIntroSlide() {
        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_welcome_title)
                .description(R.string.intro_welcome_desc)
                .image(R.drawable.intro_welcome)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .canGoBackward(true)
                .buttonCtaLabel(R.string.intro_get_started_button)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextSlide();
                    }
                })
                .build());
    }

    private void addFitbitInstallSlide() {
        if (!isFitbitAppInstalled()) {
            mFitbitInstallSlide = createFitbitInstallSlide();
            addSlide(mFitbitInstallSlide);
        }
    }

    private boolean isFitbitAppInstalled() {
        List<String> packageNames = Func.getInstalledPackageNames(mPackageManager, getApplicationContext());
        return packageNames.contains(Constants.FITBIT_PACKAGE_NAME);
    }

    private void addEnableNotificationAccessSlide() {
        Set<String> EnabledListenerPackagesSet = NotificationManagerCompat.
                getEnabledListenerPackages(this);
        if (EnabledListenerPackagesSet.contains(Constants.PACKAGE_NAME)
                && EnabledListenerPackagesSet.contains(Constants.FITBIT_PACKAGE_NAME)) {
            CustomSlideFragment fragment = new CustomSlideFragment();
            fragment.setCanGoForward(true)
                    .setCanGoBackward(true)
                    .setTitleText(R.string.intro_enable_access_success_title)
                    .setDescriptionText(R.string.intro_enable_access_success_desc)
                    .setImage(R.drawable.intro_enable_notifications);

            mEnableNotificationSlide = new FragmentSlide.Builder()
                    .fragment(fragment)
                    .background(R.color.purple_intro)
                    .backgroundDark(R.color.purpleDark_intro)
                    .build();
        } else {
            CustomSlideFragment fragment = new CustomSlideFragment();
            fragment.setCanGoForward(false)
                    .setCanGoBackward(true)
                    .setTitleText(R.string.intro_enable_access_title)
                    .setDescriptionText(R.string.intro_enable_access_desc)
                    .setImage(R.drawable.intro_enable_notifications);

            mEnableNotificationSlide = new FragmentSlide.Builder()
                    .fragment(fragment)
                    .background(R.color.purple_intro)
                    .backgroundDark(R.color.purpleDark_intro)
                    .buttonCtaLabel(R.string.enable_notification_access)
                    .buttonCtaClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Set<String> EnabledListenerPackagesSet = NotificationManagerCompat.
                                    getEnabledListenerPackages(AppIntroActivity.this);
                            if (!EnabledListenerPackagesSet.contains(Constants.PACKAGE_NAME)
                                    || !EnabledListenerPackagesSet.contains(Constants.FITBIT_PACKAGE_NAME)) {
                                startActivityForResult(
                                        new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
                                        ENABLE_NOTIFICATION_ACCESS_INTENT);
                            }
                        }
                    })
                    .build();
        }

        addSlide(mEnableNotificationSlide);
    }

    private void addDemoSlide() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        final boolean dismissPlaceholderNotif = preferences.getBoolean(
                getString(R.string.dismiss_placeholder_notif_key), false);
        final int placeholderNotifDismissDelayMillis = preferences.getInt(
                getString(R.string.placeholder_dismiss_delay_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;
        final Handler handler = new Handler();

        // Demo
        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_done_title)
                .description(R.string.intro_done_desc)
                .image(R.drawable.intro_done)
                .background(R.color.colorAccent)
                .backgroundDark(R.color.colorAccentDark)
                .buttonCtaLabel(R.string.test_notification)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle newExtra = new Bundle();

                        NotificationCompat.Builder builder = new NotificationCompat.Builder(AppIntroActivity.this);
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
                        builder.setSmallIcon(R.drawable.ic_sms_white_24dp)
                                .setContentText(sb.toString())
                                .setExtras(newExtra)
                                .setContentTitle("Sample Notification Title")
                                .setContent(contentView);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            builder.setChannelId(Constants.NOTIFICATION_CHANNEL_ID);
                        }

                        // Creates an explicit intent for the SettingsActivity in the app
                        Intent settingsIntent = new Intent(AppIntroActivity.this, SettingsActivity.class);

                        // The stack builder object will contain an artificial back stack for the
                        // started Activity.
                        // This ensures that navigating backward from the Activity leads out of
                        // the application to the Home screen.
                        TaskStackBuilder stackBuilder = TaskStackBuilder.create(AppIntroActivity.this);
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

                        Toast.makeText(AppIntroActivity.this, getString(R.string.test_notification_sent), Toast.LENGTH_LONG)
                                .show();

                        if (dismissPlaceholderNotif) {
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE))
                                            .cancel(NOTIFICATION_ID);
                                }
                            }, placeholderNotifDismissDelayMillis);
                        }
                    }
                })
                .build());
    }

    private void addStartServiceSlide() {
        SimpleSlide startServiceSlide = new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_start_service_title)
                .description(R.string.intro_start_service_desc)
                .image(R.drawable.intro_check)
                .background(R.color.colorAccent)
                .backgroundDark(R.color.colorAccentDark)
                .canGoForward(true)
                .buttonCtaLabel(R.string.start_service)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startService(new Intent(AppIntroActivity.this, NLService.class));
                        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                        preferences.edit().putBoolean(getString(R.string.notification_listener_service_state_key), true).apply();
                        NLService.setEnabled(true);
                        nextSlide();
                    }
                })
                .build();
        addSlide(startServiceSlide);
    }

    private void addDNDModeSlide() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            addSlide(new SimpleSlide.Builder()
                    .layout(R.layout.fragment_intro)
                    .title(R.string.intro_dnd_mode_title)
                    .description(R.string.intro_dnd_mode_desc)
                    .image(R.drawable.intro_dnd_mode_info)
                    .background(R.color.black)
                    .backgroundDark(R.color.grey)
                    .buttonCtaLabel(R.string.intro_dnd_mode_button)
                    .buttonCtaClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            AlertDialog dialog = new AlertDialog.Builder(AppIntroActivity.this)
                                    .setPositiveButton(R.string.intro_dnd_mode_button_configure, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", Constants.PACKAGE_NAME, null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton(android.R.string.cancel, null).create();
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                dialog.setTitle(getString(R.string.intro_dnd_mode_button_oreo));
                                dialog.setMessage(getString(R.string.intro_dnd_mode_oreo_message));
                            }
                            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                dialog.setTitle(getString(R.string.intro_dnd_mode_button_nougat));
                                dialog.setMessage(getString(R.string.intro_dnd_mode_nougat_message));
                            } else {
                                dialog.setTitle(getString(R.string.intro_dnd_mode_button_marshmallow));
                                dialog.setMessage(getString(R.string.intro_dnd_mode_marshmallow_message));
                            }
                            dialog.show();
                        }
                    }).build());
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        LAUNCH_ACTIVITY_ANIM_BUNDLE = ActivityOptions.
                makeCustomAnimation(AppIntroActivity.this,
                        R.transition.left_in,
                        R.transition.left_out).toBundle();
        mPackageManager = getPackageManager();

        boolean setupDone = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.done_first_launch_key), false);
        if (setupDone) { // Setup already finished once. So, this is repeat setup
            new AlertDialog.Builder(AppIntroActivity.this)
                    .setTitle(getString(R.string.intro_setup_issues_title))
                    .setMessage(getString(R.string.intro_setup_issues_message))
                    .setPositiveButton(R.string.intro_setup_issues_ok, null)
                    .create()
                    .show();
        }

        // Introduction slide
        addIntroSlide();

        // Fitbit install slide
        addFitbitInstallSlide();

        // Enable notifications slide
        addEnableNotificationAccessSlide();

        // Fitbit setup - 3 steps (3 slides)
        // Step 1
        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_setup_fitbit_title1)
                .description(R.string.intro_setup_fitbit_desc1)
                .image(R.drawable.step_one)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .build());

        // Step 2
        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_setup_fitbit_title2)
                .description(R.string.intro_setup_fitbit_desc2)
                .image(R.drawable.step_two)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .build());

        // Step 3
        mLaunchFitbitSlide = createLaunchFitbitSlide();
        addSlide(mLaunchFitbitSlide);

        // App Choices slide
        mAppChoicesSlide = createAppChoicesSlide();
        addSlide(mAppChoicesSlide);

        // Start service
        addStartServiceSlide();

        // Do not disturb mode announcement slide
        addDNDModeSlide();

        // Demo Slide
        addDemoSlide();

        setButtonBackVisible(true);
        setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);
        setButtonNextVisible(true);
        setButtonNextFunction(BUTTON_NEXT_FUNCTION_NEXT_FINISH);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.right_in, R.transition.right_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch(requestCode) {
            case ENABLE_NOTIFICATION_ACCESS_INTENT: {
                Set<String> EnabledListenerPackagesSet = NotificationManagerCompat.
                        getEnabledListenerPackages(this);
                if (EnabledListenerPackagesSet.contains(Constants.PACKAGE_NAME)
                        && EnabledListenerPackagesSet.contains(Constants.FITBIT_PACKAGE_NAME)) {
                    CustomSlideFragment fragment = (CustomSlideFragment) mEnableNotificationSlide.getFragment();
                    fragment.setCanGoForward(true)
                            .setTitleText(R.string.intro_enable_access_update_title)
                            .setDescriptionText(R.string.intro_enable_access_update_desc);
                }
                return;
            }
            case INSTALL_FITBIT_INTENT: {
                if (isFitbitAppInstalled()) {
                    CustomSlideFragment fragment = (CustomSlideFragment) mFitbitInstallSlide.getFragment();
                    fragment.setCanGoForward(true);
                    nextSlide();
                }
                return;
            }
            case LAUNCH_FITBIT_INTENT: {
                CustomSlideFragment fragment = (CustomSlideFragment) mLaunchFitbitSlide.getFragment();
                fragment.setCanGoForward(true);
                return;
            }
            case APP_CHOICES_INTENT: {
                CustomSlideFragment fragment = (CustomSlideFragment) mAppChoicesSlide.getFragment();
                fragment.setCanGoForward(true);
                return;
            }
            default:
        }
    }

    private FragmentSlide createAppChoicesSlide() {
        CustomSlideFragment fragment = new CustomSlideFragment();
        fragment.setCanGoForward(false)
                .setCanGoBackward(true)
                .setTitleText(R.string.app_choices)
                .setDescriptionText(R.string.intro_select_apps_desc)
                .setImage(R.drawable.view_list);

        return new FragmentSlide.Builder()
                .fragment(fragment)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .buttonCtaLabel(R.string.app_choices_activity_title)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivityForResult(AppChoicesActivity.newIntent(AppIntroActivity.this),
                                APP_CHOICES_INTENT, LAUNCH_ACTIVITY_ANIM_BUNDLE);
                    }
                })
                .build();
    }

    private FragmentSlide createLaunchFitbitSlide() {
        CustomSlideFragment fragment = new CustomSlideFragment();
        fragment.setCanGoForward(false)
                .setCanGoBackward(true)
                .setTitleText(R.string.intro_setup_fitbit_title3)
                .setDescriptionText(R.string.intro_setup_fitbit_desc3)
                .setImage(R.drawable.step_three);

        return new FragmentSlide.Builder()
                .fragment(fragment)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .buttonCtaLabel(R.string.intro_setup_fitbit_button3)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent i = mPackageManager.
                                    getLaunchIntentForPackage(Constants.FITBIT_PACKAGE_NAME);
                            startActivityForResult(i, LAUNCH_FITBIT_INTENT);

                        } catch (ActivityNotFoundException e) {
                            Toast.makeText(AppIntroActivity.this, getString(R.string.intro_get_fitbit_toast_text3), Toast.LENGTH_LONG).show();
                        }
                    }
                })
                .build();
    }

    private FragmentSlide createFitbitInstallSlide() {
        CustomSlideFragment fragment = new CustomSlideFragment();
        fragment.setCanGoForward(false)
                .setCanGoBackward(true)
                .setTitleText(R.string.intro_get_fitbit_title)
                .setDescriptionText(R.string.intro_get_fitbit_desc)
                .setImage(R.drawable.get_app);

        return new FragmentSlide.Builder()
                .fragment(fragment)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .buttonCtaLabel(R.string.intro_get_fitbit_button)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!isFitbitAppInstalled()) {
                            try {
                                startActivityForResult(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("market://details?id=" +
                                                  Constants.FITBIT_PACKAGE_NAME)),
                                        INSTALL_FITBIT_INTENT);
                            } catch (android.content.ActivityNotFoundException e) {
                                startActivityForResult(new Intent(Intent.ACTION_VIEW,
                                        Uri.parse("https://play.google.com/store/apps/details?id=" +
                                                  Constants.FITBIT_PACKAGE_NAME)),
                                        INSTALL_FITBIT_INTENT);
                            }
                        }
                    }
                })
                .build();
    }
}