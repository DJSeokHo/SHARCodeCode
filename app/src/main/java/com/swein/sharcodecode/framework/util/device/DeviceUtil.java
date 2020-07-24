package com.swein.sharcodecode.framework.util.device;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class DeviceUtil {

    public static void vibrate(Activity activity, long milliseconds) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);

        if(vibrator != null) {
            if(vibrator.hasVibrator()) {
                vibrator.vibrate(milliseconds);
            }
        }
    }

    public static float getScreenCenterX(Context context) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        if(windowManager == null) {
            return 0;
        }
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.widthPixels * 0.5f;
    }

    public static float getScreenCenterY(Context context) {
        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics displayMetrics = new DisplayMetrics();

        if(windowManager == null) {
            return 0;
        }
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        return displayMetrics.heightPixels * 0.5f;
    }
}
