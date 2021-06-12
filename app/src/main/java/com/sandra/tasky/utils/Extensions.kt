package com.sandra.tasky.utils

import android.content.Context
import android.content.Intent
import android.view.View
import org.joda.time.DateTime
import java.util.*

fun Context.startActivity(clazz: Class<*>) = startActivity(Intent(this, clazz))

fun View.show() {
    visibility = View.VISIBLE
}

fun View.hide() {
    visibility = View.GONE
}

fun View.showIf(condition: Boolean) {
    if (condition) show() else hide()
}

fun String.capitalFirstLetter() =
    replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

fun DateTime.withEmptyTime(): DateTime = this.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)