package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.ui.fragment.customer.model.GetPendingPost
import kotlinx.android.synthetic.main.new_item_my_swipe_post.view.*
import kotlinx.android.synthetic.main.new_item_my_swipe_post_pending.view.*
import java.text.SimpleDateFormat
import java.util.*

class MyPendingPostAdapter(var context: Context?, arrayList: ArrayList<GetPendingPost.DataBean>?, click: MyOnClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list = arrayList
    private var myOnClick = click

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_item_my_swipe_post, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
       val postData = list?.get(position)
        holder.bind(postData, myOnClick,position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(postData: GetPendingPost.DataBean?, click: MyOnClick, position: Int) = with(itemView) {
            /*delete_post.visibility = View.GONE
            swipe_layout.isSwipeEnabled = false
*/

            if (postData!!.itemCount!!.length>1){
                itemView.txtLaptopOrder.text=postData.itemCount
            }else{
                itemView.txtLaptopOrder.text=postData.itemCount

            }


            itemView.etPrice.text = "$%.2f".format(postData.totalPrice!!.toDouble())

            itemView.txtTitle.text = postData.postTitle
           // itemView.etPrice.text="$ "+postData.totalPrice
            itemView.username.text=postData.fullName
            itemView.txtTimeShow.text=postData.ago

            itemView.itemCount_deli.text=postData.itemCount+" "+"item delivered"
            Glide.with(context).load(postData.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(userImg)
            itemView.llParentItem.setOnClickListener {
                click.OnClickItem(postData.postId!!, postData.userId,position )
            }
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

            /*try {
                val date1 = simpleDateFormat.parse(postData.crd)
                val date2 = simpleDateFormat.parse(postData.upd)

                printDifference(date1,date2,itemView.txtTimeShow)

            } catch (e: ParseException) {
                e.printStackTrace()
            }


            //itemView.itemQuntyTxt.text = postData.itemQuantity
            Glide.with(context).load(postData.itemImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(userImg)
*/


/*
            itemView.itemPrice.text = "%.2f".format(postData.requestData!![0].bidPrice.toString().toDouble())
            itemView.itemPickDt.text = postData.collectiveDate
            itemView.itemPickTime.text = Constant.setTimeFormat(postData.collectiveTime!!)
            itemView.itemPickaddr.text = postData.pickupAdrs

            Glide.with(context).load(postData.itemImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(itemImage)

            itemView.main_click.setOnClickListener {
                click.OnClick(postData.id!!, postData.requestData?.get(0)?.id.toString())
            }

            when {
                postData.requestData!![0].requestStatus.equals("pending") -> itemView.tv_post_status.text = context.getString(R.string.pending)

                postData.requestData!![0].requestStatus.equals("accept") -> when {
                    postData.requestData!![0].deliveryStatus.equals("pending") -> itemView.tv_post_status.text = context.getString(R.string.accepted)
                    postData.requestData!![0].deliveryStatus.equals("picked") -> itemView.tv_post_status.text = context.getString(R.string.item_picked)
                    postData.requestData!![0].deliveryStatus.equals("outForDeliver") -> itemView.tv_post_status.text = context.getString(R.string.out_for_delivery)
                    postData.requestData!![0].deliveryStatus.equals("delivered") -> itemView.tv_post_status.text = context.getString(R.string.delivered)
                }

                postData.requestData!![0].requestStatus.equals("complete") -> itemView.tv_post_status.text = context.getString(R.string.delivered)
            }

            Glide.with(context).load(postData.requestData!![0].applyProfileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_assign_img)

            itemView.itemAsignTo.text = postData.requestData!![0].applyUserName

            itemView.rl_customer_post_status.visibility = View.VISIBLE
        }
*/

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