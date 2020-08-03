package com.swein.sharcodecode.popup;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.swein.sharcodecode.R;
import com.swein.sharcodecode.framework.util.view.ViewUtil;

class ARHintPopupViewHolder {

    public interface ARHintPopupViewHolderDelegate {
        void onConfirm();
    }

    private View view;
    private ARHintPopupViewHolderDelegate arHintPopupViewHolderDelegate;

    private TextView textViewTitle;
    private TextView textViewMessage;
    private Button buttonConfirm;

    public ARHintPopupViewHolder(Context context, ARHintPopupViewHolderDelegate arHintPopupViewHolderDelegate) {
        this.arHintPopupViewHolderDelegate = arHintPopupViewHolderDelegate;
        view = ViewUtil.inflateView(context, R.layout.view_holder_ar_hint_popup, null);

        findView();
        setListener();
    }

    private void findView() {
        textViewTitle = view.findViewById(R.id.textViewTitle);
        textViewMessage = view.findViewById(R.id.textViewMessage);
        buttonConfirm = view.findViewById(R.id.buttonConfirm);
    }

    public void setTitle(String title) {
        textViewTitle.setText(title);
    }

    public void setMessage(String message) {
        textViewMessage.setText(message);
    }

    private void setListener() {
        buttonConfirm.setOnClickListener(view -> arHintPopupViewHolderDelegate.onConfirm());
    }

    public View getView() {
        return view;
    }
}
