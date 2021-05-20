package com.sandra.tasky.entity

import com.applandeo.materialcalendarview.EventDay
import com.sandra.tasky.R
import com.sandra.tasky.RepeatType
import com.sandra.tasky.utils.TimeUtils
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.io.Serializable
import java.util.*

class SimpleTask(
        // The max value for an int is 2,147,483, 647 which is over 2 billions
        var id: Int = EMPTY_ID,
        var title: String = "",
        var note: String = "",
        var dueDate: DateTime? = null, // todo?
        var isCompleted: Boolean = false,
        var isTimePresent: Boolean = false,
        var shouldShowInWidget: Boolean = true,
        var repeat: RepeatType = RepeatType.REPEAT_ONCE,
        // todo this should be just LONG?
        var category: TaskCategory? = null
) : Serializable {

    fun parseDateTime(): String {
        return parseDate() + " " + parseTime()
    }

    fun parseDate(): String {
        return DateTimeFormat.fullDate().print(dueDate)
    }

    fun parseTime(): String {
        return DateTimeFormat.shortTime().print(dueDate)
    }

    val isRepeating: Boolean
        get() = repeat != RepeatType.REPEAT_ONCE

    val isInFuture: Boolean
        get() = dueDate!!.millis > TimeUtils.getCurrentMillis()  // todo check all this duedate!!
    //task is already cleared in setupDateTimeForDB()

    val isExpired: Boolean
        get() = dueDate!!.millis < TimeUtils.getCurrentMillis()

    fun asEventDay(): EventDay {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = dueDate!!.millis
        //TODO repating task
        return EventDay(calendar, if (isCompleted) R.drawable.calendar_event_checked else R.drawable.calendar_event_todo)
    }

    fun fullTaskEquals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val task = o as SimpleTask
        if (id != task.id) return false
        if (isCompleted != task.isCompleted) return false
        if (isTimePresent != task.isTimePresent) return false
        if (shouldShowInWidget != task.shouldShowInWidget) return false
        if (repeat != task.repeat) return false
        if (title != task.title) return false
        if (note != task.note) return false
        if (if (dueDate != null) dueDate != task.dueDate else task.dueDate != null) return false
        return if (category != null) category == task.category else task.category == null
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || javaClass != other.javaClass) return false
        val task = other as SimpleTask
        return id == task.id
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return title + " " + if (dueDate != null) parseDateTime() else " "
    }

    companion object {
        const val EMPTY_ID = -1
    }
}