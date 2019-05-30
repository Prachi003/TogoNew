package com.togocourier.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.togocourier.R;
import com.togocourier.responceBean.LocationInfo;
import com.togocourier.ui.activity.HomeActivity;
import com.togocourier.util.Constant;
import com.togocourier.util.PreferenceConnector;
import com.togocourier.util.SessionManager;

import java.util.HashMap;
import java.util.Map;

public class MyLocationService extends Service {
    private static final String TAG = "TOGO";
    private LocationManager mLocationManager = null;
    private static final int LOCATION_INTERVAL = 1000; //10sec
    private static final float LOCATION_DISTANCE = 10f; //50m
    NotificationManager mNotifyMgr;

    private class LocationListener implements android.location.LocationListener {
        Location mLastLocation;

        LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            // updateLocationToServer(location.getLatitude(), location.getLongitude());

             /*locationInfo.setLatitude(Double.parseDouble(new DecimalFormat("##.#####").format(location.getLatitude())));
            locationInfo.setLongitude(Double.parseDouble(new DecimalFormat("##.#####").format(location.getLongitude())));*/

            String f_id = PreferenceConnector.INSTANCE.readString(getBaseContext(), PreferenceConnector.INSTANCE.getUSERID(), "");

            LocationInfo locationInfo = new LocationInfo();
            locationInfo.setFirebaseid(f_id);
            locationInfo.setLatitude(location.getLatitude());
            locationInfo.setLongitude(location.getLongitude());
            locationInfo.setLastUpdate(ServerValue.TIMESTAMP);

            FirebaseDatabase.getInstance().getReference().child(Constant.INSTANCE.getLOCATION()).child(f_id).setValue(locationInfo);

            mLastLocation.set(location);
        }

        @Override
        public void onProviderDisabled(String provider) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }
    }

    LocationListener[] mLocationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        super.onStartCommand(intent, flags, startId);

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


        return START_STICKY;
    }

    @Override
    public void onCreate() {
        initializeLocationManager();

        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[1]);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        try {
            mLocationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE,
                    mLocationListeners[0]);
        } catch (SecurityException ex) {
            ex.printStackTrace();
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            for (LocationListener mLocationListener : mLocationListeners) {
                try {
                    mLocationManager.removeUpdates(mLocationListener);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        mNotifyMgr.cancel(101);
    }

    private void initializeLocationManager() {

        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
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


    private void updateLocationToServer(final double lat, final double lng) {
        if (isConnectingToInternet(getApplicationContext())) {
            StringRequest stringRequest = new StringRequest(Request.Method.POST, "http://togocouriers.com/index.php/service/userpost/updateLatLong",
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
                    }) {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> headers = new HashMap<>();
                    SessionManager sessionManager = new SessionManager(getApplicationContext());
                    headers.put("authToken", sessionManager.getAUTH_TOKEN());
                    return headers;
                }

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();
                    params.put("latitude", String.valueOf(lat));
                    params.put("longitude", String.valueOf(lng));
                    return params;
                }
            };
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);

        } else {

        }
    }

    public static boolean isConnectingToInternet(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] info = connectivity.getAllNetworkInfo();
            if (info != null)

                for (int i = 0; i < info.length; i++)

                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        return true;
                    }
        }

        return false;
    }
}



























