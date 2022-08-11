package com.safey.lungmonitoring.ui.dashboard.home.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.ui.dashboard.home.SectionModel
import com.safey.lungmonitoring.utils.GridSpacingItemDecoration
import kotlinx.android.synthetic.main.row_dashboard.view.*


class DashboardDataAdapter(
    var context: Context,
    private var list: List<SectionModel>,var symptonItemClickListener: SymptonItemClickListener,var testItemClickListener: TestItemClickListner
) :
    RecyclerView.Adapter<DashboardDataAdapter.MyViewHolder>() {

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =  LayoutInflater.from(parent.context)
            .inflate(R.layout.row_dashboard, parent, false) as View
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        holder.itemView.morning.text = list[position].section
        if (list[position].section == context.getString(R.string.morning))
            holder.itemView.imageTime.setImageResource(R.drawable.ic_morning)
        else if (list[position].section == context.getString(R.string.afternoon))
            holder.itemView.imageTime.setImageResource(R.drawable.ic_afternoon)
        else if (list[position].section == context.getString(R.string.evening))
            holder.itemView.imageTime.setImageResource(R.drawable.ic_evening)
        else
            holder.itemView.imageTime.setImageResource(R.drawable.ic_night)


        if (list[position].testResult?.isNotEmpty() == true) {
            holder.itemView.testRecyclerView.visibility = View.VISIBLE
            val lm = LinearLayoutManager(context)
            holder.itemView.testRecyclerView.addItemDecoration(
               /* GridSpacingItemDecoration(
                    2,
                    50,
                    false
                )*/
                DividerItemDecoration(context,LinearLayoutManager.VERTICAL,)
            )

            holder.itemView.testRecyclerView.layoutManager =
                LinearLayoutManager(this.context,  RecyclerView.VERTICAL, false)
            val adapter = list[position].testResult?.let {
                InnerDashboardDataAdapter(
                    context,
                    it,
                    testItemClickListener
                )
            }


            holder.itemView.testRecyclerView.adapter = adapter
        }
        else holder.itemView.testRecyclerView.visibility = View.GONE

        holder.itemView.recyclerViewSymptons.layoutManager =
            GridLayoutManager(this.context,  2,RecyclerView.VERTICAL,false)

        holder.itemView.recyclerViewSymptons.addItemDecoration(GridSpacingItemDecoration(2, 50, false))
/*        val listSymptonslist = ArrayList<Symptons>()
        for (sympton in listSymptons){

            if (sympton.section == list[position].section && list[position].date == sympton.date){
                sympton.symptonList?.let { listSymptonslist.addAll(it) }
            }
        }*/
        holder.itemView.recyclerViewSymptons.adapter =
            list[position].symptonList?.let { SymptonDashboardAdapter(context, it,symptonItemClickListener) }

        /*holder.itemView.layoutfvc.pre.text = context.getString(R.string.pre)
        holder.itemView.layoutfvc1.pre.text = context.getString(R.string.post)
        holder.itemView.layoutfvc2.pre.text = context.getString(R.string.pre)
        holder.itemView.layoutsympton.lblTime.text = context.getString(R.string.pre)
        holder.itemView.layoutfvc4.pre.text = context.getString(R.string.post)

        var count = 0
        var total = 0.0
        var totalFev1Fvc = 0.0
        var totalPEF = 0.0
        var time = ""
        var ispost = false
        for (i in 0 until list[position].testResult?.size!!){
            for (j in list[position].testResult!![i].trialResult!!.indices){
                if (!ispost)
                ispost = list[position].testResult!![i].trialResult!![j].isPost
                count++
                if (list[position].testResult?.get(i)?.type ==2) {
                    total += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "FVC" }[0].predictedPer
                    totalFev1Fvc += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "FEV1/FVC" }[0].predictedPer
                    totalPEF += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "PEF" }[0].measuredValue
                }
                else
                {
                    total += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "BEV" }[0].predictedPer
                    totalFev1Fvc += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "FEV1" }[0].predictedPer
                    totalPEF += list[position].testResult!![i].trialResult!![j].mesurementlist!!.filter { it.measurement == "PEF" }[0].measuredValue

                }

            }
        }
        holder.itemView.layoutfvc.lblPercentageValue.text = "${(total / count).toInt()}%"
        holder.itemView.layoutfvc1.lblPercentageValue.text = "${(totalFev1Fvc / count).toInt()}%"
        holder.itemView.layoutfvc2.lblPercentageValue.text = "${String.format("%.2f",(totalPEF / count))}L"

        if (list[position].testResult?.get(0)?.type ==2) {
            holder.itemView.layoutfvc.lblMeasurement.text = context.getString(R.string.fvc)
            holder.itemView.layoutfvc1.lblMeasurement.text = context.getString(R.string.fev1fvc)
        }else{
            holder.itemView.layoutfvc.lblMeasurement.text = context.getString(R.string.bev)
            holder.itemView.layoutfvc1.lblMeasurement.text = context.getString(R.string.fev1)
        }
        holder.itemView.layoutfvc2.lblMeasurement.text = context.getString(R.string.pef)

        holder.itemView.layoutfvc.lblTime.text  = list[position].testResult!![0].testtime!!
        holder.itemView.layoutfvc1.lblTime.text = list[position].testResult!![0].testtime!!
        holder.itemView.layoutfvc2.lblTime.text = list[position].testResult!![0].testtime!!

        if (!ispost) {
            holder.itemView.layoutfvc.pre.text = context.getString(R.string.pre)
            holder.itemView.layoutfvc1.pre.text = context.getString(R.string.pre)
            holder.itemView.layoutfvc2.pre.text = context.getString(R.string.pre)
        }
        else{
            holder.itemView.layoutfvc.pre.text = context.getString(R.string.post)
            holder.itemView.layoutfvc1.pre.text = context.getString(R.string.post)
            holder.itemView.layoutfvc2.pre.text = context.getString(R.string.post)
        }*/




    }

    override fun getItemCount(): Int {
        return list.size
    }


}