package com.swein.sharcodecode.popup;

import android.content.Context;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARConstants;
import com.swein.sharcodecode.framework.util.animation.AnimationUtil;
import com.swein.sharcodecode.framework.util.view.ViewUtil;

public class ARMeasureHeightHintViewHolder {

    public interface ARMeasureHeightHintViewHolderDelegate {
        void onConfirm(ARConstants.MeasureHeightWay measureHeightWay);
        void onClose();
        void onConfirmInput(float height);
    }

    private View view;

    private TextView textViewAuto;
    private TextView textViewDraw;
    private TextView textViewUnit;
    private EditText editText;
    private TextView textViewConfirm;

    private FrameLayout frameLayoutRoot;

    private ARMeasureHeightHintViewHolderDelegate arMeasureHeightHintViewHolderDelegate;

    public ARMeasureHeightHintViewHolder(Context context, ARMeasureHeightHintViewHolderDelegate arMeasureHeightHintViewHolderDelegate, ARConstants.ARUnit arUnit) {
        this.arMeasureHeightHintViewHolderDelegate = arMeasureHeightHintViewHolderDelegate;
        view = ViewUtil.inflateView(context, R.layout.view_holder_ar_measure_height_popup, null);
        findView();
        setListener();

        textViewUnit.setText(MathTool.getLengthUnitString(arUnit));
    }

    private void findView() {
        textViewAuto = view.findViewById(R.id.textViewAuto);
        textViewDraw = view.findViewById(R.id.textViewDraw);
        textViewUnit = view.findViewById(R.id.textViewUnit);
        frameLayoutRoot = view.findViewById(R.id.frameLayoutRoot);
        editText = view.findViewById(R.id.editText);
        textViewConfirm = view.findViewById(R.id.textViewConfirm);
    }

    private void setListener() {
        textViewAuto.setOnClickListener(view -> arMeasureHeightHintViewHolderDelegate.onConfirm(ARConstants.MeasureHeightWay.AUTO));
        textViewDraw.setOnClickListener(view -> arMeasureHeightHintViewHolderDelegate.onConfirm(ARConstants.MeasureHeightWay.DRAW));
        textViewConfirm.setOnClickListener(view -> {

            String heightString = editText.getText().toString().trim();

            if(heightString.equals("")) {
                AnimationUtil.shakeView(view.getContext(), editText);
                return;
            }

            float height = 0;

            try {
                height = Float.parseFloat(heightString);
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            arMeasureHeightHintViewHolderDelegate.onConfirmInput(height);
        });

        frameLayoutRoot.setOnClickListener(view -> arMeasureHeightHintViewHolderDelegate.onClose());
    }

    public View getView() {
        return view;
    }
}
