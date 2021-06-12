package com.sandra.tasky.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService.RemoteViewsFactory
import com.sandra.tasky.R
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.settings.AppSettings
import com.sandra.tasky.utils.withEmptyTime
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes

class TaskViewFactory(private val context: Context) : RemoteViewsFactory {

    private val db = TaskDatabase(context)
    private var list = getWidgetTasks()

    override fun onCreate() {}
    override fun onDataSetChanged() {
        list = getWidgetTasks()
    }

    override fun onDestroy() {}

    override fun getViewAt(position: Int): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.widget_item)
        val task = list[position]
        row.setTextViewText(R.id.tvWidgetTitle, task.title)
        if (task.dueDate != null) {
            setDueDateVisible(row)
            row.setTextViewText(R.id.tvWidgetDueDate, getDateText(task))
        } else {
            setDueDateGone(row)
        }

        // open task detail in TaskActivity
        val fillIntent = Intent()
        fillIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, task)
        row.setOnClickFillInIntent(R.id.widget_layout_parent, fillIntent)
        return row
    }

    private fun setDueDateVisible(row: RemoteViews) {
        row.setViewVisibility(R.id.tvWidgetDueDate, View.VISIBLE)
        row.setViewVisibility(R.id.widgetSpace, View.GONE)
    }

    private fun setDueDateGone(row: RemoteViews) {
        row.setViewVisibility(R.id.tvWidgetDueDate, View.GONE)
        row.setViewVisibility(R.id.widgetSpace, View.VISIBLE)
    }

    private fun getDateText(task: SimpleTask): String {
        val dataDate = task.dueDate!!.withEmptyTime()
        val currentDate = DateTime().withEmptyTime()
        val diffDays = Days.daysBetween(currentDate, dataDate).days.toLong()
        val date = context.run {
            if (diffDays == 0L) {
                if (!task.isTimePresent) {
                    getString(R.string.today)
                } else {
                    val isExpired = (Hours.hoursBetween(DateTime(), task.dueDate).hours < 0
                            || Minutes.minutesBetween(DateTime(), task.dueDate).minutes < 0)
                    val expiredOrTodayText =
                        getString(if (isExpired) R.string.expired else R.string.today)
                    getString(R.string.at, expiredOrTodayText, task.parseTime())
                }
            } else if (diffDays < 0) {
                getString(R.string.expired)
            } else if (diffDays == 1L) {
                if (task.isTimePresent) {
                    getString(R.string.at, getString(R.string.tomorrow), task.parseTime())
                } else {
                    getString(R.string.tomorrow)
                }
            } else if (diffDays <= 10) {
                getString(R.string.in_x_days, diffDays)
            } else {
                task.parseDate()
            }
        }

        return "${context.getString(R.string.due_date)}: $date"
    }

    private fun getWidgetTasks(): List<SimpleTask> {
        return db.getTasksInWidget(
            AppSettings.shouldWidgetShowExpiredTasks(context),
            AppSettings.getWidgetTimeSpan(context)
        )
    }

    override fun getCount() = list.size

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount() = 1

    override fun getItemId(position: Int) = position.toLong()

    override fun hasStableIds() = true

}