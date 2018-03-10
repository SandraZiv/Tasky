package com.sandra.tasky.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.service.UpdateWidgetService;
import com.sandra.tasky.widget.TaskWidget;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static android.app.AlarmManager.RTC;
import static android.os.Build.VERSION.SDK_INT;

public class TaskyUtils {

    private static long TIME_OFFSET = 60 * 1000;

    public static void updateWidget(Context context) {
        ComponentName taskyWidget = new ComponentName(context, TaskWidget.class);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetManager.getAppWidgetIds(taskyWidget), R.id.widget_list);
    }

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

        long newTimeMillis = System.currentTimeMillis() + TaskyUtils.untilMidnight(context);

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

    public static DateTime moveToNextRepeat(SimpleTask task) {
        return new DateTime(calculateNewTaskTime(task));
    }

    private static long calculateNewTaskTime(SimpleTask task) {
        switch (task.getRepeat()) {
            case TaskyConstants.REPEAT_DAY:
                return task.getDueDate().plusDays(1).getMillis();
            case TaskyConstants.REPEAT_WEEK:
                return task.getDueDate().plusWeeks(1).getMillis();
            case TaskyConstants.REPEAT_MONTH:
                return task.getDueDate().plusMonths(1).getMillis();
            case TaskyConstants.REPEAT_YEAR:
                return task.getDueDate().plusYears(1).getMillis();
            default:
                return task.getDueDate().getMillis();
        }
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
