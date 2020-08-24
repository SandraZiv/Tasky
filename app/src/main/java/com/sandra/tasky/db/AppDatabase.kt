package com.sandra.tasky.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.sandra.tasky.entity.SimpleTask
import com.sandra.tasky.entity.TaskCategory


// version<100 reserved for sqlite in case there are problems with room
@Database(
        entities = [SimpleTask::class, TaskCategory::class],
        version = 100
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun taskDao(): SimpleTaskDao
    abstract fun categoriesDao(): TaskCategoryDao

    companion object {
        fun buildDatabase(context: Context): AppDatabase =
                Room.databaseBuilder(context, AppDatabase::class.java, "task_database.db")
                        .addMigrations(object : Migration(12, 100) {
                            override fun migrate(database: SupportSQLiteDatabase) {
                                // todo any table alterations go here
                            }
                        })
                        .build()
    }
}
