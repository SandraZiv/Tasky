package com.sandra.tasky.activities.home.list

import androidx.recyclerview.widget.RecyclerView
import com.sandra.tasky.R
import com.sandra.tasky.databinding.ItemTaskBinding
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.utils.capitalFirstLetter
import com.sandra.tasky.utils.hide
import com.sandra.tasky.utils.show

class TasksViewHolder(private val binding: ItemTaskBinding, private val taskItemListener: TaskItemListener) :
    RecyclerView.ViewHolder(binding.root) {

    fun bind(task: SimpleTask) {
        binding.tvTitle.text = cutText(task.title, MAX_TITLE_LENGTH)

        binding.cbCompleted.apply {
            isChecked = task.isCompleted
            setOnClickListener { taskItemListener.onTaskChecked(task, isChecked) }
        }

        binding.tvNote.apply {
            if (task.note.isNotEmpty()) {
                text = cutText(task.note, MAX_TEXT_LENGTH)
                show()
            } else {
                hide()
            }
        }

        binding.tvDueDate.apply {
            if (task.dueDate != null) {
                text = if (task.isExpired) {
                    itemView.context.getString(R.string.expired).capitalFirstLetter()
                } else {
                    // todo extract this logic to task so there is just one method
                    if (task.isTimePresent) task.parseDateTime() else task.parseDate()
                }
                show()
            } else {
                hide()
            }
        }

        binding.root.setOnClickListener { taskItemListener.onTaskClicked(task) }
        binding.root.setOnLongClickListener {
            taskItemListener.onTaskLongClicked(task)
            true
        }

    }

    private fun cutText(text: String?, limit: Int): String {
        return (if (text!!.length > limit) text.substring(0, limit) + "..." else text)
    }

    interface TaskItemListener {
        fun onTaskClicked(task: SimpleTask)
        fun onTaskLongClicked(task: SimpleTask)
        fun onTaskChecked(task: SimpleTask, isChecked: Boolean)
    }

    companion object {
        private const val MAX_TITLE_LENGTH = 70
        private const val MAX_TEXT_LENGTH = 100
    }

}