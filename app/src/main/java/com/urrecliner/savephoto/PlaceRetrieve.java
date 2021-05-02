package com.urrecliner.savephoto;

import android.content.Context;

import static com.urrecliner.savephoto.Vars.NO_MORE_PAGE;

public class PlaceRetrieve {

    static boolean byPlaceName = false;

    public PlaceRetrieve(Context mContext, double latitude, double longitude, String placeType, String pageToken, String radius, String placeName) {

        StringBuilder url;
        if (!placeName.equals("")) {
            byPlaceName = true;
            placeName = placeName.replace(" ", "%20");
            url = new StringBuilder("https://maps.googleapis.com/maps/api/place/findplacefromtext/json?");
            url.append("input=").append(placeName).append("&inputtype=textquery");
            url.append("&locationbias=circle:").append(radius)
                    .append("@").append(latitude).append(",").append(longitude);
        } else {
            byPlaceName = false;
            url = new StringBuilder("https://maps.googleapis.com/maps/api/place/nearbysearch/json?");
            if (!placeType.equals("all"))
                url.append("&type=").append(placeType);
            url.append("&location=").append(latitude).append(",").append(longitude);
            url.append("&radius=").append(radius);
        }
        url.append("&language=ko");
        if (pageToken != null && !pageToken.equals(NO_MORE_PAGE)) {
            url.append("&pagetoken=").append(pageToken);
//            Toast.makeText(mContext, "retrieving more places " + size, Toast.LENGTH_LONG).show();
        } else {
            url.append("&fields=formatted_address,name,icon,geometry");
        }
        url.append("&key=").append(mContext.getString(R.string.maps_api_key));
        PlaceInfoBuild placeInfoBuild = new PlaceInfoBuild();
        placeInfoBuild.execute(url.toString(), placeName);
    }
}
