package com.sandra.tasky;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.sandra.tasky.receiver.UpdateWidgetReceiver;

import static android.app.AlarmManager.RTC;
import static android.os.Build.VERSION.SDK_INT;

public class TaskyUtils {

    public static void setAlarm(Context context, long timeInMillis, boolean isRepeatable) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent setAlarmIntent = new Intent(context, UpdateWidgetReceiver.class);
        setAlarmIntent.setAction(TaskyConstants.WIDGET_UPDATE_ACTION);
        setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, isRepeatable);
        PendingIntent setAlarmPI = PendingIntent.getBroadcast(context, TaskyConstants.WIDGET_UPDATE_REQUEST_CODE, setAlarmIntent, 0);

        if (SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.set(RTC, timeInMillis, setAlarmPI);
        } else if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M) {
            alarmManager.setExact(RTC, timeInMillis, setAlarmPI);
        } else if (SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(RTC, timeInMillis, setAlarmPI);
        }
    }
}
