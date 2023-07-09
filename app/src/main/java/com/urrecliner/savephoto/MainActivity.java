package com.urrecliner.savephoto;

import static com.urrecliner.savephoto.GPSTracker.oAltitude;
import static com.urrecliner.savephoto.GPSTracker.oLatitude;
import static com.urrecliner.savephoto.GPSTracker.oLongitude;
import static com.urrecliner.savephoto.Vars.NO_MORE_PAGE;
import static com.urrecliner.savephoto.Vars.byPlaceName;
import static com.urrecliner.savephoto.Vars.cameraOrientation;
import static com.urrecliner.savephoto.Vars.currActivity;
import static com.urrecliner.savephoto.Vars.mActivity;
import static com.urrecliner.savephoto.Vars.mCamera;
import static com.urrecliner.savephoto.Vars.mContext;
import static com.urrecliner.savephoto.Vars.pageToken;
import static com.urrecliner.savephoto.Vars.placeInfos;
import static com.urrecliner.savephoto.Vars.placeType;
import static com.urrecliner.savephoto.Vars.sharedAutoLoad;
import static com.urrecliner.savephoto.Vars.sharedLocation;
import static com.urrecliner.savephoto.Vars.sharedLogo;
import static com.urrecliner.savephoto.Vars.sharedMap;
import static com.urrecliner.savephoto.Vars.sharedPref;
import static com.urrecliner.savephoto.Vars.sharedRadius;
import static com.urrecliner.savephoto.Vars.sharedVoice;
import static com.urrecliner.savephoto.Vars.sharedWithPhoto;
import static com.urrecliner.savephoto.Vars.sigMap;
import static com.urrecliner.savephoto.Vars.strAddress;
import static com.urrecliner.savephoto.Vars.strPlace;
import static com.urrecliner.savephoto.Vars.strVoice;
import static com.urrecliner.savephoto.Vars.tvAddress;
import static com.urrecliner.savephoto.Vars.tvVoice;
import static com.urrecliner.savephoto.Vars.typeAdapter;
import static com.urrecliner.savephoto.Vars.typeIcons;
import static com.urrecliner.savephoto.Vars.typeInfos;
import static com.urrecliner.savephoto.Vars.typeNames;
import static com.urrecliner.savephoto.Vars.typeNumber;
import static com.urrecliner.savephoto.Vars.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.SparseIntArray;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.view.WindowInsetsController;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.exifinterface.media.ExifInterface;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private final static int VOICE_RECOGNISE = 1234;
    private final static int SAVE_MAP = 2345;
    private CameraPreview mCameraPreview;

    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private SensorManager mSensorManager;
    private DeviceOrientation deviceOrientation;
    static String map_api_key;
    Bitmap cameraImage;
    public static Bitmap googleShot = null;
    public static int zoomValue = 15;

    private static long now_time;
    private boolean exitFlag = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        setFullScreen();
        currActivity = this.getClass().getSimpleName();
        mActivity = this;
        mContext = getApplicationContext();
        askPermission();
        initiate_Variables();
        setFullScreen();

        ImageView btnShot = findViewById(R.id.btnShot);
        btnShot.setOnClickListener(v -> {exitFlag = false; take_Picture();});
        ImageView btnShotExit = findViewById(R.id.btnShotExit);
        btnShotExit.setOnClickListener(v -> {exitFlag = true; take_Picture();});

        new GPSTracker().get();
        startCamera();

        ImageView mSpeak = findViewById(R.id.btnSpeak);
        mSpeak.setOnClickListener(v -> startGetVoice());

        ImageView mPlace = findViewById(R.id.btnPlace);
        mPlace.setOnClickListener(v -> {
            pageToken = NO_MORE_PAGE;
            placeInfos = new ArrayList<>();
            mPlace.setImageResource(typeIcons[typeNumber]);
            EditText et = findViewById(R.id.placeAddress);
            String placeName = et.getText().toString();
            if (placeName.startsWith("?")) {
                String[] placeNames = placeName.split("\n");
                byPlaceName = placeNames[0].substring(1);
            } else
                byPlaceName = "";
            new PlaceRetrieve(mContext, oLatitude, oLongitude, placeType, pageToken, sharedRadius, byPlaceName);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    selectPlace();
                    mPlace.setImageBitmap(utils.maskedIcon(typeIcons[typeNumber]));
                }
            }, 1500);
        });
        mPlace.setImageBitmap(utils.maskedIcon(typeIcons[typeNumber]));

        if (!isNetworkAvailable()) {
            Toast.makeText(mContext, "No Network Available", Toast.LENGTH_LONG).show();
        }
        tvVoice.setText(sharedVoice);
        tvAddress.setText(sharedLocation);
        final View v = findViewById(R.id.frame);
        v.post(() -> {
            utils.deleteOldLogFiles();
        });
        if (sharedAutoLoad) {
            new PlaceRetrieve(mContext, oLatitude, oLongitude, placeType, pageToken, sharedRadius, byPlaceName);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    selectPlace();
                }
            }, 15000);
        }

        show_logo();
        ImageView ivLogo = findViewById(R.id.logo);
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedLogo = (++sharedLogo) % 3;  // logo count
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putInt("logo", sharedLogo);
                editor.apply();
                show_logo();
            }
        });

        ImageView ivMap = findViewById(R.id.map);
        float opacity = (sharedMap) ? 1f: 0.3f;
        ivMap.setAlpha(opacity);
        ivMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sharedMap = !sharedMap;
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("map", sharedMap);
                editor.apply();
                float opacity = (sharedMap) ? 1f: 0.2f;
                ivMap.setAlpha(opacity);
            }
        });

        ImageView ivSet = findViewById(R.id.setting);
        ivSet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start_Setting();
            }
        });

    }

    private void start_Setting() {
        Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
//        startActivityForResult(intent);
        startActivity(intent);
    }

    private void selectPlace() {
        Intent intent = new Intent(MainActivity.this, SelectActivity.class);
        startActivity(intent);
    }

    private void show_logo() {
        sigMap = utils.buildSignatureMap();
        ImageView iv = findViewById(R.id.logo);
        iv.setImageBitmap(sigMap);
    }
    private void initiate_Variables() {

        utils = new Utils(this);
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        deviceOrientation = new DeviceOrientation();

        utils.getPreference();
        map_api_key = getString(R.string.maps_api_key);
        pageToken = NO_MORE_PAGE;
        placeInfos = new ArrayList<>();
        tvVoice = findViewById(R.id.textVoice);
        tvAddress = findViewById(R.id.placeAddress);

        typeInfos = new ArrayList<>();
        for (int i = 0; i < typeNames.length; i++) {
            typeInfos.add(new TypeInfo(typeNames[i], typeIcons[i]));
        }
        RecyclerView typeRecyclerView = findViewById(R.id.type_recycler);
        LinearLayoutManager mLinearLayoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        typeRecyclerView.setLayoutManager(mLinearLayoutManager);
        typeAdapter = new TypeAdapter(typeInfos);
        typeRecyclerView.setAdapter(typeAdapter);

    }

    static void inflateAddress() {
        mActivity.runOnUiThread(() -> {
            String s = strPlace + "\n" + strAddress;
            tvAddress.setText(s);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_RECOGNISE) {
            if (resultCode == RESULT_OK) {
                ArrayList<String> result = data
                        .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                strVoice = (strVoice + " " + result.get(0)).trim();
                tvVoice.setText(strVoice);
            }
        } else if (requestCode == SAVE_MAP) {
            save_GoogleMap(googleShot);
            checkIfExit();

        } else {
            Toast.makeText(mContext, "Request Code:" + requestCode + ", Result Code:" + resultCode + " not as expected", Toast.LENGTH_LONG).show();
        }
    }

    private void checkIfExit() {
        startCamera();
        strVoice = "";
        if (exitFlag) {
            finish();
            Intent intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.setType("image/gallery");
            intent.setAction(Intent.ACTION_PICK);
            startActivity(intent);
            new Timer().schedule(new TimerTask() {
                public void run() {
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }
            }, 5000);   // wait while photo generated
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cM = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo aNI = cM.getActiveNetworkInfo();
        return aNI != null && aNI.isConnected();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(deviceOrientation.getEventListener(), mAccelerometer, SensorManager.SENSOR_DELAY_UI);
        mSensorManager.registerListener(deviceOrientation.getEventListener(), mMagnetometer, SensorManager.SENSOR_DELAY_UI);
    }

    private void startActivityForResult(Intent intent) {
        show_logo();
    }

    private void startGetVoice() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());    //데이터 설정
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 10);   //검색을 말한 결과를 보여주는 갯수

        try {
            startActivityForResult(intent, VOICE_RECOGNISE);
        } catch (ActivityNotFoundException a) {
            //
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(ExifInterface.ORIENTATION_NORMAL, 0);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_90, 90);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_180, 180);
        ORIENTATIONS.append(ExifInterface.ORIENTATION_ROTATE_270, 270);
    }

    private void take_Picture() {

        int mDeviceRotation = ORIENTATIONS.get(deviceOrientation.getOrientation());
        if (mDeviceRotation == 0)
            cameraOrientation = 1;
        else if (mDeviceRotation == 180)
            cameraOrientation = 3;
        else if (mDeviceRotation == 90)
            cameraOrientation = 6;
        else
            cameraOrientation = 8;

        sharedLocation = tvAddress.getText().toString();

        try {
            strPlace = sharedLocation.substring(0, sharedLocation.indexOf("\n"));
            if (strPlace.equals("")) {
                strPlace = " ";
            }
            strAddress = sharedLocation.substring(sharedLocation.indexOf("\n") + 1);
        } catch (Exception e) {
            strPlace = strAddress;
            strAddress = "?";
        }
        strVoice = tvVoice.getText().toString();
        if (strVoice.length() < 1)
            strVoice = " ";
        tvVoice.setText("");
        sharedVoice = strVoice;
        utils.putPlacePreference();

        mCamera.takePicture(null, null, rawCallback, jpegCallback); // null is for silent shot
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            //			 Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            //			 Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            cameraImage = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            new SaveImageTask().execute("");
        }
    };

    private class SaveImageTask extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String... data) {
            mCamera.stopPreview();
            mCamera.release();

            now_time = System.currentTimeMillis();
            BuildBitMap buildBitMap = new BuildBitMap(cameraImage, oLatitude, oLongitude, oAltitude, mActivity, mContext, cameraOrientation);
            buildBitMap.makeOutMap(strVoice, strPlace, strAddress, sharedWithPhoto, now_time,"");
            return "";
        }

        @Override
        protected void onPostExecute(String none) {

            if (sharedMap) {
                googleShot = null;
                Intent intent = new Intent(mContext, LandActivity.class);
                intent.putExtra("lan", oLatitude);
                intent.putExtra("lon", oLongitude);
                intent.putExtra("alt", oAltitude);
                intent.putExtra("zoom", 15);
                startActivityForResult(intent, SAVE_MAP);
            } else {
                checkIfExit();
            }
        }
    }

    private static void save_GoogleMap(Bitmap googleShot) {
        BuildBitMap buildBitMap = new BuildBitMap(googleShot, oLatitude, oLongitude, oAltitude, mActivity, mContext, cameraOrientation);
        buildBitMap.makeOutMap(strVoice, strPlace, strAddress, sharedWithPhoto, now_time, "Map");
    }

    public void startCamera() {

        if (mCameraPreview == null) {
            mCameraPreview = new CameraPreview(this, (SurfaceView) findViewById(R.id.camera_surface));
            mCameraPreview.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT));
            ((FrameLayout) findViewById(R.id.frame)).addView(mCameraPreview);
            mCameraPreview.setKeepScreenOn(true);
        }

        mCameraPreview.setCamera(null);
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
        mCamera = Camera.open(0);
//        try {
//            // camera cameraOrientation
//            mCamera.setDisplayOrientation(90);
//
//        } catch (RuntimeException ex) {
//            Toast.makeText(getApplicationContext(), "camera cameraOrientation " + ex.getMessage(),
//                    Toast.LENGTH_LONG).show();
//            utils.log(logID, "CAMERA not found " + ex.getMessage());
//        }
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(0);
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
        for (Camera.Size size : params.getSupportedPictureSizes()) {
            float ratio = (float) size.width / (float) size.height;
            if (size.width >= 1920 && ratio > 1.4 && ratio < 1.8) {
                params.setPictureSize(size.width, size.height);
                break;
            }
        }
        mCamera.setParameters(params);
        mCamera.startPreview();
        mCameraPreview.setCamera(mCamera);
    }

    // ↓ ↓ ↓ P E R M I S S I O N   RELATED /////// ↓ ↓ ↓ ↓  BEST CASE 20/09/27 with no lambda
    private final static int ALL_PERMISSIONS_RESULT = 101;
    ArrayList permissionsToRequest;
    ArrayList<String> permissionsRejected = new ArrayList<>();
    String [] permissions;

    private void askPermission() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(getApplicationContext().getPackageName(), PackageManager.GET_PERMISSIONS);
            permissions = info.requestedPermissions;//This array contain
        } catch (Exception e) {
            Log.e("Permission", "Not done", e);
        }

        permissionsToRequest = findUnAskedPermissions();
        if (permissionsToRequest.size() != 0) {
            requestPermissions((String[]) permissionsToRequest.toArray(new String[0]),
//            requestPermissions(permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    ALL_PERMISSIONS_RESULT);
        }
    }

    private ArrayList findUnAskedPermissions() {
        ArrayList <String> result = new ArrayList<>();
        for (String perm : permissions) if (hasPermission(perm)) result.add(perm);
        return result;
    }
    private boolean hasPermission(String permission) {
        return (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ALL_PERMISSIONS_RESULT) {
            for (Object perms : permissionsToRequest) {
                if (hasPermission((String) perms)) {
                    permissionsRejected.add((String) perms);
                }
            }
            if (permissionsRejected.size() > 0) {
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
                if (shouldShowRequestPermissionRationale(permissionsRejected.get(0))) {
                    String msg = "These permissions are mandatory for the application. Please allow access.";
                    showDialog(msg);
                }
            }
        }
    }
    private void showDialog(String msg) {
        showMessageOKCancel(msg, (dialog, which)
                -> MainActivity.this.requestPermissions(permissionsRejected.toArray(
                        new String[0]), ALL_PERMISSIONS_RESULT));
    }
    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new android.app.AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    void setFullScreen() {
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            ActionBar ab = getSupportActionBar();
            ab.hide();
            WindowInsetsController controller = getWindow().getInsetsController();
            if (controller != null) {
                controller.hide(WindowInsets.Type.statusBars());
                controller.setSystemBarsBehavior(
                        WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
//        }
//        controller.hide(WindowInsets.Type.statusBars() |
//                WindowInsets.Type.navigationBars());
    }

// ↑ ↑ ↑ ↑ P E R M I S S I O N    RELATED /////// ↑ ↑ ↑

}