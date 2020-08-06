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

import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.HitTestResult;
import com.swein.sharcodecode.R;
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

    private ImageView imageViewBack;
    private ImageView imageViewReset;

    private ARSelectUnitViewHolder arSelectUnitViewHolder;
    private ARMeasureHeightHintViewHolder arMeasureHeightHintViewHolder;
    private ARDrawObjectViewHolder arDrawObjectViewHolder;
    private ARHintPopupViewHolder arHintPopupViewHolder;
    private FrameLayout frameLayoutPopup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        initESS();

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
            ARBuilder.instance.clearWallObject();

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