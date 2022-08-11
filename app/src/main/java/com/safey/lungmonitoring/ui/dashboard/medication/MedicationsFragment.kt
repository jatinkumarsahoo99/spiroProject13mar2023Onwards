package com.safey.lungmonitoring.ui.dashboard.medication

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.safey.lungmonitoring.MobileNavigationDirections
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.interfaces.DialogStyle1Click
import com.safey.lungmonitoring.model.CustomDialogStyle1DataModel
import com.safey.lungmonitoring.ui.dashboard.medication.adapter.MedicationListAdapter
import com.safey.lungmonitoring.ui.dashboard.medication.viewmodel.MedicationViewModel
import com.safey.lungmonitoring.utils.CustomDialogs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_medications.*
import kotlinx.android.synthetic.main.layout_test_type.*
@AndroidEntryPoint
class MedicationsFragment : Fragment(R.layout.fragment_medications),
    MedicationListAdapter.MedicationClickListener, DialogStyle1Click {
    private lateinit var deleteMedId: String
    private lateinit var adapter: MedicationListAdapter
    val medicationViewModel : MedicationViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        medicationViewModel.getAllMedication()

        medicationViewModel.allMedication.observe(viewLifecycleOwner, Observer {
            adapter = MedicationListAdapter(requireContext(),medicationList = it,this)
            recyclerViewMedList.adapter = adapter
        })

        recyclerViewMedList.layoutManager = GridLayoutManager(context, 2)

        floatingActionButton.setOnClickListener{
            val action = MobileNavigationDirections.actionGlobalAddMedicationFragment("NA")
            findNavController().navigate(action)
        }
    }

    override fun onClick(id: String) {
        val action = MobileNavigationDirections.actionGlobalAddMedicationFragment(id)

        findNavController().navigate(action)
    }

    override fun onDeleteClick(id: String) {
        deleteMedId = id
        CustomDialogs.dialogStyleDelete(model = CustomDialogStyle1DataModel(requireActivity(),message = getString(R.string.are_you_sure_you_want_to_delete_your_medications),positiveButton = getString(R.string.btn_txt_confirm),negativeButton = getString(R.string.cancel),dialogStyle1Click = this))
    }

    override fun positiveButtonClick() {
        medicationViewModel.deleteMed(deleteMedId)
    }

    override fun negativeButton() {}
}
