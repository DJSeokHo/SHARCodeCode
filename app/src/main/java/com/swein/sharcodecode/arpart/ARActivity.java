package com.swein.sharcodecode.arpart;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.PlaneRenderer;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.framework.util.ar.ARUtil;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.device.DeviceUtil;
import com.swein.sharcodecode.framework.util.toast.ToastUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ARActivity extends FragmentActivity {

    private final static String TAG = "ARActivity";

    private ArSceneView arSceneView;

    private TextView textView;
    private FrameLayout frameLayoutTooCloseTooFar;
    private TextView textViewTooCloseTooFar;

    private Node centerPoint;

    private Button buttonBack;
    private Button buttonReDetect;

    private Node tempLineNode;
    private FaceToCameraNode tempTextNode;

    private List<Node> floorPolygon = new ArrayList<>();
    private List<Node> cellPolygon = new ArrayList<>();

    private Node tempNode;

    private float screenCenterX;
    private float screenCenterY;

    private boolean shadow = true;

    private ViewRenderable viewRenderableSizeText;
    private Material pointMaterial;
    private Material lineMaterial;

    private boolean isReadyToAutoClose = false;
    private boolean isAutoClosed = false;

    private Config.PlaneFindingMode planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        initData();
        findView();
        setListener();

    }

    private void initData() {

        MaterialFactory
                .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
                .thenAccept(material -> {
                    pointMaterial = material;
                    lineMaterial = material;
                });

        ViewRenderable.builder()
                .setView(this, R.layout.view_renderable_text)
                .build()
                .thenAccept(viewRenderable -> {

                    viewRenderableSizeText = viewRenderable;
                    viewRenderable.setShadowCaster(false);
                    viewRenderable.setShadowReceiver(false);
                });

        WindowManager wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screenCenterX = (float)dm.widthPixels * 0.5f;
        screenCenterY = (float)dm.heightPixels * 0.5f;
    }

    private void findView() {

        arSceneView = findViewById(R.id.arSceneView);
        textView = findViewById(R.id.textView);
        frameLayoutTooCloseTooFar = findViewById(R.id.frameLayoutTooCloseTooFar);
        textViewTooCloseTooFar = findViewById(R.id.textViewTooCloseTooFar);
        buttonBack = findViewById(R.id.buttonBack);
        buttonReDetect = findViewById(R.id.buttonReDetect);
    }

    private void setListener() {
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

                    List<HitResult> hitTestResultList = frame.hitTest(screenCenterX, screenCenterY);

                    for (HitResult hitResult : hitTestResultList) {

                        if(hitResult.getTrackable().getTrackingState() == TrackingState.TRACKING) {

                            if(isAutoClosed) {
                                return false;
                            }

                            if(isReadyToAutoClose) {

                                drawLine(floorPolygon.get(floorPolygon.size() - 1), floorPolygon.get(0));
                                DeviceUtil.vibrate(this, 5);
                                isAutoClosed = true;

                                clearTemp();

                                if(centerPoint != null) {
                                    arSceneView.getScene().removeChild(centerPoint);
                                    centerPoint = null;
                                }

                                createCellPolygon();
                                return false;
                            }


//                            Anchor anchor = hitResult.createAnchor();
//                            AnchorNode anchorNode = createAnchorNode(anchor, pointMaterial, shadow);
                            Node node = createNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);

                            arSceneView.getScene().addChild(node);
                            floorPolygon.add(node);

                            DeviceUtil.vibrate(this, 5);

                            if(floorPolygon.size() >= 2) {
                                drawLine(floorPolygon.get(floorPolygon.size() - 2), floorPolygon.get(floorPolygon.size() - 1));
                            }

                            clearTemp();


                            tempNode = createNode(node.getWorldPosition().x, node.getWorldPosition().y, node.getWorldPosition().z,
                                    pointMaterial, shadow);
                            arSceneView.getScene().addChild(tempNode);

                            break;
                        }
                    }

                    return false;
                });

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

                    updatePlanRenderer();

                    Collection<Plane> planeCollection = frame.getUpdatedTrackables(Plane.class);

                    checkPlaneSize(planeCollection);

                    if(isAutoClosed) {
                        return;
                    }

                    List<HitResult> hitTestResultList = frame.hitTest(screenCenterX, screenCenterY);

                    checkPlanType(planeCollection, hitTestResultList);

                    for (HitResult hitResult : hitTestResultList) {
                        if(hitResult.getTrackable().getTrackingState() == TrackingState.TRACKING) {

                            toggleDistanceHint(hitResult.getDistance());

                            if(centerPoint != null) {
                                centerPoint.setLocalPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
                            }
                            else {
                                centerPoint = createNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);
                                arSceneView.getScene().addChild(centerPoint);
                            }

                            if(tempNode == null) {
                                return;
                            }

                            drawTempLine(tempNode, centerPoint);

                            if(floorPolygon.size() < 3) {
                                return;
                            }

                            if(checkClose(centerPoint, floorPolygon.get(0))) {
                                drawTempLine(tempNode, floorPolygon.get(0));

                                if(!isReadyToAutoClose) {
                                    DeviceUtil.vibrate(this, 5);
                                }

                                isReadyToAutoClose = true;
                            }
                            else {
                                drawTempLine(tempNode, centerPoint);
                                isReadyToAutoClose = false;
                            }

                            return;
                        }
                    }
                });

        buttonBack.setOnClickListener(view -> back());

        buttonReDetect.setOnClickListener(view -> reset());
    }

    private void back() {
        if(isAutoClosed) {

            for(Node node : floorPolygon) {
                arSceneView.getScene().removeChild(node);
            }

            for(Node node : cellPolygon) {
                arSceneView.getScene().removeChild(node);
            }

            floorPolygon.clear();
            cellPolygon.clear();

            clearTemp();

            if(centerPoint != null) {
                arSceneView.getScene().removeChild(centerPoint);
                centerPoint = null;
            }


            isAutoClosed = false;
            isReadyToAutoClose = false;
        }
        else {

            if(floorPolygon.size() == 1) {

                arSceneView.getScene().removeChild(floorPolygon.get(0));
                floorPolygon.clear();

                clearTemp();
                if(centerPoint != null) {
                    arSceneView.getScene().removeChild(centerPoint);
                    centerPoint = null;
                }
            }
            else if(floorPolygon.size() > 1) {

                List<Node> childList = floorPolygon.get(floorPolygon.size() - 2).getChildren();

                if(!childList.isEmpty()) {
                    Node childNode = childList.get(0);
                    childNode.setParent(null);
                    floorPolygon.get(floorPolygon.size() - 2).removeChild(childNode);
                }

                arSceneView.getScene().removeChild(floorPolygon.get(floorPolygon.size() - 1));
                floorPolygon.remove(floorPolygon.size() - 1);

                clearTemp();
                if(centerPoint != null) {
                    arSceneView.getScene().removeChild(centerPoint);
                    centerPoint = null;
                }

                tempNode = createNode(
                        floorPolygon.get(floorPolygon.size() - 1).getWorldPosition().x,
                        floorPolygon.get(floorPolygon.size() - 1).getWorldPosition().y,
                        floorPolygon.get(floorPolygon.size() - 1).getWorldPosition().z,
                        pointMaterial, shadow);
                arSceneView.getScene().addChild(tempNode);
            }
        }
    }

    private void createCellPolygon() {
        cellPolygon.clear();

        Node node;
        for(int i = 0; i < floorPolygon.size(); i++) {
            node = createNode(
                    floorPolygon.get(i).getWorldPosition().x,
                    floorPolygon.get(i).getWorldPosition().y + 1,
                    floorPolygon.get(i).getWorldPosition().z ,
                    pointMaterial, shadow);
            cellPolygon.add(node);

            arSceneView.getScene().addChild(node);
        }

        // draw vertical line
        for(int i = 0; i < floorPolygon.size(); i++) {
            drawLine(floorPolygon.get(i), cellPolygon.get(i));
        }

        // connect node and make line close
        for(int i = 0; i < cellPolygon.size() - 1; i++) {
            drawLine(cellPolygon.get(i), cellPolygon.get(i + 1));
        }
        drawLine(cellPolygon.get(cellPolygon.size() - 1), cellPolygon.get(0));
    }


    private void calculate() {

    }

    private void clearTemp() {
        if(tempNode != null) {
            arSceneView.getScene().removeChild(tempNode);
            tempNode = null;
        }
        if(tempTextNode != null) {
            arSceneView.getScene().removeChild(tempTextNode);
            tempTextNode = null;
        }

        if(tempLineNode != null) {
            arSceneView.getScene().removeChild(tempLineNode);
            tempLineNode = null;
        }
    }

    private void checkPlanType(Collection<Plane> planeCollection, List<HitResult> hitTestResultList) {

        if(planeCollection.isEmpty() || hitTestResultList.isEmpty()) {
            return;
        }

        Pose pose = null;

        for (HitResult hitResult : hitTestResultList) {
            if (hitResult.getTrackable().getTrackingState() == TrackingState.TRACKING) {
                pose = hitResult.getHitPose();
                break;
            }
        }

        if(pose == null) {
            return;
        }

        for (Plane plane : planeCollection) {

            if(plane.isPoseInPolygon(pose) && plane.isPoseInExtents(pose)) {
                if(plane.getType() == Plane.Type.VERTICAL && plane.getTrackingState() == TrackingState.TRACKING) {
                    ILog.iLogDebug(TAG, "wall");
                    // A vertical plane (e.g. a wall).
//                            textViewDistance.setTextColor(android.graphics.Color.RED);
                }
                else if(plane.getType() == Plane.Type.HORIZONTAL_DOWNWARD_FACING && plane.getTrackingState() == TrackingState.TRACKING) {
                    // A horizontal plane facing downward (e.g. a ceiling).
//                            textViewDistance.setTextColor(android.graphics.Color.GREEN);
                    ILog.iLogDebug(TAG, "ceiling");
                }
                else if(plane.getType() == Plane.Type.HORIZONTAL_UPWARD_FACING && plane.getTrackingState() == TrackingState.TRACKING) {
                    // A horizontal plane facing upward (e.g. floor or tabletop).
//                            textViewDistance.setTextColor(android.graphics.Color.BLUE);
                    ILog.iLogDebug(TAG, "floor");
                }
            }

            break;
        }
    }

    private boolean checkClose(Node startNode, Node endNode) {
//        if(Vector3.subtract(startNode.getWorldPosition(), endNode.getWorldPosition()).length() < 0.06) {
        if(ARUtil.getNodesDistanceMetersWithoutHeight(startNode, endNode) < 0.06) {
            ILog.iLogDebug(TAG, "checkClose nice!!!!!!");
            return true;
        }
        else {
            return false;
        }
    }

    private void toggleDistanceHint(float distance) {
        if(distance < 0.5) {
            frameLayoutTooCloseTooFar.setVisibility(View.VISIBLE);
            textViewTooCloseTooFar.setText(R.string.ar_too_close);
        }
        else if(distance > 10) {
            frameLayoutTooCloseTooFar.setVisibility(View.VISIBLE);
            textViewTooCloseTooFar.setText(R.string.ar_too_far);
        }
        else {
            textViewTooCloseTooFar.setText("");
            frameLayoutTooCloseTooFar.setVisibility(View.GONE);
        }
    }

    private void updatePlanRenderer() {
        PlaneRenderer planeRenderer = arSceneView.getPlaneRenderer();

        planeRenderer.getMaterial().thenAccept(material -> {
            material.setFloat3(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS, 1000f, 1000f, 1000f);
            material.setFloat3(PlaneRenderer.MATERIAL_COLOR, new Color(1f, 1f, 1f, 1f));
        });

//        // Build texture sampler
//        Texture.Sampler sampler = Texture.Sampler.builder()
//                .setMinFilter(Texture.Sampler.MinFilter.LINEAR)
//                .setMagFilter(Texture.Sampler.MagFilter.LINEAR)
//                .setWrapMode(Texture.Sampler.WrapMode.REPEAT).build();
//
//        // Build texture with sampler
//        CompletableFuture<Texture> trigrid = Texture.builder()
//                .setSource(this, R.drawable.grid_blue)
//                .setSampler(sampler).build();
//
//        planeRenderer.getMaterial().thenAcceptBoth(trigrid, (material, texture) -> {
//            material.setTexture(PlaneRenderer.MATERIAL_TEXTURE, texture);
//            material.setFloat(PlaneRenderer.MATERIAL_SPOTLIGHT_RADIUS, 1000f);
//        });
    }

//    private AnchorNode createAnchorNode(Anchor anchor, Material material, boolean shadow) {
//
//        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
//        modelRenderable.setShadowReceiver(shadow);
//        modelRenderable.setShadowCaster(shadow);
//
//        AnchorNode anchorNode = new AnchorNode(anchor);
//        anchorNode.setRenderable(modelRenderable);
//
//        return anchorNode;
//    }

    private Node createNode(float tx, float ty, float tz, Material material, boolean shadow) {
        ModelRenderable modelRenderable = ShapeFactory.makeSphere(0.01f, Vector3.zero(), material);
        modelRenderable.setShadowReceiver(shadow);
        modelRenderable.setShadowCaster(shadow);
        Node node = new Node();
        node.setRenderable(modelRenderable);
        node.setLocalPosition(new Vector3(tx, ty, tz));
//        node.setWorldPosition(new Vector3(tx, ty, tz));
        // Create the transformable andy and add it to the anchor.
        return node;
    }

//    private void makeVerticalCenterCube(float tx, float ty, float tz, Runnable runnable) {
//
//        if(centerPoint != null) {
//            centerPoint.setLocalPosition(new Vector3(tx, ty, tz));
//            runnable.run();
//        }
//        else {
//            MaterialFactory
//                    .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
//                    .thenAccept(material -> {
//
//                        ModelRenderable modelRenderable = ShapeFactory.makeCylinder(0.01f, 0.0001f, Vector3.zero(), material);
//                        modelRenderable.setShadowReceiver(shadow);
//                        modelRenderable.setShadowCaster(shadow);
//                        Node node = new Node();
//                        node.setRenderable(modelRenderable);
//                        node.setLocalPosition(new Vector3(tx, ty, tz));
//                        // Create the transformable andy and add it to the anchor.
//                        arSceneView.getScene().addChild(node);
//                        this.centerPoint = node;
//                        runnable.run();
//                    });
//        }
//    }

    private void drawLine(Node startNode, Node endNode) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        ModelRenderable lineModelRenderable = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), lineMaterial);
        lineModelRenderable.setShadowCaster(shadow);
        lineModelRenderable.setShadowReceiver(shadow);

        Node lineNode = new Node();

        lineNode.setParent(startNode);
        lineNode.setRenderable(lineModelRenderable);
        lineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
        lineNode.setWorldRotation(rotationFromAToB);

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
                    faceToCameraNode.setLocalPosition(new Vector3(0f, 0.05f, 0f));
                    faceToCameraNode.setRenderable(viewRenderable);
                });

    }

    private void drawTempLine(Node startNode, Node endNode) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);

        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        if(tempLineNode != null) {

            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), lineMaterial);
            lineMode.setShadowCaster(shadow);
            lineMode.setShadowReceiver(shadow);

            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }
        else {
            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), lineMaterial);
            lineMode.setShadowCaster(shadow);
            lineMode.setShadowReceiver(shadow);

            tempLineNode = new Node();
            tempLineNode.setParent(startNode);
            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }

        if(tempTextNode != null) {
            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.1fCM", difference.length() * 100));
        }
        else {
            if(tempLineNode != null) {

                ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.1fCM", difference.length() * 100));

                tempTextNode = new FaceToCameraNode();
                tempTextNode.setParent(tempLineNode);

                tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                tempTextNode.setLocalPosition(new Vector3(0f, 0.05f, 0f));
                tempTextNode.setRenderable(viewRenderableSizeText);

            }
        }
    }

    private void checkPlaneSize(Collection<Plane> planeCollection) {
        for (Plane plane : planeCollection) {

            if (plane.getTrackingState() == TrackingState.TRACKING) {
//                                ILog.iLogDebug(TAG, "size ?? " + (plane.getExtentX() * plane.getExtentZ() * 2));

                if((plane.getExtentX() * plane.getExtentZ() * 2) > 3) {
                    textView.setVisibility(View.GONE);
                }
            }
        }
    }

//    private void drawTempVerticalLine(Node startNode, Node endNode, boolean updateText) {
//
//        Vector3 startVector3 = startNode.getWorldPosition();
//        Vector3 endVector3 = endNode.getWorldPosition();
//
//        Vector3 difference = Vector3.subtract(startVector3, endVector3);
//        ILog.iLogDebug(TAG, "difference length " + difference.length());
//        Vector3 directionFromTopToBottom = difference.normalized();
//        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());
//
//        if(tempLineNode != null) {
//
////            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), tempLineNode.getRenderable().getMaterial());
//            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, difference.length(), 0.005f), Vector3.zero(), tempLineNode.getRenderable().getMaterial());
//            lineMode.setShadowCaster(shadow);
//            lineMode.setShadowReceiver(shadow);
//
//            tempLineNode.setRenderable(lineMode);
//            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
//            tempLineNode.setWorldRotation(rotationFromAToB);
//        }
//        else {
//            MaterialFactory
//                    .makeOpaqueWithColor(this, new Color(android.graphics.Color.GREEN))
//                    .thenAccept(material -> {
//
////                        ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), material);
//                        ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, difference.length(), 0.005f), Vector3.zero(), material);
//                        lineMode.setShadowCaster(shadow);
//                        lineMode.setShadowReceiver(shadow);
//
//                        tempLineNode = new Node();
//                        tempLineNode.setParent(startNode);
//                        tempLineNode.setRenderable(lineMode);
//                        tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
//                        tempLineNode.setWorldRotation(rotationFromAToB);
//                    });
//        }
//
//        if(tempTextNode != null) {
//            textViewSize.setText(String.format("%.1fCM", difference.length() * 100));
//        }
//        else {
//            if(updateText && tempLineNode != null) {
//                ViewRenderable.builder()
//                        .setView(this, R.layout.view_renderable_text)
//                        .build()
//                        .thenAccept(viewRenderable -> {
//
//                            textViewSize = (TextView)viewRenderable.getView();
//                            textViewSize.setText(String.format("%.1fCM", difference.length() * 100));
//                            viewRenderable.setShadowCaster(false);
//                            viewRenderable.setShadowReceiver(false);
//
//                            tempTextNode = new FaceToCameraNode();
//                            tempTextNode.setParent(tempLineNode);
//
//                            tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
//                            tempTextNode.setLocalPosition(new Vector3(0f, 0.01f, 0f));
//                            tempTextNode.setRenderable(viewRenderable);
//                        });
//            }
//        }
//
//    }

    private static Session createArSession(Activity activity, boolean installRequested, Config.LightEstimationMode lightEstimationMode, Config.PlaneFindingMode planeFindingMode) throws UnavailableException {
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
        config.setPlaneFindingMode(planeFindingMode);

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
            ILog.iLogError(TAG, "Exception: " + sessionException);
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
                Session session = createArSession(this, true, null, planeFindingMode);

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

    private void reset() {

        if (arSceneView == null) {
            return;
        }

        arSceneView.pause();

        arSceneView.setupSession(null);

        try {
//                Config.LightEstimationMode lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR;
//                Session session = createArSession(this, true, lightEstimationMode);
            Session session = createArSession(this, true, null, planeFindingMode);

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