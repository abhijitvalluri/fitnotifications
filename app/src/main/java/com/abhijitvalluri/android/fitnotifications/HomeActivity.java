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

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.abhijitvalluri.android.fitnotifications.setup.AppIntroActivity;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;

/**
 * Main activity for the app
 */

public class HomeActivity extends AppCompatActivity {

    private static final int APP_INTRO_FIRST_LAUNCH_INTENT = 1;

    private Bundle LAUNCH_ACTIVITY_ANIM_BUNDLE;
    private DrawerLayout mDrawerLayout;
    private SmoothDrawerToggle mDrawerToggle;
    private Toolbar mToolbar;
    private NavigationView mNavDrawer;
    private SharedPreferences mPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Set a Toolbar to replace the ActionBar.
        mToolbar = (Toolbar) findViewById(R.id.home_toolbar);
        setSupportActionBar(mToolbar);

        LAUNCH_ACTIVITY_ANIM_BUNDLE = ActivityOptions.
                makeCustomAnimation(HomeActivity.this,
                        R.transition.left_in,
                        R.transition.left_out).toBundle();

        // Initialize settings to defaults

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new SmoothDrawerToggle(this, mDrawerLayout, mToolbar, R.string.drawer_open,  R.string.drawer_close);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        mNavDrawer = (NavigationView) findViewById(R.id.navDrawer);
        setupDrawerContent(mNavDrawer);


        PreferenceManager.setDefaultValues(this, R.xml.main_settings, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        Fragment frag = new HomeFragment();

        // Add a new fragment to the appropriate view element
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.findFragmentById(R.id.flContent) == null) {
            fragmentManager.beginTransaction().add(R.id.flContent, frag).commit();
        }

        if (mPreferences.getInt(getString(R.string.version_key), 0) < Constants.VERSION_CODE
                && mPreferences.getInt(getString(R.string.version_key), 0) > 0) {

            mNavDrawer.setCheckedItem(R.id.nav_whats_new);
            setTitle(R.string.whats_new);
            frag = InfoFragment.newInstance(getString(R.string.whats_new_text));
            fragmentManager.beginTransaction().replace(R.id.flContent, frag).commit();

            mPreferences.edit().putInt(getString(R.string.version_key), Constants.VERSION_CODE).apply();
        }

        if (!mPreferences.getBoolean(getString(R.string.done_first_launch_key), false)) { // This is the first launch
            startActivityForResult(new Intent(HomeActivity.this, AppIntroActivity.class),
                    APP_INTRO_FIRST_LAUNCH_INTENT,
                    LAUNCH_ACTIVITY_ANIM_BUNDLE);
        }

    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
            new NavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(MenuItem menuItem) {
                    selectDrawerItem(menuItem);
                    return true;
                }
            });
    }

    private void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the fragment to show based on nav item clicked
        final Fragment frag;
        Fragment currFrag = getSupportFragmentManager().findFragmentById(R.id.flContent);
        boolean isInfoFragment = false;

        if (currFrag instanceof InfoFragment) {
            isInfoFragment = true;
        }

        setTitle(menuItem.getTitle());

        switch (menuItem.getItemId()) {
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
                    ((InfoFragment) currFrag).updateWebViewContent(getString(R.string.contact_us_text));
                    frag = null;
                } else {
                    frag = InfoFragment.newInstance(getString(R.string.contact_us_text));
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
            mDrawerToggle.runWhenIdle(new Runnable() {
                @Override
                public void run() {
                    // Insert the fragment by replacing any existing fragment
                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().replace(R.id.flContent, frag).commit();
                }
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
    public void onConfigurationChanged(Configuration newConfig) {
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
    public boolean onOptionsItemSelected(MenuItem item) {

        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.menu_main_settings:
                startActivity(SettingsActivity.newIntent(this), LAUNCH_ACTIVITY_ANIM_BUNDLE);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private class SmoothDrawerToggle extends ActionBarDrawerToggle {
        private Runnable runnable;

        public SmoothDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
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

        public void runWhenIdle(Runnable runnable) {
            this.runnable = runnable;
        }
    }
}

