package com.togocourier.adapter

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.togocourier.R
import com.togocourier.responceBean.Chat
import com.togocourier.util.PreferenceConnector
import kotlinx.android.synthetic.main.chat_left_side_view.view.*
import kotlinx.android.synthetic.main.chat_right_side_view.view.*
import kotlinx.android.synthetic.main.full_image_view_dialog.*
import java.text.SimpleDateFormat
import java.util.*

private val VIEW_TYPE_ME = 1
private val VIEW_TYPE_OTHER = 2

class ChattingAdapter(var mContext: Context, private var chatList: ArrayList<Chat>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var uid = PreferenceConnector.readString(mContext, PreferenceConnector.USERID, "")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var v: View? = null

        when (viewType) {
            VIEW_TYPE_ME -> {
                v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_right_side_view, parent, false)
                return MyChatViewHolder(v!!, mContext)
            }
            VIEW_TYPE_OTHER -> {
                v = LayoutInflater.from(parent?.context).inflate(R.layout.chat_left_side_view, parent, false)
                return OtherChatViewHolder(v!!, mContext)
            }
        }

        return OtherChatViewHolder(v!!, mContext)

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val chat = chatList[position]

        if (TextUtils.equals(chatList[position].uid, uid)) {
            holder as MyChatViewHolder
            holder.bind(chat)
        } else {
            holder as OtherChatViewHolder
            holder.bind1(chat)

        }


    }

    override fun getItemCount(): Int {
        return chatList.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (TextUtils.equals(chatList[position].uid, uid)) {
            VIEW_TYPE_ME
        } else {
            VIEW_TYPE_OTHER
        }
    }

    class MyChatViewHolder(itemView: View, var mContext: Context) : RecyclerView.ViewHolder(itemView) {
        fun bind(chat: Chat) = with(itemView) {
            val sfd = SimpleDateFormat("hh:mm a", Locale.US)
            val sim = SimpleDateFormat("MM/dd/yyyy", Locale.US)
            try {
                val date_str = sim.format(Date(chat.timestamp as Long)).trim()
                val currentDate = sim.format(Calendar.getInstance().time).trim()

                if (date_str == currentDate) {
                    itemView.time_ago_.text = sfd.format(Date(chat.timestamp as Long))
                } else {
                    itemView.time_ago_.text = date_str
                }
            } catch (ex: IllegalArgumentException) {

            }

            if (chat.message.startsWith("https://firebasestorage.googleapis.com/") || chat.message.startsWith("content://")) {
                Glide.with(mContext).load(chat.message).apply(RequestOptions().placeholder(R.drawable.chat_image_placeholder)).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: com.bumptech.glide.request.target.Target<Drawable>, isFirstResource: Boolean): Boolean {
                        img_progress_right.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: com.bumptech.glide.request.target.Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        img_progress_right.visibility = View.GONE
                        return false
                    }
                }).into(itemView.chat_my_image_view)

                itemView.rl_right_image_view.visibility = View.VISIBLE
                itemView.my_message.visibility = View.GONE
            } else {
                itemView.my_message.visibility = View.VISIBLE
                itemView.my_message.text = chat.message
                itemView.rl_right_image_view.visibility = View.GONE
            }

            itemView.rl_right_image_view.setOnClickListener {
                full_screen_photo_dialog(chat.message)
            }

        }

        private fun full_screen_photo_dialog(image_url: String) {
            val openDialog = Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            openDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
            openDialog.setContentView(R.layout.full_image_view_dialog)
            if (image_url != "") {
                Glide.with(mContext).load(image_url).apply(RequestOptions().placeholder(R.drawable.chat_image_placeholder)).into(openDialog.photo_view)
            }
            openDialog.iv_back_dialog.setOnClickListener {
                openDialog.dismiss()
            }

            openDialog.show()
        }

    }

    class OtherChatViewHolder(itemView: View, var mContext: Context) : RecyclerView.ViewHolder(itemView) {
        fun bind1(chat: Chat) = with(itemView) {
            val sfd = SimpleDateFormat("hh:mm a", Locale.US)

                val sim = SimpleDateFormat("MM/dd/yyyy", Locale.US)
                try {
                    val date_str = sim.format(Date(chat.timestamp as Long)).trim()
                    val currentDate = sim.format(Calendar.getInstance().time).trim()

                    if (date_str == currentDate) {
                        itemView.time_ago.text = sfd.format(Date(chat.timestamp as Long))
                    } else {
                        itemView.time_ago.text = date_str
                    }
                } catch (ex: IllegalArgumentException) {

                }




            if (chat.message.startsWith("https://firebasestorage.googleapis.com/") || chat.message.startsWith("content://")) {
                Glide.with(mContext).load(chat.message).apply(RequestOptions().placeholder(R.drawable.chat_image_placeholder)).listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(e: GlideException?, model: Any, target: com.bumptech.glide.request.target.Target<Drawable>, isFirstResource: Boolean): Boolean {
                        img_progress_left.visibility = View.GONE
                        return false
                    }

                    override fun onResourceReady(resource: Drawable, model: Any, target: com.bumptech.glide.request.target.Target<Drawable>, dataSource: DataSource, isFirstResource: Boolean): Boolean {
                        img_progress_left.visibility = View.GONE
                        return false
                    }
                }).into(itemView.chat_other_image_view)

                itemView.rl_left_image_view.visibility = View.VISIBLE
                itemView.other_message.visibility = View.GONE
            } else {
                itemView.other_message.visibility = View.VISIBLE
                itemView.other_message.text = chat.message
                itemView.rl_left_image_view.visibility = View.GONE
            }

            itemView.rl_left_image_view.setOnClickListener {
                full_screen_photo_dialog(chat.message)
            }
        }

        private fun full_screen_photo_dialog(image_url: String) {
            val openDialog = Dialog(mContext, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
            openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            openDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
            openDialog.setContentView(R.layout.full_image_view_dialog)
            if (image_url != "") {
                Glide.with(mContext).load(image_url).apply(RequestOptions().placeholder(R.drawable.chat_image_placeholder)).into(openDialog.photo_view)
            }
            openDialog.iv_back_dialog.setOnClickListener {
                openDialog.dismiss()
            }

            openDialog.show()
        }
    }


}