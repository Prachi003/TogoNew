package com.togocourier.adapter

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.togocourier.Interface.PayCardAddDelClick
import com.togocourier.R
import com.togocourier.responceBean.CardPaymentListBean
import kotlinx.android.synthetic.main.new_item_add_card_list.view.*

class NewAddCardListAdapter (var context: Context?, arrayList: ArrayList<CardPaymentListBean.DataBean>?, private var myClick: PayCardAddDelClick) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var list = arrayList

    override fun getItemCount(): Int {
        return list!!.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.new_item_add_card_list, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val cardData = list?.get(position)
        holder.bind(cardData!!, myClick)

        holder.itemView.delete_card.setOnClickListener {
            myClick.deleteMyPost(cardData.cardId!!,position)
        }
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(cardData: CardPaymentListBean.DataBean, myClick: PayCardAddDelClick) = with(itemView) {
            delete_card.visibility = View.VISIBLE
            swipe_layout.isSwipeEnabled = true

            itemView.tv_card_type.text = cardData.cardType

            val temp: String = cardData.cardNumber.toString()
            if (temp != "") {
                val output = temp.substring(temp.length - 4)
                itemView.tv_card_number.text = output
            }

            itemView.main_click.setOnClickListener {
                myClick.GetPosition(adapterPosition)
            }
        }

    }
}