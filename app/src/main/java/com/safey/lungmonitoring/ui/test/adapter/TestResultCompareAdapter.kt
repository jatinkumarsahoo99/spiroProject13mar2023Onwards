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
import com.safey.lungmonitoring.utils.Utility.addImage
import com.safey.lungmonitoring.utils.Utility.rounded
import kotlinx.android.synthetic.main.row_test_result_compare.view.*


class TestResultCompareAdapter(
    var context: Context,
    private var airTestValues: MutableList<TestMeasurements>? = null,
    var postAirTestValues: MutableList<TestMeasurements>?,
    var bestTestResult: Int
) :
RecyclerView.Adapter<TestResultCompareAdapter.MyViewHolder>() {


    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view

         val view =   LayoutInflater.from(parent.context)
                .inflate(R.layout.row_test_result_compare, parent, false) as View

        return MyViewHolder(view)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {



            val data = airTestValues!![position]
            val postdata = postAirTestValues!![position]
            holder.view.lblHeaderMeasurement.text = data.measurement
            holder.view.lblMeasurement.text = "$bestTestResult [star-icon] "
            holder.view.lblMeasurement.addImage("[star-icon]", R.drawable.ic_star,
                context.resources.getDimensionPixelOffset(R.dimen._12sdp),
                context.resources.getDimensionPixelOffset(R.dimen._12sdp))
            holder.view.lblMeasureValue.text = String.format(
                "%.2f",
                data.measuredValue
            ) + " ${data.unit}"

        holder.view.lblPostMeasureValue.text = String.format(
            "%.2f",
            postdata.measuredValue
        ) + " ${data.unit}"

            if (data.predictedValue != " -  ") {
                val predicatedPer: Double =
                    (Math.round((data.measuredValue / (data.predictedValue!!.toDouble())) * 100 * 100) / 100.0)//data.predictedValue

                holder.view.lblPercentage.text =
                    String.format("%.2f", predicatedPer.toDouble()) + "%"
                if (predicatedPer>=100)
                    holder.view.lblPercentage.setTextColor(Color.parseColor("#00D16C"))
                else
                    holder.view.lblPercentage.setTextColor(Color.parseColor("#000000"))
            } else
                holder.view.lblPercentage.text = " -  "

        if (postdata.predictedValue != " -  ") {
            val predicatedPer =
                (Math.round((postdata.measuredValue / (postdata.predictedValue!!.toDouble())) * 100 * 100) / 100.0)//data.predictedValue
            holder.view.lblPostPercentage.text =
                String.format("%.2f", predicatedPer.toDouble()) + "%"

            if (predicatedPer>=100)
                holder.view.lblPostPercentage.setTextColor(Color.parseColor("#00D16C"))
            else
                holder.view.lblPostPercentage.setTextColor(Color.parseColor("#000000"))
        } else
            holder.view.lblPostPercentage.text = " -  "

        if (postdata.lln != " -  ") {
            holder.view.lblPostLNN.text = postdata.lln?.toDouble()?.let { rounded(it).toString() }
        }
        else
            holder.view.lblPostLNN.text = postdata.lln.toString()

        if (postdata.zScore != " -  ") {
            holder.view.lblPostZscore.text = postdata.zScore?.toDouble()?.let { rounded(it).toString() }
        }
        else
            holder.view.lblPostZscore.text = postdata.zScore.toString()
        if (data.lln != " -  ") {
            holder.view.lblLNN.text = data.lln?.toDouble()?.let { rounded(it).toString() }
        }
        else
            holder.view.lblLNN.text = data.lln.toString()

        if (data.zScore != " -  ") {
            holder.view.lblZscore.text = data.zScore?.toDouble()?.let { rounded(it).toString() }
        }
        else
            holder.view.lblZscore.text = data.zScore.toString()





       // holder.view.lblLNN.text = airTestValues[position].lnn

    }

    override fun getItemCount() = airTestValues!!.size

    override fun getItemViewType(position: Int): Int {
        return super.getItemViewType(position)
    }

    interface setonClickListner {
        fun onClick(position: Int)
    }
}
