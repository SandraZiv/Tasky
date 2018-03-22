package com.sandra.tasky.service;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.utils.AlarmUtils;
import com.sandra.tasky.utils.NotificationUtils;
import com.sandra.tasky.utils.TimeUtils;

import java.util.List;

public class RebootService extends IntentService {

    public RebootService() {
        super("RebootService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        TaskDatabase db = new TaskDatabase(this);
        List<SimpleTask> tasks = db.getAllTasks();

        for (SimpleTask task : tasks) {
            //tweak date for repeating
            if (task.getDueDate() != null && !task.isCompleted()) {
                while (!TimeUtils.isInFuture(task) && task.isRepeating()) {
                    task.setDueDate(TimeUtils.moveToNextRepeat(task));
                }
            }
            //set alarms and notifications
            if (task.getDueDate() != null && TimeUtils.isInFuture(task)) {
                AlarmUtils.initTaskAlarm(this, task);
                NotificationUtils.setNotificationReminder(this, task);
            }
        }


    }
}
