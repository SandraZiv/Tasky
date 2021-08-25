package com.sandra.tasky.activities.home.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sandra.tasky.databinding.ItemTaskBinding
import com.sandra.tasky.entity.SimpleTask

class TasksAdapter(
    private val tasks: List<SimpleTask>,
    private val taskItemListener: TasksViewHolder.TaskItemListener  // todo don't expose this
) : RecyclerView.Adapter<TasksViewHolder>() {

    // todo anims

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TasksViewHolder {
        val view = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TasksViewHolder(view, taskItemListener)
    }

    override fun onBindViewHolder(holder: TasksViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount() = tasks.size

}