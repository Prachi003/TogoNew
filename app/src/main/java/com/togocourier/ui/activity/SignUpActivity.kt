package com.togocourier.ui.activity

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.gson.Gson
import com.togocourier.R
import com.togocourier.image.picker.ImagePicker
import com.togocourier.responceBean.Chat
import com.togocourier.responceBean.RegistrationResponse
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.*
import com.togocourier.view.cropper.CropImage
import com.togocourier.view.cropper.CropImageView
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.dialog_show_welcome.*
import kotlinx.android.synthetic.main.new_activity_sign_up.*
import kotlinx.android.synthetic.main.new_dialog_apply_bid.*
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS", "RECEIVER_NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class SignUpActivity : AppCompatActivity(), View.OnClickListener {
    private var userType = ""
    private var autocompleteFragment: PlaceAutocompleteFragment? = null
    private var profileImageBitmap: Bitmap? = null
    private var cardImageBitmap: Bitmap? = null
    private var address = ""
    private var myUId: String? = ""
    private var getMessageCount = 0
    private var openDialog: Dialog? = null
    private var userInfoFCM: UserInfoFCM? = null

    private var otherUId: String? = ""
    private var otherProfileImage: String? = ""

    var otherName: String? = ""

    private var image_FirebaseURL: Uri? = null

    private var DatabaseReference = FirebaseDatabase.getInstance()

    private var lat = ""
    private var lng = ""
    private var chatNode: String? = null
    private var gson = Gson()
    private var firebaseDatabase: FirebaseDatabase? = null

    private var email: String = ""
    private var fullName: String = ""
    private var profileImg: String = ""
    private var uID: String = ""
    private var progress: ProgressDialog? = null

    // variable to track event time
    private var mLastClickTime: Long = 0

    private var cardImg: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_sign_up)
        firebaseDatabase = FirebaseDatabase.getInstance()
        progress = ProgressDialog(this)

        Constant.hideSoftKeyboard(this)

        profileImage.setOnClickListener(this)
        idCardLay.setOnClickListener(this)
        signUpBtn.setOnClickListener(this)
        signInLay.setOnClickListener(this)
        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
        if (userType.equals("1")) idCardLay.visibility = View.GONE
        autocompleteFragment = fragmentManager.findFragmentById(R.id.place_autocomplete_fragment) as PlaceAutocompleteFragment
        autocompleteFragment!!.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {
                addressTxt.text = place.address
                address = place.address.toString()
                lat = place.latLng.latitude.toString()
                lng = place.latLng.longitude.toString()
            }

            override fun onError(status: Status) {

            }
        })

    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.profileImage -> {
                //   selectImage()

                if (Build.VERSION.SDK_INT >= 23) {
                    if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                    } else {
                        ImagePicker.pickImage(this@SignUpActivity)
                    }
                } else {
                    ImagePicker.pickImage(this@SignUpActivity)
                }
            }

            R.id.idCardLay -> {
                // selectFileCard()

                if (Build.VERSION.SDK_INT >= 23) {
                    if (this.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        requestPermissions(
                                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                                Constant.MY_PERMISSIONS_REQUEST_CAMERA)
                        cardImg = true
                    } else {
                        cardImg = true
                        ImagePicker.pickImage(this@SignUpActivity)
                    }
                } else {
                    cardImg = true
                    ImagePicker.pickImage(this@SignUpActivity)
                }
            }

            R.id.signUpBtn -> {
                if (userType == "1") {
                    if (isValidCustomerData()) doRegistration()
                } else {
                    if (isValidCourierData()) doRegistration()
                }
            }
            R.id.signInLay -> {
                finish()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            Constant.MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this@SignUpActivity)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }

            Constant.MY_PERMISSIONS_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    ImagePicker.pickImage(this@SignUpActivity)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == 234) {    // Image Picker
                val imageUri = ImagePicker.getImageURIFromResult(this, requestCode, resultCode, data)

                if (userType == Constant.COURIOR && cardImg) {
                    try {
                        cardImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                        cardImg = false
                        idTxt.text = "Id Card is Uploaded"
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Constant.snackbar(mainLayout, resources.getString(R.string.alertImageException))
                    } catch (error: OutOfMemoryError) {
                        Constant.snackbar(mainLayout, resources.getString(R.string.alertOutOfMemory))
                    }

                } else {
                    if (imageUri != null) {
                        // Calling Image Cropper
                        CropImage.activity(imageUri).setCropShape(CropImageView.CropShape.RECTANGLE)
                                .setAspectRatio(4, 3)
                                .start(this)
                    } else {
                        Constant.snackbar(mainLayout, resources.getString(R.string.something_went_wrong))
                    }
                }
            } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {   // Image Cropper
                val result = CropImage.getActivityResult(data)
                try {
                    if (result != null) {
                        profileImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, result.uri)
                        profileImage.setImageBitmap(profileImageBitmap)
                    }

                } catch (e: Exception) {
                    e.printStackTrace()
                    Constant.snackbar(mainLayout, resources.getString(R.string.alertImageException))
                } catch (error: OutOfMemoryError) {
                    Constant.snackbar(mainLayout, resources.getString(R.string.alertOutOfMemory))
                }

            }
        }


    }

    private fun isValidCustomerData(): Boolean {
        val v = Validation()

        if (v.isEmpty(userTxt)) {
            Constant.snackbar(mainLayout, "Full Name can't be empty")
            userTxt.requestFocus()
            return false
        } else if (v.isValidName(userTxt)) {
            Constant.snackbar(mainLayout, "Name allow alphabets and space only")
            userTxt.requestFocus()
            return false
        } else if (v.isEmpty(emailTxt)) {
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
        } else if (v.isEmpty(contactTxt)) {
            Constant.snackbar(mainLayout, "Contact no can't be empty")
            contactTxt.requestFocus()
            return false
        } else if (!isContactNoValid(contactTxt)) {
            Constant.snackbar(mainLayout, "Enter valid contact no")
            contactTxt.requestFocus()
            return false
        } else if (v.isEmpty(addressTxt)) {
            Constant.snackbar(mainLayout, "Please enter your address")
            return false
        }
        return true
    }

    private fun isValidCourierData(): Boolean {
        val v = Validation()
        if (v.isEmpty(userTxt)) {
            Constant.snackbar(mainLayout, "Full Name can't be empty")
            userTxt.requestFocus()
            return false
        } else if (v.isValidName(userTxt)) {
            Constant.snackbar(mainLayout, "Name allow alphabets and space only")
            userTxt.requestFocus()
            return false
        } else if (v.isEmpty(emailTxt)) {
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
        } else if (v.isEmpty(contactTxt)) {
            Constant.snackbar(mainLayout, "Contact no can't be empty")
            contactTxt.requestFocus()
            return false
        } else if (!isContactNoValid(contactTxt)) {
            Constant.snackbar(mainLayout, "Enter valid contact no")
            contactTxt.requestFocus()
            return false
        } else if (v.isEmpty(addressTxt)) {
            Constant.snackbar(mainLayout, "Please enter your address")
            contactTxt.requestFocus()
            return false
        } else if (cardImageBitmap == null) {
            Constant.snackbar(mainLayout, "Please Add your Id Card")
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

    private fun isContactNoValid(editText: EditText): Boolean {
        val getValue = editText.text.toString().trim { it <= ' ' }
        return getValue.length in 7..11
    }

    private fun doRegistration() {
        signUpBtn.isEnabled = false
        if (Constant.isNetworkAvailable(this@SignUpActivity, mainLayout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST, Constant.BASE_URL + Constant.Registration_Url, Response.Listener { response ->
                val resultResponse = String(response.data)
                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")

                    if (status == "success") {
                        val registrationResponce = gson.fromJson(resultResponse, RegistrationResponse::class.java)
                        setSession(registrationResponce)
                        PreferenceConnector.writeString(this@SignUpActivity, PreferenceConnector.PASSWORD, pwdTxt.text.toString())
                        PreferenceConnector.writeString(this@SignUpActivity, PreferenceConnector.POSTID, "")
                        fullName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "").toString()
                        uID = PreferenceConnector.readString(this, PreferenceConnector.USERID, "").toString()
                        userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "").toString()
                        profileImg = PreferenceConnector.readString(this, PreferenceConnector.USERPROFILEIMAGE, "").toString()
                        email = PreferenceConnector.readString(this, PreferenceConnector.USEREMAIL, "")
                        firebaseDatabase!!.reference.child(Constant.ARG_USERS).child("0").addListenerForSingleValueEvent(object :ValueEventListener{
                            override fun onDataChange(p0: DataSnapshot) {
                                if (p0.getValue(UserInfoFCM::class.java) != null) {
                                    userInfoFCM = p0.getValue(UserInfoFCM::class.java)!!
                                    if (!userInfoFCM!!.name.equals(fullName)){
                                        showWelcome()

                                    }
                                    // title_header.text = userInfoFcm.name
                                }

                            }

                            override fun onCancelled(p0: DatabaseError) {

                            }

                        })

                    } else {
                        progress!!.dismiss()
                        Constant.snackbar(mainLayout, message)
                    }
                    signUpBtn.isEnabled = true
                } catch (e: JSONException) {
                    e.printStackTrace()
                    signUpBtn.isEnabled = true
                }
            }, Response.ErrorListener { error ->
                signUpBtn.isEnabled = true
                val networkResponse = error.networkResponse
                // progressBar.visibility = View.GONE
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
                    params.put("userType", PreferenceConnector.readString(this@SignUpActivity, PreferenceConnector.USERTYPE, ""))
                    params.put("deviceType", "2")
                    params.put("deviceToken", FirebaseInstanceId.getInstance().token.toString())
                    params.put("socialId", "")
                    params.put("socialType", "")
                    params.put("email", emailTxt.text.toString())
                    params.put("fullName", userTxt.text.toString())
                    params.put("countryCode", "+1")
                    params.put("contactNo", contactTxt.text.toString())
                    params.put("address", address)
                    params.put("latitude", lat)
                    params.put("longitude", lng)
                    params.put("password", pwdTxt.text.toString())
                    if (profileImageBitmap == null) {
                        params.put("profileImage", "")
                    }
                    if (cardImageBitmap == null) {
                        params.put("uploadIdCard", "")
                    }
                    return params
                }

                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (profileImageBitmap != null) {
                            params.put("profileImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(profileImageBitmap!!), "image/jpg"))
                        }
                        if (cardImageBitmap != null) {
                            params.put("uploadIdCard", DataPart("idCard.jpg", AppHelper.getFileDataFromDrawable(cardImageBitmap!!), "image/jpg"))

                        }
                        return params
                    }
            }
            multipartRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(multipartRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun setSession(registrationResponce: RegistrationResponse) {
        PreferenceConnector.clear(this)
        PreferenceConnector.writeString(this, PreferenceConnector.USERTYPE, registrationResponce.userData?.userType.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.ADDBANK, registrationResponce.userData?.addBank.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERID, registrationResponce.userData?.id.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USEREMAIL, registrationResponce.userData?.email.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERSOCIALID, registrationResponce.userData?.socialId.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERSOCIALTYPE, registrationResponce.userData?.socialType.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERPROFILEIMAGE, registrationResponce.userData?.profileImage.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERFULLNAME, registrationResponce.userData?.fullName.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERCOUNTRYCODE, registrationResponce.userData?.countryCode.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERCONTACTNO, registrationResponce.userData?.contactNo.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERADDRESS, registrationResponce.userData?.address.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERLATITUTE, registrationResponce.userData?.latitude.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERLONGITUTE, registrationResponce.userData?.longitude.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.USERAUTHTOKEN, registrationResponce.userData?.authToken.toString())
        PreferenceConnector.writeString(this, PreferenceConnector.ISLOGIN, "yes")
        PreferenceConnector.writeInteger(this, PreferenceConnector.RATTING, registrationResponce.userData?.rating!!)

        val session = SessionManager(this)
        session.createSessionAuthToken(registrationResponce.userData?.authToken.toString())
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
                .addOnCompleteListener(this@SignUpActivity) { task ->
                    if (task.isSuccessful) {
                        addUserFirebaseDatabase(email, fullName, profileImg, userType, uID)
                    } else {

                        // Write data to user table in firebase
                        addUserFirebaseDatabase()
                    }
                }
    }

    private fun addUserFirebaseDatabase(email: String, name: String, image: String, userType: String, uID: String) {
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

        val myName = PreferenceConnector.readString(this@SignUpActivity, PreferenceConnector.USERFULLNAME, "")


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
            openDialog!!.dismiss()
            progress!!.show()
            addUserFirebaseDatabase()
            gettingDataFromUserTable()
            firebaseChatRegister()


        }

        //inputFilter(openDialog!!.popPriceTxt)


        openDialog!!.show()
    }


}

