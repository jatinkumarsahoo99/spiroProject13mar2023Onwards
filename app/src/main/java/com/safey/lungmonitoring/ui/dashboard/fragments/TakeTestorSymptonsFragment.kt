package com.safey.lungmonitoring.ui.dashboard.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.utils.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_take_test.*

@AndroidEntryPoint
class TakeTestorSymptonsFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_test, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        layoutTakeTest.setOnClickListener{
            SafeyApplication.firstTestResult = null
            SafeyApplication.postTestResult = null
            Constants.isPost = false
            Constants.postTrialCount =0

            val action = TakeTestorSymptonsFragmentDirections.actionTakeTestorSymptonsFragmentToDeviceSetupFragment()
            findNavController().navigate(action)
        }


        /*layout_Sympton.setOnClickListener{
            val action = TakeTestorSymptonsFragmentDirections.actionTakeTestorSymptonsFragmentToAddSymptonsFragment("")
            findNavController().navigate(action)
        }*/
        imageViewCross.setOnClickListener{
            findNavController().navigateUp()
        }
    }


}