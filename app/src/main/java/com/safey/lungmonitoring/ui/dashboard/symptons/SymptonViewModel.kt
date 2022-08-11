package com.safey.lungmonitoring.ui.dashboard.symptons

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safey.lungmonitoring.data.tables.patient.Symptoms

import com.safey.lungmonitoring.repo.PatientRepository
import com.safey.lungmonitoring.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SymptomViewModel @Inject constructor(private val patientRepository: PatientRepository) : ViewModel() {


    var allSymptom: SingleLiveEvent<List<Symptoms>> = SingleLiveEvent()
    var symptom: SingleLiveEvent<Symptoms> = SingleLiveEvent()

    var listSymptons: List<Symptoms> = ArrayList()
    var symptoms = Symptoms()
    @SuppressLint("CheckResult")
    fun getAllSympton() {
        patientRepository.getSymptons().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { symptons ->
                this.allSymptom.postValue(symptons)
            }
    }
    @SuppressLint("CheckResult")
    fun getSymptonById(id:String) {
        patientRepository.getSymptonById(id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { sympton ->
                this.symptom.postValue(sympton)
            }
    }

    fun insertSymptons(symptons: Symptoms) {
        viewModelScope.launch {
            patientRepository.insertSymptons(symptons)
        }
    }
    fun insertSymptons(symptonsList: List<Symptoms>) {
        viewModelScope.launch(Dispatchers.IO) {
            deleteSymptoms()
            for (symptons in symptonsList) {
                patientRepository.insertSymptons(symptons)
            }
        }
    }

    fun updateSymptons(symptons: Symptoms) {
        viewModelScope.launch {
            patientRepository.updateSymptons(symptons)
        }
    }

    fun deleteSymptoms(){
        viewModelScope.launch(Dispatchers.IO) {
            patientRepository.deleteAllSympton()
        }
    }
    fun deleteSymptom(symptoms: Symptoms){
        viewModelScope.launch(Dispatchers.IO) {
            patientRepository.deleteSymptoms(symptoms)
        }
    }

}