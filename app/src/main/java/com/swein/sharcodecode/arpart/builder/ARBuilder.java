package com.swein.sharcodecode.arpart.builder;

import android.content.Context;

import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.rendering.Color;
import com.google.ar.sceneform.rendering.Material;
import com.google.ar.sceneform.rendering.MaterialFactory;
import com.google.ar.sceneform.rendering.ViewRenderable;
import com.swein.sharcodecode.arpart.bean.basic.SegmentBean;

public class ARBuilder {

    public enum ARUnit {
        M, CM
    }

    public enum ARProcess {
        MEASURE_HEIGHT, MEASURE_ROOM, DRAW_WALL_OBJECT
    }

    private static ARBuilder instance = new ARBuilder();
    public static ARBuilder getInstance() {
        return instance;
    }

    private ARBuilder() {}

    // temp guide line
    public SegmentBean guideSegment;

    // point of screen center
    public Node screenCenterNode;

    // build process state
    public ARProcess arProcess = ARProcess.MEASURE_HEIGHT;

    // current unit
    public ARUnit arUnit = ARUnit.M;

    // material
    public Material pointMaterial;
    public Material segmentMaterial;

    public Material objectPointMaterial;
    public Material objectSegmentMaterial;

    public Material centerNodeMaterial;
    public Material guideSegmentMaterial;

    public interface MaterialCreatedDelegate {
        void onCreated(Material material);
    }
    public void createColorMaterial(Context context, int color, MaterialCreatedDelegate materialCreatedDelegate) {
        MaterialFactory
                .makeOpaqueWithColor(context, new Color(color))
                .thenAccept(materialCreatedDelegate::onCreated);
    }

    public interface ViewRenderableCreatedDelegate {
        void onCreated(ViewRenderable viewRenderable);
    }
    public void createViewRenderable(Context context, int layoutResource, ViewRenderableCreatedDelegate viewRenderableCreatedDelegate, boolean shadow) {
        ViewRenderable.builder()
                .setView(context, layoutResource)
                .build()
                .thenAccept(viewRenderable -> {
                    viewRenderable.setShadowCaster(shadow);
                    viewRenderable.setShadowReceiver(shadow);
                    viewRenderableCreatedDelegate.onCreated(viewRenderable);
                });
    }

    public void initMaterial(Context context) {
        createColorMaterial(context, android.graphics.Color.GREEN, material -> {
            pointMaterial = material;
            segmentMaterial = material;
            centerNodeMaterial = material;
            guideSegmentMaterial = material;
        });
    }
}
