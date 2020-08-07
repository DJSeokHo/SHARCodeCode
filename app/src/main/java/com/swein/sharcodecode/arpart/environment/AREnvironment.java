package com.swein.sharcodecode.arpart.environment;

import android.app.Activity;

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
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.FaceToCameraNode;
import com.swein.sharcodecode.arpart.bean.object.WallObjectBean;
import com.swein.sharcodecode.arpart.builder.ARBuilder;
import com.swein.sharcodecode.arpart.builder.material.ARMaterial;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARConstants;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.device.DeviceUtil;
import com.swein.sharcodecode.framework.util.toast.ToastUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AREnvironment {

    public interface AREnvironmentDelegate {
        void onUpdatePlaneType(String type);
        void onDetectingTargetMinimumPlaneAreaSize(int percentage);
        void onMeasureHeight(float height);

        // ar builder
        void onCalculate(float height, float area, float circumference, float wallArea, float volume);
        void backToMeasureHeight();
    }

    public interface AREnvironmentShowHintDelegate {
        void onDetectTargetMinimumPlaneAreaSizeFinished();
        void showDetectFloorHint();
        void showMeasureHeightSelectPopup();
        void showSelectWallObjectPopup();
    }

    private final static String TAG = "AREnvironment";

    public static AREnvironment instance = new AREnvironment();

    private AREnvironment() {}

    private Activity activity;

    public Config.PlaneFindingMode planeFindingMode;

    public boolean isHitCeiling = false;

    public float hitPointX;
    public float hitPointY;

    // 감지 필요한 최소 cloud point 면적
    public int targetMinimumAreaSize = 0;


    private AREnvironmentDelegate arEnvironmentDelegate;
    private AREnvironmentShowHintDelegate arEnvironmentShowHintDelegate;

    public void init(Activity activity, AREnvironmentDelegate arEnvironmentDelegate, AREnvironmentShowHintDelegate arEnvironmentShowHintDelegate) {
        this.activity = activity;
        this.arEnvironmentDelegate = arEnvironmentDelegate;
        this.arEnvironmentShowHintDelegate = arEnvironmentShowHintDelegate;
        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL;

        initARBuilder();

        // 감지 필요한 최소 cloud point 면적
        targetMinimumAreaSize = 3;

        hitPointX = DeviceUtil.getScreenCenterX(activity);
        hitPointY = DeviceUtil.getScreenCenterY(activity);

        arEnvironmentShowHintDelegate.showDetectFloorHint();

        ARConstants.arProcess = ARConstants.ARProcess.DETECT_PLANE;
        ARConstants.measureHeightWay = ARConstants.MeasureHeightWay.NONE;
    }

    private void initARBuilder() {
        ARBuilder.instance.init(activity, new ARBuilder.ARBuilderDelegate() {
            @Override
            public void onCalculate(float height, float area, float circumference, float wallArea, float volume) {
                arEnvironmentDelegate.onCalculate(height, area, circumference, wallArea, volume);
            }

            @Override
            public void backToMeasureHeight() {

                ARConstants.measureHeightWay = ARConstants.MeasureHeightWay.NONE;
                ARConstants.arProcess = ARConstants.ARProcess.MEASURE_HEIGHT_HINT;

                arEnvironmentDelegate.backToMeasureHeight();
            }

            @Override
            public void backToSelectWallObject() {
                ARConstants.wallObjectType = ARConstants.WallObjectType.NONE;
                arEnvironmentShowHintDelegate.showSelectWallObjectPopup();
            }
        });
    }

    public boolean checkCameraEnable(Frame frame) {
        return frame.getCamera().getTrackingState() == TrackingState.TRACKING;
    }

    public boolean checkFrameEnable(Frame frame) {
        return frame != null;
    }

    public void checkUpdatedPlaneSize(Collection<Plane> planeCollection) {

        if(ARConstants.arProcess != ARConstants.ARProcess.DETECT_PLANE) {
            return;
        }

        for (Plane plane : planeCollection) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {

                // detecting size real-time, update UI
                int percentage = (int) (((plane.getExtentX() * plane.getExtentZ() * 2) / targetMinimumAreaSize) * 100);
                arEnvironmentDelegate.onDetectingTargetMinimumPlaneAreaSize(percentage);

                if((plane.getExtentX() * plane.getExtentZ() * 2) > targetMinimumAreaSize) {

                    arEnvironmentShowHintDelegate.onDetectTargetMinimumPlaneAreaSizeFinished();

                    // detect size finished, update UI
                    ARConstants.arProcess = ARConstants.ARProcess.MEASURE_HEIGHT_HINT;
                }

            }
        }
    }

    public void updateCloudPoint(ArSceneView arSceneView) {

        Frame frame = arSceneView.getArFrame();
        if (frame == null) {
            return;
        }

        // update plan cloud point area
        ARTool.updatePlanRenderer(arSceneView.getPlaneRenderer());
    }

    public void updatePlaneType(ArSceneView arSceneView) {

        Frame frame = arSceneView.getArFrame();
        if (frame == null) {
            return;
        }

        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);

        // check plan type
        String planType = ARTool.checkPlanType(
                hitTestResultList, "",
                arSceneView.getContext().getString(R.string.ar_plane_type_wall),
                arSceneView.getContext().getString(R.string.ar_plane_type_ceiling),
                arSceneView.getContext().getString(R.string.ar_plane_type_floor));

        isHitCeiling = planType.equals(arSceneView.getContext().getString(R.string.ar_plane_type_ceiling));

        arEnvironmentDelegate.onUpdatePlaneType(planType);
    }

    public void setInputHeight(float height) {
        ARBuilder.instance.height = height;
    }

    public void onTouch(ArSceneView arSceneView) {

        Frame frame = arSceneView.getArFrame();

        if(!AREnvironment.instance.checkCameraEnable(frame)) {
            // if camera disable
            return;
        }

        if(!AREnvironment.instance.checkFrameEnable(frame)) {
            // if frame disable
            return;
        }


        if(ARConstants.arProcess == ARConstants.ARProcess.DETECT_PLANE) {
            return;
        }

        if(ARConstants.arProcess == ARConstants.ARProcess.SELECTED_WALL_OBJECT) {
            arEnvironmentShowHintDelegate.showSelectWallObjectPopup();
            return;
        }

        if(ARConstants.arProcess == ARConstants.ARProcess.MEASURE_HEIGHT_HINT) {
            arEnvironmentShowHintDelegate.showMeasureHeightSelectPopup();
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.MEASURE_HEIGHT) {

            switch (ARConstants.measureHeightWay) {
                case AUTO:
                    measureHeightAutoWhenTouch(arSceneView);
                    break;

                case DRAW:
                    measureHeightDrawWhenTouch(arSceneView);
                    break;
            }
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.MEASURE_ROOM) {

            List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
            for (HitResult hitResult : hitTestResultList) {
                Trackable trackable = hitResult.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                    // if ready to auto close
                    if(ARBuilder.instance.isReadyToAutoClose) {

                        ARBuilder.instance.clearGuideSegment();
                        ARBuilder.instance.clearGuide();

//                        ARConstants.arProcess = ARConstants.ARProcess.DRAW_WALL_OBJECT;
                        ARConstants.arProcess = ARConstants.ARProcess.SELECTED_WALL_OBJECT;


                        ARBuilder.instance.autoCloseFloorSegment(activity);
                        ARBuilder.instance.createRoom(activity);

                        arEnvironmentShowHintDelegate.showSelectWallObjectPopup();
                        return;
                    }

                    // calculate normal vector of detected plane
                    ARBuilder.instance.createDetectedPlaneNormalVector(trackable);

                    // create first anchorNode
                    ARBuilder.instance.createAnchorNode(hitResult, arSceneView);

                    // two vector is too near
                    if(ARBuilder.instance.isNodesTooNearClosed()) {
                        return;
                    }

                    // create guide floor node
                    ARBuilder.instance.createGuideFloorNode(hitResult, activity);

                    if(ARBuilder.instance.floorGuideList.size() >= 2) {
                        ARBuilder.instance.drawSegment(activity,
                                ARBuilder.instance.floorGuideList.get(ARBuilder.instance.floorGuideList.size() - 2),
                                ARBuilder.instance.floorGuideList.get(ARBuilder.instance.floorGuideList.size() - 1)
                        );
                    }

                    ARBuilder.instance.clearGuideSegment();

                }
            }
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.DRAW_WALL_OBJECT) {
            drawWallObjectWhenTouch();
        }
    }

    public void onUpdateFrame(ArSceneView arSceneView) {

        Frame frame = arSceneView.getArFrame();


        /* =============== real-time part =============== */
        if(!AREnvironment.instance.checkCameraEnable(frame)) {
            // if camera disable
            return;
        }

        if(!AREnvironment.instance.checkFrameEnable(frame)) {
            // if frame disable
            return;
        }

        // update cloud point
        AREnvironment.instance.updateCloudPoint(arSceneView);

        // if plan size big than target minimum area size
        // then hide finding plan view
        checkUpdatedPlaneSize(frame.getUpdatedTrackables(Plane.class));

        // update plane type real-time
        AREnvironment.instance.updatePlaneType(arSceneView);
        /* =============== real-time part =============== */


        if(ARConstants.arProcess == ARConstants.ARProcess.MEASURE_HEIGHT) {

            switch (ARConstants.measureHeightWay) {
                case AUTO:
                    measureHeightAutoWhenUpdate(arSceneView);
                    break;

                case DRAW:
                    measureHeightDrawWhenUpdate(arSceneView);
                    break;
            }
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.MEASURE_ROOM) {

            List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
            for (HitResult hitResult : hitTestResultList) {
                Trackable trackable = hitResult.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                    ARBuilder.instance.checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                    ARBuilder.instance.showGuidePoint(hitResult, arSceneView, true);

                    if(ARBuilder.instance.checkFloorGuideEmpty()) {
                        return;
                    }

                    ARBuilder.instance.drawFloorGuideSegment(
                            ARBuilder.instance.floorGuideList.get(ARBuilder.instance.floorGuideList.size() - 1),
                            ARBuilder.instance.guidePointNode);

                    ARBuilder.instance.checkPolygonAutoClose(activity);

                    return;
                }
            }
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.DRAW_WALL_OBJECT) {
            drawWallObjectWhenUpdate(arSceneView);
        }
    }

    private void drawWallObjectWhenTouch() {

        // make node on wall
        if(ARBuilder.instance.wallTempPoint == null) {
            // create wall object start point

            ARBuilder.instance.currentWallIndex = ARBuilder.instance.currentGuideIndex;

            Vector3 vector3World = new Vector3(
                    ARBuilder.instance.wallGuidePoint.getWorldPosition().x,
                    ARBuilder.instance.wallGuidePoint.getWorldPosition().y,
                    ARBuilder.instance.wallGuidePoint.getWorldPosition().z
            );
            Vector3 vector3Local = MathTool.transformWorldPositionToLocalPositionOfParent(ARBuilder.instance.anchorNode, vector3World);


            ARBuilder.instance.wallTempPoint = ARTool.createLocalNode(
                    vector3Local.x,
                    vector3Local.y,
                    vector3Local.z,
                    ARMaterial.instance.wallPointMaterial, true);

            ARBuilder.instance.wallTempPoint.setParent(ARBuilder.instance.anchorNode);


            if(ARBuilder.instance.wallObjectBeanList.isEmpty()) {
                ARBuilder.instance.wallObjectBeanList.add(new WallObjectBean());

                WallObjectBean wallObjectBean = ARBuilder.instance.wallObjectBeanList.get(0);

                // add point
                wallObjectBean.objectPointList.add(ARBuilder.instance.wallTempPoint);

                Vector3 horizontalVector3 = new Vector3(
                        ARBuilder.instance.wallGuidePoint.getWorldPosition().x,
                        ARBuilder.instance.wallGuidePoint.getWorldPosition().y,
                        ARBuilder.instance.wallGuidePoint.getWorldPosition().z
                );

                Vector3 horizontalLocalPosition = MathTool.transformWorldPositionToLocalPositionOfParent(ARBuilder.instance.anchorNode, horizontalVector3);
                Node horizontalNode = ARTool.createLocalNode(horizontalLocalPosition.x, horizontalLocalPosition.y, horizontalLocalPosition.z,
                        ARMaterial.instance.wallPointMaterial, true);

                horizontalNode.setParent(ARBuilder.instance.anchorNode);
                wallObjectBean.objectPointList.add(horizontalNode);

                wallObjectBean.objectPointList.add(ARBuilder.instance.wallGuidePoint);

                Vector3 verticalVector3 = new Vector3();
                verticalVector3.x = ARBuilder.instance.wallTempPoint.getWorldPosition().x;
                verticalVector3.y = ARBuilder.instance.wallGuidePoint.getWorldPosition().y;
                verticalVector3.z = ARBuilder.instance.wallTempPoint.getWorldPosition().z;
                Node verticalNode = ARTool.createLocalNode(verticalVector3.x, verticalVector3.y, verticalVector3.z, ARMaterial.instance.wallPointMaterial, true);
                verticalNode.setParent(ARBuilder.instance.anchorNode);
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
                            .setView(activity, R.layout.view_renderable_text)
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

            ARBuilder.instance.clearGuideSegment();
            // create wall object end point
            ARBuilder.instance.wallTempPoint.setParent(null);
            ARBuilder.instance.wallTempPoint = null;

            ARBuilder.instance.currentWallIndex = -1;
            ARBuilder.instance.currentGuideIndex = -1;

            for(int i = 0; i < ARBuilder.instance.wallObjectBeanList.size(); i++) {
                WallObjectBean wallObjectBean = ARBuilder.instance.wallObjectBeanList.get(i);

                for(Node node : wallObjectBean.objectPointList) {
                    ARTool.removeChildFormNode(node);
                    node.setParent(null);
                }
                wallObjectBean.objectPointList.clear();
                wallObjectBean.objectTextList.clear();
                wallObjectBean.objectLineList.clear();
                wallObjectBean.viewRenderableList.clear();
            }

            ARBuilder.instance.wallObjectBeanList.clear();
        }
    }

    private void drawWallObjectWhenUpdate(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
        if(frame == null) {
            return;
        }
        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);


        for (HitResult hitResult : hitTestResultList) {

            // draw center point
            if(ARBuilder.instance.guidePointNode != null) {
                ARBuilder.instance.guidePointNode.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
            }
            else {
                ARBuilder.instance.guidePointNode = ARTool.createWorldNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(),
                        ARMaterial.instance.pointMaterial, true);
                ARBuilder.instance.guidePointNode.setParent(arSceneView.getScene());
            }
            // draw center point

            List<Vector3> result = new ArrayList<>();
            List<Integer> indexList = ARBuilder.instance.getThoughWall(arSceneView, result, hitResult);

            if(result.isEmpty()) {

                if(ARBuilder.instance.wallGuidePoint != null) {
                    ARBuilder.instance.wallGuidePoint.setParent(null);
                }
                ARBuilder.instance.currentGuideIndex = -1;
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

            for(int i = 0; i < distanceList.size(); i++) {
                if(distance == distanceList.get(i)) {
                    resultIndex = i;
                    break;
                }
            }

            ARBuilder.instance.currentGuideIndex = indexList.get(resultIndex);
            ILog.iLogDebug(TAG, String.valueOf(ARBuilder.instance.currentGuideIndex));

            if(ARBuilder.instance.wallGuidePoint != null) {
                ARBuilder.instance.wallGuidePoint.setWorldPosition(new Vector3(result.get(resultIndex).x, result.get(resultIndex).y, result.get(resultIndex).z));
            }
            else {
                ARBuilder.instance.wallGuidePoint = ARTool.createWorldNode(result.get(resultIndex).x, result.get(resultIndex).y, result.get(resultIndex).z,
                        ARMaterial.instance.wallPointMaterial, true);
            }
            ARBuilder.instance.wallGuidePoint.setParent(arSceneView.getScene());


            if(ARBuilder.instance.wallTempPoint != null) {

                if(ARBuilder.instance.currentGuideIndex == ARBuilder.instance.currentWallIndex) {

                    if(ARBuilder.instance.wallObjectBeanList.isEmpty()) {
                        return;
                    }

                    WallObjectBean wallObjectBean = ARBuilder.instance.wallObjectBeanList.get(0);


                    Vector3 horizontalVector3 = new Vector3();
                    horizontalVector3.x = ARBuilder.instance.wallGuidePoint.getWorldPosition().x;
                    horizontalVector3.y = ARBuilder.instance.wallTempPoint.getWorldPosition().y;
                    horizontalVector3.z = ARBuilder.instance.wallGuidePoint.getWorldPosition().z;

                    Vector3 horizontalLocalPosition = MathTool.transformWorldPositionToLocalPositionOfParent(ARBuilder.instance.anchorNode, horizontalVector3);
                    Vector3 verticalVector3 = new Vector3();
                    verticalVector3.x = ARBuilder.instance.wallTempPoint.getWorldPosition().x;
                    verticalVector3.y = ARBuilder.instance.wallGuidePoint.getWorldPosition().y;
                    verticalVector3.z = ARBuilder.instance.wallTempPoint.getWorldPosition().z;
                    Vector3 verticalLocalPosition = MathTool.transformWorldPositionToLocalPositionOfParent(ARBuilder.instance.anchorNode, verticalVector3);

                    // draw wall temp line
                    if(wallObjectBean.viewRenderableList.size() < 4) {
                        return;
                    }

                    wallObjectBean.objectPointList.get(1).setLocalPosition(horizontalLocalPosition);
                    wallObjectBean.objectPointList.get(3).setLocalPosition(verticalLocalPosition);

                    ARBuilder.instance.drawTempWallLine(wallObjectBean.objectPointList.get(0), wallObjectBean.objectPointList.get(1),
                            wallObjectBean.objectLineList.get(0), wallObjectBean.objectTextList.get(0), wallObjectBean.viewRenderableList.get(0));

                    ARBuilder.instance.drawTempWallLine(wallObjectBean.objectPointList.get(1), wallObjectBean.objectPointList.get(2),
                            wallObjectBean.objectLineList.get(1), wallObjectBean.objectTextList.get(1), wallObjectBean.viewRenderableList.get(1));

                    ARBuilder.instance.drawTempWallLine(wallObjectBean.objectPointList.get(2), wallObjectBean.objectPointList.get(3),
                            wallObjectBean.objectLineList.get(2), wallObjectBean.objectTextList.get(2), wallObjectBean.viewRenderableList.get(2));

                    ARBuilder.instance.drawTempWallLine(wallObjectBean.objectPointList.get(3), wallObjectBean.objectPointList.get(0),
                            wallObjectBean.objectLineList.get(3), wallObjectBean.objectTextList.get(3), wallObjectBean.viewRenderableList.get(3));
                }
                else {
                    ARBuilder.instance.wallGuidePoint.setWorldPosition(
                            new Vector3(
                                    ARBuilder.instance.wallTempPoint.getWorldPosition().x,
                                    ARBuilder.instance.wallTempPoint.getWorldPosition().y,
                                    ARBuilder.instance.wallTempPoint.getWorldPosition().z)
                    );
                }
            }
        }
    }

    private void measureHeightDrawWhenTouch(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
        if(frame == null) {
            return;
        }
        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                // calculate normal vector of detected plane
                ARBuilder.instance.createDetectedPlaneNormalVector(trackable);

                // create first anchorNode
                ARBuilder.instance.createAnchorNode(hitResult, arSceneView);

                // two vector is too near
                if(ARBuilder.instance.isNodesTooNearClosed()) {
                    return;
                }

                // only one node can on floor
                if(ARBuilder.instance.measureHeightFloorNode != null && ARBuilder.instance.measureHeightCeilingNode == null) {
                    if(!isHitCeiling) {
                        return;
                    }
                }

                ARBuilder.instance.createMeasureHeightDrawNode(hitResult, activity);

                if(ARBuilder.instance.measureHeightFloorNode != null && ARBuilder.instance.measureHeightCeilingNode != null) {
                    // finish measure height

                    if(isHitCeiling) {

                        // get distance between floor and ceiling
                        Vector3 floorPoint = new Vector3(
                                ARBuilder.instance.measureHeightFloorNode.getWorldPosition().x,
                                ARBuilder.instance.measureHeightFloorNode.getWorldPosition().y,
                                ARBuilder.instance.measureHeightFloorNode.getWorldPosition().z
                        );
                        Vector3 ceilingPoint = new Vector3(
                                ARBuilder.instance.measureHeightCeilingNode.getWorldPosition().x,
                                ARBuilder.instance.measureHeightCeilingNode.getWorldPosition().y,
                                ARBuilder.instance.measureHeightCeilingNode.getWorldPosition().z
                        );
                        float height = MathTool.getLengthBetweenPointToPlane(ceilingPoint, floorPoint, ARBuilder.instance.normalVectorOfPlane);

                        ARBuilder.instance.height = height;

                        // clear node when measure height finished
                        ARBuilder.instance.clearAnchor();
                        ARBuilder.instance.clearMeasureHeightFloorNode();
                        ARBuilder.instance.clearMeasureHeightCeilingNode();

                        // to next process - measure room
                        ARConstants.arProcess = ARConstants.ARProcess.MEASURE_ROOM;

                        // update UI
                        arEnvironmentDelegate.onMeasureHeight(height);
                    }

                    return;
                }

                ARBuilder.instance.clearGuideSegment();
            }
        }
    }

    private void measureHeightDrawWhenUpdate(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
        if(frame == null) {
            return;
        }
        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                ARBuilder.instance.checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                ARBuilder.instance.showGuidePoint(hitResult, arSceneView, false);

                if (ARBuilder.instance.measureHeightFloorNode != null && ARBuilder.instance.measureHeightCeilingNode != null) {
                    return;
                }

                if (ARBuilder.instance.measureHeightFloorNode == null && ARBuilder.instance.measureHeightCeilingNode == null) {
                    return;
                }

                if (ARBuilder.instance.measureHeightFloorNode != null) {
                    ARBuilder.instance.drawFloorGuideSegment(ARBuilder.instance.measureHeightFloorNode, ARBuilder.instance.guidePointNode);
                }

                return;
            }
        }
    }

    private void measureHeightAutoWhenTouch(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
        if(frame == null) {
            return;
        }
        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                // calculate normal vector of detected plane
                ARBuilder.instance.createDetectedPlaneNormalVector(trackable);

                // can only one anchor node on floor
                if(ARBuilder.instance.anchorNode != null) {
                    return;
                }

                // create first anchorNode
                ARBuilder.instance.createAnchorNode(hitResult, arSceneView);

                // create temp node for measuring height
                ARBuilder.instance.createMeasureHeightAutoNode(activity);

            }
        }
    }

    private void measureHeightAutoWhenUpdate(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
        if(frame == null) {
            return;
        }

        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                ARBuilder.instance.checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                ARBuilder.instance.showGuidePoint(hitResult, arSceneView, false);

                if(isHitCeiling) {
                    // get distance ceiling

                    Vector3 floorPoint = new Vector3(
                            ARBuilder.instance.anchorNode.getWorldPosition().x,
                            ARBuilder.instance.anchorNode.getWorldPosition().y,
                            ARBuilder.instance.anchorNode.getWorldPosition().z
                    );
                    Vector3 ceiling = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());

                    float height = MathTool.getLengthBetweenPointToPlane(ceiling, floorPoint, ARBuilder.instance.normalVectorOfPlane);

                    ARBuilder.instance.height = height;

                    // clear node when measure height finished
                    ARBuilder.instance.clearAnchor();
                    ARBuilder.instance.clearMeasureHeightFloorNode();
                    ARBuilder.instance.clearMeasureHeightCeilingNode();

                    // to next process - measure room
                    ARConstants.arProcess = ARConstants.ARProcess.MEASURE_ROOM;

                    // update UI
                    arEnvironmentDelegate.onMeasureHeight(height);

                    return;
                }

                return;
            }
        }
    }


    public void reset(Activity activity, ArSceneView arSceneView, Runnable finishActivity, Runnable findingPlane) {

        if (arSceneView == null) {
            return;
        }

        if(arSceneView.getArFrame() != null) {
            arSceneView.getArFrame().acquirePointCloud().release();
        }

        arSceneView.pause();
        arSceneView.setupSession(null);

        try {
//                Config.LightEstimationMode lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR;
//                Session session = createArSession(this, true, lightEstimationMode);
            Session session = createArSession(activity, true, null, planeFindingMode);

            if (session == null) {
                finishActivity.run();
            }
            else {
                arSceneView.setupSession(session);
            }
        }
        catch (UnavailableException e) {
            handleSessionException(activity, e);
        }

        try {
            arSceneView.resume();
        }
        catch (CameraNotAvailableException ex) {
            ToastUtil.showCustomShortToastNormal(activity, "Unable to get camera");
            finishActivity.run();
            return;
        }

        if (arSceneView.getSession() != null) {
            // loading...finding plane
            findingPlane.run();
        }

        ARBuilder.instance.reset();
    }

    public void resume(Activity activity, ArSceneView arSceneView, Runnable finishActivity, Runnable findingPlane) {

        if (arSceneView == null) {
            return;
        }

        if (arSceneView.getSession() == null) {

            try {
//                Config.LightEstimationMode lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR;
//                Session session = createArSession(this, true, lightEstimationMode);
                Session session = createArSession(activity, true, null, AREnvironment.instance.planeFindingMode);

                if (session == null) {
                    finishActivity.run();
                }
                else {
                    arSceneView.setupSession(session);
                }
            }
            catch (UnavailableException e) {
                handleSessionException(activity, e);
            }
        }

        try {
            arSceneView.resume();
        }
        catch (CameraNotAvailableException ex) {
            ToastUtil.showCustomShortToastNormal(activity, "Unable to get camera");
            finishActivity.run();
            return;
        }

        if (arSceneView.getSession() != null) {
            // loading...finding plane
            findingPlane.run();
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

    private static void handleSessionException(
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

    public void back() {
        ARBuilder.instance.back();
    }

    public void pause(ArSceneView arSceneView) {
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    public void destroy(ArSceneView arSceneView) {
        if (arSceneView != null) {
            arSceneView.destroy();
        }

        ARConstants.arProcess = ARConstants.ARProcess.DETECT_PLANE;
        ARConstants.arUnit = ARConstants.ARUnit.M;
        ARConstants.measureHeightWay = ARConstants.MeasureHeightWay.NONE;

        ARBuilder.instance.destroy();
        targetMinimumAreaSize = 0;

    }

}
