package com.togocourier.fcm_services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.togocourier.R.color
import com.togocourier.R.drawable
import com.togocourier.ui.activity.ChatActivity
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.util.Constant

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage?) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage?.data

        val type = data?.get("type")
        if (type.equals("chat")) {
            if (Constant.ChatOpponentId != data?.get("uid")) {
                sendChatNotification(data!!)
            }
        } else {
            sendNotification(data!!)
        }

    }

    private fun sendNotification(data: Map<String, String>) {
        val type = data["type"]
        val message = data["body"]
        val postId = data["reference_id"]
        val requestId = data["requestId"]

        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("type", type)
        intent.putExtra("reference_id", postId)
        intent.putExtra("requestId", requestId)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val CHANNEL_ID = "my_channel_01"// The id of the channel.
        val name = "Abc"// The user-visible name of the channel.
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel: NotificationChannel

        val iUniqueId = (System.currentTimeMillis() and 0xfffffff).toInt()

        val pendingIntent = PendingIntent.getActivity(applicationContext, iUniqueId, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(drawable.new_noti_logo_img)
                .setContentTitle("TOGO")
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(applicationContext, color.new_app_color))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(mChannel)

        }
        notificationManager.notify(iUniqueId, notificationBuilder.build())
    }

    private fun sendChatNotification(data: Map<String, String>) {
        val chatTitle = data["username"]
        val title = data["title"]
        var message = data["text"]
        if (message!!.startsWith("https://firebasestorage.googleapis.com/") || message.startsWith("content://")) {
            message = "Image"
        }
        val opponentId = data["uid"]

        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("otherUID", opponentId)
        intent.putExtra("title", title)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val CHANNEL_ID = "my_channel_01"// The id of the channel.
        val name = "Abc"// The user-visible name of the channel.
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel: NotificationChannel

        val iUniqueId = (System.currentTimeMillis() and 0xfffffff).toInt()

        val pendingIntent = PendingIntent.getActivity(applicationContext, iUniqueId, intent, PendingIntent.FLAG_ONE_SHOT)
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(drawable.new_noti_logo_img)
                .setContentTitle(chatTitle)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setColor(ContextCompat.getColor(applicationContext, color.new_app_color))
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            notificationManager.createNotificationChannel(mChannel)
        }

        notificationManager.notify(iUniqueId, notificationBuilder.build())
    }
}