package com.sandra.tasky.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;


public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        final boolean isFirstRun = getActivity()
                .getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE)
                .getBoolean(TaskyConstants.PREFS_FIRST_RUN, true);

        Preference preference = findPreference(TaskyConstants.PREFS_RESTART_SCHEDULER);
        final String lastUpdate = getActivity().getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE)
                .getString(TaskyConstants.PREFS_LAST_UPDATE, getString(R.string.scheduler_running));
        preference.setSummary(isFirstRun ? getString(R.string.scheduler_to_be_init) : lastUpdate);
        preference.setEnabled(!isFirstRun);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!isFirstRun) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(TaskyConstants.PREFS_FIRST_RUN, true);
                    editor.putString(TaskyConstants.PREFS_LAST_UPDATE, getString(R.string.scheduler_running));
                    editor.apply();
                    preference.setSummary(getString(R.string.scheduler_restarted));
                    preference.setEnabled(false);
                    Toast.makeText(getActivity(), R.string.scheduler_restarted, Toast.LENGTH_LONG).show();
                } else {
                    preference.setSummary(lastUpdate);
                    //this is actually never called
                    Toast.makeText(getActivity(), R.string.please_restart_widget_to_complete, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }
}
