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
import com.google.ar.sceneform.math.Vector3;
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.builder.ARBuilder;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.toast.ToastUtil;

import java.util.Collection;
import java.util.List;

public class AREnvironment {

    public interface AREnvironmentDelegate {
        void onUpdatePlaneType(String type);

        void showDetectFloorHint();
        void showMeasureHeightSelectPopup();
        void onMeasureHeight(float height);
    }

    private final static String TAG = "AREnvironment";

    public static AREnvironment instance = new AREnvironment();

    private AREnvironment() {}

    private Activity activity;

    public Config.PlaneFindingMode planeFindingMode;

    public boolean isHitCeiling = false;

    public float hitPointX;
    public float hitPointY;


    private AREnvironmentDelegate arEnvironmentDelegate;

    public void init(Activity activity, AREnvironmentDelegate arEnvironmentDelegate) {
        this.activity = activity;
        this.arEnvironmentDelegate = arEnvironmentDelegate;
        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL;

        arEnvironmentDelegate.showDetectFloorHint();
    }


    public boolean checkPlanEnable(Frame frame) {
        if (frame == null) {
            return false;
        }

        if (frame.getCamera().getTrackingState() != TrackingState.TRACKING) {
            return false;
        }

        return true;
    }

    public void updateCloudPoint(ArSceneView arSceneView) {

        Frame frame = arSceneView.getArFrame();
        if (frame == null) {
            return;
        }

        // update plan cloud point area
        ARTool.updatePlanRenderer(arSceneView.getPlaneRenderer());

        // if plan size big than target minimum area size
        // then hide finding plan view
        Collection<Plane> planeCollection = frame.getUpdatedTrackables(Plane.class);
        ARBuilder.instance.checkPlaneSize(planeCollection);

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

    public void onTouch(ArSceneView arSceneView) {

        if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.DETECT_PLANE) {
            return;
        }

        if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.MEASURE_HEIGHT_HINT) {
            arEnvironmentDelegate.showMeasureHeightSelectPopup();
        }
        else if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.MEASURE_HEIGHT) {

            switch (ARBuilder.instance.measureHeightWay) {
                case AUTO:
                    measureHeightAutoWhenTouch(arSceneView);
                    break;

                case DRAW:
                    measureHeightDrawWhenTouch(arSceneView);
                    break;
            }
        }
        else if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.MEASURE_ROOM) {

            Frame frame = arSceneView.getArFrame();
            List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
            for (HitResult hitResult : hitTestResultList) {
                Trackable trackable = hitResult.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {


                    if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.DRAW_WALL_OBJECT) {

                        return;
                    }

                    // if ready to auto close
                    if(ARBuilder.instance.isReadyToAutoClose) {

                        ARBuilder.instance.clearTemp();
                        ARBuilder.instance.clearGuide();

                        ARBuilder.instance.autoCloseFloorSegment(activity);
                        ARBuilder.instance.createRoom(activity);

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

                    ARBuilder.instance.clearTemp();

                }
            }
        }
    }

    public void onUpdateFrame(ArSceneView arSceneView) {

        if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.MEASURE_HEIGHT) {

            switch (ARBuilder.instance.measureHeightWay) {
                case AUTO:
                    measureHeightAutoWhenUpdate(arSceneView);
                    break;

                case DRAW:
                    measureHeightDrawWhenUpdate(arSceneView);
                    break;
            }
        }
        else if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.MEASURE_ROOM) {

            Frame frame = arSceneView.getArFrame();

            List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
            for (HitResult hitResult : hitTestResultList) {
                Trackable trackable = hitResult.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                    ARBuilder.instance.checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                    ARBuilder.instance.showGuidePoint(hitResult, arSceneView, true);

                    if(ARBuilder.instance.checkFloorGuideEmpty()) {
                        return;
                    }

                    // auto closed
                    if(ARBuilder.instance.arProcess == ARBuilder.ARProcess.DRAW_WALL_OBJECT) {

                    }
                    else {
                        ARBuilder.instance.drawFloorGuideSegment(
                                ARBuilder.instance.floorGuideList.get(ARBuilder.instance.floorGuideList.size() - 1),
                                ARBuilder.instance.guidePointNode);

                        ARBuilder.instance.checkPolygonAutoClose(activity);
                    }

                    return;
                }
            }
        }
    }

    private void drawWallObjectWhenTouch() {

    }

    private void drawWallObjectWhenUpdate() {

    }

    private void measureHeightDrawWhenTouch(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
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
                        // get distance ceiling

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
                        arEnvironmentDelegate.onMeasureHeight(height);
                    }

                    return;
                }

                ARBuilder.instance.clearTemp();
            }
        }
    }

    private void measureHeightDrawWhenUpdate(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();

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
        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                // calculate normal vector of detected plane
                ARBuilder.instance.createDetectedPlaneNormalVector(trackable);

                // can only one node on floor
                if(ARBuilder.instance.anchorNode != null) {
                    return;
                }

                // create first anchorNode
                ARBuilder.instance.createAnchorNode(hitResult, arSceneView);

                ARBuilder.instance.createMeasureHeightAutoNode(hitResult, activity);

            }
        }
    }

    private void measureHeightAutoWhenUpdate(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
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

    public void pause(ArSceneView arSceneView) {
        if (arSceneView != null) {
            arSceneView.pause();
        }
    }

    public void destroy(ArSceneView arSceneView) {
        if (arSceneView != null) {
            arSceneView.destroy();
        }
    }

}
