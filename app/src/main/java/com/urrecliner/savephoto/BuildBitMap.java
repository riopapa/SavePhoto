package com.urrecliner.savephoto;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import androidx.exifinterface.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static com.urrecliner.savephoto.Vars.sharedAlpha;

class BuildBitMap {

    private long nowTime;
    private static final SimpleDateFormat sdfExif = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.KOREA);
    private final SimpleDateFormat sdfFileName = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.KOREA);
    String phonePrefix = "";
    String sFood, sPlace, sAddress, sLatLng;
    double latitude, longitude, altitude;
    Bitmap outBitmap, signatureMap;
    Activity activity;
    Context context;
    int cameraOrientation;

    public BuildBitMap(Bitmap outBitmap, double latitude, double longitude, double altitude, Activity activity, Context context, int cameraOrientation) {
        this.latitude = latitude; this.longitude = longitude; this.altitude = altitude;
        this.outBitmap = outBitmap;
        this.activity = activity;this.context = context;
        this.cameraOrientation = cameraOrientation;
        this.signatureMap = buildSignatureMap();
        sLatLng = String.format(Locale.ENGLISH, "%.5f, %.5f ; %.1f", latitude, longitude, altitude);
    }

    void makeOutMap(String sFood, String sName, String sAddress, boolean withPhoto) {
        this.sFood = sFood; this.sPlace = sName; this.sAddress = sAddress;
        nowTime = System.currentTimeMillis();
        int width = outBitmap.getWidth();
        int height = outBitmap.getHeight();
        if (cameraOrientation == 6 && width > height)
            outBitmap = rotateBitMap(outBitmap, 90);
        if (cameraOrientation == 1 && width < height)
            outBitmap = rotateBitMap(outBitmap, 90);
        if (cameraOrientation == 3)
            outBitmap = rotateBitMap(outBitmap, 180);
//        if (Build.MODEL.equals("nexus 6P"))
//            phonePrefix = "IMG_";

        if (withPhoto) {
            String outFileName = sdfFileName.format(nowTime);
            File newFile = new File(getPublicCameraDirectory(), phonePrefix + outFileName + ".jpg");
            writeCameraFile(outBitmap, newFile);
            setNewFileExif(newFile);
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)));
        }

        Bitmap mergedMap = markDateLocSignature(outBitmap, nowTime);
        nowTime += 150;
        String foodName = sFood.trim();
        if (foodName.length() > 2)
            foodName = "(" + foodName +")";
        String outFileName2 = sdfFileName.format(nowTime) + "_" + sPlace + foodName;
        File newFile2 = new File(getPublicCameraDirectory(), phonePrefix + outFileName2 + " _ha.jpg");
        writeCameraFile(mergedMap, newFile2);
        setNewFileExif(newFile2);
        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile2)));
    }

    File getPublicCameraDirectory() {
        return new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM),"/Camera");
    }

    private void setNewFileExif(File fileHa) {
        ExifInterface exifHa;

        try {
            exifHa = new ExifInterface(fileHa.getAbsolutePath());
            exifHa.setAttribute(ExifInterface.TAG_MAKE, Build.MANUFACTURER);
            exifHa.setAttribute(ExifInterface.TAG_MODEL, Build.MODEL);
            exifHa.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertGPS2DMS(latitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, latitudeGPS2DMS(latitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertGPS2DMS(longitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, longitudeGPS2DMS(longitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, convertALT2DMS(altitude));
            exifHa.setAttribute(ExifInterface.TAG_GPS_ALTITUDE_REF, (altitude> 0)? "0":"1");
            exifHa.setAttribute(ExifInterface.TAG_ORIENTATION, "1");
            exifHa.setAttribute(ExifInterface.TAG_DATETIME, sdfExif.format(nowTime));
            exifHa.setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, "Save Photo by riopapa");
            exifHa.saveAttributes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String latitudeGPS2DMS(double latitude) {
        return latitude < 0.0d ? "S" : "N";
    }

    private String longitudeGPS2DMS(double longitude) {
        return longitude < 0.0d ? "W" : "E";
    }

    private static String convertGPS2DMS(double latitude) {
        latitude = Math.abs(latitude);
        int degree = (int) latitude;
        latitude *= 60;
        latitude -= (degree * 60.0d);
        int minute = (int) latitude;
        latitude *= 60;
        latitude -= (minute * 60.0d);
        int second = (int) (latitude * 10000.d);
        return degree + "/1," + minute + "/1," + second + "/10000";
    }

    private static String convertALT2DMS(double altitude) {
        return ""+((altitude > 0) ? altitude:-altitude);
    }

    Bitmap markDateLocSignature(Bitmap photoMap, long timeStamp) {
        int photoWidth = photoMap.getWidth();
        int photoHeight = photoMap.getHeight();
        Bitmap newMap = Bitmap.createBitmap(photoWidth, photoHeight, photoMap.getConfig());
        Canvas canvas = new Canvas(newMap);
        canvas.drawBitmap(photoMap, 0f, 0f, null);
        markDateTime(timeStamp, photoWidth, photoHeight, canvas);
        markSignature(photoWidth, photoHeight, canvas);
        markFoodPlaceAddress(photoWidth, photoHeight, canvas);
        return newMap;
    }

    private void markFoodPlaceAddress(int width, int height, Canvas canvas) {

        int xPos = width / 2;
        int fontSize = (height + width) / 48;  // gps
        int yPos = height - fontSize;
        if (width < height)
            yPos -= fontSize;
//        yPos = drawTextOnCanvas(canvas, sLatLng, fontSize, xPos, yPos);
//        fontSize = fontSize * 14 / 10;  // address
//        yPos -= fontSize + fontSize / 6;
        yPos = drawTextOnCanvas(canvas, sAddress, fontSize, xPos, yPos);
        fontSize = fontSize * 12 / 10;  // Place
        yPos -= fontSize + fontSize / 4;
        yPos = drawTextOnCanvas(canvas, sPlace, fontSize, xPos, yPos);
        yPos -= fontSize + fontSize / 4; // food
        drawTextOnCanvas(canvas, sFood, fontSize, xPos, yPos);
    }

    private void markDateTime(long timeStamp, int width, int height, Canvas canvas) {
        final SimpleDateFormat sdfDate = new SimpleDateFormat("`yy/MM/dd(EEE)", Locale.KOREA);
        final SimpleDateFormat sdfHourMin = new SimpleDateFormat("HH:mm", Locale.KOREA);
        int fontSize = (width>height) ? (width+height)/50 : (width+height)/70;  // date time
        String dateTime = sdfDate.format(timeStamp);
        int xPos = (width>height) ? width/10+fontSize: width/6+fontSize;
        int yPos = (width>height) ? height/10: height/12;
        drawTextOnCanvas(canvas, dateTime, fontSize, xPos, yPos);
        yPos += fontSize;
        dateTime = sdfHourMin.format(timeStamp);
        fontSize = fontSize * 8 / 9;
        drawTextOnCanvas(canvas, dateTime, fontSize, xPos, yPos);
    }

    private  void markSignature(int width, int height, Canvas canvas) {
        int sigSizeX = signatureMap.getWidth();
        int sigSizeY = signatureMap.getHeight();
//        Bitmap sigMap = Bitmap.createScaledBitmap(signatureMap, sigSizeX, sigSizeY, false);
        int xPos = width - sigSizeX - sigSizeX / 2;
        int yPos = (width>height) ? height/20: height/22;
        Paint paint = new Paint(); paint.setAlpha(Integer.parseInt(sharedAlpha));
        canvas.drawBitmap(signatureMap, xPos, yPos, null);
    }

    private int drawTextOnCanvas(Canvas canvas, String text, int fontSize, int xPos, int yPos) {
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setTextSize(fontSize);
        int cWidth = canvas.getWidth() * 3 / 4;
        float tWidth = paint.measureText(text);
        int pos;
        if (tWidth > cWidth) {
            int length = text.length() / 2;
            for (pos = length; pos < text.length(); pos++)
                if (text.startsWith(" ", pos))
                    break;
            String text1 = text.substring(pos);
            drawOutLinedText(canvas, text1, xPos, yPos, fontSize);
            yPos -= fontSize + fontSize / 4;
            text1 = text.substring(0, pos);
            drawOutLinedText(canvas, text1, xPos, yPos, fontSize);
            return yPos;
        }
        else
            drawOutLinedText(canvas, text, xPos, yPos, fontSize);
        return yPos;
    }

    private void drawOutLinedText(Canvas canvas, String text, int xPos, int yPos, int textSize) {

        int color = ContextCompat.getColor(context, R.color.infoColor);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        paint.setTypeface(Typeface.DEFAULT_BOLD);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setAntiAlias(true);
        paint.setTextSize(textSize);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setStrokeWidth((float)textSize/5+3);
        paint.setTypeface(context.getResources().getFont(R.font.the_jamsil));
        canvas.drawText(text, xPos, yPos, paint);

        paint.setColor(color);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawText(text, xPos, yPos, paint);
    }

    private void writeCameraFile(Bitmap bitmap, File file) {
        FileOutputStream os;
        try {
            os = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.close();
            activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file)));
        } catch (IOException e) {
            Log.e("ioException", e.toString());
            Toast.makeText(context, e.toString(),Toast.LENGTH_LONG).show();
        }
    }

    Bitmap buildSignatureMap() {
        Bitmap sigMap;
        File sigFile = new File (Environment.getExternalStorageDirectory(),"signature.png");
        if (sigFile.exists()) {
            sigMap = BitmapFactory.decodeFile(sigFile.toString(), null);
        }
        else
            sigMap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.digital_logo);
        Bitmap newBitmap = Bitmap.createBitmap(sigMap.getWidth(), sigMap.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(sigMap, 0, 0, null);
        return newBitmap;
    }

    Bitmap rotateBitMap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
    }

}
