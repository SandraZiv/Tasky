package com.sandra.tasky.db

import android.content.Context
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.entity.TaskCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object DatabaseWrapper {

    suspend fun addTask(context: Context, task: SimpleTask) = withContext(Dispatchers.IO) {
        TaskDatabase(context).addTask(task)
    }

    suspend fun updateTask(context: Context, task: SimpleTask) = withContext(Dispatchers.IO) {
        TaskDatabase(context).updateTask(task)
    }

    suspend fun deleteTask(context: Context, task: SimpleTask) = withContext(Dispatchers.IO) {
        TaskDatabase(context).deleteTask(task)
    }

    // todo check if constantly initing db is ok?

    suspend fun getAllCategories(context: Context): List<TaskCategory> = withContext(Dispatchers.IO) {
        return@withContext TaskDatabase(context).allCategories
    }
}