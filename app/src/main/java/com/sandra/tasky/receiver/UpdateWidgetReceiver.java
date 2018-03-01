package com.sandra.tasky.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.PowerManager;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class UpdateWidgetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UPDATE_WIDGET");

        wakeLock.acquire();

        TaskyUtils.updateWidget(context);

//        if (intent.getExtras() != null && intent.getExtras().getString(TaskyConstants.ALARM_EXTRA_TITLE) != null) {
//            Toast.makeText(context, context.getString(R.string.reminder) + " " + intent.getExtras().getString(TaskyConstants.ALARM_EXTRA_TITLE), Toast.LENGTH_LONG).show();
//        }

        //update log settings
        SharedPreferences.Editor editor = context.getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE).edit();
        editor.putString(TaskyConstants.PREFS_LAST_UPDATE, context.getString(R.string.last_update) + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()));
        editor.apply();

        wakeLock.release();

        if (intent.getExtras() != null && intent.getExtras().getBoolean(TaskyConstants.ALARM_EXTRA_REPEATABLE)) {
            //set alarm for next midnight
            TaskyUtils.setAlarm(context, intent.getExtras().getLong(TaskyConstants.ALARM_EXTRA_TIME) + TaskyConstants.INTERVAL_DAY, null, true);
        }

    }
}
