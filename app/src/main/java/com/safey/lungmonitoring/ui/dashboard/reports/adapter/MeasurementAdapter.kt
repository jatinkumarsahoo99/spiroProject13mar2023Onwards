package com.safey.lungmonitoring.ui.dashboard.reports.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.model.MeasurementModel
import kotlinx.android.synthetic.main.row_measurement.view.*

class MeasurementAdapter(context:Context, var list:MutableList<MeasurementModel>, var lastSelectedPosition:Int) : RecyclerView.Adapter<MeasurementAdapter.MeasurementViewHolder>() {

    var selectedList : MutableList<String> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MeasurementViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_measurement, parent, false)
        return MeasurementViewHolder(view)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MeasurementViewHolder, position: Int) {

        holder.itemView.txtMeasurement.text = list[position].measurement
        if (list[position].checked) {
          holder.itemView.imageSelect.visibility = View.VISIBLE
        }
        else {
            holder.itemView.imageSelect.visibility = View.GONE
        }
        holder.itemView.setOnClickListener{
            if (selectedList.contains(list[position].measurement)) {
                list[position].checked = false
                selectedList.remove(list[position].measurement)
            }
            else {
                if (selectedList.size<5) {
                    list[position].checked = true
                    selectedList.add(list[position].measurement)
                }
            }
            notifyDataSetChanged()
        }

    }

    override fun getItemCount(): Int {
        return list.size
    }

    class MeasurementViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun getSelected() : List<String>{
        return selectedList
    }

}