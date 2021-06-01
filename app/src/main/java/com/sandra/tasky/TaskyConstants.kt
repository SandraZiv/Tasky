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

    const val ALARM_EXTRA_TASK = "com.sandra.tasky.ALARM_EXTRA_TASK"
    const val ALARM_EXTRA_REPEATABLE = "ALARM_EXTRA_REPEATABLE"
    const val ALARM_EXTRA_TIME = "ALARM_EXTRA_TIME"

    const val TASK_BUNDLE_KEY = "com.sandra.tasky.task"

}