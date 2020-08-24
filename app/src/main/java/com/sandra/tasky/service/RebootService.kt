package com.sandra.tasky.service

import android.app.IntentService
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.sandra.tasky.R
import com.sandra.tasky.db.AppDatabase
import com.sandra.tasky.utils.AlarmUtils
import com.sandra.tasky.utils.NotificationUtils
import com.sandra.tasky.utils.TimeUtils

class RebootService : IntentService("RebootService") {

    override fun onCreate() {
        super.onCreate()
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(NOTIFICATION_ID, getNotification(this))
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            stopForeground(true)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        val tasks = AppDatabase.buildDatabase(this).taskDao().getAll()
        for (task in tasks) {
            //tweak date for repeating
            if (task.dueDate != null && !task.isCompleted) {
                while (!task.isInFuture && task.isRepeating) {
                    task.dueDate = TimeUtils.moveToNextRepeat(task)
                }
            }
            //set alarms and notifications
            if (task.dueDate != null && task.isInFuture) {
                AlarmUtils.initTaskAlarm(this, task)
                NotificationUtils.setNotificationReminder(this, task)
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private fun getNotification(context: Context): Notification {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val mChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.reboot_service),
                NotificationManager.IMPORTANCE_NONE
        )
        manager.createNotificationChannel(mChannel)
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
        builder.setContentTitle(context.getString(R.string.app_name))
                .setContentText(getString(R.string.checking_tasks))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(NotificationUtils.largeIcon(context))
                .setAutoCancel(true)
        return builder.build()
    }

    companion object {
        private const val NOTIFICATION_ID = 13652
        private const val CHANNEL_ID = "com.sandra.tasky.service.CHANNEL_ID"
    }
}