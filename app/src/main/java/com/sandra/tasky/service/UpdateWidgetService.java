package com.sandra.tasky.service;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.TaskyUtils;

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

        //set alarm for next midnight
        if (intent != null && intent.getExtras() != null && intent.getExtras().getBoolean(TaskyConstants.ALARM_EXTRA_REPEATABLE)) {
            TaskyUtils.setMidnightUpdater(this);
        }
    }

    private void createLogEntry() {
        SharedPreferences.Editor editor = getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE).edit();
        editor.putString(TaskyConstants.PREFS_LAST_UPDATE, getString(R.string.last_update) + " " + new SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(new Date()));
        editor.apply();
    }
}
