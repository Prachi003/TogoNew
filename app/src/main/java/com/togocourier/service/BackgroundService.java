package com.togocourier.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.service.notification.StatusBarNotification;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.togocourier.R;
import com.togocourier.responceBean.LocationInfo;
import com.togocourier.ui.activity.HomeActivity;
import com.togocourier.util.Constant;
import com.togocourier.util.PreferenceConnector;

public class BackgroundService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private LocationListener mLocationListener;
    private LocationManager mLocationManager;
    private NotificationManager mNotifyMgr;

    final private static String PRIMARY_CHANNEL = "default";

    private final int LOCATION_INTERVAL = 1000;
    private final float LOCATION_DISTANCE = 10f;

    private BackgroundService gpsService;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    private class LocationListener implements android.location.LocationListener {
        private Location mLastLocation;

        LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            String f_id = PreferenceConnector.INSTANCE.readString(getBaseContext(), PreferenceConnector.INSTANCE.getUSERID(), "");

            if (!f_id.equals("")) {
                LocationInfo locationInfo = new LocationInfo();
                locationInfo.setFirebaseid(f_id);
                locationInfo.setLatitude(location.getLatitude());
                locationInfo.setLongitude(location.getLongitude());
                locationInfo.setLastUpdate(ServerValue.TIMESTAMP);

                FirebaseDatabase.getInstance().getReference().child(Constant.INSTANCE.getLOCATION()).child(f_id).setValue(locationInfo);
            }
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

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForeground(1, getNotification());
        }*/
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocationManager != null) {
            try {
                mLocationManager.removeUpdates(mLocationListener);
            } catch (Exception ex) {
                // Log.i(TAG, "fail to remove location listners, ignore", ex);
            }
        }
    }

    private void initializeLocationManager() {
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        initializeLocationManager();
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener);

        } catch (java.lang.SecurityException ex) {
            // Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            // Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

        openNotification();


    }

    public void stopTracking() {
        if (mNotifyMgr != null) {
            mNotifyMgr.cancel(101);
        }
        this.onDestroy();
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
       //mNotifyMgr.notify(notificationId, notification);

        startForeground(1, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification getNotification() {

        NotificationChannel channel = new NotificationChannel(PRIMARY_CHANNEL, "My Channel", NotificationManager.IMPORTANCE_DEFAULT);

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);

        Notification.Builder builder = new Notification.Builder(getApplicationContext(), "channel_01")
                .setAutoCancel(true);
        return builder.build();
    }

    public class LocationServiceBinder extends Binder {
        public BackgroundService getService() {
            return BackgroundService.this;
        }
    }

}
