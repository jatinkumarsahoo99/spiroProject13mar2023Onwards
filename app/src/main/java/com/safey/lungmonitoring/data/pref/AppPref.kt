package com.safey.lungmonitoring.data.pref

import android.content.Context
import android.content.SharedPreferences


class AppPrefs(context: Context) {

    private val APP_PREF_NAME = "AppPref"
    private val DefaultDataLoaded = "DefaultDataLoaded"
    private val PeakflowDevice = "PeakflowDevice"
    private val SpirometerDevice = "SpirometerDevice"
    private val locationallow = "locationallow"

    private var preferences: SharedPreferences = context.getSharedPreferences(APP_PREF_NAME, Context.MODE_PRIVATE)

    var isDefaultDataLoaded: Boolean
        get() = preferences.getBoolean(DefaultDataLoaded, false)
        set(status) = preferences.edit().putBoolean(DefaultDataLoaded, status).apply()

    var isPeakflow: Boolean
        get() = preferences.getBoolean(PeakflowDevice, false)
        set(status) = preferences.edit().putBoolean(PeakflowDevice, status).apply()

    var isSpiroMeter: Boolean
        get() = preferences.getBoolean(SpirometerDevice, false)
        set(status) = preferences.edit().putBoolean(SpirometerDevice, status).apply()

    var isLocationAllowed: Boolean
        get() = preferences.getBoolean(locationallow, false)
        set(status) = preferences.edit().putBoolean(locationallow, status).apply()

}