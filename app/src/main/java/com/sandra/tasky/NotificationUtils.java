package com.sandra.tasky;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.sandra.tasky.activities.TaskActivity;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.service.NotificationService;

import java.io.IOException;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.os.Build.VERSION.SDK_INT;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;

public class NotificationUtils {


    private static final int TASK_REMINDER_NOTIFICATION_ID = 1000;

    private static final String TASK_REMINDER_CHANNEL_ID = "TASK_REMINDER_CHANNEL_ID";

    public static void clearAllNotifications(Context context) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancelAll();
    }

    public static void cancelNotification(Context context, SimpleTask task) {
        ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(TASK_REMINDER_NOTIFICATION_ID + task.getId());

    }

    public static void taskReminder(Context context, SimpleTask task) {
        NotificationManager manager = ((NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE));

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle(task.getTitle())
                .setContentText(task.isTimePresent()? task.parseDateTime() : task.parseDate())
                .setContentIntent(openTaskActivity(context, task))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon(context))
                .setAutoCancel(true);

        //deprecated in API 26
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND);

        builder.setPriority(PRIORITY_HIGH);

        //If there is notification with the same id and it has not yet been canceled
        //it will be replaced by the updated information.
        manager.notify(TASK_REMINDER_NOTIFICATION_ID + task.getId(), builder.build());
    }

    public static void setNotificationReminder(Context context, SimpleTask task) {
//        Intent setAlarmIntent = new Intent(context, NotificationReceiver.class);
        Intent setAlarmIntent = new Intent(context, NotificationService.class);
        setAlarmIntent.setAction(TaskyConstants.NOTIFICATION_ACTION);

        try {
            setAlarmIntent.putExtra(TaskyConstants.NOTIFICATION_TASK_BUNDLE_KEY, TaskyUtils.serialize(task));
        } catch (IOException e) {
            e.printStackTrace();
        }

        PendingIntent pi = PendingIntent.getService(
//        PendingIntent pi = PendingIntent.getBroadcast(
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
        openTaskActivityIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, task);

        return PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                openTaskActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static Bitmap largeIcon(Context context) {
        Resources res = context.getResources();
        return BitmapFactory.decodeResource(res, R.mipmap.ic_launcher);
    }

}
