package com.example.geofence;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CallLog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.room.Room;

import com.example.geofence.databinding.ActivityMapsBinding;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback,
        GoogleMap.OnMapLongClickListener, GoogleMap.OnMarkerClickListener {

    private static final int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private final int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    private GoogleMap mMap;
    private ArrayList<Circle> circles = new ArrayList<>();

    private HashMap<Marker, Circle> markerCircleMap = new HashMap<>();
    private static final int CIRCLE_RADIUS = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        com.example.geofence.databinding.ActivityMapsBinding binding = ActivityMapsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Button cancelButton = findViewById(R.id.cancel_button);

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Return to the main screen and clear circles
                clearCircles();
                finish();
            }
        });

        GeofenceDatabase db = Room.databaseBuilder(getApplicationContext(),GeofenceDatabase.class,"locations").build();
        LocationsDao locationsDao = db.locationsDao();

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {

            //stores current session
            SharedPreferences prefs = getSharedPreferences("pref", Context.MODE_PRIVATE);
            int sessions = prefs.getInt("sessions1", 0); // retrieving value from shared preferences

            // Store circle centers in the ContentProvider
            if (circles.size() >= 1){
                for (Circle circle : circles) {

                    Locations locations = new Locations();
                    locations.session = sessions;
                    LatLng center = circle.getCenter();
                    locations.center = center;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            locationsDao.insertLocation(locations);
                        }
                    }).start();

                }
                //starts service with intent
                Intent intent1 = new Intent(MapsActivity.this, GeofenceService.class);
                startService(intent1);

                //goes back to main
                Intent intent = new Intent(MapsActivity.this, MainActivity.class);
                Toast.makeText(getApplicationContext(), "Locations Saved Successfully!", Toast.LENGTH_SHORT).show();
                startActivity(intent);

            }else {
                //make a toast if user pressed button without choosing any location
                Toast.makeText(getApplicationContext(), "Please select at least 1 location!", Toast.LENGTH_SHORT).show();
            }

        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        enableMyLocation();

        mMap.setOnMapLongClickListener(this);
        mMap.setOnMarkerClickListener(this);
    }

    // Enable the location
    @SuppressLint("MissingPermission")
    private void enableMyLocation() {
        // Check if permission to access the device's location has been granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            Log.e("Permission", "Location permission granted");
            // Get the user's current location and zoom in on it
            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {}
            };
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        } else {
            Log.e("Permission", "Location permission denied");
            // Request permission to access the device's location
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_ACCESS_REQUEST_CODE);
        }
    }

    // What happens on the Long click
    @Override
    public void onMapLongClick(LatLng point) {
        CircleOptions circleOptions = new CircleOptions()
                .center(point)
                .radius(CIRCLE_RADIUS)
                .strokeWidth(2)
                .strokeColor(Color.RED)
                .fillColor(Color.argb(70, 255, 0, 0));

        Circle circle = mMap.addCircle(circleOptions);
        circles.add(circle);
        addMarker(point,circle);
    }


    private void clearCircles() {
        for (Circle circle : circles) {
            circle.remove();
        }
        circles.clear();
    }

    // Permissions
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("Permission", "Location permission granted");
                enableMyLocation();
            } else {
                Log.e("Permission", "Location permission denied");
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // What happens when you click on the marker
    @Override
    public boolean onMarkerClick(Marker marker) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete Marker");
        builder.setMessage("Are you sure you want to delete this marker?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                Circle circle = markerCircleMap.get(marker);
                if (circle != null) {
                    markerCircleMap.remove(marker);
                    circle.remove();
                }
                marker.remove();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
        return true;
    }

    // Add marker to the circle
    private void addMarker(LatLng point, Circle circle) {
        Marker marker = mMap.addMarker(new MarkerOptions().position(point).draggable(true));
        markerCircleMap.put(marker, circle);
    }


}


