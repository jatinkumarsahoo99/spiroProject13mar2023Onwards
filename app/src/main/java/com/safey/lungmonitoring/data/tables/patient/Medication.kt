package com.safey.lungmonitoring.data.tables.patient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity(tableName = "Medication")
data class Medication(
    var medicationName: String? = null,
    var freqType: Int = 0,
    var noOfTimes: Int = 0,
    var days: String? = null,
    var medColor:String? = null,
    var medIcon:String? = null
){
    @PrimaryKey
    @ColumnInfo(name = "medication_guid")
    @NotNull
    var guid: String = UUID.randomUUID().toString()

    @ColumnInfo(name = "creation_date")
    var creationDate: Long = 0L

    @ColumnInfo(name = "modification_date")
    var modificationDate: Long = 0L

}