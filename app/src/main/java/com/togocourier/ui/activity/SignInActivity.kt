package com.togocourier.ui.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.CursorLoader
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.AnimatedVectorDrawable
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.provider.Settings
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.responceBean.Chat
import com.togocourier.responceBean.RegistrationResponse
import com.togocourier.responceBean.UploadIdResponse
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.*
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.dialog_show_welcome.*
import kotlinx.android.synthetic.main.new_activity_sign_in.*
import kotlinx.android.synthetic.main.new_upload_id_layout.*
import org.json.JSONException
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.lang.reflect.Array.get
import java.util.*
import kotlin.collections.HashMap

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SignInActivity : AppCompatActivity(), View.OnClickListener,
        com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private var callbackManager: CallbackManager? = null

    val RC_SIGN_IN = 101
    var otherName: String? = ""
    private var otherProfileImage: String? = ""
    private var openDialog: Dialog? = null
    private var userInfoFCM: UserInfoFCM? = null


    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var mGoogleApiClient: GoogleApiClient? = null
    private var lat: Double? = null
    private var otherUId: String? = ""
    private var image_FirebaseURL: Uri? = null
    private var getMessageCount = 0


    private var firebaseDatabase: FirebaseDatabase? = null
    private var DatabaseReference = FirebaseDatabase.getInstance()



    private var myUId: String? = ""

    private var lng: Double? = null
    private var lmgr: LocationManager? = null
    private var rocketAnimation:AnimatedVectorDrawable?=null
    private var isGPSEnable: Boolean = false
    var alertDialog: Dialog? = null

    private var email: String = ""
    private var fullName: String = ""
    private var profileImg: String = ""
    private var uID: String = ""
    private var userType = ""
    private var chatNode: String? = null

    private var cardImageBitmap: Bitmap? = null
    private var isRememberMeChecked = false
    private var address: String = ""

    private var mGoogleSignInClient: GoogleSignInClient? = null

    // variable to track event time
    private var mLastClickTime: Long = 0

    private var gson = Gson()
    private var progress: ProgressDialog? = null

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_sign_in)
        firebaseDatabase = FirebaseDatabase.getInstance()

        progress = ProgressDialog(this@SignInActivity)
        initializeView()

        FacebookSdk.sdkInitialize(applicationContext)
        callbackManager = CallbackManager.Factory.create()
        fbSetUpMethod()

        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        userType = RemPreferenceConnector.readString(this, RemPreferenceConnector.USERTYPE, "")

        if (userType == Constant.CUSTOMER){
            RemPreferenceConnector.writeString(this, RemPreferenceConnector.WELCOMESCREENSHOW_USER, "save_cust")

        }else if(userType == Constant.COURIOR){
            RemPreferenceConnector.writeString(this, RemPreferenceConnector.WELCOMESCREENSHOW_COURIER, "save_cust")

        }
    }

    private fun initializeView() {
        signInBtn.setOnClickListener(this)
        fbLoginBtn.setOnClickListener(this)
        forgotPwdTxt.setOnClickListener(this)
        signUpLay.setOnClickListener(this)
        gmailLoginBtn.setOnClickListener(this)
        rl_remember_me.setOnClickListener(this)
        iv_back.setOnClickListener(this)

        val emId = RemPreferenceConnector.readString(this, RemPreferenceConnector.REM_USEREMAIL, "")
        val pwd = RemPreferenceConnector.readString(this, RemPreferenceConnector.REM_PWD, "")

        if (pwd != "") {
            isRememberMeChecked = true
            cb_rem_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_check_box_ico))
        }

        emailTxt.setText(emId)
        pwdTxt.setText(pwd)
    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.signInBtn -> {
                Constant.hideSoftKeyboard(this)
                if (isValidData()) doSignIn()
            }
            R.id.fbLoginBtn -> {
                if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
                    if (isGpsEnable())
                        LoginManager.getInstance().logInWithReadPermissions(
                                this, Arrays.asList("public_profile", "email"))
                }

            }
            R.id.forgotPwdTxt -> {
                startActivity(Intent(this, ForgotPassActivity::class.java))
            }
            R.id.signUpLay -> {
                startActivity(Intent(this, SignUpActivity::class.java))
            }
            R.id.gmailLoginBtn -> {
                gmailLoginBtn.isEnabled = false
                signIn()
            }

            R.id.rl_remember_me -> {
                if (isRememberMeChecked) {
                    isRememberMeChecked = false
                    cb_rem_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_inactive_check_box_ico))
                } else {
                    isRememberMeChecked = true
                    cb_rem_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_active_check_box_ico))
                }
            }

            R.id.iv_back -> {
                onBackPressed()
            }
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, UserSelectionActivity::class.java))
        finish()
    }

    private fun isGpsEnable(): Boolean {
        isGPSEnable = lmgr!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        if (!isGPSEnable) {
            val ab = android.support.v7.app.AlertDialog.Builder(this)
            ab.setTitle(R.string.gps_not_enable)
            ab.setMessage(R.string.do_you_want_to_enable)
            ab.setCancelable(false)
            ab.setPositiveButton(R.string.settings, { dialog, which ->
                isGPSEnable = true
                val `in` = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(`in`)
            })
            ab.show()
        }
        return isGPSEnable
    }

    private fun isValidData(): Boolean {
        val v = Validation()
        if (v.isEmpty(emailTxt)) {
            Constant.snackbar(mainLayout, "Email address can't be empty")
            emailTxt.requestFocus()
            return false
        } else if (!isEmailValid(emailTxt)) {
            Constant.snackbar(mainLayout, "Enter valid email")
            emailTxt.requestFocus()
            return false
        } else if (v.isEmpty(pwdTxt)) {
            Constant.snackbar(mainLayout, "Password can't be empty")
            pwdTxt.requestFocus()
            return false
        } else if (!isPasswordValid(pwdTxt)) {
            Constant.snackbar(mainLayout, "Password should be 6 character long")
            pwdTxt.requestFocus()
            return false
        }
        return true
    }

    private fun isEmailValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim()
        return android.util.Patterns.EMAIL_ADDRESS.matcher(getValue).matches()
    }

    private fun isPasswordValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim()
        return getValue.length > 5
    }

    private fun doSignIn() {
        signInBtn.isEnabled = false
        if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
            //  progressBar.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.Log_In_Url,
                    Response.Listener { response ->
                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {

                                if (isRememberMeChecked) {
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_USEREMAIL, emailTxt.text.toString())
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_PWD, pwdTxt.text.toString())
                                } else {
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_USEREMAIL, "")
                                    RemPreferenceConnector.writeString(this, RemPreferenceConnector.REM_PWD, "")
                                }

                                progress!!.dismiss()

                                val signInResponce = gson.fromJson(response, RegistrationResponse::class.java)
                                setSession(signInResponce)
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, signInResponce.userData!!.authToken.toString())
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())
                                PreferenceConnector.writeString(this@SignInActivity,PreferenceConnector.POSTID,"")
                                PreferenceConnector.writeString(this@SignInActivity,PreferenceConnector.ADDBANK, signInResponce.userData!!.addBank!!)

                                fullName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
                                uID = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
                                userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                profileImg = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")
                                email = PreferenceConnector.readString(this, PreferenceConnector.USEREMAIL, "")
                                loginFirebaseDataBase()
                                val intent = Intent(this, HomeActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()
                                /*val intent = Intent(this, HomeActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()*/

                                RemPreferenceConnector.writeString(this, RemPreferenceConnector.USERTYPE, userType)

                              //  HelperClass.stopJobDispatcher(this)
                              //  HelperClass.stopBackgroundService(this)

                                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                    HelperClass.stopBackgroundService(this)
                                } else {
                                    HelperClass.stopJobDispatcher(this)
                                }

                                /*if(userType.equals(Constant.CUSTOMER)){
                                  //  stopService(Intent(this, MyLocationService::class.java))

                                    HelperClass.stopJobDispatcher(this)

                                    *//*val helper = HelperClass(this, this)
                                    helper.stopAlarmService(this)*//*
                                }*/

                            } else {
                                signInBtn.isEnabled = true
                                //   progressBar.visibility = View.GONE
                                progress!!.dismiss()

                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Currently you are inactive by admin, please contact admin", false)
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

                        //  Toast.makeText(this@SignInActivity, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("email", emailTxt.text.toString())
                    params.put("password", pwdTxt.text.toString())
                    params.put("userType", userType)
                    params.put("deviceType", "2")
                    params.put("deviceToken", FirebaseInstanceId.getInstance().token.toString())
                    return params
                }
            }

            stringRequest.retryPolicy = DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        }else{
            signInBtn.isEnabled = true
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }

        callbackManager?.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Constant.REQUEST_CAMERA) {
                if (data != null) {

                    cardImageBitmap = data.extras!!.get("data") as Bitmap
                    val bytes = ByteArrayOutputStream()
                    cardImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    alertDialog!!.idTxt.text = "Id Card is Uploaded"
                }
            } else if (requestCode == Constant.SELECT_FILE) {
                if (data != null) {
                    val selectedImageUri = data.data
                    val projection = arrayOf(MediaStore.MediaColumns.DATA)
                    val cursorLoader = CursorLoader(this, selectedImageUri, projection, null, null, null)
                    val cursor = cursorLoader.loadInBackground()
                    val column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA)
                    cursor.moveToFirst()
                    val selectedImagePath = cursor.getString(column_index)

                    cardImageBitmap = ImageUtil.decodeFile(selectedImagePath)
                    try {
                        cardImageBitmap = ImageUtil.modifyOrientation(cardImageBitmap!!, selectedImagePath)
                        val bytes = ByteArrayOutputStream()
                        cardImageBitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                        alertDialog!!.idTxt.text = "Id Card is Uploaded"

                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                }
            }

        }


    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            fullName = account?.displayName.toString()
            email = account?.email.toString()
            profileImg = account?.photoUrl.toString()
            val socialId = account?.id.toString()
            val deviceToken = FirebaseInstanceId.getInstance().token.toString()
            doRegistration(fullName, email, socialId, "gmail", profileImg, deviceToken)

        } catch (e: ApiException) {
            //  progressBar.visibility = View.GONE
            progress!!.dismiss()

            gmailLoginBtn.isEnabled = true
        }

    }

    private fun signIn() {
        // progressBar.visibility = View.VISIBLE
        progress!!.show()

        val signInIntent = mGoogleSignInClient?.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    private fun setSession(signInResponce: RegistrationResponse) {
        PreferenceConnector.clear(this)
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERTYPE, signInResponce.userData?.userType.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERID, signInResponce.userData?.id.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USEREMAIL, signInResponce.userData?.email.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERSOCIALID, signInResponce.userData?.socialId.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERSOCIALTYPE, signInResponce.userData?.socialType.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERPROFILEIMAGE,signInResponce .userData?.profileImage.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERFULLNAME, signInResponce .userData?.fullName.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERCOUNTRYCODE, signInResponce .userData?.countryCode.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERCONTACTNO,signInResponce .userData?.contactNo.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERADDRESS, signInResponce .userData?.address.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERLATITUTE, signInResponce .userData?.latitude.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERLONGITUTE, signInResponce .userData?.longitude.toString())
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.ISLOGIN, "yes")
        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.ISNOTIFICATIONON, signInResponce .userData?.notificationStatus.toString())
        PreferenceConnector.writeInteger(this@SignInActivity, PreferenceConnector.RATTING, signInResponce .userData?.rating!!)

        val session = SessionManager(this)
        session.createSessionAuthToken(signInResponce.userData?.authToken.toString())

    }

    private fun fbSetUpMethod() {
        LoginManager.getInstance().registerCallback(callbackManager, object : FacebookCallback<LoginResult> {
            override fun onSuccess(loginResult: LoginResult?) {
                val sSocialId = loginResult?.accessToken?.userId
                val request = GraphRequest.newMeRequest(loginResult?.accessToken) { `object`, response ->
                    try {
                        var email_fb = ""
                        if (`object`.has("email")) {
                            email_fb = `object`.getString("email")
                        }
                        val socialId_ = `object`.getString("id")
                        val firstname = `object`.getString("first_name")
                        val lastname = `object`.getString("last_name")
                        val fullname = firstname + " " + lastname
                        val profileImage = "https://graph.facebook.com/$sSocialId/picture?type=large"
                        val deviceToken: String = FirebaseInstanceId.getInstance().token.toString()
                        val params = HashMap<String, String>()
                        params.put("deviceToken", deviceToken)

                        doRegistration(fullname, email_fb, socialId_, "facebook", profileImage, deviceToken)
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                }
                val parameters = Bundle()
                parameters.putString("fields", "id, first_name, last_name, email, picture")
                request.parameters = parameters
                request.executeAsync()
            }

            override fun onCancel() {

            }

            override fun onError(exception: FacebookException?) {
                if (exception is FacebookAuthorizationException) {
                    if (AccessToken.getCurrentAccessToken() != null) {
                        LoginManager.getInstance().logOut()
                    }
                }
                exception?.printStackTrace()
            }
        })
    }

    private fun doRegistration(fullname: String, email_fb: String, socialId_: String, socialType: String, profileImage: String, deviceToken: String) {
        if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
            //  progressBar.visibility = View.VISIBLE
            progress!!.show()

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.Registration_Url, Response.Listener { response ->
                val resultResponse = String(response.data)
                try {

                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    gmailLoginBtn.isEnabled = true
                    if (status == "success") {
                        val registrationResponce = gson.fromJson(resultResponse, RegistrationResponse::class.java)

                        if (registrationResponce.userData!!.userType.equals(Constant.COURIOR)) {

                            if (registrationResponce.userData?.uploadIdCard.equals("")) {
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, registrationResponce.userData?.authToken.toString())
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERID, registrationResponce.userData?.id.toString())
                                uploadIdCardDialog()
                                //   progressBar.visibility = View.GONE
                                progress!!.dismiss()

                            } else {
                                progress!!.dismiss()
                                setSession(registrationResponce)
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, registrationResponce.userData?.authToken.toString())
                                PreferenceConnector.writeString(this@SignInActivity,PreferenceConnector.ADDBANK, registrationResponce.userData?.addBank!!)
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())
                                PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.POSTID, "")
                                fullName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
                                uID = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
                                profileImg = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")
                                email = PreferenceConnector.readString(this, PreferenceConnector.USEREMAIL, "")
                                addUserFirebaseDatabase()
                                gettingDataFromUserTable()
                                firebaseChatRegister()

                                val intent = Intent(this, HomeActivity::class.java)
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                startActivity(intent)
                                finish()

                               // showWelcome()

                            }

                        } else if (registrationResponce.userData?.userType.equals(Constant.CUSTOMER)) {
                            progress!!.dismiss()

                            setSession(registrationResponce)
                            PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, registrationResponce.userData?.authToken.toString())
                            PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())
                            fullName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
                            PreferenceConnector.writeString(this@SignInActivity,PreferenceConnector.ADDBANK, registrationResponce.userData?.addBank!!)

                            uID = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
                            profileImg = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")
                            email = PreferenceConnector.readString(this, PreferenceConnector.USEREMAIL, "")
                            firebaseDatabase!!.reference.child(Constant.ARG_USERS).child(uID).addListenerForSingleValueEvent(object :ValueEventListener{
                                override fun onDataChange(p0: DataSnapshot) {
                                    if (p0.getValue(UserInfoFCM::class.java) != null) {
                                        userInfoFCM = p0.getValue(UserInfoFCM::class.java)!!
                                        if (!userInfoFCM!!.name.equals(fullName)){
                                            showWelcome()

                                        }else{
                                            val intent = Intent(this@SignInActivity, HomeActivity::class.java)
                                            startActivity(intent)
                                            finish()
                                        }
                                        // title_header.text = userInfoFcm.name
                                    }else{
                                        showWelcome()
                                    }

                                }

                                override fun onCancelled(p0: DatabaseError) {

                                }

                            })
                        }


                    } else {
                        // progressBar.visibility = View.GONE
                        progress!!.dismiss()

                        gmailLoginBtn.isEnabled = true

                        if (userType == Constant.COURIOR) {
                            if (message == "Currently you are inactivate user") {
                                val helper = HelperClass(this, this)
                                helper.inActiveByAdmin("Currently you are inactive by admin, please contact admin", false)
                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } else {
                            Constant.snackbar(mainLayout, message)
                        }

                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    gmailLoginBtn.isEnabled = true
                }
            }, Response.ErrorListener { error ->
                val networkResponse = error.networkResponse
                //  progressBar.visibility = View.GONE
                progress!!.dismiss()

                if (networkResponse != null) {
                    if (networkResponse.statusCode == 300) {
                        val helper = HelperClass(this, this)
                        helper.sessionExpairDialog()
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
                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("userType", userType)
                    params.put("deviceType", "2")
                    params.put("deviceToken", deviceToken)
                    params.put("socialType", socialType)
                    params.put("email", email_fb)
                    params.put("contactNo","")
                    params.put("countryCode","+1")
                    params.put("fullName", fullname)
                    params.put("socialId", socialId_)
                    params.put("address", address)
                    params.put("latitude", lat.toString())
                    params.put("longitude", lng.toString())
                    params.put("profileImage", profileImage)

                    return params
                }


            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(multipartRequest)

        }else{
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    // location update.....................................................

    override fun onLocationChanged(p0: Location?) {
        lat = p0?.latitude
        lng = p0?.longitude
        if (lat != null && lng != null) {
            address = getCompleteAddressString(p0?.latitude!!, p0.longitude)
            if (mGoogleApiClient!!.isConnected) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)

            }
        }
    }

    override fun onConnected(p0: Bundle?) {
        startLocationUpdates()
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
        mGoogleSignInClient!!.signOut()

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
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this@SignInActivity)
    }
    //....................................................................................................


    private fun getCompleteAddressString(LATITUDE: Double, LONGITUDE: Double): String {
        var strAdd = ""
        val geocoder = Geocoder(this, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
            if (addresses != null) {
                val returnedAddress = addresses[0]
                val strReturnedAddress = StringBuilder("")
                for (i in 0..returnedAddress.maxAddressLineIndex) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n")
                }
                strAdd = strReturnedAddress.toString()

            } else {
                Toast.makeText(this, "No Address returned!", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
           Toast.makeText(this, "Cannot get Address!", Toast.LENGTH_SHORT).show()

        }
        return strAdd
    }




    fun uploadIdCardDialog() {
        alertDialog = Dialog(this)
        alertDialog?.requestWindowFeature(Window.FEATURE_NO_TITLE)
        alertDialog?.setContentView(R.layout.new_upload_id_layout)
        alertDialog?.setCancelable(false)
        alertDialog?.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        alertDialog?.idCardLay?.setOnClickListener {
            selectImage()
        }
        alertDialog?.close_popup?.setOnClickListener {
            alertDialog?.dismiss()
        }

        alertDialog?.doneBtn?.setOnClickListener {
            if (cardImageBitmap != null) {
                uploadIdCardApi()
            } else Toast.makeText(this, "Please Add your Id Card", Toast.LENGTH_SHORT).show()

        }
        alertDialog?.show()

    }

    private fun firebaseChatRegister() {
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(uID + "@togo.com", "123456")
                .addOnCompleteListener(this, { task ->
                    if (task.isSuccessful) {
                        addUserFirebaseDatabase(email, fullName, profileImg, userType, uID)
                    } else {
                        loginFirebaseDataBase()

                    }
                })
    }

    private fun loginFirebaseDataBase() {
        FirebaseAuth.getInstance()
                .signInWithEmailAndPassword(uID + "@togo.com", "123456")
                .addOnCompleteListener(this@SignInActivity) { task ->
                    if (task.isSuccessful) {
                        addUserFirebaseDatabase(email, fullName, profileImg, uID,userType)
                    } else {

                        // Write data to user table in firebase
                        addUserFirebaseDatabase(email, fullName, profileImg, uID,userType)

                    }
                }
    }

 /*   private fun addUserFirebaseDatabase(email: String, name: String, image: String, userType: String, uID: String) {
        val database = FirebaseDatabase.getInstance().reference
        val user = UserInfoFCM()
        user.notificationStatus = PreferenceConnector.readString(this, PreferenceConnector.ISNOTIFICATIONON, "")
        user.uid = uID
        user.userType = userType
        user.email = email
        user.name = name
        user.firebaseToken = FirebaseInstanceId.getInstance().token.toString()
        user.profilePic = image

        database.child(Constant.ARG_USERS)
                .child(user.uid)
                .setValue(user)
                .addOnCompleteListener { task ->
                    //  progressBar.visibility = View.GONE
                    progress!!.dismiss()

                    if (task.isSuccessful) {
                        if (userType == Constant.COURIOR) {
                            val helper = HelperClass(this, this)
                            helper.inActiveByAdmin("Registration done, but you cannot access the app before admin give you login access", true)
                        } else {
                            val intent = Intent(this, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                    } else {
                        Toast.makeText(this, "data not store at firebase server", Toast.LENGTH_SHORT).show()
                    }
                }
    }*/


    private fun addUserFirebaseDatabase(email: String, name: String, image: String, uID: String,usertype:String) {
        val database = FirebaseDatabase.getInstance().reference
        val user = UserInfoFCM()
        user.notificationStatus = PreferenceConnector.readString(this, PreferenceConnector.ISNOTIFICATIONON, "")
        user.uid = uID
        user.email = email
        user.userType = usertype
        user.name = name
        user.firebaseToken = FirebaseInstanceId.getInstance().token.toString()
        user.profilePic = image

        database.child(Constant.ARG_USERS)
                .child(user.uid)
                .setValue(user)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                      /*  val intent = Intent(this, HomeActivity::class.java)
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                        startActivity(intent)
                        finish()*/
                    } else {
                        Toast.makeText(this, "data not store at firebase server", Toast.LENGTH_SHORT).show()
                    }

                    //  progressBar.visibility = View.GONE
                    progress!!.dismiss()
                }
    }


    private fun addUserFirebaseDatabase() {
        val device_token = FirebaseInstanceId.getInstance().token
        val name = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
        val uId = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
        val profilePic: String
        profilePic = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")
        val database = FirebaseDatabase.getInstance().reference
        val userModel = UserInfoFCM()
        userModel.firebaseToken = device_token!!
        userModel.name = name
        userModel.profilePic = profilePic
        userModel.uid = uId

        database.child(Constant.ARG_USERS).child(uId).setValue(userModel).addOnCompleteListener {
            myUId = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
            otherUId = "0"

            chatNode = gettingNotes()

            firebaseDatabase!!.getReference().child(Constant.ARG_CHAT_ROOMS).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (!dataSnapshot.hasChild(chatNode!!)) {
                        if (otherUId == "0") {
                            send_msg()
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {

                }
            })
        }
    }

    private fun gettingNotes(): String {
        val myUid_ = Integer.parseInt(myUId)
        val otherUID_ = Integer.parseInt(otherUId)

        if (myUid_ < otherUID_) {
            chatNode = myUId + "_" + otherUId
        } else {
            chatNode = otherUId + "_" + myUId
        }

        return chatNode as String
    }

    private fun send_msg() {
        val pushKey = this.chatNode?.let { DatabaseReference.reference.child(Constant.ARG_CHAT_ROOMS).child(it).push().key }
        var msg = "Welcome to Togo Courier Services"
        val firebase_id: String = FirebaseInstanceId.getInstance().id
        val firebaseToken: String = FirebaseInstanceId.getInstance().token!!

        if (msg == "" && image_FirebaseURL != null) {
            msg = image_FirebaseURL.toString()
        } else if (msg == "") {
            return
        }


        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(this.otherUId!!).child(this.myUId!!).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.getValue(Chat::class.java) != null) {
                    val chat = p0.getValue(Chat::class.java)
                    getMessageCount = chat!!.messageCount + 1

                    chat_send_msg_data(msg, firebase_id, firebaseToken, pushKey)
                } else {
                    chat_send_msg_data(msg, firebase_id, firebaseToken, pushKey)
                }
            }

        })
    }


    private fun chat_send_msg_data(msg: String, firebase_id: String, firebaseToken: String, pushKey: String?) {

        val otherChat = Chat()
        otherChat.message = msg
        if (otherName != null) {
            otherChat.name = otherName as String
        } else {
            otherChat.name = "admin"
        }

        otherChat.deleteby = ""
        otherChat.title = title.toString()
        otherChat.firebaseId = firebase_id
        otherChat.timestamp = ServerValue.TIMESTAMP
        otherChat.uid = "0"
        otherChat.firebaseToken = firebaseToken
        otherChat.key = pushKey.toString()
        otherChat.messageCount = getMessageCount
        otherChat.profilePic = this.otherProfileImage!!


        val myChat = Chat()
        myChat.message = msg
        myChat.name = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
        myChat.deleteby = ""
        myChat.title = title as String
        myChat.firebaseId = firebase_id
        myChat.timestamp = ServerValue.TIMESTAMP
        myChat.uid =PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
        myChat.firebaseToken = firebaseToken
        myChat.key = pushKey.toString()
        myChat.messageCount = 0
        myChat.profilePic = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")

        DatabaseReference.reference.child(Constant.ARG_CHAT_ROOMS).child(this.chatNode!!).child(pushKey!!).setValue(otherChat)
        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUId!!).child("0").setValue(otherChat)
        DatabaseReference.reference.child(Constant.ARG_HISTORY).child("0").child(myUId!!).setValue(myChat)
        // progressBar.visibility = View.GONE
        progress!!.dismiss()

        //  message.setText("")
        image_FirebaseURL = null

        val myName = PreferenceConnector.readString(this@SignInActivity, PreferenceConnector.USERFULLNAME, "")


    }


/*
    private fun gettingDataFromUserTable() {
        firebaseDatabase!!.getReference().child(Constant.ARG_USERS).child("0").addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                userInfoFCM = dataSnapshot.getValue(UserInfoFCM::class.java)

                assert(userInfoFCM != null)
                otherName = userInfoFCM!!.name

                if (!userInfoFCM!!.profilePic.equals("")) {
                    otherProfileImage = userInfoFCM!!.profilePic
                } else {
                    otherProfileImage = ""
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }
*/


    private fun gettingDataFromUserTable() {
        firebaseDatabase!!.reference.child(Constant.ARG_USERS).child("0").addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.getValue(UserInfoFCM::class.java) != null) {
                    userInfoFCM = p0.getValue(UserInfoFCM::class.java)!!
                    assert(userInfoFCM != null)
                    otherName = userInfoFCM!!.name

                    if (!userInfoFCM!!.profilePic.equals("")) {
                        otherProfileImage = userInfoFCM!!.profilePic
                    } else {
                        otherProfileImage = ""
                    }
                    // title_header.text = userInfoFcm.name
                }

            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })
    }

    private fun selectImage() {
        val items: Array<CharSequence> = arrayOf(getString(R.string.text_take_photo), getString(R.string.text_chose_gellery), getString(R.string.text_cancel))
        val alert: AlertDialog.Builder = AlertDialog.Builder(this)
        alert.setTitle(getString(R.string.text_add_photo))
        alert.setItems(items, { dialogInterface, i ->
            if (items[i] == getString(R.string.text_take_photo)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                    } else {
                        intentToCaptureImage()
                    }
                } else {
                    intentToCaptureImage()
                }
            } else if (items[i] == getString(R.string.text_chose_gellery)) {
                if (Build.VERSION.SDK_INT >= 23) {
                    if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
                    } else {
                        val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(intent, Constant.SELECT_FILE)
                    }
                } else {
                    val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.SELECT_FILE)
                }
            } else if (items[i] == getString(R.string.text_cancel)) {
                dialogInterface.dismiss()
            }
        })
        alert.show()
    }


    private fun intentToCaptureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, Constant.REQUEST_CAMERA)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.SELECT_FILE)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }
            Constant.MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(intent, Constant.REQUEST_CAMERA)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }
            Constant.MY_PERMISSIONS_REQUEST_FILE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun uploadIdCardApi() {
        if (Constant.isNetworkAvailable(this@SignInActivity, mainLayout)) {
            //  progressBar.visibility = View.VISIBLE
            progress!!.show()

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.UploadIdResponse, Response.Listener { response ->
                val resultResponse = String(response.data)
                //  progressBar.visibility = View.GONE
                progress!!.dismiss()

                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    if (status == "success") {
                        alertDialog?.dismiss()
                        val registrationResponce = gson.fromJson(resultResponse, UploadIdResponse::class.java)
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERTYPE, registrationResponce.data?.userType.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERID, registrationResponce.data?.id.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USEREMAIL, registrationResponce.data?.email.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERSOCIALID, registrationResponce.data?.socialId.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERSOCIALTYPE, registrationResponce.data?.socialType.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERPROFILEIMAGE,registrationResponce.data?.profileImage.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERFULLNAME, registrationResponce.data?.fullName.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERCOUNTRYCODE, registrationResponce.data?.countryCode.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERCONTACTNO,registrationResponce.data?.contactNo.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERADDRESS, registrationResponce.data?.address.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERLATITUTE,registrationResponce.data?.latitude.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.USERLONGITUTE, registrationResponce.data?.longitude.toString())
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.ISLOGIN, "yes")
                        PreferenceConnector.writeString(this@SignInActivity, PreferenceConnector.ISNOTIFICATIONON, registrationResponce.data?.notificationStatus.toString())
                       // PreferenceConnector.writeInteger(this@SignInActivity, PreferenceConnector.RATTING, registrationResponce.data?.rating!!.toInt())
                        fullName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
                        uID = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
                        profileImg = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "")
                        email = PreferenceConnector.readString(this, PreferenceConnector.USEREMAIL, "")
                        addUserFirebaseDatabase()
                        gettingDataFromUserTable()
                        firebaseChatRegister()

                        showWelcome()


                    } else {

                        if (userType == Constant.COURIOR) {
                            if (message == "Currently you are inactivate user") {
                                val helper = HelperClass(this, this)
                                helper.inActiveByAdmin("Registration done, but you cannot access the app before admin give you login access", false)
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
                //  progressBar.visibility = View.GONE
                progress!!.dismiss()

                if (networkResponse != null) {
                    if (networkResponse.statusCode == 300) {
                        val helper = HelperClass(this, this)
                        helper.sessionExpairDialog()
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
                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@SignInActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (cardImageBitmap != null) {
                            params.put("uploadIdCard", DataPart("idCard.jpg", AppHelper.getFileDataFromDrawable(cardImageBitmap!!), "image/jpg"))
                        }
                        return params
                    }

                override fun getParams(): MutableMap<String, String> {
                    val userId=PreferenceConnector.readString(this@SignInActivity,PreferenceConnector.USERID,"")

                    val params=HashMap<String,String>()
                    params.put("userId",userId)

                    return params

                }


            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(multipartRequest)

        }else{
           Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }










    fun showWelcome(){
        openDialog = Dialog(this)
        openDialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog!!.setCancelable(false)
        openDialog!!.setContentView(R.layout.dialog_show_welcome)
        openDialog!!.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val lWindowParams = WindowManager.LayoutParams()
        lWindowParams.copyFrom(openDialog!!.window!!.attributes)
        lWindowParams.width = WindowManager.LayoutParams.MATCH_PARENT
        lWindowParams.height = WindowManager.LayoutParams.WRAP_CONTENT
        openDialog!!.window!!.attributes = lWindowParams
        openDialog!!.letsGoBtn.setOnClickListener {
            progress!!.show()
            if (userType=="2"){
                val helper = HelperClass(this, this)
                helper.inActiveByAdmin("Registration done, but you cannot access the app before admin give you login access", false)
                openDialog!!.dismiss()

            }else{
                val intent = Intent(this, HomeActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()

            }


        }

        //inputFilter(openDialog!!.popPriceTxt)


        openDialog!!.show()
    }



}
