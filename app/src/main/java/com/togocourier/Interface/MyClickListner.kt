package com.togocourier.Interface

import android.app.Dialog

interface MyClickListner {

    fun getPassword(oldPassword: String, newPassword: String, openDialog: Dialog)
}