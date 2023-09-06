package com.example.geofence;

import androidx.room.TypeConverter;

import com.google.android.gms.maps.model.LatLng;

public class LatLngConverter {
    @TypeConverter
    public static LatLng fromString(String value) {
        String[] parts = value.split(",");
        return new LatLng(Double.parseDouble(parts[0]), Double.parseDouble(parts[1]));
    }

    @TypeConverter
    public static String fromLatLng(LatLng latLng) {
        return latLng.latitude + "," + latLng.longitude;
    }
}