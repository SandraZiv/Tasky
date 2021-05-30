package com.sandra.tasky.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.sandra.tasky.RepeatType;
import com.sandra.tasky.utils.NotificationUtils;
import com.sandra.tasky.R;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.entity.TaskCategory;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.sandra.tasky.db.DatabaseConstants.CATEGORIES_KEY_ID;
import static com.sandra.tasky.db.DatabaseConstants.CATEGORIES_TITLE;
import static com.sandra.tasky.db.DatabaseConstants.FALSE;
import static com.sandra.tasky.db.DatabaseConstants.TASKS_KEY_ID;
import static com.sandra.tasky.db.DatabaseConstants.TASK_CATEGORY_FK;
import static com.sandra.tasky.db.DatabaseConstants.TASK_COMPLETED_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_DATE_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_NOTE_COLUMN;
import static com.sandra.tasky.db.DatabaseConstants.TASK_REPEAT_COLUMN;
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

    public int addTask(SimpleTask task) {
        ContentValues newValues = new ContentValues();

        newValues.put(TASK_TITLE_COLUMN, task.getTitle());
        newValues.put(TASK_COMPLETED_COLUMN, (task.isCompleted() ? TRUE : FALSE));
        newValues.put(TASK_NOTE_COLUMN, task.getNote());
        newValues.put(TASK_DATE_COLUMN,
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getMillis()).toString());
        newValues.put(TASK_TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));
        newValues.put(TASK_SHOW_IN_WIDGET_COLUMN, (task.getShouldShowInWidget() ? TRUE : FALSE));
        newValues.put(TASK_REPEAT_COLUMN, task.getRepeat().getValue());
        newValues.put(TASK_CATEGORY_FK, task.getCategory() == null ? null : task.getCategory().getId());

        return (int) dbWritable.insert(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, null, newValues);
    }

    public boolean addCategory(TaskCategory category) {
        ContentValues values = new ContentValues();
        values.put(CATEGORIES_TITLE, category.getTitle());
        return dbWritable.insert(TaskDatabaseOpenHelper.DATABASE_TABLE_CATEGORIES, null, values) != -1;
    }

    public int updateTask(SimpleTask task) {
        ContentValues updateValues = new ContentValues();

        updateValues.put(TASK_TITLE_COLUMN, task.getTitle());
        updateValues.put(TASK_COMPLETED_COLUMN, (task.isCompleted() ? TRUE : FALSE));
        updateValues.put(TASK_NOTE_COLUMN, task.getNote());
        updateValues.put(TASK_DATE_COLUMN,
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getMillis()).toString());
        updateValues.put(TASK_TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));
        updateValues.put(TASK_SHOW_IN_WIDGET_COLUMN, (task.getShouldShowInWidget() ? TRUE : FALSE));
        updateValues.put(TASK_REPEAT_COLUMN, task.getRepeat().getValue());
        updateValues.put(TASK_CATEGORY_FK, task.getCategory() == null ? null : task.getCategory().getId());

        String where = TASKS_KEY_ID + " = " + task.getId();

        return dbWritable.update(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, updateValues, where, null);
    }

    public int updateCategory(TaskCategory category) {
        ContentValues values = new ContentValues();
        String where = CATEGORIES_KEY_ID + " = " + category.getId();
        return dbWritable.update(TaskDatabaseOpenHelper.DATABASE_TABLE_CATEGORIES, values, where, null);
    }

    public SimpleTask getTaskById(int id) {
        String sqlQuery = "select * from " + TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS
                + " where " + TASKS_KEY_ID + " = " + id;
        Cursor cursor = dbReadable.rawQuery(sqlQuery, null);

        SimpleTask task = null;

        if (cursor.moveToFirst()) {
            String title = cursor.getString(1);
            boolean completed = (cursor.getInt(2) == TRUE);
            String note = cursor.getString(3);
            DateTime date;
            if (cursor.getString(4) != null) {
                date = getDateFromString(cursor.getString(4));
            } else {
                date = null;
            }

            boolean timePresent = (cursor.getInt(5) == TRUE);
            boolean showInWidget = (cursor.getInt(6) == TRUE);

            RepeatType repeat = RepeatType.Companion.getByValue(
                    cursor.getInt(cursor.getColumnIndex(DatabaseConstants.TASK_REPEAT_COLUMN))
            );

            int categoryId = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.TASK_CATEGORY_FK));

            task = new SimpleTask(id, title, note, date, completed, timePresent, showInWidget,
                    repeat, getCategoryById(categoryId));
        }

        cursor.close();
        return task;
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
                if (cursor.getString(4) != null) {
                    date = getDateFromString(cursor.getString(4));
                } else {
                    date = null;
                }
                boolean timePresent = (cursor.getInt(5) == TRUE);
                boolean showInWidget = (cursor.getInt(6) == TRUE);

                RepeatType repeat = RepeatType.Companion.getByValue(
                        cursor.getInt(cursor.getColumnIndex(DatabaseConstants.TASK_REPEAT_COLUMN))
                );

                int categoryId = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.TASK_CATEGORY_FK));

                list.add(new SimpleTask(id, title, note, date, completed, timePresent, showInWidget,
                        repeat, getCategoryById(categoryId)));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    private TaskCategory getCategoryById(int id) {
        List<TaskCategory> categories = getAllCategories();
        for (TaskCategory category : categories) {
            if (category.getId() == id) {
                return category;
            }
        }
        return null;
    }

    public List<TaskCategory> getAllCategories() {
        List<TaskCategory> categories = new LinkedList<>();

        Cursor cursor = dbReadable.query(TaskDatabaseOpenHelper.DATABASE_TABLE_CATEGORIES,
                null, null, null, null, null, null);

        while (cursor.moveToNext()) {
            TaskCategory category = new TaskCategory(cursor.getInt(cursor.getColumnIndex(CATEGORIES_KEY_ID)),
                    cursor.getString(cursor.getColumnIndex(CATEGORIES_TITLE)));
            categories.add(category);
        }

        cursor.close();
        return categories;
    }

    //used only in widget
    public List<SimpleTask> getTasksInWidget(boolean showExpired, String timeSpan) {
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
        where += " and " + TASK_COMPLETED_COLUMN + " = " + FALSE;
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
                RepeatType repeat = RepeatType.Companion.getByValue(
                        cursor.getInt(cursor.getColumnIndex(DatabaseConstants.TASK_REPEAT_COLUMN))
                );

                int categoryId = cursor.getInt(cursor.getColumnIndex(DatabaseConstants.TASK_CATEGORY_FK));

                list.add(new SimpleTask(id, title, note, date, completed, timePresent, showInWidget,
                        repeat, getCategoryById(categoryId)));

            } while (cursor.moveToNext());
        }

        cursor.close();
        return list;
    }

    public int deleteCategory(TaskCategory category) {
        int id = category.getId();
        String where = TASKS_KEY_ID + " = " + id;
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_CATEGORIES, where, null);
    }

    public int deleteTask(SimpleTask task) {
        NotificationUtils.INSTANCE.cancelNotification(context, task);

        int id = task.getId();
        String where = TASKS_KEY_ID + " = " + id;
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, where, null);
    }

    public int deleteAllTasks() {
        NotificationUtils.INSTANCE.cancelAllNotifications(context);
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, null, null);
    }

    public int deleteAllTasksInCategory(int[] tasksIds) {
        String where = "";
        for (int i = 0; i < tasksIds.length; i++) {
            if (i != 0) {
                where += " OR ";
            }
            where += TASKS_KEY_ID + " = " + tasksIds[i];
            NotificationUtils.INSTANCE.cancelNotification(context, tasksIds[i]);
        }
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, where, null);
    }

    public int deleteAllCategories() {
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_CATEGORIES, null, null);
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