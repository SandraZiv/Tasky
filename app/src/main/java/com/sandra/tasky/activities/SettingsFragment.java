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

        Preference preference = findPreference(TaskyConstants.PREFS_RESTART_SCHEDULER);
        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                if (!getActivity()
                        .getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE)
                        .getBoolean(TaskyConstants.PREFS_FIRST_RUN, true)) {
                    SharedPreferences.Editor editor = getActivity().getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE).edit();
                    editor.putBoolean(TaskyConstants.PREFS_FIRST_RUN, true);
                    editor.apply();
                    Toast.makeText(getActivity(), R.string.scheduler_restarted, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.please_restart_widget_to_complete, Toast.LENGTH_LONG).show();
                }
                return false;
            }
        });
    }
}
