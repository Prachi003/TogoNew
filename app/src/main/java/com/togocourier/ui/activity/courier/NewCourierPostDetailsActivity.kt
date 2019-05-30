package com.togocourier.ui.activity.courier

import android.Manifest
import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
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
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.responceBean.PostDetailsResponce
import com.togocourier.responceBean.RattingResponceBean
import com.togocourier.ui.activity.ChatActivity
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.util.*
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_pending_courier_detail.view.*
import kotlinx.android.synthetic.main.full_image_view_dialog.*
import kotlinx.android.synthetic.main.new_activity_courier_post_details.*
import kotlinx.android.synthetic.main.new_dialog_apply_bid.*
import kotlinx.android.synthetic.main.new_dialog_change_status.*
import kotlinx.android.synthetic.main.new_dialog_commission.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class NewCourierPostDetailsActivity : AppCompatActivity(), View.OnClickListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private var userType = ""
    private var postId = ""
    private var postUserId = ""
    private var postStatus = ""
    private val gson = Gson()
    private var from = ""
    private var otherUID = ""
    private var data: PostDetailsResponce.PostDataBean = PostDetailsResponce.PostDataBean()
    private var deliveryStatus: String = ""
    private var deliveryStatusTxt: String = ""

    //ownerEnd
    private var requestId = ""
    private var signatureBitmap: Bitmap? = null

    private var rattingResponce = RattingResponceBean.ResultBean()

    //for location update
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var mGoogleApiClient: GoogleApiClient? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private var lmgr: LocationManager? = null
    private var isGPSEnable: Boolean = false
    private var bidPrice: Double? = null
    private var commision: Double? = null
    var myPostResponce= GetMyPost.DataBean.ItemBean()


    // variable to track event time
    private var mLastClickTime: Long = 0
    private var count = -1

    private var openDialog: Dialog? = null

    private var pickUpLat: String = ""
    private var pickUpLng: String = ""
    private var deliveryLat: String = ""
    private var deliveryLng: String = ""
    private var applyUserId: String = ""
    private var FROM=""


    private var statusDialog: Dialog? = null
    private var progress: ProgressDialog? = null
    private var helper: HelperClass? = null

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_courier_post_details)

        helper = HelperClass(this, this)
        progress = ProgressDialog(this)
        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")

        val bundle = intent.extras
       /* postId = bundle!!.getString("POSTID")
        from = bundle.getString("FROM")

        requestId = if (bundle.getString("REQUESTID") != null) {
            bundle.getString("REQUESTID")
        } else {
            ""
        }*/

        if (bundle.getByteArray("SIGN_URI") != null) {
            val imageByteArray = bundle.getByteArray("SIGN_URI")
            signatureBitmap = BitmapFactory.decodeByteArray(imageByteArray, 0, imageByteArray.size)

        }
        if (intent!=null){
            applyUserId=intent.getStringExtra("applyUserId")

            myPostResponce=intent.getParcelableExtra("itembean")
            FROM=intent.getStringExtra("FROM")
        }
        iv_map_courier.setOnClickListener(this)

        updateUi(myPostResponce)

        //location update............
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        iv_back_courier.setOnClickListener(this)
        apply_status_btn.setOnClickListener(this)
        change_status_btn.setOnClickListener(this)


    }





    fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url + "?status=new&isActive=1&page=1&postId="+myPostResponce.postId,
                    Response.Listener { response ->
                        //view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            //myPostArrayList!!.clear()
                            if (status == "success") {
                                val data = result.getJSONArray("data")
                                for (i in 0..data!!.length() - 1) {
                                    val datajson = data.getJSONObject(i)
                                    // myPostArrayList!!.add(myPostResponce)
                                    val item = datajson.getJSONArray("item")
                                    for (j in 0..item!!.length() - 1) {
                                        val itemjson = item.getJSONObject(j)
                                         myPostResponce = gson.fromJson(itemjson.toString(), GetMyPost.DataBean.ItemBean::class.java)
                                        ///updateUi(myPostResponce)
                                        if (myPostResponce.itemStatus.equals("delivered")){
                                            currentStatusTxt.text = deliveryStatusTxt
                                            currentStatusTxt.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))

                                        }else{
                                            currentStatusTxt.text = deliveryStatusTxt

                                            currentStatusTxt.setTextColor(ContextCompat.getColor(this,R.color.colorBtn))

                                        }
                                        //myPostArrayList!!.add(myPostResponceN)

                                    }




                                }
                                // adapter!!.notifyDataSetChanged()
                               // courierPostItemAdapter.notifyDataSetChanged()
                                // myNewPostAdapter?.notifyDataSetChanged()
                                // view.noDataTxt.visibility = View.GONE
                            } else {

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        view.noDataTxtN.visibility = View.VISIBLE
                                    }
                                } else {
                                    view.noDataTxtN.visibility = View.VISIBLE
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
                    param.put("authToken", PreferenceConnector.readString(this@NewCourierPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
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
    private fun updateUi(myPostResponce: GetMyPost.DataBean.ItemBean) {
        //data = postDetailsResponce?.getPostData()!!
        postId = myPostResponce.postId!!
        postUserId = myPostResponce.userId!!
        tv_delivery_amount.text="$ "+myPostResponce.price
       // otherUID = myPostResponce.applyUserId.toString()

        //for bid price
        /*if (!TextUtils.isEmpty(bidPrice.toString()) && !TextUtils.isEmpty(data.requestData?.commision.toString())) {
            bidPrice = myPostResponce?.bidPrice?.toDouble()
            commision = data.myPostResponce?.commision?.toDouble()
        }
*/

        postStatus = myPostResponce.itemStatus!!
        if (!TextUtils.isEmpty(myPostResponce.itemImage)) {
            // Picasso.with(this).load(data.itemImage).placeholder(R.drawable.new_app_icon1).into(iv_item_image)
            Glide.with(this).load(myPostResponce.itemImageUrl).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_item_image)

         /*   if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                !isDestroyed
            } else {
                TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
            }) {
            }*/
        }
        tv_item_name.text = myPostResponce.itemTitle
        tv_item_quantity.text = "Qty: " + myPostResponce.itemQuantity

        if (myPostResponce.description.equals("")) {
            tv_description.text = getString(R.string.na_txt)
        } else {
            tv_description.text = myPostResponce.description
        }
        if (myPostResponce.signatureStatus == "1") {
            ly_signature.visibility = View.VISIBLE
            ly_signature.setOnClickListener(this)
        }


        if (myPostResponce.itemStatus != null) {
            applyButtonManageCourier(myPostResponce)
        } else {
            if (myPostResponce.itemStatus.equals("new")) {
                currentStatusTxt.text = getString(R.string.status_new)
                apply_status_btn.visibility = View.VISIBLE
                change_status_btn.visibility = View.GONE
                ly_current_status.visibility = View.GONE

                tv_delivery_amount.text = "$%.2f".format(data.price.toString().toDouble())
            } else {
                if(myPostResponce.itemStatus.equals("delivered")){
                    currentStatusTxt.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))

                }


                currentStatusTxt.text = """${myPostResponce.itemStatus?.substring(0, 1)!!.toUpperCase()}${myPostResponce.itemStatus?.substring(1)!!.toLowerCase()}"""
            }
        }

        tv_pickup_date_time.text = myPostResponce.collectiveDate + ", " + Constant.setTimeFormat(myPostResponce.collectiveTime!!)
        tv_delivery_date_time.text = myPostResponce.deliveryDate + ", " + Constant.setTimeFormat(myPostResponce.deliveryTime!!)
        tv_pickup_address.text = myPostResponce.pickupAdrs
        tv_delivery_address.text = myPostResponce.deliveryAdrs

        val senderName = myPostResponce.senderName!!.substring(0, 1).toUpperCase() + myPostResponce.senderName!!.substring(1)
        tv_pickup_person_name.text = senderName

        val name = myPostResponce.receiverName!!.substring(0, 1).toUpperCase() + myPostResponce.receiverName!!.substring(1)
        tv_delivery_person_name.text = name

        if (myPostResponce.senderContactNo.equals("")) {
            ly_pickup_someone_contact.visibility = View.GONE
            tv_pickup_contact_na.visibility = View.VISIBLE
        } else {
            tv_pickup_person_contact.text = myPostResponce.senderContactNo
            ly_pickup_someone_contact.visibility = View.VISIBLE
            tv_pickup_contact_na.visibility = View.GONE
        }

        if (myPostResponce.receiverContactNo.equals("")) {
            ly_someone_contact.visibility = View.GONE
            tv_delivery_contact_na.visibility = View.VISIBLE
        } else {
            tv_delivery_person_contact.text = myPostResponce.receiverContactNo
            ly_someone_contact.visibility = View.VISIBLE
            tv_delivery_contact_na.visibility = View.GONE
        }

        pickUpLat = myPostResponce.pickupLat.toString()
        pickUpLng = myPostResponce.pickupLong.toString()
        deliveryLat = myPostResponce.deliverLat.toString()
        deliveryLng = myPostResponce.deliverLong.toString()

        if (myPostResponce.signatureStatus == "0") {
            ly_signature.visibility = View.GONE

        } else if (myPostResponce.signatureStatus == "1") {
            if (myPostResponce.itemStatus.equals("delivered", ignoreCase = true)) {
                if (myPostResponce.pickUpDate == "0000-00-00") {
                    ly_take_signature.visibility = View.GONE
                    tv_no_signature_admin.visibility = View.VISIBLE
                    ly_signature.visibility = View.VISIBLE
                } else {
                    if (myPostResponce.signatureImage != null && myPostResponce.signatureImage != Constant.signatureUrl) {
                        tv_no_signature_admin.visibility = View.GONE
                        Glide.with(this).load(myPostResponce.signatureImage).into(iv_signature)
                        iv_signature.visibility = View.VISIBLE
                        ly_signature.visibility = View.VISIBLE
                        ly_signature.isEnabled = false

                    } else if (signatureBitmap != null) {
                        ly_signature.visibility = View.VISIBLE
                        iv_signature.setImageBitmap(signatureBitmap)
                        iv_signature.visibility = View.VISIBLE
                        ly_signature.setOnClickListener(this)
                    }
                }
            } else {
                if (myPostResponce.signatureImage != null && myPostResponce.signatureImage != Constant.signatureUrl) {
                    tv_no_signature_admin.visibility = View.VISIBLE
                    Glide.with(this).load(myPostResponce.signatureImage).into(iv_signature)
                    iv_signature.visibility = View.VISIBLE
                    ly_signature.visibility = View.VISIBLE
                    ly_signature.isEnabled = true

                } else if (signatureBitmap != null) {
                    ly_signature.visibility = View.VISIBLE
                    iv_signature.setImageBitmap(signatureBitmap)
                    iv_signature.visibility = View.VISIBLE
                    ly_signature.setOnClickListener(this)
                }
            }
        }


            iv_item_image.setOnClickListener {
                zoomImageDialog(myPostResponce.itemImageUrl!!)
            }

    }

    private fun applyButtonManageCourier(data: GetMyPost.DataBean.ItemBean?) {

        when {
            data?.itemStatus.equals("new", ignoreCase = true) -> {
                apply_status_btn.visibility = View.VISIBLE
                change_status_btn.visibility = View.GONE
                currentStatusTxt.text = getString(R.string.na_txt)

                ly_current_status.visibility = View.GONE
                tv_delivery_amount.text = "$%.2f".format(data!!.price.toString().toDouble())
                rl_tip.visibility=View.VISIBLE
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount_new.text = "$%.2f".format(0)

                }else{
                    tv_tip_amount_new.text = "$%.2f".format(data.tipPrice!!.toDouble())
                }

            }
            /*data?.itemStatus.equals("pending", ignoreCase = true) -> {
                apply_status_btn.visibility = View.GONE
                change_status_btn.visibility = View.GONE
                currentStatusTxt.text = getString(R.string.pending)

                ly_current_status.visibility = View.VISIBLE
               // tv_delivery_amount.text = "$%.2f".format(data.bidPrice.toString().toDouble())

            }*/
            data?.itemStatus.equals("accept", ignoreCase = true) -> {
                apply_status_btn.visibility = View.GONE
                change_status_btn.visibility = View.VISIBLE
                currentStatusTxt.text = getString(R.string.accepted)
                iv_cour_commission.visibility = View.VISIBLE

                ly_current_status.visibility = View.VISIBLE
              //  tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())

                ly_cour_commission.setOnClickListener(this)
                if (data!!.tipPrice.equals("NA")){
                    tv_tip_amount_new.text = "$%.2f".format(0)

                }else{
                    tv_tip_amount_new.text = "$%.2f".format(data.tipPrice!!.toDouble())
                }

            }
            data?.itemStatus.equals("picked", ignoreCase = true) -> {
                //  startService(Intent(this, MyLocationService::class.java))

                //  HelperClass.startJobDispatcher(this)

                //  HelperClass.startBackgroundService(this)

                // helper!!.startAlarmService(this)

                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    HelperClass.startBackgroundService(this)
                } else {
                    HelperClass.startJobDispatcher(this)
                }

                iv_map_courier.visibility=View.VISIBLE


                apply_status_btn.visibility = View.GONE
                change_status_btn.visibility = View.VISIBLE
                currentStatusTxt.text = getString(R.string.item_picked)
                iv_cour_commission.visibility = View.VISIBLE

                ly_current_status.visibility = View.VISIBLE
               // tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())

                ly_cour_commission.setOnClickListener(this)
                if (data!!.tipPrice.equals("NA")){
                    tv_tip_amount_new.text = "$%.2f".format(0)

                }else{
                    tv_tip_amount_new.text = "$%.2f".format(data.tipPrice!!.toDouble())
                }
            }
            data?.itemStatus.equals("outForDeliver", ignoreCase = true) -> {
              /*  if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    HelperClass.startBackgroundService(this)
                } else {
                    HelperClass.startJobDispatcher(this)
                }*/

                apply_status_btn.visibility = View.GONE
                change_status_btn.visibility = View.VISIBLE
                currentStatusTxt.text = getString(R.string.out_for_delivery)
                iv_cour_commission.visibility = View.VISIBLE
                iv_map_courier.visibility=View.VISIBLE

                ly_current_status.visibility = View.VISIBLE
               // tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())

                // For Signature
                if (data!!.signatureStatus == "1") {
                    ly_signature.visibility = View.VISIBLE
                    ly_signature.setOnClickListener(this)
                }

                ly_cour_commission.setOnClickListener(this)
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount_new.text = "$%.2f".format(0)

                }else{
                    tv_tip_amount_new.text = "$%.2f".format(data.tipPrice!!.toDouble())
                }

            }data?.itemStatus.equals("pending", ignoreCase = true) -> {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                HelperClass.startBackgroundService(this)
            } else {
                HelperClass.startJobDispatcher(this)
            }

            if (data!!.tipPrice.equals("NA")){
                tv_tip_amount_new.text = "$%.2f".format(0)

            }else{
                tv_tip_amount_new.text = "$%.2f".format(data.tipPrice!!.toDouble())
            }

            apply_status_btn.visibility = View.GONE
            change_status_btn.visibility = View.VISIBLE
            currentStatusTxt.text = getString(R.string.pending)
            iv_cour_commission.visibility = View.VISIBLE

            ly_current_status.visibility = View.VISIBLE
            // tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())

            // For Signature
            if (data.signatureStatus == "1") {
                ly_signature.visibility = View.VISIBLE
                ly_signature.setOnClickListener(this)
            }
            if (data.tipPrice.equals("NA")){
                tv_tip_amount_new.text = "$%.2f".format(0)

            }else{
                tv_tip_amount_new.text = "$%.2f".format(data.tipPrice!!.toDouble())
            }


            ly_cour_commission.setOnClickListener(this)

        }
            data?.itemStatus.equals("delivered", ignoreCase = true) -> {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    HelperClass.stopBackgroundService(this)
                } else {
                    HelperClass.stopJobDispatcher(this)
                }

                apply_status_btn.visibility = View.GONE
                change_status_btn.visibility = View.GONE
                currentStatusTxt.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))
                currentStatusTxt.text = getString(R.string.delivered)
                ly_reviews.visibility = View.GONE

                ly_current_status.visibility = View.VISIBLE

                ly_apply_status_btn.visibility = View.GONE
                if (data!!.tipPrice.equals("NA")){
                    tv_tip_amount_new.text = "$%.2f".format(0)

                }else{
                    tv_tip_amount_new.text = "$%.2f".format(data.tipPrice!!.toDouble())
                }
               // tv_review_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())

                /*if (data.requestData?.tip != null && data.requestData?.tip != "") {
                    tv_tip_amount.text = "$%.2f".format(data.requestData!!.tip.toString().toDouble())
                    ly_tip.visibility = View.VISIBLE
                }*/

             /*   if (data.ra == "YES") {
                    getReviewRatting()

                } else if (data.ratingStatus == "NO") {
                    ly_reviews.visibility = View.VISIBLE
                    tv_wait_for_review.visibility = View.VISIBLE
                    ly_set_review.visibility = View.GONE
                }
*/
                ly_cour_amount.setOnClickListener(this)


            }
        }


        if (data!!.requestStatus=="pending"){
            change_status_btn.visibility=View.GONE
        }

    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.iv_back_courier -> {
                onBackPressed()
            }

            R.id.apply_status_btn -> {
                if (isGpsEnable()) {
                    openApplyBidDialog()
                }
            }

            R.id.change_status_btn -> {
                if (isGpsEnable()) {
                    openChangeStatusDialog()
                }
            }

            R.id.applyBtn -> {
                if (isValidBidPrice(openDialog!!.popPriceTxt)) {
                    Constant.hideSoftKeyboard(this)
                    //  doBidRequest(openDialog!!.popPriceTxt.text.toString(), openDialog!!.popProgressBar, openDialog!!)
                    doBidRequest(openDialog!!.popPriceTxt.text.toString(), openDialog!!)
                }
            }

            R.id.iv_map_courier -> {
                val intent = Intent(this, NewCourierMapActivity::class.java)
                intent.putExtra("pickUpLat", pickUpLat)
                intent.putExtra("applyUserId",applyUserId)

                intent.putExtra("pickUpLng", pickUpLng)
                intent.putExtra("deliveryLat", deliveryLat)
                intent.putExtra("deliveryLng", deliveryLng)
                startActivity(intent)
            }

            R.id.rl_picked_status -> {
                courierPickedStatusClick()

                statusDialog!!.rl_picked_status.isEnabled = true
                statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                statusDialog!!.rl_delivered_status.isEnabled = false
               /* if (deliveryStatus == "picked") {
                    courierPickedStatusClick()

                    statusDialog!!.rl_picked_status.isEnabled = true
                    statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                    statusDialog!!.rl_delivered_status.isEnabled = false
                }*/
            }

            R.id.rl_out_for_delivery_status -> {
               /* if (deliveryStatus == "picked") {
                    courierOutForDeliveryStatusClick()

                    statusDialog!!.rl_picked_status.isEnabled = false
                    statusDialog!!.rl_out_for_delivery_status.isEnabled = true
                    statusDialog!!.rl_delivered_status.isEnabled = false
                }*/
              /*  setItemOutForDeliveryDateTime()
                setItemPickupDateTime()*/
                statusDialog!!.rl_delivered_status.isEnabled = false

                statusDialog!!.iv_item_picked_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
                statusDialog!!.iv_item_picked.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_picked_status))
                statusDialog!!.tv_item_picked.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
                statusDialog!!.iv_item_picked_tick.visibility = View.VISIBLE

                statusDialog!!.iv_out_for_delivery_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
                statusDialog!!.iv_out_for_delivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_out_delivery_status))
                statusDialog!!.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
                statusDialog!!.iv_out_for_delivery_tick.visibility = View.VISIBLE

                statusDialog!!.iv_delivered_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
                statusDialog!!.iv_delivered.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_delivered_status))
                statusDialog!!.tv_delivered.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
                statusDialog!!.iv_delivered_tick.visibility = View.GONE

                deliveryStatus = "outForDeliver"
                deliveryStatusTxt = "Out for delivery"

                //courierOutForDeliveryStatusClick()

                statusDialog!!.rl_picked_status.isEnabled = false
                statusDialog!!.rl_out_for_delivery_status.isEnabled = true
                statusDialog!!.rl_delivered_status.isEnabled = false
            }

            R.id.rl_delivered_status -> {
                courierDeliveredStatusClick()

                statusDialog!!.rl_picked_status.isEnabled = false
                statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                statusDialog!!.rl_delivered_status.isEnabled = true
                /*if (deliveryStatus == "outForDeliver") {
                    courierDeliveredStatusClick()

                    statusDialog!!.rl_picked_status.isEnabled = false
                    statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                    statusDialog!!.rl_delivered_status.isEnabled = true
                }*/
            }

            R.id.doneBtn -> {
                Constant.hideSoftKeyboard(this)
                if (deliveryStatus != "") {
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.US)
                    val date = sdf.format(Calendar.getInstance().time)

                    val sdf1 = SimpleDateFormat("HH:mm:ss", Locale.US)
                    val time = sdf1.format(Date())

                    if (myPostResponce.signatureStatus == "1" && deliveryStatus == "delivered" && signatureBitmap == null) {
                        Toast.makeText(this, "Signature is not available", Toast.LENGTH_SHORT).show()

                    } else if (deliveryStatus != "accept" && myPostResponce.requestStatus != deliveryStatus) {
                        changeDeliverStatus(deliveryStatus, date, time)
                        when (deliveryStatus) {
                            "picked" -> myPostResponce.requestStatus = "picked"
                            "outForDeliver" -> myPostResponce.requestStatus = "outForDeliver"
                            "delivered" -> myPostResponce.requestStatus = "delivered"
                        }
                    } else {
                        Toast.makeText(this, "Please select status", Toast.LENGTH_SHORT).show()
                    }

                    statusDialog!!.dismiss()
                }
            }

            R.id.rl_status_cancel -> {
                Constant.hideSoftKeyboard(this)
                statusDialog!!.dismiss()
            }

            R.id.ly_signature -> {
                startActivity(Intent(this, NewTakeSignatureActivity::class.java)
                        .putExtra("POSTID", postId)
                        .putExtra("applyUserId",applyUserId)
                        .putExtra("itembean",myPostResponce)
                        .putExtra("REQUESTID", requestId)
                        .putExtra("FROM", Constant.newPost))
                finish()
            }

            R.id.iv_chat_courier -> {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("otherUID", data.userId.toString())
                intent.putExtra("title", data.title.toString())
                startActivity(intent)
            }

            R.id.ly_cour_amount -> {
                amountCalculationDialog(bidPrice!!, commision!!)
            }

            R.id.ly_cour_commission -> {
                amountCalculationDialog(bidPrice!!, commision!!)
            }
        }
    }

    private fun amountCalculationDialog(bidPrice: Double, commision: Double) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.new_dialog_commission)
        dialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(dialog.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        dialog.window!!.attributes = lWindowParams

        val adminPrice: Double = ((bidPrice * commision) / 100)
        val yourPrice: Double = (bidPrice - adminPrice)
        val totalPrice: Double = (adminPrice + yourPrice)

        dialog.tv_your_amount.text = "$%.2f".format(yourPrice)
        dialog.tv_togo_fee_amount.text = "$%.2f".format(adminPrice)
        dialog.tv_total_amount.text = "$%.2f".format(totalPrice)

        dialog.rl_commission_cancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun openChangeStatusDialog() {
        statusDialog = Dialog(this)
        statusDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        statusDialog!!.setCancelable(false)
        statusDialog!!.setContentView(R.layout.new_dialog_change_status)
        statusDialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(statusDialog!!.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        statusDialog!!.window!!.attributes = lWindowParams

        deliveryStatus = ""
        deliveryStatusTxt = ""
        setRequestStatus()

        statusDialog!!.rl_picked_status.setOnClickListener(this)
        statusDialog!!.rl_out_for_delivery_status.setOnClickListener(this)
        statusDialog!!.rl_delivered_status.setOnClickListener(this)

        statusDialog!!.doneBtn.setOnClickListener(this)

        statusDialog!!.rl_status_cancel.setOnClickListener(this)
        statusDialog!!.show()
    }

    private fun setRequestStatus() {
        when {
            myPostResponce.itemStatus.equals("picked") -> {
                courierPickedStatusClick()
               // setItemOutForDeliveryDateTime()
                //setItemPickupDateTime()

                statusDialog!!.rl_picked_status.isEnabled = true
                statusDialog!!.rl_out_for_delivery_status.isEnabled = true
                statusDialog!!.rl_delivered_status.isEnabled = false

            }

            myPostResponce.itemStatus.equals("pending") -> {
                setAcceptStatusInDialog()
               // setItemOutForDeliveryDateTime()
                //setItemPickupDateTime()
                statusDialog!!.rl_picked_status.isEnabled = true
                statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                statusDialog!!.rl_delivered_status.isEnabled = false

            }
            myPostResponce.itemStatus.equals("outForDeliver") -> {
                courierOutForDeliveryStatusClick()
                //setItemOutForDeliveryDateTime()
               // setItemPickupDateTime()

                statusDialog!!.rl_picked_status.isEnabled = true
                statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                statusDialog!!.rl_delivered_status.isEnabled = true

            }
            myPostResponce.itemStatus.equals("delivered") -> {
                ly_apply_status_btn.visibility = View.GONE
                tv_review_delivery_amount.text = "$%.2f".format(myPostResponce.price)
                ly_reviews.visibility = View.VISIBLE
                ly_cour_amount.setOnClickListener(this)
            }

        }

            /*when {

               *//* myPostResponce.itemStatus!!.equals("accept") -> {
                    setAcceptStatusInDialog()

                    statusDialog!!.rl_picked_status.isEnabled = true
                    statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                    statusDialog!!.rl_delivered_status.isEnabled = false

                }*//*
                myPostResponce.itemStatus!!.equals("picked") -> {
                    courierPickedStatusClick()

                    setItemPickupDateTime()

                    statusDialog!!.rl_picked_status.isEnabled = false
                    statusDialog!!.rl_out_for_delivery_status.isEnabled = true
                    statusDialog!!.rl_delivered_status.isEnabled = false

                }myPostResponce.itemStatus!!.equals("pending") -> {
                setAcceptStatusInDialog()
               // setAcceptStatusInDialog()
               // courierPickedStatusClick()

                //setItemPickupDateTime()
             //   setItemOutForDeliveryDateTime()


                statusDialog!!.rl_picked_status.isEnabled = false
                statusDialog!!.rl_out_for_delivery_status.isEnabled = true
                statusDialog!!.rl_delivered_status.isEnabled = false

            }
                myPostResponce.itemStatus!!.equals("outForDeliver") -> {
                    courierOutForDeliveryStatusClick()

                    setItemPickupDateTime()
                    setItemOutForDeliveryDateTime()

                    statusDialog!!.rl_picked_status.isEnabled = false
                    statusDialog!!.rl_out_for_delivery_status.isEnabled = false
                    statusDialog!!.rl_delivered_status.isEnabled = true

                }
                myPostResponce.itemStatus!!.equals("delivered") -> {
                    ly_apply_status_btn.visibility = View.GONE
                    tv_review_delivery_amount.text = "$%.2f".format(myPostResponce.price)
                    ly_reviews.visibility = View.VISIBLE
                    ly_cour_amount.setOnClickListener(this)
                }
            }*/


    }

    private fun setItemPickupDateTime() {
        val date = formateDateFromstring("yyyy-MM-dd", "MM/dd/yyyy", myPostResponce.pickUpDate!!)

        val inFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        val outFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val time24 = outFormat.format(inFormat.parse(myPostResponce.pickUpTime))

        statusDialog!!.tv_item_picked_date_time.text = date + ", " + time24
        statusDialog!!.tv_item_picked_date_time.visibility = View.VISIBLE
    }

    private fun setItemOutForDeliveryDateTime() {
        val date = formateDateFromstring("yyyy-MM-dd", "MM/dd/yyyy", myPostResponce.outOfDeliveryDate!!)

        val inFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
        val outFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val time24 = outFormat.format(inFormat.parse(myPostResponce.outOfDeliveryTime))

        statusDialog!!.tv_out_for_delivery_date_time.text = date + ", " + time24
        statusDialog!!.tv_out_for_delivery_date_time.visibility = View.VISIBLE
    }

    private fun setAcceptStatusInDialog() {
        statusDialog!!.iv_item_picked_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_item_picked.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_picked_status))
        statusDialog!!.tv_item_picked.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_item_picked_tick.visibility = View.GONE

        statusDialog!!.iv_out_for_delivery_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_out_for_delivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_out_delivey_status))
        statusDialog!!.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_out_for_delivery_tick.visibility = View.GONE

        statusDialog!!.iv_delivered_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_delivered.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_delivered_status))
        statusDialog!!.tv_delivered.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_delivered_tick.visibility = View.GONE

        deliveryStatus = "picked"
        deliveryStatusTxt = "Item picked"

    }

    /*private fun setAcceptStatusInDialog() {
        statusDialog!!.iv_item_picked_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_item_picked.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_picked_status))
        statusDialog!!.tv_item_picked.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_item_picked_tick.visibility = View.GONE

        statusDialog!!.iv_out_for_delivery_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_out_for_delivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_out_delivey_status))
        statusDialog!!.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_out_for_delivery_tick.visibility = View.GONE

        statusDialog!!.iv_delivered_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_delivered.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_delivered_status))
        statusDialog!!.tv_delivered.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_delivered_tick.visibility = View.GONE

        deliveryStatus = "accept"
    }*/

    private fun courierPickedStatusClick() {
        statusDialog!!.iv_item_picked_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
        statusDialog!!.iv_item_picked.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_picked_status))
        statusDialog!!.tv_item_picked.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
        statusDialog!!.iv_item_picked_tick.visibility = View.VISIBLE

        statusDialog!!.iv_out_for_delivery_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_out_for_delivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_out_delivey_status))
        statusDialog!!.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_out_for_delivery_tick.visibility = View.GONE

        statusDialog!!.iv_delivered_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_delivered.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_delivered_status))
        statusDialog!!.tv_delivered.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_delivered_tick.visibility = View.GONE

        deliveryStatus = "picked"
        deliveryStatusTxt = "Item picked"
    }

    private fun courierOutForDeliveryStatusClick() {
        statusDialog!!.iv_item_picked_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
        statusDialog!!.iv_item_picked.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_picked_status))
        statusDialog!!.tv_item_picked.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
        statusDialog!!.iv_item_picked_tick.visibility = View.VISIBLE

        statusDialog!!.iv_out_for_delivery_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
        statusDialog!!.iv_out_for_delivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_out_delivery_status))
        statusDialog!!.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
        statusDialog!!.iv_out_for_delivery_tick.visibility = View.VISIBLE

        statusDialog!!.iv_delivered_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_courier_status_bg))
        statusDialog!!.iv_delivered.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_inactive_delivered_status))
        statusDialog!!.tv_delivered.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
        statusDialog!!.iv_delivered_tick.visibility = View.GONE

        deliveryStatus = "outForDeliver"
        deliveryStatusTxt = "Out for delivery"
    }

    private fun courierDeliveredStatusClick() {
        statusDialog!!.iv_item_picked_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
        statusDialog!!.iv_item_picked.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_picked_status))
        statusDialog!!.tv_item_picked.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
        statusDialog!!.iv_item_picked_tick.visibility = View.VISIBLE

        statusDialog!!.iv_out_for_delivery_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
        statusDialog!!.iv_out_for_delivery.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_out_delivery_status))
        statusDialog!!.tv_out_for_delivery.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
        statusDialog!!.iv_out_for_delivery_tick.visibility = View.VISIBLE

        statusDialog!!.iv_delivered_bg.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_courier_status_bg))
        statusDialog!!.iv_delivered.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.ic_active_delivered_status))
        statusDialog!!.tv_delivered.setTextColor(ContextCompat.getColor(this, R.color.new_active_status_color))
        statusDialog!!.iv_delivered_tick.visibility = View.VISIBLE

        deliveryStatus = "delivered"
        deliveryStatusTxt = "Delivered"
    }

    private fun changeDeliverStatus(deliveryStatus: String, date: String, time: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.DeliveryStatus, Response.Listener { response ->
                val resultResponse = String(response.data)
                //  progressBar?.visibility = View.GONE
                progress!!.dismiss()

                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")

                    if (status == "return") {
                        Constant.returnAlertDialog(this@NewCourierPostDetailsActivity, message)
                        return@Listener
                    }

                    if (status == "success") {
                        currentStatusTxt.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))


                        currentStatusTxt.text = deliveryStatusTxt

                        if (result.getString("requestStatus") == "delivered") {

                           /* if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                HelperClass.stopBackgroundService(this)
                            } else {
                                HelperClass.stopJobDispatcher(this)
                            }*/
                            currentStatusTxt.text = deliveryStatusTxt
                            ly_apply_status_btn.visibility = View.GONE
//                            tv_review_delivery_amount.text = "$%.2f".format(bidPrice.toString().toDouble())
                            ly_reviews.visibility = View.GONE
                            ly_cour_commission.visibility=View.GONE
                            ly_cour_amount.setOnClickListener(this)
                        }

                        if (result.getString("requestStatus") == "picked" || result.getString("requestStatus") == "outForDeliver") {
                            //  startService(Intent(this, MyLocationService::class.java))
                            // HelperClass.startJobDispatcher(this)

                            //  HelperClass.startBackgroundService(this)

                            //   helper!!.startAlarmService(this)

                           /* if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                HelperClass.startBackgroundService(this)
                            } else {
                                HelperClass.startJobDispatcher(this)
                            }*/
                        }

                        getMyNewPost(mainLayout)

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
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                // progressBar.visibility = View.GONE
                progress!!.dismiss()

                if (networkResponse != null) {

                    if (networkResponse.statusCode == 300) {
                     /*   val helper = HelperClass(this, this)
                        helper.sessionExpairDialog()*/
                    } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()


                    val result = String(networkResponse.data)
                    try {
                        val response = JSONObject(result)
                        val message = response.getString("message")

                        Snackbar.make(mainLayout, message, Snackbar.LENGTH_LONG).setAction("ok", null).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                error.printStackTrace()
            }) {

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("authToken", PreferenceConnector.readString(this@NewCourierPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return params
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", myPostResponce.postId!!)
                    params.put("itemStatus", deliveryStatus)
                    params.put("date", date)
                    params.put("itemId", myPostResponce.postItemId!!)
                    params.put("time", time)

                    if (signatureBitmap == null) {
                        params.put("signature", "")
                    }

                    return params
                }

                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (signatureBitmap != null) {
                            params.put("signature", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(signatureBitmap!!), "image/jpg"))
                        }
                        return params
                    }
            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(multipartRequest)
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

    private fun doBidRequest(price: String, dialog: Dialog) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
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
                                Constant.returnAlertDialog(this@NewCourierPostDetailsActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                dialog.dismiss()
                                apply_status_btn.visibility = View.GONE

                                applyBidPriceSuccess("You successfully applied for this post")

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
                        //  popProgressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                /*val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()*/
                            } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@NewCourierPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    params.put("postUserId", postUserId)
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

    override fun onResume() {
        super.onResume()
       // getpostDetails(requestId)

    }

    // location update.....................................................
    override fun onLocationChanged(p0: Location?) {
        lat = p0?.latitude
        lng = p0?.longitude
        if (lat != null && lng != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }
    }

    override fun onConnected(p0: Bundle?) {
        if (lat == null && lng == null) {
            startLocationUpdates()
        }
    }

    override fun onConnectionSuspended(p0: Int) {}

    override fun onConnectionFailed(p0: ConnectionResult) {}

    override fun onStart() {
        super.onStart()
        mGoogleApiClient?.connect()
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient?.disconnect()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                        Constant.ACCESS_FINE_LOCATION)
            } else {
                val pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        } else {
            val pendingResult = LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }

    private fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
    }

    private fun isGpsEnable(): Boolean {
        isGPSEnable = lmgr?.isProviderEnabled(LocationManager.GPS_PROVIDER)!!
        if (!isGPSEnable) {
            val ab = android.support.v7.app.AlertDialog.Builder(this)
            ab.setTitle(R.string.gps_not_enable)
            ab.setMessage(R.string.do_you_want_to_enable)
            ab.setPositiveButton(R.string.settings, { dialog, which ->
                isGPSEnable = true
                val `in` = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(`in`)
            })
            ab.show()
        }
        return isGPSEnable
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

    private fun formateDateFromstring(inputFormat: String, outputFormat: String, inputDate: String): String {
        val parsed: Date?
        var outputDate = ""

        val df_input = SimpleDateFormat(inputFormat, java.util.Locale.getDefault())
        val df_output = SimpleDateFormat(outputFormat, java.util.Locale.getDefault())

        try {
            parsed = df_input.parse(inputDate)
            outputDate = df_output.format(parsed)

        } catch (e: ParseException) {

        }

        return outputDate

    }

    private fun getReviewRatting() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getReviewByPostId,
                    Response.Listener { response ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            if (status == "success") {
                                val gson = Gson()
                                rattingResponce = gson.fromJson(response, RattingResponceBean.ResultBean::class.java)

                                ly_reviews.visibility = View.VISIBLE
                                tv_wait_for_review.visibility = View.GONE
                                ly_set_review.visibility = View.VISIBLE

                                //review_ratingBar.rating = rattingResponce.ratingreview!!.rating.toString().toFloat()

                                when {
                                    rattingResponce.rating!! == "1" -> tv_rating.text = "(Horrible)"
                                    rattingResponce.rating == "2" -> tv_rating.text = "(Bad)"
                                    rattingResponce.rating == "3" -> tv_rating.text = "(Average)"
                                    rattingResponce.rating == "4" -> tv_rating.text = "(Good)"
                                    rattingResponce.rating == "5" -> tv_rating.text = "(Excellent)"
                                }

                              //  tv_review.text = rattingResponce.ratingreview!!.review

                                Glide.with(this).load(data.requestData!!.customerProfileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_customer)

                                tv_customer_name.text = data.requestData!!.customerName
                                rl_customer_info.visibility = View.VISIBLE

                                /* val inFormat = SimpleDateFormat("HH:mm:ss", Locale.US)
                                 val outFormat = SimpleDateFormat("hh:mm a", Locale.US)
                                 val time24 = outFormat.format(inFormat.parse(data.requestData!!.ratingTime))*/

                               // val date = formateDateFromstring("yyyy-MM-dd", "MM/dd/yyyy", rattingResponce.ratingreview!!.ratingDate!!)
                               // tv_rating_time.text = date

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
                               /* val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()*/
                            } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@NewCourierPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
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

    private fun zoomImageDialog(image_url: String) {
        val openDialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.window!!.attributes.windowAnimations = R.style.PauseDialogAnimation
        openDialog.setContentView(R.layout.full_image_view_dialog)
        if (image_url != "") {
            Glide.with(this).load(image_url).apply(RequestOptions().placeholder(R.drawable.chat_image_placeholder)).into(openDialog.photo_view)
        }
        openDialog.iv_back_dialog.setOnClickListener {
            openDialog.dismiss()
        }

        openDialog.show()
    }
}