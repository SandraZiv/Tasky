package com.sandra.tasky.activities.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.sandra.tasky.R
import com.sandra.tasky.TaskyConstants
import com.sandra.tasky.db.DatabaseWrapper
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.hide
import kotlinx.android.synthetic.main.item_task.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HomeListAdapter(private val context: Context, private val taskList: List<SimpleTask>) : BaseAdapter() {

    override fun getCount(): Int {
        return taskList.size
    }

    override fun getItem(position: Int): Any {
        return taskList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        val task = taskList[position]
        val itemView = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false)
        itemView.tvTitle.text = cutText(task.title, TaskyConstants.MAX_TITLE_LENGTH)
        val checkBox = itemView.cbCompleted
        checkBox.isChecked = task.isCompleted
        checkBox.setOnClickListener {
            task.isCompleted = checkBox.isChecked
            // todo
            CoroutineScope(Dispatchers.Main).launch {
                DatabaseWrapper.updateTask(context, task)
                notifyDataSetChanged()
            }
        }
        if (task.note.isNotEmpty()) {
            itemView.tvNote.text = cutText(task.note, TaskyConstants.MAX_TEXT_LENGTH)
        } else {
            itemView.tvNote.hide()
        }
        if (task.dueDate != null) {
            if (task.isExpired) {
                itemView.tvDueDate.text = context.getString(R.string.expired).capitalize()
            } else {
                itemView.tvDueDate.text = if (task.isTimePresent) task.parseDateTime() else task.parseDate()
            }
        } else {
            itemView.tvDueDate.hide()
        }
        return itemView
    }

    private fun cutText(text: String?, limit: Int): String {
        return (if (text!!.length > limit) text.substring(0, limit) + "..." else text)
    }

}