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

    public static AREnvironment getInstance() {
        return instance;
    }

    private static AREnvironment instance = new AREnvironment();

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
        ARBuilder.getInstance().checkPlaneSize(planeCollection);

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

        if(ARBuilder.getInstance().arProcess == ARBuilder.ARProcess.DETECT_PLANE) {
            return;
        }

        if(ARBuilder.getInstance().arProcess == ARBuilder.ARProcess.MEASURE_HEIGHT_HINT) {
            arEnvironmentDelegate.showMeasureHeightSelectPopup();
        }
        else if(ARBuilder.getInstance().arProcess == ARBuilder.ARProcess.MEASURE_HEIGHT) {

            switch (ARBuilder.getInstance().measureHeightWay) {
                case AUTO:
                    measureHeightAutoWhenTouch(arSceneView);
                    break;

                case DRAW:
                    measureHeightDrawWhenTouch(arSceneView);
                    break;
            }
        }
        else if(ARBuilder.getInstance().arProcess == ARBuilder.ARProcess.MEASURE_ROOM) {

            Frame frame = arSceneView.getArFrame();
            List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
            for (HitResult hitResult : hitTestResultList) {
                Trackable trackable = hitResult.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {


                    if(ARBuilder.getInstance().isAutoClosed) {

                        return;
                    }

                    // if ready to auto close
                    if(ARBuilder.getInstance().isReadyToAutoClose) {

                        ARBuilder.getInstance().clearTemp();
                        ARBuilder.getInstance().clearGuide();

                        ARBuilder.getInstance().autoCloseFloorSegment(activity);
                        ARBuilder.getInstance().createRoom(activity);

                        return;
                    }

                    // calculate normal vector of detected plane
                    ARBuilder.getInstance().createDetectedPlaneNormalVector(trackable);

                    // create first anchorNode
                    ARBuilder.getInstance().createAnchorNode(hitResult, arSceneView);

                    // two vector is too near
                    if(ARBuilder.getInstance().isNodesTooNearClosed()) {
                        return;
                    }

                    // create guide floor node
                    ARBuilder.getInstance().createGuideFloorNode(hitResult, activity);

                    if(ARBuilder.getInstance().floorGuideList.size() >= 2) {
                        ARBuilder.getInstance().drawSegment(activity,
                                ARBuilder.getInstance().floorGuideList.get(ARBuilder.getInstance().floorGuideList.size() - 2),
                                ARBuilder.getInstance().floorGuideList.get(ARBuilder.getInstance().floorGuideList.size() - 1)
                        );
                    }

                    ARBuilder.getInstance().clearTemp();

                }
            }
        }
    }

    public void onUpdateFrame(ArSceneView arSceneView) {

        if(ARBuilder.getInstance().arProcess == ARBuilder.ARProcess.MEASURE_HEIGHT) {

            switch (ARBuilder.getInstance().measureHeightWay) {
                case AUTO:
                    measureHeightAutoWhenUpdate(arSceneView);
                    break;

                case DRAW:
                    measureHeightDrawWhenUpdate(arSceneView);
                    break;
            }
        }
        else if(ARBuilder.getInstance().arProcess == ARBuilder.ARProcess.MEASURE_ROOM) {

            Frame frame = arSceneView.getArFrame();

            List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
            for (HitResult hitResult : hitTestResultList) {
                Trackable trackable = hitResult.getTrackable();
                if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                    ARBuilder.getInstance().checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                    ARBuilder.getInstance().showGuidePoint(hitResult, arSceneView, true);

                    if(ARBuilder.getInstance().checkFloorGuideEmpty()) {
                        return;
                    }

                    // auto closed
                    if(ARBuilder.getInstance().isAutoClosed) {

                    }
                    else {
                        ARBuilder.getInstance().drawFloorGuideSegment(
                                ARBuilder.getInstance().floorGuideList.get(ARBuilder.getInstance().floorGuideList.size() - 1),
                                ARBuilder.getInstance().guidePointNode);

                        ARBuilder.getInstance().checkPolygonAutoClose(activity);
                    }

                    return;
                }
            }
        }
    }

    private void measureHeightDrawWhenTouch(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                // calculate normal vector of detected plane
                ARBuilder.getInstance().createDetectedPlaneNormalVector(trackable);

                // create first anchorNode
                ARBuilder.getInstance().createAnchorNode(hitResult, arSceneView);

                // two vector is too near
                if(ARBuilder.getInstance().isNodesTooNearClosed()) {
                    return;
                }

                // only one node can on floor
                if(ARBuilder.getInstance().measureHeightFloorNode != null && ARBuilder.getInstance().measureHeightCeilingNode == null) {
                    if(!isHitCeiling) {
                        return;
                    }
                }

                ARBuilder.getInstance().createMeasureHeightDrawNode(hitResult, activity);

                if(ARBuilder.getInstance().measureHeightFloorNode != null && ARBuilder.getInstance().measureHeightCeilingNode != null) {
                    // finish measure height

                    if(isHitCeiling) {
                        // get distance ceiling

                        Vector3 floorPoint = new Vector3(
                                ARBuilder.getInstance().measureHeightFloorNode.getWorldPosition().x,
                                ARBuilder.getInstance().measureHeightFloorNode.getWorldPosition().y,
                                ARBuilder.getInstance().measureHeightFloorNode.getWorldPosition().z
                        );
                        Vector3 ceilingPoint = new Vector3(
                                ARBuilder.getInstance().measureHeightCeilingNode.getWorldPosition().x,
                                ARBuilder.getInstance().measureHeightCeilingNode.getWorldPosition().y,
                                ARBuilder.getInstance().measureHeightCeilingNode.getWorldPosition().z
                        );

                        float height = ARTool.getLengthBetweenPointToPlane(ceilingPoint, floorPoint, ARBuilder.getInstance().normalVectorOfPlane);
                        arEnvironmentDelegate.onMeasureHeight(height);
                    }

                    return;
                }

                ARBuilder.getInstance().clearTemp();
            }
        }
    }

    private void measureHeightDrawWhenUpdate(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();

        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                ARBuilder.getInstance().checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                ARBuilder.getInstance().showGuidePoint(hitResult, arSceneView, false);

                if (ARBuilder.getInstance().measureHeightFloorNode != null && ARBuilder.getInstance().measureHeightCeilingNode != null) {
                    return;
                }

                if (ARBuilder.getInstance().measureHeightFloorNode == null && ARBuilder.getInstance().measureHeightCeilingNode == null) {
                    return;
                }

                if (ARBuilder.getInstance().measureHeightFloorNode != null) {
                    ARBuilder.getInstance().drawFloorGuideSegment(ARBuilder.getInstance().measureHeightFloorNode, ARBuilder.getInstance().guidePointNode);
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
                ARBuilder.getInstance().createDetectedPlaneNormalVector(trackable);

                // can only one node on floor
                if(ARBuilder.getInstance().anchorNode != null) {
                    return;
                }

                // create first anchorNode
                ARBuilder.getInstance().createAnchorNode(hitResult, arSceneView);

                ARBuilder.getInstance().createMeasureHeightAutoNode(hitResult, activity);

            }
        }
    }

    private void measureHeightAutoWhenUpdate(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();
        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                ARBuilder.getInstance().checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                ARBuilder.getInstance().showGuidePoint(hitResult, arSceneView, false);

                if(isHitCeiling) {
                    // get distance ceiling

                    Vector3 floorPoint = new Vector3(
                            ARBuilder.getInstance().anchorNode.getWorldPosition().x,
                            ARBuilder.getInstance().anchorNode.getWorldPosition().y,
                            ARBuilder.getInstance().anchorNode.getWorldPosition().z
                    );
                    Vector3 ceiling = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());

                    float height = ARTool.getLengthBetweenPointToPlane(ceiling, floorPoint, ARBuilder.getInstance().normalVectorOfPlane);
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
                Session session = createArSession(activity, true, null, AREnvironment.getInstance().planeFindingMode);

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
