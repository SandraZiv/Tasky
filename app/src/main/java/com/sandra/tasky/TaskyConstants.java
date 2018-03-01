package com.sandra.tasky;


public class TaskyConstants {

    public static final String NOTIFICATION_ACTION = "com.sandra.tasky.notification.ACTION";
    public static final String NOTIFICATION_TASK_BUNDLE_KEY = "com.sandra.tasky.notification.TASK_BUNDLE_KEY";

    public static final String WIDGET_UPDATE_ACTION = "com.sandra.tasky.widget.UPDATE_WIDGET";
    public static final String WIDGET_FIRST_RUN = "first_run_of_widget_1.19";
    public static final String PREFS_FIRST_RUN = "prefs_first_run_of_widget_1.19";
    public static final String PREFS_LAST_UPDATE = "prefs_last_update";
    public static final String ALARM_EXTRA_TITLE = "ALARM_EXTRA_TITLE";
    public static final String ALARM_EXTRA_REPEATABLE = "ALARM_EXTRA_REPEATABLE";
    public static final String ALARM_EXTRA_TIME = "ALARM_EXTRA_TIME";

    public static final long INTERVAL_DAY = 24 * 60 * 60 * 1000;

    public static final int EMPTY_ID = -1;
    public static final String TASK_BUNDLE_KEY = "com.sandra.tasky.task";

    public static final int MAX_TITLE_LENGTH = 35;
    public static final int MAX_TEXT_LENGTH = 100;

    public static final long ALL_CATEGORY_ID = -1L;
    public static final long OTHERS_CATEGORY_ID = -2L;

    public static final String SELECTED_CATEGORY_KEY = "SELECTED_CATEGORY_KEY";

    public static final String PREF_GENERAL = "PREF_GENERAL";

    public static String PREF_SORT = "PREF_SORT";
    public static final int SORT_DUE_DATE = 0;
    public static final int SORT_TITLE = 1;
    public static final int SORT_COMPLETED = 2;
    public static final int SORT_DEFAULT = SORT_DUE_DATE;

}
