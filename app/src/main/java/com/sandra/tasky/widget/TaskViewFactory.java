package com.sandra.tasky.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.Minutes;

import java.util.List;


public class TaskViewFactory implements RemoteViewsService.RemoteViewsFactory {
    TaskDatabase db;

    private List<SimpleTask> list;

    private Context context;
    private int appWidgetId;

    public TaskViewFactory(Context applicationContext, Intent intent) {
        this.context = applicationContext;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        db = new TaskDatabase(context);
        this.list = db.getTasksInWidget(preferences.getBoolean(TaskyConstants.PREFS_SHOW_COMPLETED, true),
                preferences.getBoolean(TaskyConstants.PREFS_SHOW_EXPIRED, false),
                preferences.getString(TaskyConstants.PREFS_TIME_SPAN, TaskyConstants.PREFS_TIME_SPAN_DEFAULT));
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        list = db.getTasksInWidget(preferences.getBoolean(TaskyConstants.PREFS_SHOW_COMPLETED, true),
                preferences.getBoolean(TaskyConstants.PREFS_SHOW_EXPIRED, false),
                preferences.getString(TaskyConstants.PREFS_TIME_SPAN, TaskyConstants.PREFS_TIME_SPAN_DEFAULT));
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RemoteViews row;
        row = new RemoteViews(context.getPackageName(), R.layout.widget_list_layout);
        row.setTextViewText(R.id.tw__widget_title, list.get(position).getTitle());
        row.setTextViewText(R.id.tw_widget_status,
                context.getString(R.string.status) + " "
                        + (list.get(position).isCompleted() ? context.getString(R.string.done) : context.getString(R.string.to_do)));
        if (list.get(position).getDueDate() != null)
            row.setTextViewText(R.id.tw_widget_due_date, getDateText(list.get(position)));
        else
            row.setTextViewText(R.id.tw_widget_due_date, context.getString(R.string.no_due_date));

        //open task detail in TaskActivity
        Intent fillIntent = new Intent();
        fillIntent.putExtra(TaskyConstants.TASK_BUNDLE_KEY, list.get(position));
        row.setOnClickFillInIntent(R.id.widget_layout_parent, fillIntent);
//        row.setOnClickFillInIntent(R.id.tw__widget_title, fillIntent);
//        row.setOnClickFillInIntent(R.id.tw_widget_status, fillIntent);
//        row.setOnClickFillInIntent(R.id.tw_widget_due_date, fillIntent);

        return row;
    }

    private String getDateText(SimpleTask task) {
        String date;
        DateTime dataDate = task.getDueDate();
        dataDate = dataDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);
        DateTime currentDate = new DateTime();
        currentDate = currentDate.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

        long diffDays = Days.daysBetween(currentDate, dataDate).getDays();
        if (diffDays == 0) {
            if (!task.isTimePresent()) {
                date = context.getString(R.string.today);
            } else {
                    boolean isExpired = Hours.hoursBetween(new DateTime(), task.getDueDate()).getHours() < 0
                            || Minutes.minutesBetween(new DateTime(), task.getDueDate()).getMinutes() < 0;
                    date = (isExpired ? context.getString(R.string.expired) : context.getString(R.string.today))
                            + " " + context.getString(R.string.at) + " " + task.parseTime();
            }
        } else if (diffDays < 0) {
            date = context.getString(R.string.expired);
        } else if (diffDays == 1) {
            date = context.getString(R.string.tommorow) + (task.isTimePresent() ? " " + context.getString(R.string.at) + " " + task.parseTime() : "");
        } else if (diffDays <= 10) {
            date = context.getString(R.string.in) + " " + diffDays + " " + context.getString(R.string.days);
        } else {
            date = task.parseDate();
        }
        return context.getString(R.string.due_date) + ": " + date;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

}
