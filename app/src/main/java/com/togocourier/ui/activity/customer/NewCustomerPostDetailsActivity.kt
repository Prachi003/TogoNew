package com.togocourier.ui.activity.customer

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.Window
import android.view.WindowManager
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
import com.togocourier.adapter.NewCourierAllRequestAdapter
import com.togocourier.responceBean.AllRequestListResponce
import com.togocourier.responceBean.PostDetailsResponce
import com.togocourier.responceBean.RattingResponceBean
import com.togocourier.ui.activity.ChatActivity
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.full_image_view_dialog.*
import kotlinx.android.synthetic.main.new_activity_customer_post_details.*
import kotlinx.android.synthetic.main.new_dialog_give_review.*
import kotlinx.android.synthetic.main.new_dialog_tip_courier_service.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class NewCustomerPostDetailsActivity : AppCompatActivity(), View.OnClickListener,
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private var userType = ""
    private var postId = ""
    private var postUserId = ""
    private var postStatus = ""
    private val gson = Gson()
    private var from = ""
    private var statusAddBank = ""
    private var otherUID = ""
    private var data: PostDetailsResponce.PostDataBean = PostDetailsResponce.PostDataBean()

    private var requestId = ""

    private var rattingResponce = RattingResponceBean()

    //for location update
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var mGoogleApiClient: GoogleApiClient? = null
    private var lat: Double? = null
    private var lng: Double? = null
    private var lmgr: LocationManager? = null

    private var bidPrice: Double? = null
    private var commision: Double? = null

    private var allRequestList: ArrayList<AllRequestListResponce.AppliedReqDataBean>? = null
    private var adapter: NewCourierAllRequestAdapter? = null

    // variable to track event time
    private var mLastClickTime: Long = 0
    var myPostResponce= GetMyPost.DataBean.ItemBean()


    private var openDialog: Dialog? = null
    private var tipCourierDialog: Dialog? = null
    private var ratting: String = ""
    private var review: String = ""
    private var applyUserId: String = ""
    private var paymentType = ""
    private var FROM=""
    private var profileImage=""

    private var clickedOwnTip: Boolean = false
    private var tipAmount: String = ""
    private var averageRating: Float = 0.0f
    private var progress: ProgressDialog? = null
    private var isGPSEnable: Boolean = false

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_customer_post_details)
        mainLayout.visibility = View.GONE
        // progressBar.visibility = View.VISIBLE
        progress = ProgressDialog(this)
        progress!!.show()

        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
        allRequestList = ArrayList()

        val bundle = intent.extras
        /*postId = bundle!!.getString("POSTID")
        from = bundle.getString("FROM")

        requestId = if (bundle.getString("REQUESTID") != null) {
            bundle.getString("REQUESTID")
        } else {
            ""
        }

        paymentType = if (bundle.getString("PaymentTypeBack") != null) {
            bundle.getString("PaymentTypeBack")
        } else {
            ""
        }*/

        if (intent!=null){
            myPostResponce=intent.getParcelableExtra("itembean")
            applyUserId=intent.getStringExtra("applyUserId")
            profileImage=intent.getStringExtra("profileImage")

            FROM=intent.getStringExtra("FROM")
        }

        if(FROM.equals("PendingCourier")){
            llChnageStatus.visibility=View.VISIBLE
        }else{
            llChnageStatus.visibility=View.GONE
        }
        updateUi(myPostResponce)

        //location update............
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        iv_back_cust.setOnClickListener(this)

       /* adapter = NewCourierAllRequestAdapter(this@NewCustomerPostDetailsActivity,
                allRequestList, object : AcceptRejectListioner {
            override fun OnClick(id: String, status: String, bitPrice: String) {
                if (status == "cancel") {
                    acceptRejectRequest(id, status)
                } else if (status == "accept") {
                    paymentType = "normal"
                    val intent = Intent(this@NewCustomerPostDetailsActivity, NewPaymentListActivity::class.java)
                    intent.putExtra("requestId", id)
                    intent.putExtra("bitPrice", bitPrice)
                    intent.putExtra("tipPrice", "")
                    intent.putExtra("paymentType", "normal")
                    intent.putExtra("postId", postId)
                    startActivity(intent)
                }
            }

            override fun OnClickUserId(userId: String, postUserId: String?) {

            }
        })*/
        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        requestPstReclr.layoutManager = layoutManager
        requestPstReclr.adapter = adapter
        //getAllRequest()
    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.iv_back_cust -> {
                onBackPressed()
            }

            R.id.tv_see_all_request -> {
                val intent = Intent(this, AllRequestActivity::class.java)
                intent.putExtra("POSTID", postId)
                startActivity(intent)
            }

            R.id.tv_write_review -> {
                openGiveReviewDialog()
            }

            R.id.ly_horrible -> {
                setHorribleRating()
                ratting = "1"
            }

            R.id.ly_bad -> {
                setBadRating()
                ratting = "2"
            }

            R.id.ly_average -> {
                setAverageRating()
                ratting = "3"
            }

            R.id.ly_good -> {
                setGoodRating()
                ratting = "4"
            }

            R.id.ly_exellent -> {
                setExcellentRating()
                ratting = "5"
            }

            R.id.submitBtn -> {
                review = openDialog!!.ed_write_review.text.toString()

                when {
                    ratting == "" -> Toast.makeText(this, "Please give rating", Toast.LENGTH_SHORT).show()
                    review == "" -> Toast.makeText(this, "Please give review", Toast.LENGTH_SHORT).show()
                    else -> {
                        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.US)
                        val date = sdf.format(Calendar.getInstance().time)

                        val sdf1 = SimpleDateFormat("HH:mm:ss", Locale.US)
                        val time = sdf1.format(Date())

                        //saveRatting(date, time)
                    }
                }
            }

            R.id.tip_courier_btn -> {
                openTipCourierServiceDialog()
            }

            R.id.tv_dollar_5 -> {
                tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))
                tipCourierDialog!!.et_tip_price.isEnabled = false

                tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_regular)

                tipAmount = "5"
            }

            R.id.tv_dollar_4 -> {
                tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))
                tipCourierDialog!!.et_tip_price.isEnabled = false

                tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_regular)

                tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_light)

                tipAmount = "4"
            }

            R.id.tv_dollar_3 -> {
                tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))
                tipCourierDialog!!.et_tip_price.isEnabled = false

                tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_regular)

                tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_light)

                tipAmount = "3"

            }

            R.id.tv_dollar_2 -> {
                tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))
                tipCourierDialog!!.et_tip_price.isEnabled = false

                tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_regular)

                tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_light)

                tipAmount = "2"

            }

            R.id.tv_dollar_1 -> {
                tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))
                tipCourierDialog!!.et_tip_price.isEnabled = false

                tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_regular)

                tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_light)

                tipAmount = "1"

            }

            R.id.tv_dollar_0 -> {
                tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))
                tipCourierDialog!!.et_tip_price.isEnabled = false

                tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_regular)

                tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_light)

                tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_light)

                tipAmount = "0"

            }

            R.id.iv_own_tip_toggle_btn -> {
                if (!clickedOwnTip) {
                    clickedOwnTip = true
                    tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_yes_ico))
                    tipCourierDialog!!.et_tip_price.isEnabled = true

                    tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_0.isEnabled = false
                    tipCourierDialog!!.tv_dollar_1.isEnabled = false
                    tipCourierDialog!!.tv_dollar_2.isEnabled = false
                    tipCourierDialog!!.tv_dollar_3.isEnabled = false
                    tipCourierDialog!!.tv_dollar_4.isEnabled = false
                    tipCourierDialog!!.tv_dollar_5.isEnabled = false

                } else {
                    clickedOwnTip = false

                    tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))
                    tipCourierDialog!!.et_tip_price.setText("")
                    tipCourierDialog!!.et_tip_price.isEnabled = false

                    tipCourierDialog!!.tv_dollar_0.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_0, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_1.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_1, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_2.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_2, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_3.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_3, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_4.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_4, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_5.setTextColor(ContextCompat.getColor(this, R.color.new_inactive_dollar_color))
                    Constant.setTypeface(tipCourierDialog!!.tv_dollar_5, this, R.font.rubik_light)

                    tipCourierDialog!!.tv_dollar_0.isEnabled = true
                    tipCourierDialog!!.tv_dollar_1.isEnabled = true
                    tipCourierDialog!!.tv_dollar_2.isEnabled = true
                    tipCourierDialog!!.tv_dollar_3.isEnabled = true
                    tipCourierDialog!!.tv_dollar_4.isEnabled = true
                    tipCourierDialog!!.tv_dollar_5.isEnabled = true
                }
            }

            R.id.payTipBtn -> {
                if (clickedOwnTip) {
                    tipAmount = tipCourierDialog!!.et_tip_price.text.toString().trim()
                }

                if (tipAmount != "" && tipAmount != "0") {
                    paymentType = "tip"
                    val intent = Intent(this@NewCustomerPostDetailsActivity, NewPaymentListActivity::class.java)
                    intent.putExtra("postId", postId)
                    intent.putExtra("requestId", requestId)
                    intent.putExtra("bitPrice", data.requestData!!.bidPrice)
                    intent.putExtra("tipPrice", tipAmount)
                    intent.putExtra("paymentType", "tip")
                    startActivity(intent)
                } else if (tipAmount == "0") {
                    cardPay()
                } else {
                    Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
                }

            }

            R.id.itemChatIcon -> {
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("otherUID", data.requestData!!.applyUserId)
                intent.putExtra("title", data.title)
                startActivity(intent)
            }

            R.id.iv_track_map -> {
                if (isGpsEnable()) {
                    val intent = Intent(this, LocationActivity::class.java)
                    intent.putExtra("applyUserId",applyUserId)
                    intent.putExtra("pickupLat",myPostResponce.pickupLat)
                    intent.putExtra("pickupLong",myPostResponce.pickupLong)
                    intent.putExtra("profileImage",profileImage)
                    intent.putExtra("deliveryLat",myPostResponce.deliverLat)
                    intent.putExtra("deliverLong",myPostResponce.deliverLong)
                    intent.putExtra("postId", data.postId)
                    startActivity(intent)
                }
            }
        }
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

    private fun getpostDetails(requestId: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Get_Post_Details_Url,
                    Response.Listener { response ->
                        val result: JSONObject?

                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status == "return") {
                                Constant.returnAlertDialog(this@NewCustomerPostDetailsActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                val postDetailsResponce = gson.fromJson(response, PostDetailsResponce::class.java)
                                statusAddBank = postDetailsResponce.getPostData()?.addBank!!
                                //updateUi(postDetailsResponce)

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

                                //  progressBar.visibility = View.GONE
                                progress!!.dismiss()
                                mainLayout.visibility = View.VISIBLE
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // progressBar.visibility = View.GONE
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
                    header.put("authToken", PreferenceConnector.readString(this@NewCustomerPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    params.put("requestId", requestId)
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

    private fun updateUi(postDetailsResponce: GetMyPost.DataBean.ItemBean) {
       // data = postDetailsResponce?.getPostData()!!
        postId = postDetailsResponce.postId!!
        postUserId = postDetailsResponce.userId!!
        txtBidPriceN.text=postDetailsResponce.price
        //...........for chat
       // otherUID = myPostResponce.applyUserId.toString()

        //for bid price
       /* if (!TextUtils.isEmpty(bidPrice.toString()) && !TextUtils.isEmpty(myPostResponce.commision.toString())) {
            bidPrice = data.requestData?.bidPrice?.toDouble()
            commision = data.requestData?.commision?.toDouble()
        }*/

        Glide.with(this).load(postDetailsResponce.itemImageUrl).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_item_image)
        tv_item_name_new.text=postDetailsResponce.itemTitle
        tv_delivery_amount.text="$"+postDetailsResponce.price

        postStatus = postDetailsResponce.itemStatus!!
       /* if (!TextUtils.isEmpty(postDetailsResponce.itemImage)) {

            if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                !isDestroyed
            } else {
                TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
            }) {
                Glide.with(this).load(postDetailsResponce.itemImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_item_image)
            }
        }*/
       // tv_item_name_new.text = data.title
        tv_item_quantity.text = "Qty: " + postDetailsResponce.itemQuantity

        if(postDetailsResponce.description.equals("")){
            tv_description.text = getString(R.string.na_txt)
        }else{
            tv_description.text = postDetailsResponce.description
        }

        if (postDetailsResponce.itemStatus.equals("new")) {
            currentStatusLay.visibility = View.GONE
            tv_delivery_amount.text = "$ %.2f".format(postDetailsResponce.price.toString().toDouble())
        } else {
           applyButtonManageCustomer(postDetailsResponce)
            currentStatusLay.visibility = View.VISIBLE

        }

        tv_pickup_date_time.text = postDetailsResponce.collectiveDate + ", " + Constant.setTimeFormat(postDetailsResponce.collectiveTime!!)
        tv_delivery_date_time.text = postDetailsResponce.deliveryDate + ", " + Constant.setTimeFormat(postDetailsResponce.deliveryTime!!)
        tv_pickup_address.text = postDetailsResponce.pickupAdrs
        tv_delivery_address.text = postDetailsResponce.deliveryAdrs

        val senderName = postDetailsResponce.senderName!!.substring(0, 1).toUpperCase() + postDetailsResponce.senderName!!.substring(1)
        tv_pickup_person_name.text = senderName

        val name = postDetailsResponce.receiverName!!.substring(0, 1).toUpperCase() + postDetailsResponce.receiverName!!.substring(1)
        tv_delivery_person_name.text = name

        if (postDetailsResponce.senderContactNo.equals("")) {
            ly_pickup_someone_contact.visibility = View.GONE
            tv_delivery_contact_na.visibility = View.VISIBLE
        } else {
            tv_pickup_person_contact.text = postDetailsResponce.senderContactNo
            ly_pickup_someone_contact.visibility = View.VISIBLE
            tv_delivery_contact_na.visibility = View.GONE
        }

        if (postDetailsResponce.receiverContactNo.equals("")) {
            ly_someone_contact.visibility = View.GONE
            tv_delivery_contact_na.visibility = View.VISIBLE
        } else {
            tv_delivery_person_contact.text = postDetailsResponce.receiverContactNo
            ly_someone_contact.visibility = View.VISIBLE
            tv_delivery_contact_na.visibility = View.GONE
        }

        // progressBar.visibility = View.GONE
        progress!!.dismiss()
        mainLayout.visibility = View.VISIBLE

        /*if (myPostResponce.requestData != null && data.requestData!!.avgRating != null && data.requestData!!.avgRating != "") {
            averageRating = Math.round(data.requestData!!.avgRating!!.toFloat()).toFloat()
            ratingBar.rating = averageRating
            review_ratingBar.rating = averageRating

        }

        if (data.ratingStatus.equals("YES")) {
            getReviewRatting()
        }*/

        /*if (data.signatureImage != null && data.signatureImage != Constant.signatureUrl) {
            Glide.with(this).load(data.signatureImage).into(iv_signature)
            iv_signature.visibility = View.VISIBLE
            ly_signature.visibility = View.VISIBLE
        }*/

        if (postDetailsResponce.signatureStatus == "0") {
            ly_signature.visibility = View.GONE

        } else if (postDetailsResponce.signatureStatus == "1") {
           /* if (postDetailsResponce.requestStatus!!.equals("delivered")) {
                if (postDetailsResponce.pickUpDate == "0000-00-00") {
                    ly_display_signature.visibility = View.GONE
                    tv_no_signature_admin.visibility = View.VISIBLE
                    ly_signature.visibility = View.VISIBLE
                } else {
                    if (postDetailsResponce.signatureImage != null && postDetailsResponce.signatureImage != Constant.signatureUrl) {
                        tv_no_signature_admin.visibility = View.GONE
                        Glide.with(this).load(postDetailsResponce.signatureImage).into(iv_signature)
                        iv_signature.visibility = View.VISIBLE
                        ly_signature.visibility = View.VISIBLE
                        ly_signature.isEnabled = false

                    }
                }
            }*/
        }


            iv_item_image.setOnClickListener {
                zoomImageDialog(postDetailsResponce.itemImageUrl!!)
            }

    }

    private fun applyButtonManageCustomer(data: GetMyPost.DataBean.ItemBean) {
        when {
            data.itemStatus!!.equals("new") -> {
                currentStatusTxt.text = getString(R.string.na_txt)
                ly_received_request.visibility = View.VISIBLE
                tv_delivery_amount.text = "$%.2f".format(data.price.toString().toDouble())
                ly_assigned_courier.visibility = View.GONE
                rl_tip.visibility = View.VISIBLE
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount.setText("0$")
                }else{
                    tv_tip_amount.text=  "$%.2f".format(data.tipPrice.toString().toDouble())
                }

                iv_track_map.visibility = View.GONE

            }
            data.itemStatus!!.equals("pending") -> {
                currentStatusTxt.text = getString(R.string.pending)
                ly_received_request.visibility = View.GONE
                ly_assigned_courier.visibility = View.GONE
                rl_tip.visibility = View.VISIBLE
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount.setText("0$")
                }else{
                    tv_tip_amount.text=  "$%.2f".format(data.tipPrice.toString().toDouble())
                }


                //tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())
                iv_track_map.visibility = View.GONE

            }
            data.itemStatus!!.equals("accept") -> {
                currentStatusTxt.text = getString(R.string.accepted)
                ly_received_request.visibility = View.GONE
                setAssignedCourierData()
                ly_assigned_courier.visibility = View.VISIBLE
                itemChatIcon.setOnClickListener(this)
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount.setText("0$")
                }else{
                    tv_tip_amount.text=  "$%.2f".format(data.tipPrice.toString().toDouble())
                }

              //  tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())
                iv_track_map.visibility = View.VISIBLE

            }
            data.itemStatus!!.equals("picked") -> {
                currentStatusTxt.text = getString(R.string.item_picked)
                ly_received_request.visibility = View.GONE
                setAssignedCourierData()
                ly_assigned_courier.visibility = View.GONE
                itemChatIcon.setOnClickListener(this)
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount.setText("0$")
                }else{
                    tv_tip_amount.text=  "$%.2f".format(data.tipPrice.toString().toDouble())
                }

               // tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())
                iv_track_map.visibility = View.VISIBLE

            }
            data.itemStatus!!.equals("outForDeliver") -> {
                currentStatusTxt.text = getString(R.string.out_for_delivery)
                ly_received_request.visibility = View.GONE
                setAssignedCourierData()
                ly_assigned_courier.visibility = View.GONE
                itemChatIcon.setOnClickListener(this)
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount.setText("0$")
                }else{
                    tv_tip_amount.text=  "$%.2f".format(data.tipPrice.toString().toDouble())
                }

              //  tv_delivery_amount.text = "$%.2f".format(data.requestData!!.bidPrice.toString().toDouble())
                iv_track_map.visibility = View.VISIBLE

            }
            data.itemStatus!!.equals("delivered") -> {
                currentStatusTxt.text = getString(R.string.delivered)
                currentStatusTxt.setTextColor(ContextCompat.getColor(this,R.color.colorGreen))
                ly_received_request.visibility = View.GONE
                ly_assigned_courier.visibility = View.GONE
                setReviewRatingData()
                ly_give_review.visibility = View.GONE
                tv_write_review.setOnClickListener(this)
                if (data.tipPrice.equals("NA")){
                    tv_tip_amount.setText("0$")
                }else{
                    tv_tip_amount.text=  "$%.2f".format(data.tipPrice.toString().toDouble())
                }

                /*if (data.requestStatus!!.equals.tip == "") {
                    tip_courier_btn.visibility = View.VISIBLE
                    rl_tip.visibility = View.GONE
                    tip_courier_btn.setOnClickListener(this)
                } else {
                    tip_courier_btn.visibility = View.GONE
                    tv_tip_amount.text = "$%.2f".format(data?.requestData?.tip.toString().toDouble())
                    rl_tip.visibility = View.VISIBLE
                }*/

               // tv_delivery_amount.text = "$%.2f".format(data!!.requestData!!.bidPrice.toString().toDouble())
                iv_track_map.visibility = View.GONE
            }
        }

        iv_track_map.setOnClickListener(this)
    }

    private fun setAssignedCourierData() {
        if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !isDestroyed
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
        }) {
           /* Glide.with(this).load(data.requestData!!.courierProfileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_assigned_courier)

            tv_courier_name.text = data.requestData!!.drop_off_person
            tv_courier_contact.text = data.requestData!!.drop_off_contact
            ratingBar.rating = averageRating
            review_ratingBar.rating = averageRating
            rl_tip.visibility = View.GONE*/
        }
    }

    private fun setReviewRatingData() {
       /* if (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            !isDestroyed
        } else {
            TODO("VERSION.SDK_INT < JELLY_BEAN_MR1")
        }) {
            Glide.with(this).load(data.requestData!!.courierProfileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_assigned_courier_img)
        }

        tv_ass_courier_name.text = data.requestData!!.drop_off_person

        if (data.ratingStatus.equals("YES")) {
            Constant.hideSoftKeyboard(this)
            tv_give_review_heading.text = getString(R.string.view_review)
            tv_write_review.text = getString(R.string.view_review)

            if (openDialog != null) {
                openDialog!!.submitBtn.visibility = View.GONE
                openDialog!!.tv_give_review.text = getString(R.string.view_review)
                openDialog!!.ly_horrible.isEnabled = false
                openDialog!!.ly_bad.isEnabled = false
                openDialog!!.ly_average.isEnabled = false
                openDialog!!.ly_good.isEnabled = false
                openDialog!!.ly_exellent.isEnabled = false

                review_ratingBar.rating = averageRating
                openDialog!!.ly_review_stars.isEnabled = false
                if (rattingResponce.ratingreview!!.review != null) {
                    openDialog!!.ed_write_review.setText(rattingResponce.ratingreview!!.review)
                }
                openDialog!!.ed_write_review.isEnabled = false

                when (rattingResponce.ratingreview?.rating) {
                    "1" -> {
                        setHorribleRating()
                    }
                    "2" -> {
                        setBadRating()
                    }
                    "3" -> {
                        setAverageRating()
                    }
                    "4" -> {
                        setGoodRating()
                    }
                    "5" -> {
                        setExcellentRating()
                    }
                }
            }
        } else {
            review_ratingBar.rating = averageRating
        }

        if (data.requestData?.tip == "") {
            tip_courier_btn.visibility = View.VISIBLE
            tip_courier_btn.setOnClickListener(this)
        } else {
            tip_courier_btn.visibility = View.GONE
        }*/
    }

    private fun setHorribleRating() {
        openDialog!!.tv_horrible.visibility = View.VISIBLE
        openDialog!!.tv_bad.visibility = View.GONE
        openDialog!!.tv_average.visibility = View.GONE
        openDialog!!.tv_good.visibility = View.GONE
        openDialog!!.tv_exellent.visibility = View.GONE

        openDialog!!.iv_horrible.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_bad.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
        openDialog!!.iv_average.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
        openDialog!!.iv_good.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
        openDialog!!.iv_exellent.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
    }

    private fun setBadRating() {
        openDialog!!.tv_horrible.visibility = View.GONE
        openDialog!!.tv_bad.visibility = View.VISIBLE
        openDialog!!.tv_average.visibility = View.GONE
        openDialog!!.tv_good.visibility = View.GONE
        openDialog!!.tv_exellent.visibility = View.GONE

        openDialog!!.iv_horrible.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_bad.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_average.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
        openDialog!!.iv_good.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
        openDialog!!.iv_exellent.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
    }

    private fun setAverageRating() {
        openDialog!!.tv_horrible.visibility = View.GONE
        openDialog!!.tv_bad.visibility = View.GONE
        openDialog!!.tv_average.visibility = View.VISIBLE
        openDialog!!.tv_good.visibility = View.GONE
        openDialog!!.tv_exellent.visibility = View.GONE

        openDialog!!.iv_horrible.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_bad.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_average.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_good.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
        openDialog!!.iv_exellent.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
    }

    private fun setGoodRating() {
        openDialog!!.tv_horrible.visibility = View.GONE
        openDialog!!.tv_bad.visibility = View.GONE
        openDialog!!.tv_average.visibility = View.GONE
        openDialog!!.tv_good.visibility = View.VISIBLE
        openDialog!!.tv_exellent.visibility = View.GONE

        openDialog!!.iv_horrible.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_bad.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_average.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_good.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_exellent.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_inactive_ico))
    }

    private fun setExcellentRating() {
        openDialog!!.tv_horrible.visibility = View.GONE
        openDialog!!.tv_bad.visibility = View.GONE
        openDialog!!.tv_average.visibility = View.GONE
        openDialog!!.tv_good.visibility = View.GONE
        openDialog!!.tv_exellent.visibility = View.VISIBLE

        openDialog!!.iv_horrible.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_bad.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_average.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_good.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
        openDialog!!.iv_exellent.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.star_active_ico))
    }

    private fun openGiveReviewDialog() {
        openDialog = Dialog(this)
        openDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog!!.setCancelable(false)
        openDialog!!.setContentView(R.layout.new_dialog_give_review)
        openDialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog!!.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog!!.window!!.attributes = lWindowParams

        setReviewRatingData()

        openDialog!!.ly_horrible.setOnClickListener(this)
        openDialog!!.ly_bad.setOnClickListener(this)
        openDialog!!.ly_average.setOnClickListener(this)
        openDialog!!.ly_good.setOnClickListener(this)
        openDialog!!.ly_exellent.setOnClickListener(this)

        openDialog!!.submitBtn.setOnClickListener(this)

        openDialog!!.rl_cancel.setOnClickListener({
            Constant.hideSoftKeyboard(this)
            openDialog!!.dismiss()

        })
        openDialog!!.show()
    }

    private fun saveRatting(date: String, time: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            //  openDialog!!.popProgressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.saveReviewRating,
                    Response.Listener { response ->
                        // openDialog!!.popProgressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "return") {
                                Constant.returnAlertDialog(this@NewCustomerPostDetailsActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                data.ratingStatus = "YES"
                                getReviewRatting()

                                //getpostDetails(requestId)
                                openDialog!!.dismiss()

                            } else {
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        //  openDialog!!.popProgressBar.visibility = View.GONE
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
                    header.put("authToken", PreferenceConnector.readString(this@NewCustomerPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("receiverId", data.requestData?.applyUserId.toString())
                    params.put("postId", postId)
                    params.put("requestId", requestId)
                    params.put("review", review)
                    params.put("rating", ratting)
                    params.put("ratingDate", date)
                    params.put("ratingTime", time)
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

    private fun getReviewRatting() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getReviewByPostId,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            if (status == "success") {
                                val gson = Gson()
                                rattingResponce = gson.fromJson(response, RattingResponceBean::class.java)
                                review_ratingBar.rating = averageRating
                                setReviewRatingData()

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
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@NewCustomerPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
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

    private fun openTipCourierServiceDialog() {
        tipCourierDialog = Dialog(this)
        tipCourierDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        tipCourierDialog!!.setCancelable(false)
        tipCourierDialog!!.setContentView(R.layout.new_dialog_tip_courier_service)
        tipCourierDialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(tipCourierDialog!!.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        tipCourierDialog!!.window!!.attributes = lWindowParams

        tipCourierDialog!!.et_tip_price.isEnabled = false
        clickedOwnTip = false
        tipCourierDialog!!.iv_own_tip_toggle_btn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_no_ico))

        tipCourierDialog!!.tv_dollar_0.setOnClickListener(this)
        tipCourierDialog!!.tv_dollar_1.setOnClickListener(this)
        tipCourierDialog!!.tv_dollar_2.setOnClickListener(this)
        tipCourierDialog!!.tv_dollar_3.setOnClickListener(this)
        tipCourierDialog!!.tv_dollar_4.setOnClickListener(this)
        tipCourierDialog!!.tv_dollar_5.setOnClickListener(this)

        tipCourierDialog!!.iv_own_tip_toggle_btn.setOnClickListener(this)

        tipCourierDialog!!.payTipBtn.setOnClickListener(this)

        tipCourierDialog!!.rl_tip_cancel.setOnClickListener({
            Constant.hideSoftKeyboard(this)
            tipCourierDialog!!.dismiss()

        })
        tipCourierDialog!!.show()
    }

    override fun onResume() {
        super.onResume()
      //  getpostDetails(requestId)
        //getAllRequest()

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

    private fun getAllRequest() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            //  progressBar.visibility = View.VISIBLE
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

                            if (status == "return") {
                                Constant.returnAlertDialogToMainActivity(this@NewCustomerPostDetailsActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                val count = result.getString("total")
                                val allrequeatListResponce = gson.fromJson(response, AllRequestListResponce::class.java)
                                allRequestList?.clear()
                                allRequestList?.addAll(allrequeatListResponce.getAppliedReqData()!!)

                                tv_see_all_request.visibility = View.VISIBLE
                                tv_see_all_request.setOnClickListener(this)

                                /* for (i in 0..allrequeatListResponce.getAppliedReqData()!!.size - 1) {
                                     if (i > 2) {
                                         tv_request_count.text = "+" + (count.toString().toInt() - 3)
                                         ly_request_count.visibility = View.VISIBLE
                                         ly_request_count.setOnClickListener(this)
                                         break
                                     } else {
                                         ly_request_count.visibility = View.GONE
                                     }
                                     val data = allrequeatListResponce.getAppliedReqData()!![i]
                                     allRequestList?.add(data)
                                 }*/

                                adapter?.notifyDataSetChanged()
                                rl_all_request.isEnabled = true
                                noDataTxt.visibility = View.GONE
                            } else {
                                allRequestList?.clear()
                                adapter?.notifyDataSetChanged()

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        rl_all_request.isEnabled = false
                                        tv_see_all_request.visibility = View.GONE
                                        noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    rl_all_request.isEnabled = false
                                    tv_see_all_request.visibility = View.GONE
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

                        // Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@NewCustomerPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
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
        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun cardPay() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.StripPay,
                    Response.Listener { response ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status == "return") {
                                Constant.returnAlertDialogToMainActivity(this@NewCustomerPostDetailsActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                tip_courier_btn.visibility = View.GONE
                                getpostDetails(requestId)
                                tipCourierDialog!!.dismiss()
                            } else {

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(mainLayout, "You have entered wrong parameter")
                                    }
                                } else {
                                    Constant.snackbar(mainLayout, "You have entered wrong parameter")
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
                    header.put("authToken", PreferenceConnector.readString(this@NewCustomerPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email", "")

                    params.put("amount", "0")
                    params.put("requestId", requestId)
                    params.put("paymentType", "tip")
                    params.put("payType", "")
                    params.put("saveDetail", "")
                    params.put("firstName", "")
                    params.put("lastName", "")
                    params.put("holderName", "")
                    params.put("dob", "")
                    params.put("country", "US")
                    params.put("currency", "USD")
                    params.put("routingNumber", "")
                    params.put("accountNo", "")
                    params.put("address", "")
                    params.put("postalCode", "")
                    params.put("city", "")
                    params.put("state", "")
                    params.put("ssnLast", "")
                    params.put("card_number", "")
                    params.put("exp_month", "")
                    params.put("exp_year", "")
                    params.put("cvv", "")
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

                            if (status == "return") {
                                Constant.returnAlertDialogToMainActivity(this@NewCustomerPostDetailsActivity, message)
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
                    header.put("authToken", PreferenceConnector.readString(this@NewCustomerPostDetailsActivity, PreferenceConnector.USERAUTHTOKEN, ""))
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
        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (paymentType == "normal") {
            finish()
        } else if (paymentType == "tip") {
            val intent = Intent(this@NewCustomerPostDetailsActivity, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        } else {
            finish()
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