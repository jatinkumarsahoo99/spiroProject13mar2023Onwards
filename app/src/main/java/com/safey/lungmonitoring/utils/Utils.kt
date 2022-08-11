package com.safey.lungmonitoring.utils

import android.content.Context
import android.text.format.DateFormat
import com.safey.lungmonitoring.R
import java.text.ParseException
import java.util.*

class Utils {


    fun getweekDay(date: Date, context: Context): String? {
        val cal = Calendar.getInstance()
        cal.time = date
        val day = cal[Calendar.DAY_OF_WEEK]
        when (day) {
            1 -> return context.getString(R.string.su)
            2 -> return context.getString(R.string.mo)
            3 -> return context.getString(R.string.tu)
            4 -> return context.getString(R.string.we)
            5 -> return context.getString(R.string.th)
            6 -> return context.getString(R.string.fr)
            7 -> return context.getString(R.string.sa)
        }
        return "Sun"
    }


    fun showDateText(selectedDate: Date,currentDate : Date) : String {
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DATE, -1)
        val tomocal = Calendar.getInstance()
        tomocal.time = currentDate
        tomocal.add(Calendar.DATE, 1)


        //String dayName = (String) android.text.format.DateFormat.format("EE",  selectedDate);
        val day = DateFormat.format("dd", selectedDate) as String
        val monthName = DateFormat.format("MMM", selectedDate) as String
        val year = DateFormat.format("yyyy", selectedDate) as String

        //dateFormat.setText(dayName+", "+day+" "+monthName+" "+year);
        return "$day $monthName $year"
    }

     fun getListDates(previousDate: Date, futureDate: Date): List<Date>? {
        val dates = ArrayList<Date>()

        val cal1 = Calendar.getInstance()
        cal1.time = previousDate
        val cal2 = Calendar.getInstance()
        cal2.time = futureDate
        while (!cal1.after(cal2)) {

            try {
                dates.add(Utility.sdformat.parse(Utility.sdformat.format(cal1.time))!!)
                cal1.add(Calendar.DATE, 1)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }
        return dates
    }

     fun getPreviousDate(currentDate: Date): Date? {
        var pastDate: Date? = null
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DATE, -14) //minus number would decrement the days

        try {
            pastDate = Utility.sdformat.parse(Utility.sdformat.format(cal.time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return pastDate
    }

    fun getPreviousDate(currentDate: Date,days:Int): Date? {
        var pastDate: Date? = null
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DATE, days) //minus number would decrement the days

        try {
            pastDate = Utility.sdformat.parse(Utility.sdformat.format(cal.time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return pastDate
    }

     fun getFutureDate(currentDate: Date): Date? {
        var pastDate: Date? = null
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DATE, 2) //minus number would decrement the days


        try {

            pastDate = Utility.sdformat.parse(Utility.sdformat.format(cal.time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return pastDate
    }
    fun getFutureDateCurrent(currentDate: Date): Date? {
        var pastDate: Date? = null
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DATE, 1) //minus number would decrement the days


        try {

            pastDate = Utility.sdformat.parse(Utility.sdformat.format(cal.time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return pastDate
    }

    fun getFutureDate1(currentDate: Date): Date? {
        var pastDate: Date? = null
        val cal = Calendar.getInstance()
        cal.time = currentDate
        cal.add(Calendar.DATE, 1) //minus number would decrement the days


        try {

            pastDate = Utility.sdformat.parse(Utility.sdformat.format(cal.time))
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        return pastDate
    }
}