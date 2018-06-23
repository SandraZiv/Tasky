package com.sandra.tasky.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.sandra.tasky.R
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import kotlinx.android.synthetic.main.content_item_task.view.*

class CalendarEventAdapter(val context: Context, val list: List<SimpleTask>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val task = getItem(position)
        val view = LayoutInflater
                .from(context)
                .inflate(R.layout.item_calendar_event, parent, false)

        view.tv_title.text = task.title

        view.cb_completed.isChecked = task.isCompleted
        view.cb_completed.setOnClickListener {
            task.isCompleted = it.cb_completed.isChecked
            val db = TaskDatabase(context)
            db.updateTask(task)
            notifyDataSetChanged()
        }

        if (task.note.isEmpty()) {
            view.tv_note.visibility = View.GONE
        } else {
            view.tv_note.text = task.note
        }

        if (!task.isTimePresent) {
            view.tv_due_date.visibility = View.GONE
        } else {
            view.tv_due_date.text = task.parseTime()
        }

        return view
    }

    override fun getItem(position: Int) = list[position]

    override fun getItemId(position: Int) = list[position].id.toLong()

    override fun getCount() = list.size

}