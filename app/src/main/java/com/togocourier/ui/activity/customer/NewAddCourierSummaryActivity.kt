package com.togocourier.ui.activity.customer

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.provider.MediaStore
import android.support.design.widget.Snackbar
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.InputFilter
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.EditText
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.togocourier.R
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.ui.activity.customer.model.AddCourierSummaryModel
import com.togocourier.ui.fragment.customer.model.GetPostId
import com.togocourier.util.*
import com.togocourier.vollyemultipart.AppHelper
import com.togocourier.vollyemultipart.VolleyMultipartRequest
import com.togocourier.vollyemultipart.VolleySingleton
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog
import kotlinx.android.synthetic.main.new_activity_add_courier_item_summary.*
import kotlinx.android.synthetic.main.new_add_courier_delivery_details.*
import kotlinx.android.synthetic.main.new_add_courier_tip_summary_details.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class NewAddCourierSummaryActivity : AppCompatActivity(), View.OnClickListener, OnMapReadyCallback {
    private var courierItemUri = ""
    private var pickupTime = ""
    private var postTitle = ""
    private var pickUpAddress = ""
    private var deliveryAddress = ""
    private var addCourierSummaryModel:AddCourierSummaryModel?=null

    private var mMap: GoogleMap? = null
    var minute_collective = 0
    var minute_delivery = 0

    var year_live = 0
    var month_live = 0
    private var delTime = ""

    var day_live = 0

    var sethourOfDay = 0
    var setminute = 0
    var setsecond = 0

    private var colTime = ""



    private var fromDate: DatePickerDialog? = null
    private var myTime: TimePickerDialog? = null
    private var now: Calendar? = null

    private var hourOfDay_collective = 0
    private var hourOfDay_delivery = 0

    private var clickedId = 0
    private var clickedPickUpId = 0
    private var clickedSignature: Boolean = false
    private var clickedOwnTip: Boolean = false

    private var pickUpDate: String = ""
    private var deliveryDate: String = ""
    private var pickUpHour: String = ""
    private var pickUpMin: String = ""
    private var pickUpFormat: String = ""
    private var deliveryCity: String = ""
    private var pickupCity: String = ""

    private var pickUpLat: String = ""
    private var postId: String = ""
    private var pickUpLng: String = ""
    private var deliveryLat: String = ""
    private var deliveryLng: String = ""

    private var collectiveTime: String = ""
    private var deliveryTime: String = ""

    private var signatureStatus: Int = 0

    private var addPicBitmap: Bitmap? = null

    //private var arrayList:ArrayList<AddCourierSummaryModel>?=null

    // variable to track event time
    private var mLastClickTime: Long = 0

    private var count = -1
    private var receiverCustomerName: String = ""
    private var senderCustomerName: String = ""
    private var progress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_add_courier_item_summary)
        progress = ProgressDialog(this@NewAddCourierSummaryActivity)
        Constant.hideSoftKeyboard(this@NewAddCourierSummaryActivity)
        initializeView()
    }

    private fun initializeView() {
        now = Calendar.getInstance()
        addCourierSummaryModel=AddCourierSummaryModel()

        // Setting Me pickup option active
        clickedPickUpId = R.id.ly_pickup_check_me
        iv_pickup_check_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_active_check_box_ico))
        tv_pickup_me.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))

        iv_pickup_someone_else.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
        tv_pickup_someone_else.setTextColor(ContextCompat.getColor(this, R.color.new_gray_color))
        ly_pickup_someone_details.visibility = View.GONE

        // Setting Me receiving delivery option active
        clickedId = R.id.ly_check_me
        iv_check_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_active_check_box_ico))
        tv_me.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))

        iv_check_someone_else.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
        tv_someone_else.setTextColor(ContextCompat.getColor(this, R.color.new_gray_color))
        ly_someone_details.visibility = View.GONE

        // Setting Signature option inactive
        clickedSignature = false
        signatureStatus = 0
        iv_check_signature.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
        tv_signature.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))

        iv_back.setOnClickListener(this)

        ly_pickup_someone_else.setOnClickListener(this)
        ly_pickup_check_me.setOnClickListener(this)
        ly_check_someone_else.setOnClickListener(this)
        ly_check_me.setOnClickListener(this)
        rl_signature.setOnClickListener(this)
        ly_pickup_date_time.setOnClickListener(this)
        ly_delivery_date_time.setOnClickListener(this)
        pickup_delivery_detail_btn.setOnClickListener(this)
        collectTimeLay.setOnClickListener(this)
        collectTimeLaypickup.setOnClickListener(this)


        if (intent != null) {
            courierItemUri = intent.getStringExtra("courierItemUri")
            postTitle = intent.getStringExtra("postTitle")
            pickUpAddress = intent.getStringExtra("pickUpAddress")
            pickupCity=intent.getStringExtra("pickupCity")
            deliveryCity=intent.getStringExtra("deliveryCity")
            deliveryAddress = intent.getStringExtra("deliveryAddress")
            pickUpLat = intent.getStringExtra("pickUpLat")
            pickUpLng = intent.getStringExtra("pickUpLng")
            deliveryLat = intent.getStringExtra("deliveryLat")
            deliveryLng = intent.getStringExtra("deliveryLng")
            try {
                if (courierItemUri!=""){
                    addPicBitmap= MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(courierItemUri))

                }

            }catch (e:OutOfMemoryError){
                Toast.makeText(this, "This device doesn't have enough space , free up storage space", Toast.LENGTH_SHORT).show()

                e.printStackTrace()
            }

            //  arrayList=intent.getParcelableArrayListExtra("param1")

         /*   addPicBitmap = if (courierItemUri == "") {
                null
            } else {
                val bitmap = try {
                    MediaStore.Images.Media.getBitmap(this.contentResolver, Uri.parse(courierItemUri))
                } catch (e: OutOfMemoryError) {
                    Toast.makeText(this, "This device doesn't have enough space , free up storage space", Toast.LENGTH_SHORT).show()
                } as Bitmap?
                bitmap
            }*/
        }

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        et_item.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length > 50) {
                    et_item.setText(p0.substring(0, 50))
                    et_item.setSelection(50) // End point Cursor.
                    Toast.makeText(this@NewAddCourierSummaryActivity, "Title should not more than 50 characters", Toast.LENGTH_SHORT).show()
                    et_item.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(51))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                et_item.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(51))
            }
        })

        et_description.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {
                if (p0!!.length > 200) {
                    et_description.setText(p0.substring(0, 200))
                    et_description.setSelection(200) // End point Cursor.
                    Toast.makeText(this@NewAddCourierSummaryActivity, "Description should not more than 200 characters", Toast.LENGTH_SHORT).show()
                    et_description.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(201))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                et_description.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(201))
            }
        })

        inputFilter(et_amount)
    }


    override fun onClick(view: View?) {

        val validation = Validation()

        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view!!.id) {
            R.id.iv_back -> {
                onBackPressed()
                if (sv_item_summary != null) {
                    sv_item_summary.fullScroll(ScrollView.FOCUS_UP)
                }
            }



            R.id.ly_pickup_someone_else -> {
                if (clickedPickUpId != R.id.ly_pickup_someone_else) {
                    clickedPickUpId = R.id.ly_pickup_someone_else
                    iv_pickup_someone_else.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_active_check_box_ico))
                    tv_pickup_someone_else.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))

                    iv_pickup_check_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
                    tv_pickup_me.setTextColor(ContextCompat.getColor(this, R.color.new_gray_color))

                    ly_pickup_someone_details.visibility = View.VISIBLE
                }
            }

            R.id.ly_pickup_check_me -> {
                if (clickedPickUpId != R.id.ly_pickup_check_me) {
                    clickedPickUpId = R.id.ly_pickup_check_me
                    iv_pickup_someone_else.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
                    tv_pickup_someone_else.setTextColor(ContextCompat.getColor(this, R.color.new_gray_color))

                    iv_pickup_check_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_active_check_box_ico))
                    tv_pickup_me.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))

                    et_pickup_first_name.text = null
                    et_pickup_last_name.text = null
                    et_pickup_contact_number.text = null
                    ly_pickup_someone_details.visibility = View.GONE
                }
            }

            R.id.ly_check_someone_else -> {
                if (clickedId != R.id.ly_check_someone_else) {
                    clickedId = R.id.ly_check_someone_else
                    iv_check_someone_else.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_active_check_box_ico))
                    tv_someone_else.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))

                    iv_check_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
                    tv_me.setTextColor(ContextCompat.getColor(this, R.color.new_gray_color))

                    ly_someone_details.visibility = View.VISIBLE
                }
            }

            R.id.ly_check_me -> {
                if (clickedId != R.id.ly_check_me) {
                    clickedId = R.id.ly_check_me
                    iv_check_someone_else.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
                    tv_someone_else.setTextColor(ContextCompat.getColor(this, R.color.new_gray_color))

                    iv_check_me.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_active_check_box_ico))
                    tv_me.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))

                    et_first_name.text = null
                    et_last_name.text = null
                    et_contact_number.text = null
                    ly_someone_details.visibility = View.GONE
                }
            }

            R.id.ly_pickup_date_time -> {
                setPickUpDateField(findViewById(R.id.tv_pickup_date_time))
            }

            R.id.ly_delivery_date_time -> {
                if (tv_delivery_date_time.text.trim() == resources.getString(R.string.pickup_before_date_time)) {
                    Constant.snackbar(main_layout, "Please first select pickup before date & time")
                } else {
                    setDeliveryDateField(findViewById(R.id.tv_delivery_date_time))
                }
            }

            R.id.pickup_delivery_detail_btn -> {
                val v = Validation()

                if (et_item.text.trim().isEmpty()) {
                    Constant.snackbar(main_layout, "Please enter title of item being picked up")

                } else if (et_quantity.text.trim().isEmpty()) {
                    Constant.snackbar(main_layout, "Please enter quantity")

                } else if (validation.isNotZero(et_quantity)) {
                    Constant.snackbar(main_layout, "Quantity can't be zero")

                } else if (clickedPickUpId == R.id.ly_pickup_someone_else) {
                    when {
                        et_pickup_first_name.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter sender's first name")
                        v.isValidName(et_pickup_first_name) -> Constant.snackbar(main_layout, "Name allow alphabets and space only")
                        et_pickup_last_name.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter sender's last name")
                        v.isValidName(et_pickup_last_name) -> Constant.snackbar(main_layout, "Name allow alphabets and space only")
                        et_pickup_contact_number.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter contact number")

                        tv_pickup_date_time.text.trim() == resources.getString(R.string.pickup_before_date_time) -> Constant.snackbar(main_layout, "Please select pickup before date & time")
                        tv_delivery_date_time.text.trim() == resources.getString(R.string.delivery_before_date_time) -> Constant.snackbar(main_layout, "Please select delivery before date & time")
                        et_amount.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter amount")
                        validation.isValidValue(et_amount) -> Constant.snackbar(main_layout, "Price is not vaild")
                        validation.isNotZero(et_amount) -> Constant.snackbar(main_layout, "Price can't be zero")
                        else -> {
                            ly_delivery_details.visibility = View.GONE
                            ly_tip_summary_details.visibility = View.VISIBLE

                            set_Tip_Delivery_Summary()
                            sv_item_summary.fullScroll(ScrollView.FOCUS_UP)
                        }

                    }
                } else if (clickedId == R.id.ly_check_someone_else) {
                    when {
                        et_first_name.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter first name")
                        v.isValidName(et_first_name) -> Constant.snackbar(main_layout, "Name allow alphabets and space only")
                        et_last_name.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter last name")
                        v.isValidName(et_last_name) -> Constant.snackbar(main_layout, "Name allow alphabets and space only23")
                        et_contact_number.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter contact number")

                        tv_pickup_date_time.text.trim() == resources.getString(R.string.pickup_before_date_time) -> Constant.snackbar(main_layout, "Please select pickup before date & time")
                        tv_delivery_date_time.text.trim() == resources.getString(R.string.delivery_before_date_time) -> Constant.snackbar(main_layout, "Please select delivery before date & time")
                        et_amount.text.trim().isEmpty() -> Constant.snackbar(main_layout, "Please enter amount")
                        validation.isValidValue(et_amount) -> Constant.snackbar(main_layout, "Price is not vaild")
                        validation.isNotZero(et_amount) -> Constant.snackbar(main_layout, "Price can't be zero")
                        else -> {
                            ly_delivery_details.visibility = View.GONE
                            ly_tip_summary_details.visibility = View.VISIBLE

                            set_Tip_Delivery_Summary()
                            sv_item_summary.fullScroll(ScrollView.FOCUS_UP)
                        }
                    }
                } else if (tv_pickup_date_time.text.trim() == resources.getString(R.string.pickup_before_date_time)) {
                    Constant.snackbar(main_layout, "Please select pickup before date & time")

                } else if (tv_delivery_date_time.text.trim() == resources.getString(R.string.delivery_before_date_time)) {
                    Constant.snackbar(main_layout, "Please select delivery before date & time")

                } else if (et_amount.text.trim().isEmpty()) {
                    Constant.snackbar(main_layout, "Please enter amount")

                } else if (validation.isValidValue(et_amount)) {
                    Constant.snackbar(main_layout, "Price is not vaild")

                } else if (validation.isNotZero(et_amount)) {
                    Constant.snackbar(main_layout, "Price can't be zero")

                } else {
                    ly_delivery_details.visibility = View.GONE
                    ly_tip_summary_details.visibility = View.VISIBLE

                    set_Tip_Delivery_Summary()
                    sv_item_summary.fullScroll(ScrollView.FOCUS_UP)
                }
            }

            R.id.rl_signature -> {
                if (!clickedSignature) {
                    clickedSignature = true
                    signatureStatus = 1
                    iv_check_signature.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_active_tick_ico))
                    tv_signature.setTextColor(ContextCompat.getColor(this, R.color.new_app_color))
                    Constant.setTypeface(tv_signature, this, R.font.rubik_medium)
                } else {
                    clickedSignature = false
                    signatureStatus = 0
                    iv_check_signature.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.new_add_inactive_check_box_ico))
                    tv_signature.setTextColor(ContextCompat.getColor(this, R.color.colorTextHint))
                    Constant.setTypeface(tv_signature, this, R.font.rubik_light)
                }
            }
            R.id.collectTimeLay -> {
                if (!TextUtils.isEmpty(tv_pickup_date_time.text)) {
                    setCollectiveTimeField(fromCollTxt)
                } else {
                    Toast.makeText(this, "Please select Shipment Collective Date", Toast.LENGTH_LONG).show()
                }
            }
            R.id.collectTimeLaypickup -> {
                if (!TextUtils.isEmpty(tv_delivery_date_time.text)) {
                    setDeleveryTimeField()
                } else {
                    Toast.makeText(this, "Please select Shipment Delivery Date", Toast.LENGTH_LONG).show()
                }

            }



            R.id.pickup_delivery_confirm_btn -> {

          /*      pickup_delivery_confirm_btn.isEnabled = false
                addCourierSummaryModel!!.description=tv_item_description.text.toString()
                addCourierSummaryModel!!.collectiveDate=pickUpDate
                addCourierSummaryModel!!.deliveryDate=deliveryDate
                addCourierSummaryModel!!.collectiveTime=collectiveTime
                addCourierSummaryModel!!.otherDetails=""
                addCourierSummaryModel!!.deliveryAdrs=deliveryAddress
                addCourierSummaryModel!!.orderNo=""
                addCourierSummaryModel!!.itemImage=courierItemUri
                addCourierSummaryModel!!.receiptImage=""
                addCourierSummaryModel!!.receiverContact=tv_delivery_person_contact.text.toString()
                addCourierSummaryModel!!.pickupLong=""
                addCourierSummaryModel!!.pickupAdrs=pickUpAddress
                addCourierSummaryModel!!.quantity=tv_item_quantity.text.toString()
                addCourierSummaryModel!!.pickupAdrs=pickUpAddress
                addCourierSummaryModel!!.price=tv_delivery_amount.text.toString()
                addCourierSummaryModel!!.deliverLat=deliveryLat
                addCourierSummaryModel!!.deliveryLng=deliveryLng
                addCourierSummaryModel!!.senderName=senderCustomerName
                if (tv_pickup_person_contact.text.toString() == resources.getString(R.string.enter_contact_number)) {
                    addCourierSummaryModel!!.senderContactNo=""
                } else {
                    addCourierSummaryModel!!.senderContactNo=tv_pickup_person_contact.text.toString()
                }
                addCourierSummaryModel!!.price=tv_delivery_amount.text.toString()
                addCourierSummaryModel!!.rcvCountryCode="+1"
                addCourierSummaryModel!!.title=et_item.text.toString()
                addCourierSummaryModel!!.receiverName=receiverCustomerName
                addCourierSummaryModel!!.deliveryTime=deliveryTime
                addCourierSummaryModel!!.collectiveToTime=""
                addCourierSummaryModel!!.orderNo=""
                addCourierSummaryModel!!.deliveryToTime=""
                addCourierSummaryModel!!.signatureStatus=signatureStatus.toString()

                addCourierSummaryModelList?.add(addCourierSummaryModel!!)*/
                addpost()

/*
                val intent = Intent(this, HomeActivity::class.java)
                intent.putParcelableArrayListExtra("param1",addCourierSummaryModelList)
                startActivity(intent)
*/



                //addpost()
            }
        }
    }

    private var deliveryToTime = ""


    private fun setDeleveryTimeField() {

        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            val orTime = "" + hourOfDay + ":" + minute
            val depTime = "" + hourOfDay + ":" + minute
            val (hourString: String, minuteSting: String) = timeFormat(hourOfDay, minute)
            if (toDelTxt.text.equals("")) {
                hourOfDay_delivery = hourOfDay
                minute_delivery = minute
            }

            delTime = "" + hourString + ":" + minuteSting
            fromDelTxt.text = Constant.setTimeFormat(delTime)

            val cal = Calendar.getInstance()
            //val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH)
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            try {

                cal.time = sdf.parse(tv_pickup_date_time.text.toString() + " " + "" + hourOfDay + ":" + minute)
                cal.add(Calendar.HOUR_OF_DAY, 1)

                hourOfDay_delivery = hourOfDay
                minute_delivery = minute

                val (hourString: String, minuteSting: String) = timeFormat(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))
                var temp = Constant.setTimeFormat("" + hourString + ":" + minuteSting)
                toDelTxt.text = temp
                deliveryToTime = "" + hourString + ":" + minuteSting


            } catch (e: ParseException) {
                e.printStackTrace()
            }
        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)
        myTime!!.show(this.getFragmentManager(), "")
        //myTime!!.is24HourMode();

        if (tv_pickup_date_time.text.toString().equals(tv_pickup_date_time.text.toString())) {
            myTime!!.setMinTime(sethourOfDay, setminute, setsecond)
        }

        myTime!!.setOnCancelListener(DialogInterface.OnCancelListener { myTime!!.dismiss() })
    }


    private fun timeFormat(hourOfDay: Int, minute: Int): Pair<String, String> {
        val hourString: String
        if (hourOfDay < 10)
            hourString = "0" + hourOfDay
        else
            hourString = "" + hourOfDay

        val minuteSting: String
        if (minute < 10)
            minuteSting = "0" + minute
        else
            minuteSting = "" + minute
        return Pair(hourString, minuteSting)
    }

    private var collectiveToTime = ""


    private fun setCollectiveTimeField(textview: TextView) {
        val c = Calendar.getInstance()
        val curr_seconds = c.get(Calendar.SECOND)
        val curr_minutes = c.get(Calendar.MINUTE)
        val curr_hour = c.get(Calendar.HOUR)

        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            hourOfDay_collective = hourOfDay
            minute_collective = minute

            val hourString: String
            if (hourOfDay < 10)
                hourString = "0" + hourOfDay
            else
                hourString = "" + hourOfDay

            val minuteSting: String
            if (minute < 10)
                minuteSting = "0" + minute
            else
                minuteSting = "" + minute

            colTime = "" + hourString + ":" + minuteSting
            textview.text = Constant.setTimeFormat(colTime)

            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            try {

                cal.time = sdf.parse(tv_pickup_date_time.text.toString() + " " + "" + hourOfDay + ":" + minute)
                cal.add(Calendar.HOUR_OF_DAY, 1)
                val (hourString: kotlin.String, minuteSting: kotlin.String) = timeFormat(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

                sethourOfDay = hourString.toInt()
                setminute = minuteSting.toInt()
                setsecond = 0
                collectiveToTime = "" + hourString + ":" + minuteSting
                toCollTxt.text = Constant.setTimeFormat("" + hourString + ":" + minuteSting)


               // tv_pickup_date_time.text = ""
                fromDelTxt.text = ""
                toDelTxt.text = ""
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)

        myTime!!.show(this.getFragmentManager(), "")
        //   myTime.is24HourMode();
        myTime!!.setMinTime(curr_hour, curr_minutes, curr_seconds)
        myTime!!.setOnCancelListener(DialogInterface.OnCancelListener { myTime!!.dismiss() })
    }

   /* private fun setCollectiveTimeField(textview: TextView) {
        val c = Calendar.getInstance()
        val curr_seconds = c.get(Calendar.SECOND)
        val curr_minutes = c.get(Calendar.MINUTE)
        val curr_hour = c.get(Calendar.HOUR)

        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            hourOfDay_collective = hourOfDay
            minute_collective = minute

            val hourString: String
            if (hourOfDay < 10)
                hourString = "0" + hourOfDay
            else
                hourString = "" + hourOfDay

            val minuteSting: String
            if (minute < 10)
                minuteSting = "0" + minute
            else
                minuteSting = "" + minute

            colTime = "" + hourString + ":" + minuteSting
            textview.text = Constant.setTimeFormat(colTime)

            val cal = Calendar.getInstance()
            val sdf = SimpleDateFormat("MM/dd/yyyy HH:mm", Locale.ENGLISH)
            try {

                cal.time = sdf.parse(shptCltDtTxt.text.toString() + " " + "" + hourOfDay + ":" + minute)
                cal.add(Calendar.HOUR_OF_DAY, 1)
                val (hourString: kotlin.String, minuteSting: kotlin.String) = timeFormat(cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE))

                sethourOfDay = hourString.toInt()
                setminute = minuteSting.toInt()
                setsecond = 0
                collectiveToTime = "" + hourString + ":" + minuteSting
                toCollTxt.text = Constant.setTimeFormat("" + hourString + ":" + minuteSting)


                shptdelDtTxt.text = ""
                fromDelTxt.text = ""
                toDelTxt.text = ""
            } catch (e: ParseException) {
                e.printStackTrace()
            }

        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)

        myTime!!.show(this.getFragmentManager(), "")
        //   myTime.is24HourMode();
        myTime!!.setMinTime(curr_hour, curr_minutes, curr_seconds)
        myTime!!.setOnCancelListener(DialogInterface.OnCancelListener { myTime!!.dismiss() })
    }*/


    // Set Tip Summary Screen Data
    @SuppressLint("SetTextI18n")
    private fun set_Tip_Delivery_Summary() {
        tv_sel_pickup_address.text = pickUpAddress
        val sb = StringBuilder()
        var a=""
        var b=""
        var c=""
        a= tv_pickup_date_time.text as String
       // b= collectiveToTime

        sb.append(a).append(" ").append(fromCollTxt.text.toString()).append("-").append(toCollTxt.text.toString())
        pickUpDate=sb.toString()
        tv_sel_pickup_date_time.text = pickUpDate
        tv_sel_drop_off_address.text = deliveryAddress
        val stringBuilder=java.lang.StringBuilder()


        var deliverydate =""
        var deliverytime=""
        deliverydate= tv_pickup_date_time.text as String
        deliverytime= deliveryToTime
        stringBuilder.append(deliverydate).append(" ").append(fromDelTxt.text.toString()).append("-").append(toDelTxt.text.toString())
        tv_sel_drop_off_date_time.text = stringBuilder.toString()

        if (clickedPickUpId == R.id.ly_pickup_someone_else) {
            val f_name = et_pickup_first_name.text.trim().toString()
            val l_name = et_pickup_last_name.text.trim().toString()

            tv_pickup_person_name.text = f_name + " " + l_name
            senderCustomerName = f_name + " " + l_name

            tv_pickup_person_contact.text = et_pickup_contact_number.text.trim().toString()

        } else if (clickedPickUpId == R.id.ly_pickup_check_me) {
            senderCustomerName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
            tv_pickup_person_name.text = senderCustomerName

            val con = PreferenceConnector.readString(this, PreferenceConnector.USERCONTACTNO, "")
            if (!con.isEmpty()) {
                tv_pickup_person_contact.text = con
            } else {
                ly_pick_contact.visibility = View.GONE
                tv_pick_contact_na.visibility = View.VISIBLE
            }

        }

        if (clickedId == R.id.ly_check_someone_else) {
            val f_name = et_first_name.text.trim().toString()
            val l_name = et_last_name.text.trim().toString()

            tv_delivery_person_name.text = f_name + " " + l_name
            receiverCustomerName = f_name + " " + l_name

            tv_delivery_person_contact.text = et_contact_number.text.trim().toString()

        } else if (clickedId == R.id.ly_check_me) {
            receiverCustomerName = PreferenceConnector.readString(this, PreferenceConnector.USERFULLNAME, "")
            tv_delivery_person_name.text = receiverCustomerName

            val cont = PreferenceConnector.readString(this, PreferenceConnector.USERCONTACTNO, "")

            if (!cont.isEmpty()) {
                tv_delivery_person_contact.text = cont
            } else {
                ly_deliver_contact.visibility = View.GONE
                tv_deliver_contact_na.visibility = View.VISIBLE
            }

        }

        tv_item_title.text = et_item.text.trim()

        if (et_description.text.trim().isEmpty()) {
            tv_item_description.text = getString(R.string.na_txt)
        } else {
            tv_item_description.text = et_description.text.trim()
        }

        tv_item_quantity.text = et_quantity.text.trim()

        tv_delivery_amount.text = et_amount.text.trim().toString().toFloat().toString()
        sv_item_summary.fullScroll(ScrollView.FOCUS_UP)

        pickup_delivery_confirm_btn.setOnClickListener(this)
    }

    // Set Pick Up Before Date
    private fun setPickUpDateField(view: TextView) {
        fromDate = DatePickerDialog.newInstance({ datePickerDialog, year, monthOfYear, dayOfMonth ->
            var date = year.toString() + "-" + (monthOfYear + 1).toString() + "-" + dayOfMonth
            var month1 = if (monthOfYear + 1 < 10) "0" + (monthOfYear + 1) else "" + (monthOfYear + 1)
            var day1 = if (dayOfMonth < 10) "0" + dayOfMonth else "" + dayOfMonth
            //var date1 = year.toString() + "-" + month1 + "-" + day1
            var date1 = month1 + "/" + day1 + "/" + year.toString()

            view.text = date1

            fromCollTxt.text = ""
            toCollTxt.text = ""

           // shptdelDtTxt.text = ""
            fromDelTxt.text = ""
            toDelTxt.text = ""

            year_live = year
            month_live = monthOfYear
            day_live = day1.toInt()

        }, now!!.get(Calendar.YEAR), now!!.get(Calendar.MONTH), now!!.get(Calendar.DAY_OF_MONTH))
        fromDate?.setMinDate(Calendar.getInstance())
        fromDate?.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fromDate?.show(this.getFragmentManager(), "")
        fromDate?.setOnCancelListener(DialogInterface.OnCancelListener {
            Log.d("TimePicker", "Dialog was cancelled")
            fromDate?.dismiss()
        })
    }

    // Set Delivery/Drop Off Before Date
    private fun setDeliveryDateField(view: TextView) {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        try {
            cal.time = sdf.parse(tv_pickup_date_time.text.toString())

        } catch (e: ParseException) {
            e.printStackTrace()
        }

        fromDate = DatePickerDialog.newInstance({ datePickerDialog, year, monthOfYear, dayOfMonth ->
            var date = year.toString() + "-" + (monthOfYear + 1).toString() + "-" + dayOfMonth

            var month1 = if (monthOfYear + 1 < 10) "0" + (monthOfYear + 1) else "" + (monthOfYear + 1)
            var day1 = if (dayOfMonth < 10) "0" + dayOfMonth else "" + dayOfMonth
            // var date1 = year.toString() + "-" + month1 + "-" + day1
            var date1 = month1 + "/" + day1 + "/" + year.toString()
            view.text = date1

            fromDelTxt.text = ""
            toDelTxt.text = ""
        }, cal!!.get(Calendar.YEAR), cal!!.get(Calendar.MONTH), cal!!.get(Calendar.DAY_OF_MONTH))



        cal.set(year_live, month_live, day_live)

        fromDate?.setMinDate(cal)
        fromDate?.setAccentColor(ContextCompat.getColor(this, R.color.colorPrimaryDark))
        fromDate?.show(this.getFragmentManager(), "")
        fromDate?.setOnCancelListener(DialogInterface.OnCancelListener {
            Log.d("TimePicker", "Dialog was cancelled")
            fromDate?.dismiss()
        })
    }

    // Set Pick Up Before Time
    private fun setPickUpTimeField(month: String, day: String, year: String, textview: TextView) {
        val tDate = Date()

        val currentHour = tDate.hours
        val currentMinutes = tDate.minutes
        val currentSeconds = tDate.seconds

        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            val format: String
            hourOfDay_collective = hourOfDay

            when {
                hourOfDay == 0 -> {
                    hourOfDay_collective += 12
                    format = "am"
                }
                hourOfDay == 12 -> format = "pm"
                hourOfDay > 12 -> {
                    hourOfDay_collective -= 12
                    format = "pm"
                }
                else -> format = "am"
            }

            val hour = if (hourOfDay_collective < 10) "0" + hourOfDay_collective else "" + hourOfDay_collective
            val min = if (minute < 10) "0" + minute else "" + minute
            val time = hour + ":" + min

            val displayFormat = SimpleDateFormat("HH:mm", Locale.US)
            val parseFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val tempTime = time + " " + format
            val date = parseFormat.parse(tempTime)
            val temp = displayFormat.format(date)
            collectiveTime = temp

            try {
                pickUpHour = hour
                pickUpMin = min
                pickUpDate = "$month/$day/$year"
                pickUpFormat = format
                textview.text = "$pickUpDate $time $format"
                Constant.setTypeface(textview, this, R.font.rubik_regular)
                textview.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))

                tv_delivery_date_time.text = resources.getText(R.string.delivery_before_date_time)
                tv_delivery_date_time.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))

            } catch (e: Exception) {
                e.printStackTrace()
            }


        }, now!!.get(Calendar.HOUR_OF_DAY), now!!.get(Calendar.MINUTE), false)

        myTime!!.show(this.fragmentManager, "")

        val datetime = Calendar.getInstance()
        val c = Calendar.getInstance()
        val date = "$day.$month.$year"

        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        val selectedDate: Date
        val currentDate: Date

        try {
            selectedDate = sdf.parse(date)
            currentDate = Calendar.getInstance().time

            val selectDate = selectedDate.toString()
            val cDate = currentDate.toString()

            //split date
            val Date1 = selectDate.substring(0, 10)
            val Date2 = cDate.substring(0, 10)

            if (Date1.contains(Date2)) {
                // it's same
                if (datetime.timeInMillis >= c.timeInMillis) {
                    myTime!!.setMinTime(currentHour, currentMinutes, currentSeconds)
                }

            }
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        myTime!!.setOnCancelListener({ myTime!!.dismiss() })
    }

    // Set Delivery/Drop Off Before Time
    private fun setDeliveryTimeField(month: String, day: String, year: String, textview: TextView) {
        val tDate = Date()

        val currentSeconds = tDate.seconds

        val cal = Calendar.getInstance()

        val dFormat = SimpleDateFormat("HH:mm", Locale.US)
        val pFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val tTime = "$pickUpHour:$pickUpMin $pickUpFormat"
        val dDate = pFormat.parse(tTime)
        val tTemp = dFormat.format(dDate)

        val timeArray = tTemp.split(":")

        cal.set(year.toInt(), month.toInt(), day.toInt(), Integer.parseInt(timeArray[0]), Integer.parseInt(timeArray[1]))
        cal.add(Calendar.MINUTE, 60)

        myTime = TimePickerDialog.newInstance({ view, hourOfDay, minute, second ->
            val format: String
            hourOfDay_delivery = hourOfDay

            when {
                hourOfDay == 0 -> {
                    hourOfDay_delivery += 12
                    format = "am"
                }
                hourOfDay == 12 -> format = "pm"
                hourOfDay > 12 -> {
                    hourOfDay_delivery -= 12
                    format = "pm"
                }
                else -> format = "am"
            }

            val hour = if (hourOfDay_delivery < 10) "0" + hourOfDay_delivery else "" + hourOfDay_delivery
            val min = if (minute < 10) "0" + minute else "" + minute
            val time = hour + ":" + min

            val displayFormat = SimpleDateFormat("HH:mm", Locale.US)
            val parseFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val tempTime = time + " " + format
            val date = parseFormat.parse(tempTime)
            val temp = displayFormat.format(date)
            deliveryTime = temp

            try {
                deliveryDate = "$month/$day/$year"
                textview.text = "$deliveryDate $time $format"
                Constant.setTypeface(textview, this, R.font.rubik_regular)
                textview.setTextColor(ContextCompat.getColor(this, R.color.colorBlack))
            } catch (e: Exception) {
                e.printStackTrace()
            }


        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false)

        myTime!!.show(this.fragmentManager, "")

        val displayFormat = SimpleDateFormat("HH:mm", Locale.US)
        val parseFormat = SimpleDateFormat("hh:mm a", Locale.US)
        val tempTime = ((pickUpHour.toInt() + 1).toString()) + ":" + pickUpMin + " " + pickUpFormat
        val date = parseFormat.parse(tempTime)
        val temp = displayFormat.format(date)

        val s = temp.split(":")

        if (pickUpDate == "$month/$day/$year") {
            myTime!!.setMinTime(Integer.parseInt(s[0]), Integer.parseInt(s[1]), currentSeconds)
        }

        myTime!!.setOnCancelListener({ myTime!!.dismiss() })
    }

    // Input filter used to restrict amount input to be round off to 2 decimal places
    private fun inputFilter(et: EditText) {
        et.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {
                if (et.text.toString().contains(".")) {
                    if (et.text.toString().substring(et.text.toString().indexOf(".") + 1, et.length()).length == 2) {
                        val fArray = arrayOfNulls<InputFilter>(1)
                        fArray[0] = InputFilter.LengthFilter(arg0.length)
                        et.filters = fArray
                    }
                }

                et.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(6))
            }

            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {

            }

            override fun afterTextChanged(arg0: Editable) {
                if (arg0.isNotEmpty()) {
                    val str = et.text.toString()
                    et.setOnKeyListener { v, keyCode, event ->
                        if (keyCode == KeyEvent.KEYCODE_DEL) {
                            count--
                            val fArray = arrayOfNulls<InputFilter>(1)
                            fArray[0] = InputFilter.LengthFilter(100)
                            et.filters = fArray
                        }
                        false
                    }
                    val t = str[arg0.length - 1]
                    if (t == '.') {
                        count = 0
                    }
                    if (count >= 0) {
                        if (count == 2) {
                            val fArray = arrayOfNulls<InputFilter>(1)
                            fArray[0] = InputFilter.LengthFilter(arg0.length)
                            et.filters = fArray
                        }
                        count++
                    }
                }
            }
        })
    }

    fun alertSuccess() {
        val alertDialog=AlertDialog.Builder(this@NewAddCourierSummaryActivity).apply {
            setTitle("Alert")
            setCancelable(false)
            setMessage("Your item has been added successfully to the items you would like to have delivered, If you’d like to add more than 1 item to the delivery you can add another delivery on the next screen going to and from another address in the same city")
            setPositiveButton("Ok", { dialog, which ->
                val intent = Intent(this@NewAddCourierSummaryActivity, HomeActivity::class.java)
                startActivity(intent)

            })

            show()


        }
        /*val alertDialog = AlertDialog.Builder(this).apply {
            setTitle("Alert")
            setCancelable(false)
            setMessage(" Your item has been added successfully to the items you would like to have delivered, If you’d like to add more than 1 item to the delivery you can add another delivery on the next screen going to and from another address in the same city")
            setPositiveButton("Ok", { dialog, which ->

            })

            show()
        }*/

    }



    private fun addpost() {
        if (Constant.isNetworkAvailable(this, main_layout)) {
            // progressBar.visibility = View.VISIBLE
            progress!!.show()

            val multipartRequest = object : VolleyMultipartRequest(Request.Method.POST,
                    Constant.BASE_URL + Constant.Add_Post_Url, Response.Listener { response ->
                val resultResponse = String(response.data)
                //  progressBar?.visibility = View.GONE
                progress!!.dismiss()

                try {
                    val result = JSONObject(resultResponse)
                    val status = result.getString("status")
                    val message = result.getString("message")
                    val postId = result.getString("postId")
                    GetPostId.instance.postID=postId
                   // PreferenceConnector.writeString(this@NewAddCourierSummaryActivity,PreferenceConnector.POSTID,postId)

                    alertSuccess()


                    }
                catch (e: JSONException) {
                    e.printStackTrace()
                }
            }, Response.ErrorListener { error ->
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

                        Snackbar.make(main_layout, message, Snackbar.LENGTH_LONG).setAction("ok", null).show()
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }

                }
                error.printStackTrace()
            }) {

                override fun getHeaders(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("authToken", PreferenceConnector.readString(this@NewAddCourierSummaryActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return params
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()

                    params.put("itemTitle", tv_item_title.text.toString())

                    if (tv_item_description.text == getString(R.string.na_txt)) {
                        params.put("description", "")
                    } else {
                        params.put("description", tv_item_description.text.toString())
                    }

                    params.put("pickupAdrs", tv_sel_pickup_address.text.toString())
                    params.put("pickupLat", pickUpLat)
                    params.put("pickupCity", pickupCity)
                    params.put("deliveryCity", deliveryCity)
                    params.put("postTitle", postTitle)
                    params.put("postId",  GetPostId.instance.postID)
                    params.put("pickupLong", pickUpLng)
                    params.put("deliveryAdrs", tv_sel_drop_off_address.text.toString())
                    params.put("deliverLat", deliveryLat)
                    params.put("deliverLong", deliveryLng)
                    params.put("collectiveDate", pickUpDate)
                    params.put("deliveryDate", deliveryDate)
                    params.put("collectiveTime", collectiveTime)
                    params.put("deliveryTime", deliveryTime)
                    params.put("quantity", tv_item_quantity.text.toString())
                    params.put("price", tv_delivery_amount.text.toString())
                    // params.put("otherDetails", "")
                 //   *//**//* params.put("orderNo", "")
                   // params.put("receiptImage", "")*//**//*

                            if (tv_pickup_person_contact.text.toString() == resources.getString(R.string.enter_contact_number)) {
                                params.put("senderContactNo", "")
                            } else {
                                params.put("senderContactNo", tv_pickup_person_contact.text.toString())
                            }

                    if (tv_delivery_person_contact.text.toString() == resources.getString(R.string.enter_contact_number)) {
                        params.put("receiverContact", "")
                    } else {
                        params.put("receiverContact", tv_delivery_person_contact.text.toString())
                    }

                    params.put("receiverName", receiverCustomerName)
                    params.put("senderName", senderCustomerName)

                    params.put("rcvCountryCode", "+1")

                    params.put("collectiveToTime", "")
                    params.put("deliveryToTime", "")
                    //   params.put("orderNo", "")
                    params.put("signatureStatus", signatureStatus.toString())

                    if (addPicBitmap == null) {
                        params.put("itemImage", "")
                    }
                    return params
                }


                override val byteData: Map<String, DataPart>?
                    @Throws(IOException::class)
                    get() {
                        val params = HashMap<String, DataPart>()
                        if (addPicBitmap != null) {
                            params.put("itemImage", DataPart("profileImage.jpg", AppHelper.getFileDataFromDrawable(addPicBitmap!!), "image/jpg"))
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

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        val pickUp = LatLng(pickUpLat.toDouble(), pickUpLng.toDouble())
        val delivery = LatLng(deliveryLat.toDouble(), deliveryLng.toDouble())
        mMap!!.addMarker(MarkerOptions().position(pickUp).icon(BitmapDescriptorFactory.fromResource(R.drawable.new_add_blue_pickup_ico)))
        mMap!!.addMarker(MarkerOptions().position(delivery).icon(BitmapDescriptorFactory.fromResource(R.drawable.new_add_blue_dot_ico)))

        val options = PolylineOptions()
        options.color(ContextCompat.getColor(this, R.color.new_app_color))
        options.width(5f)

        val url = getURL(pickUp, delivery)

        val FetchUrl = FetchUrl()

        // Start downloading json data from Google Directions API
        FetchUrl.execute(url + "&key=" + resources.getString(R.string.google_maps_key))

        //move map camera
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(pickUp))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(11f))


    }

    private fun getURL(from: LatLng, to: LatLng): String {
        val origin = "origin=" + from.latitude + "," + from.longitude
        val dest = "destination=" + to.latitude + "," + to.longitude
        val sensor = "sensor=false"
        val mode = "mode=driving"
        val alternative = "alternatives=true"
        val params = "$origin&$dest&$sensor&$mode&$alternative"
        return "https://maps.googleapis.com/maps/api/directions/json?$params"
    }

    // Fetches data from url passed
    private inner class FetchUrl : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg url: String): String {
            // For storing data from web service
            var data = ""

            try {
                val data1 = AddressLocationTask(this@NewAddCourierSummaryActivity, url[0], object : AddressLocationTask.AddressLocationListner {
                    override fun getLocation(result: String?) {
                        data = result.toString()

                        val parserTask = ParserTask()
                        parserTask.execute(data)
                    }
                }).execute()
            } catch (e: Exception) {
                e.printStackTrace()
            }


            return data
        }
    }

    private inner class ParserTask : AsyncTask<String, Int, List<List<HashMap<String, String>>>>() {
        // Parsing the data in non-ui thread
        override fun doInBackground(vararg jsonData: String): List<List<HashMap<String, String>>> {

            val jObject: JSONObject
            var routes: List<List<HashMap<String, String>>> = ArrayList<ArrayList<HashMap<String, String>>>()

            try {
                jObject = JSONObject(jsonData[0])
                val parser = DataParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }

        // Executes in UI thread, after the parsing process
        override fun onPostExecute(result: List<List<HashMap<String, String>>>) {
            var points: ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null

            for (i in result.indices) {

                points = ArrayList<LatLng>()
                lineOptions = PolylineOptions()

                // Fetching i-th route
                val path = result[i]

                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]

                    val lat = java.lang.Double.parseDouble(point["lat"])
                    val lng = java.lang.Double.parseDouble(point["lng"])

                    val position = LatLng(lat, lng)

                    points.add(position)
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(5f)
                lineOptions.color(ContextCompat.getColor(this@NewAddCourierSummaryActivity, R.color.new_app_color))
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap!!.addPolyline(lineOptions)
            }
        }
    }


    // On Back Press
    override fun onBackPressed() {
        if (ly_tip_summary_details.visibility == View.VISIBLE) {
            ly_delivery_details.visibility = View.VISIBLE
            ly_tip_summary_details.visibility = View.GONE
            sv_item_summary.fullScroll(ScrollView.FOCUS_UP)
        } else {
            finish()
        }
    }
}

class DataParser {
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude  */
    fun parse(jObject: JSONObject): List<List<HashMap<String, String>>> {

        val routes = ArrayList<List<HashMap<String, String>>>()
        val jRoutes: JSONArray
        var jLegs: JSONArray
        var jSteps: JSONArray

        try {
            jRoutes = jObject.getJSONArray("routes")

            /** Traversing all routes  */
            for (i in 0 until jRoutes.length()) {
                jLegs = (jRoutes.get(0) as JSONObject).getJSONArray("legs")
                val path = ArrayList<HashMap<String, String>>()

                /** Traversing all legs  */
                for (j in 0 until jLegs.length()) {
                    jSteps = (jLegs.get(j) as JSONObject).getJSONArray("steps")

                    /** Traversing all steps  */
                    for (k in 0 until jSteps.length()) {
                        val polyline = ((jSteps.get(k) as JSONObject).get("polyline") as JSONObject).get("points") as String
                        val list = decodePoly(polyline)

                        /** Traversing all points  */
                        for (l in list.indices) {
                            val hm = HashMap<String, String>()
                            hm.put("lat", java.lang.Double.toString(list[l].latitude))
                            hm.put("lng", java.lang.Double.toString(list[l].longitude))
                            path.add(hm)
                        }
                    }
                    routes.add(path)
                }
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: Exception) {
        }

        return routes
    }





    // Method to decode polyline points
    private fun decodePoly(encoded: String): List<LatLng> {

        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].toInt() - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5,
                    lng.toDouble() / 1E5)
            poly.add(p)
        }

        return poly
    }



}
