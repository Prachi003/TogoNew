package com.togocourier.ui.phase3.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.Interface.PostOnClick
import com.togocourier.R
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import kotlinx.android.synthetic.main.new_detail_post_adapter.view.*

class CourierPostItemdetailAdapter(context: Context, itemBeanList: ArrayList<GetMyPost.DataBean.ItemBean>, postOnClick: PostOnClick, fromS: String) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var postOnClickListener = postOnClick
    private var list = itemBeanList
    private var from = fromS
    private var context = context

    @SuppressLint("NewApi", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val requestData = list.get(position)
        holder.itemView.txtAddN.text = requestData.itemTitle
        holder.itemView.tvPriceText.text = "$ " + requestData.price
        Picasso.with(context).load(list[position].itemImageUrl).into(holder.itemView.itemImageN)
        if (from.equals("pending")) {
            if (requestData.itemStatus.equals("pending")) {
                holder.itemView.txtStatus.text = context.getString(R.string.pending)
            } else if (requestData.itemStatus.equals("picked")) {
                holder.itemView.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorBtn))

                holder.itemView.txtStatus.text = context.getString(R.string.item_picked)

            } else if (requestData.itemStatus.equals("outForDeliver")) {
                holder.itemView.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorBtn))

                holder.itemView.txtStatus.text = context.getString(R.string.out_for_delivery)

            } else if (requestData.itemStatus.equals("delivered")) {
                holder.itemView.txtStatus.setTextColor(ContextCompat.getColor(context, R.color.colorGreen))

                holder.itemView.txtStatus.text = context.getString(R.string.delivered)
            }

            /*else if (requestData.itemStatus.equals("accept")) {
                if (requestData.itemStatus.equals("pending")) holder.itemView.txtStatus.text = context.getString(R.string.accepted)
                else if (requestData.itemStatus.equals("picked")) holder.itemView.txtStatus.text = context.getString(R.string.item_picked)
                else if (requestData.itemStatus.equals("outForDeliver")) holder.itemView.txtStatus.text = context.getString(R.string.out_for_delivery)
                else if (requestData.itemStatus.equals("delivered")) holder.itemView.txtStatus.text = context.getString(R.string.delivered)
            }*/
            holder.itemView.txtStatus.visibility = View.VISIBLE
            /*if (requestData.itemStatus.equals("delivered")){
                holder.itemView.txtStatus.setTextColor(ContextCompat.getColor(context,R.color.colorGreen))
                val upperString = requestData.itemStatus!!.substring(0, 1).toUpperCase() + requestData.itemStatus!!.substring(1)

                holder.itemView.txtStatus.text=upperString

            }else{
                holder.itemView.txtStatus.setTextColor(ContextCompat.getColor(context,R.color.colorBtn))
                val upperString = requestData.itemStatus!!.substring(0, 1).toUpperCase() + requestData.itemStatus!!.substring(1)

                holder.itemView.txtStatus.text=upperString

            }*/
        } else {
            holder.itemView.txtStatus.visibility = View.GONE
        }
        holder.itemView.setOnClickListener {
            postOnClickListener.GetPosition(position)
        }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_detail_post_adapter, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(list: ArrayList<GetMyPost.DataBean.ItemBean>?) = with(itemView) {

        }

    }

}
