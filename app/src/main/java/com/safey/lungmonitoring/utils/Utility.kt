package com.safey.lungmonitoring.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextUtils
import android.text.style.ImageSpan
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.data.datautils.enumMedicationFrequency
import com.safey.lungmonitoring.data.tables.patient.AirGraphData
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.TestMeasurements
import com.safey.lungmonitoring.data.tables.patient.TrialResult
import com.safey.lungmonitoring.ui.dashboard.home.HomeFragmentDirections
import com.safey.lungmonitoring.ui.dashboard.medication.MedFrequency
import com.safey.lungmonitoring.ui.dashboard.symptons.SymptomModel
import com.safey.lungmonitoring.ui.profile.Avatar
import com.safey.safey_medication.model.MedColors
import com.safey.safey_medication.model.MedIcon
import info.safey.safey_sdk.TestResult
import info.safey.safey_sdk.Variance
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.nio.channels.FileChannel
import java.nio.charset.Charset
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


object Utility{

    var sdformat = SimpleDateFormat(Constants.DATE_FORMAT_PATTERN)

    private const val imageURL = "imageURL"



    enum class ReportType{
        PDF,
        CSV
    }
    enum class TestType{
        FEVC,
        FIVC
    }
    enum class ReportRangeType{
        WEEK,
        LASTWEEK,
        THISMONTH,
        LASTMONTH,
        LAST3MONTH,
        LAST6MONTH
    }

    @Throws(IOException::class)
    fun moveFile(file: File, dir: File) {
        val newFile = File(dir, file.getName())
        var outputChannel: FileChannel? = null
        var inputChannel: FileChannel? = null
        try {
            outputChannel = FileOutputStream(newFile).getChannel()
            inputChannel = FileInputStream(file).getChannel()
            inputChannel.transferTo(0, inputChannel.size(), outputChannel)
            inputChannel.close()
            //file.delete()
        } finally {
            if (inputChannel != null) inputChannel.close()
            if (outputChannel != null) outputChannel.close()
        }
    }

    fun getFormattedDateTime(date: Date?): String? {
        val cal = Calendar.getInstance()
        cal.time = date
        //2nd of march 2015
        val day = cal[Calendar.DATE]
        return if (!(day > 10 && day < 19)) when (day % 10) {
            1 -> SimpleDateFormat("d'st' MMM'' yy    hh:mm a").format(date)
            2 -> SimpleDateFormat("d'nd' MMM'' yy    hh:mm a").format(date)
            3 -> SimpleDateFormat("d'rd' MMM'' yy    hh:mm a").format(date)
            else -> SimpleDateFormat("d'th' MMM'' yy    hh:mm a").format(date)
        } else SimpleDateFormat("d'th'  MMMM yy    hh:mm a").format(date)
    }
    fun getFormattedDate(date: Date?): String? {
        val cal = Calendar.getInstance()
        cal.time = date
        //2nd of march 2015
        val day = cal[Calendar.DATE]
        return if (!(day > 10 && day < 19)) when (day % 10) {
            1 -> SimpleDateFormat("d'st' MMM'' yyyy").format(date)
            2 -> SimpleDateFormat("d'nd' MMM'' yyyy").format(date)
            3 -> SimpleDateFormat("d'rd' MMM'' yyyy").format(date)
            else -> SimpleDateFormat("d'th' MMM'' yyyy").format(date)
        } else SimpleDateFormat("d'th'  MMMM yyyy").format(date)
    }

    fun getpreviousweek():Array<String> {

        val c = Calendar.getInstance();
        // Set the calendar to monday of the current week
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        c.add(Calendar.DATE, -1 * 7);
        // Print dates of the current week starting on Monday
        val df =  SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH);
        val listDate =  ArrayList<String>();

        for ( i in 0..7)
        {
            listDate.add(df.format(c.time));
            c.add(Calendar.DAY_OF_MONTH, 1);
        }


        val MONDAY = listDate[0]
        val SUNDAY = listDate[6]
        return arrayOf(MONDAY,SUNDAY)
    }


    fun getCurrentWeek(): String? {
        val mCalendar = Calendar.getInstance()
        val date = Date()
        mCalendar.time = date

        // 1 = Sunday, 2 = Monday, etc.
        val day_of_week = mCalendar[Calendar.DAY_OF_WEEK]
        val monday_offset: Int
        monday_offset = if (day_of_week == 1) {
            -6
        } else 2 - day_of_week // need to minus back
        mCalendar.add(Calendar.DAY_OF_YEAR, monday_offset)
        val mDateMonday = mCalendar.time

        // return 6 the next days of current day (object cal save current day)
        mCalendar.add(Calendar.DAY_OF_YEAR, 6)
        val mDateSunday = mCalendar.time

        //Get format date
        val strDateFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(strDateFormat)
        var MONDAY = sdf.format(mDateMonday)
        val SUNDAY = sdf.format(mDateSunday)

        // Sub String
//        if (MONDAY.substring(3, 6) == SUNDAY.substring(3, 6)) {
//            MONDAY = MONDAY.substring(0, 2)
//        }
        return "$MONDAY"
    }

    fun getFormattedMonthDate(date: Date?): String? {
        val cal = Calendar.getInstance()
        cal.time = date
        //2nd of march 2015

        return SimpleDateFormat("MMM''yy").format(cal.time)
    }
    fun getFormattedDay(date: Date?): String? {
        val cal = Calendar.getInstance()
        cal.time = date
        //2nd of march 2015
        val day = cal[Calendar.DATE]
        return if (!(day > 10 && day < 19)) when (day % 10) {
            1 -> SimpleDateFormat("d'st' MMM").format(date)
            2 -> SimpleDateFormat("d'nd' MMM").format(date)
            3 -> SimpleDateFormat("d'rd' MMM").format(date)
            else -> SimpleDateFormat("d'th' MMM").format(date)
        } else SimpleDateFormat("d'th'  MMM").format(date)
    }

fun rounded( number : Double) : Double{
    return Math.round(number * 100.0) / 100.0
}
    fun TextView.addImage(atText: String, @DrawableRes imgSrc: Int, imgWidth: Int, imgHeight: Int) {
        val ssb = SpannableStringBuilder(this.text)

        val drawable = ContextCompat.getDrawable(this.context, imgSrc) ?: return
        drawable.mutate()
        drawable.setBounds(0, 0,
            imgWidth,
            imgHeight)
        val start = text.indexOf(atText)
        ssb.setSpan(VerticalImageSpan(drawable), start, start + atText.length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        this.setText(ssb, TextView.BufferType.SPANNABLE)

    }

    class VerticalImageSpan(drawable: Drawable?) :
        ImageSpan(drawable!!) {
        /**
         * update the text line height
         */
        override fun getSize(
            paint: Paint, text: CharSequence?, start: Int, end: Int,
            fontMetricsInt: Paint.FontMetricsInt?
        ): Int {
            val drawable = drawable
            val rect: Rect = drawable.bounds
            if (fontMetricsInt != null) {
                val fmPaint: Paint.FontMetricsInt = paint.getFontMetricsInt()
                val fontHeight: Int = fmPaint.descent - fmPaint.ascent
                val drHeight: Int = rect.bottom - rect.top
                val centerY: Int = fmPaint.ascent + fontHeight / 2
                fontMetricsInt.ascent = centerY - drHeight / 2
                fontMetricsInt.top = fontMetricsInt.ascent
                fontMetricsInt.bottom = centerY + drHeight / 2
                fontMetricsInt.descent = fontMetricsInt.bottom
            }
            return rect.right
        }

        /**
         * see detail message in android.text.TextLine
         *
         * @param canvas the canvas, can be null if not rendering
         * @param text the text to be draw
         * @param start the text start position
         * @param end the text end position
         * @param x the edge of the replacement closest to the leading margin
         * @param top the top of the line
         * @param y the baseline
         * @param bottom the bottom of the line
         * @param paint the work paint
         */
        override fun draw(
            canvas: Canvas, text: CharSequence?, start: Int, end: Int,
            x: Float, top: Int, y: Int, bottom: Int, paint: Paint
        ) {
            val drawable = drawable
            canvas.save()
            val fmPaint: Paint.FontMetricsInt = paint.getFontMetricsInt()
            val fontHeight: Int = fmPaint.descent - fmPaint.ascent
            val centerY: Int = y + fmPaint.descent - fontHeight / 2
            val transY = centerY - (drawable.bounds.bottom - drawable.bounds.top) / 2
            canvas.translate(x, transY.toFloat())
            drawable.alpha = 255
            drawable.draw(canvas)
            canvas.restore()
        }
    }



    fun getDaysAgo(daysAgo: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.YEAR, -daysAgo)

        return calendar.time
    }

    fun getAge(millis: Long): Double {

       /* val year = dob.split("-")[0].toInt()
        val month = dob.split("-")[1].toInt()
        val day = dob.split("-")[2].toInt()*/
        var dob = Calendar.getInstance()
        val today = Calendar.getInstance()
        dob.timeInMillis = millis

        //create calendar object for current day
        //create calendar object for current day
        val currentTime = System.currentTimeMillis()
        val now = Calendar.getInstance()
        now.timeInMillis = currentTime

        //Get difference between years

        //Get difference between years
       var years = now[Calendar.YEAR] - dob.get(Calendar.YEAR)
        val currMonth = now[Calendar.MONTH] + 1
        val birthMonth: Int = dob.get(Calendar.MONTH) + 1

        //Get difference between months

        //Get difference between months
        var months = currMonth - birthMonth

        //if month difference is in negative then reduce years by one
        //and calculate the number of months.

        //if month difference is in negative then reduce years by one
        //and calculate the number of months.
        if (months < 0) {
            years--
            months = 12 - birthMonth + currMonth
            if (now[Calendar.DATE] < dob.get(Calendar.DATE)) months--
        } else if (months === 0 && now[Calendar.DATE] < dob.get(Calendar.DATE)) {
            years--
            months = 11
        }
        //Log.e("months", "$years $months")
        var dp = months.toDouble()
        while (dp > 1) {
            dp /= 10.0
        }

        var yearmonth = "$years.$months"
        //Log.e("getAge: ", "" + yearmonth)
        return yearmonth.toDouble()
    }

    fun showDialog(context: Context, title: String, message: String){
        androidx.appcompat.app.AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("Ok") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    /* fun convertEthnicity(ethncity: Int, context: Context): String? {
        val pos = ethncity
        return if (pos < 5) context.resources.getStringArray(R.array.ethnicities)[ethncity - 1] else context.resources.getStringArray(
            R.array.ethnicities
        )[context.resources.getStringArray(R.array.ethnicities).size - 1]

    }*/

    fun feetToCentimeter(feet: String): Double {
        var dCentimeter = 0.0
        if (!TextUtils.isEmpty(feet)) {
            if (feet.contains(".")) {
                val tempfeet = feet.toString().split(".").toTypedArray()
                if (!TextUtils.isEmpty(tempfeet[0])) {
                    if (Integer.parseInt(tempfeet[0])<8)
                        dCentimeter += java.lang.Double.valueOf(tempfeet[0]) * 30.48
                    else
                        dCentimeter = java.lang.Double.valueOf(tempfeet[0])
                }
                if (!TextUtils.isEmpty(tempfeet[1])) {
                    if (Integer.parseInt(tempfeet[0])<8)
                        dCentimeter += java.lang.Double.valueOf("0."+tempfeet[1]) * 2.54
                }
            }
            else{
                if (!TextUtils.isEmpty(feet)) {
                    if (Integer.parseInt(feet)<8)
                        dCentimeter += java.lang.Double.valueOf(feet) * 30.48
                    else
                        dCentimeter = java.lang.Double.valueOf(feet)
                }
            }


        }
        return dCentimeter
        //Format to decimal digit as per your requirement
    }

    fun getImageData(
        context: Context,
        jsonFile: String,
        objName: String?,
        characterType: String
    ): List<Avatar>? {

        val AvatarList: MutableList<Avatar> = ArrayList<Avatar>()


        try {
            val jsonArray =
                JSONArray(loadJSONFromAsset(context, jsonFile))
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            val jsonObjectData = jsonObject.getJSONObject(objName)
            val jsonArray1 = jsonObjectData.getJSONArray("data")
            for (i in 0 until jsonArray1.length()) {

                if (jsonFile == "Avatar.json") {
                    val jsonObj = jsonArray1.getJSONObject(i)
                    if (jsonObj.getString("type").toLowerCase() == characterType.toLowerCase()) {
                        val imageUrl =
                            jsonObj.getString(imageURL)
                        val avatar = Avatar()
                        avatar.resourceId = (resoureId(context, imageUrl))
                        avatar.imageURL = (imageUrl)
                        avatar.avatarColor = (jsonObj.getString("color"))
                        AvatarList.add(avatar)
                    }
                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return if (jsonFile == "Avatar.json") return AvatarList
        else null
    }


    fun resoureId(context: Context, imageUrl: String?): Int {
        val resources = context.resources
        try {
            return resources.getIdentifier(imageUrl, "drawable", context.packageName)
        } catch (e: Exception) {
        }
        return resources.getIdentifier("ic_male", "drawable", context.packageName)
    }

    fun loadJSONFromAsset(context: Context, jsonFile: String?): String? {
        var json: String? = null
        var inputStream: InputStream? = null
        try {
            inputStream = context.assets.open(jsonFile!!)
            val size = inputStream.available()
            Log.i("Load", "----->$size")
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            json = String(buffer, Charset.defaultCharset())
        } catch (ex: IOException) {
            ex.printStackTrace()
            return null
        } finally {
            assert(inputStream != null)
            try {
                inputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return json
    }


    fun show(context: Context?, s: String?) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }


    fun getTrialResult(
        testResult: List<TestResult>,
        trialCount: Int,
        context: Fragment,
        devicetype: Int,
        testType: String,
        sessionScore: String,
        variance: List<Variance>
    ) {

            val listTrialResult: MutableList<TrialResult> = ArrayList<TrialResult>()
            val airTestResult = AirTestResult()
            for (i in testResult.indices) {
                var airGraphDataList: MutableList<AirGraphData> = ArrayList()


                var trialResults: TrialResult = TrialResult()
                for (airgraphdata in testResult[i].graphPoints) {
                    val airGraphData: AirGraphData = AirGraphData()
                    airGraphData.volume = airgraphdata.volume
                    airGraphData.flow = airgraphdata.flow
                    airGraphData.second = airgraphdata.second
                    airGraphData.direction = airgraphdata.direction

                    airGraphDataList.add(airGraphData)
                }
                val testMeasurementsList: MutableList<TestMeasurements> = ArrayList()
                for (measuredValues in testResult[i].measuredValues) {
                    val testMeasurement: TestMeasurements = TestMeasurements()
                    testMeasurement.measurement = measuredValues.measurement
                    testMeasurement.measuredValue = measuredValues.measuredValue
                    testMeasurement.unit = measuredValues.unit
                    //if(measuredValues.predicted != " -  ")
                    testMeasurement.predictedValue = measuredValues.predicted
                    testMeasurement.lln = measuredValues.LLN
                    testMeasurement.uln = measuredValues.ULN
                    testMeasurement.zScore = measuredValues.zScore
                    testMeasurement.predictedPer = measuredValues.predictedPer
                    testMeasurementsList.add(testMeasurement)
                }


                val trialResult = TrialResult()
                trialResult.graphDataList = airGraphDataList
                trialResult.mesurementlist = testMeasurementsList
                trialResult.isBest = testResult[i].isBest
                trialResult.isPost = testResult[i].isPost
                if (trialResult.isPost) {


                    val airTestResult1 = AirTestResult()
                  /*  if (SafeyApplication.postTestResult==null){
                        if (trialCount-1 == i) {
                            Constants.postTrialCount = i*/
                            Constants.isPost = true
                            trialResult.isPost = true
                            val postTrialResult: MutableList<TrialResult> = ArrayList<TrialResult>()
                            postTrialResult.add(trialResult)
                            airTestResult1.trialResult = postTrialResult
                            airTestResult1.createdAt = Date()
                            airTestResult1.type = devicetype
                            airTestResult1.sessionScore = sessionScore
                            airTestResult1.testtype =  if(testType == "FEVC")  1 else 2
                            SafeyApplication.postTestResult = airTestResult1
                        /*}
                        else
                            listTrialResult.add(trialResult)
                    }
                    else*/
                    {
                        if (i == Constants.postTrialCount){
                            //Constants.isPost = true
                        }
                        else
                            listTrialResult.add(trialResult)
                    }
                }
                else {
                    listTrialResult.add(trialResult)
                    Constants.isPost = false
                }

            }

            val listVariance : MutableList<com.safey.lungmonitoring.data.tables.patient.Variance> = ArrayList()
            for (varance in variance){
                listVariance.add(com.safey.lungmonitoring.data.tables.patient.Variance(varance.measurement,varance.measurementValue,varance.percentage))
            }

            airTestResult.trialResult = listTrialResult
            airTestResult.createdAt = Date()
            airTestResult.type = devicetype
            airTestResult.sessionScore = sessionScore
            airTestResult.testtype =  if(testType == "FEVC")  1 else 2

            airTestResult.variance = listVariance
            //airTestResult.type = testResult[0].testType

            SafeyApplication.firstTestResult = airTestResult

        val action = if (SafeyApplication.postTestResult!=null)
          HomeFragmentDirections.actionGlobalTestResultsFragment(trialCount-1)
        else
          HomeFragmentDirections.actionGlobalTestResultsFragment(trialCount)
        NavHostFragment.findNavController(context).navigate(action)

    }


    fun showToast(context: Context, s: String) {
        Toast.makeText(context, s, Toast.LENGTH_LONG).show()
    }


    fun getFrequency(context: Context): MutableList<MedFrequency> {
        val frequencyList: MutableList<MedFrequency> = ArrayList<MedFrequency>()
       for (medfrequecy in enumMedicationFrequency.values()){
           frequencyList.add(MedFrequency(medfrequecy.value,medfrequecy.name))
       }
        return frequencyList
    }

    fun getMedicationsData(
        context: Context?,
        jsonFile: String?,
        objName: String?
    ): List<String>? {
        val medicationList: MutableList<String> = java.util.ArrayList<String>()
       // var medication: Medication
        try {
            val jsonArray =
                JSONArray(loadJSONFromAsset(context!!, jsonFile))
            for (i in 0 until jsonArray.length()) {
                val jsonObject: JSONObject = jsonArray.getJSONObject(i)
                val jsonObjectData = jsonObject.getJSONObject(objName)
                val jsonArray1 = jsonObjectData.getJSONArray("data")
                for (j in 0 until jsonArray1.length()) {
                    val jsonObject1 = jsonArray1.getJSONObject(j)
                  //  medication = Medication()
//                    medication.medicationName=jsonObject1.getString("name")
//                    medication.strength=jsonObject1.getString("strength")
                    medicationList.add(jsonObject1.getString("name") + " " + jsonObject1.getString("strength"))
                }
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        return medicationList
    }
    fun getMedIconsImageData(context: Context): MutableList<MedIcon> {
        val avatarList: MutableList<MedIcon> = ArrayList<MedIcon>()
        try {
            val jsonArray =
                JSONArray(
                    loadJSONFromAsset(
                        context,
                        Constants.MED_ICON_JSON_FILENAME
                    )
                )
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            val jsonArray1 = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray1.length()) {
                val jsonObj = jsonArray1.getJSONObject(i)
                val imageUrl = jsonObj.getString(Constants.imageURL)

                avatarList.add(
                    MedIcon(
                        jsonObj.getInt("id"),
                        imageUrl,
                        resoureId(context, imageUrl),
                        "#000000"
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return avatarList
    }
    fun getMedColorData(context: Context): MutableList<MedColors> {
        val avatarList: MutableList<MedColors> = ArrayList<MedColors>()
        try {
            val jsonArray =
                JSONArray(
                    loadJSONFromAsset(
                       context,
                        Constants.MED_COLOR_JSON_FILENAME
                    )
                )
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            val jsonArray1 = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray1.length()) {
                val jsonObj = jsonArray1.getJSONObject(i)
                avatarList.add(
                    MedColors(
                        jsonObj.getInt("id"),
                        jsonObj.getString("colorcode")
                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return avatarList
    }

    fun getSymptonsData(context: Context): MutableList<SymptomModel> {
        val symptomList: MutableList<SymptomModel> = ArrayList<SymptomModel>()
        try {
            val jsonArray =
                JSONArray(
                    loadJSONFromAsset(
                        context,
                        Constants.Sympton_JSON_FILENAME
                    )
                )
            val jsonObject: JSONObject = jsonArray.getJSONObject(0)
            val jsonArray1 = jsonObject.getJSONArray("data")
            for (i in 0 until jsonArray1.length()) {
                val jsonObj = jsonArray1.getJSONObject(i)
                val imageUrl = jsonObj.getString(Constants.imageURL)
                symptomList.add(
                    SymptomModel(
                        jsonObj.getInt("id"),
                        imageUrl,
                        resoureId(context, imageUrl),
                        jsonObj.getString(Constants.sympton)


                    )
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return symptomList
    }

}