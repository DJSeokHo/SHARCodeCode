package com.swein.sharcodecode.arpart.recordlist.adapter.item;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.swein.sharcodecode.arpart.bean.RoomBean;

import java.lang.ref.WeakReference;

public class ARRecordListItemViewHolder extends RecyclerView.ViewHolder {

    private WeakReference<View> view;

    public RoomBean roomBean;

    public ARRecordListItemViewHolder(@NonNull View itemView) {
        super(itemView);
        view = new WeakReference<>(itemView);
        findView();
    }

    private void findView() {

    }

    public void updateView() {

    }

}
