package com.education.schoolapp;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityCompat.OnRequestPermissionsResultCallback;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class AddressMapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, LocationListener, OnRequestPermissionsResultCallback {

    private static final String TAG = AddressMapsActivity.class.toString();
    private GoogleMap mMap;
    private GoogleApiClient mGoogleLocation;
    private FloatingActionButton mDirectionFab;
    private TextView mAddressText;
    private Location mCurLocation;
    private Double mCurLatitude;
    private Double mCurLongitude;
    private LatLng mAddressLocation;
    private LocationRequest mLocReq;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_address_maps);

        mLocReq = LocationRequest.create();
        mLocReq.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        mGoogleLocation = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDirectionFab = (FloatingActionButton) findViewById(R.id.directions_fab);
        mDirectionFab.setOnClickListener(this);

        mAddressText = (TextView) findViewById(R.id.address_text);
    }

    @Override
    protected void onStart() {
        super.onStart();
        verifyStoragePermissions(this);
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            mGoogleLocation.connect();
        }
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

        // Add a marker in Sydney and move the camera
        mAddressLocation = new LatLng(-34, 151);
        CameraPosition Location =
                new CameraPosition.Builder().target(mAddressLocation)
                        .zoom(13f)
                        .build();
        mMap.addMarker(new MarkerOptions().position(mAddressLocation).title("Marker in Sydney"));
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(Location));
    }

    @Override
    public void onConnected(Bundle bundle) {
        mCurLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleLocation);
        if (mCurLocation != null) {
            Log.i(TAG, "Got the location");
            mCurLatitude = mCurLocation.getLatitude();
            mCurLongitude = mCurLocation.getLongitude();
        } else {
            Log.i(TAG, "Requesting for Location updates");
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleLocation, mLocReq, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.directions_fab) {
            if (mCurLatitude != null && mCurLongitude != null) {
                Intent mapIntent = new Intent(android.content.Intent.ACTION_VIEW,
                        Uri.parse("http://maps.google.com/maps?saddr=" + mCurLatitude + "," + mCurLongitude +
                                "&daddr=" + mAddressLocation.latitude + "," + mAddressLocation.longitude));
                mapIntent.setComponent(new ComponentName("com.google.android.apps.maps",
                        "com.google.android.maps.MapsActivity"));
                startActivity(mapIntent);
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurLocation = location;
        if (mCurLocation != null) {
            mCurLatitude = mCurLocation.getLatitude();
            mCurLongitude = mCurLocation.getLongitude();
        }
    }
    // Storage Permissions
    private static final int REQUEST_LOCATION = 2;
    private static String[] PERMISSIONS_LOCATION = {
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_LOCATION,
                    REQUEST_LOCATION
            );
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleLocation.connect();
            }
        }
    }
}
