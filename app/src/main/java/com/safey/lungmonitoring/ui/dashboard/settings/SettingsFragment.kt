package com.safey.lungmonitoring.ui.dashboard.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.pref.UserSession
import com.safey.lungmonitoring.ui.dashboard.medication.adapter.FreuencyAdapter
import com.safey.lungmonitoring.ui.dashboard.medication.MedFrequency
import kotlinx.android.synthetic.main.fragment_settings.*
import kotlinx.android.synthetic.main.safey_next_cancel_button.view.*


class SettingsFragment : Fragment(R.layout.fragment_settings),
    FreuencyAdapter.MedFrequencyClickListener {

    private lateinit var adapter: FreuencyAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rangeTypeList = ArrayList<MedFrequency>()
        rangeTypeList.add(MedFrequency(UserSession.SpirometerDataSet.Nhances.ordinal,getString(R.string.nhances)))
        rangeTypeList.add(MedFrequency(UserSession.SpirometerDataSet.GLI.ordinal,getString(R.string.gli)))

        val selectedPos = if (UserSession(requireContext()).dataSet ==UserSession.SpirometerDataSet.GLI.ordinal)
            1
        else
            0

        recylclerviewSpiroDataSet.layoutManager = GridLayoutManager(context, 2)
        adapter = FreuencyAdapter(rangeTypeList,selectedPos,this)
        recylclerviewSpiroDataSet.adapter = adapter
        layout_confirmation.button_Next.text = getString(R.string.save)
        layout_confirmation.visibility = View.GONE

        layout_confirmation.button_cancel.setOnClickListener{
            requireActivity().onBackPressed()
        }
        layout_confirmation.button_Next.setOnClickListener{

            if (adapter.getSelected().id==UserSession.SpirometerDataSet.GLI.ordinal)
            UserSession(requireContext()).setDataSet(
                UserSession.SpirometerDataSet.GLI)
            else
                UserSession(requireContext()).setDataSet(
                    UserSession.SpirometerDataSet.Nhances)
            requireActivity().onBackPressed()
        }
    }

    override fun onClick(medFrequency: MedFrequency) {
        layout_confirmation.visibility = View.VISIBLE
    }


}