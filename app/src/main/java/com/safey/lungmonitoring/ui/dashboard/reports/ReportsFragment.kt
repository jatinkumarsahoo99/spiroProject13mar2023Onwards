package com.safey.lungmonitoring.ui.dashboard.reports

import android.os.Bundle
import android.view.ContextThemeWrapper
import android.view.View
import android.widget.PopupMenu
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.ui.dashboard.medication.MedFrequency
import com.safey.lungmonitoring.ui.dashboard.medication.adapter.FreuencyAdapter
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.Utility
import kotlinx.android.synthetic.main.fragment_reports.*
import kotlinx.android.synthetic.main.safey_next_cancel_button.view.*
import java.text.SimpleDateFormat
import java.util.*


class ReportsFragment : Fragment(R.layout.fragment_reports),
    FreuencyAdapter.MedFrequencyClickListener {

    private lateinit var startdate: Date
    private lateinit var currentDate: Date
    private var reportRange: Int = 0
    private var selection: Pair<Long, Long>? = null
    private lateinit var adapter: FreuencyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rangeTypeList = ArrayList<MedFrequency>()
        rangeTypeList.add(MedFrequency(Utility.ReportType.PDF.ordinal,getString(R.string.pdf)))
        rangeTypeList.add(MedFrequency(Utility.ReportType.CSV.ordinal,getString(R.string.csv)))
        recyclerViewType.layoutManager = GridLayoutManager(context, 2)

        adapter = FreuencyAdapter(rangeTypeList,0,this)
        recyclerViewType.adapter = adapter

//        edDate.setOnClickListener{
//
//            val constraintsBuilder =
//                CalendarConstraints.Builder()
//                    .setValidator(DateValidatorPointBackward.now())
//
//
//
//            val dateRangePicker =
//                MaterialDatePicker.Builder.dateRangePicker()
//                    .setTitleText("Report period")
//                    .setTheme(R.style.MaterialCalendarTheme)
//                    .setSelection(selection)
//                    .setCalendarConstraints(constraintsBuilder.build())
//                if (selection!=null)
//                dateRangePicker.setSelection(selection)
//
//           var dateRangePickerBuilder =  dateRangePicker.build()
//               dateRangePickerBuilder.show(parentFragmentManager,"Date")
//
//
//            dateRangePickerBuilder.addOnPositiveButtonClickListener {
//                val startDate: Long = it.first
//                val endDate: Long = it.second
//                selection = it
//                val simpleFormat = SimpleDateFormat("dd MMM ")
//                val simpleFormat1 = SimpleDateFormat("dd MMM yyyy")
//
//
//                edDate.setText(simpleFormat.format(startDate).toString() +" - "+simpleFormat1.format(endDate))
//            }
//        }


        currentDate = Calendar.getInstance().time
        edDate.isFocusable = false
        edDate.isClickable = true

        rangeType.isFocusable = false
        rangeType.isClickable = true

        safey_next_cancel_button.button_Next.text = getString(R.string.next)
        safey_next_cancel_button.button_cancel.setOnClickListener{
            findNavController().popBackStack()
        }

        safey_next_cancel_button.button_Next.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if (SafeyApplication.appPrefs1?.isPeakflow == true || SafeyApplication.appPrefs1?.isSpiroMeter==true)
                if (SafeyApplication.appPrefs1?.isSpiroMeter == true) {
                    val action = ReportsFragmentDirections.actionReportsFragmentToTestTypeFragment(
                        adapter.getSelected().id,
                        reportRange
                    )
                    findNavController().navigate(action)
                }
                else
                {
                    val action = ReportsFragmentDirections.actionReportsFragmentToDownloadFragment(adapter.getSelected().id,
                           1,  reportRange, getString(R.string.fev1)+","+getString(R.string.pef)
                    )
                    findNavController().navigate(action)
                }
                else
                {
                    Utility.showDialog(requireContext(),"Report data error","No test results recorded to generate reports.")
                    safey_next_cancel_button.button_Next.isEnabled = false
                    safey_next_cancel_button.button_Next.alpha = Constants.alphaBlur
                }
            }
        })

        //enabledSaveButton()

        rangeType.setOnClickListener{
            val wrapper = ContextThemeWrapper(requireContext(),R.style.PopupMenu)
            val popup = PopupMenu(wrapper,it)
            popup.menuInflater.inflate(R.menu.popup_menu_range, popup.menu)
            popup.setOnMenuItemClickListener { item ->
               reportRange =  when(item.itemId){
                    R.id.week -> Utility.ReportRangeType.WEEK.ordinal
                  R.id.last_week -> Utility.ReportRangeType.LASTWEEK.ordinal
                  R.id.this_month -> Utility.ReportRangeType.THISMONTH.ordinal
                  R.id.last_month -> Utility.ReportRangeType.LASTMONTH.ordinal
                  R.id.three_month -> Utility.ReportRangeType.LAST3MONTH.ordinal
                  R.id.six_months -> Utility.ReportRangeType.LAST6MONTH.ordinal
                  else -> 0
              }
                currentDate = Calendar.getInstance().time
                when (reportRange) {
                    Utility.ReportRangeType.WEEK.ordinal -> {
                        val sdate = Utility.getCurrentWeek()
                        startdate = SimpleDateFormat("dd/MM/yyyy").parse(sdate)
                    }
                    Utility.ReportRangeType.LASTWEEK.ordinal -> {
                        val arrayDates=  Utility.getpreviousweek()
                        startdate = SimpleDateFormat("dd/MM/yyyy").parse(arrayDates[0])
                        currentDate = SimpleDateFormat("dd/MM/yyyy").parse(arrayDates[1])
                    }
                    Utility.ReportRangeType.THISMONTH.ordinal -> {
                        val c = Calendar.getInstance() // this takes current date

                        c[Calendar.DAY_OF_MONTH] = 1
                        startdate = c.time

                    }
                    Utility.ReportRangeType.LASTMONTH.ordinal -> {
                        val aCalendar = Calendar.getInstance()

                        aCalendar.add(Calendar.MONTH, -1)

                        aCalendar[Calendar.DATE] = 1

                        startdate = aCalendar.time

                        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)

                        currentDate = aCalendar.time

                    }
                    Utility.ReportRangeType.LAST3MONTH.ordinal -> {
                        val date = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        calendar.add(Calendar.MONTH, -3)
                        calendar[Calendar.DAY_OF_MONTH] = 1
                        startdate = calendar.time

                        val aCalendar = Calendar.getInstance()
                        aCalendar.add(Calendar.MONTH, -1)
                        aCalendar[Calendar.DATE] = 1
                        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        currentDate = aCalendar.time
                    }
                    Utility.ReportRangeType.LAST6MONTH.ordinal -> {
                        val date = Date()
                        val calendar = Calendar.getInstance()
                        calendar.time = date
                        calendar.add(Calendar.MONTH, -6)
                        calendar[Calendar.DAY_OF_MONTH] = 1
                        startdate = calendar.time

                        val aCalendar = Calendar.getInstance()
                        aCalendar.add(Calendar.MONTH, -1)
                        aCalendar[Calendar.DATE] = 1
                        aCalendar[Calendar.DATE] = aCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        currentDate = aCalendar.time
                    }
                }

                Constants.reportStartDate = startdate
                Constants.reportEndDate = currentDate
                edDate.setText(Utility.getFormattedDay(startdate)+" - "+Utility.getFormattedDate(currentDate))

                rangeType.setText(item.title.toString())
                enabledSaveButton()
                true
            }
            popup.show()
        }

    }

    override fun onResume() {
        super.onResume()
        enabledSaveButton()
    }

    private fun enabledSaveButton() {

            if (rangeType.text.toString().trim() == getString(R.string.select) )
            {
                safey_next_cancel_button.button_Next.isEnabled = false
                safey_next_cancel_button.button_Next.alpha = Constants.alphaBlur

            } else {
                safey_next_cancel_button.button_Next.isEnabled =  true
                safey_next_cancel_button.button_Next.alpha  = Constants.alphaClear

            }

    }

    override fun onClick(medFrequency: MedFrequency) {

    }
}