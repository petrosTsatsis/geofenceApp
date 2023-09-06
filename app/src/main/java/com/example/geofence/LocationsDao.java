package com.example.geofence;

import android.provider.CallLog;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

@Dao
public interface LocationsDao {

    @Query("SELECT locations_center FROM locations WHERE locations_session = (SELECT MAX(locations_session) FROM locations)")
    public List<LatLng> getLocationsFromSession();

    @Insert
    public void insertLocation(Locations... locations);

    @Query("SELECT * FROM locations")
    public List<Locations> getAll();
}
