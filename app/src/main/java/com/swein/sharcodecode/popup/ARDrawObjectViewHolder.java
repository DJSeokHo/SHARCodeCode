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

    public ARConstants.WallObjectType wallObjectType;

    public interface ARDrawObjectViewHolderDelegate {
        void onDrawObject(ARConstants.WallObjectType wallObjectType);
        void onSave();
        void onClose();
    }

    private View view;
    private ARDrawObjectViewHolderDelegate arDrawObjectViewHolderDelegate;

    private TextView textViewWindow;
    private TextView textViewDoor;

    private Button buttonSave;

    private ImageView imageViewClose;

    public ARDrawObjectViewHolder(Context context, ARConstants.WallObjectType wallObjectType, ARDrawObjectViewHolderDelegate arDrawObjectViewHolderDelegate) {
        this.arDrawObjectViewHolderDelegate = arDrawObjectViewHolderDelegate;
        this.wallObjectType = wallObjectType;

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
            wallObjectType = ARConstants.WallObjectType.WINDOW;
            arDrawObjectViewHolderDelegate.onDrawObject(wallObjectType);
        });
        textViewDoor.setOnClickListener(view -> {
            wallObjectType = ARConstants.WallObjectType.DOOR;
            arDrawObjectViewHolderDelegate.onDrawObject(wallObjectType);
        });

        buttonSave.setOnClickListener(view -> arDrawObjectViewHolderDelegate.onSave());

        imageViewClose.setOnClickListener(view -> arDrawObjectViewHolderDelegate.onClose());
    }

    public View getView() {
        return view;
    }

}
