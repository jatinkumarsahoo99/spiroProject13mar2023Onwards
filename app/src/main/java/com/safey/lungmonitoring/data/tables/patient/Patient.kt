package com.safey.lungmonitoring.data.tables.patient

import androidx.room.*
import com.safey.lungmonitoring.data.datautils.DateConverter
import org.jetbrains.annotations.NotNull
import java.util.*


@Entity(tableName = "Patient")
data class Patient constructor(
    var FirstName: String = "",
    var LastName: String = "",
    var Gender: Int = 0,
    var Height: String = "",
    var ethnicity:Int = 0,
    var avatar : String = "",
    var HeightUnit : Int = 0,
    var UHID :String = ""
) {

    @PrimaryKey
    @ColumnInfo(name = "patient_guid")
    @NotNull
    var guid: String = UUID.randomUUID().toString()

    @ColumnInfo(name = "creation_date")
    var creationDate: Long = 0L

    @ColumnInfo(name = "modification_date")
    var modificationDate: Long = 0L

    @TypeConverters(DateConverter::class)
    @ColumnInfo(name = "birthdate")
    var BirthDate: Long = 0L

    @Ignore
    var avatarResourceId : Int = 0

}