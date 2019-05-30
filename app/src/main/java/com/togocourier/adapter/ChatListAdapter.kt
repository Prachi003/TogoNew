package com.togocourier.adapter

import android.content.Context
import android.content.Intent
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.togocourier.R
import com.togocourier.responceBean.Chat
import com.togocourier.ui.activity.ChatActivity
import com.togocourier.util.TimeAgo
import kotlinx.android.synthetic.main.chat_list_item.view.*
import java.util.*

class ChatListAdapter(var mContext: Context, private var histortList: ArrayList<Chat>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val chat = histortList[position]
        holder.bind(chat)
    }

    override fun getItemCount(): Int {
        return histortList.size
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_list_item, parent, false)
        return ViewHolder(v)
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(chat: Chat) = with(itemView) {

            if (chat.message.startsWith("https://firebasestorage.googleapis.com/")) {
                itemView.last_meassage.text = resources.getString(R.string.image)
            } else {
                itemView.last_meassage.text = chat.message
            }

            if (chat.messageCount != 0) {
                if (chat.messageCount <= 99) {
                    itemView.tv_message_count.text = chat.messageCount.toString()
                } else {
                    itemView.tv_message_count.text = resources.getString(R.string.limit_chat_count)
                }
                itemView.rl_message_count.visibility = View.VISIBLE
            } else {
                itemView.rl_message_count.visibility = View.GONE
            }

            itemView.chat_title.text = chat.title
            itemView.opponent_name.text = chat.name!!.substring(0, 1).toUpperCase() + chat.name!!.substring(1)

            if (chat.profilePic != "") {
                Glide.with(context).load(chat.profilePic).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(itemView.image_opponents)
            }

            itemView.setOnClickListener {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra("otherUID", chat.uid)
                intent.putExtra("title", chat.title)
                context.startActivity(intent)
            }

            if (chat.timestamp != null) {
                itemView.date_time.text = TimeAgo.toRelative(chat.timestamp as Long, Calendar.getInstance().timeInMillis)
            }
        }

    }


}