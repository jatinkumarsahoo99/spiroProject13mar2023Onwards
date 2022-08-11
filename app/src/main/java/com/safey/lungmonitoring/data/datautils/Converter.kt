package com.safey.lungmonitoring.data.datautils

import android.util.Log
import androidx.room.TypeConverter
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class Converter {
    private val TAG: String = "Converter"

    // Set timezone value as GMT 존맛탱... to make time as reasonable
    var df: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss'Z'", Locale.getDefault())
    @TypeConverter
    public fun timeToDate(value: String?): Date? {
        return if (value != null) {
            try {
                return df.parse(value)
            } catch (e: ParseException) {
                Log.e(TAG, e.message!!)
            }
            null
        } else {
            null
        }
    }

    @TypeConverter
    fun dateToTime(value: Date?): String? {
        return if (value != null) {
            df.format(value)
        } else {
            null
        }
    }

    init {
        df.setTimeZone(TimeZone.getTimeZone("GMT"))
    }
}