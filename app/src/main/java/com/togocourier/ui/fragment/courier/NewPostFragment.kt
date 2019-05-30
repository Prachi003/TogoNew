package com.togocourier.ui.fragment.courier

import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.text.TextUtils
import android.view.*
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.MyOnClick
import com.togocourier.R
import com.togocourier.adapter.NewPostAdapter
import com.togocourier.responceBean.UpdateProfileInfo
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.ui.phase3.activity.CostumerNewPostDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.fragment_new_post.view.*
import kotlinx.android.synthetic.main.new_dialog_add_mobile_nu.*
import kotlinx.android.synthetic.main.new_fragment_profile.view.*
import org.json.JSONException
import org.json.JSONObject

class NewPostFragment : Fragment() {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var mContext: Context? = null
    lateinit var coordinateLay: CoordinatorLayout
    private var gson = Gson()
    private var myPostArrayList: ArrayList<GetMyPost.DataBean>? = null
    private var NewPostAdapter: NewPostAdapter? = null
    private var progress: ProgressDialog? = null
    var myPostResponce=GetMyPost.DataBean()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_new_post, container, false)
        initializeView(view)
        showPopupContact()

        //getNewPost(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.isClickable = true
        view.requestFocus()

    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)
        mContext = context
    }

    private fun initializeView(view: View) {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout

        val layoutManager = LinearLayoutManager(mContext)
        progress = ProgressDialog(context!!)
        myPostArrayList = ArrayList()
        view.nwPstReclr.layoutManager = layoutManager

        NewPostAdapter = NewPostAdapter("NewPostFragment", mContext, myPostArrayList as ArrayList<GetMyPost.DataBean>, object : MyOnClick {
            override fun OnClick(id: String, requestId: String, position: Int) {

            }

            override fun OnClickItem(postId: String, userId: String?, position: Int) {
                val intent = Intent(activity, CostumerNewPostDetailActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("userId",userId)
                intent.putExtra("FROM", "courierlist")
                startActivity(intent)
            }

            override fun deleteMyPost(postId: String, position: Int) {

            }


        })
        view.nwPstReclr.adapter = NewPostAdapter
        NewPostAdapter?.notifyDataSetChanged()
    }

    fun showPopupContact(){
        val user_contact = PreferenceConnector.readString(this.context!!, PreferenceConnector.USERCONTACTNO, "")

        if (user_contact.isEmpty() || user_contact == "") {
            Alert("For better experience please add your contact number")


        }
    }


    fun Alert(msg:String) {

        val alertDialog = AlertDialog.Builder(context)

        alertDialog.setTitle("Alert")

        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)

        alertDialog.setPositiveButton("Ok", { dialog, which ->
            addMobileNu(view)
        })

        alertDialog.show()

    }

    private fun addMobileNu(view: View?) {
        val openDialog = Dialog(context)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setCancelable(false)
        openDialog.setContentView(R.layout.new_dialog_add_mobile_nu)
        openDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog.window!!.attributes = lWindowParams

        openDialog.btnDonemobile.setOnClickListener {
            if (TextUtils.isEmpty(openDialog.edt_mobile_new.text.trim())){
                Toast.makeText(context,"Enter Mobile nu",Toast.LENGTH_SHORT).show()
            }else{
                updateProfile(view!!,openDialog.edt_mobile_new.text.toString().trim(),openDialog)
            }
        }



        openDialog.rl_cancel_new_mobile.setOnClickListener({
            Constant.hideSoftKeyboard(context as Activity)
            openDialog.dismiss()

        })
        openDialog.show()
    }

    private fun updateProfile(view: View, mobile: String, openDialog: Dialog) {
        if (Constant.isNetworkAvailable(context!!, view)) {
            //   view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.UpdateProfile, Response.Listener { response ->
                val resultResponse = String(response.data)
                // view.progressBar.visibility = View.GONE
                progress!!.dismiss()

                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")


                    if (status == "success") {
                        val gson = Gson()
                        val updateProfileInf = gson.fromJson(resultResponse, UpdateProfileInfo::class.java)
                        updateSession(updateProfileInf)
                        openDialog.dismiss()

                    } else {
                        val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
                        if (userType == Constant.COURIOR) {
                            if (message == "Currently you are inactivate user") {
                                val helper = HelperClass(context!!, activity!!)
                                helper.inActiveByAdmin("Admin inactive your account", true)
                            } else {
                                Constant.snackbar(view.parentLay, message)
                            }
                        } else {
                            Constant.snackbar(view.parentLay, message)
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                //  view.progressBar.visibility = View.GONE
                progress!!.dismiss()

                if (networkResponse != null) {

                    if (networkResponse.statusCode == 300) {
                        val helper = HelperClass(context!!, activity!!)
                        helper.sessionExpairDialog()
                    } else Toast.makeText(context!!, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()


                    val result = String(networkResponse.data)
                    try {
                        val response = JSONObject(result)
                        val message = response.getString("message")
                        Snackbar.make(view.parentLay, message, Snackbar.LENGTH_LONG).setAction("ok", null).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                error.printStackTrace()
            }) {
                override fun getHeaders(): MutableMap<String, String> {
                    val param = java.util.HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = java.util.HashMap<String, String>()
                    params.put("contactNo", mobile)
                    return params
                }

            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(multipartRequest)

        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }


    /*override fun onClick(view: View) {
        when (view.id) {
            R.id.tabRightIcon -> {
               mContext?.startActivity(Intent(mContext,NotificationActivity::class.java))
            }
        }

    }*/

    override fun onResume() {
        super.onResume()
        getNewPost(view!!)
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): NewPostFragment {
            val fragment = NewPostFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    private fun getNewPost(view: View) {
        if (Constant.isNetworkAvailable(mContext!!, coordinateLay)) {
            // view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_Courier_AllPost_List_Url+"?userId&latitude&longitude"
                    , Response.Listener { response ->
                //  view.progressBar.visibility = View.GONE
                progress!!.dismiss()

                val result: JSONObject?
                try {
                    result = JSONObject(response)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    myPostArrayList?.clear()

                        if (status == "success") {
                            val data = result.getJSONArray("data")
                            for (i in 0..data!!.length() - 1) {
                                val datajson = data.getJSONObject(i)
                                myPostResponce = gson.fromJson(datajson.toString(), GetMyPost.DataBean::class.java)
                                myPostArrayList!!.add(myPostResponce)

                                val item = datajson.getJSONArray("item")
                                for (j in 0..item!!.length() - 1) {
                                    val itemjson = item.getJSONObject(j)
                                    val myPostResponceN = gson.fromJson(itemjson.toString(), GetMyPost.DataBean.ItemBean::class.java)

                                }

                            }

                            NewPostAdapter?.notifyDataSetChanged()






                            val userType = PreferenceConnector.readString(mContext!!, PreferenceConnector.USERTYPE, "")

                       /* if (activity != null && userType == Constant.COURIOR) {
                            if (myPostResponce.isTrack.equals("YES")) {
                                // activity!!.startService(Intent(activity!!, MyLocationService::class.java))

                                //  HelperClass.startBackgroundService(mContext!!)

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    HelperClass.startBackgroundService(mContext!!)
                                } else {
                                    HelperClass.startJobDispatcher(mContext!!)
                                }

                            } else if (myPostResponce.isTrack.equals("NO")) {
                                // activity!!.stopService(Intent(activity!!, MyLocationService::class.java))
                                // HelperClass.stopBackgroundService(mContext!!)

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    HelperClass.stopBackgroundService(mContext!!)
                                } else {
                                    HelperClass.stopJobDispatcher(mContext!!)
                                }
                            }
                        }*/
                    } else {
                        NewPostAdapter?.notifyDataSetChanged()
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
                } catch (e: JSONException) {
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

    private fun updateSession(updateProfileInf: UpdateProfileInfo) {
        PreferenceConnector.writeString(context!!, PreferenceConnector.USEREMAIL, updateProfileInf.data?.email.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERPROFILEIMAGE, updateProfileInf.data?.profileImage.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERFULLNAME, updateProfileInf.data?.fullName.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERCOUNTRYCODE, updateProfileInf.data?.countryCode.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERCONTACTNO, updateProfileInf.data?.contactNo.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERADDRESS, updateProfileInf.data?.address.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERLATITUTE, updateProfileInf.data?.latitude.toString())
        PreferenceConnector.writeString(context!!, PreferenceConnector.USERLONGITUTE, updateProfileInf.data?.longitude.toString())

        //updateProfileFCM()
    }
}
