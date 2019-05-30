package com.togocourier.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.togocourier.R
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_term_condition.*
import org.json.JSONException
import org.json.JSONObject
import android.webkit.WebSettings
import android.graphics.Bitmap
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import com.facebook.internal.Utility


class TermConditionActivity : AppCompatActivity() {
    private var URL = Constant.baseWebViewUrl
    var termcondition=""

    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_term_condition)
        progress = ProgressDialog(this)

        back.setOnClickListener {
            onBackPressed()
        }
        getTermsandCondition()

        //  progressBar.visibility = View.VISIBLE
       // progress!!.show()



        back.setOnClickListener {
            onBackPressed()
        }
    }



    private fun getTermsandCondition() {
        if (Constant.isNetworkAvailable(this, terms_condition)) {
            //  list_progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.baseWebViewUrl,
                    Response.Listener { response ->
                        //  list_progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val JsonObject=result.optJSONObject("result")
                                 termcondition=JsonObject.getString("termcondition")
                                loadUrl(termcondition)


                            } else {

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
/*
                                        no_list_data_found.visibility = View.VISIBLE
                                        list_recycler_view.visibility = View.GONE
*/
                                    }
                                } else {
/*
                                    no_list_data_found.visibility = View.VISIBLE
                                    list_recycler_view.visibility = View.GONE
*/
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        //  list_progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()
                            }
                        }
                    }) {



            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrl(termcondition: String) {


        try {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(termcondition)
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this,"",Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this,"",Toast.LENGTH_SHORT).show()
        }

    }

    private inner class CustomWebViewClient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            view.loadUrl(url)
            return true
        }
    }
}
