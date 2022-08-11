package com.safey.lungmonitoring.data.tables.patient

/*@Entity(tableName = "TrialResult", foreignKeys = [
ForeignKey(entity = TestResult::class,
parentColumns = arrayOf("testresult_guid"),
childColumns = arrayOf("trialResult_TestResultId"),
onDelete = CASCADE
)])*/
data class Variance(var measurement : String,var measurementValue : String,var percentage : String)
data class TrialResult (
    var graphDataList: List<AirGraphData>? = null,
    var mesurementlist: List<TestMeasurements>? = null,
    var isBest:Boolean = false,
    var isPost:Boolean = false
) /*{
    @PrimaryKey
    @ColumnInfo(name = "trialresult_guid")
    @NotNull
    var guid: String = UUID.randomUUID().toString()

    @ColumnInfo(name = "trialResult_TestResultId")
    var TestResultId: String = ""

    @ColumnInfo(name = "creation_date")
    var creationDate: Long = 0L

    @ColumnInfo(name = "modification_date")
    var modificationDate: Long = 0L

}*/