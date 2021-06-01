package com.sandra.tasky

import android.content.Context

object AppPrefs {

    fun getSortingCriteria(context: Context) = getGeneralPreferences(context)
        .getInt(PREFS_SORT_CRITERIA_KEY, SortType.getDefault().value)

    fun updateSortingCriteria(context: Context, sortCriteria: Int) = getGeneralPreferences(context)
        .edit()
        .putInt(PREFS_SORT_CRITERIA_KEY, sortCriteria)
        .apply()

    fun isWidgetEnabled(context: Context) = getWidgetPreferences(context)
        .getBoolean(PREFS_IS_WIDGET_ENABLED_KEY, false)

    fun setWidgetEnabled(context: Context, isEnabled: Boolean) = getWidgetPreferences(context)
        .edit()
        .putBoolean(PREFS_IS_WIDGET_ENABLED_KEY, isEnabled)
        .apply()

    private fun getWidgetPreferences(context: Context) =
        context.getSharedPreferences(WIDGET_PREF, Context.MODE_PRIVATE)

    private fun getGeneralPreferences(context: Context) =
        context.getSharedPreferences(PREF_GENERAL, Context.MODE_PRIVATE)


    private const val PREF_GENERAL = "PREF_GENERAL"
    private const val PREFS_SORT_CRITERIA_KEY = "PREF_SORT"

    private const val WIDGET_PREF = "first_run_of_widget_1.19"
    private const val PREFS_IS_WIDGET_ENABLED_KEY = "PREFS_IS_WIDGET_ENABLED"
}