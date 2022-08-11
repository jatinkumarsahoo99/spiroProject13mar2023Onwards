package com.safey.lungmonitoring.ui.test


import android.os.Bundle

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.databinding.RowConfirmationBinding
import com.safey.lungmonitoring.utils.Utility
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DeleteBottomSheetDialog : BottomSheetDialogFragment() {
    private lateinit var binding: RowConfirmationBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =DataBindingUtil.inflate(inflater,R.layout.row_confirmation, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.safeyNextCancelButton.buttonNext.text = getString(R.string.btn_txt_confirm)
        binding.txtMsg.text = getString(R.string.delete_this_trial)

        binding.safeyNextCancelButton.buttonNext.setOnClickListener{
            TestResultsFragment.safeyDeviceKit?.deleteTrial()
            Utility.showToast(requireContext(), getString(R.string.trial_deleted))
            dismiss()
            findNavController().popBackStack()
        }
        binding.safeyNextCancelButton.buttonCancel.setOnClickListener{
            dismiss()
        }
    }


}