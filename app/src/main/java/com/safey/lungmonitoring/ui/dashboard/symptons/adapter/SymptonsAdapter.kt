package com.safey.lungmonitoring.ui.dashboard.symptons

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import kotlinx.android.synthetic.main.row_add_symptons.view.*
import kotlinx.android.synthetic.main.row_test_type.view.imageSelected

class SymptonAdapter(context:Context, var symptomList : List<SymptomModel>, var clickListner : SymptonClickListener) : RecyclerView.Adapter<SymptonAdapter.SymptonViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SymptonViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_add_symptons, parent, false)
        return SymptonViewHolder(view)
    }

    override fun onBindViewHolder(holder: SymptonViewHolder, position: Int) {
        holder.itemView.txtSympton.text = symptomList[position].sympton
        holder.itemView.image_Sympton.setImageResource(symptomList[position].resourceId)

        if (symptomList[position].checked)
            holder.itemView.imageSelected.setImageResource(R.drawable.ic_selected_test_type)
        else
            holder.itemView.imageSelected.setImageResource(R.drawable.ic_radio_unchecked)
        holder.itemView.setOnClickListener{
            symptomList[position].checked = !symptomList[position].checked
            notifyDataSetChanged()

        }


    }

    override fun getItemCount(): Int {
        return symptomList.size
    }

    class SymptonViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun getSelected(): List<SymptomModel>{
        return symptomList
    }
}


interface SymptonClickListener {
    fun onClick(item: SymptomModel)
}
