package com.safey.lungmonitoring.ui.dashboard.home


import android.content.Context
import android.os.Bundle

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.databinding.RowFvctypeBinding
import com.safey.lungmonitoring.ui.dashboard.home.viewmodel.DashboardViewModel
import com.safey.lungmonitoring.ui.dashboard.reports.adapter.MeasurementAdapter
import com.safey.lungmonitoring.ui.dashboard.trends.TrendsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DashboardFVCBottomSheetDialog : BottomSheetDialogFragment() {


    private lateinit var adapter: MeasurementAdapter
    private lateinit var binding: RowFvctypeBinding
    val dashboardViewModel : DashboardViewModel by activityViewModels()
    val trendsViewModel : TrendsViewModel by activityViewModels()

    var list = ArrayList<String>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =DataBindingUtil.inflate(inflater,R.layout.row_fvctype, container, false)

        return binding.root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.safeyNextCancelButton.buttonNext.text = getString(R.string.select)
        binding.recyclerViewMeasurements.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL,false)


//        list.clear()
//        list.add(enumMeasurementsDashboard.FVC.name)
//        list.add(enumMeasurementsDashboard.FEV1.name)
//        list.add(enumMeasurementsDashboard.PEF.name)
//
//        adapter = MeasurementAdapter(requireContext(),list,UserSession(requireContext()).measurementType  - 1)
//
//        binding.recyclerViewMeasurements.adapter = adapter
//
//        binding.safeyNextCancelButton.buttonNext.setOnClickListener{
//            UserSession(requireContext()).measurementType = list.indexOf(adapter.getSelected()) + 1
//            dashboardViewModel.setMeasurementType(UserSession(requireContext()).measurementType)
//            trendsViewModel.setMeasurementType(UserSession(requireContext()).measurementType)
//            dismiss()
//            findNavController().popBackStack()
//        }
        binding.safeyNextCancelButton.buttonCancel.setOnClickListener{
            dismiss()
        }
    }




}