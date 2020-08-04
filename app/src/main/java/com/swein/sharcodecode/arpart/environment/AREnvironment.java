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
import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.builder.ARBuilder;
import com.swein.sharcodecode.arpart.builder.tool.ARTool;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.toast.ToastUtil;

import java.util.Collection;
import java.util.List;

public class AREnvironment {

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

    public void init(Activity activity) {
        this.activity = activity;
        planeFindingMode = Config.PlaneFindingMode.HORIZONTAL;
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

    public String updateCloudPointAndPlayType(ArSceneView arSceneView) {

        Frame frame = arSceneView.getArFrame();
        if (frame == null) {
            return "";
        }

        // update plan cloud point area
        ARTool.updatePlanRenderer(arSceneView.getPlaneRenderer());

        // if plan size big than target minimum area size
        // then hide finding plan view
        Collection<Plane> planeCollection = frame.getUpdatedTrackables(Plane.class);
        ARBuilder.getInstance().checkPlaneSize(planeCollection);

        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);

        // check plan type

        String planType = ARTool.checkPlanType(
                hitTestResultList, "",
                arSceneView.getContext().getString(R.string.ar_plane_type_wall),
                arSceneView.getContext().getString(R.string.ar_plane_type_ceiling),
                arSceneView.getContext().getString(R.string.ar_plane_type_floor));

        isHitCeiling = planType.equals(arSceneView.getContext().getString(R.string.ar_plane_type_ceiling));

        return planType;
    }

    public void onTouch(ArSceneView arSceneView) {
        Frame frame = arSceneView.getArFrame();

        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                // if ready to auto close
                if(ARBuilder.getInstance().isReadyToAutoClose) {

                    ARBuilder.getInstance().clearTemp();
                    ARBuilder.getInstance().clearGuide();

                    ARBuilder.getInstance().autoCloseFloorSegment(activity);




//                    createCellPolygon();
//                    calculate();
//                    createWall();
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

                ARBuilder.getInstance().drawFloorSegment(activity);

                ARBuilder.getInstance().clearTemp();

            }
        }
    }

    public void onUpdateFrame(ArSceneView arSceneView) {

        Frame frame = arSceneView.getArFrame();

        List<HitResult> hitTestResultList = frame.hitTest(hitPointX, hitPointY);
        for (HitResult hitResult : hitTestResultList) {
            Trackable trackable = hitResult.getTrackable();
            if (trackable instanceof Plane && ((Plane) trackable).isPoseInPolygon(hitResult.getHitPose())) {

                ARBuilder.getInstance().checkDistanceBetweenCameraAndPlane(hitResult.getDistance());
                ARBuilder.getInstance().showGuidePoint(hitResult, arSceneView);

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

//                if(isHitCeiling) {
//                    // get distance ceiling
//                    if(centerPoint != null) {
//                        centerPoint.setWorldPosition(new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz()));
//                    }
//                    else {
//                        centerPoint = ARUtil.createWorldNode(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz(), pointMaterial, shadow);
//                    }
////                                Vector3 floorPoint = new Vector3(bottomAnchorPolygon.get(0).getWorldPosition().x, bottomAnchorPolygon.get(0).getWorldPosition().y, bottomAnchorPolygon.get(0).getWorldPosition().z);
//                    Vector3 floorPoint = new Vector3(anchorNode.getWorldPosition().x, anchorNode.getWorldPosition().y, anchorNode.getWorldPosition().z);
//                    Vector3 ceiling = new Vector3(hitResult.getHitPose().tx(), hitResult.getHitPose().ty(), hitResult.getHitPose().tz());
//
//                    height = ARUtil.getLengthBetweenPointToPlane(ceiling, floorPoint, normalVectorOfPlane);
//                    textViewHeightRealTime.setText(String.valueOf(height));
//                }
//                else {
//                    drawTempLine(floorPolygonList.get(floorPolygonList.size() - 1), centerPoint);
//                }

                    ARBuilder.getInstance().checkPolygonAutoClose(activity);
                }

                return;
            }
        }
    }

    public void reset(Activity activity, ArSceneView arSceneView, Runnable finishActivity, Runnable findingPlane) {

        if (arSceneView == null) {
            return;
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
