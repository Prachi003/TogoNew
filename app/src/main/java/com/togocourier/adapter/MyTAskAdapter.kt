package com.togocourier.adapter

import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.togocourier.Interface.MyTaskListOnClick
import com.togocourier.R
import com.togocourier.responceBean.MyAllTaskResponce
import kotlinx.android.synthetic.main.new_courier_post_adapter.view.*
import kotlinx.android.synthetic.main.new_item_my_swipe_post_pending.view.*

class MyTAskAdapter(var context: Context?, arrayList: List<MyAllTaskResponce.DataBean>?, private var myClick: MyTaskListOnClick,private var status: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list = arrayList
    var statusN=status

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_item_my_swipe_post_pending, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val postData = list?.get(position)
        holder.bind(postData!!, myClick,statusN,position)

    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        fun bind(postData: MyAllTaskResponce.DataBean, myClick: MyTaskListOnClick, statusN: String, position: Int) = with(itemView) {
/*
            delete_post.visibility = View.GONE
            swipe_layout.isSwipeEnabled = false
*/

            itemView.txtTitleN.text = postData.postTitle

            //itemView.txtLaptopOrderN.text=postData.itemCount

            //itemView.etPriceN.text ="$ "+postData.totalPrice
            itemView.txtTimeShowN.text=postData.ago
            if (statusN.equals("completed")){
                itemView.itemCount.text = "Delivered"
                itemView.itemCount.setTextColor(ContextCompat.getColor(context,R.color.colorGreen))
            }else{
                itemView.itemCount.text = postData.itemCount +" "+"Delivered item"

            }

            if (postData.itemCount!!.length>1){
                itemView.txtLaptopOrderN.text = postData.itemCount
            }else{
                itemView.txtLaptopOrderN.text = "0"+postData.itemCount

            }


            itemView.etPriceN.text = "$%.2f".format(postData.totalPrice!!.toDouble())


/*
            itemView.itemPickDt.text = postData.collectiveDate
            itemView.itemPickTime.text = Constant.setTimeFormat(postData.collectiveTime!!)
            itemView.itemPickaddr.text = postData.pickupAdrs
*/


            itemView.setOnClickListener {
                myClick.OnClick(postData.postId!!, "",position)
            }

           /* when {
                postData.requestStatus.equals("pending") -> itemView.tv_courier_post_status.text = context.getString(R.string.pending)

                postData.requestStatus.equals("accept") -> when {
                    postData.deliveryStatus.equals("pending") -> itemView.tv_courier_post_status.text = context.getString(R.string.accepted)
                    postData.deliveryStatus.equals("picked") -> itemView.tv_courier_post_status.text = context.getString(R.string.item_picked)
                    postData.deliveryStatus.equals("outForDeliver") -> itemView.tv_courier_post_status.text = context.getString(R.string.out_for_delivery)
                    postData.deliveryStatus.equals("delivered") -> itemView.tv_courier_post_status.text = context.getString(R.string.delivered)
                }

                postData.requestStatus.equals("complete") -> itemView.tv_courier_post_status.text = context.getString(R.string.delivered)
            }*/

           // itemView.rl_courier_post_status.visibility = View.VISIBLE

        }

    }
}