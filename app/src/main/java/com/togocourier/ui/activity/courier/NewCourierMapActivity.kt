package com.togocourier.ui.activity.courier

import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.os.SystemClock
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.togocourier.R
import com.togocourier.util.AddressLocationTask
import kotlinx.android.synthetic.main.new_activity_courier_map.*
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.util.ArrayList
import java.util.HashMap

class NewCourierMapActivity : AppCompatActivity(), OnMapReadyCallback, View.OnClickListener {
    private var mMap: GoogleMap? = null

    private var pickUpLat: String = ""
    private var pickUpLng: String = ""
    private var deliveryLat: String = ""
    private var deliveryLng: String = ""

    // variable to track event time
    private var mLastClickTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.new_activity_courier_map)

        pickUpLat = intent.getStringExtra("pickUpLat")
        pickUpLng = intent.getStringExtra("pickUpLng")
        deliveryLat = intent.getStringExtra("deliveryLat")
        deliveryLng = intent.getStringExtra("deliveryLng")

        // calculateDistance()

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        iv_back.setOnClickListener(this)
    }

    private fun calculateDistance() {
        val loc1 = Location("")
        loc1.latitude = pickUpLat.toDouble()
        loc1.longitude = pickUpLng.toDouble()

        val loc2 = Location("")
        loc2.latitude = deliveryLat.toDouble()
        loc2.longitude = deliveryLng.toDouble()

        val distanceInMeters = loc1.distanceTo(loc2)
        tv_distance.text = "Distance:  %.2f".format((distanceInMeters * 0.000621).toString().toDouble()) + " miles"

        /* val result = FloatArray(1)
         Location.distanceBetween(pickUpLat.toDouble(), pickUpLng.toDouble(), deliveryLat.toDouble(), deliveryLng.toDouble(), result)
         tv_distance.text = "Distance:  %.2f".format((result[0]) * 0.000621371) + " miles"*/
    }

    override fun onClick(view: View) {
        // Preventing multiple clicks, using threshold of 1/2 second
        if (SystemClock.elapsedRealtime() - mLastClickTime < 500) {
            return
        }
        mLastClickTime = SystemClock.elapsedRealtime()

        when (view.id) {
            R.id.iv_back -> {
                onBackPressed()
            }
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
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(12f))
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
                val data1 = AddressLocationTask(this@NewCourierMapActivity, url[0], AddressLocationTask.AddressLocationListner { result ->
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

                points = ArrayList()
                lineOptions = PolylineOptions()

                // Fetching i-th route
                val path = result[i]

                // Fetching all the points in i-th route
                for (j in path.indices) {
                    val point = path[j]

                    if (point["distance"] != null) {
                        val distance = point["distance"]
                        /*val str = distance!!.replace("mi", "")
                        tv_distance.text = "Distance:  %.2f".format(str.toDouble()) + " miles"*/

                        if (distance!!.contains("mi")) {
                            val str = distance.replace("mi", "")
                            tv_distance.text = "Distance:  %.2f".format(str.toDouble()) + " miles"
                        } else if (distance.contains("ft")) {
                            val str = distance.replace("ft", "")
                            tv_distance.text = "Distance:  %.2f".format(str.toDouble()*0.000189394) + " miles"
                        }else{
                            val str = distance.replace("km", "")

                            tv_distance.text = "Distance:  %.2f".format(str.toDouble()) + " miles"

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
                lineOptions.color(ContextCompat.getColor(this@NewCourierMapActivity, R.color.new_app_color))
            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap!!.addPolyline(lineOptions)
            }
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