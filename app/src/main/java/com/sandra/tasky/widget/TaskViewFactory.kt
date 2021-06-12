package com.sandra.tasky.widget

import android.appwidget.AppWidgetManager
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
import org.joda.time.DateTime
import org.joda.time.Days
import org.joda.time.Hours
import org.joda.time.Minutes

class TaskViewFactory(private val context: Context, intent: Intent) : RemoteViewsFactory {

    private var list: List<SimpleTask?> = getWidgetTasks()
    val db = TaskDatabase(context)
    // todo do i need this
    private val appWidgetId: Int = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)

    override fun onCreate() {}
    override fun onDataSetChanged() {
        list = getWidgetTasks()
    }

    override fun onDestroy() {}
    override fun getCount(): Int {
        return list.size
    }

    override fun getViewAt(position: Int): RemoteViews {
        val row = RemoteViews(context.packageName, R.layout.widget_item)
        val task = list[position]!!
        row.setTextViewText(R.id.tvWidgetTitle, task.title)
        if (task.dueDate != null) {
            setDueDateVisible(row)
            row.setTextViewText(R.id.tvWidgetDueDate, getDateText(task))
        } else {
            setDueDateGone(row)
        }

        //open task detail in TaskActivity
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

    private fun getDateText(task: SimpleTask): String {  // todo refactor
        val date: String?
        var dataDate = task.dueDate!!
        dataDate = dataDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        var currentDate = DateTime()
        currentDate = currentDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0)
        val diffDays = Days.daysBetween(currentDate, dataDate).days.toLong()
        date = if (diffDays == 0L) {
            if (!task.isTimePresent) {
                context.getString(R.string.today)
            } else {
                val isExpired = (Hours.hoursBetween(DateTime(), task.dueDate).hours < 0
                        || Minutes.minutesBetween(DateTime(), task.dueDate).minutes < 0)
                ((if (isExpired) context.getString(R.string.expired) else context.getString(R.string.today))
                        + " " + context.getString(R.string.at) + " " + task.parseTime())
            }
        } else if (diffDays < 0) {
            context.getString(R.string.expired)
        } else if (diffDays == 1L) {
            context.getString(R.string.tomorrow) + if (task.isTimePresent) " " + context.getString(R.string.at) + " " + task.parseTime() else ""
        } else if (diffDays <= 10) {
            context.getString(R.string.`in`) + " " + diffDays + " " + context.getString(R.string.days)
        } else {
            task.parseDate()
        }
        return context.getString(R.string.due_date) + ": " + date
    }

    private fun getWidgetTasks(): List<SimpleTask?> {
        return db.getTasksInWidget(
            AppSettings.shouldWidgetShowExpiredTasks(context),
            AppSettings.getWidgetTimeSpan(context)
        )
    }

    override fun getLoadingView(): RemoteViews? {
        return null
    }

    override fun getViewTypeCount(): Int {
        return 1
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun hasStableIds(): Boolean {
        return true
    }
}