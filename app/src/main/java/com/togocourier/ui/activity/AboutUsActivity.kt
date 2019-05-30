package com.togocourier.ui.activity

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Layout.JUSTIFICATION_MODE_INTER_WORD
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
import kotlinx.android.synthetic.main.activity_about_us.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class AboutUsActivity : AppCompatActivity() {
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)
        progress = ProgressDialog(this)
        aboutUs()

        back.setOnClickListener {
            onBackPressed()
        }
    }


    private fun aboutUs() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getAboutusContent,
                    Response.Listener { response ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            val jsonobjest = result.getJSONObject("result")
                            if (status == "success") {
                                val content = jsonobjest.getString("content")

                               // text.justificationMode = JUSTIFICATION_MODE_INTER_WORD
                                text.text = content

                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    }, Response.ErrorListener { error ->
                //   progressBar.visibility = View.GONE
                progress!!.dismiss()

                val networkResponse = error.networkResponse
                if (networkResponse != null) {
                    if (networkResponse.statusCode == 300) {
                        val helper = HelperClass(this, this)
                        helper.sessionExpairDialog()
                    }
                }
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@AboutUsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }
            }

            stringRequest.retryPolicy = DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
        }else{
           Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }
}
