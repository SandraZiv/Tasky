package com.sandra.tasky.adapter

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import com.sandra.tasky.R
import com.sandra.tasky.activities.PAGES
import com.sandra.tasky.activities.PAGE_FIRST
import com.sandra.tasky.activities.TasksFragment

class SwipeAdapter(fragmentManager: FragmentManager, val context: Context) : FragmentPagerAdapter(fragmentManager) {

    override fun getItem(position: Int): Fragment = TasksFragment.createFragment(position)

    override fun getPageTitle(position: Int): CharSequence =
            if (position == PAGE_FIRST) context.getString(R.string.tasks)
            else context.getString(R.string.calendar)

    override fun getCount(): Int = PAGES

}