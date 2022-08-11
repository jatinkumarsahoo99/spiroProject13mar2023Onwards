package com.safey.lungmonitoring.ui.dashboard.medication.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.data.tables.patient.Medication
import com.safey.lungmonitoring.databinding.RowMedicationListBinding
import kotlinx.android.synthetic.main.med_icons_item_layout.view.*
import kotlinx.android.synthetic.main.row_medication_list.view.*

class MedicationListAdapter(var context:Context,var medicationList : List<Medication>,val clickListener : MedicationClickListener) :   RecyclerView.Adapter<MedicationListAdapter.BindingViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BindingViewHolder {

        val rootView: ViewDataBinding =
            RowMedicationListBinding.inflate(LayoutInflater.from(context), parent, false)

        return BindingViewHolder(rootView)
    }

    override fun onBindViewHolder(holder: BindingViewHolder, position: Int) {
        val medication = medicationList[position]

        holder.itemBinding.setVariable(com.safey.lungmonitoring.BR.medication, medication)
        holder.itemBinding.executePendingBindings()

        holder.itemView.imageView2.setColorFilter(Color.parseColor(medication.medColor))

        holder.itemView.setOnClickListener{
            clickListener.onClick(medication.guid)
        }

        holder.itemView.image_med_delete.setOnClickListener{
            clickListener.onDeleteClick(medication.guid)

        }

    }

    override fun getItemCount() = medicationList.size

    class BindingViewHolder(val itemBinding: ViewDataBinding) :
        RecyclerView.ViewHolder(itemBinding.root)

    interface MedicationClickListener {
        fun onClick(id:String)
        fun onDeleteClick(id:String)
    }


}