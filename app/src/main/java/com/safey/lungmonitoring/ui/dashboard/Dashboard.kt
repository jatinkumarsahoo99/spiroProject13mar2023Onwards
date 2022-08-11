package com.safey.lungmonitoring.ui.dashboard


import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.safey.lungmonitoring.MobileNavigationDirections

import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.databinding.ActivityDashboardBinding
import com.safey.lungmonitoring.data.pref.UserSession
import com.safey.lungmonitoring.interfaces.FragmentClickViewModel
import com.safey.lungmonitoring.interfaces.FragmentListener
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.Constants.FRAGMENT_CODE_HOME
import com.safey.lungmonitoring.utils.Constants.START_OVER
import com.safey.lungmonitoring.utils.Constants.currentFragment
import com.safey.lungmonitoring.utils.Constants.fromAvatar
import com.safey.lungmonitoring.utils.Utility
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.hamburger_menu.*
import kotlinx.android.synthetic.main.safey_toolbar.view.*


@AndroidEntryPoint
class Dashboard : AppCompatActivity(),FragmentListener {


    private lateinit var navController: NavController
    private lateinit var fragmentClickViewModel: FragmentClickViewModel
    private lateinit var activity: Dashboard

    private lateinit var binding: ActivityDashboardBinding
    private lateinit var toolbar : Toolbar
    private lateinit var appBarConfiguration: AppBarConfiguration
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        bind()
        init()

        val actionBarDrawerToggle: ActionBarDrawerToggle =
            object : ActionBarDrawerToggle(this, binding.drawerLayout, toolbar, R.string.open, R.string.close) {
                private val scaleFactor = 5f
                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                    super.onDrawerSlide(drawerView, slideOffset)
                    val slideX = drawerView.width * slideOffset
                    binding.navHostFragment.translationX = slideX
                    binding.drawerLayout.bringChildToFront(drawerView);
                    binding.drawerLayout.requestLayout();
                    //below line used to remove shadow of drawer
                    binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
                }
            }

        actionBarDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer)
        actionBarDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_drawer) //set your own
        binding.drawerLayout.setScrimColor(Color.TRANSPARENT)
        binding.drawerLayout.drawerElevation = 0f
        binding.drawerLayout.addDrawerListener(actionBarDrawerToggle)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        actionBarDrawerToggle.syncState()


        setSupportActionBar(toolbar)

        val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        appBarConfiguration = AppBarConfiguration(
                setOf(R.id.nav_home,
                   /* R.id.profileFragment3
                    ,R.id.medicationsFragment,R.id.reportsFragment,R.id.trendsFragment,R.id.aboutFragment,R.id.legalFragment,R.id.settingsFragment*/
                ),
                drawerLayout = binding.drawerLayout
        )

        setupActionBarWithNavController(navController,appBarConfiguration)
//        toolbar.setupWithNavController(navController)
        //bottom_nav_view.setupWithNavController(navController)
       binding.navigationView.setupWithNavController(navController)

   /*     toolbar.iconRight.setOnClickListener {
            updateNavItems(1)
            fromAvatar = false
            val action = MobileNavigationDirections.actionGlobalProfileFragment3()
            navController.navigate(action)
        }*/

        navController.addOnDestinationChangedListener { controller, destination, arguments ->
         /*   toolbar.iconRight.visibility = View.VISIBLE*/
         /*   toolbar.iconRight.setImageResource(Utility.resoureId(applicationContext,UserSession(applicationContext).profileIcon))*/
             when (destination.id) {
                R.id.home ->  {
                    toolbar.txtHeader.text =  resources.getString(R.string.welcome) +" "+UserSession(applicationContext).firstName
                    updateNavItems(0)
                }
                R.id.profileFragment3 -> {
                    toolbar.txtHeader.text = resources.getString(R.string.profile)
                    /*toolbar.iconRight.visibility = View.GONE*/
                }
                R.id.medicationsFragment ->  toolbar.txtHeader.text = resources.getString(R.string.medications)
                R.id.reportsFragment ->  toolbar.txtHeader.text = resources.getString(R.string.reports)
                R.id.downloadFragment ->  toolbar.txtHeader.text = resources.getString(R.string.reports)
                R.id.measurementsFragment ->  toolbar.txtHeader.text = resources.getString(R.string.reports)
                R.id.testTypeFragment ->  toolbar.txtHeader.text = resources.getString(R.string.reports)
                R.id.trendsFragment ->  toolbar.txtHeader.text = resources.getString(R.string.trends)
                R.id.legalFragment ->  toolbar.txtHeader.text = resources.getString(R.string.legal)
                R.id.aboutFragment ->  toolbar.txtHeader.text = resources.getString(R.string.about)
                R.id.settingsFragment ->  toolbar.txtHeader.text = resources.getString(R.string.settings)
                R.id.avatarDialogFragment4 ->  toolbar.txtHeader.text = resources.getString(R.string.choose_avatar)
                R.id.deviceSetupFragment ->  toolbar.txtHeader.text = getString(R.string.device_setup)
                R.id.nav_home -> {
                    toolbar.txtHeader.text = getString(R.string.welcome) + " " + UserSession(applicationContext).firstName
                    toolbar.postButton.visibility = View.GONE
                    updateNavItems(0)
                }
                R.id.spirometryTestFrag -> {
                    toolbar.txtHeader.text =  getString(R.string.device_setup)
                  /*  toolbar.iconRight.visibility = View.GONE*/
                }
                R.id.trialFragment ->  toolbar.txtHeader.text = getString(R.string.spirometry_test)
                R.id.addMedicationFragment -> toolbar.txtHeader.text =  getString(R.string.addmedication)
                R.id.addSymptonsFragment ->  toolbar.txtHeader.text = getString(R.string.add_symptons)
                else ->  toolbar.txtHeader.text = ""
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        if (navController.currentDestination?.id==R.id.deviceSetupFragment || navController.currentDestination?.id==R.id.testResultsFragment || navController.currentDestination?.id==R.id.spirometryTestFrag || navController.currentDestination?.id==R.id.downloadFragment) {
            onBackPressedDispatcher.onBackPressed()
            return super.onSupportNavigateUp()
        }
        else if (navController.currentDestination?.id == R.id.home) {
            finish()
            return true
        }
        else {
            return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
        }
    }



    private fun bind() {
        toolbar = findViewById(R.id.safey_appBar)
    }
    private fun init(){
        activity = this
        fragmentClickViewModel = ViewModelProvider(this)[FragmentClickViewModel::class.java]
        //viewResultViewModel = ViewModelProvider(this).get(ViewResultViewModel::class.java)
        fragmentClickViewModel.getFragmentClick().observe(this) { item: String ->
            currentFragment = item
            when {
                item.equals(START_OVER, ignoreCase = true) -> {
                   // af.startOver()
                }
            }
        }

        updateNavItems(0)
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)

        binding.safeyAppBar.iconLeft.setOnClickListener {
            if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)

            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        dashboard.setOnClickListener{
            updateNavItems(0)
            drawerClose()
            fromAvatar = false
            val action = MobileNavigationDirections.actionGlobalNavHome()
            navController.navigate(action)
        }
        /* my_profile.setOnClickListener{
             updateNavItems(1)
             drawerClose()
             fromAvatar = false
            val action = MobileNavigationDirections.actionGlobalProfileFragment3()
             navController.navigate(action)
         }
         medications.setOnClickListener{
             updateNavItems(2)
             drawerClose()
             val action = MobileNavigationDirections.actionGlobalMedicationsFragment()
             navController.navigate(action)
         }
         trends.setOnClickListener{
             updateNavItems(3)
             drawerClose()
             val action = MobileNavigationDirections.actionGlobalTrendsFragment()
             navController.navigate(action)
         }
         reports.setOnClickListener{
             updateNavItems(4)
             drawerClose()
             val action = MobileNavigationDirections.actionGlobalReportsFragment3()
             navController.navigate(action)
         }
         about.setOnClickListener{
             updateNavItems(6)
             drawerClose()
             val action = MobileNavigationDirections.actionGlobalAboutFragment()
             navController.navigate(action)
         }
         legal.setOnClickListener{
             updateNavItems(7)
             drawerClose()
             val action = MobileNavigationDirections.actionGlobalLegalFragment()
             navController.navigate(action)
         }

         settings.setOnClickListener{
             updateNavItems(5)
             drawerClose()
             val action = MobileNavigationDirections.actionGlobalSettingsFragment()
             navController.navigate(action)
         }*/
    }

    fun drawerClose(){
        if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                binding.drawerLayout.closeDrawer(GravityCompat.START)

            } else {
                binding.drawerLayout.openDrawer(GravityCompat.START)
            }
    }

    public fun updateNavItems(position : Int){
        dashboard_selected_image.visibility = View.INVISIBLE
        my_profile_selected_image.visibility = View.INVISIBLE
   /*     medications_selected_image.visibility = View.INVISIBLE
        reports_selected_image.visibility = View.INVISIBLE
        about_selected_image.visibility = View.INVISIBLE
        trends_selected_image.visibility = View.INVISIBLE
        legal_selected_image.visibility = View.INVISIBLE
        setting_selected_image.visibility = View.INVISIBLE
        updateTextColor(my_profile, R.color.black)
        updateTextColor(medications,R.color.black)
        updateTextColor(reports, R.color.black)
        updateTextColor(trends,R.color.black)
        updateTextColor(about,R.color.black)
        updateTextColor(legal,R.color.black)
        updateTextColor(settings,R.color.black)*/
        updateTextColor(dashboard,R.color.black)
        when(position){
            0->{
                updateTextColor(dashboard)
                dashboard_selected_image.visibility = View.VISIBLE
            }
        /*    1->{
                updateTextColor(my_profile)
                my_profile_selected_image.visibility = View.VISIBLE
            }
            2->{
                updateTextColor(medications)
                medications_selected_image.visibility = View.VISIBLE
            }
            3->{
                updateTextColor(trends)
                trends_selected_image.visibility = View.VISIBLE
            }
            4->{
                updateTextColor(reports)
                reports_selected_image.visibility = View.VISIBLE
            }
            5->{
                updateTextColor(settings)
                setting_selected_image.visibility = View.VISIBLE
            }
            6->{
                updateTextColor(about)
                about_selected_image.visibility = View.VISIBLE
            }
            7->{
                updateTextColor(legal)
                legal_selected_image.visibility = View.VISIBLE
            }*/
        }
    }
    private fun updateTextColor(textview:TextView, color:Int = R.color.colorAccent){
        textview.setTextColor(ContextCompat.getColor(this,color))
    }

    override fun onCLick(fragLabel: String) {
        currentFragment = fragLabel
        when (fragLabel) {
            START_OVER -> {
               // af.startOver()
            }
            FRAGMENT_CODE_HOME -> {
               // af.addHomeFragment()
            }
            Constants.FRAGMENT_CODE_DEVICE_SETUP -> {
                //af.addDeviceSetupFragment()
            }
            Constants.FRAGMENT_CODE_SPIROMETRYTEST -> {
               // af.addSpirometryTestFragment()
            }
        }
    }
    public fun naviGate(pos:Int){
        updateNavItems(pos)
        fromAvatar = false
        val action = MobileNavigationDirections.actionGlobalNavHome()
        navController.navigate(action)
    }

}