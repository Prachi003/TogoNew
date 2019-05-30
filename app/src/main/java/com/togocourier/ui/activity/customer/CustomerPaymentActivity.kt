package com.togocourier.ui.activity.customer

import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.Window
import android.view.WindowManager
import android.widget.NumberPicker
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.togocourier.R
import com.togocourier.responceBean.CardPaymentListBean
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.phase3.activity.PendingCostumerDetailActivity
import com.togocourier.util.*
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_payment.*
import kotlinx.android.synthetic.main.new_dialog_year_month.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class CustomerPaymentActivity : AppCompatActivity() {

    private var strcardNum1 = ""
    private var strcardNum2 = ""
    private var strcardNum3 = ""
    private var strcardNum4 = ""
    private var TotalcardNum = ""

    private var postId = ""
    private var postItemId = ""
    private var courierId = ""
    private var requestId = ""
    private var bitPrice = ""
    private var tipPrice = ""
    private var paymentType = ""
    private var expireMnth = ""
    private var expireYear = ""
    private var stateName = ""
    private var cityName = ""
    private var cardDetails: CardPaymentListBean.DataBean? = null
    private var progress: ProgressDialog? = null

    private var c_no = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_payment)

        progress = ProgressDialog(this)

        if (intent != null) {
            val bundle: Bundle = intent.extras
            postId = bundle.getString("postId")
            postItemId = bundle.getString("postItemId")
            requestId = bundle.getString("requestId")
            courierId = bundle.getString("courierId")
            bitPrice = bundle.getString("bitPrice")
            tipPrice = bundle.getString("tipPrice")
            paymentType = bundle.getString("paymentType")

            if (intent.hasExtra("cardDetails")) cardDetails = intent.getParcelableExtra("cardDetails")
        }

        if (cardDetails != null) {
            cardHolderName.setText(cardDetails!!.cardHolderName)

            cardNum1.setText(cardDetails!!.cardNumber.toString().substring(0, 4))
            cardNum2.setText(cardDetails!!.cardNumber.toString().substring(4, 8))
            cardNum3.setText(cardDetails!!.cardNumber.toString().substring(8, 12))
            cardNum4.setText(cardDetails!!.cardNumber.toString().substring(12, 16))

            strcardNum1 = cardNum1.text.toString()
            strcardNum2 = cardNum2.text.toString()
            strcardNum3 = cardNum3.text.toString()
            strcardNum4 = cardNum4.text.toString()

          /*  tv_date.text = cardDetails!!.expMonth + "/" + cardDetails!!.expYear
            expireMnth = cardDetails!!.expMonth.toString()
            expireYear = cardDetails!!.expYear.toString()*/

            ed_cvv.setText(cardDetails!!.cardCvv)
        }

        iv_back_press.setOnClickListener {
            onBackPressed()
        }

        if (paymentType == "normal") {
            tv_pay_btn.text = "Pay $%.2f".format(bitPrice.toDouble())
        } else if (paymentType == "tip") {
            tv_pay_btn.text = "Pay $%.2f".format(tipPrice.toDouble())
        }

        pay_from_card.setOnClickListener {
            val cvv = ed_cvv.text.toString()
            TotalcardNum = strcardNum1 + strcardNum2 + strcardNum3 + strcardNum4
            val cardHolderName = cardHolderName.text.toString()

            if (isCardVaild()) {
                if (paymentType.equals("tip")){
                    tipPay(expireMnth, expireYear, cvv, TotalcardNum, cardHolderName,postId,requestId)
                }else{
                    cardPay(expireMnth, expireYear, cvv, TotalcardNum, cardHolderName)
                }


            }
        }

        getCardComponents()

        tv_date.setOnClickListener {
            openExpiryDateDialog()
        }
    }

    private fun openExpiryDateDialog() {
        val openDialog = Dialog(this)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setCancelable(false)
        openDialog.setContentView(R.layout.new_dialog_year_month)
        openDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog.window!!.attributes = lWindowParams

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val month = Calendar.getInstance().get(Calendar.MONTH)
        openDialog.monthPicker.minValue = 1
        openDialog.monthPicker.maxValue = 12
        openDialog.monthPicker.value = month + 1
        openDialog.yearPicker.maxValue = year + 20
        openDialog.yearPicker.minValue = year
        openDialog.yearPicker.wrapSelectorWheel = false
        openDialog.yearPicker.value = year
        openDialog.yearPicker.descendantFocusability = NumberPicker.FOCUS_BLOCK_DESCENDANTS

        openDialog.setDateBtn.setOnClickListener({
            var date = (openDialog.monthPicker.value.toString() + "/" + openDialog.yearPicker.value.toString())

            expireMnth = openDialog.monthPicker.value.toString()
            expireYear = openDialog.yearPicker.value.toString().substring(2, 4)

            val expireMnth = if (expireMnth.toInt() < 10) {
                "0" + expireMnth.toInt()
            } else {
                expireMnth
            }

            tv_date.text = expireMnth + "/" + expireYear
            openDialog.dismiss()

        })
        openDialog.rl_cancel.setOnClickListener({
            Constant.hideSoftKeyboard(this)
            openDialog.dismiss()

        })
        openDialog.show()
    }

    private fun getCardComponents() {
        var cardHolderName = cardHolderName.text.toString().trim()

        cardNum1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (cardNum1.text.length == 4) {
                    cardNum2.requestFocus()

                }
                strcardNum1 = cardNum1.text.toString()

            }
        })

        cardNum2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (cardNum2.text.length == 4) {
                    cardNum3.requestFocus()
                }
                strcardNum2 = cardNum2.text.toString()
            }
        })

        cardNum3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (cardNum3.text.length == 4) {
                    cardNum4.requestFocus()
                }
                strcardNum3 = cardNum3.text.toString()
            }
        })

        cardNum4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                strcardNum4 = cardNum4.text.toString()
            }

        })
    }

    private fun cardPay(exp_month: String, exp_year: String, cvv: String, totalcardNum: String, cardHolderName: String) {

        if (Constant.isNetworkAvailable(this, mainLayout)) {
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
                                Constant.returnAlertDialogToMainActivity(this@CustomerPaymentActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                AddCardSuccess("Payment has done successfully")

                                /* if (paymentType == "normal") {
                                     acceptRejectRequest(requestId,postId)
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
                                        Constant.snackbar(mainLayout, "You have entered wrong parameter")
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

                        // Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {


                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@CustomerPaymentActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                  //  params.put("email", PreferenceConnector.readString(this@CustomerPaymentActivity, PreferenceConnector.USEREMAIL, ""))

                    if (paymentType == "normal") {
                        params.put("price", bitPrice)
                    } else if (paymentType == "tip") {
                        params.put("price", tipPrice)
                    }

                    params.put("requestId", requestId)
                   // params.put("paymentType", paymentType)
                    //params.put("payType", "1")
                   // params.put("saveDetail", "2")
                    //params.put("holderName", cardHolderName)
                   // params.put("dob", "")
                    //params.put("country", "US")
                   // params.put("currency", "USD")
                    //params.put("routingNumber", "")
                    params.put("courierId", courierId)
                   // params.put("accountNo", "")
                    //params.put("address", "")
                   // params.put("postalCode", "")
                  //  params.put("city", "")
                  //  params.put("state", "")
                   // params.put("ssnLast", "")
                    params.put("cardNumber", totalcardNum)
                    params.put("cardExpMonth", exp_month)
                    params.put("cardExpYear", exp_year)
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


    private fun tipPay(exp_month: String, exp_year: String, cvv: String, totalcardNum: String, cardHolderName: String, postId: String, requestId: String) {

        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.paymentTipByCard,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status == "return") {
                                Constant.returnAlertDialogToMainActivity(this@CustomerPaymentActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                val  intent=Intent(this,PendingCostumerDetailActivity::class.java)
                                intent.putExtra("POSTID",postId)
                                intent.putExtra("FROM","")
                                intent.putExtra("userId","")
                                startActivity(intent)

                                /* if (paymentType == "normal") {
                                     acceptRejectRequest(requestId,postId)
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
                                        Constant.snackbar(mainLayout, "You have entered wrong parameter")
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

                        // Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {


                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@CustomerPaymentActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    //  params.put("email", PreferenceConnector.readString(this@CustomerPaymentActivity, PreferenceConnector.USEREMAIL, ""))

                    if (paymentType == "normal") {
                        params.put("price", bitPrice)
                    } else if (paymentType == "tip") {
                        params.put("price", tipPrice)
                    }

                    params.put("itemId", postItemId)
                    // params.put("paymentType", paymentType)
                    //params.put("payType", "1")
                    // params.put("saveDetail", "2")
                    //params.put("holderName", cardHolderName)
                    // params.put("dob", "")
                    //params.put("country", "US")
                    // params.put("currency", "USD")
                    //params.put("routingNumber", "")
                    params.put("courierId", courierId)
                    // params.put("accountNo", "")
                    //params.put("address", "")
                    // params.put("postalCode", "")
                    //  params.put("city", "")
                    //  params.put("state", "")
                    // params.put("ssnLast", "")
                    params.put("cardNumber", totalcardNum)
                    params.put("cardExpMonth", exp_month)
                    params.put("cardExpYear", exp_year)
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


    private fun isCardVaild(): Boolean {
        val v = Validation()

        if (v.isEmpty(cardHolderName)) {
            Constant.snackbar(mainLayout, "Cardholder Name can't be empty")
            cardHolderName.requestFocus()
            return false
        } else if (v.isEmpty(cardNum1)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum1.requestFocus()
            return false
        } else if (v.isEmpty(cardNum2)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum2.requestFocus()
            return false
        } else if (v.isEmpty(cardNum3)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum3.requestFocus()
            return false
        } else if (c_no.length < 10) {
            if (v.isEmpty(cardNum4)) {
                Constant.snackbar(mainLayout, "Card number can't be empty")
                cardNum4.requestFocus()
                return false
            } else if (v.isEmpty(tv_date)) {
                Constant.snackbar(mainLayout, "Expiry date can't be empty")
                tv_date.requestFocus()
                return false
            } else if (v.isEmpty(ed_cvv)) {
                Constant.snackbar(mainLayout, "CVV number can't be empty")
                ed_cvv.requestFocus()
                return false
            }
        }/* else if (v.isEmpty(cardNum4)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            cardNum4.requestFocus()
            return false
        }*/ else if (v.isEmpty(tv_date)) {
            Constant.snackbar(mainLayout, "Expiry date can't be empty")
            tv_date.requestFocus()
            return false
        } else if (v.isEmpty(ed_cvv)) {
            Constant.snackbar(mainLayout, "CVV number can't be empty")
            ed_cvv.requestFocus()
            return false
        }
        return true
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
                intent.putExtra("REQUESTID", requestId)
                intent.putExtra("PaymentTypeBack", paymentType)
                startActivity(intent)
                finish()
            }

            this.finish()
        })
        alertDialog.show()
    }

    private fun acceptRejectRequest(id: String, postId: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
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
                                Constant.returnAlertDialog(this@CustomerPaymentActivity, message)
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
                    header.put("authToken", PreferenceConnector.readString(this@CustomerPaymentActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("bidId", id)
                    params.put("postId", postId)
                    params.put("acceptOrReject", "accept")
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
        finish()
    }
}
