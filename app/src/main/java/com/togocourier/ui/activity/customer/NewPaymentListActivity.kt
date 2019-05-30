package com.togocourier.ui.activity.customer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.GetAdpaterPosition
import com.togocourier.R
import com.togocourier.adapter.NewPaymentListAdapter
import com.togocourier.responceBean.CardPaymentListBean
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.phase3.activity.PendingCostumerDetailActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_activity_payment_list.*
import kotlinx.android.synthetic.main.new_dialog_add_cvv.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class NewPaymentListActivity : AppCompatActivity(), View.OnClickListener {
    // variable to track event time
    private var mLastClickTime: Long = 0

    private var postId: String = ""
    private var postItemId: String = ""
    private var FROM: String = ""
    private var reqId: String = ""
    private var bidPrice: String = ""
    private var tipPrice: String = ""
    private var courierId: String = ""
    private var paymentType: String = ""
    private var adapter: NewPaymentListAdapter? = null
    private var cardList = ArrayList<CardPaymentListBean.DataBean>()
    private var progress: ProgressDialog? = null

    private var gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_payment_list)
        val bundle = intent.extras
        postId = bundle!!.getString("postId")
        postItemId = bundle.getString("postItemId")
        FROM = bundle.getString("FROM")
        courierId = bundle.getString("courierId")
        reqId = bundle.getString("requestId")
        bidPrice = bundle.getString("bitPrice")
        tipPrice = bundle.getString("tipPrice")
        paymentType = bundle.getString("paymentType")

        progress = ProgressDialog(this)
        cardList = ArrayList()

        getCardPaymentList()

        adapter = NewPaymentListAdapter(this, cardList, object : GetAdpaterPosition {
            override fun GetPosition(position: Int) {
                val data = cardList[position]
                /* val intent = Intent(this@NewPaymentListActivity, CustomerPaymentActivity::class.java)
                 intent.putExtra("requestId", reqId)
                 intent.putExtra("postId", postId)
                 intent.putExtra("bitPrice", bidPrice)
                 intent.putExtra("cardDetails", data)
                 intent.putExtra("tipPrice", tipPrice)
                 intent.putExtra("paymentType", paymentType)
                 startActivity(intent)*/

                // cardPay(data.expMonth!!, data.expYear!!, data.cardCvv!!, data.cardNumber!!, data.cardHolderName!!)


                addCvv(data.cardCvv, data.userId, data.costomerToken)
            }
        })

        recycler_view.adapter = adapter
        recycler_view.visibility = View.GONE

        iv_back.setOnClickListener(this)
        add_card_btn.setOnClickListener(this)
    }

    private fun addCvv(cardCvv: String?, userId: String?, costomerToken: String?) {
        val openDialog = Dialog(this)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setCancelable(false)
        openDialog.setContentView(R.layout.new_dialog_add_cvv)
        openDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))


        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog.window!!.attributes = lWindowParams



        openDialog.btnDone.setOnClickListener {
            if (TextUtils.isEmpty(openDialog.edt_cvv.text.toString().trim())) {
                Toast.makeText(this, "Please Enter Cvv", Toast.LENGTH_SHORT).show()
            } else if (!cardCvv!!.equals(openDialog.edt_cvv.text.toString().trim())) {
                Toast.makeText(this, "Please enter valid cvv", Toast.LENGTH_SHORT).show()

            } else if (FROM.equals("bid")) {
                openDialog.dismiss()
                paymentTip(costomerToken!!,openDialog.edt_cvv.text.toString())
            }else{
                pay(openDialog.edt_cvv.text.toString(), openDialog, userId, costomerToken)

            }

        }
        openDialog.rl_cancel_new.setOnClickListener({
            Constant.hideSoftKeyboard(this)
            openDialog.dismiss()

        })
        openDialog.show()
    }


    private fun deleteCardApi(postId: String, position: Int) {
        if (Constant.isNetworkAvailable(this, list_layout)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deleteCardById,
                    Response.Listener { response ->
                        //  view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val message = result.getString("message")
                            if(message.equals("Invalid token")){
                                val helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }else{
                                val status = result.getString("status")
                                if (status == "success") {
                                    cardList.removeAt(position)
                                    adapter?.notifyDataSetChanged()
                                    getCardPaymentList()

                                } else {
                                    val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                    if (userType == Constant.COURIOR) {
                                        if (message == "Currently you are inactivate user") {
                                            val helper = HelperClass(this, this)
                                            helper.inActiveByAdmin("Admin inactive your account", true)
                                        }
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
                                val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()
                            }
                        }

                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewPaymentListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("cardId", postId)
                    return params
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

    override fun onClick(view: View?) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        if (view != null) {
            when (view.id) {
                R.id.iv_back -> {
                    onBackPressed()
                }

                R.id.add_card_btn -> {
                    val intent = Intent(this@NewPaymentListActivity, CustomerPaymentActivity::class.java)
                    intent.putExtra("requestId", reqId)
                    intent.putExtra("postId", postId)
                    intent.putExtra("postItemId",postItemId)
                    intent.putExtra("courierId", courierId)
                    intent.putExtra("bitPrice", bidPrice)
                    intent.putExtra("cardDetails", "")
                    intent.putExtra("tipPrice", tipPrice)
                    intent.putExtra("paymentType", paymentType)
                    startActivity(intent)
                }
            }
        }
    }

    var cvv = ""

    private fun pay(cvv: String, openDialog: Dialog, userId: String?, costomerToken: String?) {

        if (Constant.isNetworkAvailable(this, list_layout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.paymentByCustomerToken,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status == "return") {
                                openDialog.dismiss()
                                Constant.returnAlertDialogToMainActivity(this, message)
                                return@Listener
                            }

                            if (status == "success") {
                                openDialog.dismiss()
                                AddCardSuccess("Payment has done successfully")

                                /* if (paymentType == "normal") {
                                     acceptRejectRequest(requestId,postId)
                                 } else if (paymentType == "tip") {
                                     AddCardSuccess("Payment has done successfully")
                                 }*/
                            } else {
                                openDialog.dismiss()
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(list_layout, "You have entered wrong parameter")
                                    }
                                } else {
                                    Constant.snackbar(list_layout, message)
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
                    header.put("authToken", PreferenceConnector.readString(this@NewPaymentListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("customerId", costomerToken!!)
                    params.put("price", bidPrice)
                    params.put("requestId", postId)
                    params.put("courierId", courierId)
                    params.put("cardCvv", cvv)
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

    private fun getCardPaymentList() {
        if (Constant.isNetworkAvailable(this, list_layout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.userCardPaymentList,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val message = result.getString("message")
                            cardList.clear()
                            if(message.equals("Invalid token")){
                                val helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }else{
                                val status = result.getString("status")

                                if (status == "success") {
                                    val JsonArray = result.getJSONArray("data")
                                    for (i in 0..JsonArray!!.length() - 1) {
                                        val jsonObject = JsonArray.getJSONObject(i)
                                        val cardListResponse = gson.fromJson(jsonObject.toString(), CardPaymentListBean.DataBean::class.java)
                                        cardList.add(cardListResponse)
                                        adapter?.notifyDataSetChanged()

                                    }

                                    no_data_found.visibility = View.GONE
                                    recycler_view.visibility = View.VISIBLE

                                } else {
                                    cardList.clear()
                                    adapter?.notifyDataSetChanged()

                                    val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                    if (userType == Constant.COURIOR) {
                                        if (message == "Currently you are inactivate user") {
                                            val helper = HelperClass(this, this)
                                            helper.inActiveByAdmin("Admin inactive your account", true)
                                        } else {
                                            no_data_found.visibility = View.VISIBLE
                                            recycler_view.visibility = View.GONE
                                        }
                                    } else {
                                        no_data_found.visibility = View.VISIBLE
                                        recycler_view.visibility = View.GONE
                                    }

                                }
                            }

                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

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
                    param.put("authToken", PreferenceConnector.readString(this@NewPaymentListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("price", bidPrice)
                    return params
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

    override fun onBackPressed() {
        finish()
    }

    private fun cardPay(exp_month: String, exp_year: String, cvv: String,
                        totalcardNum: String, cardHolderName: String) {

        if (Constant.isNetworkAvailable(this, list_layout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.StripPay,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status == "return") {
                                Constant.returnAlertDialogToMainActivity(this@NewPaymentListActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                AddCardSuccess("Payment has done successfully")

                                /*if (paymentType == "normal") {
                                    acceptRejectRequest(reqId)
                                } else if (paymentType == "tip") {
                                    AddCardSuccess("Payment has done successfully")
                                }*/
                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(list_layout, "You have entered wrong parameter")
                                    }
                                } else {
                                    Constant.snackbar(list_layout, message)
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
                    header.put("authToken", PreferenceConnector.readString(this@NewPaymentListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("dob", "")
                    params.put("country", "US")
                    params.put("routingNumber", "")
                    params.put("email", PreferenceConnector.readString(this@NewPaymentListActivity, PreferenceConnector.USEREMAIL, ""))
                    params.put("city", "")
                    params.put("state", "")
                    params.put("address", "")
                    params.put("cvv", cvv)
                    params.put("exp_month", exp_month)

                    if (paymentType == "normal") {
                        params.put("amount", bidPrice)
                    } else if (paymentType == "tip") {
                        params.put("amount", tipPrice)
                    }

                    params.put("ssnLast", "")
                    params.put("payType", "1")
                    params.put("saveDetail", "2")
                    params.put("postalCode", "")
                    params.put("exp_year", exp_year)
                    params.put("currency", "USD")
                    params.put("accountNo", "")
                    params.put("card_number", totalcardNum)
                    params.put("requestId", reqId)
                    params.put("holderName", cardHolderName)
                    params.put("paymentType", paymentType)

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

    private fun AddCardSuccess(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)

        alertDialog.setPositiveButton("Ok", { dialog, which ->
            if (paymentType == "normal") {
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else if (paymentType == "tip") {
                val intent = Intent(this, NewCustomerPostDetailsActivity::class.java)
                intent.putExtra("POSTID", postId)
                intent.putExtra("FROM", Constant.completeTask)
                intent.putExtra("REQUESTID", reqId)
                intent.putExtra("PaymentTypeBack", paymentType)
                startActivity(intent)
                finish()
            }

            this.finish()
        })
        alertDialog.show()
    }

    private fun acceptRejectRequest(id: String) {
        if (Constant.isNetworkAvailable(this, list_layout)) {
            //  progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Accept_reject_Request_Url,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status.equals("return")) {
                                Constant.returnAlertDialog(this@NewPaymentListActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                AddCardSuccess("Payment has done successfully")

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(list_layout, message)
                                    }
                                } else {
                                    Constant.snackbar(list_layout, message)
                                }
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

                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@NewPaymentListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("requestId", id)
                    params.put("requestStatus", "accept")
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


    private fun paymentTip(customerId: String, cardCvv: String?) {
        if (Constant.isNetworkAvailable(this, list_layout)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.paymentTip,
                    Response.Listener { response ->
                        //  view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val message = result.getString("message")
                            if(message.equals("Invalid token")){
                                val helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }else{
                                val status = result.getString("status")
                                if (status == "success") {
                                    val  intent=Intent(this,PendingCostumerDetailActivity::class.java)
                                    intent.putExtra("POSTID",postId)
                                    intent.putExtra("FROM","")
                                    intent.putExtra("userId","")
                                    startActivity(intent)

                                } else {
                                    val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                    if (userType == Constant.COURIOR) {
                                        if (message == "Currently you are inactivate user") {
                                            val helper = HelperClass(this, this)
                                            helper.inActiveByAdmin("Admin inactive your account", true)
                                        }
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
                                val helper = HelperClass(this, this)
                                helper.sessionExpairDialog()
                            }
                        }

                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewPaymentListActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("customerId", customerId)
                    params.put("price", tipPrice)
                    params.put("itemId", postItemId)
                    params.put("cardCvv", cardCvv!!)
                    params.put("courierId", reqId)
                    return params
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

}