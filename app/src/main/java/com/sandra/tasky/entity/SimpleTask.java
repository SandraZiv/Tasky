package com.sandra.tasky.entity;

import com.applandeo.materialcalendarview.EventDay;
import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.Serializable;
import java.util.Calendar;


public class SimpleTask implements Serializable {
    private int id;
    private String title;
    private String note;
    private DateTime dueDate;
    private boolean completed;
    private boolean timePresent;
    private boolean showInWidget;
    private int repeat;
    private TaskCategory category;

    public SimpleTask() {
        this.id = TaskyConstants.EMPTY_ID;
        this.title = "";
        this.note = "";
        this.dueDate = null;
        this.completed = false;
        this.timePresent = false;
        this.showInWidget = true;
        this.repeat = TaskyConstants.REPEAT_ONCE;
        this.category = null;
    }

    public SimpleTask(int id, String title, String note, DateTime dueDate, boolean completed,
                      boolean timePresent, boolean showInWidget, int repeat, TaskCategory category) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.dueDate = dueDate;
        this.completed = completed;
        this.timePresent = timePresent;
        this.showInWidget = showInWidget;
        this.repeat = repeat;
        this.category = category;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public DateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(DateTime dueDate) {
        this.dueDate = dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public boolean isTimePresent() {
        return timePresent;
    }

    public void setTimePresent(boolean timePresent) {
        this.timePresent = timePresent;
    }

    public String parseDateTime() {
        return parseDate() + " " + parseTime();
    }

    public String parseDate() {
        return DateTimeFormat.fullDate().print(dueDate);
    }

    public String parseTime() {
        return DateTimeFormat.shortTime().print(dueDate);
    }

    public boolean isShowInWidget() {
        return showInWidget;
    }

    public void setShowInWidget(boolean showInWidget) {
        this.showInWidget = showInWidget;
    }

    public boolean isRepeating() {
        return repeat != TaskyConstants.REPEAT_ONCE;
    }

    public int getRepeat() {
        return repeat;
    }

    public void setRepeat(int repeat) {
        this.repeat = repeat;
    }

    public TaskCategory getCategory() {
        return category;
    }

    public void setCategory(TaskCategory category) {
        this.category = category;
    }

    public EventDay asEventDay() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(dueDate.getMillis());
        return new EventDay(calendar, completed ? R.drawable.calendar_event_checked : R.drawable.calendar_event_todo);
    }

    public boolean fullTaskEquals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleTask task = (SimpleTask) o;

        if (id != task.id) return false;
        if (completed != task.completed) return false;
        if (timePresent != task.timePresent) return false;
        if (showInWidget != task.showInWidget) return false;
        if (repeat != task.repeat) return false;
        if (title != null ? !title.equals(task.title) : task.title != null) return false;
        if (note != null ? !note.equals(task.note) : task.note != null) return false;
        if (dueDate != null ? !dueDate.equals(task.dueDate) : task.dueDate != null) return false;
        return category != null ? category.equals(task.category) : task.category == null;

    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleTask task = (SimpleTask) o;

        return id == task.id;

    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public String toString() {
        return title + " " + (dueDate != null ? parseDateTime() : " ");
    }
}
