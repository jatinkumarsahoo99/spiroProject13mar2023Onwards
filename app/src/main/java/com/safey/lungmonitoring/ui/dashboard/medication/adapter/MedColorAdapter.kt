package com.safey.lungmonitoring.ui.dashboard.medication.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.safey_medication.model.MedColors
import kotlinx.android.synthetic.main.color_icon_item_layout.view.*
import java.util.*


class MedColorAdapter(var myContext: Context, private val medColorList: ArrayList<MedColors>, private var lastSelectedPosition:Int,var medColorClickListner: MedColorClickListner): RecyclerView.Adapter<MedColorAdapter.MedColorViewHolder>() {

    var context = myContext

    class MedColorViewHolder(private val view: View): RecyclerView.ViewHolder(view), View.OnClickListener{

        private var medColor: MedColors?=null

        init {
            view.setOnClickListener(this)
        }
        override fun onClick(v: View?) {
            Log.d("RecyclerView", "CLICK!")        }

        fun bindData(medColor: MedColors, position: Int, lastSelectedPosition: Int) {
            this.medColor = medColor

        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): MedColorViewHolder {
        val inflatedView = parent.inflate(R.layout.color_icon_item_layout, false)
        return MedColorViewHolder(inflatedView)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onBindViewHolder(holder: MedColorViewHolder, position: Int) {
        val medColor = medColorList[position]

        val bgShape = holder.itemView.textView57.getBackground() as GradientDrawable
        bgShape.setColor(Color.parseColor(medColor.medColor))

        if(lastSelectedPosition == position)
            holder.itemView.viewBg.background=ContextCompat.getDrawable(context, R.drawable.gray_circle)
        else
            holder.itemView.viewBg.background=ContextCompat.getDrawable(context,R.drawable.gray_circle_no_border)

        holder.itemView.setOnClickListener {
            lastSelectedPosition=position

            notifyDataSetChanged()
            medColorClickListner.onClick(medColor)
        }
    }

    override fun getItemCount(): Int {
        return medColorList.size
    }

    fun ViewGroup.inflate(@LayoutRes layoutRes: Int, attachToRoot: Boolean = false): View {
        return LayoutInflater.from(context).inflate(layoutRes, this, attachToRoot)
    }


    interface MedColorClickListner {
        fun onClick(medColor: MedColors)
    }
    fun getSelected():String
    {
        return medColorList[lastSelectedPosition].medColor
    }

}
