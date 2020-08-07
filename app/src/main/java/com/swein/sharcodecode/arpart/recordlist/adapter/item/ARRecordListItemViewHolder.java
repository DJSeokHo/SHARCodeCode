package com.swein.sharcodecode.arpart.recordlist.adapter.item;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.bean.RoomBean;

import java.lang.ref.WeakReference;

public class ARRecordListItemViewHolder extends RecyclerView.ViewHolder {

    private WeakReference<View> view;

    public RoomBean roomBean;

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
        textViewHeight = view.get().findViewById(R.id.textViewHeight);
        textViewArea = view.get().findViewById(R.id.textViewArea);
        textViewCircumference = view.get().findViewById(R.id.textViewCircumference);
        textViewWallArea = view.get().findViewById(R.id.textViewWallArea);
        textViewVolume = view.get().findViewById(R.id.textViewVolume);
    }

    public void updateView() {

    }

}
