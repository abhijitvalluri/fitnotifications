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

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;
import android.view.MenuItem;

import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;

/**
 * This is the main settings activity to store the various settings in the SharedPreferences
 */
public class SettingsActivity extends AppCompatActivity {

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        private SharedPreferences mPreferences;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.main_settings);
            mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Initialize delay summaries
            String key = getString(R.string.placeholder_dismiss_delay_key);
            boolean prefValue = mPreferences.getBoolean(getString(R.string.dismiss_placeholder_notif_key), false);
            updateSummaryWithPlurals(key,
                    mPreferences.getInt(key, Constants.DEFAULT_DELAY_SECONDS),
                    R.plurals.placeholder_dismiss_delay_summary,
                    R.string.placeholder_dismiss_delay_summary0,
                    prefValue);

            key = getString(R.string.relayed_dismiss_delay_key);
            prefValue = mPreferences.getBoolean(getString(R.string.dismiss_relayed_notif_key), false);
            updateSummaryWithPlurals(key,
                    mPreferences.getInt(key, Constants.DEFAULT_DELAY_SECONDS),
                    R.plurals.relayed_dismiss_delay_summary,
                    R.string.relayed_dismiss_delay_summary0,
                    prefValue);

            key = getString(R.string.notif_limit_duration_key);
            prefValue = mPreferences.getBoolean(getString(R.string.limit_notif_key), false);
            updateSummaryWithPlurals(key,
                    mPreferences.getInt(key, Constants.DEFAULT_DELAY_SECONDS),
                    R.plurals.notif_limit_duration_summary,
                    R.string.notif_limit_duration_summary0,
                    prefValue);
            findPreference(key).setEnabled(prefValue);

            key = getString(R.string.disable_forward_screen_on_key);
            updateDisableWhenScreenOnSummary(key,
                                             mPreferences.getBoolean(key, false));

            key = getString(R.string.transliterate_notification_key);
            updateTransliterateNotificationSummary(key,
                                                   mPreferences.getBoolean(key, true));

            prefValue = mPreferences.getBoolean(getString(R.string.split_notification_key), false);
            key = getString(R.string.notification_text_limit_key);
            updateSummaryWithPlurals(key,
                                     mPreferences.getInt(key, Constants.DEFAULT_NOTIF_CHAR_LIMIT),
                                     R.plurals.notification_text_limit_summary_enabled,
                                     R.string.notification_text_limit_summary_disabled,
                                     prefValue);

            key = getString(R.string.num_split_notifications_key);
            updateSummaryWithPlurals(key,
                                     mPreferences.getInt(key, Constants.DEFAULT_NUM_NOTIF),
                                     R.plurals.num_split_notifications_summary_enabled,
                                     R.string.num_split_notifications_summary_disabled,
                                     prefValue);

            key = getString(R.string.display_app_name_key);
            updateDisplayAppNameSummary(key, mPreferences.getBoolean(key, true));
        }

        private void updateDisplayAppNameSummary(String summaryKey, boolean enable) {
            if (enable) {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.display_app_name_summary_enabled));
            } else {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.display_app_name_summary_disabled));
            }
        }

        private void updateTransliterateNotificationSummary(String summaryKey, boolean enable) {
            if (enable) {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.transliterate_notification_summary_enabled));
            } else {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.transliterate_notification_summary_disabled));
            }
        }

        private void updateDisableWhenScreenOnSummary(String summaryKey, boolean disable) {
            if (disable) {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.disable_forward_screen_on_summary));
            } else {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.enable_forward_screen_on_summary));
            }
        }

        private void updateSummaryWithPlurals(String summaryKey,
                                              int value,
                                              @PluralsRes int pluralsId,
                                              @StringRes int stringId,
                                              boolean enabled) {
            if (enabled) {
                findPreference(summaryKey).setSummary(getResources()
                        .getQuantityString(pluralsId, value, value));
            } else {
                findPreference(summaryKey).setSummary(getString(stringId));
            }

            findPreference(summaryKey).setEnabled(enabled);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (key.equals(getString(R.string.dismiss_placeholder_notif_key))
                    || key.equals(getString(R.string.placeholder_dismiss_delay_key))) {
                boolean dismissNotif = mPreferences.getBoolean(
                        getString(R.string.dismiss_placeholder_notif_key), false);
                int delaySeconds = mPreferences.getInt(
                        getString(R.string.placeholder_dismiss_delay_key), 
                        Constants.DEFAULT_DELAY_SECONDS);
                updateSummaryWithPlurals(getString(R.string.placeholder_dismiss_delay_key),
                              delaySeconds,
                              R.plurals.placeholder_dismiss_delay_summary,
                              R.string.placeholder_dismiss_delay_summary0,
                              dismissNotif);
                HomeFragment.onPlaceholderNotifSettingUpdated(dismissNotif, delaySeconds);
                NLService.onPlaceholderNotifSettingUpdated(dismissNotif, delaySeconds);

            } else if (key.equals(getString(R.string.dismiss_relayed_notif_key))
                    || key.equals(getString(R.string.relayed_dismiss_delay_key))) {
                boolean dismissNotif = mPreferences.getBoolean(
                        getString(R.string.dismiss_relayed_notif_key), false);
                int delaySeconds = mPreferences.getInt(
                        getString(R.string.relayed_dismiss_delay_key),
                        Constants.DEFAULT_DELAY_SECONDS);
                updateSummaryWithPlurals(getString(R.string.relayed_dismiss_delay_key),
                              delaySeconds,
                              R.plurals.relayed_dismiss_delay_summary,
                              R.string.relayed_dismiss_delay_summary0,
                              dismissNotif);
                NLService.onRelayedNotifSettingUpdated(dismissNotif, delaySeconds);

            } else if (key.equals(getString(R.string.limit_notif_key))
                    || key.equals(getString(R.string.notif_limit_duration_key))) {
                boolean limitNotif = mPreferences.getBoolean(
                        getString(R.string.limit_notif_key), false);
                int durationSeconds = mPreferences.getInt(
                        getString(R.string.notif_limit_duration_key),
                        Constants.DEFAULT_DELAY_SECONDS);
                updateSummaryWithPlurals(getString(R.string.notif_limit_duration_key),
                              durationSeconds,
                              R.plurals.notif_limit_duration_summary,
                              R.string.notif_limit_duration_summary0,
                              limitNotif);
                NLService.onLimitNotificationSettingUpdated(limitNotif, durationSeconds);
            } else if (key.equals(getString(R.string.disable_forward_screen_on_key))) {
                boolean disable = mPreferences.getBoolean(key, false);
                updateDisableWhenScreenOnSummary(key, disable);
                NLService.onDisableWhenScreenOnUpdated(disable);
            } else if (key.equals(getString(R.string.transliterate_notification_key))) {
                boolean enable = mPreferences.getBoolean(key, true);
                updateTransliterateNotificationSummary(key, enable);
                NLService.onTransliterateNotificationUpdated(enable);
            } else if (key.equals(getString(R.string.split_notification_key))
                    || key.equals(getString(R.string.notification_text_limit_key))
                    || key.equals(getString(R.string.num_split_notifications_key))) {
                boolean enable = mPreferences.getBoolean(getString(R.string.split_notification_key),
                                                         false);
                int notifCharLimit = mPreferences.getInt(
                        getString(R.string.notification_text_limit_key),
                        Constants.DEFAULT_NOTIF_CHAR_LIMIT);
                int numSplitNotif = mPreferences.getInt(
                        getString(R.string.num_split_notifications_key),
                        Constants.DEFAULT_NUM_NOTIF);

                updateSummaryWithPlurals(
                        getString(R.string.notification_text_limit_key),
                        notifCharLimit,
                        R.plurals.notification_text_limit_summary_enabled,
                        R.string.notification_text_limit_summary_disabled,
                        enable);

                updateSummaryWithPlurals(
                        getString(R.string.num_split_notifications_key),
                        numSplitNotif,
                        R.plurals.num_split_notifications_summary_enabled,
                        R.string.num_split_notifications_summary_disabled,
                        enable);

                NLService.onSplitNotificationSettingUpdated(enable, notifCharLimit, numSplitNotif);
            } else if (key.equals(getString(R.string.display_app_name_key))) {
                boolean enable = mPreferences.getBoolean(key, true);
                updateDisplayAppNameSummary(key, enable);
                NLService.onDisplayAppNameUpdated(enable);
            } else if (key.equals(getString(R.string.forward_priority_only_notifications_key))) {
                boolean forwardOnlyPriorityNotifs = mPreferences.getBoolean(key, false);
                NLService.onForwardOnlyPriorityNotifSettingUpdated(forwardOnlyPriorityNotifs);
            }
        }

        @Override
        public void onResume() {
            super.onResume();
            mPreferences.registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            mPreferences.unregisterOnSharedPreferenceChangeListener(this);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.right_in, R.transition.right_out);
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

}
