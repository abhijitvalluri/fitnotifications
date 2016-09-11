package com.abhijitvalluri.android.fitnotifications;

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
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.utils.Func;
import com.heinrichreimersoftware.materialintro.app.IntroActivity;
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

    private SimpleSlide mEnableNotificationSlide;
    private SimpleSlide mFitbitInstallSlide;
    private SimpleSlide mLaunchFitbitSlide;
    private SimpleSlide mAppChoicesSlide;
    private PackageManager mPackageManager;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, AppIntroActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        LAUNCH_ACTIVITY_ANIM_BUNDLE = ActivityOptions.
                makeCustomAnimation(AppIntroActivity.this,
                        R.transition.left_in,
                        R.transition.left_out).toBundle();
        mPackageManager = getPackageManager();

        // Introduction slide
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

        // Fitbit install slide
        List<String> packageNames = Func.getInstalledPackageNames(mPackageManager);
        if (!packageNames.contains(Constants.FITBIT_PACKAGE_NAME)) { // Fitbit app is not installed
            mFitbitInstallSlide = createFitbitSlide(false);
            addSlide(mFitbitInstallSlide);
        }

        // Enable notifications slide
        Set<String> EnabledListenerPackagesSet = NotificationManagerCompat.
                getEnabledListenerPackages(this);
        if (EnabledListenerPackagesSet.contains(Constants.PACKAGE_NAME)
                && EnabledListenerPackagesSet.contains(Constants.FITBIT_PACKAGE_NAME)) {
            mEnableNotificationSlide = new SimpleSlide.Builder()
                    .layout(R.layout.fragment_intro)
                    .title(R.string.intro_enable_access_success_title)
                    .description(R.string.intro_enable_access_success_desc)
                    .image(R.drawable.intro_enable_notifications)
                    .background(R.color.purple_intro)
                    .backgroundDark(R.color.purpleDark_intro)
                    .canGoForward(true)
                    .build();
        } else {
            mEnableNotificationSlide = new SimpleSlide.Builder()
                    .layout(R.layout.fragment_intro)
                    .title(R.string.intro_enable_access_title)
                    .description(R.string.intro_enable_access_desc)
                    .image(R.drawable.intro_enable_notifications)
                    .background(R.color.purple_intro)
                    .backgroundDark(R.color.purpleDark_intro)
                    .canGoForward(false)
                    .buttonCtaLabel(R.string.enable_notification_access)
                    .buttonCtaClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            startActivityForResult(
                               new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"),
                               ENABLE_NOTIFICATION_ACCESS_INTENT);
                        }
                    })
                    .build();
        }

        addSlide(mEnableNotificationSlide);

        // Fitbit setup - 3 steps (3 slides)
        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_setup_fitbit_title1)
                .description(R.string.intro_setup_fitbit_desc1)
                .image(R.drawable.step_one)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .buttonCtaLabel(R.string.intro_setup_fitbit_toast_button)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(
                              AppIntroActivity.this,
                              getString(R.string.intro_setup_fitbit_toast_text1), Toast.LENGTH_LONG)
                             .show();
                    }
                })
                .build());

        addSlide(new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_setup_fitbit_title2)
                .description(R.string.intro_setup_fitbit_desc2)
                .image(R.drawable.step_two)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .build());

        // Launch Fitbit slide
        mLaunchFitbitSlide = createLaunchFitbitSlide(false);
        addSlide(mLaunchFitbitSlide);

        // App Choices slide
        mAppChoicesSlide = createAppChoicesSlide(false);
        addSlide(mAppChoicesSlide);

        // Start service
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
                        NLService.setEnabled(true);
                        nextSlide();
                    }
                })
                .build();
        addSlide(startServiceSlide);

        // Do not disturb mode announcement slide
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
                            new AlertDialog.Builder(AppIntroActivity.this)
                                    .setMessage(getString(R.string.intro_dnd_mode_message))
                                    .setTitle(getString(R.string.intro_dnd_mode_button))
                                    .setPositiveButton("Configure", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent();
                                            intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            Uri uri = Uri.fromParts("package", Constants.PACKAGE_NAME, null);
                                            intent.setData(uri);
                                            startActivity(intent);
                                        }
                                    })
                                    .setNegativeButton("CANCEL", null)
                                    .create()
                                    .show();
                        }
                    })
                    .build());
        }

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
                        builder.setSmallIcon(R.mipmap.ic_launcher)
                                .setContentText(sb.toString())
                                .setExtras(newExtra)
                                .setContentTitle("Sample Notification Title")
                                .setContent(contentView);

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

        setButtonBackVisible(true);
        setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);
        setButtonNextVisible(true);
        setButtonNextFunction(BUTTON_NEXT_FUNCTION_NEXT_FINISH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        int slideIndex;
        switch(requestCode) {
            case ENABLE_NOTIFICATION_ACCESS_INTENT:
                Set<String> EnabledListenerPackagesSet = NotificationManagerCompat.
                        getEnabledListenerPackages(this);
                if (EnabledListenerPackagesSet.contains(Constants.PACKAGE_NAME)
                        && EnabledListenerPackagesSet.contains(Constants.FITBIT_PACKAGE_NAME)) {
                    slideIndex = indexOfSlide(mEnableNotificationSlide);
                    mEnableNotificationSlide = new SimpleSlide.Builder()
                            .layout(R.layout.fragment_intro)
                            .title(R.string.intro_enable_access_update_title)
                            .description(R.string.intro_enable_access_update_desc)
                            .image(R.drawable.intro_enable_notifications)
                            .background(R.color.purple_intro)
                            .backgroundDark(R.color.purpleDark_intro)
                            .canGoForward(true)
                            .canGoBackward(false)
                            .build();
                    setSlide(slideIndex, mEnableNotificationSlide);
                    nextSlide();
                }
                return;
            case INSTALL_FITBIT_INTENT:
                slideIndex = indexOfSlide(mFitbitInstallSlide);
                mFitbitInstallSlide = createFitbitSlide(true);
                setSlide(slideIndex, mFitbitInstallSlide);
                return;
            case LAUNCH_FITBIT_INTENT:
                slideIndex = indexOfSlide(mLaunchFitbitSlide);
                mLaunchFitbitSlide = createLaunchFitbitSlide(true);
                setSlide(slideIndex, mLaunchFitbitSlide);
                return;
            case APP_CHOICES_INTENT:
                slideIndex = indexOfSlide(mAppChoicesSlide);
                mAppChoicesSlide = createAppChoicesSlide(true);
                setSlide(slideIndex, mAppChoicesSlide);
                return;
            default:
        }
    }

    private SimpleSlide createAppChoicesSlide(boolean canGoFwd) {
        return new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.app_choices)
                .description(R.string.intro_select_apps_desc)
                .image(R.drawable.view_list)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .canGoForward(canGoFwd)
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

    private SimpleSlide createLaunchFitbitSlide(boolean canGoFwd) {
        return new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_setup_fitbit_title3)
                .description(R.string.intro_setup_fitbit_desc3)
                .image(R.drawable.step_three)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .canGoForward(canGoFwd)
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

    private SimpleSlide createFitbitSlide(boolean canGoFwd) {
        return new SimpleSlide.Builder()
                .layout(R.layout.fragment_intro)
                .title(R.string.intro_get_fitbit_title)
                .description(R.string.intro_get_fitbit_desc)
                .image(R.drawable.get_app)
                .background(R.color.fitbitColor_intro)
                .backgroundDark(R.color.fitbitColorDark_intro)
                .canGoForward(canGoFwd)
                .buttonCtaLabel(R.string.intro_get_fitbit_button)
                .buttonCtaClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + Constants.FITBIT_PACKAGE_NAME)), INSTALL_FITBIT_INTENT);
                        } catch (android.content.ActivityNotFoundException e) {
                            startActivityForResult(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + Constants.FITBIT_PACKAGE_NAME)), INSTALL_FITBIT_INTENT);
                        }
                    }
                })
                .build();
    }
}