package com.togocourier.ui.fragment.customer

import android.content.Intent
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
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
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.adapter.MyCompletedPostAdapter
import com.togocourier.responceBean.MyPostResponce
import com.togocourier.ui.phase3.activity.PendingCostumerDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_completed_post.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.MutableMap


class CompletedPostFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    lateinit var coordinateLay: CoordinatorLayout
    private var myCompletedPostAdapter: MyCompletedPostAdapter? = null
    private var completedList = ArrayList<MyPostResponce.DataBean>()
    private var userType = ""
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_completed_post, container, false)

        progress = ProgressDialog(context!!)
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
        myCompletedPostAdapter = MyCompletedPostAdapter(context, completedList, object : MyOnClick {
            override fun OnClickItem(postId: String, userId: String?, position: Int) {
                val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                intent.putExtra("userId",completedList[position].userId)
                intent.putExtra("FROM", "couriercompleted")
                intent.putExtra("POSTID", postId)
                startActivity(intent)

            }

            override fun deleteMyPost(postId: String, position: Int) {}

            override fun OnClick(id: String, requestId: String, position: Int) {
                val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                intent.putExtra("POSTID", id)
                intent.putExtra("userId",completedList[position].userId)
                intent.putExtra("REQUESTID", requestId)
                intent.putExtra("FROM", "couriercompleted")
                startActivity(intent)
            }

        })
        view.noDataTxt.visibility = View.GONE
        view.mNewPstReclr.adapter = myCompletedPostAdapter
        return view
    }


    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): CompletedPostFragment {
            val fragment = CompletedPostFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }


    override fun onResume() {
        super.onResume()
        if (view != null) {
            getMyCompletedPost(view!!)
        }
    }

    private fun getMyCompletedPost(view: View) {
        if (Constant.isNetworkAvailable(context!!, coordinateLay)) {
            //  view.progressBar.visibility = View.VISIBLE
                progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url +"?status=complete&isActive=1&page=1&postId=0",
                    Response.Listener { response ->
                      //  view.progressBar.visibility = View.GONE
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

                                completedList.clear()

                                if (status == "success") {
                                    val data=result.getJSONArray("data")
                                    for (i in 0..data!!.length() - 1){
                                        val jsonObject=data.getJSONObject(i)
                                        val gson = Gson()
                                        val mypostResponce = gson.fromJson(jsonObject.toString(), MyPostResponce.DataBean::class.java)
                                        completedList.add(mypostResponce)
                                        myCompletedPostAdapter?.notifyDataSetChanged()



                                    }



                                    //view.noDataTxt.visibility = View.VISIBLE
                                } else {

                                    if (userType == Constant.COURIOR) {
                                        if (message == "Currently you are inactivate user") {
                                            val helper = HelperClass(context!!, activity!!)
                                            helper.inActiveByAdmin("Admin inactive your account", true)
                                        } else {
                                            view.noDataTxt.visibility = View.VISIBLE
                                        }
                                    } else {
                                        view.noDataTxt.visibility = View.VISIBLE
                                    }

                                }
                            }

                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                      //  view.progressBar.visibility = View.GONE
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
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)
        }else{
           Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }
}