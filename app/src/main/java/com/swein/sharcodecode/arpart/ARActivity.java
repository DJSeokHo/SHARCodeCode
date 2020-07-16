package com.swein.sharcodecode.arpart;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.toast.ToastUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ARActivity extends AppCompatActivity {

    private final static String TAG = "ARActivity";

    private ArSceneView arSceneView;

    private TextView textView;

    private List<AnchorNode> anchorNodeList = new ArrayList<>();

    private Node centerNode;

    private float centerX;
    private float centerY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        centerX = (float)dm.widthPixels * 0.5f;
        centerY= (float)dm.heightPixels * 0.5f;


        arSceneView = findViewById(R.id.arSceneView);
        textView = findViewById(R.id.textView);

        // Set a touch listener on the Scene to listen for taps.
        arSceneView.getScene().setOnTouchListener(
                        (HitTestResult hitTestResult, MotionEvent event) -> {

                            ILog.iLogDebug(TAG, "tab");
                            ILog.iLogDebug(TAG, hitTestResult.getDistance());

                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return false;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return false;
                            }

                            // 循环 Detect 每一帧
                            for (Plane plane : frame.getUpdatedTrackables(Plane.class)) {
                                if (plane.getTrackingState() == TrackingState.TRACKING) {
                                    // find plane
                                    ILog.iLogDebug(TAG, plane.getCenterPose());
                                    ILog.iLogDebug(TAG, plane.getExtentX());
                                    ILog.iLogDebug(TAG, plane.getExtentZ());
                                    ILog.iLogDebug(TAG, plane.getPolygon());
                                    ILog.iLogDebug(TAG, hitTestResult.getDistance());
                                }
                            }

//                            List<HitResult> hitTestResultList = frame.hitTest(event.getX(), event.getY());
                            List<HitResult> hitTestResultList = frame.hitTest(centerX, centerY);

                            for (HitResult hitResult : hitTestResultList) {
                                Anchor anchor = hitResult.getTrackable().createAnchor(hitResult.getHitPose());
//                                Anchor anchor = hitResult.createAnchor();


                                ILog.iLogDebug(TAG, hitResult.getDistance());


                                makeCube(anchor);

                                if(anchorNodeList.size() >= 2) {
                                    AnchorNode start = anchorNodeList.get(0);
                                    AnchorNode end = anchorNodeList.get(anchorNodeList.size() - 1);

                                    float dx = start.getAnchor().getPose().tx() - end.getAnchor().getPose().tx();
                                    float dy = start.getAnchor().getPose().ty() - end.getAnchor().getPose().ty();
                                    float dz = start.getAnchor().getPose().tz() - end.getAnchor().getPose().tz();

                                    float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
                                    ILog.iLogDebug(TAG, "distanceMeters is " + distanceMeters);

                                }

                                break;
                            }

                            switch (event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    ILog.iLogDebug(TAG, "ACTION_DOWN");
                                    break;

                                case MotionEvent.ACTION_UP:
                                    ILog.iLogDebug(TAG, "ACTION_UP");
                                    break;

                                case MotionEvent.ACTION_MOVE:
                                    ILog.iLogDebug(TAG, "ACTION_MOVE");
                                    break;
                            }
                            // Otherwise return false so that the touch event can propagate to the scene.
                            return false;
                        });

        // Set an update listener on the Scene that will hide the loading message once a Plane is
        // detected.
        arSceneView.getScene().addOnUpdateListener(
                        frameTime -> {
                            // get camera frame when find a plan
                            Frame frame = arSceneView.getArFrame();
                            if (frame == null) {
                                return;
                            }

                            if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                                return;
                            }

                            Collection<Plane> planeCollection = frame.getUpdatedTrackables(Plane.class);

                            for (Plane plane : planeCollection) {
                                if (plane.getTrackingState() == TrackingState.TRACKING) {
                                    // find plane
                                    textView.setVisibility(View.GONE);
                                }
                            }

                            List<HitResult> hitTestResultList = frame.hitTest(centerX, centerY);

                            for (HitResult hitResult : hitTestResultList) {
//                                Anchor anchor = hitResult.getTrackable().createAnchor(hitResult.getHitPose());
////                                Anchor anchor = hitResult.createAnchor();

                                // real-time distance
//                                ILog.iLogDebug(TAG, hitResult.getDistance());

//                                if(anchorNode != null) {
//                                    arSceneView.getScene().removeChild(anchorNode);
//                                    anchorNode = null;
//                                }
//                                makeCenterCube(anchor);

                                if(centerNode != null) {
                                    centerNode.setLocalPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
                                }
                                else {
                                    makeCenterCube(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());
                                }


                                break;
                            }
                        });
    }

    private void makeCube(Anchor anchor) {

        MaterialFactory
                .makeOpaqueWithColor(this, new Color(android.graphics.Color.WHITE))
                .thenAccept(material -> {
                    ModelRenderable modelRenderable = ShapeFactory.makeCube(new Vector3(0.05f, 0.05f, 0.05f), new Vector3(0f, 0.05f, 0f), material);

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setRenderable(modelRenderable);
                    arSceneView.getScene().addChild(anchorNode);
                    anchorNodeList.add(anchorNode);
                });
    }

    private void makeCenterCube(float tx, float ty, float tz) {

        MaterialFactory
                .makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                    ModelRenderable modelRenderable = ShapeFactory.makeCube(new Vector3(0.05f, 0.05f, 0.05f), new Vector3(0f, 0.05f, 0f), material);

                    Node node = new Node();
                    node.setRenderable(modelRenderable);
                    node.setLocalPosition(new Vector3(tx, ty, tz));
                    // Create the transformable andy and add it to the anchor.
                    arSceneView.getScene().addChild(node);
                    this.centerNode = node;
                });
    }

    private static Session createArSession(Activity activity, boolean installRequested, Config.LightEstimationMode lightEstimationMode) throws UnavailableException {
        Session session;
        // if we have the camera permission, create the session
        switch (ArCoreApk.getInstance().requestInstall(activity, !installRequested)) {
            case INSTALL_REQUESTED:
                installRequested = true;
                return null;
            case INSTALLED:
                break;
        }
        session = new Session(activity);
        // IMPORTANT!!!  ArSceneView requires the `LATEST_CAMERA_IMAGE` non-blocking update mode.
        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        config.setLightEstimationMode(lightEstimationMode);
        session.configure(config);
        return session;
    }

    public static void handleSessionException(
            Activity activity, UnavailableException sessionException) {

        String message;
        if (sessionException instanceof UnavailableArcoreNotInstalledException) {
            message = "Please install ARCore";
        }
        else if (sessionException instanceof UnavailableApkTooOldException) {
            message = "Please update ARCore";
        }
        else if (sessionException instanceof UnavailableSdkTooOldException) {
            message = "Please update this app";
        }
        else if (sessionException instanceof UnavailableDeviceNotCompatibleException) {
            message = "This device does not support AR";
        }
        else {
            message = "Failed to create AR session";
            Log.e(TAG, "Exception: " + sessionException);
        }

        ToastUtil.showCustomShortToastNormal(activity, message);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (arSceneView == null) {
            return;
        }

        if (arSceneView.getSession() == null) {
            // If the session wasn't created yet, don't resume rendering.
            // This can happen if ARCore needs to be updated or permissions are not granted yet.
            try {
                Config.LightEstimationMode lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR;
                Session session = createArSession(this, true, lightEstimationMode);

                if (session == null) {
                    finish();
                }
                else {
                    arSceneView.setupSession(session);
                }
            }
            catch (UnavailableException e) {
                handleSessionException(this, e);
            }
        }

        try {
            arSceneView.resume();
        }
        catch (CameraNotAvailableException ex) {
            ToastUtil.showCustomShortToastNormal(ARActivity.this, "Unable to get camera");
            finish();
            return;
        }

        if (arSceneView.getSession() != null) {
            // loading...finding plane
            textView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (arSceneView != null) {
            arSceneView.destroy();
        }
    }
}