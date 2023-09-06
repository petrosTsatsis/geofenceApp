package com.example.geofence;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class GeofenceService extends Service {

    private LocationManager locationManager;
    private LocationListener locationListener;
    private static List<LatLng> newLocations = new ArrayList<>();
    private boolean isPaused = false;

    @Override
    public void onCreate() {
        super.onCreate();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        locationListener = location -> {
            if (!isPaused) {
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                Log.d("location", String.valueOf(latLng));
                newLocations.add(latLng);
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, locationListener);
    }




    @Override
    public void onDestroy() {
        super.onDestroy();

        SharedPreferences prefs = getSharedPreferences("pref", Context.MODE_PRIVATE);
        int sessions = prefs.getInt("sessions1", 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("sessions1", sessions + 1);
        editor.apply();
        newLocations.clear();
        Toast.makeText(getApplicationContext(), "Service stopped Successfully!", Toast.LENGTH_SHORT).show();
        stopLocationUpdates();
        stopSelf();
    }

    private void stopLocationUpdates() {
        locationManager.removeUpdates(locationListener);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public List<LatLng> getNewLocations() {
        return newLocations;
    }


    public void pauseService() {
        isPaused = true;
        stopLocationUpdates();
    }

    public void resumeService() {
        isPaused = false;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 50, locationListener);
        }
    }

    public boolean isPaused() {
        return isPaused;
    }
}
