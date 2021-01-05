package com.sandra.tasky.entity

import java.io.Serializable


data class TaskCategory(
        var id: Int = OTHERS_CATEGORY_ID,
        var title: String
) : Serializable {

    companion object {
        const val ALL_CATEGORY_ID = -1
        const val OTHERS_CATEGORY_ID = -2
    }
}