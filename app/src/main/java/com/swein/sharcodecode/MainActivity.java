package com.swein.sharcodecode;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.ar.core.ArCoreApk;
import com.swein.sharcodecode.arpart.constants.ARConstants;
import com.swein.sharcodecode.arpart.recordlist.ARRecordListActivity;
import com.swein.sharcodecode.framework.module.basicpermission.BasicPermissionActivity;
import com.swein.sharcodecode.framework.module.basicpermission.PermissionManager;
import com.swein.sharcodecode.framework.module.basicpermission.RequestPermission;
import com.swein.sharcodecode.framework.util.activity.ActivityUtil;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.thread.ThreadUtil;

/**
 * ARCore 核⼼类介绍
 *
 * Session
 * com.google.ar.core.Session类，Session管理AR系统状态并处理Session⽣命周期。 该类是
 * ARCore API的主要⼊⼝点。 该类允许⽤户创建Session，配置Session，启动/停⽌Session，最重要
 * 的是接收视频帧，以允许访问Camera图像和设备姿势。
 *
 * Config
 * com.google.ar.core.Config类，⽤于保存Session的设置。
 *
 * Frame
 * com.google.ar.core.Frame类，该类通过调⽤update()⽅法，获取状态信息并更新AR系统。
 *
 * HitResult
 * com.google.ar.core.HitResult类，该类定义了命中点射线与估算的真实⼏何世界之间的交集。
 *
 * Point
 * com.google.ar.core.Point类，它代表ARCore正在跟踪的空间点。 它是创建锚点（调⽤
 * createAnchor⽅法）时，或者进⾏命中检测（调⽤hitTest⽅法）时，返回的结果。
 *
 * PointCloud
 * com.google.ar.core.PointCloud类，它包含⼀组观察到的3D点和信⼼值。
 *
 * Plane
 * com.google.ar.core.Plane类，描述了现实世界平⾯表⾯的最新信息。
 *
 * Anchor
 * com.google.ar.core.Anchor类，描述了现实世界中的固定位置和⽅向。 为了保持物理空间的固定
 * 位置，这个位置的数字描述信息将随着ARCore对空间的理解的不断改进⽽更新。
 *
 * Pose
 * com.google.ar.core.Pose类, 姿势表示从⼀个坐标空间到另⼀个坐标空间位置不变的转换。 在所
 * 有的ARCore API⾥，姿势总是描述从对象本地坐标空间到世界坐标空间的转换。

 * 随着ARCore对环境的了解不断变化，它将调整坐标系模式以便与真实世界保持⼀致。 这时，
 * Camera和锚点的位置（坐标）可能会发⽣明显的变化，以便它们所代表的物体处理恰当的位置。
 * 这意味着，每⼀帧图像都应被认为是在⼀个完全独⽴的世界坐标空间中。锚点和Camera的坐标不应
 * 该在渲染帧之外的地⽅使⽤，如果需考虑到某个位置超出单个渲染框架的范围，则应该创建⼀个锚
 * 点或者应该使⽤相对于附近现有锚点的位置。
 *
 * ImageMetadata
 * com.google.ar.core.ImageMetadata类，提供了对Camera图像捕捉结果的元数据的访问。
 *
 * LightEstimate
 * com.google.ar.core.LightEstimate保存关于真实场景光照的估计信息。 通过 getLightEstimate()
 *
 * 得到。
 */
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

        Button button = findViewById(R.id.button);

        button.setOnClickListener(view -> startAR());

        button.performClick();
    }

    private void checkDeviceSupportAR() {
        ArCoreApk.Availability availability = ArCoreApk.getInstance().checkAvailability(this);
        if (availability.isTransient()) {
            // Re-query at 5Hz while compatibility is checked in the background.

            ThreadUtil.startUIThread(200, this::checkDeviceSupportAR);
        }

        if (availability.isSupported()) {
            ILog.iLogDebug(TAG, "ar is ok");
            ActivityUtil.startNewActivityWithoutFinish(this, ARRecordListActivity.class);
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
        if (Double.parseDouble(openGlVersionString) < ARConstants.MIN_OPEN_GL_VERSION) {
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