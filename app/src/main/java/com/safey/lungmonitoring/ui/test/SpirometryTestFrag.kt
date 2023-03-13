package com.safey.lungmonitoring.ui.test

import android.app.Dialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.databinding.FragmentSpirometryTestBinding
import com.safey.lungmonitoring.utils.Utility
import info.safey.safey_sdk.*
import android.os.CountDownTimer
import androidx.core.content.ContextCompat
import com.safey.lungmonitoring.interfaces.DialogStyle1Click
import com.safey.lungmonitoring.model.CustomDialogStyle1DataModel
import com.safey.lungmonitoring.ui.dashboard.Dashboard
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.CustomDialogs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_dashboard.*
import kotlinx.android.synthetic.main.safey_toolbar.view.*

@AndroidEntryPoint
class SpirometryTestFrag : Fragment(),  IErrorCallback,
    ITrialCallback, ITestCallback, IConnectionCallback {

    private var dialog: Dialog? = null
    private var devicetype: Int = 0
    private  var mCountDownTimer: CountDownTimer? = null
    private lateinit var binding: FragmentSpirometryTestBinding

    // private  var safeyLungMonitor: SafeyLungMonitor? = null
    private  var safeyDeviceKit: SafeyDeviceKit? = null
    var TAG = SpirometryTestFrag::class.java.name


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
                Log.e(TAG, "handleOnBackPressed: " )
                safeyDeviceKit?.disconnect()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
       binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_spirometry_test,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        getSafeyDeviceKit()

        devicetype = arguments?.getInt("deviceType",1)!!
        if(devicetype ==2) {
            (requireActivity() as Dashboard).safey_appBar.txtHeader.text = getString(R.string.title_spirometrytest)
            binding.liquidviewPretest.liquidFillView.setFrontWaveColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.wavecolor
                )
            )
            binding.liquidviewPretest.liquidFillView.setBehindWaveColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.wavecolor
                )
            )
            binding.liquidviewPretest.liquidFillView.setCenterTitleColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.wavecolor
                )
            )
        }
        else {
            (requireActivity() as Dashboard).safey_appBar.txtHeader.text =""
            binding.liquidviewPretest.liquidFillView.setFrontWaveColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.wavegreencolor
                )
            )
            binding.liquidviewPretest.liquidFillView.setBehindWaveColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.wavegreencolor
                )
            )
            binding.liquidviewPretest.liquidFillView.setCenterTitleColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.wavegreencolor
                )
            )
        }
        binding.liquidviewPretest.liquidFillView.progress = 0
        if (Constants.isPost)
            binding.liquidviewPretest.preTest.text = getString(R.string.posttest)
        else
            binding.liquidviewPretest.preTest.text = getString(R.string.pre_test)

        binding.liquidviewPretest.liquidFillView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if (safeyDeviceKit?.isTrialStarted() == false) {
                    binding.liquidviewPretest.preTest.text = ""
                    binding.liquidviewPretest.liquidFillView.setCenterTitle("")
                    binding.liquidviewPretest.liquidFillView.setCenterTitleColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
                    safeyDeviceKit?.startTrial(Constants.isPost)
                    binding.liquidviewPretest.ensureATi.visibility = View.INVISIBLE
                    binding.liquidviewPretest.timerGroup.visibility = View.VISIBLE
                    binding.liquidviewPretest.circularProgressIndicator.max = 100

/*
                    dialog = CustomDialogs.dialogStyleProgress(CustomDialogStyle1DataModel(requireActivity(),"","Please wait while we prepare for your test... ","","",
                        object : DialogStyle1Click{
                            override fun positiveButtonClick() {}

                            override fun negativeButton() {}

                        }))*/
                }
            }
        })

        binding.liquidviewPretest.safeyCancelButton.buttonCancel.setOnClickListener{
            mCountDownTimer?.cancel()
            mCountDownTimer?.onFinish()
            safeyDeviceKit?.disconnect()
            findNavController().popBackStack(R.id.nav_home,false)
        }



    }

    private fun getSafeyDeviceKit(){
        safeyDeviceKit = SafeyDeviceKit.init(requireActivity(),"7659-2779-4723-9301-2442")
        safeyDeviceKit?.registerTestCallback(this)
        safeyDeviceKit?.registerErrorCallback(this)
        safeyDeviceKit?.registerTrialCallback(this)
        safeyDeviceKit?.registerConnectionCallback(this)
    }

    override fun onPause() {
        super.onPause()
        if (mCountDownTimer!=null)
            mCountDownTimer?.cancel()
        if (safeyDeviceKit!=null) {
            safeyDeviceKit?.unregisterCallbacks()
        }
    }


    override fun getConnected(isConnected: Boolean) {

        if (!isConnected) {
            findNavController().popBackStack(R.id.nav_home,false)
        }
    }



    override fun enableTrial() {
        binding.liquidviewPretest.liquidFillView.progress = 0

    }


    override fun info(message: String) {
        when (message) {
            "INF_01" -> {
               /* textViewInfo.text = getString(info.safey.safey_sdk.R.string.no_device_found)
                buttonscanDevice.text = resources.getString(info.safey.safey_sdk.R.string.scan_device)
                buttonscanDevice.show()*/
            }
            "INF_02" -> {
              /*  textViewInfo.text = getString(info.safey.safey_sdk.R.string.saved_device_not_found)
                devicesRecyclerView.hide()
                buttonDeleteDevice.show()*/
            }
            "INF_03" -> {
                binding.liquidviewPretest.preTest.text = getString(R.string.invalid_trail_plz_retry)
                binding.liquidviewPretest.preTest.setTextColor(Color.BLACK)
            }
            "INF_05" -> {
                binding.liquidviewPretest.liquidFillView.mBottomTitle =
                    getString(R.string.start_blowing)
//                dialog!!.cancel()
                var count = 0
                binding.liquidviewPretest.circularProgressIndicator.progress = count
                mCountDownTimer = object : CountDownTimer(20000, 1000) {
                    override fun onTick(millisUntilFinished: Long) {
                        Log.v("Log_tag", "Tick of Progress$count$millisUntilFinished")
                        count++
                        binding.liquidviewPretest.tvTime.text = count.toString()
                        binding.liquidviewPretest.circularProgressIndicator.progress =
                            count as Int * 100 / (20000 / 1000)
                    }

                    override fun onFinish() {
                        //Do what you want
                        count = 0
                        binding.liquidviewPretest.tvTime.text = "0"
                        binding.liquidviewPretest.circularProgressIndicator.progress = 0
                        mCountDownTimer?.cancel()
                    }
                }
                mCountDownTimer?.start()
            }
            "INF_10" -> binding.liquidviewPretest.liquidFillView.mBottomTitle = getString(R.string.done)
            "INF_11" -> {
                binding.liquidviewPretest.liquidFillView.mBottomTitle =
                    getString(R.string.you_are_doing_good)
                binding.liquidviewPretest.liquidFillView.setCenterTitleColor(ContextCompat.getColor(requireContext(),R.color.colorAccent))
            }
            "INF_12" -> binding.liquidviewPretest.liquidFillView.mBottomTitle = getString(R.string.keep_blowing)
            "INF_13" -> {
                binding.liquidviewPretest.preTest.text = getString(R.string.insufficient_blow)
                binding.liquidviewPretest.liquidFillView.progress = 0

                binding.liquidviewPretest.preTest.setTextColor(Color.RED)
                binding.liquidviewPretest.liquidFillView.mBottomTitle = getString(R.string.click_here_to_start)
                binding.liquidviewPretest.liquidFillView.setCenterTitle(getString(R.string.start))
            }
            "INF_16" -> {
                binding.liquidviewPretest.preTest.text = getString(R.string.fan_moving)
                binding.liquidviewPretest.preTest.setTextColor(Color.BLACK)

            }
            "INF_17" -> {
                binding.liquidviewPretest.liquidFillView.progress = 0
                mCountDownTimer?.cancel()
                binding.liquidviewPretest.preTest.text = getString(R.string.time_out)
                binding.liquidviewPretest.preTest.setTextColor(Color.RED)
                binding.liquidviewPretest.liquidFillView.mBottomTitle = getString(R.string.click_here_to_start)
                binding.liquidviewPretest.liquidFillView.setCenterTitle(getString(R.string.start))
            }

            /*  "INF_03" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.invalid_trail_plz_retry)
              "INF_04" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.four_trials_completed)
              "INF_05" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.start_blowing)
              "INF_06" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.device_disconnected)
              "INF_07" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.trial_completed)
              "INF_08" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.test_completed)
              "INF_09" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.no_data_available)
              "INF_10" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.done)
              "INF_11" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.you_are_doing_good)
              "INF_12" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.keep_blowing)
              "INF_13" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.insufficient_blow)
              "INF_14" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.no_trial_to_delete)
              "INF_15" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.trial_deleted)
              "INF_16" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.fan_moving)
              "INF_17" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.time_out)
              "INF_18" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.device_off)
              "INF_19" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.peak_flow)
              "INF_20" -> textViewInfo.text = getString(info.safey.safey_sdk.R.string.spirometer)
              "INF_21" -> textViewBatteryDetails.text = getString(info.safey.safey_sdk.R.string.invalid_test_by_bev)
       */       else -> {

     }
        }
    }

    override fun onProgressChange(progress: Int) {
       binding.liquidviewPretest.liquidFillView.progress = progress
    }

  /*  override fun getTestResult(airGraphData: FlowVolumeData, trialCount: Int) {
        TODO("Not yet implemented")
    }*/

    override fun getTestResults(testResult: String, trialCount: Int,sessionScore : String) {
        binding.liquidviewPretest.liquidFillView.progress = 0
        mCountDownTimer?.cancel()
        val testData =  Gson().fromJson(testResult, TestData::class.java)
        val testResultList: List<TestResult> = testData.testResults
        Utility.getTrialResult(testResultList, trialCount, this,devicetype,testData.testType,testData.sessionScore,testData.variance)
        Thread.sleep(20)

        binding.liquidviewPretest.preTest.text = ""
        binding.liquidviewPretest.preTest.setTextColor(Color.BLACK)
        binding.liquidviewPretest.liquidFillView.mBottomTitle = getString(R.string.click_here_to_start)


    }


    override fun invalidManeuver(trialCount: Int) {
        binding.liquidviewPretest.liquidFillView.progress = 0
        mCountDownTimer?.cancel()
        mCountDownTimer?.onFinish()

    }


    override fun testCompleted() {

    }


}