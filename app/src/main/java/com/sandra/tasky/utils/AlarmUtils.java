package com.sandra.tasky.utils;


import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.service.UpdateWidgetService;

import org.joda.time.DateTime;

import java.io.IOException;

import static android.app.AlarmManager.RTC;
import static android.os.Build.VERSION.SDK_INT;
import static com.sandra.tasky.utils.TaskyUtils.serialize;
import static com.sandra.tasky.utils.TaskyUtils.updateWidget;
import static com.sandra.tasky.utils.TimeUtils.calculateNewTaskTime;
import static com.sandra.tasky.utils.TimeUtils.untilMidnight;

public class AlarmUtils {

    private static long TIME_OFFSET = 60 * 1000;

    public static void initTaskAlarm(Context context, SimpleTask task) {
        try {
            Intent setAlarmIntent = new Intent(context, UpdateWidgetService.class);
            setAlarmIntent.setAction(TaskyConstants.WIDGET_TASK_UPDATE_ACTION);

            boolean isRepeating = task.isRepeating();

            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, isRepeating);

            long taskTimeMillis;

            if (isRepeating) {
                setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TASK, serialize(task));
                taskTimeMillis = task.getDueDate().getMillis();
            } else {
                //offset is set so it can write expired
                //TODO refactor
                taskTimeMillis = task.getDueDate().getMillis() + TIME_OFFSET;
            }

            setAlarm(context, setAlarmIntent, taskTimeMillis, TaskyConstants.WIDGET_PI_REQUEST_CODE(task));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rescheduleTask(Context context, SimpleTask task) {
        try {
            //set alarm
            Intent setAlarmIntent = new Intent(context, UpdateWidgetService.class);
            setAlarmIntent.setAction(TaskyConstants.WIDGET_TASK_UPDATE_ACTION);
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, true);

            long newTimeMillis = calculateNewTaskTime(task);
            task.setDueDate(new DateTime(newTimeMillis));
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TASK, serialize(task));

            setAlarm(context, setAlarmIntent, newTimeMillis, TaskyConstants.WIDGET_PI_REQUEST_CODE(task));

            //update task and set notification (if it is enabled)
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                    context.getString(R.string.pref_show_notifications_key),
                    context.getResources().getBoolean(R.bool.pref_show_notifications_default))) {

                NotificationUtils.setNotificationReminder(context, task);
            }

            //update widget is done in postExecute()
            new rescheduleTaskAsync().execute(context, task);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMidnightUpdater(Context context) {
        Intent setAlarmIntent = new Intent(context, UpdateWidgetService.class);
        setAlarmIntent.setAction(TaskyConstants.WIDGET_MIDNIGHT_UPDATE_ACTION);

        long newTimeMillis = System.currentTimeMillis() + untilMidnight(context);

        setAlarm(context, setAlarmIntent, newTimeMillis, TaskyConstants.MIDNIGHT_UPDATER_PI_REQUEST_CODE);
    }

    private static void setAlarm(Context context, Intent setAlarmIntent, long timeInMillis, int pendingIntentId) {

        setAlarmIntent.setAction(TaskyConstants.WIDGET_TASK_UPDATE_ACTION);

        setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TIME, timeInMillis);

        PendingIntent setAlarmPI = PendingIntent.getService(
                context,
                pendingIntentId,
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


    private static class rescheduleTaskAsync extends AsyncTask<Object, Integer, Integer> {
        Context context;

        @Override
        protected Integer doInBackground(Object... params) {
            context = (Context) params[0];
            SimpleTask task = (SimpleTask) params[1];
            TaskDatabase database = new TaskDatabase(context);
            return database.updateTask(task);
        }

        @Override
        protected void onPostExecute(Integer integer) {
            updateWidget(context);
        }
    }
}
