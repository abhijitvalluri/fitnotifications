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

package com.abhijitvalluri.android.fitnotifications.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;

import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.services.NLService;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

/**
 * Widget for the Fit Notifications app
 */
public class ServiceToggle extends AppWidgetProvider {

    private static final String TOGGLE_CLICKED = "serviceToggleButtonClick";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {
            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.service_toggle_widget);
            if (NLService.isEnabled()) {
                views.setImageViewResource(R.id.widgetToggleButton,R.drawable.ic_speaker_notes_white_48dp);
                views.setInt(R.id.widgetToggleButton, "setBackgroundResource", R.drawable.round_rectangle_green);
                views.setTextViewText(R.id.widgetToggleText, context.getString(R.string.widget_on_text));
                views.setTextColor(R.id.widgetToggleText, ContextCompat.getColor(context, R.color.green));
            } else {
                views.setImageViewResource(R.id.widgetToggleButton,R.drawable.ic_speaker_notes_off_white_48dp);
                views.setInt(R.id.widgetToggleButton, "setBackgroundResource", R.drawable.round_rectangle_red);
                views.setTextViewText(R.id.widgetToggleText, context.getString(R.string.widget_off_text));
                views.setTextColor(R.id.widgetToggleText, ContextCompat.getColor(context, R.color.red));
            }

            views.setOnClickPendingIntent(
                                    R.id.widgetToggleButton,
                                    getPendingSelfIntent(context, appWidgetId));

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (TOGGLE_CLICKED.equals(intent.getAction())) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.service_toggle_widget);
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
            if (NLService.isEnabled()) {
                preferences.edit().putBoolean(context.getString(R.string.notification_listener_service_state_key), false).apply();
                NLService.setEnabled(false);
                views.setImageViewResource(R.id.widgetToggleButton, R.drawable.ic_speaker_notes_off_white_48dp);
                views.setInt(R.id.widgetToggleButton, "setBackgroundResource", R.drawable.round_rectangle_red);
                views.setTextViewText(R.id.widgetToggleText, context.getString(R.string.widget_off_text));
                views.setTextColor(R.id.widgetToggleText, ContextCompat.getColor(context, R.color.red));
            } else {
                preferences.edit().putBoolean(context.getString(R.string.notification_listener_service_state_key), true).apply();
                NLService.setEnabled(true);
                views.setImageViewResource(R.id.widgetToggleButton,R.drawable.ic_speaker_notes_white_48dp);
                views.setInt(R.id.widgetToggleButton, "setBackgroundResource", R.drawable.round_rectangle_green);
                views.setTextViewText(R.id.widgetToggleText, context.getString(R.string.widget_on_text));
                views.setTextColor(R.id.widgetToggleText, ContextCompat.getColor(context, R.color.green));
            }

            views.setOnClickPendingIntent(
                    R.id.widgetToggleButton,
                    getPendingSelfIntent(context, 0));

            ComponentName componentName = new ComponentName(context, ServiceToggle.class);
            appWidgetManager.updateAppWidget(componentName, views);
        }
    }

    private PendingIntent getPendingSelfIntent(Context context, int appWidgetId) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(ServiceToggle.TOGGLE_CLICKED);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, PendingIntent.FLAG_IMMUTABLE);
    }
}
