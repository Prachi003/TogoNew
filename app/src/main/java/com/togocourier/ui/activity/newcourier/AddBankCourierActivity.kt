package com.togocourier.ui.activity.newcourier

import android.app.AlertDialog
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.responceBean.BankInfoBean
import com.togocourier.responceBean.PaymentResponce
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.Validation
import com.togocourier.vollyemultipart.VolleySingleton
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import kotlinx.android.synthetic.main.activity_add_bank_courier.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class AddBankCourierActivity : AppCompatActivity() {
    private var now: Calendar? = null
    private var fromDate: DatePickerDialog? = null
    private var addBank=""
    private var gson = Gson()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_bank_courier)
        initView()
        addBank = PreferenceConnector.readString(this, PreferenceConnector.ADDBANK, "")
        if (addBank.equals("NO")){
            //view.ly_add_card.visibility = View.VISIBLE
        }else{
           updateBank()
        }

    }


    fun initView(){
        tvDob.setOnClickListener {
            setDateField()
        }

        txtDate.setOnClickListener {
            setDateField()
        }

        iv_back_press.setOnClickListener {
            onBackPressed()
        }

        rl_add_card_account.setOnClickListener {
            edtFirstNamenew.text.toString()
            edtLastName.text.toString()
            val tv_dob =   txtDate.text.toString()
            val postal_code =   edtPostCode.text.toString()
            val ssn_last =   edtssnNo.text.toString()
            val routing_num =   edtRouting.text.toString()
            val accNum = edtAccontnumber.text.toString()

            if(isAddBankVaild()){
                cardPay(postal_code, accNum, routing_num, tv_dob, ssn_last)
            }
        }
    }


    fun isAddBankVaild(): Boolean {
        val v = Validation()

        if (v.isEmpty(edtFirstNamenew)) {
            Constant.snackbar(mainLayout, "First name can't be empty")
            edtFirstNamenew.requestFocus()
            return false
        }
        else if (v.isEmpty(edtLastName)) {
            Constant.snackbar(mainLayout, "Last name can't be empty")
            edtLastName.requestFocus()
            return false
        }

        else if (v.isEmpty(edtAccontnumber)) {
            Constant.snackbar(mainLayout, "Account number can't be empty")
            edtAccontnumber.requestFocus()
            return false
        }else if (v.isEmpty(edtRouting)) {
            Constant.snackbar(mainLayout, "Routing number can't be empty")
            edtRouting.requestFocus()
            return false
        }

        else if (v.isEmpty(edtPostCode)) {
            Constant.snackbar(mainLayout, "Postal code can't be empty")
            edtPostCode.requestFocus()
            return false
        }
        else if (v.isEmpty(edtssnNo)) {
            Constant.snackbar(mainLayout, "SSN_Last can't be empty")
            edtssnNo.requestFocus()
            return false
        } else if (v.isEmpty(txtDate)) {
            Constant.snackbar(mainLayout, "Date of birth can't be empty")
            txtDate.requestFocus()
            return false
        }
        else if (!edtRouting.text.toString().trim().equals(edtConfirmRouting.text.toString().trim())){
            Constant.snackbar(mainLayout, "Enter Valid routing number" + "")
            return false

        }
        return true
    }

    private fun setDateField() {
        now = Calendar.getInstance()
        fromDate = DatePickerDialog.newInstance({ datePickerDialog, year, monthOfYear, dayOfMonth ->
            val date = year.toString() + "-" + (monthOfYear + 1).toString() + "-" + dayOfMonth
            txtDate.text = date
            //  dob = date
        }, now!!.get(Calendar.YEAR), now!!.get(Calendar.MONTH), now!!.get(Calendar.DAY_OF_MONTH))
        fromDate?.setMaxDate(Calendar.getInstance())
        fromDate?.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fromDate?.show(this.fragmentManager, "")
        fromDate?.setOnCancelListener({
            Log.d("TimePicker", "Dialog was cancelled")
            fromDate?.dismiss()
        })

    }



    private fun cardPay(postalCode: String, accountNo: String, routingNumber: String, dob: String, ssnLast: String) {

        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.CoriorBankAdd,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("Stripes", "onResponse: " + response)
                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                PreferenceConnector.writeString(this, PreferenceConnector.ADDBANK, "YES")

                                gson.fromJson(response, PaymentResponce::class.java)
                                AddCardSuccess("Bank account add sucessfully")
                            } else {

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE,"")
                                if(userType == Constant.COURIOR){
                                    if(message.equals("Currently you are inactivate user")){
                                        val helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(mainLayout, message)
                                    }
                                }else{
                                    Constant.snackbar(mainLayout, message)
                                }

                            }
                        } catch (e: JSONException) {
                            Constant.snackbar(mainLayout, "Something went wrong...")
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 400){
                                val helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }
                        }
                    })
            {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@AddBankCourierActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("firstName", edtFirstNamenew.text.toString())
                    params.put("lastName", edtLastName.text.toString())
                    params.put("dob", dob)
                    params.put("country", "US")
                    params.put("currency", "USD")
                    params.put("routingNumber",routingNumber)
                    params.put("accountNumber",accountNo)
                    params.put("postalCode",postalCode)
                    params.put("ssnLast", ssnLast)
                    return params
                }
            }
            stringRequest.setRetryPolicy(DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        }
    }



    fun deleteBank() {

        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deleteBankAccount,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("Stripes", "onResponse: " + response)
                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {

                                edtFirstNamenew.isEnabled=true
                                edtLastName.isEnabled=true
                                edtAccontnumber.isEnabled=true
                                edtssnNo.isEnabled=true
                                edtConfirm.isEnabled=true
                                edtRouting.isEnabled=true
                                edtConfirmRouting.isEnabled=true
                                edtPostCode.isEnabled=true
                                edtFirstNamenew.setText("")
                                edtLastName.setText("")
                                edtAccontnumber.setText("")
                                edtssnNo.setText("")
                                edtConfirm.setText("")
                                edtRouting.setText("")
                                edtConfirmRouting.setText("")
                                edtPostCode.setText("")
                                txtDate.setText("")
                                txtButtonAdd.setText(getString(R.string.add_account))

                                PreferenceConnector.writeString(this, PreferenceConnector.ADDBANK, "NO")



                            } else {

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        val helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(mainLayout, message)
                                    }
                                }else{
                                    Constant.snackbar(mainLayout, message)
                                }

                            }
                        } catch (e: JSONException) {
                            Constant.snackbar(mainLayout, "Something went wrong...")
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 400){
                                val helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }
                        }
                    })
            {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@AddBankCourierActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("","")
                    return params
                }
            }
            stringRequest.setRetryPolicy(DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        }
    }


    fun updateBank() {

        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progressBar.visibility = View.VISIBLE
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getBankDetail,
                    Response.Listener { response ->
                        progressBar.visibility = View.GONE
                        println("#" + response)
                        Log.e("Stripes", "onResponse: " + response)
                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val data = result.getJSONObject("data")
                                val bankInfoBean=gson.fromJson(data.toString(),BankInfoBean.DataBean::class.java)
                                setData(bankInfoBean )
                            } else {

                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE,"")
                                if(userType.equals(Constant.COURIOR)){
                                    if(message.equals("Currently you are inactivate user")){
                                        val helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account",true)
                                    }else{
                                        Constant.snackbar(mainLayout, message)
                                    }
                                }else{
                                    Constant.snackbar(mainLayout, message)
                                }

                            }
                        } catch (e: JSONException) {
                            Constant.snackbar(mainLayout, "Something went wrong...")
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error->
                        progressBar.visibility = View.GONE
                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 400){
                                val helper = HelperClass(this,this)
                                helper.sessionExpairDialog()
                            }
                        }
                    })
            {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@AddBankCourierActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    return params
                }
            }
            stringRequest.setRetryPolicy(DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT))
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        }
    }

    fun setData(bankInfoBean: BankInfoBean.DataBean) {
        edtFirstNamenew.isEnabled=false
        edtLastName.isEnabled=false
        edtAccontnumber.isEnabled=false
        edtssnNo.isEnabled=false
        edtConfirm.isEnabled=false
        edtRouting.isEnabled=false
        edtConfirmRouting.isEnabled=false
        edtPostCode.isEnabled=false
        edtFirstNamenew.setText(bankInfoBean.firstName)
        edtLastName.setText(bankInfoBean.lastName)
        edtAccontnumber.setText(bankInfoBean.accountNumber)
        edtssnNo.setText("****")
        edtConfirm.setText(bankInfoBean.accountNumber)
        edtRouting.setText(bankInfoBean.routingNumber)
        edtConfirmRouting.setText(bankInfoBean.routingNumber)
        edtPostCode.setText(bankInfoBean.postalCode)
        txtDate.setText(bankInfoBean.dob)
        if (addBank.equals("NO")){
            txtButtonAdd.setText(getString(R.string.add_account))
        }else{
            txtButtonAdd.setText(getString(R.string.delete_account))
            rl_add_card_account.setOnClickListener { v: View? ->
                deleteBank()
            }
        }


    }


    fun AddCardSuccess(msg:String) {

        val alertDialog = AlertDialog.Builder(this)

        alertDialog.setTitle("Alert")

        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)

        alertDialog.setPositiveButton("Ok", { dialog, which ->
           finish()
        })

        alertDialog.show()

    }
}
