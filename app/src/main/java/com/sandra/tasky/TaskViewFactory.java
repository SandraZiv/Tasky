package com.sandra.tasky;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.List;


public class TaskViewFactory implements RemoteViewsService.RemoteViewsFactory {
    TaskDatabase db;

    private List<SimpleTask> list;

    private Context context;
    private int appWidgetId;

    public TaskViewFactory(Context applicationContext, Intent intent) {
        this.context = applicationContext;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

        db = new TaskDatabase(context);
        this.list = db.getActiveTasks();
    }

    @Override
    public void onCreate() {

    }

    @Override
    public void onDataSetChanged() {
        list = db.getActiveTasks();
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
        row.setTextViewText(R.id.tw_widget_status, "Status: " + (list.get(position).isCompleted() ? "Done" : "To do"));
        if(list.get(position).getDueDate()!=null)
            row.setTextViewText(R.id.tw_widget_due_date, getDateText(list.get(position)));
        else
            row.setTextViewText(R.id.tw_widget_due_date, "No due date");
        Intent intent = new Intent();
        Bundle extras = new Bundle();

        extras.putSerializable(TaskWidget.KEY_TASK_WIDGET, list.get(position));
        intent.putExtras(extras);

        row.setOnClickFillInIntent(R.id.tw_title, intent);
        return row;
    }

    private String getDateText(SimpleTask task) {
        String date;
        DateTime dataDate = task.getDueDate();
        long diff = Days.daysBetween(new DateTime(), dataDate).getDays();
        if(diff == 0) date = "today";
        else if(diff == 1) date = "tomorrow";
        else if(diff < 0) date = "expired";
        else if(diff <= 10) date = "in " + diff + " days";
        else date = task.parseDate();
        return "Due date: " + date;
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
