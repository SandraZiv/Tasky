package com.sandra.tasky.activities;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.SwitchPreferenceCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.sandra.tasky.R;
import com.sandra.tasky.TaskyConstants;
import com.sandra.tasky.utils.NotificationUtils;
import com.sandra.tasky.utils.TaskyUtils;


public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {

    private Toast mToast;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.preferences);
        setHasOptionsMenu(true);

        //init listener
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        //setting preferences
        SharedPreferences sharedPreferences = getPreferenceScreen().getSharedPreferences();

        enableVibrateAndSound(sharedPreferences, getString(R.string.pref_show_notifications_key));
        setWidgetUpdater();
        setTimeSpan(sharedPreferences);
        setGooglePlayPreference();
        setAppVersion();

    }

    private void enableVibrateAndSound(SharedPreferences sharedPreferences, String notificationPrefKey) {
        CheckBoxPreference vibrate = (CheckBoxPreference) findPreference(getString(R.string.pref_vibrate_key));
        CheckBoxPreference sound = (CheckBoxPreference) findPreference(getString(R.string.pref_sound_key));

        boolean enabled = sharedPreferences.getBoolean(notificationPrefKey, getResources().getBoolean(R.bool.pref_show_notifications_default));
        vibrate.setEnabled(enabled);
        sound.setEnabled(enabled);
    }

    private void setTimeSpan(SharedPreferences sharedPreferences) {
        Preference p = findPreference(getString(R.string.pref_time_span_key));
        String prefValue = sharedPreferences.getString(p.getKey(), "");
        setPreferenceSummary(p, prefValue);
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

    private void setWidgetUpdater() {
        Preference preference = findPreference(getString(R.string.pref_restart_scheduler_key));

        final SharedPreferences sharedPreferences = getActivity().getSharedPreferences(TaskyConstants.WIDGET_PREF, Context.MODE_PRIVATE);
        
        if (!sharedPreferences.getBoolean(TaskyConstants.PREFS_IS_WIDGET_ENABLED, TaskyConstants.WIDGET_DEFAULT)) {
            preference.setSummary(getString(R.string.widget_not_set));
            preference.setEnabled(false);
            return;
        }

        preference.setSummary(sharedPreferences.getString(TaskyConstants.PREFS_LAST_UPDATE, getString(R.string.scheduler_running)));

        preference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                preference.setSummary(getString(R.string.scheduler_restarted));
                mToast = TaskyUtils.addToast(mToast, getActivity(), R.string.scheduler_restarted, true);
                TaskyUtils.setMidnightUpdater(getContext());

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(TaskyConstants.PREFS_LAST_UPDATE, getString(R.string.scheduler_running));
                editor.apply();

                return true;
            }
        });
    }

    private void setGooglePlayPreference() {
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
    }

    private void setAppVersion() {
        Preference pVersion = findPreference(getString(R.string.pref_version_key));
        try {
            pVersion.setSummary(getActivity().getApplicationContext().getPackageManager().getPackageInfo
                    (getActivity().getApplicationContext().getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void cancelNotifications(SharedPreferences sharedPreferences, String key) {
        boolean showNotifications = sharedPreferences
                .getBoolean(key, getResources().getBoolean(R.bool.pref_show_notifications_default));

        if (!showNotifications) {
            NotificationUtils.cancelAllNotifications(getContext());
        }

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference p = findPreference(key);
        if (p != null && p instanceof ListPreference) {
            setPreferenceSummary(p, sharedPreferences.getString(key, ""));
        } else if (p != null && key.equals(getString(R.string.pref_show_notifications_key))) {
            enableVibrateAndSound(sharedPreferences, key);
            cancelNotifications(sharedPreferences, key);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.settings_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_reset) {
            resetSettings();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void resetSettings() {
        SharedPreferences preferences = getPreferenceScreen().getSharedPreferences();
        SharedPreferences.Editor editor = preferences.edit();

        editor.clear();
        editor.apply();

        SwitchPreferenceCompat showNotifications = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_show_notifications_key));
        showNotifications.setChecked(getResources().getBoolean(R.bool.pref_show_notifications_default));

        CheckBoxPreference vibrate = (CheckBoxPreference) findPreference(getString(R.string.pref_vibrate_key));
        vibrate.setChecked(getResources().getBoolean(R.bool.pref_vibrate_default));

        CheckBoxPreference sound = (CheckBoxPreference) findPreference(getString(R.string.pref_sound_key));
        sound.setChecked(getResources().getBoolean(R.bool.pref_sound_default));

        SwitchPreferenceCompat showExpired = (SwitchPreferenceCompat) findPreference(getString(R.string.pref_show_expired_key));
        showExpired.setChecked(getResources().getBoolean(R.bool.pref_show_expired_default));

        Preference timeSpan = findPreference(getString(R.string.pref_time_span_key));
        setPreferenceSummary(timeSpan, getString(R.string.pref_time_span_default));

        mToast = TaskyUtils.addToast(mToast, getActivity().getApplicationContext(), R.string.settings_reset, true);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}
