package com.safey.lungmonitoring.ui.devicesetup

import android.Manifest
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity.LOCATION_SERVICE
import android.app.Activity.RESULT_OK
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.pref.AppPrefs
import com.safey.lungmonitoring.interfaces.DialogStyle1Click
import com.safey.lungmonitoring.model.CustomDialogStyle1DataModel
import com.safey.lungmonitoring.ui.devicesetup.adapter.DeviceAdapter
import com.safey.lungmonitoring.ui.devicesetup.adapter.DeviceClickListener
import com.safey.lungmonitoring.ui.devicesetup.adapter.TestTypeAdapter
import com.safey.lungmonitoring.ui.devicesetup.adapter.TestTypeClickListener
import com.safey.lungmonitoring.ui.devicesetup.viewmodel.DeviceSetupViewModel
import com.safey.lungmonitoring.utils.Constants
import com.safey.lungmonitoring.utils.CustomDialogs
import com.safey.lungmonitoring.utils.LocationPermissionUtil
import com.safey.lungmonitoring.utils.Utility
import dagger.hilt.android.AndroidEntryPoint
import info.safey.safey_sdk.*
import kotlinx.android.synthetic.main.fragment_device_setup.*
import kotlinx.android.synthetic.main.layout_battery.view.*
import kotlinx.android.synthetic.main.layout_test_type.*
import kotlinx.android.synthetic.main.permission_dialog.*
import kotlinx.android.synthetic.main.permission_dialog.view.*
import kotlinx.android.synthetic.main.safey_save_button.view.*


@AndroidEntryPoint
class DeviceSetupFragment : Fragment(), TestTypeClickListener, DeviceClickListener, IScannerCallback,
    IErrorCallback,IConnectionCallback,IDeviceCallback,ITrialCallback, DialogStyle1Click {

    private var deviceType: Int = 0
    private lateinit var bluetoothDevice: BluetoothDevice
    private lateinit var deviceAdapter: DeviceAdapter
    private val TAG: String = DeviceSetupFragment::class.java.name
    private lateinit var adapter: TestTypeAdapter
    private var testTypeList: MutableList<TestTypeModel> = ArrayList()
    private  var listDevices: ArrayList<DeviceModel> = ArrayList()
    val viewModel : DeviceSetupViewModel by viewModels()
     //var safeyLungMonitor : SafeyLungMonitor? = null
     var safeyDeviceKit : SafeyDeviceKit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val onBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                isEnabled = true
                Log.e(TAG, "handleOnBackPressed: " )
                if(safeyDeviceKit?.isConnected == true)
                    safeyDeviceKit?.disconnect()
                else
                    findNavController().popBackStack(R.id.nav_home,false)
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_setup, container, false)
    }



    var requestBluetooth = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            permission_dialog.visibility = View.VISIBLE
            permission_dialog.permission_image.setImageResource(R.drawable.ic_location_permission)
            permission_dialog.safey_permission_bluetooth.text=(getString(R.string.gps_location_message))
            permission_dialog.appCompatTextView.text = getString(R.string.enable_location_services)
        }else{
            Constants.showToast("Permisssions are not granted",requireContext())
        }
    }


    var requestLocation = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            permission_dialog.visibility = View.GONE
            getSafeyLungMonitor()

        }else{
            Log.e(TAG, "Deny permission" )
        }
    }



    val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            when {
                permissions.get(Manifest.permission.BLUETOOTH_SCAN) == true -> {
                    val bluetoothManager = requireActivity().getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
                    // Ensures Bluetooth is available on the device and it is enabled. If not,
                    // displays a dialog requesting user permission to enable Bluetooth.
                    if (!bluetoothManager.adapter.isEnabled) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        requestBluetooth.launch(enableBtIntent)
                    }
                    else
                    {
                        permission_dialog.visibility = View.GONE
                        checkLocationPermission()
                    }
                }
                permissions.get(Manifest.permission.BLUETOOTH_CONNECT) == true -> {
                    val bluetoothManager = requireActivity().getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
                    // Ensures Bluetooth is available on the device and it is enabled. If not,
                    // displays a dialog requesting user permission to enable Bluetooth.
                    if (!bluetoothManager.adapter.isEnabled) {
                        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                        requestBluetooth.launch(enableBtIntent)
                    }
                    else
                    {
                        permission_dialog.visibility = View.GONE
                        checkLocationPermission()
                    }

                }

                else -> {
                    Log.e("TAG", "deny location ")
                }
            }
        }



    override fun onResume() {
        super.onResume()
        viewModel.getPatient()
        layout_battery.safey_save_button.isEnabled = false
        viewModel.patient.observe(viewLifecycleOwner, {
            if (it!=null)
            {
                viewModel.patientData = it
                layout_battery.safey_save_button.isEnabled = true
            }

        })

        val bluetoothManager = requireActivity().getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothManager.adapter.isEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestMultiplePermissions.launch(arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT))
            }
            else
                checkLocationPermission()
        }
        else {
            permission_dialog.visibility= View.VISIBLE
        }
        btnAllow.setOnClickListener{
            if (bluetoothManager.adapter.isEnabled)
                askLocationPermissions()
            else
                askBluetoothPermissions()
        }

        btnDeny.setOnClickListener{
            findNavController().popBackStack(R.id.nav_home,false)
        }

        layout_battery.safey_save_button.button_save.text = getString(R.string.starttest)
        txtmsg.text = getString(R.string.device_type_msg)
        imageDevice.visibility = View.INVISIBLE
        safey_save_button.button_save.text = getString(R.string.allow)
        enabledSaveButton(false)
        recyclerViewTestType.layoutManager = GridLayoutManager(context, 2)
        deviceAdapter= DeviceAdapter(requireContext(),listDevices,this)
        recyclerViewTestType.adapter = deviceAdapter
        safey_save_button.button_save.setOnClickListener{
            if (safey_save_button.button_save.text.equals(getString(R.string.allow))){
                safey_save_button.progressBar.visibility = View.VISIBLE
                safey_save_button.button_save.isEnabled = false
                safey_save_button.button_save.alpha = Constants.alphaBlur
                safeyDeviceKit?.connectDevice(bluetoothDevice)
                viewModel.scanCount =+ 1
            }
            else{
                layout_battery.imageDevice.setImageResource(R.drawable.ic_spiro_tick)
                layout_battery.visibility = View.VISIBLE
            }

        }
        layout_battery.safey_save_button.button_save.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if(viewModel.patientData.guid.isNotEmpty()) {
                    var height = 0
                    height = if(viewModel.patientData.HeightUnit == 1){
                        if (viewModel.patientData.Height.contains(".")){
                            Integer.parseInt(viewModel.patientData.Height.split(".")[0])
                        } else
                            (viewModel.patientData.Height).toInt()
                    } else {
                        (Utility.feetToCentimeter(viewModel.patientData.Height)).toInt()
                    }
                    Log.e(TAG, "onViewCreated: $height")
                    val safeyPerson = SafeyPerson(
//                        viewModel.patientData.ethnicity,
                        1,
                        1,
//                        viewModel.patientData.Gender,
                        age = Utility.getAge(viewModel.patientData.BirthDate),
//                        height = height,
                        height = 170,
                        weight = 0
                    )
                    safeyDeviceKit?.startTest(safeyPerson)
                }
            }

        })
    }

    private fun checkLocationPermission(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
            )
            {
                permission_dialog.visibility = View.VISIBLE
                permission_dialog.permission_image.setImageResource(R.drawable.ic_location_permission)
                permission_dialog.safey_permission_bluetooth.text=(getString(R.string.gps_location_message))
                permission_dialog.appCompatTextView.text = getString(R.string.enable_location_services)
            }
            else {
                if (ActivityCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(
                        requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                ){
                    permission_dialog.visibility = View.VISIBLE
                    permission_dialog.permission_image.setImageResource(R.drawable.ic_location_permission)
                    permission_dialog.safey_permission_bluetooth.text=(getString(R.string.gps_location_message))
                    permission_dialog.appCompatTextView.text = getString(R.string.enable_location_services)
                }
                else {
                    getSafeyLungMonitor()
                    permission_dialog.visibility = View.GONE
                }
            }

        } else {
            getSafeyLungMonitor()
            permission_dialog.visibility = View.GONE

        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        setHasOptionsMenu(true)


    }
    fun getSafeyLungMonitor(){
        safeyDeviceKit = SafeyDeviceKit.init(requireActivity(),"7659-2779-4723-9301-2442")
        safeyDeviceKit?.registerScannerCallback(this)
        safeyDeviceKit?.registerErrorCallback(this)
        safeyDeviceKit?.registerDeviceCallback(this)
        safeyDeviceKit?.registerConnectionCallback(this)
        safeyDeviceKit?.registerTrialCallback(this)

        safeyDeviceKit?.scanDevice()

        progressbar.visibility = View.VISIBLE
        viewModel.scanCount =+ 1

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun enabledSaveButton(boolean: Boolean) {
        if (!boolean)
        {
            safey_save_button.button_save.isEnabled = false
            safey_save_button.button_save.background = ContextCompat.getDrawable(requireContext(), R.drawable.shapesdisable)
        } else {
             safey_save_button.button_save.isEnabled =  true
             safey_save_button.button_save.background  = ContextCompat.getDrawable(requireContext(), R.drawable.btn_pink_round)
        }
    }


    private fun askBluetoothPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requestMultiplePermissions.launch(arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT))
        }
        else{
            val bluetoothManager = requireActivity().getSystemService(AppCompatActivity.BLUETOOTH_SERVICE) as BluetoothManager
            // Ensures Bluetooth is available on the device and it is enabled. If not,
            // displays a dialog requesting user permission to enable Bluetooth.
            if (!bluetoothManager.adapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                requestBluetooth.launch(enableBtIntent)
            }
        }
    }

    private fun askLocationPermissions(){

        if(!AppPrefs(requireContext()).isLocationAllowed)
            CustomDialogs.dialogStyleDelete(model = CustomDialogStyle1DataModel(requireActivity(),message = getString(R.string.locationmsg),positiveButton = getString(R.string.allow),negativeButton = getString(R.string.deny),
                dialogStyle1Click = this))
        else
            LocationPermissionUtil.checkLocationPermissions(requireActivity(), this::onLocationPermissionsGranted)




//        locationPermissionRequest.launch(
//            arrayOf(
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        )

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        LocationPermissionUtil.onRequestPermissionsResult(
            requireActivity(),
            requestCode,
            this::onLocationPermissionsGranted
        )
    }

    private fun onLocationPermissionsGranted() {
        checkLocationPermission()
    }
//    @TargetApi(30)
//    private fun Context.checkBackgroundLocationPermissionAPI30(backgroundLocationRequestCode: Int) {
//        if (checkSinglePermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)) return
//        AlertDialog.Builder(this)
//            .setTitle(R.string.background_location_permission_title)
//            .setMessage(R.string.background_location_permission_message)
//            .setPositiveButton(R.string.yes) { _,_ ->
//                // this request will take user to Application's Setting page
//                requestPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), backgroundLocationRequestCode)
//            }
//            .setNegativeButton(R.string.no) { dialog,_ ->
//                dialog.dismiss()
//            }
//            .create()
//            .show()
//
//    }

    private fun statusCheck() {
        val manager = requireActivity().getSystemService(LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        } else {
            getSafeyLungMonitor()
            permission_dialog.visibility = View.GONE
        }
    }


    //GPS Alert Dialog
    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(true)
            .setPositiveButton("Yes") { p0, p1 ->
                p0!!.cancel()
                val enableLocation = (Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                requestLocation.launch(enableLocation)

            }

            .setNegativeButton("No") { dialog, _ -> dialog.cancel() }
        val alert: AlertDialog = builder.create()
        alert.show()
    }



    override fun onPause() {
        super.onPause()
        if (safeyDeviceKit!=null) {
            safeyDeviceKit?.unregisterCallbacks()

        }
    }

    override fun onClick(item: TestTypeModel) {
        safeyDeviceKit?.setSelectedTestType(item.position)
        enabledSaveButton(true)
    }

    override fun enableTrial() {
        val action = DeviceSetupFragmentDirections.actionDeviceSetupFragmentToSpirometryTestFrag(deviceType)
        findNavController().navigate(action)
    }

   /* override fun enableTest() {
        TODO("Not yet implemented")
    }*/

    @SuppressLint("SetTextI18n")
    override fun getBatteryStatus(batteryStatus: String) {
        layout_battery.batteryperc.text = "${getString(R.string.batteryperc)}  $batteryStatus"
    }

    override fun getBluetoothDevice(device: BluetoothDevice) {
        if(safey_save_button.progressBar.visibility == View.VISIBLE && viewModel.scanCount >= 1){
            safey_save_button.progressBar.visibility = View.VISIBLE
            safey_save_button.button_save.isEnabled = false
            safey_save_button.button_save.alpha = Constants.alphaBlur
            safeyDeviceKit?.connectDevice(bluetoothDevice)
            viewModel.scanCount =+ 1
        }
        else {
            progressbar.visibility = View.GONE
            layout_test_type.visibility = View.VISIBLE
            if (!listDevices.contains(DeviceModel(device, false))) {
                listDevices.add(DeviceModel(device, false))
                deviceAdapter = DeviceAdapter(requireContext(), listDevices, this)
                recyclerViewTestType.adapter = deviceAdapter
            }
        }
    }

    override fun getConnected(isConnected: Boolean) {
        if (isConnected){
            Log.e(TAG, "getConnected: "+isConnected )
            progressbar.visibility = View.GONE
            safey_save_button.progressBar.visibility = View.GONE
            safey_save_button.button_save.isEnabled = true
            safey_save_button.button_save.alpha = Constants.alphaClear

        }
        else
        {
            if(safey_save_button.progressBar.visibility == View.VISIBLE && viewModel.scanCount == 1){
                getSafeyLungMonitor()
            }
            else
                findNavController().popBackStack(R.id.nav_home,false)
        }
    }



    @SuppressLint("NotifyDataSetChanged")
    override fun info(message: String) {
        Log.e(TAG, "info: $message")
        when(message){
            "INF_01" ->{
                progressbar.visibility  = View.GONE
                CustomDialogs.dialogStyleDelete(model = CustomDialogStyle1DataModel(requireActivity(),message = getString(R.string.no_device_found),positiveButton = getString(R.string.tryagain),negativeButton = getString(R.string.cancel),dialogStyle1Click = this))
            }
            "INF_02" -> {
                listDevices.clear()
                deviceAdapter.notifyDataSetChanged()
                progressbar.visibility  = View.INVISIBLE
                CustomDialogs.dialogStyleDelete(model = CustomDialogStyle1DataModel(requireActivity(),message = getString(R.string.saved_device_not_found),positiveButton = getString(R.string.tryagain),negativeButton = getString(R.string.cancel),dialogStyle1Click = this))


            }
            "INF_19" -> {
               layout_battery.imageDevice.setImageResource(R.drawable.ic_peakflow_tick)
               layout_battery.visibility = View.VISIBLE
                deviceType = 1
                layout_battery.safey_save_button.button_save.background = ContextCompat.getDrawable(requireContext(),R.drawable.btn_green_round)
            }
            "INF_20" -> {
                deviceType = 2
               setSpirtoTestType()
            }
        }
    }



    override fun lastConnectedDeviceFound(device: BluetoothDevice) {
    }


    override fun selectTestType() {
        Log.e(TAG, "selectTestType: called" )
        setSpirtoTestType()
    }

    private fun setSpirtoTestType() {
        layout_test_type.visibility = View.VISIBLE
        layout_battery.visibility = View.GONE
        txtmsg.text = getString(R.string.please_select_to_desired_n_spirometry_test_type)
        imageDevice.visibility = View.VISIBLE
        testTypeList = ArrayList()
        val items: List<String> = (resources.getStringArray(R.array.test_type_array).toList())
        val myImageList = intArrayOf(
            R.drawable.ic_fevc
            /*R.drawable.ic_fivc,R.drawable.ic_flow_volume_loop,R.drawable.ic_svc,R.drawable.ic_maximum_volume*/
        )

        //for (i in items.indices) {
            testTypeList.add(TestTypeModel(1,items[0], myImageList[0], false))
        //}
        recyclerViewTestType.layoutManager = GridLayoutManager(context, 2)
        adapter = TestTypeAdapter(requireContext(), testtypeModelList = testTypeList, this)
        recyclerViewTestType.adapter = adapter
        safey_save_button.button_save.text = getString(R.string.next)
        enabledSaveButton(false)
    }


    override fun onClick(item: DeviceModel) {
        bluetoothDevice = item.bluetoothDevice
        enabledSaveButton(true)
    }

    override fun positiveButtonClick() {
        if(safeyDeviceKit!=null) {
            safeyDeviceKit!!.removeDevice()
            safeyDeviceKit!!.scanDevice()
            progressbar.visibility = View.VISIBLE
        }
        else {
            AppPrefs(requireContext()).isLocationAllowed = true
            LocationPermissionUtil.checkLocationPermissions(
                requireActivity(),
                this::onLocationPermissionsGranted
            )
        }
    }

    override fun negativeButton() {
        if (safeyDeviceKit!=null)
        findNavController().popBackStack(R.id.nav_home,false)
        else
        {
            CustomDialogs.dialogStyleSuccess(model = CustomDialogStyle1DataModel(requireActivity(),message = getString(R.string.locationdenymsg),positiveButton = getString(R.string.ok),negativeButton = getString(R.string.deny),
                dialogStyle1Click = object : DialogStyle1Click {
                    override fun positiveButtonClick() {
                        findNavController().popBackStack(R.id.nav_home,false)
                    }

                    override fun negativeButton() {

                    }

                }))
        }
    }


}