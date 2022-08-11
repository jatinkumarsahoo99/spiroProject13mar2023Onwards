package com.safey.lungmonitoring.data.datautils

import androidx.room.TypeConverter
import java.util.*

class DateConverter {
     @TypeConverter
    fun fromTimestamp(TestDateTime: Long?): Date? {
        return if (TestDateTime == null) null else Date(TestDateTime)
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}