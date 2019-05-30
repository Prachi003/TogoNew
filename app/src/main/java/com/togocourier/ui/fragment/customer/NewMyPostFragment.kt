package com.togocourier.ui.fragment.customer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
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
import com.togocourier.adapter.MyPostNewAdapter
import com.togocourier.ui.activity.customer.NewCustomerPostDetailsActivity
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.ui.phase3.activity.CostumerNewPostDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_my_post_new.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.MutableMap

class NewMyPostFragment : Fragment() {

    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mContext: Context? = null
    lateinit var coordinateLay: CoordinatorLayout
    private var gson = Gson()
    private var myPostArrayList: ArrayList<GetMyPost.DataBean>? = null
    private var myNewPostAdapter: MyPostNewAdapter? = null
    private var progress: ProgressDialog? = null

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        val view = inflater.inflate(R.layout.fragment_my_post_new, container, false)
        initializeView(view)

        /*val user_contact = PreferenceConnector.readString(context!!, PreferenceConnector.USERCONTACTNO, "")
        if (user_contact.isEmpty() || user_contact == "") {
            val builder = AlertDialog.Builder(context!!)
            builder.setTitle("Alert")
            builder.setCancelable(false)
            builder.setMessage("Please add contact no")
            builder.setPositiveButton("Ok") { dialogInterface, i ->
                dialogInterface.dismiss()

                val activity = activity
                if (activity is HomeActivity) {
                    val myactivity = activity as HomeActivity?
                    myactivity!!.profileFragmentClick()
                }
            }

            builder.setNegativeButton("Cancel") { dialogInterface, i ->
                dialogInterface.dismiss()
            }
            val alert = builder.create()
            alert.show()
        }*/
        return view
    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = fragmentManager!!.popBackStackImmediate(backStackName, 0)
        var i = fragmentManager?.backStackEntryCount
        if (i != null) {
            while (i > 0) {
                fragmentManager?.popBackStackImmediate()
                i--
            }
        }
        if (!fragmentPopped) {
            val transaction = fragmentManager!!.beginTransaction()
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.isClickable = true
        view.requestFocus()

        getMyNewPost(view)
    }

    private fun initializeView(view: View) {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        val layoutManager = LinearLayoutManager(mContext)

        progress = ProgressDialog(context!!)
        myPostArrayList = ArrayList()
        view.mNewPstReclr.layoutManager = layoutManager
        myNewPostAdapter = MyPostNewAdapter("NewMyPostFragment", mContext, myPostArrayList as ArrayList<GetMyPost.DataBean>, object : MyOnClick {
            override fun OnClickItem(postId: String, userId: String?, position: Int) {
                val intent = Intent(activity, CostumerNewPostDetailActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("applyUserId","")
                intent.putExtra("FROM", "costumernew")
                startActivity(intent)
            }

            override fun deleteMyPost(postId: String, position: Int) {
                //delete Api calling
                deletePost(view, postId, position)
            }

            override fun OnClick(id: String, requestId: String, position: Int) {
                // Preventing multiple clicks, using threshold of 1/2 second
                if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
                    return
                }
                mLastClickTime = SystemClock.elapsedRealtime()

                val intent = Intent(activity, NewCustomerPostDetailsActivity::class.java)
                intent.putExtra("POSTID", id)
                intent.putExtra("FROM", Constant.newPost)
                startActivity(intent)
            }
        })
        view.mNewPstReclr.adapter = myNewPostAdapter
    }

    override fun onAttach(context: Context?) {
        mContext = context
        super.onAttach(context)
    }

    override fun onResume() {
        super.onResume()
        if (view != null) {
            getMyNewPost(view!!)
        }
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"


        fun newInstance(param1: String, param2: String): NewMyPostFragment {
            val fragment = NewMyPostFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url + "?status=new&isActive=1&page=1&postId=0",
                    Response.Listener { response ->
                        //view.progressBar?.visibility = View.GONE
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
                                myPostArrayList!!.clear()
                                if (status == "success") {
                                    val data = result.getJSONArray("data")
                                    for (i in 0..data!!.length() - 1) {
                                        val datajson = data.getJSONObject(i)
                                        val myPostResponce = gson.fromJson(datajson.toString(), GetMyPost.DataBean::class.java)
                                        myPostArrayList!!.add(myPostResponce)
                                        val item = datajson.getJSONArray("item")
                                        /*for (j in 0..item!!.length() - 1) {
                                            val itemjson = item.getJSONObject(j)
                                            val myPostResponceN = gson.fromJson(itemjson.toString(), GetMyPost.DataBean.ItemBean::class.java)
                                            myPostArrayList!!.add(myPostResponceN)

                                        }*/


                                    }
                                    myNewPostAdapter?.notifyDataSetChanged()
                                    view.noDataTxt.visibility = View.GONE
                                } else {
                                    if (context != null) {
                                        val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
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

                                    myNewPostAdapter?.notifyDataSetChanged()
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
                                if (context != null) {
                                    val helper = HelperClass(context!!, activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }

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
        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    fun deletePost(view: View, postId: String, position: Int) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deletePostById,
                    Response.Listener { response ->
                        //  view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                myPostArrayList?.removeAt(position)
                                myNewPostAdapter?.notifyDataSetChanged()
                                getMyNewPost(view)

                            } else {
                                val userType = PreferenceConnector.readString(mContext!!, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(mContext!!, activity!!)
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
                                if (context != null) {
                                    val helper = HelperClass(context!!, activity!!)
                                    helper.sessionExpairDialog()
                                }
                            }
                        }

                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(mContext!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(mContext!!).addToRequestQueue(stringRequest)
        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }


}
