package com.sandra.tasky;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;


public class TaskDatabase {

    private static final int TRUE = 1;
    private static final int FALSE = 0;

    //creating table columns
    private static final String TITLE_COLUMN = "TASK_TITLE_COLUMN";
    private static final String COMPLETED_COLUMN = "TASK_COMPLETED_COLUMN";
    private static final String NOTE_COLUMN = "TASK_NOTE_COLUMN";
    private static final String DATE_COLUMN = "TASK_DATE_COLUMN";
    private static final String TIME_PRESENT_COLUMN = "TASK_TIME_PRESENT_COLUMN";

    //creating index
    private static final String KEY_ID = "ID";

    private TaskDatabaseOpenHelper taskDatabaseOpenHelper;

    private SQLiteDatabase dbWritable;
    private SQLiteDatabase dbReadable;

    public TaskDatabase(Context context) {
        this.taskDatabaseOpenHelper = new TaskDatabaseOpenHelper(context,
                TaskDatabaseOpenHelper.DATABASE_NAME,
                null,
                TaskDatabaseOpenHelper.DATABASE_VERSION);

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
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getTimeInMillis()).toString());
        newValues.put(TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));

        dbWritable.insert(TaskDatabaseOpenHelper.DATABASE_TABLE, null, newValues);
    }

    public int updateData(SimpleTask task) {
        ContentValues updateValues = new ContentValues();

        updateValues.put(TITLE_COLUMN, task.getTitle());
        updateValues.put(COMPLETED_COLUMN, (task.isCompleted() ? TRUE : FALSE));
        updateValues.put(NOTE_COLUMN, task.getNote());
        updateValues.put(DATE_COLUMN,
                (task.getDueDate() == null) ? null : new Timestamp(task.getDueDate().getTimeInMillis()).toString());
        updateValues.put(TIME_PRESENT_COLUMN, (task.isTimePresent() ? TRUE : FALSE));

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
                Calendar cal = Calendar.getInstance();
                if (cursor.getString(4) != null)
                    cal.setTime(getDateFromString(cursor.getString(4)));
                else
                    cal = null;
                boolean timePresent = (cursor.getInt(5) == TRUE);
                list.add(new SimpleTask(id, title, note, cal, completed, timePresent));

            } while (cursor.moveToNext());
        }

        return list;
    }

    public int deleteData(SimpleTask task) {
        int id = task.getId();
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE, KEY_ID + " = " + id, null);
    }

    public int deleteAllData() {
        return dbWritable.delete(TaskDatabaseOpenHelper.DATABASE_TABLE, "", null);
    }


    private java.util.Date getDateFromString(String tmpDate) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        java.util.Date date = null;
        try {
            date = dateFormat.parse(tmpDate);
        } catch (ParseException e) {
            Log.e("date", "Parsing datetime failed", e);
        }
        return date;
    }

    public class TaskDatabaseOpenHelper extends SQLiteOpenHelper {

        static final String DATABASE_NAME = "task_database.db";
        static final String DATABASE_TABLE = "taskTable";
        static final int DATABASE_VERSION = 4;

        static final String CREATE_TABLE = "create table " + DATABASE_TABLE + " ( "
                + KEY_ID + " integer primary key autoincrement,"
                + TITLE_COLUMN + " text not null,"
                + COMPLETED_COLUMN + " smallint check (" + COMPLETED_COLUMN + " in (0,1) ),"
                + NOTE_COLUMN + " text,"
                + DATE_COLUMN + " timestamp,"
                + TIME_PRESENT_COLUMN + " smallint check (" + TIME_PRESENT_COLUMN + " in (0,1) )" + ");";

        public TaskDatabaseOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("drop table " + DATABASE_TABLE);
            db.execSQL(CREATE_TABLE);
        }
    }

}
