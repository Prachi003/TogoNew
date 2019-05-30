package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.responceBean.MyPostResponce
import kotlinx.android.synthetic.main.new_completed_item.view.*
import kotlinx.android.synthetic.main.new_item_my_swipe_post.view.*

class MyCompletedPostAdapter(var context: Context?, arrayList: ArrayList<MyPostResponce.DataBean>?, click: MyOnClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list = arrayList
    private var myOnClick = click

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.new_completed_item, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val postData = list?.get(position)
        holder.bind(postData, myOnClick,position)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(postData: MyPostResponce.DataBean?, click: MyOnClick, position: Int) = with(itemView) {


            itemView.txtTitlen.text = postData!!.postTitle
            itemView.txtLaptopOrdern.text = postData.deliveredItem
            itemView.txtTimeShown.text=postData.ago
            itemView.itemCountn.text=postData.postStatus
           // itemView.etPricen.text = "$"+postData.totalPrice

            itemView.etPricen.text = "$%.2f".format(postData.totalPrice!!.toDouble())


/*
            itemView.itemPickDt.text = postData.collectiveDate
            itemView.itemPickTime.text = Constant.setTimeFormat(postData.collectiveTime!!)
*/
            //itemView.itemPickaddr.text = postData.pickupAdrs

//            Glide.with(context).load(postData.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(itemImage)

            itemView.llParentn.setOnClickListener {
                click.OnClick(postData.postId!!,"",position)
            }

           /* when {
                postData.requestData!![0].requestStatus.equals("pending") -> itemView.tv_post_status.text = context.getString(R.string.pending)

                postData.requestData!![0].requestStatus.equals("accept") -> when {
                    postData.requestData!![0].deliveryStatus.equals("pending") -> itemView.tv_post_status.text = context.getString(R.string.accepted)
                    postData.requestData!![0].deliveryStatus.equals("picked") -> itemView.tv_post_status.text = context.getString(R.string.item_picked)
                    postData.requestData!![0].deliveryStatus.equals("outForDeliver") -> itemView.tv_post_status.text = context.getString(R.string.out_for_delivery)
                    postData.requestData!![0].deliveryStatus.equals("delivered") -> itemView.tv_post_status.text = context.getString(R.string.delivered)
                }

                postData.requestData!![0].requestStatus.equals("complete") -> itemView.tv_post_status.text = context.getString(R.string.delivered)
            }
*/
            //Glide.with(context).load(postData.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_assign_img)

           /* itemView.username.text = postData.fullName.toString()

            itemView.rl_customer_post_status.visibility = View.VISIBLE*/
        }

        }


}