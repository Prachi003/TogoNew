package com.togocourier.ui.fragment.customer

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.SystemClock
import android.support.design.widget.CoordinatorLayout
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.text.TextUtils
import android.view.*
import android.widget.FrameLayout
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.gson.Gson
import com.togocourier.Interface.PostOnClick
import com.togocourier.R
import com.togocourier.responceBean.UpdateProfileInfo
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.activity.customer.AddCourierItemImageActivity
import com.togocourier.ui.activity.customer.model.newcustomer.GetMyPost
import com.togocourier.ui.fragment.customer.adapter.CustomerPostAdapter
import com.togocourier.ui.fragment.customer.model.GetPostId
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_add_courier_fragment.*
import kotlinx.android.synthetic.main.new_add_courier_fragment.view.*
import kotlinx.android.synthetic.main.new_dialog_add_mobile_nu.*
import kotlinx.android.synthetic.main.new_fragment_profile.view.*
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap

class NewAddCourierFragment : Fragment(), View.OnClickListener, PostOnClick {
    var total=0.0
    var myPostResponce= GetMyPost.DataBean()
    private var customerPostAdapter: CustomerPostAdapter? = null
    private var fm: FragmentManager? = null
    var totalprice = 0.0
    lateinit var coordinateLay: CoordinatorLayout
    lateinit var frameLayout: FrameLayout
    private var progress: ProgressDialog? = null
    private var mLastClickTime: Long = 0
    private var pickupAdrs=""
    private var pickupCity=""
    private var deliveryCity=""
    private var deliveryAdrs=""
    private var pickUpLat=""
    private var pickUpLng=""
    private var deliveryLat=""
    private var deliveryLng=""
/*
    deliveryLat = intent.getStringExtra("deliveryLat")
    deliveryLng = intent.getStringExtra("deliveryLng")
*/



    private var gson = Gson()
    private var myPostArrayList: ArrayList<GetMyPost.DataBean.ItemBean>? = null




    override fun delete(position: Int, itemView: View) {
        if (!myPostArrayList!!.get(position).price!!.equals("")){
           /* total= total- myPostArrayList!!.get(position).price!!.toFloat()
            view!!.et_priceItem.setText("$ "+total)*/
            deletepost(myPostArrayList!!.get(position).postItemId!!,view,position)

        }else{
            deletepost(myPostArrayList!!.get(position).postItemId!!,view,position)

        }
    }

    override fun GetPosition(position: Int) {
            if (TextUtils.isEmpty(et_title.text.toString().trim())) Toast.makeText(context,"Please Enter title",Toast.LENGTH_SHORT).show()


        else if (et_title.text.toString().trim().length<2){
            Toast.makeText(context,"Delivery title should not less than 2 characters.",Toast.LENGTH_SHORT).show()
        }else if (et_title.text.toString().trim().length>20){
            Toast.makeText(context,"Delivery title should not greator than 20 characters.",Toast.LENGTH_SHORT).show()
        }

        else if (position==0) {
            val intent = Intent(context, AddCourierItemImageActivity::class.java)
            intent.putExtra("postTitle",et_title.text.toString().trim())
            intent.putExtra("deliveryAdrs",deliveryAdrs)
            intent.putExtra("pickupCity",pickupCity)
            intent.putExtra("pickUpLat",pickUpLat)
            intent.putExtra("picUpLng",pickUpLng)
            intent.putExtra("deliveryLat",deliveryLat)
            intent.putExtra("deliveryLng",deliveryLng)

            intent.putExtra("deliveryCity",deliveryCity)
            intent.putExtra("pickupAdrs",pickupAdrs)
            startActivity(intent)

        }

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
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(context!!, PreferenceConnector.USERAUTHTOKEN, ""))
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
            VolleySingleton.getInstance(context!!).addToRequestQueue(multipartRequest)

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


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.new_add_courier_fragment, container, false)
        initializeView(view)
        return view
    }

    private fun initializeView(view: View?) {
        coordinateLay = activity?.findViewById<View>(R.id.coordinateLay) as CoordinatorLayout
        frameLayout = activity?.findViewById<View>(R.id.fragmentPlace) as FrameLayout
        val layoutManager = GridLayoutManager(context, 3)
        myPostArrayList = ArrayList()
        //val myPostResponce1 = GetMyPost.DataBean()
        //myPostArrayList!!.addAll(myPostResponce1.item!!)
        progress = ProgressDialog(context!!)
        customerPostAdapter = CustomerPostAdapter(myPostArrayList, this, context!!)
        view!!.recyclerPost.layoutManager = layoutManager as RecyclerView.LayoutManager?
        view.recyclerPost.adapter = customerPostAdapter
        view.add_courier_item_btn_new.setOnClickListener(this)
        showPopupContact()
    }

    fun showPopupContact(){
        val user_contact = PreferenceConnector.readString(this.context!!, PreferenceConnector.USERCONTACTNO, "")

        if (user_contact.isEmpty() || user_contact == "") {
            Alert("For better experience please add your contact number")


        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.isFocusableInTouchMode = true
        view.isClickable = true
        view.requestFocus()

        getMyNewPost(view)
    }

    override fun onClick(p0: View?) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()
        when (p0!!.id) {
            R.id.add_courier_item_btn_new -> {
              sharePost(et_title.text.toString(), myPostResponce.postId!!)
            }


        }
    }

    fun getMyNewPost(view: View) {
        if (Constant.isNetworkAvailable(context!!, coordinateLay)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.Get_My_Post_Url + "?status=new&isActive=0&page=1&postId=0",
                    Response.Listener { response ->
                        //view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()
                        myPostArrayList!!.clear()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val message = result.getString("message")

                            if(message.equals("Invalid token")){
                                val helper = HelperClass(context!!, activity!!)
                                helper.sessionExpairDialog()
                            }else{
                                val status = result.getString("status")

                                // myPostResponce = GetMyPost.DataBean()
                                val myPostResponcei = GetMyPost.DataBean.ItemBean()
                                myPostArrayList!!.add(myPostResponcei)
                                if (status == "success") {
                                    val data = result.getJSONArray("data")
                                    for (i in 0..data!!.length() - 1) {
                                        val datajson = data.getJSONObject(i)
                                        val postId = datajson.getString("postId")
                                        GetPostId.instance.postID=postId

                                        myPostResponce = gson.fromJson(datajson.toString(), GetMyPost.DataBean::class.java)

                                        val item = datajson.getJSONArray("item")
                                        for (j in 0..item!!.length() - 1) {
                                            val itemjson = item.getJSONObject(j)
                                            if (!itemjson.getString("price").equals("")){
                                                total +=itemjson.getString("price").toFloat()

                                            }
                                            val myPostResponceN = gson.fromJson(itemjson.toString(), GetMyPost.DataBean.ItemBean::class.java)
                                            pickupAdrs= myPostResponceN.pickupAdrs!!
                                            pickupCity=myPostResponceN.pickupCity!!
                                            pickUpLat= myPostResponceN.pickupLat!!
                                            deliveryLat= myPostResponceN.deliverLat!!
                                            deliveryLng= myPostResponceN.deliverLong!!
                                            pickUpLng= myPostResponceN.pickupLong!!
                                            deliveryCity=myPostResponceN.deliveryCity!!
                                            deliveryAdrs=myPostResponceN.deliveryAdrs!!
                                            myPostArrayList!!.add(myPostResponceN)

                                        }


                                    }

                                    // val getitem=gson.fromJson(response,GetMyPost.DataBean.ItemBean::class.java)
                                    //view.noDataTxt.visibility = View.GONE
                                } else {
                                    if (context != null) {
                                        val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
                                        if (userType == Constant.COURIOR) {
                                            if (message == "Currently you are inactivate user") {
                                                val helper = HelperClass(context!!, activity!!)
                                                helper.inActiveByAdmin("Admin inactive your account", true)
                                            } else {
                                                //  view.noDataTxt.visibility = View.VISIBLE
                                            }
                                        } else {
//                                        view.noDataTxt.visibility = View.VISIBLE
                                        }
                                    }

                                }

                                customerPostAdapter!!.notifyDataSetChanged()
                                setData(myPostArrayList, view, myPostResponce,total)

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


    @SuppressLint("SetTextI18n")
    private fun setData(myPostArrayList: ArrayList<GetMyPost.DataBean.ItemBean>?, view: View, myPostResponce: GetMyPost.DataBean, total: Double) {
        if (myPostArrayList!!.size > 1) {
            view.txtPrice.visibility = View.VISIBLE
            view.et_priceItem.visibility = View.VISIBLE
            view.llButton.visibility = View.VISIBLE
            view.viewPrice.visibility = View.VISIBLE
            view.et_title.isEnabled=false
            view.txtTitlenew.text="Name This Delivery"
            view.txtDeliveryItem.text="Items Being Delivered"

            view.et_priceItem.isEnabled=false
            view.isClickable = false
            view.et_priceItem.setText("$ "+total)
            view.et_title.setText(myPostResponce.postTitle)
            view.et_title.setTextColor(ContextCompat.getColor(context!!, R.color.colorBlack))
            view.et_priceItem.setTextColor(ContextCompat.getColor(context!!, R.color.colorBlack))
            view.txtTitlenew.setTextColor(ContextCompat.getColor(context!!, R.color.new_gray_color))

            //txtTitle.setTextColor(context!!.getResources().getColor(R.color.new_gray_color, context!!.theme))      }
        }else{
            view.llButton.visibility =View.GONE
            view.viewPrice.visibility = View.GONE
            view.et_priceItem.visibility = View.GONE
            view.txtPrice.visibility = View.GONE
            view.txtTitlenew.text= "Name This Delivery"
            view.txtDeliveryItem.text="Items Being Delivered"

            view.et_title?.isEnabled = true
            view.et_priceItem?.isEnabled = true

        }


    }

    private fun sharePost(title: String,postId: String) {
        if (Constant.isNetworkAvailable(context!!, coordinateLay)) {
            // view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.sharePost
                    , Response.Listener { response ->
                //  view.progressBar.visibility = View.GONE
                progress!!.dismiss()

                val result: JSONObject?
                try {
                    result = JSONObject(response)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    if (status == "success") {
                        val intent = Intent(context, HomeActivity::class.java)
                        intent.putParcelableArrayListExtra("param1",myPostArrayList)
                        startActivity(intent)
                      //  getMyNewPost(view!!)
                        val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")

                        if (userType == Constant.COURIOR) {
                            if (message == "Currently you are inactivate user") {
                                val helper = HelperClass(context!!, activity!!)
                                helper.inActiveByAdmin("Admin inactive your account", true)
                            } else {
                                // view.noDataTxt.visibility = View.VISIBLE
                            }
                        } else {
                            // view.noDataTxt.visibility = View.VISIBLE
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
                    params.put("postId", postId)
                    params.put("title", title)

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


    private fun deletepost(itemId: String, view: View?, position: Int) {
        if (Constant.isNetworkAvailable(context!!, coordinateLay)) {
            // view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deletePost
                    , Response.Listener { response ->
                //  view.progressBar.visibility = View.GONE
                progress!!.dismiss()

                val result: JSONObject?
                try {
                    result = JSONObject(response)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    total= total- myPostArrayList!!.get(position).price!!.toFloat()
                    view!!.et_priceItem.setText("$ "+total)

                    myPostArrayList!!.removeAt(position)

                    customerPostAdapter!!.notifyDataSetChanged()
                    setData(myPostArrayList, view, myPostResponce,total)
                  /*  if (true) {
                        getMyNewPost(view)
                    }*/

                    if (status == "success") {
                      //  getMyNewPost(view!!)
                        val userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
                        if (userType == Constant.COURIOR) {
                            if (message == "Currently you are inactivate user") {
                                val helper = HelperClass(context!!, activity!!)
                                helper.inActiveByAdmin("Admin inactive your account", true)
                            } else {
                                // view.noDataTxt.visibility = View.VISIBLE
                            }
                        } else {
                            // view.noDataTxt.visibility = View.VISIBLE
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

                    params.put("postId", "")
                    params.put("itemId", itemId)

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

    fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
        val backStackName = fragment.javaClass.name
        val fragmentPopped = fragmentManager!!.popBackStackImmediate(backStackName, 0)
        var i = fm?.backStackEntryCount
        if (i != null) {
            while (i > 0) {
                fm?.popBackStackImmediate()
                i--
            }
        }
        if (!fragmentPopped) {
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_UNSET)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
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

}


