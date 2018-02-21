package com.sandra.tasky.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sandra.tasky.R;
import com.sandra.tasky.entity.SimpleTask;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import static com.sandra.tasky.db.DatabaseConstants.FALSE;
import static com.sandra.tasky.db.DatabaseConstants.TASKS_KEY_ID;
import static com.sandra.tasky.db.DatabaseConstants.TASK_COMPLETED_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_DATE_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_NOTE_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_SHOW_IN_WIDGET_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_TIME_PRESENT_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_TITLE_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TRUE;

public class TaskDatabase {

    private static TaskDatabaseOpenHelper taskDatabaseOpenHelper = null;

    private SQLiteDatabase dbWritable;
    private SQLiteDatabase dbReadable;

    private Context context;

    public TaskDatabase(Context context) {
        this.context = context;

        taskDatabaseOpenHelper = TaskDatabaseOpenHelper.getInstance(context, taskDatabaseOpenHelper);

        dbWritable = taskDatabaseOpenHelper.getWritableDatabase();
        dbReadable = taskDatabaseOpenHelper.getReadableDatabase();

        JodaTimeAndroid.init(context);
    }

    public void closeDatabase() {
        taskDatabaseOpenHelper.close();
    }

    public void addTask(SimpleTask task) {
        ContentValues newValues = new ContentValues();

        newValues.put(TASK_TITLE_COLUMN, task.getTitle());
        newValues.put(TASK_COMPLETED_COLUMN, (task.isCompleted() ? TRUE : FALSE));
        newValues.put(TASK_NOTE_COLUMN, task.getNote());
        newValues.put(TASK_DATE_COLUMN,
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getMillis()).toString());
        newValues.put(TASK_TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));
        newValues.put(TASK_SHOW_IN_WIDGET_COLUMN, (task.isShowInWidget() ? TRUE : FALSE));

        dbWritable.insert(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, null, newValues);
    }

    public int updateTask(SimpleTask task) {
        ContentValues updateValues = new ContentValues();

        updateValues.put(TASK_TITLE_COLUMN, task.getTitle());
        updateValues.put(TASK_COMPLETED_COLUMN, (task.isCompleted() ? TRUE : FALSE));
        updateValues.put(TASK_NOTE_COLUMN, task.getNote());
        updateValues.put(TASK_DATE_COLUMN,
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getMillis()).toString());
        updateValues.put(TASK_TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));
        updateValues.put(TASK_SHOW_IN_WIDGET_COLUMN, (task.isShowInWidget() ? TRUE : FALSE));

        return dbWritable.update(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, updateValues, TASKS_KEY_ID + " = " + task.getId(), null);
    }

    public List<SimpleTask> getAllTasks() {
        List<SimpleTask> list = new LinkedList<>();

        String sqlQuery = "select * from " + TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS + " order by " + TASK_DATE_COLUMN;
        Cursor cursor = dbReadable.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                boolean completed = (cursor.getInt(2) == TRUE);
                String note = cursor.getString(3);
                DateTime date;
                if (cursor.getString(4) != null)
                    date = getDateFromString(cursor.getString(4));
                else
                    date = null;
                boolean timePresent = (cursor.getInt(5) == TRUE);
                boolean showInWidget = (cursor.getInt(6) == TRUE);
                list.add(new SimpleTask(id, title, note, date, completed, timePresent, showInWidget));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    //used only in widget
    public List<SimpleTask> getTasksInWidget(boolean showCompleted, boolean showExpired, String timeSpan) {
        List<SimpleTask> list = new LinkedList<>();
        String where = " where (" + TASK_DATE_COLUMN + " is null or ";
        if (timeSpan.equals(context.getString(R.string.pref_time_span_default))) {
            if (showExpired) {
                where += TASK_DATE_COLUMN + " is not null)";
            } else {
                where += "date(" + TASK_DATE_COLUMN + ")" + " >= date('now'))";
            }
        } else {
            if (showExpired) {
                where += "date(" + TASK_DATE_COLUMN + ")" + " <= date('now', '" + timeSpan + "'))";
            } else {
                where += "date(" + TASK_DATE_COLUMN + ")" + " between date('now') and date('now', '" + timeSpan + "'))";
            }
        }
        where += (showCompleted ? "" : " and " + TASK_COMPLETED_COLUMN + " = " + FALSE);
        where += " and " + TASK_SHOW_IN_WIDGET_COLUMN + "=" + TRUE;

        String sqlQuery = "select * from " + TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS + where + " order by " + TASK_DATE_COLUMN;
        Cursor cursor = dbReadable.rawQuery(sqlQuery, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(0);
                String title = cursor.getString(1);
                boolean completed = (cursor.getInt(2) == TRUE);
                String note = cursor.getString(3);
                DateTime date;
                if (cursor.getString(4) != null) {
                    date = getDateFromString(cursor.getString(4));
                } else
                    date = null;
                boolean timePresent = (cursor.getInt(5) == TRUE);
                boolean showInWidget = (cursor.getInt(6) == TRUE);
                list.add(new SimpleTask(id, title, note, date, completed, timePresent, showInWidget));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public int deleteTasks(SimpleTask task) {
        int id = task.getId();
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, TASKS_KEY_ID + " = " + id, null);
    }

    public int deleteAllTasks() {
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, "", null);
    }


    private DateTime getDateFromString(String tmpDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        java.util.Date date = null;
//        DateTime date = null;
        try {
            date = dateFormat.parse(tmpDate);
//            DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");
//            date = dateTimeFormatter.parseDateTime(tmpDate);
        } catch (Exception e) {
            Log.e("date", "Parsing datetime failed", e);
        }
        return new DateTime(date);
    }
}
