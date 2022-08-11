package com.safey.lungmonitoring.data.tables.patient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.jetbrains.annotations.NotNull


import java.util.*
import kotlin.collections.ArrayList

@Entity(tableName = "TestResult")
data class AirTestResult (
    var trialResult: List<TrialResult>? = ArrayList(),
    var type: Int? = null,
    var testtype: Int? = null,
    var createdAt: Date? = null,
    var createdDate: String? = null,
    var testtime: String? = null,
    var active: Boolean? = null,
    var sessionScore: String? = null,
    var variance: List<Variance>? =ArrayList(),
    var userId:String?=null,
    var fname:String? ="jks",
    var gender:Int?=null,
    var height:String?=null,
    var dob:Long = 0L,
    var age:String = ""
){
    @PrimaryKey
    @ColumnInfo(name = "testresult_guid")
    @NotNull
    var guid: String = UUID.randomUUID().toString()

    @ColumnInfo(name = "creation_date")
    var creationDate: Long = 0L

    @ColumnInfo(name = "modification_date")
    var modificationDate: Long = 0L

}

class Converters{
    @TypeConverter
    fun fromDeveloperList(countryLang: List<TrialResult?>?): String? {
        val type = object : TypeToken<List<TrialResult>>() {}.type
        return Gson().toJson(countryLang, type)
    }
    @TypeConverter
    fun toDeveloperList(countryLangString: String?): List<TrialResult>? {
        val type = object : TypeToken<List<TrialResult>>() {}.type
        return Gson().fromJson<List<TrialResult>>(countryLangString, type)
    }

    @TypeConverter
    fun fromVarianceList(countryLang: List<Variance?>?): String? {
        val type = object : TypeToken<List<Variance>>() {}.type
        return Gson().toJson(countryLang, type)
    }
    @TypeConverter
    fun toVarianceList(countryLangString: String?): List<Variance>? {
        val type = object : TypeToken<List<Variance>>() {}.type
        return Gson().fromJson<List<Variance>>(countryLangString, type)
    }

    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time?.toLong()
    }

    @TypeConverter
    fun fromString(stringListString: String): List<String> {
        return stringListString.split(",").map { it }
    }

    @TypeConverter
    fun toString(stringList: List<String>): String {
        return stringList.joinToString(separator = ",")
    }
}





