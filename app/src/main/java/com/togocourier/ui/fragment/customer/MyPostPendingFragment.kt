package com.togocourier.ui.fragment.customer

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
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.adapter.MyPendingPostAdapter
import com.togocourier.ui.activity.customer.NewCustomerPostDetailsActivity
import com.togocourier.ui.fragment.customer.model.GetPendingPost
import com.togocourier.ui.phase3.activity.PendingCostumerDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_my_post_pending.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList

class MyPostPendingFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mContext: Context? = null
    private var arraylist = ArrayList<String>()
    lateinit var coordinateLay: CoordinatorLayout
    private var gson = Gson()
    private var myPostArrayList: ArrayList<GetPendingPost.DataBean>? = null
    private var myPendingPostAdapter: MyPendingPostAdapter? = null
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_my_post_pending, container, false)
        if (view != null) {
            initializeView(view)
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.isClickable = true
        view.requestFocus()
    }


    override fun onResume() {
        super.onResume()
        getMyNewPost(this.view!!)

    }

    private fun initializeView(view: View) {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        val layoutManager = LinearLayoutManager(mContext)
        progress = ProgressDialog(context!!)
        myPostArrayList = ArrayList()
        view.mPnPstReclr.layoutManager = layoutManager
        myPendingPostAdapter = MyPendingPostAdapter(mContext, myPostArrayList as ArrayList<GetPendingPost.DataBean>, object : MyOnClick {
            override fun OnClickItem(postId: String, userId: String?, position: Int) {
                val intent = Intent(activity, PendingCostumerDetailActivity::class.java)
                intent.putExtra("FROM", Constant.pendingPost)
                intent.putExtra("userId", myPostArrayList!![position].userId)
                intent.putExtra("POSTID", postId)
                startActivity(intent)

            }



            override fun deleteMyPost(postId: String, position: Int) {
            }

            override fun OnClick(id: String, requestId: String, position: Int) {
                val intent = Intent(activity, NewCustomerPostDetailsActivity::class.java)
                intent.putExtra("POSTID", id)
                intent.putExtra("FROM", Constant.pendingPost)
                intent.putExtra("userId", myPostArrayList!![position].userId)

                intent.putExtra("REQUESTID", requestId)
                startActivity(intent)
            }
        })
        view.mPnPstReclr.adapter = myPendingPostAdapter
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    companion object {
        // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): MyPostPendingFragment {
            val fragment = MyPostPendingFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            //   view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getPendingPost + "?status=pending&isActive=1&page=1&postId=0",
                                      Response.Listener { response ->
                      //  view.progressBar.visibility = View.GONE
                        progress!!.dismiss()
                        var myPostResponce = GetPendingPost.DataBean()
                        val result: JSONObject?
                         myPostArrayList!!.clear()
                        try {
                            result = JSONObject(response)
                            val message = result.getString("message")
                            if(message.equals("Invalid token")){
                                val helper = HelperClass(context!!, activity!!)
                                helper.sessionExpairDialog()
                            }else{
                                val status = result.getString("status")


                                if (status == "success") {
                                    val data = result.getJSONArray("data")
                                    for (i in 0..data!!.length() - 1) {
                                        val datajson = data.getJSONObject(i)
                                        myPostResponce = gson.fromJson(datajson.toString(), GetPendingPost.DataBean::class.java)
                                        myPostArrayList!!.add(myPostResponce)

                                        // val item = datajson.getJSONArray("item")
                                        /* for (j in 0..item!!.length() - 1) {
                                             val itemjson = item.getJSONObject(j)
                                             val myPostResponceN = gson.fromJson(itemjson.toString(), GetPendingPost.DataBean.ItemBean::class.java)
                                             myPostArrayList!!.add(myPostResponceN)


                                         }*/



                                    }
                                    myPendingPostAdapter?.notifyDataSetChanged()
                                    view.noDataTxt.visibility = View.GONE
                                } else if (message.equals("No Content")) {
                                    view.noDataTxt.visibility = View.VISIBLE

                                }

                                // view.noDataTxt.visibility = View.GONE
                                else {
                                    if(context != null) {
                                        myPostArrayList?.clear()
                                        myPendingPostAdapter?.notifyDataSetChanged()

                                        val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
                                        if (userType == Constant.COURIOR) {
                                            if (message == "Currently you are inactivate user") {
                                                val helper = HelperClass(context!!, activity!!)
                                                helper.inActiveByAdmin("Admin inactive your account", true)
                                            } else {
                                                view.noDataTxt.visibility = View.VISIBLE
                                            }
                                        } else {
                                            view.noDataTxt.visibility = View.GONE
                                        }
                                    }
                                }
                            }






                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                if (context != null) {
                                    val helper = HelperClass(context!!, activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }
                      //  view.progressBar.visibility = View.GONE
                        progress!!.dismiss()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(mContext!!).addToRequestQueue(stringRequest)
        }else{
           Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }
}
