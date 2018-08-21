package com.example.ehabs.googlemaps;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnCameraIdleListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.location.Location;

import org.json.JSONArray;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;


import android.location.LocationManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.VisibleRegion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.io.StringReader;
import java.security.spec.ECField;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraMoveListener,
        GoogleMap.OnCameraMoveCanceledListener,
        GoogleMap.OnCameraIdleListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowCloseListener {

    private static final int REQUEST_LOCATION = 0;
    // here are all buttons and initialized variables
    private CountDownTimer countDownTimer;
    private CountDownTimer updateDownTimer;
    LatLng centerMap;
    String dontmarker;
    boolean first = false;
    private GoogleMap mMap;
    private GoogleApiClient client;
    private LocationRequest locationRequest;
    public Location lastLocation;
    private Marker currentLocationMarker;
    TextView txtview;
    TextView print;
    Button breq, search;
    ToggleButton tracking;
    Button gpsview;
    boolean trackison = false;
    boolean track = false;
    boolean infowindowopen = false;
    boolean gpsviewbool;
    boolean zoom = false;
    ToggleButton stationupdates;
    boolean updateflag = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //initialize buttons to layout
        breq = (Button) findViewById(R.id.button2);
        search = (Button) findViewById(R.id.search);
        tracking = (ToggleButton) findViewById(R.id.toggleButton);
        gpsview = (Button) findViewById(R.id.view);
        stationupdates = (ToggleButton) findViewById(R.id.stationUpdates);


        SharedPreferences prefs = getSharedPreferences("sharedPrefName", Context.MODE_PRIVATE);
        trackison = prefs.getBoolean("trackison", false);
        if (trackison == true) {
            track = true;

        }
        tracking.setChecked(trackison);


        search.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                specialrequest();
            }
        });

        tracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences prefs = getSharedPreferences("sharedPrefName", Context.MODE_PRIVATE);
                trackison = prefs.getBoolean("trackison", false);
                if (isChecked) {
                    // The toggle is eniabled
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("trackison", true);
                    editor.commit();
                    track = true;
                    trackison = true;
                } else {
                    // The toggle is disabled
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("trackison", false);
                    editor.commit();
                    if(countDownTimer!=null) {
                        countDownTimer.cancel();
                    }
                    track = false;
                    trackison = false;
                }
            }
        });

        stationupdates.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    // The toggle is eniabled
                    updateflag = true;

                } else {
                    // The toggle is disabled
                    mMap.clear();
                    updateflag = false;
                    if(updateDownTimer !=null) {
                        updateDownTimer.cancel();
                    }


                }
            }
        });
        gpsview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences prefs = getSharedPreferences("sharedPrefName", Context.MODE_PRIVATE);
                gpsviewbool = prefs.getBoolean("gpsviewbool", false);

                if (gpsviewbool == false) {


                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("gpsviewbool", true);
                    editor.commit();
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                } else {

                    // The toggle is disabled

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("gpsviewbool", false);
                    editor.commit();
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                }
            }
        });

        final Context context = this;
        breq.setOnClickListener(new View.OnClickListener() {


            @Override
            public void onClick(View view) {
                LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

                boolean check = checkgps();


                doRequest(100);
                zoom = true;

            }
        });

    }


    // finds stations within 100 miles radius
    private void doRequest(int radius) {
        centerMap = mMap.getCameraPosition().target;
        mMap.clear();


        String URL_POST = "http://webapp.transflodev.com/svc1.transflomobile.com/api/v3/stations/" + Integer.toString(radius);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_POST, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
//                Toast.makeText(getApplication(), response, Toast.LENGTH_SHORT).show();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("truckStops");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject truckstops = jsonArray.getJSONObject(i);

                        double latn = truckstops.getDouble("lat");
                        double lngn = truckstops.getDouble("lng");
                        String name = truckstops.getString("name");
                        String city = truckstops.getString("city");
                        String State = truckstops.getString("state");
                        String raw1 = truckstops.getString("rawLine1");
                        String raw2 = truckstops.getString("rawLine2");
                        String raw3 = truckstops.getString("rawLine3");


                        Location loc2 = new Location("");
                        Location loc1 = new Location("");
                        loc1.setLatitude(latn);
                        loc1.setLongitude(lngn);
                        if (lastLocation != null) {
                            loc2.setLatitude(lastLocation.getLatitude());
                            loc2.setLongitude(lastLocation.getLongitude());
                        }
                        double distanceInMeters = (loc1.distanceTo(loc2) * 0.000621371);
                        String dis = Double.toString(distanceInMeters);

                        String allinfo;
                        allinfo = name + " " + city + ", " + State + "\nDistance: " + dis + " Miles " + "\n" + raw1 + "\n" + raw2 + "\n" + raw3;


                        CustomObject myData = new CustomObject();
                        myData.info = allinfo;
                        myData.lat = latn;
                        myData.lng = lngn;


                        LatLng latLng = new LatLng(latn, lngn);
                        MarkerOptions markerOptions = new MarkerOptions();
                        if (lastLocation != null) {
                            currentLocationMarker.setAnchor(0.5f, 0.5f);
                        }
                        markerOptions.position(latLng);
                        markerOptions.title(name + ", " + city + ", " + State);
//                        markerOptions.snippet("Distance" + dis + "Miles" + "\n" + raw1 +"\n" + raw2 + "\n" + raw3 );

                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.gasold));
                        currentLocationMarker = mMap.addMarker(markerOptions);
                        currentLocationMarker.setAnchor(0.5f, 0.5f);
                        currentLocationMarker.setTag(myData);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, error + "", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String codeP = "amNhdGFsYW5AdHJhbnNmbG8uY29tOnJMVGR6WmdVTVBYbytNaUp6RlIxTStjNmI1VUI4MnFYcEVK\n" +
                        "QzlhVnFWOEF5bUhaQzdIcjVZc3lUMitPTS9paU8=";
                Map<String, String> params = new HashMap<String, String>();

//                String auth = "Basic " + Base64.encodeToString(codeP.getBytes(), Base64.NO_WRAP);
                params.put("Authorization", "Basic " + "amNhdGFsYW5AdHJhbnNmbG8uY29tOnJMVGR6WmdVTVBYbytNaUp6RlIxTStjNmI1VUI4MnFYcEVKQzlhVnFWOEF5bUhaQzdIcjVZc3lUMitPTS9paU8=");
//
//               params.put("lat","27.950524");
//                params.put("lng","-82.457842");
                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                if (lastLocation == null || updateflag == true) {
                    params.put("lat", Double.toString(centerMap.latitude));
                    params.put("lng", Double.toString(centerMap.longitude));
                    return params;
                }

                params.put("lat", Double.toString(lastLocation.getLatitude()));
                params.put("lng", Double.toString(lastLocation.getLongitude()));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //all maps listeners
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        mMap.setOnCameraMoveListener(this);
        mMap.setOnCameraMoveCanceledListener(this);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowCloseListener((GoogleMap.OnInfoWindowCloseListener) this);

        SharedPreferences prefs = getSharedPreferences("sharedPrefName", Context.MODE_PRIVATE);
        gpsviewbool = prefs.getBoolean("gpsviewbool", false);

        if (gpsviewbool == true) {
            mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        } else {
            mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }


        buildGoogleApiClient();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
//            Toast.makeText(getApplication(), "This is my Toast message!",
//                    Toast.LENGTH_LONG).show();

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 900);


            return;
        } else {

        }
        mMap.setMyLocationEnabled(false);
        MarkerInfoWindowAdapter markerInfoWindowAdapter = new MarkerInfoWindowAdapter(getApplicationContext());
        googleMap.setInfoWindowAdapter(markerInfoWindowAdapter);

        checkgps();


    }

    @Override
    public void onInfoWindowClose(Marker marker) {
        if (trackison == true) {
            cancelTimer();
            startTimer();
        }

        infowindowopen = false;

    }

    @Override
    public void onCameraMoveStarted(int reason) {


        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            if (trackison == true) {
                track = false;
            }
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            if (trackison == true) {
                track = false;
            }
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            if (trackison == true) {
                track = false;
            }
        }

    }
//unused function


    private int getMapVisibleRadius() {
        VisibleRegion visibleRegion = mMap.getProjection().getVisibleRegion();

        float[] distanceWidth = new float[1];
        float[] distanceHeight = new float[1];

        LatLng farRight = visibleRegion.farRight;
        LatLng farLeft = visibleRegion.farLeft;
        LatLng nearRight = visibleRegion.nearRight;
        LatLng nearLeft = visibleRegion.nearLeft;

        Location.distanceBetween(
                (farLeft.latitude + nearLeft.latitude) / 2,
                farLeft.longitude,
                (farRight.latitude + nearRight.latitude) / 2,
                farRight.longitude,
                distanceWidth
        );

        Location.distanceBetween(
                farRight.latitude,
                (farRight.longitude + farLeft.longitude) / 2,
                nearRight.latitude,
                (nearRight.longitude + nearLeft.longitude) / 2,
                distanceHeight
        );

        double radiusInMeters = Math.sqrt(Math.pow(distanceWidth[0], 2) + Math.pow(distanceHeight[0], 2)) / 2;
        double radiusInMiles = radiusInMeters * 0.000621371;
        return (int) (radiusInMiles / 2);
    }


    protected synchronized void buildGoogleApiClient() {
        client = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        client.connect();
    }


    @Override
    public void onLocationChanged(Location location) {

        lastLocation = location;

        if (track == true && trackison == true) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));
        }

        if (currentLocationMarker != null) {
            currentLocationMarker.remove();
        }
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());


        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title("current Location");


        currentLocationMarker = mMap.addMarker(markerOptions);
        currentLocationMarker.setAnchor(0.5f, 0.5f);
        currentLocationMarker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.dell));
        dontmarker = currentLocationMarker.getId();
        if (first == false || zoom == true) {
            doRequest(100);
            mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            Circle circle = mMap.addCircle(new CircleOptions().center(new LatLng(latLng.latitude, latLng.longitude)).radius(100000 * 1.60934).strokeColor(Color.RED));
            circle.setVisible(false);
            int zoomLevel = getZoomLevel(circle);
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latLng.latitude, latLng.longitude), zoomLevel));
            zoom = false;


            first = true;
        } else first = true;


    }

    public int getZoomLevel(Circle circle) {
        int zoomLevel = 10;
        if (circle != null) {

            double radius = circle.getRadius();
            double scale = radius / 500;
            zoomLevel = (int) (16 - Math.log(scale) / Math.log(2));
        }
        return zoomLevel;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(2000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            // Check Permissions Now

            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    //timer for search
    public void specialstartTimer() {
        countDownTimer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long l) {
                track = false;
                trackison = false;
            }


            public void onFinish() {

                if (tracking.isChecked() == true) {
                    track = true;
                    trackison = true;
                } else {
                    track = false;
                }

            }

        }.start();
    }

    //starts a 5 second times
    public void startTimer() {
        countDownTimer = new CountDownTimer(5000, 1000) {


            @Override
            public void onTick(long l) {
            }

            public void onFinish() {
                track = true;
            }

        }.start();


    }

    //cancels the times
    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onCameraMove() {
//        Toast.makeText(this, "The camera is moving.",
//                Toast.LENGTH_SHORT).show();

        cancelTimer();
        if (trackison == true) {
            track = false;
        }
    }

    @Override
    public void onCameraMoveCanceled() {
//        Toast.makeText(this, "Camera movement canceled.",
//                Toast.LENGTH_SHORT).show();

        if (trackison == true) {
            cancelTimer();
            startTimer();

        }
    }

    @Override
    public void onCameraIdle() {
//        Toast.makeText(this, "The camera has stopped moving.",
//                Toast.LENGTH_SHORT).show();

        if (updateflag == true) {
            doRequest(getMapVisibleRadius());
            updateTimer();
        }
        if (trackison == true && infowindowopen == false) {
            cancelTimer();
            startTimer();


        }
    }

    Marker lastClicked = null;

    @Override
    public boolean onMarkerClick(Marker marker) {

        infowindowopen = true;
        if (trackison == true) {
            track = false;
            startTimer();


        }

        if (marker.getId() == dontmarker)
            return false;
        if (lastClicked != null) {
            if (lastClicked == currentLocationMarker || marker == currentLocationMarker || lastClicked.getId() == dontmarker)
                return false;
            try {
                lastClicked.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gasold));
            } catch (Exception e) {
            }

        }
        marker.setIcon(BitmapDescriptorFactory.fromResource(R.mipmap.gasnew));
        lastClicked = marker;

//        if(trackison == true) {
//            track = true;
//        }
//        marker.showInfoWindow();
        return false;
    }


    //json request for search variables
    public void specialrequest() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);

        alert.setTitle("Search request");
        alert.setMessage("Please input flowing values: ");


        final EditText name = new EditText(this);
        name.setHint("Name");
        final EditText state = new EditText(this);
        state.setHint("State");
        final EditText city = new EditText(this);
        city.setHint("City");
        final EditText zip = new EditText(this);
        zip.setHint("Zip");

        LinearLayout layout = new LinearLayout(getApplicationContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(name);
        layout.addView(state);
        layout.addView(city);
        alert.setView(layout);


        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

                // Do something with value!
                doSpecialRequest(name.getText().toString(), state.getText().toString(), city.getText().toString(), zip.getText().toString());
                Log.i(TAG, name.toString());
                if (trackison == true) {
                    specialstartTimer();
                }
            }
        });

        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                // Canceled.
            }
        });

        alert.show();


    }

    //json request for search variables

    public void doSpecialRequest(final String nameP, final String stateP, final String cityP, final String zipP) {

        centerMap = mMap.getCameraPosition().target;
        mMap.clear();

        String URL_POST = "http://webapp.transflodev.com/svc1.transflomobile.com/api/v3/stations/2500";
        StringRequest stringRequest = new StringRequest(Request.Method.POST, URL_POST, new Response.Listener<String>() {


            @Override
            public void onResponse(String response) {
//                Toast.makeText(getApplication(), response, Toast.LENGTH_SHORT).show();
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(response);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                try {
                    JSONArray jsonArray = jsonObject.getJSONArray("truckStops");
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject truckstops = jsonArray.getJSONObject(i);

                        double latn = truckstops.getDouble("lat");
                        double lngn = truckstops.getDouble("lng");
                        String name = truckstops.getString("name");
                        String city = truckstops.getString("city");
                        String State = truckstops.getString("state");
                        String raw1 = truckstops.getString("rawLine1");
                        String raw2 = truckstops.getString("rawLine2");
                        String raw3 = truckstops.getString("rawLine3");


                        Location loc2 = new Location("");
                        Location loc1 = new Location("");
                        loc1.setLatitude(latn);
                        loc1.setLongitude(lngn);
                        if (lastLocation != null) {
                            loc2.setLatitude(lastLocation.getLatitude());
                            loc2.setLongitude(lastLocation.getLongitude());
                        }
                        double distanceInMeters = (loc1.distanceTo(loc2) * 0.000621371);

                        String dis = Double.toString(distanceInMeters);

                        String allinfo;
                        allinfo = name + " " + city + ", " + State + "\nDistance: " + dis + " Miles" + "\n" + raw1 + "\n" + raw2 + "\n" + raw3;


                        CustomObject myData = new CustomObject();
                        myData.info = allinfo;
                        myData.lat = latn;
                        myData.lng = lngn;


                        LatLng latLng = new LatLng(latn, lngn);
                        MarkerOptions markerOptions = new MarkerOptions();
                        if (lastLocation != null) {
                            currentLocationMarker.setAnchor(0.5f, 0.5f);
                        }
                        markerOptions.position(latLng);
                        markerOptions.title(name + ", " + city + ", " + State);
//                        markerOptions.snippet("Distance" + dis + "Miles" + "\n" + raw1 +"\n" + raw2 + "\n" + raw3 );

                        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.gasold));
                        if (cityP.length() == 0 && stateP.length() == 0) {
                            if (name.toLowerCase().contains(nameP.toLowerCase())) {
                                markerOptions.visible(true);
                            } else
                                markerOptions.visible(false);
                        } else if (name.length() == 0 && stateP.length() == 0) {
                            if (city.toLowerCase().equals(stateP.toLowerCase())) {
                                markerOptions.visible(true);
                            } else
                                markerOptions.visible(false);

                        } else if (cityP.length() == 0) {
                            if (name.toLowerCase().contains(nameP.toLowerCase()) && stateP.toLowerCase().equals(State.toLowerCase())) {
                                markerOptions.visible(true);
                            } else
                                markerOptions.visible(false);

                        } else if (stateP.length() == 0) {
                            if (name.toLowerCase().contains(nameP.toLowerCase()) && city.toLowerCase().equals(cityP.toLowerCase())) {
                                markerOptions.visible(true);
                            } else
                                markerOptions.visible(false);


                        } else {
                            if (name.toLowerCase().contains(nameP.toLowerCase()) && State.toLowerCase().equals(stateP.toLowerCase()) && city.toLowerCase().equals(cityP.toLowerCase())) {
                                markerOptions.visible(true);

                            } else {
                                markerOptions.visible(false);

                            }
                        }
                        currentLocationMarker.setAnchor(0.5f, 0.5f);
                        currentLocationMarker = mMap.addMarker(markerOptions);

                        currentLocationMarker.setTag(myData);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(MapsActivity.this, error + "", Toast.LENGTH_SHORT).show();

            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                String codeP = "amNhdGFsYW5AdHJhbnNmbG8uY29tOnJMVGR6WmdVTVBYbytNaUp6RlIxTStjNmI1VUI4MnFYcEVK\n" +
                        "QzlhVnFWOEF5bUhaQzdIcjVZc3lUMitPTS9paU8=";
                Map<String, String> params = new HashMap<String, String>();

//                String auth = "Basic " + Base64.encodeToString(codeP.getBytes(), Base64.NO_WRAP);
                params.put("Authorization", "Basic " + "amNhdGFsYW5AdHJhbnNmbG8uY29tOnJMVGR6WmdVTVBYbytNaUp6RlIxTStjNmI1VUI4MnFYcEVKQzlhVnFWOEF5bUhaQzdIcjVZc3lUMitPTS9paU8=");
//
//               params.put("lat","27.950524");
//                params.put("lng","-82.457842");
                return params;
            }

            //Pass Your Parameters here
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                if (lastLocation == null) {
                    params.put("lat", Double.toString(centerMap.latitude));
                    params.put("lng", Double.toString(centerMap.longitude));
                    return params;
                }

                params.put("lat", Double.toString(lastLocation.getLatitude()));
                params.put("lng", Double.toString(lastLocation.getLongitude()));
                return params;
            }
        };

        RequestQueue requestQueue = Volley.newRequestQueue(this);
        requestQueue.add(stringRequest);


    }

    //checks if gps is enabled
    public boolean checkgps() {
        final Context context = this;
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = false;
        boolean network_enabled = false;

        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }

        try {
            network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }

        if (!gps_enabled && !network_enabled) {

            // notify user
            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setMessage(context.getResources().getString(R.string.gps_network_not_enabled));
            dialog.setPositiveButton(context.getResources().getString(R.string.open_location_settings), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(myIntent);
                    //get gps
                }
            });
            dialog.setNegativeButton(context.getString(R.string.Cancel), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                    // TODO Auto-generated method stub

                }
            });
            dialog.show();
            return false;
        } else return true;

    }

    private static final String TAG = "MapsActivity";


    //the results of permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {


        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            checkgps();

            LocationServices.FusedLocationApi.requestLocationUpdates(client, locationRequest, this);
//            mMap.setMyLocationEnabled(true);
        } else {
            Toast.makeText(getApplication(), "Please enable location permission",
                    Toast.LENGTH_LONG).show();
        }

    }

    //unused function due to bug
    public static Bitmap getResizedBitmap(Bitmap image, int newHeight, int newWidth) {
        int width = image.getWidth();
        int height = image.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // create a matrix for the manipulation
        Matrix matrix = new Matrix();
        // resize the bit map
        matrix.postScale(scaleWidth, scaleHeight);
        // recreate the new Bitmap
        Bitmap resizedBitmap = Bitmap.createBitmap(image, 0, 0, width, height,
                matrix, false);
        return resizedBitmap;
    }

    //timer for zooming and panning for requests
    public void updateTimer() {
        updateDownTimer = new CountDownTimer(3000, 1000) {


            @Override
            public void onTick(long l) {
                updateflag = false;
            }


            public void onFinish() {
                updateflag = true;
            }

        }.start();


    }
}