package com.swein.sharcodecode.arpart.builder;

import android.content.Context;
import android.graphics.Color;

import com.google.ar.core.HitResult;
import com.google.ar.core.Plane;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.Material;
import com.swein.sharcodecode.arpart.bean.RoomBean;
import com.swein.sharcodecode.arpart.bean.basic.SegmentBean;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;
import com.swein.sharcodecode.arpart.constants.ARESSArrows;
import com.swein.sharcodecode.framework.util.eventsplitshot.eventcenter.EventCenter;

import java.util.Collection;

public class ARBuilder {

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

    // node shadow
    public boolean nodeShadow = true;

    // temp guide line
    public SegmentBean guideSegment;

    // point of screen center
    public Node guidePoint;

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

    public RoomBean roomBean;


    public void init(Context context) {

        // 감지 필요한 최소 cloud point 면적
        targetMinimumAreaSize = 3;

        // create room bean
        roomBean = new RoomBean();

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

        roomBean.floorFixedY = hitResult.getHitPose().ty();

        if(roomBean.anchorNode != null) {
            roomBean.floorFixedY = roomBean.anchorNode.getWorldPosition().y;
        }

        if(guidePoint != null) {
            guidePoint.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), roomBean.floorFixedY, hitResult.getHitPose().tz()));
        }
        else {
            guidePoint = ARTool.createWorldNode(hitResult.getHitPose().tx(), roomBean.floorFixedY, hitResult.getHitPose().tz(), pointMaterial, nodeShadow);
            guidePoint.setParent(arSceneView.getScene());
        }
    }

    /**
     * check room's floor is empty
     */
    public boolean checkRoomFloorEmpty() {
        return roomBean.floorBean.planeBean.segmentList.isEmpty();
    }

    public void clear() {

        targetMinimumAreaSize = 0;

        // temp guide line
        guideSegment = null;

        // point of screen center
        guidePoint = null;

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

        roomBean = null;
    }
}
