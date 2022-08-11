package com.safey.lungmonitoring.utils

import android.app.Activity
import androidx.appcompat.widget.Toolbar
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_ABOUT
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_DEVICE_SETUP
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_HOME
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_LEGAL
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_MEDICATIONS
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_PROFILE
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_REPORT
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_TREND

class UpdateNavbar private constructor() {


    fun init(activity: Activity?, toolbar: Toolbar?) {
        Companion.activity = activity
        Companion.toolbar = toolbar
    }

    fun updateFragmentByCode(code: String) {
        when {
            code.equals(FRAGMENT_CODE_HOME, ignoreCase = true) -> {
                updateToHome()
            }
            code.equals(FRAGMENT_CODE_DEVICE_SETUP, ignoreCase = true) -> {
                updateToDeviceSetup()
            }
            code.equals(FRAGMENT_CODE_MEDICATIONS, ignoreCase = true) -> {
                updateToMedicationSetup()
            }
            code.equals(FRAGMENT_CODE_PROFILE, ignoreCase = true) -> {
                updateToProfileSetup()
            }
            code.equals(FRAGMENT_CODE_REPORT, ignoreCase = true) -> {
                updateToReportsSetup()
            }
            code.equals(FRAGMENT_CODE_LEGAL, ignoreCase = true) -> {
                updateToLegalSetup()
            }
            code.equals(FRAGMENT_CODE_ABOUT, ignoreCase = true) -> {
                updateToAboutSetup()
            }
            code.equals(FRAGMENT_CODE_TREND, ignoreCase = true) -> {
                updateToTrendsSetup()
            }

        }
    }

    private fun updateToHome() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

           // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = "\n Welcome"
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
           // Logger.tag("Exception").error(e)
        }
    }

    private fun updateToDeviceSetup() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

            // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = activity?.getString(R.string.device_setup)
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
        }
    }

    private fun updateToMedicationSetup() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

            // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = activity?.getString(R.string.medications)
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
        }
    }

    private fun updateToProfileSetup() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

            // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = activity?.getString(R.string.profile)
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
        }
    }

    private fun updateToTrendsSetup() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

            // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = activity?.getString(R.string.trends)
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
        }
    }

    private fun updateToReportsSetup() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

            // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = activity?.getString(R.string.reports)
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
        }
    }

    private fun updateToAboutSetup() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

            // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = activity?.getString(R.string.about)
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
        }
    }


    private fun updateToLegalSetup() {
        try {
            val model = NavBarModel()
            model.activity = activity
            model.toolbar = toolbar

            // model.title = "\n ${activity?.getString(R.string.welcome,userSession.firstName)}"
            model.title = activity?.getString(R.string.legal)
            model.iconLeft = R.drawable.ic_drawer
            model.isHidden = false

            Constants.updateNavBarProgress(model)
        } catch (e: Exception) {
            // Logger.tag("Exception").error(e)
        }
    }

    companion object {
        private var activity: Activity? = null
        private var toolbar: Toolbar? = null
        val instance = UpdateNavbar()
    }
}