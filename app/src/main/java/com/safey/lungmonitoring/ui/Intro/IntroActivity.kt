package com.safey.lungmonitoring.ui.Intro

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.material.tabs.TabLayoutMediator
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.interfaces.PrivacyRadioCheckListner
import com.safey.lungmonitoring.ui.profile.ProfileActivity
import com.safey.lungmonitoring.utils.Constants
import kotlinx.android.synthetic.main.activity_intro.*

class IntroActivity : AppCompatActivity(), PrivacyRadioCheckListner {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        // Instantiate a ViewPager2 and a PagerAdapter.


        // The pager adapter, which provides the pages to the view pager widget.
        val pagerAdapter = IntroPagerAdapter(this,this)
        viewPager.adapter = pagerAdapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            Log.e( "onCreate: " ,""+position)
        }.attach()

        buttonContinue.alpha = Constants.alphaBlur
        buttonContinue.isEnabled = false



        buttonContinue.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if (viewPager.currentItem<3)
                    viewPager.currentItem = viewPager.currentItem + 1
                else
                    startActivity(Intent(this@IntroActivity,ProfileActivity::class.java))

            }

        })
        buttonSkip.setOnClickListener{
            startActivity(Intent(this,ProfileActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (viewPager.currentItem == 0) {
            // If the user is currently looking at the first step, allow the system to handle the
            // Back button. This calls finish() on this activity and pops the back stack.
            super.onBackPressed()
        } else {
            // Otherwise, select the previous step.
            viewPager.currentItem = viewPager.currentItem - 1
        }
    }

    override fun allChecked(isChecked:Boolean) {

        if (isChecked) {
            buttonContinue.isEnabled = true
            buttonContinue.alpha = Constants.alphaClear
        }
        else{
            buttonContinue.alpha = Constants.alphaBlur
            buttonContinue.isEnabled = false
        }
    }
}