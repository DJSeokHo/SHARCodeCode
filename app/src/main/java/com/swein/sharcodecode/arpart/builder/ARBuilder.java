package com.swein.sharcodecode.arpart.builder;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.Trackable;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.FaceToCameraNode;
import com.swein.sharcodecode.arpart.bean.RoomBean;
import com.swein.sharcodecode.arpart.bean.basic.PointBean;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;
import com.swein.sharcodecode.arpart.constants.ARESSArrows;
import com.swein.sharcodecode.framework.util.ar.ARUtil;
import com.swein.sharcodecode.framework.util.device.DeviceUtil;
import com.swein.sharcodecode.framework.util.eventsplitshot.eventcenter.EventCenter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ARBuilder {

    public interface ARBuilderDelegate {
        void onCalculate(float height, float area, float circumference, float wallArea, float volume);
    }

    private final static String TAG = "ARBuilder";

    public enum ARUnit {
        M, CM
    }

    public enum ARProcess {
        READY, MEASURE_HEIGHT, MEASURE_ROOM, DRAW_WALL_OBJECT
    }

    private static ARBuilder instance = new ARBuilder();
    public static ARBuilder getInstance() {
        return instance;
    }

    private ARBuilder() {}

    // check floor build
    public boolean isReadyToAutoClose;
    public boolean isAutoClosed;

    // node shadow
    public boolean nodeShadow = true;

    // guide line
    public Node guideSegmentNode;

    // point of screen center
    public Node guidePointNode;

    public Node guideSizeTextNode;
    public ViewRenderable guideSizeTextView;

    // build process state
    public ARProcess arProcess = ARProcess.READY;

    // current unit
    public ARUnit arUnit = ARUnit.M;

    // 감지 필요한 최소 cloud point 면적
    public int targetMinimumAreaSize = 0;

    // material
    public Material pointMaterial;
    public Material segmentMaterial;

    public Material objectPointMaterial;
    public Material objectSegmentMaterial;
    public Material objectGuideNodeMaterial;

    public Material guideNodeMaterial;
    public Material guideSegmentMaterial;

    public List<Node> floorGuideList;
    public float floorFixedY;

    public float height = 1;

    // anchor of room
    public AnchorNode anchorNode;

    public Vector3 normalVectorOfPlane;

    private RoomBean roomBean;

    private ARBuilderDelegate arBuilderDelegate;

    public void init(Context context, ARBuilderDelegate arBuilderDelegate) {
        this.arBuilderDelegate = arBuilderDelegate;

        // 감지 필요한 최소 cloud point 면적
        targetMinimumAreaSize = 3;

        // create node material
        ARTool.createColorMaterial(context, Color.GREEN, material -> {
            pointMaterial = material;
            segmentMaterial = material;

            guideNodeMaterial = material;
            guideSegmentMaterial = material;
        });

        // create object node material
        ARTool.createColorMaterial(context, Color.BLUE, material -> {
            objectPointMaterial = material;
            objectSegmentMaterial = material;
            objectGuideNodeMaterial = material;
        });

        ARTool.createViewRenderable(context, R.layout.view_renderable_text, viewRenderable -> {
            guideSizeTextView = viewRenderable;
        }, false);

        floorGuideList = new ArrayList<>();
    }

    public void checkPlaneSize(Collection<Plane> planeCollection) {
        for (Plane plane : planeCollection) {
            if (plane.getTrackingState() == TrackingState.TRACKING) {
                if((plane.getExtentX() * plane.getExtentZ() * 2) > targetMinimumAreaSize) {
                    EventCenter.getInstance().sendEvent(ARESSArrows.DETECTED_TARGET_MINIMUM_AREA_SIZE_FINISHED, this, null);
                }
            }
        }
    }

    public void checkDistanceBetweenCameraAndPlane(float distance) {
        if(distance < 0.5) {
            EventCenter.getInstance().sendEvent(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_TOO_CLOSE, this, null);
        }
        else if(distance > 10) {
            EventCenter.getInstance().sendEvent(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_TOO_FAR, this, null);
        }
        else {
            EventCenter.getInstance().sendEvent(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_OK, this, null);
        }
    }

    public void showGuidePoint(HitResult hitResult, ArSceneView arSceneView) {

        floorFixedY = hitResult.getHitPose().ty();

        if(anchorNode != null) {
            floorFixedY = anchorNode.getWorldPosition().y;
        }

        if(guidePointNode != null) {
            guidePointNode.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), floorFixedY, hitResult.getHitPose().tz()));
        }
        else {
            guidePointNode = ARTool.createWorldNode(hitResult.getHitPose().tx(), floorFixedY, hitResult.getHitPose().tz(), pointMaterial, nodeShadow);
            guidePointNode.setParent(arSceneView.getScene());
        }
    }

    public void createDetectedPlaneNormalVector(Trackable trackable) {
        if(normalVectorOfPlane == null) {
            // calculate normal vector of detected plane
            normalVectorOfPlane = ARTool.getNormalVectorOfThreeVectors(
                    new Vector3(((Plane) trackable).getCenterPose().tx(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz()),
                    new Vector3(((Plane) trackable).getCenterPose().tx() + ((Plane) trackable).getExtentX(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz()),
                    new Vector3(((Plane) trackable).getCenterPose().tx(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz() + ((Plane) trackable).getExtentZ())
            );
//                    ILog.iLogDebug(TAG, "normalVectorOfPlane by cal " + normalVectorOfPlane.x + " " + normalVectorOfPlane.y + " " + normalVectorOfPlane.z);
        }
    }

    public void createAnchorNode(HitResult hitResult, ArSceneView arSceneView) {

        if(anchorNode == null) {
            Anchor anchor = hitResult.createAnchor();
            ARBuilder.getInstance().anchorNode = ARTool.createAnchorNode(anchor);
            ARBuilder.getInstance().anchorNode.setParent(arSceneView.getScene());
        }
    }

    public boolean isNodesTooNearClosed() {
        if(floorGuideList.size() > 1) {
            return Vector3.subtract(floorGuideList.get(floorGuideList.size() - 2).getLocalPosition(),
                    floorGuideList.get(floorGuideList.size() - 1).getLocalPosition()).length() < 0.05;
        }
        return false;
    }

    public void createGuideFloorNode(HitResult hitResult, Activity activity) {
        Node node;
        if(floorGuideList.isEmpty()) {
            node = ARUtil.createLocalNode(0, 0, 0, pointMaterial, nodeShadow);
        }
        else {
//                                Quaternion localRotation = new Quaternion(hitResult.getHitPose().qx(), hitResult.getHitPose().qy(), hitResult.getHitPose().qz(), hitResult.getHitPose().qw());
//                                localRotation = Quaternion.multiply(this.anchorNode.getWorldRotation().inverted(), Preconditions.checkNotNull(localRotation));
            Vector3 hitWorldPosition = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());
            Vector3 localPosition = ARTool.transformWorldPositionToLocalPositionOfParent(anchorNode, hitWorldPosition);

            node = ARUtil.createLocalNode(localPosition.x, localPosition.y, localPosition.z, pointMaterial, nodeShadow);
        }

        node.setParent(anchorNode);
        floorGuideList.add(node);

        DeviceUtil.vibrate(activity, 5);

        if(anchorNode != null) {
            floorFixedY = anchorNode.getWorldPosition().y;
        }
    }

    public void drawFloorGuideSegment(Node startNode, Node endNode) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);
        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), segmentMaterial);
        lineMode.setShadowCaster(nodeShadow);
        lineMode.setShadowReceiver(nodeShadow);

        if(guideSegmentNode == null) {
            guideSegmentNode = new Node();
            guideSegmentNode.setParent(startNode);
        }

        guideSegmentNode.setRenderable(lineMode);
        guideSegmentNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
        guideSegmentNode.setWorldRotation(rotationFromAToB);

        float length = ARTool.getLengthByUnit(arUnit, difference.length());

        if(guideSizeTextNode != null) {

            ((TextView) guideSizeTextView.getView()).setText(String.format("%.2f", length) + ARTool.getLengthUnitString(arUnit));

        }
        else {

            ((TextView) guideSizeTextView.getView()).setText(String.format("%.2f", length) + ARTool.getLengthUnitString(arUnit));

            guideSizeTextNode = new FaceToCameraNode();

            if(guideSegmentNode != null) {
                guideSizeTextNode.setParent(guideSegmentNode);
            }

            guideSizeTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
            guideSizeTextNode.setLocalPosition(new Vector3(0f, 0.08f, 0f));
            guideSizeTextNode.setRenderable(guideSizeTextView);

        }
    }

    public void drawSegment(Context context, Node startNode, Node endNode) {

        Node lineNode = ARTool.drawSegment(startNode, endNode, segmentMaterial, nodeShadow);

        float length = ARTool.getLengthOfTwoNode(startNode, endNode);
        ARTool.setSegmentSizeTextView(context, length, arUnit, lineNode, (viewRenderable, faceToCameraNode) -> {

        });
    }

    public void autoCloseFloorSegment(Activity activity) {

        if(floorGuideList.size() > 2) {

            Node startNode = ARBuilder.getInstance().floorGuideList.get(ARBuilder.getInstance().floorGuideList.size() - 1);
            Node endNode = ARBuilder.getInstance().floorGuideList.get(0);

            Node lineNode = ARTool.drawSegment(startNode, endNode, segmentMaterial, nodeShadow);

            float length = ARTool.getLengthOfTwoNode(startNode, endNode);
            ARTool.setSegmentSizeTextView(activity, length, arUnit, lineNode, (viewRenderable, faceToCameraNode) -> {

            });

            isAutoClosed = true;

            DeviceUtil.vibrate(activity, 5);
        }
    }

    public void checkPolygonAutoClose(Activity activity) {

        // at least need 3 point
        if(floorGuideList.size() < 3) {
            return;
        }

        if(checkClose(guidePointNode, floorGuideList.get(0))) {
            drawFloorGuideSegment(floorGuideList.get(floorGuideList.size() - 1), floorGuideList.get(0));

            if(!isReadyToAutoClose) {
                DeviceUtil.vibrate(activity, 5);
            }

            isReadyToAutoClose = true;
        }
        else {
            drawFloorGuideSegment(floorGuideList.get(floorGuideList.size() - 1), guidePointNode);
            isReadyToAutoClose = false;
        }
    }

    private boolean checkClose(Node startNode, Node endNode) {
        return ARTool.getNodesDistanceMetersWithoutHeight(startNode, endNode) < 0.06;
    }

    /**
     * check room's floor is empty
     */
    public boolean checkFloorGuideEmpty() {
        return floorGuideList.isEmpty();
    }


    public void createRoom(Context context) {

        if(roomBean != null) {
            roomBean = null;
        }

        roomBean = new RoomBean();

        // create floor
        roomBean.height = height;
        roomBean.floorFixedY = floorFixedY;
        roomBean.normalVectorOfPlane = normalVectorOfPlane;

        PointBean pointBean;
        for(int i = 0; i < floorGuideList.size(); i++) {
            pointBean = new PointBean();
            pointBean.point = ARTool.createLocalNode(
                    floorGuideList.get(i).getLocalPosition().x,
                    floorGuideList.get(i).getLocalPosition().y,
                    floorGuideList.get(i).getLocalPosition().z,
                    pointMaterial, nodeShadow
            );

            pointBean.point.setParent(anchorNode);
            roomBean.floor.pointList.add(pointBean);
        }

        // create ceiling
        for(int i = 0; i < floorGuideList.size(); i++) {
            pointBean = new PointBean();
            pointBean.point = ARTool.createLocalNode(
                    floorGuideList.get(i).getLocalPosition().x,
                    floorGuideList.get(i).getLocalPosition().y + height,
                    floorGuideList.get(i).getLocalPosition().z,
                    pointMaterial, nodeShadow
            );

            pointBean.point.setParent(anchorNode);
            roomBean.ceiling.pointList.add(pointBean);
        }

        // connect floor and ceiling
        for(int i = 0; i < roomBean.floor.pointList.size() - 1; i++) {
            drawSegment(context, roomBean.floor.pointList.get(i).point, roomBean.floor.pointList.get(i + 1).point);
        }
        drawSegment(context, roomBean.floor.pointList.get(roomBean.floor.pointList.size() - 1).point, roomBean.floor.pointList.get(0).point);

        for(int i = 0; i < roomBean.ceiling.pointList.size() - 1; i++) {
            drawSegment(context, roomBean.ceiling.pointList.get(i).point, roomBean.ceiling.pointList.get(i + 1).point);
        }
        drawSegment(context, roomBean.ceiling.pointList.get(roomBean.ceiling.pointList.size() - 1).point, roomBean.ceiling.pointList.get(0).point);

        for(int i = 0; i < roomBean.floor.pointList.size(); i++) {
            drawSegment(context, roomBean.floor.pointList.get(i).point, roomBean.ceiling.pointList.get(i).point);
        }


        // create wall


        // calculate
        roomBean.calculate(arUnit);
        arBuilderDelegate.onCalculate(roomBean.height, roomBean.area, roomBean.circumference, roomBean.wallArea, roomBean.volume);

        // clear guide
        for(Node node : floorGuideList) {
            ARTool.removeChildFormNode(node);
            node.setParent(null);
        }
        floorGuideList.clear();

        clearGuide();
        clearTemp();
    }

    public void calculate() {

    }

    public void clearTemp() {

        if(guideSizeTextNode != null) {
            guideSizeTextNode.setParent(null);
            guideSizeTextNode = null;
        }

        if(guideSegmentNode != null) {
            guideSegmentNode.setParent(null);
            guideSegmentNode = null;
        }

    }

    public void back() {

        if(isAutoClosed) {

            if(anchorNode != null) {
                ARTool.removeChildFormNode(anchorNode);
                anchorNode.setParent(null);
                anchorNode = null;
            }

            floorGuideList.clear();

            // clear room bean
            if(roomBean != null) {
                roomBean.clear();
                roomBean = null;
            }

//            for(WallObjectBean wallObjectBean : wallObjectBeans) {
//                for(Node node : wallObjectBean.objectPointList) {
//                    node.setParent(null);
//                }
//            }
//            wallObjectBeans.clear();

            clearTemp();
            clearGuide();

            floorFixedY = 0;

//            if(wallGuidePoint != null) {
//                wallGuidePoint.setParent(null);
//                wallGuidePoint = null;
//            }
//
//            if(wallTempPoint != null) {
//                wallTempPoint.setParent(null);
//                wallTempPoint = null;
//            }
//
//            currentGuideIndex = -1;
//            currentWallIndex = -1;

//            linearLayout.setVisibility(View.GONE);
//            textViewHeight.setText("");
//            textViewArea.setText("");
//            textViewCircumference.setText("");
//            textViewWallArea.setText("");
//            textViewVolume.setText("");

        }
        else {

            if(floorGuideList.size() == 1) {

                if(anchorNode != null) {
                    ARTool.removeChildFormNode(anchorNode);
                    anchorNode = null;
                }

                floorGuideList.clear();
//                textViewSizeList.clear();
//                wallBeanList.clear();
//
//                for(WallObjectBean wallObjectBean : wallObjectBeans) {
//                    for(Node node : wallObjectBean.objectPointList) {
//                        node.setParent(null);
//                    }
//                }
//                wallObjectBeans.clear();

                clearTemp();
                clearGuide();

                floorFixedY = 0;

//                if(wallGuidePoint != null) {
//                    wallGuidePoint.setParent(null);
//                    wallGuidePoint = null;
//                }
//
//                if(wallTempPoint != null) {
//                    wallTempPoint.setParent(null);
//                    wallTempPoint = null;
//                }
//
//                currentGuideIndex = -1;
//                currentWallIndex = -1;
            }
            else if(floorGuideList.size() > 1) {

                ARTool.removeChildFormNode(floorGuideList.get(floorGuideList.size() - 2));
                floorGuideList.get(floorGuideList.size() - 1).setParent(null);
                floorGuideList.remove(floorGuideList.size() - 1);
//                textViewSizeList.remove(textViewSizeList.size() - 1);

                clearTemp();
                clearGuide();

            }
        }

        isAutoClosed = false;
        isReadyToAutoClose = false;
    }

    public void clearGuide() {
        if(guidePointNode != null) {
            guidePointNode.setParent(null);
            guidePointNode = null;
        }
    }

    public void clear() {

        targetMinimumAreaSize = 0;

        // temp guide line
        guideSegmentNode = null;

        // point of screen center
        guidePointNode = null;

        guideSizeTextNode = null;
        guideSizeTextView = null;

        // build process state
        arProcess = ARProcess.READY;

        // current unit
        arUnit = ARUnit.M;

        // material
        pointMaterial = null;
        segmentMaterial = null;

        objectPointMaterial = null;
        objectSegmentMaterial = null;
        objectGuideNodeMaterial = null;

        guideNodeMaterial = null;
        guideSegmentMaterial = null;

        anchorNode = null;

        floorGuideList.clear();
        floorGuideList = null;

        isReadyToAutoClose = false;
        isAutoClosed = false;
    }
}
