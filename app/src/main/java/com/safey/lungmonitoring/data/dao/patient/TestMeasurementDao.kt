/*
package com.safey.lungapp.data.dao.patient

import androidx.room.*
import com.safey.lungapp.data.tables.patient.TestMeasurements
import com.safey.lungapp.data.tables.patient.TrialTestMeasurements
import io.reactivex.Single

@Dao
interface TrialTestMeasurementDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(trialTestMeasurements: TrialTestMeasurements)


    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(trialTestMeasurements: TrialTestMeasurements)

    @Query("SELECT * FROM TrialTestMeasurements ORDER BY creation_date DESC LIMIT 1;")
    fun getLastTrialMeasurementAdded(): Single<TrialTestMeasurements>

    @Query("SELECT * FROM TrialTestMeasurements ORDER BY modification_date DESC LIMIT 1;")
    fun getLastTrialMeasurementsUpdated(): Single<TrialTestMeasurements>

    @Query("SELECT * FROM TrialTestMeasurements")
    fun getTrialMeasurements(): Single<List<TrialTestMeasurements>>


    @Query("SELECT * FROM TrialTestMeasurements WHERE testMeasurement_TrialResultId= :id")
    fun getTrialMeasurementBytrailId(id: String): Single<TrialTestMeasurements>

    @Delete
    fun deleteTrialMeasurement(trialTestMeasurements: TrialTestMeasurements)




}*/
