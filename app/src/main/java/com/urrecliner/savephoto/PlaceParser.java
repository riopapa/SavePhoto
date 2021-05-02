package com.urrecliner.savephoto;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.urrecliner.savephoto.Vars.NO_MORE_PAGE;
import static com.urrecliner.savephoto.Vars.pageToken;

public class PlaceParser {


    private HashMap<String, String> onePlace2Map(JSONObject placeJson)
    {
        HashMap<String, String> placeList = new HashMap<>();
        String placeName = "--NA--";
        String vicinity= "--NA--";
        String icon = "";

        try {
            if (!placeJson.isNull("name")) {
                placeName = placeJson.getString("name");
            }
            if (!placeJson.isNull("vicinity")) {
                vicinity = placeJson.getString("vicinity");
            }
            if (!placeJson.isNull("formatted_address")) {
                vicinity = placeJson.getString("formatted_address");
            }
            vicinity = vicinity.replace("KR","")
                    .replace("대한민국","").replace("서울특별시","");
            String latitude = placeJson.getJSONObject("geometry").getJSONObject("location").getString("lat");
            String longitude = placeJson.getJSONObject("geometry").getJSONObject("location").getString("lng");
            icon = placeJson.getString("icon");
            String []icons = icon.split("/");
            icon = icons[icons.length-1];
            icons = icon.split("-");
            icon = icons[0];

            placeList.put("name", placeName);
            placeList.put("vicinity", vicinity);
            placeList.put("lat", latitude);
            placeList.put("lng", longitude);
            placeList.put("icon", icon);

        }
        catch (JSONException e) {
            e.printStackTrace();
        }
        return placeList;

    }
    private List<HashMap<String, String>> getAllPlaces(JSONArray jsonArray, boolean byPlaceName)
    {
        int count = jsonArray.length();
        List<HashMap<String, String>> placeList = new ArrayList<>();
        HashMap<String, String> placeMap;
        Log.w("jsonMap", jsonArray.toString());
        for(int i = 0; i<count;i++)
        {
            try {
                placeMap = onePlace2Map((JSONObject) jsonArray.get(i));
//                Log.w("placeMap", placeMap.toString());
                placeList.add(placeMap);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return placeList;
    }

    public static final String NO_MORE_PAGE = "no more";
    public static String pageToken;

    public List<HashMap<String, String>> parse(String jsonData, boolean byPlaceName)
    {
        JSONArray jsonArray = null;
        JSONObject jsonObject;
//        utils.log("json data", jsonData);

        if (byPlaceName) {
            try {
                jsonObject = new JSONObject(jsonData);
                jsonArray = jsonObject.getJSONArray("candidates");
                pageToken = (jsonObject.isNull("next_page_token")) ?
                        NO_MORE_PAGE : jsonObject.getString("next_page_token");
            } catch (JSONException e) {
                pageToken = NO_MORE_PAGE;
                e.printStackTrace();
            }

        } else {
            try {
                jsonObject = new JSONObject(jsonData);
                jsonArray = jsonObject.getJSONArray("results");
                pageToken = (jsonObject.isNull("next_page_token")) ?
                        NO_MORE_PAGE : jsonObject.getString("next_page_token");
            } catch (JSONException e) {
                pageToken = NO_MORE_PAGE;
                e.printStackTrace();
            }
        }
        assert jsonArray != null;
        return getAllPlaces(jsonArray, byPlaceName);
    }
}

/*

by placename
https://maps.googleapis.com/maps/api/place/findplacefromtext/json?input=브롱스&inputtype=textquery&locationbias=circle:200@37.3865803,127.12455589999999&language=ko&fields=formatted_address,name,geometry,icon&key=myKey

{
   "candidates" : [
      {
         "formatted_address" : "대한민국 경기도 화성시 동탄면 동탄순환대로 830 209호",
         "geometry" : {
            "location" : {
               "lat" : 37.198823,
               "lng" : 127.0958309
            },
            "viewport" : {
               "northeast" : {
                  "lat" : 37.20017282989271,
                  "lng" : 127.0971807298927
               },
               "southwest" : {
                  "lat" : 37.19747317010727,
                  "lng" : 127.0944810701072
               }
            }
         },
         "name" : "(주)브롱호스트코리아"
      },
      {
         "formatted_address" : "대한민국 동안구 관양동 1595번지 세방글로벌시티 912호 안양시 경기도 KR",
         "geometry" : {
            "location" : {
               "lat" : 37.3949216,
               "lng" : 126.9609466
            },
            "viewport" : {
               "northeast" : {
                  "lat" : 37.39627142989272,
                  "lng" : 126.9622964298927
               },
               "southwest" : {
                  "lat" : 37.39357177010728,
                  "lng" : 126.9595967701073
               }
            }
         },
         "name" : "(주)브롱"
      }
   ],
   "status" : "OK"
}

by location nearby

https://maps.googleapis.com/maps/api/place/nearbysearch/json?&location=37.3865803,127.12455589999999&radius=100&language=ko&fields=formatted_address,name,geometry&key=mykey

{
   "html_attributions" : [],
   "next_page_token" : "ATtYBwI1FuIR3152vB9P9ttU2GYx89MP-0bFZ5Qx3Rc-O_dTynBZfdBjI1awLjPznWnsk-tozpB0sTeBGrulOIOFv3GyCzYseyegDuecgsCHMomWWee4k3N2xQ_2kniTq4GmEyxM0C2ADFQ62krv5s4OJrTqlTEAeQfS25GLgK5blXw8O0dmD5XuNQU5QHhQLaULCQZ7o2z1d2uHNTjflnqfUDwe-3O5N0Q78SlK83OyS1d6cBvOMinx_84ZKQmNi750ZS9OPvrwM_p8tzTI_zuNlYalK4QjsxY6MWo4Abx5OK55XvlqgUI9fRRf9fNo4nFIdrvCi7rJYJzXiCN2U8-W0rmE8O1pf0ta06GBaMC4FwR0nDIjYCbUEFgXRcyeCAFYhNEJN9R4I9cRoJk7nNYAn8zqon-E1KmX-OF1cEXdZzR1PX8hh8rbvK2fiUrE9YhfGNTovF3DMfFt4O9mFq0n_C_YvhgNxD3xqSSt4tcb0mLBGkhoSuBeS_X9H6fEXtR2AmMT-Sl4tUZBz3mlZOsf3AbrL1ue0pvZ-2pxmkq5NRUxZBKk6qQwQe9ij158nY_FrS-i3uFNRwWnSF6Fdt1KICGMScVtmEjq_JswfSMrjF-baS4B-oHxVxwNMPUIazIZRpSUD2NqfC2HRoHEKhSFTAJpqKNZwgM0SqR90hYY7c1g7qg7ycw1QKLjQ3PhJsk5HbIIUa-5XBTZGZ8Z5g-m-QgoS2hGTxcy8YiJ49Cw9MZ9JrEB-iB62eN0njyq8Qcuf4B09L6x6QofM-x-U5lkUDlS3URvcRWNAnrhvVntgZ01XPIPMPvouR03qB4f7dud3JLRooGCBgbJZ7g66GmBRN3JgWDvL5ISvj54GVvCPOioUOGfgM791tbEYFU0_J8_onY",
   "results" : [
      {
         "geometry" : {
            "location" : {
               "lat" : 37.4449168,
               "lng" : 127.1388684
            },
            "viewport" : {
               "northeast" : {
                  "lat" : 37.4654,
                  "lng" : 127.1796
               },
               "southwest" : {
                  "lat" : 37.3351,
                  "lng" : 127.1023
               }
            }
         },
         "icon" : "https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/geocode-71.png",
         "name" : "성남시",
         "photos" : [
            {
               "height" : 3456,
               "html_attributions" : [
                  "\u003ca href=\"https://maps.google.com/maps/contrib/104593287122441722806\"\u003eGaram Lee\u003c/a\u003e"
               ],
               "photo_reference" : "ATtYBwLLQYNtqlk7EhHVd2aCBYenokrD4G2uXj3Otc_UEUfu10-VOmJpNU2H8Xl6_oQbP5GD1cqLl6_6lfhTUqR1Ottf2zBx8T1EIaS_NtImqHkMFFoomMbjPpP0hjQZI7TO4JgmkOVihw8o-J0Lnz6HcYSf9yRcXaxo_YHrG8WV_YXd1sKG",
               "width" : 4608
            }
         ],
         "place_id" : "ChIJvyAScPGnfDUR1JfjvGZWMVA",
         "reference" : "ChIJvyAScPGnfDUR1JfjvGZWMVA",
         "scope" : "GOOGLE",
         "types" : [ "locality", "political" ],
         "vicinity" : "성남시"
      },
      {
         "business_status" : "OPERATIONAL",
         "geometry" : {
            "location" : {
               "lat" : 37.3868094,
               "lng" : 127.1254602
            },
            "viewport" : {
               "northeast" : {
                  "lat" : 37.3881583802915,
                  "lng" : 127.1268091802915
               },
               "southwest" : {
                  "lat" : 37.3854604197085,
                  "lng" : 127.1241112197085
               }
            }
         },
         "icon" : "https://maps.gstatic.com/mapfiles/place_api/icons/v1/png_71/shopping-71.png",
         "name" : "나일론핑크",
         "place_id" : "ChIJmwaWD-JZezURRDnu8CjENd0",
         "plus_code" : {
            "compound_code" : "94PG+P5 대한민국 경기도 성남시",
            "global_code" : "8Q9994PG+P5"
         },
         "rating" : 3,
         "reference" : "ChIJmwaWD-JZezURRDnu8CjENd0",
         "scope" : "GOOGLE",
         "types" : [ "shopping_mall", "point_of_interest", "establishment" ],
         "user_ratings_total" : 11,
         "vicinity" : "성남시 분당구 서현동 245-3"
      },

 */
