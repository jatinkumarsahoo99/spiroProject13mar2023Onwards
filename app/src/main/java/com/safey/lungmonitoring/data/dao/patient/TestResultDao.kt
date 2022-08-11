package com.safey.lungmonitoring.data.dao.patient

import androidx.room.*
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import io.reactivex.Single
import java.util.*

@Dao
interface TestResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(airTestResult: AirTestResult)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(airTestResult: AirTestResult)

    @Query("SELECT * FROM TestResult ORDER BY creation_date DESC LIMIT 1;")
    fun getLastTestResultAdded(): Single<AirTestResult>

    @Query("SELECT * FROM TestResult ORDER BY modification_date DESC LIMIT 1;")
    fun getLastTestResultUpdated(): Single<AirTestResult>

    @Query("SELECT * FROM TestResult")
    fun getTestResult(): Single<List<AirTestResult>>

    @Query("SELECT * FROM TestResult where createdAt BETWEEN :fdate AND :tdate ORDER BY createdAt ASC ")
    fun getTestResult(fdate:Date,tdate:Date): Single<List<AirTestResult>>

    @Query("SELECT COUNT(*) FROM TestResult")
    fun getAllTestResultCount(): Single<Int>

    @Query("SELECT * FROM TestResult WHERE testresult_guid= :id")
    fun getTestResultById(id: String): Single<AirTestResult>

    @Delete
    fun deleteTestResult(testResult: AirTestResult)


    @Query("Select * from TestResult where createdDate in (SELECT createdDate FROM TestResult GROUP BY createdDate order by createdDate desc  limit 2)")
    fun getLastTwoDatesTestResults(): Single<List<AirTestResult>>

}