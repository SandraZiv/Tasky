package com.sandra.tasky.activities.home

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.sandra.tasky.R
import com.sandra.tasky.activities.home.TasksFragment.Companion.TOTAL_PAGES
import com.sandra.tasky.activities.home.TasksFragment.Companion.PAGE_FIRST

// todo refactor to something that is not deprecated
class SwipeAdapter(fragmentManager: FragmentManager, val context: Context) : androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = TasksFragment.createFragment(position)

    override fun getPageTitle(position: Int): CharSequence =
            if (position == PAGE_FIRST) context.getString(R.string.tasks)
            else context.getString(R.string.calendar)

    override fun getCount(): Int = TOTAL_PAGES

}