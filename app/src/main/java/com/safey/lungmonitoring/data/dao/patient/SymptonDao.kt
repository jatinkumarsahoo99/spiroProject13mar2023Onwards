package com.safey.lungmonitoring.data.dao.patient

import androidx.room.*
import com.safey.lungmonitoring.data.tables.patient.Symptoms
import io.reactivex.Single
import java.util.*

@Dao
interface SymptomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(symptoms: Symptoms)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(symptoms: Symptoms)

    @Query("SELECT * FROM Symptoms ORDER BY creation_date DESC LIMIT 1;")
    fun getLastSymptomAdded(): Single<Symptoms>

    @Query("SELECT * FROM Symptoms ORDER BY modification_date DESC LIMIT 1;")
    fun getLastSymptomUpdated(): Single<Symptoms>

    @Query("SELECT * FROM Symptoms")
    fun getSymptoms(): Single<List<Symptoms>>

    @Query("SELECT * FROM Symptoms where modification_date BETWEEN :fdate AND :tdate ORDER BY modification_date ASC ")
    fun getSymptoms(fdate: Date, tdate: Date): Single<List<Symptoms>>

    @Query("SELECT COUNT(*) FROM Symptoms")
    fun getAllSymptomCount(): Single<Int>

    @Query("SELECT * FROM Symptoms WHERE symptom_guid= :id")
    fun getSymptomById(id: String): Single<Symptoms>

    @Delete
    fun deleteSympton(symptoms: Symptoms)

    @Query("DELETE FROM Symptoms")
    fun deleteAllSymptons()



}