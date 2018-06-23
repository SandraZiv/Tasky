package com.sandra.tasky.utils;


import android.content.Context;

import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.entity.SimpleTask;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

public class TimeUtils {

    public static boolean isInFuture(SimpleTask task) {
        DateTime current = DateTime.now();
        current = current.withSecondOfMinute(0).withMillisOfSecond(0);
        //task is already cleared in setupDateTimeForDB()

        return task.getDueDate().getMillis() > current.getMillis();
    }

    public static boolean isExpired(SimpleTask task) {
        DateTime current = DateTime.now();
        current = current.withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).withMillisOfSecond(0);

        return task.getDueDate().getMillis() < current.getMillis();
    }

    public static long untilMidnight(Context context) {
        JodaTimeAndroid.init(context);

        LocalTime localTime = LocalTime.now();
        long timeMidnight = 24 * 60 * 60
                - localTime.getHourOfDay() * 60 * 60
                - localTime.getMinuteOfHour() * 60
                - localTime.getSecondOfMinute();
        timeMidnight *= 1000;

        return timeMidnight;
    }

    public static DateTime moveToNextRepeat(SimpleTask task) {
        return new DateTime(calculateNewTaskTime(task));
    }

    public static long calculateNewTaskTime(SimpleTask task) {
        switch (task.getRepeat()) {
            case TaskyConstants.REPEAT_DAY:
                return task.getDueDate().plusDays(1).getMillis();
            case TaskyConstants.REPEAT_WEEK:
                return task.getDueDate().plusWeeks(1).getMillis();
            case TaskyConstants.REPEAT_MONTH:
                return task.getDueDate().plusMonths(1).getMillis();
            case TaskyConstants.REPEAT_YEAR:
                return task.getDueDate().plusYears(1).getMillis();
            default:
                return task.getDueDate().getMillis();
        }
    }

    //validate only date not time
    public static boolean dateEqual(DateTime first, DateTime second) {
        return first.getYear() == second.getYear()
                && first.getMonthOfYear() == second.getMonthOfYear()
                && first.getDayOfMonth() == second.getDayOfMonth();
    }
}
