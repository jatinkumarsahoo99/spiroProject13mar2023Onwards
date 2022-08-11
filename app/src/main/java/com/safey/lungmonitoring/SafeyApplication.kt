package com.safey.lungmonitoring

import android.app.Application
import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import androidx.appcompat.app.AppCompatDelegate
import com.safey.lungmonitoring.data.pref.AppPrefs
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.repo.PatientRepository
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject


@HiltAndroidApp
class SafeyApplication : Application() {



    @Inject
    lateinit var patientRepository: PatientRepository

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    companion object {
        var appPrefs1: AppPrefs? = null
        lateinit var mContext: Context
        var firstTestResult: AirTestResult? = null
        var firstTestResultHelping: AirTestResult? = null
        var postTestResult: AirTestResult? = null
        var thirdTestResult: AirTestResult? = null
        var fourthTestResult: AirTestResult? = null
        var status:Boolean ? = false
        fun getMyContext(): Context? {
            return mContext
        }
    }

    override fun onCreate() {
        super.onCreate()
        mContext = this
        appPrefs1 = AppPrefs(applicationContext)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
//        initAcra {
//            //core configuration:
//            buildConfigClass = BuildConfig::class.java
//            reportFormat = StringFormat.JSON
//            //each plugin you chose above can be configured in a block like this:
//            mailSender {
//                //required
//                mailTo = "pravinmca09@gmail.com"
//                //defaults to true
//                reportAsFile = true
//                //defaults to ACRA-report.stacktrace
//                reportFileName = "Crash.txt"
//                //defaults to "<applicationId> Crash Report"
//                subject = getString(R.string.mail_subject)
//                //defaults to empty
//                body = getString(R.string.mail_body)
//            }
//        }

    }


}