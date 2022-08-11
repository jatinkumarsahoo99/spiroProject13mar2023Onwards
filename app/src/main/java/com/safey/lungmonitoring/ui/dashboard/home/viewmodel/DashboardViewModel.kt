package com.safey.lungmonitoring.ui.dashboard.home.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.Patient
import com.safey.lungmonitoring.data.tables.patient.Symptoms
import com.safey.lungmonitoring.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class DashboardViewModel @Inject constructor(private val patientRepository: PatientRepository)  : ViewModel() {
    var testResultList: MutableLiveData<List<AirTestResult>> = MutableLiveData()
    var sessionTestResultList: MutableLiveData<List<AirTestResult>> = MutableLiveData()
    var testList: List<AirTestResult> = ArrayList()
    var sessionTestList: List<AirTestResult> = ArrayList()
    var symptonsList: List<Symptoms> = ArrayList()
    var postList: MutableList<AirTestResult> = ArrayList()
    var measurementType:MutableLiveData<Int> = MutableLiveData()
    var patient: Patient = Patient()
    @SuppressLint("CheckResult")
    fun getTestData(){
        patientRepository.getTestResults().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { testResults ->
                getSymptonData(testResults)
            }
    }


    @SuppressLint("CheckResult")
    fun getSymptonData( testResults: List<AirTestResult>){
        patientRepository.getSymptons().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { symptons ->
                testResultList.postValue(testResults)
                testList = testResults
                symptonsList = symptons

            }
    }
    @SuppressLint("CheckResult")
    fun getPatientDetails(){
        patientRepository.getPatient().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { patient ->
                if (patient.isNotEmpty()) {
                    this.patient = patient[0]
                    //patientData=patient[0]
                }
            }
    }
    fun setMeasurementType(measurementType :Int){
        this.measurementType.postValue(measurementType)
    }

    @SuppressLint("CheckResult")
    fun getLastSessionTestResult() {
        patientRepository.getLastTwoDatesTestResults().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { testResult ->
                sessionTestResultList.postValue(testResult)
                sessionTestList = testResult
            }
    }
    fun deleteTestData(airTestResult: AirTestResult) {
        viewModelScope.launch {
            patientRepository.deleteTestResult(airTestResult)
//            getLastSessionTestResult()

        }
    }
    fun postTestData(airTestResult: AirTestResult) {
        postList.add(airTestResult)

    }

}