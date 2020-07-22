package com.swein.sharcodecode.framework.util.device;

import android.app.Activity;
import android.app.Service;
import android.os.Vibrator;

public class DeviceUtil {

    public static void vibrate(Activity activity, long milliseconds) {
        Vibrator vibrator = (Vibrator) activity.getSystemService(Service.VIBRATOR_SERVICE);

        if(vibrator != null) {
            if(vibrator.hasVibrator()) {
                vibrator.vibrate(milliseconds);
            }
        }
    }

}
