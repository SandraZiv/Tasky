package com.sandra.tasky.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.service.UpdateWidgetService;
import com.sandra.tasky.widget.TaskWidget;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.LocalTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static android.app.AlarmManager.RTC;
import static android.os.Build.VERSION.SDK_INT;

public class TaskyUtils {

    public static void setAlarm(Context context, long timeInMillis, String taskTitle, boolean isRepeatable) {
        Intent setAlarmIntent = new Intent(context, UpdateWidgetService.class);
        setAlarmIntent.setAction(TaskyConstants.WIDGET_UPDATE_ACTION);
        setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TITLE, taskTitle);
        setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, isRepeatable);
        setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TIME, timeInMillis);

        //TODO id?
        PendingIntent setAlarmPI = PendingIntent.getService(
                context,
                (int) System.currentTimeMillis(),
                setAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager.set(RTC, timeInMillis, setAlarmPI);
        } else if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M) {
            alarmManager.setExact(RTC, timeInMillis, setAlarmPI);
        } else if (SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(RTC, timeInMillis, setAlarmPI);
        }
    }

    public static void setMidnightUpdater(Context context) {
        setAlarm(context, System.currentTimeMillis() + TaskyUtils.untilMidnight(context), null, true);
    }

    public static void updateWidget(Context context) {
        ComponentName taskyWidget = new ComponentName(context, TaskWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(taskyWidget), R.id.widget_list);
    }

    public static Toast addToast(Toast toast, Context context, String msg, boolean isShort) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(context, msg, isShort ? Toast.LENGTH_SHORT : Toast.LENGTH_LONG);
        toast.show();

        return toast;
    }

    public static Toast addToast(Toast toast, Context context, int id, boolean isShort) {
        String msg = context.getString(id);
        return addToast(toast, context, msg, isShort);
    }


    public static byte[] serialize(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    private static long untilMidnight(Context context) {
        JodaTimeAndroid.init(context);

        LocalTime localTime = LocalTime.now();
        long timeMidnight = 24 * 60 * 60
                - localTime.getHourOfDay() * 60 * 60
                - localTime.getMinuteOfHour() * 60
                - localTime.getSecondOfMinute();
        timeMidnight *= 1000;

        return timeMidnight;
    }
}
