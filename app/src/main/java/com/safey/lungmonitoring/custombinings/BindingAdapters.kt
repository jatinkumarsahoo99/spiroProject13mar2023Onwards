@file:JvmName("BindingUtils")
package com.safey.lungmonitoring.custombinings

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.databinding.InverseMethod
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.datautils.Ethnicities
import com.safey.lungmonitoring.data.datautils.enumGender
import com.safey.lungmonitoring.data.datautils.enumMedicationFrequency
import com.safey.lungmonitoring.data.tables.patient.Medication
import com.safey.lungmonitoring.utils.Utility
import java.text.SimpleDateFormat
import java.util.*


@InverseMethod("posToGender")
fun genderToPos(gender: String): Int {
    return enumGender.fromString(gender).value
}

fun posToGender(position: Int): String {
    return if (position>0) enumGender.fromInt(position).getFormatString().toString()
    else
        ""
}



@InverseMethod("positionToEthnicity")
fun ethnicityToPosition(ethnicity: String): Int {
    return if (ethnicity.isNotEmpty())
        Ethnicities.fromString(ethnicity).value
    else
        0
}

fun positionToEthnicity(position: Int): String {
    return if (position>0)
        Ethnicities.fromInt(position).getFormatString().toString()
    else
        ""
}


@InverseMethod("longToDate")
fun dateToLong(date: String): Long {
    return if(date.isNotEmpty())
        Date(date).time
    else
        0L
}

fun longToDate(date: Long): String {
    val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
    return  if(date==0L) "" else formatter.format(Date(date)).toString()
}
fun longDate(date: Long): Date {
    return Date(date)
}
fun longToDate(date: Long,format:String=" MMM'yyyy"): String {
    val formatter = SimpleDateFormat(format, Locale.ENGLISH)
    return  if(date==0L) "" else formatter.format(Date(date)).toString()
}

@BindingAdapter("image")
fun setImage(view: ImageView,string: String){
    view.setImageResource(Utility.resoureId(view.context,string))
}

@BindingAdapter("frequencyText")
fun setFrequencyText(view: TextView, medication: Medication) {

    if (medication.freqType == enumMedicationFrequency.EveryDay.value)
        view.text = view.context.getString(R.string.everyday)
    else
        view.text = medication.days
}



