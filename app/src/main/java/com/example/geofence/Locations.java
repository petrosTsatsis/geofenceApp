package com.example.geofence;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;;
import com.google.android.gms.maps.model.LatLng;

@Entity(tableName = "locations")
public class Locations {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "locations_center")
    public LatLng center;

    @ColumnInfo(name = "locations_session")
    public int session;
}
