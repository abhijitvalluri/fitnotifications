package com.abhijitvalluri.android.fitnotifications;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.abhijitvalluri.android.fitnotifications.models.AppSelection;
import com.abhijitvalluri.android.fitnotifications.services.NLService;
import com.abhijitvalluri.android.fitnotifications.utils.AppSelectionsStore;
import com.abhijitvalluri.android.fitnotifications.utils.Constants;
import com.abhijitvalluri.android.fitnotifications.utils.Func;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * AppChoicesFragment is a fragment that holds the recycler view of a list of apps and their choices.
 */
public class AppChoicesActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private TextView mLoadingView;
    private AppSelectionsStore mAppSelectionsStore;
    private List<AppSelection> mAppSelections;
    private PackageManager mPackageManager;
    private ActivityAdapter mAdapter;
    private boolean mShowOnlyEnabledApps = false;

    public static Intent newIntent(Context packageContext) {
        return new Intent(packageContext, AppChoicesActivity.class);
    }

    private class AppListSetup extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            return appListTask();
        }

        @Override
        protected void onPostExecute(Void result) {
            setupAdapter();
        }
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_selector);

        mPackageManager = getPackageManager();
        mAppSelectionsStore = AppSelectionsStore.get(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.app_selections_recycler_view);
        mLoadingView = (TextView) findViewById(R.id.app_list_loading_text_view);
        mLoadingView.setText(getString(R.string.app_list_loading_text));

        mRecyclerView.setVisibility(View.GONE);
        mLoadingView.setVisibility(View.VISIBLE);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        new AppListSetup().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_search, menu);

        MenuItem searchItem = menu.findItem(R.id.menu_app_search);
        MenuItem filterEnabledAppsItem = menu.findItem(R.id.menu_filter_enabled);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
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
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            case R.id.menu_filter_enabled:
                mShowOnlyEnabledApps = !mShowOnlyEnabledApps; // toggles the state of the filter
                item.setChecked(mShowOnlyEnabledApps);
                recyclerViewShowEnabled();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
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

    private Void appListTask() {
        List<ResolveInfo> packages = Func.getInstalledPackages(mPackageManager);
        Collections.sort(packages, new Comparator<ResolveInfo>() {
            @Override
            public int compare(ResolveInfo lhs, ResolveInfo rhs) {
                return String.CASE_INSENSITIVE_ORDER.compare(
                        lhs.loadLabel(mPackageManager).toString(),
                        rhs.loadLabel(mPackageManager).toString());
            }
        });

        // getAppSelectionsSubList is also needed for the subsequent calls to contains()
        List<AppSelection> appSelections = mAppSelectionsStore.getAppSelections();

        for (ResolveInfo info : packages) {
            String appPackageName = info.activityInfo.packageName;
            String appName = info.loadLabel(mPackageManager).toString();

            if (!mAppSelectionsStore.contains(appPackageName) && !appPackageName.equals(Constants.PACKAGE_NAME)) {
                mAppSelectionsStore.addAppSelection(new AppSelection(appPackageName, appName));
            }
        }

        // Remove uninstalled apps from the database.
        for (AppSelection appSelection : appSelections) {
            if (isUninstalled(appSelection.getAppPackageName(), packages)) {
                mAppSelectionsStore.deleteAppSelection(appSelection);
            }
        }

        return null;
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
                    &&(!mShowOnlyEnabledApps || appSelection.isSelected())) {
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
        }
         else {
            mLoadingView.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void setupAdapter() {
        mAppSelections = mAppSelectionsStore.getAppSelections();
        mAdapter = new ActivityAdapter(mAppSelections);
        mRecyclerView.setAdapter(mAdapter);
        mLoadingView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
        invalidateOptionsMenu();
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
        private AppSelection mAppSelection;
        private TextView mAppNameTB;
        private ImageView mImageView;
        private CheckBox mAppSelectCB;

        public ActivityHolder(View itemView) {
            super(itemView);

            mAppNameTB = (TextView) itemView.findViewById(R.id.appNameTextBox);
            mAppSelectCB = (CheckBox) itemView.findViewById(R.id.appSelectCheckBox);
            mImageView = (ImageView) itemView.findViewById(R.id.appIconImageView);
            mAppSelectCB.setOnClickListener(this);
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
            mAppSelection.setSelected(isChecked);
            mAppSelectionsStore.updateAppSelection(mAppSelection);
        }
    }

    private class ActivityAdapter extends RecyclerView.Adapter<ActivityHolder> {
        private List<AppSelection> mAppSelectionsSubList;

        public ActivityAdapter(List<AppSelection> appSelectionsSubList) {
            mAppSelectionsSubList = appSelectionsSubList;
        }

        @Override
        public ActivityHolder onCreateViewHolder(ViewGroup parent, int viewType) {
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
