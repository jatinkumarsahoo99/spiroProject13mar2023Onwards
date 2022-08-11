package com.safey.lungmonitoring.ui.test


import android.os.Bundle

import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View
import androidx.databinding.DataBindingUtil
import androidx.navigation.fragment.findNavController

import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.databinding.AddPostDialogBinding
import com.safey.lungmonitoring.utils.Constants
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddPostSheetDialog : BottomSheetDialogFragment() {
    private lateinit var binding: AddPostDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =DataBindingUtil.inflate(inflater,R.layout.add_post_dialog, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.safeyNextCancelButton.buttonNext.text = getString(R.string.starttest)


        binding.safeyNextCancelButton.buttonNext.setOnClickListener{
            if(binding.edMedication.text.isNotEmpty()) {
                Constants.isPost = true
                dismiss()
                findNavController().popBackStack()
            }
            else
                Constants.showToast("Enter medication name",requireContext())
        }
        binding.safeyNextCancelButton.buttonCancel.setOnClickListener{
            dismiss()
        }
    }


}