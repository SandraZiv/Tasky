package com.sandra.tasky.utils

import android.content.Context
import com.sandra.tasky.RepeatType
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.entity.SimpleTask
import net.danlew.android.joda.JodaTimeAndroid
import org.joda.time.DateTime
import org.joda.time.LocalTime

object TimeUtils {

    fun getCurrentMillis() = DateTime.now().withSecondOfMinute(0).withMillisOfSecond(0).millis

    fun untilMidnight(context: Context?): Long {
        JodaTimeAndroid.init(context)
        val localTime = LocalTime.now()
        var timeMidnight = 24 * 60 * 60 - localTime.hourOfDay * 60 * 60 - localTime.minuteOfHour * 60 - localTime.secondOfMinute.toLong()
        timeMidnight *= 1000
        return timeMidnight
    }

    fun moveToNextRepeat(task: SimpleTask): DateTime {
        return DateTime(calculateNewTaskTime(task))
    }

    fun calculateNewTaskTime(task: SimpleTask): Long {
        return when (task.repeat) {
            RepeatType.REPEAT_DAY -> task.dueDate!!.plusDays(1).millis
            RepeatType.REPEAT_WEEK -> task.dueDate!!.plusWeeks(1).millis
            RepeatType.REPEAT_MONTH -> task.dueDate!!.plusMonths(1).millis
            RepeatType.REPEAT_YEAR -> task.dueDate!!.plusYears(1).millis
            else -> task.dueDate!!.millis
        }
    }

    //validate only date not time
    fun dateEqual(first: DateTime, second: DateTime?): Boolean {
        return first.year == second!!.year && first.monthOfYear == second.monthOfYear && first.dayOfMonth == second.dayOfMonth
    }
}