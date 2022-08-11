package com.safey.lungmonitoring.ui.dashboard.profile

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.text.method.DigitsKeyListener
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.safey.lungmonitoring.MobileNavigationDirections
import com.safey.lungmonitoring.ProfileNavigationDirections
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.custombinings.*
import com.safey.lungmonitoring.data.pref.UserSession
import com.safey.lungmonitoring.databinding.FragmentProfileBinding
import com.safey.lungmonitoring.interfaces.DialogStyle1Click
import com.safey.lungmonitoring.model.CustomDialogStyle1DataModel
import com.safey.lungmonitoring.ui.dashboard.Dashboard

import com.safey.lungmonitoring.ui.dashboard.profile.viewmodel.ProfileViewModel
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.CustomDialogs
import com.safey.lungmonitoring.utils.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_profile.*
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProfileFragment : Fragment(), TextWatcher {
    private lateinit var binding: FragmentProfileBinding
    val viewModel :ProfileViewModel by activityViewModels()
    private  var genderStatus: Int =0
    private var arraymonth = listOf(
        "Jan",
        "Feb",
        "Mar",
        "Apr",
        "May",
        "Jun",
        "Jul",
        "Aug",
        "Sep",
        "Oct",
        "Nov",
        "Dec"
    )
    private var year: Int = 0
    private var monthOfYear: Int = 0
    private var day: Int = 0
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)
        return binding.root
    }


    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.edDOB.setOnClickListener{
            val c = Calendar.getInstance()

            year = c.get(Calendar.YEAR)
            monthOfYear = c.get(Calendar.MONTH)
            day = c.get(Calendar.DAY_OF_MONTH)

            if (binding.edDOB.text!!.isNotEmpty()) {
                val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
                val date: Date? = formatter.parse(binding.edDOB.text.toString())
                date?.let {
                    c.time = date
                    year = c.get(Calendar.YEAR)
                    monthOfYear = c.get(Calendar.MONTH)
                    day = c.get(Calendar.DAY_OF_MONTH)
                }
            }


            val dpd = DatePickerDialog(
                requireContext(),
                { _, year, monthOfYear, dayOfMonth ->
                    this.year = year
                    this.monthOfYear = monthOfYear
                    this.day = dayOfMonth
                    binding.edDOB.setText( "$dayOfMonth ${arraymonth[monthOfYear]}, $year")
                    enableSaveButton()
                },
                year,
                monthOfYear,
                day)

            dpd.datePicker.maxDate = Utility.getDaysAgo(3).time
            dpd.datePicker.minDate = Utility.getDaysAgo(95).time

            //dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
            dpd.show()

            dpd.getButton(DatePickerDialog.BUTTON_POSITIVE)
                .setTextColor(Color.parseColor("#AF4EA6"))
            dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE)
                .setTextColor(Color.parseColor("#AF4EA6"))

        }


        binding.edGender.isFocusable = false
        binding.edGender.isClickable = true

        binding.edDOB.isFocusable = false
        binding.edDOB.isClickable = true

        binding.edEthnicity.isFocusable = false
        binding.edEthnicity.isClickable = true
        binding.edGender.setOnClickListener{
            val wrapper = ContextThemeWrapper(requireContext(),R.style.PopupMenu)
            val popup = PopupMenu(wrapper,it)
            popup.menuInflater.inflate(R.menu.popup_menu_gender, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                genderStatus = if(item.title.toString().lowercase() == "male") 1 else
                    2
                binding.edGender.setText(item.title.toString())
                binding.profileImage.visibility = View.GONE
                if(genderStatus==2 || genderStatus==0){
                    binding.profileImageBg.setImageResource(R.drawable.woman_1)
                    binding.profileImageBg.tag = "woman_1"


                }
                if(genderStatus==1 || genderStatus==0){
                    binding.profileImageBg.setImageResource(R.drawable.man_1)
                    binding.profileImageBg.tag = "man_1"
                }

                viewModel.patientData.avatar = binding.profileImageBg.tag.toString()
                enableSaveButton()
                true
            }
            popup.show()
        }

        binding.edEthnicity.setOnClickListener{
            val wrapper = ContextThemeWrapper(requireContext(),R.style.PopupMenu)
            val popup = PopupMenu(wrapper,it)
            popup.menuInflater.inflate(R.menu.popup_menu_ethnicity, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                binding.edEthnicity.setText(item.title.toString())
                enableSaveButton()
                true
            }
            popup.show()
        }

        binding.btnSave.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {

                viewModel.patientData.Height = binding.edHeight.text.toString()
                viewModel.patientData.LastName = binding.edLname.text.toString()
                viewModel.patientData.FirstName = binding.edFname.text.toString()
                viewModel.patientData.BirthDate = binding.edDOB.text.toString().toLong()
                viewModel.patientData.Gender = genderStatus
                viewModel.patientData.HeightUnit =  if (binding.radioCM.isChecked)  1 else 2
                viewModel.patientData.ethnicity = ethnicityToPosition(binding.edEthnicity.text.toString())
                if (!UserSession(requireContext()).isLoggedIn) {

                    viewModel.insertPatient(patient = viewModel.patientData)
                    UserSession(requireContext()).setUserData(viewModel.patientData)

                    CustomDialogs.dialogStyleSuccess(CustomDialogStyle1DataModel(requireActivity(),"Patient Data","Data Saved Successfully","Ok","",object : DialogStyle1Click{
                        override fun positiveButtonClick() {
                            val intent = Intent(requireContext(), Dashboard::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                        }

                        override fun negativeButton() {

                        }

                    }))


                } else {
                    viewModel.updatePatient(patient = viewModel.patientData)
                    UserSession(requireContext()).setUserData(viewModel.patientData)

                   //val action= MobileNavigationDirections.actionGlobalNavHome()
                    //findNavController().navigate(action)
                    CustomDialogs.dialogStyleSuccess(CustomDialogStyle1DataModel(requireActivity(),"Patient Data","Data updated successfully","Ok","",object : DialogStyle1Click{
                        override fun positiveButtonClick() {
                            findNavController().popBackStack()
                        }

                        override fun negativeButton() {

                        }

                    }))

                }
                SafeyApplication.appPrefs1?.isDefaultDataLoaded = true




            }
        })

        binding.profileImageBg.setOnClickListener {
            if (genderStatus == 0) {
                Utility.showDialog(
                    requireContext(),
                    getString(R.string.error),
                    getString(R.string.please_select_gender)
                )
            } else {

                viewModel.patientData.Height = binding.edHeight.text.toString()
                viewModel.patientData.LastName = binding.edLname.text.toString()
                viewModel.patientData.FirstName = binding.edFname.text.toString()
                viewModel.patientData.BirthDate = binding.edDOB.text.toString().toLong()
                viewModel.patientData.Gender = genderStatus
                viewModel.patientData.HeightUnit =  if (binding.radioCM.isChecked)  1 else 2
                viewModel.patientData.ethnicity = ethnicityToPosition(binding.edEthnicity.text.toString())
                if(UserSession(requireContext()).isLoggedIn){

                    val action = MobileNavigationDirections.actionGlobalAvatarDialogFragment4(genderStatus)
                    findNavController().navigate(action)
                }
                else {
                    val action =
                        ProfileNavigationDirections.actionGlobalAvatarDialogFragment3(genderStatus)
                    findNavController().navigate(action)
                }

            }
        }


        if (!Constants.fromAvatar)
            viewModel.getPatient()

        viewModel.patient.observe(requireActivity()) {

            if (it != null) {
                    genderStatus = it.Gender
                    binding.edGender.setText(posToGender(it.Gender))
                    binding.edDOB.setText(it.BirthDate.toString())
                    binding.edEthnicity.setText(positionToEthnicity(it.ethnicity))
                    binding.edFname.setText(it.FirstName)
                    binding.edLname.setText(it.LastName)
                    binding.edHeight.setText(""+it.Height)
                    binding.profileImageBg.setImageResource(Utility.resoureId(view.context,it.avatar))

                    if (it.HeightUnit == 1)
                        binding.radioCM.isChecked = true
                    if (it.HeightUnit == 2)
                        binding.radioFT.isChecked = true
                    binding.profileImage.visibility = View.GONE
                    viewModel.patientData = it
                    enableSaveButton()
                }

        }

        viewModel.avatar.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
           // binding.patient = viewModel.patientData
           // binding.patient!!.avatar= it
            viewModel.patientData.avatar = it
            binding.profileImageBg.tag = it
            binding.profileImageBg.setImageResource(Utility.resoureId(requireContext(),it))
            binding.profileImage.visibility = View.GONE

            enableSaveButton()
        })

        edFname.addTextChangedListener(this)
        edLname.addTextChangedListener(this)
        edDOB.addTextChangedListener(this)
        edGender.addTextChangedListener(this)
        edEthnicity.addTextChangedListener(this)
        edHeight.addTextChangedListener(this)
        edHeight.keyListener = DigitsKeyListener.getInstance("0123456789. ")
        val regex = Regex("[0-9.\\d ]")

        edHeight.filters = arrayOf(InputFilter { source, _, _, _, _, _ ->
            source.filter { regex.matches(it.toString()) }
        })
        enableSaveButton()
    }

    fun enableSaveButton(){
        if (binding.edFname.text.toString().trim() == "" ||
            binding.edLname.text.toString().trim() == "" ||
            binding.edGender.text.toString().trim() == "" ||
            binding.edDOB.text.toString().trim() == "" ||
            binding.edHeight.text.toString().trim() == "" ||
            binding.edEthnicity.text.toString().trim() == "") {

            binding.btnSave.isEnabled = false
            binding.btnSave.alpha = 0.4f
        }
        else
        {
            binding.btnSave.isEnabled = true
            binding.btnSave.alpha = 0.9f
        }
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        enableSaveButton()
    }

    override fun afterTextChanged(p0: Editable?) {}
}