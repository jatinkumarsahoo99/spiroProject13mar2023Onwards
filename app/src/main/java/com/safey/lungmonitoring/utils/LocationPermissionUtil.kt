package com.safey.lungmonitoring.utils

import android.Manifest
import android.annotation.TargetApi
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat

private const val REQUEST_CODE_FOREGROUND = 1
private const val REQUEST_CODE_FOREGROUND_AND_BACKGROUND = 2

object LocationPermissionUtil {

    private fun Context.isPermissionGranted(permission: String): Boolean = ActivityCompat
        .checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private val Context.isFineLocationPermissionGranted
        get() = isPermissionGranted(
            Manifest.permission.ACCESS_FINE_LOCATION
        )

    private val Context.isBackgroundPermissionGranted
        get() = when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            else -> isFineLocationPermissionGranted
        }

    private val Context.isFineAndBackgroundLocationPermissionsGranted
        get() = isFineLocationPermissionGranted && isBackgroundPermissionGranted

    private fun Activity.checkFineLocationPermission() {
        if (isFineLocationPermissionGranted) return

        val shouldShowFineLocationPermissionRationale = ActivityCompat
            .shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            )

       // if (shouldShowFineLocationPermissionRationale) {
//            presentAlertDialog(
//                R.string.dialog_fine_location_rationale_title,
//                R.string.dialog_fine_location_rationale_description,
//                R.string.yes,
//            ) {
            requestLocationPermissions()
//            }
//        } else {
//            requestLocationPermissions()
//        }
       // }
    }

    private fun Activity.requestLocationPermissions() =
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.Q) {
            requestFineLocationAndBackground()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_CODE_FOREGROUND
            )
        }

    @TargetApi(29)
    private fun Activity.requestFineLocationAndBackground() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ),
            REQUEST_CODE_FOREGROUND_AND_BACKGROUND
        )
    }

    @TargetApi(29)
    private fun Activity.checkBackgroundLocationPermission() {
        if (isFineAndBackgroundLocationPermissionsGranted) return

        val shouldShowBackgroundPermissionRationale = ActivityCompat
            .shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )

//        if (shouldShowBackgroundPermissionRationale) {
//            presentAlertDialog(
//                R.string.dialog_background_location_rationale_title,
//                R.string.dialog_background_location_rationale_description,
//                R.string.yes,
//            ) {
                requestFineLocationAndBackground()
//            }
//        } else {
//            requestFineLocationAndBackground()
//        }
    }

    fun checkLocationPermissions(activity: Activity, action: () -> Unit) = with(activity) {
        if (isFineAndBackgroundLocationPermissionsGranted) {
            action()
            return
        }

        checkFineLocationPermission()
    }

    fun onRequestPermissionsResult(
        activity: Activity,
        requestCode: Int,
        action: () -> Unit
    ) = with(activity) {
        when (requestCode) {
            REQUEST_CODE_FOREGROUND -> {
                if (!isFineLocationPermissionGranted) {
                    checkFineLocationPermission()
                    return
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    checkBackgroundLocationPermission()
                } else {
                    action()
                }
            }
            REQUEST_CODE_FOREGROUND_AND_BACKGROUND -> {
                if (!isFineLocationPermissionGranted) {
                    checkFineLocationPermission()
                    return
                }

                if (isBackgroundPermissionGranted) {
                    action()
                } else {
                    checkBackgroundLocationPermission()
                }
            }
        }
    }
}
