package com.sandra.tasky.service

import android.app.IntentService
import android.content.Context
import android.content.Intent
import com.sandra.tasky.R
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.AlarmUtils
import com.sandra.tasky.utils.TaskyUtils
import java.text.SimpleDateFormat
import java.util.*

class UpdateWidgetService : IntentService("UpdateWidgetService") {
    override fun onHandleIntent(intent: Intent?) {
        TaskyUtils.updateWidget(this)
        createLogEntry()
        if (intent == null || intent.extras == null) {
            return
        }
        val action = intent.action
        if (TaskyConstants.WIDGET_TASK_UPDATE_ACTION == action) {
            //check if alarm is repeating
            if (intent.extras != null && intent.extras!!.getBoolean(TaskyConstants.ALARM_EXTRA_REPEATABLE)) {
                try {
                    val task = TaskyUtils.deserialize(intent.getByteArrayExtra(TaskyConstants.ALARM_EXTRA_TASK)) as SimpleTask
                    if (checkTask(task)) {
                        AlarmUtils.rescheduleTask(this, task)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (TaskyConstants.WIDGET_MIDNIGHT_UPDATE_ACTION == action) {
            //set alarm for next midnight
            AlarmUtils.setMidnightUpdater(this)
        }
    }

    private fun createLogEntry() {
        val preferences = getSharedPreferences(TaskyConstants.WIDGET_PREF, Context.MODE_PRIVATE)
        if (preferences.getBoolean(TaskyConstants.PREFS_IS_WIDGET_ENABLED, TaskyConstants.WIDGET_DEFAULT)) {
            preferences.edit()
                    .putString(TaskyConstants.PREFS_LAST_UPDATE, getString(R.string.last_update) + " " + SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date()))
                    .apply()
        }
    }

    //maybe unnecessary
    private fun checkTask(task: SimpleTask): Boolean {
        val other = TaskDatabase(this).getTaskById(task.id)
        return task.fullTaskEquals(other)
    }
}