package com.sandra.tasky

import com.sandra.tasky.entity.SimpleTask

object TaskyConstants {

    const val NOTIFICATION_ACTION = "com.sandra.tasky.notification.ACTION"
    const val NOTIFICATION_TASK_BUNDLE_KEY = "com.sandra.tasky.notification.TASK_BUNDLE_KEY"
    fun NOTIFICATION_PI_REQUEST_CODE(task: SimpleTask): Int {
        return TASK_BUNDLE_KEY.hashCode() + task.id
    }

    const val WIDGET_TASK_UPDATE_ACTION = "com.sandra.tasky.widget.WIDGET_TASK_UPDATE_ACTION"
    fun WIDGET_PI_REQUEST_CODE(task: SimpleTask): Int {
        return ALARM_EXTRA_TASK.hashCode() + task.id
    }

    const val WIDGET_MIDNIGHT_UPDATE_ACTION = "com.sandra.tasky.widget.WIDGET_MIDNIGHT_UPDATE_ACTION"
    const val MIDNIGHT_UPDATER_PI_REQUEST_CODE = 18310
    const val WIDGET_PREF = "first_run_of_widget_1.19"
    const val PREFS_LAST_UPDATE = "prefs_last_update"
    const val PREFS_IS_WIDGET_ENABLED = "PREFS_IS_WIDGET_ENABLED"
    const val WIDGET_ENABLED = true
    const val WIDGET_DISABLED = false
    const val WIDGET_DEFAULT = WIDGET_DISABLED
    const val ALARM_EXTRA_TASK = "com.sandra.tasky.ALARM_EXTRA_TASK"
    const val ALARM_EXTRA_REPEATABLE = "ALARM_EXTRA_REPEATABLE"
    const val ALARM_EXTRA_TIME = "ALARM_EXTRA_TIME"

    const val TASK_BUNDLE_KEY = "com.sandra.tasky.task"
    const val MAX_TITLE_LENGTH = 70
    const val MAX_TEXT_LENGTH = 100

    const val PREF_GENERAL = "PREF_GENERAL"
    const val PREF_SORT = "PREF_SORT"

}