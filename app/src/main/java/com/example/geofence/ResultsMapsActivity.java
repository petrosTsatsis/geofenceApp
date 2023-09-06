package com.example.geofence;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.room.Room;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.maps.android.SphericalUtil;

import java.util.List;



import java.util.List;

public class ResultsMapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = MapsActivity.class.getSimpleName();
    private GoogleMap Mmap;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    private Location lastKnownLocation;

    private List<LatLng> loc;
    private List<LatLng> newLoc;


    @Override
    public void onBackPressed() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_results_maps);
        GeofenceService geofenceService = new GeofenceService();
        newLoc = geofenceService.getNewLocations();

        //new thread
        new Thread(() -> {
            GeofenceDatabase db = Room.databaseBuilder(getApplicationContext(),
                    GeofenceDatabase.class, "locations").build();
            LocationsDao locationsDao = db.locationsDao();
            //gets last session's location
            loc = locationsDao.getLocationsFromSession();
            db.close();
        }).start();


        Button back = findViewById(R.id.BackButton);
        back.setOnClickListener(view -> {
            Intent intent = new Intent(ResultsMapsActivity.this, MainActivity.class);
            startActivity(intent);
        });

        //PAUSE Button
        Button pauseResumeButton = findViewById(R.id.PauseResumeButton);
        pauseResumeButton.setOnClickListener(view -> {
            Intent intent = new Intent(ResultsMapsActivity.this, GeofenceService.class);
            if (stopService(intent)) {
                if (geofenceService.isPaused()) {
                    Toast.makeText(getApplicationContext(), "LOCATION RESUMED", Toast.LENGTH_SHORT).show();

                } else {
                    Toast.makeText(getApplicationContext(), "LOCATION PAUSED", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No service running", Toast.LENGTH_SHORT).show();
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap Mmap) {
        this.Mmap = Mmap;


        for (LatLng location : loc) {
            Mmap.addCircle(new CircleOptions()
                    .center(location)
                    .radius(100) 
                    .strokeColor(Color.RED)
                    );
        }

        getLocationPermission();
        updateLocationUI();
    }



    private void getLocationPermission() {

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        locationPermissionGranted = false;
        if (requestCode
                == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (Mmap == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                Mmap.setMyLocationEnabled(true);
                Mmap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                Mmap.setMyLocationEnabled(false);
                Mmap.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }
}
