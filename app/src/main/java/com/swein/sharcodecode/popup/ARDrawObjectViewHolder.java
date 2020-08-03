package com.swein.sharcodecode.popup;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.framework.util.view.ViewUtil;

class ARDrawObjectViewHolder {

    public final static String WINDOW_TYPE = "WINDOW_TYPE";
    public final static String DOOR_TYPE = "DOOR_TYPE";

    public interface ARDrawObjectViewHolderDelegate {
        void onDrawObject(String type);
    }

    private View view;
    private ARDrawObjectViewHolderDelegate arDrawObjectViewHolderDelegate;

    private TextView textViewWindow;
    private TextView textViewDoor;

    public ARDrawObjectViewHolder(Context context, ARDrawObjectViewHolderDelegate arDrawObjectViewHolderDelegate) {
        this.arDrawObjectViewHolderDelegate = arDrawObjectViewHolderDelegate;
        view = ViewUtil.inflateView(context, R.layout.view_holder_draw_object_popup, null);

        findView();
        setListener();
    }

    private void findView() {
        textViewWindow = view.findViewById(R.id.textViewWindow);
        textViewDoor = view.findViewById(R.id.textViewDoor);
    }

    private void setListener() {
        textViewWindow.setOnClickListener(view -> arDrawObjectViewHolderDelegate.onDrawObject(WINDOW_TYPE));
        textViewDoor.setOnClickListener(view -> arDrawObjectViewHolderDelegate.onDrawObject(DOOR_TYPE));
    }

    public View getView() {
        return view;
    }

}
