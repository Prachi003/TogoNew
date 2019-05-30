package com.togocourier.locationService

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.service.notification.StatusBarNotification
import android.support.v4.app.ActivityCompat
import android.support.v4.app.NotificationCompat
import android.support.v4.content.ContextCompat
import com.facebook.FacebookSdk.getApplicationContext
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.togocourier.R
import com.togocourier.responceBean.LocationInfo
import com.togocourier.ui.activity.HomeActivity
import com.togocourier.util.Constant
import com.togocourier.util.PreferenceConnector
import com.togocourier.util.SessionManager

class AlarmReceiver : BroadcastReceiver(),
        LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    private var sessionManager: SessionManager? = null
    var f_id: String = String()
    /*   private static final long INTERVAL = 10000 * 30;
    private static final long FASTEST_INTERVAL = 3000 * 30;*/


    internal var mLocationRequest: LocationRequest? = null
    internal var mGoogleApiClient: GoogleApiClient? = null
    internal var mCurrentLocation: Location? = null

    internal var mContext: Context? = null

    internal var current_lat: Double = 0.toDouble()
    internal var current_long: Double = 0.toDouble()

    private var mNotifyMgr: NotificationManager? = null

    protected fun createLocationRequest() {
        mLocationRequest = LocationRequest()
        mLocationRequest?.interval = INTERVAL
        mLocationRequest?.fastestInterval = FASTEST_INTERVAL
        mLocationRequest?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    override fun onReceive(context: Context, intent: Intent) {

        mContext = context
        sessionManager = SessionManager(context)
        if (f_id.equals("")) {
            var userId = PreferenceConnector.readString(context, PreferenceConnector.USERID, "")
            f_id = userId
        }
        createLocationRequest()
        mGoogleApiClient = GoogleApiClient.Builder(context)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build()
        // For our recurring task, we'll just display a message
        // Toast.makeText(context, "I'm running", Toast.LENGTH_SHORT).show();
        mGoogleApiClient?.connect()

        mNotifyMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?

        val notifications: Array<StatusBarNotification>
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            assert(mNotifyMgr != null)
            notifications = mNotifyMgr!!.activeNotifications

            if (notifications.isNotEmpty()) {
                for (notification in notifications) {
                    if (notification.id != 101) {
                        // Do something.
                        applyStatusBar("You are on the way", 101)
                    }
                }
            } else {
                applyStatusBar("You are on the way", 101)
            }
        }
    }



    private fun applyStatusBar(iconTitle: String, notificationId: Int) {
        val CHANNEL_ID = "my_channel_01"// The id of the channel.
        val name = "Abc"// The user-visible name of the channel.
        val importance = NotificationManager.IMPORTANCE_HIGH
        var mChannel: NotificationChannel?

        val mBuilder = NotificationCompat.Builder(mContext!!, CHANNEL_ID)
                .setSmallIcon(R.drawable.new_noti_logo_img)
                .setContentTitle(iconTitle)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.new_app_color))

        val resultIntent = Intent(mContext, HomeActivity::class.java)

        val resultPendingIntent = PendingIntent.getActivity(mContext, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT)
        mBuilder.setContentIntent(resultPendingIntent)
        val notification = mBuilder.build()
        notification.flags = notification.flags or (Notification.FLAG_NO_CLEAR or Notification.FLAG_ONGOING_EVENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            assert(mNotifyMgr != null)
            mNotifyMgr!!.createNotificationChannel(mChannel)

        }
        mNotifyMgr!!.notify(notificationId, notification)
    }


    override fun onConnected(bundle: Bundle?) {
        startLocationUpdates()
    }

    protected fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(mContext!!, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext!!, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) return
        val pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this)
    }

    override fun onConnectionSuspended(i: Int) {}

    override fun onConnectionFailed(connectionResult: ConnectionResult) {}

    override fun onLocationChanged(location: Location) {
        mCurrentLocation = location
        if (null != mCurrentLocation) {
            current_lat = mCurrentLocation!!.latitude
            current_long = mCurrentLocation!!.longitude
            // Toast.makeText(mContext, "" + current_lat + "\n" + current_long, Toast.LENGTH_SHORT).show();
            startTrackLocationApi()
            val startPoint = Location("locationA")
            startPoint.latitude = location.latitude
            startPoint.longitude = location.longitude

            current_lat = location.latitude
            current_long = location.longitude

            var locationInfo = LocationInfo()
            locationInfo.firebaseid = f_id
            locationInfo.latitude = current_lat
            locationInfo.longitude = current_long
            locationInfo.lastUpdate = ServerValue.TIMESTAMP

            val database = FirebaseDatabase.getInstance().reference
            database.child(Constant.LOCATION).child(f_id).setValue(locationInfo)


        }

    }


    private fun startTrackLocationApi() {

    }

    companion object {

        private val TAG = "MainActivity"
        /*private val INTERVAL = (1000 * 20).toLong()
        private val FASTEST_INTERVAL = (1000 * 10).toLong()*/

        private val INTERVAL: Long = 1
        private val FASTEST_INTERVAL: Long = 20000
    }

}