package com.safey.lungmonitoring.ui.test.viewmodel

import android.annotation.SuppressLint
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.custombinings.longDate
import com.safey.lungmonitoring.data.tables.patient.*
import com.safey.lungmonitoring.repo.PatientRepository
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.SingleLiveEvent
import com.safey.lungmonitoring.utils.Utility
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TestResultViewModel @Inject constructor(private val patientRepository: PatientRepository) : ViewModel() {


    var testResults: SingleLiveEvent<List<AirTestResult>> = SingleLiveEvent()
    var testResult: SingleLiveEvent<AirTestResult> = SingleLiveEvent()
    var inserted: SingleLiveEvent<String> = SingleLiveEvent()

    var testResultData= AirTestResult()
    var patient: SingleLiveEvent<Patient> = SingleLiveEvent()
    var patientData = Patient()

    @SuppressLint("CheckResult")
    fun getPatient() {
        patientRepository.getPatient().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { patient ->

//                this.patient.postValue(patient[0])
                this.patient.postValue(Patient(
                    "jks",
                    "sahoo",
                    40,
                    "170",
                    2,
                "",
                    20,
                    "9127903706695674"
                ))
//                patientData = patient[0]
                patientData = Patient(
                    "jks",
                    "sahoo",
                    40,
                    "170",
                    2,
                    "",
                    20,
                    "9127903706695674"
                )
               /* patientData.BirthDate = SafeyApplication.firstTestResult!!.dob
                patientData.UHID = SafeyApplication.firstTestResult!!.userId.toString()
                patientData.Height = SafeyApplication.firstTestResult!!.height.toString()
                patientData.ethnicity = 20
                patientData.FirstName = SafeyApplication.firstTestResult!!.fname.toString()*/
            }
    }





@SuppressLint("CheckResult")
    fun getTestResult() {
        patientRepository.getTestResults().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { patient ->
                if (patient.isNotEmpty()) {
                    this.testResults.postValue(patient)

                }
            }
    }

    @SuppressLint("CheckResult")
    fun getTestResult(id:String) {
        patientRepository.getTestResultById(id).subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { testResult ->
                this.testResult.postValue(testResult)
            }
    }



    fun insertTestResult(airTestResult: AirTestResult) {
        viewModelScope.launch {
            patientRepository.insertTestResult(airTestResult)


        }
    }

    fun completeTest( insertCallback:Int =0) {

        val testResult = SafeyApplication.firstTestResult
        val listTrialResult: MutableList<TrialResult> = ArrayList<TrialResult>()
        val airTestResult = AirTestResult()

        extractData(testResult!!, listTrialResult,false)


        if (SafeyApplication.postTestResult!=null)
            extractData(SafeyApplication.postTestResult!!, listTrialResult,true)


        airTestResult.trialResult = listTrialResult
        val currentDate = Date()
        val date = SimpleDateFormat(Constants.DATE_FORMAT_PATTERN).format(currentDate)
        val time = SimpleDateFormat("hh:mm a").format(currentDate)
        val splitDate = date.split("-").toTypedArray()

        // airTestResult.person = PersonRepository.instance!!.getPerson()
        airTestResult.active = true
        airTestResult.createdDate = date
        airTestResult.testtime = time
        airTestResult.createdAt = currentDate
        airTestResult.type = testResult.type
        airTestResult.testtype = testResult.testtype
        airTestResult.sessionScore = testResult.sessionScore
        airTestResult.variance = testResult.variance
        airTestResult.fname = patientData.FirstName
        airTestResult.userId = patientData.UHID
        airTestResult.gender = patientData.Gender
        airTestResult.height = patientData.Height
        airTestResult.age = Utility.getAge(patientData.BirthDate).toString()
        airTestResult.dob = patientData.BirthDate

        insertTestResult(airTestResult)

        if (insertCallback ==0) {
            SafeyApplication.firstTestResult = null
            SafeyApplication.postTestResult = null
            Constants.isPost = false
            Constants.postTrialCount = 0

            inserted.postValue("true")


        }
    }

    private fun extractData(
        testResult: AirTestResult,

        listTrialResult: MutableList<TrialResult>,isPost:Boolean
    ) {

        for(i in testResult!!.trialResult!!.indices) {
            val airGraphDataList: MutableList<AirGraphData> = ArrayList()


            var trialResults: TrialResult =
                TrialResult()
            for (airgraphdata in testResult.trialResult!![i].graphDataList!!) {
                val airGraphData: AirGraphData = AirGraphData()
                airGraphData.volume = airgraphdata.volume
                airGraphData.flow = airgraphdata.flow
                airGraphData.second = airgraphdata.second
                airGraphData.direction = airgraphdata.direction

                airGraphDataList.add(airGraphData)
            }
            val testMeasurementsList: MutableList<TestMeasurements> = ArrayList()
            for (measuredValues in testResult.trialResult!![i].mesurementlist!!) {
                val testMeasurement: TestMeasurements = TestMeasurements()
                testMeasurement.measurement = measuredValues.measurement
                testMeasurement.measuredValue = measuredValues.measuredValue
                testMeasurement.predictedValue = measuredValues.predictedValue
                testMeasurement.predictedPer = measuredValues.predictedPer
                testMeasurement.unit = measuredValues.unit
                testMeasurement.lln =measuredValues.lln
                testMeasurement.uln =measuredValues.uln
                testMeasurement.zScore = measuredValues.zScore
                testMeasurementsList.add(testMeasurement)
            }


            val trialResult: TrialResult =
                TrialResult()
            trialResult.graphDataList = airGraphDataList
            trialResult.mesurementlist = testMeasurementsList
            trialResult.isBest = testResult.trialResult!![i].isBest
            trialResult.isPost = isPost

            listTrialResult.add(trialResult)
        }
    }





}