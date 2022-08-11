package com.safey.lungmonitoring.ui.dashboard.home

import android.app.DatePickerDialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.data.datautils.enumMeasurementsDashboard
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.Symptoms
import com.safey.lungmonitoring.data.tables.patient.TrialResult
import com.safey.lungmonitoring.databinding.FragmentHomeBinding
import com.safey.lungmonitoring.interfaces.FragmentListener
import com.safey.lungmonitoring.interfaces.MeasurementSelect
import com.safey.lungmonitoring.ui.dashboard.Dashboard
import com.safey.lungmonitoring.ui.dashboard.adapter.CalenderAdapter
import com.safey.lungmonitoring.ui.dashboard.adapter.LinearLayoutPagerManager
import com.safey.lungmonitoring.ui.dashboard.home.adapter.DashboardDataAdapter
import com.safey.lungmonitoring.ui.dashboard.home.adapter.SymptonItemClickListener
import com.safey.lungmonitoring.ui.dashboard.home.adapter.TestItemClickListner
import com.safey.lungmonitoring.ui.dashboard.home.viewmodel.DashboardViewModel
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.Utility
import com.safey.lungmonitoring.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import info.safey.graph.charts.LineChart
import info.safey.graph.components.*
import info.safey.graph.data.Entry
import info.safey.graph.data.LineData
import info.safey.graph.data.LineDataSet
import kotlinx.android.synthetic.main.fragment_test_results.*
import java.text.SimpleDateFormat
import java.util.*
import info.safey.graph.formatter.ValueFormatter
import com.safey.lungmonitoring.utils.Constants.decimalTimeFormat
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.safey_toolbar.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.LinkedHashMap


@AndroidEntryPoint
class HomeFragment : Fragment() , CalenderAdapter.ItemClickListener, MeasurementSelect,
    SymptonItemClickListener, TestItemClickListner {
    private lateinit var testAdapter: DashboardDataAdapter
    private lateinit var fragListner: FragmentListener
    private lateinit var futureDate: Date
    private lateinit var previousDate: Date
      var listsectionModelall: MutableList<SectionModel> = ArrayList()
      var symptonSectionModelall: MutableList<SectionModel> = ArrayList()
     lateinit var listSubSectionModelall: MutableList<SubSectionModel>
     lateinit var listSymptonSectionModelall: MutableList<SectionModel>


    private var lastPage = 14
    private lateinit var calenderAdapter: CalenderAdapter
    private lateinit var currentDate: Date

    val dashboardViewModel :DashboardViewModel by activityViewModels()

    lateinit var selectedDate: Date
    private var sortedDate: List<Date> = ArrayList()
    private lateinit var binding: FragmentHomeBinding

    private var arraymonth = listOf(
        "January",
        "February",
        "March",
        "April",
        "May",
        "June",
        "July",
        "August",
        "September",
        "October",
        "November",
        "December"
    )
    private var requestQueue: RequestQueue? = null
    private var year: Int = 0
    private var monthOfYear: Int = 0
    private var day: Int = 0
var count:Int = 0;
    override fun onAttach(context: Context) {
        super.onAttach(context)
        fragListner = context as FragmentListener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
               requireActivity().finish()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater,container,false)
        return binding.root
    }
    public fun content(){
        count++;
        print(">>>>>>>>>>>>>>>>>>>"+count.toString());
        refresh(1000)
    }
    private fun refresh(milliseconds:Long){
        var handler:Handler = Handler()

        var runable:Runnable = Runnable (){
             kotlin.run {
                 content()
             }
        };
        handler.postDelayed(runable,milliseconds)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentDate = Utility.sdformat.parse(Utility.sdformat.format(Date()))!!
        selectedDate = currentDate

        futureDate = Utils().getFutureDate(currentDate)!!
        previousDate = Utils().getPreviousDate(currentDate)!!
        sortedDate = Utils().getListDates(previousDate, futureDate)!!

        dateListSetup()

        binding.imagePlus.setOnClickListener{
            val action = HomeFragmentDirections.actionNavHomeToTakeTestorSymptonsFragment()
            findNavController().navigate(action)
        }


        binding.recyclerViewTestData.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        dashboardViewModel.getTestData()

        dashboardViewModel.getLastSessionTestResult()
        binding.lastSession.text = "${getString(R.string.last_session)}${binding.labelFVC.text}"
        binding.todaySession.text = "${getString(R.string.today_session)}${binding.labelFVC.text}"

        (requireActivity() as Dashboard).safey_appBar.postButton.visibility = View.INVISIBLE

        if( SafeyApplication.firstTestResultHelping != null && Constants.isDelete){
            println("result may be deleted")
            dashboardViewModel.deleteTestData(SafeyApplication.firstTestResultHelping!!)
            Toast.makeText(context, "Data Saved Successfully.", Toast.LENGTH_SHORT).show()
            refreshAdapter(selectedDate)
            refreshRecycleView()
        }

        dashboardViewModel.sessionTestResultList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

                Collections.sort(
                    dashboardViewModel.sessionTestList
                ) { p0, p1 ->
                    simpleDateFormat.parse(p0!!.createdDate)
                        .compareTo(simpleDateFormat.parse(p1!!.createdDate));
                }
                val hashMap: HashMap<String, MutableList<AirTestResult>> = HashMap()





                for (i in dashboardViewModel.sessionTestList.indices) {
                    if (hashMap.containsKey(dashboardViewModel.sessionTestList[i].createdDate)) {
                        hashMap[dashboardViewModel.sessionTestList[i].createdDate]!!.add(
                            dashboardViewModel.sessionTestList[i]
                        )
                    } else {
                        val dummyList: ArrayList<AirTestResult> = ArrayList<AirTestResult>()
                        dummyList.add(dashboardViewModel.sessionTestList[i])
                        hashMap.put(dashboardViewModel.sessionTestList[i].createdDate!!, dummyList)
                    }

                    if (i == dashboardViewModel.sessionTestList.size - 1) {

                        if (hashMap.keys.size == 1) {
                            binding.todaySession.visibility = View.GONE
                            binding.txtFVCValueToday.visibility = View.GONE
                            binding.txtFVCpercentageToday.visibility = View.GONE
                            binding.txtUnitToday.visibility = View.GONE
                            binding.lastSession.text =
                                "${getString(R.string.today_session)}${binding.labelFVC.text}"
                        } else {
                            binding.todaySession.visibility = View.VISIBLE
                            binding.txtFVCValueToday.visibility = View.VISIBLE
                            binding.txtFVCpercentageToday.visibility = View.GONE
                            binding.txtUnitToday.visibility = View.VISIBLE
                            binding.lastSession.text =
                                "${getString(R.string.last_session)}${binding.labelFVC.text}"
                        }

                        var sessioncount = 0
                        for (key in hashMap.keys) {

                            val result: MutableList<AirTestResult>? = hashMap[key]
                            var count = 0
                            var total = 0.0
                            ++sessioncount
                            for (j in 0 until result?.size!!) {
                                for (k in result[j].trialResult!!.indices) {
                                    count++
                                    if (result[j].trialResult!![k].mesurementlist!!.filter { it.measurement == binding.labelFVC.text }
                                            .isNotEmpty())
                                        total += result[j].trialResult!![k].mesurementlist!!.filter { it.measurement == binding.labelFVC.text }[0].measuredValue
                                }
                            }
                            if (total > 0) {
                                val session = total / count
                                if (sessioncount == 1)
                                    if (hashMap.keys.size == 1)
                                        binding.txtFVCValue.text = String.format("%.2f", session)
                                    else
                                        binding.txtFVCValueToday.text =
                                            String.format("%.2f", session)
                                else
                                    binding.txtFVCValue.text = String.format("%.2f", session)
                            }
                        }
                    }
                }
            }
        }

        dashboardViewModel.testResultList.observe(viewLifecycleOwner) {

            if (dashboardViewModel.testList.isNotEmpty() || dashboardViewModel.symptonsList.isNotEmpty()) {
                binding.scrollView.visibility = View.VISIBLE
                binding.grpupNoData.visibility = View.GONE
                refreshAdapter(selectedDate)
            } else {
                binding.scrollView.visibility = View.GONE
                binding.grpupNoData.visibility = View.VISIBLE
            }
        }
        /*binding.scrollView.visibility = View.VISIBLE
        binding.grpupNoData.visibility = View.GONE*/



       // binding.labelFVC.text = enumMeasurementsDashboard.fromInt(UserSession(requireContext()).measurementType).getFormatString()

        /*binding.labelFVC.setOnClickListener{
            findNavController().navigate(HomeFragmentDirections.actionNavHomeToDashboardFVCBottomSheetDialog(1))
        }

        dashboardViewModel.measurementType.observe(viewLifecycleOwner, {
            binding.labelFVC.text = enumMeasurementsDashboard.fromInt(it).getFormatString()
            binding.lastSession.text = "${getString(R.string.last_session)}${binding.labelFVC.text}"
            binding.todaySession.text = "${getString(R.string.today_session)}${binding.labelFVC.text}"

            refreshAdapter(selectedDate)
        })*/

        val c = Calendar.getInstance()
        val formatter = SimpleDateFormat("MMMM''yy", Locale.ENGLISH)
        binding.labelMonth.text = (formatter.format(c.time))
        binding.labelMonth.setOnClickListener{

                val c = Calendar.getInstance()

                year = c.get(Calendar.YEAR)
                monthOfYear = c.get(Calendar.MONTH)
                day = c.get(Calendar.DAY_OF_MONTH)

                if (binding.labelMonth.text!!.isNotEmpty()) {
                    selectedDate.let {
                        c.time = selectedDate
                        year = c.get(Calendar.YEAR)
                        monthOfYear = c.get(Calendar.MONTH)
                        day = c.get(Calendar.DAY_OF_MONTH)
                    }
                }


                val dpd = DatePickerDialog(
                    requireContext(),
                    { _, year, monthOfYear, dayOfMonth ->
                        this.year = year
                        this.monthOfYear = monthOfYear
                        this.day = dayOfMonth

                        val cal = Calendar.getInstance()
                        cal.set(year,monthOfYear,dayOfMonth)

                        currentDate = Utility.sdformat.parse(Utility.sdformat.format(cal.time))!!

                        selectedDate = currentDate

                        futureDate = Utils().getFutureDate(currentDate)!!
                        previousDate = Utils().getPreviousDate(currentDate)!!
                        sortedDate = Utils().getListDates(previousDate, futureDate)!!
                        calenderAdapter = context?.let { CalenderAdapter(it, sortedDate, Utility.sdformat.parse(Utility.sdformat.format(Date()))!!,this, this) }!!
                        binding.recyclerviewDates.adapter = calenderAdapter
                        //scroll to current date
                        binding.recyclerviewDates.scrollToPosition(14)
                        refreshAdapter(selectedDate)
                        binding.labelMonth.setText( " ${arraymonth[monthOfYear]}'$year")
                    },
                    year,
                    monthOfYear,
                    day)

                val c1 = Calendar.getInstance()

                dpd.datePicker.maxDate = c1.timeInMillis
                dpd.datePicker.minDate = Utility.getDaysAgo(95).time

                //dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
                dpd.show()

                dpd.getButton(DatePickerDialog.BUTTON_POSITIVE)
                    .setTextColor(Color.parseColor("#AF4EA6"))
                dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                    .setTextColor(Color.parseColor("#AF4EA6"))

            }


    }

    private fun dateListSetup() {
        binding.recyclerviewDates.setHasFixedSize(true)
        val linearLayoutManager =
            LinearLayoutPagerManager(activity, 0, false, 7)
        binding.recyclerviewDates.layoutManager = linearLayoutManager
        val snapHelper = LinearSnapHelper() // Or PagerSnapHelper
        snapHelper.attachToRecyclerView(binding.recyclerviewDates)
        calenderAdapter = context?.let { CalenderAdapter(it, sortedDate, Utility.sdformat.parse(Utility.sdformat.format(Date()))!!,this, this) }!!
        binding.recyclerviewDates.adapter = calenderAdapter
        //scroll to current date
        binding.recyclerviewDates.scrollToPosition(14)
    }
   /* private fun postData(data:JSONObject,testResult: AirTestResult) {
        val url = "http://api-demo.ehealthsystem.com/nirmalyaRest/api/post-test-report-spiro"
        Log.wtf("",data.toString())
        val request = JsonObjectRequest(Request.Method.POST, url, data, {
                response ->try {
            print(response.getString("message"));
            Toast.makeText(context,response.getString("message"),Toast.LENGTH_SHORT).show()
            *//* dashboardViewModel.deleteTestData(testResult)
             refreshRecycleView(selectedDate)*//*
            onDeleteClick(testResult)
            *//* dashboardViewModel.deleteATestResult()*//*
            print(response.getString("message"));
        } catch (e: JSONException) {
            e.printStackTrace()
        }
        }, { error -> error.printStackTrace() })
        requestQueue?.add(request)
    }*/

    fun selectedDate(): Date {
//        Log.e("selectedDateSwipe", selectedDate.toString() + "")
        return selectedDate
    }

    override fun onClick(view: View?, adapterPosition: Int, b: Date?) {

        if (b != null) {
            refreshAdapter(b)
        }
    }

    private fun refreshAdapter(b: Date) {

        selectedDate = b
        calenderAdapter.notifyDataSetChanged()

        val position = sortedDate.indexOf(selectedDate)
        if (lastPage > position) { //User Move to left}
            lastPage = position
            binding.recyclerviewDates.scrollToPosition(position - 1)
        } else if (lastPage < position) { //User Move to right}
            lastPage = position
            binding.recyclerviewDates.scrollToPosition(position)
        }


           val listSectionModel = dashboardViewModel.testList
            val listSymptonModel = dashboardViewModel.symptonsList



            if(listSectionModel.isEmpty() && listSymptonModel.isEmpty()) {
                binding.grpupNoData.visibility = View.VISIBLE
                binding.scrollView.visibility = View.GONE
            }
            else
            {
               // if (listSectionModel.any { it.testtype == 1 }) {
                    processTestData(listSectionModel,listSymptonModel)
                    binding.grpupNoData.visibility = View.GONE
                    binding.scrollView.visibility = View.VISIBLE
//                }
//                else {
//
//                    binding.grpupNoData.visibility = View.VISIBLE
//                    binding.scrollView.visibility = View.GONE
//                }
            }

       /*    if (listSectionModel.size >= 2) {
                 binding.imagePlus.visibility = View.GONE
             } else if (listSectionModel.size < 2) {
                 binding.imagePlus.visibility = View.VISIBLE
             } else {
                 binding.imagePlus.visibility = View.GONE
             }*/

    }
    fun observeTestData(){
        dashboardViewModel.sessionTestResultList.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                Collections.sort(
                    dashboardViewModel.sessionTestList
                ) { p0, p1 ->
                    simpleDateFormat.parse(p0!!.createdDate)
                        .compareTo(simpleDateFormat.parse(p1!!.createdDate));
                }
                val hashMap: java.util.HashMap<String, MutableList<AirTestResult>> =
                    java.util.HashMap()
                for (i in dashboardViewModel.sessionTestList.indices) {
                    if (hashMap.containsKey(dashboardViewModel.sessionTestList[i].createdDate)) {
                        hashMap[dashboardViewModel.sessionTestList[i].createdDate]!!.add(
                            dashboardViewModel.sessionTestList[i]
                        )
                    } else {
                        val dummyList: java.util.ArrayList<AirTestResult> =
                            java.util.ArrayList<AirTestResult>()
                        dummyList.add(dashboardViewModel.sessionTestList[i])
                        hashMap.put(dashboardViewModel.sessionTestList[i].createdDate!!, dummyList)
                    }

                    if (i == dashboardViewModel.sessionTestList.size - 1) {

                        if (hashMap.keys.size == 1) {
                            binding.todaySession.visibility = View.GONE
                            binding.txtFVCValueToday.visibility = View.GONE
                            binding.txtFVCpercentageToday.visibility = View.GONE
                            binding.txtUnitToday.visibility = View.GONE
                            binding.lastSession.text =
                                "${getString(R.string.today_session)}${binding.labelFVC.text}"
                        } else {
                            binding.todaySession.visibility = View.VISIBLE
                            binding.txtFVCValueToday.visibility = View.VISIBLE
                            binding.txtFVCpercentageToday.visibility = View.GONE
                            binding.txtUnitToday.visibility = View.VISIBLE
                            binding.lastSession.text =
                                "${getString(R.string.last_session)}${binding.labelFVC.text}"
                        }

                        var sessioncount = 0
                        for (key in hashMap.keys) {
                            val result: MutableList<AirTestResult>? = hashMap[key]
                            var count = 0
                            var total = 0.0
                            ++sessioncount
                            for (j in 0 until result?.size!!) {
                                for (k in result[j].trialResult!!.indices) {
                                    count++
                                    if (result[j].trialResult!![k].mesurementlist!!.filter { it.measurement == binding.labelFVC.text }
                                            .isNotEmpty())
                                        total += result[j].trialResult!![k].mesurementlist!!.filter { it.measurement == binding.labelFVC.text }[0].measuredValue
                                }
                            }
                            if (total > 0) {
                                val session = total / count
                                if (sessioncount == 1)
                                    if (hashMap.keys.size == 1)
                                        binding.txtFVCValue.text = String.format("%.2f", session)
                                    else
                                        binding.txtFVCValueToday.text =
                                            String.format("%.2f", session)
                                else
                                    binding.txtFVCValue.text = String.format("%.2f", session)
                            }
                        }
                    }
                }
            }
        }
    }
    private fun refreshRecycleView() {



        dashboardViewModel.getTestData()
        observeTestData()

        System.out.println(dashboardViewModel.testList)
        val listSectionModel = dashboardViewModel.testList
        val listSymptonModel = dashboardViewModel.symptonsList
        System.out.println(listSectionModel)

        if (listSectionModel.isEmpty() && listSymptonModel.isEmpty()) {
            binding.grpupNoData.visibility = View.VISIBLE
            binding.scrollView.visibility = View.GONE
        } else {
            processTestData(listSectionModel, listSymptonModel)
            binding.grpupNoData.visibility = View.GONE
            binding.scrollView.visibility = View.VISIBLE
            testAdapter.notifyDataSetChanged()
        }

            /*if (listSectionModel.size >= 2) {
                binding.imagePlus.visibility = View.GONE
            } else if (listSectionModel.size < 2) {
                binding.imagePlus.visibility = View.VISIBLE
            } else {
                binding.imagePlus.visibility = View.GONE
            }*/
    }

    fun processTestData(testresult: List<AirTestResult>, listSymptonModel: List<Symptoms>){
        val hashMap: LinkedHashMap<String, MutableList<AirTestResult>> = LinkedHashMap()
        val hashMapSymptons: LinkedHashMap<String, MutableList<Symptoms>> = LinkedHashMap()


listsectionModelall = ArrayList()

        val simpleDateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

        Collections.sort(testresult
        ) { p0, p1 -> simpleDateFormat.parse(p0!!.testtime).compareTo(simpleDateFormat.parse(p1!!.testtime)); }

        Collections.sort(listSymptonModel
        ) { p0, p1 -> simpleDateFormat.parse(p0!!.symptomtime).compareTo(simpleDateFormat.parse(p1!!.symptomtime)); }
      // val testResultLocal = testresult.sortedBy { it.testtime }

      // val testResultFEVC= testresult.filter { it.testtype == 1 }
        for (i in testresult.indices) {

            val sortDate: String? = testresult[i].testtime

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
                hashMap[timeKey]!!.add(testresult[i])
            }
            else {
                val dummyList: ArrayList<AirTestResult> = ArrayList<AirTestResult>()
                dummyList.add(testresult[i])
                hashMap.put(timeKey, dummyList)
            }


            if (i == testresult.size-1) {
                listsectionModelall = ArrayList()
                for (key in hashMap.keys) {
                    val sectionModel = SectionModel()
                    sectionModel.section = key
                    sectionModel.date = hashMap[key]!![0].createdDate
                    sectionModel.testResult = hashMap[key]
//                    var innerhashMap: TreeMap<String, MutableList<AirTestResult>> = TreeMap()
//                    for (j in 0 until sectionModel.testResult?.size!!){
//
//                        val time: String = sectionModel.testResult!![j].testtime!!
//                        if (innerhashMap.containsKey(time)){
//                            innerhashMap[time]!!.add(sectionModel.testResult!![j])
//                        }
//                        else {
//                            val dummyList: ArrayList<AirTestResult> = ArrayList<AirTestResult>()
//                            dummyList.add(sectionModel.testResult!![j])
//                            innerhashMap.put(time, dummyList)
//                        }
//                        if (j == sectionModel.testResult!!.size-1) {
//                           listSubSectionModelall = ArrayList()
//                            for (innerkey in innerhashMap.keys) {
//                                val subSectionModel = SubSectionModel()
//                                subSectionModel.section = innerkey
//                                subSectionModel.testResult = innerhashMap[innerkey]
//                                listSubSectionModelall.add(subSectionModel)
//                            }
//
//                        }
//                    }

                    //sectionModel.subSectionModel = listSubSectionModelall

                    listsectionModelall.add(sectionModel)
                }



//                for (i in listsectionModelall){
//                    for (j in symptonSectionModelall){
//                        if (i.section == j.section) {
//                            i.symptonList = j.symptonList
//                            break
//                        }
//                    }
//                }

            }


        }

        for (i in listSymptonModel.indices) {

            val sortDate: String? = listSymptonModel[i].symptomtime

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


            if (hashMapSymptons.containsKey(timeKey)) {
                hashMapSymptons[timeKey]!!.add(listSymptonModel[i])
            } else {
                val dummyList: ArrayList<Symptoms> = ArrayList<Symptoms>()
                dummyList.add(listSymptonModel[i])
                hashMapSymptons.put(timeKey, dummyList)
            }



            if (i == listSymptonModel.size - 1) {
                symptonSectionModelall = ArrayList()
                for (key in hashMapSymptons.keys) {
                    val sectionModel = SectionModel()
                    sectionModel.section = key
                    sectionModel.date = hashMapSymptons[key]!![0].createdDate
                    sectionModel.symptonList = hashMapSymptons[key]

                    //symptonSectionModelall.add(sectionModel)

                    if(listsectionModelall.any { it.date == sectionModel.date }) {
                        if (listsectionModelall.any { it.section == key })
                            listsectionModelall.filter { it.section == key }[0].symptonList =
                                hashMapSymptons[key]
                        else {
                            if (sectionModel.section == getString(R.string.morning)) {
                                listsectionModelall.add(0, sectionModel)
                            } else if (sectionModel.section == getString(R.string.afternoon)) {
                                if (listsectionModelall.any { it.section != getString(R.string.morning) })
                                    listsectionModelall.add(0, sectionModel)
                                else
                                    if (listsectionModelall.size == 0)
                                        listsectionModelall.add(0, sectionModel)
                                    else
                                        listsectionModelall.add(1, sectionModel)
                            } else {
                                if (listsectionModelall.size > 0)
                                    listsectionModelall.add(listsectionModelall.size, sectionModel)
                                else
                                    listsectionModelall.add(0, sectionModel)
                            }


                        }
                    }
                    else
                    {
                        listsectionModelall.add(sectionModel)
                    }
//                       }     var innerhashMap: TreeMap<String, MutableList<Symptons>> =
//                                TreeMap()
//                            for (j in 0 until sectionModel.symptonList?.size!!) {
//
//                                val time: String = sectionModel.symptonList!![j].symptontime!!
//                                if (innerhashMap.containsKey(time)) {
//                                    innerhashMap[time]!!.add(sectionModel.symptonList!![j])
//                                } else {
//                                    val dummyList: ArrayList<Symptons> =
//                                        ArrayList<Symptons>()
//                                    dummyList.add(sectionModel.symptonList!![j])
//                                    innerhashMap.put(time, dummyList)
//                                }
//                                if (j == sectionModel.symptonList!!.size - 1) {
//                                    listSymptonSectionModelall = ArrayList()
//                                    for (innerkey in innerhashMap.keys) {
//                                        val symptonSectionModel = SymptonSectionModel()
//                                        symptonSectionModel.section = innerkey
//                                        symptonSectionModel.listSymptons = innerhashMap[innerkey]
//                                        listSymptonSectionModelall.add(symptonSectionModel)
//                                    }
//
//                                }
//                            }
//

                    // sectionModel.subSymptonsSectionModel = listSymptonSectionModelall

                    // symptonSectionModelall.add(sectionModel)
                }

            }
        }
//        var displayMetrics = DisplayMetrics()
//
//        val screen = requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)
//        var screenWidth = displayMetrics.widthPixels
        testAdapter = DashboardDataAdapter(requireContext(), listsectionModelall,this,this)
        binding.recyclerViewTestData.adapter = testAdapter

        //setupCharts(binding.chart,"pravin")
    }


    private fun setupCharts(chart: LineChart, xAxisTitle: String) {
        chart.setGridBackgroundColor(Color.TRANSPARENT)
        chart.legend.isEnabled = false
        chart.legend.textColor = Color.WHITE
        chart.description.isEnabled = false
        chart.setTouchEnabled(false)
        chart.isDragEnabled = false
        chart.setPinchZoom(false)

        val xl = chart.xAxis
        xl.setAvoidFirstLastClipping(true)
        xl.axisMinimum = 0f
        xl.axisMaximum = 7f

        xl.setPosition(XAxis.XAxisPosition.BOTTOM)
        xl.setDrawGridLines(true)
        xl.setDrawAxisLine(true)

        val xAxisFormatter: ValueFormatter = DayAxisValueFormatter(chart,listsectionModelall)
        xl.labelCount = 7
        //xl.mForceLabels = true
        xl.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        xl.isEnabled = true
       // xl.axisLineColor = ContextCompat.getColor(requireContext(), R.color.white)
        xl.setDrawLabels(true)
        xl.gridColor = Color.parseColor("#465EC1")

        xl.valueFormatter = xAxisFormatter


        val leftAxis = chart.axisLeft
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)
        leftAxis.setDrawGridLines(true)
        leftAxis.gridColor = Color.parseColor("#465EC1")
        //leftAxis.setLabelCount(10,true)
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        leftAxis.isEnabled = true
        leftAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.white)
        leftAxis.setDrawLabels(false)
        leftAxis.setDrawAxisLine(false)
        leftAxis.setDrawGridLines(true)
        leftAxis.enableGridDashedLine(5f, 5f, 2f);
        leftAxis.valueFormatter = MyDecimalValueFormatter()



        val rightAxis = chart.axisRight
        rightAxis.isEnabled = false

        setData()

        chart.invalidate()
    }
    class MyDecimalValueFormatter : ValueFormatter() {
        override fun getFormattedValue(value: Float): String {
            return String.format("%.2f",value)
        }

    }
    class DayAxisValueFormatter(
        private val chart: LineChart,
        var listsectionModelall: MutableList<SectionModel>
    ) : ValueFormatter() {

        override fun getFormattedValue(value: Float): String? {
            return if (listsectionModelall.size>0)
                when {
                    value.toInt()==0 -> if (listsectionModelall.size>0) listsectionModelall[0].section else ""
                    value.toInt() == 3 -> if (listsectionModelall.size>1) listsectionModelall[1].section else ""
                    value.toInt()==6 -> if (listsectionModelall.size>2) listsectionModelall[2].section else ""
                    else -> ""
                }
            else
                ""
        }
    }

    private fun setData() {
        val entries = ArrayList<Entry>()
        val listxvalues = arrayListOf(0,3,6)
        for (i in 0 until listsectionModelall.size) {
            val xVal = listxvalues[i]
            var total = 0.0
            var count =0
            for (j in 0 until listsectionModelall[i].testResult?.size!!) {
                for (k in listsectionModelall[i].testResult!![j].trialResult!!.indices) {
                    count++
                    if(listsectionModelall[i].testResult!![j].trialResult!![k].mesurementlist!!.filter { it.measurement == binding.labelFVC.text }.isNotEmpty())
                        total += listsectionModelall[i].testResult!![j].trialResult!![k].mesurementlist!!.filter { it.measurement == binding.labelFVC.text }[0].measuredValue
                }
            }

            val yVal = (total / count)
            val yvalue = (decimalTimeFormat.format(yVal)).toFloat()
            Log.e( "setData: ",""+yvalue+"" )
            entries.add(Entry(xVal.toFloat(), yvalue))
        }


        // create a dataset and give it a type
        val set1 = LineDataSet(entries, "DataSet 1")
        set1.setDrawCircleHole(true)
        set1.setDrawCircles(true)

        set1.lineWidth = 1.5f
        set1.circleRadius = 4f
        set1.setCircleColor(Color.WHITE)
        set1.valueTextColor = Color.WHITE
        set1.setMode(LineDataSet.Mode.CUBIC_BEZIER)
        set1.setColor(Color.WHITE)
        // create a data object with the data sets
        val data = LineData(set1)

        data.setValueTextSize(9f)
        data.setDrawValues(true)

        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        data.notifyDataChanged()
        // set data
        chart.data = data
    }


    override fun onCLick(measurement: Int) {

        binding.labelFVC.text = enumMeasurementsDashboard.fromInt(measurement).getFormatString()
    }

    override fun onClick(item: Symptoms) {
        val action = HomeFragmentDirections.actionNavHomeToAddSymptonsFragment(item.guid)
        findNavController().navigate(action)
    }

    override fun onClick(testResult: AirTestResult) {
        SafeyApplication.firstTestResult = testResult
        SafeyApplication.firstTestResultHelping = testResult
        Constants.isDelete = false
        SafeyApplication.status = true
        (requireActivity() as Dashboard).safey_appBar.postButton.visibility = View.VISIBLE

        if (testResult.trialResult?.any { it.isPost } == true) {
            SafeyApplication.postTestResult = AirTestResult()
            SafeyApplication.postTestResult!!.trialResult = testResult.trialResult?.filter { it.isPost }
            var ispost= 0

           var airTestResult = AirTestResult()
            val trialResults : MutableList<TrialResult> = ArrayList()
            airTestResult = SafeyApplication.firstTestResult!!
            for (i in 0 until SafeyApplication.firstTestResult!!.trialResult?.size!!)
            {
                if (SafeyApplication.firstTestResult!!.trialResult?.get(i)?.isPost == false){
                    SafeyApplication.firstTestResult!!.trialResult?.get(i)
                        ?.let { trialResults.add(it) }
                }
            }
            airTestResult.trialResult = trialResults

             SafeyApplication.firstTestResult= airTestResult
        }
        else
            SafeyApplication.postTestResult = null

        val action = HomeFragmentDirections.actionNavHomeToTestResultsFragment(0)
        findNavController().currentDestination?.getAction(R.id.action_nav_home_to_testResultsFragment)?.let {
            findNavController().navigate(action)
        }
    }

    override fun onDeleteClick(testResult: AirTestResult) {
        dashboardViewModel.deleteTestData(testResult)
        refreshAdapter(selectedDate)
        refreshRecycleView()
    }

    override fun onUpdate(testResult: AirTestResult) {
        TODO("Not yet implemented")
    }

/*    override fun onPost(testResult: AirTestResult) {
        dashboardViewModel.postTestData(testResult)
        try {
            var mainJson: JSONObject = JSONObject();
//                var json1: JSONObject = JSONObject();
            var jsonArr: JSONArray = JSONArray();

//                json.put("trailResult",dashboardViewModel.testList[0].toString())
            dashboardViewModel?.postList.forEach {
                var json1: JSONObject = JSONObject();
                var trialResult: JSONArray = JSONArray()
                var variance: JSONArray = JSONArray();
                json1.put("type", it.type)
                json1.put("testType", it.testtype)
                json1.put("createdAt", it.createdAt)
                json1.put("createdDt", it.createdDate)
                json1.put("testTime", it.testtime)
                json1.put("active", it.active)
                mainJson.put("userId", it.userId)
                mainJson.put("fname",it.fname)
                mainJson.put("gender", "Male")
                mainJson.put("height", it.height)
                it.trialResult?.forEach {
                    var json2: JSONObject = JSONObject()
                    var measurement: JSONArray = JSONArray()
                    var graphDataList: JSONArray = JSONArray();
                    json2.put("isBest", it.isBest)
                    json2.put("isPost", it.isPost)
                    it.graphDataList?.forEach {
                        var graph: JSONObject = JSONObject()
                        graph.put("flow", if(it.flow.toString().trim() == "-") JSONObject.NULL else it.flow)
                        graph.put("volume",if(it.volume.toString().trim() == "-") JSONObject.NULL else it.volume )
                        graph.put("second", if (it.second.toString().trim() == "-") JSONObject.NULL else it.second)
                        graph.put("direction", if( it.direction.toString().trim() =="-") JSONObject.NULL else it.direction)
                        graphDataList.put(graph)
                    }
                    it.mesurementlist?.forEach {
                        var graph: JSONObject = JSONObject()
                        graph.put("measurement", it.measurement)
                        graph.put("measuredValue", it.measuredValue)
                        graph.put("predictedValue",if(it.predictedValue?.trim() == "-") JSONObject.NULL else it.predictedValue?.toDouble() )
                        graph.put("unit", it.unit)
                        graph.put("lln", if (it.lln?.trim() =="-")  JSONObject.NULL else it.lln?.trim())
                        graph.put("uln", if (it.uln?.trim() =="-") JSONObject.NULL else it.uln?.trim() )
                        graph.put("zScore", if (it.zScore?.trim() == "-") JSONObject.NULL else it.zScore?.trim())
                        graph.put("predictedPer", if(it.predictedPer.toString().trim() == "-") JSONObject.NULL else it.predictedPer)
                        measurement.put(graph)
                    }
                    json2.put("graphDataList", graphDataList)
                    json2.put("measurement", measurement)
                    trialResult.put(json2)
                }
                it.variance?.forEach {
                    var varian: JSONObject = JSONObject()
                    varian.put("measurement", it.measurement)
                    varian.put("measurementValue", it.measurementValue)
                    varian.put("percentage", it.percentage)
                    variance.put(varian)
                }
                json1.put("trialResult", trialResult)
                json1.put("variance", variance)
                jsonArr.put(json1)
            }
            mainJson.put("spiroData", jsonArr)
            Log.wtf("data",mainJson.toString())
            postData(mainJson,testResult)

            *//*    dashboardViewModel.deleteTestData(testResult)
                refreshRecycleView(selectedDate)*//*
        } catch (e: Error) {
        }
    }*/
}