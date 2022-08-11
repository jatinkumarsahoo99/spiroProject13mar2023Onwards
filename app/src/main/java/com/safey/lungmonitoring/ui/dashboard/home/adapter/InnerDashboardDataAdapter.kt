package com.safey.lungmonitoring.ui.dashboard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.SafeyApplication
import com.safey.lungmonitoring.data.tables.patient.AirTestResult
import com.safey.lungmonitoring.data.tables.patient.TestMeasurements
import com.safey.lungmonitoring.utils.Utility.rounded
import kotlinx.android.synthetic.main.fragment_test_results.view.*
import kotlinx.android.synthetic.main.row_fvc_dashboard.view.*

class InnerDashboardDataAdapter(
    var context: Context, private var list: List<AirTestResult>,var testItemClickListener: TestItemClickListner
) :
    RecyclerView.Adapter<InnerDashboardDataAdapter.MyViewHolder>(){

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =  LayoutInflater.from(parent.context)
            .inflate(R.layout.row_fvc_dashboard, parent, false) as View
        return MyViewHolder(view)
    }



    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        var data = list[position]
        //data.testResult.
        var measurement =""
        if (data.type == 2){
            SafeyApplication.appPrefs1?.isSpiroMeter = true
            measurement = if (data.testtype == 1) {
                "FVC"
            } else {
                "FIVC"
            }
        } else {
            SafeyApplication.appPrefs1?.isPeakflow = true
            measurement = "FEV1"

        }
        //var trialResult:List<TrialResult> = ArrayList()
        val trialResult = if(data.trialResult?.any { it.isPost } == true)
            data.trialResult?.filter { it.isPost }!!
        else {
             if(data.trialResult?.any { it.isBest } == true)
                data.trialResult?.filter { it.isBest }!!
            else
                data.trialResult
        }
        if (trialResult != null) {
            if (trialResult.isNotEmpty()) {
                val measurementData: TestMeasurements? =
                    trialResult[0].mesurementlist?.filter { it.measurement == measurement }?.get(0)
                "${measurementData?.measuredValue?.let { rounded(it) }}${measurementData?.unit}".also {
                    holder.itemView.lblMeasurementValue.text = it
                }
                holder.itemView.lblPercentage.visibility = View.VISIBLE
                holder.itemView.lblDivider.visibility = View.VISIBLE
                holder.itemView.lblSpace.visibility = View.VISIBLE
                if (measurementData?.predictedPer!! > 0.0)
                    (String.format(
                        "%.2f",
                        measurementData.predictedPer
                    ) + "%").also { holder.itemView.lblPercentage.text = it }
                else {
                    holder.itemView.lblPercentage.visibility = View.GONE
                    holder.itemView.lblDivider.visibility = View.GONE
                    holder.itemView.lblSpace.visibility = View.GONE
                }

                holder.itemView.lblMeasurement.text = measurement
            }
        }
        if (data.trialResult?.any { it.isPost } == true){
            holder.itemView.pre.visibility = View.VISIBLE
        }
        else
            holder.itemView.pre.visibility = View.INVISIBLE

//        var count = 0
//        var total = 0.0
//        var totalFev1Fvc = 0.0
//        var totalPEF = 0.0
//        var time = ""
//        var ispost = false
//        for (i in 0 until list[position].testResult?.size!!){
//            for (j in list[position].testResult!![i].trialResult!!.indices){
//                if (!ispost)
//                ispost = list[position].testResult!![i].trialResult!![j].isPost
//                count++
//                if (list[position].testResult?.get(i)?.type ==2) {
//                    holder.itemView.visibility = View.VISIBLE
//                    if (list[position].testResult?.get(i)?.testtype ==1) {
//                        total += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "`FVC" }[0].predictedPer
//                        totalFev1Fvc += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "FEV1/FVC" }[0].predictedPer
//                        totalPEF += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "PEF" }[0].measuredValue
//                    }
//                    /*else
//                    {
//                        total += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "FIVC" }[0].predictedPer
//                        totalFev1Fvc += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "FIV1/FIVC" }[0].predictedPer
//                        totalPEF += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "PIF" }[0].measuredValue
//
//                    }*/
//                }
//                else
//                {
//                    holder.itemView.visibility = View.GONE
//                    //total += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "BEV" }[0].predictedPer
//                    totalFev1Fvc += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "FEV1" }[0].measuredValue
//                    totalPEF += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "PEF" }[0].measuredValue
//
//                }
//
//            }
//        }
//        holder.itemView.lblPercentageValue.text = "${(total / count).toInt()}%"

//        holder.itemView.layoutfvc2.lblPercentageValue.text = "${String.format("%.2f",(totalPEF / count))}L"
//
//        if (list[position].testResult?.get(0)?.type ==2) {
//            if (list[position].testResult?.get(0)?.testtype ==1) {
//                holder.itemView.lblMeasurement.text = context.getString(R.string.fvc)
////                holder.itemView.layoutfvc1.lblMeasurement.text = context.getString(R.string.fev1fvc)
////                holder.itemView.layoutfvc2.lblMeasurement.text = context.getString(R.string.pef)
//                holder.itemView.lblPercentageValue.text =
//                    "${(totalFev1Fvc / count).toInt()}%"
//                holder.itemView.lblValue.text =
//            }
           /* else
            {
                holder.itemView.layoutfvc.lblMeasurement.text = context.getString(R.string.fivc)
                holder.itemView.layoutfvc2.lblMeasurement.text = context.getString(R.string.pif)
                holder.itemView.layoutfvc1.lblMeasurement.text = context.getString(R.string.fiv1fivc)
                holder.itemView.layoutfvc1.lblPercentageValue.text =
                    "${(totalFev1Fvc / count).toInt()}%"
            }*/
//        }else{
//            holder.itemView.layoutfvc.lblMeasurement.text = context.getString(R.string.bev)
//            holder.itemView.layoutfvc1.lblMeasurement.text = context.getString(R.string.fev1)
//            holder.itemView.layoutfvc2.lblMeasurement.text = context.getString(R.string.pef)
//            holder.itemView.layoutfvc1.lblPercentageValue.text = "${String.format("%.2f",(totalFev1Fvc / count))}L"
//        }

//        holder.itemView.layoutfvc.lblTime.visibility = View.V
//        holder.itemView.layoutfvc1.lblTime.visibility = View.GONE
//        holder.itemView.layoutfvc2.lblTime.visibility = View.GONE
        holder.itemView.lblTime.text  = list[position].testtime!!
        holder.itemView.PatientName.text = list[position].fname!!
        holder.itemView.setOnClickListener{
            testItemClickListener.onClick(list[position])
            /*holder.itemView.imgShare.visibility =View.VISIBLE*/
        }
        holder.itemView.deleteData.setOnClickListener{
            testItemClickListener.onDeleteClick(list[position])
            notifyItemRemoved(position)
            notifyDataSetChanged()
        }
      /*  holder.itemView.postData.setOnClickListener{
            testItemClickListener.onPost(list[position])
            notifyDataSetChanged()
        }*/
    }

    override fun getItemCount(): Int {
        return list.size
    }
}

interface TestItemClickListner{
    fun onClick( testResult: AirTestResult)
    fun onDeleteClick(testResult: AirTestResult)
    fun onUpdate(testResult: AirTestResult)
   /* fun onPost(testResult: AirTestResult)*/
}