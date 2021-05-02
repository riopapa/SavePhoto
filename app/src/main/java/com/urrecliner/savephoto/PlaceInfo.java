package com.urrecliner.savephoto;

import static com.urrecliner.savephoto.GPSTracker.oLatitude;
import static com.urrecliner.savephoto.GPSTracker.oLongitude;
import static com.urrecliner.savephoto.Vars.sharedSortType;

class PlaceInfo {
    String oName;
    String oAddress;
    String oIcon;
    String oLat;
    String oLng;
    String distance;
    Double lat, lng;    // derived from string

    public PlaceInfo(String oName, String oAddress, String oIcon, String oLat, String oLng) {
        this.oName = oName;
        this.oAddress = oAddress;
        this.oIcon = oIcon;
        this.oLat = oLat;
        this.oLng = oLng;
        if (sharedSortType.equals("distance")) {
            lat = Double.parseDouble(oLat);
            lng = Double.parseDouble(oLng);
            distance = ((Math.sqrt((oLatitude-lat)*(oLatitude-lat)+(oLongitude-lng)*(oLongitude-lng))*1000L+1000L)+"");
        }
    }

    public void setoName(String oName) {
        this.oName = oName;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }
}
