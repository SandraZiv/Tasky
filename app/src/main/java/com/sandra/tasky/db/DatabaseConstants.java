package com.sandra.tasky.db;

class DatabaseConstants {

    static final int TRUE = 1;
    static final int FALSE = 0;

    //creating table columns for tasks table
    static final String TASKS_KEY_ID = "ID";
    static final String TASK_TITLE_COLUMN = "TASK_TITLE_COLUMN";
    static final String TASK_COMPLETED_COLUMN = "TASK_COMPLETED_COLUMN";
    static final String TASK_NOTE_COLUMN = "TASK_NOTE_COLUMN";
    static final String TASK_DATE_COLUMN = "TASK_DATE_COLUMN";
    static final String TASK_TIME_PRESENT_COLUMN = "TASK_TIME_PRESENT_COLUMN";
    static final String TASK_SHOW_IN_WIDGET_COLUMN = "SHOW_IN_WIDGET_COLUMN";
    static final String TASK_REPEAT_COLUMN = "TASK_REPEAT_COLUMN";
    static final String TASK_CATEGORY_FK = "TASK_CATEGORY_FK";

    //creating table columns for categories table
    static final String CATEGORIES_KEY_ID = "ID";
    static final String CATEGORIES_TITLE = "CATEGORIES_TITLE";
}