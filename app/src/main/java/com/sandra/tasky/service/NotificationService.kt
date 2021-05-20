package com.sandra.tasky.service

import android.app.IntentService
import android.content.Intent
import androidx.preference.PreferenceManager
import com.sandra.tasky.R
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.NotificationUtils
import com.sandra.tasky.utils.TaskyUtils
import net.danlew.android.joda.JodaTimeAndroid

class NotificationService : IntentService("NotificationService") {
    override fun onHandleIntent(intent: Intent?) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(this)
        val prefShowNotifications = preferences.getBoolean(getString(R.string.pref_show_notifications_key), resources.getBoolean(R.bool.pref_show_notifications_default))
        if (prefShowNotifications && intent != null && TaskyConstants.NOTIFICATION_ACTION == intent.action) {
            try {
                //TODO two tasks at the same time?
                JodaTimeAndroid.init(this)
                val task = TaskyUtils.deserialize(intent.getByteArrayExtra(TaskyConstants.NOTIFICATION_TASK_BUNDLE_KEY)) as SimpleTask
                if (checkTask(task) && !task.isCompleted) {
                    NotificationUtils.showTaskReminder(this, task)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun checkTask(task: SimpleTask): Boolean {
        val other = TaskDatabase(this@NotificationService).getTaskById(task.id)
        return task.dueDate == other.dueDate
    }
}