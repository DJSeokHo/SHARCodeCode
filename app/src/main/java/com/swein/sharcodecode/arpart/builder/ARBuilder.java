package com.swein.sharcodecode.arpart.builder;

import android.app.Activity;
import android.content.Context;
import android.widget.TextView;

import com.google.ar.core.Anchor;
import com.google.ar.core.HitResult;
import com.google.ar.core.InstantPlacementPoint;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Trackable;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.ShapeFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.arpart.FaceToCameraNode;
import com.swein.sharcodecode.arpart.bean.RoomBean;
import com.swein.sharcodecode.arpart.bean.basic.PlaneBean;
import com.swein.sharcodecode.arpart.bean.basic.PointBean;
import com.swein.sharcodecode.arpart.bean.object.WallObjectBean;
import com.swein.sharcodecode.arpart.builder.material.ARMaterial;
import com.swein.sharcodecode.arpart.builder.renderable.ARRenderable;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARConstants;
import com.swein.sharcodecode.arpart.constants.ARESSArrows;
import com.swein.sharcodecode.framework.util.device.DeviceUtil;
import com.swein.sharcodecode.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.sharcodecode.framework.util.thread.ThreadUtil;

import java.util.ArrayList;
import java.util.List;

public class ARBuilder {

    private final static String TAG = "ARBuilder";

    public static ARBuilder instance = new ARBuilder();
    private ARBuilder() {}


    public interface ARBuilderDelegate {
        void onCalculate(float height, float area, float circumference, float wallArea, float volume);
        void backToMeasureHeight();
        void backToSelectWallObject();
    }


    // check floor build
    public boolean isReadyToAutoClose;

    // guide line
    public Node guideSegmentNode;

    // point of screen center
    public Node guidePointNode;

    public Node guideSizeTextNode;

    // point of screen center of wall
    public Node wallGuidePoint;
    public Node wallTempPoint;
    public WallObjectBean wallObjectBeanTemp;



    public List<Node> floorGuideList = new ArrayList<>();
    public float floorFixedY = -999;

    public Node measureHeightFloorNode;
    public Node measureHeightCeilingNode;

    public float height = 0;

    // anchor of room
    public AnchorNode anchorNode;

    public Vector3 normalVectorOfPlane;

    public RoomBean roomBean;

    private ARBuilderDelegate arBuilderDelegate;


    public int currentWallIndex = -1;
    public int currentGuideIndex = -1;

    public void init(Context context, ARBuilderDelegate arBuilderDelegate) {
        this.arBuilderDelegate = arBuilderDelegate;

        floorGuideList = new ArrayList<>();

        ARMaterial.instance.init(context);
        ARRenderable.instance.init(context);
    }


    public void checkDistanceBetweenCameraAndPlane(float distance) {
        if(distance < 0.5) {
            EventCenter.instance.sendEvent(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_TOO_CLOSE, this, null);
        }
        else if(distance > 10) {
            EventCenter.instance.sendEvent(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_TOO_FAR, this, null);
        }
        else {
            EventCenter.instance.sendEvent(ARESSArrows.CAMERA_AND_PLANE_DISTANCE_OK, this, null);
        }
    }

    public void showGuidePoint(HitResult hitResult, ArSceneView arSceneView, boolean limitY) {

        floorFixedY = hitResult.getHitPose().ty();

        if(anchorNode != null && limitY) {
            floorFixedY = anchorNode.getWorldPosition().y;
        }

        if(guidePointNode != null) {
            guidePointNode.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), floorFixedY, hitResult.getHitPose().tz()));
        }
        else {
            guidePointNode = ARTool.createWorldNode(hitResult.getHitPose().tx(), floorFixedY, hitResult.getHitPose().tz(), ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);
            guidePointNode.setParent(arSceneView.getScene());
        }
    }

    public void showGuidePoint(Pose pose, ArSceneView arSceneView, boolean limitY) {

        floorFixedY = pose.ty();

        if(anchorNode != null && limitY) {
            floorFixedY = anchorNode.getWorldPosition().y;
        }

        if(guidePointNode != null) {
            guidePointNode.setWorldPosition(new Vector3(pose.tx(), floorFixedY, pose.tz()));
        }
        else {
            guidePointNode = ARTool.createWorldNode(pose.tx(), floorFixedY, pose.tz(), ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);
            guidePointNode.setParent(arSceneView.getScene());
        }
    }

    public void showGuidePoint(InstantPlacementPoint instantPlacementPoint, ArSceneView arSceneView, boolean limitY) {

        floorFixedY = instantPlacementPoint.getPose().ty();

        if(anchorNode != null && limitY) {
            floorFixedY = anchorNode.getWorldPosition().y;
        }

        if(guidePointNode != null) {
            guidePointNode.setWorldPosition(new Vector3(instantPlacementPoint.getPose().tx(), floorFixedY, instantPlacementPoint.getPose().tz()));
        }
        else {
            guidePointNode = ARTool.createWorldNode(instantPlacementPoint.getPose().tx(), floorFixedY, instantPlacementPoint.getPose().tz(), ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);
            guidePointNode.setParent(arSceneView.getScene());
        }
    }

    public void createDetectedPlaneNormalVector(Trackable trackable) {
        if(normalVectorOfPlane == null) {
            // calculate normal vector of detected plane
            normalVectorOfPlane = MathTool.getNormalVectorOfThreeVectors(
                    new Vector3(((Plane) trackable).getCenterPose().tx(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz()),
                    new Vector3(((Plane) trackable).getCenterPose().tx() + ((Plane) trackable).getExtentX(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz()),
                    new Vector3(((Plane) trackable).getCenterPose().tx(), ((Plane) trackable).getCenterPose().ty(), ((Plane) trackable).getCenterPose().tz() + ((Plane) trackable).getExtentZ())
            );
        }
    }

    public void createAnchorNode(HitResult hitResult, ArSceneView arSceneView) {

        if(anchorNode == null) {
            Anchor anchor = hitResult.createAnchor();
            anchorNode = ARTool.createAnchorNode(anchor);
            anchorNode.setParent(arSceneView.getScene());
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
            node = ARTool.createLocalNode(0, 0, 0, ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);
        }
        else {
//            Quaternion localRotation = new Quaternion(hitResult.getHitPose().qx(), hitResult.getHitPose().qy(), hitResult.getHitPose().qz(), hitResult.getHitPose().qw());
//            localRotation = Quaternion.multiply(this.anchorNode.getWorldRotation().inverted(), Preconditions.checkNotNull(localRotation));

//            Vector3 hitWorldPosition = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());
            Vector3 hitWorldPosition = new Vector3(hitResult.getHitPose().tx(), floorFixedY, hitResult.getHitPose().tz());
            Vector3 localPosition = MathTool.transformWorldPositionToLocalPositionOfParent(anchorNode, hitWorldPosition);

            node = ARTool.createLocalNode(localPosition.x, localPosition.y, localPosition.z, ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);
        }

        node.setParent(anchorNode);
        floorGuideList.add(node);

        DeviceUtil.vibrate(activity, 5);

        if(anchorNode != null) {
            floorFixedY = anchorNode.getWorldPosition().y;
        }
    }

    public void createMeasureHeightDrawNode(HitResult hitResult, Activity activity) {

        if(measureHeightFloorNode != null && measureHeightCeilingNode != null) {
            ARTool.removeChildFormNode(measureHeightFloorNode);
            ARTool.removeChildFormNode(measureHeightCeilingNode);

            measureHeightFloorNode.setParent(null);
            measureHeightCeilingNode.setParent(null);

            measureHeightFloorNode = null;
            measureHeightCeilingNode = null;
        }

        Vector3 hitWorldPosition = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());
        Vector3 localPosition = MathTool.transformWorldPositionToLocalPositionOfParent(anchorNode, hitWorldPosition);

        Node node = ARTool.createLocalNode(localPosition.x, localPosition.y, localPosition.z, ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);

        node.setParent(anchorNode);

        if(measureHeightFloorNode == null && measureHeightCeilingNode == null) {
            measureHeightFloorNode = node;
        }
        else if(measureHeightFloorNode != null && measureHeightCeilingNode == null) {
            measureHeightCeilingNode = node;
        }

        DeviceUtil.vibrate(activity, 5);
    }

    public void createMeasureHeightAutoNode(Activity activity) {
        Node node = ARTool.createLocalNode(0, 0, 0, ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow);
        node.setParent(anchorNode);
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

        ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), ARMaterial.instance.segmentMaterial);
        lineMode.setShadowCaster(ARMaterial.instance.nodeShadow);
        lineMode.setShadowReceiver(ARMaterial.instance.nodeShadow);

        if(guideSegmentNode == null) {
            guideSegmentNode = new Node();
            guideSegmentNode.setParent(startNode);
        }

        guideSegmentNode.setRenderable(lineMode);
        guideSegmentNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
        guideSegmentNode.setWorldRotation(rotationFromAToB);

        float length = MathTool.getLengthByUnit(ARConstants.arUnit, difference.length());

        if(guideSizeTextNode != null) {

            ((TextView) ARRenderable.instance.guideSizeTextView.getView()).setText(String.format("%.2f", length) + MathTool.getLengthUnitString(ARConstants.arUnit));

        }
        else {

            ((TextView) ARRenderable.instance.guideSizeTextView.getView()).setText(String.format("%.2f", length) + MathTool.getLengthUnitString(ARConstants.arUnit));

            guideSizeTextNode = new FaceToCameraNode();

            if(guideSegmentNode != null) {
                guideSizeTextNode.setParent(guideSegmentNode);
            }

            guideSizeTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
            guideSizeTextNode.setLocalPosition(new Vector3(0f, 0.08f, 0f));
            guideSizeTextNode.setRenderable(ARRenderable.instance.guideSizeTextView);

        }
    }

    public void drawSegment(Context context, Node startNode, Node endNode) {

        Node lineNode = ARTool.drawSegment(startNode, endNode, ARMaterial.instance.segmentMaterial, ARMaterial.instance.nodeShadow);

        float length = MathTool.getLengthOfTwoNode(startNode, endNode);
        ARTool.setSegmentSizeTextView(context, length, ARConstants.arUnit, lineNode, (viewRenderable, faceToCameraNode) -> {

        });
    }

    public void drawWallObjectSegment(Context context, Node startNode, Node endNode, float textHeight, boolean showSizeText) {

        Node lineNode = ARTool.drawSegment(startNode, endNode, ARMaterial.instance.objectSegmentMaterial, ARMaterial.instance.nodeShadow);

        if(showSizeText) {
            float length = MathTool.getLengthOfTwoNode(startNode, endNode);
            ARTool.setSegmentSizeTextView(context, length, ARConstants.arUnit, lineNode, textHeight, (viewRenderable, faceToCameraNode) -> {

            });
        }

    }

    public void autoCloseFloorSegment(Activity activity) {

        if(floorGuideList.size() > 2) {

            Node startNode = ARBuilder.instance.floorGuideList.get(ARBuilder.instance.floorGuideList.size() - 1);
            Node endNode = ARBuilder.instance.floorGuideList.get(0);

            Node lineNode = ARTool.drawSegment(startNode, endNode, ARMaterial.instance.segmentMaterial, ARMaterial.instance.nodeShadow);

            float length = MathTool.getLengthOfTwoNode(startNode, endNode);
            ARTool.setSegmentSizeTextView(activity, length, ARConstants.arUnit, lineNode, (viewRenderable, faceToCameraNode) -> {

            });

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
        return MathTool.getNodesDistanceMetersWithoutHeight(startNode, endNode) < 0.06;
    }

    /**
     * check room's floor is empty
     */
    public boolean checkFloorGuideEmpty() {
        return floorGuideList.isEmpty();
    }


    public void createRoom(Context context) {

        if(roomBean != null) {
            roomBean.clear();
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
                    ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow
            );

            pointBean.point.setParent(anchorNode);
            roomBean.floor.pointList.add(pointBean);
        }
        roomBean.floor.createSegment();
        roomBean.floor.type = ARConstants.PLANE_TYPE_FLOOR;

        // create ceiling
        for(int i = 0; i < floorGuideList.size(); i++) {
            pointBean = new PointBean();
            pointBean.point = ARTool.createLocalNode(
                    floorGuideList.get(i).getLocalPosition().x,
                    floorGuideList.get(i).getLocalPosition().y + height,
                    floorGuideList.get(i).getLocalPosition().z,
                    ARMaterial.instance.pointMaterial, ARMaterial.instance.nodeShadow
            );

            pointBean.point.setParent(anchorNode);
            roomBean.ceiling.pointList.add(pointBean);
        }
        roomBean.ceiling.createSegment();
        roomBean.ceiling.type = ARConstants.PLANE_TYPE_CEILING;

        // connect floor and ceiling and vertical
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
        PlaneBean planeBean;
        for(int i = 0; i < roomBean.floor.pointList.size(); i++) {

            planeBean = new PlaneBean();

            if(i < roomBean.floor.pointList.size() - 1) {
                planeBean.pointList.add(roomBean.floor.pointList.get(i));
                planeBean.pointList.add(roomBean.floor.pointList.get(i + 1));
                planeBean.pointList.add(roomBean.ceiling.pointList.get(i + 1));
                planeBean.pointList.add(roomBean.ceiling.pointList.get(i));
            }
            else {

                planeBean.pointList.add(roomBean.floor.pointList.get(i));
                planeBean.pointList.add(roomBean.floor.pointList.get(0));
                planeBean.pointList.add(roomBean.ceiling.pointList.get(0));
                planeBean.pointList.add(roomBean.ceiling.pointList.get(i));
            }

            planeBean.createSegment();
            planeBean.type = ARConstants.PLANE_TYPE_WALL;
            roomBean.wallList.add(planeBean);
        }

        // calculate
        ThreadUtil.startThread(() -> {
            roomBean.calculate();
            ThreadUtil.startUIThread(0, () -> {
                arBuilderDelegate.onCalculate(roomBean.height, roomBean.area, roomBean.circumference, roomBean.wallArea, roomBean.volume);
            });
        });

        clearGuidePlane();
        clearGuide();
        clearGuideSegment();
    }

    public void drawTempWallLine(Node startNode, Node endNode, Node tempLineNode, Node tempTextNode, ViewRenderable viewRenderableSizeText) {

        Vector3 startVector3 = startNode.getWorldPosition();
        Vector3 endVector3 = endNode.getWorldPosition();

        Vector3 difference = Vector3.subtract(startVector3, endVector3);

        Vector3 directionFromTopToBottom = difference.normalized();
        Quaternion rotationFromAToB = Quaternion.lookRotation(directionFromTopToBottom, Vector3.up());

        if(tempLineNode != null) {

            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), ARMaterial.instance.wallSegmentMaterial);
            lineMode.setShadowCaster(ARMaterial.instance.nodeShadow);
            lineMode.setShadowReceiver(ARMaterial.instance.nodeShadow);

            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }
        else {
            ModelRenderable lineMode = ShapeFactory.makeCube(new Vector3(0.005f, 0.005f, difference.length()), Vector3.zero(), ARMaterial.instance.wallSegmentMaterial);
            lineMode.setShadowCaster(ARMaterial.instance.nodeShadow);
            lineMode.setShadowReceiver(ARMaterial.instance.nodeShadow);

            tempLineNode = new Node();
            tempLineNode.setParent(startNode);
            tempLineNode.setRenderable(lineMode);
            tempLineNode.setWorldPosition(Vector3.add(startVector3, endVector3).scaled(0.5f));
            tempLineNode.setWorldRotation(rotationFromAToB);
        }

        float length = MathTool.getLengthByUnit(ARConstants.arUnit, difference.length());

        if(tempTextNode != null) {
            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + MathTool.getLengthUnitString(ARConstants.arUnit));
        }
        else {

            ((TextView) viewRenderableSizeText.getView()).setText(String.format("%.2f", length) + MathTool.getLengthUnitString(ARConstants.arUnit));

            tempTextNode = new FaceToCameraNode();
            tempTextNode.setParent(tempLineNode);

            tempTextNode.setLocalRotation(Quaternion.axisAngle(new Vector3(0f, 1f, 0f), 0f));
            tempTextNode.setLocalPosition(new Vector3(0f, 0.08f, 0f));
            tempTextNode.setRenderable(viewRenderableSizeText);

        }
    }

    public List<Integer> getThoughWall(ArSceneView arSceneView, List<Vector3> resultList, HitResult hitResult) {

        List<Integer> indexList = new ArrayList<>();

        if(roomBean == null) {
            return indexList;
        }

        Vector3 normalVector;
        Vector3 rayVector;
        Vector3 rayOrigin;
        Vector3 planePoint;

        for(int i = 0; i < roomBean.wallList.size(); i++) {

            // check wall test
            normalVector = MathTool.getNormalVectorOfThreeVectors(
                    new Vector3(
                            roomBean.wallList.get(i).pointList.get(0).point.getWorldPosition().x,
                            roomBean.wallList.get(i).pointList.get(0).point.getWorldPosition().y,
                            roomBean.wallList.get(i).pointList.get(0).point.getWorldPosition().z),

                    new Vector3(
                            roomBean.wallList.get(i).pointList.get(1).point.getWorldPosition().x,
                            roomBean.wallList.get(i).pointList.get(1).point.getWorldPosition().y,
                            roomBean.wallList.get(i).pointList.get(1).point.getWorldPosition().z),

                    new Vector3(
                            roomBean.wallList.get(i).pointList.get(3).point.getWorldPosition().x,
                            roomBean.wallList.get(i).pointList.get(3).point.getWorldPosition().y,
                            roomBean.wallList.get(i).pointList.get(3).point.getWorldPosition().z)
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

            planePoint = new Vector3(
                    roomBean.wallList.get(i).pointList.get(0).point.getWorldPosition().x,
                    roomBean.wallList.get(i).pointList.get(0).point.getWorldPosition().y,
                    roomBean.wallList.get(i).pointList.get(0).point.getWorldPosition().z);

            Vector3 result = new Vector3();
            boolean isPointInPlane = MathTool.calculateIntersectionOfLineAndPlane(rayVector, rayOrigin, normalVector, planePoint, result) == 1;

//            boolean isPointInPoly = ARUtil.checkIsVectorInPolygon(result, wallPoint);
            boolean isPointInPoly = MathTool.checkIsVectorInPolygon(result, roomBean.wallList.get(i).pointList.get(0).point.getWorldPosition(), roomBean.wallList.get(i).pointList.get(2).point.getWorldPosition());

            if(isPointInPlane && isPointInPoly) {
                resultList.add(result);
                indexList.add(i);
            }
        }

        return indexList;
    }

    public void clearGuideSegment() {

        if(guideSizeTextNode != null) {
            guideSizeTextNode.setParent(null);
            guideSizeTextNode = null;
        }

        if(guideSegmentNode != null) {
            guideSegmentNode.setParent(null);
            guideSegmentNode = null;
        }
    }

    private void backToMeasureHeight() {
        height = 0;

        clearGuidePlane();
        clearGuideSegment();
        clearGuide();
        clearAnchor();

        arBuilderDelegate.backToMeasureHeight();
    }

    public void clearGuidePlane() {
        for(Node node : floorGuideList) {
            ARTool.removeChildFormNode(node);
            node.setParent(null);
        }
        floorGuideList.clear();
    }

    public void clearGuide() {
        if(guidePointNode != null) {
            guidePointNode.setParent(null);
            guidePointNode = null;
        }
    }

    public boolean clearAnchor() {
        if(anchorNode != null) {
            ARTool.removeChildFormNode(anchorNode);
            anchorNode.setParent(null);
            anchorNode = null;
            return true;
        }

        return false;
    }

    public void clearMeasureHeightFloorNode() {

        if(measureHeightFloorNode != null) {
            ARTool.removeChildFormNode(measureHeightFloorNode);
            measureHeightFloorNode.setParent(null);
            measureHeightFloorNode = null;
        }

    }

    public void clearMeasureHeightCeilingNode() {
        if(measureHeightCeilingNode != null) {
            ARTool.removeChildFormNode(measureHeightCeilingNode);
            measureHeightCeilingNode.setParent(null);
            measureHeightCeilingNode = null;
        }
    }

    public void clearWallObject() {

        if(wallObjectBeanTemp != null) {
            wallObjectBeanTemp.clear();
            wallObjectBeanTemp = null;
        }

        if(wallGuidePoint != null) {
            wallGuidePoint.setParent(null);
            wallGuidePoint = null;
        }

        if(wallTempPoint != null) {
            wallTempPoint.setParent(null);
            wallTempPoint = null;
        }
    }

    public void clearRoomBean() {
        if(roomBean != null) {
            roomBean.clear();
            roomBean = null;
        }
    }

    public void back() {

        if(ARConstants.arProcess == ARConstants.ARProcess.MEASURE_HEIGHT) {

            if(ARConstants.measureHeightWay == ARConstants.MeasureHeightWay.AUTO) {

                if(!clearAnchor()) {
                    arBuilderDelegate.backToMeasureHeight();
                }
            }
            else if(ARConstants.measureHeightWay == ARConstants.MeasureHeightWay.DRAW) {

                if(measureHeightFloorNode == null && measureHeightCeilingNode == null) {

                    if(!clearAnchor()) {
                        arBuilderDelegate.backToMeasureHeight();
                    }
                }

                clearMeasureHeightFloorNode();
                clearMeasureHeightCeilingNode();
            }
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.MEASURE_ROOM) {

            if(floorGuideList.size() == 1) {

                clearAnchor();
                clearGuidePlane();
                clearGuideSegment();
                clearGuide();

                floorFixedY = -999;
            }
            else if(floorGuideList.size() > 1) {

                ARTool.removeChildFormNode(floorGuideList.get(floorGuideList.size() - 2));
                floorGuideList.get(floorGuideList.size() - 1).setParent(null);
                floorGuideList.remove(floorGuideList.size() - 1);

                clearGuideSegment();
                clearGuide();

            }
            else {

                backToMeasureHeight();
            }

            isReadyToAutoClose = false;
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.DRAW_WALL_OBJECT) {

            if(wallObjectBeanTemp != null) {

                clearWallObject();

                currentGuideIndex = -1;
                currentWallIndex = -1;

                clearGuide();

                ARConstants.arProcess = ARConstants.ARProcess.SELECTED_WALL_OBJECT;

                arBuilderDelegate.backToSelectWallObject();

                return;
            }

            clearWallObject();

            currentGuideIndex = -1;
            currentWallIndex = -1;

            ARConstants.arProcess = ARConstants.ARProcess.MEASURE_ROOM;

            clearGuidePlane();
            clearGuideSegment();
            clearGuide();
            clearAnchor();
            clearRoomBean();

            isReadyToAutoClose = false;
        }
        else if(ARConstants.arProcess == ARConstants.ARProcess.SELECTED_WALL_OBJECT) {

            if(roomBean != null) {
                // clear object on the wall
                if(!roomBean.wallObjectList.isEmpty()) {
                    // get last one
                    PlaneBean planeBean = roomBean.wallObjectList.get(roomBean.wallObjectList.size() - 1);
                    planeBean.clear();
                    roomBean.wallObjectList.remove(planeBean);
                }
                else {

                    clearWallObject();

                    currentGuideIndex = -1;
                    currentWallIndex = -1;

                    ARConstants.arProcess = ARConstants.ARProcess.MEASURE_ROOM;

                    clearGuidePlane();
                    clearGuideSegment();
                    clearGuide();
                    clearAnchor();
                    clearRoomBean();

                    isReadyToAutoClose = false;
                }
            }
        }
    }

    public void reset() {
        clearGuidePlane();
        clearGuide();
        clearGuideSegment();
        clearAnchor();
        clearWallObject();
        clearRoomBean();

        height = 0;
        floorFixedY = -999;
        normalVectorOfPlane = null;

        isReadyToAutoClose = false;
    }

    public void destroy() {

        clearAnchor();

        height = 0;

        currentWallIndex = -1;
        currentGuideIndex = -1;

        clearGuideSegment();
        clearGuide();

        clearGuidePlane();

        isReadyToAutoClose = false;

        clearMeasureHeightCeilingNode();
        clearMeasureHeightFloorNode();

        ARMaterial.instance.destroy();
        ARRenderable.instance.destroy();

        clearWallObject();

        wallGuidePoint = null;
        wallTempPoint = null;

    }
}
