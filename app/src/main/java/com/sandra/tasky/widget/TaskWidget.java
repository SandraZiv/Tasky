package com.sandra.tasky.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;
import com.sandra.tasky.activities.HomeScreenActivity;
import com.sandra.tasky.activities.TaskActivity;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalTime;

public class TaskWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        JodaTimeAndroid.init(context);

       initMidnightUpdater(context);

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
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    private void initMidnightUpdater(Context context) {

        Boolean firstRun = context.getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE)
                .getBoolean(TaskyConstants.PREFS_FIRST_RUN, true);

        if (firstRun) {
            Toast.makeText(context, R.string.widget_init, Toast.LENGTH_SHORT).show();

            LocalTime localTime = LocalTime.now();
            long timeMidnight = 24 * 60 * 60 - localTime.getHourOfDay() * 60 * 60 - localTime.getMinuteOfHour() * 60 - localTime.getSecondOfMinute();
            timeMidnight *= 1000;
            TaskyUtils.setAlarm(context, System.currentTimeMillis() + timeMidnight, null, true);

            SharedPreferences.Editor editor = context.getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE).edit();
            editor.putBoolean(TaskyConstants.PREFS_FIRST_RUN, false);
            editor.apply();
        }
    }
}
