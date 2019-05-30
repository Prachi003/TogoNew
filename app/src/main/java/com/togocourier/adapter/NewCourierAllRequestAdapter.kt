package com.togocourier.adapter

import android.app.AlertDialog
import android.content.Context
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.togocourier.Interface.AcceptRejectListioner
import com.togocourier.R
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import kotlinx.android.synthetic.main.new_detail_request_item.view.*

class NewCourierAllRequestAdapter(var context: Context?, arrayList: ArrayList<GetMyPost.DataBean.RequestsBean>?, acceptReject: AcceptRejectListioner, onChatClick: onChatClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list = arrayList
    var onChatClicked=onChatClick
    private var acceptRejectListioner = acceptReject

    override fun getItemCount(): Int {
        return list!!.size
    }

    interface onChatClick{
        fun OnClick( position: Int)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_detail_request_item, parent, false)
        return ViewHolder(v, context)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val requestData = list?.get(position)
        holder.bind(requestData,acceptRejectListioner,onChatClicked,position)
    }

    class ViewHolder(itemView: View, var context: Context?) : RecyclerView.ViewHolder(itemView) {

        fun bind(requestData: GetMyPost.DataBean.RequestsBean?, acceptReject: AcceptRejectListioner, onChatClicked: onChatClick, position: Int) = with(itemView) {
            if (!TextUtils.isEmpty(requestData?.profileImage)) {
                Glide.with(context).load(requestData?.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(itemImg)
            }

            val nameArray = requestData!!.fullName!!.split(" ")
            val builder = StringBuilder()
            val cap = nameArray[0].substring(0, 1).toUpperCase() + nameArray[0].substring(1)
            builder.append(cap).append(" ")
            itemView.itemNameTxt.text = requestData.fullName

          //  itemView.item_contact_num.text = requestData.applyUserContact
            itemView.itemPrice.text = "$ %.2f".format(requestData.bidPrice.toString().toDouble())
            // itemView.ratingBar.rating = requestData.rating?.toFloat()!!
            itemView.ratingBar.rating = Math.round(requestData.rating!!.toFloat()).toFloat()


            /*itemView.setOnClickListener {
                val intent = Intent(context, AllRequestActivity::class.java)
                intent.putExtra("POSTID", requestData.postId)
                context.startActivity(intent)
            }*/

            itemView.acceptLay.setOnClickListener {
                defaultDialog(acceptReject, requestData)
            }

            itemView.rejectLay.setOnClickListener {
                acceptReject.OnClick(requestData.bidId!!, "cancel", requestData.bidPrice.toString(),requestData.applyUserId)
            }

            itemView.itemChatIcon.setOnClickListener {
                onChatClicked.OnClick(position)
               /* val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("otherUID", requestData.applyUserId.toString())
                intent.putExtra("title", requestData)
                context.startActivity(intent)*/
            }
        }

        private fun defaultDialog(acceptRejectListioner: AcceptRejectListioner, requestData: GetMyPost.DataBean.RequestsBean?) {
            val alertDialog = AlertDialog.Builder(context)
            alertDialog.setTitle("Alert")
            alertDialog.setMessage("First you need to complete the payment procedure.")
            alertDialog.setPositiveButton("Ok", { dialog, which ->
                acceptRejectListioner.OnClick(requestData!!.bidId!!, "accept", requestData.bidPrice.toString(), requestData.applyUserId)
            })

            alertDialog.setNegativeButton("Cancel", { dialog, which ->
                dialog.cancel()
            })
            alertDialog.show()

        }
    }
}
