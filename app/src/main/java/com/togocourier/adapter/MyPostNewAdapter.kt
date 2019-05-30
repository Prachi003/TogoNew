package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import kotlinx.android.synthetic.main.new_courier_post_adapter.view.*
import java.util.*

class MyPostNewAdapter(var from: String, var context: Context?, arrayList: ArrayList<GetMyPost.DataBean>?, private var myClick: MyOnClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list = arrayList

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_courier_post_adapter, parent, false)
        return ViewHolder(v, from)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val postData = list?.get(position)

        holder.bind(postData, myClick,position)

       /* holder.itemView.delete_post.setOnClickListener {
            myClick.deleteMyPost(postData?.id!!,position)
        }*/
    }
    class ViewHolder(itemView: View, var from: String) : RecyclerView.ViewHolder(itemView),View.OnClickListener {
        override fun onClick(v: View?) {

        }

        fun bind(postData: GetMyPost.DataBean?, myClick: MyOnClick, position: Int) = with(itemView) {
           /* delete_post.visibility = View.VISIBLE
            swipe_layout.isSwipeEnabled  = true*/
            //itemView.txtPrice.text="$ "+postData!!.totalPrice
            itemView.txtTitleNew.text= postData!!.postTitle
            //itemView.txtLaptopOrderQuantity.text=postData.itemCount
            itemView.setOnClickListener {
                myClick.OnClickItem(postData.postId!!, postData.userId,position)
            }
            itemView.txtTime.text=postData.ago


            if (postData.itemCount!!.length>1){
                itemView.txtLaptopOrderQuantity.text = postData.itemCount
            }else{
                itemView.txtLaptopOrderQuantity.text = "0"+postData.itemCount

            }


            itemView.txtPrice.text = "$%.2f".format(postData.totalPrice!!.toDouble())




        }

        fun printDifference(startDate: Date, endDate: Date, textView: TextView) {
            //milliseconds
            var different = endDate.time - startDate.time
            val secondsInMilli: Long = 1000
            val minutesInMilli = secondsInMilli * 60
            val hoursInMilli = minutesInMilli * 60
            val daysInMilli = hoursInMilli * 24
            val weeksInMilli = daysInMilli * 7
            val monthInMilli = weeksInMilli * 30
            val yearInMilli = monthInMilli * 12

            val elapsedYears = different / yearInMilli
            different = different % yearInMilli

            val elapsedMonths = different / monthInMilli
            different = different % monthInMilli

            val elapsedWeeks = different / weeksInMilli
            different = different % weeksInMilli

            val elapsedDays = different / daysInMilli
            different = different % daysInMilli

            val elapsedHours = different / hoursInMilli
            different = different % hoursInMilli

            val elapsedMinutes = different / minutesInMilli
            different = different % minutesInMilli

            val elapsedSeconds = different / secondsInMilli

            if (elapsedYears != 0L) {
                if (elapsedYears == 1L) {
                    textView.text = String.format("%s %s", elapsedYears.toString(),"Year ago")
                } else {
                    textView.text = String.format("%s %s", elapsedYears.toString(), "Year ago")
                }

            } else if (elapsedMonths != 0L) {
                if (elapsedMonths == 1L) {
                    textView.text = String.format("%s %s", elapsedMonths.toString(), "month ago")
                } else {
                    textView.text = String.format("%s %s", elapsedMonths.toString(), "month ago")
                }

            } else if (elapsedWeeks != 0L) {
                if (elapsedWeeks == 1L) {
                    textView.text = String.format("%s %s", elapsedWeeks.toString(), "week ago")
                } else {
                    textView.text = String.format("%s %s", elapsedWeeks.toString(), "week ago")
                }

            } else if (elapsedDays != 0L) {
                if (elapsedDays == 1L) {
                    textView.text = String.format("%s %s", elapsedDays.toString(), "day ago")
                } else {
                    textView.text = String.format("%s %s", elapsedDays.toString(), "day ago")
                }

            } else if (elapsedHours != 0L) {
                if (elapsedHours == 1L) {
                    textView.text = String.format("%s %s", elapsedHours.toString(), "hour ago")
                } else {
                    textView.text = String.format("%s %s", elapsedHours.toString(), "hour ago")
                }

            } else if (elapsedMinutes != 0L) {
                if (elapsedMinutes == 1L) {
                    textView.text = String.format("%s %s", elapsedMinutes.toString(), "minute ago")
                } else {
                    textView.text = String.format("%s %s", elapsedMinutes.toString(), "minute ago")
                }

            } else if (elapsedSeconds != 0L) {
                if (elapsedSeconds == 1L) {
                    textView.text = String.format("%s %s", elapsedSeconds.toString(), "second ago")
                } else {
                    textView.text = String.format("%s %s", elapsedSeconds.toString(), "second ago")
                }
            }
        }


    }







}

