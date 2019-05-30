package com.togocourier.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Created by chiranjib on 28/11/17.
 */
 object  PreferenceConnector {
    val PREF_NAME = "TOGO"
    val MODE = Context.MODE_PRIVATE
    val USERTYPE = "USERTYPE"
    val POSTID = "POSTID"
    val USERID = "USERID"
    val USEREMAIL = "USEREMAIL"
    val USERSOCIALID = "USERSOCIALID"
    val USERSOCIALTYPE = "USERSOCIALTYPE"
    val USERPROFILEIMAGE = "USERPROFILEIMAGE"
    val USERFULLNAME = "USERFULLNAME"
    val USERCOUNTRYCODE = "USERCOUNTRYCODE"
    val USERCONTACTNO = "USERCONTACTNO"
    val USERADDRESS = "USERADDRESS"
    val USERLATITUTE = "USERLATITUTE"
    val USERLONGITUTE = "USERLONGITUTE"
    val USERAUTHTOKEN = "USERAUTHTOKEN"
    val PASSWORD = "PASSWORD"
    val ADDBANK = "ADDBANK"
    val ISLOGIN = "ISLOGIN"
    val ISNOTIFICATIONON = "ISNOTIFICATIONON"
    val RATTING = "RATTING"



    fun getEditor(context: Context): SharedPreferences.Editor {
        return getPreferences(context).edit()
    }

    fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, MODE)
    }


    fun clear(context: Context) {
        getEditor(context).clear().commit()
    }

    fun writeInteger(context: Context, key: String, value: Int) {
        getEditor(context).putInt(key, value).commit()
    }

    fun readInteger(context: Context, key: String, defValue: Int): Int {
        return getPreferences(context).getInt(key, defValue)
    }

    fun writeString(context: Context, key: String, value: String) {
        getEditor(context).putString(key, value).commit()
    }

    fun readString(context: Context, key: String, defValue: String): String {
        return getPreferences(context).getString(key, defValue)
    }

    fun writeBoolean(context: Context, key: String, value: Boolean) {
        getEditor(context).putBoolean(key, value)
    }

    fun readBoolean(context: Context, key: String, defValue: Boolean): Boolean {
        return getPreferences(context).getBoolean(key, defValue)
    }
}
