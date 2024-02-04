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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.appchoices.store.AppSelectionsStore;
import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.settings.SettingsActivity;
import com.abhijitvalluri.android.fitnotifications.setup.AppIntroActivity;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;


/**
 * Main activity for the app
 */

public class HomeActivity extends AppCompatActivity {

    private static final int APP_INTRO_FIRST_LAUNCH_INTENT = 1;

    private Bundle LAUNCH_ACTIVITY_ANIM_BUNDLE;
    private DrawerLayout mDrawerLayout;
    private SmoothDrawerToggle mDrawerToggle;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set a Toolbar to replace the ActionBar.
        Toolbar toolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        LAUNCH_ACTIVITY_ANIM_BUNDLE = ActivityOptions.
                makeCustomAnimation(HomeActivity.this,
                        R.transition.left_in,
                        R.transition.left_out).toBundle();

        // Initialize settings to defaults

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new SmoothDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        NavigationView navDrawer = (NavigationView) findViewById(R.id.navDrawer);
        setupDrawerContent(navDrawer);


        PreferenceManager.setDefaultValues(this, R.xml.main_settings, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Fragment frag = new HomeFragment();

        // Add a new fragment to the appropriate view element
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.flContent) == null) {
            fragmentManager.beginTransaction().add(R.id.flContent, frag).commit();
        }

        if (mPreferences.getInt(getString(R.string.version_key), 0) < Constants.VERSION_CODE
                && mPreferences.getBoolean(getString(R.string.done_first_launch_key), false)) {
            // App has been updated

            mPreferences.edit().putInt(getString(R.string.version_key), Constants.VERSION_CODE).apply();

            // Disable dismiss placeholder notification setting because it makes the notification disappear
            // on the Fitbit device when the notification is dismissed on the phone.
            mPreferences.edit()
                    .putBoolean(getString(R.string.dismiss_placeholder_notif_key), false).apply();

            int delaySeconds = mPreferences.getInt(
                    getString(R.string.placeholder_dismiss_delay_key),
                    Constants.DEFAULT_DELAY_SECONDS);
            HomeFragment.onPlaceholderNotifSettingUpdated(false, delaySeconds);
            NLService.onPlaceholderNotifSettingUpdated(false, delaySeconds);

            navDrawer.setCheckedItem(R.id.nav_whats_new);
            setTitle(R.string.whats_new);
            frag = InfoFragment.newInstance(getString(R.string.whats_new_text));
            fragmentManager.beginTransaction().replace(R.id.flContent, frag).commit();

            // Open the database to update it in case the version is incremented.
            AppSelectionsStore store = AppSelectionsStore.get(this);
            Log.i("FitNotificationInfo", "Checking db in case version is incremented: " + store.toString());
        }

        if (!mPreferences.getBoolean(getString(R.string.done_first_launch_key), false)) { // This is the first launch
            startActivityForResult(new Intent(HomeActivity.this, AppIntroActivity.class),
                    APP_INTRO_FIRST_LAUNCH_INTENT,
                    LAUNCH_ACTIVITY_ANIM_BUNDLE);
        }

        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            if (manager == null) {
                Log.e("FitNotificationErrors", "Error: Notification manager is null!");
                return;
            }

            manager.deleteNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_003);
            manager.deleteNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_005);
            manager.deleteNotificationChannel(Constants.NOTIFICATION_CHANNEL_ID_007);

            String id = Constants.NOTIFICATION_CHANNEL_ID_CURRENT;
            CharSequence name = getString(R.string.notification_channel_name);
            String desc = getString(R.string.notification_channel_desc);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(id, name, importance);
            /* Setting the notification sound to a silent sound file (silent.ogg) causes the
               sound of the relayed notifications to also be suppressed...

               Need to figure out a way to make my placeholder notif not make a sound, and not
               suppress sound of relayed notif. and to make fitbit actually relay notif and vibrate

               fitbit does not relay notif if importance is less than default (low or min).
               SO fitbit only relays notifs with default or higher importance. But such notifs make sound. SO placeholder will make sound
               If importance is low, placeholder wont make sound but won't get relayed.

               If I set importance to default but set sound to null, placeholder makes no sound and notif is relayed but fitbit won't vibrate, I think?
               Must verify if Fitbit does not vibrate or if it is just my Fitbit Ionic being very flaky/inconsistent. This may be THE SOLUTION.
               Note: I will implement this! Check if users report issues.

               If I enable the always vibrate feature on Fitbit app then it will vibrate. But then it will also relay notifs in DND mode.
               I can then also enable setting in my app to not relay notifs in DND mode...
            */

            channel.setSound(null, null);
            channel.setShowBadge(false);
            channel.setDescription(desc);
            channel.enableLights(false);
            channel.enableVibration(false);
            manager.createNotificationChannel(channel);
        }

    }

    private void sendFeedback() {
        final LinearLayout layout = new LinearLayout(HomeActivity.this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(32,16,32,16);

        final TextView title = new TextView(HomeActivity.this);
        title.setText(R.string.feedback_step1_message);
        title.setTextSize(18);
        final EditText input = new EditText(HomeActivity.this);
        layout.addView(title);
        layout.addView(input);

        final AlertDialog dialog = new AlertDialog.Builder(HomeActivity.this)
            .setTitle(R.string.feedback_step1_title)
            .setView(layout)
            .setPositiveButton(android.R.string.ok, null)
            .setNegativeButton(android.R.string.cancel, null)
            .create();

        dialog.setOnShowListener((DialogInterface.OnShowListener) dialogInterface -> {

            Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
            button.setOnClickListener(view -> {
                String feedback = input.getText().toString();
                feedback = feedback.trim();
                if (feedback.isEmpty()) {
                    Toast.makeText(HomeActivity.this, R.string.no_feedback_message, Toast.LENGTH_SHORT).show();
                } else {
                    feedback += "\n\n";
                    String uriText =
                            "mailto:android@abhijitvalluri.com" +
                                    "?subject=" + Uri.encode(getString(R.string.email_subject)) +
                                    "&body=" + Uri.encode(feedback);

                    Uri uri = Uri.parse(uriText);

                    Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
                    sendIntent.setData(uri);
                    startActivity(Intent.createChooser(sendIntent, getString(R.string.select_send_feedback_app)));

                    dialog.dismiss();
                }
            });
        });

        dialog.show();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    selectDrawerItem(menuItem);
                    return true;
                });
    }

    public static Intent userDonationIntent() {
        String url = "https://abhijitvalluri.com/android/donate";
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        return i;
    }

    @SuppressLint("NonConstantResourceId")
    private void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        final Fragment frag;
        Fragment currFrag = getSupportFragmentManager().findFragmentById(R.id.flContent);
        boolean isInfoFragment = currFrag instanceof InfoFragment;

        if (menuItem.getItemId() != R.id.send_feedback &&
            menuItem.getItemId() != R.id.nav_donate) {
            setTitle(menuItem.getTitle());
        }

        switch (menuItem.getItemId()) {
            case R.id.nav_donate:
                startActivity(userDonationIntent());
                mDrawerLayout.closeDrawers();
                return;
            case R.id.send_feedback:
                sendFeedback();
                mDrawerLayout.closeDrawers();
                return;
            case R.id.nav_home:
                setTitle(R.string.app_name);
                frag = new HomeFragment();
                break;
            case R.id.nav_about_app:
                if (isInfoFragment) {
                    ((InfoFragment) currFrag).updateWebViewContent(getString(R.string.about_app_text));
                    frag = null;
                } else {
                    frag = InfoFragment.newInstance(getString(R.string.about_app_text));
                }
                break;
            case R.id.nav_whats_new:
                if (isInfoFragment) {
                    ((InfoFragment) currFrag).updateWebViewContent(getString(R.string.whats_new_text));
                    frag = null;
                } else {
                    frag = InfoFragment.newInstance(getString(R.string.whats_new_text));
                }
                break;
            case R.id.nav_faqs:
                if (isInfoFragment) {
                    ((InfoFragment) currFrag).updateWebViewContent(getString(R.string.faqs_text));
                    frag = null;
                } else {
                    frag = InfoFragment.newInstance(getString(R.string.faqs_text));
                }
                break;
            case R.id.nav_manual_setup:
                if (isInfoFragment) {
                    ((InfoFragment) currFrag).updateWebViewContent(getString(R.string.instructions_text));
                    frag = null;
                } else {
                    frag = InfoFragment.newInstance(getString(R.string.instructions_text));
                }
                break;
            case R.id.nav_opensource:
                if (isInfoFragment) {
                    ((InfoFragment) currFrag).updateWebViewContent(getString(R.string.opensource_text));
                    frag = null;
                } else {
                    frag = InfoFragment.newInstance(getString(R.string.opensource_text));
                }
                break;
            case R.id.nav_contact:
                setTitle(menuItem.getTitle());
                if (isInfoFragment) {
                    ((InfoFragment) currFrag).updateWebViewContent(getString(R.string.smart_dino_text));
                    frag = null;
                } else {
                    frag = InfoFragment.newInstance(getString(R.string.smart_dino_text));
                }
                break;
            default:
                // something unexpected has happened Log it may be?
                return;
        }

        // Highlight the selected item has been done by NavigationView
        menuItem.setChecked(true);
        // Set action bar title
        if (frag != null) {
            mDrawerToggle.runWhenIdle(() -> {
                // Insert the fragment by replacing any existing fragment
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction().replace(R.id.flContent, frag).commit();
            });
        }

        // Close the navigation drawer
        mDrawerLayout.closeDrawers();
    }

        @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_settings, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.menu_main_settings) {
            startActivity(SettingsActivity.newIntent(this), LAUNCH_ACTIVITY_ANIM_BUNDLE);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_INTRO_FIRST_LAUNCH_INTENT) {
            if (resultCode == Activity.RESULT_OK) {
                mPreferences.edit().putBoolean(getString(R.string.done_first_launch_key), true).apply();
            } else {
                new AlertDialog.Builder(HomeActivity.this)
                        .setMessage(getString(R.string.setup_incomplete))
                        .setTitle(getString(R.string.setup_incomplete_title))
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> startActivityForResult(new Intent(HomeActivity.this, AppIntroActivity.class), APP_INTRO_FIRST_LAUNCH_INTENT, LAUNCH_ACTIVITY_ANIM_BUNDLE))
                        .setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            mPreferences.edit().putBoolean(getString(R.string.done_first_launch_key), true).apply();
                            Toast.makeText(HomeActivity.this, R.string.setup_incomplete_cancel, Toast.LENGTH_LONG).show();
                        })
                        .create()
                        .show();
            }
        }
    }

    private class SmoothDrawerToggle extends ActionBarDrawerToggle {
        private Runnable runnable;

        SmoothDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            invalidateOptionsMenu();
        }
        @Override
        public void onDrawerClosed(View view) {
            super.onDrawerClosed(view);
            invalidateOptionsMenu();
        }
        @Override
        public void onDrawerStateChanged(int newState) {
            super.onDrawerStateChanged(newState);
            if (runnable != null && newState == DrawerLayout.STATE_SETTLING) {
                runnable.run();
                runnable = null;
            }
        }

        void runWhenIdle(Runnable runnable) {
            this.runnable = runnable;
        }
    }
}

