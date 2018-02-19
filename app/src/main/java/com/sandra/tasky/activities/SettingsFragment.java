package com.sandra.tasky.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;


public class SettingsFragment extends PreferenceFragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        final boolean isFirstRun = getActivity()
                .getSharedPreferences(TaskyConstants.WIDGET_FIRST_RUN, Context.MODE_PRIVATE)
                .getBoolean(TaskyConstants.PREFS_FIRST_RUN, true);

        Preference preference = findPreference(getString(R.string.pref_restart_scheduler_key));
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

        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();
        Preference p = findPreference(getString(R.string.pref_time_span_key));
        String prefValue = sharedPreferences.getString(p.getKey(), "");
        setPreferenceSummary(p, prefValue);

        //open in google play
        Preference pGooglePlay = findPreference(getString(R.string.pref_view_in_google_play_key));
        pGooglePlay.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                final String appPackageName = getActivity().getApplicationContext().getPackageName();
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_market) + appPackageName)));
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_url) + appPackageName)));
                }
                return true;
            }
        });


        //set version
        Preference pVersion = findPreference(getString(R.string.pref_version_key));
        try {
            pVersion.setSummary(getActivity().getApplicationContext().getPackageManager().getPackageInfo
                    (getActivity().getApplicationContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

    }

    private void setPreferenceSummary(Preference preference, String value) {
        if (preference instanceof ListPreference) {
            // For list preferences, figure out the label of the selected value
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(value);
            if (prefIndex >= 0) {
                // Set the summary to that label
                listPreference.setSummary(
                        prefIndex == listPreference.getEntries().length - 1 ?
                                listPreference.getEntries()[prefIndex]
                                : listPreference.getEntries()[prefIndex] + " " + getString(R.string.in_advance));
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        if (p != null && p instanceof ListPreference) {
            setPreferenceSummary(p, sharedPreferences.getString(key, ""));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
