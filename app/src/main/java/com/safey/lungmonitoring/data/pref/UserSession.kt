package com.safey.lungmonitoring.data.pref

import android.content.Context
import android.content.SharedPreferences
import com.safey.lungmonitoring.data.tables.patient.Patient

class UserSession(context: Context) {

    private val mFirstName: String = "FirstName"
    private val mLastName: String = "LastName"

    private val mIsLoggedIn: String = "IsLoggedIn"
    private val MeasurementType: String = "MeasurementDashboarad"
    private val profileImage: String = "ProfileImage"


    private val prefName = "UserSession"

    private var preferences: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)

    fun setUserData(userAccounts: Patient) {
        val edit = preferences.edit()

        edit.putString(mFirstName, userAccounts.FirstName)
        edit.putString(mLastName, userAccounts.LastName)
        edit.putString(profileImage,userAccounts.avatar)
        edit.putBoolean(mIsLoggedIn, true)
        setDataSet(SpirometerDataSet.GLI)
        edit.apply()

    }
    enum class SpirometerDataSet(){
        Nhances,
        GLI
    }

    fun setDataSet(spirometerDataSet: SpirometerDataSet){
        val edit = preferences.edit()
        edit.putInt("SpirometerDataSet",spirometerDataSet.ordinal)
        edit.apply()
    }

    var dataSet
        get() = preferences.getInt("SpirometerDataSet", 1)
        set(mValue) = preferences.edit().putInt("SpirometerDataSet", mValue).apply()
    fun clearSession() {
        preferences.edit().clear().apply()
    }


    var firstName: String?
        get() = preferences.getString(mFirstName, "")
        set(mValue) = preferences.edit().putString(mFirstName, mValue).apply()

    var lastName: String?
        get() = preferences.getString(mLastName, "")
        set(mValue) = preferences.edit().putString(mLastName, mValue).apply()


    var isLoggedIn: Boolean
        get() = preferences.getBoolean(mIsLoggedIn, false)
        set(mValue) = preferences.edit().putBoolean(mIsLoggedIn, mValue).apply()


    var measurementType: Int
        get() = preferences.getInt(MeasurementType, 1)
        set(mValue) = preferences.edit().putInt(MeasurementType, mValue).apply()

    var profileIcon: String?
        get() = preferences.getString(profileImage, "")
        set(mValue) = preferences.edit().putString(profileImage, mValue).apply()



}