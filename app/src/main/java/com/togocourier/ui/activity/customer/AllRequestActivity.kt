package com.togocourier.ui.activity.customer

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.AcceptRejectListioner
import com.togocourier.R
import com.togocourier.adapter.AllRequestAdapter
import com.togocourier.responceBean.AllRequestListResponce
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_all_request.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList

class AllRequestActivity : AppCompatActivity(), View.OnClickListener {

    private var postId = ""
    private var userType = ""
    private var adapter: AllRequestAdapter? = null
    private var allRequestList: ArrayList<AllRequestListResponce.AppliedReqDataBean>? = null
    private var gson = Gson()
    private var progress: ProgressDialog? = null

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_request)

        progress = ProgressDialog(this)
        val bundle = intent.extras
        postId = bundle!!.getString("POSTID")
        userType = PreferenceConnector.readString(this@AllRequestActivity, PreferenceConnector.USERTYPE, "")
        allRequestList = ArrayList()

        adapter = AllRequestAdapter(this@AllRequestActivity,
                allRequestList as ArrayList<AllRequestListResponce.AppliedReqDataBean>, object : AcceptRejectListioner {
            override fun OnClickUserId(userId: String, postUserId: String?) {

            }

            override fun OnClick(id: String, status: String, bitPrice: String, applyUserId: String?) {
                if (status == "cancel") {
                    acceptRejectRequest(id, status)
                } else if (status == "accept") {
                    val intent = Intent(this@AllRequestActivity, NewPaymentListActivity::class.java)
                    intent.putExtra("requestId", id)
                    intent.putExtra("bitPrice", bitPrice)
                    intent.putExtra("tipPrice", "")
                    intent.putExtra("paymentType", "normal")
                    intent.putExtra("postId", postId)
                    startActivity(intent)
                }
            }
        })
        val layoutManager = GridLayoutManager(this, 2)
        requestPstReclr.layoutManager = layoutManager
        requestPstReclr.adapter = adapter
        getAllRequest()

        backBtn.setOnClickListener(this)

    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.backBtn -> {
                finish()
            }
        }
    }

    private fun getAllRequest() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Get_All_Request_Url,
                    Response.Listener { response ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status.equals("return")) {
                                Constant.returnAlertDialogToMainActivity(this@AllRequestActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                val allrequeatListResponce = gson.fromJson(response, AllRequestListResponce::class.java)
                                allRequestList?.clear()
                                allRequestList?.addAll(allrequeatListResponce.getAppliedReqData()!!)
                                adapter?.notifyDataSetChanged()
                            } else {
                                allRequestList?.clear()
                                adapter?.notifyDataSetChanged()

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    noDataTxt.visibility = View.VISIBLE
                                }
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

                        //  Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@AllRequestActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("userType", userType)
                    params.put("requestStatus", "pending")
                    params.put("postId", postId)
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


    private fun acceptRejectRequest(id: String, status: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Accept_reject_Request_Url,
                    Response.Listener { response ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status.equals("return")) {
                                Constant.returnAlertDialogToMainActivity(this@AllRequestActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                getAllRequest()
                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(mainLayout, message)
                                    }
                                } else {
                                    Constant.snackbar(mainLayout, message)
                                }


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

                        //  Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@AllRequestActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("requestId", id)
                    params.put("requestStatus", status)
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

}


