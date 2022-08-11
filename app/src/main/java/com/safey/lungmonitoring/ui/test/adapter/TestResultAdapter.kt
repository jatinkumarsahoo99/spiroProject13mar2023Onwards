package com.safey.lungmonitoring.ui.test.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.tables.patient.TestMeasurements
import com.safey.lungmonitoring.utils.Utility
import kotlinx.android.synthetic.main.row_test_result.view.*
import kotlinx.android.synthetic.main.row_test_result.view.lblLNN
import kotlinx.android.synthetic.main.row_test_result.view.lblMeasureValue
import kotlinx.android.synthetic.main.row_test_result.view.lblMeasurement
import kotlinx.android.synthetic.main.row_test_result.view.lblPercentage
import kotlinx.android.synthetic.main.row_test_result.view.lblZscore
import kotlinx.android.synthetic.main.row_test_result_compare.view.*


class TestResultAdapter(var context: Context, private var airTestValues: MutableList<TestMeasurements>? = null) :
RecyclerView.Adapter<TestResultAdapter.MyViewHolder>() {


    companion object{
        const val TYPE_HEADER =1
        const val TYPE_DATA =2
    }

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val view :View = if (viewType== TYPE_HEADER)
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_test_result_header, parent, false) as View
        else
            LayoutInflater.from(parent.context)
                .inflate(R.layout.row_test_result, parent, false) as View

        return MyViewHolder(view)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        if(position>0) {

            val data = airTestValues!![position-1]
            holder.view.lblMeasurement.text = data.measurement // +" [star-icon] "
//            holder.view.lblMeasurement.addImage("[star-icon]", R.drawable.ic_star,
//                context.resources.getDimensionPixelOffset(R.dimen._12sdp),
//                context.resources.getDimensionPixelOffset(R.dimen._12sdp))
            holder.view.lblMeasureValue.text = String.format(
                "%.2f",
                data.measuredValue
            ) + " ${data.unit}"

            if (data.predictedValue != " -  ") {
                val predicatedPer =
                    (data.predictedPer ) //data.predictedValue
                holder.view.lblPercentage.text =
                    String.format("%.2f", predicatedPer.toDouble()) + "%"
                if (predicatedPer>=100)
                    holder.view.lblPercentage.setTextColor(Color.parseColor("#00D16C"))
                else
                    holder.view.lblPercentage.setTextColor(Color.parseColor("#000000"))
            } else
                holder.view.lblPercentage.text = " -  "


            if (data.lln != " -  ") {
                holder.view.lblLNN.text = data.lln?.toDouble()?.let { Utility.rounded(it).toString() }
            }
            else
                holder.view.lblLNN.text = data.lln.toString()

            if (data.zScore != " -  ") {
                holder.view.lblZscore.text = data.zScore?.toDouble()?.let { Utility.rounded(it).toString() }
            }
            else
                holder.view.lblZscore.text = data.zScore.toString()

        }

       // holder.view.lblLNN.text = airTestValues[position].lnn

    }

    override fun getItemCount() = airTestValues!!.size + 1

    override fun getItemViewType(position: Int): Int {
        if (position == 0)
            return TYPE_HEADER
        else
            return TYPE_DATA
        //return super.getItemViewType(position)
    }

    interface setonClickListner {
        fun onClick(position: Int)
    }
}
