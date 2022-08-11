package com.safey.lungmonitoring.repo

import androidx.annotation.WorkerThread
import com.safey.lungmonitoring.data.dao.patient.MedicationDao
import com.safey.lungmonitoring.data.dao.patient.PatientDao
import com.safey.lungmonitoring.data.dao.patient.SymptomDao
import com.safey.lungmonitoring.data.dao.patient.TestResultDao
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.Medication
import com.safey.lungmonitoring.data.tables.patient.Patient
import com.safey.lungmonitoring.data.tables.patient.Symptoms
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject

class PatientRepository @Inject constructor(private val patientDao: PatientDao,val medicationDao: MedicationDao,var testResultDao: TestResultDao,var symptomDao: SymptomDao/*,var trialResultDao: TrialResultDao,var testMeasurementDao: TrialTestMeasurementDao*/) {

    // Patient
    val getLastPatientUpdated: Single<Patient> = patientDao.getLastPatientUpdated()
    val getLastPatientAdded: Single<Patient> = patientDao.getLastPatientAdded()

    @WorkerThread
    suspend fun deletePatient(patient: Patient) = withContext(Dispatchers.IO) {
        patientDao.deletePatient(patient)
    }

    fun getPatientById(id: String): Single<Patient>{
        return patientDao.getPatientById(id)
    }

    fun getPatient(): Single<List<Patient>>{
        return patientDao.getPatient()
    }

    @WorkerThread
    suspend fun insertPatient(patient: Patient) = withContext(Dispatchers.IO) {
        patient.creationDate = System.currentTimeMillis()
        patient.modificationDate = System.currentTimeMillis()
        patientDao.insert(patient)
    }

    @WorkerThread
    suspend fun updatePatient(patient: Patient) = withContext(Dispatchers.IO) {
        patient.modificationDate = System.currentTimeMillis()
        patientDao.update(patient)
    }
    @WorkerThread
    suspend fun deleteAllPatient() = withContext(Dispatchers.IO) {
        patientDao.deletePatientTable()
    }
    //-----------------------------------------------------------------------------------------------
    //medication
    val getLastMedicationUpdated: Single<Medication> = medicationDao.getLastMedicationUpdated()
    val getLastMedicationAdded: Single<Medication> = medicationDao.getLastMedicationAdded()


    @WorkerThread
    suspend fun deleteMedication(medication: Medication) = withContext(Dispatchers.IO) {
        medicationDao.deleteMedication(medication)
    }


    fun getMedicationById(id: String): Single<Medication>{
        return medicationDao.getMedicationById(id)
    }

    fun getMedication(): Single<List<Medication>>{
        return medicationDao.getMedication()
    }


    fun getMedication(name:String): Single<Int>{
        return medicationDao.getMedicationCount(name)
    }



    @WorkerThread
    suspend fun insertMedication(medication: Medication) = withContext(Dispatchers.IO) {
        medication.creationDate = System.currentTimeMillis()
        medication.modificationDate = System.currentTimeMillis()
        medicationDao.insert(medication)
    }

    @WorkerThread
    suspend fun updateMedication(medication: Medication) = withContext(Dispatchers.IO) {
        medication.modificationDate = System.currentTimeMillis()
        medicationDao.update(medication)
    }

 //-----------------------------------------------------------------------------------------------
    //testresult
    val getLastTestResultUpdated: Single<AirTestResult> = testResultDao.getLastTestResultUpdated()
    val getLastTestResultAdded: Single<AirTestResult> = testResultDao.getLastTestResultAdded()

    @WorkerThread
    suspend fun deleteTestResult(airTestResult: AirTestResult) = withContext(Dispatchers.IO) {
        testResultDao.deleteTestResult(airTestResult)
    }


    fun getTestResultById(id: String): Single<AirTestResult>{
        return testResultDao.getTestResultById(id)
    }
    fun getLastTwoDatesTestResults(): Single<List<AirTestResult>>{
        return testResultDao.getLastTwoDatesTestResults()
    }

    fun getTestResults(): Single<List<AirTestResult>>{
        return testResultDao.getTestResult()
    }
    fun getTestResults(fdate:Date,tdate:Date): Single<List<AirTestResult>>{
        return testResultDao.getTestResult(fdate,tdate)
    }


    @WorkerThread
    suspend fun insertTestResult(airTestResult: AirTestResult) = withContext(Dispatchers.IO) {
        airTestResult.creationDate = System.currentTimeMillis()
        airTestResult.modificationDate = System.currentTimeMillis()
       testResultDao.insert(airTestResult)
    }

    @WorkerThread
    suspend fun updateTestResult(airTestResult: AirTestResult) = withContext(Dispatchers.IO) {
        airTestResult.modificationDate = System.currentTimeMillis()
        testResultDao.update(airTestResult)
    }


    //--------------------------------------------------------------------------------------------

    //Sympton
    val getLastSymptonUpdated: Single<Symptoms> = symptomDao.getLastSymptomUpdated()
    val getLastSymptonAdded: Single<Symptoms> = symptomDao.getLastSymptomAdded()

    @WorkerThread
    suspend fun deleteSymptoms(symptoms: Symptoms) = withContext(Dispatchers.IO) {
        symptomDao.deleteSympton(symptoms)
    }


    fun getSymptonById(id: String): Single<Symptoms>{
        return symptomDao.getSymptomById(id)
    }

    fun getSymptons(fdate:Date,tdate:Date): Single<List<Symptoms>>{
        return symptomDao.getSymptoms(fdate,tdate)
    }
    fun getSymptons(): Single<List<Symptoms>>{
        return symptomDao.getSymptoms()
    }
    fun deleteAllSympton(){
        return symptomDao.deleteAllSymptons()
    }
    @WorkerThread
    suspend fun insertSymptons(symptons: Symptoms) = withContext(Dispatchers.IO) {
        symptons.creationDate = System.currentTimeMillis()
        symptons.modificationDate = System.currentTimeMillis()
        symptomDao.insert(symptons)
    }

    @WorkerThread
    suspend fun updateSymptons(symptons: Symptoms) = withContext(Dispatchers.IO) {
        symptons.modificationDate = System.currentTimeMillis()
        symptomDao.update(symptons)
    }


    /* //-----------------------------------------------------------------------------------------------
     //trialresult
     val getLastTrialResultUpdated: Single<TrialResult> = trialResultDao.getLastTrialResultAdded()
     val getLastTrialResultAdded: Single<TrialResult> = trialResultDao.getLastTrialResultUpdated()

     @WorkerThread
     suspend fun deleteTrialResult(trialResult: TrialResult) = withContext(Dispatchers.IO) {
         trialResultDao.deleteTrialResult(trialResult)
     }


     fun getTrialResultById(id: String): Single<TrialResult>{
         return trialResultDao.getTrialResultByTestId(id)
     }

     fun getTrialResult(): Single<List<TrialResult>>{
         return trialResultDao.getTrialResult()
     }


     @WorkerThread
     suspend fun insertTrialResult(trialResult: TrialResult) = withContext(Dispatchers.IO) {
         trialResult.creationDate = System.currentTimeMillis()
         trialResult.modificationDate = System.currentTimeMillis()
         trialResultDao.insert(trialResult)
     }

     @WorkerThread
     suspend fun insertTrialResult(trialResult: List<TrialResult>) = withContext(Dispatchers.IO) {
         for (t in trialResult){
             t.creationDate = System.currentTimeMillis()
             t.modificationDate = System.currentTimeMillis()
             trialResultDao.insert(t)
         }
     }

     @WorkerThread
     suspend fun updateTrialResult(trialResult: TrialResult) = withContext(Dispatchers.IO) {
         trialResult.modificationDate = System.currentTimeMillis()
         trialResultDao.update(trialResult)
     }


     //-----------------------------------------------------------------------------------------------
     //TestMeasurement
     val getLastTestMeasurementUpdated: Single<TrialTestMeasurements> = testMeasurementDao.getLastTrialMeasurementsUpdated()
     val getLastTestMeasurementsAdded: Single<TrialTestMeasurements> = testMeasurementDao.getLastTrialMeasurementAdded()

     @WorkerThread
     suspend fun deleteTestMeasurement(trialTestMeasurements: TrialTestMeasurements) = withContext(Dispatchers.IO) {
         testMeasurementDao.deleteTrialMeasurement(trialTestMeasurements)
     }


     fun getTestmeasurementById(id: String): Single<TrialTestMeasurements>{
         return testMeasurementDao.getTrialMeasurementBytrailId(id)
     }

     fun getTestMeasurements(): Single<List<TrialTestMeasurements>>{
         return testMeasurementDao.getTrialMeasurements()
     }


     @WorkerThread
     suspend fun insertTestmeasurements(trialTestMeasurements: TrialTestMeasurements) = withContext(Dispatchers.IO) {
         trialTestMeasurements.creationDate = System.currentTimeMillis()
         trialTestMeasurements.modificationDate = System.currentTimeMillis()
         testMeasurementDao.insert(trialTestMeasurements)
     }

     @WorkerThread
     suspend fun updateTestMeasurement(trialTestMeasurements: TrialTestMeasurements) = withContext(Dispatchers.IO) {
         trialTestMeasurements.modificationDate = System.currentTimeMillis()
         testMeasurementDao.update(trialTestMeasurements)
     }
 */

}