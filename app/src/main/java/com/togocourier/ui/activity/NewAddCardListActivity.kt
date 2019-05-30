package com.togocourier.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.GetAdpaterPosition
import com.togocourier.Interface.PayCardAddDelClick
import com.togocourier.R
import com.togocourier.adapter.NewAddCardListAdapter
import com.togocourier.responceBean.CardPaymentListBean
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_activity_add_card_list.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList

class NewAddCardListActivity : AppCompatActivity(), View.OnClickListener {
    // variable to track event time
    private var mLastClickTime: Long = 0

    private var adapter: NewAddCardListAdapter? = null
    private var cardList = ArrayList<CardPaymentListBean.DataBean>()

    private var gson = Gson()
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_add_card_list)

        progress = ProgressDialog(this)
        cardList = ArrayList()

        getCardPaymentList()

        adapter = NewAddCardListAdapter(this, cardList, object : PayCardAddDelClick {
            override fun deleteMyPost(postId: String, position: Int) {
                //delete Api calling
                deleteCardApi( postId, position)
            }

            override fun GetPosition(position: Int) {
/*
                val data = cardList[position]
                val intent = Intent(this@NewAddCardListActivity, NewAddCardActivity::class.java)
                intent.putExtra("cardDetails", data)
                startActivity(intent)
                finish()
*/
            }

        })
        list_recycler_view.adapter = adapter
        list_recycler_view.visibility = View.GONE

        iv_list_back.setOnClickListener(this)
        add_new_card_btn.setOnClickListener(this)
    }

    private fun deleteCardApi(postId: String, position: Int) {
        if (Constant.isNetworkAvailable(this, add_list_layout)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deleteCardById,
                    Response.Listener { response ->
                        //  view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                cardList.removeAt(position)
                                adapter?.notifyDataSetChanged()
                                getCardPaymentList()

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    }
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        //  view.progressBar?.visibility = View.GONE
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
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCardListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("cardId", postId)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
        }else{
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onClick(view: View?) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        if (view != null) {
            when (view.id) {
                R.id.iv_list_back -> {
                    onBackPressed()
                }

                R.id.add_new_card_btn -> {
                    val intent = Intent(this@NewAddCardListActivity, NewAddCardActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }

    private fun getCardPaymentList() {
        if (Constant.isNetworkAvailable(this, add_list_layout)) {
            //  list_progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL +  Constant.userCardPaymentList,
                    Response.Listener { response ->
                        //  list_progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val JsonArray=result.getJSONArray("data")

                                for (i in 0..JsonArray!!.length() - 1){
                                    val jsonObject=JsonArray.getJSONObject(i)
                                    val cardListResponse = gson.fromJson(jsonObject.toString(), CardPaymentListBean.DataBean::class.java)
                                    cardList.add(cardListResponse)
                                    adapter?.notifyDataSetChanged()

                                }

                                no_list_data_found.visibility = View.GONE
                                list_recycler_view.visibility = View.VISIBLE

                            } else {
                                cardList.clear()
                                adapter?.notifyDataSetChanged()

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        no_list_data_found.visibility = View.VISIBLE
                                        list_recycler_view.visibility = View.GONE
                                    }
                                } else {
                                    no_list_data_found.visibility = View.VISIBLE
                                    list_recycler_view.visibility = View.GONE
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

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCardListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    return params
                }


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
}