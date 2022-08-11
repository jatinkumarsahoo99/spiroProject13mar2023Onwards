package com.safey.lungmonitoring.ui.test.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.data.tables.patient.Variance
import kotlinx.android.synthetic.main.row_test_result.view.*
import kotlinx.android.synthetic.main.row_test_result.view.lblMeasureValue
import kotlinx.android.synthetic.main.row_test_result.view.lblMeasurement
import kotlinx.android.synthetic.main.row_test_result_compare.view.*


class VarianceAdapter(var context: Context, private var varianceList: MutableList<Variance>? = null) :
RecyclerView.Adapter<VarianceAdapter.MyViewHolder>() {




    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view

           val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.row_variance, parent, false) as View


        return MyViewHolder(view)
    }


    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {



            val data = varianceList!![position]
            holder.view.lblMeasurement.text = data.measurement

            holder.view.lblMeasureValue.text = String.format(
                "%.4f",
                data.measurementValue.toDouble()) + " L"



       // holder.view.lblLNN.text = airTestValues[position].lnn

    }

    override fun getItemCount() = varianceList!!.size




}
