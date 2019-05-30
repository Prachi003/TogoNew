package com.togocourier.ui.phase3.activity

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.AcceptRejectListioner
import com.togocourier.Interface.PostOnClick
import com.togocourier.R
import com.togocourier.adapter.NewCourierAllRequestAdapter
import com.togocourier.responceBean.UpdateProfileInfo
import com.togocourier.ui.activity.ChatActivity
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.activity.customer.NewCustomerPostDetailsActivity
import com.togocourier.ui.activity.customer.NewPaymentListActivity
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.ui.phase3.adapter.CourierPostItemdetailAdapter
import com.togocourier.util.*
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_courier_new_post_detail.*
import kotlinx.android.synthetic.main.activity_pending_courier_detail.*
import kotlinx.android.synthetic.main.fragment_my_post_new.view.*
import kotlinx.android.synthetic.main.new_dialog_add_mobile_nu.*
import kotlinx.android.synthetic.main.new_dialog_apply_bid.*
import kotlinx.android.synthetic.main.new_fragment_profile.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.Map
import kotlin.collections.MutableMap

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class CostumerNewPostDetailActivity : AppCompatActivity(), View.OnClickListener, PostOnClick, NewCourierAllRequestAdapter.onChatClick {
    override fun OnClick(position: Int) {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("otherUID", requestBeanArrayList!!.get(position).applyUserId)
        intent.putExtra("title", txtTitleOrder.text.toString().trim())
        startActivity(intent)
    }

    private var mLastClickTime: Long = 0
    private var POSTID = ""
    private var progress: ProgressDialog? = null
    private var gson = Gson()
    private var myPostArrayList: ArrayList<GetMyPost.DataBean.ItemBean>? = null
    private var requestBeanArrayList: ArrayList<GetMyPost.DataBean.RequestsBean>? = null
    private lateinit var courierPostItemAdapter: CourierPostItemdetailAdapter
    private var FROM = ""
    private var userId = ""
    private var profileImage = ""

    var myPostResponce=GetMyPost.DataBean()

    private var openDialog: Dialog? = null
    private var count = -1
    private var adapter: NewCourierAllRequestAdapter? = null


    override fun delete(position: Int, itemView: View) {

    }

    override fun GetPosition(position: Int) {
        val intent = Intent(this, NewCustomerPostDetailsActivity::class.java)
        val itemBean: GetMyPost.DataBean.ItemBean = myPostArrayList!!.get(position)
        intent.putExtra("itembean", itemBean)
        intent.putExtra("profileImage", profileImage)
        if(myPostResponce.applyUserId!=null){
            intent.putExtra("applyUserId", myPostResponce.applyUserId)

        }else{
            intent.putExtra("applyUserId", "")

        }
        intent.putExtra("FROM", "costumerPost")
        startActivity(intent)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_courier_new_post_detail)
        if (intent != null) {
            POSTID = intent.getStringExtra("POSTID")
            FROM = intent.getStringExtra("FROM")
            //userId=intent.getStringExtra("userId")
        }
        initView()
    }

    private fun initView() {
        progress = ProgressDialog(this)
        myPostArrayList = ArrayList()
        requestBeanArrayList = ArrayList()
        courierPostItemAdapter = CourierPostItemdetailAdapter(this, myPostArrayList!!, this, "new")
        recyclerPostDetail.layoutManager = GridLayoutManager(this, 2)
        recyclerPostDetail.adapter = courierPostItemAdapter
        backImg.setOnClickListener(this)

        adapter = NewCourierAllRequestAdapter(this@CostumerNewPostDetailActivity,
                requestBeanArrayList, object : AcceptRejectListioner {
            override fun OnClick(id: String, status: String, bitPrice: String, applyUserId: String?) {
                if (status == "cancel") {
                    acceptRejectRequest(id, status, POSTID)
                } else if (status == "accept") {

                    if (myPostResponce.postStatus.equals("new")){
                        val intent = Intent(this@CostumerNewPostDetailActivity, NewPaymentListActivity::class.java)
                        intent.putExtra("requestId", id)
                        intent.putExtra("postItemId","")
                        intent.putExtra("bitPrice", bitPrice)
                        intent.putExtra("FROM", FROM)
                        intent.putExtra("tipPrice", "")
                        intent.putExtra("paymentType", "normal")
                        intent.putExtra("courierId", applyUserId)
                        intent.putExtra("postId", POSTID)
                        startActivity(intent)
                    }else{
                        Toast.makeText(this@CostumerNewPostDetailActivity,"This post is already accepted.",Toast.LENGTH_SHORT).show()
                    }

                }
            }

            override fun OnClickUserId(userId: String, postUserId: String?) {

            }
        }, this)
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        requestRecyclerView.layoutManager = layoutManager
        requestRecyclerView.adapter = adapter
        apply_courier_btn_n.setOnClickListener(this)
        apply_courier_btn_new.setOnClickListener(this)
        getMyNewPost(rlParent)
    }

    private fun acceptRejectRequest(id: String, status: String, postId: String) =
            if (Constant.isNetworkAvailable(this, rlParent)) {
                // progressBar.visibility = View.VISIBLE
                progress!!.show()

                val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Accept_reject_Request_Url,
                        Response.Listener { response ->
                            // progressBar.visibility = View.GONE
                            progress!!.dismiss()

                            val result: JSONObject?
                            try {
                                result = JSONObject(response)
                                val status: String = result.getString("status")
                                val message = result.getString("message")

                                if (status == "return") {
                                    Constant.returnAlertDialogToMainActivity(this@CostumerNewPostDetailActivity, message)
                                    return@Listener
                                }

                                if (status == "success") {
                                    getMyNewPost(rlParent)
                                } else {
                                    val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                    if (userType == Constant.COURIOR) {
                                        if (message == "Currently you are inactivate user") {
                                            val helper = HelperClass(this, this)
                                            helper.inActiveByAdmin("Admin inactive your account", true)
                                        } else {
                                            Constant.snackbar(rlParent, message)
                                        }
                                    } else {
                                        Constant.snackbar(rlParent, message)
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
                        header.put("authToken", PreferenceConnector.readString(this@CostumerNewPostDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                        return header
                    }

                    override fun getParams(): Map<String, String> {
                        val params = HashMap<String, String>()
                        params.put("bidId", id)
                        params.put("postId", postId)
                        params.put("acceptOrReject", status)
                        return params
                    }
                }
                stringRequest.retryPolicy = DefaultRetryPolicy(
                        20000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
                VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
            } else {
                Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
            }


    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        when (v!!.id) {
            R.id.backImg -> {
                onBackPressed()
            }

            R.id.apply_courier_btn_n -> {
                if (FROM.equals("courierlist")) {
                    llApplycourier.visibility = View.VISIBLE
                    apply_courier_btn.visibility = View.GONE
                    txtBidPrice.setText(txtTotalPrice.text.toString())
                } else {
                    llApplycourier.visibility = View.GONE

                }
            }

            R.id.apply_courier_btn_new -> {
                val user_contact = PreferenceConnector.readString(this, PreferenceConnector.USERCONTACTNO, "")
                if (user_contact.isEmpty() || user_contact == "") {
                    addMobileNu(rlParentPending)

                } else {
                    openApplyBidDialog()

                }

            }

            R.id.applyBtn -> {
                if (isValidBidPrice(openDialog!!.popPriceTxt)) {
                    Constant.hideSoftKeyboard(this)
                    //  doBidRequest(openDialog!!.popPriceTxt.text.toString(), openDialog!!.popProgressBar, openDialog!!)
                    doBidRequest(openDialog!!.popPriceTxt.text.toString(), openDialog!!)
                }
            }


        }
    }

    private fun addMobileNu(view: View?) {
        val openDialog = Dialog(this)
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
            if (TextUtils.isEmpty(openDialog.edt_mobile_new.text.trim())) {
                Toast.makeText(this, "Enter Mobile nu", Toast.LENGTH_SHORT).show()
            } else {
                updateProfile(view!!, openDialog.edt_mobile_new.text.toString().trim(), openDialog)
            }
        }



        openDialog.rl_cancel_new_mobile.setOnClickListener({
            Constant.hideSoftKeyboard(this)
            openDialog.dismiss()

        })
        openDialog.show()
    }


    private fun updateProfile(view: View, mobile: String, openDialog: Dialog) {
        if (Constant.isNetworkAvailable(this, view)) {
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
                        openApplyBidDialog()

                    } else {
                        val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                        if (userType == Constant.COURIOR) {
                            if (message == "Currently you are inactivate user") {
                                val helper = HelperClass(this, this)
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
                        val helper = HelperClass(this, this)
                        helper.sessionExpairDialog()
                    } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()


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
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@CostumerNewPostDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("contactNo", mobile)

                    return params
                }

            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(multipartRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateSession(updateProfileInf: UpdateProfileInfo) {
        PreferenceConnector.writeString(this, PreferenceConnector.USEREMAIL, updateProfileInf.data?.email.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERPROFILEIMAGE, updateProfileInf.data?.profileImage.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERFULLNAME, updateProfileInf.data?.fullName.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERCOUNTRYCODE, updateProfileInf.data?.countryCode.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERCONTACTNO, updateProfileInf.data?.contactNo.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERADDRESS, updateProfileInf.data?.address.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERLATITUTE, updateProfileInf.data?.latitude.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERLONGITUTE, updateProfileInf.data?.longitude.toString())

        //updateProfileFCM()
    }

    private fun doBidRequest(price: String, dialog: Dialog) {
        if (Constant.isNetworkAvailable(this, rlParent)) {
            // popProgressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Send_Request_Url,
                    Response.Listener { response ->
                        //  popProgressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "return") {
                                Constant.returnAlertDialog(this@CostumerNewPostDetailActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                dialog.dismiss()
                                //apply_status_btn.visibility = View.GONE

                                applyBidPriceSuccess("You successfully applied for this post")

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(rlParent, message)
                                    }
                                } else {
                                    Constant.snackbar(rlParent, message)
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        //  popProgressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()
                            } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@CostumerNewPostDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", POSTID)
                    params.put("userId", userId)
                    params.put("bidPrice", price)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun openApplyBidDialog() {
        openDialog = Dialog(this)
        openDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog!!.setCancelable(false)
        openDialog!!.setContentView(R.layout.new_dialog_apply_bid)
        openDialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog!!.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog!!.window!!.attributes = lWindowParams

        inputFilter(openDialog!!.popPriceTxt)

        openDialog!!.applyBtn.setOnClickListener(this)

        openDialog!!.rl_cancel.setOnClickListener({
            Constant.hideSoftKeyboard(this)
            openDialog!!.dismiss()

        })
        openDialog!!.show()
    }

    private fun isValidBidPrice(popPriceTxt: EditText): Boolean {
        val v = Validation()
        when {
            v.isEmpty(popPriceTxt) -> {
                Toast.makeText(this, "Price can't be empty", Toast.LENGTH_SHORT).show()
                popPriceTxt.requestFocus()
                return false
            }
            v.isValidValue(popPriceTxt) -> {
                Toast.makeText(this, "Price is not vaild", Toast.LENGTH_SHORT).show()
                popPriceTxt.requestFocus()
                return false

            }
            v.isNotZero(popPriceTxt) -> {
                Toast.makeText(this, "Price can't be zero", Toast.LENGTH_SHORT).show()
                popPriceTxt.requestFocus()
                return false
            }
            else -> return true
        }
    }

    // Input filter used to restrict amount input to be round off to 2 decimal places
    private fun inputFilter(et: EditText) {
        et.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                if (et.text.toString().contains(".")) {
                    if (et.text.toString().substring(et.text.toString().indexOf(".") + 1, et.length()).length == 2) {
                        val fArray = arrayOfNulls<InputFilter>(1)
                        fArray[0] = InputFilter.LengthFilter(arg0.length)
                        et.filters = fArray
                    }
                }

                et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun afterTextChanged(arg0: Editable) {
                if (arg0.isNotEmpty()) {
                    val str = et.text.toString()
                    et.setOnKeyListener { v, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_DEL) {
                            count--
                            val fArray = arrayOfNulls<InputFilter>(1)
                            fArray[0] = InputFilter.LengthFilter(100)
                            et.filters = fArray
                        }
                        false
                    }
                    val t = str[arg0.length - 1]
                    if (t == '.') {
                        count = 0
                    }
                    if (count >= 0) {
                        if (count == 2) {
                            val fArray = arrayOfNulls<InputFilter>(1)
                            fArray[0] = InputFilter.LengthFilter(arg0.length)
                            et.filters = fArray
                        }
                        count++
                    }
                }
            }
        })
    }


    fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(this, rlParent)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url + "?status=new&isActive=1&page=1&postId=" + POSTID,
                    Response.Listener { response ->
                        //view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            myPostArrayList!!.clear()
                            if (status == "success") {
                                val data = result.getJSONArray("data")
                                for (i in 0..data!!.length() - 1) {
                                    val datajson = data.getJSONObject(i)
                                     myPostResponce = gson.fromJson(datajson.toString(), GetMyPost.DataBean::class.java)
                                    userId = myPostResponce.userId!!

                                    profileImage= myPostResponce.profileImage!!
                                    // myPostArrayList!!.add(myPostResponce)
                                    val item = datajson.getJSONArray("item")
                                    for (j in 0..item!!.length() - 1) {
                                        val itemjson = item.getJSONObject(j)
                                        val myPostResponceN = gson.fromJson(itemjson.toString(), GetMyPost.DataBean.ItemBean::class.java)
                                        myPostArrayList!!.add(myPostResponceN)

                                    }

                                    val requests = datajson.getJSONArray("requests")
                                    for (k in 0..requests.length() - 1) {
                                        val requestjson = requests.getJSONObject(k)
                                        val requestModel = gson.fromJson(requestjson.toString(), GetMyPost.DataBean.RequestsBean::class.java)
                                        requestBeanArrayList!!.add(requestModel)

                                    }



                                    setData(myPostResponce)


                                }

                                adapter!!.notifyDataSetChanged()
                                courierPostItemAdapter.notifyDataSetChanged()
                                // myNewPostAdapter?.notifyDataSetChanged()
                                // view.noDataTxt.visibility = View.GONE
                            } else {

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        view.noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    view.noDataTxt.visibility = View.VISIBLE
                                }
                            }

                            //myNewPostAdapter?.notifyDataSetChanged()

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
                    param.put("authToken", PreferenceConnector.readString(this@CostumerNewPostDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
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

    private fun setData(myPostResponce: GetMyPost.DataBean?) {
        txtTitleOrder.text = myPostResponce!!.postTitle
        txtTotalPrice.text = "$%.2f".format(myPostResponce.totalPrice!!.toDouble())

        //txtTotalPrice.text = "$ " + myPostResponce.totalPrice
        if (FROM.equals("courierlist")) {
            llApplycourier.visibility = View.VISIBLE
            apply_courier_btn_n.visibility = View.GONE
            txtBidPrice.setText(txtTotalPrice.text.toString())
            apply_courier_btn_n.setOnClickListener {

            }

        } else {
            if (requestBeanArrayList!!.size > 0) {
                txtNA.visibility = View.GONE
                txtRequest.visibility = View.VISIBLE
            }else{
                txtRequest.visibility = View.VISIBLE

                txtNA.visibility = View.VISIBLE

            }



            requestRecyclerView.visibility = View.VISIBLE
            apply_courier_btn_n.visibility = View.GONE

        }
    }

    private fun applyBidPriceSuccess(msg: String) {
        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Alert")

        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)

        alertDialog.setPositiveButton("Ok", { dialog, which ->
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()

            this.finish()
        })
        alertDialog.show()

    }
}
