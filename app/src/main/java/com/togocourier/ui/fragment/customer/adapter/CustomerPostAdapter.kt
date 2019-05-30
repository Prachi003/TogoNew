package com.togocourier.ui.fragment.customer.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.squareup.picasso.Picasso
import com.togocourier.Interface.PostOnClick
import com.togocourier.R
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import kotlinx.android.synthetic.main.new_customer_post_adapter.view.*


class CustomerPostAdapter(addList: ArrayList<GetMyPost.DataBean.ItemBean>? = null, postOnClick: PostOnClick, mContext: Context) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var postOnClickListener = postOnClick
    private var list = addList
    private var context = mContext
    var courierItemUri = ""

    @SuppressLint("NewApi", "SetTextI18n")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as CustomerPostAdapter.ViewHolder
        //  val requestData = list?.get(position)
        holder.bind(postOnClickListener, list)
        holder.itemView.cardPost.setOnClickListener {
            postOnClickListener.GetPosition(position)
        }

        holder.itemView.card.setOnClickListener {
          postOnClickListener.delete(position,holder.itemView)
        }

        if (position == 0) {

        } else {
            holder.itemView.itemImage.visibility = View.VISIBLE
            holder.itemView.rlText.visibility = View.VISIBLE
            holder.itemView.tvPrice.visibility = View.VISIBLE
            holder.itemView.imgAdd.visibility = View.GONE
            holder.itemView.txtAdd.visibility = View.VISIBLE
            holder.itemView.card.visibility = View.VISIBLE
            holder.itemView.place.visibility = View.VISIBLE
            holder.itemView.tvPrice.setText("$ " + list!![position].price)
            holder.itemView.txtAdd.typeface = Typeface.DEFAULT_BOLD
            holder.itemView.txtAdd.setText(list!![position].itemTitle)
            Picasso.with(context).load(list!![position].itemImageUrl).into(holder.itemView.itemImage)
             courierItemUri = list!![position].itemImageUrl!!
            holder.itemView.txtAdd.setTextColor(ContextCompat.getColor(context, R.color.colorWhite));


        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.new_customer_post_adapter, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {

        if (list!!.size == 0) {
            return 1
        } else {
            return list!!.size
        }

    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        @SuppressLint("SetTextI18n")
        fun bind(postOnClick: PostOnClick, list: ArrayList<GetMyPost.DataBean.ItemBean>?) = with(itemView) {

        }

    }
}




