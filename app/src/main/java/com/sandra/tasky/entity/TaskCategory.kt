package com.sandra.tasky.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(
        tableName = "categoriesTable",
        indices = [Index(value = ["CATEGORIES_TITLE"], unique = true)]
)
data class TaskCategory(
        @ColumnInfo(name="CATEGORIES_TITLE") var title: String,
        @PrimaryKey(autoGenerate = true) @ColumnInfo(name="ID") var id: Int = OTHERS_CATEGORY_ID
) : Serializable {

    companion object {
        const val ALL_CATEGORY_ID = -1
        const val OTHERS_CATEGORY_ID = -2
    }
}