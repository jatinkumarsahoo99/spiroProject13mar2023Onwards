package com.safey.lungmonitoring.ui.devicesetup.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import com.safey.lungmonitoring.ui.devicesetup.TestTypeModel
import kotlinx.android.synthetic.main.row_test_type.view.*

class TestTypeAdapter(context:Context,var testtypeModelList : List<TestTypeModel>,var clickListner : TestTypeClickListener) : RecyclerView.Adapter<TestTypeAdapter.TestTypeViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestTypeViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.row_test_type, parent, false)
        return TestTypeViewHolder(view)
    }

    override fun onBindViewHolder(holder: TestTypeViewHolder, position: Int) {
        holder.itemView.txtTestType.text = testtypeModelList[position].title
        holder.itemView.image_testtype.setImageResource( testtypeModelList[position].resourceId)
        if (testtypeModelList[position].checked)
            holder.itemView.imageSelected.visibility = View.VISIBLE
        else
            holder.itemView.imageSelected.visibility = View.INVISIBLE
        holder.itemView.setOnClickListener{

            testtypeModelList.forEach { user -> user.checked=(false) }
            testtypeModelList[position].checked = true


            notifyDataSetChanged()
            clickListner.onClick(testtypeModelList[position])
        }

        if (testtypeModelList[position].isBgShow)
            holder.itemView.img_testtype_bg.visibility = View.VISIBLE
        else
            holder.itemView.img_testtype_bg.visibility = View.INVISIBLE
    }

    override fun getItemCount(): Int {
        return testtypeModelList.size
    }

    class TestTypeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    fun getSelected(): List<TestTypeModel>{

        return testtypeModelList.filter { it.checked }
    }
}



interface TestTypeClickListener {

    fun onClick(item: TestTypeModel)
}
