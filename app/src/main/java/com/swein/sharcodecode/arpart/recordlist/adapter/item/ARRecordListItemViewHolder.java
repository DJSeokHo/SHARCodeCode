package com.swein.sharcodecode.arpart.recordlist.adapter.item;

import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.bean.RoomBean;
import com.swein.sharcodecode.arpart.builder.tool.MathTool;
import com.swein.sharcodecode.arpart.constants.ARConstants;

import java.lang.ref.WeakReference;

public class ARRecordListItemViewHolder extends RecyclerView.ViewHolder {

    private WeakReference<View> view;

    public RoomBean roomBean;

    private TextView textViewName;
    private TextView textViewUnit;
    private TextView textViewHeight;
    private TextView textViewArea;
    private TextView textViewCircumference;
    private TextView textViewWallArea;
    private TextView textViewVolume;

    public ARRecordListItemViewHolder(@NonNull View itemView) {
        super(itemView);
        view = new WeakReference<>(itemView);
        findView();
    }

    private void findView() {
        textViewName = view.get().findViewById(R.id.textViewName);
        textViewUnit = view.get().findViewById(R.id.textViewUnit);
        textViewHeight = view.get().findViewById(R.id.textViewHeight);
        textViewArea = view.get().findViewById(R.id.textViewArea);
        textViewCircumference = view.get().findViewById(R.id.textViewCircumference);
        textViewWallArea = view.get().findViewById(R.id.textViewWallArea);
        textViewVolume = view.get().findViewById(R.id.textViewVolume);
    }

    public void updateView() {

        textViewName.setText(roomBean.name);

        textViewUnit.setText(roomBean.unit);

        ARConstants.ARUnit arUnit = MathTool.getUnit(roomBean.unit);

        textViewHeight.setText(view.get().getContext().getString(R.string.ar_area_height_title) + " " +
                String.format("%.2f", MathTool.getLengthByUnit(arUnit, roomBean.height)) + MathTool.getLengthUnitString(arUnit));

        textViewCircumference.setText(view.get().getContext().getString(R.string.ar_area_circumference_title) + " " +
                String.format("%.2f", MathTool.getLengthByUnit(arUnit, roomBean.circumference)) + MathTool.getLengthUnitString(arUnit));

        SpannableStringBuilder wallAreaString = new SpannableStringBuilder(view.get().getContext().getString(R.string.ar_wall_area_title) + " " + String.format("%.2f", roomBean.wallArea));
        wallAreaString.append(MathTool.getAreaUnitString(arUnit));
        textViewWallArea.setText(wallAreaString);

        SpannableStringBuilder areaString = new SpannableStringBuilder(view.get().getContext().getString(R.string.ar_area_title) + " " + String.format("%.2f", roomBean.area));
        areaString.append(MathTool.getAreaUnitString(arUnit));
        textViewArea.setText(areaString);

        SpannableStringBuilder volumeString = new SpannableStringBuilder(view.get().getContext().getString(R.string.ar_volume_title) + " " + String.format("%.2f", roomBean.volume));
        volumeString.append(MathTool.getVolumeUnitString(arUnit));
        textViewVolume.setText(volumeString);
    }

}
