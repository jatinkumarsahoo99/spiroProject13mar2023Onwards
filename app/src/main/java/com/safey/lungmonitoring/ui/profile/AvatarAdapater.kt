package com.safey.lungmonitoring.ui.profile

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.safey.lungmonitoring.R
import kotlinx.android.synthetic.main.row_avatar.view.*
import java.util.*

class AvatarAdapater(var AvatarContext: Context, list: List<Avatar>, avatarInterface: AvatarInterface) : RecyclerView.Adapter<AvatarAdapater.MyViewHolder>() {
    var AvatarList: List<Avatar> = ArrayList()
    var avatar: Avatar? = null
    var avatarInterface: AvatarInterface
    var selectedItem = -1



    interface AvatarInterface {
        fun selectedAvatar(avatar: Avatar?)
    }



    init {
        AvatarList = list
        this.avatarInterface = avatarInterface
    }

    class MyViewHolder(val view: View) : RecyclerView.ViewHolder(view)


    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): MyViewHolder {
        // create a new view
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_avatar, parent, false) as View

        /* val dateText = view.findViewById(R.id.dateText) as TextView
         val timeText = view.findViewById(R.id.timeText) as TextView
         val textFev = view.findViewById(R.id.fev1Measured) as TextView
         val textFvc = view.findViewById(R.id.fvcMeasured) as TextView*/


        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        var avatar = AvatarList[position]
        holder.view.avatarImage.setImageResource(avatar.resourceId)
        if (selectedItem == position) {
            holder.view.avatarImage.background = AvatarContext.resources.getDrawable(R.drawable.circle_border)
        } else {
            holder.view.avatarImage.background = null
        }
        holder.view.setOnClickListener {
            selectedItem = position
            avatar = AvatarList[position]
            avatarInterface.selectedAvatar(avatar)
            notifyDataSetChanged()
        }
    }

    override fun getItemCount(): Int {
        return AvatarList.size
    }


}