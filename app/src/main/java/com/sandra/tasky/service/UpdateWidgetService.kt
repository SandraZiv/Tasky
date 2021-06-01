package com.sandra.tasky.service

import android.app.IntentService
import android.content.Intent
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.AlarmUtils
import com.sandra.tasky.utils.TaskyUtils

class UpdateWidgetService : IntentService("UpdateWidgetService") {

    override fun onHandleIntent(intent: Intent?) {
        TaskyUtils.updateWidget(this)
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


    // todo maybe unnecessary?
    private fun checkTask(task: SimpleTask): Boolean {
        val other = TaskDatabase(this).getTaskById(task.id)
        return task.fullTaskEquals(other)
    }
}