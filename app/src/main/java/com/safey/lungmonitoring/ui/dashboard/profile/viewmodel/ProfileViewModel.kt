package com.safey.lungmonitoring.ui.dashboard.profile.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safey.lungmonitoring.data.tables.patient.Patient
import com.safey.lungmonitoring.repo.PatientRepository
import com.safey.lungmonitoring.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject
@HiltViewModel
class ProfileViewModel @Inject constructor(private val patientRepository: PatientRepository) : ViewModel() {


    var patient: SingleLiveEvent<Patient> = SingleLiveEvent()
    var avatar: SingleLiveEvent<String> = SingleLiveEvent()
    //private var userAccountList: MutableList<UserAccounts> = mutableListOf()
    var patientData=Patient()


    @SuppressLint("CheckResult")
    fun getPatient() {
        patientRepository.getPatient().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { patient ->
                if (patient.isNotEmpty()) {
                    this.patient.postValue(patient[0])
                    //patientData=patient[0]
                }
            }
    }

    fun insertPatient(patient: Patient) {
        viewModelScope.launch {
            patientRepository.insertPatient(patient)
        }
    }

    fun updatePatient(patient: Patient) {
        viewModelScope.launch {
            patientRepository.updatePatient(patient)
        }
    }
    fun deleteAllPatient() {
        viewModelScope.launch {
            patientRepository.deleteAllPatient()
        }
    }

}