package com.sandra.tasky.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.sandra.tasky.NotificationUtils;
import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;
import com.sandra.tasky.db.TaskDatabase;
import com.sandra.tasky.entity.SimpleTask;

import net.danlew.android.joda.JodaTimeAndroid;

public class NotificationService extends IntentService {

    public NotificationService() {
        super("NotificationService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        boolean prefShowNotifications = preferences.getBoolean(getString(R.string.pref_show_notifications_key), getResources().getBoolean(R.bool.pref_show_notifications_default));

        if (prefShowNotifications && intent != null && TaskyConstants.NOTIFICATION_ACTION.equals(intent.getAction())) {
            try {
                //TODO two tasks at the same time?
                JodaTimeAndroid.init(this);
                SimpleTask task = (SimpleTask) TaskyUtils.deserialize(intent.getByteArrayExtra(TaskyConstants.NOTIFICATION_TASK_BUNDLE_KEY));
                if (checkTask(task)) {
                    NotificationUtils.taskReminder(this, task);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private boolean checkTask(SimpleTask task) {
        TaskDatabase db = new TaskDatabase(this);
        SimpleTask other = db.getTaskById(task.getId());
        return task.getDueDate().equals(other.getDueDate());
    }
}
