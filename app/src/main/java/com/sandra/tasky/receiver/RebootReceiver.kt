package com.sandra.tasky.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Build.VERSION
import com.sandra.tasky.AppPrefs
import com.sandra.tasky.service.RebootService
import com.sandra.tasky.utils.AlarmUtils

class RebootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("android.intent.action.BOOT_COMPLETED" == intent.action) {
            //wake midnight updater
            if (!AppPrefs.isWidgetEnabled(context)) {
                AlarmUtils.setMidnightUpdater(context)
            }

            //wake widget alarms and notifications
            if (VERSION.SDK_INT < Build.VERSION_CODES.O) {
                context.startService(Intent(context, RebootService::class.java))
            } else {
                context.startForegroundService(Intent(context, RebootService::class.java))
            }
        }
    }
}