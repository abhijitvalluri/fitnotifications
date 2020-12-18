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

package com.abhijitvalluri.android.fitnotifications.appchoices;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.appchoices.models.AppSelection;
import com.abhijitvalluri.android.fitnotifications.appchoices.settings.AppSettingsActivity;
import com.abhijitvalluri.android.fitnotifications.appchoices.store.AppSelectionsStore;
import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.utils.DebugLog;
import com.abhijitvalluri.android.fitnotifications.utils.Func;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

/**
 * AppChoicesActivity is an activity that holds the recycler view of a list of apps and their choices.
 */
public class AppChoicesActivity extends AppCompatActivity {

    private static final int APP_SELECTIONS_REQUEST = 0;
    private static final String STATE_APP_SELECTIONS = "appSelections";
    private static final String STATE_RECYCLER_VIEW = "recyclerView";
    private static final String STATE_SETUP_COMPLETE = "setupComplete";

    private RecyclerView mRecyclerView;
    private TextView mLoadingView;
    private ProgressBar mProgressBar;
    private AppSelectionsStore mAppSelectionsStore;
    private ArrayList<AppSelection> mAppSelections;
    private PackageManager mPackageManager;
    private ActivityAdapter mAdapter;
    private SharedPreferences mPreferences;
    private boolean mShowOnlyEnabledApps;
    private boolean mSetupComplete = false;
    private Bundle LAUNCH_ACTIVITY_ANIM_BUNDLE;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, AppChoicesActivity.class);
    }

    private class AppListSetupTaskRunner {
        private final Executor executor = Executors.newSingleThreadExecutor();
        private final Handler handler = new Handler(Looper.getMainLooper());

        public void executeAsync() {
            executor.execute(() -> {
                final boolean success = appListTask();
                if (success) {
                    handler.post(AppChoicesActivity.this::setupAdapter);
                }
            });
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selector);

        LAUNCH_ACTIVITY_ANIM_BUNDLE = ActivityOptions.
                makeCustomAnimation(AppChoicesActivity.this,
                        R.transition.left_in,
                        R.transition.left_out).toBundle();

        mPackageManager = getPackageManager();
        mAppSelectionsStore = AppSelectionsStore.get(this);
        mRecyclerView = (RecyclerView) findViewById(R.id.app_selections_recycler_view);
        mLoadingView = (TextView) findViewById(R.id.app_list_loading_text_view);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        PreferenceManager.setDefaultValues(this, R.xml.main_settings, false);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mShowOnlyEnabledApps = mPreferences.getBoolean(getString(R.string.show_enabled_apps_key), false);

        if (savedInstanceState != null && getSetupStatus(savedInstanceState)) {
            mAppSelections = savedInstanceState.getParcelableArrayList(STATE_APP_SELECTIONS);

            if (mShowOnlyEnabledApps) {
                List<AppSelection> appSelectionsSubList = new ArrayList<>();

                for (AppSelection appSelection : mAppSelections) {
                    if (appSelection.isSelected()) {
                        appSelectionsSubList.add(appSelection);
                    }
                }
                mAdapter = new ActivityAdapter(appSelectionsSubList);
            } else {
                mAdapter = new ActivityAdapter(mAppSelections);
            }
            mRecyclerView.setAdapter(mAdapter);

            Parcelable listState = savedInstanceState.getParcelable(STATE_RECYCLER_VIEW);
            if (listState != null && mRecyclerView.getLayoutManager() != null) {
                mRecyclerView.getLayoutManager().onRestoreInstanceState(listState);
            }
            mSetupComplete = getSetupStatus(savedInstanceState);

            DebugLog log = DebugLog.get(getApplicationContext());
            if (log.isEnabled()) {
                log.writeLog("Restoring state may cause problems for some users");
                log.writeLog("Number of applications: " + mAppSelections.size());
                log.writeLog("Loading Text View text: " + mLoadingView.getText());
                log.writeLog("Loading Text View status: " + mLoadingView.getVisibility());
                log.writeLog("Setup status: " + (mSetupComplete ? "true" : "false"));
            }
        } else {
            mLoadingView.setText(getString(R.string.app_list_loading_text));
            mRecyclerView.setVisibility(View.GONE);
            mLoadingView.setVisibility(View.VISIBLE);
            mProgressBar.setVisibility(View.VISIBLE);
            new AppListSetupTaskRunner().executeAsync();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelableArrayList(STATE_APP_SELECTIONS, mAppSelections);
        savedInstanceState.putBoolean(STATE_SETUP_COMPLETE, mSetupComplete);
        if (mRecyclerView.getLayoutManager() != null) {
            Parcelable listState = mRecyclerView.getLayoutManager().onSaveInstanceState();
            savedInstanceState.putParcelable(STATE_RECYCLER_VIEW, listState);
        }

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_search, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_app_search);
        MenuItem filterEnabledAppsItem = menu.findItem(R.id.menu_filter_enabled);

        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_query_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return recyclerViewShowSearchResult(query);
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return recyclerViewShowSearchResult(newText);
            }
        });

        searchItem.setEnabled(false);
        filterEnabledAppsItem.setEnabled(false);
        filterEnabledAppsItem.setChecked(mShowOnlyEnabledApps);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.menu_app_search);
        MenuItem filterEnabledItem = menu.findItem(R.id.menu_filter_enabled);

        if (mAdapter == null) { // cannot search yet
            searchItem.setEnabled(false);
            filterEnabledItem.setEnabled(false);
        } else {
            searchItem.setEnabled(true);
            filterEnabledItem.setEnabled(true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_filter_enabled) {
            mShowOnlyEnabledApps = !mShowOnlyEnabledApps; // toggles the state of the filter
            mPreferences.edit().putBoolean(getString(R.string.show_enabled_apps_key), mShowOnlyEnabledApps).apply();
            item.setChecked(mShowOnlyEnabledApps);
            recyclerViewShowEnabled();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStop() {
        super.onStop();
        NLService.onAppSelectionsUpdated(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.transition.right_in, R.transition.right_out);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_SELECTIONS_REQUEST && resultCode == RESULT_OK) {
            AppSelection appSelection = data.getParcelableExtra(
                    AppSettingsActivity.APP_SELECTION_EXTRA);

            if (appSelection != null) {
                mAppSelectionsStore.updateAppSelection(appSelection);
                updateAppSelections(appSelection);
            }
        }
    }

    private boolean getSetupStatus(Bundle state) {
        return state.getBoolean(STATE_SETUP_COMPLETE);
    }

    private void updateAppSelections(AppSelection appSelection) {
        String appPackageName = appSelection.getAppPackageName();
        for (int i = 0; i != mAppSelections.size(); i++) {
            if (mAppSelections.get(i).getAppPackageName().equals(appPackageName)) {
                mAppSelections.set(i, appSelection);
                break;
            }
        }
    }

    private AppSelection getAppSelection(String appPackageName) {
        for (int i = 0; i != mAppSelections.size(); i++) {
            if (mAppSelections.get(i).getAppPackageName().equals(appPackageName)) {
                return mAppSelections.get(i);
            }
        }

        return null;
    }

    private boolean appListTask() {
        DebugLog log = DebugLog.get(getApplicationContext());
        List<ResolveInfo> packages = Func.getInstalledPackages(mPackageManager, getApplicationContext());

        if (log.isEnabled()) {
            log.writeLog("Number of packages retrieved from getInstalledPackages: " + packages.size());
        }

        // getAppSelectionsSubList is also needed for the subsequent calls to contains()
        List<AppSelection> appSelections = mAppSelectionsStore.getAppSelections();

        if (log.isEnabled()) {
            log.writeLog("Number of apps in App selection store: " + appSelections.size());
        }

        try {
            for (ResolveInfo info : packages) {
                String appPackageName = info.activityInfo.packageName;
                String appName = info.loadLabel(mPackageManager).toString();

                if (!mAppSelectionsStore.contains(appPackageName) && !appPackageName.equals(Constants.PACKAGE_NAME)) {
                    mAppSelectionsStore.addAppSelection(new AppSelection(appPackageName, appName));
                } else if (mAppSelectionsStore.contains(appPackageName) && !appName.equals(mAppSelectionsStore.getAppName(appPackageName))) {
                    AppSelection appSelection = mAppSelectionsStore.getAppSelection(appPackageName);
                    appSelection.setAppName(appName);
                    mAppSelectionsStore.updateAppSelection(appSelection);
                }
            }
        } catch (Exception e) {
            Log.e("DB_INSERT", "Error inserting appSelection entry into database. Exception: " + e.getMessage());
            AppChoicesActivity.this.runOnUiThread(() -> new AlertDialog.Builder(AppChoicesActivity.this)
                    .setTitle("Error processing apps")
                    .setMessage("There was an error while processing the apps. Please enable logs and send them to the developer.")
                    .setPositiveButton(android.R.string.ok, null)
                    .create().show());
            return false;
        }

        if (log.isEnabled()) {
            log.writeLog("Number of apps in App selection store after additions: " + mAppSelectionsStore.size());
        }

        // Remove uninstalled apps from the database.
        for (AppSelection appSelection : appSelections) {
            if (isUninstalled(appSelection.getAppPackageName(), packages)) {
                mAppSelectionsStore.deleteAppSelection(appSelection);
            }
        }

        if (log.isEnabled()) {
            log.writeLog("Number of apps in App selection store after deletions: " + mAppSelectionsStore.size());
        }

        return true;
    }

    private void recyclerViewShowEnabled() {
        if (mAdapter == null) {
            return;
        }

        if (mShowOnlyEnabledApps) {
            List<AppSelection> appSelectionsSubList = new ArrayList<>();

            for (AppSelection appSelection : mAppSelections) {
                if (appSelection.isSelected()) {
                    appSelectionsSubList.add(appSelection);
                }
            }

            updateUI(appSelectionsSubList);
        } else {
            updateUI(mAppSelections);
        }
    }

    private boolean recyclerViewShowSearchResult(String appNameSubStr) {
        if (mAdapter == null) {
            return false;
        }

        appNameSubStr = appNameSubStr.trim();
        List<AppSelection> appSelectionsSubList = new ArrayList<>();

        for (AppSelection appSelection : mAppSelections) {
            if (appSelection.getAppName().toLowerCase().contains(appNameSubStr.toLowerCase())
                    && (!mShowOnlyEnabledApps || appSelection.isSelected())) {
                appSelectionsSubList.add(appSelection);
            }
        }
        updateUI(appSelectionsSubList);

        return true;
    }

    private void updateUI(List<AppSelection> appSelections) {
        mAdapter.setAppSelectionsSubList(appSelections);
        mAdapter.notifyDataSetChanged();

        if (appSelections.isEmpty()) {
            mLoadingView.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            mLoadingView.setText(getString(R.string.app_list_empty_search));
        } else {
            mLoadingView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
        mProgressBar.setVisibility(View.GONE);
    }

    private void setupAdapter() {
        mAppSelections = mAppSelectionsStore.getAppSelections();

        DebugLog log = DebugLog.get(getApplicationContext());
        if (log.isEnabled()) {
            log.writeLog("In setupAdapter: number of apps is: " + mAppSelections.size());
        }

        if (mShowOnlyEnabledApps) {
            List<AppSelection> appSelectionsSubList = new ArrayList<>();

            for (AppSelection appSelection : mAppSelections) {
                if (appSelection.isSelected()) {
                    appSelectionsSubList.add(appSelection);
                }
            }
            mAdapter = new ActivityAdapter(appSelectionsSubList);
        } else {
            mAdapter = new ActivityAdapter(mAppSelections);
        }

        mRecyclerView.setAdapter(mAdapter);
        mLoadingView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
        mSetupComplete = true;
    }

    private boolean isUninstalled(String appPackageName, List<ResolveInfo> activities) {
        for (ResolveInfo info : activities) {
            if (info.activityInfo.packageName.equals(appPackageName)) {
                return false;
            }
        }

        return true;
    }

    private class ActivityHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private final TextView mAppNameTB;
        private final ImageView mImageView;
        private final CheckBox mAppSelectCB;

        private AppSelection mAppSelection;

        public ActivityHolder(View itemView) {
            super(itemView);

            mAppNameTB = (TextView) itemView.findViewById(R.id.appNameTextBox);
            mAppSelectCB = (CheckBox) itemView.findViewById(R.id.appSelectCheckBox);
            mImageView = (ImageView) itemView.findViewById(R.id.appIconImageView);
            ImageView settings = (ImageView) itemView.findViewById(R.id.appSettingsIcon);

            mAppSelectCB.setOnClickListener(this);
            settings.setOnClickListener(v -> {
                Intent intent = new Intent(getApplicationContext(), AppSettingsActivity.class);
                AppSelection appSelection = getAppSelection(mAppSelection.getAppPackageName());
                if (appSelection == null) { // Never going to happen but let's cover our bases
                    appSelection = mAppSelection;
                }
                intent.putExtra(AppSettingsActivity.APP_SELECTION_EXTRA, appSelection);
                startActivityForResult(intent, APP_SELECTIONS_REQUEST, LAUNCH_ACTIVITY_ANIM_BUNDLE);
            });
        }

        public void bindActivity(AppSelection appSelection) {
            mAppSelection = appSelection;
            mAppNameTB.setText(appSelection.getAppName());
            mAppSelectCB.setChecked(appSelection.isSelected());
            Drawable icon;
            try {
                icon = mPackageManager.getApplicationIcon(appSelection.getAppPackageName());
            } catch (PackageManager.NameNotFoundException e) {
                icon = null;
            }
            mImageView.setImageDrawable(icon);
        }

        @Override
        public void onClick(View v) {
            boolean isChecked = mAppSelectCB.isChecked();
            AppSelection appSelection = getAppSelection(mAppSelection.getAppPackageName());
            if (appSelection == null) { // Never going to happen but let's cover our bases
                appSelection = mAppSelection;
            }
            appSelection.setSelected(isChecked);
            mAppSelectionsStore.updateAppSelection(appSelection);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {
        private List<AppSelection> mAppSelectionsSubList;

        public ActivityAdapter(List<AppSelection> appSelectionsSubList) {
            mAppSelectionsSubList = appSelectionsSubList;
        }

        @NonNull
        @Override
        public ActivityHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(AppChoicesActivity.this);
            View view = inflater.inflate(R.layout.app_select_list_item, parent, false);
            return new ActivityHolder(view);
        }

        @Override
        public void onBindViewHolder(ActivityHolder activityHolder, int position) {
            AppSelection appSelection = mAppSelectionsSubList.get(position);
            activityHolder.bindActivity(appSelection);
        }

        @Override
        public int getItemCount() {
            return mAppSelectionsSubList.size();
        }

        public void setAppSelectionsSubList(List<AppSelection> appSelectionsSubList) {
            mAppSelectionsSubList = appSelectionsSubList;
        }
    }
}
