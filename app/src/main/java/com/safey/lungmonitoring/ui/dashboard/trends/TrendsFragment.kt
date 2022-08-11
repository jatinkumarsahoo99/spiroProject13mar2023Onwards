package com.safey.lungmonitoring.ui.dashboard.trends

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.datautils.enumMeasurementsDashboard
import com.safey.lungmonitoring.data.pref.UserSession
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.ui.dashboard.home.SectionModel
import dagger.hilt.android.AndroidEntryPoint
import info.safey.graph.charts.LineChart
import info.safey.graph.components.XAxis
import info.safey.graph.data.Entry
import info.safey.graph.data.LineData
import info.safey.graph.data.LineDataSet
import info.safey.graph.formatter.ValueFormatter
import kotlinx.android.synthetic.main.fragment_trends.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@AndroidEntryPoint
class TrendsFragment : Fragment(R.layout.fragment_trends) {

    private lateinit var adapter: TrendsTestResultAdapter
     lateinit var listsectionModelall: MutableList<SectionModel>

    val trendsViewModel: TrendsViewModel by activityViewModels()

    private var arraymonth = listOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerViewBestTest.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)
        adapter = TrendsTestResultAdapter(requireContext())
        recyclerViewBestTest.adapter = adapter

        week.setOnClickListener{
            isSelected(1)
        }
        amonth.setOnClickListener{
            isSelected(2)
        }

        six_months.setOnClickListener{
            isSelected(3)
        }
        ayear.setOnClickListener{
            isSelected(4)
        }
        all.setOnClickListener{
            isSelected(5)
        }

        fvc.text = enumMeasurementsDashboard.fromInt(UserSession(requireContext()).measurementType).getFormatString()
        fvc.setOnClickListener{
            findNavController().navigate(TrendsFragmentDirections.actionTrendsFragmentToDashboardFVCBottomSheetDialog(1))
        }

        trendsViewModel.measurementType.observe(viewLifecycleOwner, {
            fvc.text = enumMeasurementsDashboard.fromInt(it).getFormatString()
        })

        listsectionModelall = ArrayList()
        for (i in 0..11){
            listsectionModelall.add(SectionModel(arraymonth[i]))
        }

        setupCharts(lineChart,"test")

    }


    private fun setupCharts(chart: LineChart, xAxisTitle: String) {
        chart.setGridBackgroundColor(Color.TRANSPARENT)
        chart.legend.isEnabled = false
        chart.legend.textColor = ContextCompat.getColor(requireContext(),R.color.wavecolor)
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setPinchZoom(false)

        val xl = chart.xAxis
        xl.setAvoidFirstLastClipping(true)
        xl.axisMinimum = 0f
        xl.axisMaximum = 12f

        xl.setPosition(XAxis.XAxisPosition.BOTTOM)
        xl.setDrawGridLines(true)
        xl.setDrawAxisLine(true)

        val xAxisFormatter: ValueFormatter = DayAxisValueFormatter(chart,listsectionModelall)
        xl.labelCount = listsectionModelall.size
        //xl.mForceLabels = true
        xl.textColor = Color.parseColor("#32000000")
        xl.isEnabled = true
        // xl.axisLineColor = ContextCompat.getColor(requireContext(), R.color.white)
        xl.setDrawLabels(true)
        xl.gridColor = Color.WHITE

        xl.valueFormatter = xAxisFormatter


        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.axisMaximum = 4f
        
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.WHITE
        leftAxis.setLabelCount(4,true)
        leftAxis.textColor = Color.parseColor("#32000000")
        leftAxis.textSize = 7f
       
        leftAxis.isEnabled = true
        leftAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.wavecolor)
        leftAxis.setDrawLabels(true)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawGridLines(true)
        leftAxis.enableGridDashedLine(5f, 5f, 2f);




        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        setData()

        chart.invalidate()
    }

    class DayAxisValueFormatter(
        private val chart: LineChart,
        var listsectionModelall: MutableList<SectionModel>
    ) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String? {
            return if (value.toInt()>=0 && value.toInt()<listsectionModelall.size)
                when(value.toInt()) {
                     value.toInt() -> listsectionModelall[value.toInt()].section
                   /* value.toInt()==0 -> if (listsectionModelall.size>0) listsectionModelall[0].section else ""
                    value.toInt() == 3 -> if (listsectionModelall.size>1) listsectionModelall[1].section else ""
                    value.toInt()==6 -> if (listsectionModelall.size>2) listsectionModelall[2].section else ""*/
                    else -> ""
                }
            else
                ""
        }
    }



    fun processTestData(testresult : List<AirTestResult>){
        var hashMap: TreeMap<String, MutableList<AirTestResult>> = TreeMap()

        val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        val testResultLocal = testresult.sortedBy { it.testtime }

        for (i in testResultLocal.indices) {

            val sortDate: String? = testResultLocal[i].testtime

            val time: Date = simpleDateFormat.parse(sortDate)

            val c = Calendar.getInstance()
            c.time = time
            val timeOfDay = c[Calendar.HOUR_OF_DAY]

            var timeKey = getString(R.string.morning)
            if (timeOfDay in 0..11) {
                // Toast.makeText(requireContext(), "Good Morning", Toast.LENGTH_SHORT).show()
                timeKey = getString(R.string.morning)
            } else if (timeOfDay in 12..17) {
                // Toast.makeText(requireContext(), "Good Evening", Toast.LENGTH_SHORT).show()
                timeKey = getString(R.string.afternoon)
            } else if (timeOfDay in 18..23) {
                //Toast.makeText(requireContext(), "Good Night", Toast.LENGTH_SHORT).show()
                timeKey = getString(R.string.evening)
            }


            if (hashMap.containsKey(timeKey)){
                hashMap[timeKey]!!.add(testResultLocal[i])
            }
            else {
                val dummyList: ArrayList<AirTestResult> = ArrayList<AirTestResult>()
                dummyList.add(testResultLocal[i])
                hashMap.put(timeKey, dummyList)
            }



            if (i == testResultLocal.size-1) {
                listsectionModelall = ArrayList()
                for (key in hashMap.keys) {
                    val sectionModel = SectionModel()
                    sectionModel.section = key
                    sectionModel.date = hashMap[key]!![0].createdDate
                    sectionModel.testResult = hashMap[key]
                    listsectionModelall.add(sectionModel)
                }


                setupCharts(lineChart,"pravin")
            }


        }
    }
    private fun setData() {
        val entries = ArrayList<Entry>()
        //val listxvalues = arrayListOf(0,3,6)
        for (i in 0 until listsectionModelall.size) {

           // val xVal = listxvalues[i]
            var total = 2.0
            var count =0
            /*for (j in 0 until listsectionModelall[i].testResult?.size!!) {
                for (k in listsectionModelall[i].testResult!![j].trialResult!!.indices) {
                    count++
                    total += listsectionModelall[i].testResult!![j].trialResult!![k].mesurementlist!!.filter { it.measurement == fvc.text }[0].measuredValue
                }
            }*/
            //listsectionModelall.add(SectionModel(arraymonth[i]))
            val yVal = (total / count)
            val yvalue = 2.0f//(Constants.decimalTimeFormat.format(yVal)).toFloat()
            Log.e( "setData: ",""+yvalue+"" )
            entries.add(Entry(i.toFloat(), yvalue))
        }


        // create a dataset and give it a type
        val set1 = LineDataSet(entries, "DataSet 1")
        set1.setDrawCircleHole(true)
        set1.setDrawCircles(true)

        set1.lineWidth = 1.5f
        set1.circleRadius = 4f
        set1.setCircleColor(ContextCompat.getColor(requireContext(),R.color.wavecolor))
        set1.valueTextColor = ContextCompat.getColor(requireContext(),R.color.wavecolor)
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER)
        set1.setColor(ContextCompat.getColor(requireContext(),R.color.wavecolor))
        // create a data object with the data sets
        val data = LineData(set1)

        data.setValueTextSize(9f)
        data.setDrawValues(true)

        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.wavecolor))
        data.notifyDataChanged()
        // set data
        lineChart.data = data
    }


    fun isSelected(btnType : Int){

        week.background = null
        amonth.background = null
        ayear.background = null
        six_months.background = null
        all.background = null

        week.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        amonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        six_months.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        ayear.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        all.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        when(btnType){
            1-> {
                week.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_blue_round)
                week.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            2-> {
                amonth.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_blue_round)
                amonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            3-> {
                six_months.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_blue_round)
                six_months.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            4-> {
                ayear.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_blue_round)
                ayear.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            5-> {
                all.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.btn_blue_round)
                all.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }

    }

}