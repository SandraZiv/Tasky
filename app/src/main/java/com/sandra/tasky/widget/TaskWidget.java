package com.sandra.tasky.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.activities.HomeScreenActivity;
import com.sandra.tasky.activities.TaskActivity;
import com.sandra.tasky.utils.TaskyUtils;

public class TaskWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        TaskyUtils.setMidnightUpdater(context);


        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            Intent svcIntent = new Intent(context, TaskWidgetService.class);
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            svcIntent.setData(Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME)));

            RemoteViews widget = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            widget.setRemoteAdapter(R.id.widget_list, svcIntent);

            //open activity for creating new task from widget
            Intent newTaskIntent = new Intent(context, TaskActivity.class);
            PendingIntent newTaskPI = PendingIntent.getActivity(context, 0, newTaskIntent, 0);
            widget.setOnClickPendingIntent(R.id.widget_btn_add_task, newTaskPI);

            //open home screen from widget
            Intent openHomeScreenIntent = new Intent(context, HomeScreenActivity.class);
            PendingIntent openHomeScreenPI = PendingIntent.getActivity(context, 0, openHomeScreenIntent, 0);
            widget.setOnClickPendingIntent(R.id.widget_tasky, openHomeScreenPI);

            //open activity from widget
            Intent clickIntent = new Intent(context, TaskActivity.class);
            PendingIntent clickPI = PendingIntent.getActivity(context, 0, clickIntent, 0);
            widget.setPendingIntentTemplate(R.id.widget_list, clickPI);

            appWidgetManager.updateAppWidget(appWidgetId, widget);
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        updatePreference(context, TaskyConstants.WIDGET_ENABLED);

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
        updatePreference(context, TaskyConstants.WIDGET_DISABLED);
    }

    private void updatePreference(Context context, boolean enabled) {
        SharedPreferences preferences = context.getSharedPreferences(TaskyConstants.WIDGET_PREF, Context.MODE_PRIVATE);
        preferences.edit().putBoolean(TaskyConstants.PREFS_IS_WIDGET_ENABLED, enabled).apply();
    }

}
