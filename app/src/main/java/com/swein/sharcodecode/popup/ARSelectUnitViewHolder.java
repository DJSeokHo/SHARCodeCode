package com.swein.sharcodecode.popup;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Spinner;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARConstants;
import com.swein.sharcodecode.framework.util.view.ViewUtil;

import java.util.ArrayList;
import java.util.List;

public class ARSelectUnitViewHolder {


    public interface ARSelectUnitViewHolderDelegate {
        void onSelectUnit(String unit);
        void onClose();
    }

    private View view;
    private ARSelectUnitViewHolderDelegate arSelectUnitViewHolderDelegate;

    private Spinner spinnerUnit;

    private List<String> unitList = new ArrayList<>();
    private String unit;

    private Button buttonConfirm;

    private FrameLayout frameLayoutRoot;

    private ARConstants.ARUnit arUnit;

    public ARSelectUnitViewHolder(Context context, ARConstants.ARUnit arUnit, ARSelectUnitViewHolderDelegate arSelectUnitViewHolderDelegate) {
        this.arSelectUnitViewHolderDelegate = arSelectUnitViewHolderDelegate;

        this.arUnit = arUnit;
        view = ViewUtil.inflateView(context, R.layout.view_holder_ar_select_unit_popup, null);

        initData();
        findView();
        initSpinner();
        setListener();
    }

    private void initData() {
        unitList.clear();
        unitList.add(view.getContext().getString(R.string.ar_unit_m));
        unitList.add(view.getContext().getString(R.string.ar_unit_cm));
        unit = MathTool.getLengthUnitString(arUnit);
    }

    private void findView() {
        spinnerUnit = view.findViewById(R.id.spinnerUnit);
        buttonConfirm = view.findViewById(R.id.buttonConfirm);
        frameLayoutRoot = view.findViewById(R.id.frameLayoutRoot);
    }

    private void initSpinner() {

        ArrayAdapter typeArrayAdapter = new ArrayAdapter(view.getContext(), R.layout.spinner_item_select, unitList.toArray());

        typeArrayAdapter.setDropDownViewResource(R.layout.spinner_item_drop);
        spinnerUnit.setAdapter(typeArrayAdapter);
        spinnerUnit.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(unit.equals("")) {
                    unit = unitList.get(0);
                    return;
                }

                unit = unitList.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        switch (arUnit) {
            case M:
                spinnerUnit.setSelection(0);
                break;
            case CM:
                spinnerUnit.setSelection(1);
                break;
        }
    }

    private void setListener() {
        buttonConfirm.setOnClickListener(view -> arSelectUnitViewHolderDelegate.onSelectUnit(unit));
        frameLayoutRoot.setOnClickListener(view -> arSelectUnitViewHolderDelegate.onClose());
    }

    public View getView() {
        return view;
    }

}
