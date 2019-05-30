package com.togocourier.ui.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Point
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.support.v7.app.AppCompatActivity
import android.transition.Slide
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.togocourier.R
import com.togocourier.adapter.ChattingAdapter
import com.togocourier.fcm_services.FcmNotificationBuilder
import com.togocourier.image.picker.ImagePicker
import com.togocourier.responceBean.Chat
import com.togocourier.responceBean.UserInfoFCM
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import kotlinx.android.synthetic.main.layout_popup_menu.view.*
import kotlinx.android.synthetic.main.new_activity_chat.*
import java.io.ByteArrayOutputStream
import java.util.*

@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ChatActivity : AppCompatActivity() {

    private var otherUID = ""
    private var title = ""
    private var myUid: String = ""
    private var blockedId: String = ""
    private var chatNode = ""
    private var galleryBitMap: Bitmap? = null
    private var userInfoFcm = UserInfoFCM()
    private var chatAdapter: ChattingAdapter? = null
    private var chatList = ArrayList<Chat>()
    private var DatabaseReference = FirebaseDatabase.getInstance()
    private var image_FirebaseURL: Uri? = null
    private var map = HashMap<String, Chat>()
    private var getMessageCount = 0

    private var popupWindow: PopupWindow? = null
    private var isMenuOpen: Boolean = false

    private var tv_user_block: TextView? = null
    private var progress: ProgressDialog? = null

    private var isDeletedByOpp: Boolean = false
    private var isClick: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_chat)

        progress = ProgressDialog(this)
        progress!!.show()

        myUid = PreferenceConnector.readString(this, PreferenceConnector.USERID, "")
        if (intent != null) {
            otherUID = intent.getStringExtra("otherUID")
            title = intent.getStringExtra("title")
            Constant.ChatOpponentId = otherUID
        }

        chatNode = if (myUid.toInt() < otherUID.toInt()) {
            myUid + "_" + otherUID
        } else {
            otherUID + "_" + myUid
        }

        //  emojiPopup = EmojiPopup.Builder.fromRootView(view_emoji).build(message)

        gettingDataFromUserTable(otherUID)

        if (otherUID.equals("0")){
            iv_menu.visibility=View.GONE
        }else{
            iv_menu.visibility=View.VISIBLE
        }

        // DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).child("messageCount").setValue(0)
        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(otherUID)) {
                    DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).child("messageCount").setValue(0)
                }
            }
        })

        getBlockData()
        getChat()
        getChatDelete()

        iv_menu.setOnClickListener {
            val location = IntArray(2)

            iv_menu.getLocationOnScreen(location)

            //Initialize the Point with x, and y positions
            val display = windowManager.defaultDisplay
            val p = Point()
            display.getSize(p)
            p.x = location[0]
            p.y = location[1]

            if (!isMenuOpen) {
                initiatePopupWindow(p)
            } else {
                isMenuOpen = false
                popupWindow!!.dismiss()
            }
        }

        send_message.setOnClickListener {
            if (!isClick) {
                isClick = true

                when (blockedId) {
                    myUid -> alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
                    otherUID -> alertMSG("You are blocked by " + userInfoFcm.name + ". Can't send any message")
                    "Both" -> alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
                    else -> send_msg()
                }
            }
            Handler().postDelayed({ isClick = false }, 3000)
        }

        /*emoji.setOnClickListener {
            emojiPopup?.toggle()
            // Toggles visibility of the Popup.
            //emojiPopup.dismiss(); // Dismisses the Popup.
            //emojiPopup.isShowing(); // Returns true when Popup is showing.

        }*/

        iv_back.setOnClickListener { onBackPressed() }

        gallery.setOnClickListener {
            when (blockedId) {
                myUid -> alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
                otherUID -> alertMSG("You are blocked by " + userInfoFcm.name + ". Can't send any message")
                "Both" -> alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
                else -> getCardFile()
            }
        }

        message.setOnTouchListener { p0, p1 ->
            // recycler_view.scrollToPosition((chatList.size - 1))

/*                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager!!.toggleSoftInputFromWindow(
                        main_chat_activity.getApplicationWindowToken(),
                        InputMethodManager.SHOW_FORCED, 0)*/

            //  emojiPopup?.dismiss()
            false
        }


        capture_image.setOnClickListener {
            when (blockedId) {
                myUid -> alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
                otherUID -> alertMSG("You are blocked by " + userInfoFcm.name + ". Can't send any message")
                "Both" -> alertMSG("You block " + userInfoFcm.name + ". Can't send any message")
                else -> captureImageFromCamera()
            }
        }

    }

    @SuppressLint("InflateParams")
    private fun initiatePopupWindow(p: Point) {
        isMenuOpen = true
        val inflater: LayoutInflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.layout_popup_menu, null)
        tv_user_block = view.findViewById(R.id.tv_user_block)
        getBlockData()

        // Initialize a new instance of popup window
        popupWindow = PopupWindow(view, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)

        // Set an elevation for the popup window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            popupWindow!!.elevation = 5F
        }

        // If API level 23 or higher then execute the code
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Create a new slide animation for popup window enter transition
            val slideIn = Slide()
            slideIn.slideEdge = Gravity.TOP
            popupWindow!!.enterTransition = slideIn

            // Slide animation for popup window exit transition
            val slideOut = Slide()
            slideOut.slideEdge = Gravity.TOP
            popupWindow!!.exitTransition = slideOut
        }

        popupWindow!!.showAtLocation(iv_menu, Gravity.TOP, p.x, iv_menu.height + (3 * (p.y) / 4))

        view.ly_block_user.setOnClickListener {
            popupWindow!!.dismiss()

            when (blockedId) {
                "" -> blockChatDialog("Are you want to block user?")
                myUid -> blockChatDialog("Are you want to unblock user?")
                otherUID -> blockChatDialog("Are you want to block user?")
                "Both" -> blockChatDialog("Are you want to unblock user?")
            }
        }

        view.tv_chat_delete.setOnClickListener {
            popupWindow!!.dismiss()
            deleteChatDialog("Are you sure you want to delete conversation?")
        }

    }
    var firebase_id=""
    var firebaseToken=""

    private fun send_msg() {
        val pushKey = DatabaseReference.reference.child(Constant.ARG_CHAT_ROOMS).child(chatNode).push().key
        var msg = message.text.toString().trim()
        if (FirebaseAuth.getInstance().currentUser?.uid!=null){
             firebase_id = FirebaseAuth.getInstance().currentUser?.uid!!
             firebaseToken  = FirebaseInstanceId.getInstance().token!!
        }else{
            firebase_id=""
            firebaseToken  = FirebaseInstanceId.getInstance().token!!
        }


        if (msg == "" && image_FirebaseURL != null) {
            msg = image_FirebaseURL.toString()
        } else if (msg == "") {
            return
        }


        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(otherUID).child(myUid).addListenerForSingleValueEvent(object : ValueEventListener {
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
        otherChat.name = userInfoFcm.name
        otherChat.deleteby = ""
        otherChat.title = title
        otherChat.firebaseId = firebase_id
        otherChat.timestamp = ServerValue.TIMESTAMP
        otherChat.uid = otherUID
        otherChat.firebaseToken = firebaseToken
        otherChat.key = pushKey.toString()
        otherChat.messageCount = getMessageCount
        otherChat.profilePic = userInfoFcm.profilePic

        val myChat = Chat()
        myChat.message = msg
        myChat.name = PreferenceConnector.readString(this@ChatActivity, PreferenceConnector.USERFULLNAME, "")
        myChat.deleteby = ""
        myChat.title = title
        myChat.firebaseId = firebase_id
        myChat.timestamp = ServerValue.TIMESTAMP
        myChat.uid = myUid
        myChat.firebaseToken = firebaseToken
        myChat.key = pushKey.toString()
        myChat.messageCount = 0
        myChat.profilePic = PreferenceConnector.readString(this@ChatActivity, PreferenceConnector.USERPROFILEIMAGE, "")

        DatabaseReference.reference.child(Constant.ARG_CHAT_ROOMS).child(chatNode).child(pushKey!!).setValue(myChat)
        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).setValue(otherChat)
        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(otherUID).child(myUid).setValue(myChat)
        // progressBar.visibility = View.GONE
        progress!!.dismiss()

        message.setText("")
        image_FirebaseURL = null

        val myName = PreferenceConnector.readString(this@ChatActivity, PreferenceConnector.USERFULLNAME, "")

        if (PreferenceConnector.readString(this@ChatActivity, PreferenceConnector.ISNOTIFICATIONON, "") == "ON") {

            if (msg.startsWith("https://firebasestorage.googleapis.com/")) {
                sendPushNotificationToReceiver(title, "Image", myName, myUid, firebaseToken)
            } else {
                sendPushNotificationToReceiver(title, msg, myName, myUid, firebaseToken)
            }


        }
    }

    private fun getChat() {
        progress!!.dismiss()



        FirebaseDatabase.getInstance().getReference().child(Constant.ARG_CHAT_ROOMS).child(chatNode).orderByKey().addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val chat = dataSnapshot.getValue(Chat::class.java)
                getChatDataInmap(chat!!,dataSnapshot.key)
            }

            override fun onChildChanged(dataSnapshot: DataSnapshot, s: String?) {
                val chat = dataSnapshot.getValue(Chat::class.java)
                getChatDataInmap(chat!!, dataSnapshot.key)
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {

            }

            override fun onChildMoved(dataSnapshot: DataSnapshot, s: String?) {

            }

            override fun onCancelled(databaseError: DatabaseError) {

            }
        })
    }

    private fun getChatDelete() {
        FirebaseDatabase.getInstance()
                .reference
                .child(Constant.ARG_CHAT_ROOMS)
                .child(chatNode).addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(p0: DataSnapshot) {
                        for (obj in p0.children) {

                            val chat = obj.getValue(Chat::class.java)
                            if (chat?.deleteby.equals(otherUID)) {
                                isDeletedByOpp = true
                            }
                            break
                        }
                    }

                    override fun onCancelled(dataSnapshot: DatabaseError) {

                    }
                })
    }

    private fun getChatDataInmap(chat: Chat, key: String?) {
        if (chat.deleteby == myUid) {
            return
        } else {
            map.put(key!!, chat)
            val demoValues: Collection<Chat> = map.values
            chatList = ArrayList(demoValues)

            chatAdapter = ChattingAdapter(this@ChatActivity, chatList)
            recycler_view.adapter = chatAdapter
            recycler_view.scrollToPosition((map.size - 1))
        }
        shortList()

    }

    private fun shortList() {
        Collections.sort(chatList) { a1, a2 ->
            if (a1!!.timestamp == null || a2!!.timestamp == null)
                -1
            else {
                val long1: Long = a1.timestamp as Long
                val long2: Long = a2.timestamp as Long
                long1.compareTo(long2)
            }
        }
        chatAdapter?.notifyDataSetChanged()
        progress!!.dismiss()
    }

    private fun gettingDataFromUserTable(firebaseUid: String) {
        DatabaseReference.reference.child(Constant.ARG_USERS).child(firebaseUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {}

            @SuppressLint("SetTextI18n")
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.getValue(UserInfoFCM::class.java) != null) {
                    userInfoFcm = p0.getValue(UserInfoFCM::class.java)!!
                    PreferenceConnector.readString(this@ChatActivity, PreferenceConnector.USERPROFILEIMAGE, "")

                    // title_header.text = userInfoFcm.name
                    title_header.text = userInfoFcm.name.substring(0, 1).toUpperCase() + userInfoFcm.name.substring(1)
                    chatAdapter = ChattingAdapter(this@ChatActivity, chatList)
                    recycler_view.adapter = chatAdapter
                }
                recycler_view.scrollToPosition((map.size - 1))

            }
        })
    }


    private fun captureImageFromCamera() {
        message.setText("")
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.CAMERA, android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_CAMERA)
            } else {
                intentToCaptureImage()
            }
        } else {
            intentToCaptureImage()
        }
    }

    private fun intentToCaptureImage() {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePictureIntent, Constant.REQUEST_CAMERA)
    }

    private fun getCardFile() {
        message.setText("")
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), Constant.MY_PERMISSIONS_REQUEST_FILE)
            } else {
                val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
            }
        } else {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {

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
                    val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(intent, Constant.REQUEST_FILE_GALLERY)
                } else {
                    Toast.makeText(this, "Your permission denied", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constant.REQUEST_CAMERA) {
            if (data != null) {
                if (data.extras != null) {
                    galleryBitMap = data.extras!!.get("data") as Bitmap
                    val bytes = ByteArrayOutputStream()
                    galleryBitMap!!.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
                    //profileImage.setImageBitmap(profileImageBitmap)
                    val uri = getImageUri(this, galleryBitMap!!)
                    creatFirebaseProfilePicUrl(uri)
                    // progressBar.visibility = View.VISIBLE
                    progress!!.show()
                }
            }
        } else if (requestCode == Constant.REQUEST_FILE_GALLERY) {
            if (data != null) {
                val imageUri = ImagePicker.getImageURIFromResult(this@ChatActivity, 234, Activity.RESULT_OK, data)
                //  progressBar.visibility = View.VISIBLE
                progress!!.show()
                creatFirebaseProfilePicUrl(imageUri)
            }
        }
    }

    private fun getImageUri(inContext: Context, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 0, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.contentResolver, inImage, "Title", null)
        return Uri.parse(path)
    }

    private fun creatFirebaseProfilePicUrl(selectedImageUri: Uri) {
        val storageRef: StorageReference
        val storage: FirebaseStorage
        val app: FirebaseApp = FirebaseApp.getInstance()!!

        storage = FirebaseStorage.getInstance(app)

        storageRef = storage.getReference("chat_photos_togo" + getString(R.string.app_name))
        val photoRef = storageRef.child(selectedImageUri.lastPathSegment)

        photoRef.putFile(selectedImageUri).addOnSuccessListener { taskSnapshot ->

            taskSnapshot.storage.downloadUrl.addOnSuccessListener({ uri ->
                image_FirebaseURL = uri
                send_message.callOnClick()
            })
        }.addOnFailureListener { e ->  }

    }

    private fun sendPushNotificationToReceiver(title: String, message: String, username: String, uid: String, firebaseToken: String) {
        FcmNotificationBuilder.initialize()
                .title(title)
                .message(message)
                .username(username)
                .uid(uid)
                .firebaseToken(firebaseToken)
                .receiverFirebaseToken(userInfoFcm.firebaseToken).send()
    }

    private fun alertMSG(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setCancelable(false)
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Ok", { dialog, which ->
            alertDialog.setCancelable(true)
        })

        alertDialog.show()
    }

    private fun deleteChatDialog(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Yes") { dialog, which ->

            for (allChat in chatList) {
                if (isDeletedByOpp) {
                    DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).setValue(null)
                    DatabaseReference.reference.child(Constant.ARG_CHAT_ROOMS).child(chatNode).setValue(null)

                    isDeletedByOpp = false

                    break
                } else if (allChat.deleteby == otherUID) {
                    //should delete all chat here
                    DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).setValue(null)
                    DatabaseReference.reference.child(Constant.ARG_CHAT_ROOMS).child(chatNode).child(allChat.key).setValue(null)

                } else if (allChat.deleteby == "") {
                    allChat.deleteby = myUid
                    DatabaseReference.reference.child(Constant.ARG_CHAT_ROOMS).child(chatNode).child(allChat.key).setValue(allChat)
                    DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).setValue(null)
                }
            }

            map.clear()
            chatList.clear()
            chatAdapter?.notifyDataSetChanged()
            alertDialog.setCancelable(true)
        }

        alertDialog.setNegativeButton("No", { dialog, which ->
            alertDialog.setCancelable(true)
        })
        alertDialog.show()
    }

    private fun getBlockData() {
        DatabaseReference.reference.child(Constant.BlockUsers).child(chatNode).child(Constant.blockedBy).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.getValue(String()::class.java) != null) {
                    blockedId = p0.getValue(String()::class.java)!!

                    when (blockedId) {
                        "Both" -> tv_user_block?.text = getString(R.string.unblocj)
                        "" -> tv_user_block?.text =getString(R.string.block_user)
                        otherUID -> tv_user_block?.text = getString(R.string.block_user)
                        myUid -> tv_user_block?.text = getString(R.string.unblocj)
                    }

                }
            }
        })
    }

    private fun blockChatDialog(msg: String) {
        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setTitle("Alert")
        alertDialog.setMessage(msg)
        alertDialog.setPositiveButton("Yes") { dialog, which ->

            when (blockedId) {
                "Both" -> {
                    DatabaseReference.reference.child(Constant.BlockUsers).child(chatNode).child(Constant.blockedBy).setValue(otherUID)
                    tv_user_block?.text = getString(R.string.unblocj)
                }
                "" -> {
                    DatabaseReference.reference.child(Constant.BlockUsers).child(chatNode).child(Constant.blockedBy).setValue(myUid)
                    tv_user_block?.text = getString(R.string.block_user)
                }
                otherUID -> {
                    DatabaseReference.reference.child(Constant.BlockUsers).child(chatNode).child(Constant.blockedBy).setValue("Both")
                    tv_user_block?.text = getString(R.string.block_user)
                }
                myUid -> {
                    DatabaseReference.reference.child(Constant.BlockUsers).child(chatNode).child(Constant.blockedBy).setValue("")
                    tv_user_block?.text =  getString(R.string.unblocj)
                }
            }

            // getBlockData()

            alertDialog.setCancelable(true)
        }

        alertDialog.setNegativeButton("No", { dialog, which ->
            alertDialog.setCancelable(true)
        })
        alertDialog.show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(otherUID)) {
                    DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).child("messageCount").setValue(0)
                }
            }
        })
        Constant.ChatOpponentId = ""
    }

    override fun onDestroy() {
        super.onDestroy()
        DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.hasChild(otherUID)) {
                    DatabaseReference.reference.child(Constant.ARG_HISTORY).child(myUid).child(otherUID).child("messageCount").setValue(0)
                }
            }
        })
        Constant.ChatOpponentId = ""
    }
}