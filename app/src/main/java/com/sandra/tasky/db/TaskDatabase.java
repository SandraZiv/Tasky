package com.sandra.tasky.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
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


public class TaskDatabase {

    private static final int TRUE = 1;
    private static final int FALSE = 0;

    //creating table columns for tasks table
    private static final String TASKS_KEY_ID = "ID";
    private static final String TASK_TITLE_COLUMN = "TASK_TITLE_COLUMN";
    private static final String TASK_COMPLETED_COLUMN = "TASK_COMPLETED_COLUMN";
    private static final String TASK_NOTE_COLUMN = "TASK_NOTE_COLUMN";
    private static final String TASK_DATE_COLUMN = "TASK_DATE_COLUMN";
    private static final String TASK_TIME_PRESENT_COLUMN = "TASK_TIME_PRESENT_COLUMN";
    private static final String TASK_SHOW_IN_WIDGET_COLUMN = "SHOW_IN_WIDGET_COLUMN";

    //creating table columns for categories table
    private static final String CATEGORIES_KEY_ID = "ID";
    private static final String CATEGORIES_TITLE = "CATEGORIES_TITLE";

    private static TaskDatabaseOpenHelper taskDatabaseOpenHelper = null;

    private SQLiteDatabase dbWritable;
    private SQLiteDatabase dbReadable;

    private Context context;

    public TaskDatabase(Context context) {
        this.context = context;

        taskDatabaseOpenHelper = TaskDatabaseOpenHelper.getInstance(context);

        dbWritable = taskDatabaseOpenHelper.getWritableDatabase();
        dbReadable = taskDatabaseOpenHelper.getReadableDatabase();

        JodaTimeAndroid.init(context);
    }

    public void closeDatabase() {
        taskDatabaseOpenHelper.close();
    }

    public void addData(SimpleTask task) {
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

    public int updateData(SimpleTask task) {
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

    public List<SimpleTask> getAllData() {
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

    public int deleteData(SimpleTask task) {
        int id = task.getId();
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE_TASKS, TASKS_KEY_ID + " = " + id, null);
    }

    public int deleteAllData() {
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

    private static class TaskDatabaseOpenHelper extends SQLiteOpenHelper {

        static final String DATABASE_NAME = "task_database.db";
        static final String DATABASE_TABLE_TASKS = "taskTable";
        static final String DATABASE_TABLE_CATEGORIES = "categoriesTable";
        static final int DATABASE_VERSION = 8;

        static final String CREATE_TABLE_TASKS = "create table " + DATABASE_TABLE_TASKS + " ( "
                + TASKS_KEY_ID + " integer primary key autoincrement,"
                + TASK_TITLE_COLUMN + " text not null,"
                + TASK_COMPLETED_COLUMN + " smallint check (" + TASK_COMPLETED_COLUMN + " in (0,1)),"
                + TASK_NOTE_COLUMN + " text,"
                + TASK_DATE_COLUMN + " timestamp,"
                + TASK_TIME_PRESENT_COLUMN + " smallint check (" + TASK_TIME_PRESENT_COLUMN + " in (0,1)),"
                + TASK_SHOW_IN_WIDGET_COLUMN + " smallint default " + TRUE
                + ");";

        static final String CREATE_TABLE_CATEGORIES = "create table " + DATABASE_TABLE_CATEGORIES + " ( "
                + CATEGORIES_KEY_ID + " integer primary key autoincrement,"
                + CATEGORIES_TITLE + " text not null"
                + ");";

        private static TaskDatabaseOpenHelper getInstance(Context context) {
            if (taskDatabaseOpenHelper == null) {
                taskDatabaseOpenHelper = new TaskDatabaseOpenHelper(context,
                        TaskDatabaseOpenHelper.DATABASE_NAME,
                        null,
                        TaskDatabaseOpenHelper.DATABASE_VERSION);
            }
            return taskDatabaseOpenHelper;
        }

        private TaskDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_TASKS);
            db.execSQL(CREATE_TABLE_CATEGORIES);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("alter table " + DATABASE_TABLE_TASKS + " rename to old");
            db.execSQL(CREATE_TABLE_TASKS);
            String columns = TASKS_KEY_ID + "," + TASK_TITLE_COLUMN + "," + TASK_COMPLETED_COLUMN + "," + TASK_NOTE_COLUMN + "," + TASK_DATE_COLUMN + "," + TASK_TIME_PRESENT_COLUMN;
            db.execSQL("insert into " + DATABASE_TABLE_TASKS + "(" + columns + ")"
                    + " select " + columns + " from old");
            db.execSQL("drop table old");

            db.execSQL(CREATE_TABLE_CATEGORIES);
        }
    }

}
