package com.swein.sharcodecode.framework.util.view;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.swein.sharcodecode.R;


public class ViewUtil {

    public static View inflateView(Context context, int resource, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(resource, viewGroup);
    }

    public static void viewFromBottom(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.view_from_bottom);
        view.startAnimation(animation);
    }

    public static void viewOutBottom(Context context, View view) {
        Animation animation = AnimationUtils.loadAnimation(context, R.anim.view_out_bottom);
        view.startAnimation(animation);
    }
}
