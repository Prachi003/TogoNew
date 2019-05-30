package com.togocourier.ui.fragment

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.adapter.NotificationAdapter
import com.togocourier.responceBean.NotificationBean
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_fragment_notification.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList

class NotificationFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var notifiactionAdapter: NotificationAdapter? = null
    private var notificationList = ArrayList<NotificationBean.DataBean>()
    private var userType: String = ""
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.new_fragment_notification, container, false)
        progress = ProgressDialog(context!!)
        notificationList(view)

        notifiactionAdapter = NotificationAdapter(activity!!, notificationList)
        view.recycler_view.adapter = notifiactionAdapter

        return view
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): NotificationFragment {
            val fragment = NotificationFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    private fun notificationList(view: View) {
        if (Constant.isNetworkAvailable(context!!, view.llMain)) {
            //  view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getNotificationListData,
                    Response.Listener { response ->
                        // view.progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")

                            if (status == "success") {
                                val gson = Gson()
                                val notificationBean = gson.fromJson(response, NotificationBean::class.java)
                                notificationList.addAll(notificationBean.data!!)
                                notifiactionAdapter?.notifyDataSetChanged()
                                view.no_data_found.visibility = View.GONE
                            } else {
                                view.no_data_found.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // view.progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                if (context != null) {
                                    val helper = HelperClass(context!!, activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("userId", PreferenceConnector.readString(context!!, PreferenceConnector.USERID, ""))

                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)

        } else{
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }/*else {
            //Constant.snackbar(view.llMain, getString(R.string.no_internet_connection))
            val snackbar = Snackbar.make(view.recycler_view, getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG)
            val sbView = snackbar.view
            val textView = sbView.findViewById<View>(android.support.design.R.id.snackbar_text) as TextView
            textView.setTextColor(Color.WHITE)
            textView.gravity = Gravity.CENTER
            snackbar.setActionTextColor(Color.WHITE)
            snackbar.show()
        }*/

    }
}
