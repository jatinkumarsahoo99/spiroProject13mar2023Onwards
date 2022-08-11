package com.safey.lungmonitoring.ui.devicesetup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.ui.devicesetup.DeviceModel
import kotlinx.android.synthetic.main.row_test_type.view.*

class DeviceAdapter(context:Context,var deviceModelList : List<DeviceModel>,var clickListner : DeviceClickListener) : RecyclerView.Adapter<DeviceAdapter.DeviceViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeviceViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_device_type, parent, false)
        return DeviceViewHolder(view)
    }

    override fun onBindViewHolder(holder: DeviceViewHolder, position: Int) {
        holder.itemView.txtTestType.text = deviceModelList[position].bluetoothDevice.name
        if (deviceModelList[position].bluetoothDevice.name.contains("PeakFlow"))
            holder.itemView.image_testtype.setImageResource( R.drawable.ic_peakflow_no_tick)
        else
            holder.itemView.image_testtype.setImageResource(R.drawable.ic_spiro_no_tick)
        if (deviceModelList[position].checked)
            holder.itemView.imageSelected.visibility = View.VISIBLE
        else
            holder.itemView.imageSelected.visibility = View.INVISIBLE
        holder.itemView.setOnClickListener{

            deviceModelList.forEach { user -> user.checked=(false) }
            deviceModelList[position].checked = true

            notifyDataSetChanged()
            clickListner.onClick(deviceModelList[position])
        }


    }

    override fun getItemCount(): Int {
        return deviceModelList.size
    }

    class DeviceViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)


}

interface DeviceClickListener {
    fun onClick(item: DeviceModel)
}
