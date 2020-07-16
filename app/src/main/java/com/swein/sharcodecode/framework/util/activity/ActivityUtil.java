package com.swein.sharcodecode.framework.util.activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.transition.Explode;
import android.transition.Fade;
import android.transition.Slide;
import android.view.Window;
import android.view.animation.AnimationUtils;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.swein.sharcodecode.R;

import java.util.List;

public class ActivityUtil {
    
    public final static String BUNDLE_KEY = "BUNDLE_KEY";

    public static void startSystemIntent(Context packageContext, Intent intent) {
        packageContext.startActivity(intent);
    }

    public static void startSystemIntentWithResultByRequestCode(Activity activity, Intent intent, int requestCode) {
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startNewActivityWithoutFinish(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        packageContext.startActivity(intent);
    }

    public static void startNewActivityWithoutFinish(Context packageContext, Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtra(BUNDLE_KEY, bundle);
        packageContext.startActivity(intent);
    }

    public static void startNewActivityWithFinish(Context packageContext, Class<?> cls) {
        Intent intent = new Intent(packageContext, cls);
        packageContext.startActivity(intent);
        ((Activity) packageContext).finish();
    }

    public static void startNewActivityWithFinish(Context packageContext, Class<?> cls, Bundle bundle) {
        Intent intent = new Intent(packageContext, cls);
        intent.putExtra(BUNDLE_KEY, bundle);
        packageContext.startActivity(intent);
        ((Activity) packageContext).finish();
    }

    public static Bundle getBundleDataFromPreActivity(Activity activity) {

        Bundle bundle = activity.getIntent().getExtras();

        return bundle;
    }

    public static Bundle getActivityResultBundleDataFromPreActivity(Intent intent) {

        Bundle bundle = intent.getExtras();

        return bundle;
    }

    public static void startNewActivityWithoutFinishForResult(Activity activity, Class<?> cls, int requestCode) {
        Intent intent = new Intent(activity, cls);
        activity.startActivityForResult(intent, requestCode);
    }

    public static void startActivityWithTransitionAnimationWithoutFinish(Activity activity, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                activity.startActivity( intent, ActivityOptions.makeSceneTransitionAnimation( activity ).toBundle());
            }
        }
    }

    public static void startActivityWithTransitionAnimationWithFinish(Activity activity, Class<?> cls) {
        Intent intent = new Intent(activity, cls);
        if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN ) {
            if ( Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ) {
                activity.startActivity( intent, ActivityOptions.makeSceneTransitionAnimation( activity ).toBundle());
                activity.finish();
            }
        }
    }

    private static boolean targetActivityFiringTransitionAnimation(Activity activity) {
        try {
            activity.getWindow().requestFeature( Window.FEATURE_CONTENT_TRANSITIONS );
            return true;
        }
        catch ( Exception e ) {
            return false;
        }
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public static void tagetActivitySetEnterTransitionAnimationExplode(Activity activity) {

        if(targetActivityFiringTransitionAnimation(activity)) {

            activity.getWindow().setEnterTransition( new Explode(  ) );

        }
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public static void tagetActivitySetEnterTransitionAnimationSlide(Activity activity) {

        if(targetActivityFiringTransitionAnimation(activity)) {

            activity.getWindow().setEnterTransition( new Slide(  ) );

        }
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public static void tagetActivitySetEnterTransitionAnimationFade(Activity activity) {

        if(targetActivityFiringTransitionAnimation(activity)) {

            activity.getWindow().setEnterTransition( new Fade(  ) );

        }
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public static void tagetActivitySetExitTransitionAnimationExplode(Activity activity) {

        if(targetActivityFiringTransitionAnimation(activity)) {

            activity.getWindow().setExitTransition( new Explode(  ) );

        }
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public static void tagetActivitySetExitTransitionAnimationSlide(Activity activity) {

        if(targetActivityFiringTransitionAnimation(activity)) {

            activity.getWindow().setExitTransition( new Slide(  ) );

        }
    }

    @TargetApi( Build.VERSION_CODES.LOLLIPOP )
    public static void tagetActivitySetExitTransitionAnimationFade(Activity activity) {

        if(targetActivityFiringTransitionAnimation(activity)) {

            activity.getWindow().setExitTransition( new Fade(  ) );
        }
    }

    public static void addFragment(FragmentActivity activity, int containerViewId, Fragment fragment, boolean withAnimation) {
        if(fragment.isAdded()) {
            return;
        }

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.add(containerViewId, fragment).commit();
    }

    public static void addFragmentWithTAG(FragmentActivity activity, int containerViewId, Fragment fragment, String tag, boolean animation, Fragment rootFragment) {
        if(fragment.isAdded()) {
            return;
        }

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        if(animation) {
            transaction.setCustomAnimations(R.anim.fragment_from_right, R.anim.fragment_out_left, R.anim.fragment_from_right, R.anim.fragment_out_left);
            if(rootFragment != null) {
                rootFragment.getView().startAnimation(AnimationUtils.loadAnimation(activity, R.anim.view_out_litte_left));
            }
        }
        else {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }

        transaction.add(containerViewId, fragment, tag).commitAllowingStateLoss();
    }

    public static void replaceFragmentWithTAG(FragmentActivity activity, int containerViewId, Fragment fragment, String tag) {

        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(containerViewId, fragment, tag).commitAllowingStateLoss();
    }

    public static void removeFragment(FragmentActivity activity, Fragment fragment, boolean animation, Fragment prevFragment) {
        if(fragment == null || !fragment.isAdded()) {
            return;
        }
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        if(animation) {
            transaction.setCustomAnimations(R.anim.fragment_from_right, R.anim.fragment_out_left, R.anim.fragment_from_right, R.anim.fragment_out_left);
            if(prevFragment != null) {
                prevFragment.getView().startAnimation(AnimationUtils.loadAnimation(activity, R.anim.view_from_little_right));
            }
        }
        else {
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        }

        transaction.remove(fragment).commit();
    }

    /**
     * use this when activity
     * @param activity
     * @param fragment
     */
    public static void hideFragment(FragmentActivity activity, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.hide(fragment).commitAllowingStateLoss();
    }

    /**
     * use this when activity
     * @param activity
     * @param fragment
     */
    public static void showFragment(FragmentActivity activity, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.show(fragment).commitAllowingStateLoss();
    }

    public static void detachFragment(FragmentActivity activity, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.detach(fragment).commit();
    }

    public static void attachFragment(FragmentActivity activity, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.attach(fragment).commit();
    }

    public static void addToBackStackFragment(FragmentActivity activity, int containerViewId, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
        transaction.replace(containerViewId, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    /**
     *
     * is activity visible
     *
     */
    private static boolean isForeground(Activity activity) {
        if (activity == null || TextUtils.isEmpty(activity.getClass().getName())) {
            return false;
        }

        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> list = am.getRunningTasks(1);
        if (list != null && list.size() > 0) {
            ComponentName cpn = list.get(0).topActivity;
            if (activity.getClass().getName().equals(cpn.getClassName()))
                return true;
        }
        return false;
    }

}
