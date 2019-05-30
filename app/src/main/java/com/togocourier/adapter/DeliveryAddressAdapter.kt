package com.togocourier.adapter

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.togocourier.Interface.getAddressListner
import com.togocourier.R
import com.togocourier.responceBean.DeliverAddressInfo
import kotlinx.android.synthetic.main.address_item_layout.view.*

class DeliveryAddressAdapter(private var matchTxt:String, private var addressList:ArrayList<DeliverAddressInfo.DataBean>,
                             var address:getAddressListner) : RecyclerView.Adapter<RecyclerView.ViewHolder> (){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.address_item_layout,parent,false)
        return ViewHolder(v,address)
    }

    override fun getItemCount(): Int {
       return addressList.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        holder.bind(addressList,position)


       if(matchTxt == addressList[position].deliveryAdrs){
           addressList[position].isCheckedItem = true

       }

        holder.itemView.checkbox.isChecked = addressList[position].isCheckedItem

        holder.itemView.setOnClickListener {
            for (value in addressList) {
                value.isCheckedItem = false
            }
            addressList[position].isCheckedItem = true
            address.getAddress(addressList[position].deliveryAdrs.toString(), addressList[position].deliverLat.toString(), addressList[position].deliverLong.toString())
            notifyDataSetChanged()
        }
    }


    class ViewHolder(itemView: View, var address: getAddressListner) : RecyclerView.ViewHolder(itemView){
        fun bind(addressList: ArrayList<DeliverAddressInfo.DataBean>, position: Int) = with(itemView){
            itemView.tv_address.text = addressList[position].deliveryAdrs
            itemView.checkbox.isChecked = addressList[position].isCheckedItem
        }
    }
}