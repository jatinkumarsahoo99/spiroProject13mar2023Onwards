package com.safey.lungmonitoring.ui.devicesetup

import android.bluetooth.BluetoothDevice

data class DeviceModel(var bluetoothDevice: BluetoothDevice, var checked : Boolean = false)