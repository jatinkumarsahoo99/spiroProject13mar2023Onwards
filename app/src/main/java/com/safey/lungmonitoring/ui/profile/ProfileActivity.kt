package com.safey.lungmonitoring.ui.profile

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.databinding.ActivityProfileBinding
import com.safey.lungmonitoring.ui.dashboard.profile.viewmodel.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.safey_toolbar.view.*
import java.util.*

@AndroidEntryPoint
class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    val viewModel :ProfileViewModel by viewModels()
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
    private lateinit var navController: NavController
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

         //binding = DataBindingUtil.setContentView(this,R.layout.activity_profile)

       /* binding.safeyAppBar.txtHeader.text = getString(R.string.profile)
        binding.safeyAppBar.iconLeft.setImageResource(R.drawable.ic_back)
*/
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_profile_fragment) as NavHostFragment
        navController = navHostFragment.navController

        setSupportActionBar(safey_appBar as Toolbar)
      /*  safey_appBar.iconRight.visibility = View.INVISIBLE*/
       /* binding.safeyAppBar.iconRight.visibility = View.INVISIBLE
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
                this,
                { _, year, monthOfYear, dayOfMonth ->
                    this.year = year
                    this.monthOfYear = monthOfYear
                    this.day = dayOfMonth
                    binding.edDOB.setText( "$dayOfMonth ${arraymonth[monthOfYear]}, $year")
                },
                year,
                monthOfYear,
                day)

            dpd.datePicker.maxDate = getDaysAgo(3).time
            dpd.datePicker.minDate = getDaysAgo(95).time

            //dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this,R.color.colorAccent))
            dpd.show()

            dpd.getButton(DatePickerDialog.BUTTON_POSITIVE as Int)
                .setTextColor(Color.parseColor("#AF4EA6"))
            dpd.getButton(DatePickerDialog.BUTTON_NEGATIVE as Int)
                .setTextColor(Color.parseColor("#AF4EA6"))

        }


        binding.edGender.isFocusable = false
        binding.edGender.isClickable = true

        binding.edDOB.isFocusable = false
        binding.edDOB.isClickable = true

        binding.edEthnicity.isFocusable = false
        binding.edEthnicity.isClickable = true
        binding.edGender.setOnClickListener{
            //val popup = PopupMenu(this, it)
            val wrapper = ContextThemeWrapper(this,R.style.PopupMenu)
            val popup = PopupMenu(wrapper,it)
            popup.menuInflater.inflate(R.menu.popup_menu_gender, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                 genderStatus = if(item.title.toString().lowercase() == "male") 1 else
                     2
                binding.edGender.setText(item.title.toString())
                binding.profileImage.visibility = View.GONE
                if(genderStatus==2 || genderStatus==0){
                    binding.profileImageBg.setImageResource(R.drawable.woman_1)
                    binding.profileImageBg.tag = "man_1"

                }
                if(genderStatus==1 || genderStatus==0){
                    binding.profileImageBg.setImageResource(R.drawable.man_1)
                    binding.profileImageBg.tag = "woman_1"
                }
                true
            }
            popup.show()
        }

        binding.edEthnicity.setOnClickListener{
            val wrapper = ContextThemeWrapper(this,R.style.PopupMenu)
            val popup = PopupMenu(wrapper,it)
            popup.menuInflater.inflate(R.menu.popup_menu_ethnicity, popup.menu)
            popup.setOnMenuItemClickListener { item ->
                binding.edEthnicity.setText(item.title.toString())
                true
            }
            popup.show()
        }

        binding.btnSave.setOnClickListener{

           *//*val profile = Patient(FirstName = binding.edFname.text.toString(), binding.edLname.text.toString(),
               genderStatus, binding.edHeight.text.toString(),ethnicity = 1)
           profile.BirthDate = DateConverter().dateToTimestamp(Date(binding.edDOB.text.toString()))!!
*//*
           viewModel.updatePatient(patient = binding.patient!!)
           startActivity(Intent(this, Dashboard::class.java))
        }

        binding.profileImageBg.setOnClickListener{
            if(genderStatus==0){
                Utility.showDialog(this,getString(R.string.error),getString(R.string.please_select_gender))
            }
            else {
                viewModel.patientData =binding.patient?.copy()!!

                val action = MobileNavigationDirections.actionGlobalAvatarDialogFragment(genderStatus)
                navController.navigate(action)
               *//* val avatarDialogFragment: AvatarDialogFragment =
                    AvatarDialogFragment.newInstance("Avatar", genderStatus)
                avatarDialogFragment.setAvatarData(this)
                supportFragmentManager.beginTransaction()
                    .add(android.R.id.content, avatarDialogFragment).addToBackStack(null).commit()*//*
            }
        }

        viewModel.getPatient()

        viewModel.patient.observe(this, {
            if(it!=null)
                binding.patient = it

            binding.profileImageBg.tag = it.avatar

            if(it.avatarResourceId>0)
                binding.profileImage.setImageResource(it.avatarResourceId)
        })*/
    }


   /* override fun getAvatar(avatar: Avatar?) {
        binding.profileImageBg.tag = avatar!!.imageURL
        //profilecolor = avatar.avatarColor.toString()
        binding.profileImageBg.setImageResource(avatar.resourceId)
        //enabledSaveButton()
    }*/
}