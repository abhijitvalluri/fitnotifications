package com.abhijitvalluri.android.fitnotifications.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.widget.RemoteViews;

import com.abhijitvalluri.android.fitnotifications.R;
import com.abhijitvalluri.android.fitnotifications.services.NLService;

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
                                    getPendingSelfIntent(context, appWidgetId, TOGGLE_CLICKED));

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
            if (NLService.isEnabled()) {
                NLService.setEnabled(false);
                views.setImageViewResource(R.id.widgetToggleButton, R.drawable.ic_speaker_notes_off_white_48dp);
                views.setInt(R.id.widgetToggleButton, "setBackgroundResource", R.drawable.round_rectangle_red);
                views.setTextViewText(R.id.widgetToggleText, context.getString(R.string.widget_off_text));
                views.setTextColor(R.id.widgetToggleText, ContextCompat.getColor(context, R.color.red));
            } else {
                NLService.setEnabled(true);
                views.setImageViewResource(R.id.widgetToggleButton,R.drawable.ic_speaker_notes_white_48dp);
                views.setInt(R.id.widgetToggleButton, "setBackgroundResource", R.drawable.round_rectangle_green);
                views.setTextViewText(R.id.widgetToggleText, context.getString(R.string.widget_on_text));
                views.setTextColor(R.id.widgetToggleText, ContextCompat.getColor(context, R.color.green));
            }

            views.setOnClickPendingIntent(
                    R.id.widgetToggleButton,
                    getPendingSelfIntent(context, 0, TOGGLE_CLICKED));

            ComponentName componentName = new ComponentName(context, ServiceToggle.class);
            appWidgetManager.updateAppWidget(componentName, views);
        }
    }

    private PendingIntent getPendingSelfIntent(Context context, int appWidgetId, String action) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action);
        return PendingIntent.getBroadcast(context, appWidgetId, intent, 0);
    }
}
