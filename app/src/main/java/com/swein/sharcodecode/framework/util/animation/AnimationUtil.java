package com.swein.sharcodecode.framework.util.animation;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.swein.sharcodecode.R;

public class AnimationUtil {

    public static Animation show(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fade_in);
    }

    public static Animation show1500(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fade_in_1500);
    }

    public static Animation show2500(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fade_in_2500);
    }

    public static Animation hide(Context context) {
        return AnimationUtils.loadAnimation(context, R.anim.fade_out);
    }

    public static void shakeView(Context context, View view) {
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
        view.startAnimation(shake);
    }

    public static void jellyView(Context context, View view) {
        Animation shake = AnimationUtils.loadAnimation(context, R.anim.jelly);
        view.startAnimation(shake);
    }

    public interface ValueAnimatorDelegate {
        void onAnimationUpdate(ValueAnimator valueAnimator);
        void onAnimationStart(Animator animator);
        void onAnimationEnd(Animator animator);
        void onAnimationCancel(Animator animator);
        void onAnimationRepeat(Animator animator);
    }

    public static void floatAnimation(float start, float end, int duration, ValueAnimatorDelegate valueAnimatorDelegate) {
        ValueAnimator animator;
        animator = ValueAnimator.ofFloat(start, end);
        animator.addUpdateListener(valueAnimatorDelegate::onAnimationUpdate);
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animator) {
                valueAnimatorDelegate.onAnimationStart(animator);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                valueAnimatorDelegate.onAnimationEnd(animator);
            }

            @Override
            public void onAnimationCancel(Animator animator) {
                valueAnimatorDelegate.onAnimationCancel(animator);
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
                valueAnimatorDelegate.onAnimationRepeat(animator);
            }
        });
        animator.setDuration(duration).start();
    }
}
