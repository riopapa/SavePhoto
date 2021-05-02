package com.urrecliner.savephoto;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Timer;
import java.util.TimerTask;

import static com.urrecliner.savephoto.GPSTracker.oLatitude;
import static com.urrecliner.savephoto.GPSTracker.oLongitude;
import static com.urrecliner.savephoto.Vars.byPlaceName;
import static com.urrecliner.savephoto.Vars.mContext;
import static com.urrecliner.savephoto.Vars.placeInfos;
import static com.urrecliner.savephoto.Vars.NO_MORE_PAGE;
import static com.urrecliner.savephoto.Vars.nowDownLoading;
import static com.urrecliner.savephoto.Vars.pageToken;
import static com.urrecliner.savephoto.Vars.placeType;
import static com.urrecliner.savephoto.Vars.selectActivity;
import static com.urrecliner.savephoto.Vars.sharedRadius;
import static com.urrecliner.savephoto.Vars.sharedPref;
import static com.urrecliner.savephoto.Vars.utils;

public class SelectActivity extends AppCompatActivity {

    static CountDownTimer waitTimer = null;
    RecyclerView placeRecycleView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place);
        selectActivity = this;
        placeRecycleView = findViewById(R.id.place_recycler);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(this);
        placeRecycleView.setLayoutManager(mLinearLayoutManager);

        waitTimer = new CountDownTimer(20000, 200) {
            public void onTick(long millisUntilFinished) {
                if (!nowDownLoading) {
                    waitTimer.cancel();
                    if (!pageToken.equals(NO_MORE_PAGE)) {
                        new PlaceRetrieve(mContext, oLatitude, oLongitude, placeType, pageToken, sharedRadius, byPlaceName);
                        new Timer().schedule(new TimerTask() {
                            public void run() {
                                waitTimer.start();
                            }
                        }, 2000);
                    } else {
                        sortPlaceInfos();
                        String s = "Total "+placeInfos.size()+" places retrieved";
                        utils.log("LIST", s);
                        Toast.makeText(mContext,s, Toast.LENGTH_SHORT).show();
                        PlaceAdapter placeAdapter = new PlaceAdapter();
                        placeRecycleView.setAdapter(placeAdapter);
                    }
                }
            }
            public void onFinish() { }
        }.start();
    }

    private void sortPlaceInfos() {

        String sortType = sharedPref.getString("sort", "none");
        switch(sortType) {
            case "name":
                placeInfos.sort((arg0, arg1) -> arg0.oName.compareTo(arg1.oName));
                break;
            case "distance":
                placeInfos.sort((arg0, arg1) -> arg0.distance.compareTo(arg1.distance));
                break;
            default:
        }
    }
}
