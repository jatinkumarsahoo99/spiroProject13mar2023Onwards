package com.safey.lungmonitoring.ui.dashboard.reports

import android.Manifest
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.itextpdf.io.image.ImageDataFactory
import com.itextpdf.kernel.colors.WebColors
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfDocumentInfo
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.*
import com.itextpdf.layout.property.*
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.custombinings.longDate
import com.safey.lungmonitoring.custombinings.posToGender
import com.safey.lungmonitoring.custombinings.positionToEthnicity
import com.safey.lungmonitoring.data.datautils.enumHeightUnit
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.ui.dashboard.home.SectionModel
import com.safey.lungmonitoring.ui.dashboard.reports.viewmodel.ReportViewModel
import com.safey.lungmonitoring.utils.*
import com.safey.lungmonitoring.utils.PrintUtility.copyLogo
import com.safey.lungmonitoring.utils.PrintUtility.keyValuePairParagraphPdf
import com.safey.lungmonitoring.utils.PrintUtility.pdfText
import com.safey.lungmonitoring.utils.PrintUtility.pdfTextA4
import dagger.hilt.android.AndroidEntryPoint
import info.safey.graph.charts.LineChart
import info.safey.graph.components.XAxis
import info.safey.graph.data.Entry
import info.safey.graph.data.LineData
import info.safey.graph.data.LineDataSet
import info.safey.graph.formatter.ValueFormatter
import info.safey.graph.renderer.XAxisRenderer
import info.safey.graph.utils.MPPointF
import info.safey.graph.utils.Transformer
import info.safey.graph.utils.ViewPortHandler
import kotlinx.android.synthetic.main.fragment_download.*
import kotlinx.android.synthetic.main.safey_cancel_button.view.*
import kotlinx.android.synthetic.main.safey_save_button.view.*
import java.io.*
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


@AndroidEntryPoint
class DownloadFragment : Fragment(R.layout.fragment_download){
    private var testType: Int = 0
    private lateinit var avgResultLabel: String
    private lateinit var measurementsArray: List<String>
    private lateinit var measurements: String
    private lateinit var fileShare: File
    private var reportType: Int= 0
    private var reportRangeType: Int = 0
    private lateinit var startdate: Date
    private var totalTest: Int = 0
    var listsectionModelall: MutableList<SectionModel> = ArrayList()
    private lateinit var futureDate: Date
    private lateinit var previousDate: Date
    private lateinit var printModel: PrintModel
    val reportViewModel : ReportViewModel by viewModels()
    private lateinit var currentDate: Date
    private  var listBitmaps: MutableList<Bitmap> = ArrayList()
    private  var listMaxMeasurements: MutableList<Double> = ArrayList()
    var fileWriter: FileWriter? = null


    lateinit var bw: BufferedWriter



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().popBackStack(R.id.nav_home,false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

         reportType = arguments?.getInt("reportType", 0)!!
         testType = arguments?.getInt("testType", 0)!!
         reportRangeType = arguments?.getInt("reportRangeType", 0)!!
         measurements = arguments?.getString("Measurements", "")!!

        Log.e( "onViewCreated: ",measurements )
        copyLogo(requireContext())
        //binding.pdfView.setBackgroundColor(TRANSPARENT)
        printModel = PrintModel(
            title = getString(R.string.print_title),
            author = getString(R.string.print_author),
            subject = getString(R.string.print_subject),
            keywords = getString(R.string.print_keywords),
            creator = getString(R.string.print_creator),
            extFileDir = activity?.getExternalFilesDir(null).toString(),
            folderName = getString(R.string.print_file_path),
            receiptFileName = getString(R.string.print_receipt_name),
            dob = getString(R.string.print_dob_label),
            gender = getString(R.string.gender),

            file_folder_path = getString(R.string.print_file_folder_path),
            pdf_filename = getString(R.string.print_pdf_filename),
            font_light = getString(R.string.mediumfont),
            font_bold = getString(R.string.mediumfont),
            utf = getString(R.string.UTF)
        )

        currentDate = Calendar.getInstance().time


        futureDate = Utils().getFutureDate(currentDate)!!
        previousDate = Utils().getPreviousDate(currentDate)!!
       avgResultLabel = when (reportRangeType) {
            Utility.ReportRangeType.WEEK.ordinal ,Utility.ReportRangeType.LASTWEEK.ordinal -> getString(R.string.weekly_avg_results)
            Utility.ReportRangeType.THISMONTH.ordinal ,
            Utility.ReportRangeType.LASTMONTH.ordinal,
            Utility.ReportRangeType.LAST3MONTH.ordinal ,
            Utility.ReportRangeType.LAST6MONTH.ordinal ->  getString(R.string.monthly_avg_results)
           else ->  getString(R.string.weekly_avg_results)
       }


        startdate = Constants.reportStartDate
        val date = Constants.reportEndDate
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar[Calendar.HOUR_OF_DAY] = 23 // for 6 hour

        calendar[Calendar.MINUTE] = 59 // for 0 min

        calendar[Calendar.SECOND] = 59 // for 0 sec
        currentDate = calendar.time

        Log.e( "onViewCreated: ", startdate.toString())
        Log.e( "onViewCreated: ", currentDate.toString())

        reportViewModel.patient.observeChange(viewLifecycleOwner, Observer {
            processTestData(testresult = reportViewModel.testList)
        })
        safey_cancel_button.button_cancel.text = getString(R.string.share)

        safey_save_button.button_save.isEnabled = false
        safey_save_button.button_save.alpha = Constants.alphaBlur

        if (reportType==Utility.ReportType.PDF.ordinal){
            reportTypeImage.setImageResource(R.drawable.ic_pdf)
            safey_save_button.button_save.text = getString(R.string.viewreport)
        }
        else {
            safey_save_button.button_save.text = getString(R.string.viewcsv)
            writeToCSV(type = reportRangeType)
            reportTypeImage.setImageResource(R.drawable.ic_csv)
        }
        safey_save_button.button_save.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
               // if (reportType==Utility.ReportType.PDF.ordinal) {
                    //safey_save_button.button_save.isEnabled = false
                    //safey_save_button.button_save.alpha = Constants.alphaBlur

                val contextWrapper = ContextWrapper(requireContext())
               val downloaddir =contextWrapper.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                    val file = File(
                        Environment.getExternalStorageDirectory(),
                        Environment.DIRECTORY_DOWNLOADS
                    )
                val file1 = File(file,"SafeyReports")
                    if (!file1.exists()) {
                        file1.mkdir()
                    }
                    //Utility.moveFile(fileShare, file1)

               // val path = File(getFilesDir(), "dl")
               // val file = File(path, filename)

                // Get URI and MIME type of file

                // Get URI and MIME type of file
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    fileShare
                )
                val mime: String? = requireActivity().contentResolver.getType(uri)

                // Open file with user selected app

                // Open file with user selected app
                val intent = Intent()
                intent.action = Intent.ACTION_VIEW
                intent.setDataAndType(uri, mime)
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                startActivity(intent)
//                    Utility.showDialog(
//                        requireContext(),
//                        "Report Download ",
//                        "Report dowloaded in Downloads/SafeyReports folder successfully."
//                    )
                }
          //  }

        })

        safey_cancel_button.button_cancel.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
              //  if (reportType==Utility.ReportType.PDF.ordinal) {
                    val outputFile = fileShare
                    //val uri: Uri = Uri.fromFile(outputFile)
               val uri = FileProvider.getUriForFile(requireContext(), requireContext().applicationContext.packageName + ".provider", outputFile);
                    val share = Intent()
                    share.action = Intent.ACTION_SEND
                if (reportType==Utility.ReportType.PDF.ordinal)
                    share.type = "application/pdf"
                else
                    share.type = "text/csv"
                    share.putExtra(Intent.EXTRA_STREAM, uri)
                    // share.setPackage("com.whatsapp")

                    requireActivity().startActivity(share)
                }
           // }

        })
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
               askStoragePermissions()
            } else {
                reportViewModel.getTestData(startdate,currentDate)
                safey_save_button.button_save.isEnabled = true
                safey_save_button.button_save.alpha = Constants.alphaClear
            }
        }
        else {
            reportViewModel.getTestData(startdate,currentDate)
            safey_save_button.button_save.isEnabled = true
            safey_save_button.button_save.alpha = Constants.alphaClear
        }
    }
    val storagePermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == true -> {
                safey_save_button.button_save.isEnabled = true
                safey_save_button.button_save.alpha = Constants.alphaClear
                reportViewModel.getTestData(startdate,currentDate)

            }
            else -> {
                safey_save_button.button_save.isEnabled = false
                safey_save_button.button_save.alpha = Constants.alphaBlur
                Log.e("TAG", "deny location " )
            }
        }
    }
    private fun askStoragePermissions(){
        storagePermissionRequest.launch(arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE))
    }
    private fun loadPdf(file: File) {
        pdfView.fromFile(file)
            .scrollHandle(DefaultScrollHandle(requireActivity()))
            .pageFitPolicy(FitPolicy.BOTH)
            .nightMode(false)
            .load()
      //  pdfView.visibility = View.VISIBLE
    }


    fun processTestData(testresult: List<AirTestResult>) {
        val hashMap: LinkedHashMap<String, MutableList<AirTestResult>> = LinkedHashMap()
        listsectionModelall = ArrayList()

        val simpleDateFormat1 = SimpleDateFormat(Constants.DATE_FORMAT_PATTERN)

        Collections.sort(
            testresult
        ) { p0, p1 ->
            simpleDateFormat1.parse(p0!!.createdDate)
                .compareTo(simpleDateFormat1.parse(p1!!.createdDate));
        }

        for (i in testresult.indices) {

            val sortDate: String? = testresult[i].createdDate

            if (reportRangeType == Utility.ReportRangeType.THISMONTH.ordinal || reportRangeType == Utility.ReportRangeType.LASTMONTH.ordinal || reportRangeType == Utility.ReportRangeType.LAST3MONTH.ordinal) {
                val calendar = Calendar.getInstance()
                calendar.time = testresult[i].createdAt

                val weekOfMonth = calendar[Calendar.WEEK_OF_MONTH]
                val section = if (reportRangeType == Utility.ReportRangeType.LAST3MONTH.ordinal)
                    "Wk $weekOfMonth"
                else
                    "Week $weekOfMonth"
                if (hashMap.containsKey(section)) {
                    hashMap[section]!!.add(testresult[i])
                } else {
                    val dummyList: ArrayList<AirTestResult> = ArrayList<AirTestResult>()
                    dummyList.add(testresult[i])
                    hashMap.put(section, dummyList)

                }


            } else if (reportRangeType == Utility.ReportRangeType.WEEK.ordinal || reportRangeType == Utility.ReportRangeType.LASTWEEK.ordinal) {
                if (hashMap.containsKey(sortDate)) {
                    hashMap[sortDate]!!.add(testresult[i])
                } else {
                    val dummyList: ArrayList<AirTestResult> = ArrayList<AirTestResult>()
                    dummyList.add(testresult[i])
                    hashMap.put(sortDate!!, dummyList)
                }


            } else {
                val calendar = Calendar.getInstance()
                calendar.time = testresult[i].createdAt
                val section = Utility.getFormattedMonthDate(calendar.time)

                if (hashMap.containsKey(section)) {
                    hashMap[section]!!.add(testresult[i])
                } else {
                    val dummyList: ArrayList<AirTestResult> = ArrayList<AirTestResult>()
                    dummyList.add(testresult[i])
                    hashMap.put(section!!, dummyList)

                }


            }
            if (i == testresult.size - 1) {
                listsectionModelall = ArrayList()
                val testtype = when(testType) {
                    0 -> 1
                    1 -> 2
                    else ->1
                }
                for (key in hashMap.keys) {

                    val sectionModel = SectionModel()
                    sectionModel.section = key
                    sectionModel.date = hashMap[key]!![0].createdDate
                    sectionModel.testResult = hashMap[key]
                    if (sectionModel.testResult?.any { it.testtype == testtype } == true)
                        listsectionModelall.add(sectionModel)
                }


            }
        }

        if (listsectionModelall.size>0) {

            measurementsArray = measurements.split(",")
            if (reportType == Utility.ReportType.PDF.ordinal) {
                for (i in measurementsArray)
                    setupCharts(chart, i)
            } else {

                for (i in 0 until listsectionModelall.size) {

                    for (j in 0 until listsectionModelall[i].testResult?.size!!) {
                        ++totalTest
                        for (measurement in measurementsArray) {

                            if (listsectionModelall[i].testResult!![j].trialResult!!.filter { it.isBest }[0].mesurementlist!!.filter { it.measurement == measurement }
                                    .isNotEmpty()) {
                                //listfvc.add(listsectionModelall[i].testResult!![j].trialResult!!.filter { it.isBest }[0].mesurementlist!!.filter { it.measurement == measurement }[0].measuredValue)
                                bw.append("$measurement,")
                                val data = listsectionModelall[i].testResult!![j]
                                bw.append(
                                    data.createdDate + ","
                                            + data.testtime + ","
                                )
                                val measuredData =
                                    listsectionModelall[i].testResult!![j].trialResult!!.filter { it.isBest }[0].mesurementlist!!.filter { it.measurement == measurement }[0]
                                // listsectionModelall[i].testResult!![j].trialResult!!.filter { it.isBest }[0].mesurementlist!!.filter { it.measurement == measurement }[0].measuredValue
                                bw.append("" + measuredData.measuredValue + "," + measuredData.predictedValue + "," + measuredData.predictedPer + "," + measuredData.lln + "," + measuredData.uln + "," + measuredData.zScore + "," + data.sessionScore)
                                bw.append('\n')
                            }
                        }
                        bw.append('\n')
                        bw.append('\n')

                    }

                }
                bw.close()
            }
        }
        else{
            Utility.showDialog(
                requireContext(),
                "Report data error",
                "No test results recorded to generate reports."
            )

//            androidx.appcompat.app.AlertDialog.Builder(requireContext())
//                .setTitle("Report data error")
//                .setMessage("No test results recorded to generate reports.")
//                .setCancelable(false)
//                .setPositiveButton("Ok") { dialog, _ ->
//                    dialog.cancel()
//                }
//                .show()

            safey_cancel_button.button_cancel.isEnabled = false
            safey_save_button.button_save.isEnabled = false

            safey_cancel_button.button_cancel.alpha = Constants.alphaBlur
            safey_save_button.button_save.alpha = Constants.alphaBlur
        }
    }


    private fun setupCharts(chart: LineChart, xAxisTitle: String) {
        chart.setGridBackgroundColor(android.graphics.Color.TRANSPARENT)
        chart.legend.isEnabled = false
        chart.legend.textColor = ContextCompat.getColor(requireContext(), R.color.report_graph_blue)
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setPinchZoom(false)
        chart.extraBottomOffset = 50f
        chart.setXAxisRenderer(CustomXAxisRenderer(chart.viewPortHandler, chart.xAxis, chart.getTransformer(chart.axisLeft.axisDependency),chart = chart))
        val xl = chart.xAxis
        xl.setAvoidFirstLastClipping(true)
        xl.axisMinimum = -1f
        xl.axisMaximum = 7f

        xl.setPosition(XAxis.XAxisPosition.BOTTOM)
        xl.setDrawGridLines(true)
        xl.setDrawAxisLine(true)

        val xAxisFormatter: ValueFormatter = DayAxisValueFormatter(chart,listsectionModelall,reportRangeType)
        xl.labelCount = 7
        //xl.mForceLabels = true
        xl.textColor = ContextCompat.getColor(requireContext(), R.color.report_graph_blue)
        xl.isEnabled = true
        // xl.axisLineColor = ContextCompat.getColor(requireContext(), R.color.white)
        xl.setDrawLabels(true)
        xl.gridColor = ContextCompat.getColor(requireContext(), R.color.report_axis_color)

        xl.valueFormatter = xAxisFormatter



        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = ContextCompat.getColor(requireContext(), R.color.report_axis_color)
        //leftAxis.setLabelCount(10,true)
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.report_graph_blue)
        leftAxis.isEnabled = true
        leftAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.report_axis_color)
        //leftAxis.setAxisLineDashedLine(DashPathEffect(floatArrayOf(1f),1f))
        leftAxis.setDrawLabels(true)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawGridLines(true)
        leftAxis.enableGridDashedLine(5f, 5f, 2f);
        leftAxis.valueFormatter = MyDecimalValueFormatter()



        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        setData(xAxisTitle)

        chart.invalidate()
    }
    class MyDecimalValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.0f",value)
        }

    }
    class DayAxisValueFormatter(
        private val chart: LineChart,
        var listsectionModelall: MutableList<SectionModel>,var reportRangeType: Int
    ) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String? {
            return if (value.toInt()<listsectionModelall.size && value.toInt()>=0) {
                if (reportRangeType == Utility.ReportRangeType.WEEK.ordinal || reportRangeType == Utility.ReportRangeType.LASTWEEK.ordinal)
                    getDayFromStringDate(listsectionModelall[value.toInt()].date,Constants.DATE_FORMAT_PATTERN,true)
                else
                    listsectionModelall[value.toInt()].section
            }
            else ""
//            return if (listsectionModelall.size>0)
//                when {
//                    value.toInt()== -> if (listsectionModelall.size>0) listsectionModelall[0].section else ""
//                    value.toInt() == 3 -> if (listsectionModelall.size>1) listsectionModelall[1].section else ""
//                    value.toInt()==6 -> if (listsectionModelall.size>2) listsectionModelall[2].section else ""
//                    else -> ""
//                }
//            else
//                ""
        }
        @Throws(ParseException::class)
        fun getDayFromStringDate(
            stringDate: String?,
            dateFormat: String?,
            abbreviated: Boolean
        ): String? {
            val pattern: String
            pattern = if (abbreviated) {
                "E\ndd" // For short day eg: Mon,Tue
            } else {
                "EEEE" // For compete day eg: Monday, Tuesday
            }
            return SimpleDateFormat(pattern)
                .format(SimpleDateFormat(dateFormat).parse(stringDate))
        }
    }

    class CustomXAxisRenderer(viewPortHandler: ViewPortHandler?, xAxis: XAxis?, trans: Transformer?, chart: LineChart) : XAxisRenderer(viewPortHandler, xAxis, trans) {
        override fun drawLabel(c: Canvas?, formattedLabel: String, x: Float, y: Float, anchor: MPPointF?, angleDegrees: Float) {
            val line: List<String> = formattedLabel.split("\n")
            info.safey.graph.utils.Utils.drawXAxisValue(c, line[0], x, y, mAxisLabelPaint, anchor, angleDegrees)
            for (i in 1 until line.size) {
                info.safey.graph.utils.Utils.drawXAxisValue(c, line[i], x, y + mAxisLabelPaint.textSize * i,
                    mAxisLabelPaint, anchor, angleDegrees)
            }
        }
    }
    private fun setData(measurement: String) {
        val entries = ArrayList<Entry>()
        //val listxvalues = arrayListOf<>(7,{0})
         totalTest = 0
        val testtype = when(testType) {
            0 -> 1
            1 -> 2
            else ->1
        }
        var listfvc = ArrayList<Double>()
        for (i in 0 until listsectionModelall.size) {
            val xVal = i
            var total = 0.0
            var count =0
            for (j in 0 until listsectionModelall[i].testResult?.size!!) {
                if (listsectionModelall[i].testResult!![j].testtype == testtype) {
                    ++totalTest

                   val data =  if (listsectionModelall[i].testResult!![j].trialResult!!.any { it.isPost }){
                        listsectionModelall[i].testResult!![j].trialResult!!.filter { it.isPost }[0]
                    }
                    else
                        listsectionModelall[i].testResult!![j].trialResult!!.filter { it.isBest }[0]
                    if (data.mesurementlist!!.filter { it.measurement == measurement }
                            .isNotEmpty()) {
                        listfvc.add(data.mesurementlist!!.filter { it.measurement == measurement }[0].measuredValue)
                        ++count
                        total += data.mesurementlist!!.filter { it.measurement == measurement }[0].measuredValue
                    }
                }
            }

            if(total>0){
                val yVal = (total / count)
                val yvalue = (Constants.decimalTimeFormat.format(yVal)).toFloat()
                Log.e( "setData: ",""+yvalue+"" )
                entries.add(Entry(xVal.toFloat(), yvalue))
            }
            else
                entries.add(Entry(xVal.toFloat(), 0.0f))
        }


        // create a dataset and give it a type
        val set1 = LineDataSet(entries, "DataSet 1")
        set1.setDrawCircleHole(true)
        set1.setDrawCircles(true)

        set1.lineWidth = 1.5f
        set1.circleRadius = 4f
        set1.setCircleColor(ContextCompat.getColor(requireContext(), R.color.report_graph_blue))
        set1.circleHoleColor = (ContextCompat.getColor(requireContext(), R.color.report_graph_blue))
        set1.valueTextColor = ContextCompat.getColor(requireContext(), R.color.report_graph_blue)
        set1.setMode(LineDataSet.Mode.LINEAR)
        set1.setFillAlpha(100);
        set1.setDrawFilled(true);
        set1.setFillColor(ContextCompat.getColor(requireContext(), R.color.report_fillcolor))
        set1.setColor(ContextCompat.getColor(requireContext(), R.color.report_graph_blue))
        // create a data object with the data sets
        val data = LineData(set1)

        data.setValueTextSize(9f)
        data.setDrawValues(true)

        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.report_graph_blue))
        data.notifyDataChanged()
        // set data
        chart.data = data
        if (listfvc.size>0) {
            listMaxMeasurements.add( Collections.max(listfvc))
            listBitmaps.add(chart.chartBitmap)
            if (measurementsArray[measurementsArray.size-1] == measurement){
            if (reportType == Utility.ReportType.PDF.ordinal)
                createPDF(reportRangeType)
            }
        }
        else {
            Utility.showDialog(
                requireContext(),
                "Report data error",
                "No test results recorded to generate reports."
            )

            safey_cancel_button.button_cancel.isEnabled = false
            safey_save_button.button_save.isEnabled = false

            safey_cancel_button.button_cancel.alpha = Constants.alphaBlur
            safey_save_button.button_save.alpha = Constants.alphaBlur

        }
        //document.add(myImg)
    }

    fun writeToCSV(type:Int) {


        val filename = when (type) {
            type -> Utility.ReportRangeType.values()[type].name
            else -> "safey"
        }
        try {

            var f1 = File(requireContext().getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f1.delete()
            if (!f1.exists()) {
                f1.mkdirs()
            }
            fileShare = File(f1, filename + ".csv")







            //lateinit var bw1: BufferedWriter
            val csvHeader =
                "Parameter,Date,Time,Measured,Predicted,Predicted %,LLN Score,ULN Score,Z-Score,Session Quality"
           val range = when (reportRangeType)
            {
                Utility.ReportRangeType.WEEK.ordinal -> getString(R.string.this_week)
               else -> ""
            }
            val header = "Report Published by : Safey Medical Devices,,,Report Date: "+Utility.getFormattedDate(Date())+",,Report Range "+range+"  - "+Utility.getFormattedDay(startdate)+" to "+Utility.getFormattedDay(currentDate)+"\n\n"
            val gpxfile = fileShare
            try {
                fileWriter = FileWriter(gpxfile)
                bw = BufferedWriter(fileWriter)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                bw = BufferedWriter(fileWriter)

            } catch (e: IOException) {
                e.printStackTrace()
            }
            bw.append(header)
            bw.append('\n')
            bw.append(csvHeader)
            bw.append('\n')

           // bw.close()
        }catch (e:java.lang.Exception){

        }
    }

    fun createPDF(type : Int) {

        //Create document file
        val filename = when(type){
            type -> Utility.ReportRangeType.values()[type].name
            else -> "safey"
        }
        try {
            val format = SimpleDateFormat("dd-MMM-yyyy HH:mm:ss a")
            val dateFormat = SimpleDateFormat("ddMMyyyy_HHmm")
            var f1 = File(activity?.getExternalFilesDir(null), "Safey")
            if (!f1.exists()) {
                f1.mkdirs()
            }
            f1.delete()
            if (!f1.exists()) {
                f1.mkdirs()
            }
            fileShare = File(f1, filename+".pdf")
            // f = File(filePath + File.separator + activity?.getString(R.string.print_file_path) + File.separator + activity?.getString(R.string.print_receipt_name))
            // f.createNewFile()
            val outputStream = FileOutputStream(fileShare)
            val writer: PdfWriter = PdfWriter(outputStream)
            val pdfDocument = PdfDocument(writer)

            val info: PdfDocumentInfo = pdfDocument.documentInfo

            info.title = printModel.title
            info.author = printModel.author
            info.subject = printModel.subject
            info.keywords = printModel.keywords
            info.creator = printModel.creator
            val document =
                Document(pdfDocument, PageSize(PageSize.A4.width, PageSize.A4.height), true)


            var table = Table(2)
            table.setAutoLayout()
            table.setBorder(Border.NO_BORDER)
            table.setWidth(PageSize.A4.width)
            table.setPaddings(16f,0f,16f,16f)
            val fileImage = File(f1,"safey.png")
            val imageUri = fileImage.absolutePath
            val img1 = Image(ImageDataFactory.create(imageUri))
            img1.setHorizontalAlignment(HorizontalAlignment.LEFT)
            img1.setWidth(111f)
            var cell = Cell()
            cell.setBorder(Border.NO_BORDER)
            cell.setWidth(111f)
            cell.setHorizontalAlignment(HorizontalAlignment.LEFT)
            cell.add(img1)


            table.addCell(cell)
            cell = Cell()
            var p = Paragraph()
            p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
            p.setPaddingRight(8f)
            p.setWidth(430f)
            cell.setWidth(430f)
            cell.setBorder(Border.NO_BORDER)


            val patientDetails: MutableMap<String, String> = LinkedHashMap()
            patientDetails["Name"] = reportViewModel.patientData.FirstName
            patientDetails["Height"] = reportViewModel.patientData.Height +" "+ enumHeightUnit.fromInt(reportViewModel.patientData.HeightUnit).getFormatString()
            patientDetails["Birth Date"] = Utility.getFormattedDate(longDate(reportViewModel.patientData.BirthDate))!!
            patientDetails["Age"] = ""+Utility.getAge(reportViewModel.patientData.BirthDate)+"yrs"
            patientDetails["Gender"] = posToGender(reportViewModel.patientData.Gender)
            patientDetails["Ethnicity"] = positionToEthnicity(reportViewModel.patientData.ethnicity)

            p.add(keyValuePairParagraphPdf(patientDetails))

            cell.setHorizontalAlignment(HorizontalAlignment.RIGHT)
            // cell.setMarginRight(16f)
            cell.add(p)
            table.addCell(cell)
            document.add(table)



            var paragraph = Paragraph(pdfText("SAFEY SPIROMETERY RESULTS","#000000",25f))
            paragraph.setMarginTop(32f)
            paragraph.setTextAlignment(TextAlignment.CENTER)
            document.add(paragraph)

            paragraph = Paragraph(pdfText("Date Range- "+Utility.getFormattedDay(startdate)+" - "+Utility.getFormattedDay(currentDate),"#000C86",16f))
            paragraph.setTextAlignment(TextAlignment.CENTER)


            document.add(paragraph)

            val reportImage = File(f1,"reportcover.png")
            val imagereportUri = reportImage.absolutePath
            val img2 = Image(ImageDataFactory.create(imagereportUri))
            img2.setHorizontalAlignment(HorizontalAlignment.CENTER)
            document.add(img2)

            p = Paragraph(PrintUtility.pdfText("Re","#000C86"))
            p.setFixedPosition(0f,0f,PageSize.A4.width)
            p.setBackgroundColor(WebColors.getRGBColor("#000C86"))

            p.add(pdfText("Report  by- Safey Spirometer App","#ffffff"))


            p.add(Tab())
            p.addTabStops(TabStop(900F, TabAlignment.RIGHT))
            p.add(pdfText("Report Date- "+Utility.getFormattedDate(Date()),"#ffffff"))
            p.add(pdfText("Re","#000C86"))
            document.add(p)




            for (i in 0 until listBitmaps.size) {
                table = Table(2)
                table.setAutoLayout()
                table.setBorder(Border.NO_BORDER)
                table.setWidth(PageSize.A4.width)
                table.setPaddings(16f, 0f, 16f, 16f)

                img1.setHorizontalAlignment(HorizontalAlignment.LEFT)
                img1.setWidth(111f)
                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                cell.setWidth(111f)
                cell.setHorizontalAlignment(HorizontalAlignment.LEFT)
                cell.add(img1)


                table.addCell(cell)
                cell = Cell()
                p = Paragraph()
                p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                p.setPaddingRight(8f)
                p.setWidth(430f)
                cell.setWidth(430f)
                cell.setBorder(Border.NO_BORDER)


                //val patientDetails: MutableMap<String, String> = LinkedHashMap()
                patientDetails["Name"] = reportViewModel.patientData.FirstName
                patientDetails["Height"] =
                    reportViewModel.patientData.Height + " " + enumHeightUnit.fromInt(
                        reportViewModel.patientData.HeightUnit
                    ).getFormatString()
                patientDetails["Birth Date"] =
                    Utility.getFormattedDate(longDate(reportViewModel.patientData.BirthDate))!!
                patientDetails["Age"] =
                    "" + Utility.getAge(reportViewModel.patientData.BirthDate) + "yrs"
                patientDetails["Gender"] = posToGender(reportViewModel.patientData.Gender)
                patientDetails["Ethnicity"] =
                    positionToEthnicity(reportViewModel.patientData.ethnicity)

                p.add(keyValuePairParagraphPdf(patientDetails))

                cell.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                // cell.setMarginRight(16f)
                cell.add(p)
                table.addCell(cell)
                document.add(table)

                table = Table(2)
                table.setBorder(Border.NO_BORDER)
                table.setMarginTop(8f)
                table.setWidth(540f)
                p = Paragraph()
                    .add(
                        pdfText(
                            "Date Range- " + Utility.getFormattedDay(startdate) + " - " + Utility.getFormattedDay(
                                currentDate
                            ), "#000000", 16f
                        )
                    )
                    .addTabStops(TabStop(1000f, TabAlignment.RIGHT))
                    .add(Tab())
                table.addCell(Cell().setBorder(Border.NO_BORDER).add(p))
                p = Paragraph()
                    .add(pdfText("Total Tests Conducted: $totalTest", "#000C86", 16f, ""))
                    .add(pdfText("Tot", "#F5E4FF", 16f, ""))
                table.addCell(
                    Cell().setTextAlignment(TextAlignment.RIGHT).setPadding(4f)
                        .setBorder(Border.NO_BORDER).setBorderTopLeftRadius(BorderRadius(24f))
                        .setBorderTopRightRadius(BorderRadius(24f))
                        .setBorderBottomLeftRadius(BorderRadius(24f))
                        .setBorderBottomRightRadius(BorderRadius(24f))
                        .setBackgroundColor(WebColors.getRGBColor("#F5E4FF"))
                        .setBorder(Border.NO_BORDER).add(p)
                )
                document.add(table)



                p = Paragraph(pdfText("Re", "#000C86"))
                p.setFixedPosition(0f, 0f, PageSize.A4.width)
                p.setBackgroundColor(WebColors.getRGBColor("#000C86"))

                p.add(pdfText("Report  by- Safey Spirometer App", "#ffffff"))



                p.add(Tab())
                p.addTabStops(TabStop(1000F, TabAlignment.RIGHT))
                p.add(pdfText("Report Date- " + Utility.getFormattedDate(Date()), "#ffffff"))
                p.add(pdfText("Re", "#000C86"))
                document.add(p)

                var tablemain = Table(1)
                tablemain.setWidth(540f)
                tablemain.setMarginTop(16f)

                var table1 = Table(3)
                table1.setWidth(540f)
                // table1.setMarginTop(16f)
                table1.setBorder(Border.NO_BORDER)
                p = Paragraph(pdfText("${measurementsArray[i]}", "#000000", 18f))
                    .add(pdfText("Tv", "#F5E4FF", 18f, ""))
                    .setVerticalAlignment(VerticalAlignment.MIDDLE)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setHorizontalAlignment(HorizontalAlignment.LEFT)
                    .setPaddingTop(3f).setBorder(Border.NO_BORDER)
                    .setBorderTopLeftRadius(BorderRadius(24f))
                    .setBorderTopRightRadius(BorderRadius(0f))
                    .setBorderBottomLeftRadius(BorderRadius(0f))
                    .setBorderBottomRightRadius(BorderRadius(24f))
                    .setBackgroundColor(WebColors.getRGBColor("#E9EBFF"))
                table1.addCell(Cell().setBorder(Border.NO_BORDER).add(p)).setExtendBottomRow(false)
                p = Paragraph(pdfText(avgResultLabel, "#000C86", 16f)).setTextAlignment(
                    TextAlignment.CENTER
                )
                table1.addCell(
                    Cell().setVerticalAlignment(VerticalAlignment.MIDDLE)
                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                        .setBorder(Border.NO_BORDER).add(p)
                )
                p = Paragraph(pdfTextA4("Highest ${measurementsArray[i]}\n", true, "#000C86", 12f)).setTextAlignment(
                    TextAlignment.CENTER
                )
                    .add(pdfText("5.", "#E9EBFF", 12f))
                    .add(pdfText("${Utility.rounded(listMaxMeasurements[i])} L/sec ", "#000C86", 12f))
                    .add(pdfText("5.", "#E9EBFF", 12f))
                table1.addCell(
                    Cell().setTextAlignment(TextAlignment.RIGHT)
                        .setHorizontalAlignment(HorizontalAlignment.RIGHT).setPadding(4f)
                        .setBorder(Border.NO_BORDER).setBorderTopLeftRadius(BorderRadius(24f))
                        .setBorderTopRightRadius(BorderRadius(24f))
                        .setBorderBottomLeftRadius(BorderRadius(24f))
                        .setBorderBottomRightRadius(BorderRadius(24f))
                        .setBackgroundColor(WebColors.getRGBColor("#E9EBFF"))
                        .setBorder(Border.NO_BORDER).add(p)
                )

                tablemain.addCell(Cell().setBorder(Border.NO_BORDER).add(table1))

                val imgTable = Table(1)
                imgTable.setBorder(Border.NO_BORDER)
                imgTable.setWidth(540f)
                imgTable.setMarginTop(16f)

                val stream = ByteArrayOutputStream()
                val bitmap: Bitmap = listBitmaps[i]
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                val myImg = Image(ImageDataFactory.create(stream.toByteArray()))
                myImg.setWidth(468f)
                myImg.setHeight(470f)
                myImg.setHorizontalAlignment(HorizontalAlignment.LEFT)
                myImg.setTextAlignment(TextAlignment.LEFT)
                imgTable.addCell(Cell().setBorder(Border.NO_BORDER).add(myImg))
                //myImg.setHorizontalAlignment(HorizontalAlignment.CENTER)
                tablemain.addCell(Cell().setBorder(Border.NO_BORDER).add(imgTable))
                tablemain.setBorderTopLeftRadius(BorderRadius(24f))
                    .setBorderTopRightRadius(BorderRadius(24f))
                    .setBorderBottomLeftRadius(BorderRadius(24f))
                    .setBorderBottomRightRadius(BorderRadius(24f))
                document.add(tablemain)

            }

            document.close()
            loadPdf(fileShare)
        }
        catch (e:Exception){
            e.printStackTrace()
        }

    }
}




