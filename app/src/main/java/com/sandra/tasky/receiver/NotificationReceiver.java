package com.sandra.tasky.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sandra.tasky.NotificationUtils;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;
import com.sandra.tasky.entity.SimpleTask;

public class NotificationReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (TaskyConstants.NOTIFICATION_ACTION.equals(intent.getAction())) {
            try {
                //TODO two tasks at the same time?
                SimpleTask task = (SimpleTask) TaskyUtils.deserialize(intent.getByteArrayExtra(TaskyConstants.NOTIFICATION_TASK_BUNDLE_KEY));
                NotificationUtils.taskReminder(context, task);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
