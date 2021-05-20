package com.sandra.tasky.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import androidx.preference.PreferenceManager
import com.sandra.tasky.R
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.db.DatabaseWrapper
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.service.UpdateWidgetService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.joda.time.DateTime
import java.io.IOException

object AlarmUtils {
    private const val TIME_OFFSET = 60 * 1000.toLong()
    fun initTaskAlarm(context: Context?, task: SimpleTask) {
        try {
            val setAlarmIntent = Intent(context, UpdateWidgetService::class.java)
            setAlarmIntent.action = TaskyConstants.WIDGET_TASK_UPDATE_ACTION
            val isRepeating = task.isRepeating
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, isRepeating)
            val taskTimeMillis: Long = if (isRepeating) {
                setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TASK, TaskyUtils.serialize(task))
                task.dueDate!!.millis
            } else {
                //offset is set so it can write expired
                //TODO refactor
                task.dueDate!!.millis + TIME_OFFSET
            }
            setAlarm(context, setAlarmIntent, taskTimeMillis, TaskyConstants.WIDGET_PI_REQUEST_CODE(task))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun rescheduleTask(context: Context, task: SimpleTask) {
        try {
            //set alarm
            val setAlarmIntent = Intent(context, UpdateWidgetService::class.java)
            setAlarmIntent.action = TaskyConstants.WIDGET_TASK_UPDATE_ACTION
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_REPEATABLE, true)
            val newTimeMillis = TimeUtils.calculateNewTaskTime(task)
            task.dueDate = DateTime(newTimeMillis)
            setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TASK, TaskyUtils.serialize(task))
            setAlarm(context, setAlarmIntent, newTimeMillis, TaskyConstants.WIDGET_PI_REQUEST_CODE(task))

            //update task and set notification (if it is enabled)
            if (PreferenceManager.getDefaultSharedPreferences(context).getBoolean(
                            context.getString(R.string.pref_show_notifications_key),
                            context.resources.getBoolean(R.bool.pref_show_notifications_default))) {
                NotificationUtils.setNotificationReminder(context, task)
            }

            CoroutineScope(Dispatchers.Main).launch {
                DatabaseWrapper.updateTask(context, task)
                TaskyUtils.updateWidget(context)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setMidnightUpdater(context: Context?) {
        val setAlarmIntent = Intent(context, UpdateWidgetService::class.java)
        setAlarmIntent.action = TaskyConstants.WIDGET_MIDNIGHT_UPDATE_ACTION
        val newTimeMillis = System.currentTimeMillis() + TimeUtils.untilMidnight(context)
        setAlarm(context, setAlarmIntent, newTimeMillis, TaskyConstants.MIDNIGHT_UPDATER_PI_REQUEST_CODE)
    }

    private fun setAlarm(context: Context?, setAlarmIntent: Intent, timeInMillis: Long, pendingIntentId: Int) {
        setAlarmIntent.action = TaskyConstants.WIDGET_TASK_UPDATE_ACTION
        setAlarmIntent.putExtra(TaskyConstants.ALARM_EXTRA_TIME, timeInMillis)
        val setAlarmPI = PendingIntent.getService(
                context,
                pendingIntentId,
                setAlarmIntent,
                PendingIntent.FLAG_UPDATE_CURRENT)
        val alarmManager = context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        if (VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            alarmManager[AlarmManager.RTC, timeInMillis] = setAlarmPI
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && VERSION.SDK_INT < Build.VERSION_CODES.M) {
            alarmManager.setExact(AlarmManager.RTC, timeInMillis, setAlarmPI)
        } else if (VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, timeInMillis, setAlarmPI)
        }
    }
}