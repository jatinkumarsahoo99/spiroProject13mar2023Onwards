package com.safey.lungmonitoring.ui.dashboard.reports.viewmodel


import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.Patient
import com.safey.lungmonitoring.repo.PatientRepository
import com.safey.lungmonitoring.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

@HiltViewModel
    class ReportViewModel @Inject constructor(private val patientRepository: PatientRepository)  : ViewModel() {
    var testList: List<AirTestResult> = ArrayList()
    var patient: SingleLiveEvent<Patient> = SingleLiveEvent()
    var patientData = Patient()

        @SuppressLint("CheckResult")
        fun getTestData(fdate: Date, tdate: Date){
            patientRepository.getTestResults(fdate,tdate).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { testResults ->

                    testList = testResults
                    getPatient()
                }
        }

        @SuppressLint("CheckResult")
        fun getPatient() {
            patientRepository.getPatient().subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { patient ->

                      this.patient.postValue(patient[0])
                    patientData = patient[0]
                    }
                }
        }

