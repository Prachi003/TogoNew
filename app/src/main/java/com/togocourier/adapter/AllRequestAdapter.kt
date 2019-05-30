package com.togocourier.adapter

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.togocourier.Interface.AcceptRejectListioner
import com.togocourier.R
import com.togocourier.responceBean.AllRequestListResponce
import com.togocourier.ui.activity.ChatActivity
import kotlinx.android.synthetic.main.new_all_request_item.view.*

class AllRequestAdapter(var context: Context?, arrayList: ArrayList<AllRequestListResponce.AppliedReqDataBean>?, acceptReject: AcceptRejectListioner) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list = arrayList
    private var acceptRejectListioner = acceptReject

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_all_request_item, parent, false)
        return ViewHolder(v, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val requestData = list?.get(position)
        holder.bind(requestData, acceptRejectListioner)
    }

    class ViewHolder(itemView: View, var context: Context?) : RecyclerView.ViewHolder(itemView) {

        @SuppressLint("SetTextI18n")
        fun bind(requestData: AllRequestListResponce.AppliedReqDataBean?, acceptRejectListioner: AcceptRejectListioner) = with(itemView) {
            itemView.itemNameTxt.text = requestData?.applyUserName
            // itemView.ratingBar.rating = requestData?.rating?.toFloat()!!
            itemView.ratingBar.rating = Math.round(requestData?.rating!!.toFloat()).toFloat()
            itemView.item_contact_num.text = requestData.applyUserContact

            if (!TextUtils.isEmpty(requestData.applyUserImage)) {
                Glide.with(context).load(requestData.applyUserImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(itemImg)
            }

            itemView.acceptLay.setOnClickListener {
                defaultDialog(acceptRejectListioner, requestData)
            }

            itemView.rejectLay.setOnClickListener {
                acceptRejectListioner.OnClick(requestData.requestId!!, "cancel", requestData.bidPrice.toString(), requestData.applyUserId)
            }

            itemView.ly_ratting.setOnClickListener {
                acceptRejectListioner.OnClickUserId(requestData.applyUserId.toString(), requestData.postUserId)
            }

            itemView.itemChatIcon.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("otherUID", requestData.applyUserId.toString())
                intent.putExtra("title", requestData.title.toString())
                context.startActivity(intent)
            }

            itemView.itemPrice.text = "$%.2f".format(requestData.bidPrice.toString().toDouble())

        }


        private fun defaultDialog(acceptRejectListioner: AcceptRejectListioner, requestData: AllRequestListResponce.AppliedReqDataBean?) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Alert")
            alertDialog.setMessage("First you need to complete the payment procedure.")
            alertDialog.setPositiveButton("Ok", { dialog, which ->
                acceptRejectListioner.OnClick(requestData?.requestId!!, "accept", requestData.bidPrice.toString(), requestData.applyUserId)
            })

            alertDialog.setNegativeButton("Cancel", { dialog, which ->
                dialog.cancel()
            })
            alertDialog.show()

        }


    }


}