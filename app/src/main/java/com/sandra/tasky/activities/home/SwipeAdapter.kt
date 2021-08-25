package com.sandra.tasky.activities.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sandra.tasky.activities.home.list.TasksFragment

class SwipeAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    override fun createFragment(position: Int): Fragment {
        return if (position == TASKS_FRAGMENT_POSITION) {
            TasksFragment()
        } else {
            CalendarFragment()
        }
    }

    override fun getItemCount() = TOTAL_PAGES

    companion object {
        const val TOTAL_PAGES = 2
        const val TASKS_FRAGMENT_POSITION = 0
        const val CALENDAR_FRAGMENT_POSITION = 1
    }

}