package com.sandra.tasky.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.sandra.tasky.db.DatabaseConstants.*;

class TaskDatabaseOpenHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "task_database.db";
    static final String DATABASE_TABLE_TASKS = "taskTable";
    static final String DATABASE_TABLE_CATEGORIES = "categoriesTable";
    private static final int DATABASE_VERSION = 8;

    private static final String CREATE_TABLE_TASKS = "create table " + DATABASE_TABLE_TASKS + " ( "
            + TASKS_KEY_ID + " integer primary key autoincrement,"
            + TASK_TITLE_COLUMN + " text not null,"
            + TASK_COMPLETED_COLUMN + " smallint check (" + TASK_COMPLETED_COLUMN + " in (0,1)),"
            + TASK_NOTE_COLUMN + " text,"
            + TASK_DATE_COLUMN + " timestamp,"
            + TASK_TIME_PRESENT_COLUMN + " smallint check (" + TASK_TIME_PRESENT_COLUMN + " in (0,1)),"
            + TASK_SHOW_IN_WIDGET_COLUMN + " smallint default " + TRUE
            + ");";

    private static final String CREATE_TABLE_CATEGORIES = "create table " + DATABASE_TABLE_CATEGORIES + " ( "
            + CATEGORIES_KEY_ID + " integer primary key autoincrement,"
            + CATEGORIES_TITLE + " text not null"
            + ");";

    static TaskDatabaseOpenHelper getInstance(Context context, TaskDatabaseOpenHelper taskDatabaseOpenHelper) {
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
//        db.execSQL("alter table " + DATABASE_TABLE_TASKS + " rename to old");
//        db.execSQL(CREATE_TABLE_TASKS);
//        String columns = TASKS_KEY_ID + "," + TASK_TITLE_COLUMN + "," + TASK_COMPLETED_COLUMN + "," + TASK_NOTE_COLUMN + "," + TASK_DATE_COLUMN + "," + TASK_TIME_PRESENT_COLUMN;
//        db.execSQL("insert into " + DATABASE_TABLE_TASKS + "(" + columns + ")"
//                + " select " + columns + " from old");
//        db.execSQL("drop table old");

        db.execSQL(CREATE_TABLE_CATEGORIES);
    }
}