package com.swein.sharcodecode.arpart;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.ar.core.Anchor;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Session;
import com.google.ar.core.Trackable;
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
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.bean.WallBean;
import com.swein.sharcodecode.bean.WallObjectBean;
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

    private LinearLayout linearLayout;
    private TextView textViewArea;
    private TextView textViewCircumference;
    private TextView textViewHeight;
    private TextView textViewWallArea;
    private TextView textViewVolume;

    private TextView textViewHeightRealTime;

    private TextView textViewPlaneType;

    private TextView textViewNearest;

    private Node centerPoint;
    private Node wallGuidePoint;
    private Node wallTempPoint;
    private int currentWallIndex = -1;
    private int currentGuideIndex = -1;

    private Button buttonBack;
    private Button buttonReDetect;

    private Node tempLineNode;
    private FaceToCameraNode tempTextNode;

//    private List<AnchorNode> bottomAnchorPolygon = new ArrayList<>();
    private AnchorNode anchorNode;
    private List<Node> floorPolygonList = new ArrayList<>();
    private List<Node> cellPolygonList = new ArrayList<>();
    private List<TextView> textViewSizeList = new ArrayList<>();
    private Vector3 normalVectorOfPlane;
    private List<WallBean> wallBeanList = new ArrayList<>();
    private List<WallObjectBean> wallObjectBeans = new ArrayList<>();

    private float screenCenterX;
    private float screenCenterY;

    private boolean shadow = true;

    private ViewRenderable viewRenderableSizeText;
    private Material pointMaterial;
    private Material wallPointMaterial;
    private Material lineMaterial;
    private Material wallLineMaterial;

    private boolean isReadyToAutoClose = false;
    private boolean isAutoClosed = false;

    private Config.PlaneFindingMode planeFindingMode = Config.PlaneFindingMode.HORIZONTAL_AND_VERTICAL;

    private float height = 1;

    private float fixedY = 0;

    private ARUtil.Unit unit = ARUtil.Unit.CM;



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

        MaterialFactory
                .makeOpaqueWithColor(this, new Color(android.graphics.Color.RED))
                .thenAccept(material -> {
                    wallPointMaterial = material;
                    wallLineMaterial = material;
                });


        ViewRenderable.builder()
                .setView(this, R.layout.view_renderable_text)
                .build()
                .thenAccept(viewRenderable -> {

                    viewRenderableSizeText = viewRenderable;
                    viewRenderable.setShadowCaster(false);
                    viewRenderable.setShadowReceiver(false);
                });

        screenCenterX = DeviceUtil.getScreenCenterX(this);
        screenCenterY = DeviceUtil.getScreenCenterY(this);
    }

    private void findView() {

        arSceneView = findViewById(R.id.arSceneView);
        textView = findViewById(R.id.textView);
        frameLayoutTooCloseTooFar = findViewById(R.id.frameLayoutTooCloseTooFar);
        textViewTooCloseTooFar = findViewById(R.id.textViewTooCloseTooFar);
        buttonBack = findViewById(R.id.buttonBack);
        buttonReDetect = findViewById(R.id.buttonReDetect);
        textViewPlaneType = findViewById(R.id.textViewPlaneType);

        linearLayout = findViewById(R.id.linearLayout);
        textViewArea = findViewById(R.id.textViewArea);
        textViewCircumference = findViewById(R.id.textViewCircumference);
        textViewHeight = findViewById(R.id.textViewHeight);
        textViewWallArea = findViewById(R.id.textViewWallArea);
        textViewVolume = findViewById(R.id.textViewVolume);

        textViewHeightRealTime = findViewById(R.id.textViewHeightRealTime);

        textViewNearest = findViewById(R.id.textViewNearest);
    }

    @SuppressLint("RestrictedApi")
    private void setListener() {
        // Set a touch listener on the Scene to listen for taps.
        arSceneView.getScene().setOnTouchListener(
                (HitTestResult hitTestResult, MotionEvent event) -> {

                    Frame frame = arSceneView.getArFrame();
                    if (frame == null) {
                        return false;
                    }

                    if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
                        return false;
                    }

                    List<HitResult> hitTestResultList = frame.hitTest(screenCenterX, screenCenterY);

                    for (HitResult hitResult : hitTestResultList) {

                        Trackable trackable = hitResult.getTrackable();

                        if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                            if(isAutoClosed) {

                                // make node on wall
                                if(wallTempPoint == null) {
                                    // create wall object start point

                                    currentWallIndex = currentGuideIndex;


                                    Vector3 vector3World = new Vector3(
                                            wallGuidePoint.getWorldPosition().x,
                                            wallGuidePoint.getWorldPosition().y,
                                            wallGuidePoint.getWorldPosition().z
                                    );
                                    Vector3 vector3Local = ARUtil.transformWorldPositionToLocalPositionOfParent(anchorNode, vector3World);


                                    wallTempPoint = ARUtil.createLocalNode(
                                            vector3Local.x,
                                            vector3Local.y,
                                            vector3Local.z,
                                            wallPointMaterial, shadow);

                                    wallTempPoint.setParent(anchorNode);


                                    if(wallObjectBeans.isEmpty()) {
                                        wallObjectBeans.add(new WallObjectBean());

                                        WallObjectBean wallObjectBean = wallObjectBeans.get(0);

                                        // add point
                                        wallObjectBean.objectPointList.add(wallTempPoint);

                                        Vector3 horizontalVector3 = new Vector3(wallGuidePoint.getWorldPosition().x, wallGuidePoint.getWorldPosition().y, wallGuidePoint.getWorldPosition().z);
                                        Vector3 horizontalLocalPosition = ARUtil.transformWorldPositionToLocalPositionOfParent(this.anchorNode, horizontalVector3);
                                        Node horizontalNode = ARUtil.createLocalNode(horizontalLocalPosition.x, horizontalLocalPosition.y, horizontalLocalPosition.z, wallPointMaterial, shadow);
                                        horizontalNode.setParent(anchorNode);
                                        wallObjectBean.objectPointList.add(horizontalNode);

                                        wallObjectBean.objectPointList.add(wallGuidePoint);

                                        Vector3 verticalVector3 = new Vector3();
                                        verticalVector3.x = wallTempPoint.getWorldPosition().x;
                                        verticalVector3.y = wallGuidePoint.getWorldPosition().y;
                                        verticalVector3.z = wallTempPoint.getWorldPosition().z;
                                        Node verticalNode = ARUtil.createLocalNode(verticalVector3.x, verticalVector3.y, verticalVector3.z, wallPointMaterial, shadow);
                                        verticalNode.setParent(anchorNode);
                                        wallObjectBean.objectPointList.add(verticalNode);

                                        // add line
                                        Node tempLineNode;
                                        for(int i = 0; i < wallObjectBean.objectPointList.size(); i++) {
                                            tempLineNode = new Node();
                                            tempLineNode.setParent(wallObjectBean.objectPointList.get(i));
                                            wallObjectBean.objectLineList.add(tempLineNode);
                                        }

                                        // add text node
                                        for(int i = 0; i < wallObjectBean.objectPointList.size(); i++) {

                                            // add text view
                                            int index = i;
                                            ViewRenderable.builder()
                                                    .setView(this, R.layout.view_renderable_text)
                                                    .build()
                                                    .thenAccept(viewRenderable -> {

                                                        viewRenderable.setShadowCaster(false);
                                                        viewRenderable.setShadowReceiver(false);
                                                        wallObjectBean.viewRenderableList.add(viewRenderable);

                                                        Node tempTextNode = new FaceToCameraNode();
                                                        tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                                                        tempTextNode.setParent(wallObjectBean.objectLineList.get(index));
                                                        tempTextNode.setRenderable(viewRenderable);
                                                        wallObjectBean.objectTextList.add(tempTextNode);

                                                    });
                                        }


                                    }

                                }
                                else {

                                    clearTemp();
                                    // create wall object end point
                                    wallTempPoint.setParent(null);
                                    wallTempPoint = null;

                                    currentWallIndex = -1;
                                    currentGuideIndex = -1;

                                    for(int i = 0; i < wallObjectBeans.size(); i++) {
                                        WallObjectBean wallObjectBean = wallObjectBeans.get(i);

                                        for(Node node : wallObjectBean.objectPointList) {
                                            ARUtil.removeChildFormNode(node);
                                            node.setParent(null);
                                        }
                                        wallObjectBean.objectPointList.clear();
                                        wallObjectBean.objectTextList.clear();
                                        wallObjectBean.objectLineList.clear();
                                        wallObjectBean.viewRenderableList.clear();
                                    }

                                    wallObjectBeans.clear();
                                }

                                return false;
                            }

                            if(isReadyToAutoClose) {

                                clearTemp();
                                clearCenter();

                                drawLine(floorPolygonList.get(floorPolygonList.size() - 1), floorPolygonList.get(0));
                                DeviceUtil.vibrate(this, 5);
                                isAutoClosed = true;

                                createCellPolygon();
                                calculate();
                                createWall();
                                return false;
                            }

                            if(normalVectorOfPlane == null) {
                                // calculate normal vector of plane
                                normalVectorOfPlane = ARUtil.getNormalVectorOfThreeVectors(
                                        new Vector3(((Plane) trackable).getCenterPose().tx(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz()),
                                        new Vector3(((Plane) trackable).getCenterPose().tx() + ((Plane) trackable).getExtentX(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz()),
                                        new Vector3(((Plane) trackable).getCenterPose().tx(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz() + ((Plane) trackable).getExtentZ())
                                );
                                ILog.iLogDebug(TAG, "normalVectorOfPlane by cal " + normalVectorOfPlane.x + " " + normalVectorOfPlane.y + " " + normalVectorOfPlane.z);
                            }

//
//                            ILog.iLogDebug(TAG, "x " + ((Plane) trackable).getExtentX());
//                            ILog.iLogDebug(TAG, "z " + ((Plane) trackable).getExtentZ());
//                            ILog.iLogDebug(TAG, "center " + ((Plane) trackable).getCenterPose().tx() + " " + ((Plane) trackable).getCenterPose().ty() + " " + ((Plane) trackable).getCenterPose().tz());


                            if(anchorNode == null) {
                                Anchor anchor = hitResult.createAnchor();
                                anchorNode = ARUtil.createAnchorNode(anchor);
                                anchorNode.setParent(arSceneView.getScene());
                            }

                            // two vector is too near
                            if(floorPolygonList.size() > 1) {
                                if(Vector3.subtract(floorPolygonList.get(floorPolygonList.size() - 2).getLocalPosition(),
                                        floorPolygonList.get(floorPolygonList.size() - 1).getLocalPosition()).length() < 0.05) {
                                    return false;
                                }
                            }

                            Node node;
//                            bottomAnchorPolygon.add(anchorNode);
                            if(floorPolygonList.isEmpty()) {
                                node = ARUtil.createLocalNode(0, 0, 0, pointMaterial, shadow);
                            }
                            else {

//                                Quaternion localRotation = new Quaternion(hitResult.getHitPose().qx(), hitResult.getHitPose().qy(), hitResult.getHitPose().qz(), hitResult.getHitPose().qw());
//                                localRotation = Quaternion.multiply(this.anchorNode.getWorldRotation().inverted(), Preconditions.checkNotNull(localRotation));

                                Vector3 hitWorldPosition = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());
                                Vector3 localPosition = ARUtil.transformWorldPositionToLocalPositionOfParent(this.anchorNode, hitWorldPosition);

                                node = ARUtil.createLocalNode(localPosition.x, localPosition.y, localPosition.z, pointMaterial, shadow);
                            }

                            node.setParent(this.anchorNode);
                            floorPolygonList.add(node);

                            DeviceUtil.vibrate(this, 5);

//                            if(!bottomAnchorPolygon.isEmpty()) {
//                                fixedY = bottomAnchorPolygon.get(0).getWorldPosition().y;
//
//                                for(int i = 0; i < bottomAnchorPolygon.size(); i++) {
//                                    bottomAnchorPolygon.get(i).setWorldPosition(new Vector3(bottomAnchorPolygon.get(i).getWorldPosition().x, fixedY, bottomAnchorPolygon.get(i).getWorldPosition().z));
//                                }
//                            }
                            if(this.anchorNode != null) {
                                fixedY = anchorNode.getWorldPosition().y;
                            }

                            if(floorPolygonList.size() >= 2) {
                                drawLine(floorPolygonList.get(floorPolygonList.size() - 2), floorPolygonList.get(floorPolygonList.size() - 1));
                            }

                            clearTemp();

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

                    ARUtil.updatePlanRenderer(arSceneView.getPlaneRenderer());

                    Collection<Plane> planeCollection = frame.getUpdatedTrackables(Plane.class);
                    checkPlaneSize(planeCollection);

                    if(isAutoClosed) {

                        List<HitResult> hitTestResultList = frame.hitTest(screenCenterX, screenCenterY);

                        String planType = ARUtil.checkPlanType(
                                hitTestResultList, "",
                                getString(R.string.ar_plane_type_wall),
                                getString(R.string.ar_plane_type_ceiling),
                                getString(R.string.ar_plane_type_floor));

                        textViewPlaneType.setText(planType);

                        for (HitResult hitResult : hitTestResultList) {

                            // draw center point
                            if(centerPoint != null) {
                                centerPoint.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
                            }
                            else {
                                centerPoint = ARUtil.createWorldNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);
                                centerPoint.setParent(arSceneView.getScene());
                            }
                            // draw center point

                            List<Vector3> result = new ArrayList<>();
                            List<Integer> indexList = getThoughWall(result, hitResult);

                            if(result.isEmpty()) {

                                if(wallGuidePoint != null) {
                                    wallGuidePoint.setParent(null);
                                }
                                currentGuideIndex = -1;
                                textViewNearest.setText("");
                                return;
                            }

                            StringBuilder stringBuilder = new StringBuilder();

                            List<Float> distanceList = new ArrayList<>();
                            float distance;
                            Vector3 camera = new Vector3(arSceneView.getArFrame().getCamera().getPose().tx(), arSceneView.getArFrame().getCamera().getPose().ty(), arSceneView.getArFrame().getCamera().getPose().tz());
                            for(int i = 0; i < result.size(); i++) {
                                distanceList.add(Vector3.subtract(camera, result.get(i)).length());
                            }

                            int resultIndex = 0;
                            distance = distanceList.get(0);
                            for(int i = 0; i < distanceList.size(); i++) {

                                stringBuilder.append(distanceList.get(i)).append(" ");

                                if(distance > distanceList.get(i)) {
                                    distance = distanceList.get(i);
                                }
                            }

//                            ILog.iLogDebug(TAG, stringBuilder.toString());

                            for(int i = 0; i < distanceList.size(); i++) {
                                if(distance == distanceList.get(i)) {
                                    resultIndex = i;
                                    break;
                                }
                            }

                            currentGuideIndex = indexList.get(resultIndex);


                            textViewNearest.setText(String.valueOf(currentGuideIndex));

                            if(wallGuidePoint != null) {
                                wallGuidePoint.setWorldPosition(new Vector3(result.get(resultIndex).x, result.get(resultIndex).y, result.get(resultIndex).z));
                            }
                            else {
                                wallGuidePoint = ARUtil.createWorldNode(result.get(resultIndex).x, result.get(resultIndex).y, result.get(resultIndex).z, wallPointMaterial, shadow);
                            }
                            wallGuidePoint.setParent(arSceneView.getScene());


                            if(wallTempPoint != null) {

                                if(currentGuideIndex == currentWallIndex) {

                                    if(wallObjectBeans.isEmpty()) {
                                        return;
                                    }

                                    WallObjectBean wallObjectBean = wallObjectBeans.get(0);


                                    Vector3 horizontalVector3 = new Vector3();
                                    horizontalVector3.x = wallGuidePoint.getWorldPosition().x;
                                    horizontalVector3.y = wallTempPoint.getWorldPosition().y;
                                    horizontalVector3.z = wallGuidePoint.getWorldPosition().z;

                                    Vector3 horizontalLocalPosition = ARUtil.transformWorldPositionToLocalPositionOfParent(this.anchorNode, horizontalVector3);
//                                    Node horizontalNode = ARUtil.createLocalNode(horizontalLocalPosition.x, horizontalLocalPosition.y, horizontalLocalPosition.z, wallPointMaterial, shadow);
//
//
                                    Vector3 verticalVector3 = new Vector3();
                                    verticalVector3.x = wallTempPoint.getWorldPosition().x;
                                    verticalVector3.y = wallGuidePoint.getWorldPosition().y;
                                    verticalVector3.z = wallTempPoint.getWorldPosition().z;
                                    Vector3 verticalLocalPosition = ARUtil.transformWorldPositionToLocalPositionOfParent(this.anchorNode, verticalVector3);
//                                    Node verticalNode = ARUtil.createLocalNode(verticalVector3.x, verticalVector3.y, verticalVector3.z, wallPointMaterial, shadow);

//                                    drawWallTempLine

                                    if(wallObjectBean.viewRenderableList.size() < 4) {
                                        return;
                                    }

                                    wallObjectBean.objectPointList.get(1).setLocalPosition(horizontalLocalPosition);
                                    wallObjectBean.objectPointList.get(3).setLocalPosition(verticalLocalPosition);

                                    drawTempWallLine(wallObjectBean.objectPointList.get(0), wallObjectBean.objectPointList.get(1),
                                            wallObjectBean.objectLineList.get(0), wallObjectBean.objectTextList.get(0), wallObjectBean.viewRenderableList.get(0));

                                    drawTempWallLine(wallObjectBean.objectPointList.get(1), wallObjectBean.objectPointList.get(2),
                                            wallObjectBean.objectLineList.get(1), wallObjectBean.objectTextList.get(1), wallObjectBean.viewRenderableList.get(1));

                                    drawTempWallLine(wallObjectBean.objectPointList.get(2), wallObjectBean.objectPointList.get(3),
                                            wallObjectBean.objectLineList.get(2), wallObjectBean.objectTextList.get(2), wallObjectBean.viewRenderableList.get(2));

                                    drawTempWallLine(wallObjectBean.objectPointList.get(3), wallObjectBean.objectPointList.get(0),
                                            wallObjectBean.objectLineList.get(3), wallObjectBean.objectTextList.get(3), wallObjectBean.viewRenderableList.get(3));
                                }
                                else {
                                    wallGuidePoint.setWorldPosition(new Vector3(wallTempPoint.getWorldPosition().x, wallTempPoint.getWorldPosition().y, wallTempPoint.getWorldPosition().z));
                                }
                            }
                        }

                        return;
                    }

                    List<HitResult> hitTestResultList = frame.hitTest(screenCenterX, screenCenterY);

                    String planType = ARUtil.checkPlanType(
                            hitTestResultList, "",
                            getString(R.string.ar_plane_type_wall),
                            getString(R.string.ar_plane_type_ceiling),
                            getString(R.string.ar_plane_type_floor));

                    textViewPlaneType.setText(planType);

                    boolean isCeiling = planType.equals(getString(R.string.ar_plane_type_ceiling));

                    for (HitResult hitResult : hitTestResultList) {

                        Trackable trackable = hitResult.getTrackable();

                        if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                            toggleDistanceHint(hitResult.getDistance());

                            if(centerPoint != null) {
//                                if(bottomAnchorPolygon.isEmpty()) {
                                if(anchorNode == null) {
                                    centerPoint.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
                                }
                                else {
                                    centerPoint.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), fixedY, hitResult.getHitPose().tz()));
                                }
                            }
                            else {

//                                if(bottomAnchorPolygon.isEmpty()) {
                                if(anchorNode == null) {
                                    centerPoint = ARUtil.createWorldNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);
                                }
                                else {
                                    centerPoint = ARUtil.createWorldNode(hitResult.getHitPose().tx(), fixedY, hitResult.getHitPose().tz(), pointMaterial, shadow);
                                }

                                centerPoint.setParent(arSceneView.getScene());
                            }

                            if(floorPolygonList.isEmpty()) {
                                return;
                            }

                            if(isCeiling) {
                                // get distance ceiling
                                if(centerPoint != null) {
                                    centerPoint.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
                                }
                                else {
                                    centerPoint = ARUtil.createWorldNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);
                                }
//                                Vector3 floorPoint = new Vector3(bottomAnchorPolygon.get(0).getWorldPosition().x, bottomAnchorPolygon.get(0).getWorldPosition().y, bottomAnchorPolygon.get(0).getWorldPosition().z);
                                Vector3 floorPoint = new Vector3(anchorNode.getWorldPosition().x, anchorNode.getWorldPosition().y, anchorNode.getWorldPosition().z);
                                Vector3 ceiling = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());

                                height = ARUtil.getLengthBetweenPointToPlane(ceiling, floorPoint, normalVectorOfPlane);
                                textViewHeightRealTime.setText(String.valueOf(height));
                            }
                            else {
                                drawTempLine(floorPolygonList.get(floorPolygonList.size() - 1), centerPoint);
                            }


                            if(floorPolygonList.size() < 3) {
                                return;
                            }

                            if(checkClose(centerPoint, floorPolygonList.get(0))) {
                                drawTempLine(floorPolygonList.get(floorPolygonList.size() - 1), floorPolygonList.get(0));

                                if(!isReadyToAutoClose) {
                                    DeviceUtil.vibrate(this, 5);
                                }

                                isReadyToAutoClose = true;
                            }
                            else {
                                drawTempLine(floorPolygonList.get(floorPolygonList.size() - 1), centerPoint);
                                isReadyToAutoClose = false;
                            }

                            return;
                        }
                    }
                });

        buttonBack.setOnClickListener(view -> back());

        buttonReDetect.setOnClickListener(view -> reset());
    }

    private void createWallObject() {

//        if(wallTempPoint != null) {
//
//            if(currentGuideIndex == currentWallIndex) {
//
//                WallObjectBean wallObjectBean = new WallObjectBean();
//                wallObjectBean.objectPointList.add(wallTempPoint);
//
//                Node node = ARUtil.createWorldNode(, wallPointMaterial, shadow);
//                node.setWorldPosition(new Vector3());
//                wallObjectBean.objectPointList.add(node);
//
//                wallObjectBean.objectPointList.add(wallGuidePoint);
//
//                node = ARUtil.createWorldNode(, wallPointMaterial, shadow);
//                node.setWorldPosition(new Vector3());
//                wallObjectBean.objectPointList.add(node);
//
////                drawWallTempLine(wallTempPoint, wallGuidePoint);
//            }
//        }
    }

    private List<Integer> getThoughWall(List<Vector3> resultList, HitResult hitResult) {

        List<Integer> indexList = new ArrayList<>();

        Vector3 normalVector;
        Vector3 rayVector;
        Vector3 rayOrigin;
        Vector3 planePoint;

        for(int i = 0; i < wallBeanList.size(); i++) {

            // check wall test
            normalVector = ARUtil.getNormalVectorOfThreeVectors(
                    new Vector3(wallBeanList.get(i).endPointList.get(0).x, wallBeanList.get(i).endPointList.get(0).y, wallBeanList.get(i).endPointList.get(0).z),
                    new Vector3(wallBeanList.get(i).endPointList.get(1).x, wallBeanList.get(i).endPointList.get(1).y, wallBeanList.get(i).endPointList.get(1).z),
                    new Vector3(wallBeanList.get(i).endPointList.get(3).x, wallBeanList.get(i).endPointList.get(3).y, wallBeanList.get(i).endPointList.get(3).z)
            );

            rayVector = new Vector3(
                    hitResult.getHitPose().tx(),
                    hitResult.getHitPose().ty(),
                    hitResult.getHitPose().tz()
            );

            rayOrigin = new Vector3(
                    arSceneView.getArFrame().getCamera().getPose().tx(),
                    arSceneView.getArFrame().getCamera().getPose().ty(),
                    arSceneView.getArFrame().getCamera().getPose().tz()
            );

            planePoint = new Vector3(wallBeanList.get(i).endPointList.get(0).x, wallBeanList.get(i).endPointList.get(0).y, wallBeanList.get(i).endPointList.get(0).z);

            Vector3 result = new Vector3();
            boolean isPointInPlane = ARUtil.calculateIntersectionOfLineAndPlane(rayVector, rayOrigin, normalVector, planePoint, result) == 1;

//            boolean isPointInPoly = ARUtil.checkIsVectorInPolygon(result, wallPoint);
            boolean isPointInPoly = ARUtil.checkIsVectorInPolygon(result, wallBeanList.get(i).endPointList.get(0), wallBeanList.get(i).endPointList.get(2));

            if(isPointInPlane && isPointInPoly) {
                resultList.add(result);
                indexList.add(i);
            }
        }

        return indexList;
    }

    private void back() {
        if(isAutoClosed) {

            if(anchorNode != null) {
                ARUtil.removeChildFormNode(anchorNode);
                anchorNode.setParent(null);
                anchorNode = null;
            }

            floorPolygonList.clear();
            cellPolygonList.clear();
            textViewSizeList.clear();
            wallBeanList.clear();

            for(WallObjectBean wallObjectBean : wallObjectBeans) {
                for(Node node : wallObjectBean.objectPointList) {
                    node.setParent(null);
                }
            }
            wallObjectBeans.clear();

            clearTemp();
            clearCenter();

            fixedY = 0;

            if(wallGuidePoint != null) {
                wallGuidePoint.setParent(null);
                wallGuidePoint = null;
            }

            if(wallTempPoint != null) {
                wallTempPoint.setParent(null);
                wallTempPoint = null;
            }

            currentGuideIndex = -1;
            currentWallIndex = -1;

        }
        else {

            if(floorPolygonList.size() == 1) {

                if(anchorNode != null) {
                    ARUtil.removeChildFormNode(anchorNode);
                    anchorNode = null;
                }

                floorPolygonList.clear();
                textViewSizeList.clear();
                wallBeanList.clear();

                for(WallObjectBean wallObjectBean : wallObjectBeans) {
                    for(Node node : wallObjectBean.objectPointList) {
                        node.setParent(null);
                    }
                }
                wallObjectBeans.clear();

                clearTemp();
                clearCenter();

                fixedY = 0;

                if(wallGuidePoint != null) {
                    wallGuidePoint.setParent(null);
                    wallGuidePoint = null;
                }

                if(wallTempPoint != null) {
                    wallTempPoint.setParent(null);
                    wallTempPoint = null;
                }

                currentGuideIndex = -1;
                currentWallIndex = -1;
            }
            else if(floorPolygonList.size() > 1) {

                ARUtil.removeChildFormNode(floorPolygonList.get(floorPolygonList.size() - 2));
                floorPolygonList.get(floorPolygonList.size() - 1).setParent(null);
                floorPolygonList.remove(floorPolygonList.size() - 1);
                textViewSizeList.remove(textViewSizeList.size() - 1);

                clearTemp();
                clearCenter();

                if(wallGuidePoint != null) {
                    wallGuidePoint.setParent(null);
                    wallGuidePoint = null;
                }

                if(wallTempPoint != null) {
                    wallTempPoint.setParent(null);
                    wallTempPoint = null;
                }

                wallBeanList.clear();

                for(WallObjectBean wallObjectBean : wallObjectBeans) {
                    for(Node node : wallObjectBean.objectPointList) {
                        node.setParent(null);
                    }
                }
                wallObjectBeans.clear();

                currentGuideIndex = -1;
                currentWallIndex = -1;
            }
        }

        linearLayout.setVisibility(View.GONE);
        textViewHeight.setText("");
        textViewArea.setText("");
        textViewCircumference.setText("");
        textViewWallArea.setText("");
        textViewVolume.setText("");

        isAutoClosed = false;
        isReadyToAutoClose = false;
    }

    private void createCellPolygon() {
        cellPolygonList.clear();

        Node node;
        for(int i = 0; i < floorPolygonList.size(); i++) {
            node = ARUtil.createLocalNode(
                    floorPolygonList.get(i).getLocalPosition().x,
                    floorPolygonList.get(i).getLocalPosition().y + height,
                    floorPolygonList.get(i).getLocalPosition().z ,
                    pointMaterial, shadow);
//            node.setParent(bottomAnchorPolygon.get(i));
            node.setParent(anchorNode);
            cellPolygonList.add(node);
        }

        // draw vertical line
        for(int i = 0; i < floorPolygonList.size(); i++) {
            drawLine(floorPolygonList.get(i), cellPolygonList.get(i));
        }

        // connect node and make line close
        for(int i = 0; i < cellPolygonList.size() - 1; i++) {
            drawLine(cellPolygonList.get(i), cellPolygonList.get(i + 1));
        }
        drawLine(cellPolygonList.get(cellPolygonList.size() - 1), cellPolygonList.get(0));
    }

    private void createWall() {

        wallBeanList.clear();

        WallBean wallBean;
        for(int i = 0; i < floorPolygonList.size(); i++) {

            wallBean = new WallBean();

            if(i < floorPolygonList.size() - 1) {
                wallBean.endPointList.add(floorPolygonList.get(i).getWorldPosition());
                wallBean.endPointList.add(floorPolygonList.get(i + 1).getWorldPosition());
                wallBean.endPointList.add(cellPolygonList.get(i + 1).getWorldPosition());
                wallBean.endPointList.add(cellPolygonList.get(i).getWorldPosition());
            }
            else {
                wallBean.endPointList.add(floorPolygonList.get(i).getWorldPosition());
                wallBean.endPointList.add(floorPolygonList.get(0).getWorldPosition());
                wallBean.endPointList.add(cellPolygonList.get(0).getWorldPosition());
                wallBean.endPointList.add(cellPolygonList.get(i).getWorldPosition());
            }

            wallBeanList.add(wallBean);
        }

    }

    private void calculate() {

        linearLayout.setVisibility(View.VISIBLE);

        textViewHeight.setText(getString(R.string.ar_area_height_title) + " " + String.format("%.2f", ARUtil.getLengthByUnit(unit, height)) + ARUtil.getLengthUnitString(unit));

        float circumference = 0;
        for(int i = 0; i < floorPolygonList.size() - 1; i++) {
            circumference += Vector3.subtract(floorPolygonList.get(i + 1).getWorldPosition(), floorPolygonList.get(i).getWorldPosition()).length();
        }
        circumference += Vector3.subtract(floorPolygonList.get(floorPolygonList.size() - 1).getWorldPosition(), floorPolygonList.get(0).getWorldPosition()).length();
        textViewCircumference.setText(getString(R.string.ar_area_circumference_title) + " " + String.format("%.2f", ARUtil.getLengthByUnit(unit, circumference)) + ARUtil.getLengthUnitString(unit));

        float wallArea = ARUtil.getLengthByUnit(unit, circumference) * ARUtil.getLengthByUnit(unit, height);

        SpannableStringBuilder wallAreaString = new SpannableStringBuilder(getString(R.string.ar_wall_area_title) + " " + String.format("%.2f", wallArea));
        wallAreaString.append(ARUtil.getAreaUnitString(unit));
        textViewWallArea.setText(wallAreaString);


        float area = ARUtil.getAreaByUnit(unit, calculateArea());
        SpannableStringBuilder areaString = new SpannableStringBuilder(getString(R.string.ar_area_title) + " " + String.format("%.2f", area));
        areaString.append(ARUtil.getAreaUnitString(unit));
        textViewArea.setText(areaString);

        float volume = ARUtil.getLengthByUnit(unit, height) * area;
        SpannableStringBuilder volumeString = new SpannableStringBuilder(getString(R.string.ar_volume_title) + " " + String.format("%.2f", volume));
        volumeString.append(ARUtil.getVolumeUnitString(unit));
        textViewVolume.setText(volumeString);

    }

    private float calculateArea() {

        List<Vector3> vector3List = new ArrayList<>();
        for(int i = 0; i < floorPolygonList.size(); i++) {
            vector3List.add(floorPolygonList.get(i).getWorldPosition());
        }
        vector3List.add(floorPolygonList.get(0).getWorldPosition());

        float area = Math.abs(ARUtil.area3DPolygon(floorPolygonList.size(), vector3List, normalVectorOfPlane));
        ILog.iLogDebug(TAG, "area is " + area);
        return area;
    }


    private void clearCenter() {
        if(centerPoint != null) {
            centerPoint.setParent(null);
            centerPoint = null;
        }
    }

    private void clearTemp() {

        if(tempTextNode != null) {
            tempTextNode.setParent(null);
            tempTextNode = null;
        }

        if(tempLineNode != null) {
            tempLineNode.setParent(null);
            tempLineNode = null;
        }

    }

    private boolean checkClose(Node startNode, Node endNode) {
        return ARUtil.getNodesDistanceMetersWithoutHeight(startNode, endNode) < 0.06;
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

        float length = ARUtil.getLengthByUnit(unit, difference.length());

        ViewRenderable.builder()
                .setView(this, R.layout.view_renderable_text)
                .build()
                .thenAccept(viewRenderable -> {

                    TextView textView = ((TextView)viewRenderable.getView());
                    textView.setText(String.format("%.2fCM", length));
                    textViewSizeList.add(textView);
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

        float length = ARUtil.getLengthByUnit(unit, difference.length());

        if(tempTextNode != null) {
            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + ARUtil.getLengthUnitString(unit));
        }
        else {
            if(tempLineNode != null) {

                ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + ARUtil.getLengthUnitString(unit));

                tempTextNode = new FaceToCameraNode();
                tempTextNode.setParent(tempLineNode);

                tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                tempTextNode.setLocalPosition(new Vector3(0f, 0.08f, 0f));
                tempTextNode.setRenderable(viewRenderableSizeText);

            }
        }
    }


    private void drawTempWallLine(Node startNode, Node endNode, Node tempLineNode, Node tempTextNode, ViewRenderable viewRenderableSizeText) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);

        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        if(tempLineNode != null) {

            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), wallLineMaterial);
            lineMode.setShadowCaster(shadow);
            lineMode.setShadowReceiver(shadow);

            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }
        else {
            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), wallLineMaterial);
            lineMode.setShadowCaster(shadow);
            lineMode.setShadowReceiver(shadow);

            tempLineNode = new Node();
            tempLineNode.setParent(startNode);
            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }

        float length = ARUtil.getLengthByUnit(unit, difference.length());

        if(tempTextNode != null) {
            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + ARUtil.getLengthUnitString(unit));
        }
        else {
            if(tempLineNode != null) {

                ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + ARUtil.getLengthUnitString(unit));

                tempTextNode = new FaceToCameraNode();
                tempTextNode.setParent(tempLineNode);

                tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
                tempTextNode.setLocalPosition(new Vector3(0f, 0.08f, 0f));
                tempTextNode.setRenderable(viewRenderableSizeText);

            }
        }
    }

    private void checkPlaneSize(Collection<Plane> planeCollection) {
        for (Plane plane : planeCollection) {

            if (plane.getTrackingState() == TrackingState.TRACKING) {

                if((plane.getExtentX() * plane.getExtentZ() * 2) > 3) {
                    textView.setVisibility(View.GONE);
                }
            }
        }
    }


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