package com.urrecliner.savephoto;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.widget.TextView;

import java.util.ArrayList;

public class Vars {

    static String currActivity = null;
    static String strPlace = null;
    static String strAddress = null;
    static String strVoice = " ";

    static Camera mCamera;

    /* -- shared Preferences --- */
    static SharedPreferences sharedPref;
    static String sharedRadius;
    static boolean sharedAutoLoad;
    static String sharedSortType;

    static Utils utils;
    static Activity mActivity;
    static TextView tvAddress;
    static TextView tvVoice;
    static Context mContext = null;

    static int cameraOrientation;

    /* place select related variables */
    static String NO_MORE_PAGE = "no more";
    static String pageToken = NO_MORE_PAGE;
    static boolean nowDownLoading = false;
    static boolean isPlaceNull = true;
    static ArrayList<PlaceInfo> placeInfos = null;
    static Activity selectActivity;

    static String [] iconNames = { "question",
            "airport", "amusement", "aquarium", "art_gallery", "atm", "baby",
            "bank_dollar", "bank_euro", "bank_pound", "bank_yen", "bar", "barber",
            "baseball", "beach", "bicycle", "bus", "cafe", "camping", "car_dealer",
            "car_repair", "casino", "civic_building", "convenience", "courthouse",
            "dentist", "doctor", "electronics", "fitness", "flower", "gas_station",
            "generic_business", "generic_recreational", "geocode", "golf", "government",
            "historic", "jewelry", "library", "lodging", "monument", "mountain",
            "movies", "museum", "pet", "police", "post_office", "repair", "restaurant",
            "school", "shopping", "ski", "stadium", "supermarket", "taxi", "tennis",
            "train", "travel_agent", "truck", "university", "wine", "worship_christian",
            "worship_general", "worship_hindu", "worship_islam", "worship_jewish", "zoo",
            "park", "bank","worship_dharma", "pharmacy", "parking", "cemetery_grave",
            "hospital"
    };

    static int [] iconRaws = { R.raw.question,
            R.raw.airport, R.raw.amusement, R.raw.aquarium, R.raw.art_gallery, R.raw.atm, R.raw.baby,
            R.raw.bank_dollar, R.raw.bank_euro, R.raw.bank_pound, R.raw.bank_yen, R.raw.bar, R.raw.barber,
            R.raw.baseball, R.raw.beach, R.raw.bicycle, R.raw.bus, R.raw.cafe, R.raw.camping, R.raw.car_dealer,
            R.raw.car_repair, R.raw.casino, R.raw.civic_building, R.raw.convenience, R.raw.courthouse,
            R.raw.dentist, R.raw.doctor, R.raw.electronics, R.raw.fitness, R.raw.flower, R.raw.gas_station,
            R.raw.generic_business, R.raw.generic_recreational, R.raw.geocode, R.raw.golf, R.raw.government,
            R.raw.historic, R.raw.jewelry, R.raw.library, R.raw.lodging, R.raw.monument, R.raw.mountain,
            R.raw.movies, R.raw.museum, R.raw.pet, R.raw.police, R.raw.post_office, R.raw.repair, R.raw.restaurant,
            R.raw.school, R.raw.shopping, R.raw.ski, R.raw.stadium, R.raw.supermarket, R.raw.taxi, R.raw.tennis,
            R.raw.train, R.raw.travel_agent, R.raw.truck, R.raw.university, R.raw.wine, R.raw.worship_christian,
            R.raw.worship_general, R.raw.worship_hindu, R.raw.worship_islam, R.raw.worship_jewish, R.raw.zoo,
            R.raw.park, R.raw.bank, R.raw.worship_dharma, R.raw.pharmacy, R.raw.parking, R.raw.cemetery_grave,
            R.raw.hospital
    };

    static ArrayList<TypeInfo> typeInfos = null;
    static TypeAdapter typeAdapter;
    static String placeType = "all";
    static String byPlaceName = "";
    static int typeNumber = 0;
    static int[] typeIcons = {R.mipmap.place_holder, R.raw.restaurant, R.raw.cafe, R.raw.bar,
            R.raw.shopping, R.raw.shopping, R.raw.park, R.raw.worship_christian, R.raw.worship_islam,
            R.raw.parking, R.raw.school, R.raw.museum, R.raw.amusement, R.raw.amusement,
            R.raw.university,R.raw.atm, R.raw.zoo, R.raw.lodging,
            R.mipmap.place_holder};
    static String[] typeNames = {"all", "restaurant", "cafe", "bar",
            "store", "shopping_mall", "park", "church","mosque",
            "parking", "school", "museum", "tourist_attraction", "amusement",
            "university", "atm", "zoo", "lodging",
            "all"};
}
