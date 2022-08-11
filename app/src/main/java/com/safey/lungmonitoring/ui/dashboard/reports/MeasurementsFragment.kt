package com.safey.lungmonitoring.ui.dashboard.reports

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.datautils.enumMeasurementsFEVC
import com.safey.lungmonitoring.data.datautils.enumMeasurementsFIVC
import com.safey.lungmonitoring.model.MeasurementModel
import com.safey.lungmonitoring.ui.dashboard.reports.adapter.MeasurementAdapter
import com.safey.lungmonitoring.utils.Utility
import kotlinx.android.synthetic.main.fragment_measurements.*
import kotlinx.android.synthetic.main.fragment_measurements.safey_next_cancel_button
import kotlinx.android.synthetic.main.row_test_type.view.*
import kotlinx.android.synthetic.main.safey_next_cancel_button.view.*

class MeasurementsFragment : Fragment(R.layout.fragment_measurements) {

    private lateinit var adapter: MeasurementAdapter
    var list = ArrayList<MeasurementModel>()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        var reportType = arguments?.getInt("reportType",0)
        var testType = arguments?.getInt("testType",0)
        var reportRangeType = arguments?.getInt("reportRangeType",0)

        if (testType == Utility.TestType.FIVC.ordinal){
            row_Test_Type.image_testtype.setImageResource(R.drawable.ic_fivc)
            row_Test_Type.txtTestType.text = getString(R.string.forced_inspiratory_nvital_capacity)
            list.clear()
            for (i in enumMeasurementsFIVC.values()){
                i.getFormatString()?.let { list.add(MeasurementModel(i.value,it)) }
            }
        }
        else
        {
            row_Test_Type.image_testtype.setImageResource(R.drawable.ic_fevc)
            row_Test_Type.txtTestType.text = getString(R.string.forced_expiratory_nvital_capacity)
            list.clear()
            for (i in enumMeasurementsFEVC.values()){
                i.getFormatString()?.let { list.add(MeasurementModel(i.value,it)) }
            }
//            if (SafeyApplication.appPrefs1?.isPeakflow==true){
//                list.add(MeasurementModel(31,getString(R.string.fev1)))
//                list.add(MeasurementModel(32,getString(R.string.pef)))
//            }
        }

//        list.add(enumMeasurementsDashboard.FVC.name)
//        list.add(enumMeasurementsDashboard.FEV1.name)
//        list.add(enumMeasurementsDashboard.PEF.name)
        recyclerViewMeasurements.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        adapter = MeasurementAdapter(requireContext(),list,0)
        recyclerViewMeasurements.adapter = adapter


        safey_next_cancel_button.button_cancel.setOnClickListener{
            findNavController().popBackStack()
        }
        safey_next_cancel_button.button_Next.text = getString(R.string.next)
        safey_next_cancel_button.button_Next.setOnClickListener{
            if (adapter.getSelected().isNotEmpty()) {
                val action =
                    MeasurementsFragmentDirections.actionMeasurementsFragmentToDownloadFragment(
                        reportType!!,
                        testType!!,
                        reportRangeType!!,
                        adapter.getSelected().joinToString( "," )
                    )
                findNavController().navigate(action)
            }
        }
    }

}