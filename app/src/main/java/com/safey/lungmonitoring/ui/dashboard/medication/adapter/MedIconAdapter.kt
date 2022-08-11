package com.safey.lungmonitoring.ui.dashboard.medication.adapter

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R

import com.safey.safey_medication.model.MedIcon
import kotlinx.android.synthetic.main.fragment_test_results.view.*

import kotlinx.android.synthetic.main.med_icons_item_layout.view.*
import java.util.*
import kotlin.collections.ArrayList


class MedIconAdapter(var context: Context, private val medIconList: ArrayList<MedIcon>,
                     var lastSelectedPosition:Int,var medIconClickListner: MedIconClickListner) :RecyclerView.Adapter<MedIconAdapter.MedIconViewHolder>() {

    private  var medcolorcode: String="#ffffff"


    class MedIconViewHolder(private val view: View):RecyclerView.ViewHolder(view),View.OnClickListener{

        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            Log.d("RecyclerView", "CLICK!")        }


    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedIconViewHolder {
        val inflatedView = parent.inflate(R.layout.med_icons_item_layout, false)
        return MedIconViewHolder(inflatedView)
    }

    override fun onBindViewHolder(holder: MedIconViewHolder, position: Int) {
        val medIcon = medIconList[position]
        //holder.bindData(medIcon,position,lastSelectedPosition,medcolorcode)
        holder.itemView.imageViewMedIcon.setImageResource(medIcon.resourceId)
        //Picasso.get().load(medIcon.resourceId).into(holder.itemView.imageViewMedIcon)
        if(lastSelectedPosition == position) {
            holder.itemView.imageViewMedIcon.setColorFilter(Color.parseColor(medcolorcode))
            holder.itemView.viewBg.background = ContextCompat.getDrawable(context, R.drawable.ic_medicon_bg_selected)
        } else {
            holder.itemView.viewBg.background = ContextCompat.getDrawable(context,R.drawable.ic_medicon_bg)
            holder.itemView.imageViewMedIcon.setColorFilter(Color.parseColor("#2B4098"), PorterDuff.Mode.MULTIPLY)
        }

        holder.itemView.setOnClickListener {
            lastSelectedPosition=position
            notifyDataSetChanged()
            medIconClickListner.onClick(medIcon)
        }
    }

    override fun getItemCount(): Int {
        return medIconList.size
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }

    interface MedIconClickListner {
        fun onClick(medIcon: MedIcon)
    }
    fun updateMedIcon(colorCode : String)
    {
        medcolorcode=colorCode
        notifyItemChanged(lastSelectedPosition)
    }

    fun updateMedIcon(position: Int, colorCode : String)
    {
        medcolorcode=colorCode
        lastSelectedPosition=position
        notifyItemChanged(position)
    }
    fun getSelected():String
    {
        return medIconList[lastSelectedPosition].medIcon
    }
}