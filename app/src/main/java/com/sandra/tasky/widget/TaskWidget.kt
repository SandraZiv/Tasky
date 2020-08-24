package com.sandra.tasky.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.sandra.tasky.R
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.activities.SplashScreenActivity
import com.sandra.tasky.activities.TaskActivity
import com.sandra.tasky.utils.AlarmUtils

class TaskWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        AlarmUtils.setMidnightUpdater(context)

        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            val svcIntent = Intent(context, TaskWidgetService::class.java)
            svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            svcIntent.data = Uri.parse(svcIntent.toUri(Intent.URI_INTENT_SCHEME))
            val widget = RemoteViews(context.packageName, R.layout.widget_layout)
            widget.setRemoteAdapter(R.id.widgetList, svcIntent)

            //open activity for creating new task from widget
            val newTaskIntent = Intent(context, TaskActivity::class.java)
            val newTaskPI = PendingIntent.getActivity(context, 0, newTaskIntent, 0)
            widget.setOnClickPendingIntent(R.id.btnWidgetAddTask, newTaskPI)

            //open home screen from widget
            //home screen is opened after splash screen is shown
            val openHomeScreenIntent = Intent(context, SplashScreenActivity::class.java)
            val openHomeScreenPI = PendingIntent.getActivity(context, 0, openHomeScreenIntent, 0)
            widget.setOnClickPendingIntent(R.id.widgetTasky, openHomeScreenPI)

            //open activity from widget
            val clickIntent = Intent(context, TaskActivity::class.java)
            val clickPI = PendingIntent.getActivity(context, 0, clickIntent, 0)
            widget.setPendingIntentTemplate(R.id.widgetList, clickPI)
            appWidgetManager.updateAppWidget(appWidgetId, widget)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
        updatePreference(context, TaskyConstants.WIDGET_ENABLED)
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
        updatePreference(context, TaskyConstants.WIDGET_DISABLED)
    }

    private fun updatePreference(context: Context, enabled: Boolean) {
        val preferences = context.getSharedPreferences(TaskyConstants.WIDGET_PREF, Context.MODE_PRIVATE)
        val msg = context.getString(if (enabled) R.string.scheduler_running else R.string.widget_not_set)
        preferences.edit()
                .putBoolean(TaskyConstants.PREFS_IS_WIDGET_ENABLED, enabled)
                .putString(TaskyConstants.PREFS_LAST_UPDATE, msg)
                .apply()
    }
}