package com.togocourier.ui.activity

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.EditText
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.togocourier.R
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.ProgressDialog
import com.togocourier.util.Validation
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_activity_forgot_pass.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class ForgotPassActivity : AppCompatActivity() {
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_forgot_pass)

        progress = ProgressDialog(this)

        sendBtn.setOnClickListener {
            if (isValidData()) {
                forgotPassword()
            }
        }

        iv_back.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }
    }


    private fun forgotPassword() {
        if (Constant.isNetworkAvailable(this@ForgotPassActivity, mainLayout)) {
            //  progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.forgotPassword,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                Constant.snackbar(mainLayout, message)
                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()
                            } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }

                        //  Toast.makeText(this@ForgotPassActivity, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email", emailTxt.text.toString())
                    return params
                }
            }

            stringRequest.retryPolicy = DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)

        }else{
           Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isValidData(): Boolean {
        val v = Validation()
        if (v.isEmpty(emailTxt)) {
            Constant.snackbar(mainLayout, "Email address can't be empty")
            emailTxt.requestFocus()
            return false
        } else if (!isEmailValid(emailTxt)) {
            Constant.snackbar(mainLayout, "Enter valid email")
            emailTxt.requestFocus()
            return false
        }
        return true
    }

    private fun isEmailValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()
    }
}
