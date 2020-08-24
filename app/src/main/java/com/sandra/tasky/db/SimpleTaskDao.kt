package com.sandra.tasky.db

import androidx.room.*
import com.sandra.tasky.entity.SimpleTask

@Dao
interface SimpleTaskDao {

    @Query("SELECT * FROM taskTable")
    fun getAll(): List<SimpleTask>

    @Query("SELECT * FROM taskTable where :id=ID")
    fun getById(id: Int): SimpleTask

    @Query("SELECT * FROM taskTable where SHOW_IN_WIDGET_COLUMN=1 ORDER BY TASK_DATE_COLUMN")
    fun getWidgetTasks(): List<SimpleTask>
    // exipred and time span logic do in code

    @Insert
    fun insertAll(vararg task: SimpleTask)

    @Update
    fun update(task: SimpleTask)

    @Delete
    fun delete(task: SimpleTask)

    @Query("DELETE FROM taskTable")
    fun deleteAll()

}