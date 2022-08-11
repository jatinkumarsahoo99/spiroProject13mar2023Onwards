/*
package com.safey.lungapp.data.dao.patient

import androidx.room.*
import com.safey.lungapp.data.tables.patient.TrialResult
import io.reactivex.Single

@Dao
interface TrialResultDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trialResult: TrialResult)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(trialResult: TrialResult)

    @Query("SELECT * FROM TrialResult ORDER BY creation_date DESC LIMIT 1;")
    fun getLastTrialResultAdded(): Single<TrialResult>

    @Query("SELECT * FROM TestResult ORDER BY modification_date DESC LIMIT 1;")
    fun getLastTrialResultUpdated(): Single<TrialResult>

    @Query("SELECT * FROM TrialResult")
    fun getTrialResult(): Single<List<TrialResult>>



    @Query("SELECT COUNT(*) FROM TrialResult")
    fun getAllTrialResultCount(): Single<Int>

    @Query("SELECT * FROM TrialResult WHERE trialResult_TestResultId= :id")
    fun getTrialResultByTestId(id: String): Single<TrialResult>

    @Delete
    fun deleteTrialResult(trialResult: TrialResult)




}*/
