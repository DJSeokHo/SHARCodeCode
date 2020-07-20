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
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.toast.ToastUtil;

import java.util.Collection;
import java.util.List;

public class ARActivity extends AppCompatActivity {

    private final static String TAG = "ARActivity";

    private ArSceneView arSceneView;

    private TextView textView;
    private TextView textViewDistance;

    private AnchorNode startAnchorNode;
    private AnchorNode endAnchorNode;
    private TextView textViewSize;

    private Node centerNode;

    private Node tempLineNode;
    private FaceToCameraNode tempTextNode;

    private float centerX;
    private float centerY;

    private boolean shadow = true;

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
        textViewDistance = findViewById(R.id.textViewDistance);

        // Set a touch listener on the Scene to listen for taps.
        arSceneView.getScene().setOnTouchListener(
                        (HitTestResult hitTestResult, MotionEvent event) -> {

//                            ILog.iLogDebug(TAG, "tab");
//                            ILog.iLogDebug(TAG, hitTestResult.getDistance());

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
//                                    ILog.iLogDebug(TAG, "cennter pose " + plane.getCenterPose());
//                                    ILog.iLogDebug(TAG, plane.getExtentX());
//                                    ILog.iLogDebug(TAG, plane.getExtentZ());
//                                    ILog.iLogDebug(TAG, plane.getPolygon());
//                                    ILog.iLogDebug(TAG, hitTestResult.getDistance());
                                }
                            }

                            List<HitResult> hitTestResultList = frame.hitTest(centerX, centerY);

                            for (HitResult hitResult : hitTestResultList) {

                                if(hitResult.getTrackable().getTrackingState() == TrackingState.TRACKING) {

                                    Anchor anchor = hitResult.getTrackable().createAnchor(hitResult.getHitPose());

                                    ILog.iLogDebug(TAG, "t " + anchor.getPose().tx() + " " + anchor.getPose().ty() + " " + anchor.getPose().tz());
                                    ILog.iLogDebug(TAG, "q " + anchor.getPose().qx() + " " + anchor.getPose().qy() + " " + anchor.getPose().qz());

                                    ILog.iLogDebug(TAG, hitResult.getDistance());

                                    makeCube(anchor, () -> {

                                        if(tempTextNode != null) {
                                            arSceneView.getScene().removeChild(tempTextNode);
                                        }

                                        if(tempLineNode != null) {
                                            arSceneView.getScene().removeChild(tempLineNode);
                                        }

                                        textViewSize = null;
                                        tempTextNode = null;
                                        tempLineNode = null;

                                        if (startAnchorNode != null && endAnchorNode != null) {
//                                    float dx = startAnchorNode.getAnchor().getPose().tx() - endAnchorNode.getAnchor().getPose().tx();
//                                    float dy = startAnchorNode.getAnchor().getPose().ty() - endAnchorNode.getAnchor().getPose().ty();
//                                    float dz = startAnchorNode.getAnchor().getPose().tz() - endAnchorNode.getAnchor().getPose().tz();

//                                            float dx = startAnchorNode.getWorldPosition().x - endAnchorNode.getWorldPosition().x;
//                                            float dy = startAnchorNode.getWorldPosition().y - endAnchorNode.getWorldPosition().y;
//                                            float dz = startAnchorNode.getWorldPosition().z - endAnchorNode.getWorldPosition().z;
//
//                                            float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                                            drawLine(startAnchorNode, endAnchorNode);
                                        }
                                    });


                                    break;
                                }
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

                                    if (plane.getTrackingState() == TrackingState.TRACKING) {

                                        if(plane.getType() == Plane.Type.VERTICAL) {
                                            // A vertical plane (e.g. a wall).
                                            textViewDistance.setTextColor(android.graphics.Color.RED);
                                        }
                                        else if(plane.getType() == Plane.Type.HORIZONTAL_DOWNWARD_FACING){
                                            // A horizontal plane facing downward (e.g. a ceiling).
                                            textViewDistance.setTextColor(android.graphics.Color.GREEN);
                                        }
                                        else if(plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING){
                                            // A horizontal plane facing upward (e.g. floor or tabletop).
                                            textViewDistance.setTextColor(android.graphics.Color.BLUE);
                                        }

                                        break;
                                    }
                                }
                            }

                            List<HitResult> hitTestResultList = frame.hitTest(centerX, centerY);

                            for (HitResult hitResult : hitTestResultList) {

                                if(hitResult.getTrackable().getTrackingState() == TrackingState.TRACKING) {

//                                    if(startAnchorNode != null && endAnchorNode == null) {
//
//                                        ILog.iLogDebug(TAG, "height ?? " + hitResult.getHitPose().ty());
////                                        makeVerticalCenterCube(startAnchorNode.getWorldPosition().x, hitResult.getHitPose().ty(), startAnchorNode.getWorldPosition().z, () -> {
//                                        makeVerticalCenterCube(startAnchorNode.getWorldPosition().x, hitResult.getHitPose().ty(), startAnchorNode.getWorldPosition().z, () -> {
////                                                drawTempVerticalLine(startAnchorNode, centerNode, distanceMeters, true);
////                                            drawTempVerticalLine(startAnchorNode, centerNode);
//
//                                            drawTempVerticalLine(startAnchorNode, centerNode, true);
//                                        });
//
//                                        return;
//                                    }

                                    makeCenterCube(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), () -> {

                                        if(startAnchorNode != null && endAnchorNode == null) {
//                                    float dx = startAnchorNode.getAnchor().getPose().tx() - centerNode.getWorldPosition().x;
//                                    float dy = startAnchorNode.getAnchor().getPose().ty() - centerNode.getWorldPosition().y;
//                                    float dz = startAnchorNode.getAnchor().getPose().tz() - centerNode.getWorldPosition().z;
//
//                                            float dx = startAnchorNode.getWorldPosition().x - centerNode.getWorldPosition().x;
//                                            float dy = startAnchorNode.getWorldPosition().y - centerNode.getWorldPosition().y;
//                                            float dz = startAnchorNode.getWorldPosition().z - centerNode.getWorldPosition().z;
//
//                                            float distanceMeters = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

                                            boolean drawableText = false;
                                            for (Plane plane : planeCollection) {
                                                if (plane.getTrackingState() == TrackingState.TRACKING) {
                                                    drawableText = plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING;
                                                    break;
                                                }
                                            }

                                            drawTempLine(startAnchorNode, centerNode, drawableText);
                                        }

                                        textViewDistance.setText(String.valueOf(hitResult.getDistance()));

                                    });

                                    break;
                                }

                            }
                        });
    }

    private void makeCube(Anchor anchor, Runnable runnable) {

        MaterialFactory
                .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                .thenAccept(material -> {

                    ModelRenderable modelRenderable = ShapeFactory.makeCylinder(0.01f, 0.0001f, Vector3.zero(), material);
                    modelRenderable.setShadowReceiver(shadow);
                    modelRenderable.setShadowCaster(shadow);

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setRenderable(modelRenderable);
                    arSceneView.getScene().addChild(anchorNode);

                    processAnchorNode(anchorNode);

                    runnable.run();
                });
    }

    private void processAnchorNode(AnchorNode anchorNode) {

        if(startAnchorNode == null && endAnchorNode == null) {
            startAnchorNode = anchorNode;
        }
        else if(startAnchorNode != null && endAnchorNode == null) {
            endAnchorNode = anchorNode;
        }
        else if(startAnchorNode != null && endAnchorNode != null) {

            arSceneView.getScene().removeChild(startAnchorNode);
            arSceneView.getScene().removeChild(endAnchorNode);

            startAnchorNode = null;
            endAnchorNode = null;
            startAnchorNode = anchorNode;
        }
    }

    private void makeCenterCube(float tx, float ty, float tz, Runnable runnable) {

        if(centerNode != null) {
            centerNode.setLocalPosition(new Vector3(tx, ty, tz));
            runnable.run();
        }
        else {
            MaterialFactory
                    .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                    .thenAccept(material -> {

                        ModelRenderable modelRenderable = ShapeFactory.makeCylinder(0.01f, 0.0001f, Vector3.zero(), material);
                        modelRenderable.setShadowReceiver(shadow);
                        modelRenderable.setShadowCaster(shadow);
                        Node node = new Node();
                        node.setRenderable(modelRenderable);
                        node.setLocalPosition(new Vector3(tx, ty, tz));
                        // Create the transformable andy and add it to the anchor.
                        arSceneView.getScene().addChild(node);
                        this.centerNode = node;
                        runnable.run();
                    });
        }
    }

    private void makeVerticalCenterCube(float tx, float ty, float tz, Runnable runnable) {

        if(centerNode != null) {
            centerNode.setLocalPosition(new Vector3(tx, ty, tz));
            runnable.run();
        }
        else {
            MaterialFactory
                    .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                    .thenAccept(material -> {

                        ModelRenderable modelRenderable = ShapeFactory.makeCylinder(0.01f, 0.0001f, Vector3.zero(), material);
                        modelRenderable.setShadowReceiver(shadow);
                        modelRenderable.setShadowCaster(shadow);
                        Node node = new Node();
                        node.setRenderable(modelRenderable);
                        node.setLocalPosition(new Vector3(tx, ty, tz));
                        // Create the transformable andy and add it to the anchor.
                        arSceneView.getScene().addChild(node);
                        this.centerNode = node;
                        runnable.run();
                    });
        }
    }

    private void drawLine(Node startNode, Node endNode) {

        ILog.iLogDebug(TAG, "draw line");
        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        MaterialFactory
                .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                .thenAccept(material -> {

                    ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.0001f, difference.length()), Vector3.zero(), material);
                    Node lineNode = new Node();
                    lineMode.setShadowCaster(shadow);
                    lineMode.setShadowReceiver(shadow);
                    lineNode.setParent(startNode);
                    lineNode.setRenderable(lineMode);
                    lineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
                    lineNode.setWorldRotation(rotationFromAToB);

                    ILog.iLogDebug(TAG, "line length is " + difference.length());

                    ViewRenderable.builder()
                            .setView(this, R.layout.view_renderable_text)
                            .build()
                            .thenAccept(viewRenderable -> {

                                ((TextView)viewRenderable.getView()).setText(String.format("%.1fCM", difference.length() * 100));
                                viewRenderable.setShadowCaster(false);
                                viewRenderable.setShadowReceiver(false);

                                FaceToCameraNode faceToCameraNode = new FaceToCameraNode();
                                faceToCameraNode.setParent(lineNode);

                                faceToCameraNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                                faceToCameraNode.setLocalPosition(new Vector3(0f, 0.01f, 0f));
                                faceToCameraNode.setRenderable(viewRenderable);
                            });
        });
    }

    private void drawTempLine(Node startNode, Node endNode, boolean updateText) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        ILog.iLogDebug(TAG, "difference length " + difference.length());
        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        if(tempLineNode != null) {

            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), tempLineNode.getRenderable().getMaterial());
//            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, difference.length(), 0.005f), Vector3.zero(), tempLineNode.getRenderable().getMaterial());
            lineMode.setShadowCaster(shadow);
            lineMode.setShadowReceiver(shadow);

            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }
        else {
            MaterialFactory
                    .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                    .thenAccept(material -> {

                        ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), material);
                        lineMode.setShadowCaster(shadow);
                        lineMode.setShadowReceiver(shadow);

                        tempLineNode = new Node();
                        tempLineNode.setParent(startNode);
                        tempLineNode.setRenderable(lineMode);
                        tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
                        tempLineNode.setWorldRotation(rotationFromAToB);
                    });
        }

        if(tempTextNode != null) {
            textViewSize.setText(String.format("%.1fCM", difference.length() * 100));
        }
        else {
            if(updateText && tempLineNode != null) {
                ViewRenderable.builder()
                        .setView(this, R.layout.view_renderable_text)
                        .build()
                        .thenAccept(viewRenderable -> {

                            textViewSize = (TextView)viewRenderable.getView();
                            textViewSize.setText(String.format("%.1fCM", difference.length() * 100));
                            viewRenderable.setShadowCaster(false);
                            viewRenderable.setShadowReceiver(false);

                            tempTextNode = new FaceToCameraNode();
                            tempTextNode.setParent(tempLineNode);

                            tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                            tempTextNode.setLocalPosition(new Vector3(0f, 0.01f, 0f));
                            tempTextNode.setRenderable(viewRenderable);
                        });
            }
        }

    }

    private void drawTempVerticalLine(Node startNode, Node endNode, boolean updateText) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        ILog.iLogDebug(TAG, "difference length " + difference.length());
        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        if(tempLineNode != null) {

//            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), tempLineNode.getRenderable().getMaterial());
            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, difference.length(), 0.005f), Vector3.zero(), tempLineNode.getRenderable().getMaterial());
            lineMode.setShadowCaster(shadow);
            lineMode.setShadowReceiver(shadow);

            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }
        else {
            MaterialFactory
                    .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                    .thenAccept(material -> {

//                        ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), material);
                        ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, difference.length(), 0.005f), Vector3.zero(), material);
                        lineMode.setShadowCaster(shadow);
                        lineMode.setShadowReceiver(shadow);

                        tempLineNode = new Node();
                        tempLineNode.setParent(startNode);
                        tempLineNode.setRenderable(lineMode);
                        tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
                        tempLineNode.setWorldRotation(rotationFromAToB);
                    });
        }

        if(tempTextNode != null) {
            textViewSize.setText(String.format("%.1fCM", difference.length() * 100));
        }
        else {
            if(updateText && tempLineNode != null) {
                ViewRenderable.builder()
                        .setView(this, R.layout.view_renderable_text)
                        .build()
                        .thenAccept(viewRenderable -> {

                            textViewSize = (TextView)viewRenderable.getView();
                            textViewSize.setText(String.format("%.1fCM", difference.length() * 100));
                            viewRenderable.setShadowCaster(false);
                            viewRenderable.setShadowReceiver(false);

                            tempTextNode = new FaceToCameraNode();
                            tempTextNode.setParent(tempLineNode);

                            tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                            tempTextNode.setLocalPosition(new Vector3(0f, 0.01f, 0f));
                            tempTextNode.setRenderable(viewRenderable);
                        });
            }
        }

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
        config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL);
//        config.setPlaneFindingMode(Config.PlaneFindingMode.HORIZONTAL);
//        config.setPlaneFindingMode(Config.PlaneFindingMode.VERTICAL);

        if(lightEstimationMode != null) {
            config.setLightEstimationMode(lightEstimationMode);
        }
        else {
            config.setLightEstimationMode(Config.LightEstimationMode.DISABLED);
        }

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
//                Config.LightEstimationMode lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR;
//                Session session = createArSession(this, true, lightEstimationMode);
                Session session = createArSession(this, true, null);

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