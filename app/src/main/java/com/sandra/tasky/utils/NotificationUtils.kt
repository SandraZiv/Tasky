package com.sandra.tasky.utils

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Build.VERSION
import androidx.core.app.NotificationCompat
import com.sandra.tasky.R
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.activities.TaskActivity
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.service.NotificationService
import com.sandra.tasky.settings.AppSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.IOException

object NotificationUtils {

    private const val TASK_REMINDER_NOTIFICATION_ID = 1000
    private const val TASK_REMINDER_CHANNEL_ID = "TASK_REMINDER_CHANNEL_ID"

    fun cancelAllNotifications(context: Context?) {
        (context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancelAll()
    }

    fun cancelNotification(context: Context, task: SimpleTask) {
        cancelNotification(context, task.id)
    }

    fun cancelNotification(context: Context, id: Int) {
        (context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .cancel(TASK_REMINDER_NOTIFICATION_ID + id)
    }

    fun showTaskReminder(context: Context, task: SimpleTask) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mChannel = NotificationChannel(
                    TASK_REMINDER_CHANNEL_ID,
                    context.getString(R.string.notifications),
                    NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(mChannel)
        }
        val builder = NotificationCompat.Builder(context, TASK_REMINDER_CHANNEL_ID)
        builder.setContentTitle(task.title)
                .setContentText(if (task.isTimePresent) task.parseDateTime() else task.parseDate())
                .setContentIntent(openTaskActivity(context, task))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setLargeIcon(largeIcon(context))
                .setAutoCancel(true)
        setNotificationDefaults(context, builder)
        if (VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
                && VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.priority = NotificationCompat.PRIORITY_HIGH
        }

        //If there is notification with the same id and it has not yet been canceled
        //it will be replaced by the updated information.
        manager.notify(TASK_REMINDER_NOTIFICATION_ID + task.id, builder.build())
    }

    //vibrate and sound
    private fun setNotificationDefaults(context: Context, builder: NotificationCompat.Builder) {
        val vibrate = AppSettings.shouldNotificationVibrate(context)
        val sound = AppSettings.shouldNotificationHaveSound(context)
        val defaults: Int = if (vibrate && sound) {
            Notification.DEFAULT_VIBRATE or Notification.DEFAULT_SOUND
        } else if (vibrate) {
            Notification.DEFAULT_VIBRATE
        } else if (sound) {
            Notification.DEFAULT_SOUND
        } else {
            return
        }

        //deprecated in API 26
        builder.setDefaults(defaults)
    }

    fun setNotificationReminder(context: Context, task: SimpleTask) {
        val setAlarmIntent = Intent(context, NotificationService::class.java)
        setAlarmIntent.action = TaskyConstants.NOTIFICATION_ACTION
        try {
            setAlarmIntent.putExtra(TaskyConstants.NOTIFICATION_TASK_BUNDLE_KEY, TaskyUtils.serialize(task))
        } catch (e: IOException) {
            e.printStackTrace()
        }
        val pi = PendingIntent.getService(
                context,
                TaskyConstants.NOTIFICATION_PI_REQUEST_CODE(task),
                setAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val manager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            manager[AlarmManager.RTC_WAKEUP, task.dueDate!!.millis] = pi // todo check if due date exists?
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && VERSION.SDK_INT < Build.VERSION_CODES.M) {
            manager.setExact(AlarmManager.RTC_WAKEUP, task.dueDate!!.millis, pi)
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            manager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, task.dueDate!!.millis, pi)
        }
    }

    private fun openTaskActivity(context: Context, task: SimpleTask?): PendingIntent {
        val openTaskActivityIntent = Intent(context, TaskActivity::class.java)
        if (task!!.isRepeating) {
            //load new task since time has changed
            CoroutineScope(Dispatchers.IO).launch {
                val database = TaskDatabase(context)
                var newTask: SimpleTask
                while (true) {
                    newTask = database.getTaskById(task.id)
                    if (task.dueDate != newTask.dueDate) {
                        break  // todo why???
                    }
                }
                openTaskActivityIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, newTask)
                // todo return stuff
            }
        } else {
            openTaskActivityIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, task)
        }
        return PendingIntent.getActivity(
                context,
                System.currentTimeMillis().toInt(),
                openTaskActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
    }

    fun largeIcon(context: Context): Bitmap {
        val res = context.resources
        return BitmapFactory.decodeResource(res, R.mipmap.ic_launcher)
    }

}