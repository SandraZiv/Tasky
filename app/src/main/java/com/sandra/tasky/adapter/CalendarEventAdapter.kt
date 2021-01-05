package com.sandra.tasky.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.sandra.tasky.R
import com.sandra.tasky.db.TaskDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.hide
import kotlinx.android.synthetic.main.item_task.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CalendarEventAdapter(val context: Context, val list: List<SimpleTask>) : BaseAdapter() {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val task = getItem(position)
        val view = LayoutInflater
                .from(context)
                .inflate(R.layout.item_task, parent, false)

        view.tvTitle.text = task.title

        view.cbCompleted.isChecked = task.isCompleted
        view.cbCompleted.setOnClickListener {
            task.isCompleted = it.cbCompleted.isChecked
            // todo
            CoroutineScope(Dispatchers.IO).launch {
                TaskDatabase(context).updateTask(task)
                withContext(Dispatchers.Main) {
                    notifyDataSetChanged()
                }
            }
        }

        if (task.note.isEmpty()) {
            view.tvNote.hide()
        } else {
            view.tvNote.text = task.note
        }

        if (!task.isTimePresent) {
            view.tvDueDate.hide()
        } else {
            view.tvDueDate.text = task.parseTime()
        }

        return view
    }

    override fun getItem(position: Int) = list[position]

    override fun getItemId(position: Int) = list[position].id.toLong()

    override fun getCount() = list.size

}