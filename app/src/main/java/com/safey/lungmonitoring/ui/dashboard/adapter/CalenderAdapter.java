package com.safey.lungmonitoring.ui.dashboard.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.safey.lungmonitoring.R;
import com.safey.lungmonitoring.ui.dashboard.home.HomeFragment;
import com.safey.lungmonitoring.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;




public class CalenderAdapter extends RecyclerView.Adapter<CalenderAdapter.ViewHolder>{

    Context context;
    List<Date> listDate = new ArrayList<>();
    Date selectedDate,currentDate;
    //CalenderItemClickListener listener;
    ItemClickListener clickListener;
    HomeFragment fragment;

    public CalenderAdapter(Context context, List<Date> sortedDate, Date currentDate, ItemClickListener listener, HomeFragment dashboardFrag) {
        this.context = context;
        this.listDate = sortedDate;
        this.currentDate = currentDate;
        //this.listener = listener;
        this.clickListener = listener;
        this.fragment =  dashboardFrag;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.show_calender_list, parent, false);



        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder,int position) {

        Date date = listDate.get(position);
        String day  = (String) android.text.format.DateFormat.format("dd",  date);
        //String name  = (String) android.text.format.DateFormat.format("EE",  date);
        String name = new Utils().getweekDay(date,context);
        holder.nametextView.setText(name);
        holder.numbertextView.setText(day);
        selectedDate = fragment.selectedDate();



        if (!date.equals(selectedDate)){
            if (date.equals(currentDate)){
                holder.parentLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_cal_round_pink));

                holder.numbertextView.setTextColor(context.getResources().getColor(R.color.colorAccent));
                holder.nametextView.setTextColor(context.getResources().getColor(R.color.pink_day));
            }
            else {
                holder.parentLayout.setBackground(null);
                holder.numbertextView.setTextColor(ContextCompat.getColor(context, R.color.black_two));
                holder.nametextView.setTextColor(ContextCompat.getColor(context, R.color.grey));
            }
        }else {

            holder.parentLayout.setBackground(ContextCompat.getDrawable(context,R.drawable.ic_cal_round_blue));

            holder.numbertextView.setTextColor(context.getResources().getColor(R.color.white));
            holder.nametextView.setTextColor(context.getResources().getColor(R.color.white));
        }



        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //selectedDate = listDate.get(position);
                clickListener.onClick(v,position, listDate.get(position));
            }
        });


    }

    @Override
    public int getItemCount() {
        return listDate.size();
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView nametextView;
        public TextView numbertextView;

        public ConstraintLayout parentLayout;
        //private ItemClickListener clickListener;

        public ViewHolder(View itemView) {
            super(itemView);

            nametextView = itemView.findViewById(R.id.dayName);
            numbertextView = itemView.findViewById(R.id.dayNumber);
            parentLayout = itemView.findViewById(R.id.parentlayout);

        }


    }

    public interface ItemClickListener{

        void onClick(View view, int adapterPosition, Date b);
    }
}
