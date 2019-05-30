package com.togocourier.service;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.service.notification.StatusBarNotification;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.togocourier.R;
import com.togocourier.responceBean.LocationInfo;
import com.togocourier.ui.activity.HomeActivity;
import com.togocourier.util.Constant;
import com.togocourier.util.PreferenceConnector;

/**
 * Created by mindiii on 25/10/18.
 */

public class MyJobLocationService extends JobService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener {

    // Get Current Location
    private Location mLastLocation;
    // Google client to interact with Google API
    private GoogleApiClient mGoogleApiClient;

    private LocationRequest mLocationRequest;
    private LocationManager locationManager;
    private NotificationManager mNotifyMgr;

    private static final int LOCATION_INTERVAL = 1000; //10sec
    private static final float LOCATION_DISTANCE = 10f; //50m

    @Override
    public void onLocationChanged(Location location) {
        String f_id = PreferenceConnector.INSTANCE.readString(getBaseContext(), PreferenceConnector.INSTANCE.getUSERID(), "");

       if(!f_id.equals("")){
           LocationInfo locationInfo = new LocationInfo();
           locationInfo.setFirebaseid(f_id);
           locationInfo.setLatitude(location.getLatitude());
           locationInfo.setLongitude(location.getLongitude());
           locationInfo.setLastUpdate(ServerValue.TIMESTAMP);

           FirebaseDatabase.getInstance().getReference().child(Constant.INSTANCE.getLOCATION()).child(f_id).setValue(locationInfo);
       }

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public boolean onStartJob(JobParameters job) {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            assert locationManager != null;
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    this);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    this);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        // Building the GoogleApi client
        buildGoogleApiClient();
        createLocationRequest();

        openNotification();

        return false;
    }

    private void openNotification() {
        mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        StatusBarNotification[] notifications;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            assert mNotifyMgr != null;
            notifications = mNotifyMgr.getActiveNotifications();

            if (notifications.length > 0) {
                for (StatusBarNotification notification : notifications) {
                    if (notification.getId() != 101) {
                        // Do something.
                        applyStatusBar("You are on the way", 101);
                    }
                }
            } else {
                applyStatusBar("You are on the way", 101);
            }
        }
    }

    private void applyStatusBar(String iconTitle, int notificationId) {
        String CHANNEL_ID = "my_channel_01";// The id of the channel.
        CharSequence name = "Abc";// The user-visible name of the channel.
        int importance = NotificationManager.IMPORTANCE_HIGH;
        NotificationChannel mChannel = null;

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.new_noti_logo_img)
                .setContentTitle(iconTitle)
                .setColor(ContextCompat.getColor(getApplicationContext(), R.color.new_app_color));

        Intent resultIntent = new Intent(this, HomeActivity.class);

        PendingIntent resultPendingIntent = PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        Notification notification = mBuilder.build();
        notification.flags |= Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            assert mNotifyMgr != null;
            mNotifyMgr.createNotificationChannel(mChannel);

        }
        mNotifyMgr.notify(notificationId, notification);
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        mNotifyMgr.cancel(101);
        return true;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }


    /**
     * Creating google api client object
     */
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
    }

    /**
     * Creating location request object
     */
    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(LOCATION_INTERVAL);
        mLocationRequest.setFastestInterval(LOCATION_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(LOCATION_DISTANCE);
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);
    }


    private void stopLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (com.google.android.gms.location.LocationListener) this);
    }
}
