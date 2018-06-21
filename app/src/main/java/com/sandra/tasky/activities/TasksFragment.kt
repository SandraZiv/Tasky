package com.sandra.tasky.activities

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sandra.tasky.R

const val PAGE_NUMBER = "page_number"
const val PAGE_FIRST = 0
const val PAGES = 2

class TasksFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val page = arguments?.getInt(PAGE_NUMBER)

        return if (page == PAGE_FIRST) createTasks(inflater, container)
        else createCalendar(inflater, container)
    }

    private fun createTasks(inflater: LayoutInflater?, container: ViewGroup?): View? {
        val view = inflater?.inflate(R.layout.fragment_tasks, container, false)
        (activity as HomeScreenActivity).initTasksList(view)
        return view
    }

    private fun createCalendar(inflater: LayoutInflater?, container: ViewGroup?): View? {
        val view = inflater?.inflate(R.layout.fragment_calendar, container, false)
        (activity as HomeScreenActivity).initCalendarList(view)
        return view
    }

    companion object {
        fun createFragment(position: Int): TasksFragment {
            val fragment = TasksFragment()
            val bundle = Bundle()
            bundle.putInt(PAGE_NUMBER, position)
            fragment.arguments = bundle
            return fragment
        }
    }
}