package com.sandra.tasky.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.service.RebootService;
import com.sandra.tasky.utils.AlarmUtils;

public class RebootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {

            //wake midnight updater
            final SharedPreferences sharedPreferences = context.getSharedPreferences(TaskyConstants.WIDGET_PREF, Context.MODE_PRIVATE);
            if (!sharedPreferences.getBoolean(TaskyConstants.PREFS_IS_WIDGET_ENABLED, TaskyConstants.WIDGET_DEFAULT)) {
                AlarmUtils.setMidnightUpdater(context);
            }

            //wake widget alarms and notifications
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.startService(new Intent(context, RebootService.class));
            } else {
                context.startForegroundService(new Intent(context, RebootService.class));
            }
        }
    }
}
