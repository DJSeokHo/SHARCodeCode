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
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARConstants;
import com.swein.sharcodecode.arpart.constants.ARESSArrows;
import com.swein.sharcodecode.arpart.environment.AREnvironment;
import com.swein.sharcodecode.framework.util.activity.ActivityUtil;
import com.swein.sharcodecode.framework.util.debug.ILog;
import com.swein.sharcodecode.framework.util.eventsplitshot.eventcenter.EventCenter;
import com.swein.sharcodecode.arpart.popup.ARDrawObjectViewHolder;
import com.swein.sharcodecode.arpart.popup.ARHintPopupViewHolder;
import com.swein.sharcodecode.arpart.popup.ARMeasureHeightHintViewHolder;
import com.swein.sharcodecode.framework.util.okhttp.OKHttpWrapper;
import com.swein.sharcodecode.framework.util.thread.ThreadUtil;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Response;

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

    private ARMeasureHeightHintViewHolder arMeasureHeightHintViewHolder;
    private ARDrawObjectViewHolder arDrawObjectViewHolder;
    private ARHintPopupViewHolder arHintPopupViewHolder;
    private FrameLayout frameLayoutPopup;

    private String name;
    private String unit;
    private float area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_a_r);

        checkBundle();
        initESS();

        findView();
        setListener();

        initAR();
    }

    private void checkBundle() {
        Bundle bundle = getIntent().getBundleExtra(ActivityUtil.BUNDLE_KEY);
        if(bundle != null) {
            name = bundle.getString("name");
            unit = bundle.getString("unit");

            switch (unit) {
                case "m":
                    ARConstants.arUnit = ARConstants.ARUnit.M;
                    break;

                case "cm":
                    ARConstants.arUnit = ARConstants.ARUnit.CM;
                    break;
            }

            area = bundle.getFloat("area");
        }
        else {
            finish();
        }
    }

    private void initESS() {

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
            public void onDetectingTargetMinimumPlaneAreaSize(int percentage) {
                showHint(percentage + "%");
            }

            @Override
            public void onMeasureHeight(float height) {

                showRealTimeHeight(height);
                clearHint();
                showMeasureRoomPopup();
            }

            @Override
            public void onCalculate(float height, float area, float circumference, float wallArea, float volume) {
                linearLayoutInfo.setVisibility(View.VISIBLE);

                textViewHeight.setText(getString(R.string.ar_area_height_title) + " " +
                        String.format("%.2f", MathTool.getLengthByUnit(ARConstants.arUnit, height)) + MathTool.getLengthUnitString(ARConstants.arUnit));

                textViewCircumference.setText(getString(R.string.ar_area_circumference_title) + " " +
                        String.format("%.2f", MathTool.getLengthByUnit(ARConstants.arUnit, circumference)) + MathTool.getLengthUnitString(ARConstants.arUnit));

                SpannableStringBuilder wallAreaString = new SpannableStringBuilder(getString(R.string.ar_wall_area_title) + " " + String.format("%.2f", MathTool.getAreaByUnit(ARConstants.arUnit, wallArea)));
                wallAreaString.append(MathTool.getAreaUnitString(ARConstants.arUnit));
                textViewWallArea.setText(wallAreaString);

                SpannableStringBuilder areaString = new SpannableStringBuilder(getString(R.string.ar_area_title) + " " + String.format("%.2f", MathTool.getAreaByUnit(ARConstants.arUnit, area)));
                areaString.append(MathTool.getAreaUnitString(ARConstants.arUnit)) ;
                textViewArea.setText(areaString);

                SpannableStringBuilder volumeString = new SpannableStringBuilder(getString(R.string.ar_volume_title) + " " + String.format("%.2f", MathTool.getVolumeByUnit(ARConstants.arUnit, volume)));
                volumeString.append(MathTool.getVolumeUnitString(ARConstants.arUnit));
                textViewVolume.setText(volumeString);

                clearHint();
            }

            @Override
            public void backToMeasureHeight() {
                textViewHeightRealTime.setText("");
                textViewHint.setText("");
                textViewHint.setVisibility(View.GONE);
                ARActivity.this.showMeasureHeightSelectPopup();
            }
        }, new AREnvironment.AREnvironmentShowHintDelegate() {
            @Override
            public void onDetectTargetMinimumPlaneAreaSizeFinished() {
                clearHint();
                ARActivity.this.showMeasureHeightSelectPopup();
            }

            @Override
            public void showDetectFloorHint() {
                ARActivity.this.showDetectFloorHint();
            }

            @Override
            public void showMeasureHeightSelectPopup() {
                ARActivity.this.showMeasureHeightSelectPopup();
            }

            @Override
            public void showSelectWallObjectPopup() {
                ARActivity.this.clearHint();
                ARActivity.this.showSelectWallObjectPopup();
            }

        });

        AREnvironment.instance.targetMinimumAreaSize = area;
    }

    @SuppressLint("RestrictedApi")
    private void setListener() {
        // Set a touch listener on the Scene to listen for taps.
        arSceneView.getScene().setOnTouchListener(
                (HitTestResult hitTestResult, MotionEvent event) -> {
                    AREnvironment.instance.onTouch(arSceneView);
                    return false;
                });

        arSceneView.getScene().addOnUpdateListener(
                frameTime -> AREnvironment.instance.onUpdateFrame(arSceneView));

        imageViewBack.setOnClickListener(view -> {
            AREnvironment.instance.back();
            clearRoomInfo();
        });

        imageViewReset.setOnClickListener(view -> AREnvironment.instance.reset(this, arSceneView, this::finish, () -> {

            clearRoomInfo();
            clearHint();
            clearRealTimeHeight();

            ARConstants.arProcess = ARConstants.ARProcess.DETECT_PLANE;
            ARConstants.measureHeightWay = ARConstants.MeasureHeightWay.NONE;
            ARConstants.planeType = ARConstants.PLANE_TYPE_NONE;
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

    private void showHint(String hint) {
        textViewHint.setText(hint);
        textViewHint.setVisibility(View.VISIBLE);
    }

    private void clearHint() {
        textViewHint.setText("");
        textViewHint.setVisibility(View.GONE);
    }

    public void showRealTimeHeight(float height) {
        String heightString = String.format("%.2f", MathTool.getLengthByUnit(ARConstants.arUnit, height)) + MathTool.getLengthUnitString(ARConstants.arUnit);
        textViewHeightRealTime.setText(heightString);
    }

    private void clearRealTimeHeight() {
        textViewHeightRealTime.setText("");
    }

    private void showDetectFloorHint() {
        arHintPopupViewHolder = new ARHintPopupViewHolder(this, this::closeDetectFloorHint);

        arHintPopupViewHolder.setTitle(getString(R.string.ar_scan_floor));
        arHintPopupViewHolder.setMessage(getString(R.string.ar_scan_ready_hint));

        frameLayoutPopup.addView(arHintPopupViewHolder.getView());
        frameLayoutPopup.setVisibility(View.VISIBLE);
    }

    private boolean closeDetectFloorHint() {

        if(arHintPopupViewHolder != null) {
            frameLayoutPopup.setVisibility(View.GONE);
            frameLayoutPopup.removeAllViews();
            arHintPopupViewHolder = null;

            return true;
        }

        return false;
    }

    private void showSelectWallObjectPopup() {
        arDrawObjectViewHolder = new ARDrawObjectViewHolder(this, ARConstants.planeType, new ARDrawObjectViewHolder.ARDrawObjectViewHolderDelegate() {

            @Override
            public void onDrawObject(String planeType) {
                ARConstants.planeType = planeType;
                closeSelectWallObjectPopup();

                ARConstants.arProcess = ARConstants.ARProcess.DRAW_WALL_OBJECT;

                if(ARConstants.planeType.equals(ARConstants.PLANE_TYPE_WINDOW)) {
                    showHint(getString(R.string.ar_draw_window));
                }
                else if(ARConstants.planeType.equals(ARConstants.PLANE_TYPE_DOOR)) {
                    showHint(getString(R.string.ar_draw_door));
                }
            }

            @Override
            public void onSave() {

                ARBuilder.instance.roomBean.name = name;
                ARBuilder.instance.roomBean.unit = MathTool.getLengthUnitString(ARConstants.arUnit);



                HashMap<String, String> hashMap = new HashMap<>();
                hashMap.put("X-AUTH-TOKEN", "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiI4NDkiLCJyb2xlcyI6WyJST0xFX1VTRVIiLCJST0xFX0FETUlOIl0sImlhdCI6MTYxMjQxMTQxNCwiZXhwIjoxNjQzOTQ3NDE0fQ.euw0iWIfXFFzyGse1w0jI1eY7kAMwEQ7EXV3nDpREQI");

                HashMap<String, String> formData = new HashMap<>();
                formData.put("name", name);
                try {
                    ILog.iLogDebug(TAG, ARBuilder.instance.roomBean.toJSONObject().toString());
                    formData.put("jsonObj", URLEncoder.encode(ARBuilder.instance.roomBean.toJSONObject().toString(), "UTF-8"));
                }
                catch (Exception e) {
                    e.printStackTrace();
                }


                OKHttpWrapper.instance.requestPostWithFormDataAndFiles("http://13.124.112.44/v1/model/new", hashMap, formData, new ArrayList<>(), new ArrayList<>(), new OKHttpWrapper.OKHttpWrapperDelegate() {
                    @Override
                    public void onFailure(@NotNull Call call, @NotNull IOException e) {
                        OKHttpWrapper.instance.cancelCall(call);
                        e.printStackTrace();
                    }

                    @Override
                    public void onResponse(@NotNull Call call, @NotNull Response response) {
                        try {
                            String responseString = OKHttpWrapper.instance.getStringResponse(response);

                            ThreadUtil.startUIThread(0, () -> {

                                try {
                                    JSONObject roomBeanJSONObject = ARBuilder.instance.roomBean.toJSONObject();
                                    HashMap<String, Object> hashMap = new HashMap<>();
                                    hashMap.put("roomBeanJSONObject", roomBeanJSONObject);

                                    EventCenter.instance.sendEvent(ARESSArrows.SAVE_ROOM_INFO, this, hashMap);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                closeSelectWallObjectPopup();
                                closeMeasureRoom();
                                closeDetectFloorHint();
                                closeMeasureHeightSelectPopup();
                                finish();

                            });
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        finally {
                            OKHttpWrapper.instance.cancelCall(call);
                        }
                    }
                });


            }

            @Override
            public void onClose() {
                closeSelectWallObjectPopup();
            }
        });

        frameLayoutPopup.addView(arDrawObjectViewHolder.getView());
        frameLayoutPopup.setVisibility(View.VISIBLE);
    }

    private boolean closeSelectWallObjectPopup() {

        if(arDrawObjectViewHolder != null) {
            frameLayoutPopup.setVisibility(View.GONE);
            frameLayoutPopup.removeAllViews();
            arDrawObjectViewHolder = null;

            return true;
        }

        return false;
    }

    private void showMeasureHeightSelectPopup() {
        arMeasureHeightHintViewHolder = new ARMeasureHeightHintViewHolder(this, new ARMeasureHeightHintViewHolder.ARMeasureHeightHintViewHolderDelegate() {
            @Override
            public void onConfirm(ARConstants.MeasureHeightWay measureHeightWay) {

                ARConstants.measureHeightWay = measureHeightWay;
                closeMeasureHeightSelectPopup();

                switch (ARConstants.measureHeightWay) {
                    case AUTO:
                        showHint(getString(R.string.ar_draw_height_by_ceiling_auto));
                        break;

                    case DRAW:
                        showHint(getString(R.string.ar_draw_height_direct));
                        break;
                }

                ARConstants.arProcess = ARConstants.ARProcess.MEASURE_HEIGHT;
            }

            @Override
            public void onClose() {
                closeMeasureHeightSelectPopup();
            }

            @Override
            public void onConfirmInput(float height) {

                AREnvironment.instance.setInputHeight(height);

                closeMeasureHeightSelectPopup();

                ARConstants.arProcess = ARConstants.ARProcess.MEASURE_ROOM;

                showRealTimeHeight(height);

                showMeasureRoomPopup();
            }

        }, ARConstants.arUnit);

        frameLayoutPopup.addView(arMeasureHeightHintViewHolder.getView());
        frameLayoutPopup.setVisibility(View.VISIBLE);
    }

    private boolean closeMeasureHeightSelectPopup() {
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

    @Override
    public void onBackPressed() {

        if(closeSelectWallObjectPopup()) {
            return;
        }

        if(closeMeasureRoom()) {
            return;
        }

        if(closeDetectFloorHint()) {
            return;
        }

        if(closeMeasureHeightSelectPopup()) {
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
        removeESS();
        super.onDestroy();
    }
}