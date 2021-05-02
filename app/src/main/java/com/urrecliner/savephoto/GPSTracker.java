package com.urrecliner.savephoto;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.core.app.ActivityCompat;

import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;
import static com.urrecliner.savephoto.Vars.isPlaceNull;
import static com.urrecliner.savephoto.Vars.mContext;
import static com.urrecliner.savephoto.Vars.strAddress;
import static com.urrecliner.savephoto.Vars.utils;

class GPSTracker  implements LocationListener {

    private static final long MIN_TIME_WALK_UPDATES = 1000; // miliSecs
    private static final float MIN_DISTANCE_WALK = 0; // meters
    protected LocationManager locationManager;

    static double oLatitude = 0;
    static double oLongitude = 0;
    static double oAltitude = 0;

    void get() {

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        try {
            locationManager = (LocationManager) mContext.getSystemService(LOCATION_SERVICE);
            assert locationManager != null;
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_WALK_UPDATES,
                        MIN_DISTANCE_WALK, this);
        } catch (Exception e) {
            utils.logE("GPS", "Start Error");
        }

        while (oLatitude == 0) {
            SystemClock.sleep((500));
            if (locationManager != null) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location != null) {
                    utils.log("GPS 1", "lat="+location.getLatitude()+" , lng="+location.getLongitude()+" time="+location.getTime());
                    oLatitude = location.getLatitude();
                    oLongitude = location.getLongitude();
                    oAltitude = location.getAltitude();
                }
            }
            utils.log("wait","location update");
        }
    }

    int updateCount = 0;
    @Override
    public void onLocationChanged(Location location) {
        if (isPlaceNull && updateCount++ < 7) {
            oLatitude = location.getLatitude();
            oLongitude = location.getLongitude();
            oAltitude = location.getAltitude();
            utils.log("location changed",oLatitude+","+oLongitude+","+oAltitude);
            Geocoder geocoder = new Geocoder(mContext, Locale.KOREA);
            strAddress = GPS2Address.get(geocoder, oLatitude, oLongitude);
            MainActivity.inflateAddress();
        } else {
            locationManager.removeUpdates(this);
        }
    }
    @Override
    public void onProviderDisabled(String provider) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }

}