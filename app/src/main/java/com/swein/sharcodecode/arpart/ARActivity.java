package com.swein.sharcodecode.arpart;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
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
import com.swein.sharcodecode.arpart.bean.object.WallObjectBean;
import com.swein.sharcodecode.arpart.builder.ARBuilder;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARESSArrows;
import com.swein.sharcodecode.arpart.environment.AREnvironment;
import com.swein.sharcodecode.framework.util.device.DeviceUtil;
import com.swein.sharcodecode.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.sharcodecode.popup.ARDrawObjectViewHolder;
import com.swein.sharcodecode.popup.ARHintPopupViewHolder;
import com.swein.sharcodecode.popup.ARMeasureHeightHintViewHolder;
import com.swein.sharcodecode.popup.ARSelectUnitViewHolder;

import java.util.ArrayList;
import java.util.List;

public class ARActivity extends FragmentActivity {

    private final static String TAG = "ARActivity";

    private ArSceneView arSceneView;

    private TextView textViewHint;
    private FrameLayout frameLayoutTooCloseTooFar;
    private TextView textViewTooCloseTooFar;

    private LinearLayout linearLayoutInfo;
    private TextView textViewArea;
    private TextView textViewCircumference;
    private TextView textViewHeight;
    private TextView textViewWallArea;
    private TextView textViewVolume;

    private TextView textViewHeightRealTime;

    private TextView textViewPlaneType;

    private TextView textViewNearest;

    private FrameLayout frameLayoutPopup;

    private Node centerPoint;
    private Node wallGuidePoint;
    private Node wallTempPoint;
    private int currentWallIndex = -1;
    private int currentGuideIndex = -1;

    private ImageView imageViewBack;
    private ImageView imageViewReset;

    private Node tempLineNode;
    private FaceToCameraNode tempTextNode;

//    private List<AnchorNode> bottomAnchorPolygon = new ArrayList<>();
    private AnchorNode anchorNode;
    private List<Node> floorPolygonList = new ArrayList<>();
    private List<Node> cellPolygonList = new ArrayList<>();
    private List<TextView> textViewSizeList = new ArrayList<>();
    private Vector3 normalVectorOfPlane;
//    private List<WallBean> wallBeanList = new ArrayList<>();
    private List<WallObjectBean> wallObjectBeans = new ArrayList<>();

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


    private ARSelectUnitViewHolder arSelectUnitViewHolder;
    private ARMeasureHeightHintViewHolder arMeasureHeightHintViewHolder;
    private ARDrawObjectViewHolder arDrawObjectViewHolder;
    private ARHintPopupViewHolder arHintPopupViewHolder;

//    private ARBuilder.ARUnit ARUnit = ARBuilder.ARUnit.CM;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        initESS();
        initData();

        findView();
        setListener();

        initAR();
    }

    private void initESS() {

        EventCenter.instance.addEventObserver(ARESSArrows.DETECTING_TARGET_MINIMUM_AREA_SIZE, this, (arrow, poster, data) -> {
            int percentage = (int) data.get("percentage");
            textViewHint.setVisibility(View.VISIBLE);
            textViewHint.setText(percentage + "%");
        });

        EventCenter.instance.addEventObserver(ARESSArrows.DETECTED_TARGET_MINIMUM_AREA_SIZE_FINISHED, this, (arrow, poster, data) -> {

            textViewHint.setVisibility(View.GONE);
            textViewHint.setText("");

            showMeasureHeightPopup();
        });

        EventCenter.instance.addEventObserver(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_TOO_CLOSE, this, (arrow, poster, data) -> {
            frameLayoutTooCloseTooFar.setVisibility(View.VISIBLE);
            textViewTooCloseTooFar.setText(R.string.ar_too_close);
        });

        EventCenter.instance.addEventObserver(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_TOO_FAR, this, (arrow, poster, data) -> {
            frameLayoutTooCloseTooFar.setVisibility(View.VISIBLE);
            textViewTooCloseTooFar.setText(R.string.ar_too_far);
        });

        EventCenter.instance.addEventObserver(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_OK, this, (arrow, poster, data) -> {
            textViewTooCloseTooFar.setText("");
            frameLayoutTooCloseTooFar.setVisibility(View.GONE);
        });
    }


    private void initData() {

        // TODO delete
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
        // TODO delete

    }

    private void findView() {

        arSceneView = findViewById(R.id.arSceneView);
        textViewHint = findViewById(R.id.textViewHint);
        frameLayoutTooCloseTooFar = findViewById(R.id.frameLayoutTooCloseTooFar);
        textViewTooCloseTooFar = findViewById(R.id.textViewTooCloseTooFar);
        imageViewBack = findViewById(R.id.imageViewBack);
        imageViewReset = findViewById(R.id.imageViewReset);
        textViewPlaneType = findViewById(R.id.textViewPlaneType);

        linearLayoutInfo = findViewById(R.id.linearLayoutInfo);
        textViewArea = findViewById(R.id.textViewArea);
        textViewCircumference = findViewById(R.id.textViewCircumference);
        textViewHeight = findViewById(R.id.textViewHeight);
        textViewWallArea = findViewById(R.id.textViewWallArea);
        textViewVolume = findViewById(R.id.textViewVolume);

        textViewHeightRealTime = findViewById(R.id.textViewHeightRealTime);

        textViewNearest = findViewById(R.id.textViewNearest);

        frameLayoutPopup = findViewById(R.id.frameLayoutPopup);
    }

    private void initAR() {
        AREnvironment.instance.init(this, new AREnvironment.AREnvironmentDelegate() {
            @Override
            public void onUpdatePlaneType(String type) {
                textViewPlaneType.setText(type);
            }

            @Override
            public void showDetectFloorHint() {
                showDetectFloorPopup();
            }

            @Override
            public void showMeasureHeightSelectPopup() {
                showMeasureHeightPopup();
            }

            @Override
            public void onMeasureHeight(float height) {
                ARBuilder.instance.height = height;
                String heightString = String.format("%.2f", MathTool.getLengthByUnit(ARBuilder.instance.arUnit, height)) + MathTool.getLengthUnitString(ARBuilder.instance.arUnit);
                textViewHeightRealTime.setText(heightString);
                ARBuilder.instance.arProcess = ARBuilder.ARProcess.MEASURE_ROOM;

                // clear node when measure height finished
                ARTool.removeChildFormNode(ARBuilder.instance.anchorNode);

                if(ARBuilder.instance.anchorNode != null) {
                    ARBuilder.instance.anchorNode.setParent(null);
                    ARBuilder.instance.anchorNode = null;
                }

                if(ARBuilder.instance.measureHeightFloorNode != null) {
                    ARBuilder.instance.measureHeightFloorNode.setParent(null);
                    ARBuilder.instance.measureHeightFloorNode = null;
                }

                if(ARBuilder.instance.measureHeightCeilingNode != null) {
                    ARBuilder.instance.measureHeightCeilingNode.setParent(null);
                    ARBuilder.instance.measureHeightCeilingNode = null;
                }

                textViewHint.setText("");
                textViewHint.setVisibility(View.GONE);

                showMeasureRoomPopup();
            }
        });

        ARBuilder.instance.init(this, new ARBuilder.ARBuilderDelegate() {
            @Override
            public void onCalculate(float height, float area, float circumference, float wallArea, float volume) {
                linearLayoutInfo.setVisibility(View.VISIBLE);

                textViewHeight.setText(getString(R.string.ar_area_height_title) + " " +
                        String.format("%.2f", MathTool.getLengthByUnit(ARBuilder.instance.arUnit, height)) + MathTool.getLengthUnitString(ARBuilder.instance.arUnit));

                textViewCircumference.setText(getString(R.string.ar_area_circumference_title) + " " +
                        String.format("%.2f", MathTool.getLengthByUnit(ARBuilder.instance.arUnit, circumference)) + MathTool.getLengthUnitString(ARBuilder.instance.arUnit));

                SpannableStringBuilder wallAreaString = new SpannableStringBuilder(getString(R.string.ar_wall_area_title) + " " + String.format("%.2f", wallArea));
                wallAreaString.append(MathTool.getAreaUnitString(ARBuilder.instance.arUnit));
                textViewWallArea.setText(wallAreaString);


                SpannableStringBuilder areaString = new SpannableStringBuilder(getString(R.string.ar_area_title) + " " + String.format("%.2f", area));
                areaString.append(MathTool.getAreaUnitString(ARBuilder.instance.arUnit));
                textViewArea.setText(areaString);

                SpannableStringBuilder volumeString = new SpannableStringBuilder(getString(R.string.ar_volume_title) + " " + String.format("%.2f", volume));
                volumeString.append(MathTool.getVolumeUnitString(ARBuilder.instance.arUnit));
                textViewVolume.setText(volumeString);
            }

            @Override
            public void backToMeasureHeight() {
                textViewHeightRealTime.setText("");
                textViewHint.setText("");
                textViewHint.setVisibility(View.GONE);
                showMeasureHeightPopup();
            }
        });

        AREnvironment.instance.hitPointX = DeviceUtil.getScreenCenterX(this);
        AREnvironment.instance.hitPointY = DeviceUtil.getScreenCenterY(this);
    }

    @SuppressLint("RestrictedApi")
    private void setListener() {
        // Set a touch listener on the Scene to listen for taps.
        arSceneView.getScene().setOnTouchListener(
                (HitTestResult hitTestResult, MotionEvent event) -> {


                    if(!AREnvironment.instance.checkPlanEnable(arSceneView.getArFrame())) {
                        return false;
                    }

                    AREnvironment.instance.onTouch(arSceneView);

                    Frame frame = arSceneView.getArFrame();

                    if(true) {
                        return false;
                    }

                    List<HitResult> hitTestResultList = frame.hitTest(0, 0);

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
                                    Vector3 vector3Local = MathTool.transformWorldPositionToLocalPositionOfParent(anchorNode, vector3World);


                                    wallTempPoint = ARTool.createLocalNode(
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
                                        Vector3 horizontalLocalPosition = MathTool.transformWorldPositionToLocalPositionOfParent(this.anchorNode, horizontalVector3);
                                        Node horizontalNode = ARTool.createLocalNode(horizontalLocalPosition.x, horizontalLocalPosition.y, horizontalLocalPosition.z, wallPointMaterial, shadow);
                                        horizontalNode.setParent(anchorNode);
                                        wallObjectBean.objectPointList.add(horizontalNode);

                                        wallObjectBean.objectPointList.add(wallGuidePoint);

                                        Vector3 verticalVector3 = new Vector3();
                                        verticalVector3.x = wallTempPoint.getWorldPosition().x;
                                        verticalVector3.y = wallGuidePoint.getWorldPosition().y;
                                        verticalVector3.z = wallTempPoint.getWorldPosition().z;
                                        Node verticalNode = ARTool.createLocalNode(verticalVector3.x, verticalVector3.y, verticalVector3.z, wallPointMaterial, shadow);
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
                                            ARTool.removeChildFormNode(node);
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

                            break;
                        }
                    }

                    return false;
                });

        arSceneView.getScene().addOnUpdateListener(
                frameTime -> {

                    if(!AREnvironment.instance.checkPlanEnable(arSceneView.getArFrame())) {
                        return;
                    }

                    AREnvironment.instance.updateCloudPoint(arSceneView);
                    AREnvironment.instance.updatePlaneType(arSceneView);

                    AREnvironment.instance.onUpdateFrame(arSceneView);

                    if(true) {
                        return;
                    }

                    // get camera frame when find a plan
                    Frame frame = arSceneView.getArFrame();

                    if(isAutoClosed) {

                        List<HitResult> hitTestResultList = frame.hitTest(0, 0);


                        for (HitResult hitResult : hitTestResultList) {

                            // draw center point
                            if(centerPoint != null) {
                                centerPoint.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
                            }
                            else {
                                centerPoint = ARTool.createWorldNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);
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
                                wallGuidePoint = ARTool.createWorldNode(result.get(resultIndex).x, result.get(resultIndex).y, result.get(resultIndex).z, wallPointMaterial, shadow);
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

                                    Vector3 horizontalLocalPosition = MathTool.transformWorldPositionToLocalPositionOfParent(this.anchorNode, horizontalVector3);
//                                    Node horizontalNode = ARUtil.createLocalNode(horizontalLocalPosition.x, horizontalLocalPosition.y, horizontalLocalPosition.z, wallPointMaterial, shadow);
//
//
                                    Vector3 verticalVector3 = new Vector3();
                                    verticalVector3.x = wallTempPoint.getWorldPosition().x;
                                    verticalVector3.y = wallGuidePoint.getWorldPosition().y;
                                    verticalVector3.z = wallTempPoint.getWorldPosition().z;
                                    Vector3 verticalLocalPosition = MathTool.transformWorldPositionToLocalPositionOfParent(this.anchorNode, verticalVector3);
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

                });

        imageViewBack.setOnClickListener(view -> {
            ARBuilder.instance.back();
            clearRoomInfo();
        });

        imageViewReset.setOnClickListener(view -> AREnvironment.instance.reset(this, arSceneView, this::finish, () -> {

            clearRoomInfo();

            textViewHint.setText("");
            textViewHint.setVisibility(View.GONE);
            textViewHeightRealTime.setText("");

            ARBuilder.instance.clearGuidePlane();
            ARBuilder.instance.clearGuide();
            ARBuilder.instance.clearTemp();
            ARBuilder.instance.clearAnchor();
            ARBuilder.instance.height = 0;
            ARBuilder.instance.floorFixedY = 0;
            ARBuilder.instance.normalVectorOfPlane = null;
            ARBuilder.instance.roomBean = null;

            ARBuilder.instance.isReadyToAutoClose = false;

            ARBuilder.instance.arProcess = ARBuilder.ARProcess.DETECT_PLANE;
            ARBuilder.instance.measureHeightWay = ARBuilder.MeasureHeightWay.NONE;

        }));

    }

    private void clearRoomInfo() {
        linearLayoutInfo.setVisibility(View.GONE);
        textViewArea.setText("");
        textViewCircumference.setText("");
        textViewHeight.setText("");
        textViewWallArea.setText("");
        textViewVolume.setText("");
    }

    private void showDetectFloorPopup() {
        arHintPopupViewHolder = new ARHintPopupViewHolder(this, this::closeDetectFloorPopup);

        arHintPopupViewHolder.setTitle(getString(R.string.ar_scan_floor));
        arHintPopupViewHolder.setMessage(getString(R.string.ar_scan_ready_hint));

        frameLayoutPopup.addView(arHintPopupViewHolder.getView());
        frameLayoutPopup.setVisibility(View.VISIBLE);
    }

    private boolean closeDetectFloorPopup() {

        if(arHintPopupViewHolder != null) {
            frameLayoutPopup.setVisibility(View.GONE);
            frameLayoutPopup.removeAllViews();
            arHintPopupViewHolder = null;

            return true;
        }

        return false;
    }

    private void showMeasureHeightPopup() {
        arMeasureHeightHintViewHolder = new ARMeasureHeightHintViewHolder(this, new ARMeasureHeightHintViewHolder.ARMeasureHeightHintViewHolderDelegate() {
            @Override
            public void onConfirm(ARBuilder.MeasureHeightWay measureHeightWay) {

                ARBuilder.instance.measureHeightWay = measureHeightWay;
                closeMeasureHeightPopup();

                switch (ARBuilder.instance.measureHeightWay) {
                    case AUTO:
                        textViewHint.setText(getString(R.string.ar_draw_height_by_ceiling_auto));
                        textViewHint.setVisibility(View.VISIBLE);
                        break;

                    case DRAW:
                        textViewHint.setText(getString(R.string.ar_draw_height_direct));
                        textViewHint.setVisibility(View.VISIBLE);
                        break;
                }

                ARBuilder.instance.arProcess = ARBuilder.ARProcess.MEASURE_HEIGHT;
            }

            @Override
            public void onClose() {
                closeMeasureHeightPopup();
            }

            @Override
            public void onConfirmInput(float height) {
                ARBuilder.instance.height = height;
                closeMeasureHeightPopup();
                ARBuilder.instance.arProcess = ARBuilder.ARProcess.MEASURE_ROOM;

                String heightString = String.format("%.2f", MathTool.getLengthByUnit(ARBuilder.instance.arUnit, height)) + MathTool.getLengthUnitString(ARBuilder.instance.arUnit);
                textViewHeightRealTime.setText(heightString);

                showMeasureRoomPopup();
            }

        }, ARBuilder.instance.arUnit);

        frameLayoutPopup.addView(arMeasureHeightHintViewHolder.getView());
        frameLayoutPopup.setVisibility(View.VISIBLE);
    }

    private boolean closeMeasureHeightPopup() {
        if(arMeasureHeightHintViewHolder != null) {
            frameLayoutPopup.setVisibility(View.GONE);
            frameLayoutPopup.removeAllViews();
            arMeasureHeightHintViewHolder = null;

            return true;
        }

        return false;
    }

    private void showMeasureRoomPopup() {
        arHintPopupViewHolder = new ARHintPopupViewHolder(this, this::closeMeasureRoom);

        arHintPopupViewHolder.setTitle(getString(R.string.ar_draw_floor_title));
        arHintPopupViewHolder.setMessage(getString(R.string.ar_draw_floor));
        frameLayoutPopup.addView(arHintPopupViewHolder.getView());
        frameLayoutPopup.setVisibility(View.VISIBLE);

        textViewHint.setVisibility(View.VISIBLE);
        textViewHint.setText(getString(R.string.ar_draw_floor));
    }

    private boolean closeMeasureRoom() {
        if(arHintPopupViewHolder != null) {
            frameLayoutPopup.setVisibility(View.GONE);
            frameLayoutPopup.removeAllViews();
            arHintPopupViewHolder = null;

            return true;
        }

        return false;
    }

//    private void showSelectUnitPopup() {
//        arSelectUnitViewHolder = new ARSelectUnitViewHolder(this, ARBuilder.instance.arUnit, new ARSelectUnitViewHolder.ARSelectUnitViewHolderDelegate() {
//            @Override
//            public void onSelectUnit(String unit) {
//
//
//                switch (unit) {
//                    case "m":
//                        ARBuilder.instance.arUnit = ARBuilder.ARUnit.M;
//                        break;
//
//                    case "cm":
//                        ARBuilder.instance.arUnit = ARBuilder.ARUnit.CM;
//                        break;
//                }
//
//                closeSelectUnitPopup();
//
//                // update all text view
//                EventCenter.instance.sendEvent(ARESSArrows.CHANGE_UNIT, this, null);
//            }
//
//            @Override
//            public void onClose() {
//                closeSelectUnitPopup();
//            }
//        });
//
//        frameLayoutPopup.addView(arSelectUnitViewHolder.getView());
//        frameLayoutPopup.setVisibility(View.VISIBLE);
//    }
//
//    private boolean closeSelectUnitPopup() {
//
//        if(arSelectUnitViewHolder != null) {
//            frameLayoutPopup.removeAllViews();
//            arSelectUnitViewHolder = null;
//
//            return true;
//        }
//
//        return false;
//    }

    private List<Integer> getThoughWall(List<Vector3> resultList, HitResult hitResult) {

        List<Integer> indexList = new ArrayList<>();
//
//        Vector3 normalVector;
//        Vector3 rayVector;
//        Vector3 rayOrigin;
//        Vector3 planePoint;
//
//        for(int i = 0; i < wallBeanList.size(); i++) {
//
//            // check wall test
//            normalVector = ARTool.getNormalVectorOfThreeVectors(
//                    new Vector3(wallBeanList.get(i).endPointList.get(0).x, wallBeanList.get(i).endPointList.get(0).y, wallBeanList.get(i).endPointList.get(0).z),
//                    new Vector3(wallBeanList.get(i).endPointList.get(1).x, wallBeanList.get(i).endPointList.get(1).y, wallBeanList.get(i).endPointList.get(1).z),
//                    new Vector3(wallBeanList.get(i).endPointList.get(3).x, wallBeanList.get(i).endPointList.get(3).y, wallBeanList.get(i).endPointList.get(3).z)
//            );
//
//            rayVector = new Vector3(
//                    hitResult.getHitPose().tx(),
//                    hitResult.getHitPose().ty(),
//                    hitResult.getHitPose().tz()
//            );
//
//            rayOrigin = new Vector3(
//                    arSceneView.getArFrame().getCamera().getPose().tx(),
//                    arSceneView.getArFrame().getCamera().getPose().ty(),
//                    arSceneView.getArFrame().getCamera().getPose().tz()
//            );
//
//            planePoint = new Vector3(wallBeanList.get(i).endPointList.get(0).x, wallBeanList.get(i).endPointList.get(0).y, wallBeanList.get(i).endPointList.get(0).z);
//
//            Vector3 result = new Vector3();
//            boolean isPointInPlane = ARUtil.calculateIntersectionOfLineAndPlane(rayVector, rayOrigin, normalVector, planePoint, result) == 1;
//
////            boolean isPointInPoly = ARUtil.checkIsVectorInPolygon(result, wallPoint);
//            boolean isPointInPoly = ARUtil.checkIsVectorInPolygon(result, wallBeanList.get(i).endPointList.get(0), wallBeanList.get(i).endPointList.get(2));
//
//            if(isPointInPlane && isPointInPoly) {
//                resultList.add(result);
//                indexList.add(i);
//            }
//        }

        return indexList;
    }


    private void createWall() {

//        wallBeanList.clear();
//
//        WallBean wallBean;
//        for(int i = 0; i < floorPolygonList.size(); i++) {
//
//            wallBean = new WallBean();
//
//            if(i < floorPolygonList.size() - 1) {
//                wallBean.endPointList.add(floorPolygonList.get(i).getWorldPosition());
//                wallBean.endPointList.add(floorPolygonList.get(i + 1).getWorldPosition());
//                wallBean.endPointList.add(cellPolygonList.get(i + 1).getWorldPosition());
//                wallBean.endPointList.add(cellPolygonList.get(i).getWorldPosition());
//            }
//            else {
//                wallBean.endPointList.add(floorPolygonList.get(i).getWorldPosition());
//                wallBean.endPointList.add(floorPolygonList.get(0).getWorldPosition());
//                wallBean.endPointList.add(cellPolygonList.get(0).getWorldPosition());
//                wallBean.endPointList.add(cellPolygonList.get(i).getWorldPosition());
//            }
//
//            wallBeanList.add(wallBean);
//        }

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

        float length = MathTool.getLengthByUnit(ARBuilder.instance.arUnit, difference.length());

        if(tempTextNode != null) {
            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + MathTool.getLengthUnitString(ARBuilder.instance.arUnit));
        }
        else {

            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + MathTool.getLengthUnitString(ARBuilder.instance.arUnit));

            tempTextNode = new FaceToCameraNode();
            tempTextNode.setParent(tempLineNode);

            tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
            tempTextNode.setLocalPosition(new Vector3(0f, 0.08f, 0f));
            tempTextNode.setRenderable(viewRenderableSizeText);

        }
    }

    @Override
    public void onBackPressed() {

        if(closeMeasureRoom()) {
            return;
        }

        if(closeDetectFloorPopup()) {
            return;
        }

        if(closeMeasureHeightPopup()) {
            return;
        }

        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AREnvironment.instance.resume(this, arSceneView, this::finish, () -> textViewHint.setVisibility(View.GONE));
    }


    @Override
    public void onPause() {
        super.onPause();
        AREnvironment.instance.pause(arSceneView);
    }

    private void removeESS() {
        EventCenter.instance.removeAllObserver(this);
    }

    @Override
    public void onDestroy() {
        AREnvironment.instance.destroy(arSceneView);
        ARBuilder.instance.destroy();
        removeESS();
        super.onDestroy();
    }
}