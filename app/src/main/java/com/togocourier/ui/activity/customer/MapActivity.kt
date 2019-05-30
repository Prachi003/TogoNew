package com.togocourier.ui.activity.customer

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.app.FragmentActivity
import android.support.v4.content.ContextCompat
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.Toast
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.StringRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.togocourier.R
import com.togocourier.responceBean.GetLatLngInfo
import com.togocourier.util.AddressLocationTask
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.ProgressDialog
import com.togocourier.vollyemultipart.VolleySingleton
import kotlinx.android.synthetic.main.activity_map.*
import kotlinx.android.synthetic.main.custommarkerlayout.view.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.HashMap
import kotlin.collections.ArrayList
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableMap
import kotlin.collections.indices

class MapActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
    private val latlngs = ArrayList<LatLng>()
    private var getLatLngInfo = GetLatLngInfo().result
    private var postId = ""
    private var applyUserId = ""
    private var progress: ProgressDialog? = null

    private var pickUpLat: String = ""
    private var pickUpLng: String = ""
    private var deliveryLat: String = ""
    private var deliveryLng: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        progress = ProgressDialog(this)

        postId = intent.getStringExtra("postId")
        applyUserId = intent.getStringExtra("applyUserId")
        getLatLongById(postId, applyUserId)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        back.setOnClickListener {
            onBackPressed()
        }

    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap
    }

    private fun getLatLongById(postId: String, applyUserId: String) {
        if (Constant.isNetworkAvailable(this, mainLayout)) {
            progress!!.show()
            val stringRequest = object : StringRequest(Request.Method.POST, Constant.BASE_URL + Constant.getLatLongById,
                    Response.Listener { response ->
                        progress!!.dismiss()

                        val result: JSONObject?
                        try {
                            result = JSONObject(response)
                            val status = result.getString("status")
                            val message = result.getString("message")
                            if (status == "success") {
                                val gson = Gson()
                                val getLatLngInfo = gson.fromJson(response, GetLatLngInfo::class.java)

                                val myLatitude: Double = getLatLngInfo?.result?.latitude?.toDouble()!!
                                val myLongitude: Double = getLatLngInfo.result?.longitude?.toDouble()!!

                                val markerView = (this.getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater).inflate(R.layout.custommarkerlayout, null)
                                val displayMetrics = DisplayMetrics()
                                markerView.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT)
                                markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels)
                                markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels)
                                markerView.buildDrawingCache()

                                if (!getLatLngInfo.result!!.profileImage?.equals("")!!) {
                                    Picasso.with(this)
                                            .load(getLatLngInfo.result!!.profileImage).placeholder(R.drawable.new_app_icon1)
                                            .into(markerView.marker_image, object : com.squareup.picasso.Callback {
                                                override fun onSuccess() {

                                                    val finalBitmap = Bitmap.createBitmap(markerView.measuredWidth, markerView.measuredHeight, Bitmap.Config.ARGB_8888)
                                                    val canvas = Canvas(finalBitmap)
                                                    markerView.draw(canvas)

                                                    // update views
                                                    val point: LatLng
                                                    val newLat = java.lang.Double.parseDouble(myLatitude.toString()) + (Math.random() - .5) / 1500// * (Math.random() * (max - min) + min);
                                                    val newLng = java.lang.Double.parseDouble(myLongitude.toString()) + (Math.random() - .5) / 1500// * (Math.random() * (max - min) + min);
                                                    point = LatLng(newLat, newLng)
                                                    // point = new LatLng(Double.parseDouble(mapBean.latitude), Double.parseDouble(mapBean.longitude));
                                                    // Creating an instance of MarkerOptions
                                                    val markerOptions = MarkerOptions()
                                                    markerOptions.position(point)

                                                    markerOptions.snippet("meri location")
                                                    mMap?.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalBitmap)))
                                                }

                                                override fun onError() {
                                                }
                                            })
                                }


                                /* mMap?.addMarker(MarkerOptions().position(pickUpMarker).title(pickupAdrs).icon(BitmapDescriptorFactory.fromResource(R.drawable.sorce_map)));
                                 mMap?.addMarker(MarkerOptions().position(dropMarker).title(deliveryAdrs).icon(BitmapDescriptorFactory.fromResource(R.drawable.destination_map)));

                                 mMap?.moveCamera(CameraUpdateFactory.newLatLng(currentLocation))

                                 mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 10f))*/

                                val pickUp = LatLng(getLatLngInfo.result!!.pickupLat!!.toDouble(), getLatLngInfo.result!!.pickupLong!!.toDouble())
                                val delivery = LatLng(getLatLngInfo.result!!.deliverLat!!.toDouble(), getLatLngInfo.result!!.deliverLong!!.toDouble())
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
                                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12f))

                                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                    //return
                                }
                                mMap?.isMyLocationEnabled = true
                                mMap?.uiSettings?.setAllGesturesEnabled(true)
                                mMap?.uiSettings?.isMyLocationButtonEnabled = true
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                    if (ContextCompat.checkSelfPermission(this,
                                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                        mMap?.isMyLocationEnabled = true
                                    }
                                } else {
                                    mMap?.isMyLocationEnabled = true
                                }

                            } else {
                                Constant.snackbar(mainLayout, message)
                            }
                        } catch (e: JSONException) {
                            e.printStackTrace()
                        }
                    },
                    Response.ErrorListener {
                        progress!!.dismiss()
                        Toast.makeText(this, "Something went wrong, please check after some time.", Toast.LENGTH_LONG).show()
                    }) {

                override fun getHeaders(): MutableMap<String, String> {
                    val header = HashMap<String, String>()
                    header.put("authToken", PreferenceConnector.readString(this@MapActivity, PreferenceConnector.USERAUTHTOKEN, ""))
                    return header
                }

                override fun getParams(): Map<String, String> {
                    val params = HashMap<String, String>()
                    params.put("postId", postId)
                    params.put("applyUserId", applyUserId)
                    return params
                }
            }
            stringRequest.retryPolicy = DefaultRetryPolicy(
                    20000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT)
            VolleySingleton.getInstance(baseContext).addToRequestQueue(stringRequest)
        }
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
                val data1 = AddressLocationTask(this@MapActivity, url[0], AddressLocationTask.AddressLocationListner { result ->
                    data = result.toString()

                    val parserTask = ParserTask()
                    parserTask.execute(data)
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
            var routes: List<List<HashMap<String, String>>> = java.util.ArrayList<java.util.ArrayList<HashMap<String, String>>>()

            try {
                jObject = JSONObject(jsonData[0])
                val parser = MapDataParser()
                routes = parser.parse(jObject)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return routes
        }

        // Executes in UI thread, after the parsing process
        override fun onPostExecute(result: List<List<HashMap<String, String>>>) {
            var points: java.util.ArrayList<LatLng>
            var lineOptions: PolylineOptions? = null

            for (i in result.indices) {

                points = java.util.ArrayList()
                lineOptions = PolylineOptions()

                // Fetching i-th route
                val path = result[i]

                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]

                    if (point["distance"] != null) {
                        val distance = point["distance"]

                        if (distance!!.contains("mi")) {
                            val str = distance.replace("mi", "")
                            tv_distance.text = "Distance:  %.2f".format(str.toDouble()) + " miles"
                        } else if (distance.contains("ft")) {
                            val str = distance.replace("ft", "")
                            tv_distance.text = "Distance:  %.2f".format(str.toDouble()*0.000189394) + " miles"
                        }
                    }

                    if (point["lat"] != null) {
                        val lat = java.lang.Double.parseDouble(point["lat"])
                        val lng = java.lang.Double.parseDouble(point["lng"])
                        val position = LatLng(lat, lng)

                        points.add(position)
                    }
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points)
                lineOptions.width(5f)
                lineOptions.color(ContextCompat.getColor(this@MapActivity, R.color.new_app_color))
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap!!.addPolyline(lineOptions)
            }
        }
    }
}

class MapDataParser {
    /** Receives a JSONObject and returns a list of lists containing latitude and longitude  */
    fun parse(jObject: JSONObject): List<List<HashMap<String, String>>> {

        val routes = java.util.ArrayList<List<HashMap<String, String>>>()
        val jRoutes: JSONArray
        var jLegs: JSONArray
        var jSteps: JSONArray

        try {
            jRoutes = jObject.getJSONArray("routes")

            /** Traversing all routes  */
            for (i in 0 until jRoutes.length()) {
                jLegs = (jRoutes.get(0) as JSONObject).getJSONArray("legs")

                val path = java.util.ArrayList<HashMap<String, String>>()

                /** Traversing all legs  */
                for (j in 0 until jLegs.length()) {

                    var jo: JSONObject = (jLegs.get(j) as JSONObject).get("distance") as JSONObject
                    val hmDistance = HashMap<String, String>()
                    hmDistance.put("distance", jo.get("text").toString())

                    path.add(hmDistance)

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

        val poly = java.util.ArrayList<LatLng>()
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
