package com.swein.sharcodecode;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.swein.sharcodecode.arpart.ARActivity;
import com.swein.sharcodecode.constants.Constants;
import com.swein.sharcodecode.framework.module.basicpermission.BasicPermissionActivity;
import com.swein.sharcodecode.framework.module.basicpermission.PermissionManager;
import com.swein.sharcodecode.framework.module.basicpermission.RequestPermission;
import com.swein.sharcodecode.framework.util.activity.ActivityUtil;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.thread.ThreadUtil;

public class MainActivity extends BasicPermissionActivity {

    private final static String TAG = "MainActivity";

    // Set to true ensures requestInstall() triggers installation if necessary.
    private boolean userRequestedInstall = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(!checkIsSupportedDeviceOrFinish(this)) {
            // device not supports OpenGL ES 3.0
            finish();
        }

        findViewById(R.id.button).setOnClickListener(view -> startAR());
    }

    private void checkDeviceSupportAR() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.

            ThreadUtil.startUIThread(200, this::checkDeviceSupportAR);
        }

        if (availability.isSupported()) {
            ILog.iLogDebug(TAG, "ar is ok");
            ActivityUtil.startNewActivityWithoutFinish(this, ARActivity.class);
        }
        else {
            ILog.iLogDebug(TAG, "ar is not ok");
            finish();
        }
    }



    /**
     * Returns false and displays an error message if Sceneform can not run, true if Sceneform can run
     * on this device.
     *
     * <p>Sceneform requires Android N on the device as well as OpenGL 3.0 capabilities.
     *
     * <p>Finishes the activity if Sceneform can not run
     */
    public static boolean checkIsSupportedDeviceOrFinish(final Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            Log.e(TAG, "Sceneform requires Android N or later");
            Toast.makeText(activity, "Sceneform requires Android N or later", Toast.LENGTH_LONG).show();
            activity.finish();
            return false;
        }

        String openGlVersionString = ((ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE))
                        .getDeviceConfigurationInfo()
                        .getGlEsVersion();
        if (Double.parseDouble(openGlVersionString) < Constants.MIN_OPEN_GL_VERSION) {
            Log.e(TAG, "Sceneform requires OpenGL ES 3.0 later");
            Toast.makeText(activity, "Sceneform requires OpenGL ES 3.0 or later", Toast.LENGTH_LONG)
                    .show();
            activity.finish();
            return false;
        }
        return true;
    }

    @RequestPermission(permissionCode= PermissionManager.PERMISSION_REQUEST_CAMERA_CODE)
    private void startAR() {
        if(PermissionManager.getInstance().requestPermission(this, true, PermissionManager.PERMISSION_REQUEST_CAMERA_CODE,
                Manifest.permission.CAMERA)) {
            ILog.iLogDebug(TAG, "startAR");
            checkDeviceSupportAR();
        }
    }

}