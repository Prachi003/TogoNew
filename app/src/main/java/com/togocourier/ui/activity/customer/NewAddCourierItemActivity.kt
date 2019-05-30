package com.togocourier.ui.activity.customer

import android.Manifest
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.text.TextUtils
import android.view.View
import android.view.Window
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.Status
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.places.AutocompleteFilter
import com.google.android.gms.location.places.Place
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment
import com.google.android.gms.location.places.ui.PlaceSelectionListener
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.togocourier.Interface.getAddressListner
import com.togocourier.R
import com.togocourier.adapter.AddressAdapter
import com.togocourier.adapter.DeliveryAddressAdapter
import com.togocourier.responceBean.AddressBean
import com.togocourier.responceBean.AddressInfo
import com.togocourier.responceBean.DeliverAddressInfo
import com.togocourier.ui.activity.customer.model.AddCourierSummaryModel
import com.togocourier.ui.activity.customer.model.newcustomer.AddressInfoK
import com.togocourier.util.Constant
import com.togocourier.util.HelperClass
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.new_activity_add_courier_item.*
import kotlinx.android.synthetic.main.new_add_courier_pickup_delivery_address.*
import kotlinx.android.synthetic.main.select_address_dialog.*
import org.json.JSONException
import org.json.JSONObject
import java.util.*
import kotlin.collections.ArrayList

class NewAddCourierItemActivity : AppCompatActivity(), View.OnClickListener, com.google.android.gms.location.LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    //getting location
    private val TAG = "TOGO"
    private var INTERVAL = (1000 * 10).toLong()
    private var FASTEST_INTERVAL = (1000 * 5).toLong()
    private var mLocationRequest: LocationRequest = LocationRequest()
    private var mGoogleApiClient: GoogleApiClient? = null
    private var lmgr: LocationManager? = null
    private var isGPSEnable: Boolean = false
    private var isSelectAddress: Boolean = false
    private var isSelectdelivery: Boolean = false

    private var currentAddress = "Location not found"
    private var postTitle = ""
    private var courierItemUri = ""
    var localDelivery=""

    private var picLat = ""
    private var picUpLat = ""
    private var picUpLng = ""
    private var picLng = ""
    private var delAddress = ""
    private var delLat = ""
    private var pickupCity=""
    private var deliveryCity=""
    private var cityD=""
    private var cityP=""
    private var delLng = ""
    private var deliveryAdrs = ""
    private var pickupAdrs = ""
    private var deliveryLat = ""
    private var deliveryLng = ""
    private var picAddress = ""

    private var curLat = ""
    private var city = ""
    private var curLng = ""

    private var deliveraddList = ArrayList<DeliverAddressInfo.DataBean>()
    private var addressListInfo = ArrayList<AddressBean.ResultBean>()
    private var addressList = ArrayList<AddressInfoK.DataBean>()

    private var mMap: GoogleMap? = null
    private var picUpLatLng: LatLng? = null
    private var deliveryLatLng: LatLng? = null
    private var arrayList:ArrayList<AddCourierSummaryModel>?=null

    // variable to track event time
    private var mLastClickTime: Long = 0
    private var progress: ProgressDialog? = null

    private fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_add_courier_item)

        progress = ProgressDialog(this)

        Constant.hideSoftKeyboard(this)
        initializeView()

        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        lmgr = getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    private fun initializeView() {
        iv_backn.setOnClickListener(this)
        rl_add_pickup_address.setOnClickListener(this)
        rl_add_delivery_address.setOnClickListener(this)
        add_courier_address_btn.setOnClickListener(this)

        if (intent != null) {
            courierItemUri = intent.getStringExtra("courierItemUri")
            postTitle = intent.getStringExtra("postTitle")
            arrayList=intent.getParcelableArrayListExtra("param1")
            deliveryAdrs=intent.getStringExtra("deliveryAdrs")
            pickupAdrs=intent.getStringExtra("pickupAdrs")
            picUpLat=intent.getStringExtra("pickUpLat")
            picUpLng=intent.getStringExtra("picUpLng")
            deliveryLat=intent.getStringExtra("deliveryLat")
            deliveryLng=intent.getStringExtra("deliveryLng")
            picUpLng=intent.getStringExtra("picUpLng")

            pickupCity=intent.getStringExtra("pickupCity")
            deliveryCity=intent.getStringExtra("deliveryCity")
            if (!deliveryCity.equals("")){
                localDelivery=deliveryCity

            }

            addressListInfo= ArrayList()

            getAddressApi(main_layout)


        }

       /* if(!TextUtils.isEmpty(pickupAdrs)){
            tv_pickup_address.setText(pickupAdrs)
            tv_delivery_address.setText(deliveryAdrs)
        }*/

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

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

    override fun onClick(view: View?) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view!!.id) {
            R.id.rl_add_pickup_address -> {
                if (isGpsEnable()){
                    getAddressApiPickUp(view)

                }



            }

            R.id.rl_add_delivery_address -> {
                if (isGpsEnable())
                    getAddressApiDeliver(view)
            }

            R.id.iv_backn -> {
                onBackPressed()
            }

            R.id.add_courier_address_btn -> {
                when {
                    tv_pickup_address.text == resources.getString(R.string.pickup_address) -> Constant.snackbar(main_layout, "Please select pickup address")
                    tv_delivery_address.text == resources.getString(R.string.drop_off_address) -> Constant.snackbar(main_layout, "Please select drop off address")
                    else -> {
                        when {
                            picLat.equals("")&&delLat.equals("") -> {
                                picLat=picUpLat
                                picLng=picUpLng
                                delLat=deliveryLat
                                delLng=deliveryLng
                            }
                            delLat.equals("")&&delLng.equals("") -> {
                                delLat=deliveryLat
                                delLng=deliveryLng

                            }
                            picLat.equals("")&&picLng.equals("") -> {
                                picLat=picUpLat
                                picLng=picUpLng

                            }
                        }
                        val intent = Intent(this, NewAddCourierSummaryActivity::class.java)
                        intent.putExtra("courierItemUri", courierItemUri)
                        intent.putExtra("pickUpAddress", tv_pickup_address.text.trim())
                        intent.putExtra("pickUpLat",picLat)
                        intent.putExtra("pickUpLng",picLng)

                        intent.putExtra("postTitle",postTitle)
                        intent.putExtra("deliveryCity",cityD)
                        intent.putExtra("pickupCity",cityP)
                        intent.putExtra("pickUpLng", picLng)
                        intent.putParcelableArrayListExtra("param1",arrayList)
                        intent.putExtra("deliveryAddress", tv_delivery_address.text.trim())
                        intent.putExtra("deliveryLat", delLat)
                        intent.putExtra("deliveryLng", delLng)
                        startActivity(intent)
                    }
                }
            }
        }
    }


    private fun selectAddressDialog(view: View) {

        //val placePickAddFragment: PlaceAutocompleteFragment?


        val openDialog = Dialog(this, R.style.AppTheme)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setContentView(R.layout.select_address_dialog)
        openDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        if (!TextUtils.isEmpty(currentAddress)) {
            openDialog.tv_current_address.text = currentAddress
        }

        openDialog.curr_loc_layout.setOnClickListener {
            /*picLat = curLat
            picLng = curLng*/

            getAddressApi(main_layout)
            if (!TextUtils.isEmpty(curLat) && !TextUtils.isEmpty(curLng) && openDialog.tv_current_address.text != "Location not found") {
                val city = getFullAddress(curLat.toDouble(), curLng.toDouble())


                openDialog.radio_btn_current_location.isChecked = false
                openDialog.dismiss()
                for (i in 0..addressListInfo.size - 1){
                    if (addressListInfo[i].cityName!!.contains(city)){
                        tv_pickup_address.text = picAddress
                        picLat = curLat
                        tv_pickup_address.text = city
                        tv_pickup_address.setTextColor(ContextCompat.getColor(this@NewAddCourierItemActivity, R.color.colorBlack))
                        picAddress = city
                        picLat = curLat
                        picLng = curLng
                        cityP=city
                        picAddress = city
                        picLng = curLng
                        isSelectAddress=true
                        openDialog.dismiss()
                        break
                    }else{
                        isSelectAddress=false
                    }
                }

                if (isSelectAddress==false){
                    val alertDialog = android.app.AlertDialog.Builder(this@NewAddCourierItemActivity)
                    alertDialog.setTitle("Alert")
                    alertDialog.setCancelable(false)
                    alertDialog.setMessage("Service is not available in selected area")
                    alertDialog.setPositiveButton("Ok", { dialog, which ->
                        alertDialog.setCancelable(true)
                    })
                    alertDialog.show()


                }


            }
        }

        openDialog.radio_btn_current_location.isChecked = false
        openDialog.radio_btn_current_location.isChecked = tv_pickup_address.text.toString().trim() == currentAddress

        val autocompleteFilter: AutocompleteFilter = AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .build()

        val adAdapter = AddressAdapter(picAddress, addressList, object : getAddressListner {
            override fun getAddress(address: String, pickupLat: String, pickupLong: String) {
                tv_pickup_address.text = address
                tv_pickup_address.setTextColor(ContextCompat.getColor(this@NewAddCourierItemActivity, R.color.colorBlack))
                picAddress = address
                picLat = pickupLat
                picLng = pickupLong

                openDialog.radio_btn_current_location.isChecked = false
                openDialog.dismiss()
            }

        })
        openDialog.recycler_view_address.adapter = adAdapter

        if (addressList.size == 0) {
            getAddressApiPickUp(view, openDialog, adAdapter)
        }

       val placePickAddFragment = fragmentManager.findFragmentById(R.id.placePicker) as PlaceAutocompleteFragment

        placePickAddFragment.setFilter(autocompleteFilter)
        placePickAddFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {

            override fun onPlaceSelected(place: Place) {
                picAddress = place.address.toString()
                picUpLatLng = place.latLng
                picLat=place.latLng.latitude.toString()
                picLng=place.latLng.longitude.toString()

                val city = getFullAddress(place.latLng.latitude, place.latLng.longitude)
                if (pickupCity.equals("")){
                    if (!TextUtils.isEmpty(curLat) && !TextUtils.isEmpty(curLng) && openDialog.tv_current_address.text != "Location not found") {
                        // val city = getFullAddress(picLat.toDouble(), picLat.toDouble())
                        for (i in 0..addressListInfo.size - 1){
                            if (addressListInfo[i].cityName!!.contains(city)){

                                tv_pickup_address.text = picAddress
                                cityP=city

                                picLat = curLat
                                picAddress = city
                                picLng = curLng
                                isSelectAddress=true





                                openDialog.dismiss()
                                break

                            }else{
                                isSelectAddress=false
                            }
                        }

                        if (isSelectAddress==false){
                            val alertDialog = android.app.AlertDialog.Builder(this@NewAddCourierItemActivity)
                            alertDialog.setTitle("Alert")
                            alertDialog.setCancelable(false)
                            alertDialog.setMessage("Service is not available in selected area")
                            alertDialog.setPositiveButton("Ok", { dialog, which ->
                                alertDialog.setCancelable(true)
                                val geocoder = Geocoder(this@NewAddCourierItemActivity, Locale.getDefault())
                                val addresses: List<Address> = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude,1)
                                addPost(place.latLng.latitude.toString(),place.latLng.longitude.toString(),city,place.address.toString(),addresses[0].adminArea,addresses[0].countryName)



                            })
                            alertDialog.show()

                        }



                    }
                }else{
                    if (!TextUtils.isEmpty(picLat) && !TextUtils.isEmpty(picLng) && openDialog.tv_current_address.text != "Location not found") {
                        // val city = getFullAddress(picLat.toDouble(), picLat.toDouble())
                        for (i in 0..addressListInfo.size - 1){
                            if (addressListInfo[i].cityName!!.contains(city)){
                                tv_pickup_address.text = currentAddress
                                picLat = curLat
                                picAddress = city
                                cityD=city
                                picLng = curLng
                                isSelectAddress=true

                                var localPickUp=""

                                if (!pickupCity.equals("")){
                                    localPickUp=pickupCity

                                }
                                if (pickupCity.equals("")){
                                    cityP=city
                                    picLat = place.latLng.latitude.toString()
                                    tv_pickup_address.text = place.address.toString()

                                    picLng = place.latLng.longitude.toString()
                                    openDialog.dismiss()

                                }else if (!localPickUp.equals(city)&&!pickupCity.equals("")){
                                    Toast.makeText(this@NewAddCourierItemActivity,"Service is not available in selected area",Toast.LENGTH_SHORT).show()
                                }else{
                                    if (pickupCity.equals(city)){
                                        picLat = place.latLng.latitude.toString()
                                        tv_pickup_address.text = place.address.toString()
                                        picLng = place.latLng.longitude.toString()
                                        openDialog.dismiss()
                                    }


                                }
                                openDialog.dismiss()
                                break
                            }else{
                                isSelectAddress=false
                            }
                        }

                        if (isSelectAddress==false){
                            val alertDialog = android.app.AlertDialog.Builder(this@NewAddCourierItemActivity)
                            alertDialog.setTitle("Alert")
                            alertDialog.setCancelable(false)
                            alertDialog.setMessage("Service is not available in selected area")
                            alertDialog.setPositiveButton("Ok", { dialog, which ->
                                alertDialog.setCancelable(true)
                              /*  val geocoder = Geocoder(this@NewAddCourierItemActivity, Locale.getDefault())
                                val addresses: List<Address> = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude,1)
                                addPost(place.latLng.latitude.toString(),place.latLng.longitude.toString(),city,place.address.toString(),addresses[0].subLocality,addresses[0].countryName)
*/
                            })
                            alertDialog.show()



                        }



                    }

                }





            }

            override fun onError(status: Status) {

            }
        })

        openDialog.cancel_action.setOnClickListener {
            openDialog.dismiss()

        }
        openDialog.setOnDismissListener {
            val fm = placePickAddFragment.fragmentManager
            val fragment = fm?.findFragmentById(R.id.placePicker) as PlaceAutocompleteFragment
            val ft = fm.beginTransaction()
            ft.remove(fragment)
            ft.commit()
        }

        openDialog.show()
    }


    private fun selectDeliverAddDialog(view: View) {
        val placePickAddFragment: PlaceAutocompleteFragment?

        val openDialog = Dialog(this, R.style.AppTheme)
        openDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        openDialog.setContentView(R.layout.select_address_dialog)
        openDialog.window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        openDialog.title_dialog.text = resources.getString(R.string.drop_off_address)
        openDialog.current_loc_view.visibility = View.GONE

        val autocompleteFilter: AutocompleteFilter = AutocompleteFilter.Builder()
                .setTypeFilter(Place.TYPE_COUNTRY)
                .build()

        val adAdapter = DeliveryAddressAdapter(delAddress, deliveraddList, object : getAddressListner {
            override fun getAddress(address: String, pickupLat: String, pickupLong: String) {
                tv_delivery_address.text = address
                tv_delivery_address.setTextColor(ContextCompat.getColor(this@NewAddCourierItemActivity, R.color.colorBlack))
                delAddress = address
                delLat = pickupLat
                delLng = pickupLong
                openDialog.dismiss()
            }

        })
        openDialog.recycler_view_address.adapter = adAdapter
        if (deliveraddList.size == 0) {
            getAddressApiDeliver(view, openDialog, adAdapter, deliveraddList)
        }
        getAddressApi(view)



        placePickAddFragment = fragmentManager.findFragmentById(R.id.placePicker) as PlaceAutocompleteFragment
        placePickAddFragment.setFilter(autocompleteFilter)

        placePickAddFragment.setOnPlaceSelectedListener(object : PlaceSelectionListener {
            override fun onPlaceSelected(place: Place) {

                delAddress = place.address.toString()
                deliveryLatLng = place.latLng
                //val city = getFullAddress(place.latLng.latitude, place.latLng.longitude)


                if (deliveryCity.equals("")){

                        val city = getFullAddress(place.latLng.latitude, place.latLng.longitude)
                        for (i in 0..addressListInfo.size - 1){
                            if (addressListInfo[i].cityName!!.equals(city)){
                                tv_delivery_address.text = place.address.toString()
                                delLat= place.latLng.latitude.toString()
                                delLng=place.latLng.longitude.toString()

                                deliveryLat = delLat
                                isSelectdelivery=true
                                deliveryLng = delLng

                                openDialog.dismiss()
                                break
                            }else{
                                isSelectdelivery=false
                            }
                        }

                        if (isSelectdelivery==false){
                            val alertDialog = android.app.AlertDialog.Builder(this@NewAddCourierItemActivity)
                            alertDialog.setTitle("Alert")
                            alertDialog.setCancelable(false)
                            alertDialog.setMessage("Service is not available in selected area")
                            alertDialog.setPositiveButton("Ok", { dialog, which ->
                                alertDialog.setCancelable(true)
                            })
                            alertDialog.show()
                            val geocoder = Geocoder(this@NewAddCourierItemActivity, Locale.getDefault())
                            val addresses: List<Address> = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude,1)
                            addPost(place.latLng.latitude.toString(),place.latLng.longitude.toString(),city,place.address.toString(),addresses[0].adminArea,addresses[0].countryName)

                        }





                }else{

                        for (i in 0..addressListInfo.size - 1){
                            if (addressListInfo[i].cityName!!.contains(city)){
                                tv_delivery_address.text = currentAddress
                                deliveryLat = delLat
                                isSelectdelivery=true
                                deliveryLng = delLng
                                if (deliveryCity.equals("")){
                                    cityD=city
                                    delLat = place.latLng.latitude.toString()
                                    tv_delivery_address.text = place.address.toString()
                                    delLng = place.latLng.longitude.toString()
                                    openDialog.dismiss()
                                }else if (!localDelivery.equals(city)&&!deliveryCity.equals("")){
                                    Toast.makeText(this@NewAddCourierItemActivity,"Service is not available in selected area",Toast.LENGTH_SHORT).show()
                                }else{
                                    if (deliveryCity.equals(city)){
                                        delLat = place.latLng.latitude.toString()
                                        tv_delivery_address.text = place.address.toString()

                                        delLng = place.latLng.longitude.toString()
                                        openDialog.dismiss()
                                    }
                                }
                                openDialog.dismiss()
                                break
                            }else{
                                isSelectdelivery=false
                            }
                        }



                    if (isSelectdelivery==false){
                        val alertDialog = android.app.AlertDialog.Builder(this@NewAddCourierItemActivity)
                        alertDialog.setTitle("Alert")
                        alertDialog.setCancelable(false)
                        alertDialog.setMessage("Service is not available in selected area")
                        alertDialog.setPositiveButton("Ok", { dialog, which ->
                            alertDialog.setCancelable(true)
                        })
                        alertDialog.show()
                        val geocoder = Geocoder(this@NewAddCourierItemActivity, Locale.getDefault())
                        val addresses: List<Address> = geocoder.getFromLocation(place.latLng.latitude, place.latLng.longitude,1)
                        addPost(place.latLng.latitude.toString(),place.latLng.longitude.toString(),city,place.address.toString(),addresses[0].locality,addresses[0].countryName)

                    }

                }




                // tv_delivery_address.text = place.address

/*
                 delLat = place.latLng.latitude.toString()
                 delLng = place.latLng.longitude.toString()
*/









            }

            override fun onError(status: Status) {

            }
        })

        openDialog.cancel_action.setOnClickListener {
            openDialog.dismiss()

        }

        openDialog.setOnDismissListener {

            val fm = placePickAddFragment.fragmentManager
            val fragment = fm?.findFragmentById(R.id.placePicker) as PlaceAutocompleteFragment
            val ft = fm.beginTransaction()
            ft.remove(fragment)
            ft.commit()
        }

        openDialog.show()
    }

    var subLocal=""
    private fun getFullAddress(latitude: Double, longitude: Double): String {
        val geocoder = Geocoder(this@NewAddCourierItemActivity, Locale.getDefault())
        val addresses: List<Address> = geocoder.getFromLocation(latitude, longitude, 1)

        if (addresses[0].subAdminArea!=null){
             city = addresses[0].subAdminArea

            if(addresses[0].subLocality==null&&!addresses[0].subAdminArea.equals(null)) {
                subLocal= addresses[0].subAdminArea
            } else if (addresses[0].subAdminArea==null){
                subLocal =addresses[0].subLocality
            } /*else

            {
                subLocal =addresses[0].subLocality
            }*/

        }else{
            city = addresses[0].locality
            subLocal= addresses[0].locality
        }



        if (false) {
            city = ""
        }

        if (subLocal.equals("")) {
            return city
        } else {
            return subLocal
        }
    }

    private fun getAddressApiPickUp(view: View, openDialog: Dialog, adAdapter: AddressAdapter) {
        if (Constant.isNetworkAvailable(this, view)) {
            // openDialog.progressBar_dialog.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getPreviousPickupAddress +"?"+"type="+"pickup",
                    Response.Listener { response ->
                        //  openDialog.progressBar_dialog.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                val adInfo = gson.fromJson(response, AddressInfoK::class.java)

                                addressList.addAll(adInfo.data!!)
                                adAdapter.notifyDataSetChanged()

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } /*else {
                                        openDialog.noDataTxt.visibility = View.VISIBLE
                                    }*/
                                } /*else {
                                    openDialog.noDataTxt.visibility = View.VISIBLE
                                }*/

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        //  openDialog.progressBar_dialog.visibility = View.GONE
                        progress!!.dismiss()
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {



                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCourierItemActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressApiDeliver(view: View, openDialog: Dialog, adAdapter: DeliveryAddressAdapter, addressList: ArrayList<DeliverAddressInfo.DataBean>) {
        if (Constant.isNetworkAvailable(this, view)) {
            //  openDialog.progressBar_dialog.visibility = View.VISIBLE
            progress!!.show()
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getPreviousPickupAddress +"?"+"type="+"dropup",
                    Response.Listener { response ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                val deliverAddInfo = gson.fromJson(response, DeliverAddressInfo::class.java)

                                addressList.addAll(deliverAddInfo.data!!)
                                adAdapter.notifyDataSetChanged()

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {
                                        openDialog.noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    openDialog.noDataTxt.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
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
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCourierItemActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }


    private fun getAddressApi(view: View) {
        if (Constant.isNetworkAvailable(this, view)) {
            //  openDialog.progressBar_dialog.visibility = View.VISIBLE
            progress!!.show()
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getAddress,
                    Response.Listener { response ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val jsonArray =result.getJSONArray("result")
                                for (i in 0..jsonArray!!.length() - 1){
                                    val jsonObject=jsonArray.getJSONObject(i)
                                    val gson = Gson()
                                    val addressInfo = gson.fromJson(jsonObject.toString(), AddressBean.ResultBean::class.java)
                                    addressListInfo.add(addressInfo)

                                }
/*
                                addressList.addAll(deliverAddInfo.data!!)
                                adAdapter.notifyDataSetChanged()*/

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {

                                        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
                                        //openDialog.noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    Toast.makeText(this,"Service is not available in selected area ",Toast.LENGTH_SHORT).show()
                                    //openDialog.noDataTxt.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
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
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCourierItemActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }


    private fun getAddressApiPickUp(view: View) {
        if (Constant.isNetworkAvailable(this, view)) {
            //  openDialog.progressBar_dialog.visibility = View.VISIBLE
            progress!!.show()
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getAddress,
                    Response.Listener { response ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
                        progress!!.dismiss()
                        addressListInfo.clear()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val jsonArray =result.getJSONArray("result")
                                for (i in 0..jsonArray!!.length() - 1){
                                    val jsonObject=jsonArray.getJSONObject(i)
                                    val gson = Gson()
                                    val addressInfo = gson.fromJson(jsonObject.toString(), AddressBean.ResultBean::class.java)
                                    addressListInfo.add(addressInfo)
                                    //selectAddressDialog(view)

                                }
                                selectAddressDialog(main_layout)
/*
                                addressList.addAll(deliverAddInfo.data!!)
                                adAdapter.notifyDataSetChanged()*/

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {

                                        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
                                        //openDialog.noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    Toast.makeText(this,"Service is not available in selected area ",Toast.LENGTH_SHORT).show()
                                    //openDialog.noDataTxt.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
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
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCourierItemActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    private fun getAddressApiDeliver(view: View) {
        if (Constant.isNetworkAvailable(this, view)) {
            //  openDialog.progressBar_dialog.visibility = View.VISIBLE
            progress!!.show()
            val stringRequest = object : StringRequest(Request.Method.GET, Constant.BASE_URL + Constant.getAddress,
                    Response.Listener { response ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            addressListInfo.clear()
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val jsonArray =result.getJSONArray("result")
                                for (i in 0..jsonArray!!.length() - 1){
                                    val jsonObject=jsonArray.getJSONObject(i)
                                    val gson = Gson()
                                    val addressInfo = gson.fromJson(jsonObject.toString(), AddressBean.ResultBean::class.java)
                                    addressListInfo.add(addressInfo)
                                    //selectAddressDialog(view)

                                }
                                selectDeliverAddDialog(view)

/*
                                addressList.addAll(deliverAddInfo.data!!)
                                adAdapter.notifyDataSetChanged()*/

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this, this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    } else {

                                        Toast.makeText(this,message,Toast.LENGTH_SHORT).show()
                                        //openDialog.noDataTxt.visibility = View.VISIBLE
                                    }
                                } else {
                                    Toast.makeText(this,"Service is not available in selected area ",Toast.LENGTH_SHORT).show()
                                    //openDialog.noDataTxt.visibility = View.VISIBLE
                                }

                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener { error ->
                        // openDialog.progressBar_dialog.visibility = View.GONE
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
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCourierItemActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)

        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }

    // location update.....................................................
    override fun onLocationChanged(p0: Location?) {
        /*delLat = p0?.latitude.toString()
        delLng = p0?.longitude.toString()

        picLat = p0?.latitude.toString()
        picLng = p0?.longitude.toString()*/

        curLat = p0?.latitude.toString()
        curLng = p0?.longitude.toString()

        currentAddress = getCompleteAddressString(p0?.latitude!!, p0.longitude)
        if (mGoogleApiClient!!.isConnected) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)

        }
    }

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
            Toast.makeText(this, "Can't get Address!", Toast.LENGTH_SHORT).show()

        }
        return strAdd
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
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
            }
        } else {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest, this)
        }
    }

    private fun stopLocationUpdates() {
        if (mGoogleApiClient != null && mGoogleApiClient!!.isConnected)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this@NewAddCourierItemActivity)
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
    }


    fun addPost(latitude:String,longitude:String,cityName:String,address:String,state:String,country:String) {
        if (Constant.isNetworkAvailable(this, main_layout)) {
            //  view.progressBar?.visibility = View.VISIBLE
            progress!!.show()

            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.deletePostById,
                    Response.Listener { response ->
                        //  view.progressBar?.visibility = View.GONE
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {

                            } else {
                                val userType = PreferenceConnector.readString(this, PreferenceConnector.USERTYPE, "")
                                if (userType == Constant.COURIOR) {
                                    if (message == "Currently you are inactivate user") {
                                        val helper = HelperClass(this,this)
                                        helper.inActiveByAdmin("Admin inactive your account", true)
                                    }
                                }

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

                                    val helper = HelperClass(this, this)
                                    helper.sessionExpairDialog()

                            }
                        }

                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val param = HashMap<String, String>()
                    param.put("authToken", PreferenceConnector.readString(this@NewAddCourierItemActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return param
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("latitude", latitude)
                    params.put("longitude", longitude)
                    params.put("cityName", cityName)
                    params.put("address", address)
                    params.put("state", state)
                    params.put("country", country)

                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(this).addToRequestQueue(stringRequest)
        } else {
            Toast.makeText(this, getString(R.string.no_internet_connection), Toast.LENGTH_SHORT).show()
        }
    }


}

