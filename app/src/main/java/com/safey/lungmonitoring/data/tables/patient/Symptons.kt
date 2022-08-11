package com.safey.lungmonitoring.data.tables.patient

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import java.util.*

@Entity(tableName = "Symptoms")
data class Symptoms(
    var symptomsNames: List<String> = ArrayList(),
    var symptomsIcons: List<String> = ArrayList(),
    var createdDate: String? = null,
    var symptomtime: String? = null
){
    @PrimaryKey
    @ColumnInfo(name = "symptom_guid")
    @NotNull
    var guid: String = UUID.randomUUID().toString()

    @ColumnInfo(name = "creation_date")
    var creationDate: Long = 0L

    @ColumnInfo(name = "modification_date")
    var modificationDate: Long = 0L

}

