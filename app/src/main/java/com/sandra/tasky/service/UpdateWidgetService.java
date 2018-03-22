package com.sandra.tasky.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;
import com.sandra.tasky.utils.AlarmUtils;
import com.sandra.tasky.utils.TaskyUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class UpdateWidgetService extends IntentService {

    public UpdateWidgetService() {
        super("UpdateWidgetService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        TaskyUtils.updateWidget(this);

        createLogEntry();

        if (intent == null || intent.getExtras() == null) {
            return;
        }

        String action = intent.getAction();
        if (TaskyConstants.WIDGET_TASK_UPDATE_ACTION.equals(action)) {
            //check if alarm is repeating
            if (intent.getExtras().getBoolean(TaskyConstants.ALARM_EXTRA_REPEATABLE)) {
                try {
                    SimpleTask task = (SimpleTask) TaskyUtils.deserialize(intent.getByteArrayExtra(TaskyConstants.ALARM_EXTRA_TASK));
                    if (checkTask(task)) {
                        AlarmUtils.rescheduleTask(this, task);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        } else if (TaskyConstants.WIDGET_MIDNIGHT_UPDATE_ACTION.equals(action)) {
            //set alarm for next midnight
            AlarmUtils.setMidnightUpdater(this);
        }

    }

    private void createLogEntry() {
        SharedPreferences preferences = getSharedPreferences(TaskyConstants.WIDGET_PREF, MODE_PRIVATE);

        if (preferences.getBoolean(TaskyConstants.PREFS_IS_WIDGET_ENABLED, TaskyConstants.WIDGET_DEFAULT)) {
            preferences.edit()
                    .putString(TaskyConstants.PREFS_LAST_UPDATE, getString(R.string.last_update) + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()))
                    .apply();
        }

    }

    //maybe unnecessary
    private boolean checkTask(SimpleTask task) {
        TaskDatabase db = new TaskDatabase(this);
        SimpleTask other = db.getTaskById(task.getId());
        return task.fullTaskEquals(other);
    }

}
