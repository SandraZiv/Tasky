package com.sandra.tasky.receiver;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.widget.TaskWidget;


public class UpdateWidgetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UPDATE_WIDGET");

        wakeLock.acquire();

        //add log instead
        Toast.makeText(context, "Tasky reminder", Toast.LENGTH_LONG).show();

        ComponentName taskyWidget = new ComponentName(context, TaskWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(taskyWidget), R.id.widget_list);

        wakeLock.release();
    }
}
