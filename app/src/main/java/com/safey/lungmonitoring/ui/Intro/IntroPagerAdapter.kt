package com.safey.lungmonitoring.ui.Intro

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.safey.lungmonitoring.interfaces.PrivacyRadioCheckListner

class IntroPagerAdapter (fa: FragmentActivity,var privacyRadioCheckListner: PrivacyRadioCheckListner) : FragmentStateAdapter(fa) {
    override fun getItemCount(): Int = 4

    override fun createFragment(position: Int): Fragment = IntroFragment(position,privacyRadioCheckListner)

}