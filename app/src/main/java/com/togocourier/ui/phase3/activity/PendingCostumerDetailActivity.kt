package com.togocourier.ui.phase3.activity

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.GridLayoutManager
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.util.Strings.capitalize
import com.google.gson.Gson
import com.togocourier.Interface.PostOnClick
import com.togocourier.R
import com.togocourier.adapter.NewCourierAllRequestAdapter
import com.togocourier.responceBean.RattingResponceBean
import com.togocourier.ui.activity.ChatActivity
import com.togocourier.ui.activity.courier.NewCourierPostDetailsActivity
import com.togocourier.ui.activity.customer.NewCustomerPostDetailsActivity
import com.togocourier.ui.activity.customer.NewPaymentListActivity
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.ui.phase3.adapter.CourierPostItemdetailAdapter
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_pending_courier_detail.*
import kotlinx.android.synthetic.main.activity_pending_courier_detail.view.*
import kotlinx.android.synthetic.main.new_activity_customer_post_details.*
import kotlinx.android.synthetic.main.new_dialog_give_review.*
import kotlinx.android.synthetic.main.new_dialog_tip_courier_service.*
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class PendingCostumerDetailActivity : AppCompatActivity(), View.OnClickListener, PostOnClick {
    var POSTID = ""
    private var progress: ProgressDialog? = null
    private var gson = Gson()
    private var mLastClickTime: Long = 0
    private var myPostArrayList: ArrayList<GetMyPost.DataBean.ItemBean>? = null
    private var requestBeanArrayList: ArrayList<GetMyPost.DataBean.RequestsBean>? = null
    private lateinit var courierPostItemAdapter: CourierPostItemdetailAdapter
    private var FROM = ""
    private var tipCourierDialog: Dialog? = null
    var myPostResponceN = GetMyPost.DataBean.ItemBean()
    private var userId = ""
    private var clickedOwnTip: Boolean = false
    private var paymentType = ""
    //private var rattingResponce = RattingResponceBean.ResultBean()

    private var openDialog: Dialog? = null
    private var review: String = ""
    private var tipAmount: String = ""
    private var txt_no_data: TextView? = null
    private var ratting: String = ""
    private var profileImage: String = ""

    var myPostResponce = GetMyPost.DataBean()

    private var count = -1
    private var adapter: NewCourierAllRequestAdapter? = null


    override fun delete(position: Int, itemView: View) {

    }

    override fun GetPosition(position: Int) {
        if (FROM.equals("Courier")) {
            val intent = Intent(this, NewCourierPostDetailsActivity::class.java)
            val itemBean: GetMyPost.DataBean.ItemBean = myPostArrayList!!.get(position)
            intent.putExtra("FROM", FROM)
            intent.putExtra("applyUserId", myPostResponce.applyUserId)
            intent.putExtra("itembean", itemBean)
            startActivity(intent)
        } else {
            val intent = Intent(this, NewCustomerPostDetailsActivity::class.java)
            val itemBean: GetMyPost.DataBean.ItemBean = myPostArrayList!!.get(position)
            intent.putExtra("FROM", FROM)

            intent.putExtra("applyUserId", myPostResponce.applyUserId)
            intent.putExtra("profileImage", profileImage)

            intent.putExtra("itembean", itemBean)
            startActivity(intent)
        }


    }

    override fun onClick(v: View?) {
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        when (v!!.id) {
            R.id.backImgN -> {
                onBackPressed()
            }

            R.id.txtReview -> {
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

            R.id.tip_courier_btn_new -> {
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
                    tipCourierDialog!!.dismiss()
                    val intent = Intent(this@PendingCostumerDetailActivity, NewPaymentListActivity::class.java)
                    intent.putExtra("postId", POSTID)
                    intent.putExtra("postItemId", myPostResponceN.postItemId)
                    intent.putExtra("requestId", myPostResponce.applyUserId)
                    intent.putExtra("courierId",  myPostResponce.applyUserId)
                    intent.putExtra("FROM", "bid")
                    intent.putExtra("bitPrice", "")
                    intent.putExtra("tipPrice", tipAmount)
                    intent.putExtra("paymentType", "tip")
                    startActivity(intent)
                } else if (tipAmount == "0") {
                    cardPay()
                } else {
                    Toast.makeText(this, "Please enter amount", Toast.LENGTH_SHORT).show()
                }

            }

            R.id.cardChatn -> {

                val intent = Intent(this@PendingCostumerDetailActivity, ChatActivity::class.java)
                intent.putExtra("otherUID", myPostResponce.applyUserId)
                intent.putExtra("title", myPostResponce.postTitle)
                startActivity(intent)

/*
                val intent = Intent(this, ChatActivity::class.java)
                intent.putExtra("otherUID", myPostResponce.applyUserId)
                intent.putExtra("title",myPostResponce.postTitle)
                startActivity(intent)
*/

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

                        saveRatting()
                    }
                }
            }


        }
    }

    private fun saveRatting() {
        if (Constant.isNetworkAvailable(this, rlParentPending)) {
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
                                Constant.returnAlertDialog(this, message)
                                return@Listener
                            }

                            if (status == "success") {
                                /*data.ratingStatus = "YES"
                                getReviewRatting()*/

                                getMyNewPost(rlParentPending)

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
/*
                                val helper = HelperClass(this, this)
*/
/*
                                helper.sessionExpairDialog()
*/
                            } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PendingCostumerDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("receiverId", myPostResponce.applyUserId!!)
                    params.put("postId", myPostResponce.postId!!)
                    params.put("review", review)
                    params.put("rating", ratting)
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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent != null) {
            POSTID = intent.getStringExtra("POSTID")
            FROM = intent.getStringExtra("FROM")
            userId = intent.getStringExtra("userId")
        }
        setContentView(R.layout.activity_pending_courier_detail)
        initView()


    }

    fun initView() {
        if (FROM.equals("Courier")) {

            llAssignedCourier.visibility = View.GONE
        } else {
            llAssignedCourier.visibility = View.VISIBLE
        }
        txt_no_data = findViewById(R.id.txt_no_data)
        progress = ProgressDialog(this)
        myPostArrayList = ArrayList()
        courierPostItemAdapter = CourierPostItemdetailAdapter(this, myPostArrayList!!, this, "pending")
        recyclerPostDetailN.layoutManager = GridLayoutManager(this, 2)
        recyclerPostDetailN.adapter = courierPostItemAdapter
        backImgN.setOnClickListener(this)
        txtReview.setOnClickListener(this)
        cardChatn.setOnClickListener(this)
        tip_courier_btn_new.setOnClickListener(this)

        //getMyNewPost(rlParentPending)


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

        //setReviewRatingData(rattingResponce)

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

    private fun setReviewRatingData(rattingResponce: RattingResponceBean.ResultBean) {


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

        openDialog!!.rl_cancel.setOnClickListener {
            openDialog!!.dismiss()
        }


        //tv_ass_courier_name.text = data.requestData!!.drop_off_person

        openDialog!!.show()
        Constant.hideSoftKeyboard(this)
/*
              tv_give_review_heading.text = getString(R.string.view_review)
              tv_write_review.text = getString(R.string.view_review)
*/


        openDialog!!.submitBtn.visibility = View.GONE
        openDialog!!.tv_give_review.text = getString(R.string.view_review)
        openDialog!!.ly_horrible.isEnabled = false
        openDialog!!.ly_bad.isEnabled = false
        openDialog!!.ly_average.isEnabled = false
        openDialog!!.ly_good.isEnabled = false
        openDialog!!.ly_exellent.isEnabled = false

        //review_ratingBar.rating = averageRating
        openDialog!!.ly_review_stars.isEnabled = false

        openDialog!!.ed_write_review.setText(rattingResponce.review)

        openDialog!!.ed_write_review.isEnabled = false

        when (rattingResponce.rating) {
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

    fun getReview(view: View) {
        if (Constant.isNetworkAvailable(this, rlParentPending)) {
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
                            if (status == "success") {


                                // adapter!!.notifyDataSetChanged()
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
                    param.put("authToken", PreferenceConnector.readString(this@PendingCostumerDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
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


    fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(this, rlParentPending)) {
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
                                    profileImage = myPostResponce.profileImage!!


                                    // myPostArrayList!!.add(myPostResponce)
                                    val item = datajson.getJSONArray("item")
                                    for (j in 0..item!!.length() - 1) {
                                        val itemjson = item.getJSONObject(j)
                                        myPostResponceN = gson.fromJson(itemjson.toString(), GetMyPost.DataBean.ItemBean::class.java)
                                        myPostArrayList!!.add(myPostResponceN)

                                    }


                                }
                                setData(myPostResponce, myPostResponceN.tipPrice)
                                // adapter!!.notifyDataSetChanged()
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
                                        txtDeliveryItem.visibility = View.GONE
                                        txtStatusCount.visibility = View.GONE

                                        txt_no_data!!.visibility = View.VISIBLE
                                    }
                                } else {
                                    txtStatusCount.visibility = View.GONE

                                    txtDeliveryItem.visibility = View.VISIBLE

                                    txt_no_data!!.visibility = View.VISIBLE
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
                    param.put("authToken", PreferenceConnector.readString(this@PendingCostumerDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
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

    @SuppressLint("SetTextI18n")
    private fun setData(myPostResponce: GetMyPost.DataBean?, tipPrice: String?) {
        if (myPostResponce!!.ratingVal!! > 0.toString() && FROM.equals("Courier")) {
            llReview.visibility = View.VISIBLE

        }
        txtDeliveryItem.visibility = View.VISIBLE
        txtStatusCount.visibility = View.VISIBLE

        txtTitleOrderN.text = myPostResponce.postTitle
        txtTotalPriceN.text = "$%.2f".format(myPostResponce.totalPrice!!.toDouble())

        //txtTotalPriceN.text="$ "+myPostResponce.totalPrice
        when {
            myPostResponce.postStatus.equals("pending") -> cardChatn.visibility = View.VISIBLE
            myPostResponce.postStatus.equals("delivered") -> txtReview.visibility = View.VISIBLE
            myPostResponce.postStatus.equals("complete") -> txtReview.visibility = View.VISIBLE
        }

        when {
            myPostResponce.postStatus.equals("complete") -> {
                txtPrice.setText("Price that the customer accepted the courier for")
                txtStatusCount.setTextColor(ContextCompat.getColor(this, R.color.colorGreen))
                txtStatusCount.text = "(Delivered)"

            }
            else -> {
                txtStatusCount.setTextColor(ContextCompat.getColor(this, R.color.colorBtn))
                txtPrice.setText("Total delivery price")

                txtStatusCount.text = "(" + myPostResponce.deliveredItem + " " + "item delivered)"

            }
        }

        review_ratingBar_new.rating = myPostResponce.rating!!.toFloat()


        when {
            myPostResponce.ratingVal.equals("0") -> txtReview.setText("Write Review >>")
            else -> {
                txtReview.setText("View Review >>")
                txtReview.setOnClickListener {
                    getReviewRatting()
                }

            }
        }
        val chars = capitalize(myPostResponce.fullName as String)
        userNamereview.text = myPostResponce.senderName.toString()
        //val sendername = capitalize(myPostResponce.senderName as String)
        //val sendername = capitalize(myPostResponce.senderName as String)

        userNameN.text = chars
        txtBidPricen.text = "$%.2f".format(myPostResponce.totalPrice!!.toDouble())

        //txtBidPricen.text="$ "+myPostResponce.totalPrice


        review_ratingBar_show.rating = myPostResponce.rating!!.toFloat()
        when {
            myPostResponce.ratingVal.equals("1") -> txtReviewRate.text = "(Horrible)"
            myPostResponce.ratingVal.equals("2") -> txtReviewRate.text = "(Bad)"
            myPostResponce.ratingVal.equals("3") -> txtReviewRate.text = "(Average)"
            myPostResponce.ratingVal.equals("4") -> txtReviewRate.text = "(Good)"
            myPostResponce.ratingVal.equals("5") -> txtReviewRate.text = "(Excellent)"
        }

        Glide.with(this).load(myPostResponce.senderImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(userImagecourierReview)

        txtDesc.text = myPostResponce.reviewVal

        Glide.with(this).load(myPostResponce.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(userImagecourier)
        if (tipPrice.equals("0")&&!FROM.equals("Courier")) {
            tip_courier_btn_new.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        getMyNewPost(view)
    }


    private fun getReviewRatting() {
        if (Constant.isNetworkAvailable(this, rlParentPending)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getReviewByPostId + "?postId=" + myPostResponce.postId,
                    Response.Listener { response ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            if (status == "success") {
                                val jsonObject = result.getJSONObject("result")
                                val gson = Gson()
                                val rattingResponce = gson.fromJson(jsonObject.toString(), RattingResponceBean.ResultBean::class.java)
                                setReviewRatingData(rattingResponce)
                                /* ly_reviews.visibility = View.VISIBLE
                                 tv_wait_for_review.visibility = View.GONE
                                 ly_set_review.visibility = View.VISIBLE

                                 //review_ratingBar.rating = rattingResponce.ratingreview!!.rating.toString().toFloat()

                                 when {
                                     rattingResponce.rating!! == "1" -> tv_rating.text = "(Horrible)"
                                     rattingResponce.rating == "2" -> tv_rating.text = "(Bad)"
                                     rattingResponce.rating == "3" -> tv_rating.text = "(Average)"
                                     rattingResponce.rating == "4" -> tv_rating.text = "(Good)"
                                     rattingResponce.rating == "5" -> tv_rating.text = "(Excellent)"
                                 }*/

                                //  tv_review.text = rattingResponce.ratingreview!!.review

                                /*  Glide.with(this).load(data.requestData!!.customerProfileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(iv_customer)

                                  tv_customer_name.text = data.requestData!!.customerName
                                  rl_customer_info.visibility = View.VISIBLE
  */
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
                                val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()
                            } else Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@PendingCostumerDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
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


    private fun cardPay() {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.zeroTip,
                    Response.Listener { response ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status == "return") {
                                Constant.returnAlertDialogToMainActivity(this@PendingCostumerDetailActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                tip_courier_btn.visibility = View.GONE
                                //getpostDetails(requestId)
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
                    header.put("authToken", PreferenceConnector.readString(this@PendingCostumerDetailActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("itemId", myPostResponceN.postItemId!!)
                    params.put("courierId", myPostResponce.applyUserId!!)
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

}
