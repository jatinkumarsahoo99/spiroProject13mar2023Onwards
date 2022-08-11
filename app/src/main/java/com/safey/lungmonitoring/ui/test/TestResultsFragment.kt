package com.safey.lungmonitoring.ui.test

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.barteksc.pdfviewer.scroll.DefaultScrollHandle
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.extensions.jsonBody
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
import com.safey.lungmonitoring.MobileNavigationDirections
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.custombinings.longDate
import com.safey.lungmonitoring.custombinings.posToGender
import com.safey.lungmonitoring.custombinings.positionToEthnicity
import com.safey.lungmonitoring.data.datautils.enumHeightUnit
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.TestMeasurements
import com.safey.lungmonitoring.data.tables.patient.TrialResult
import com.safey.lungmonitoring.data.tables.patient.Variance
import com.safey.lungmonitoring.interfaces.DialogStyle1Click
import com.safey.lungmonitoring.model.CustomDialogStyle1DataModel
import com.safey.lungmonitoring.model.Report_Measurements
import com.safey.lungmonitoring.ui.dashboard.Dashboard
import com.safey.lungmonitoring.ui.dashboard.home.HomeFragmentDirections
import com.safey.lungmonitoring.ui.test.adapter.TestResultAdapter
import com.safey.lungmonitoring.ui.test.adapter.TestResultCompareAdapter
import com.safey.lungmonitoring.ui.test.adapter.VarianceAdapter
import com.safey.lungmonitoring.ui.test.viewmodel.TestResultViewModel
import com.safey.lungmonitoring.utils.*
import dagger.hilt.android.AndroidEntryPoint
import info.safey.graph.charts.LineChart
import info.safey.graph.components.Legend
import info.safey.graph.components.LegendEntry
import info.safey.graph.components.XAxis
import info.safey.graph.components.YAxis
import info.safey.graph.data.Entry
import info.safey.graph.data.LineData
import info.safey.graph.data.LineDataSet
import info.safey.safey_sdk.IConnectionCallback
import info.safey.safey_sdk.SafeyDeviceKit
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.fragment_test_results.*
import kotlinx.android.synthetic.main.layout_report_testdetail.*
import kotlinx.android.synthetic.main.safey_toolbar.view.*
import kotlinx.android.synthetic.main.session_quality.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

@AndroidEntryPoint
class TestResultsFragment : Fragment(),IConnectionCallback, DialogStyle1Click {

    companion object {
         var safeyDeviceKit: SafeyDeviceKit?= null
    }
    private lateinit var navController: NavController
    private var dialog: Dialog? = null
    private lateinit var printModel: PrintModel
    private lateinit var fileShare: File
    private lateinit var testResultCompareAdapter: TestResultCompareAdapter
    private lateinit var testResultAdapter: TestResultAdapter
    private var testNo: Int = 0
    private var bestTestResult: Int=0
    private var trialCount: Int = 0
    private lateinit var binding: com.safey.lungmonitoring.databinding.FragmentTestResultsBinding
    val viewModel: TestResultViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
                if (trialCount > 0) {
                    AlertDialog.Builder(requireContext())
                        .setCancelable(false)
                        .setTitle("Alert")
                        .setMessage(getString(R.string.you_have_not_saved_your_result))
                        .setPositiveButton(getString(R.string.quit)) { p0, _ ->
                            safeyDeviceKit?.disconnect()
                            findNavController().popBackStack(R.id.nav_home,false)
                            p0!!.dismiss()
                        }.setNegativeButton(getString(R.string.cancel)) { p0, _ -> p0!!.dismiss() }.show()

                } else
                    findNavController().popBackStack(R.id.nav_home,false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)


    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_test_results, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        trialCount = arguments?.getInt("trial")!!
        binding.txtAddTrial.setOnClickListener{
            Constants.isPost = false
            findNavController().popBackStack()
        }

        val linearLayoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.layoutManager = linearLayoutManager

        val linearLayoutManagerCompare =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewCompare.layoutManager = linearLayoutManagerCompare

        val linearLayoutManagerVariance =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.layoutSession.sessionRecyclerView.layoutManager = linearLayoutManagerVariance





        when (trialCount) {
            3 -> {
                txtAddTrial.visibility = View.GONE
            }
        }

        if (trialCount>1) {
            binding.textAll.visibility = View.VISIBLE
            text_all.isEnabled = true
        }





        binding.safeyFinishAddpostButton.buttonFinishTest.setOnClickListener {
//            safeyDeviceKit?.finishTest()
        /*  viewModel.completeTest()*/
             binding.imgShare.visibility = View.VISIBLE
            CustomDialogs.dialogStyleShare(CustomDialogStyle1DataModel(requireActivity(),"","","","",this))
            (requireActivity() as Dashboard).safey_appBar.postButton.visibility = View.VISIBLE

        }

        (requireActivity() as Dashboard).safey_appBar.postButton.setOnClickListener{

            createPDF()

        }

        viewModel.inserted.observe(viewLifecycleOwner, {
            findNavController().popBackStack(R.id.nav_home,false)

        })

        binding.safeyFinishAddpostButton.imageDelete.setOnClickListener{
            parentFragmentManager.let { it1 -> DeleteBottomSheetDialog().show(it1,"deleteDialog") }
        }

        binding.safeyFinishAddpostButton.buttonAddPost.setOnClickListener{
            parentFragmentManager.let { it1 -> AddPostSheetDialog().show(it1,"addPostDialog") }

        }


        val airtestResult: AirTestResult = SafeyApplication.firstTestResult!!
        if (airtestResult.testtype == 1) {
            (requireActivity() as Dashboard).safey_appBar.txtHeader.text =
                getString(R.string.fevc_testresults)
        }
        else
            (requireActivity() as Dashboard).safey_appBar.txtHeader.text = getString(R.string.fivc_testresults)


        if (Constants.isPost) {
            binding.textPost.visibility = View.VISIBLE
            text_post.isEnabled = true

            showTrialResult(SafeyApplication.postTestResult!!)
            testResultAdapter = TestResultAdapter(
                requireContext(),
                airTestValues = SafeyApplication.postTestResult?.trialResult!![0].mesurementlist as MutableList<TestMeasurements>?
            )
            recyclerView.adapter = testResultAdapter

            for (i  in 1..airtestResult.trialResult!!.size){
                when (i) {
                    1 -> text_1st.isEnabled = true
                    2 -> text_2nd.isEnabled = true
                    3 -> text_3rd.isEnabled = true
                    4 -> text_4th.isEnabled = true

                }
            }

            isSelected(airtestResult.trialResult!!.size,true,false)
        }else
        {
            showTrialResult(airtestResult.trialResult!![airtestResult.trialResult!!.size - 1],airtestResult.trialResult!!.size)

            testResultAdapter = if (trialCount == 0)
                TestResultAdapter(
                    context = requireContext(),
                    airTestValues = airtestResult.trialResult!![0].mesurementlist as MutableList<TestMeasurements>?
                )
            else
                TestResultAdapter(
                    context = requireContext(),
                    airTestValues = airtestResult.trialResult!![airtestResult.trialResult!!.size - 1].mesurementlist as MutableList<TestMeasurements>?
                )

            recyclerView.adapter = testResultAdapter
        }

        //Setup charts
        setupCharts(chart, "Flow")
        setupCharts(volumechart, "Volume")

        setupCharts(chartReport, "Flow",1)
        setupCharts(chartReportvolume, "Volume",1)



        if (trialCount == 0) {
            binding.txtAddTrial.visibility = View.GONE
            if(SafeyApplication.status == true){
                 binding.imgShare.visibility = View.VISIBLE
            }

            binding.safeyFinishAddpostButton.root.visibility = View.GONE
          /*  (requireActivity() as Dashboard).safey_appBar.iconRight.visibility = View.VISIBLE*/
        }
        else {
            getSafeyDeviceKit()
          /*  (requireActivity() as Dashboard).safey_appBar.iconRight.visibility = View.GONE*/
        }


        binding.imgShare.setOnClickListener{

            createPDF1()
        }

        //Trial Button handling
        text_1st.setOnClickListener {
            testNo = 1
            isSelected(testNo)


            testResultAdapter = TestResultAdapter(
                requireContext(),
                airtestResult.trialResult!![testNo - 1].mesurementlist as MutableList<TestMeasurements>?
            )
            recyclerView.adapter = testResultAdapter
            showTrialResult(airtestResult.trialResult!![testNo - 1],testNo)

        }

        text_2nd.setOnClickListener {
            testNo = 2
            isSelected(testNo)

            testResultAdapter = TestResultAdapter(
                requireContext(),
                airtestResult.trialResult!![testNo - 1].mesurementlist as MutableList<TestMeasurements>?
            )
            recyclerView.adapter = testResultAdapter
            showTrialResult(airtestResult.trialResult!![testNo - 1],testNo)
        }

        text_3rd.setOnClickListener {
            testNo = 3
            isSelected(testNo)

            testResultAdapter = TestResultAdapter(
                requireContext(),
                airtestResult.trialResult!![testNo - 1].mesurementlist as MutableList<TestMeasurements>?
            )
            recyclerView.adapter = testResultAdapter
            showTrialResult(airtestResult.trialResult!![testNo - 1],testNo)

        }

        text_4th.setOnClickListener {
            testNo = 4
            isSelected(testNo)

            testResultAdapter = TestResultAdapter(
                requireContext(),
                airTestValues = airtestResult.trialResult!![testNo - 1].mesurementlist as MutableList<TestMeasurements>?
            )
            recyclerView.adapter = testResultAdapter
            showTrialResult(airtestResult.trialResult!![testNo - 1],testNo)
        }


        binding.compare.setOnClickListener {
            if ( binding.imageCompareCross.visibility == View.GONE){
                binding.imageCompareCross.visibility = View.VISIBLE
                binding.recyclerViewCompare.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            }

        }
        binding.imageCompareCross.setOnClickListener{
            binding.imageCompareCross.visibility = View.GONE
            binding.recyclerViewCompare.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
        }
        text_all.setOnClickListener{
            text_all.isEnabled = true

            showTrialResult(airtestResult)
            if (airtestResult.trialResult!!.any { it.isBest })
            testResultAdapter = TestResultAdapter(
                requireContext(),
                airTestValues = airtestResult.trialResult!!.filter { it.isBest }[0].mesurementlist as MutableList<TestMeasurements>?
            )
            else
                testResultAdapter = TestResultAdapter(
                    requireContext(),
                    airTestValues = SafeyApplication.postTestResult?.trialResult!![0].mesurementlist as MutableList<TestMeasurements>?
                )
            recyclerView.adapter = testResultAdapter
            isSelected(testNo,false,true)
        }

        text_post.setOnClickListener{

            showTrialResult(SafeyApplication.postTestResult!!)
            testResultAdapter = TestResultAdapter(
                requireContext(),
                airTestValues = SafeyApplication.postTestResult?.trialResult!![0].mesurementlist as MutableList<TestMeasurements>?
            )
            recyclerView.adapter = testResultAdapter


            isSelected(testNo,true,false)
        }




        binding.flowVolume.setOnClickListener{
                volume_sec.alpha = 0.65f
                volume_sec_line.alpha = 0.65f
                flow_volume.alpha = 0.95f
                flow_volume_line.visibility = View.INVISIBLE
                volume_sec_line.visibility = View.VISIBLE
            binding.chart.visibility = View.VISIBLE
            binding.volumechart.visibility = View.INVISIBLE
            binding.txtVolume.text = getString(R.string.volume_l)
            binding.flowLS.text = getString(R.string.flow_l_s_)
        }

        binding.volumeSec.setOnClickListener{
            flow_volume.alpha = 0.65f
            flow_volume_line.alpha = 0.65f

            volume_sec.alpha = 0.95f
            volume_sec_line.visibility = View.INVISIBLE
            flow_volume_line.visibility = View.VISIBLE
            binding.volumechart.visibility = View.VISIBLE
            binding.chart.visibility = View.INVISIBLE

            binding.txtVolume.text = getString(R.string.time_s)
            binding.flowLS.text = getString(R.string.volume_l)
        }


        for (i in airtestResult.trialResult!!.indices) {
            if (airtestResult.trialResult!![i].isBest) {
                bestTestResult = i
                bestTest(bestTestResult)
               break
            }
        }

        if (SafeyApplication.postTestResult!=null) {
            binding.safeyFinishAddpostButton.buttonAddPost.isEnabled = false
            binding.safeyFinishAddpostButton.buttonAddPost.alpha = 0.4f
            binding.compare.visibility = View.INVISIBLE
            binding.viewCompare.visibility = View.INVISIBLE
            binding.imageCompareCross.visibility = View.GONE
            val bestAirTestValues = airtestResult.trialResult!![bestTestResult].mesurementlist as MutableList<TestMeasurements>?
            val postAirTestValues = SafeyApplication.postTestResult?.trialResult!![0].mesurementlist as MutableList<TestMeasurements>?
            testResultCompareAdapter = TestResultCompareAdapter(
                requireContext(),
                airTestValues = bestAirTestValues,postAirTestValues,bestTestResult+1
            )
            binding.recyclerViewCompare.adapter = testResultCompareAdapter
        }

        binding.layoutSession.labelMore.setOnClickListener{
            if (grpsession.visibility == View.GONE) {
                grpsession.visibility = View.VISIBLE
                labelMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_up_arrow, 0)

            }
            else{
                grpsession.visibility= View.GONE
                labelMore.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_down_arrow, 0)
            }
        }

        if (trialCount == 0) {
            testNo=1
            isSelected(testNo)
        }


        var varianceAdapter = VarianceAdapter(
            requireContext(),
            airtestResult.variance as MutableList<Variance>?
        )
        binding.layoutSession.sessionRecyclerView.adapter = varianceAdapter

        binding.layoutSession.lblInterpretation.visibility = View.GONE
        binding.layoutSession.lblTestPerformed.text = "Test performed: "+SimpleDateFormat("dd MMM yyyy hh:mm a").format(airtestResult.createdAt)
        binding.layoutSession.txtSessionScore.text = ""+airtestResult.sessionScore
        if (airtestResult.trialResult?.size!! > 1)
            binding.layoutSession.root.visibility = View.VISIBLE
        else
            binding.layoutSession.root.visibility = View.GONE



        //Report PDF

        PrintUtility.copyLogo(requireContext())
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

        viewModel.getPatient()

        showReportResult(SafeyApplication.firstTestResult!!)


    }
    private fun showTrialResult(trialResult: TrialResult,testno: Int) {
        try {

            binding.chart.clear()
            binding.volumechart.clear()

            val legends = ArrayList<LegendEntry>()

            //for (i in testResult.trialResult!!.indices) {
                val entries = ArrayList<Entry>()
                val entriesVS = ArrayList<Entry>()
                val legendEntry = LegendEntry()
                for (graphData in trialResult.graphDataList!!) {
                    entries.add(Entry(graphData.volume.toFloat(), graphData.flow.toFloat()))
                    entriesVS.add(Entry(graphData.second.toFloat(), graphData.volume.toFloat()))
                }
               // if (testResult.trialResult!![i].isBest) {
                   // bestTestResult = i

                    legendEntry.formColor = ContextCompat.getColor(requireContext(), R.color.graph_line_color)
               // } else {
                 //   legendEntry.formColor = ContextCompat.getColor(requireContext(), R.color.white)
               // }

                //legendEntry.label = "Trial $tCount"


                legends.add(legendEntry)

                setDatalineChart(entries, true, legends, chart)
                setDatalineChart(entriesVS, true, legends, volumechart)
            for (i  in 1..testno){
                when (i) {
                    1 -> text_1st.isEnabled = true
                    2 -> text_2nd.isEnabled = true
                    3 -> text_3rd.isEnabled = true
                    4 -> text_4th.isEnabled = true

                }
            }

                // setDatalineChart(entriesVS, testResult.trialResult!![i].isBest, legends, lineChartVS)
           // }


            /*val adapter = TestResultAdapter(testResult.trialResult[].measuredValues)
            recyclerViewResult.adapter = adapter
            adapter.notifyDataSetChanged()*/

           // bestTest(bestTestResult)

            isSelected(testno)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    //get all trial data and display in graph
    private fun showTrialResult(testResult: AirTestResult) {
        try {

            val legends = ArrayList<LegendEntry>()
            binding.chart.clear()
            binding.volumechart.clear()
            for (i in testResult.trialResult!!.indices) {
                val entries = ArrayList<Entry>()
                val entriesVS = ArrayList<Entry>()
                val legendEntry = LegendEntry()
                for (graphData in testResult.trialResult!![i].graphDataList!!) {
                    entries.add(Entry(graphData.volume.toFloat(), graphData.flow.toFloat()))
                    entriesVS.add(Entry(graphData.second.toFloat(), graphData.volume.toFloat()))
                }
                if (testResult.trialResult!![i].isBest) {
                    bestTestResult = i
                    legendEntry.formColor = ContextCompat.getColor(requireContext(), R.color.graph_line_color)
                } else {
                    legendEntry.formColor = ContextCompat.getColor(requireContext(), R.color.white)
                }
                val tCount = i + 1
                legendEntry.label = "Trial $tCount"

                legends.add(legendEntry)
                setDatalineChart(entries, testResult.trialResult!![i].isBest, legends, chart)
                setDatalineChart(entriesVS, testResult.trialResult!![i].isBest, legends, volumechart)
                when (tCount) {
                    1 -> text_1st.isEnabled = true
                    2 -> text_2nd.isEnabled = true
                    3 -> text_3rd.isEnabled = true
                    4 -> text_4th.isEnabled = true

                }

            }


            /*val adapter = TestResultAdapter(testResult.trialResult[].measuredValues)
            recyclerViewResult.adapter = adapter
            adapter.notifyDataSetChanged()*/

            bestTest(bestTestResult)

            isSelected(testResult.trialResult!!.size)


        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showReportResult(testResult: AirTestResult) {
        try {

            var legends = ArrayList<LegendEntry>()
            chartReport .clear()
            chartReportvolume.clear()

            for (i in testResult.trialResult!!.indices) {
                val entries = ArrayList<Entry>()
                val entriesVS = ArrayList<Entry>()
                val legendEntry = LegendEntry()
                for (graphData in testResult.trialResult!![i].graphDataList!!) {
                    entries.add(Entry(graphData.volume.toFloat(), graphData.flow.toFloat()))
                    entriesVS.add(Entry(graphData.second.toFloat(), graphData.volume.toFloat()))
                }
                if (testResult.trialResult!![i].isBest) {
                    bestTestResult = i
                    legendEntry.formColor =
                        ContextCompat.getColor(requireContext(), R.color.graph_line_color)
                } else {
                    legendEntry.formColor =
                        ContextCompat.getColor(requireContext(), R.color.graph_line_color_grey)
                }
                val tCount = i + 1
                legendEntry.label = "Trial $tCount"

                legends.add(legendEntry)
                setReportDatalineChart(entries, testResult.trialResult!![i].isBest, legends, chartReport)
                setReportDatalineChart(entriesVS, testResult.trialResult!![i].isBest, legends, chartReportvolume)
            }
            if (SafeyApplication.postTestResult!=null){
            //for (i in SafeyApplication.postTestResult!!.trialResult!!.indices) {
                val entries = ArrayList<Entry>()
                val entriesVS = ArrayList<Entry>()
                val legendEntry = LegendEntry()
                for (graphData in SafeyApplication.postTestResult!!.trialResult!![0].graphDataList!!) {
                    entries.add(Entry(graphData.volume.toFloat(), graphData.flow.toFloat()))
                    entriesVS.add(Entry(graphData.second.toFloat(), graphData.volume.toFloat()))
                }

                    legendEntry.formColor =
                        ContextCompat.getColor(requireContext(), R.color.graph_line_color_grey)

                val tCount =  1
                legendEntry.label = "Trial $tCount"

                legends.add(legendEntry)
                setReportDatalineChart(entries, false, legends, chartReport)
                setReportDatalineChart(entriesVS, false, legends, chartReportvolume)
            }
//            chartReport.chartBitmap
//            chartReportvolume.chartBitmap

//             legends = ArrayList<LegendEntry>()
//            chartReport .clear()
//            chartReportvolume.clear()
//
//            for (i in testResult.trialResult!!.indices) {
//                val entries = ArrayList<Entry>()
//                val entriesVS = ArrayList<Entry>()
//                val legendEntry = LegendEntry()
//                for (graphData in testResult.trialResult!![i].graphDataList!!) {
//                        entries.add(Entry(graphData.volume.toFloat(), graphData.flow.toFloat()))
//                        entriesVS.add(Entry(graphData.second.toFloat(), graphData.volume.toFloat()))
//                }
//                if (testResult.trialResult!![i].isBest) {
//                    legendEntry.formColor = ContextCompat.getColor(requireContext(), R.color.graph_line_color)
//                } else {
//                    legendEntry.formColor = ContextCompat.getColor(requireContext(), R.color.white)
//                }
//                val tCount = i + 1
//                legendEntry.label = "Trial $tCount"
//
//                legends.add(legendEntry)
//                setReportDatalineChart(entries, testResult.trialResult!![i].isBest, legends, chartReport)
//                setReportDatalineChart(entriesVS, testResult.trialResult!![i].isBest, legends, chartReportvolume)
//
//
//            }
//





        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setReportDatalineChart(
        values: ArrayList<Entry>,
        isBest: Boolean,
        legends: ArrayList<LegendEntry>,
        chart: LineChart
    ) {

        values.add(0, Entry(0.0f,0.0f))
        //val entry =  values.get(values.size-1)

        // values.add(values.size-1, Entry(entry.x,0.0f))
        chart.xAxis.setLabelCount(5,true)
        chart.xAxis.granularity = 1f

        var data: LineData? = chart.data
        val set1 = LineDataSet(values, "DataSet ")
        set1.setDrawValues(false)
        if (isBest) {
            set1.color = ContextCompat.getColor(requireContext(), R.color.rpt_graph_line_color_best)
            set1.lineWidth = 1.4f
        } else {
            set1.color = ContextCompat.getColor(requireContext(), R.color.rpt_line_color)
            set1.lineWidth = 1.9f
        }
        if (data == null) {
            data = LineData(set1)
        } else {
            data.addDataSet(set1)
        }
        data.setValueTextSize(9f)

        val l: Legend = chart.legend

        l.setCustom(legends)

        data.setDrawValues(false)
        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        data.notifyDataChanged()
        chart.data = data
        chart.animateX(2000)
        chart.notifyDataSetChanged()
        chart.invalidate()
    }



    //current trial handling
    private fun isSelected(testno: Int, isPost:Boolean=false,isAll:Boolean=false) {

        if (text_1st.isEnabled) {
            text_1st.visibility = View.VISIBLE
            text_1st.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_bg)
            text_1st.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.test_inactive_color
                )
            )
        }
        if (text_2nd.isEnabled) {
            text_2nd.visibility = View.VISIBLE
            text_2nd.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_bg)
            text_2nd.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        }
        if (text_3rd.isEnabled) {
            text_3rd.visibility = View.VISIBLE
            text_3rd.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_bg)
            text_3rd.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        }
        if (text_4th.isEnabled) {
            text_4th.visibility = View.VISIBLE
            text_4th.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_bg)
            text_4th.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        }

        if (text_post.isEnabled || SafeyApplication.postTestResult!=null) {
            text_post.isEnabled = true
            text_post.visibility = View.VISIBLE
            text_post.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_bg)
            text_post.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        }
        if (text_all.isEnabled) {
            text_all.visibility = View.VISIBLE
            text_all.background =
                ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_bg)
            text_all.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        }



        if (isPost){
            text_post.isEnabled = true
            text_post.background =ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_trial_selected_bg
            )
            text_post.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            return
        }

        if (isAll){
            text_all.isEnabled = true
            text_all.background =ContextCompat.getDrawable(
                requireContext(),
                R.drawable.ic_trial_selected_bg
            )
            text_all.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            return
        }

        if (!isAll || !isPost)
        when (testno) {
            1 -> {
                text_1st.background = ContextCompat.getDrawable(
                    requireContext(),
                    R.drawable.ic_trial_selected_bg
                )
                text_1st.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            2 -> {
                if (text_2nd.isEnabled) {
                    text_2nd.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_selected_bg)
                    text_2nd.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
            3 -> {
                if (text_3rd.isEnabled) {
                    text_3rd.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_selected_bg)
                    text_3rd.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
            4 -> {
                if (text_4th.isEnabled) {
                    text_4th.background =
                        ContextCompat.getDrawable(requireContext(), R.drawable.ic_trial_selected_bg)
                    text_4th.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                }
            }
        }


    }


    //Best trial handling
    private fun bestTest(testno: Int) {
        image_1st.visibility = View.GONE
        image_2nd.visibility = View.GONE
        image_3rd.visibility = View.GONE
        image_4th.visibility = View.GONE
        text_1st.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        text_2nd.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        text_3rd.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        text_4th.setTextColor(ContextCompat.getColor(requireContext(), R.color.test_inactive_color))
        when (testno) {
            0 -> {
                image_1st.visibility = View.VISIBLE
                if(!Constants.isPost)
                text_1st.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            1 -> {
                image_2nd.visibility = View.VISIBLE
                if(!Constants.isPost)
                text_2nd.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            2 -> {
                image_3rd.visibility = View.VISIBLE
                if(!Constants.isPost)
                text_3rd.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
            3 -> {
                image_4th.visibility = View.VISIBLE
                if(!Constants.isPost)
                text_4th.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
            }
        }
    }

    fun getSafeyDeviceKit(){
        safeyDeviceKit = SafeyDeviceKit.init(requireActivity(),"7659-2779-4723-9301-2442")
        safeyDeviceKit?.registerConnectionCallback(this)
    }


    private fun setDatalineChart(
        values: ArrayList<Entry>,
        isBest: Boolean,
        legends: ArrayList<LegendEntry>,
        chart: LineChart
    ) {

        values.add(0, Entry(0.0f,0.0f))
       //val entry =  values.get(values.size-1)

       // values.add(values.size-1, Entry(entry.x,0.0f))
        chart.xAxis.setLabelCount(5,true)
        chart.xAxis.granularity = 1f

        var data: LineData? = chart.data
        val set1 = LineDataSet(values, "DataSet ")
        set1.setDrawValues(false)
        if (isBest) {
            set1.color = ContextCompat.getColor(requireContext(), R.color.graph_line_color)
            set1.lineWidth = 3.4f
        } else {
            set1.color = ContextCompat.getColor(requireContext(), R.color.white)
            set1.lineWidth = 2.9f
        }
        if (data == null) {
            data = LineData(set1)
        } else {
            data.addDataSet(set1)
        }
        data.setValueTextSize(9f)

        val l: Legend = chart.legend

        l.setCustom(legends)

        data.setDrawValues(false)
        data.setValueTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        data.notifyDataChanged()
        chart.data = data
        chart.animateX(2000)
        chart.notifyDataSetChanged()
        chart.invalidate()
    }



    private fun setupCharts(chart: LineChart, xAxisTitle: String) {
        chart.description.isEnabled = false
        chart.description.text = xAxisTitle
        chart.description.textSize = 12F
        chart.setGridBackgroundColor(Color.TRANSPARENT)
        chart.legend.isEnabled = false
        chart.legend.textColor = Color.WHITE
        val xl: XAxis = chart.xAxis
        var listMaxVolume : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.maxByOrNull { it.volume }?.volume?.let { listMaxVolume.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.volume }?.volume?.let { listMaxVolume.add(it) }
        }

        var listMaxFlow : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.maxByOrNull { it.flow }?.flow?.let { listMaxFlow.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.flow }?.flow?.let { listMaxFlow.add(it) }
        }

        var listMinFlow : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.minByOrNull { it.flow }?.flow?.let { listMinFlow.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.flow }?.flow?.let { listMinFlow.add(it) }
        }

        var listMaxSecond : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.maxByOrNull { it.second }?.second?.let { listMaxSecond.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.second }?.second?.let { listMaxSecond.add(it) }
        }

        val flow = Collections.max(listMaxFlow)
        var maxflow = Math.round(flow).toInt()
        maxflow += 2

        val volume = Collections.max(listMaxVolume)
        var maxVolume = Math.round(volume).toInt()
        maxVolume += 2

        val time = Collections.max(listMaxSecond)
        var maxTime = Math.round(time).toInt()
        maxTime += 2

        val minflowval = Collections.min(listMinFlow)
        var minflow = Math.round(minflowval).toInt()
        minflow -= 2


        chart.getXAxis().setGranularity(1.0f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.axisLeft.setGranularity(1.0f);
        chart.axisLeft.setGranularityEnabled(true);
        chart.getXAxis().setAxisMinValue(0.0f); //experiment with these values
        if (xAxisTitle.equals("Flow")) {
            chart.xAxis.axisMaxLabels = maxVolume
            chart.getXAxis().setAxisMaxValue(maxVolume.toFloat());
            chart.getXAxis().setLabelCount(maxVolume, true);
        }
        else
        {
            chart.xAxis.axisMaxLabels = maxTime
            chart.getXAxis().setAxisMaxValue(maxTime.toFloat());
            chart.getXAxis().setLabelCount(maxTime, true);
        }
        xl.textColor = ContextCompat.getColor(requireContext(), R.color.white)
        xl.isEnabled = true
        xl.axisLineColor = ContextCompat.getColor(requireContext(), R.color.white)
        val leftAxis: YAxis = chart.axisLeft
        leftAxis.isEnabled = true
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.white)

        if (xAxisTitle.equals("Flow")) {
            if (SafeyApplication.firstTestResult?.testtype == 1) {
                chart.axisLeft.setAxisMinValue(0.0f);
                chart.axisLeft.setAxisMaxValue(maxflow.toFloat())
                leftAxis.axisMaxLabels = maxflow+1
                leftAxis.setLabelCount(maxflow+1, true)
            } else {
                chart.axisLeft.setAxisMinValue(minflow.toFloat());
                chart.axisLeft.setAxisMaxValue(1.0f)

                var num = Math.abs(minflow)
                num += 2
                leftAxis.axisMaxLabels = num
                leftAxis.setLabelCount(num, true)
            }

        }
        else{
            if (SafeyApplication.firstTestResult?.testtype == 1)
                chart.axisLeft.setAxisMinValue(0.0f);
            chart.axisLeft.setAxisMaxValue(maxVolume.toFloat())
            leftAxis.axisMaxLabels = maxVolume
            leftAxis.setLabelCount(maxVolume, true)
        }
        leftAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.graph_line_color)


       // leftAxis.axisMaximum = 7f
        leftAxis.zeroLineColor = ContextCompat.getColor(requireContext(), R.color.white)




    }

    private fun setupCharts(chart: LineChart, xAxisTitle: String,report:Int =1) {
        chart.description.isEnabled = false
        chart.description.text = xAxisTitle
        chart.description.textSize = 12F
        chart.setGridBackgroundColor(Color.TRANSPARENT)
        chart.legend.isEnabled = false
        chart.legend.textColor = Color.BLACK
        val xl: XAxis = chart.xAxis
        var listMaxVolume : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.maxByOrNull { it.volume }?.volume?.let { listMaxVolume.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.volume }?.volume?.let { listMaxVolume.add(it) }
        }

        var listMaxFlow : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.maxByOrNull { it.flow }?.flow?.let { listMaxFlow.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.flow }?.flow?.let { listMaxFlow.add(it) }
        }

        var listMinFlow : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.minByOrNull { it.flow }?.flow?.let { listMinFlow.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.flow }?.flow?.let { listMinFlow.add(it) }
        }

        var listMaxSecond : MutableList<Double> = ArrayList()
        for ( trial in SafeyApplication.firstTestResult?.trialResult!!){
            trial.graphDataList?.maxByOrNull { it.second }?.second?.let { listMaxSecond.add(it) }
        }
        if (SafeyApplication.postTestResult!=null){
            SafeyApplication.postTestResult?.trialResult!![0].graphDataList?.maxByOrNull { it.second }?.second?.let { listMaxSecond.add(it) }
        }

        val flow = Collections.max(listMaxFlow)
        var maxflow = Math.round(flow).toInt()
        maxflow += 2

        val volume = Collections.max(listMaxVolume)
        var maxVolume = Math.round(volume).toInt()
        maxVolume += 2

        val time = Collections.max(listMaxSecond)
        var maxTime = Math.round(time).toInt()
        maxTime += 2

        val minflowval = Collections.min(listMinFlow)
        var minflow = Math.round(minflowval).toInt()
        minflow -= 2





        chart.getXAxis().setGranularity(1.0f);
        chart.getXAxis().setGranularityEnabled(true);
        chart.axisLeft.setGranularity(1.0f);
        chart.axisLeft.setGranularityEnabled(true);
        chart.getXAxis().setAxisMinValue(0.0f); //experiment with these values
        if (xAxisTitle.equals("Flow")) {
            chart.xAxis.axisMaxLabels = maxVolume
            chart.getXAxis().setAxisMaxValue(maxVolume.toFloat());
            chart.getXAxis().setLabelCount(maxVolume, true);
        }
        else
        {
            chart.xAxis.axisMaxLabels = maxTime
            chart.getXAxis().setAxisMaxValue(maxTime.toFloat());
            chart.getXAxis().setLabelCount(maxTime, true);
        }
        xl.textColor = ContextCompat.getColor(requireContext(), R.color.black_two)
        xl.isEnabled = true
        xl.axisLineColor = ContextCompat.getColor(requireContext(), R.color.black_two)
        val leftAxis: YAxis = chart.axisLeft
        leftAxis.isEnabled = true
        leftAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_two)

        if (xAxisTitle.equals("Flow")) {
            if (SafeyApplication.firstTestResult?.testtype == 1) {
                chart.axisLeft.setAxisMinValue(0.0f);
                chart.axisLeft.setAxisMaxValue(maxflow.toFloat())
                leftAxis.axisMaxLabels = maxflow+1
                leftAxis.setLabelCount(maxflow+1, true)
            } else {
                chart.axisLeft.setAxisMinValue(minflow.toFloat());
                chart.axisLeft.setAxisMaxValue(1.0f)

                var num = Math.abs(minflow)
                num += 2
                leftAxis.axisMaxLabels = num
                leftAxis.setLabelCount(num, true)
            }

        }
        else{
            if (SafeyApplication.firstTestResult?.testtype == 1)
                chart.axisLeft.setAxisMinValue(0.0f);

            chart.axisLeft.setAxisMaxValue(maxVolume.toFloat())
            leftAxis.axisMaxLabels = maxVolume
            leftAxis.setLabelCount(maxVolume, true)
        }
        leftAxis.axisLineColor = ContextCompat.getColor(requireContext(), R.color.graph_line_color)


        // leftAxis.axisMaximum = 7f
        leftAxis.zeroLineColor = ContextCompat.getColor(requireContext(), R.color.black_two)




    }

    override fun getConnected(isConnected: Boolean) {
        if (!isConnected)
            findNavController().popBackStack(R.id.nav_home,false)
    }

    override fun positiveButtonClick() {

            createPDF()
    }

    override fun negativeButton() {
        safeyDeviceKit?.finishTest()
            viewModel.completeTest()
       // findNavController().popBackStack(R.id.nav_home,false)
    }

    fun createPDF() {
        dialog = CustomDialogs.dialogStyleProgress(CustomDialogStyle1DataModel(requireActivity(),"",getString(R.string.please_wait),"","",this))
        CoroutineScope(Dispatchers.Main).launch {
            //Create document file
            val filename = "Safey PFT Report"

            try {
                val format = SimpleDateFormat(Constants.DATE_FORMAT_PATTERN)
                val dateFormat = SimpleDateFormat("ddMMyyyy_HHmm")
                var f1 = File(activity?.getExternalFilesDir(null), "Safey")
                if (!f1.exists()) {
                    f1.mkdirs()
                }
                f1.delete()
                if (!f1.exists()) {
                    f1.mkdirs()
                }
                fileShare = File(f1, filename + ".pdf")
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
                table.setPaddings(16f, 0f, 16f, 16f)
                val fileImage = File(f1, "ehealthlogo.png")
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
                patientDetails["Name"] = SafeyApplication.firstTestResult?.fname.toString()
                patientDetails["Height"] =
                    SafeyApplication.firstTestResult?.height.toString() + " " + enumHeightUnit.fromInt(
                        viewModel.patientData.HeightUnit
                    ).getFormatString()
                patientDetails["Birth Date"] =
                    Utility.getFormattedDate(SafeyApplication.firstTestResult?.dob?.let {
                        longDate(
                            it.toLong())
                    })!!
                patientDetails["Age"] =
                    "" + Utility.getAge(SafeyApplication.firstTestResult?.dob!!.toLong()) + "yrs"
                patientDetails["Gender"] = posToGender(
                    /*SafeyApplication.firstTestResult?.gender!!.toInt()*/
                1
                )
               /* patientDetails["Ethnicity"] =
                    positionToEthnicity(viewModel.patientData.ethnicity)*/
                patientDetails["UHID"] = SafeyApplication.firstTestResult?.userId.toString()

                p.add(PrintUtility.keyValuePairParagraphPdf(patientDetails))

                cell.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                // cell.setMarginRight(16f)
                cell.add(p)
                table.addCell(cell)
                document.add(table)


//            table = Table(2)
//            // table.setAutoLayout()
//            table.setBorder(Border.NO_BORDER)
//            //table.setWidth(300f)
//            //table.setPaddings(16f,0f,16f,16f)
//
//
//            cell = Cell()
//            cell.setBorder(Border.NO_BORDER)
//            p = Paragraph()
//
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#ffffff", 12f))
//            cell.setHorizontalAlignment(HorizontalAlignment.LEFT)
//            cell.add(p)
//
//
//            table.addCell(cell)
////            cell = Cell()
////             p = Paragraph()
////            p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
////            p.setPaddingRight(8f)
////            p.setWidth(430f)
////            cell.setWidth(430f)
//
//            cell = Cell()
//            p = Paragraph()
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#000000", 8f))
//            p.setHorizontalAlignment(HorizontalAlignment.LEFT)
//
//            cell.setBorder(Border.NO_BORDER)
//            cell.add(p)
//            table.addCell(cell)
//            document.add(table)


                table = Table(8)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference : GLI 2012", "#000000", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference : GLI", "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Session Quality", "#000000", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
//            val filesesson = when (SafeyApplication.firstTestResult!!.sessionScore) {
//                "A" -> "a.png"
//                "B" -> "b.png"
//                "C" -> "c.png"
//                "D" -> "d.png"
//                "E" -> "e.png"
//                "F" -> "f.png"
//                else -> "na.png"
//            }
//            var varianceImage = File(f1, filesesson)
//            var imagevarianceUri = varianceImage.absolutePath
//            var imgvariance = Image(ImageDataFactory.create(imagevarianceUri))
//            imgvariance.setHorizontalAlignment(HorizontalAlignment.CENTER)
//            imgvariance.setTextAlignment(TextAlignment.CENTER)

                p = Paragraph(
                    PrintUtility.pdfTextA4(
                        SafeyApplication.firstTestResult!!.sessionScore!!,
                        true,
                        "#00D16C",
                        8.0f
                    )
                )

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                cell.setVerticalAlignment(VerticalAlignment.MIDDLE)
                table.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference", "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Suggested Diagnosis   - ", "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("   Obstructive dialgonstics" , "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("", "#ffffff", 8f))
                p.setBackgroundColor(WebColors.getRGBColor("#ffffff"), 8f, 8f, 8f, 8f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                cell.setBackgroundColor(WebColors.getRGBColor("#ffffff"))
                    .setBorderBottomLeftRadius(BorderRadius(8f))
                    .setBorderTopRightRadius(BorderRadius(8f))
                    .setBorderTopLeftRadius(BorderRadius(8f))
                    .setBorderBottomRightRadius(BorderRadius(8f))
                table.addCell(cell)

                document.add(table)



                table = Table(14)
                table.setMarginTop(8f)
                table.setPaddingTop(8f)

                table.setWidth(530f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Flow/Volume", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)

                cell.add(p)
                table.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Trial", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Ref", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("1", "#000000", 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                var starImage = File(f1, "rpt_star.png")
                var imageStarUri = starImage.absolutePath
                var imgstar = Image(ImageDataFactory.create(imageStarUri))
                imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                imgstar.setTextAlignment(TextAlignment.CENTER)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 0) {
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)

                var color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 1 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                 color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("2", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 1) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)
                 color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size ==2 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("3", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 2) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)
                 color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 3 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = if (SafeyApplication.postTestResult != null)
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)
                else
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                table.setBorderTopLeftRadius(BorderRadius(8f))
                table.setBorderTopRightRadius(BorderRadius(8f))
                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)

                table = Table(2)
                table.setWidth(530f)
                table.setPaddingBottom(16f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))
                    .setPaddingBottom(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                var stream = ByteArrayOutputStream()

                var bitmap: Bitmap? = chartReport.chartBitmap
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                var myImg = Image(ImageDataFactory.create(stream.toByteArray()))
                myImg.setWidth(480f)
                myImg.setHeight(160f)
                myImg.setHorizontalAlignment(HorizontalAlignment.LEFT)
                myImg.setTextAlignment(TextAlignment.LEFT)
                table.addCell(
                    Cell().setBorder(Border.NO_BORDER)
                        .setBackgroundColor(WebColors.getRGBColor("#F9FAFF")).add(myImg)
                        .setPaddingBottom(12f)
                )


                // table.setBorderTopLeftRadius(BorderRadius(8f))
                // table.setBorderTopRightRadius(BorderRadius(8f))
                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)


                table = Table(14)
                table.setWidth(530f)
                table.setMarginTop(6f)
                table.setPaddingTop(16f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Volume/Time", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Trial", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Ref", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("1", "#000000", 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 0) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)

                 color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 1 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("2", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 1) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)
                 color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 2 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("3", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 2) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)

                 color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 3 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = if (SafeyApplication.postTestResult != null)
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)
                else
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)

                p.setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference", "#F9FAFF", 8f))
                p.setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                table.setBorderTopLeftRadius(BorderRadius(8f))
                table.setBorderTopRightRadius(BorderRadius(8f))
                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)

                table = Table(2)
                table.setWidth(530f)
                table.setPaddingBottom(16f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                p.setPaddingBottom(16f)
                cell.add(p)
                table.addCell(cell)

                stream = ByteArrayOutputStream()

                bitmap = chartReportvolume.chartBitmap
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                myImg = Image(ImageDataFactory.create(stream.toByteArray()))
                myImg.setWidth(480f)
                myImg.setHeight(160f)
                myImg.setHorizontalAlignment(HorizontalAlignment.LEFT)
                myImg.setTextAlignment(TextAlignment.LEFT)
                table.addCell(
                    Cell().setBorder(Border.NO_BORDER)
                        .setBackgroundColor(WebColors.getRGBColor("#F9FAFF")).add(myImg)
                        .setPaddingBottom(16f)
                )


                // table.setBorderTopLeftRadius(BorderRadius(8f))
                // table.setBorderTopRightRadius(BorderRadius(8f))

                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)


                var tableMeasurment = Table(2)
                tableMeasurment.setWidth(530f)
                cell = Cell()
                cell.setBorder(Border.NO_BORDER)

                p = Paragraph(PrintUtility.pdfText("Pre- Bronchodilator", "#000C86", 10f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)

                p = Paragraph(PrintUtility.pdfText("Post- Bronchodilator", "#000C86", 10f))

                p.setTextAlignment(TextAlignment.RIGHT)
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)


                tableMeasurment.addCell(cell)

                document.add(tableMeasurment)

                var tablePrePost = Table(2)
                tablePrePost.setWidth(530f)
                tablePrePost.setBorder(Border.NO_BORDER)

                tableMeasurment = Table(9)
                tableMeasurment.setWidth(400f)
                tableMeasurment.setAutoLayout()

                var bgcolor = "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.setBorderTopLeftRadius(BorderRadius(8f))
                cell.setHeight(10f)
                cell.add(p)

                tableMeasurment.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Measurements", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setHeight(10f)
                cell.add(p)

                tableMeasurment.addCell(cell)


                if (bestTestResult == 0) {
                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    p = Paragraph().add(
                        PrintUtility.pdfText(
                            "BEST",
                            "#000C86",
                            7f,
                            ""
                        )
                    )
                        .setBackgroundColor(WebColors.getRGBColor("#FFC725"))
                        .setBorderBottomLeftRadius(BorderRadius(4f))
                        .setBorderBottomRightRadius(BorderRadius(4f))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    tableMeasurment.addCell(
                        Cell().setMarginLeft(2f).setTextAlignment(TextAlignment.CENTER)
                            .setBorder(Border.NO_BORDER)
                            .setBackgroundColor(WebColors.getRGBColor(bgcolor))
                            .setBorder(Border.NO_BORDER).add(p)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    )
                } else {
                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Trial 1", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.RIGHT)

                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                    tableMeasurment.addCell(cell)
                }

                if (bestTestResult == 1) {

                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Best", "#000000", 7f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    p = Paragraph().add(
                        PrintUtility.pdfText(
                            "BEST",
                            "#000C86",
                            7f,
                            ""
                        )
                    )
                        .setBackgroundColor(WebColors.getRGBColor("#FFC725"))
                        .setBorderBottomLeftRadius(BorderRadius(4f))
                        .setBorderBottomRightRadius(BorderRadius(4f))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    tableMeasurment.addCell(
                        Cell().setMarginLeft(2f).setTextAlignment(TextAlignment.CENTER)
                            .setBorder(Border.NO_BORDER)
                            .setBackgroundColor(WebColors.getRGBColor(bgcolor))
                            .setBorder(Border.NO_BORDER).add(p)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    )

                } else {
                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    cell.setHeight(10f)
                    p = Paragraph(PrintUtility.pdfText("Trial 3", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                    tableMeasurment.addCell(cell)
                }



                if (bestTestResult == 2) {
                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Best", "#000000", 7f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    p = Paragraph().add(
                        PrintUtility.pdfText(
                            "BEST",
                            "#000C86",
                            7f,
                            ""
                        )
                    )
                        .setBackgroundColor(WebColors.getRGBColor("#FFC725"))
                        .setBorderBottomLeftRadius(BorderRadius(4f))
                        .setBorderBottomRightRadius(BorderRadius(4f))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    tableMeasurment.addCell(
                        Cell().setMarginLeft(2f).setTextAlignment(TextAlignment.CENTER)
                            .setBorder(Border.NO_BORDER)
                            .setBackgroundColor(WebColors.getRGBColor(bgcolor))
                            .setBorder(Border.NO_BORDER).add(p)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    )
                } else {
                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    cell.setHeight(10f)
                    p = Paragraph(PrintUtility.pdfText("Trial 3", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                    tableMeasurment.addCell(cell)
                }

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("LNN", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Z-score  ", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)


                cell.setBorderTopRightRadius(BorderRadius(8f))
                tableMeasurment.addCell(cell)


//            document.add(tableMeasurment)
//
//            tableMeasurment = Table(9)
//            tableMeasurment.setWidth(400f)
//            tableMeasurment.setAutoLayout()
//            table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
//            table.setBorder(Border.NO_BORDER)
                bgcolor = "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))




                cell.add(p)

                tableMeasurment.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Measurements", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.add(p)

                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Trial 1", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)

                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1)
                    Paragraph(PrintUtility.pdfText("Trial 2", "#000000", 8f, 0.5f))
                else
                    Paragraph(PrintUtility.pdfText("Trial 2", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2)
                    Paragraph(PrintUtility.pdfText("Trial 3", "#000000", 8f, 0.5f))
                else
                    Paragraph(PrintUtility.pdfText("Trial 3", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("LNN", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Z-score  ", "#50000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)


                tableMeasurment.addCell(cell)


                //var result1 = SafeyApplication.firstTestResult!!.trialResult!!.filter { it.isBest }[0]



                var list = arrayOf(
                    getString(R.string.fvc),
                    getString(R.string.fev1),
                    getString(R.string.fev1fvc),
                    getString(R.string.pef)
                )

                val listMeasure: MutableList<Report_Measurements> = ArrayList()
                if (SafeyApplication.firstTestResult!!.type == 1) {
                     list = arrayOf(
                        getString(R.string.fev1),

                        getString(R.string.pef)
                    )
                    for (i in 0..SafeyApplication.firstTestResult!!.trialResult!!.size - 1) {
                        var result =
                            SafeyApplication.firstTestResult!!.trialResult!![i].mesurementlist
                        var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()

                        var fev1 =
                            result!!.filter { it.measurement == getString(R.string.fev1) }[0]
                        testMeasurementsList.add(fev1)

                        var pefval =
                            result.filter { it.measurement == getString(R.string.pef) }[0]
                        testMeasurementsList.add(pefval)
                        listMeasure.add(Report_Measurements(testMeasurementsList))
                    }

                }
                else {
                     list = arrayOf(
                        getString(R.string.fvc),
                        getString(R.string.fev1),
                        getString(R.string.fev1fvc),
                        getString(R.string.pef)
                    )
                    for (i in 0..SafeyApplication.firstTestResult!!.trialResult!!.size - 1) {
                        var result =
                            SafeyApplication.firstTestResult!!.trialResult!![i].mesurementlist
                        var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()
                        var fvcval =
                            result!!.filter { it.measurement == getString(R.string.fvc) }[0]
                        testMeasurementsList.add(fvcval)
                        var fev1 =
                            result.filter { it.measurement == getString(R.string.fev1) }[0]
                        testMeasurementsList.add(fev1)
                        var fev1fvcval =
                            result.filter { it.measurement == getString(R.string.fev1fvc) }[0]
                        testMeasurementsList.add(fev1fvcval)
                        var pefval =
                            result.filter { it.measurement == getString(R.string.pef) }[0]
                        testMeasurementsList.add(pefval)
                        listMeasure.add(Report_Measurements(testMeasurementsList))
                    }
                }

                for (i in 0 until list.size) {
                    bgcolor = "#FFFFFF"

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.LEFT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    if (i == 0) {
                        cell.setBorderTopLeftRadius(BorderRadius(8f))
                    }

                    cell.add(p)

                    tableMeasurment.addCell(cell)


                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText(list[i], "#50000000", 8f))
                    p.setTextAlignment(TextAlignment.LEFT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                    cell.add(p)

                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    var result = SafeyApplication.firstTestResult!!.trialResult!![0].mesurementlist
                    var testMeasurementsMain: TestMeasurements =
                        result!!.filter { it.measurement == list[i] }[0]
                    p = Paragraph(
                        PrintUtility.pdfText(
                            "" + String.format(
                                "%.2f",
                                testMeasurementsMain.measuredValue
                            ) + " ${testMeasurementsMain.unit}", "#50000000", 8f
                        )
                    )
                    p.setTextAlignment(TextAlignment.CENTER)

                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1) {

                        val result =
                            SafeyApplication.firstTestResult!!.trialResult!![1].mesurementlist
                        val testMeasurements = result!!.filter { it.measurement == list[i] }[0]
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurements.measuredValue
                                ) + " ${testMeasurements.unit}", "#000000", 8f
                            )
                        )
                    } else {

                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurementsMain.measuredValue
                                ) + " ${testMeasurementsMain.unit}", "#ffffff", 8f
                            )
                        )
                    }

                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2) {
                        val result =
                            SafeyApplication.firstTestResult!!.trialResult!![2].mesurementlist
                        val testMeasurements = result!!.filter { it.measurement == list[i] }[0]
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurements.measuredValue
                                ) + " ${testMeasurements.unit}", "#000000", 8f
                            )
                        )
                    } else {

                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurementsMain.measuredValue
                                ) + " ${testMeasurementsMain.unit}", "#ffffff", 8f
                            )
                        )
                    }

                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.predictedValue!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsMain.predictedValue!!,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.predictedPer),
                                "#00D16C",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsMain.predictedPer,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.lln != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.lln!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsMain.lln,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.zScore != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.zScore!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + (testMeasurementsMain.zScore),
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)
                    if (i == 0) {
                        cell.setBorderTopRightRadius(BorderRadius(8f))
                    }



                    tableMeasurment.addCell(cell)
                }

                tableMeasurment.setBorderTopLeftRadius(BorderRadius(8f))
                tableMeasurment.setBorderTopRightRadius(BorderRadius(8f))
                //document.add(tableMeasurment)


                tablePrePost.addCell(
                    Cell().setWidth(400f).add(tableMeasurment).setBorder(Border.NO_BORDER)
                )


                tableMeasurment = Table(2)
                tableMeasurment.setWidth(120f)
                tableMeasurment.setAutoLayout()
                    .setBorder(Border.NO_BORDER)
                    .setPaddingLeft(20f)
                    .setMarginLeft(10f)

                tableMeasurment.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                tableMeasurment.setBorder(Border.NO_BORDER)
                bgcolor = "#F9FAFF"
                cell = Cell()

                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Best", "#F9FAFF", 7f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.setBorderTopLeftRadius(BorderRadius(8f))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                tableMeasurment.addCell(cell)


                cell = Cell()

                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref %", "#F9FAFF", 7f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.setBorderTopRightRadius(BorderRadius(8f))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                tableMeasurment.addCell(cell)


                val listMeasurePost: MutableList<Report_Measurements> = ArrayList()
                if (SafeyApplication.postTestResult != null)
                    for (i in 0..SafeyApplication.postTestResult!!.trialResult!!.size - 1) {
                        var results =
                            SafeyApplication.postTestResult!!.trialResult!![i].mesurementlist
                        var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()
                        var fvcval =
                            results!!.filter { it.measurement == getString(R.string.fvc) }[0]
                        testMeasurementsList.add(fvcval)
                        var fev1 =
                            results.filter { it.measurement == getString(R.string.fev1) }[0]
                        testMeasurementsList.add(fev1)
                        var fev1fvcval =
                            results.filter { it.measurement == getString(R.string.fev1fvc) }[0]
                        testMeasurementsList.add(fev1fvcval)
                        var pefval =
                            results.filter { it.measurement == getString(R.string.pef) }[0]
                        testMeasurementsList.add(pefval)
                        listMeasurePost.add(Report_Measurements(testMeasurementsList))
                    }


                for (i in 0..4) {
                    var text1 = ""
                    var text2 = ""
                    var textcolor = ""
                    if (i == 0) {
                        bgcolor = "#F9FAFF"
                        text1 = "Best"
                        text2 = "Ref %"
                        textcolor = "#000000"
                    } else {
                        if (SafeyApplication.postTestResult == null) {
                            bgcolor = "#FFFFFF"
                            text1 = "-"
                            text2 = "-"
                            textcolor = "#FFFFFF"
                        } else {

                            val result =
                                SafeyApplication.postTestResult!!.trialResult!![0].mesurementlist
                            val testMeasurements =
                                result!!.filter { it.measurement == list[i - 1] }[0]
                            val measuredValue = String.format(
                                "%.2f",
                                testMeasurements.measuredValue
                            ) + " ${testMeasurements.unit}"

                            val perc = if (testMeasurements.predictedValue != " -  ")
                                "" + Utility.rounded(testMeasurements.predictedPer)
                            else
                                "-"
                            bgcolor = "#FFFFFF"
                            text1 = measuredValue
                            text2 = "" + perc
                            textcolor = "#FFFFFF"
                        }
                    }
                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (i == 0)
                        Paragraph(PrintUtility.pdfText(text1, "#000000", 8f, 0.5f))
                    else
                        Paragraph(PrintUtility.pdfText(text1, "#000000", 8f))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    tableMeasurment.addCell(cell)


                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (i == 0)
                        Paragraph(PrintUtility.pdfText(text2, "#000000", 8f, 0.5f))
                    else
                        Paragraph(PrintUtility.pdfText(text2, "#000000", 8f))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    tableMeasurment.addCell(cell)
                }
                tableMeasurment.setBorderTopLeftRadius(BorderRadius(8f))
                tableMeasurment.setBorderTopRightRadius(BorderRadius(8f))
                tablePrePost.addCell(
                    Cell().setWidth(130f).setMarginLeft(20f).add(tableMeasurment)
                        .setHorizontalAlignment(HorizontalAlignment.LEFT).setBorder(
                            Border.NO_BORDER
                        )
                )


                document.add(tablePrePost)

                p = Paragraph(PrintUtility.pdfText("Re", "#1F4E78"))
                p.setFixedPosition(0f, 0f, PageSize.A4.width)
                p.setBackgroundColor(WebColors.getRGBColor("#1F4E78"))

                p.add(PrintUtility.pdfText("Prepared by: eHealthSystem Technologies", "#ffffff"))



                p.add(Tab())
                p.addTabStops(TabStop(1000F, TabAlignment.RIGHT))
                p.add(
                    PrintUtility.pdfText(
                        "Test Date- " + Utility.getFormattedDateTime(Date()),
                        "#ffffff"
                    )
                )
                p.add(PrintUtility.pdfText("Re", "#1F4E78"))
                document.add(p)


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
                patientDetails["Name"] = viewModel.patientData.FirstName
                patientDetails["Height"] =
                    viewModel.patientData.Height + " " + enumHeightUnit.fromInt(
                        viewModel.patientData.HeightUnit
                    ).getFormatString()
                patientDetails["Birth Date"] =
                    Utility.getFormattedDate(longDate(viewModel.patientData.BirthDate))!!
                patientDetails["Age"] =
                    "" + Utility.getAge(viewModel.patientData.BirthDate) + "yrs"
                patientDetails["Gender"] = posToGender(viewModel.patientData.Gender)
                patientDetails["Ethnicity"] =
                    positionToEthnicity(viewModel.patientData.ethnicity)

                p.add(PrintUtility.keyValuePairParagraphPdf(patientDetails))

                cell.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                // cell.setMarginRight(16f)
                cell.add(p)
                table.addCell(cell)
                document.add(table)

//            table = Table(2)
//            // table.setAutoLayout()
//            table.setBorder(Border.NO_BORDER)
//            //table.setWidth(300f)
//            //table.setPaddings(16f,0f,16f,16f)
//
//
//            cell = Cell()
//            cell.setBorder(Border.NO_BORDER)
//            p = Paragraph()
//
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#ffffff", 12f))
//            cell.setHorizontalAlignment(HorizontalAlignment.LEFT)
//            cell.add(p)
//
//
//            table.addCell(cell)
////            cell = Cell()
////             p = Paragraph()
////            p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
////            p.setPaddingRight(8f)
////            p.setWidth(430f)
////            cell.setWidth(430f)
//
//            cell = Cell()
//            p = Paragraph()
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#000000", 8f))
//            p.setHorizontalAlignment(HorizontalAlignment.LEFT)
//
//            cell.setBorder(Border.NO_BORDER)
//            cell.add(p)
//            table.addCell(cell)
//            document.add(table)

                p = Paragraph(PrintUtility.pdfText("Re", "#1F4E78"))
                p.setFixedPosition(0f, 0f, PageSize.A4.width)
                p.setBackgroundColor(WebColors.getRGBColor("#1F4E78"))

                p.add(PrintUtility.pdfText("Prepared by: eHealthSystem Technologies", "#ffffff"))



                p.add(Tab())
                p.addTabStops(TabStop(1000F, TabAlignment.RIGHT))
                p.add(
                    PrintUtility.pdfText(
                        "Test Date- " + Utility.getFormattedDateTime(Date()),
                        "#ffffff"
                    )
                )
                p.add(PrintUtility.pdfText("Re", "#1F4E78"))
                document.add(p)


                table = Table(1)
                table.setWidth(540f)
                // table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setMarginTop(16f)
                table.setBorder(Border.NO_BORDER)
                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                var best = bestTestResult + 1
                p = Paragraph(PrintUtility.pdfText("Me", "#F9FAFF", 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor("#FFFFFF"))
                p.add(
                    PrintUtility.pdfText(
                        "   Other Measurements from Best Trial - trial $best",
                        "#000C86",
                        14f
                    )
                )
                cell.add(p).setBackgroundColor(WebColors.getRGBColor("#F9FAFF"))
                cell.setBorderTopRightRadius(BorderRadius(8f))
                cell.setBorderTopLeftRadius(BorderRadius(8f))

                table.addCell(cell)
                document.add(table)
                tableMeasurment = Table(6)
                tableMeasurment.setWidth(540f)
                tableMeasurment.setAutoLayout()
                tableMeasurment.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                tableMeasurment.setBorder(Border.NO_BORDER)
                bgcolor = "#F9FAFF"

//            cell = Cell()
//            cell.setBorder(Border.NO_BORDER)
//            p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
//            p.setTextAlignment(TextAlignment.LEFT)
//            p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//            cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//
//
//
//
//            cell.add(p)
//
//            tableMeasurment.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Me", "#FFFFFF", 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                p.add(PrintUtility.pdfText("Measurements", "#000000", 8f, 0.5f))
                cell.add(p)

                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Measured Value", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)

                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                tableMeasurment.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Reference Value", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("LNN", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Z-score  ", "#50000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                cell.setBorderTopRightRadius(BorderRadius(8f))

                tableMeasurment.addCell(cell)

                var result =if(SafeyApplication.firstTestResult!!.trialResult!!.size==1)
                 SafeyApplication.firstTestResult!!.trialResult!!.get(0)
                else
                 SafeyApplication.firstTestResult!!.trialResult!!.filter { it.isBest }[0]

                var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()

                testMeasurementsList.addAll(result.mesurementlist!!)


                for (i in 0 until testMeasurementsList.size) {
                    bgcolor = "#FFFFFF"

//                cell = Cell()
//                cell.setBorder(Border.NO_BORDER)
//                p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
//                p.setTextAlignment(TextAlignment.LEFT)
//                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//                if(i==0) {
//                    cell.setBorderTopLeftRadius(BorderRadius(8f))
//                }
//
//                cell.add(p)
//
//                tableMeasurment.addCell(cell)


                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Me", "#FFFFFF", 8f))
                    p.setTextAlignment(TextAlignment.LEFT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    p.add(
                        PrintUtility.pdfText(
                            testMeasurementsList[i].measurement!!,
                            "#50000000",
                            8f
                        )
                    )
                    cell.add(p)

                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(
                        PrintUtility.pdfText(
                            "" + String.format(
                                "%.2f",
                                testMeasurementsList[i].measuredValue
                            ) + " ${testMeasurementsList[i].unit}", "#50000000", 8f
                        )
                    )
                    p.setTextAlignment(TextAlignment.CENTER)

                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                    tableMeasurment.addCell(cell)



                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsList[i].predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].predictedValue!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsList[i].predictedValue,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsList[i].predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].predictedPer),
                                "#00D16C",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsList[i].predictedPer,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsList[i].lln != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].lln!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsList[i].lln,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    if (testMeasurementsList[i].zScore != " -  ")
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].zScore!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + (testMeasurementsList[i].zScore),
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)
                    if (i == 0) {
                        cell.setBorderTopRightRadius(BorderRadius(8f))
                    }



                    tableMeasurment.addCell(cell)
                }

                tableMeasurment.setBorderTopLeftRadius(BorderRadius(8f))
                tableMeasurment.setBorderTopRightRadius(BorderRadius(8f))
                document.add(tableMeasurment)

                p = Paragraph(PrintUtility.pdfText("Re", "#1F4E78"))
                p.setFixedPosition(0f, 0f, PageSize.A4.width)
                p.setBackgroundColor(WebColors.getRGBColor("#1F4E78"))

                p.add(PrintUtility.pdfText("Prepared by: eHealthSystem Technologies", "#ffffff"))
                p.add(Tab())
                p.addTabStops(TabStop(1000F, TabAlignment.RIGHT))
                p.add(
                    PrintUtility.pdfText(
                        "Test Date- " + Utility.getFormattedDateTime(Date()),
                        "#ffffff"
                    )
                )
                p.add(PrintUtility.pdfText("Re", "#1F4E78"))
                document.add(p)



                document.close()
                loadPdf(fileShare)


                if (trialCount != 0) {
                    safey_finish_addpost_button.visibility = View.GONE
                    txtAddTrial.visibility = View.GONE
                    safeyDeviceKit?.finishTest()
                    viewModel.completeTest(1)
                    trialCount = 0
                }
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    fileShare
                );
                dialog?.dismiss()
               /* val share = Intent()
                share.action = Intent.ACTION_SEND
                share.type = "application/pdf"
                share.putExtra(Intent.EXTRA_STREAM, uri)
                requireActivity().startActivity(share)*/

                // prepareFilePart("RedcliffeReport.pdf",fileShare.toUri())

                // viewModel.upload(prepareFilePart("RedcliffeReport.pdf",fileShare.toUri()))

                convertToBase64(fileShare,SafeyApplication.firstTestResult?.userId.toString())

              /*  val file = FileDataPart.from("path_to_your_file", name = "image")
                val (_, _, result) = Fuel.upload("http://10.0.2.2:3000/test")
                    .add(file)
                    .responseObject<CustomResponse>()*/


            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

//            val username = "pravinmca09@gmail.com"
//            val password = "Holzoo@03"
//
//
//
//            Thread {
//                try {
//                    val props = Properties()
//                    props["mail.smtp.auth"] = "true"
//                    props["mail.smtp.starttls.enable"] = "true"
//                    props["mail.smtp.host"] = "smtp.gmail.com"
//                    props["mail.smtp.port"] = "587"
//                    val session: javax.mail.Session = javax.mail.Session.getInstance(props,
//                        object : Authenticator() {
//                            override fun getPasswordAuthentication(): PasswordAuthentication {
//                                //return super.getPasswordAuthentication()
//                                return  PasswordAuthentication(
//                                    username, password
//                                )
//                            }
//                        })
//                    // TODO Auto-generated method stub
//                    val message: Message = MimeMessage(session)
//                    message.setFrom(InternetAddress(username))
//                    message.setRecipients(
//                        Message.RecipientType.TO,
//                        InternetAddress.parse("pravinbadgujar90@gmail.com")
//                    )
//                    message.setSubject("email")
//                    message.setText(
//                        """HI,
//
// great"""
//                    )
//                    Transport.send(message)
//                    println("Done")
//                } catch (e: MessagingException) {
//                    throw RuntimeException(e)
//                }
//            }.start()
//
//        }
//        catch (e:Exception){
//            e.printStackTrace()
//        }

//        CoroutineScope(Dispatchers.IO).launch {
//            SendEmailService.getInstance(requireContext()).sendEmail();
//
//        }
        }
    }
    fun createPDF1() {
        dialog = CustomDialogs.dialogStyleProgress(CustomDialogStyle1DataModel(requireActivity(),"",getString(R.string.please_wait),"","",this))
        CoroutineScope(Dispatchers.Main).launch {
            //Create document file
            val filename = "Safey PFT Report"

            try {
                val format = SimpleDateFormat(Constants.DATE_FORMAT_PATTERN)
                val dateFormat = SimpleDateFormat("ddMMyyyy_HHmm")
                var f1 = File(activity?.getExternalFilesDir(null), "Safey")
                if (!f1.exists()) {
                    f1.mkdirs()
                }
                f1.delete()
                if (!f1.exists()) {
                    f1.mkdirs()
                }
                fileShare = File(f1, filename + ".pdf")
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
                table.setPaddings(16f, 0f, 16f, 16f)
                val fileImage = File(f1, "ehealthlogo.png")
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
                patientDetails["Name"] = SafeyApplication.firstTestResult?.fname.toString()
                patientDetails["Height"] =
                    SafeyApplication.firstTestResult?.height.toString() + " " + enumHeightUnit.fromInt(
                        viewModel.patientData.HeightUnit
                    ).getFormatString()
                patientDetails["Birth Date"] =
                    Utility.getFormattedDate(SafeyApplication.firstTestResult?.dob?.let {
                        longDate(
                            it.toLong())
                    })!!
                patientDetails["Age"] =
                    "" + Utility.getAge(SafeyApplication.firstTestResult?.dob!!.toLong()) + "yrs"
                patientDetails["Gender"] = posToGender(
                    /*SafeyApplication.firstTestResult?.gender!!.toInt()*/
                    1
                )
                /* patientDetails["Ethnicity"] =
                     positionToEthnicity(viewModel.patientData.ethnicity)*/
                patientDetails["UHID"] = SafeyApplication.firstTestResult?.userId.toString()

                p.add(PrintUtility.keyValuePairParagraphPdf(patientDetails))

                cell.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                // cell.setMarginRight(16f)
                cell.add(p)
                table.addCell(cell)
                document.add(table)


//            table = Table(2)
//            // table.setAutoLayout()
//            table.setBorder(Border.NO_BORDER)
//            //table.setWidth(300f)
//            //table.setPaddings(16f,0f,16f,16f)
//
//
//            cell = Cell()
//            cell.setBorder(Border.NO_BORDER)
//            p = Paragraph()
//
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#ffffff", 12f))
//            cell.setHorizontalAlignment(HorizontalAlignment.LEFT)
//            cell.add(p)
//
//
//            table.addCell(cell)
////            cell = Cell()
////             p = Paragraph()
////            p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
////            p.setPaddingRight(8f)
////            p.setWidth(430f)
////            cell.setWidth(430f)
//
//            cell = Cell()
//            p = Paragraph()
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#000000", 8f))
//            p.setHorizontalAlignment(HorizontalAlignment.LEFT)
//
//            cell.setBorder(Border.NO_BORDER)
//            cell.add(p)
//            table.addCell(cell)
//            document.add(table)


                table = Table(8)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference : GLI 2012", "#000000", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference : GLI", "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Session Quality", "#000000", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
//            val filesesson = when (SafeyApplication.firstTestResult!!.sessionScore) {
//                "A" -> "a.png"
//                "B" -> "b.png"
//                "C" -> "c.png"
//                "D" -> "d.png"
//                "E" -> "e.png"
//                "F" -> "f.png"
//                else -> "na.png"
//            }
//            var varianceImage = File(f1, filesesson)
//            var imagevarianceUri = varianceImage.absolutePath
//            var imgvariance = Image(ImageDataFactory.create(imagevarianceUri))
//            imgvariance.setHorizontalAlignment(HorizontalAlignment.CENTER)
//            imgvariance.setTextAlignment(TextAlignment.CENTER)

                p = Paragraph(
                    PrintUtility.pdfTextA4(
                        SafeyApplication.firstTestResult!!.sessionScore!!,
                        true,
                        "#00D16C",
                        8.0f
                    )
                )

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                cell.setVerticalAlignment(VerticalAlignment.MIDDLE)
                table.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference", "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Suggested Diagnosis   - ", "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("   Obstructive dialgonstics" , "#ffffff", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("", "#ffffff", 8f))
                p.setBackgroundColor(WebColors.getRGBColor("#ffffff"), 8f, 8f, 8f, 8f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                cell.setBackgroundColor(WebColors.getRGBColor("#ffffff"))
                    .setBorderBottomLeftRadius(BorderRadius(8f))
                    .setBorderTopRightRadius(BorderRadius(8f))
                    .setBorderTopLeftRadius(BorderRadius(8f))
                    .setBorderBottomRightRadius(BorderRadius(8f))
                table.addCell(cell)

                document.add(table)



                table = Table(14)
                table.setMarginTop(8f)
                table.setPaddingTop(8f)

                table.setWidth(530f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Flow/Volume", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)

                cell.add(p)
                table.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Trial", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Ref", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("1", "#000000", 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                var starImage = File(f1, "rpt_star.png")
                var imageStarUri = starImage.absolutePath
                var imgstar = Image(ImageDataFactory.create(imageStarUri))
                imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                imgstar.setTextAlignment(TextAlignment.CENTER)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 0) {
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)

                var color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 1 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("2", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 1) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size ==2 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("3", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 2) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 3 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = if (SafeyApplication.postTestResult != null)
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)
                else
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                table.setBorderTopLeftRadius(BorderRadius(8f))
                table.setBorderTopRightRadius(BorderRadius(8f))
                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)

                table = Table(2)
                table.setWidth(530f)
                table.setPaddingBottom(16f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))
                    .setPaddingBottom(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                var stream = ByteArrayOutputStream()

                var bitmap: Bitmap? = chartReport.chartBitmap
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                var myImg = Image(ImageDataFactory.create(stream.toByteArray()))
                myImg.setWidth(480f)
                myImg.setHeight(160f)
                myImg.setHorizontalAlignment(HorizontalAlignment.LEFT)
                myImg.setTextAlignment(TextAlignment.LEFT)
                table.addCell(
                    Cell().setBorder(Border.NO_BORDER)
                        .setBackgroundColor(WebColors.getRGBColor("#F9FAFF")).add(myImg)
                        .setPaddingBottom(12f)
                )


                // table.setBorderTopLeftRadius(BorderRadius(8f))
                // table.setBorderTopRightRadius(BorderRadius(8f))
                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)


                table = Table(14)
                table.setWidth(530f)
                table.setMarginTop(6f)
                table.setPaddingTop(16f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Volume/Time", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Trial", "#000000", 12f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Ref", "#F9FAFF", 8f))
                    .setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("1", "#000000", 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 0) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)

                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 1 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                    .setPaddingTop(12f).setPaddingRight(6f)

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("2", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 1) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 2 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2) "#000000" else "#F9FAFF"
                p = Paragraph(PrintUtility.pdfText("3", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                p.add(PrintUtility.pdfText("3", "#F9FAFF", 6f))
                if (bestTestResult == 2) {
                    starImage = File(f1, "rpt_star.png")
                    imageStarUri = starImage.absolutePath
                    imgstar = Image(ImageDataFactory.create(imageStarUri))
                    imgstar.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    imgstar.setTextAlignment(TextAlignment.CENTER)
                    p.add(imgstar)
                }
                cell.add(p)
                table.addCell(cell)

                color =
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size == 3 && SafeyApplication.postTestResult!=null) "#000000" else "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Post", color, 12f))
                p.setPaddingTop(12f).setPaddingRight(6f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = if (SafeyApplication.postTestResult != null)
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)
                else
                    Paragraph(PrintUtility.pdfText("Post", "#F9FAFF", 8f)).setPaddingTop(12f)

                p.setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Reference", "#F9FAFF", 8f))
                p.setPaddingTop(12f)
                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                table.addCell(cell)

                table.setBorderTopLeftRadius(BorderRadius(8f))
                table.setBorderTopRightRadius(BorderRadius(8f))
                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)

                table = Table(2)
                table.setWidth(530f)
                table.setPaddingBottom(16f)
                table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setBorder(Border.NO_BORDER)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                // cell.setWidth((PageSize.A4.width -32 )/2)
                p = Paragraph(PrintUtility.pdfText("Refer", "#F9FAFF", 8f))

                p.setTextAlignment(TextAlignment.LEFT)
                p.setPaddingBottom(16f)
                cell.add(p)
                table.addCell(cell)

                stream = ByteArrayOutputStream()

                bitmap = chartReportvolume.chartBitmap
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, stream)
                myImg = Image(ImageDataFactory.create(stream.toByteArray()))
                myImg.setWidth(480f)
                myImg.setHeight(160f)
                myImg.setHorizontalAlignment(HorizontalAlignment.LEFT)
                myImg.setTextAlignment(TextAlignment.LEFT)
                table.addCell(
                    Cell().setBorder(Border.NO_BORDER)
                        .setBackgroundColor(WebColors.getRGBColor("#F9FAFF")).add(myImg)
                        .setPaddingBottom(16f)
                )


                // table.setBorderTopLeftRadius(BorderRadius(8f))
                // table.setBorderTopRightRadius(BorderRadius(8f))

                table.setBorderBottomLeftRadius(BorderRadius(8f))
                table.setBorderBottomRightRadius(BorderRadius(8f))
                document.add(table)


                var tableMeasurment = Table(2)
                tableMeasurment.setWidth(530f)
                cell = Cell()
                cell.setBorder(Border.NO_BORDER)

                p = Paragraph(PrintUtility.pdfText("Pre- Bronchodilator", "#000C86", 10f))

                p.setTextAlignment(TextAlignment.LEFT)
                cell.add(p)
                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)

                p = Paragraph(PrintUtility.pdfText("Post- Bronchodilator", "#000C86", 10f))

                p.setTextAlignment(TextAlignment.RIGHT)
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)


                tableMeasurment.addCell(cell)

                document.add(tableMeasurment)

                var tablePrePost = Table(2)
                tablePrePost.setWidth(530f)
                tablePrePost.setBorder(Border.NO_BORDER)

                tableMeasurment = Table(9)
                tableMeasurment.setWidth(400f)
                tableMeasurment.setAutoLayout()

                var bgcolor = "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.setBorderTopLeftRadius(BorderRadius(8f))
                cell.setHeight(10f)
                cell.add(p)

                tableMeasurment.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Measurements", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setHeight(10f)
                cell.add(p)

                tableMeasurment.addCell(cell)


                if (bestTestResult == 0) {
                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    p = Paragraph().add(
                        PrintUtility.pdfText(
                            "BEST",
                            "#000C86",
                            7f,
                            ""
                        )
                    )
                        .setBackgroundColor(WebColors.getRGBColor("#FFC725"))
                        .setBorderBottomLeftRadius(BorderRadius(4f))
                        .setBorderBottomRightRadius(BorderRadius(4f))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    tableMeasurment.addCell(
                        Cell().setMarginLeft(2f).setTextAlignment(TextAlignment.CENTER)
                            .setBorder(Border.NO_BORDER)
                            .setBackgroundColor(WebColors.getRGBColor(bgcolor))
                            .setBorder(Border.NO_BORDER).add(p)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    )
                } else {
                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Trial 1", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.RIGHT)

                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                    tableMeasurment.addCell(cell)
                }

                if (bestTestResult == 1) {

                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Best", "#000000", 7f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    p = Paragraph().add(
                        PrintUtility.pdfText(
                            "BEST",
                            "#000C86",
                            7f,
                            ""
                        )
                    )
                        .setBackgroundColor(WebColors.getRGBColor("#FFC725"))
                        .setBorderBottomLeftRadius(BorderRadius(4f))
                        .setBorderBottomRightRadius(BorderRadius(4f))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    tableMeasurment.addCell(
                        Cell().setMarginLeft(2f).setTextAlignment(TextAlignment.CENTER)
                            .setBorder(Border.NO_BORDER)
                            .setBackgroundColor(WebColors.getRGBColor(bgcolor))
                            .setBorder(Border.NO_BORDER).add(p)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    )

                } else {
                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    cell.setHeight(10f)
                    p = Paragraph(PrintUtility.pdfText("Trial 3", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                    tableMeasurment.addCell(cell)
                }



                if (bestTestResult == 2) {
                    cell = Cell()
                    cell.setHeight(10f)
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Best", "#000000", 7f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    p = Paragraph().add(
                        PrintUtility.pdfText(
                            "BEST",
                            "#000C86",
                            7f,
                            ""
                        )
                    )
                        .setBackgroundColor(WebColors.getRGBColor("#FFC725"))
                        .setBorderBottomLeftRadius(BorderRadius(4f))
                        .setBorderBottomRightRadius(BorderRadius(4f))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    //p.add(PrintUtility.pdfText("Re", "#FFC725"))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setHorizontalAlignment(HorizontalAlignment.CENTER)
                    tableMeasurment.addCell(
                        Cell().setMarginLeft(2f).setTextAlignment(TextAlignment.CENTER)
                            .setBorder(Border.NO_BORDER)
                            .setBackgroundColor(WebColors.getRGBColor(bgcolor))
                            .setBorder(Border.NO_BORDER).add(p)
                            .setTextAlignment(TextAlignment.CENTER)
                            .setHorizontalAlignment(HorizontalAlignment.RIGHT)
                    )
                } else {
                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    cell.setHeight(10f)
                    p = Paragraph(PrintUtility.pdfText("Trial 3", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.RIGHT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                    tableMeasurment.addCell(cell)
                }

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("LNN", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setHeight(10f)
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Z-score  ", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.RIGHT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.RIGHT)


                cell.setBorderTopRightRadius(BorderRadius(8f))
                tableMeasurment.addCell(cell)


//            document.add(tableMeasurment)
//
//            tableMeasurment = Table(9)
//            tableMeasurment.setWidth(400f)
//            tableMeasurment.setAutoLayout()
//            table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
//            table.setBorder(Border.NO_BORDER)
                bgcolor = "#F9FAFF"

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))




                cell.add(p)

                tableMeasurment.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Measurements", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.add(p)

                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Trial 1", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)

                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1)
                    Paragraph(PrintUtility.pdfText("Trial 2", "#000000", 8f, 0.5f))
                else
                    Paragraph(PrintUtility.pdfText("Trial 2", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2)
                    Paragraph(PrintUtility.pdfText("Trial 3", "#000000", 8f, 0.5f))
                else
                    Paragraph(PrintUtility.pdfText("Trial 3", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("LNN", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Z-score  ", "#50000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)


                tableMeasurment.addCell(cell)


                //var result1 = SafeyApplication.firstTestResult!!.trialResult!!.filter { it.isBest }[0]



                var list = arrayOf(
                    getString(R.string.fvc),
                    getString(R.string.fev1),
                    getString(R.string.fev1fvc),
                    getString(R.string.pef)
                )

                val listMeasure: MutableList<Report_Measurements> = ArrayList()
                if (SafeyApplication.firstTestResult!!.type == 1) {
                    list = arrayOf(
                        getString(R.string.fev1),

                        getString(R.string.pef)
                    )
                    for (i in 0..SafeyApplication.firstTestResult!!.trialResult!!.size - 1) {
                        var result =
                            SafeyApplication.firstTestResult!!.trialResult!![i].mesurementlist
                        var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()

                        var fev1 =
                            result!!.filter { it.measurement == getString(R.string.fev1) }[0]
                        testMeasurementsList.add(fev1)

                        var pefval =
                            result.filter { it.measurement == getString(R.string.pef) }[0]
                        testMeasurementsList.add(pefval)
                        listMeasure.add(Report_Measurements(testMeasurementsList))
                    }

                }
                else {
                    list = arrayOf(
                        getString(R.string.fvc),
                        getString(R.string.fev1),
                        getString(R.string.fev1fvc),
                        getString(R.string.pef)
                    )
                    for (i in 0..SafeyApplication.firstTestResult!!.trialResult!!.size - 1) {
                        var result =
                            SafeyApplication.firstTestResult!!.trialResult!![i].mesurementlist
                        var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()
                        var fvcval =
                            result!!.filter { it.measurement == getString(R.string.fvc) }[0]
                        testMeasurementsList.add(fvcval)
                        var fev1 =
                            result.filter { it.measurement == getString(R.string.fev1) }[0]
                        testMeasurementsList.add(fev1)
                        var fev1fvcval =
                            result.filter { it.measurement == getString(R.string.fev1fvc) }[0]
                        testMeasurementsList.add(fev1fvcval)
                        var pefval =
                            result.filter { it.measurement == getString(R.string.pef) }[0]
                        testMeasurementsList.add(pefval)
                        listMeasure.add(Report_Measurements(testMeasurementsList))
                    }
                }

                for (i in 0 until list.size) {
                    bgcolor = "#FFFFFF"

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
                    p.setTextAlignment(TextAlignment.LEFT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    if (i == 0) {
                        cell.setBorderTopLeftRadius(BorderRadius(8f))
                    }

                    cell.add(p)

                    tableMeasurment.addCell(cell)


                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText(list[i], "#50000000", 8f))
                    p.setTextAlignment(TextAlignment.LEFT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                    cell.add(p)

                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    var result = SafeyApplication.firstTestResult!!.trialResult!![0].mesurementlist
                    var testMeasurementsMain: TestMeasurements =
                        result!!.filter { it.measurement == list[i] }[0]
                    p = Paragraph(
                        PrintUtility.pdfText(
                            "" + String.format(
                                "%.2f",
                                testMeasurementsMain.measuredValue
                            ) + " ${testMeasurementsMain.unit}", "#50000000", 8f
                        )
                    )
                    p.setTextAlignment(TextAlignment.CENTER)

                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 1) {

                        val result =
                            SafeyApplication.firstTestResult!!.trialResult!![1].mesurementlist
                        val testMeasurements = result!!.filter { it.measurement == list[i] }[0]
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurements.measuredValue
                                ) + " ${testMeasurements.unit}", "#000000", 8f
                            )
                        )
                    } else {

                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurementsMain.measuredValue
                                ) + " ${testMeasurementsMain.unit}", "#ffffff", 8f
                            )
                        )
                    }

                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    if (SafeyApplication.firstTestResult!!.trialResult!!.size > 2) {
                        val result =
                            SafeyApplication.firstTestResult!!.trialResult!![2].mesurementlist
                        val testMeasurements = result!!.filter { it.measurement == list[i] }[0]
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurements.measuredValue
                                ) + " ${testMeasurements.unit}", "#000000", 8f
                            )
                        )
                    } else {

                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + String.format(
                                    "%.2f",
                                    testMeasurementsMain.measuredValue
                                ) + " ${testMeasurementsMain.unit}", "#ffffff", 8f
                            )
                        )
                    }

                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.predictedValue!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsMain.predictedValue!!,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.predictedPer),
                                "#00D16C",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsMain.predictedPer,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.lln != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.lln!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsMain.lln,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsMain.zScore != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsMain.zScore!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + (testMeasurementsMain.zScore),
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)
                    if (i == 0) {
                        cell.setBorderTopRightRadius(BorderRadius(8f))
                    }



                    tableMeasurment.addCell(cell)
                }

                tableMeasurment.setBorderTopLeftRadius(BorderRadius(8f))
                tableMeasurment.setBorderTopRightRadius(BorderRadius(8f))
                //document.add(tableMeasurment)


                tablePrePost.addCell(
                    Cell().setWidth(400f).add(tableMeasurment).setBorder(Border.NO_BORDER)
                )


                tableMeasurment = Table(2)
                tableMeasurment.setWidth(120f)
                tableMeasurment.setAutoLayout()
                    .setBorder(Border.NO_BORDER)
                    .setPaddingLeft(20f)
                    .setMarginLeft(10f)

                tableMeasurment.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                tableMeasurment.setBorder(Border.NO_BORDER)
                bgcolor = "#F9FAFF"
                cell = Cell()

                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Best", "#F9FAFF", 7f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.setBorderTopLeftRadius(BorderRadius(8f))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                tableMeasurment.addCell(cell)


                cell = Cell()

                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref %", "#F9FAFF", 7f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))

                cell.setBorderTopRightRadius(BorderRadius(8f))
                    .setHorizontalAlignment(HorizontalAlignment.CENTER)
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                tableMeasurment.addCell(cell)


                val listMeasurePost: MutableList<Report_Measurements> = ArrayList()
                if (SafeyApplication.postTestResult != null)
                    for (i in 0..SafeyApplication.postTestResult!!.trialResult!!.size - 1) {
                        var results =
                            SafeyApplication.postTestResult!!.trialResult!![i].mesurementlist
                        var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()
                        var fvcval =
                            results!!.filter { it.measurement == getString(R.string.fvc) }[0]
                        testMeasurementsList.add(fvcval)
                        var fev1 =
                            results.filter { it.measurement == getString(R.string.fev1) }[0]
                        testMeasurementsList.add(fev1)
                        var fev1fvcval =
                            results.filter { it.measurement == getString(R.string.fev1fvc) }[0]
                        testMeasurementsList.add(fev1fvcval)
                        var pefval =
                            results.filter { it.measurement == getString(R.string.pef) }[0]
                        testMeasurementsList.add(pefval)
                        listMeasurePost.add(Report_Measurements(testMeasurementsList))
                    }


                for (i in 0..4) {
                    var text1 = ""
                    var text2 = ""
                    var textcolor = ""
                    if (i == 0) {
                        bgcolor = "#F9FAFF"
                        text1 = "Best"
                        text2 = "Ref %"
                        textcolor = "#000000"
                    } else {
                        if (SafeyApplication.postTestResult == null) {
                            bgcolor = "#FFFFFF"
                            text1 = "-"
                            text2 = "-"
                            textcolor = "#FFFFFF"
                        } else {

                            val result =
                                SafeyApplication.postTestResult!!.trialResult!![0].mesurementlist
                            val testMeasurements =
                                result!!.filter { it.measurement == list[i - 1] }[0]
                            val measuredValue = String.format(
                                "%.2f",
                                testMeasurements.measuredValue
                            ) + " ${testMeasurements.unit}"

                            val perc = if (testMeasurements.predictedValue != " -  ")
                                "" + Utility.rounded(testMeasurements.predictedPer)
                            else
                                "-"
                            bgcolor = "#FFFFFF"
                            text1 = measuredValue
                            text2 = "" + perc
                            textcolor = "#FFFFFF"
                        }
                    }
                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (i == 0)
                        Paragraph(PrintUtility.pdfText(text1, "#000000", 8f, 0.5f))
                    else
                        Paragraph(PrintUtility.pdfText(text1, "#000000", 8f))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    tableMeasurment.addCell(cell)


                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (i == 0)
                        Paragraph(PrintUtility.pdfText(text2, "#000000", 8f, 0.5f))
                    else
                        Paragraph(PrintUtility.pdfText(text2, "#000000", 8f))
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                        .setHorizontalAlignment(HorizontalAlignment.CENTER)
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    tableMeasurment.addCell(cell)
                }
                tableMeasurment.setBorderTopLeftRadius(BorderRadius(8f))
                tableMeasurment.setBorderTopRightRadius(BorderRadius(8f))
                tablePrePost.addCell(
                    Cell().setWidth(130f).setMarginLeft(20f).add(tableMeasurment)
                        .setHorizontalAlignment(HorizontalAlignment.LEFT).setBorder(
                            Border.NO_BORDER
                        )
                )


                document.add(tablePrePost)

                p = Paragraph(PrintUtility.pdfText("Re", "#1F4E78"))
                p.setFixedPosition(0f, 0f, PageSize.A4.width)
                p.setBackgroundColor(WebColors.getRGBColor("#1F4E78"))

                p.add(PrintUtility.pdfText("Prepared by: eHealthSystem Technologies", "#ffffff"))



                p.add(Tab())
                p.addTabStops(TabStop(1000F, TabAlignment.RIGHT))
                p.add(
                    PrintUtility.pdfText(
                        "Test Date- " + Utility.getFormattedDateTime(Date()),
                        "#ffffff"
                    )
                )
                p.add(PrintUtility.pdfText("Re", "#1F4E78"))
                document.add(p)


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
                patientDetails["Name"] = viewModel.patientData.FirstName
                patientDetails["Height"] =
                    viewModel.patientData.Height + " " + enumHeightUnit.fromInt(
                        viewModel.patientData.HeightUnit
                    ).getFormatString()
                patientDetails["Birth Date"] =
                    Utility.getFormattedDate(longDate(viewModel.patientData.BirthDate))!!
                patientDetails["Age"] =
                    "" + Utility.getAge(viewModel.patientData.BirthDate) + "yrs"
                patientDetails["Gender"] = posToGender(viewModel.patientData.Gender)
                patientDetails["Ethnicity"] =
                    positionToEthnicity(viewModel.patientData.ethnicity)

                p.add(PrintUtility.keyValuePairParagraphPdf(patientDetails))

                cell.setHorizontalAlignment(HorizontalAlignment.RIGHT)
                // cell.setMarginRight(16f)
                cell.add(p)
                table.addCell(cell)
                document.add(table)

//            table = Table(2)
//            // table.setAutoLayout()
//            table.setBorder(Border.NO_BORDER)
//            //table.setWidth(300f)
//            //table.setPaddings(16f,0f,16f,16f)
//
//
//            cell = Cell()
//            cell.setBorder(Border.NO_BORDER)
//            p = Paragraph()
//
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#ffffff", 12f))
//            cell.setHorizontalAlignment(HorizontalAlignment.LEFT)
//            cell.add(p)
//
//
//            table.addCell(cell)
////            cell = Cell()
////             p = Paragraph()
////            p.setHorizontalAlignment(HorizontalAlignment.RIGHT)
////            p.setPaddingRight(8f)
////            p.setWidth(430f)
////            cell.setWidth(430f)
//
//            cell = Cell()
//            p = Paragraph()
//            p.add(PrintUtility.pdfText("Technician : Dr. Raj Mehta ", "#000000", 8f))
//            p.setHorizontalAlignment(HorizontalAlignment.LEFT)
//
//            cell.setBorder(Border.NO_BORDER)
//            cell.add(p)
//            table.addCell(cell)
//            document.add(table)

                p = Paragraph(PrintUtility.pdfText("Re", "#1F4E78"))
                p.setFixedPosition(0f, 0f, PageSize.A4.width)
                p.setBackgroundColor(WebColors.getRGBColor("#1F4E78"))

                p.add(PrintUtility.pdfText("Prepared by: eHealthSystem Technologies", "#ffffff"))



                p.add(Tab())
                p.addTabStops(TabStop(1000F, TabAlignment.RIGHT))
                p.add(
                    PrintUtility.pdfText(
                        "Test Date- " + Utility.getFormattedDateTime(Date()),
                        "#ffffff"
                    )
                )
                p.add(PrintUtility.pdfText("Re", "#1F4E78"))
                document.add(p)


                table = Table(1)
                table.setWidth(540f)
                // table.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                table.setMarginTop(16f)
                table.setBorder(Border.NO_BORDER)
                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                var best = bestTestResult + 1
                p = Paragraph(PrintUtility.pdfText("Me", "#F9FAFF", 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor("#FFFFFF"))
                p.add(
                    PrintUtility.pdfText(
                        "   Other Measurements from Best Trial - trial $best",
                        "#000C86",
                        14f
                    )
                )
                cell.add(p).setBackgroundColor(WebColors.getRGBColor("#F9FAFF"))
                cell.setBorderTopRightRadius(BorderRadius(8f))
                cell.setBorderTopLeftRadius(BorderRadius(8f))

                table.addCell(cell)
                document.add(table)
                tableMeasurment = Table(6)
                tableMeasurment.setWidth(540f)
                tableMeasurment.setAutoLayout()
                tableMeasurment.setBackgroundColor(WebColors.getRGBColor("#F9FAFF"), 8f, 8f, 8f, 8f)
                tableMeasurment.setBorder(Border.NO_BORDER)
                bgcolor = "#F9FAFF"

//            cell = Cell()
//            cell.setBorder(Border.NO_BORDER)
//            p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
//            p.setTextAlignment(TextAlignment.LEFT)
//            p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//            cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//
//
//
//
//            cell.add(p)
//
//            tableMeasurment.addCell(cell)


                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Me", "#FFFFFF", 8f))
                p.setTextAlignment(TextAlignment.LEFT)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                p.add(PrintUtility.pdfText("Measurements", "#000000", 8f, 0.5f))
                cell.add(p)

                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Measured Value", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)

                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                tableMeasurment.addCell(cell)



                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Reference Value", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Ref Value", bgcolor, 8f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("LNN", "#000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                tableMeasurment.addCell(cell)

                cell = Cell()
                cell.setBorder(Border.NO_BORDER)
                p = Paragraph(PrintUtility.pdfText("Z-score  ", "#50000000", 8f, 0.5f))
                p.setTextAlignment(TextAlignment.CENTER)
                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                cell.setBorderTopRightRadius(BorderRadius(8f))

                tableMeasurment.addCell(cell)

                var result =if(SafeyApplication.firstTestResult!!.trialResult!!.size==1)
                    SafeyApplication.firstTestResult!!.trialResult!!.get(0)
                else
                    SafeyApplication.firstTestResult!!.trialResult!!.filter { it.isBest }[0]

                var testMeasurementsList: MutableList<TestMeasurements> = ArrayList()

                testMeasurementsList.addAll(result.mesurementlist!!)


                for (i in 0 until testMeasurementsList.size) {
                    bgcolor = "#FFFFFF"

//                cell = Cell()
//                cell.setBorder(Border.NO_BORDER)
//                p = Paragraph(PrintUtility.pdfText("M", bgcolor, 8f))
//                p.setTextAlignment(TextAlignment.LEFT)
//                p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//                cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
//                if(i==0) {
//                    cell.setBorderTopLeftRadius(BorderRadius(8f))
//                }
//
//                cell.add(p)
//
//                tableMeasurment.addCell(cell)


                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(PrintUtility.pdfText("Me", "#FFFFFF", 8f))
                    p.setTextAlignment(TextAlignment.LEFT)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    p.add(
                        PrintUtility.pdfText(
                            testMeasurementsList[i].measurement!!,
                            "#50000000",
                            8f
                        )
                    )
                    cell.add(p)

                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = Paragraph(
                        PrintUtility.pdfText(
                            "" + String.format(
                                "%.2f",
                                testMeasurementsList[i].measuredValue
                            ) + " ${testMeasurementsList[i].unit}", "#50000000", 8f
                        )
                    )
                    p.setTextAlignment(TextAlignment.CENTER)

                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)

                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))


                    tableMeasurment.addCell(cell)



                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsList[i].predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].predictedValue!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsList[i].predictedValue,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsList[i].predictedValue != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].predictedPer),
                                "#00D16C",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsList[i].predictedPer,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    p = if (testMeasurementsList[i].lln != " -  ")
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].lln!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        Paragraph(
                            PrintUtility.pdfText(
                                "" + testMeasurementsList[i].lln,
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)



                    tableMeasurment.addCell(cell)

                    cell = Cell()
                    cell.setBorder(Border.NO_BORDER)
                    if (testMeasurementsList[i].zScore != " -  ")
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + Utility.rounded(testMeasurementsList[i].zScore!!.toDouble()),
                                "#50000000",
                                8f
                            )
                        )
                    else
                        p = Paragraph(
                            PrintUtility.pdfText(
                                "" + (testMeasurementsList[i].zScore),
                                "#50000000",
                                8f
                            )
                        )
                    p.setTextAlignment(TextAlignment.CENTER)
                    p.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.setBackgroundColor(WebColors.getRGBColor(bgcolor))
                    cell.add(p).setHorizontalAlignment(HorizontalAlignment.CENTER)
                    if (i == 0) {
                        cell.setBorderTopRightRadius(BorderRadius(8f))
                    }



                    tableMeasurment.addCell(cell)
                }

                tableMeasurment.setBorderTopLeftRadius(BorderRadius(8f))
                tableMeasurment.setBorderTopRightRadius(BorderRadius(8f))
                document.add(tableMeasurment)

                p = Paragraph(PrintUtility.pdfText("Re", "#1F4E78"))
                p.setFixedPosition(0f, 0f, PageSize.A4.width)
                p.setBackgroundColor(WebColors.getRGBColor("#1F4E78"))

                p.add(PrintUtility.pdfText("Prepared by: eHealthSystem Technologies", "#ffffff"))
                p.add(Tab())
                p.addTabStops(TabStop(1000F, TabAlignment.RIGHT))
                p.add(
                    PrintUtility.pdfText(
                        "Test Date- " + Utility.getFormattedDateTime(Date()),
                        "#ffffff"
                    )
                )
                p.add(PrintUtility.pdfText("Re", "#1F4E78"))
                document.add(p)



                document.close()
                loadPdf(fileShare)


                if (trialCount != 0) {
                    safey_finish_addpost_button.visibility = View.GONE
                    txtAddTrial.visibility = View.GONE
                    safeyDeviceKit?.finishTest()
                    viewModel.completeTest(1)
                    trialCount = 0
                }
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().applicationContext.packageName + ".provider",
                    fileShare
                );
                dialog?.dismiss()
                val share = Intent()
                share.action = Intent.ACTION_SEND
                share.type = "application/pdf"
                share.putExtra(Intent.EXTRA_STREAM, uri)
                requireActivity().startActivity(share)

                // prepareFilePart("RedcliffeReport.pdf",fileShare.toUri())

                // viewModel.upload(prepareFilePart("RedcliffeReport.pdf",fileShare.toUri()))

               /* convertToBase64(fileShare)*/

                /*  val file = FileDataPart.from("path_to_your_file", name = "image")
                  val (_, _, result) = Fuel.upload("http://10.0.2.2:3000/test")
                      .add(file)
                      .responseObject<CustomResponse>()*/


            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }

//            val username = "pravinmca09@gmail.com"
//            val password = "Holzoo@03"
//
//
//
//            Thread {
//                try {
//                    val props = Properties()
//                    props["mail.smtp.auth"] = "true"
//                    props["mail.smtp.starttls.enable"] = "true"
//                    props["mail.smtp.host"] = "smtp.gmail.com"
//                    props["mail.smtp.port"] = "587"
//                    val session: javax.mail.Session = javax.mail.Session.getInstance(props,
//                        object : Authenticator() {
//                            override fun getPasswordAuthentication(): PasswordAuthentication {
//                                //return super.getPasswordAuthentication()
//                                return  PasswordAuthentication(
//                                    username, password
//                                )
//                            }
//                        })
//                    // TODO Auto-generated method stub
//                    val message: Message = MimeMessage(session)
//                    message.setFrom(InternetAddress(username))
//                    message.setRecipients(
//                        Message.RecipientType.TO,
//                        InternetAddress.parse("pravinbadgujar90@gmail.com")
//                    )
//                    message.setSubject("email")
//                    message.setText(
//                        """HI,
//
// great"""
//                    )
//                    Transport.send(message)
//                    println("Done")
//                } catch (e: MessagingException) {
//                    throw RuntimeException(e)
//                }
//            }.start()
//
//        }
//        catch (e:Exception){
//            e.printStackTrace()
//        }

//        CoroutineScope(Dispatchers.IO).launch {
//            SendEmailService.getInstance(requireContext()).sendEmail();
//
//        }
        }
    }
    fun convertToBase64(attachment: File,uhid:String) {

        val encoder: Base64.Encoder = Base64.getEncoder()
        val encoded: String = encoder.encodeToString(attachment.readBytes() )
      /* Log.wtf("E",encoded)*/
        val current = LocalDateTime.now()

        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatted = current.format(formatter)

        var mainJson: JSONObject = JSONObject();
        mainJson.put("fileName",encoded)
        mainJson.put("userId",uhid)
        mainJson.put("docName","Spiro")
        mainJson.put("docType","pdf")
        mainJson.put("createdDt",formatted.toString())
        val progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please Wait")
        progressDialog.setMessage("Loading ...")
        progressDialog.show()
        Fuel.post("http://api-demo.ehealthsystem.com/nirmalyaRest/api/post-test-report-spiro").jsonBody(
            mainJson.toString()
        ) .also { println(it) }
            .response {
                    request, response, result ->
                val (bytes, error) = result
                if (bytes != null) {
                    progressDialog.dismiss()
                    progressDialog.cancel()
                    println(">>>>>>>>>>>>>>>>[response bytes] ${String(bytes)}")
                 /*   Toast.makeText(context,"Data Saved Successfully",Toast.LENGTH_LONG).show()*/
                    Constants.isPost = false
                    Constants.isDelete = true
                   /* findNavController().popBackStack()*/
                 /*   val intent = Intent(context, Dashboard::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)*/
                   val intent = Intent(this@TestResultsFragment.requireContext(),Dashboard::class.java)
                    startActivity(intent)

                  /*  (requireActivity() as Dashboard).naviGate(0)*/

                }
            }



    }


    private fun loadPdf(file: File) {
        pdfView.fromFile(file)
            .scrollHandle(DefaultScrollHandle(requireActivity()))
            .pageFitPolicy(FitPolicy.BOTH)
            .nightMode(false)
            .load()
        //  pdfView.visibility = View.VISIBLE
    }
    fun findSuggestedDiagnosis() : String{

       val age =  Utility.getAge(viewModel.patientData.BirthDate)

      val airTestResult: TrialResult? =   if (SafeyApplication.postTestResult!=null)
          SafeyApplication.postTestResult!!.trialResult?.get(0)
        else
            SafeyApplication.firstTestResult!!.trialResult!!.filter { it.isBest }[0]

        val fvc =  airTestResult!!.mesurementlist!!.filter { it.measurement== getString(R.string.fvc) }[0].measuredValue
        val fvcperc =  airTestResult!!.mesurementlist!!.filter { it.measurement== getString(R.string.fvc) }[0].predictedPer
        val fev1fvc = airTestResult.mesurementlist!!.filter { it.measurement== getString(R.string.fev1fvc) }[0].measuredValue
        val fev1fvcperc = airTestResult.mesurementlist!!.filter { it.measurement== getString(R.string.fev1fvc) }[0].predictedPer
        val fev1fvclln = airTestResult.mesurementlist!!.filter { it.measurement== getString(R.string.fev1fvc) }[0].lln
        val fvclln = airTestResult.mesurementlist!!.filter { it.measurement== getString(R.string.fvc) }[0].lln

        if (age<=18){
           if (fvcperc>=80 && fev1fvcperc>=85){
               return "Normal"
           }
            else if (fvcperc>=80 && fev1fvcperc<85){
                return "Obstructive defect"
            }
           else if (fvcperc<80 && fev1fvcperc>=85){
               return "Restrictive pattern"
           }
           else if (fvcperc<80 && fev1fvcperc<85){
               return "Mixed pattern"
           }
        }
        else
        {
            if (fvclln != " -  " && fev1fvclln != " -  ") {
                if (fvc>=fvclln!!.toDouble() && (fev1fvc>=fev1fvclln!!.toDouble() || fev1fvc>=70 )) {
                    return "Normal"
                }
                else if (fvc>=fvclln.toDouble() && (fev1fvc<fev1fvclln!!.toDouble() || fev1fvc<70 )) {
                    return "Obstructive defect"
                }
                else if (fvc<fvclln.toDouble() && (fev1fvc>=fev1fvclln!!.toDouble() || fev1fvc>=70 )) {
                    return "Restrictive pattern"
                }
                else if (fvc<fvclln.toDouble() && (fev1fvc<fev1fvclln!!.toDouble() || fev1fvc<70 )) {
                    return "Mixed pattern"
                }
            }
        }

        return ""
    }


}


