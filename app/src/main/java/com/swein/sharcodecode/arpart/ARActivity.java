package com.swein.sharcodecode.arpart;

import android.app.Activity;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
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
import com.swein.sharcodecode.framework.util.ar.ARUtil;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.device.DeviceUtil;
import com.swein.sharcodecode.framework.util.math.MathUtil;
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

    private TextView textViewPlaneType;

    private Node centerPoint;

    private Button buttonBack;
    private Button buttonReDetect;

    private Node tempLineNode;
    private FaceToCameraNode tempTextNode;

    private List<AnchorNode> bottomAnchorPolygon = new ArrayList<>();
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

    private float height = 1;

    private enum Unit {
        M, CM
    }

    private Unit unit = Unit.CM;

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
    }

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
                                calculate();
                                return false;
                            }


                            Anchor anchor = hitResult.createAnchor();
                            AnchorNode anchorNode = ARUtil.createAnchorNode(anchor);
                            anchorNode.setParent(arSceneView.getScene());
                            bottomAnchorPolygon.add(anchorNode);

                            Node node = ARUtil.createLocalNode(0, 0, 0, pointMaterial, shadow);
                            node.setParent(anchorNode);
                            floorPolygon.add(node);

                            DeviceUtil.vibrate(this, 5);

                            if(floorPolygon.size() >= 2) {
                                drawLine(floorPolygon.get(floorPolygon.size() - 2), floorPolygon.get(floorPolygon.size() - 1));
                            }

                            clearTemp();


                            tempNode = ARUtil.createWorldNode(node.getWorldPosition().x, node.getWorldPosition().y, node.getWorldPosition().z,
                                    pointMaterial, shadow);
                            tempNode.setParent(arSceneView.getScene());

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
                        return;
                    }

                    List<HitResult> hitTestResultList = frame.hitTest(screenCenterX, screenCenterY);

                    textViewPlaneType.setText(
                            ARUtil.checkPlanType(
                                    hitTestResultList, "",
                                    getString(R.string.ar_plane_type_wall),
                                    getString(R.string.ar_plane_type_ceiling),
                                    getString(R.string.ar_plane_type_floor))
                    );

                    for (HitResult hitResult : hitTestResultList) {

                        Trackable trackable = hitResult.getTrackable();

                        if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                            toggleDistanceHint(hitResult.getDistance());

                            if(centerPoint != null) {
                                centerPoint.setLocalPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
                            }
                            else {
                                centerPoint = ARUtil.createWorldNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);
                                centerPoint.setParent(arSceneView.getScene());
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

            for(AnchorNode anchorNode : bottomAnchorPolygon) {
                ARUtil.removeChildFormNode(anchorNode);
                anchorNode.setParent(null);
            }

            bottomAnchorPolygon.clear();
            floorPolygon.clear();
            cellPolygon.clear();

            clearTemp();
            clearCenter();

            isAutoClosed = false;
            isReadyToAutoClose = false;
        }
        else {

            if(bottomAnchorPolygon.size() == 1) {
                bottomAnchorPolygon.get(0).setParent(null);
                ARUtil.removeChildFormNode(bottomAnchorPolygon.get(0));
                bottomAnchorPolygon.clear();
                floorPolygon.clear();

                clearTemp();
                clearCenter();
            }
            else if(bottomAnchorPolygon.size() > 1) {

                ARUtil.removeChildFormNode(bottomAnchorPolygon.get(bottomAnchorPolygon.size() - 1));
                bottomAnchorPolygon.get(bottomAnchorPolygon.size() - 1).setParent(null);
                bottomAnchorPolygon.remove(bottomAnchorPolygon.size() - 1);
                floorPolygon.remove(floorPolygon.size() - 1);

                ARUtil.removeChildFormNode(floorPolygon.get(floorPolygon.size() - 1));

                clearTemp();
                clearCenter();

                tempNode = ARUtil.createWorldNode(
                        floorPolygon.get(floorPolygon.size() - 1).getWorldPosition().x,
                        floorPolygon.get(floorPolygon.size() - 1).getWorldPosition().y,
                        floorPolygon.get(floorPolygon.size() - 1).getWorldPosition().z,
                        pointMaterial, shadow);

                tempNode.setParent(arSceneView.getScene());
            }
        }

        linearLayout.setVisibility(View.GONE);
        textViewHeight.setText("");
        textViewArea.setText("");
        textViewCircumference.setText("");
        textViewWallArea.setText("");
        textViewVolume.setText("");
    }

    private void createCellPolygon() {
        cellPolygon.clear();

        Node node;
        for(int i = 0; i < floorPolygon.size(); i++) {
            node = ARUtil.createLocalNode(
                    floorPolygon.get(i).getLocalPosition().x,
                    floorPolygon.get(i).getLocalPosition().y + height,
                    floorPolygon.get(i).getLocalPosition().z ,
                    pointMaterial, shadow);
            node.setParent(bottomAnchorPolygon.get(i));
            cellPolygon.add(node);
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

        linearLayout.setVisibility(View.VISIBLE);

        textViewHeight.setText(getString(R.string.ar_area_height_title) + " " + String.format("%.2f", getLengthByUnit(height)) + getLengthUnitString(unit));

        float circumference = 0;
        for(int i = 0; i < floorPolygon.size() - 1; i++) {
            circumference += Vector3.subtract(floorPolygon.get(i + 1).getWorldPosition(), floorPolygon.get(i).getWorldPosition()).length();
        }
        circumference += Vector3.subtract(floorPolygon.get(floorPolygon.size() - 1).getWorldPosition(), floorPolygon.get(0).getWorldPosition()).length();
        textViewCircumference.setText(getString(R.string.ar_area_circumference_title) + " " + String.format("%.2f", getLengthByUnit(circumference)) + getLengthUnitString(unit));

        float wallArea = getLengthByUnit(circumference) * getLengthByUnit(height);

        SpannableStringBuilder wallAreaString = new SpannableStringBuilder(getString(R.string.ar_wall_area_title) + " " + String.format("%.2f", wallArea));
        wallAreaString.append(getAreaUnitString(unit));
        textViewWallArea.setText(wallAreaString);


        float area = getAreaByUnit(calculateArea());
        SpannableStringBuilder areaString = new SpannableStringBuilder(getString(R.string.ar_area_title) + " " + String.format("%.2f", area));
        areaString.append(getAreaUnitString(unit));
        textViewArea.setText(areaString);

        float volume = getLengthByUnit(height) * area;
        SpannableStringBuilder volumeString = new SpannableStringBuilder(getString(R.string.ar_volume_title) + " " + String.format("%.2f", volume));
        volumeString.append(getVolumeUnitString(unit));
        textViewVolume.setText(volumeString);

    }

    private float calculateArea() {

        // get normal vector of bottom plane
        Vector3 normalVectorOfPlane = MathUtil.getNormalVectorOfThreeVectors(
                floorPolygon.get(0).getWorldPosition(),
                floorPolygon.get(1).getWorldPosition(),
                floorPolygon.get(floorPolygon.size() - 1).getWorldPosition()
        );

        ILog.iLogDebug(TAG, "normalVectorOfPlane " + normalVectorOfPlane.x + " " + normalVectorOfPlane.y + " " + normalVectorOfPlane.z);

        List<Vector3> vector3List = new ArrayList<>();
        for(int i = 0; i < floorPolygon.size(); i++) {
            vector3List.add(floorPolygon.get(i).getWorldPosition());
        }
        vector3List.add(floorPolygon.get(0).getWorldPosition());

        float area = MathUtil.area3DPolygon(floorPolygon.size(), vector3List, normalVectorOfPlane);
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

        float length = getLengthByUnit(difference.length());

        ViewRenderable.builder()
                .setView(this, R.layout.view_renderable_text)
                .build()
                .thenAccept(viewRenderable -> {

                    ((TextView)viewRenderable.getView()).setText(String.format("%.2fCM", length));
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

        float length = getLengthByUnit(difference.length());

        if(tempTextNode != null) {
            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + getLengthUnitString(unit));
        }
        else {
            if(tempLineNode != null) {

                ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + getLengthUnitString(unit));

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

    private String getLengthUnitString(Unit unit) {
        switch (unit) {
            case M:
                return "m";

            case CM:
                return "cm";

            default:
                return "";
        }
    }

    private SpannableString getAreaUnitString(Unit unit) {
        switch (unit) {
            case M:
                return getM2();

            case CM:
                return getCM2();

            default:
                return null;
        }
    }

    private SpannableString getVolumeUnitString(Unit unit) {
        switch (unit) {
            case M:
                return getM3();

            case CM:
                return getCM3();

            default:
                return null;
        }
    }

    private float getLengthByUnit(float length) {
        switch (unit) {
            case CM:
                return length * 100;

            case M:
                return length;

            default:
                return 0;
        }
    }

    private float getAreaByUnit(float area) {
        switch (unit) {
            case CM:
                return area * 10000;

            case M:
                return area;

            default:
                return 0;
        }
    }

    private SpannableString getM2() {
        SpannableString m2 = new SpannableString("m2");
        m2.setSpan(new RelativeSizeSpan(0.5f), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        m2.setSpan(new SuperscriptSpan(), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return m2;
    }

    private SpannableString getCM2() {
        SpannableString cm2 = new SpannableString("cm2");
        cm2.setSpan(new RelativeSizeSpan(0.5f), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cm2.setSpan(new SuperscriptSpan(), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return cm2;
    }

    private SpannableString getM3() {
        SpannableString m2 = new SpannableString("m3");
        m2.setSpan(new RelativeSizeSpan(0.5f), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        m2.setSpan(new SuperscriptSpan(), 1, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return m2;
    }

    private SpannableString getCM3() {
        SpannableString cm2 = new SpannableString("cm3");
        cm2.setSpan(new RelativeSizeSpan(0.5f), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        cm2.setSpan(new SuperscriptSpan(), 2, 3, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return cm2;
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