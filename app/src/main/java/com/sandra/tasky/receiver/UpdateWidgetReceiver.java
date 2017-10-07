package com.sandra.tasky.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;


public class UpdateWidgetReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "UPDATE_WIDGET");

        wakeLock.acquire();

        //add log instead
        String message = (intent.getExtras() != null && intent.getExtras().getString(TaskyConstants.ALARM_EXTRA_TITLE) != null) ?
                intent.getExtras().getString(TaskyConstants.ALARM_EXTRA_TITLE) : context.getString(R.string.app_name);
        Toast.makeText(context, message, Toast.LENGTH_LONG).show();

        TaskyUtils.updateWidget(context);

        wakeLock.release();

        if (intent.getExtras() != null && intent.getExtras().getBoolean(TaskyConstants.ALARM_EXTRA_REPEATABLE)) {
            //set alarm for next midnight
            TaskyUtils.setAlarm(context, System.currentTimeMillis() + TaskyConstants.INTERVAL_DAY, null, true);
        }

    }
}
