package com.safey.lungmonitoring.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.dao.patient.PatientDao
import com.safey.lungmonitoring.data.pref.UserSession
import com.safey.lungmonitoring.data.tables.patient.Patient
import com.safey.lungmonitoring.databinding.ActivitySplashBinding
/*import com.safey.lungmonitoring.repo.PatientRepository
import com.safey.lungmonitoring.ui.Intro.IntroActivity*/
import com.safey.lungmonitoring.ui.dashboard.Dashboard
import com.safey.lungmonitoring.ui.dashboard.profile.viewmodel.ProfileViewModel
import com.safey.lungmonitoring.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class Splash : AppCompatActivity() {
    @Inject
    lateinit var userSession: UserSession

    //    val viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    lateinit var viewModel: ProfileViewModel;
    var patientData = Patient();
    //    var patientRepository = PatientRepository();
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        DataBindingUtil.setContentView<ActivitySplashBinding>(this, R.layout.activity_splash)
        val viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        Constants.isDelete = false
        Handler().postDelayed({
            val intent = Intent(this, Dashboard::class.java)
            startActivity(intent)
            finish()
        }, 2000)

        when {
            intent?.action == Intent.ACTION_SEND -> {
                if ("text/plain" == intent.type) {
                    handleSendText(intent) // Handle text being sent
                }
            }
        }
    }

    private fun handleSendText(intent: Intent) {
        patientData.FirstName = intent.getStringExtra("FirstName").toString()
        patientData.LastName = intent.getStringExtra("LastName").toString()
        patientData.Gender = if (intent.getStringExtra("Gender").toString().toUpperCase() == "MALE") 1 else 2
        patientData.Height = intent.getStringExtra("Height").toString()
        patientData.UHID = intent.getStringExtra("UHID").toString()
        patientData.HeightUnit = Integer.parseInt(intent.getStringExtra("HeightUnit").toString())
        patientData.BirthDate = intent.getStringExtra("BirthDate").toString().toLong()
        patientData.ethnicity =  Integer.parseInt(intent.getStringExtra("ethnicity").toString())
        viewModel.deleteAllPatient()
        viewModel.insertPatient(patient = patientData)
        findViewById<TextView>(R.id.test).text =   patientData.HeightUnit.toString()
    }

}