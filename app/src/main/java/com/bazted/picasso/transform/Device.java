package com.bazted.picasso.transform;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Point;
import android.media.MediaScannerConnection;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.opengl.GLES10;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.view.Display;
import android.view.WindowManager;

import javax.microedition.khronos.opengles.GL10;
import java.io.*;

public class Device {
    public static final int SCREEN_WIDTH = 0;
    private static final int SCREEN_HEIGHT = 1;
    private static final String MAX_TEXTURE = "MAX_TEXTURE";

    public static int getDeviceParam(Context context, int SCREEN_PARAMETER) {
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        if (Build.VERSION.SDK_INT > 12) {
            display.getSize(size);
        } else {
            size.x = display.getWidth();
            size.y = display.getHeight();
        }

        if (SCREEN_PARAMETER == SCREEN_WIDTH) {
            return size.x;
        } else {
            return size.y;
        }

    }

    public static int getAvailableScreenHeight(Context context) {
        int height = getDeviceParam(context, SCREEN_HEIGHT);

        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) {
            height -= context.getResources().getDimensionPixelSize(resId);
        }

        return height;


    }

    public static boolean isOnline(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return (networkInfo != null && networkInfo.isConnected());
    }


/*
    public static void showDensity(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
//        Log.e("screen=",
//                Device.getDeviceParam(context,
//                        Device.SCREEN_HEIGHT)
//                        + "*"
//                        + Device.getDeviceParam(context,
//                        Device.SCREEN_WIDTH) + "density=" + density);

        Toast.makeText(
                context,
                Device.getDeviceParam(context,
                        Device.SCREEN_HEIGHT)
                        + "*"
                        + Device.getDeviceParam(context,
                        Device.SCREEN_WIDTH) + "density=" + density,
                Toast.LENGTH_LONG).show();
    }*/

    public static void refreshSd(File file, Context context) {
        MediaScannerConnection.scanFile(
                context,
                new String[]{file.getAbsolutePath()},
                null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    @Override
                    public void onScanCompleted(String path, Uri uri) {
                        // file was scanned
                    }
                }
        );
    }

    /**
     * returns true if AlwaysFinishActivities option is enabled/checked
     */

    public static boolean isAlwaysFinishActivitiesOptionEnabled(Context context) {
        int alwaysFinishActivitiesInt;
        if (Build.VERSION.SDK_INT >= 17) {
            alwaysFinishActivitiesInt = Settings.System.getInt(context.getContentResolver(), Settings.Global.ALWAYS_FINISH_ACTIVITIES, 0);
        } else {
            alwaysFinishActivitiesInt = Settings.System.getInt(context.getContentResolver(), Settings.System.ALWAYS_FINISH_ACTIVITIES, 0);
        }

        return alwaysFinishActivitiesInt == 1;
    }

    public static int maxTextureSize(Context context) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        int maxTexture = sharedPreferences.getInt(MAX_TEXTURE, -1);
        int deviceHeight = getDeviceParam(context, SCREEN_HEIGHT);
        if (maxTexture == -1 || maxTexture == deviceHeight) {
            int[] maxTextureSize = new int[1];
            GLES10.glGetIntegerv(GL10.GL_MAX_TEXTURE_SIZE, maxTextureSize, 0);
            maxTexture = Math.max(maxTextureSize[0], deviceHeight);
            sharedPreferences.edit().putInt(MAX_TEXTURE, maxTexture).commit();
        }
        return maxTexture;

    }

    public static String getRealPathFromURI(Context context, String uriToParse) {
        if (uriToParse != null) {
            if (uriToParse.startsWith("content")) {
                Uri contentUri = Uri.parse(uriToParse);
                Cursor cursor = null;
                try {
                    String[] proj = {MediaStore.Images.Media.DATA};
                    cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
                    if (cursor != null) {
                        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                        cursor.moveToFirst();
                        String string = cursor.getString(column_index);
                        if (string != null) {
                            return string;
                        }
                    }
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } else if (uriToParse.startsWith("file")) {
                Uri contentUri = Uri.parse(uriToParse);
                return contentUri.getPath();
            }
        }

        return null;
    }

    public static String getBase64ofFile(final String absoluteFilePath) {
        if (absoluteFilePath != null) {
            try {
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                InputStream inputStream = new FileInputStream(absoluteFilePath);
                byte[] bytes;
                byte[] buffer = new byte[inputStream.available()];
                int bytesRead;
                try {
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        output.write(buffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                bytes = output.toByteArray();

                return Base64.encodeToString(bytes, Base64.DEFAULT);


            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
