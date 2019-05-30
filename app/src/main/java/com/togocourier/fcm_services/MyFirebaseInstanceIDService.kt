package com.togocourier.fcm_services

import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.FirebaseInstanceIdService

class MyFirebaseInstanceIDService : FirebaseInstanceIdService() {
    private val TAG = "MyFirebaseIIDService"

    override fun onTokenRefresh() {
        val refreshedToken = FirebaseInstanceId.getInstance().token
    }
}