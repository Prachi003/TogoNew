package com.togocourier.ui.fragment

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.togocourier.Interface.MyClickListner
import com.togocourier.R
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.ui.activity.AboutUsActivity
import com.togocourier.ui.activity.NewAddCardListActivity
import com.togocourier.ui.activity.TermConditionActivity
import com.togocourier.ui.activity.UserSelectionActivity
import com.togocourier.ui.activity.newcourier.AddBankCourierActivity
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_term_condition.*
import kotlinx.android.synthetic.main.new_activity_home.*
import kotlinx.android.synthetic.main.new_fragment_settings.*
import kotlinx.android.synthetic.main.new_fragment_settings.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class SettingsFragment : Fragment(), View.OnClickListener {
    private var mParam1: String? = null
    private var mParam2: String? = null
    private var progress: ProgressDialog? = null
    private var usertype=""
    private var addBank=""

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            mParam1 = arguments?.getString(ARG_PARAM1)
            mParam2 = arguments?.getString(ARG_PARAM2)
        }
    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"

        fun newInstance(param1: String, param2: String): SettingsFragment {
            val fragment = SettingsFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.new_fragment_settings, container, false)
        activity?.headerLay?.visibility = View.GONE
        activity?.footer?.visibility = View.GONE
        progress = ProgressDialog(context!!)


        usertype = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
        addBank = PreferenceConnector.readString(context!!, PreferenceConnector.ADDBANK, "")
        /*if (userType==Constant.CUSTOMER){
            txtAddPaymentnew.setText("Add Payment Card")
        }else{
            txtAddPaymentnew.setText("Add Bank Account")

        }*/

        if (usertype == Constant.CUSTOMER) {
            view.ly_add_card.visibility = View.VISIBLE
        }else{

            if (addBank.equals("NO")){
                view.txtAddPaymentnew.setText("Add Bank Account")
                //view.ly_add_card.visibility = View.VISIBLE
            }else if(addBank.equals("YES")){
                view.txtAddPaymentnew.setText("Update  Bank Account")
            }

            view.ly_add_card.visibility = View.VISIBLE



        }

        view.ly_change_password.setOnClickListener {
            val socialType = PreferenceConnector.readString(context!!, PreferenceConnector.USERSOCIALTYPE, "")
            val helper = HelperClass(context!!, activity!!)
            if (socialType != "facebook" && socialType != "gmail") {
                helper.changePasswordDialog(object : MyClickListner {
                    override fun getPassword(oldPassword: String, newPassword: String, openDialog: Dialog) {
                        val oldPasswordSession = PreferenceConnector.readString(context!!, PreferenceConnector.PASSWORD, "")

                        when {
                            oldPasswordSession != oldPassword -> {
                                Toast.makeText(context!!, "Please enter correct old password", Toast.LENGTH_SHORT).show()
                                return
                            }
                            oldPasswordSession == newPassword -> {
                                Toast.makeText(context!!, "Old password and new password should be different", Toast.LENGTH_SHORT).show()
                                return
                            }
                            else -> changePassword(view, newPassword, openDialog)
                        }

                    }
                })
            } else {
                val alertDialog = AlertDialog.Builder(context!!)
                alertDialog.setTitle("Alert")
                alertDialog.setCancelable(false)
                alertDialog.setMessage("You can't change your password in social login")
                alertDialog.setPositiveButton("Ok", { dialog, which ->
                    alertDialog.setCancelable(true)

                })
                alertDialog.show()
            }
        }

        if (PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "") == "ON") {
            view.toggle_notifation.setImageResource(R.drawable.on_btn)
        } else if (PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "") == "OFF") {
            view.toggle_notifation.setImageResource(R.drawable.off_btn)
        }

        view.toggle_notifation.setOnClickListener {
            isNotificationONOFF(view)
        }

        view.ly_logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            //  context!!.stopService(Intent(context!!, MyLocationService::class.java))

            // HelperClass.stopJobDispatcher(context!!)
            /*  HelperClass.stopBackgroundService(context!!)*/

            /* val helper = HelperClass(context!!, activity!!)
             helper.stopAlarmService(context!!)*/

            logOut(view)
        }

        view.iv_back.setOnClickListener(this)

        view.rl_add_card.setOnClickListener(this)

        view.ly_ratting_app.setOnClickListener(this)

        view.about_us.setOnClickListener(this)

        view.ly_termncondition.setOnClickListener(this)
        return view
    }

    override fun onClick(view: View?) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view!!.id) {
            R.id.iv_back -> {
                activity!!.onBackPressed()
            }

            R.id.about_us -> {
                val intent = Intent(context!!, AboutUsActivity::class.java)
                startActivity(intent)
            }

            R.id.ly_termncondition -> {
/*
                val intent = Intent(context!!, TermConditionActivity::class.java)
                startActivity(intent)
*/              getTermsandCondition(view)
            }

            R.id.rl_add_card -> {
                if(usertype.equals(Constant.CUSTOMER)){
                    val intent = Intent(context!!, NewAddCardListActivity::class.java)
                    startActivity(intent)
                }else{
                    val intent = Intent(context!!, AddBankCourierActivity::class.java)
                    startActivityForResult(intent,101)

                }

            }

            R.id.ly_ratting_app -> {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.togocourier")))
            }
        }
    }

    private fun changePassword(view: View, newPassword: String, openDialog: Dialog) {
        if (Constant.isNetworkAvailable(context!!, view.main_settings)) {
            //  view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.changePassword,
                    Response.Listener { response ->
                        //  view.progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                openDialog.dismiss()
                                PreferenceConnector.writeString(context!!, PreferenceConnector.PASSWORD, newPassword)
                                clickToLogOut()

                            } else {
                                val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(context!!, activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(view.main_settings, message)
                                    }
                                } else {
                                    Constant.snackbar(view.main_settings, message)
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
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("oldPassword", PreferenceConnector.readString(context!!, PreferenceConnector.PASSWORD, ""))
                    params.put("password", newPassword)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)
        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun isNotificationONOFF(view: View) {
        if (Constant.isNetworkAvailable(context!!, view.main_settings)) {
            //  view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.updateNotiStatus,
                    Response.Listener { response ->
                        //  view.progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            val data=result.getJSONObject("data")
                            val notificationStatus = data.getString("notificationStatus")
                            if (status == "success") {
                                PreferenceConnector.writeString(context!!, PreferenceConnector.ISNOTIFICATIONON, notificationStatus)

                                if (PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "") == "ON") {
                                    view.toggle_notifation.setImageResource(R.drawable.on_btn)
                                } else if (PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "") == "OFF") {
                                    view.toggle_notifation.setImageResource(R.drawable.off_btn)
                                }
                                updateNotifationFCM()

                            } else {
                                val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(context!!, activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        Constant.snackbar(view.main_settings, message)
                                    }
                                } else {
                                    Constant.snackbar(view.main_settings, message)
                                }


                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        //  view.progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        Toast.makeText(activity, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                val helper = HelperClass(context!!, activity!!)
                                helper.sessionExpairDialog()
                            }
                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateNotifationFCM() {
        val user = UserInfoFCM()
        user.notificationStatus = PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "")
        user.uid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID, "")
        user.email = PreferenceConnector.readString(context!!, PreferenceConnector.USEREMAIL, "")
        user.name = PreferenceConnector.readString(context!!, PreferenceConnector.USERFULLNAME, "")
        user.firebaseToken = FirebaseInstanceId.getInstance().token!!
        user.profilePic = PreferenceConnector.readString(context!!, PreferenceConnector.USERPROFILEIMAGE, "")


        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(user.uid).setValue(user).addOnCompleteListener { task ->

        }

    }

    private fun logOut(view: View) {
        if (Constant.isNetworkAvailable(context!!, view.main_settings)) {
            //  view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.logOut,
                    Response.Listener { response ->
                        //  view.progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val myUid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID, "")
                                FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(myUid).child("firebaseToken").setValue("")

                                //  HelperClass.stopBackgroundService(context!!)

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    HelperClass.stopBackgroundService(context!!)
                                } else {
                                    HelperClass.stopJobDispatcher(context!!)
                                }

                                PreferenceConnector.clear(context!!)

                                val intent = Intent(context, UserSelectionActivity::class.java)
                                startActivity(intent)
                                activity?.finish()

                            } else {
                                Constant.snackbar(view.main_settings, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        //  view.progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        if (context != null && activity != null) {
                            PreferenceConnector.clear(context!!)

                            val intent = Intent(context, UserSelectionActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                        }
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(context!!).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    fun clickToLogOut() {
        val builder1 = AlertDialog.Builder(context!!)
        builder1.setMessage("Password modification will expire your current session")
        builder1.setCancelable(true)

        builder1.setPositiveButton(
                "Ok",
                { dialog, id ->
                    PreferenceConnector.clear(context!!)
                    val intent = Intent(context, UserSelectionActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                    dialog.cancel()
                })

        val alert11 = builder1.create()
        alert11.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==101){
            addBank = PreferenceConnector.readString(context!!, PreferenceConnector.ADDBANK, "")
            if (addBank.equals("NO")){
                view!!.txtAddPaymentnew.setText(R.string.add_account)
            }else if(addBank.equals("YES")){
                view!!.txtAddPaymentnew.setText(getString(R.string.update_bank_account))
            }

        }
    }

    var termcondition=""

    private fun getTermsandCondition(view: View) {
        if (Constant.isNetworkAvailable(this!!.context!!, view)) {
            //  list_progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.baseWebViewUrl,
                    Response.Listener { response ->
                        //  list_progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val JsonObject=result.optJSONObject("result")
                                termcondition=JsonObject.getString("termcondition")
                                loadUrl(termcondition)


                            } else {

                                val userType = PreferenceConnector.readString(this.context!!, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this.context!!, this!!.activity!!)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
/*
                                        no_list_data_found.visibility = View.VISIBLE
                                        list_recycler_view.visibility = View.GONE
*/
                                    }
                                } else {
/*
                                    no_list_data_found.visibility = View.VISIBLE
                                    list_recycler_view.visibility = View.GONE
*/
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        //  list_progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        Toast.makeText(context, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()

                        val networkResponse = error.networkResponse
                        if (networkResponse != null) {
                            if (networkResponse.statusCode == 300) {
                                val helper = HelperClass(this!!.context!!, this!!.activity!!)
                                helper.sessionExpairDialog()
                            }
                        }
                    }) {



            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this.context!!).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(this.context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun loadUrl(termcondition: String) {


        try {
            val i = Intent(Intent.ACTION_VIEW)
            i.data = Uri.parse(termcondition)
            startActivity(i)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this.context!!,"",Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this.context!!,"",Toast.LENGTH_SHORT).show()
        }

    }

}
