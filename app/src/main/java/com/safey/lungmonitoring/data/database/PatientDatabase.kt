package com.safey.lungmonitoring.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.safey.lungmonitoring.data.dao.patient.*

import com.safey.lungmonitoring.data.tables.patient.*

@Database(entities = [
    Patient::class, Medication::class,AirTestResult::class,Symptoms::class/*,TrialResult::class,TrialTestMeasurements::class*/
], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class PatientDatabase : RoomDatabase() {
    abstract fun patientDao(): PatientDao
    abstract fun medicationDao(): MedicationDao
    abstract fun testResultDao(): TestResultDao
    abstract fun symptomDao(): SymptomDao
   /* abstract fun trialTestMeasurementDao(): TrialTestMeasurementDao
    abstract fun trialResultDao(): TrialResultDao*/
}
