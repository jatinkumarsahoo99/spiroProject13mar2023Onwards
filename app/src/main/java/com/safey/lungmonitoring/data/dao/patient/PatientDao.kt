package com.safey.lungmonitoring.data.dao.patient

import androidx.room.*
import com.safey.lungmonitoring.data.tables.patient.Patient
import io.reactivex.Single

@Dao
interface PatientDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(patient: Patient)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(patient: Patient)



    @Query("SELECT * FROM Patient ORDER BY creation_date DESC LIMIT 1;")
    fun getLastPatientAdded(): Single<Patient>

    @Query("SELECT * FROM Patient ORDER BY modification_date DESC LIMIT 1;")
    fun getLastPatientUpdated(): Single<Patient>

    @Query("SELECT * FROM Patient")
    fun getPatient(): Single<List<Patient>>


    @Query("SELECT COUNT(*) FROM Patient")
    fun getAllPatientCount(): Single<Int>


    @Query("SELECT * FROM Patient WHERE patient_guid= :id")
    fun getPatientById(id: String): Single<Patient>


    @Delete
    fun deletePatient(patient: Patient)

    @Query("DELETE FROM Patient")
    fun deletePatientTable()
}