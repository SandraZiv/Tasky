package com.sandra.tasky.activities.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.applandeo.materialcalendarview.EventDay
import com.applandeo.materialcalendarview.listeners.OnDayClickListener
import com.sandra.tasky.R
import com.sandra.tasky.adapter.CalendarEventAdapter
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.TimeUtils
import com.sandra.tasky.utils.capitalFirstLetter
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import java.util.*
import kotlin.collections.ArrayList

class CalendarFragment : Fragment(), OnDayClickListener {

    // todo
    val tasks = emptyList<SimpleTask>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_calendar, container, true)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // todo load tasks

        view.calendarView.setOnDayClickListener(this)
        view.calendarView.setDate(Calendar.getInstance())

        // completed tasks go first, so that if there were more tasks
        // on same day, uncompleted icon will be shown
        val events: MutableList<EventDay?> = ArrayList()
        for (task in tasks) {
            if (task.dueDate != null) {
                events.add(task.asEventDay())
            }
        }
        view.calendarView.setEvents(events)

    }

    override fun onDayClick(eventDay: EventDay) {
        val context = requireContext()

        val builder = AlertDialog.Builder(context)
        val day = DateTime(eventDay.calendar.timeInMillis)
        val dayFormatted = DateTimeFormat.fullDate().print(day)
        builder.setTitle(dayFormatted.capitalFirstLetter())
        val selectedDayTasks: MutableList<SimpleTask> = java.util.ArrayList()
        for (task in tasks) {
            if (task.dueDate != null && TimeUtils.dateEqual(day, task.dueDate)) {
                selectedDayTasks.add(task)
            }
        }
        if (selectedDayTasks.isNotEmpty()) {
            val calendarEventAdapter = CalendarEventAdapter(context, selectedDayTasks)
            builder.setAdapter(calendarEventAdapter) { dialog, which ->
                // todo
//                openTaskActivity(selectedDayTasks[which])
                dialog.dismiss()
            }
        } else {
            builder.setMessage(R.string.no_tasks_here)
        }
//        builder.setPositiveButton(R.string.add_task) { _, _ -> createNewTask(day) }
        builder.setPositiveButton(R.string.add_task) { _, _ -> }
        builder.setNegativeButton(R.string.cancel) { dialog, _ -> dialog.cancel() }
        builder.show()
    }
}