package com.safey.lungmonitoring.ui.dashboard.reports

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.ui.devicesetup.TestTypeModel
import com.safey.lungmonitoring.ui.devicesetup.adapter.TestTypeAdapter
import com.safey.lungmonitoring.ui.devicesetup.adapter.TestTypeClickListener
import com.safey.lungmonitoring.utils.Utility
import kotlinx.android.synthetic.main.fragment_test_type.*

import kotlinx.android.synthetic.main.layout_test_type.recyclerViewTestType
import kotlinx.android.synthetic.main.safey_next_cancel_button.view.*


class TestTypeFragment : Fragment(R.layout.fragment_test_type), TestTypeClickListener {

    private var selectedpos: Int = -1
    private lateinit var adapter: TestTypeAdapter
    private var testTypeList: MutableList<TestTypeModel> = ArrayList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        testTypeList = ArrayList()
       var reportType = arguments?.getInt("reportType",0)
       var reportRangeType = arguments?.getInt("reportRangeType",0)

        val items: List<String> = (resources.getStringArray(R.array.test_type_array).toList())
        val myImageList = intArrayOf(R.drawable.ic_fevc, R.drawable.ic_fivc,R.drawable.ic_flow_volume_loop,R.drawable.ic_svc,R.drawable.ic_maximum_volume)

        for (i in items.indices) {
            testTypeList.add(TestTypeModel(i,items[i],myImageList[i],i==selectedpos))
        }
        recyclerViewTestType.layoutManager = GridLayoutManager(context, 2)
        adapter = TestTypeAdapter(requireContext(),testtypeModelList = testTypeList,this)
        recyclerViewTestType.adapter = adapter
        safey_next_cancel_button.button_Next.text= getString(R.string.next)
        safey_next_cancel_button.button_Next.setOnClickListener{

            if(adapter.getSelected().isNotEmpty()) {
                selectedpos = adapter.getSelected()[0].position
                val testType = if (adapter.getSelected()[0].position == 0)
                    Utility.TestType.FEVC.ordinal
                else
                    Utility.TestType.FIVC.ordinal
                findNavController().navigate(
                    TestTypeFragmentDirections.actionTestTypeFragmentToMeasurementsFragment(
                        reportType!!,
                         testType,
                        reportRangeType!!
                    )
                )
            }
        }

        safey_next_cancel_button.button_cancel.setOnClickListener{
            findNavController().popBackStack()
        }
    }

    override fun onClick(item: TestTypeModel) {

    }
}