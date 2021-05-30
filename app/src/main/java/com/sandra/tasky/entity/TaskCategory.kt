package com.sandra.tasky.entity

import android.content.Context
import com.sandra.tasky.R
import java.io.Serializable

// todo how to use ID fro DB?
data class TaskCategory(var id: Int = DEFAULT_CATEGORY_ID, var title: String) : Serializable {

    companion object {
        // todo check for compatibility with v1
        private const val DEFAULT_CATEGORY_ID = -1

        fun createDefaultCategory(context: Context, hasOtherCategories: Boolean): TaskCategory {
            val titleId = if (hasOtherCategories) R.string.others else R.string.all
            return TaskCategory(DEFAULT_CATEGORY_ID, context.getString(titleId))
        }
    }
}