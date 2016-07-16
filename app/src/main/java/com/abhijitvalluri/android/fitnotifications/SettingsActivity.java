package com.abhijitvalluri.android.fitnotifications;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;

/**
 * This is the main settings activity to store the various settings in the SharedPreferences
 */
public class SettingsActivity extends AppCompatActivity {

    private static Context sHomeActivityContext;

    public static Intent newIntent(Context packageContext) {
        sHomeActivityContext = packageContext;
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

        // Callback interface to let HomeActivity update the setup menu
        public interface SetupCallback {
            void onOverrideInteractiveSetup(boolean enabled);
        }

        private SharedPreferences mPreferences;
        private SetupCallback mSetupCallback;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mSetupCallback = (SetupCallback) sHomeActivityContext;

            addPreferencesFromResource(R.xml.main_settings);
            mPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

            // Initialize delay summaries
            String key = getString(R.string.placeholder_dismiss_delay_key);
            updateDelaySummary(key,
                    mPreferences.getInt(key, Constants.DEFAULT_DELAY_SECONDS),
                    R.plurals.placeholder_dismiss_delay_summary,
                    R.string.placeholder_dismiss_delay_summary0,
                    mPreferences.getBoolean(getString(R.string.dismiss_placeholder_notif_key), false));

            key = getString(R.string.relayed_dismiss_delay_key);
            updateDelaySummary(key,
                    mPreferences.getInt(key, Constants.DEFAULT_DELAY_SECONDS),
                    R.plurals.relayed_dismiss_delay_summary,
                    R.string.relayed_dismiss_delay_summary0,
                    mPreferences.getBoolean(getString(R.string.dismiss_relayed_notif_key), false));

            key = getString(R.string.notif_limit_duration_key);
            updateDelaySummary(key,
                    mPreferences.getInt(key, Constants.DEFAULT_DELAY_SECONDS),
                    R.plurals.notif_limit_duration_summary,
                    R.string.notif_limit_duration_summary0,
                    mPreferences.getBoolean(getString(R.string.limit_notif_key), false));

            key = getString(R.string.override_interactive_setup_key);
            updateInteractiveSetupSummary(key);
        }

        private void updateInteractiveSetupSummary(String summaryKey) {
            if (mPreferences.getBoolean(summaryKey, false)) {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.override_interactive_setup_enabled_summary));
            } else {
                findPreference(summaryKey).setSummary(getResources().getString(R.string.override_interactive_setup_disabled_summary));
            }
        }

        private void updateDelaySummary(
                String summaryKey, int delaySeconds, int pluralsId, int stringId, boolean enabled) {
            if (enabled) {
                findPreference(summaryKey).setSummary(getResources()
                        .getQuantityString(pluralsId, delaySeconds, delaySeconds));
            } else {
                findPreference(summaryKey).setSummary(getString(stringId));
            }
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
                updateDelaySummary(getString(R.string.placeholder_dismiss_delay_key),
                              delaySeconds,
                              R.plurals.placeholder_dismiss_delay_summary,
                              R.string.placeholder_dismiss_delay_summary0,
                              dismissNotif);
                HomeActivity.onPlaceholderNotifSettingUpdated(dismissNotif, delaySeconds);
                NLService.onPlaceholderNotifSettingUpdated(dismissNotif, delaySeconds);

            } else if (key.equals(getString(R.string.dismiss_relayed_notif_key))
                    || key.equals(getString(R.string.relayed_dismiss_delay_key))) {
                boolean dismissNotif = mPreferences.getBoolean(
                        getString(R.string.dismiss_relayed_notif_key), false);
                int delaySeconds = mPreferences.getInt(
                        getString(R.string.relayed_dismiss_delay_key),
                        Constants.DEFAULT_DELAY_SECONDS);
                updateDelaySummary(getString(R.string.relayed_dismiss_delay_key),
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
                updateDelaySummary(getString(R.string.notif_limit_duration_key),
                              durationSeconds,
                              R.plurals.notif_limit_duration_summary,
                              R.string.notif_limit_duration_summary0,
                              limitNotif);
                NLService.onLimitNotificationSettingUpdated(limitNotif, durationSeconds);
            } else if (key.equals(getString(R.string.override_interactive_setup_key))) {
                updateInteractiveSetupSummary(key);
                mSetupCallback.onOverrideInteractiveSetup(!mPreferences.getBoolean(key, false));
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
