package com.safey.lungmonitoring.ui.dashboard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.tables.patient.Symptoms
import com.safey.lungmonitoring.utils.Utility
import kotlinx.android.synthetic.main.row_sympton_dashboard.view.*


class SymptonDashboardAdapter(
    var context: Context,
    private var list: List<Symptoms>,
   var symptonItemClickListner: SymptonItemClickListener
) :
    RecyclerView.Adapter<SymptonDashboardAdapter.MyViewHolder>(){


    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =  LayoutInflater.from(parent.context)
            .inflate(R.layout.row_sympton_dashboard, parent, false) as View

        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {


       holder.itemView.lblTime.text = list[position].symptomtime
        holder.itemView.imageSympton1.visibility = View.INVISIBLE
        holder.itemView.imageSympton2.visibility = View.INVISIBLE
        holder.itemView.imageSympton3.visibility = View.INVISIBLE
        holder.itemView.imageSympton4.visibility = View.INVISIBLE
        holder.itemView.imageSympton5.visibility = View.INVISIBLE
        holder.itemView.imageSympton6.visibility = View.INVISIBLE
        for (i in 0 until list[position].symptomsIcons.size) {

            when(i)
            {
                0->{
                    holder.itemView.imageSympton1.visibility = View.VISIBLE
                    holder.itemView.imageSympton1.setImageResource(Utility.resoureId(context, list[position].symptomsIcons[i]))
                }
                1->{
                    holder.itemView.imageSympton2.visibility = View.VISIBLE
                    holder.itemView.imageSympton2.setImageResource(Utility.resoureId(context, list[position].symptomsIcons[i]))
                }
                2->{
                    holder.itemView.imageSympton3.visibility = View.VISIBLE
                    holder.itemView.imageSympton3.setImageResource(Utility.resoureId(context, list[position].symptomsIcons[i]))
                }
                3->{
                    holder.itemView.imageSympton4.visibility = View.VISIBLE
                    holder.itemView.imageSympton4.setImageResource(Utility.resoureId(context, list[position].symptomsIcons[i]))
                }
                4->{
                    holder.itemView.imageSympton5.visibility = View.VISIBLE
                    holder.itemView.imageSympton5.setImageResource(Utility.resoureId(context, list[position].symptomsIcons[i]))
                }
               5->{
                    holder.itemView.imageSympton6.visibility = View.VISIBLE
                    holder.itemView.imageSympton6.setImageResource(Utility.resoureId(context, list[position].symptomsIcons[i]))
                }

            }

        }
        holder.itemView.setOnClickListener {
            symptonItemClickListner.onClick(list[position])
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

interface SymptonItemClickListener {
    fun onClick(item: Symptoms)
}