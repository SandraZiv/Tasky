package com.sandra.tasky.entity;

import com.sandra.tasky.TaskyConstants;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

import java.io.Serializable;


public class SimpleTask implements Serializable {
    private int id;
    private String title;
    private String note;
    private DateTime dueDate;
    private boolean completed;
    private boolean timePresent;
    private boolean showInWidget;

    public SimpleTask() {
        this.id = TaskyConstants.EMPTY_ID;
        this.title = "";
        this.note = "";
        this.dueDate = null;
        this.completed = false;
        this.timePresent = false;
        this.showInWidget = true;
    }

    public SimpleTask(int id, String title, String note, DateTime dueDate, boolean completed, boolean timePresent, boolean showInWidget) {
        this.id = id;
        this.title = title;
        this.note = note;
        this.dueDate = dueDate;
        this.completed = completed;
        this.timePresent = timePresent;
        this.showInWidget = showInWidget;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SimpleTask that = (SimpleTask) o;

        if (id != that.id) return false;
        return title.equals(that.title);

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + title.hashCode();
        return result;
    }
}
