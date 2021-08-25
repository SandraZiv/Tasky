package com.sandra.tasky.db

import android.content.Context
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.entity.TaskCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

object DatabaseWrapper {

    // Task methods
    fun getTasks(context: Context) = flow<List<SimpleTask>> {
        val db = TaskDatabase(context)
        while(true) {
            emit(db.allTasks)
            delay(5000)
        }
    }

    suspend fun addTask(context: Context, task: SimpleTask) = withContext(Dispatchers.IO) {
        TaskDatabase(context).addTask(task)
    }

    suspend fun updateTask(context: Context, task: SimpleTask) = withContext(Dispatchers.IO) {
        TaskDatabase(context).updateTask(task)
    }

    suspend fun deleteTask(context: Context, task: SimpleTask) = withContext(Dispatchers.IO) {
        TaskDatabase(context).deleteTask(task)
    }

    suspend fun deleteAllTasks(context: Context) = withContext(Dispatchers.IO) {
        TaskDatabase(context).deleteAllTasks()
    }

    suspend fun deleteAllTasksInCategory(context: Context, taskIds: IntArray) = withContext(Dispatchers.IO) {
        TaskDatabase(context).deleteAllTasksInCategory(taskIds)
    }


    // Category methods
    suspend fun addCategory(context: Context, category: TaskCategory) = withContext(Dispatchers.IO) {
        TaskDatabase(context).addCategory(category)
    }

    suspend fun deleteCategory(context: Context, category: TaskCategory) = withContext(Dispatchers.IO) {
        TaskDatabase(context).deleteCategory(category)
    }

    suspend fun getAllCategories(context: Context): List<TaskCategory> = withContext(Dispatchers.IO) {
        return@withContext TaskDatabase(context).allCategories
    }
}