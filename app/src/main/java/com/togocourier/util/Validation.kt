package com.togocourier.util

import android.widget.EditText
import android.widget.TextView
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Created by chiranjib on 28/11/17.
 */
class Validation {
    private val USERNAME_PATTERN = "^[a-z0-9_-]{3,15}$"
    private val FULLNAME_PATTERN = "^[\\p{L} .'-]+$"
    private val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"

    //  private val NAME_PATTERN = "^[A-Za-z]+\$"
    private val NAME_PATTERN = "^[\\p{L} .'-]+$"

    private var pattern: Pattern? = null
    private var matcher: Matcher? = null

    private fun getString(textView: TextView): String {
        return textView.text.toString().trim { it <= ' ' }
    }

    // "use isEmpty instead of the isNullValue"
    fun isNullValue(textView: TextView): Boolean {
        if (getString(textView).isEmpty()) {
            // textView.setError("Field cannot be empty");
            return false
        }
        textView.error = null
        return true
    }

    fun isEmpty(textView: TextView): Boolean {
        if (getString(textView).isEmpty()) {
            //textView.setError("field can't be empty");
            textView.requestFocus()
            return true
        }
        return false
    }

    fun isNotZero(editText: EditText): Boolean {
        if (editText.text.toString().toDouble() <= 0) {
            editText.requestFocus()
            return true
        }

        return false
    }

    fun isValidValue(editText: EditText): Boolean {
        if (editText.text.toString().equals(".")) {
            editText.requestFocus()
            return true
        }

        return false
    }

    fun isEmptyN(textView: TextView): Boolean {
        if (getString(textView).isEmpty()) {
            textView.requestFocus()
            return true
        }
        return false
    }

    fun isValidName(editText: EditText): Boolean {
        val pattern = Pattern.compile(NAME_PATTERN, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(getString(editText))
        val bool = matcher!!.matches()
        if (!bool) {
            editText.requestFocus()
            return true
        }
        return false
    }

    fun isUserNameValid(textView: TextView): Boolean {
        pattern = Pattern.compile(USERNAME_PATTERN)
        matcher = pattern!!.matcher(getString(textView))
        val bool = matcher!!.matches()
        if (!bool) {
            textView.error = "enter valid userName"
            textView.requestFocus()
        }
        return bool
    }

    fun isFullNameValid(textView: TextView): Boolean {
        val pattern = Pattern.compile(FULLNAME_PATTERN, Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(getString(textView))
        return matcher.find()
    }

    fun isPassword_Valid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }// trm white space
        return getValue.length >= 6

    }

    fun isPasswordValid(textView: TextView): Boolean {
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern!!.matcher(getString(textView))
        val bool = matcher!!.matches()
        if (!bool) {
            textView.error = "At least 4 characters required"
            textView.requestFocus()
        }
        return bool
    }

    fun isEmailValid(textView: TextView): Boolean {
        val bool = android.util.Patterns.EMAIL_ADDRESS.matcher(getString(textView)).matches()
        if (!bool) {
            // textView.setError("enter valid email");
            textView.requestFocus()
        }
        return bool
    }
}