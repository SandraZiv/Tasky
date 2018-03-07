package com.sandra.tasky.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
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

import static android.app.AlarmManager.INTERVAL_FIFTEEN_MINUTES;
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
        Log.d("TIM", "init");
        try {
            Intent setAlarmIntent = new Intent(context, UpdateWidgetService.class);
            setAlarmIntent.setAction(TaskyConstants.WIDGET_TASK_UPDATE_ACTION);
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, true);
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TASK, serialize(task));
            //TODO ad if for repeat and then set offset
            setAlarm(context, setAlarmIntent, task.getDueDate().getMillis() + TIME_OFFSET);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void rescheduleTask(Context context, SimpleTask task) {
        Log.d("TIM", "reschedule");
        try {
            //set alarm
            Intent setAlarmIntent = new Intent(context, UpdateWidgetService.class);
            setAlarmIntent.setAction(TaskyConstants.WIDGET_TASK_UPDATE_ACTION);
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, true);

            long newTimeMillis = task.getDueDate().getMillis() + INTERVAL_FIFTEEN_MINUTES;
            task.setDueDate(new DateTime(newTimeMillis));
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TASK, serialize(task));

            setAlarm(context, setAlarmIntent, newTimeMillis + TIME_OFFSET);

            //update task and set notification
            //update widget is done in postExecute()
            NotificationUtils.setNotificationReminder(context, task);
            new rescheduleTaskAsync().execute(context, task);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void setMidnightUpdater(Context context) {
        Intent setAlarmIntent = new Intent(context, UpdateWidgetService.class);
        setAlarmIntent.setAction(TaskyConstants.WIDGET_MIDNIGHT_UPDATE_ACTION);

        long newTimeMillis = System.currentTimeMillis() + TaskyUtils.untilMidnight(context);

        setAlarm(context, setAlarmIntent, newTimeMillis);
    }

    private static void setAlarm(Context context, Intent setAlarmIntent, long timeInMillis) {

        setAlarmIntent.setAction(TaskyConstants.WIDGET_TASK_UPDATE_ACTION);

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
            Log.d("TIM", "updateWidget");
            updateWidget(context);
        }
    }

}
