package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import kotlinx.android.synthetic.main.new_courier_post_adapter.view.*

class NewPostAdapter(var from: String, var context: Context?, arrayList: ArrayList<GetMyPost.DataBean>?, private var myClick: MyOnClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
    }

    class ViewHolder(itemView: View, var from: String) : RecyclerView.ViewHolder(itemView) {
        fun bind(postData: GetMyPost.DataBean?, myClick: MyOnClick, position: Int) = with(itemView) {

            itemView.txtTitleNew.text = postData!!.postTitle
            if (postData.itemCount!!.length>1){
                itemView.txtLaptopOrderQuantity.text = postData.itemCount
            }else{
                itemView.txtLaptopOrderQuantity.text = "0"+postData.itemCount

            }


            itemView.txtPrice.text = "$%.2f".format(postData.totalPrice!!.toDouble())



/*
            val StrPrice = String(format: "%.2f", Double (objNot.strTotalPrice)!)
            cell.lblPrice.text = "$ " + StrPrice
*/


           // itemView.txtPrice.text =  "$ "+postData.totalPrice
            itemView.txtTime.text =  postData.ago



            itemView.setOnClickListener {
                myClick.OnClickItem(postData.postId!!,postData.userId,position)
            }


        }

    }
}