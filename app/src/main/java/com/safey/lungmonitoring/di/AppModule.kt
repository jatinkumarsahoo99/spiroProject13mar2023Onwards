package com.safey.lungmonitoring.di

import android.content.Context
import androidx.room.Room
import com.safey.lungmonitoring.data.dao.patient.*
import com.safey.lungmonitoring.data.database.PatientDatabase
import com.safey.lungmonitoring.data.pref.UserSession
import com.safey.lungmonitoring.repo.PatientRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    // Database

    @Provides
    @Singleton
    fun providesPatientDatabase(@ApplicationContext context: Context): PatientDatabase = Room.databaseBuilder(context, PatientDatabase::class.java, "PatientDatabase").build()

    // patient Dao
    @Provides
    fun providesPatientDao(patientDatabase: PatientDatabase): PatientDao = patientDatabase.patientDao()

    @Provides
    @Singleton
    fun providesUserSession(@ApplicationContext context: Context): UserSession = UserSession(context)


    // Repository
    @Provides
    fun providesPatientRepository(patientDao: PatientDao, medicationDao: MedicationDao, testResultDao: TestResultDao, symptomDao: SymptomDao/*,trialResultDao: TrialResultDao,testMeasurementDao: TrialTestMeasurementDao*/): PatientRepository = PatientRepository(patientDao,medicationDao =medicationDao,testResultDao,symptomDao/*,trialResultDao = trialResultDao,testMeasurementDao*/)

    @Provides
    fun providesMedicationDao(medicationDatabase: PatientDatabase): MedicationDao = medicationDatabase.medicationDao()


    @Provides
    fun providesTestResultDao(medicationDatabase: PatientDatabase): TestResultDao = medicationDatabase.testResultDao()

    @Provides
    fun providesSymptonDao(symptonDB: PatientDatabase): SymptomDao = symptonDB.symptomDao()

/*

    @Provides
    fun providesTrialResultDao(medicationDatabase: PatientDatabase): TrialResultDao = medicationDatabase.trialResultDao()


    @Provides
    fun providesTrialTestMeasurementsDao(medicationDatabase: PatientDatabase): TrialTestMeasurementDao = medicationDatabase.trialTestMeasurementDao()

*/



}