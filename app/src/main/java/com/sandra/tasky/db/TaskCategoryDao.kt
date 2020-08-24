package com.sandra.tasky.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.sandra.tasky.entity.TaskCategory

@Dao
interface TaskCategoryDao {

    @Query("SELECT * FROM categoriesTable")
    fun getAll(): List<TaskCategory>

    @Query("SELECT * FROM categoriesTable where :id=ID")
    fun geyById(id: Long): TaskCategory

    @Insert
    fun insertAll(vararg category: TaskCategory)

    @Delete
    fun delete(category: TaskCategory)

    @Query("DELETE FROM categoriesTable")
    fun deleteAll()

}