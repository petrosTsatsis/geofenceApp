package com.example.geofence;

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@Database(entities = {Locations.class}, version = 1)
@TypeConverters({LatLngConverter.class})
public abstract class GeofenceDatabase extends RoomDatabase {
    public abstract LocationsDao locationsDao();
}
