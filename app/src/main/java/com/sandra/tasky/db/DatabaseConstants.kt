package com.sandra.tasky.db

internal object DatabaseConstants {
    // same does the room
    const val TRUE = 1
    const val FALSE = 0

    // creating table columns for tasks table
    const val TASKS_KEY_ID = "ID"
    const val TASK_TITLE_COLUMN = "TASK_TITLE_COLUMN"
    const val TASK_COMPLETED_COLUMN = "TASK_COMPLETED_COLUMN"
    const val TASK_NOTE_COLUMN = "TASK_NOTE_COLUMN"
    const val TASK_DATE_COLUMN = "TASK_DATE_COLUMN"
    const val TASK_TIME_PRESENT_COLUMN = "TASK_TIME_PRESENT_COLUMN"
    const val TASK_SHOW_IN_WIDGET_COLUMN = "SHOW_IN_WIDGET_COLUMN"
    const val TASK_REPEAT_COLUMN = "TASK_REPEAT_COLUMN"
    const val TASK_CATEGORY_FK = "TASK_CATEGORY_FK"

    // creating table columns for categories table
    const val CATEGORIES_KEY_ID = "ID"
    const val CATEGORIES_TITLE = "CATEGORIES_TITLE"
}