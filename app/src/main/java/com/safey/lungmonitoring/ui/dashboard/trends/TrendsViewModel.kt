package com.safey.lungmonitoring.ui.dashboard.trends

import android.annotation.SuppressLint
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.repo.PatientRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

@HiltViewModel
class TrendsViewModel @Inject constructor(private val patientRepository: PatientRepository)  : ViewModel() {
    var testResultList: MutableLiveData<List<AirTestResult>> = MutableLiveData()
    var testList: List<AirTestResult> = ArrayList()

    var measurementType:MutableLiveData<Int> = MutableLiveData()

    @SuppressLint("CheckResult")
    fun getTestData(fdate:Date, tdate:Date){
        patientRepository.getTestResults(fdate,tdate).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { testResults ->
                if (testResults.isNotEmpty()) {
                    testResultList.postValue(testResults)
                    testList = testResults
                }

            }
    }

    fun setMeasurementType(measurementType :Int){
        this.measurementType.postValue(measurementType)
    }



}