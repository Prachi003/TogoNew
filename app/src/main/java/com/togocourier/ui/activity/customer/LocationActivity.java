package com.togocourier.ui.activity.customer;

import android.Manifest;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import com.togocourier.R;
import com.togocourier.responceBean.GetLatLngInfo;
import com.togocourier.responceBean.LocationInfo;
import com.togocourier.responceBean.UserInfo;
import com.togocourier.util.Constant;
import com.togocourier.util.GoogleDirection;
import com.togocourier.util.PreferenceConnector;
import com.togocourier.util.ProgressDialog;
import com.togocourier.vollyemultipart.VolleySingleton;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class LocationActivity extends AppCompatActivity implements LocationListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation;
    private static final String TAG = "MapsActivity";
    private static final long INTERVAL = 1;
    private static final long FASTEST_INTERVAL = 3000;

    /*private static final long INTERVAL = (10000);
    private static final long FASTEST_INTERVAL = (5000);*/

    Marker marker;
    LocationInfo locationInfo;
    UserInfo userInfo;
    String userId = "";
    SupportMapFragment mapFragment;
    private GoogleDirection gd;
    private LatLng start;
    private LatLng end;
    private double courLat, courLong;
    private View markerView;

    public String applyUserId;
    public String profileImage;
    private ProgressDialog progress;
    private TextView tv_distance;

    private boolean isFirstAppear = false;

    private String BASE_URL = "http://togocouriers.com/dev-toogo/index.php/service/";
    public String getLatLongById = "userpost/getLatLongById";
    private String pickupLat="",pickupLong="",deliveryLat="",deliverLong="";

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        isFirstAppear = true;

        progress = new ProgressDialog(this);
        //progress.show();
        tv_distance = findViewById(R.id.tv_distance);
        gd = new GoogleDirection(this);
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        findViewById(R.id.back).setOnClickListener(view -> onBackPressed());

        if (getIntent().getStringExtra("applyUserId") != null) {
            userId = getIntent().getStringExtra("applyUserId");
            profileImage = getIntent().getStringExtra("profileImage");
            pickupLat=getIntent().getStringExtra("pickupLat");
            pickupLong=getIntent().getStringExtra("pickupLong");
            deliveryLat=getIntent().getStringExtra("deliveryLat");
            deliverLong=getIntent().getStringExtra("deliverLong");
            String postId = getIntent().getStringExtra("postId");
            applyUserId = getIntent().getStringExtra("applyUserId");
            getUserInfo(applyUserId);


        }







    }

    private void getLatLongById(String postId, String applyUserId) {
        progress.show();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, BASE_URL + getLatLongById,
                response -> {
                   // progress.dismiss();

                    try {
                        JSONObject result = new JSONObject(response);
                        String status = result.getString("status");
                        String message = result.getString("message");

                        if (status.equals("success")) {
                            isFirstAppear = false;
                            Gson gson = new Gson();
                            GetLatLngInfo getLatLngInfo = gson.fromJson(response, GetLatLngInfo.class);

                            /*courLat = Double.parseDouble(getLatLngInfo.getResult().getLatitude());
                            courLong = Double.parseDouble(getLatLngInfo.getResult().getLongitude());*/

                            markerView = LayoutInflater.from(this).inflate(R.layout.custommarkerlayout, null);
                            DisplayMetrics displayMetrics = new DisplayMetrics();

                            markerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
                            markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
                            markerView.buildDrawingCache();

                            if (!Objects.requireNonNull(Objects.requireNonNull(getLatLngInfo.getResult()).getProfileImage()).isEmpty()) {
                                Picasso.with(this)
                                        .load(getLatLngInfo.getResult().getProfileImage())
                                        .placeholder(R.drawable.new_app_icon1)
                                        .into(markerView.findViewById(R.id.marker_image), new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                setCustomMarker();
                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        });
                            } else {
                                Picasso.with(this)
                                        .load(R.drawable.new_app_icon1)
                                        .into(markerView.findViewById(R.id.marker_image), new Callback() {
                                            @Override
                                            public void onSuccess() {
                                                setCustomMarker();
                                            }

                                            @Override
                                            public void onError() {

                                            }
                                        });
                            }


                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }, error -> {

        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> header = new HashMap<>();
                header.put("authToken", PreferenceConnector.INSTANCE.readString(LocationActivity.this, PreferenceConnector.INSTANCE.getUSERAUTHTOKEN(), ""));
                return header;
            }

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> params = new HashMap<>();
                params.put("postId", postId);
                params.put("applyUserId", applyUserId);
                return params;
            }
        };

        stringRequest.setRetryPolicy(new DefaultRetryPolicy(20000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.Companion.getInstance(this).addToRequestQueue(stringRequest);

    }

    private void setCustomMarker() {

     /*   markerView = LayoutInflater.from(this).inflate(R.layout.custommarkerlayout, null);
        DisplayMetrics displayMetrics = new DisplayMetrics();
        markerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        markerView.buildDrawingCache();*/
        Bitmap finalBitmap = Bitmap.createBitmap(markerView.getMeasuredWidth(), markerView.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(finalBitmap);
        markerView.draw(canvas);

        //update views
        LatLng point;
        double newLat = Double.parseDouble(String.valueOf(courLat)) + (Math.random() - .5) / 1500;
        double newLng = Double.parseDouble(String.valueOf(courLong)) + (Math.random() - .5) / 1500;
        point = new LatLng(newLat, newLng);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(point);

        marker = mMap.addMarker(markerOptions.icon(BitmapDescriptorFactory.fromBitmap(finalBitmap)));
        //   pointToPosition(point);

        LatLng pickLatLng = new LatLng(Double.parseDouble(pickupLat), Double.parseDouble(pickupLong));
        LatLng deliveryLatLng = new LatLng(Double.parseDouble(deliveryLat), Double.parseDouble(deliverLong));

        start = pickLatLng;
        end = deliveryLatLng;

        mMap.addMarker(new MarkerOptions().position(start).icon(BitmapDescriptorFactory.fromResource(R.drawable.new_add_blue_pickup_ico)));
        mMap.addMarker(new MarkerOptions().position(end).icon(BitmapDescriptorFactory.fromResource(R.drawable.new_add_blue_dot_ico)));

        PolylineOptions options = new PolylineOptions();
        options.color(ContextCompat.getColor(LocationActivity.this, R.color.new_app_color));
        options.width(5f);

        String url = getUrl(start, end);
        FetchUrl fetchUrl = new FetchUrl();
        fetchUrl.execute(url + "&key=" + getResources().getString(R.string.google_maps_key));

        pointToPosition(point);

    }

    void getLocationFromFCM(String userId) {

        FirebaseDatabase.getInstance().getReference().child("location").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(LocationInfo.class) != null)

                    locationInfo = dataSnapshot.getValue(LocationInfo.class);

                //  end = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());

                if (locationInfo != null && locationInfo.getLatitude() != null) {
                    courLat = locationInfo.getLatitude();
                    courLong = locationInfo.getLongitude();
                    if (isFirstAppear) {
                        if (marker == null) {
                            //mapFragment.getMapAsync(LocationActivity.this);
                            markerView = LayoutInflater.from(LocationActivity.this).inflate(R.layout.custommarkerlayout, null);
                            DisplayMetrics displayMetrics = new DisplayMetrics();

                            markerView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                            markerView.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
                            markerView.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
                            markerView.buildDrawingCache();


                            Picasso.with(LocationActivity.this)
                                    .load(profileImage)
                                    .placeholder(R.drawable.new_app_icon1)
                                    .into(markerView.findViewById(R.id.marker_image), new Callback() {
                                        @Override
                                        public void onSuccess() {
                                            setCustomMarker();
                                        }

                                        @Override
                                        public void onError() {

                                        }
                                    });


                            //setCustomMarker();
                        }
                    }

                    animateMarker(locationInfo.getLatitude(), locationInfo.getLongitude(), marker);
                }

          /*      if (marker == null)
                    mapFragment.getMapAsync(LocationActivity.this);*/




            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    /*    FirebaseDatabase.getInstance().getReference().child("location").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(LocationInfo.class) != null)

                    locationInfo = dataSnapshot.getValue(LocationInfo.class);

                //  end = new LatLng(locationInfo.getLatitude(), locationInfo.getLongitude());

                if (locationInfo != null && locationInfo.getLatitude() != null) {
                    courLat = locationInfo.getLatitude();
                    courLong = locationInfo.getLongitude();
                    // updateCourierMarker();

                    if (isFirstAppear) {
                        setCustomMarker();
                        *//*if (marker == null) {
                            getLatLongById(postId, applyUserId);
                        }*//*
                    }

                    animateMarker(locationInfo.getLatitude(), locationInfo.getLongitude(), marker);
                }

                if (marker == null)
                    mapFragment.getMapAsync(LocationActivity.this);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });*/
    }

    void getUserInfo(final String userId) {
        FirebaseDatabase.getInstance().getReference().child("users").child(userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.getValue(UserInfo.class) != null) {
                    userInfo = dataSnapshot.getValue(UserInfo.class);
                    getLocationFromFCM(userId);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }


    @Override
    public void onStart() {
        super.onStart();
        Log.e(TAG, "onStart fired ..............");
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.e(TAG, "onStop fired ..............");

    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e(TAG, "onConnected - isConnected ...............: " + mGoogleApiClient.isConnected());
        startLocationUpdates();

    }

    protected void startLocationUpdates() {

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, LocationActivity.this);
        Log.e(TAG, "Location update started ..............: ");
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == 1) {
            mGoogleApiClient.connect();
        }

    }

    @Override
    public void onLocationChanged(Location location) {
        /*animateMarker(location, marker);*/
    }


    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e(TAG, "On Pause call...............................");

    }

    protected void stopLocationUpdates() {

       /* LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
        Log.e(TAG, "Location update stopped .......................");*/
    }

    @Override
    public void onResume() {
        super.onResume();

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }

    }

    public void animateMarker(double latitute, double longitude, final Marker marker) {
        if (marker != null) {
            final LatLng startPosition = marker.getPosition();
            final LatLng endPosition = new LatLng(latitute, longitude);

            final float startRotation = marker.getRotation();

            final LatLngInterpolator latLngInterpolator = new LatLngInterpolator.LinearFixed();
            ValueAnimator valueAnimator = ValueAnimator.ofFloat(0, 1);
            valueAnimator.setDuration(1000); // duration 1 second
            valueAnimator.setInterpolator(new LinearInterpolator());
            valueAnimator.addUpdateListener(animation -> {
                try {
                    float v = animation.getAnimatedFraction();
                    LatLng newPosition = latLngInterpolator.interpolate(v, startPosition, endPosition);
                    marker.setPosition(newPosition);
                    //marker.setRotation(computeRotation(v, startRotation, destination.getBearing()));
                } catch (Exception ex) {

                }
            });

            valueAnimator.start();
        }
    }


    private interface LatLngInterpolator {
        LatLng interpolate(float fraction, LatLng a, LatLng b);

        class LinearFixed implements LatLngInterpolator {
            @Override
            public LatLng interpolate(float fraction, LatLng a, LatLng b) {
                double lat = (b.latitude - a.latitude) * fraction + a.latitude;
                double lngDelta = b.longitude - a.longitude;
                // Take the shortest path across the 180th meridian.
                if (Math.abs(lngDelta) > 180) {
                    lngDelta -= Math.signum(lngDelta) * 360;
                }
                double lng = lngDelta * fraction + a.longitude;
                return new LatLng(lat, lng);
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(false);

        //setCustomMarker();

        /*if (marker == null) {
            getLatLongById(postId, applyUserId);
        }*/
    }

    private void pointToPosition(LatLng position) {
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(position)
                .zoom(12f).build();
        //Zoom in and animate the camera.
        mMap.moveCamera(CameraUpdateFactory.newLatLng(position));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        progress.dismiss();
    }

    private String getUrl(LatLng origin, LatLng dest) {
        String sOrigin = "origin=" + origin.latitude + "," + origin.longitude;
        String sDest = "destination=" + dest.latitude + "," + dest.longitude;
        String sensor = "sensor=false";
        String mode = "mode=driving";
        String alternative = "alternatives=true";
        String params = sOrigin + "&" + sDest + "&" + sensor + "&" + mode + "&" + alternative;

        return "https://maps.googleapis.com/maps/api/directions/json?" + params;
    }

    /**
     * A method to download json data from url
     */
    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);

            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();

            // Connecting to url
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

            StringBuilder sb = new StringBuilder();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            Log.d("downloadUrl", data);
            br.close();

        } catch (Exception e) {
            Log.d("Exception", e.toString());
        } finally {
            assert iStream != null;
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    // Fetches data from url passed
    @SuppressLint("StaticFieldLeak")
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();

            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);

        }
    }

    /*....................................................*/
    // Fetches data from url passed
    @SuppressLint("StaticFieldLeak")
    private class DownloadTask extends AsyncTask<String, Void, String> {

        // Downloading data in non-ui thread
        @Override
        protected String doInBackground(String... url) {

            // For storing data from web service

            String data = "";

            try {
                // Fetching the data from web service
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        // Executes in UI thread, after the execution of
        // doInBackground()
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    @SuppressLint("StaticFieldLeak")
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);
                JDataParser parser = new JDataParser(0);
                // Starts parsing data
                routes = parser.parse(jObject);

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {

                    HashMap<String, String> point = path.get(j);

                    if (point.get("distance") != null) {
                        String distance = point.get("distance");

                        if (distance.contains("mi")) {
                            String st = distance.replace("mi", "");
                            tv_distance.setText(String.format("Distance: %.2f", Double.parseDouble(st)) + " miles");
                        } else if (distance.contains("ft")) {
                            String st = distance.replace("ft", "");
                            tv_distance.setText(String.format("Distance: %.2f", Double.parseDouble(st) * 0.000189394) + " miles");
                        }else {
                            String st = distance.replace("km", "");
                            tv_distance.setText(String.format("Distance: %.2f", Double.parseDouble(st)) + " miles");



                        }
                    }

                    if (point.get("lat") != null) {
                        double lat = Double.parseDouble(point.get("lat"));
                        double lng = Double.parseDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);
                        points.add(position);
                    }
                }

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(5f);
                lineOptions.color(ContextCompat.getColor(LocationActivity.this, R.color.new_app_color));

                Log.d("onPostExecute", "onPostExecute lineoptions decoded");

            }

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
                mMap.addPolyline(lineOptions);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }

}
