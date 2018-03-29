package com.sandra.tasky.utils;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.activities.TaskActivity;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.service.NotificationService;

import java.io.IOException;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.os.Build.VERSION.SDK_INT;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;

public class NotificationUtils {


    private static final int TASK_REMINDER_NOTIFICATION_ID = 1000;

    private static final String TASK_REMINDER_CHANNEL_ID = "TASK_REMINDER_CHANNEL_ID";

    public static void cancelAllNotifications(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancelAll();
    }

    public static void cancelNotification(Context context, SimpleTask task) {
        cancelNotification(context, task.getId());
    }

    public static void cancelNotification(Context context, int id) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(TASK_REMINDER_NOTIFICATION_ID + id);
    }

    public static void showTaskReminder(Context context, SimpleTask task) {
        NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel mChannel = new NotificationChannel(
                    TASK_REMINDER_CHANNEL_ID,
                    context.getString(R.string.notifications),
                    NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(mChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID);

        builder.setContentTitle(task.getTitle())
                .setContentText(task.isTimePresent() ? task.parseDateTime() : task.parseDate())
                .setContentIntent(openTaskActivity(context, task))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon(context))
                .setAutoCancel(true);

        setNotificationDefaults(context, builder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setPriority(PRIORITY_HIGH);
        }

        //If there is notification with the same id and it has not yet been canceled
        //it will be replaced by the updated information.
        manager.notify(TASK_REMINDER_NOTIFICATION_ID + task.getId(), builder.build());
    }

    //vibrate and sound
    private static void setNotificationDefaults(Context context, NotificationCompat.Builder builder) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        boolean vibrate = preferences.getBoolean(context.getString(R.string.pref_vibrate_key),
                context.getResources().getBoolean(R.bool.pref_vibrate_default));
        boolean sound = preferences.getBoolean(context.getString(R.string.pref_sound_key),
                context.getResources().getBoolean(R.bool.pref_sound_default));

        int defaults;
        if (vibrate && sound) {
            defaults = Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND;
        } else if (vibrate) {
            defaults = Notification.DEFAULT_VIBRATE;
        } else if (sound) {
            defaults = Notification.DEFAULT_SOUND;
        } else {
            return;
        }

        //deprecated in API 26
        builder.setDefaults(defaults);

    }

    public static void setNotificationReminder(Context context, SimpleTask task) {
        Intent setAlarmIntent = new Intent(context, NotificationService.class);
        setAlarmIntent.setAction(TaskyConstants.NOTIFICATION_ACTION);

        try {
            setAlarmIntent.putExtra(TaskyConstants.NOTIFICATION_TASK_BUNDLE_KEY, TaskyUtils.serialize(task));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PendingIntent pi = PendingIntent.getService(
                context,
                TaskyConstants.NOTIFICATION_PI_REQUEST_CODE(task),
                setAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager manager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        if (SDK_INT < Build.VERSION_CODES.KITKAT) {
            manager.set(RTC_WAKEUP, task.getDueDate().getMillis(), pi);
        } else if (SDK_INT >= Build.VERSION_CODES.KITKAT && SDK_INT < Build.VERSION_CODES.M) {
            manager.setExact(RTC_WAKEUP, task.getDueDate().getMillis(), pi);
        } else if (SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(RTC_WAKEUP, task.getDueDate().getMillis(), pi);
        }
    }

    private static PendingIntent openTaskActivity(Context context, SimpleTask task) {
        Intent openTaskActivityIntent = new Intent(context, TaskActivity.class);

        if (task.isRepeating()) {
            //load new task since time has changed
            openTaskActivityIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, getRescheduledTask(context, task));
        } else {
            openTaskActivityIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, task);
        }


        return PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                openTaskActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
    }

    private static SimpleTask getRescheduledTask(Context context, SimpleTask old) {
        TaskDatabase database = new TaskDatabase(context);

        SimpleTask newTask;
        while (true) {
            newTask = database.getTaskById(old.getId());
            if (!old.getDueDate().equals(newTask.getDueDate())) {
                break;
            }
        }
        return newTask;
    }

}
