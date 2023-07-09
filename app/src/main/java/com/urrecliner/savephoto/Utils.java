package com.urrecliner.savephoto;

import static com.urrecliner.savephoto.Vars.mContext;
import static com.urrecliner.savephoto.Vars.sharedAlpha;
import static com.urrecliner.savephoto.Vars.sharedAutoLoad;
import static com.urrecliner.savephoto.Vars.sharedLocation;
import static com.urrecliner.savephoto.Vars.sharedLogo;
import static com.urrecliner.savephoto.Vars.sharedMap;
import static com.urrecliner.savephoto.Vars.sharedPref;
import static com.urrecliner.savephoto.Vars.sharedRadius;
import static com.urrecliner.savephoto.Vars.sharedSortType;
import static com.urrecliner.savephoto.Vars.sharedWithPhoto;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Collator;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

class Utils {

    Context context;
    final private String PREFIX = "log_";

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd", Locale.US);
    private final SimpleDateFormat dateTimeLogFormat = new SimpleDateFormat("MM-dd HH.mm.ss sss", Locale.US);

    public Utils (Context context) {
        this.context = context;
    }

    void log(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String log = traceName(traces[5].getMethodName()) + traceName(traces[4].getMethodName()) + traceClassName(traces[3].getClassName())+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " {"+ tag + "} " + text;
        Log.w(tag , log);
        append2file(dateTimeLogFormat.format(new Date())+" " +log);
    }

    private String traceName (String s) {
        if (s.equals("performResume") || s.equals("performCreate") || s.equals("callActivityOnResume") || s.equals("access$1200")
                || s.equals("access$000") || s.equals("handleReceiver"))
            return "";
        else
            return s + "> ";
    }
    private String traceClassName(String s) {
        return s.substring(s.lastIndexOf(".")+1);
    }

    void logE(String tag, String text) {
        StackTraceElement[] traces;
        traces = Thread.currentThread().getStackTrace();
        String log = traceName(traces[5].getMethodName()) + traceName(traces[4].getMethodName()) + traceClassName(traces[3].getClassName())+"> "+traces[3].getMethodName() + "#" + traces[3].getLineNumber() + " {"+ tag + "} " + text;
        Log.e("<" + tag + ">" , log);
        append2file(dateTimeLogFormat.format(new Date())+" : " +log);
    }

    private void append2file(String textLine) {

        File directory = getPackageDirectory();
        if (!directory.exists()) {
            boolean ok = directory.mkdirs();
            Log.w("Dire",""+ok);
        }
        BufferedWriter bw = null;
        FileWriter fw = null;
        String fullName = directory + "/" + PREFIX + dateFormat.format(new Date())+".txt";
        try {
            File file = new File(fullName);
            if (!file.exists()) {
                if (!file.createNewFile()) {
                    logE("createFile", " Error");
                }
            }
            String outText = "\n"+textLine+"\n";
            fw = new FileWriter(file.getAbsoluteFile(), true);
            bw = new BufferedWriter(fw);
            bw.write(outText);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) bw.close();
                if (fw != null) fw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    File getPackageDirectory() {
        File directory = new File(Environment.getExternalStorageDirectory(), context.getResources().getString(R.string.app_name));
        try {
            if (!directory.exists()) {
                if(directory.mkdirs()) {
                    Log.e("mkdirs","Failed "+directory);
                }
            }
        } catch (Exception e) {
            Log.e("creating Directory error", directory + "_" + e);
        }
        return directory;
    }

    void getPreference() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        sharedRadius = sharedPref.getString("radius", "");
        if (sharedRadius.equals("")) {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("radius", "200");
            editor.putBoolean("autoLoad", true);
            editor.putString("sort", "none");
            editor.putString("alpha", "163");
            editor.putBoolean("WithPhoto", true);
            editor.putInt("logo", 0);
            editor.apply();
//            editor.commit();
        }
        sharedRadius = sharedPref.getString("radius", "200");
        sharedAutoLoad = sharedPref.getBoolean("autoLoad", false);
        sharedSortType = sharedPref.getString("sort", "none");
        sharedAlpha = sharedPref.getString("alpha", "163");
        sharedLocation = sharedPref.getString("location","");
        sharedWithPhoto = sharedPref.getBoolean("WithPhoto", true);
        sharedLogo = sharedPref.getInt("logo", 0);
        sharedMap = sharedPref.getBoolean("map", true);
    }

    void putPlacePreference() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("location", sharedLocation);
        editor.apply();

    }

    void deleteOldLogFiles() {

        String oldDate = PREFIX + dateFormat.format(System.currentTimeMillis() - 3*24*60*60*1000L);
        File packageDirectory = getPackageDirectory();
        File[] files = packageDirectory.listFiles();
        Collator myCollator = Collator.getInstance();
        if (files != null) {
            for (File file : files) {
                String shortFileName = file.getName();
                if (myCollator.compare(shortFileName, oldDate) < 0) {
                    if (file.delete())
                        Log.e("file", "Delete Error " + file);
                }
            }
        }
    }

    Bitmap maskedIcon(int rawId) {

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), rawId);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        for(int x = 0;x < pixels.length;++x){
            if(pixels[x] != 0){
                pixels[x] = Color.YELLOW;
            }
        }

        Bitmap bm = Bitmap.createBitmap(pixels, w, h, Bitmap.Config.ARGB_8888);
        Bitmap resultingImage = bm.copy(Bitmap.Config.ARGB_8888, true);
        Paint paint = new Paint();
        Canvas canvas = new Canvas(resultingImage);
        canvas.drawBitmap(resultingImage, 0, 0, paint);

        Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, w-16, h-16, false);
//        paint = new Paint();
////        canvas = new Canvas(resultingImage);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DARKEN));
        canvas.drawBitmap(resizedBitmap,8,8,paint);
        return resultingImage;
    }

//
//    private  String getAppLabel(Context context) {
//        PackageManager packageManager = context.getPackageManager();
//        ApplicationInfo applicationInfo = null;
//        try {
//            applicationInfo = packageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
//        } catch (final PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
//    }
    public Bitmap buildSignatureMap() {
        int [] logos = {R.mipmap.signature, R.mipmap.digital_logo, R.mipmap.gglogo};

        Bitmap sigMap;
        File sigFile = new File (Environment.getExternalStorageDirectory(),"signature.png");
        if (sigFile.exists()) {
            sigMap = BitmapFactory.decodeFile(sigFile.toString(), null);
        }
        else
            sigMap = BitmapFactory.decodeResource(context.getResources(), logos[sharedLogo]);

        int width = sigMap.getWidth();
        int height = sigMap.getHeight();
        Bitmap newMap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newMap);
        Paint p = new Paint();
        p.setAlpha(120);
        canvas.drawBitmap(sigMap, 0, 0, p);
        width = 240;
        height = 240 * sigMap.getHeight() / sigMap.getWidth();
        return Bitmap.createScaledBitmap(newMap, width, height, false);
    }
}
