package com.safey.lungmonitoring.ui.dashboard.medication.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safey.lungmonitoring.data.tables.patient.Medication
import com.safey.lungmonitoring.repo.PatientRepository
import com.safey.lungmonitoring.utils.SingleLiveEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationViewModel @Inject constructor(private val patientRepository: PatientRepository) : ViewModel() {


    var allMedication: SingleLiveEvent<List<Medication>> = SingleLiveEvent()
    var medication: SingleLiveEvent<Medication> = SingleLiveEvent()
    var medicationCount: SingleLiveEvent<Int> = SingleLiveEvent()

    var medicationData = Medication()
    @SuppressLint("CheckResult")
    fun getAllMedication() {
        patientRepository.getMedication().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { medication ->
                this.allMedication.postValue(medication)
            }
    }

    fun insertMedication(medication: Medication) {
        viewModelScope.launch {
            patientRepository.insertMedication(medication)
        }
    }

    fun updateMedication(medication: Medication) {
        viewModelScope.launch {
            patientRepository.updateMedication(medication)
        }
    }

    @SuppressLint("CheckResult")
    fun getMedicationId(guid:String) {
        patientRepository.getMedicationById(guid).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { medication ->
                    this.medication.postValue(medication)

            }
    }

    @SuppressLint("CheckResult")
    fun getMedicationCount(name:String){
         patientRepository.getMedication(name).subscribeOn(Schedulers.io())
             .observeOn(AndroidSchedulers.mainThread())
             .subscribe { medicationcount ->
                 this.medicationCount.postValue(medicationcount)
             }
    }

    fun deleteMed(guid: String){
        viewModelScope.launch(Dispatchers.Main) {
            var medication = Medication()
            medication.guid = guid
            patientRepository.deleteMedication(medication = medication)
            getAllMedication()
        }
    }

}