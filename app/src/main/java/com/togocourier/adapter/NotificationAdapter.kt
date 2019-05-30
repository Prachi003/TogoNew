package com.togocourier.adapter

import android.content.Intent
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.RecyclerView
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.togocourier.R
import com.togocourier.responceBean.NotificationBean
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.activity.courier.NewCourierPostDetailsActivity
import com.togocourier.ui.activity.customer.NewCustomerPostDetailsActivity
import com.togocourier.ui.phase3.activity.CostumerNewPostDetailActivity
import com.togocourier.ui.phase3.activity.PendingCostumerDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import kotlinx.android.synthetic.main.new_notification_item.view.*

class NotificationAdapter(var activity: FragmentActivity, private var notificationList: ArrayList<NotificationBean.DataBean>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        holder as ViewHolder
        val listItem = notificationList[position]
        holder.bind(listItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val v: View = LayoutInflater.from(parent.context).inflate(R.layout.new_notification_item, parent, false)
        return ViewHolder(v, activity)
    }

    override fun getItemCount(): Int {
        return notificationList.size
    }

    class ViewHolder(itemView: View, var activity: FragmentActivity) : RecyclerView.ViewHolder(itemView) {

        fun bind(notificationItem: NotificationBean.DataBean) = with(itemView) {
            itemView.day_ago.text = notificationItem.showtime

            val nameArray = notificationItem.fullName!!.split(" ")
            val builder = StringBuilder()
            nameArray
                    .map { it.substring(0, 1).toUpperCase() + it.substring(1) }
                    .forEach { builder.append(it + " ") }

            val sourceString: String
            if (notificationItem.notiMsg!!.startsWith(notificationItem.fullName!!)) {
                val message = notificationItem.notiMsg!!.split(notificationItem.fullName!!)

                sourceString = when {
                    message.size > 2 -> "<b>" + builder.toString() + "</b> " + message[1] + notificationItem.fullName + message[2]
                    message.size > 1 -> "<b>" + builder.toString() + "</b> " + message[1]
                    else -> "<b>" + builder.toString() + "</b> " + message[0]
                }
                itemView.noti_msg.text = Html.fromHtml(sourceString)
            } else {
                itemView.noti_msg.text = notificationItem.notiMsg
            }


            if (!notificationItem.profileImage.equals("")) {
                Glide.with(context).load(notificationItem.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(itemView.profile_img)
            }

            itemView.setOnClickListener {
                val type = notificationItem.type
                val postId = notificationItem.postId
                val requestId = notificationItem.requestId
                val userType = PreferenceConnector.readString(context, PreferenceConnector.USERTYPE, "")

                if (type != null) {
                    if (type == "sendrequest") {
                        if (userType == Constant.CUSTOMER) {
                            val intent = Intent(activity, CostumerNewPostDetailActivity::class.java)
                            intent.putExtra("FROM", Constant.pendingPost)
                            intent.putExtra("userId", "")
                            intent.putExtra("POSTID", postId)
                            context.startActivity(intent)


                        } else if (userType == Constant.COURIOR) {
                            val intent = Intent(context, NewCourierPostDetailsActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("FROM", Constant.newPost)
                            intent.putExtra("REQUESTID", "")
                            context.startActivity(intent)
                        }

                    } else if (type == "deliverScreen") {
                        if (userType == Constant.CUSTOMER) {
                            val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                            intent.putExtra("FROM", Constant.pendingPost)
                            intent.putExtra("userId", "")
                            intent.putExtra("POSTID", postId)
                            context.startActivity(intent)


                        } else if (userType == Constant.COURIOR) {
                            val intent = Intent(context, NewCourierPostDetailsActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("FROM", Constant.pendingPost)
                            intent.putExtra("REQUESTID", requestId)
                            context.startActivity(intent)
                        }

                    } else if (type == "updateScreen") {
                        if (userType == Constant.CUSTOMER) {
                            val intent = Intent(context, NewCustomerPostDetailsActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("FROM", Constant.pendingTask)
                            intent.putExtra("REQUESTID", requestId)
                            context.startActivity(intent)

                        } else if (userType == Constant.COURIOR) {
                            val intent = Intent(context, NewCourierPostDetailsActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("FROM", Constant.pendingTask)
                            intent.putExtra("REQUESTID", requestId)
                            context.startActivity(intent)
                        }

                    } else if (type == "addpost") {
                        val intent = Intent(activity, CostumerNewPostDetailActivity::class.java)
                        intent.putExtra("POSTID", postId)
                        intent.putExtra("userId","")
                        intent.putExtra("FROM", "courierlist")
                        activity.startActivity(intent)

                    } else if (type == "review") {
                        if (userType == Constant.CUSTOMER) {
                            val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                            intent.putExtra("POSTID", id)
                            intent.putExtra("userId","")
                            intent.putExtra("REQUESTID", requestId)
                            intent.putExtra("FROM", "couriercompleted")
                            activity.startActivity(intent)


                        } else if (userType == Constant.COURIOR) {
                            val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("userId","")

                            intent.putExtra("FROM", "Courier")
                            intent.putExtra("REQUESTID", requestId)
                            activity.startActivity(intent)
                        }

                    } else if (type == "tip") {
                        if (userType == Constant.CUSTOMER) {
                            val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("userId", "")

                            intent.putExtra("FROM", "Costumer")
                            intent.putExtra("REQUESTID", requestId)
                            activity.startActivity(intent)

                        } else if (userType == Constant.COURIOR) {
                            val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("itembean", "")
                            intent.putExtra("userId", "")
                            intent.putExtra("FROM", "Courier")
                            intent.putExtra("REQUESTID", requestId)
                            activity.startActivity(intent)
                        }
                    }else if(type=="accept"){
                        if (userType==Constant.COURIOR){
                            val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                            intent.putExtra("POSTID", postId)
                            intent.putExtra("itembean","")
                            intent.putExtra("userId","")
                            intent.putExtra("FROM", "Courier")
                            intent.putExtra("REQUESTID", requestId)
                            activity.startActivity(intent)
                        }
                    }

                }
            }
        }
    }
}