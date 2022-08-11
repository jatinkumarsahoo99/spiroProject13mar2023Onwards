package com.safey.lungmonitoring.ui.dashboard.medication

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.tables.patient.Medication
import com.safey.lungmonitoring.databinding.FragmentAddMedicationsBinding
import com.safey.lungmonitoring.interfaces.DialogStyle1Click
import com.safey.lungmonitoring.model.CustomDialogStyle1DataModel
import com.safey.lungmonitoring.ui.dashboard.Dashboard
import com.safey.lungmonitoring.ui.dashboard.medication.adapter.FreuencyAdapter
import com.safey.lungmonitoring.ui.dashboard.medication.adapter.MedColorAdapter
import com.safey.lungmonitoring.ui.dashboard.medication.adapter.MedIconAdapter
import com.safey.lungmonitoring.ui.dashboard.medication.viewmodel.MedicationViewModel
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.CustomDialogs
import com.safey.lungmonitoring.utils.Utility.getFrequency
import com.safey.lungmonitoring.utils.Utility.getMedColorData
import com.safey.lungmonitoring.utils.Utility.getMedIconsImageData
import com.safey.lungmonitoring.utils.Utility.getMedicationsData
import com.safey.safey_medication.model.MedColors
import com.safey.safey_medication.model.MedIcon
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.safey_toolbar.view.*


@AndroidEntryPoint
class AddMedicationFragment : Fragment(), MedIconAdapter.MedIconClickListner,
    MedColorAdapter.MedColorClickListner, FreuencyAdapter.MedFrequencyClickListener,
    DialogStyle1Click {

    private lateinit var binding: FragmentAddMedicationsBinding
    val viewmodels : MedicationViewModel by viewModels()
    private lateinit var medFreqAdapter: FreuencyAdapter
    private lateinit var medicineList: List<String>
    private lateinit var medColorAdapter: MedColorAdapter
    private lateinit var medIconAdapter: MedIconAdapter

    private var selectedDays: String = String()

    var checkedItems =
        booleanArrayOf(false, false, false, false, false, false, false)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_add_medications,container,false)// inflater.inflate(R.layout.fragment_add_medications, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        medicineList = getMedicationsData(requireContext(), "medications.json", "medications")!!
        val medicationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, medicineList)
        binding.txtMedication.setAdapter(medicationAdapter)

        binding.recyclerViewFrequency.layoutManager = GridLayoutManager(context, 2)
        medFreqAdapter =
            FreuencyAdapter(getFrequency(requireContext()) as ArrayList<MedFrequency>,0,this)

        binding.recyclerViewFrequency.adapter = medFreqAdapter

        medIconAdapter =
            MedIconAdapter(requireContext(),getMedIconsImageData(requireContext()) as ArrayList<MedIcon>,0,this)

        binding.recyclerViewMedicineType.adapter = medIconAdapter

        medColorAdapter=MedColorAdapter(requireContext(),
            getMedColorData(requireContext()) as ArrayList<MedColors>, 0,this)

        binding.recyclerViewMedicationColor.adapter = medColorAdapter

        binding.imageMedPlus.setOnClickListener{
            onIncrementClick()
        }
        binding.imageMedMinus.setOnClickListener{
            onDecrementClick()
        }


        binding.layoutNextCancelButton.buttonNext.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                viewmodels.medicationData.noOfTimes = binding.lblMedCount.text.toString().toInt()
                viewmodels.medicationData.freqType = medFreqAdapter.getSelected().id
                viewmodels.medicationData.days = selectedDays
                viewmodels.medicationData.medicationName = binding.txtMedication.text.toString()
                viewmodels.medicationData.medColor = medColorAdapter.getSelected()
                viewmodels.medicationData.medIcon = medIconAdapter.getSelected()
                if (!arguments?.getString("id")?.equals("NA")!!) {
                    viewmodels.updateMedication(viewmodels.medicationData)
                    CustomDialogs.dialogStyleSuccess(model = CustomDialogStyle1DataModel(requireActivity(),message =getString(
                        R.string.your_medication_updated_successfully),positiveButton = getString(
                        R.string.proceed_dashboard),negativeButton = getString(R.string.cancel),dialogStyle1Click = this@AddMedicationFragment))

                } else {
                    viewmodels.getMedicationCount(arguments?.getString("id")!!)
                    viewmodels.medicationCount.observe(viewLifecycleOwner, {
                        if (it>0){
                            binding.txtMedication.error = getString(R.string.medication_exist)
                        } else {
                            viewmodels.medicationData.medicationName = binding.txtMedication.text.toString()
                            viewmodels.insertMedication(viewmodels.medicationData)
                            CustomDialogs.dialogStyleSuccess(model = CustomDialogStyle1DataModel(requireActivity(),message =getString(
                                R.string.your_medication_saved_successfully),positiveButton = getString(
                                R.string.proceed_dashboard),negativeButton = getString(R.string.cancel),dialogStyle1Click = this@AddMedicationFragment))

                        }
                    })

                }
            }

        })
        binding.layoutNextCancelButton.buttonCancel.setOnClickListener{
            findNavController().popBackStack()
        }

        enabledSaveButton()
        binding.txtMedication.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                    enabledSaveButton()
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })
        binding.layoutNextCancelButton.buttonNext.text = getString(R.string.save)
        if (arguments?.getString("id")?.equals("NA") != true) {
            arguments?.getString("id")?.let {
                (requireActivity() as Dashboard).safey_appBar.txtHeader.text = getString(R.string.edit_medication)
                viewmodels.getMedicationId(it)
                viewmodels.medication.observe(viewLifecycleOwner) { medication ->
                    viewmodels.medicationData = medication
                    binding.medication = medication

                    binding.txtMedication.isEnabled = false
                    binding.txtMedication.alpha = Constants.alphaBlur
                    medFreqAdapter =
                        FreuencyAdapter(
                            getFrequency(requireContext()) as ArrayList<MedFrequency>,
                            medication.freqType,
                            this
                        )
                    medFreqAdapter.update(medication.freqType)
                    binding.recyclerViewFrequency.adapter = medFreqAdapter

                    selectedDays = medication.days.toString()
                    val selectDays = selectedDays.split(" ")
                    val days = resources.getStringArray(R.array.weekdays_short)
                    for (i in days.indices) {
                        checkedItems[i] = selectDays.contains(days[i])
                    }

                    val iconIndex = getMedIconsImageData(requireContext()).indexOfFirst {
                        it.medIcon == medication.medIcon
                    }

                    medIconAdapter =
                        MedIconAdapter(
                            requireContext(),
                            getMedIconsImageData(requireContext()) as ArrayList<MedIcon>,
                            iconIndex,
                            this
                        )

                    medIconAdapter.updateMedIcon(iconIndex, medication.medColor!!)
                    binding.recyclerViewMedicineType.adapter = medIconAdapter

                    val colorIndex = getMedColorData(requireContext()).indexOfFirst {
                        it.medColor == medication.medColor
                    }
                    medColorAdapter = MedColorAdapter(
                        requireContext(),
                        getMedColorData(requireContext()) as ArrayList<MedColors>, colorIndex, this
                    )

                    binding.recyclerViewMedicationColor.adapter = medColorAdapter

                }
            }
        }
        else {

            binding.layoutNextCancelButton.buttonCancel.visibility = View.GONE

            binding.medication = Medication(medicationName = "", noOfTimes = 1)
        }
    }


    private fun enabledSaveButton(): Boolean {
        return if (binding.txtMedication.text.toString().trim() == "" )
         {
            binding.layoutNextCancelButton.buttonNext.isEnabled = false
            binding.layoutNextCancelButton.buttonNext.background = ContextCompat.getDrawable(
                requireContext(),
                R.drawable.shapesdisable
            )
            true
        } else {
            binding.layoutNextCancelButton.buttonNext.isEnabled =  true
            binding.layoutNextCancelButton.buttonNext.background  = ContextCompat.getDrawable(requireContext(), R.drawable.btn_pink_round)
            false
        }
    }

    override fun onClick(medColor: MedColors) {
        viewmodels.medicationData.medColor= medColor.medColor
        medIconAdapter.updateMedIcon(medColor.medColor)
        enabledSaveButton()
    }

    override fun onClick(medIcon: MedIcon) {
        viewmodels.medicationData.medIcon=medIcon.medIcon
        enabledSaveButton()
    }

    override fun onClick(medFrequency: MedFrequency) {
        viewmodels.medicationData.freqType = medFrequency.id
        if (medFrequency.id==2){
            weekDaysPicker()
        }
    }
    private fun onIncrementClick() {
        var doses = binding.lblMedCount.text.toString().toInt()
        if (doses < 10)
            doses++
        binding.lblMedCount.text = doses.toString()
        enabledSaveButton()

    }

    private fun onDecrementClick() {
        var doses = binding.lblMedCount.text.toString().toInt()
        if (doses > 1)
            doses--
        binding.lblMedCount.text = doses.toString()
        enabledSaveButton()

    }

    private fun weekDaysPicker() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.days_of_week))
            .setCancelable(false)
            .setMultiChoiceItems(
                resources.getStringArray(R.array.weekdays),
                checkedItems
            ) { dialog, position, checked ->
                // Respond to item chosen
//                Log.e(TAG, "onCreate: $position")
                checkedItems[position] = checked

            }
            .setNeutralButton(resources.getString(R.string.ok)) { dialog, which ->
                // Respond to neutral button press
//                Log.e(TAG, "onCreate: ${checkedItems[1]}")

                selectedDays = ""
                for (i in checkedItems.indices) {
                    if (checkedItems[i])
                        selectedDays =
                            selectedDays + " " + resources.getStringArray(R.array.weekdays_short)[i]
                }
                if (selectedDays.trim().split(" ").toTypedArray().size > 6) {
                   medFreqAdapter.update(1)
                   selectedDays = ""
                } else if (checkedItems.filter { it }.isEmpty()) {
                   medFreqAdapter.update(1)
                   selectedDays = ""
                } //else
                    ///textViewDays.text = selectedDays

                viewmodels.medicationData.days = selectedDays
                enabledSaveButton()
            }.show()
    }

    override fun positiveButtonClick() {
        findNavController().popBackStack(R.id.nav_home,false)
    }

    override fun negativeButton() {}
}