package com.safey.lungmonitoring.data.dao.patient

import androidx.room.*
import com.safey.lungmonitoring.data.tables.patient.Medication
import io.reactivex.Single

@Dao
interface MedicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(medication: Medication)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun update(medication: Medication)

    @Query("SELECT * FROM Medication ORDER BY creation_date DESC LIMIT 1;")
    fun getLastMedicationAdded(): Single<Medication>

    @Query("SELECT * FROM Medication ORDER BY modification_date DESC LIMIT 1;")
    fun getLastMedicationUpdated(): Single<Medication>

    @Query("SELECT * FROM Medication")
    fun getMedication(): Single<List<Medication>>

    @Query("SELECT COUNT(*) FROM Medication")
    fun getAllMedicationCount(): Single<Int>

    @Query("SELECT * FROM Medication WHERE medication_guid= :id")
    fun getMedicationById(id: String): Single<Medication>

    @Delete
    fun deleteMedication(patient: Medication)


    @Query("SELECT COUNT(medicationName) FROM Medication where medicationName= :medName ")
    fun getMedicationCount(medName: String): Single<Int>


}