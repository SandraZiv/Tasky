package com.sandra.tasky.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;

import com.sandra.tasky.R;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.utils.AlarmUtils;
import com.sandra.tasky.utils.NotificationUtils;
import com.sandra.tasky.utils.TimeUtils;

import java.util.List;

public class RebootService extends IntentService {

    public RebootService() {
        super("RebootService");
    }

    private static final int NOTIFICATION_ID = 13652;
    private static final String CHANNEL_ID = "com.sandra.tasky.service.CHANNEL_ID";

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, getNotification(this));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true);
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        TaskDatabase db = new TaskDatabase(this);
        List<SimpleTask> tasks = db.getAllTasks();

        for (SimpleTask task : tasks) {
            //tweak date for repeating
            if (task.getDueDate() != null && !task.isCompleted()) {
                while (!TimeUtils.isInFuture(task) && task.isRepeating()) {
                    task.setDueDate(TimeUtils.moveToNextRepeat(task));
                }
            }
            //set alarms and notifications
            if (task.getDueDate() != null && TimeUtils.isInFuture(task)) {
                AlarmUtils.initTaskAlarm(this, task);
                NotificationUtils.setNotificationReminder(this, task);
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification(Context context) {
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationChannel mChannel = new NotificationChannel(
                CHANNEL_ID,
                "Reboot Service",
                NotificationManager.IMPORTANCE_NONE
        );
        manager.createNotificationChannel(mChannel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID);

        builder.setContentTitle(context.getString(R.string.app_name))
                .setContentText("Preparing tasks...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(NotificationUtils.largeIcon(context))
                .setAutoCancel(true);

        return builder.build();
    }
}
