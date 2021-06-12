package com.sandra.tasky.settings

import android.content.Context
import androidx.annotation.BoolRes
import androidx.preference.PreferenceManager
import com.sandra.tasky.R

object AppSettings {

    fun shouldShowNotifications(context: Context) = getBooleanPrefWithDefaultValue(
        context, PREF_SHOULD_SHOW_NOTIFICATION_KEY, R.bool.pref_show_notifications_default
    )

    fun shouldNotificationVibrate(context: Context) = getBooleanPrefWithDefaultValue(
        context, PREF_SHOULD_NOTIFICATION_VIBRATE, R.bool.pref_vibrate_default
    )

    fun shouldNotificationHaveSound(context: Context) = getBooleanPrefWithDefaultValue(
        context, PREF_SHOULD_NOTIFICATION_HAVE_SOUND, R.bool.pref_sound_default
    )

    fun shouldWidgetShowExpiredTasks(context: Context) = getBooleanPrefWithDefaultValue(
        context, PREF_SHOULD_WIDGET_SHOW_EXPIRED_TASK, R.bool.pref_show_expired_default
    )

    fun getWidgetTimeSpan(context: Context) = getPreferences(context)
        .getString(PREF_WIDGET_TIME_SPAN, PREF_WIDGET_TIME_SPAN_DEFAULT)


    private fun getBooleanPrefWithDefaultValue(
        context: Context,
        key: String,
        @BoolRes defaultValueId: Int
    ): Boolean {
        val defaultValue = context.resources.getBoolean(defaultValueId)
        return getPreferences(context).getBoolean(key, defaultValue)
    }

    private fun getPreferences(context: Context) =
        PreferenceManager.getDefaultSharedPreferences(context)

    const val PREF_SHOULD_SHOW_NOTIFICATION_KEY = "pref_show_notifications"
    const val PREF_SHOULD_NOTIFICATION_VIBRATE = "pref_vibrate"
    const val PREF_SHOULD_NOTIFICATION_HAVE_SOUND = "pref_sound"
    const val PREF_SHOULD_WIDGET_SHOW_EXPIRED_TASK = "pref_show_expired"
    const val PREF_WIDGET_TIME_SPAN = "pref_time_span"
    const val PREF_WIDGET_TIME_SPAN_DEFAULT = "-1"

    const val PREF_APP_ON_GOOGLE_PLAY = "pref_view_in_google_play"
    const val PREF_APP_VERSION = "pref_version"
}