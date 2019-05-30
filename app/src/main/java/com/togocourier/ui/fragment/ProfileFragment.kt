package com.togocourier.ui.fragment

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.text.InputFilter
import android.text.TextUtils
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.image.picker.ImagePicker
import com.togocourier.responceBean.GetUserDataProfile
import com.togocourier.responceBean.UpdateProfileInfo
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.*
import com.togocourier.view.cropper.CropImage
import com.togocourier.view.cropper.CropImageView
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_activity_home.*
import kotlinx.android.synthetic.main.new_courier_post_adapter.view.*
import kotlinx.android.synthetic.main.new_dialog_apply_bid.*
import kotlinx.android.synthetic.main.new_fragment_profile.*
import kotlinx.android.synthetic.main.new_fragment_profile.view.*
import kotlinx.android.synthetic.main.new_payment_dialog.*
import kotlinx.android.synthetic.main.new_payment_dialog.view.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

@Suppress("RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ProfileFragment : Fragment() {
    private var from: String? = null
    private var userId: String? = null
    private var postUserId: String? = null
    private var userType: String = ""
    private var email: String = ""
    private var fullName: String = ""
    private var countryCode: String = ""
    var getUserDataProfile=GetUserDataProfile.DataBean()

    private var openDialog: Dialog? = null

    private var gson = Gson()

    private var contactNum: String = ""
    private var address: String = ""
    private var profileImg: String = ""
    private var lat = ""
    private var lng = ""
    private var ratting: Int = 0
    private var profileImageBitmap: Bitmap? = null
    private var autocompleteFragment: SupportPlaceAutocompleteFragment? = null
    private var fm: FragmentManager? = null
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (arguments != null) {
            from = arguments?.getString(ARG_PARAM1)
            userId = arguments?.getString(ARG_PARAM2)
            postUserId = arguments?.getString(ARG_PARAM3)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.new_fragment_profile, container, false)
        progress = ProgressDialog(context!!)
        activity?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        activity?.headerLay?.visibility = View.GONE
        activity?.footer?.visibility = View.VISIBLE

        userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
        email = PreferenceConnector.readString(context!!, PreferenceConnector.USEREMAIL, "")
        fullName = PreferenceConnector.readString(context!!, PreferenceConnector.USERFULLNAME, "")
        countryCode = PreferenceConnector.readString(context!!, PreferenceConnector.USERCOUNTRYCODE, "")
        contactNum = PreferenceConnector.readString(context!!, PreferenceConnector.USERCONTACTNO, "")
        address = PreferenceConnector.readString(context!!, PreferenceConnector.USERADDRESS, "")
        profileImg = PreferenceConnector.readString(context!!, PreferenceConnector.USERPROFILEIMAGE, "")
        lat = PreferenceConnector.readString(context!!, PreferenceConnector.USERLATITUTE, "")
        lng = PreferenceConnector.readString(context!!, PreferenceConnector.USERLONGITUTE, "")
        ratting = PreferenceConnector.readInteger(context!!, PreferenceConnector.RATTING, 0)

        val fRating = ratting.toFloat()
        view.ratingBar.rating = fRating
        view.iv_profile_img.isEnabled = false
        view.ly_location.visibility = View.GONE
        view.rl_address.isEnabled = false
        view.rl_address.isClickable = false
        view.rl_address.isClickable = false

        //setDate(view)

        view.iv_profile_img.setOnClickListener {
            if (Build.VERSION.SDK_INT >= 23) {
                if (context!!.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(
                            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                } else {
                    ImagePicker.pickImage(this@ProfileFragment)
                }
            } else {
                ImagePicker.pickImage(this@ProfileFragment)
            }
        }

        view.iv_settings.setOnClickListener {
            replaceFragment(SettingsFragment(), true, R.id.fragmentPlace)
        }

        view.iv_edit.setOnClickListener {
            Constant.hideSoftKeyboard(this.activity!!)
            activity!!.footer.visibility = View.GONE
            view.iv_back.visibility = View.VISIBLE
            view.btn_update.visibility = View.VISIBLE
            view.iv_edit.visibility = View.GONE
            view.btn_withdraw.visibility=View.GONE
            view.ed_email.isEnabled = true
            view.ed_name.isEnabled = true
            view.ed_phone.isEnabled = true
            view.tv_location.isEnabled = true
            view.iv_profile_img.isEnabled = true
            view.ly_location.isEnabled = true
            view.ly_location.visibility = View.VISIBLE
            showKeyboard(view.ed_name)
        }

        view.btn_withdraw.setOnClickListener {
            showpayment(view)
        }

        view.iv_back.setOnClickListener {
            userType = PreferenceConnector.readString(context!!, PreferenceConnector.USERTYPE, "")
            email = PreferenceConnector.readString(context!!, PreferenceConnector.USEREMAIL, "")
            fullName = PreferenceConnector.readString(context!!, PreferenceConnector.USERFULLNAME, "")
            countryCode = PreferenceConnector.readString(context!!, PreferenceConnector.USERCOUNTRYCODE, "")
            contactNum = PreferenceConnector.readString(context!!, PreferenceConnector.USERCONTACTNO, "")
            address = PreferenceConnector.readString(context!!, PreferenceConnector.USERADDRESS, "")
            profileImg = PreferenceConnector.readString(context!!, PreferenceConnector.USERPROFILEIMAGE, "")
            lat = PreferenceConnector.readString(context!!, PreferenceConnector.USERLATITUTE, "")
            lng = PreferenceConnector.readString(context!!, PreferenceConnector.USERLONGITUTE, "")
            ratting = PreferenceConnector.readInteger(context!!, PreferenceConnector.RATTING, 0)

            //setDate(view, getUserDataProfile)

            view.btn_update.visibility = View.GONE
            view.iv_back.visibility = View.GONE
            view.iv_edit.visibility = View.VISIBLE
            view.iv_settings.visibility = View.VISIBLE

            view.tv_name.text = view.ed_name.text.toString()
            view.tv_card_address.text = address

            view.ed_email.isEnabled = false
            view.ed_name.isEnabled = false
            view.ed_phone.isEnabled = false
            view.tv_location.isEnabled = false
            view.iv_profile_img.isEnabled = false
            view.ly_location.isEnabled = false
            view.ly_location.visibility = View.GONE

            activity!!.footer.visibility = View.VISIBLE
        }




        autocompleteFragment = childFragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as SupportPlaceAutocompleteFragment
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                address = place.address.toString()
                view.tv_location.text = place.address.toString()
                lat = place.latLng.latitude.toString()
                lng = place.latLng.longitude.toString()
                activity!!.footer.visibility = View.GONE
                Constant.hideSoftKeyboard(activity!!)
            }

            override fun onError(status: Status) {

            }
        })
        getUserProfile(view)




        view.btn_update.setOnClickListener {
            if (isValidCustomerData(view)) updateProfile(view)

        }


        if (userType == Constant.CUSTOMER) {
            if (from != null) {
                if (from!! == "AllRequestActivity") {
                    view.ratingBar.visibility = View.VISIBLE
                    view.ly_ratting_bar.isEnabled = false
                }
            } else view.ratingBar.visibility = View.GONE

        } else if (userType == Constant.COURIOR) {
            view.rlBalanceN.visibility=View.VISIBLE
            view.ratingBar.visibility = View.VISIBLE
            view.btn_withdraw.visibility=View.VISIBLE
            view.rlImage.visibility=View.VISIBLE
        }
        return view
    }

    fun showpayment(view: View) {
        openDialog = Dialog(context)
        openDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog!!.setCancelable(false)
        openDialog!!.setContentView(R.layout.new_payment_dialog)
        openDialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog!!.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog!!.window!!.attributes = lWindowParams
        openDialog!!.txtPrice.text
        openDialog!!.popPriceTxt_new.setFilters(arrayOf<InputFilter>(InputFilter.LengthFilter(getUserDataProfile.withdrawalLimit!!.toInt())))
        val  newtotal= getUserDataProfile.myAmount!!.toDouble()/100
        openDialog!!.txtPrice.text="$%.2f".format(newtotal)
        // openDialog!!.applyBtn.setOnClickListener(this)

        openDialog!!.rlcancelNew.setOnClickListener({
            Constant.hideSoftKeyboard(this.activity!!)
            openDialog!!.dismiss()

        })

        openDialog!!.submitBtnNew.setOnClickListener {
          if (TextUtils.isEmpty(openDialog!!.popPriceTxt_new.text.toString().trim()))  {
              Toast.makeText(context,"Please enter Amount",Toast.LENGTH_SHORT).show()
          }else if  (openDialog!!.popPriceTxt_new.text.toString().equals("0")){
              Toast.makeText(context,"Amount can't be zero",Toast.LENGTH_SHORT).show()

          }else if (getUserDataProfile.withdrawalLimit!!.toInt()>openDialog!!.popPriceTxt_new.text.toString().toInt()){
              Toast.makeText(context,"Amount is more than withdraw limit",Toast.LENGTH_SHORT).show()

          }

          else {
              withdrawApi(view,openDialog!!.popPriceTxt_new.text.toString().trim(), openDialog!!)
          }
        }
        openDialog!!.show()

    }

    private fun replaceFragment(fragment: Fragment, addToBackStack: Boolean, containerId: Int) {
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
            val transaction = fragmentManager!!.beginTransaction()
            transaction.replace(containerId, fragment, backStackName).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            if (addToBackStack)
                transaction.addToBackStack(backStackName)
            transaction.commit()
        }

    }

    private fun showKeyboard(ettext: EditText) {
        ettext.requestFocus()
        ettext.postDelayed({
            val keyboard = context!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            keyboard.showSoftInput(ettext, 0)
        }, 200)
    }

    private fun setDate(view: View, userDataProfile: GetUserDataProfile.DataBean) {
        Glide.with(context!!).load(userDataProfile.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(view.iv_profile)
        Glide.with(context!!).load(userDataProfile.profileImage).apply(RequestOptions().placeholder(R.drawable.new_app_icon1)).into(view.iv_profile_img)
        view.ed_Balance.setText("Balance")
        val  newtotal= getUserDataProfile.myAmount!!.toDouble()/100

        view.ed_myPrice.setText("$%.2f".format(newtotal))
        view.tv_name.text = userDataProfile.fullName
        view.ed_name.setText(userDataProfile.fullName)
        view.ed_email.setText(userDataProfile.email)
        if (contactNum != "") {
            view.ed_phone.setText(userDataProfile.contactNo)
        } else {
            view.ed_phone.hint = "NA"
        }

        if (address != "") {
            view.tv_card_address.text = userDataProfile.address
            view.tv_location.text = userDataProfile.address
        } else {
            view.tv_card_address.hint = "NA"
            view.tv_location.hint = "NA"
        }

    }

    companion object {
        private val ARG_PARAM1 = "param1"
        private val ARG_PARAM2 = "param2"
        private val ARG_PARAM3 = "param3"

        fun newInstance(param1: String, param2: String, postUserId: String?): ProfileFragment {
            val fragment = ProfileFragment()
            val args = Bundle()
            args.putString(ARG_PARAM1, param1)
            args.putString(ARG_PARAM2, param2)
            args.putString(ARG_PARAM3, postUserId)

            fragment.arguments = args
            return fragment
        }
    }

    private fun withdrawApi(view: View, price: String, openDialog: Dialog) {
        if (Constant.isNetworkAvailable(this.context!!, view)) {
            // view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.withdraw
                    , Response.Listener { response ->
                //  view.progressBar.visibility = View.GONE
                progress!!.dismiss()

                val result: JSONObject?
                try {
                    result = JSONObject(response)
                    val status = result.getString("status")
                    val message = result.getString("message")

                    if (status == "success") {
                        openDialog.dismiss()








                        /* if (activity != null && userType == Constant.COURIOR) {
                             if (myPostResponce.isTrack.equals("YES")) {
                                 // activity!!.startService(Intent(activity!!, MyLocationService::class.java))

                                 //  HelperClass.startBackgroundService(mContext!!)

                                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                     HelperClass.startBackgroundService(mContext!!)
                                 } else {
                                     HelperClass.startJobDispatcher(mContext!!)
                                 }

                             } else if (myPostResponce.isTrack.equals("NO")) {
                                 // activity!!.stopService(Intent(activity!!, MyLocationService::class.java))
                                 // HelperClass.stopBackgroundService(mContext!!)

                                 if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                     HelperClass.stopBackgroundService(mContext!!)
                                 } else {
                                     HelperClass.stopJobDispatcher(mContext!!)
                                 }
                             }
                         }*/
                    } else {
                        openDialog.dismiss()
                        val userType = PreferenceConnector.readString(this!!.context!!, PreferenceConnector.USERTYPE, "")
                        if (userType == Constant.COURIOR) {
                            if (message == "Currently you are inactivate user") {
                                val helper = HelperClass(this!!.context!!, activity!!)
                                helper.inActiveByAdmin("Admin inactive your account", true)
                            } else {
                                Toast.makeText(context,message,Toast.LENGTH_SHORT).show()

                                //view.noDataTxt.visibility = View.VISIBLE
                            }
                        } else {
                            //view.noDataTxt.visibility = View.VISIBLE
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

                    params.put("price", price)


                    return params
                }


            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this!!.context!!).addToRequestQueue(stringRequest)
        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }



    private fun getUserProfile(view: View) {
        if (Constant.isNetworkAvailable(this.context!!, view)) {
            // view.progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getUserProfile+"?userId="+PreferenceConnector.readString(this!!.context!!,PreferenceConnector.USERID,"")
                    , Response.Listener { response ->
                //  view.progressBar.visibility = View.GONE
                progress!!.dismiss()

                val result: JSONObject?
                try {
                    result = JSONObject(response)
                    val message = result.getString("message")
                    if(message.equals("Invalid token")){
                        val helper = HelperClass(this.context!!, this.activity!!)
                        helper.sessionExpairDialog()
                    }else{
                        val status = result.getString("status")

                        if (status == "success") {
                            val data = result.getJSONObject("data")
                            getUserDataProfile=gson.fromJson(data.toString(),GetUserDataProfile.DataBean::class.java)
                            setDate(view,getUserDataProfile)









                            /* if (activity != null && userType == Constant.COURIOR) {
                                 if (myPostResponce.isTrack.equals("YES")) {
                                     // activity!!.startService(Intent(activity!!, MyLocationService::class.java))

                                     //  HelperClass.startBackgroundService(mContext!!)

                                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                         HelperClass.startBackgroundService(mContext!!)
                                     } else {
                                         HelperClass.startJobDispatcher(mContext!!)
                                     }

                                 } else if (myPostResponce.isTrack.equals("NO")) {
                                     // activity!!.stopService(Intent(activity!!, MyLocationService::class.java))
                                     // HelperClass.stopBackgroundService(mContext!!)

                                     if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                         HelperClass.stopBackgroundService(mContext!!)
                                     } else {
                                         HelperClass.stopJobDispatcher(mContext!!)
                                     }
                                 }
                             }*/
                        } else {
                            val userType = PreferenceConnector.readString(this!!.context!!, PreferenceConnector.USERTYPE, "")
                            if (userType == Constant.COURIOR) {
                                if (message == "Currently you are inactivate user") {
                                    val helper = HelperClass(this!!.context!!, activity!!)
                                    helper.inActiveByAdmin("Admin inactive your account", true)
                                } else {
                                    //view.noDataTxt.visibility = View.VISIBLE
                                }
                            } else {
                                //view.noDataTxt.visibility = View.VISIBLE
                            }

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


            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this!!.context!!).addToRequestQueue(stringRequest)
        } else {
            Toast.makeText(context!!, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }



    private fun updateProfile(view: View) {
        if (Constant.isNetworkAvailable(context!!, view.parentLay)) {
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

                    activity!!.footer.visibility = View.VISIBLE

                    if (status == "success") {
                        val gson = Gson()
                        val updateProfileInf = gson.fromJson(resultResponse, UpdateProfileInfo::class.java)
                        updateSession(updateProfileInf)
                        Constant.snackbar(view.parentLay, getString(R.string.update_profile))
                        view.btn_update.visibility = View.GONE
                        view.iv_back.visibility = View.GONE
                        view.iv_edit.visibility = View.VISIBLE
                        view.iv_settings.visibility = View.VISIBLE

                        view.tv_name.text = view.ed_name.text.toString()
                        view.tv_card_address.text = address

                        view.ed_email.isEnabled = false
                        view.ed_name.isEnabled = false
                        view.ed_phone.isEnabled = false
                        view.tv_location.isEnabled = false
                        view.iv_profile_img.isEnabled = false
                        view.ly_location.isEnabled = false
                        view.ly_location.visibility = View.GONE

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

                    params.put("email", view.ed_email.text.toString())
                    params.put("fullName", view.ed_name.text.toString())
                    params.put("countryCode", "+1")
                    params.put("contactNo", view.ed_phone.text.toString())
                    params.put("address", address)
                    params.put("latitude", lat)
                    params.put("longitude", lng)

                    return params
                }

                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (profileImageBitmap != null) {
                            params.put("profileImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(profileImageBitmap!!), "image/jpg"))
                        }

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

        updateProfileFCM()
    }

    private fun isEmailValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()
    }

    private fun isValidCustomerData(view: View): Boolean {
        val v = Validation()

        if (v.isEmpty(view.ed_name)) {
            Constant.snackbar(view.parentLay, "Full Name can't be empty")
            view.ed_name.requestFocus()
            return false
        } else if (v.isValidName(view.ed_name)) {
            Constant.snackbar(view.parentLay, "Name allow alphabets and space only")
            view.ed_name.requestFocus()
            return false
        } else if (v.isEmpty(view.ed_email)) {
            Constant.snackbar(view.parentLay, "Email address can't be empty")
            view.ed_email.requestFocus()
            return false
        } else if (!isEmailValid(view.ed_email)) {
            Constant.snackbar(view.parentLay, "Enter valid email")
            view.ed_name.requestFocus()
            return false
        } else if (v.isEmpty(view.ed_phone)) {
            Constant.snackbar(view.parentLay, "Contact no can't be empty")
            view.ed_phone.requestFocus()
            return false
        } else if (!isContactNoValid(view.ed_phone)) {
            Constant.snackbar(view.parentLay, "Enter valid contact no")
            view.ed_phone.requestFocus()
            return false
        } else if (v.isEmpty(view.tv_location)) {
            Constant.snackbar(view.parentLay, "Please enter your address")
            view.tv_location.requestFocus()
            return false
        }
        return true
    }

    private fun isContactNoValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }
        return getValue.length in 7..11
    }

    private fun updateProfileFCM() {
        val user = UserInfoFCM()
        user.notificationStatus = PreferenceConnector.readString(context!!, PreferenceConnector.ISNOTIFICATIONON, "")
        user.uid = PreferenceConnector.readString(context!!, PreferenceConnector.USERID, "")
        user.email = PreferenceConnector.readString(context!!, PreferenceConnector.USEREMAIL, "")
        user.name = PreferenceConnector.readString(context!!, PreferenceConnector.USERFULLNAME, "")
        user.firebaseToken = FirebaseInstanceId.getInstance().token!!
        user.userType=PreferenceConnector.readString(context!!,PreferenceConnector.USERTYPE,"")
        user.profilePic = PreferenceConnector.readString(context!!, PreferenceConnector.USERPROFILEIMAGE, "")

        FirebaseDatabase.getInstance().reference.child(Constant.ARG_USERS).child(user.uid).setValue(user).addOnCompleteListener { task ->

        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
                                            grantResults: IntArray) {
        when (requestCode) {

            Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this@ProfileFragment)
                }
            }

            Constant.MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this@ProfileFragment)
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 234) {    // Image Picker
                val imageUri = ImagePicker.getImageURIFromResult(context, requestCode, resultCode, data)
                if (imageUri != null) {
                    // Calling Image Cropper
                    CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE)
                            .setAspectRatio(4, 3)
                            .start(context!!, this@ProfileFragment)
                } else {
                    Constant.snackbar(parentLay, resources.getString(R.string.something_went_wrong))
                }

            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {   // Image Cropper
                val result = CropImage.getActivityResult(data)
                try {
                    if (result != null) {
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(context!!.contentResolver, result.uri)
                        iv_profile.setImageBitmap(profileImageBitmap)
                        iv_profile_img.setImageBitmap(profileImageBitmap)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Constant.snackbar(parentLay, resources.getString(R.string.alertImageException))
                } catch (error: OutOfMemoryError) {
                    Constant.snackbar(parentLay, resources.getString(R.string.alertOutOfMemory))
                }

            }
        }
    }

}
