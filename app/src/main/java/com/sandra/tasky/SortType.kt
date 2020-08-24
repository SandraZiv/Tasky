package com.sandra.tasky

enum class SortType(val value: Int) {

    SORT_DUE_DATE(0),
    SORT_TITLE(1),
    SORT_COMPLETED(2);

    companion object {
        fun getDefault() = SORT_DUE_DATE
    }

}