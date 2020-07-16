package com.swein.sharcodecode.framework.util.toast;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.swein.sharcodecode.R;

public class ToastUtil {

    public static void showCustomShortToastNormal(Context context, String string) {

        Toast toast = new Toast(context);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.toast_custom_background);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(15, 15, 15, 15);

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setPadding(15, 15, 15, 15);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PT, 5.58f);
        textView.setTextColor(Color.WHITE);
        linearLayout.addView(textView);

        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();

    }

    public static void showCustomLongToastNormal(Context context, String string) {

        Toast toast = new Toast(context);

        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setBackgroundResource(R.drawable.toast_custom_background);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.setPadding(15, 15, 15, 15);

        TextView textView = new TextView(context);
        textView.setText(string);
        textView.setPadding(15, 15, 15, 15);
        textView.setGravity(Gravity.CENTER);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PT,5.58f);
        textView.setTextColor(Color.WHITE);
        linearLayout.addView(textView);

        toast.setView(linearLayout);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.show();
    }

}
