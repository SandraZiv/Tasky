package com.sandra.tasky.settings

import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sandra.tasky.BuildConfig
import com.sandra.tasky.R
import com.sandra.tasky.utils.NotificationUtils
import com.sandra.tasky.utils.ToastWrapper

@Keep
class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        setHasOptionsMenu(true)

        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        setupScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        val p = findPreference<Preference>(key) ?: return
        when {
            p is ListPreference -> {
                setPreferenceSummary(p, sharedPreferences.getString(key, "")!!)
            }
            key == AppSettings.PREF_SHOULD_SHOW_NOTIFICATION_KEY -> {
                enableVibrateAndSound(sharedPreferences, key)
                cancelNotifications(sharedPreferences, key)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.settings_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_reset) {
            resetSettings()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupScreen() {
        // todo use this or other shared preferences
        setTimeSpan(preferenceScreen.sharedPreferences)
        setGooglePlayPreference()
        setAppVersion()
    }

    private fun enableVibrateAndSound(sharedPreferences: SharedPreferences, notificationPrefKey: String) {
        val vibrate = findPreference(AppSettings.PREF_SHOULD_NOTIFICATION_VIBRATE) as CheckBoxPreference?
        val sound = findPreference(AppSettings.PREF_SHOULD_NOTIFICATION_HAVE_SOUND) as CheckBoxPreference?
        val enabled = sharedPreferences.getBoolean(notificationPrefKey, resources.getBoolean(R.bool.pref_show_notifications_default))
        vibrate!!.isEnabled = enabled
        sound!!.isEnabled = enabled
    }

    private fun setTimeSpan(sharedPreferences: SharedPreferences) {
        val p = findPreference<ListPreference>(AppSettings.PREF_WIDGET_TIME_SPAN)!!
        val prefValue = sharedPreferences.getString(p.key, "")!!
        setPreferenceSummary(p, prefValue)
    }

    private fun setGooglePlayPreference() {
        val pGooglePlay = findPreference<Preference>(AppSettings.PREF_APP_ON_GOOGLE_PLAY)!!
        pGooglePlay.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val appPackageName = BuildConfig.APPLICATION_ID
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_market) + appPackageName)))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.google_play_url) + appPackageName)))
            }
            true
        }
    }

    private fun setAppVersion() {
        findPreference<Preference>(AppSettings.PREF_APP_VERSION)!!.summary = BuildConfig.VERSION_NAME
    }

    private fun cancelNotifications(sharedPreferences: SharedPreferences, key: String) {
        val showNotifications = sharedPreferences
                .getBoolean(key, resources.getBoolean(R.bool.pref_show_notifications_default))
        if (!showNotifications) {
            NotificationUtils.cancelAllNotifications(context)
        }
    }

    private fun resetSettings() {
        preferenceScreen.sharedPreferences
                .edit()
                .clear()
                .apply()

        preferenceScreen = null
        addPreferencesFromResource(R.xml.preferences)
        setupScreen()

        ToastWrapper.showShort(requireContext(), R.string.settings_reset)
    }

    private fun setPreferenceSummary(preference: Preference, value: String) {
        if (preference is ListPreference) {
            // For list preferences, figure out the label of the selected value
            val prefIndex = preference.findIndexOfValue(value)
            if (prefIndex >= 0) {
                // Set the summary to that label
                preference.summary = if (prefIndex == preference.entries.size - 1) preference.entries[prefIndex] else preference.entries[prefIndex].toString() + " " + getString(R.string.in_advance)
            }
        }
    }

}