package com.swein.sharcodecode.popup;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.arpart.constants.ARConstants;
import com.swein.sharcodecode.framework.util.view.ViewUtil;

public class ARDrawObjectViewHolder {

    public String planeType;

    public interface ARDrawObjectViewHolderDelegate {
        void onDrawObject(String planeType);
        void onSave();
        void onClose();
    }

    private View view;
    private ARDrawObjectViewHolderDelegate arDrawObjectViewHolderDelegate;

    private TextView textViewWindow;
    private TextView textViewDoor;

    private Button buttonSave;

    private ImageView imageViewClose;

    public ARDrawObjectViewHolder(Context context, String planeType, ARDrawObjectViewHolderDelegate arDrawObjectViewHolderDelegate) {
        this.arDrawObjectViewHolderDelegate = arDrawObjectViewHolderDelegate;
        this.planeType = planeType;

        view = ViewUtil.inflateView(context, R.layout.view_holder_draw_object_popup, null);

        findView();
        setListener();
    }

    private void findView() {
        textViewWindow = view.findViewById(R.id.textViewWindow);
        textViewDoor = view.findViewById(R.id.textViewDoor);
        buttonSave = view.findViewById(R.id.buttonSave);
        imageViewClose = view.findViewById(R.id.imageViewClose);
    }

    private void setListener() {
        textViewWindow.setOnClickListener(view -> {
            planeType = ARConstants.PLANE_TYPE_WINDOW;
            arDrawObjectViewHolderDelegate.onDrawObject(planeType);
        });
        textViewDoor.setOnClickListener(view -> {
            planeType = ARConstants.PLANE_TYPE_DOOR;
            arDrawObjectViewHolderDelegate.onDrawObject(planeType);
        });

        buttonSave.setOnClickListener(view -> arDrawObjectViewHolderDelegate.onSave());

        imageViewClose.setOnClickListener(view -> arDrawObjectViewHolderDelegate.onClose());
    }

    public View getView() {
        return view;
    }

}
