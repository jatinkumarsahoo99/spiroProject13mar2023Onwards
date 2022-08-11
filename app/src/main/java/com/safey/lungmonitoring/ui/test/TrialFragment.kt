package com.safey.lungmonitoring.ui.test

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.safey.lungmonitoring.R
import info.safey.safey_sdk.LiquidFillView
import kotlinx.android.synthetic.main.fragment_trial.*
import kotlinx.android.synthetic.main.liquidview_pretest.*
import kotlinx.android.synthetic.main.liquidview_pretest.view.*


class TrialFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_trial, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFillChart()
        liquidview_pretest.liquidFillView.setOnClickListener{
            ensure_a_ti.visibility = View.INVISIBLE
            timerGroup.visibility = View.VISIBLE
            liquidFillView.progress = 200
            liquidFillView.startAnimation()

        }

    }

    private fun setupFillChart() {
        liquidFillView.startAnimation()
        liquidFillView.setFrontWaveColor(Color.parseColor("#29B6F6"))
        liquidFillView.setBehindWaveColor(Color.parseColor("#76DDF7"))
        liquidFillView.setBorderColor(Color.parseColor("#29B6F6"))
        liquidFillView.setTextColor(LiquidFillView.DEFAULT_TEXT_COLOR)
        liquidFillView.startAnimation()
        liquidFillView.progress = 0


    }

}