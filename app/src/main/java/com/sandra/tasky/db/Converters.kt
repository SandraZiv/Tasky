package com.sandra.tasky.db

import androidx.room.TypeConverter
import com.sandra.tasky.RepeatType
import org.joda.time.DateTime
import java.sql.Timestamp
import java.text.SimpleDateFormat
import java.util.*

class Converters {

    @TypeConverter
    fun fromInt(value: Int): RepeatType {
        return RepeatType.getByValue(value)
    }

    @TypeConverter
    fun repeatTypeToInt(value: RepeatType): Int {
        return value.value
    }

    @TypeConverter
    fun fromTimestamp(value: String): DateTime {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        val date = dateFormat.parse(value)
        return DateTime(date)
    }

    @TypeConverter
    fun dateTimeToTimestamp(value: DateTime): String {
        return Timestamp(value.millis).toString()
    }
}