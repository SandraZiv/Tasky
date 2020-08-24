package com.sandra.tasky

enum class RepeatType(val value: Int) {

    REPEAT_ONCE(0),
    REPEAT_DAY(1),
    REPEAT_WEEK(2),
    REPEAT_MONTH(3),
    REPEAT_YEAR(4);

    companion object {
        fun getDefault() = REPEAT_ONCE

        fun getByValue(value: Int): RepeatType {
            for (repeatType in values()) {
                if (repeatType.value == value) {
                    return repeatType
                }
            }
            return getDefault()
        }
    }

}