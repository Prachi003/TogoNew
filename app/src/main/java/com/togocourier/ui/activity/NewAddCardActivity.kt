package com.togocourier.ui.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
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
import com.togocourier.util.*
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_activity_add_card.*
import kotlinx.android.synthetic.main.new_dialog_year_month.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class NewAddCardActivity : AppCompatActivity(), View.OnClickListener {

    private var strcardNum1 = ""
    private var strcardNum2 = ""
    private var strcardNum3 = ""
    private var strcardNum4 = ""
    private var TotalcardNum = ""

    private var expireMnth = ""
    private var expireYear = ""
    private var cardDetails: CardPaymentListBean.DataBean? = null
    private var progress: ProgressDialog? = null

    private var c_no = ""

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_add_card)

        progress = ProgressDialog(this)

        if (intent != null && intent.hasExtra("cardDetails")) {
            cardDetails = intent.getParcelableExtra("cardDetails")
        }

       /* if (cardDetails != null) {
            tv_card_header.text = "Card Details"
            add_cardHolderName.setText(cardDetails!!.cardHolderName)

            add_cardNum1.setText(cardDetails!!.cardNumber.toString().substring(0, 4))
            add_cardNum2.setText(cardDetails!!.cardNumber.toString().substring(4, 8))
            add_cardNum3.setText(cardDetails!!.cardNumber.toString().substring(8, 12))
            add_cardNum4.setText(cardDetails!!.cardNumber.toString().substring(12, 16))

            strcardNum1 = add_cardNum1.text.toString()
            strcardNum2 = add_cardNum2.text.toString()
            strcardNum3 = add_cardNum3.text.toString()
            strcardNum4 = add_cardNum4.text.toString()

            *//*tv_add_date.text = cardDetails!!.expMonth + "/" + cardDetails!!.expYear
            expireMnth = cardDetails!!.expMonth.toString()
            expireYear = cardDetails!!.expYear.toString()*//*

            ed_add_cvv.setText(cardDetails!!.cardCvv)

            add_cardHolderName.isEnabled = false
            add_cardNum1.isEnabled = false
            add_cardNum2.isEnabled = false
            add_cardNum3.isEnabled = false
            add_cardNum4.isEnabled = false
            tv_add_date.isEnabled = false
            ed_add_cvv.isEnabled = false
            rl_add_card.visibility = View.GONE
        } else {
            tv_card_header.text = "Add Card"
        }*/

        getCardComponents()

        iv_add_back.setOnClickListener(this)
        rl_add_card.setOnClickListener(this)
        tv_add_date.setOnClickListener(this)
    }

    override fun onClick(view: View?) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view?.id) {
            R.id.iv_add_back -> {
                onBackPressed()
            }

            R.id.tv_add_date -> {
                openExpiryDateDialog()
            }

            R.id.rl_add_card -> {
                Constant.hideSoftKeyboard(this)
                val cvv = ed_add_cvv.text.toString()
                TotalcardNum = strcardNum1 + strcardNum2 + strcardNum3 + strcardNum4
                val cardHolderName = add_cardHolderName.text.toString()

                if (isCardVaild()) {
                    addCardApi(TotalcardNum, cardHolderName, cvv)
                }

            }
        }
    }

    private fun addCardApi(totalcardNum: String, cardHolderName: String, cvv: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.AddCardDetail,
                    Response.Listener { response ->
                        //  progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")

                            if (status == "return") {
                                Constant.returnAlertDialogToMainActivity(this@NewAddCardActivity, message)
                                return@Listener
                            }

                            if (status == "success") {
                                val intent = Intent(this, NewAddCardListActivity::class.java)
                                startActivity(intent)
                                finish()
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

                        //  Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {


                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@NewAddCardActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("cardNumber", totalcardNum)
                    params.put("cardHolderName", cardHolderName)
                    params.put("cardCvv", cvv)
                    params.put("cardExpMonth", expireMnth)
                    params.put("cardExpYear", expireYear)

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

        c_no = add_cardNum1.text.toString() + add_cardNum2.text.toString() + add_cardNum3.text.toString() + add_cardNum4.text.toString()

        if (v.isEmpty(add_cardHolderName)) {
            Constant.snackbar(mainLayout, "Cardholder Name can't be empty")
            add_cardHolderName.requestFocus()
            return false
        } else if (v.isEmpty(add_cardNum1)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            add_cardNum1.requestFocus()
            return false
        } else if (v.isEmpty(add_cardNum2)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            add_cardNum2.requestFocus()
            return false
        } else if (v.isEmpty(add_cardNum3)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            add_cardNum3.requestFocus()
            return false
        } /*else if (v.isEmpty(add_cardNum4)) {
            Constant.snackbar(mainLayout, "Card number can't be empty")
            add_cardNum4.requestFocus()
            return false
        } */
        else if (c_no.length < 10) {
            if (v.isEmpty(add_cardNum4)) {
                Constant.snackbar(mainLayout, "Card number can't be empty")
                add_cardNum4.requestFocus()
                return false
            }else if (v.isEmpty(tv_add_date)) {
                Constant.snackbar(mainLayout, "Expiry date can't be empty")
                tv_add_date.requestFocus()
                return false
            } else if (v.isEmpty(ed_add_cvv)) {
                Constant.snackbar(mainLayout, "CVV number can't be empty")
                ed_add_cvv.requestFocus()
                return false
            }
        } else if (v.isEmpty(tv_add_date)) {
            Constant.snackbar(mainLayout, "Expiry date can't be empty")
            tv_add_date.requestFocus()
            return false
        } else if (v.isEmpty(ed_add_cvv)) {
            Constant.snackbar(mainLayout, "CVV number can't be empty")
            ed_add_cvv.requestFocus()
            return false
        }
        return true
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

            tv_add_date.text = expireMnth + "/" + expireYear
            openDialog.dismiss()

        })
        openDialog.rl_cancel.setOnClickListener({
            Constant.hideSoftKeyboard(this)
            openDialog.dismiss()

        })
        openDialog.show()
    }

    private fun getCardComponents() {
        add_cardNum1.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (add_cardNum1.text.length == 4) {
                    add_cardNum2.requestFocus()

                }
                strcardNum1 = add_cardNum1.text.toString()

            }
        })

        add_cardNum2.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (add_cardNum2.text.length == 4) {
                    add_cardNum3.requestFocus()
                }
                strcardNum2 = add_cardNum2.text.toString()
            }
        })

        add_cardNum3.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (add_cardNum3.text.length == 4) {
                    add_cardNum4.requestFocus()
                }
                strcardNum3 = add_cardNum3.text.toString()
            }
        })

        add_cardNum4.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                strcardNum4 = add_cardNum4.text.toString()
            }

        })
    }

    override fun onBackPressed() {
        val intent = Intent(this, NewAddCardListActivity::class.java)
        startActivity(intent)
        finish()
    }
}