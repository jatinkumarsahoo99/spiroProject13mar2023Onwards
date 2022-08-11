package com.safey.lungmonitoring.ui.dashboard.trends

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import kotlinx.android.synthetic.main.row_test_type.view.*

class TrendsTestResultAdapter(context:Context) : RecyclerView.Adapter<TrendsTestResultAdapter.TrendsBestTestViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrendsBestTestViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_best_test_result, parent, false)
        return TrendsBestTestViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrendsBestTestViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return 3
    }

    class TrendsBestTestViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

}