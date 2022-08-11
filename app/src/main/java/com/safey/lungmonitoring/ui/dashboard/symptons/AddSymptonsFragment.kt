package com.safey.lungmonitoring.ui.dashboard.symptons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.tables.patient.Symptoms

import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_add_symptons.*
import kotlinx.android.synthetic.main.safey_next_cancel_button.view.*
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddSymptonsFragment() : Fragment(), SymptonClickListener {

    private lateinit var listSymptoms: ArrayList<SymptomModel>
    private lateinit var symptonAdapter: SymptonAdapter
    val viewmodels : SymptomViewModel by viewModels()
    var symptomId : String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_symptons, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        symptomId = arguments?.getString("symptomId","").toString()
        safey_next_cancel_button.button_Next.text = getString(R.string.save)
        listSymptoms = ArrayList()

        listSymptoms = Utility.getSymptonsData(requireContext()) as ArrayList<SymptomModel>

        recyclerViewSymptons.layoutManager = GridLayoutManager(context, 2)
        symptonAdapter= SymptonAdapter(requireContext(),listSymptoms,this)
        recyclerViewSymptons.adapter = symptonAdapter

        if (symptomId.isNotEmpty()) {
            viewmodels.getSymptonById(symptomId)

            viewmodels.symptom.observe(requireActivity()) { it ->
                viewmodels.symptoms = it
                for (i in listSymptoms) {
                    if (viewmodels.symptoms.symptomsNames.any { it.contains(i.sympton) })
                        i.checked = true
                }
                symptonAdapter = SymptonAdapter(requireContext(), listSymptoms, this)
                recyclerViewSymptons.adapter = symptonAdapter

            }
        }


        safey_next_cancel_button.button_Next.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                val list: List<SymptomModel> = symptonAdapter.getSelected()
                val listSymptonNames = ArrayList<String>()
                val listSymptonIcons = ArrayList<String>()


                for (sympton in list){
                    if (sympton.checked) {

                        listSymptonNames.add(sympton.sympton)
                        listSymptonIcons.add(sympton.imageResourse)
                    }
                }
                if (symptomId == "") {
                    if (listSymptonNames.size>0) {
                        val currentDate = Date()
                        val date = SimpleDateFormat(Constants.DATE_FORMAT_PATTERN).format(currentDate)
                        val time = SimpleDateFormat("hh:mm a").format(currentDate)
                        viewmodels.insertSymptons(
                            Symptoms(
                                listSymptonNames,
                                listSymptonIcons,
                                date,
                                time
                            )
                        )
                    }
                    else{
                        Utility.showDialog(requireContext(),"Select Symptoms","Please select atleast one symptoms")
                        return
                    }
                }
                else {
                    if(listSymptonNames.size>0) {
                        viewmodels.symptoms.symptomsNames = listSymptonNames
                        viewmodels.symptoms.symptomsIcons = listSymptonIcons
                        viewmodels.updateSymptons(viewmodels.symptoms)
                    }
                    else
                    {
                        viewmodels.deleteSymptom(viewmodels.symptoms)
                    }
                }
                findNavController().popBackStack(R.id.nav_home,false)
            }

        })
        safey_next_cancel_button.button_cancel.setOnClickListener{
            findNavController().navigateUp()
        }
    }

    override fun onClick(item: SymptomModel) {

    }

}