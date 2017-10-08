package com.sandra.tasky.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.entity.SimpleTask;

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

    //creating table columns
    private static final String TITLE_COLUMN = "TASK_TITLE_COLUMN";
    private static final String COMPLETED_COLUMN = "TASK_COMPLETED_COLUMN";
    private static final String NOTE_COLUMN = "TASK_NOTE_COLUMN";
    private static final String DATE_COLUMN = "TASK_DATE_COLUMN";
    private static final String TIME_PRESENT_COLUMN = "TASK_TIME_PRESENT_COLUMN";
    private static final String SHOW_IN_WIDGET_COLUMN = "SHOW_IN_WIDGET_COLUMN";

    //creating index
    private static final String KEY_ID = "ID";

    private static TaskDatabaseOpenHelper taskDatabaseOpenHelper = null;

    private SQLiteDatabase dbWritable;
    private SQLiteDatabase dbReadable;

    public TaskDatabase(Context context) {
        taskDatabaseOpenHelper = TaskDatabaseOpenHelper.getInstance(context);

        dbWritable = taskDatabaseOpenHelper.getWritableDatabase();
        dbReadable = taskDatabaseOpenHelper.getReadableDatabase();
    }

    public void closeDatabase() {
        taskDatabaseOpenHelper.close();
    }

    public void addData(SimpleTask task) {
        ContentValues newValues = new ContentValues();

        newValues.put(TITLE_COLUMN, task.getTitle());
        newValues.put(COMPLETED_COLUMN, (task.isCompleted() ? TRUE : FALSE));
        newValues.put(NOTE_COLUMN, task.getNote());
        newValues.put(DATE_COLUMN,
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getMillis()).toString());
        newValues.put(TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));
        newValues.put(SHOW_IN_WIDGET_COLUMN, (task.isShowInWidget() ? TRUE : FALSE));

        dbWritable.insert(TaskDatabaseOpenHelper.DATABASE_TABLE, null, newValues);
    }

    public int updateData(SimpleTask task) {
        ContentValues updateValues = new ContentValues();

        updateValues.put(TITLE_COLUMN, task.getTitle());
        updateValues.put(COMPLETED_COLUMN, (task.isCompleted() ? TRUE : FALSE));
        updateValues.put(NOTE_COLUMN, task.getNote());
        updateValues.put(DATE_COLUMN,
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getMillis()).toString());
        updateValues.put(TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));
        updateValues.put(SHOW_IN_WIDGET_COLUMN, (task.isShowInWidget() ? TRUE : FALSE));

        return dbWritable.update(TaskDatabaseOpenHelper.DATABASE_TABLE, updateValues, KEY_ID + " = " + task.getId(), null);
    }

    public List<SimpleTask> getAllData() {
        List<SimpleTask> list = new LinkedList<>();

        String sqlQuery = "select * from " + TaskDatabaseOpenHelper.DATABASE_TABLE + " order by " + DATE_COLUMN;
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
        String where = " where (" + DATE_COLUMN + " is null or ";
        if (timeSpan.equals(TaskyConstants.PREFS_TIME_SPAN_DEFAULT)) {
            if (showExpired) {
                where += DATE_COLUMN + " is not null)";
            } else {
                where += "date(" + DATE_COLUMN + ")" + " >= date('now'))";
            }
        } else {
            if (showExpired) {
                where += "date(" + DATE_COLUMN + ")" + " <= date('now', '" + timeSpan + "'))";
            } else {
                where += "date(" + DATE_COLUMN + ")" + " between date('now') and date('now', '" + timeSpan + "'))";
            }
        }
        where += (showCompleted ? "" : " and " + COMPLETED_COLUMN + " = " + FALSE);
        where += " and " + SHOW_IN_WIDGET_COLUMN + "=" + TRUE;

        String sqlQuery = "select * from " + TaskDatabaseOpenHelper.DATABASE_TABLE + where + " order by " + DATE_COLUMN;
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
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE, KEY_ID + " = " + id, null);
    }

    public int deleteAllData() {
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE, "", null);
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
        static final String DATABASE_TABLE = "taskTable";
        static final int DATABASE_VERSION = 7;

        static final String CREATE_TABLE = "create table " + DATABASE_TABLE + " ( "
                + KEY_ID + " integer primary key autoincrement,"
                + TITLE_COLUMN + " text not null,"
                + COMPLETED_COLUMN + " smallint check (" + COMPLETED_COLUMN + " in (0,1)),"
                + NOTE_COLUMN + " text,"
                + DATE_COLUMN + " timestamp,"
                + TIME_PRESENT_COLUMN + " smallint check (" + TIME_PRESENT_COLUMN + " in (0,1)),"
                + SHOW_IN_WIDGET_COLUMN + " smallint default " + TRUE
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
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("alter table " + DATABASE_TABLE + " rename to old");
            db.execSQL(CREATE_TABLE);
            String columns = KEY_ID + "," + TITLE_COLUMN + "," + COMPLETED_COLUMN + "," + NOTE_COLUMN + "," + DATE_COLUMN + "," + TIME_PRESENT_COLUMN;
            db.execSQL("insert into " + DATABASE_TABLE + "(" + columns + ")"
                    + " select " + columns + " from old");
            db.execSQL("drop table old");
        }
    }

}
