package com.safey.lungmonitoring.ui.dashboard.medication.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.ui.dashboard.medication.MedFrequency
import kotlinx.android.synthetic.main.row_med_frequency.view.*
import java.util.*


class FreuencyAdapter(private val freqList: ArrayList<MedFrequency>, var lastSelectedPosition: Int , var medFrequencyClickListener: MedFrequencyClickListener): RecyclerView.Adapter<FreuencyAdapter.MedColorViewHolder>() {

    class MedColorViewHolder(view: View): RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedColorViewHolder {
        val inflatedView = parent.inflate(R.layout.row_med_frequency, false)
        return MedColorViewHolder(inflatedView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MedColorViewHolder, position: Int) {
        val medFrequency = freqList[position]
        holder.itemView.txtFreq.text = medFrequency.freqName
        if(lastSelectedPosition == position)
            holder.itemView.imageCheck.visibility=View.VISIBLE
        else
            holder.itemView.imageCheck.visibility=View.INVISIBLE


        holder.itemView.setOnClickListener {
            lastSelectedPosition=position

            notifyDataSetChanged()
            medFrequencyClickListener.onClick(medFrequency)
        }
    }

    override fun getItemCount(): Int {
        return freqList.size
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    fun update(position:Int){
        lastSelectedPosition = position - 1
        notifyDataSetChanged()
    }


    interface MedFrequencyClickListener {
        fun onClick(medFrequency: MedFrequency)
    }
    fun getSelected(): MedFrequency
    {
        return freqList[lastSelectedPosition]
    }

}
