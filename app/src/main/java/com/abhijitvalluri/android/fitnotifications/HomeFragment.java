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

import android.app.ActivityOptions;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.setup.AppIntroActivity;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;

import java.util.Set;

/**
 * Contains the main home fragment
 */
public class HomeFragment extends Fragment {

    private final Handler mHandler = new Handler();

    private static boolean mDismissPlaceholderNotif;
    private static int mPlaceholderNotifDismissDelayMillis;

    private TextView mInstructionTV;
    private TextView mAppSelectionTV;
    private Button mServiceButton;
    private TextView mDemoTV;
    private TextView mNotificationAccessTV;
    private TextView mServiceStateTV;
    private TextView mImproveTransliterationTV;
    private TextView mRateAppTV;

    private SharedPreferences mPreferences;
    private Bundle LAUNCH_ACTIVITY_ANIM_BUNDLE;
    private Context mContext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mInstructionTV = (TextView) v.findViewById(R.id.instructionsTV);
        mAppSelectionTV = (TextView) v.findViewById(R.id.appSelectionTV);
        mServiceButton = (Button) v.findViewById(R.id.serviceButton);
        mDemoTV = (TextView) v.findViewById(R.id.demoNotifTV);
        mNotificationAccessTV = (TextView) v.findViewById(R.id.notificationAccessTV);
        mServiceStateTV = (TextView) v.findViewById(R.id.serviceStateText);
        mImproveTransliterationTV = (TextView) v.findViewById(R.id.improve_transliteration);
        mRateAppTV = (TextView) v.findViewById(R.id.rate_app);

        mContext = getContext();
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
    public void onResume() {
        super.onResume();
        initializeServiceButtons();
        updateNotificationAccessText();
    }

    // TODO: implement proper callbacks where possible
    //TODO: use Google Analytics! HIGH PRIORITY!
    public static void onPlaceholderNotifSettingUpdated(boolean dismissNotif, int delaySeconds) {
        mDismissPlaceholderNotif = dismissNotif;
        mPlaceholderNotifDismissDelayMillis = delaySeconds*1000;
    }

    private void activateTextViewLinks() {
        mInstructionTV.setText(R.string.instructions);
        mInstructionTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AppIntroActivity.newIntent(mContext), LAUNCH_ACTIVITY_ANIM_BUNDLE);
            }
        });

        mAppSelectionTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(AppChoicesActivity.newIntent(mContext), LAUNCH_ACTIVITY_ANIM_BUNDLE);
            }
        });
    }

    private void initializeSettings() {
        PreferenceManager.setDefaultValues(mContext, R.xml.main_settings, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mDismissPlaceholderNotif = mPreferences.getBoolean(
                getString(R.string.dismiss_placeholder_notif_key), false);
        mPlaceholderNotifDismissDelayMillis = mPreferences.getInt(
                getString(R.string.placeholder_dismiss_delay_key), Constants.DEFAULT_DELAY_SECONDS)
                *1000;
    }

    private void initializeButtons() {
        initializeServiceButtons();
        initializeDemoButton();
        initializeEnableNotificationButton();

        mImproveTransliterationTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String uriText =
                        "mailto:android@abhijitvalluri.com" +
                                "?subject=" + Uri.encode("Improve Transliterations for <SPECIFY_LANGUAGE>") +
                                "&body=" + Uri.encode("<MESSAGE>");

                Uri uri = Uri.parse(uriText);

                Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                sendIntent.setData(uri);
                startActivity(Intent.createChooser(sendIntent, "Send email"));
            }
        });

        mRateAppTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse("market://details?id=" + Constants.PACKAGE_NAME);
                Intent gotoPlayStore = new Intent(Intent.ACTION_VIEW, uri);
                // To count with Play market backstack, After pressing back button,
                // to taken back to our application, we need to add following flags to intent.
                gotoPlayStore.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
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
        mNotificationAccessTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new
                        Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"));
            }
        });
    }

    private void initializeDemoButton() {
        mDemoTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bundle newExtra = new Bundle();

                NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext);
                String notificationText = "Sample notification subject";
                String notificationBigText = "Sample notification body. This is where the details of the notification will be shown.";

                StringBuilder sb = new StringBuilder();
                sb.append("[").append("example").append("] ");
                sb.append(notificationText);
                if (notificationBigText.length() > 0) {
                    sb.append(" -- ").append(notificationBigText);
                }

                RemoteViews contentView = new RemoteViews(mContext.getPackageName(), R.layout.custom_notification);
                contentView.setTextViewText(R.id.customNotificationText, getString(R.string.placeholder_notification_text));
                builder.setSmallIcon(R.drawable.ic_sms_white_24dp)
                        .setContentText(sb.toString())
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
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                builder.setContentIntent(settingsPendingIntent).setAutoCancel(true);

                ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                        .notify(Constants.NOTIFICATION_ID, builder.build());

                Toast.makeText(mContext, getString(R.string.test_notification_sent), Toast.LENGTH_LONG)
                        .show();

                if (mDismissPlaceholderNotif) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE))
                                    .cancel(Constants.NOTIFICATION_ID);
                        }
                    }, mPlaceholderNotifDismissDelayMillis);
                }
            }
        });
    }

    private void updateNotificationAccessText() {
        Set<String> EnabledListenerPackagesSet = NotificationManagerCompat.
                getEnabledListenerPackages(getContext());
        if (EnabledListenerPackagesSet.contains(Constants.PACKAGE_NAME)
                && EnabledListenerPackagesSet.contains(Constants.FITBIT_PACKAGE_NAME)) {
            mNotificationAccessTV.setText(getString(R.string.notification_access_disable_textView));
        } else {
            mNotificationAccessTV.setText(getString(R.string.notification_access_enable_textView));
        }
    }

    private void initializeServiceButtons() {
        boolean serviceEnabled = NLService.isEnabled();

        if(serviceEnabled) {
            mServiceButton.setText(R.string.turn_off_service);
            mServiceStateTV.setText(R.string.service_on);
            mServiceStateTV.setTextColor(ContextCompat.getColor(getContext(), R.color.brightGreen));
            mServiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.stopService(new Intent(mContext, NLService.class));
                    NLService.setEnabled(false);
                    initializeServiceButtons();
                }
            });
        } else {
            mServiceButton.setText(R.string.turn_on_service);
            mServiceStateTV.setText(R.string.service_off);
            mServiceStateTV.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            mServiceButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mContext.startService(new Intent(mContext, NLService.class)); // TODO: Check if I should use the same intent I used to start the service
                                                                                  // otherwise it may not stop the correct service.
                    NLService.setEnabled(true);
                    initializeServiceButtons();
                }
            });
        }
    }
}
