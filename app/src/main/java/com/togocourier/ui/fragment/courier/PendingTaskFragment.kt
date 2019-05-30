package com.togocourier.ui.fragment.courier

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.MyTaskListOnClick
import com.togocourier.R
import com.togocourier.adapter.MyTAskAdapter
import com.togocourier.responceBean.MyAllTaskResponce
import com.togocourier.ui.phase3.activity.PendingCostumerDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_pending_task.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList

class PendingTaskFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null

    private var mContext: Context? = null
    private var gson = Gson()
    lateinit var coordinateLay: CoordinatorLayout
    private var myTaskList: ArrayList<MyAllTaskResponce.DataBean>? = null
    private var adpter: MyTAskAdapter? = null
    private var progress: ProgressDialog? = null
    var myPostResponce=MyAllTaskResponce.DataBean()
    var myPostResponceN=MyAllTaskResponce.DataBean.ItemBean()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_pending_task, container, false)
        initializeView(view)
        return view


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.isClickable = true
        view.requestFocus()


    }

    private fun initializeView(view: View) {
        progress = ProgressDialog(context!!)
        myTaskList = ArrayList()
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        val layoutManager = LinearLayoutManager(mContext)
        myTaskList = ArrayList()
        view.mPnTaskReclr.layoutManager = layoutManager

        adpter = MyTAskAdapter(mContext, myTaskList, object : MyTaskListOnClick {
            override fun OnClick(postId: String, requestId: String, position: Int) {
                val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("itembean",myPostResponceN)
                intent.putExtra("userId","")
                intent.putExtra("FROM", "Courier")
                intent.putExtra("REQUESTID", requestId)
                startActivity(intent)
            }

        },"pending")
        view.mPnTaskReclr.adapter = adpter

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): PendingTaskFragment {
            val fragment = PendingTaskFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onResume() {
        super.onResume()
        getMyTaskRequest(view!!)
    }


    private fun getMyTaskRequest(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, view.mainLayout)) {
          //  view.progressBar.visibility = View.VISIBLE
            progress!!.show()
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_All_Request_Url+"?userId="+PreferenceConnector.readString(mContext!!, PreferenceConnector.USERID, "")+"&latitude&longitude&status=accept",
                    Response.Listener { response ->
                       /* if (view.progressBar != null) {
                            view.progressBar.visibility = View.GONE
                        }*/

                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val message = result.getString("message")
                            if(message.equals("Invalid token")){
                                val helper = HelperClass(context!!, activity!!)
                                helper.sessionExpairDialog()
                            }else{
                                val status = result.getString("status")
                                myTaskList!!.clear()
                                if (status == "success") {
                                    val data = result.getJSONArray("data")
                                    for (i in 0..data!!.length() - 1) {
                                        val datajson = data.getJSONObject(i)
                                        myPostResponce = gson.fromJson(datajson.toString(), MyAllTaskResponce.DataBean::class.java)
                                        myTaskList!!.add(myPostResponce)
                                        val item = datajson.getJSONArray("item")
                                        for (j in 0..item!!.length() - 1) {
                                            val itemjson = item.getJSONObject(j)
                                            myPostResponceN = gson.fromJson(itemjson.toString(), MyAllTaskResponce.DataBean.ItemBean::class.java)

                                        }

                                    }
                                    /* val allrequeatListResponce = gson.fromJson(response, MyAllTaskResponce::class.java)
                                     myTaskList?.clear()
                                     myTaskList?.addAll(allrequeatListResponce.getAppliedReqData()!!)*/
                                    adpter?.notifyDataSetChanged()
                                } else {

                                    myTaskList?.clear()
                                    adpter?.notifyDataSetChanged()

                                    val userType = PreferenceConnector.readString(mContext!!, PreferenceConnector.USERTYPE, "")
                                    if (userType == Constant.COURIOR) {
                                        if (message == "Currently you are inactivate user") {
                                            val helper = HelperClass(mContext!!, activity!!)
                                            helper.inActiveByAdmin("Admin inactive your account", true)
                                        } else {
                                            view.noDataTxt.visibility = View.VISIBLE
                                        }
                                    } else {
                                        view.noDataTxt.visibility = View.VISIBLE
                                    }
                                }


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
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                /*override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("userType", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERTYPE, ""))
                    params.put("requestStatus", "pending")
                    params.put("postId", "")
                    return params
                }*/
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(mContext!!).addToRequestQueue(stringRequest)
        }else{
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }
}
