package com.sandra.tasky.settings

import android.content.*
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import android.net.Uri
import android.os.Bundle
import androidx.annotation.Keep
import androidx.preference.CheckBoxPreference
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.sandra.tasky.BuildConfig
import com.sandra.tasky.R
import com.sandra.tasky.utils.NotificationUtils

@Keep
class SettingsFragment : PreferenceFragmentCompat(), OnSharedPreferenceChangeListener {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.preferences)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        setupScreen()
    }

    override fun onDestroy() {
        super.onDestroy()
        preferenceScreen.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String) {
        when (key) {
            AppSettings.PREF_WIDGET_TIME_SPAN -> {
                setTimeSpan()
            }
            AppSettings.PREF_SHOULD_SHOW_NOTIFICATION_KEY -> {
                enableVibrateAndSound()
                cancelNotifications()
            }
        }
    }

    private fun enableVibrateAndSound() {
        val vibrate =
            findPreference(AppSettings.PREF_SHOULD_NOTIFICATION_VIBRATE) as CheckBoxPreference?
        val sound =
            findPreference(AppSettings.PREF_SHOULD_NOTIFICATION_HAVE_SOUND) as CheckBoxPreference?
        val enabled = AppSettings.shouldShowNotifications(requireContext())

        vibrate!!.isEnabled = enabled
        sound!!.isEnabled = enabled
    }

    private fun cancelNotifications() {
        if (!AppSettings.shouldShowNotifications(requireContext())) {
            NotificationUtils.cancelAllNotifications(context)
        }
    }

    private fun setupScreen() {
        setTimeSpan()
        setGooglePlayPreference()
        setAppVersion()
    }

    private fun setTimeSpan() {
        val preference = findPreference<ListPreference>(AppSettings.PREF_WIDGET_TIME_SPAN) ?: return
        val prefIndex = preference.findIndexOfValue(AppSettings.getWidgetTimeSpan(requireContext()))
        if (prefIndex >= 0) {
            val entryText = preference.entries[prefIndex]
            preference.summary =
                if (prefIndex == preference.entries.size - 1) entryText
                else getString(R.string.in_advance, entryText)
        }
    }

    private fun setGooglePlayPreference() {
        val pGooglePlay = findPreference<Preference>(AppSettings.PREF_APP_ON_GOOGLE_PLAY)!!
        pGooglePlay.onPreferenceClickListener = Preference.OnPreferenceClickListener {
            val appPackageName = BuildConfig.APPLICATION_ID
            try {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.google_play_market) + appPackageName)
                    )
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(getString(R.string.google_play_url) + appPackageName)
                    )
                )
            }
            true
        }
    }

    private fun setAppVersion() {
        findPreference<Preference>(AppSettings.PREF_APP_VERSION)!!.summary =
            BuildConfig.VERSION_NAME
    }

}