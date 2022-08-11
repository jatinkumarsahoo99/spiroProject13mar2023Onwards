package com.safey.lungmonitoring.ui.Intro

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.interfaces.PrivacyRadioCheckListner
import kotlinx.android.synthetic.main.fragment_intro.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [IntroFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IntroFragment(position: Int, var privacyRadioCheckListner: PrivacyRadioCheckListner) : Fragment(),
    CompoundButton.OnCheckedChangeListener {

    var position = position



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_intro, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        label.text = ""
        when(position){
            0 -> {
                scrollView.visibility = View.VISIBLE
                checkBox2.setOnCheckedChangeListener(this)
                checkBox3.setOnCheckedChangeListener(this)
                checkBox4.setOnCheckedChangeListener(this)

            }
            else -> {
                scrollView.visibility = View.GONE
                when(position) {
                    1 -> {
                        image.setImageResource(R.drawable.ic_palmhand)
                        label.text = getString(R.string.measure_your_lung_in_palm_of_your_hands)
                    }
                    2 -> {
                        label.text = (getString(R.string.most_accurate_spirometer_device))
                        image.setImageResource(R.drawable.ic_accuracy)
                    }
                    3 -> {
                        image.setImageResource(R.drawable.ic_cleared)
                        label.text = (getString(R.string.fda_cleared))
                    }
                }
            }
        }
    }

    override fun onCheckedChanged(p0: CompoundButton?, p1: Boolean) {
        if (checkBox2.isChecked && checkBox3.isChecked ){
            privacyRadioCheckListner.allChecked(true)
        }
        else
            privacyRadioCheckListner.allChecked(false)

    }


}